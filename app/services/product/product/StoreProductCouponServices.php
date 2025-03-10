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
declare (strict_types=1);

namespace app\services\product\product;

use app\services\BaseServices;
use app\dao\product\product\StoreProductCouponDao;
use app\services\activity\coupon\StoreCouponIssueServices;
use app\services\order\StoreOrderCartInfoServices;
use app\services\order\StoreOrderServices;
use app\services\user\UserServices;
use crmeb\exceptions\AdminException;
use crmeb\services\CacheService;
use think\annotation\Inject;
use think\exception\ValidateException;

/**
 *
 * Class StoreProductCouponServices
 * @package app\services\coupon
 * @mixin StoreProductCouponDao
 */
class StoreProductCouponServices extends BaseServices
{
    /**
     * @var StoreProductCouponDao
     */
    #[Inject]
    protected StoreProductCouponDao $dao;

    /**
     * 商品关联优惠券
     * @param int $id
     * @param array $coupon_ids
     * @return bool
     */
    public function setCoupon(int $id, array $coupon_ids)
    {
        $this->dao->delete(['product_id' => $id]);
        if ($coupon_ids) {
            $data = $data_all = [];
            $data['product_id'] = $id;
            $data['add_time'] = time();
            foreach ($coupon_ids as $cid) {
                if (!empty($cid) && (int)$cid) {
                    $data['issue_coupon_id'] = $cid;
                    $data_all[] = $data;
                }
            }
            $res = true;
            if ($data_all) {
                $res = $this->dao->saveAll($data_all);
            }
            if (!$res) throw new AdminException('关联优惠券失败！');
        }
        return true;
    }

    /**
 	* 获取下单赠送优惠券
	* @param int $uid
	* @param string $orderId
	* @param $order
	* @return array|mixed
	*/
    public function getOrderProductCoupon(int $uid, string $orderId, $order = [])
    {
		if (!$uid || !$orderId) {
			return [];
		}
		if (!$order) {
			/** @var StoreOrderServices $storeOrder */
			$storeOrder = app()->make(StoreOrderServices::class);
			$order = $storeOrder->getOne(['order_id' => $orderId]);
		}
        if (!$order || $order['uid'] != $uid) {
            throw new ValidateException('订单不存在');
        }
        $key = 'order_product_coupon_' . $uid . '_' . $order['id'];
        return CacheService::redisHandler()->get($key, []);
    }

    /**
     * 下单赠送优惠劵
     * @param int $uid
     * @param int $orderId
     * @return bool
     */
    public function giveOrderProductCoupon(int $uid, int $orderId)
    {
        /** @var UserServices $userServices */
        $userServices = app()->make(UserServices::class);
        $user = $userServices->getUserInfo($uid);
        if (!$user) {
            throw new ValidateException('用户不存在');
        }
        /** @var StoreOrderServices $storeOrder */
        $storeOrder = app()->make(StoreOrderServices::class);
        $order = $storeOrder->getOne(['id' => $orderId]);
        if (!$order || $order['uid'] != $uid) {
            throw new ValidateException('订单不存在');
        }
        /** @var StoreOrderCartInfoServices $storeOrderCartInfo */
        $storeOrderCartInfo = app()->make(StoreOrderCartInfoServices::class);
        $productIds = $storeOrderCartInfo->getColumn(['oid' => $order['id']], 'product_id');
        $list = [];
        if ($productIds) {
            $couponList = $this->dao->getProductCoupon($productIds);
            if ($couponList) {
                /** @var StoreCouponIssueServices $storeCoupon */
                $storeCoupon = app()->make(StoreCouponIssueServices::class);
                $list = $storeCoupon->orderPayGiveCoupon($uid, array_column((array)$couponList, 'issue_coupon_id'));
                if ($list) {
                    $ids = array_column($list, 'cid');
                    $coupons = $storeCoupon->getColumn(['id' => $ids], 'id,type,coupon_type,product_id,category_id,brand_id', 'id');
                    foreach ($list as &$item) {
						$item['applicable_type'] = $coupons[$item['cid']]['type'] ?? 1;
                        $item['coupon_type'] = $coupons[$item['cid']]['coupon_type'] ?? 1;
						$item['product_id'] = $coupons[$item['cid']]['product_id'] ?? 1;
						$item['category_id'] = $coupons[$item['cid']]['category_id'] ?? 1;
						$item['brand_id'] = $coupons[$item['cid']]['brand_id'] ?? 1;
                        $item['add_time'] = date('Y-m-d', $item['add_time']);
                        $item['end_time'] = date('Y-m-d', $item['end_time']);
                    }
                }
            }
        }
        $key = 'order_product_coupon_' . $uid . '_' . $orderId;
        CacheService::redisHandler()->set($key, $list, 7200);
        return true;
    }
}
