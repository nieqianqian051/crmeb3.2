package com.maijishop.crmeb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maijishop.crmeb.entity.ProductCategory;
import com.maijishop.crmeb.mapper.ProductCategoryMapper;
import com.maijishop.crmeb.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 *
 * @author maijishop
 */
@Slf4j
@Service
public class CategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements CategoryService {

    @Resource
    private ProductCategoryMapper productCategoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String CATEGORY_CACHE_KEY = "product:category:";
    private static final String CATEGORY_TREE_CACHE_KEY = "product:category:tree";

    @Override
    public List<ProductCategory> getCategoryList(Long pid, Integer isShow) {
        String cacheKey = CATEGORY_CACHE_KEY + "pid:" + pid;
        if (isShow != null) {
            cacheKey += ":show:" + isShow;
        }

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ProductCategory> cacheList = (List<ProductCategory>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheList != null) {
            return cacheList;
        }

        // 从数据库获取
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCategory::getPid, pid)
                .eq(ProductCategory::getIsDel, 0);
        
        if (isShow != null) {
            queryWrapper.eq(ProductCategory::getIsShow, isShow);
        }
        
        queryWrapper.orderByDesc(ProductCategory::getSort);
        
        List<ProductCategory> categoryList = productCategoryMapper.selectList(queryWrapper);

