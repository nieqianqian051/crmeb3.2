package com.maijishop.crmeb.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maijishop.crmeb.common.constant.CommonConstant;
import com.maijishop.crmeb.common.utils.Result;
import com.maijishop.crmeb.entity.User;
import com.maijishop.crmeb.entity.UserAddress;
import com.maijishop.crmeb.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 *
 * @author maijishop
 */
@Slf4j
@Api(tags = "用户接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<String> login(@RequestParam String username, @RequestParam String password) {
        String token = userService.login(username, password);
        return Result.success("登录成功", token);
    }

    /**
     * 手机号登录
     */
    @ApiOperation("手机号登录")
    @PostMapping("/login/phone")
    public Result<String> loginByPhone(@RequestParam String phone, @RequestParam String code) {
        String token = userService.loginByPhone(phone, code);
        return Result.success("登录成功", token);
    }

    /**
     * 微信登录
     */
    @ApiOperation("微信登录")
    @PostMapping("/login/weixin")
    public Result<String> loginByWeixin(@RequestParam String code) {
        String token = userService.loginByWeixin(code);
        return Result.success("登录成功", token);
    }

    /**
     * 用户注册
     */
    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Long> register(@RequestBody @Valid User user, @RequestParam(required = false) String code) {
        Long userId = userService.register(user, code);
        return Result.success("注册成功", userId);
    }

    /**
     * 获取用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            return Result.error(CommonConstant.UNAUTHORIZED, "未登录");
        }
        User user = userService.getUserById(Long.valueOf(userId));
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setPassword(null); // 不返回密码
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @ApiOperation("更新用户信息")
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            return Result.error(CommonConstant.UNAUTHORIZED, "未登录");
        }
        user.setId(Long.valueOf(userId));
        boolean success = userService.updateUserInfo(user);
        return success ? Result.success("更新成功") : Result.error("更新失败");
    }

    /**
     * 获取用户地址列表
     */
    @ApiOperation("获取用户地址列表")
    @GetMapping("/address/list")
    public Result<List<UserAddress>> getUserAddressList(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            return Result.error(CommonConstant.UNAUTHORIZED, "未登录");
        }
        List<UserAddress> addressList = userService.getUserAddressList(Long.valueOf(userId));
        return Result.success(addressList);
    }

    /**
     * 获取用户默认地址
     */
    @ApiOperation("获取用户默认地址")
    @GetMapping("/address/default")
    public Result<UserAddress> getDefaultUserAddress(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            return Result.error(CommonConstant.UNAUTHORIZED, "未登录");
        }
        UserAddress address = userService.getDefaultUserAddress(Long.valueOf(userId));
        return Result.success(address);
    }

    /**
     * 保存用户地址
     */
    @ApiOperation("保存用户地址")
    @PostMapping("/address/save")
    public Result<Long> saveUserAddress(@RequestBody @Valid UserAddress address, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            return Result.error(CommonConstant.UNAUTHORIZED, "未登录");
        }
        address.setUserId(Long.valueOf(userId));
        Long addressId = userService.saveUserAddress(address);
        return Result.success("保存成功", addressId);
    }

    /**
     * 删除用户地址
     */
    @ApiOperation("删除用户地址")
    @DeleteMapping("/address/{id}")
    public Result<Void> deleteUserAddress(@PathVariable Long id, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isBlank(userId)) {
            return Result.error(CommonConstant.UNAUTHORIZED, "未登录");
        }
        boolean success = userService.deleteUserAddress(id, Long.valueOf(userId));
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 用户分页列表查询（管理员接口）
     */
    @ApiOperation("用户分页列表查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "limit", value = "每页条数", required = true, dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keyword", value = "关键字", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "userType", value = "用户类型", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "beginTime", value = "开始时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "string", paramType = "query")
    })
    @GetMapping("/page")
    public Result<IPage<User>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer userType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        Page<User> pageParam = new Page<>(page, limit);
        IPage<User> pageData = userService.getUserPage(pageParam, keyword, userType, status, beginTime, endTime);
        return Result.success(pageData);
    }

    /**
     * 用户统计数据（管理员接口）
     */
    @ApiOperation("用户统计数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "beginTime", value = "开始时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "string", paramType = "query")
    })
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getUserStatistics(
            @RequestParam(required = false) String beginTime,
            @RequestParam(required = false) String endTime) {
        Map<String, Object> statistics = userService.getUserStatistics(beginTime, endTime);
        return Result.success(statistics);
    }
} 