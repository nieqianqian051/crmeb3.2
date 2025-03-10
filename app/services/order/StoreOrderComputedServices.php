<?php
// +----------------------------------------------------------------------
// | CRMEB [ CRMEB赋能开发者，助力企业发展 ]
// +----------------------------------------------------------------------
// | Copyright (c) 2016~2020 https://www.crmeb.com All rights reserved.
// +----------------------------------------------------------------------
// | Licensed CRMEB并不是自由软件，未经许可不能去掉CRMEB相关版权
// +----------------------------------------------------------------------
// | Author: CRMEB Team <admin@crmeb.com>
// +----------------------------------------------------------------------

namespace app\services\order;

use app\services\activity\discounts\StoreDiscountsServices;
use app\services\BaseServices;
use app\dao\order\StoreOrderDao;
use app\services\other\CityAreaServices;
use app\services\pay\PayServices;
use app\services\product\brand\StoreBrandServices;
use app\services\product\category\StoreProductCategoryServices;
use app\services\user\member\MemberCardServices;
use app\services\user\UserServices;
use think\annotation\Inject;
use think\exception\ValidateException;
use app\services\user\UserAddressServices;
use app\services\activity\coupon\StoreCouponUserServices;
use app\services\product\shipping\ShippingTemplatesFreeServices;
use app\services\product\shipping\ShippingTemplatesRegionServices;
use app\services\product\shipping\ShippingTemplatesServices;
use app\services\user\UserBillServices;
use function Swoole\Coroutine\batch;

/**
 * 订单计算金额
 * Class StoreOrderComputedServices
 * @package app\services\order
 * @mixin StoreOrderDao
 */
class StoreOrderComputedServices extends BaseServices
{
    /**
     * 支付类型
     * @var string[]
     */
    public array $payType = ['weixin' => '微信支付', 'yue' => '余额支付', 'offline' => '线下支付', 'pc' => 'pc'];

    /**
     * 额外参数
     * @var array
     */
    protected array $paramData = [];

    /**
     * @var StoreOrderDao
     */
    #[Inject]
    protected StoreOrderDao $dao;

    /**
     * 设置额外参数
     * @param array $paramData
     * @return $this
     */
    public function setParamData(array $paramData)
    {
        $this->paramData = $paramData;
        return $this;
    }

