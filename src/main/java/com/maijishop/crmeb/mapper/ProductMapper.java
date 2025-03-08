package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maijishop.crmeb.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 商品分页列表查询
     */
    @Select("<script>" +
            "SELECT * FROM tb_product WHERE is_del = 0" +
            "<if test='categoryId != null'> AND category_id = #{categoryId}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (name LIKE CONCAT('%', #{keyword}, '%') OR keyword LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            "<if test='isShow != null'> AND is_show = #{isShow}</if>" +
            "<if test='productType != null'> AND product_type = #{productType}</if>" +
            "<if test='isHot != null'> AND is_hot = #{isHot}</if>" +
            "<if test='isRecommend != null'> AND is_recommend = #{isRecommend}</if>" +
            "<if test='isNew != null'> AND is_new = #{isNew}</if>" +
            " ORDER BY sort DESC, id DESC" +
            "</script>")
    IPage<Product> selectPageList(Page<Product> page,
                                  @Param("categoryId") Long categoryId,
                                  @Param("keyword") String keyword,
                                  @Param("isShow") Integer isShow,
                                  @Param("productType") Integer productType,
                                  @Param("isHot") Integer isHot,
                                  @Param("isRecommend") Integer isRecommend,
                                  @Param("isNew") Integer isNew);

    /**
     * 更新商品库存
     */
    @Update("UPDATE tb_product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * 获取热销商品列表
     */
    @Select("SELECT * FROM tb_product WHERE is_del = 0 AND is_show = 1 AND is_hot = 1 ORDER BY sort DESC, sales DESC LIMIT #{limit}")
    List<Product> selectHotProducts(@Param("limit") Integer limit);

    /**
     * 获取推荐商品列表
     */
    @Select("SELECT * FROM tb_product WHERE is_del = 0 AND is_show = 1 AND is_recommend = 1 ORDER BY sort DESC, id DESC LIMIT #{limit}")
    List<Product> selectRecommendProducts(@Param("limit") Integer limit);

    /**
     * 获取新品列表
     */
    @Select("SELECT * FROM tb_product WHERE is_del = 0 AND is_show = 1 AND is_new = 1 ORDER BY sort DESC, id DESC LIMIT #{limit}")
    List<Product> selectNewProducts(@Param("limit") Integer limit);
} 