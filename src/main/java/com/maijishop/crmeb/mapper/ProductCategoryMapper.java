package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maijishop.crmeb.entity.ProductCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品分类Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface ProductCategoryMapper extends BaseMapper<ProductCategory> {

    /**
     * 获取指定父级ID下的所有分类
     *
     * @param pid 父级ID
     * @return 分类列表
     */
    @Select("SELECT * FROM product_category WHERE pid = #{pid} ORDER BY sort DESC")
    List<ProductCategory> selectByPid(@Param("pid") Long pid);

    /**
     * 获取分类及其子分类数量
     *
     * @param pid 父级ID
     * @return 分类列表
     */
    @Select("SELECT c.*, (SELECT COUNT(*) FROM product_category WHERE pid = c.id) AS child_count FROM product_category c WHERE c.pid = #{pid} ORDER BY c.sort DESC")
    List<ProductCategory> selectWithChildCount(@Param("pid") Long pid);

    /**
     * 查询指定分类下的商品数量
     *
     * @param categoryId 分类ID
     * @return 商品数量
     */
    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId} AND is_del = 0")
    int countProductByCategoryId(@Param("categoryId") Long categoryId);
} 