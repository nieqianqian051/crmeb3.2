package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maijishop.crmeb.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 订单商品Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    /**
     * 根据订单ID查询订单商品列表
     */
    @Select("SELECT * FROM tb_order_item WHERE order_id = #{orderId} AND is_del = 0")
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询订单商品列表
     */
    @Select("SELECT * FROM tb_order_item WHERE order_no = #{orderNo} AND is_del = 0")
    List<OrderItem> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 更新订单商品评价状态
     */
    @Update("UPDATE tb_order_item SET is_comment = #{isComment}, update_time = NOW() WHERE id = #{id}")
    int updateCommentStatus(@Param("id") Long id, @Param("isComment") Integer isComment);

    /**
     * 更新订单商品退款状态
     */
    @Update("UPDATE tb_order_item SET refund_status = #{refundStatus}, update_time = NOW() WHERE id = #{id}")
    int updateRefundStatus(@Param("id") Long id, @Param("refundStatus") Integer refundStatus);

    /**
     * 批量插入订单商品
     */
    int batchInsert(@Param("list") List<OrderItem> orderItems);

    /**
     * 查询未评价的订单商品
     */
    @Select("SELECT * FROM tb_order_item WHERE order_id = #{orderId} AND is_comment = 0 AND is_del = 0")
    List<OrderItem> selectUncommentItems(@Param("orderId") Long orderId);

    /**
     * 查询用户购买过的商品ID列表
     */
    @Select("SELECT DISTINCT product_id FROM tb_order_item oi " +
            "JOIN tb_order o ON oi.order_id = o.id " +
            "WHERE o.user_id = #{userId} AND o.pay_status = 1 AND o.is_del = 0 AND oi.is_del = 0")
    List<Long> selectUserBoughtProductIds(@Param("userId") Long userId);
} 