    /**
     * 计算订单金额
     * @param int $uid
     * @param array $userInfo
     * @param array $cartGroup
     * @param int $addressId
     * @param string $payType
     * @param bool $useIntegral
     * @param int $couponId
     * @param int $shippingType
     * @return array
     */
    public function computedOrder(int $uid, array $userInfo, array $cartGroup, int $addressId, string $payType, bool $useIntegral = false, int $couponId = 0, int $shippingType = 1)
    {
        $offlinePayStatus = (int)sys_config('offline_pay_status') ?? (int)2;
        $systemPayType = PayServices::PAY_TYPE;
        if ($offlinePayStatus == 2) unset($systemPayType['offline']);
        if ($payType && !array_key_exists($payType, $systemPayType)) {
            throw new ValidateException('选择支付方式有误');
        }
        if ($uid && !$userInfo) {
            /** @var UserServices $userServices */
            $userServices = app()->make(UserServices::class);
            $userInfo = $userServices->getUserCacheInfo($uid);
            if (!$userInfo) {
                throw new ValidateException('用户不存在!');
            }
        }
        $cartInfo = $cartGroup['cartInfo'];
        $priceGroup = $cartGroup['priceGroup'];
        $deduction = $cartGroup['deduction'];
        $other = $cartGroup['other'];
        $promotions = $other['promotions'] ?? [];
        $payPrice = (float)$priceGroup['totalPrice'];
        $payIntegral = (int)$priceGroup['totalIntegral'] ?? 0;
        $couponPrice = (float)$priceGroup['couponPrice'];
        $firstOrderPrice = (float)$priceGroup['firstOrderPrice'];
        $changePrice = (float)$priceGroup['changePrice'] ?? 0.00;
        $addr = $cartGroup['addr'] ?? [];
        $postage = $priceGroup;
        if (!$addr || $addr['id'] != $addressId) {
            /** @var UserAddressServices $addressServices */
            $addressServices = app()->make(UserAddressServices::class);
            $addr = $addressServices->getAdderssCache($addressId);
            //改变地址重新计算邮费
            $postage = [];
        }
        $type = (int)$deduction['type'] ?? 0;
        $results = batch([
            'promotions' => function () use ($cartInfo, $type) {
                $promotionsPrice = 0;
                if ($type == 8) return $promotionsPrice;
                foreach ($cartInfo as $key => $cart) {
                    if (isset($cart['promotions_true_price']) && isset($cart['price_type']) && $cart['price_type'] == 'promotions') {
                        $promotionsPrice = bcadd((string)$promotionsPrice, (string)bcmul((string)$cart['promotions_true_price'], (string)$cart['cart_num'], 2), 2);
                    }
                }
                return $promotionsPrice;
            },
            'postage' => function () use ($uid, $shippingType, $payType, $cartInfo, $addr, $payPrice, $postage, $other, $type) {
                if ($type == 8) $shippingType = 2;
                return $this->computedPayPostage($uid, $shippingType, $payType, $cartInfo, $addr, $payPrice, $postage, $other);
            },
        ]);

        $promotionsDetail = [];
        if ($promotions) {
            foreach ($promotions as $key => $value) {
                if (isset($value['details']['sum_promotions_price']) && $value['details']['sum_promotions_price']) {
                    $promotionsDetail[] = ['id' => $value['id'], 'name' => $value['name'], 'title' => $value['title'], 'desc' => $value['desc'], 'promotions_price' => $value['details']['sum_promotions_price'], 'promotions_type' => $value['promotions_type']];
                }
            }
            if ($promotionsDetail) {
                $typeArr = array_column($promotionsDetail, 'promotions_type');
                array_multisort($typeArr, SORT_ASC, $promotionsDetail);
            }
        }

        // [$p, $couponPrice] = $results['coupon'];
        [$p, $payPostage, $storePostageDiscount, $storeFreePostage, $isStoreFreePostage] = $results['postage'];
        if ($couponPrice < $payPrice) {//优惠券金额
            $payPrice = bcsub((string)$payPrice, (string)$couponPrice, 2);
        } else {
            $payPrice = 0;
        }
        if ($type == 8) {
            $firstOrderPrice = 0;
            $payPrice = 0;
        }
        if ($firstOrderPrice < $payPrice) {//首单优惠金额
            $payPrice = bcsub((string)$payPrice, (string)$firstOrderPrice, 2);
        } else {
            $payPrice = 0;
        }
        if ($uid && sys_config('integral_ratio_status') && in_array($type, [0, 6])) {
            //使用积分
            [$payPrice, $deductionPrice, $usedIntegral, $SurplusIntegral] = $this->useIntegral($useIntegral, $userInfo, $payPrice, $other);
        }

        $payPrice = (float)bcadd((string)$payPrice, (string)$payPostage, 2);
        foreach ($cartInfo as &$item) {
            $item['invalid'] = false;
            if ($shippingType === 2 && in_array(2, $item['productInfo']['delivery_type'])) {
                $item['invalid'] = true;
            }
        }

        $result = [
            'total_price' => $priceGroup['totalPrice'],
            'pay_price' => max($payPrice, 0),
            'pay_integral' => max($payIntegral, 0),
            'total_postage' => bcadd((string)$payPostage, (string)($storePostageDiscount ?? 0), 2),
            'pay_postage' => $payPostage,
            'first_order_price' => $firstOrderPrice ?? 0,
            'coupon_price' => $couponPrice ?? 0,
            'promotions_price' => $results['promotions'] ?? 0,
            'promotions_detail' => $promotionsDetail,
            'deduction_price' => $deductionPrice ?? 0,
            'usedIntegral' => $usedIntegral ?? 0,
            'SurplusIntegral' => $SurplusIntegral ?? 0,
            'storePostageDiscount' => $storePostageDiscount ?? 0,
            'isStoreFreePostage' => $isStoreFreePostage ?? false,
            'storeFreePostage' => $storeFreePostage ?? 0,
            'change_price' => $changePrice,
            'cartInfo' => $cartInfo
        ];
        $this->paramData = [];
        return $result;
    }

