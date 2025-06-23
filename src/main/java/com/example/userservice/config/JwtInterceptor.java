//// JwtInterceptor.java
//@Component
//@Slf4j
//public class JwtInterceptor implements HandlerInterceptor {
//
//    @Autowired
//    private JwtUtil jwtUtil;
//
//    private static final List<String> WHITELIST = Arrays.asList(
//            "/user/register",
//            "/user/login"
//    );
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        // 跳过白名单
//        if (WHITELIST.stream().anyMatch(path -> request.getRequestURI().endsWith(path))) {
//            return true;
//        }
//
//        // 获取token
//        String token = request.getHeader("Authorization");
//        if (token == null || !token.startsWith("Bearer ")) {
//            throw new RuntimeException("未登录或token无效");
//        }
//
//        // 验证token
//        token = token.substring(7);
//        if (!jwtUtil.validateToken(token)) {
//            throw new RuntimeException("token已过期或无效");
//        }
//
//        // 传递用户信息
//        request.setAttribute("userId", jwtUtil.extractUserId(token));
//        request.setAttribute("username", jwtUtil.extractUsername(token));
//
//        return true;
//    }
//}

package com.example.userservice.config;

import com.example.userservice.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

/**
 * JWT拦截器
 * 用于验证请求中的JWT token
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> WHITELIST = Arrays.asList(
            "/user/register",
            "/user/login",
            "/actuator/health"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestURI = request.getRequestURI();
        
        // 跳过白名单
        if (WHITELIST.stream().anyMatch(path -> requestURI.endsWith(path))) {
            return true;
        }

        // 获取token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("请求缺少Authorization头: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 验证token
        token = token.substring(7);
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token验证失败: {}", requestURI);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        // 传递用户信息到请求属性中
        request.setAttribute("userId", jwtUtil.extractUserId(token));
        request.setAttribute("username", jwtUtil.extractUsername(token));

        log.debug("Token验证成功: userId={}, username={}, uri={}", 
                jwtUtil.extractUserId(token), jwtUtil.extractUsername(token), requestURI);
        
        return true;
    }
}