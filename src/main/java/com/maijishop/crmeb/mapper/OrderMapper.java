package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maijishop.crmeb.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    /**
     * 订单分页列表查询
     */
    @Select("<script>" +
            "SELECT * FROM tb_order WHERE is_del = 0" +
            "<if test='userId != null'> AND user_id = #{userId}</if>" +
            "<if test='orderNo != null and orderNo != \"\"'> AND order_no = #{orderNo}</if>" +
            "<if test='status != null'> AND status = #{status}</if>" +
            "<if test='orderType != null'> AND order_type = #{orderType}</if>" +
            "<if test='payStatus != null'> AND pay_status = #{payStatus}</if>" +
            "<if test='shippingStatus != null'> AND shipping_status = #{shippingStatus}</if>" +
            "<if test='beginTime != null'> AND create_time &gt;= #{beginTime}</if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>" +
            " ORDER BY id DESC" +
            "</script>")
    IPage<Order> selectPageList(Page<Order> page,
                               @Param("userId") Long userId,
                               @Param("orderNo") String orderNo,
                               @Param("status") Integer status,
                               @Param("orderType") Integer orderType,
                               @Param("payStatus") Integer payStatus,
                               @Param("shippingStatus") Integer shippingStatus,
                               @Param("beginTime") LocalDateTime beginTime,
                               @Param("endTime") LocalDateTime endTime);

    /**
     * 更新订单支付状态
     */
    @Update("UPDATE tb_order SET pay_status = #{payStatus}, pay_time = #{payTime}, transaction_id = #{transactionId}, status = #{status}, update_time = #{updateTime} WHERE order_no = #{orderNo} AND pay_status = 0")
    int updatePayStatus(@Param("orderNo") String orderNo,
                       @Param("payStatus") Integer payStatus,
                       @Param("payTime") LocalDateTime payTime,
                       @Param("transactionId") String transactionId,
                       @Param("status") Integer status,
                       @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新订单发货状态
     */
    @Update("UPDATE tb_order SET shipping_status = #{shippingStatus}, shipping_time = #{shippingTime}, express_code = #{expressCode}, express_name = #{expressName}, express_no = #{expressNo}, status = #{status}, update_time = #{updateTime} WHERE id = #{id} AND shipping_status = 0")
    int updateShippingStatus(@Param("id") Long id,
                            @Param("shippingStatus") Integer shippingStatus,
                            @Param("shippingTime") LocalDateTime shippingTime,
                            @Param("expressCode") String expressCode,
                            @Param("expressName") String expressName,
                            @Param("expressNo") String expressNo,
                            @Param("status") Integer status,
                            @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新订单状态
     */
    @Update("UPDATE tb_order SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id,
                    @Param("status") Integer status,
                    @Param("updateTime") LocalDateTime updateTime);

    /**
     * 取消超时未支付订单
     */
    @Update("UPDATE tb_order SET status = 5, update_time = NOW() WHERE status = 0 AND pay_status = 0 AND create_time <= #{timeoutTime}")
    int cancelTimeoutOrders(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 统计订单数据
     */
    @Select("SELECT " +
            "COUNT(*) AS totalCount, " +
            "SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) AS unpaidCount, " +
            "SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) AS unshippedCount, " +
            "SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) AS receivedCount, " +
            "SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) AS completedCount, " +
            "SUM(CASE WHEN status = 5 THEN 1 ELSE 0 END) AS cancelledCount, " +
            "SUM(CASE WHEN status = 6 THEN 1 ELSE 0 END) AS refundedCount, " +
            "SUM(CASE WHEN pay_status = 1 THEN pay_amount ELSE 0 END) AS totalSalesAmount " +
            "FROM tb_order WHERE is_del = 0" +
            "<if test='beginTime != null'> AND create_time &gt;= #{beginTime}</if>" +
            "<if test='endTime != null'> AND create_time &lt;= #{endTime}</if>")
    Map<String, Object> statisticOrder(@Param("beginTime") LocalDateTime beginTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 查询待支付订单
     */
    @Select("SELECT * FROM tb_order WHERE status = 0 AND pay_status = 0 AND is_del = 0 AND create_time <= #{timeoutTime}")
    List<Order> selectUnpaidTimeoutOrders(@Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 获取商品销量排行
     */
    @Select("SELECT p.id, p.name, p.image, COUNT(oi.id) AS salesCount, SUM(oi.quantity) AS salesQuantity " +
            "FROM tb_order o " +
            "JOIN tb_order_item oi ON o.id = oi.order_id " +
            "JOIN tb_product p ON oi.product_id = p.id " +
            "WHERE o.pay_status = 1 AND o.is_del = 0" +
            "<if test='beginTime != null'> AND o.pay_time &gt;= #{beginTime}</if>" +
            "<if test='endTime != null'> AND o.pay_time &lt;= #{endTime}</if>" +
            "GROUP BY p.id " +
            "ORDER BY salesQuantity DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> selectProductRanking(@Param("beginTime") LocalDateTime beginTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                @Param("limit") Integer limit);
} 