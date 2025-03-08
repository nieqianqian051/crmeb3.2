package com.maijishop.crmeb.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author maijishop
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("tb_user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户账号
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 性别：0=未知，1=男，2=女
     */
    private Integer gender;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 国家/地区代码
     */
    private String countryCode;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 余额
     */
    private BigDecimal balance;

    /**
     * 积分
     */
    private Integer integral;

    /**
     * 经验值
     */
    private Integer experience;

    /**
     * 等级ID
     */
    private Long levelId;

    /**
     * 用户类型：0=普通用户，1=管理员
     */
    private Integer userType;

    /**
     * 是否为分销员：0=否，1=是
     */
    private Integer isDistributor;

    /**
     * 分销商等级ID
     */
    private Long distributorLevelId;

    /**
     * 推广员编号
     */
    private String spreadCode;

    /**
     * 上级推广员ID
     */
    private Long spreadUid;

    /**
     * 上级推广员二级ID
     */
    private Long spreadTwoUid;

    /**
     * 最后一次登录IP
     */
    private String lastLoginIp;

    /**
     * 最后一次登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 登录次数
     */
    private Integer loginCount;

    /**
     * 用户标签ID，多个用逗号隔开
     */
    private String tagIds;

    /**
     * 是否启用：0=禁用，1=启用
     */
    private Integer status;

    /**
     * 消费金额
     */
    private BigDecimal consumeAmount;

    /**
     * 下单数量
     */
    private Integer orderCount;

    /**
     * 下单支付数量
     */
    private Integer orderPayCount;

    /**
     * 微信用户标识
     */
    private String openid;

    /**
     * 微信UnionID
     */
    private String unionid;

    /**
     * 是否注销：0=正常，1=注销
     */
    private Integer isLogoff;

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