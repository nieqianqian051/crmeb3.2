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

namespace app\model\user;

use app\model\store\SystemStoreStaff;
use crmeb\basic\BaseModel;
use crmeb\traits\ModelTrait;
use think\model;

/**
 * Class UserRecharge
 * @package app\model\user
 */
class UserRecharge extends BaseModel
{
    use ModelTrait;

    /**
     * 数据表主键
     * @var string
     */
    protected $pk = 'id';

    /**
     * 模型名称
     * @var string
     */
    protected $name = 'user_recharge';

    protected $insert = ['add_time'];

    protected function setAddTimeAttr()
    {
        return time();
    }

    /**
     * 关联user
     * @return model\relation\HasOne
     */
    public function user()
    {
        return $this->hasOne(User::class, 'uid', 'uid', false)->bind([
            'nickname' => 'nickname',
            'avatar' => 'avatar',
            'phone' => 'phone',
            'now_money' => 'now_money',
            'integral' => 'integral',
            'delete_time' => 'delete_time'
        ]);
    }

    /**
     * @return model\relation\HasOne
     */
    public function staff()
    {
        return $this->hasOne(SystemStoreStaff::class, 'id', 'staff_id')
            ->field(['id', 'uid', 'store_id', 'staff_name'])
            ->bind([
                'staff_uid' => 'uid',
                'staff_store_id' => 'store_id',
                'staff_name' => 'staff_name'
            ]);
    }

    /**
     * 用户uid
     * @param Model $query
     * @param $value
     */
    public function searchUidAttr($query, $value)
    {
        if (is_array($value))
            $query->whereIn('uid', $value);
        else
            $query->where('uid', $value);
    }

    /**
     * 门店ID
     * @param $query
     * @param $value
     */
    public function searchStoreIdAttr($query, $value)
    {
        if ($value !== '') {
            if ($value == -1) {//所有门店
                $query->where('store_id', '>', 0);
            } else {
                $query->where('store_id', $value);
            }
        }
    }

    /**
     * 门店店员ID
     * @param $query
     * @param $value
     */
    public function searchStaffIdAttr($query, $value)
    {
        if ($value) $query->where('staff_id', $value);
    }

    /**
     * 订单号
     * @param Model $query
     * @param $value
     */
    public function searchOrderIdAttr($query, $value)
    {
        $query->where('order_id', $value);
    }

    /**
     * 充值类型
     * @param Model $query
     * @param $value
     */
    public function searchRechargeTypeAttr($query, $value)
    {
        $query->where('recharge_type', $value);
    }

    /**退款金额
     * @param $query
     * @param $value
     */
    public function searchRefundPriceAttr($query, $value)
    {
        $query->where('refund_price', $value);
    }

    /**
     * 是否支付
     * @param Model $query
     * @param $value
     */
    public function searchPaidAttr($query, $value)
    {
        if ($value !== '') $query->where('paid', $value);
    }

    /**
     * 模糊搜索
     * @param Model $query
     * @param $value
     */
    public function searchLikeAttr($query, $value)
    {
        $query->where(function ($query) use ($value) {
            $query->whereLike('uid|order_id', "%" . $value . "%")->whereOr('uid', 'in', function ($query) use ($value) {
                $query->name('user')->whereLike('nickname|real_name|phone', "%" . $value . "%")->field('uid')->select();
            });
        });
    }

    public function searchChannelTypeAttr($query, $value)
    {
        if ($value !== '') $query->where('channel_type', $value);
    }
}
