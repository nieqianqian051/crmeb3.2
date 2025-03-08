package com.maijishop.crmeb.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maijishop.crmeb.entity.OrderAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 订单地址Mapper接口
 *
 * @author maijishop
 */
@Mapper
public interface OrderAddressMapper extends BaseMapper<OrderAddress> {

    /**
     * 根据订单ID查询订单地址
     */
    @Select("SELECT * FROM tb_order_address WHERE order_id = #{orderId} AND is_del = 0 LIMIT 1")
    OrderAddress selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询订单地址
     */
    @Select("SELECT * FROM tb_order_address WHERE order_no = #{orderNo} AND is_del = 0 LIMIT 1")
    OrderAddress selectByOrderNo(@Param("orderNo") String orderNo);
} 