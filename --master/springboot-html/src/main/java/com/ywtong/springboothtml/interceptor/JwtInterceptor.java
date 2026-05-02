package com.ywtong.springboothtml.interceptor;

import com.ywtong.springboothtml.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * JWT Token拦截器
 * 验证请求中的JWT Token是否有效
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

        // 从请求头中获取Token
        String token = request.getHeader("Authorization");

        // 如果没有Token或格式不正确
        if (token == null || !token.startsWith("Bearer ")) {
            sendJsonError(response, 401, "未登录,请先登录");
            return false;
        }

        // 去掉"Bearer "前缀
        token = token.substring(7);

        // 验证Token
        try {
            if (!jwtUtil.validateToken(token)) {
                sendJsonError(response, 401, "Token已过期,请重新登录");
                return false;
            }

            // Token有效,将用户信息存入request attribute供后续使用
            Long userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            request.setAttribute("currentUserId", userId);
            request.setAttribute("currentUsername", username);
            request.setAttribute("currentUserRole", role);

            return true;

        } catch (Exception e) {
            sendJsonError(response, 401, "Token无效: " + e.getMessage());
            return false;
        }
    }

    /**
     * 发送JSON格式的错误响应
     */
    private void sendJsonError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("{\"code\":\"" + status + "\",\"message\":\"" + message + "\"}");
        writer.flush();
    }
}
