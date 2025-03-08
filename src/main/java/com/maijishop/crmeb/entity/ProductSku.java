package com.maijishop.crmeb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU实体类
 *
 * @author maijishop
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_product_sku")
public class ProductSku implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * SKU ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品条码
     */
    private String barCode;

    /**
     * SKU编码
     */
    private String skuCode;

    /**
     * SKU图片
     */
    private String image;

    /**
     * SKU名称
     */
    private String skuName;

    /**
     * 规格属性值，格式：规格:属性值;规格:属性值
     */
    private String specs;

    /**
     * 售价
     */
    private BigDecimal price;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * 市场价/划线价
     */
    private BigDecimal originalPrice;

    /**
     * VIP会员价
     */
    private BigDecimal vipPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 预警库存
     */
    private Integer alertStock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 重量（克）
     */
    private BigDecimal weight;

    /**
     * 体积（立方厘米）
     */
    private BigDecimal volume;

    /**
     * 是否默认：0=否，1=是
     */
    private Integer isDefault;

    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;

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