    /**
     * 使用优惠卷
     * @param int $couponId
     * @param int $uid
     * @param $cartInfo
     * @param $payPrice
     */
    public function useCouponId(int $couponId, int $uid, $cartInfo, $payPrice, $promotions)
    {
        //使用优惠劵
        $couponPrice = 0;
        if ($couponId && $cartInfo) {
            /** @var StoreCouponUserServices $couponServices */
            $couponServices = app()->make(StoreCouponUserServices::class);
            $couponInfo = $couponServices->getOne([['id', '=', $couponId], ['uid', '=', $uid], ['is_fail', '=', 0], ['status', '=', 0], ['start_time', '<=', time()], ['end_time', '>=', time()]], '*', ['issue']);
            if (!$couponInfo) {
                throw new ValidateException('选择的优惠劵无效!');
            }
            $type = $couponInfo['applicable_type'] ?? 0;
            $flag = false;
            $price = 0;
            $count = 0;
            $promotionsList = [];
            if ($promotions) {
                $promotionsList = array_combine(array_column($promotions, 'id'), $promotions);
            }
            $isOverlay = function ($cart) use ($promotionsList) {
                $productInfo = $cart['productInfo'] ?? [];
                if (!$productInfo) {
                    return false;
                }
                if (isset($cart['promotions_id']) && $cart['promotions_id']) {
                    foreach ($cart['promotions_id'] as $key => $promotions_id) {
                        $promotions = $promotionsList[$promotions_id] ?? [];
                        if ($promotions && $promotions['promotions_type'] != 4) {
                            $overlay = is_string($promotions['overlay']) ? explode(',', $promotions['overlay']) : $promotions['overlay'];
                            if (!in_array(5, $overlay)) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            };
            switch ($type) {
                case 0:
                    foreach ($cartInfo as $cart) {
                        if (!$isOverlay($cart)) continue;
                        $price = bcadd($price, bcmul((string)$cart['truePrice'], (string)$cart['cart_num'], 2), 2);
                        $count++;
                    }
                    break;
                case 1://品类券
                    /** @var StoreProductCategoryServices $storeCategoryServices */
                    $storeCategoryServices = app()->make(StoreProductCategoryServices::class);
                    $cateGorys = $storeCategoryServices->getAllById((int)$couponInfo['category_id']);
                    if ($cateGorys) {
                        $cateIds = array_column($cateGorys, 'id');
                        foreach ($cartInfo as $cart) {
                            if (!$isOverlay($cart)) continue;
                            if (isset($cart['productInfo']['cate_id']) && array_intersect(explode(',', $cart['productInfo']['cate_id']), $cateIds)) {
                                $price = bcadd($price, bcmul((string)$cart['truePrice'], (string)$cart['cart_num'], 2), 2);
                                $count++;
                            }
                        }
                    }
                    break;
                case 2:
                    foreach ($cartInfo as $cart) {
                        if (!$isOverlay($cart)) continue;
                        if (isset($cart['product_id']) && in_array($cart['product_id'], explode(',', $couponInfo['product_id']))) {
                            $price = bcadd($price, bcmul((string)$cart['truePrice'], (string)$cart['cart_num'], 2), 2);
                            $count++;
                        }
                    }
                    break;
                case 3:
                    /** @var StoreBrandServices $storeBrandServices */
                    $storeBrandServices = app()->make(StoreBrandServices::class);
                    $brands = $storeBrandServices->getAllById((int)$couponInfo['brand_id']);
                    if ($brands) {
                        $brandIds = array_column($brands, 'id');
                        foreach ($cartInfo as $cart) {
                            if (!$isOverlay($cart)) continue;
                            if (isset($cart['productInfo']['brand_id']) && in_array($cart['productInfo']['brand_id'], $brandIds)) {
                                $price = bcadd((string)$price, (string)bcmul((string)$cart['truePrice'], (string)$cart['cart_num'], 2), 2);
                                $count++;
                            }
                        }
                    }
                    break;
            }
            if ($count && $couponInfo['use_min_price'] <= $price) {
                $flag = true;
            }
            if (!$flag) {
                return [$payPrice, 0];
//                throw new ValidateException('不满足优惠劵的使用条件!');
            }
            //满减券
            if ($couponInfo['coupon_type'] == 1) {
                $couponPrice = $couponInfo['coupon_price'];
            } else {
                if ($couponInfo['coupon_price'] <= 0) {//0折
                    $couponPrice = $price;
                } else if ($couponInfo['coupon_price'] >= 100) {
                    $couponPrice = 0;
                } else {
                    $truePrice = (float)bcmul((string)$price, bcdiv((string)$couponInfo['coupon_price'], '100', 2), 2);
                    $couponPrice = (float)bcsub((string)$price, (string)$truePrice, 2);
                }
            }
            if ($couponPrice < $payPrice) {
                $payPrice = (float)bcsub((string)$payPrice, (string)$couponPrice, 2);
            } else {
                $couponPrice = $payPrice;
                $payPrice = 0;
            }
        }
        return [$payPrice, $couponPrice];
    }


    /**
     * 使用积分
     * @param $useIntegral
     * @param $userInfo
     * @param $payPrice
     * @param $other
     * @return array
     */
    public function useIntegral(bool $useIntegral, $userInfo, string $payPrice, array $other)
    {
        $SurplusIntegral = 0;
        $deductionPrice = 0;
        $usedIntegral = 0;
        if ($userInfo && $userInfo['integral'] > 0) {
            /** @var UserBillServices $userBillServices */
            $userBillServices = app()->make(UserBillServices::class);
            // 可用积分
            $usable = bcsub((string)$userInfo['integral'], (string)$userBillServices->getBillSum(['uid' => $userInfo['uid'], 'is_frozen' => 1]), 0);
            $SurplusIntegral = $usable;
            if ($useIntegral && $payPrice && $usable > 0) {
                $integralMaxType = sys_config('integral_max_type', 1);//积分抵用上限类型1：积分、2：订单金额比例
                if ($integralMaxType == 1) {//最多抵用积分
                    $integralMaxNum = sys_config('integral_max_num', 200);
                    if ($integralMaxNum > 0 && $usable > $integralMaxNum) {
                        $integral = $integralMaxNum;
                    } else {
                        $integral = $usable;
                    }
                    $deductionPrice = (float)bcmul((string)$integral, (string)$other['integralRatio'], 2);
                    if ($deductionPrice < $payPrice) {
                        $payPrice = bcsub((string)$payPrice, (string)$deductionPrice, 2);
                        $usedIntegral = $integral;
                    } else {
                        if ($other['integralRatio']) {
                            $deductionPrice = $payPrice;
                            $usedIntegral = (int)ceil(bcdiv((string)$payPrice, (string)$other['integralRatio'], 2));
                        }
                        $payPrice = 0;
                    }
                } else {//最高抵用比率
                    $integralMaxRate = sys_config('integral_max_rate', 0);
                    $deductionPrice = (float)bcmul((string)$usable, (string)$other['integralRatio'], 2);
                    if ($integralMaxRate > 0 && $integralMaxRate <= 100) {
                        $integralMaxPrice = (float)bcmul((string)$payPrice, (string)bcdiv((string)$integralMaxRate, '100', 2), 2);
                    } else {
                        $integralMaxPrice = $payPrice;
                    }
                    $deductionPrice = min($deductionPrice, $integralMaxPrice);
                    $payPrice = bcsub((string)$payPrice, (string)$deductionPrice, 2);
                    $usedIntegral = ceil(bcdiv((string)$deductionPrice, (string)$other['integralRatio'], 2));
                }
                $SurplusIntegral = (int)bcsub((string)$SurplusIntegral, (string)$usedIntegral, 0);
                if ($payPrice <= 0) $payPrice = 0;
            }
        }
        return [$payPrice, $deductionPrice, $usedIntegral, $SurplusIntegral];
    }

    /**
     * 计算邮费
     * @param int $uid
     * @param int $shipping_type
     * @param string $payType
     * @param array $cartInfo
     * @param array $addr
     * @param string $payPrice
     * @param array $postage
     * @param array $other
     * @return array
     * @throws \Throwable
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function computedPayPostage(int $uid, int $shipping_type, string $payType, array $cartInfo, array $addr, string $payPrice, array $postage = [], array $other = [])
    {
        $storePostageDiscount = 0;
        $storeFreePostage = $postage['storeFreePostage'] ?? 0;
        $isStoreFreePostage = false;
        if (!$storeFreePostage) {
            $storeFreePostage = floatval(sys_config('store_free_postage')) ?: 0;//满额包邮金额
        }
        if (!$addr && !isset($addr['id']) || !$cartInfo) {
            $payPostage = 0;
        } else {
            //$shipping_type = 1 快递发货 $shipping_type = 2 门店自提
            if ($shipping_type == 2) {
                if (!sys_config('store_func_status', 1) || !sys_config('store_self_mention', 1)) $shipping_type = 1;
            }
            //门店自提 || （线下支付 && 线下支付包邮） 没有邮费支付
            if ($shipping_type === 2 || ($payType == 'offline' && ((isset($other['offlinePostage']) && $other['offlinePostage']) || sys_config('offline_postage')) == 1)) {
                $payPostage = 0;
            } else {
                if (!$postage || !isset($postage['storePostage']) || !isset($postage['storePostageDiscount'])) {
                    $postage = $this->getOrderPriceGroup($uid, $cartInfo, $addr, $storeFreePostage);
                }
                $payPostage = $postage['storePostage'];
                $storePostageDiscount = $postage['storePostageDiscount'];
                /** @var UserServices $userService */
                $userService = app()->make(UserServices::class);
                //享受svip 运费折扣
                if ($userService->checkUserIsSvip($uid)) {
                    $payPostage = bcsub((string)$payPostage, (string)$storePostageDiscount, 2);
                } else {
                    $storePostageDiscount = 0;
                }
                $isStoreFreePostage = $postage['isStoreFreePostage'] ?? false;

                $payPrice = (float)bcadd((string)$payPrice, (string)$payPostage, 2);
            }
        }
        return [$payPrice, $payPostage, $storePostageDiscount, $storeFreePostage, $isStoreFreePostage];
    }


