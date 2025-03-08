package com.maijishop.crmeb.common.interceptor;

import com.alibaba.fastjson.JSON;
import com.maijishop.crmeb.common.constant.CommonConstant;
import com.maijishop.crmeb.common.utils.JwtUtil;
import com.maijishop.crmeb.common.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证拦截器
 *
 * @author maijishop
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头中的Token
        String token = request.getHeader(jwtUtil.getHeader());
        if (StringUtils.isBlank(token)) {
            // 尝试从请求参数获取Token
            token = request.getParameter("token");
        }

        // Token为空
        if (StringUtils.isBlank(token)) {
            responseError(response, CommonConstant.UNAUTHORIZED, "未登录");
            return false;
        }

        // 验证Token
        if (!jwtUtil.validateToken(token)) {
            responseError(response, CommonConstant.UNAUTHORIZED, "登录已过期");
            return false;
        }

        // Token有效，将用户ID和用户名存入请求属性中
        try {
            String userId = jwtUtil.getUserId(token);
            String username = jwtUtil.getUsername(token);
            request.setAttribute("userId", userId);
            request.setAttribute("username", username);
            return true;
        } catch (Exception e) {
            log.error("解析Token失败", e);
            responseError(response, CommonConstant.UNAUTHORIZED, "登录状态异常");
            return false;
        }
    }

    /**
     * 响应错误信息
     *
     * @param response 响应对象
     * @param code     错误码
     * @param message  错误信息
     */
    private void responseError(HttpServletResponse response, int code, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(200);
        response.getWriter().write(JSON.toJSONString(Result.error(code, message)));
    }
} 