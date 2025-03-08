package com.maijishop.crmeb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maijishop.crmeb.entity.Product;
import com.maijishop.crmeb.entity.ProductCategory;
import com.maijishop.crmeb.entity.ProductSku;
import com.maijishop.crmeb.mapper.ProductMapper;
import com.maijishop.crmeb.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author maijishop
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Resource
    private ProductMapper productMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    // Redis中商品相关的key前缀
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final String PRODUCT_VIEWS_KEY = PRODUCT_KEY_PREFIX + "views:";
    private static final String HOT_PRODUCTS_KEY = PRODUCT_KEY_PREFIX + "hot";
    private static final String RECOMMEND_PRODUCTS_KEY = PRODUCT_KEY_PREFIX + "recommend";
    private static final String NEW_PRODUCTS_KEY = PRODUCT_KEY_PREFIX + "new";

    @Override
    public IPage<Product> getProductPage(Page<Product> page, Long categoryId, String keyword, Integer isShow, 
                                        Integer productType, Integer isHot, Integer isRecommend, Integer isNew) {
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        
        // 构建查询条件
        if (categoryId != null && categoryId > 0) {
            queryWrapper.eq(Product::getCategoryId, categoryId);
        }
        
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Product::getProductName, keyword)
                    .or()
                    .like(Product::getKeyword, keyword)
                    .or()
                    .like(Product::getProductDesc, keyword)
            );
        }
        
        if (isShow != null) {
            queryWrapper.eq(Product::getIsShow, isShow);
        }
        
        if (productType != null) {
            queryWrapper.eq(Product::getProductType, productType);
        }
        
        if (isHot != null) {
            queryWrapper.eq(Product::getIsHot, isHot);
        }
        
        if (isRecommend != null) {
            queryWrapper.eq(Product::getIsRecommend, isRecommend);
        }
        
        if (isNew != null) {
            queryWrapper.eq(Product::getIsNew, isNew);
        }
        
        // 按更新时间降序排序
        queryWrapper.orderByDesc(Product::getUpdateTime);
        
        return productMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Product getProductById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        
        // 尝试从缓存获取
        String cacheKey = PRODUCT_KEY_PREFIX + id;
        Product product = (Product) redisTemplate.opsForValue().get(cacheKey);
        
        if (product != null) {
            return product;
        }
        
        // 缓存未命中，从数据库获取
        product = productMapper.selectById(id);
        
        if (product != null) {
            // 存入缓存，设置过期时间为1小时
            redisTemplate.opsForValue().set(cacheKey, product, 1, java.util.concurrent.TimeUnit.HOURS);
        }
        
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveProduct(Product product, List<ProductSku> skuList) {
        if (product == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        
        // 设置创建和更新时间
        Date now = new Date();
        product.setCreateTime(now);
        product.setUpdateTime(now);
        
        // 保存商品基本信息
        productMapper.insert(product);
        Long productId = product.getId();
        
        // 处理SKU信息
        if (skuList != null && !skuList.isEmpty()) {
            for (ProductSku sku : skuList) {
                sku.setProductId(productId);
                sku.setCreateTime(now);
                sku.setUpdateTime(now);
                // 通过ProductSkuMapper插入SKU信息
                // 这里假设已经有了productSkuMapper，实际中需要添加这个依赖
                // productSkuMapper.insert(sku);
            }
        }
        
        // 清除相关缓存
        clearProductCache(productId);
        
        return productId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateProduct(Product product, List<ProductSku> skuList) {
        if (product == null || product.getId() == null) {
            return false;
        }
        
        Long productId = product.getId();
        Date now = new Date();
        product.setUpdateTime(now);
        
        // 更新商品基本信息
        int result = productMapper.updateById(product);
        
        // 处理SKU信息
        if (skuList != null && !skuList.isEmpty()) {
            // 先删除原有SKU
            // productSkuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
            
            // 添加新的SKU信息
            for (ProductSku sku : skuList) {
                sku.setProductId(productId);
                sku.setUpdateTime(now);
                if (sku.getId() == null) {
                    sku.setCreateTime(now);
                    // productSkuMapper.insert(sku);
                } else {
                    // productSkuMapper.updateById(sku);
                }
            }
        }
        
        // 清除相关缓存
        clearProductCache(productId);
        
        return result > 0;
    }

    @Override
    public boolean updateProductStatus(Long id, Integer isShow) {
        if (id == null || id <= 0 || isShow == null) {
            return false;
        }
        
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, id)
                .set(Product::getIsShow, isShow)
                .set(Product::getUpdateTime, new Date());
        
        int result = productMapper.update(null, updateWrapper);
        
        // 清除相关缓存
        clearProductCache(id);
        
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteProduct(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        // 首先删除相关的SKU
        // productSkuMapper.delete(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, id));
        
        // 删除商品
        int result = productMapper.deleteById(id);
        
        // 清除相关缓存
        clearProductCache(id);
        
        return result > 0;
    }

    @Override
    public List<ProductSku> getProductSkuList(Long productId) {
        if (productId == null || productId <= 0) {
            return Collections.emptyList();
        }
        
        // 这里需要ProductSkuMapper的支持
        // return productSkuMapper.selectList(new LambdaQueryWrapper<ProductSku>().eq(ProductSku::getProductId, productId));
        
        // 暂时返回空列表
        return Collections.emptyList();
    }

    @Override
    public ProductSku getProductSkuById(Long id) {
        if (id == null || id <= 0) {
            return null;
        }
        
        // 需要ProductSkuMapper的支持
        // return productSkuMapper.selectById(id);
        
        // 暂时返回null
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long productId, Long productSkuId, Integer quantity) {
        if (productId == null || productId <= 0 || quantity == null || quantity <= 0) {
            return false;
        }
        
        // 锁定商品
        String lockKey = "product_stock_lock:" + productId;
        try {
            // 获取锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, java.util.concurrent.TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                // 减少商品库存
                int result = productMapper.decreaseStock(productId, quantity);
                
                // 减少SKU库存
                if (productSkuId != null && productSkuId > 0) {
                    // 需要ProductSkuMapper的支持
                    // productSkuMapper.decreaseStock(productSkuId, quantity);
                }
                
                // 清除相关缓存
                clearProductCache(productId);
                
                return result > 0;
            } else {
                log.warn("获取库存锁失败, productId={}, skuId={}", productId, productSkuId);
                return false;
            }
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean increaseStock(Long productId, Long productSkuId, Integer quantity) {
        if (productId == null || productId <= 0 || quantity == null || quantity <= 0) {
            return false;
        }
        
        // 增加商品库存
        int result = productMapper.increaseStock(productId, quantity);
        
        // 增加SKU库存
        if (productSkuId != null && productSkuId > 0) {
            // 需要ProductSkuMapper的支持
            // productSkuMapper.increaseStock(productSkuId, quantity);
        }
        
        // 清除相关缓存
        clearProductCache(productId);
        
        return result > 0;
    }

    @Override
    public boolean incrementProductViews(Long id) {
        if (id == null || id <= 0) {
            return false;
        }
        
        // 使用Redis来统计商品浏览量
        String key = PRODUCT_VIEWS_KEY + id;
        redisTemplate.opsForValue().increment(key, 1);
        
        // 异步更新到数据库，这里简单处理
        // 实际中可以使用定时任务将Redis中的浏览量同步到数据库
        try {
            Long views = (Long) redisTemplate.opsForValue().get(key);
            if (views != null && views > 0 && views % 10 == 0) { // 每增加10次才更新数据库
                LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(Product::getId, id)
                        .set(Product::getViews, views);
                productMapper.update(null, updateWrapper);
            }
        } catch (Exception e) {
            log.error("更新商品浏览量失败, id={}", id, e);
            return false;
        }
        
        return true;
    }

    @Override
    public List<Product> getHotProducts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10条
        }
        
        // 尝试从缓存获取
        List<Object> cachedList = redisTemplate.opsForList().range(HOT_PRODUCTS_KEY, 0, limit - 1);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList.stream()
                    .map(obj -> (Product) obj)
                    .collect(Collectors.toList());
        }
        
        // 缓存未命中，从数据库获取
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getIsShow, 1)
                .eq(Product::getIsHot, 1)
                .orderByDesc(Product::getSales, Product::getViews)
                .last("LIMIT " + limit);
        
        List<Product> hotProducts = productMapper.selectList(queryWrapper);
        
        // 存入缓存，设置过期时间为1小时
        if (!hotProducts.isEmpty()) {
            // 先清除原有缓存
            redisTemplate.delete(HOT_PRODUCTS_KEY);
            // 添加到缓存
            redisTemplate.opsForList().rightPushAll(HOT_PRODUCTS_KEY, hotProducts.toArray());
            redisTemplate.expire(HOT_PRODUCTS_KEY, 1, java.util.concurrent.TimeUnit.HOURS);
        }
        
        return hotProducts;
    }

    @Override
    public List<Product> getRecommendProducts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10条
        }
        
        // 尝试从缓存获取
        List<Object> cachedList = redisTemplate.opsForList().range(RECOMMEND_PRODUCTS_KEY, 0, limit - 1);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList.stream()
                    .map(obj -> (Product) obj)
                    .collect(Collectors.toList());
        }
        
        // 缓存未命中，从数据库获取
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getIsShow, 1)
                .eq(Product::getIsRecommend, 1)
                .orderByDesc(Product::getSales, Product::getViews)
                .last("LIMIT " + limit);
        
        List<Product> recommendProducts = productMapper.selectList(queryWrapper);
        
        // 存入缓存，设置过期时间为1小时
        if (!recommendProducts.isEmpty()) {
            // 先清除原有缓存
            redisTemplate.delete(RECOMMEND_PRODUCTS_KEY);
            // 添加到缓存
            redisTemplate.opsForList().rightPushAll(RECOMMEND_PRODUCTS_KEY, recommendProducts.toArray());
            redisTemplate.expire(RECOMMEND_PRODUCTS_KEY, 1, java.util.concurrent.TimeUnit.HOURS);
        }
        
        return recommendProducts;
    }

    @Override
    public List<Product> getNewProducts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10; // 默认返回10条
        }
        
        // 尝试从缓存获取
        List<Object> cachedList = redisTemplate.opsForList().range(NEW_PRODUCTS_KEY, 0, limit - 1);
        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList.stream()
                    .map(obj -> (Product) obj)
                    .collect(Collectors.toList());
        }
        
        // 缓存未命中，从数据库获取
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Product::getIsShow, 1)
                .eq(Product::getIsNew, 1)
                .orderByDesc(Product::getCreateTime)
                .last("LIMIT " + limit);
        
        List<Product> newProducts = productMapper.selectList(queryWrapper);
        
        // 存入缓存，设置过期时间为1小时
        if (!newProducts.isEmpty()) {
            // 先清除原有缓存
            redisTemplate.delete(NEW_PRODUCTS_KEY);
            // 添加到缓存
            redisTemplate.opsForList().rightPushAll(NEW_PRODUCTS_KEY, newProducts.toArray());
            redisTemplate.expire(NEW_PRODUCTS_KEY, 1, java.util.concurrent.TimeUnit.HOURS);
        }
        
        return newProducts;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveProductCategory(ProductCategory category) {
        // 这里需要ProductCategoryMapper的支持，暂时无法实现
        // 实际开发中需要添加相关依赖
        return null;
    }

    @Override
    public boolean updateProductCategory(ProductCategory category) {
        // 这里需要ProductCategoryMapper的支持，暂时无法实现
        // 实际开发中需要添加相关依赖
        return false;
    }

    @Override
    public boolean deleteProductCategory(Long id) {
        // 这里需要ProductCategoryMapper的支持，暂时无法实现
        // 实际开发中需要添加相关依赖
        return false;
    }

    @Override
    public List<ProductCategory> getProductCategoryList(Long pid, Integer isShow) {
        // 这里需要ProductCategoryMapper的支持，暂时无法实现
        // 实际开发中需要添加相关依赖
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getProductCategoryTree(Integer isShow) {
        // 这里需要ProductCategoryMapper的支持，暂时无法实现
        // 实际开发中需要添加相关依赖
        return Collections.emptyList();
    }
    
    /**
     * 清除商品相关的缓存
     *
     * @param productId 商品ID
     */
    private void clearProductCache(Long productId) {
        if (productId != null && productId > 0) {
            // 清除商品缓存
            redisTemplate.delete(PRODUCT_KEY_PREFIX + productId);
            // 清除热门、推荐、新品商品列表缓存
            redisTemplate.delete(HOT_PRODUCTS_KEY);
            redisTemplate.delete(RECOMMEND_PRODUCTS_KEY);
            redisTemplate.delete(NEW_PRODUCTS_KEY);
        }
    }
} 