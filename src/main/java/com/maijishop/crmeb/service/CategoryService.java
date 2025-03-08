package com.maijishop.crmeb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.maijishop.crmeb.entity.ProductCategory;

import java.util.List;
import java.util.Map;

/**
 * 商品分类服务接口
 *
 * @author maijishop
 */
public interface CategoryService extends IService<ProductCategory> {

    /**
     * 获取指定父级ID下的分类列表
     *
     * @param pid    父级ID
     * @param isShow 是否显示
     * @return 分类列表
     */
    List<ProductCategory> getCategoryList(Long pid, Integer isShow);

    /**
     * 获取所有分类列表
     *
     * @param isShow 是否显示
     * @return 所有分类列表
     */
    List<ProductCategory> getAllCategoryList(Integer isShow);

    /**
     * 根据ID获取分类信息
     *
     * @param id 分类ID
     * @return 分类信息
     */
    ProductCategory getCategoryById(Long id);

    /**
     * 保存分类信息
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean saveCategory(ProductCategory category);

    /**
     * 更新分类信息
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean updateCategory(ProductCategory category);

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteCategory(Long id);

    /**
     * 获取分类树结构
     *
     * @param isShow 是否显示
     * @return 分类树结构
     */
    List<Map<String, Object>> getCategoryTree(Integer isShow);
} 