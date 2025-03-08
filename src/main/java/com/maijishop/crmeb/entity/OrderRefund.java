package com.maijishop.crmeb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款实体类
 *
 * @author maijishop
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_order_refund")
public class OrderRefund implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 退款ID
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
     * 退款单号
     */
    private String refundNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款金额
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款说明
     */
    private String refundExplain;

    /**
     * 退款凭证图片，多张用逗号分隔
     */
    private String refundProof;

    /**
     * 退款类型：1=仅退款，2=退货退款
     */
    private Integer refundType;

    /**
     * 退款状态：0=退款申请中，1=商家审核中，2=退款中，3=退款成功，4=退款失败
     */
    private Integer refundStatus;

    /**
     * 拒绝原因
     */
    private String rejectReason;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 处理人员
     */
    private String handlePerson;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 退款回调单号
     */
    private String refundTradeNo;

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