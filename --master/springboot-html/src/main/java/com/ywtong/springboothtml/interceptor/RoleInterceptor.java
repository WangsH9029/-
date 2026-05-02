package com.ywtong.springboothtml.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;

/**
 * 基于角色的权限拦截器
 * 实现细粒度的接口访问控制
 * 支持从JWT Token(request attribute)或Session中获取用户信息
 */
@Component
public class RoleInterceptor implements HandlerInterceptor {

    private static final String SESSION_USER_ID = "currentUserId";
    private static final String SESSION_ROLE = "currentUserRole";

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

        // 优先从request attribute获取(JWT拦截器设置的)
        String role = (String) request.getAttribute(SESSION_ROLE);
        Object userId = request.getAttribute(SESSION_USER_ID);

        // 如果JWT中没有,则从Session获取(兼容旧方式)
        if (role == null || userId == null) {
            HttpSession session = request.getSession();
            role = (String) session.getAttribute(SESSION_ROLE);
            userId = session.getAttribute(SESSION_USER_ID);
        }

        String uri = request.getRequestURI();
        String method = request.getMethod();

        // 未登录检查
        if (userId == null) {
            sendJsonError(response, 401, "未登录,请先登录");
            return false;
        }

        // 管理员专属接口
        if (isAdminOnlyEndpoint(uri, method)) {
            if (!"ROLE_ADMIN".equals(role)) {
                sendJsonError(response, 403, "无权限访问,仅管理员可操作");
                return false;
            }
        }

        // 农户专属接口
        if (isFarmerOnlyEndpoint(uri, method)) {
            if (!"ROLE_FARMER".equals(role)) {
                sendJsonError(response, 403, "无权限访问,仅农户可操作");
                return false;
            }
        }

        // 普通用户不能访问的接口
        if (isUserForbiddenEndpoint(uri, method)) {
            if ("ROLE_USER".equals(role)) {
                sendJsonError(response, 403, "无权限访问,普通用户不可操作");
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否为管理员专属接口
     */
    private boolean isAdminOnlyEndpoint(String uri, String method) {
        // 用户管理列表(仅管理员可查看所有用户)
        if (uri.matches(".*/api/user/list.*")) {
            return true;
        }

        // 删除用户(仅管理员)
        if (uri.matches(".*/api/user/\\d+") && "DELETE".equals(method)) {
            return true;
        }

        // 修改用户角色(仅管理员)
        if (uri.matches(".*/api/user/\\d+/role") && "PUT".equals(method)) {
            return true;
        }

        // 用户认证审核(仅管理员)
        if (uri.matches(".*/api/user/\\d+/verify") && "PUT".equals(method)) {
            return true;
        }

        // 删除订单(仅管理员)
        if (uri.matches(".*/api/order/\\d+") && "DELETE".equals(method)) {
            return true;
        }

        // 统计数据(仅管理员)
        if (uri.matches(".*/api/statistics/.*")) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为农户专属接口
     */
    private boolean isFarmerOnlyEndpoint(String uri, String method) {
        // 查看我的商品(仅农户)
        if (uri.matches(".*/api/product/my.*")) {
            return true;
        }

        // 创建商品(仅农户和管理员)
        if (uri.matches(".*/api/product") && "POST".equals(method)) {
            return true;
        }

        return false;
    }

    /**
     * 判断是否为普通用户禁止访问的接口
     */
    private boolean isUserForbiddenEndpoint(String uri, String method) {
        // 商品管理相关(普通用户不能创建/修改/删除商品)
        if (uri.matches(".*/api/product") && "POST".equals(method)) {
            return true;
        }
        if (uri.matches(".*/api/product/\\d+") && ("PUT".equals(method) || "DELETE".equals(method))) {
            return true;
        }

        return false;
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
