package com.maijishop.crmeb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户等级实体类
 *
 * @author maijishop
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user_level")
public class UserLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 等级ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    private String name;

    /**
     * 等级图标
     */
    private String icon;

    /**
     * 等级背景图
     */
    private String backgroundImage;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 升级需要的经验值
     */
    private Integer experience;

    /**
     * 会员价格
     */
    private BigDecimal price;

    /**
     * 有效期类型：0=永久，1=固定天数
     */
    private Integer validType;

    /**
     * 有效期天数
     */
    private Integer validDays;

    /**
     * 会员权益说明
     */
    private String benefits;

    /**
     * 折扣率（0-100）
     */
    private Integer discount;

    /**
     * 状态：0=禁用，1=启用
     */
    private Integer status;

    /**
     * 是否免运费：0=否，1=是
     */
    private Integer isFreeShipping;

    /**
     * 是否显示：0=否，1=是
     */
    private Integer isShow;

    /**
     * 是否为付费会员：0=否，1=是
     */
    private Integer isPaid;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 备注
     */
    private String remark;

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