package com.maijishop.crmeb.controller;

import com.maijishop.crmeb.common.utils.Result;
import com.maijishop.crmeb.entity.ProductCategory;
import com.maijishop.crmeb.service.CategoryService;
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
 * 商品分类控制器
 *
 * @author maijishop
 */
@Slf4j
@RestController
@RequestMapping("/category")
@Api(tags = "商品分类管理", description = "商品分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 获取商品分类列表
     */
    @GetMapping("/list")
    @ApiOperation("获取商品分类列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pid", value = "父级ID", required = false, dataType = "long", paramType = "query", defaultValue = "0"),
            @ApiImplicitParam(name = "isShow", value = "是否显示", required = false, dataType = "int", paramType = "query")
    })
    public Result<List<ProductCategory>> getCategoryList(
            @RequestParam(value = "pid", required = false, defaultValue = "0") Long pid,
            @RequestParam(value = "isShow", required = false) Integer isShow) {
        List<ProductCategory> list = categoryService.getCategoryList(pid, isShow);
        return Result.success(list);
    }

    /**
     * 获取所有分类列表
     */
    @GetMapping("/listAll")
    @ApiOperation("获取所有分类列表")
    @ApiImplicitParam(name = "isShow", value = "是否显示", required = false, dataType = "int", paramType = "query")
    public Result<List<ProductCategory>> getAllCategoryList(
            @RequestParam(value = "isShow", required = false) Integer isShow) {
        List<ProductCategory> list = categoryService.getAllCategoryList(isShow);
        return Result.success(list);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/detail/{id}")
    @ApiOperation("获取分类详情")
    @ApiImplicitParam(name = "id", value = "分类ID", required = true, dataType = "long", paramType = "path")
    public Result<ProductCategory> getCategoryDetail(@PathVariable Long id) {
        ProductCategory category = categoryService.getCategoryById(id);
        if (category == null) {
            return Result.fail("分类不存在");
        }
        return Result.success(category);
    }

    /**
     * 获取分类树结构
     */
    @GetMapping("/tree")
    @ApiOperation("获取分类树结构")
    @ApiImplicitParam(name = "isShow", value = "是否显示", required = false, dataType = "int", paramType = "query")
    public Result<List<Map<String, Object>>> getCategoryTree(
            @RequestParam(value = "isShow", required = false) Integer isShow) {
        List<Map<String, Object>> tree = categoryService.getCategoryTree(isShow);
        return Result.success(tree);
    }

    /**
     * 保存分类
     */
    @PostMapping("/save")
    @ApiOperation("保存分类")
    public Result<Boolean> saveCategory(@RequestBody ProductCategory category) {
        try {
            boolean success = categoryService.saveCategory(category);
            return success ? Result.success(true) : Result.fail("保存分类失败");
        } catch (Exception e) {
            log.error("保存分类失败", e);
            return Result.fail("保存分类失败: " + e.getMessage());
        }
    }

    /**
     * 更新分类
     */
    @PutMapping("/update")
    @ApiOperation("更新分类")
    public Result<Boolean> updateCategory(@RequestBody ProductCategory category) {
        try {
            if (category.getId() == null) {
                return Result.fail("分类ID不能为空");
            }
            boolean success = categoryService.updateCategory(category);
            return success ? Result.success(true) : Result.fail("更新分类失败");
        } catch (Exception e) {
            log.error("更新分类失败", e);
            return Result.fail("更新分类失败: " + e.getMessage());
        }
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/delete/{id}")
    @ApiOperation("删除分类")
    @ApiImplicitParam(name = "id", value = "分类ID", required = true, dataType = "long", paramType = "path")
    public Result<Boolean> deleteCategory(@PathVariable Long id) {
        try {
            boolean success = categoryService.deleteCategory(id);
            return success ? Result.success(true) : Result.fail("删除分类失败，可能存在子分类或关联商品");
        } catch (Exception e) {
            log.error("删除分类失败", e);
            return Result.fail("删除分类失败: " + e.getMessage());
        }
    }
} 