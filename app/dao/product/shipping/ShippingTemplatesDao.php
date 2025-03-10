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

namespace app\dao\product\shipping;

use app\dao\BaseDao;
use app\model\product\shipping\ShippingTemplates;

/**
 * 运费模版
 * Class ShippingTemplatesDao
 * @package app\dao\product\shipping
 */
class ShippingTemplatesDao extends BaseDao
{

    /**
     * 设置模型
     * @return string
     */
    protected function setModel(): string
    {
        return ShippingTemplates::class;
    }

	/**
 	* 获取运费模版
	* @param int $type
	* @param int $relation_id
	* @return array
	*/
	public function getTemp(int $type = 0, int $relation_id = 0)
    {
        return $this->search(['type' => $type, 'relation_id' => $relation_id])->order('sort DESC,id DESC')->column('id,name');
    }

    /**
	 * 获取选择模板列表
	 * @param array $where
	 * @return array
	 */
    public function getSelectList(array $where = [])
    {
        return $this->search($where)->order('sort DESC,id DESC')->column('id,name');
    }

    /**
     * 获取
     * @param array $where
     * @param int $page
     * @param int $limit
     * @return array
     * @throws \think\db\exception\DataNotFoundException
     * @throws \think\db\exception\DbException
     * @throws \think\db\exception\ModelNotFoundException
     */
    public function getShippingList(array $where, int $page, int $limit)
    {
        return $this->search($where)->order('sort DESC,id DESC')->page($page, $limit)->select()->toArray();
    }

    /**
     * 插入数据返回主键id
     * @param array $data
     * @return int|string
     */
    public function insertGetId(array $data)
    {
        return $this->getModel()->insertGetId($data);
    }

    /**
     * 获取运费模板指定条件下的数据
     * @param array $where
     * @param string $field
     * @param string $key
     * @return array
     */
    public function getShippingColumn(array $where, string $field, string $key)
    {
        return $this->search($where)->column($field, $key);
    }
}