        // 存入缓存
        if (!categoryList.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, categoryList, 1, TimeUnit.HOURS);
        }

        return categoryList;
    }

    @Override
    public List<ProductCategory> getAllCategoryList(Integer isShow) {
        String cacheKey = CATEGORY_CACHE_KEY + "all";
        if (isShow != null) {
            cacheKey += ":show:" + isShow;
        }

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<ProductCategory> cacheList = (List<ProductCategory>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheList != null) {
            return cacheList;
        }

        // 从数据库获取
        LambdaQueryWrapper<ProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductCategory::getIsDel, 0);
        
        if (isShow != null) {
            queryWrapper.eq(ProductCategory::getIsShow, isShow);
        }
        
        queryWrapper.orderByAsc(ProductCategory::getLevel)
                .orderByDesc(ProductCategory::getSort);
        
        List<ProductCategory> categoryList = productCategoryMapper.selectList(queryWrapper);

        // 存入缓存
        if (!categoryList.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, categoryList, 1, TimeUnit.HOURS);
        }

        return categoryList;
    }

    @Override
    public ProductCategory getCategoryById(Long id) {
        if (id == null) {
            return null;
        }

        String cacheKey = CATEGORY_CACHE_KEY + id;

        // 尝试从缓存获取
        ProductCategory category = (ProductCategory) redisTemplate.opsForValue().get(cacheKey);
        if (category != null) {
            return category;
        }

        // 从数据库获取
        category = productCategoryMapper.selectById(id);

        // 存入缓存
        if (category != null) {
            redisTemplate.opsForValue().set(cacheKey, category, 1, TimeUnit.HOURS);
        }

        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCategory(ProductCategory category) {
        if (category == null) {
            return false;
        }

        // 设置创建时间和更新时间
        Date now = new Date();
        if (category.getCreateTime() == null) {
            category.setCreateTime(now);
        }
        category.setUpdateTime(now);

        // 设置默认属性
        if (category.getIsDel() == null) {
            category.setIsDel(0);
        }

        // 设置层级
        if (category.getPid() == null || category.getPid() == 0) {
            category.setPid(0L);
            category.setLevel(1); // 一级分类
        } else {
            // 获取父级分类
            ProductCategory parentCategory = getCategoryById(category.getPid());
            if (parentCategory != null) {
                category.setLevel(parentCategory.getLevel() + 1);
            } else {
                category.setLevel(1);
            }
        }

        boolean result = save(category);

        // 清除相关缓存
        cleanCategoryCache(category);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(ProductCategory category) {
        if (category == null || category.getId() == null) {
            return false;
        }

        // 设置更新时间
        category.setUpdateTime(new Date());

        // 如果修改了父级ID，需要更新层级
        if (category.getPid() != null) {
            ProductCategory oldCategory = getCategoryById(category.getId());
            if (oldCategory != null && !oldCategory.getPid().equals(category.getPid())) {
                if (category.getPid() == 0) {
                    category.setLevel(1); // 一级分类
                } else {
                    // 获取父级分类
                    ProductCategory parentCategory = getCategoryById(category.getPid());
                    if (parentCategory != null) {
                        category.setLevel(parentCategory.getLevel() + 1);
                    }
                }
            }
        }

        boolean result = updateById(category);

        // 清除相关缓存
        cleanCategoryCache(category);

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long id) {
        if (id == null) {
            return false;
        }

        // 检查是否有子分类
        Integer childCount = productCategoryMapper.hasChildren(id);
        if (childCount != null && childCount > 0) {
            log.warn("分类存在子分类，无法删除, id={}", id);
            return false;
        }

        // 检查分类下是否有商品
        Integer productCount = productCategoryMapper.hasProducts(id);
        if (productCount != null && productCount > 0) {
            log.warn("分类下存在商品，无法删除, id={}", id);
            return false;
        }

        // 获取分类信息（用于后续清理缓存）
        ProductCategory category = getCategoryById(id);

        // 逻辑删除
        ProductCategory updateEntity = new ProductCategory();
        updateEntity.setId(id);
        updateEntity.setIsDel(1);
        updateEntity.setUpdateTime(new Date());
        
        boolean result = updateById(updateEntity);

        // 清除相关缓存
        if (category != null) {
            cleanCategoryCache(category);
        } else {
            // 如果没有获取到分类信息，清除所有分类缓存
            Set<String> keys = redisTemplate.keys(CATEGORY_CACHE_KEY + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> getCategoryTree(Integer isShow) {
        String cacheKey = CATEGORY_TREE_CACHE_KEY;
        if (isShow != null) {
            cacheKey += ":show:" + isShow;
        }

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cacheTree = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cacheTree != null) {
            return cacheTree;
        }

        // 获取所有分类
        List<ProductCategory> allCategories = getAllCategoryList(isShow);
        
        // 构建树结构
        List<Map<String, Object>> tree = buildCategoryTree(allCategories, 0L);

        // 存入缓存
        if (!tree.isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, tree, 1, TimeUnit.HOURS);
        }

        return tree;
    }

    /**
     * 构建分类树结构
     *
     * @param categories 所有分类列表
     * @param pid 父级ID
     * @return 树结构
     */
    private List<Map<String, Object>> buildCategoryTree(List<ProductCategory> categories, Long pid) {
        return categories.stream()
                .filter(category -> pid.equals(category.getPid()))
                .map(category -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", category.getId());
                    map.put("name", category.getName());
                    map.put("pid", category.getPid());
                    map.put("icon", category.getIcon());
                    map.put("sort", category.getSort());
                    map.put("isShow", category.getIsShow());
                    map.put("level", category.getLevel());
                    
                    // 递归获取子分类
                    List<Map<String, Object>> children = buildCategoryTree(categories, category.getId());
                    if (!children.isEmpty()) {
                        map.put("children", children);
                    }
                    
                    return map;
                })
                .sorted(Comparator.comparing(o -> -(Integer) o.get("sort")))
                .collect(Collectors.toList());
    }

    /**
     * 清除分类相关缓存
     *
     * @param category 分类
     */
    private void cleanCategoryCache(ProductCategory category) {
        if (category == null) {
            return;
        }

        // 清除该分类的缓存
        redisTemplate.delete(CATEGORY_CACHE_KEY + category.getId());

        // 清除该分类所在父级的子分类列表缓存
        String pidCacheKey = CATEGORY_CACHE_KEY + "pid:" + category.getPid();
        redisTemplate.delete(pidCacheKey);
        redisTemplate.delete(pidCacheKey + ":show:0");
        redisTemplate.delete(pidCacheKey + ":show:1");

        // 清除所有分类列表缓存
        redisTemplate.delete(CATEGORY_CACHE_KEY + "all");
        redisTemplate.delete(CATEGORY_CACHE_KEY + "all:show:0");
        redisTemplate.delete(CATEGORY_CACHE_KEY + "all:show:1");

        // 清除分类树缓存
        redisTemplate.delete(CATEGORY_TREE_CACHE_KEY);
        redisTemplate.delete(CATEGORY_TREE_CACHE_KEY + ":show:0");
        redisTemplate.delete(CATEGORY_TREE_CACHE_KEY + ":show:1");
    }
} 