    /**
     * 运费计算,总金额计算
     * @param int $uid
     * @param $cartInfo
     * @param $addr
     * @param $storeFreePostage
     * @return array
     * @throws \Throwable
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getOrderPriceGroup(int $uid, $cartInfo, $addr, $storeFreePostage = null)
    {
        $storePostage = 0;
        $storePostageDiscount = 0;
        $isStoreFreePostage = false;//是否满额包邮
        if (is_null($storeFreePostage)) {
            $storeFreePostage = floatval(sys_config('store_free_postage')) ?: 0;//满额包邮金额
        }
        $sumPrice = $this->getOrderSumPrice($cartInfo, 'sum_price');//获取订单原总金额
        $totalPrice = $this->getOrderSumPrice($cartInfo, 'truePrice');//获取订单svip、用户等级优惠之后总金额
        $costPrice = $this->getOrderSumPrice($cartInfo, 'costPrice');//获取订单成本价
        $totalIntegral = (int)$this->getOrderSumPrice($cartInfo, 'integral');//获取订单总积分
        $vipPrice = $this->getOrderSumPrice($cartInfo, 'vip_truePrice');//获取订单等级和付费会员总优惠金额
        $levelPrice = $this->getOrderSumPrice($cartInfo, 'level');//获取会员等级优惠
        $memberPrice = $this->getOrderSumPrice($cartInfo, 'member');//获取付费会员优惠
        $changePrice = (float)$this->getOrderSumPrice($cartInfo, 'change_price', false);//获取改价优惠金额


        //如果满额包邮等于0
        $free_shipping = 0;
        $postageArr = [];
        if (isset($cartInfo[0]['productInfo']['product_type']) && in_array($cartInfo[0]['productInfo']['product_type'], [1, 2])) {
            $storePostage = 0;
        } elseif ($cartInfo && $addr) {
            //优惠套餐包邮判断
            if (isset($cartInfo[0]['type']) && $cartInfo[0]['type'] == 5 && isset($cartInfo[0]['activity_id']) && $cartInfo[0]['activity_id']) {
                /** @var StoreDiscountsServices $discountService */
                $discountService = app()->make(StoreDiscountsServices::class);
                $free_shipping = $discountService->value(['id' => $cartInfo[0]['activity_id']], 'free_shipping');
            }
            if ($free_shipping) {
                $storePostage = 0;
            } else if (sys_config('whole_free_shipping') == 1 && $totalPrice >= $storeFreePostage) {//如果总价大于等于满额包邮 邮费等于0
                $isStoreFreePostage = true;
                $storePostage = 0;
            } else {

                // 判断商品包邮和固定运费
                foreach ($cartInfo as &$item) {
                    if (!isset($item['productInfo']['freight'])) continue;
                    if ($item['productInfo']['freight'] == 1) {
                        $item['postage_price'] = 0;
                    } elseif ($item['productInfo']['freight'] == 2) {
                        $item['postage_price'] = bcmul((string)$item['productInfo']['postage'], (string)$item['cart_num'], 2);
                        $storePostage = bcadd((string)$storePostage, (string)$item['postage_price'], 2);
                    }
                }

                //按照运费模板计算每个运费模板下商品的件数/重量/体积以及总金额 按照首重倒序排列
                $cityId = (int)($addr['city_id'] ?? 0);
                $ids = [];
                if ($cityId) {
                    /** @var CityAreaServices $cityAreaServices */
                    $cityAreaServices = app()->make(CityAreaServices::class);
                    $ids = $cityAreaServices->getRelationCityIds($cityId);
                }
                $cityIds = array_merge([0], $ids);

                $tempIds[] = 1;
                foreach ($cartInfo as $key_c => $item_c) {
                    if (isset($item_c['productInfo']['freight']) && $item_c['productInfo']['freight'] == 3) {
                        $tempIds[] = $item_c['productInfo']['temp_id'];
                    }
                }
                $tempIds = array_unique($tempIds);
                /** @var ShippingTemplatesServices $shippServices */
                $shippServices = app()->make(ShippingTemplatesServices::class);
                $temp = $shippServices->getShippingColumnCache(['id' => $tempIds], 'appoint,group', 'id');
                /** @var ShippingTemplatesRegionServices $regionServices */
                $regionServices = app()->make(ShippingTemplatesRegionServices::class);
                $regions = $regionServices->getTempRegionListCache($tempIds, $cityIds);
                $temp_num = [];
                foreach ($cartInfo as $cart) {
                    if (isset($cart['productInfo']['freight']) && in_array($cart['productInfo']['freight'], [1, 2])) {
                        continue;
                    }
                    $tempId = $cart['productInfo']['temp_id'] ?? 1;
                    $group = isset($temp[$tempId]['group']) ? $temp[$tempId]['group'] : $temp[1]['group'];
                    if ($group == 1) {
                        $num = $cart['cart_num'];
                    } elseif ($group == 2) {
                        $num = $cart['cart_num'] * $cart['productInfo']['attrInfo']['weight'];
                    } else {
                        $num = $cart['cart_num'] * $cart['productInfo']['attrInfo']['volume'];
                    }
                    $region = $regions[$tempId] ?? ($regions[1] ?? []);
                    if (!$region) {
                        continue;
                    }
                    if (!isset($temp_num[$tempId])) {
                        $temp_num[$tempId] = [
                            'number' => $num,
                            'group' => $group,
                            'price' => bcmul($cart['cart_num'], $cart['truePrice'], 2),
                            'first' => $region['first'],
                            'first_price' => $region['first_price'],
                            'continue' => $region['continue'],
                            'continue_price' => $region['continue_price'],
                            'temp_id' => $tempId
                        ];
                    } else {
                        $temp_num[$tempId]['number'] += $num;
                        $temp_num[$tempId]['price'] += $cart['pay_price'];
                    }
                }
                if ($temp_num) {
                    /** @var ShippingTemplatesFreeServices $freeServices */
                    $freeServices = app()->make(ShippingTemplatesFreeServices::class);
                    $freeList = $freeServices->isFreeListCache($tempIds, $cityIds);
                    if ($freeList) {
                        foreach ($temp_num as $k => $v) {
                            if (isset($temp[$v['temp_id']]['appoint']) && $temp[$v['temp_id']]['appoint'] && isset($freeList[$v['temp_id']])) {
                                $free = $freeList[$v['temp_id']];
                                $condition = $free['number'] <= $v['number'];
                                if ($free['price'] <= $v['price'] && $condition) {
                                    unset($temp_num[$k]);
                                }
                            }
                        }
                    }
                    //首件运费最大值
                    $maxFirstPrice = $temp_num ? max(array_column($temp_num, 'first_price')) : 0;
                    //初始运费为0
                    $storePostage_arr = [];

                    $i = 0;
                    //循环运费数组
                    foreach ($temp_num as $fk => $fv) {
                        //找到首件运费等于最大值
                        if ($fv['first_price'] == $maxFirstPrice) {
                            //每次循环设置初始值
                            $tempArr = $temp_num;
                            $Postage = 0;
                            //计算首件运费
                            if ($fv['number'] <= $fv['first']) {
                                $Postage = bcadd($Postage, $fv['first_price'], 2);
                            } else {
                                if ($fv['continue'] <= 0) {
                                    $Postage = $Postage;
                                } else {
                                    $Postage = bcadd(bcadd($Postage, $fv['first_price'], 2), bcmul(ceil(bcdiv(bcsub($fv['number'], $fv['first'], 2), $fv['continue'] ?? 0, 2)), $fv['continue_price'], 4), 2);
                                }
                            }
                            $postageArr[$i]['data'][$fk] = $Postage;

                            //删除计算过的首件数据
                            unset($tempArr[$fk]);
                            //循环计算剩余运费
                            foreach ($tempArr as $ck => $cv) {
                                if ($cv['continue'] <= 0) {
                                    $Postage = $Postage;
                                } else {
                                    $one_postage = bcmul(ceil(bcdiv($cv['number'], $cv['continue'] ?? 0, 2)), $cv['continue_price'], 2);
                                    $Postage = bcadd($Postage, $one_postage, 2);
                                    $postageArr[$i]['data'][$ck] = $one_postage;
                                }
                            }
                            $postageArr[$i]['sum'] = $Postage;
                            $storePostage_arr[] = $Postage;
                            $i++;
                        }
                    }
                    $maxStorePostage = $storePostage_arr ? max($storePostage_arr) : 0;
//                //获取运费计算中的最大值
                    $storePostage = bcadd((string)$storePostage, (string)$maxStorePostage, 2);
                }
            }
        }

        //会员邮费享受折扣
        if ($storePostage) {
            //看是否开启会员折扣奖励
            /** @var MemberCardServices $memberCardService */
            $memberCardService = app()->make(MemberCardServices::class);
            $express_rule_number = $memberCardService->isOpenMemberCardCache('express');
            $express_rule_number = $express_rule_number <= 0 ? 0 : $express_rule_number;

            /** @var UserServices $userService */
            $userService = app()->make(UserServices::class);
            $userIsSvip = $userService->checkUserIsSvip($uid);

            $truePostageArr = [];
            foreach ($postageArr as $postageitem) {
                if ($postageitem['sum'] == ($maxStorePostage ?? 0)) {
                    $truePostageArr = $postageitem['data'];
                    break;
                }
            }
            $cartAlready = [];
            foreach ($cartInfo as &$item) {
                if (isset($item['productInfo']['freight']) && in_array($item['productInfo']['freight'], [1, 2])) {
                    if (isset($item['postage_price']) && $item['postage_price'] && $express_rule_number && $express_rule_number < 100 && $userIsSvip) {
                        $item['postage_price'] = bcmul($item['postage_price'], bcdiv($express_rule_number, 100, 4), 2);
                    }
                    continue;
                }
                $tempId = $item['productInfo']['temp_id'] ?? 0;
                $tempPostage = $truePostageArr[$tempId] ?? 0;
                $tempNumber = $temp_num[$tempId]['number'] ?? 0;
                if (!$tempId || !$tempPostage) continue;
                $group = $temp_num[$tempId]['group'];

                if ($group == 1) {
                    $num = $item['cart_num'];
                } elseif ($group == 2) {
                    $num = $item['cart_num'] * $item['productInfo']['attrInfo']['weight'];
                } else {
                    $num = $item['cart_num'] * $item['productInfo']['attrInfo']['volume'];
                }

                if ((($cartAlready[$tempId]['number'] ?? 0) + $num) >= $tempNumber) {
                    $price = isset($cartAlready[$tempId]['price']) ? bcsub((string)$tempPostage, (string)$cartAlready[$tempId]['price'], 6) : $tempPostage;
                } else {
                    $price = bcmul((string)$tempPostage, bcdiv((string)$num, (string)$tempNumber, 6), 6);
                }
                $cartAlready[$tempId]['number'] = bcadd((string)($cartAlready[$tempId]['number'] ?? 0), (string)$num, 4);
                $cartAlready[$tempId]['price'] = bcadd((string)($cartAlready[$tempId]['price'] ?? 0.00), (string)$price, 4);

                if ($express_rule_number && $express_rule_number < 100 && $userIsSvip) {
                    $price = bcmul($price, bcdiv($express_rule_number, 100, 4), 4);
                }
                $price = sprintf("%.2f", $price);
                $item['postage_price'] = $price;
            }
            if ($express_rule_number && $express_rule_number < 100) {
                $payPostage = bcmul($storePostage, bcdiv($express_rule_number, 100, 4), 2);
                $storePostageDiscount = bcsub($storePostage, $payPostage, 2);
            } else {
                $storePostageDiscount = 0;
            }

        }
        return compact('storePostage', 'storeFreePostage', 'isStoreFreePostage', 'sumPrice', 'totalPrice', 'totalIntegral', 'costPrice', 'vipPrice', 'levelPrice', 'memberPrice', 'storePostageDiscount','changePrice', 'cartInfo');
    }

    /**
     * 获取某个字段总金额
     * @param $cartInfo
     * @param string $key
     * @param bool $is_unit
     * @return int|string
     */
    public function getOrderSumPrice($cartInfo, $key = 'truePrice', $is_unit = true)
    {
        $SumPrice = 0;
        foreach ($cartInfo as $cart) {
            if (isset($cart['cart_info'])) $cart = $cart['cart_info'];
            if (isset($cart['is_gift']) && $cart['is_gift']) {
                continue;
            }
            if ($is_unit) {
                if ($key == 'level' || $key == 'member') {
                    if (isset($cart['price_type']) && $cart['price_type'] == $key) {
                        $SumPrice = bcadd($SumPrice, bcmul($cart['cart_num'], $cart['vip_truePrice'], 2), 2);
                    }
                } else {
                    $SumPrice = bcadd($SumPrice, bcmul($cart['cart_num'] ?? 1, $cart[$key] ?? 0, 2), 2);
                }

            } else {
                $SumPrice = bcadd($SumPrice, $cart[$key] ?? 0, 2);
            }
        }
        return $SumPrice;
    }
}
