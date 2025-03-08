package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maijishop.crmeb.entity.ProductSku;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品SKU Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface ProductSkuMapper extends BaseMapper<ProductSku> {

    /**
     * 根据商品ID查询SKU列表
     *
     * @param productId 商品ID
     * @return SKU列表
     */
    List<ProductSku> selectByProductId(@Param("productId") Long productId);

    /**
     * 减少SKU库存
     *
     * @param id       SKU ID
     * @param quantity 减少数量
     * @return 影响行数
     */
    @Update("UPDATE product_sku SET stock = stock - #{quantity}, sales = sales + #{quantity}, update_time = NOW() " +
            "WHERE id = #{id} AND stock >= #{quantity} AND is_del = 0")
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加SKU库存
     *
     * @param id       SKU ID
     * @param quantity 增加数量
     * @return 影响行数
     */
    @Update("UPDATE product_sku SET stock = stock + #{quantity}, sales = sales - #{quantity}, update_time = NOW() " +
            "WHERE id = #{id} AND is_del = 0")
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 批量插入SKU
     *
     * @param skuList SKU列表
     * @return 影响行数
     */
    int batchInsert(@Param("list") List<ProductSku> skuList);

    /**
     * 批量更新SKU
     *
     * @param skuList SKU列表
     * @return 影响行数
     */
    int batchUpdate(@Param("list") List<ProductSku> skuList);

    /**
     * 根据商品ID删除所有SKU
     *
     * @param productId 商品ID
     * @return 影响行数
     */
    int deleteByProductId(@Param("productId") Long productId);
} 