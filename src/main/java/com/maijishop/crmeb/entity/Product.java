package com.maijishop.crmeb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author maijishop
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_product")
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 商品ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 轮播图，多个图片用逗号分隔
     */
    private String sliderImage;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品简介
     */
    private String intro;

    /**
     * 关键字
     */
    private String keyword;

    /**
     * 商品条码
     */
    private String barCode;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品类型：0=普通商品，1=秒杀商品，2=砍价商品，3=拼团商品，4=会员专属，5=积分商品
     */
    private Integer productType;

    /**
     * 单位
     */
    private String unit;

    /**
     * 商品售价
     */
    private BigDecimal price;

    /**
     * 市场价/划线价
     */
    private BigDecimal originalPrice;

    /**
     * 成本价
     */
    private BigDecimal costPrice;

    /**
     * VIP会员价
     */
    private BigDecimal vipPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 虚拟销量
     */
    private Integer fictitiousSales;

    /**
     * 实际销量
     */
    private Integer sales;

    /**
     * 浏览量
     */
    private Integer views;

    /**
     * 是否热卖：0=否，1=是
     */
    private Integer isHot;

    /**
     * 是否推荐：0=否，1=是
     */
    private Integer isRecommend;

    /**
     * 是否新品：0=否，1=是
     */
    private Integer isNew;

    /**
     * 是否包邮：0=否，1=是
     */
    private Integer isPostage;

    /**
     * 运费模板ID
     */
    private Long tempId;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 规格类型：0=单规格，1=多规格
     */
    private Integer specType;

    /**
     * 积分抵扣比例
     */
    private Integer integralRatio;

    /**
     * 赠送积分
     */
    private Integer giveIntegral;

    /**
     * 是否上架：0=下架，1=上架
     */
    private Integer isShow;

    /**
     * 商品详情
     */
    private String content;

    /**
     * 供应商ID
     */
    private Long supplierId;

    /**
     * 是否审核：0=未审核，1=审核通过，2=审核未通过
     */
    private Integer isVerify;

    /**
     * 审核未通过原因
     */
    private String verifyReason;

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