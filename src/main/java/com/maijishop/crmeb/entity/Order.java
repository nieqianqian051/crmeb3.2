package com.maijishop.crmeb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author maijishop
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order")
public class Order implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 订单类型：0=普通订单，1=秒杀订单，2=砍价订单，3=拼团订单，4=积分订单
     */
    private Integer orderType;

    /**
     * 订单状态：0=待付款，1=待发货，2=待收货，3=待评价，4=已完成，5=已取消，6=已退款
     */
    private Integer status;

    /**
     * 支付状态：0=未支付，1=已支付
     */
    private Integer payStatus;

    /**
     * 支付方式：0=余额支付，1=微信支付，2=支付宝支付，3=银行卡支付
     */
    private Integer payType;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货状态：0=未发货，1=已发货
     */
    private Integer shippingStatus;

    /**
     * 发货时间
     */
    private LocalDateTime shippingTime;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人电话
     */
    private String receiverPhone;

    /**
     * 收货人地址
     */
    private String receiverAddress;

    /**
     * 快递公司编码
     */
    private String expressCode;

    /**
     * 快递公司名称
     */
    private String expressName;

    /**
     * 快递单号
     */
    private String expressNo;

    /**
     * 商品总数量
     */
    private Integer totalNum;

    /**
     * 商品总价格
     */
    private BigDecimal totalPrice;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券金额
     */
    private BigDecimal couponAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实际支付金额
     */
    private BigDecimal payAmount;

    /**
     * 支付流水号
     */
    private String transactionId;

    /**
     * 使用积分
     */
    private Integer useIntegral;

    /**
     * 积分抵扣金额
     */
    private BigDecimal integralAmount;

    /**
     * 赠送积分
     */
    private Integer giveIntegral;

    /**
     * 用户备注
     */
    private String userRemark;

    /**
     * 管理员备注
     */
    private String adminRemark;

    /**
     * 是否删除：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDel;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 