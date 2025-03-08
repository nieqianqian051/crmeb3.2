package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maijishop.crmeb.entity.OrderRefund;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单退款Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface OrderRefundMapper extends BaseMapper<OrderRefund> {

    /**
     * 退款申请分页列表查询
     */
    @Select("<script>" +
            "SELECT * FROM tb_order_refund WHERE is_del = 0" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND order_no = #{orderNo}</if>" +
            "<if test='refundNo != null and refundNo != \"\"'> AND refund_no = #{refundNo}</if>" +
            "<if test='refundStatus != null'> AND refund_status = #{refundStatus}</if>" +
            "<if test='refundType != null'> AND refund_type = #{refundType}</if>" +
            "<if test='beginTime != null'> AND create_time &gt;= #{beginTime}</if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>" +
            " ORDER BY id DESC" +
            "</script>")
    IPage<OrderRefund> selectPageList(Page<OrderRefund> page,
                                     @Param("userId") Long userId,
                                     @Param("orderNo") String orderNo,
                                     @Param("refundNo") String refundNo,
                                     @Param("refundStatus") Integer refundStatus,
                                     @Param("refundType") Integer refundType,
                                     @Param("beginTime") LocalDateTime beginTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 更新退款状态
     */
    @Update("UPDATE tb_order_refund SET refund_status = #{refundStatus}, update_time = NOW() WHERE id = #{id}")
    int updateRefundStatus(@Param("id") Long id, @Param("refundStatus") Integer refundStatus);

    /**
     * 处理退款申请
     */
    @Update("UPDATE tb_order_refund SET " +
            "refund_status = #{refundStatus}, " +
            "reject_reason = #{rejectReason}, " +
            "handle_person = #{handlePerson}, " +
            "handle_time = #{handleTime}, " +
            "handle_remark = #{handleRemark}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int handleRefund(@Param("id") Long id,
                    @Param("refundStatus") Integer refundStatus,
                    @Param("rejectReason") String rejectReason,
                    @Param("handlePerson") String handlePerson,
                    @Param("handleTime") LocalDateTime handleTime,
                    @Param("handleRemark") String handleRemark);

    /**
     * 完成退款
     */
    @Update("UPDATE tb_order_refund SET " +
            "refund_status = #{refundStatus}, " +
            "refund_time = #{refundTime}, " +
            "refund_trade_no = #{refundTradeNo}, " +
            "update_time = NOW() " +
            "WHERE id = #{id}")
    int completeRefund(@Param("id") Long id,
                      @Param("refundStatus") Integer refundStatus,
                      @Param("refundTime") LocalDateTime refundTime,
                      @Param("refundTradeNo") String refundTradeNo);

    /**
     * 查询用户的退款申请列表
     */
    @Select("SELECT * FROM tb_order_refund WHERE user_id = #{userId} AND is_del = 0 ORDER BY id DESC")
    List<OrderRefund> selectUserRefundList(@Param("userId") Long userId);

    /**
     * 查询订单的退款申请列表
     */
    @Select("SELECT * FROM tb_order_refund WHERE order_id = #{orderId} AND is_del = 0 ORDER BY id DESC")
    List<OrderRefund> selectOrderRefundList(@Param("orderId") Long orderId);

    /**
     * 统计退款数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM tb_order_refund WHERE is_del = 0" +
            "<if test='refundStatus != null'> AND refund_status = #{refundStatus}</if>" +
            "<if test='beginTime != null'> AND create_time &gt;= #{beginTime}</if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>" +
            "</script>")
    int countRefunds(@Param("refundStatus") Integer refundStatus,
                    @Param("beginTime") LocalDateTime beginTime,
                    @Param("endTime") LocalDateTime endTime);
} 