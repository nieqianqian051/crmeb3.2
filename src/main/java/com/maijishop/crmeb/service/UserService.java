package com.maijishop.crmeb.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.maijishop.crmeb.entity.User;
import com.maijishop.crmeb.entity.UserAddress;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 *
 * @author maijishop
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return token
     */
    String login(String username, String password);

    /**
     * 手机号登录
     *
     * @param phone 手机号
     * @param code  验证码
     * @return token
     */
    String loginByPhone(String phone, String code);

    /**
     * 微信登录
     *
     * @param code 微信code
     * @return token
     */
    String loginByWeixin(String code);

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @param code 验证码
     * @return 用户ID
     */
    Long register(User user, String code);

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);

    /**
     * 根据手机号获取用户信息
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User getUserByPhone(String phone);

    /**
     * 根据OpenID获取用户信息
     *
     * @param openid 微信OpenID
     * @return 用户信息
     */
    User getUserByOpenid(String openid);

    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return 是否成功
     */
    boolean updateUserInfo(User user);

    /**
     * 更新用户余额
     *
     * @param userId 用户ID
     * @param amount 变动金额（正数增加，负数减少）
     * @param type   变动类型：1=充值，2=消费，3=退款，4=提现
     * @param remark 备注
     * @return 是否成功
     */
    boolean updateUserBalance(Long userId, BigDecimal amount, Integer type, String remark);

    /**
     * 更新用户积分
     *
     * @param userId 用户ID
     * @param integral 变动积分（正数增加，负数减少）
     * @param type     变动类型：1=购物获得，2=签到获得，3=使用积分
     * @param remark   备注
     * @return 是否成功
     */
    boolean updateUserIntegral(Long userId, Integer integral, Integer type, String remark);

    /**
     * 设置用户收货地址
     *
     * @param address 地址信息
     * @return 地址ID
     */
    Long saveUserAddress(UserAddress address);

    /**
     * 获取用户收货地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    List<UserAddress> getUserAddressList(Long userId);

    /**
     * 获取用户默认收货地址
     *
     * @param userId 用户ID
     * @return 默认地址
     */
    UserAddress getDefaultUserAddress(Long userId);

    /**
     * 删除用户收货地址
     *
     * @param id     地址ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUserAddress(Long id, Long userId);

    /**
     * 用户分页列表查询
     *
     * @param page      分页参数
     * @param keyword   关键字
     * @param userType  用户类型
     * @param status    状态
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 用户分页列表
     */
    IPage<User> getUserPage(Page<User> page, String keyword, Integer userType, Integer status, String beginTime, String endTime);

    /**
     * 用户统计数据
     *
     * @param beginTime 开始时间
     * @param endTime   结束时间
     * @return 统计数据
     */
    Map<String, Object> getUserStatistics(String beginTime, String endTime);
} 