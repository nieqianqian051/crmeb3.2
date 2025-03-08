package com.maijishop.crmeb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maijishop.crmeb.entity.Product;
import com.maijishop.crmeb.entity.ProductCategory;
import com.maijishop.crmeb.entity.ProductSku;

import java.util.List;
import java.util.Map;

/**
 * 商品服务接口
 *
 * @author maijishop
 */
public interface ProductService extends IService<Product> {

    /**
     * 商品分页列表查询
     *
     * @param page        分页参数
     * @param categoryId  分类ID
     * @param keyword     关键字
     * @param isShow      是否上架
     * @param productType 商品类型
     * @param isHot       是否热门
     * @param isRecommend 是否推荐
     * @param isNew       是否新品
     * @return 商品分页列表
     */
    IPage<Product> getProductPage(Page<Product> page, Long categoryId, String keyword, Integer isShow, Integer productType, Integer isHot, Integer isRecommend, Integer isNew);

    /**
     * 根据ID获取商品信息
     *
     * @param id 商品ID
     * @return 商品信息
     */
    Product getProductById(Long id);

    /**
     * 保存商品信息
     *
     * @param product 商品信息
     * @param skuList 商品规格列表
     * @return 商品ID
     */
    Long saveProduct(Product product, List<ProductSku> skuList);

    /**
     * 更新商品信息
     *
     * @param product 商品信息
     * @param skuList 商品规格列表
     * @return 是否成功
     */
    boolean updateProduct(Product product, List<ProductSku> skuList);

    /**
     * 更新商品上下架状态
     *
     * @param id     商品ID
     * @param isShow 上下架状态：0=下架，1=上架
     * @return 是否成功
     */
    boolean updateProductStatus(Long id, Integer isShow);

    /**
     * 删除商品
     *
     * @param id 商品ID
     * @return 是否成功
     */
    boolean deleteProduct(Long id);

    /**
     * 获取商品SKU列表
     *
     * @param productId 商品ID
     * @return SKU列表
     */
    List<ProductSku> getProductSkuList(Long productId);

    /**
     * 根据ID获取商品SKU信息
     *
     * @param id SKU ID
     * @return SKU信息
     */
    ProductSku getProductSkuById(Long id);

    /**
     * 减少商品库存
     *
     * @param productId   商品ID
     * @param productSkuId 商品SKU ID
     * @param quantity     数量
     * @return 是否成功
     */
    boolean decreaseStock(Long productId, Long productSkuId, Integer quantity);

    /**
     * 增加商品库存
     *
     * @param productId   商品ID
     * @param productSkuId 商品SKU ID
     * @param quantity     数量
     * @return 是否成功
     */
    boolean increaseStock(Long productId, Long productSkuId, Integer quantity);

    /**
     * 增加商品浏览量
     *
     * @param id 商品ID
     * @return 是否成功
     */
    boolean incrementProductViews(Long id);

    /**
     * 获取热门商品列表
     *
     * @param limit 数量限制
     * @return 热门商品列表
     */
    List<Product> getHotProducts(Integer limit);

    /**
     * 获取推荐商品列表
     *
     * @param limit 数量限制
     * @return 推荐商品列表
     */
    List<Product> getRecommendProducts(Integer limit);

    /**
     * 获取新品列表
     *
     * @param limit 数量限制
     * @return 新品列表
     */
    List<Product> getNewProducts(Integer limit);

    /**
     * 保存商品分类
     *
     * @param category 分类信息
     * @return 分类ID
     */
    Long saveProductCategory(ProductCategory category);

    /**
     * 更新商品分类
     *
     * @param category 分类信息
     * @return 是否成功
     */
    boolean updateProductCategory(ProductCategory category);

    /**
     * 删除商品分类
     *
     * @param id 分类ID
     * @return 是否成功
     */
    boolean deleteProductCategory(Long id);

    /**
     * 获取商品分类列表
     *
     * @param pid   父级ID
     * @param isShow 是否显示：0=否，1=是
     * @return 分类列表
     */
    List<ProductCategory> getProductCategoryList(Long pid, Integer isShow);

    /**
     * 获取商品分类树结构
     *
     * @param isShow 是否显示：0=否，1=是
     * @return 分类树结构
     */
    List<Map<String, Object>> getProductCategoryTree(Integer isShow);
} 