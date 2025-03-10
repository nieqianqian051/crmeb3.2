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

namespace app\dao\activity\live;


use app\dao\BaseDao;
use app\model\activity\live\LiveRoom;

/**
 * 直播间
 * Class LiveRoomDao
 * @package app\dao\activity\live
 */
class LiveRoomDao extends BaseDao
{

    /**
     * @return string
     * @author xaboy
     * @day 2020/7/29
     */
    protected function setModel(): string
    {
        return LiveRoom::class;
    }


    /**
     * @param array $where
     * @param string $field
     * @param array $with
     * @param int $page
     * @param int $limit
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getList(array $where, string $field = '*', array $with = [], int $page = 0, int $limit = 0)
    {
        return $this->search($where)->field($field)->with($with)
        	->when($page && $limit, function ($query) use ($page, $limit) {
                $query->page($page, $limit);
            })->when(!$page && $limit, function ($query) use ($page, $limit) {
                $query->limit($limit);
            })->order('sort desc,id desc')->select()->toArray();
    }

    /**
     * @param $roomId
     * @return array|\think\Model|null
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function validRoom($roomId)
    {
        return $this->getModel()->where('id', $roomId)->where('status', 'IN', [0, 2])->where('is_del', 0)->find();
    }

    public function getRooms(array $roomIds)
    {
        return $this->search(['room_id' => $roomIds])->column('live_status,id', 'room_id');
    }
}
