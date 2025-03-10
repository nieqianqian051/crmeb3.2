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

namespace app\dao\wechat;

use crmeb\basic\BaseModel;
use think\model;
use app\dao\BaseDao;
use app\model\wechat\WechatReply;
use app\model\wechat\WechatKey;

/**
 *
 * Class UserWechatUserDao
 * @package app\dao\user
 */
class WechatReplyKeyDao extends BaseDao
{
    /**
     * 主表别名
     * @var string
     */
    protected string $alias = 'r';

    /**
     * 附表别名
     * @var string
     */
    protected string $joinAlis = 'k';

    /**
     * 设置模型
     * @return string
     */
    protected function setModel(): string
    {
        return WechatReply::class;
    }

    /**
     * 设置join连表模型
     * @return string
     */
    protected function setJoinModel(): string
    {
        return WechatKey::class;
    }

    /**
     * 关联模型
     * @param string $key
     * @param string $join
     * @return BaseModel
     */
    protected function getModel(string $key = 'id', string $join = 'LEFT')
    {
        /** @var WechatKey $keys */
        $keys = app()->make($this->setJoinModel());
        $name = $keys->getName();
        return parent::getModel()->join($name . ' ' . $this->joinAlis, $this->alias . '.' . $key . ' = ' . $this->joinAlis . '.reply_id', $join)->alias($this->alias);
    }

    /**
     * 获取所有关键字
     * @param array $where
     * @param bool $search
     * @return BaseModel
     */
    public function search(array $where = [],bool $search = false)
    {
        return $this->getModel()->when(isset($where['key']) && $where['key'], function ($query) use ($where) {
            $query->where($this->joinAlis . '.keys', 'LIKE', "%$where[key]%");
        })->when(isset($where['type']) && $where['type'], function ($query) use ($where) {
            $query->where($this->alias . '.type', $where['type']);
        })->where($this->joinAlis . '.keys', '<>', 'subscribe')
            ->where($this->joinAlis . '.keys', '<>', 'default');
    }

    /**
     * 获取关键字回复列表
     * @param array $where
     * @param bool $group
     * @param int $page
     * @param int $limit
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getReplyKeyList(array $where, int $page, int $limit)
    {
        return $this->search($where)->page($page, $limit)->group($this->alias . '.id')->field($this->alias . '.*,' . $this->joinAlis . '.keys')->select()->toArray();
    }

    /**
     * 获取条件下的条数
     * @param array $where
     * @param string $field
     * @return int
     */
    public function count(array $where = [], string $field = '*'): int
    {
        return $this->search($where)->group($this->alias . '.id')->count($field);
    }
}
