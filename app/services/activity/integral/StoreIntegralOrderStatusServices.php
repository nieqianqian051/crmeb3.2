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

namespace app\services\activity\integral;


use app\dao\activity\integral\StoreIntegralOrderStatusDao;
use app\services\BaseServices;
use think\annotation\Inject;

/**
 * 订单状态
 * Class StoreIntegralOrderStatusServices
 * @package app\services\activity\integral
 * @mixin StoreIntegralOrderStatusDao
 */
class StoreIntegralOrderStatusServices extends BaseServices
{

	/**
	* @var StoreIntegralOrderStatusDao
	*/
	#[Inject]
	protected StoreIntegralOrderStatusDao $dao;

    /**
     * 订单状态分页
     * @param array $where
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getStatusList(array $where)
    {
        [$page, $limit] = $this->getPageValue();
        $list = $this->dao->getStatusList($where, $page, $limit);
        foreach ($list as &$item) {
            if (is_int($item['change_time'])) $item['change_time'] = date('Y-m-d H:i:s', $item['change_time']);
        }
        $count = $this->dao->count($where);
        return compact('list', 'count');
    }

}
