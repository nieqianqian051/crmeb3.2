package com.maijishop.crmeb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maijishop.crmeb.common.utils.Result;
import com.maijishop.crmeb.entity.Product;
import com.maijishop.crmeb.entity.ProductCategory;
import com.maijishop.crmeb.entity.ProductSku;
import com.maijishop.crmeb.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 商品管理控制器
 *
 * @author maijishop
 */
@Slf4j
@RestController
@RequestMapping("/product")
@Api(tags = "商品管理", description = "商品相关接口")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 分页获取商品列表
     */
    @GetMapping("/list")
    @ApiOperation("分页获取商品列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "每页记录数", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "categoryId", value = "分类ID", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "isShow", value = "是否上架", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "productType", value = "商品类型", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "isHot", value = "是否热门", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "isRecommend", value = "是否推荐", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "isNew", value = "是否新品", dataType = "int", paramType = "query")
    })
    public Result<IPage<Product>> getProductList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "isShow", required = false) Integer isShow,
            @RequestParam(value = "productType", required = false) Integer productType,
            @RequestParam(value = "isHot", required = false) Integer isHot,
            @RequestParam(value = "isRecommend", required = false) Integer isRecommend,
            @RequestParam(value = "isNew", required = false) Integer isNew) {
        
        Page<Product> pageParam = new Page<>(page, limit);
        IPage<Product> result = productService.getProductPage(pageParam, categoryId, keyword, isShow, productType, isHot, isRecommend, isNew);
        
        return Result.success(result);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取商品详情")
    @ApiImplicitParam(name = "id", value = "商品ID", required = true, dataType = "long", paramType = "path")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        
        // 增加商品浏览量
        productService.incrementProductViews(id);
        
        return Result.success(product);
    }

    /**
     * 获取商品SKU列表
     */
    @GetMapping("/sku/list/{productId}")
    @ApiOperation("获取商品SKU列表")
    @ApiImplicitParam(name = "productId", value = "商品ID", required = true, dataType = "long", paramType = "path")
    public Result<List<ProductSku>> getProductSkuList(@PathVariable Long productId) {
        List<ProductSku> skuList = productService.getProductSkuList(productId);
        return Result.success(skuList);
    }

    /**
     * 保存商品信息
     */
    @PostMapping("/save")
    @ApiOperation("保存商品信息")
    public Result<Long> saveProduct(@RequestBody Map<String, Object> params) {
        try {
            Product product = new Product();
            // 这里应该从params中提取商品基本信息并设置到product对象中
            // 提取sku列表
            List<ProductSku> skuList = null;
            // 这里应该从params中提取sku信息并构建skuList
            
            Long productId = productService.saveProduct(product, skuList);
            return Result.success(productId);
        } catch (Exception e) {
            log.error("保存商品失败", e);
            return Result.fail("保存商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品信息
     */
    @PutMapping("/update")
    @ApiOperation("更新商品信息")
    public Result<Boolean> updateProduct(@RequestBody Map<String, Object> params) {
        try {
            Product product = new Product();
            // 这里应该从params中提取商品基本信息并设置到product对象中
            // 提取sku列表
            List<ProductSku> skuList = null;
            // 这里应该从params中提取sku信息并构建skuList
            
            boolean success = productService.updateProduct(product, skuList);
            return success ? Result.success(true) : Result.fail("更新商品失败");
        } catch (Exception e) {
            log.error("更新商品失败", e);
            return Result.fail("更新商品失败: " + e.getMessage());
        }
    }

    /**
     * 更新商品上下架状态
     */
    @PutMapping("/status/{id}/{isShow}")
    @ApiOperation("更新商品上下架状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "商品ID", required = true, dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "isShow", value = "上下架状态：0=下架，1=上架", required = true, dataType = "int", paramType = "path")
    })
    public Result<Boolean> updateProductStatus(@PathVariable Long id, @PathVariable Integer isShow) {
        boolean success = productService.updateProductStatus(id, isShow);
        return success ? Result.success(true) : Result.fail("更新商品状态失败");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除商品")
    @ApiImplicitParam(name = "id", value = "商品ID", required = true, dataType = "long", paramType = "path")
    public Result<Boolean> deleteProduct(@PathVariable Long id) {
        boolean success = productService.deleteProduct(id);
        return success ? Result.success(true) : Result.fail("删除商品失败");
    }

    /**
     * 获取热门商品列表
     */
    @GetMapping("/hot/{limit}")
    @ApiOperation("获取热门商品列表")
    @ApiImplicitParam(name = "limit", value = "返回数量", required = true, dataType = "int", paramType = "path")
    public Result<List<Product>> getHotProducts(@PathVariable Integer limit) {
        List<Product> hotProducts = productService.getHotProducts(limit);
        return Result.success(hotProducts);
    }

    /**
     * 获取推荐商品列表
     */
    @GetMapping("/recommend/{limit}")
    @ApiOperation("获取推荐商品列表")
    @ApiImplicitParam(name = "limit", value = "返回数量", required = true, dataType = "int", paramType = "path")
    public Result<List<Product>> getRecommendProducts(@PathVariable Integer limit) {
        List<Product> recommendProducts = productService.getRecommendProducts(limit);
        return Result.success(recommendProducts);
    }

    /**
     * 获取新品列表
     */
    @GetMapping("/new/{limit}")
    @ApiOperation("获取新品列表")
    @ApiImplicitParam(name = "limit", value = "返回数量", required = true, dataType = "int", paramType = "path")
    public Result<List<Product>> getNewProducts(@PathVariable Integer limit) {
        List<Product> newProducts = productService.getNewProducts(limit);
        return Result.success(newProducts);
    }

    /**
     * 获取商品分类列表
     */
    @GetMapping("/category/list")
    @ApiOperation("获取商品分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pid", value = "父级ID", required = false, dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "isShow", value = "是否显示：0=否，1=是", required = false, dataType = "int", paramType = "query")
    })
    public Result<List<ProductCategory>> getProductCategoryList(
            @RequestParam(value = "pid", required = false, defaultValue = "0") Long pid,
            @RequestParam(value = "isShow", required = false) Integer isShow) {
        List<ProductCategory> categoryList = productService.getProductCategoryList(pid, isShow);
        return Result.success(categoryList);
    }

    /**
     * 获取商品分类树结构
     */
    @GetMapping("/category/tree")
    @ApiOperation("获取商品分类树结构")
    @ApiImplicitParam(name = "isShow", value = "是否显示：0=否，1=是", required = false, dataType = "int", paramType = "query")
    public Result<List<Map<String, Object>>> getProductCategoryTree(
            @RequestParam(value = "isShow", required = false) Integer isShow) {
        List<Map<String, Object>> categoryTree = productService.getProductCategoryTree(isShow);
        return Result.success(categoryTree);
    }
} 