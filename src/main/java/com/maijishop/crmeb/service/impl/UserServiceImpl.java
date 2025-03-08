package com.maijishop.crmeb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.maijishop.crmeb.common.constant.CommonConstant;
import com.maijishop.crmeb.common.exception.BusinessException;
import com.maijishop.crmeb.common.utils.JwtUtil;
import com.maijishop.crmeb.entity.User;
import com.maijishop.crmeb.entity.UserAddress;
import com.maijishop.crmeb.mapper.UserAddressMapper;
import com.maijishop.crmeb.mapper.UserMapper;
import com.maijishop.crmeb.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 *
 * @author maijishop
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String login(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException("用户名或密码不能为空");
        }

        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == CommonConstant.DISABLE) {
            throw new BusinessException("用户已被禁用");
        }

        // 更新登录信息
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(user.getLoginCount() + 1);
        userMapper.updateById(user);

        // 生成token
        return jwtUtil.createToken(user.getId().toString(), user.getUsername());
    }

    @Override
    public String loginByPhone(String phone, String code) {
        if (StringUtils.isBlank(phone) || StringUtils.isBlank(code)) {
            throw new BusinessException("手机号或验证码不能为空");
        }

        // TODO: 校验验证码
        
        // 查询用户
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            // 用户不存在则注册
            user = new User();
            user.setPhone(phone);
            user.setUsername(phone);
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setPassword(passwordEncoder.encode(phone.substring(phone.length() - 6)));
            user.setGender(CommonConstant.UNKNOWN_SEX);
            user.setAvatar(""); // 默认头像
            user.setBalance(BigDecimal.ZERO);
            user.setIntegral(0);
            user.setExperience(0);
            user.setUserType(0); // 普通用户
            user.setStatus(CommonConstant.ENABLE);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setIsDel(CommonConstant.NOT_DELETED);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLoginCount(1);
            userMapper.insert(user);
        } else {
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLoginCount(user.getLoginCount() + 1);
            userMapper.updateById(user);
        }

        // 生成token
        return jwtUtil.createToken(user.getId().toString(), user.getUsername());
    }

    @Override
    public String loginByWeixin(String code) {
        if (StringUtils.isBlank(code)) {
            throw new BusinessException("微信code不能为空");
        }

        // TODO: 调用微信API获取openid
        String openid = "test_openid"; // 模拟openid
        String unionid = "test_unionid"; // 模拟unionid

        // 查询用户
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            // 用户不存在则注册
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(unionid);
            user.setUsername("wx_" + openid.substring(openid.length() - 6));
            user.setNickname("微信用户");
            user.setPassword(passwordEncoder.encode(openid)); // 随机密码
            user.setGender(CommonConstant.UNKNOWN_SEX);
            user.setAvatar(""); // 微信头像
            user.setBalance(BigDecimal.ZERO);
            user.setIntegral(0);
            user.setExperience(0);
            user.setUserType(0); // 普通用户
            user.setStatus(CommonConstant.ENABLE);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            user.setIsDel(CommonConstant.NOT_DELETED);
            user.setLastLoginTime(LocalDateTime.now());
            user.setLoginCount(1);
            userMapper.insert(user);
        } else {
            // 更新登录信息
            user.setLastLoginTime(LocalDateTime.now());
            user.setLoginCount(user.getLoginCount() + 1);
            userMapper.updateById(user);
        }

        // 生成token
        return jwtUtil.createToken(user.getId().toString(), user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long register(User user, String code) {
        if (user == null) {
            throw new BusinessException("用户信息不能为空");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }

        if (StringUtils.isBlank(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }

        // 判断用户名是否已存在
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 判断手机号是否已存在
        if (StringUtils.isNotBlank(user.getPhone())) {
            existUser = userMapper.selectByPhone(user.getPhone());
            if (existUser != null) {
                throw new BusinessException("手机号已存在");
            }

            // TODO: 校验验证码
        }

        // 设置用户默认信息
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (StringUtils.isBlank(user.getNickname())) {
            user.setNickname("用户" + System.currentTimeMillis() % 1000);
        }
        user.setGender(user.getGender() == null ? CommonConstant.UNKNOWN_SEX : user.getGender());
        user.setBalance(BigDecimal.ZERO);
        user.setIntegral(0);
        user.setExperience(0);
        user.setUserType(0); // 普通用户
        user.setStatus(CommonConstant.ENABLE);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsDel(CommonConstant.NOT_DELETED);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginCount(1);

        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public User getUserById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }

    @Override
    public User getUserByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        return userMapper.selectByUsername(username);
    }

    @Override
    public User getUserByPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return null;
        }
        return userMapper.selectByPhone(phone);
    }

    @Override
    public User getUserByOpenid(String openid) {
        if (StringUtils.isBlank(openid)) {
            return null;
        }
        return userMapper.selectByOpenid(openid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserInfo(User user) {
        if (user == null || user.getId() == null) {
            throw new BusinessException("用户信息不能为空");
        }

        User existUser = userMapper.selectById(user.getId());
        if (existUser == null) {
            throw new BusinessException("用户不存在");
        }

        // 不允许修改敏感信息
        user.setUsername(null);
        user.setPassword(null);
        user.setPhone(null);
        user.setOpenid(null);
        user.setUnionid(null);
        user.setBalance(null);
        user.setIntegral(null);
        user.setUserType(null);
        user.setStatus(null);
        user.setIsDel(null);
        user.setCreateTime(null);
        
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserBalance(Long userId, BigDecimal amount, Integer type, String remark) {
        if (userId == null || amount == null || type == null) {
            throw new BusinessException("参数不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 扣减余额时，检查余额是否足够
        if (amount.compareTo(BigDecimal.ZERO) < 0 && user.getBalance().add(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("余额不足");
        }

        // 更新用户余额
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);
        updateWrapper.setSql("balance = balance + " + amount);
        updateWrapper.set(User::getUpdateTime, LocalDateTime.now());
        boolean success = update(updateWrapper);

        if (success) {
            // TODO: 记录余额变动记录
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUserIntegral(Long userId, Integer integral, Integer type, String remark) {
        if (userId == null || integral == null || type == null) {
            throw new BusinessException("参数不能为空");
        }

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 扣减积分时，检查积分是否足够
        if (integral < 0 && user.getIntegral() + integral < 0) {
            throw new BusinessException("积分不足");
        }

        // 更新用户积分
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, userId);
        updateWrapper.setSql("integral = integral + " + integral);
        // 增加积分时，同时增加经验值
        if (integral > 0) {
            updateWrapper.setSql("experience = experience + " + integral);
        }
        updateWrapper.set(User::getUpdateTime, LocalDateTime.now());
        boolean success = update(updateWrapper);

        if (success) {
            // TODO: 记录积分变动记录
        }

        return success;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveUserAddress(UserAddress address) {
        if (address == null || address.getUserId() == null) {
            throw new BusinessException("地址信息不能为空");
        }

        // 检查用户是否存在
        User user = userMapper.selectById(address.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 如果设置为默认地址，则将该用户其他地址设置为非默认
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, address.getUserId());
            updateWrapper.set(UserAddress::getIsDefault, 0);
            userAddressMapper.update(null, updateWrapper);
        }

        if (address.getId() == null) {
            // 新增地址
            address.setCreateTime(LocalDateTime.now());
            address.setUpdateTime(LocalDateTime.now());
            address.setIsDel(CommonConstant.NOT_DELETED);
            userAddressMapper.insert(address);
        } else {
            // 修改地址
            address.setUpdateTime(LocalDateTime.now());
            userAddressMapper.updateById(address);
        }

        return address.getId();
    }

    @Override
    public List<UserAddress> getUserAddressList(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        queryWrapper.eq(UserAddress::getIsDel, CommonConstant.NOT_DELETED);
        queryWrapper.orderByDesc(UserAddress::getIsDefault);
        queryWrapper.orderByDesc(UserAddress::getUpdateTime);
        return userAddressMapper.selectList(queryWrapper);
    }

    @Override
    public UserAddress getDefaultUserAddress(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }

        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId);
        queryWrapper.eq(UserAddress::getIsDefault, 1);
        queryWrapper.eq(UserAddress::getIsDel, CommonConstant.NOT_DELETED);
        queryWrapper.last("LIMIT 1");
        UserAddress address = userAddressMapper.selectOne(queryWrapper);

        // 如果没有默认地址，则返回最近使用的地址
        if (address == null) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserAddress::getUserId, userId);
            queryWrapper.eq(UserAddress::getIsDel, CommonConstant.NOT_DELETED);
            queryWrapper.orderByDesc(UserAddress::getUpdateTime);
            queryWrapper.last("LIMIT 1");
            address = userAddressMapper.selectOne(queryWrapper);
        }

        return address;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUserAddress(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new BusinessException("参数不能为空");
        }

        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getId, id);
        queryWrapper.eq(UserAddress::getUserId, userId);
        UserAddress address = userAddressMapper.selectOne(queryWrapper);
        
        if (address == null) {
            throw new BusinessException("地址不存在或无权操作");
        }

        // 逻辑删除
        address.setIsDel(CommonConstant.DELETED);
        address.setUpdateTime(LocalDateTime.now());
        return userAddressMapper.updateById(address) > 0;
    }

    @Override
    public IPage<User> getUserPage(Page<User> page, String keyword, Integer userType, Integer status, String beginTime, String endTime) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDel, CommonConstant.NOT_DELETED);
        
        // 关键字搜索
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> 
                wrapper.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword)
            );
        }
        
        // 用户类型
        if (userType != null) {
            queryWrapper.eq(User::getUserType, userType);
        }
        
        // 状态
        if (status != null) {
            queryWrapper.eq(User::getStatus, status);
        }
        
        // 时间范围
        if (StringUtils.isNotBlank(beginTime)) {
            queryWrapper.ge(User::getCreateTime, LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        if (StringUtils.isNotBlank(endTime)) {
            queryWrapper.le(User::getCreateTime, LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 排序
        queryWrapper.orderByDesc(User::getCreateTime);
        
        return page(page, queryWrapper);
    }

    @Override
    public Map<String, Object> getUserStatistics(String beginTime, String endTime) {
        Map<String, Object> result = new HashMap<>(16);
        
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getIsDel, CommonConstant.NOT_DELETED);
        
        // 时间范围
        if (StringUtils.isNotBlank(beginTime)) {
            queryWrapper.ge(User::getCreateTime, LocalDateTime.parse(beginTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        if (StringUtils.isNotBlank(endTime)) {
            queryWrapper.le(User::getCreateTime, LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        
        // 总用户数
        int totalCount = count(queryWrapper);
        result.put("totalCount", totalCount);
        
        // 今日新增用户数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LambdaQueryWrapper<User> todayQueryWrapper = new LambdaQueryWrapper<>();
        todayQueryWrapper.eq(User::getIsDel, CommonConstant.NOT_DELETED);
        todayQueryWrapper.ge(User::getCreateTime, today);
        int todayCount = count(todayQueryWrapper);
        result.put("todayCount", todayCount);
        
        // 本月新增用户数
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LambdaQueryWrapper<User> monthQueryWrapper = new LambdaQueryWrapper<>();
        monthQueryWrapper.eq(User::getIsDel, CommonConstant.NOT_DELETED);
        monthQueryWrapper.ge(User::getCreateTime, monthStart);
        int monthCount = count(monthQueryWrapper);
        result.put("monthCount", monthCount);
        
        return result;
    }
} 