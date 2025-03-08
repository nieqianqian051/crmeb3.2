package com.maijishop.crmeb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单商品实体类
 *
 * @author maijishop
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order_item")
public class OrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 订单商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品SKU ID
     */
    private Long productSkuId;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品规格
     */
    private String specs;

    /**
     * 商品SKU编码
     */
    private String skuCode;

    /**
     * 商品条码
     */
    private String barCode;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 商品数量
     */
    private Integer quantity;

    /**
     * 商品总价
     */
    private BigDecimal totalPrice;

    /**
     * 商品实际支付单价
     */
    private BigDecimal payPrice;

    /**
     * 商品实际支付总价
     */
    private BigDecimal payTotalPrice;

    /**
     * 是否评价：0=未评价，1=已评价
     */
    private Integer isComment;

    /**
     * 退款状态：0=未申请退款，1=申请退款中，2=退款成功，3=退款失败
     */
    private Integer refundStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 是否删除：0=未删除，1=已删除
     */
    @TableLogic
    private Integer isDel;
} 