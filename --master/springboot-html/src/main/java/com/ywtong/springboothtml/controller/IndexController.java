package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.LoginUserInfo;
import com.ywtong.springboothtml.entity.Resp;
import com.ywtong.springboothtml.entity.User;
import com.ywtong.springboothtml.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
public class IndexController {

    private static final String SESSION_USER_ID = "currentUserId";
    private static final String SESSION_USERNAME = "currentUsername";
    private static final String SESSION_ROLE = "currentUserRole";

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/login")
    @ResponseBody
    public Resp<LoginUserInfo> login(@RequestParam String account,
                                     @RequestParam String password,
                                     HttpSession session) {
        try {
            User user = userService.login(account, password);
            session.setAttribute(SESSION_USER_ID, user.getId());
            session.setAttribute(SESSION_USERNAME, user.getUsername());
            session.setAttribute(SESSION_ROLE, user.getRole());
            LoginUserInfo loginUserInfo = new LoginUserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getNickname(),
                    user.getRole()
            );
            return Resp.success(loginUserInfo);
        } catch (Exception e) {
            return Resp.fail("401", e.getMessage());
        }
    }

    @PostMapping("/logout")
    @ResponseBody
    public Resp<String> logout(HttpSession session) {
        session.invalidate();
        return Resp.success("退出成功");
    }

    // 主页面跳转
    @GetMapping("/toMain")
    public String toMain(HttpSession session) {
        return requireRole(session, "ROLE_ADMIN", "main");
    }

    @GetMapping("/toMainForFarmer")
    public String toMainForFarmer(HttpSession session) {
        return requireRole(session, "ROLE_FARMER", "mainForFarmer");
    }

    @GetMapping("/toMainForUser")
    public String toMainForUser(HttpSession session) {
        return requireRole(session, "ROLE_USER", "mainForUser");
    }

    @GetMapping("/toMall")
    public String toMall(HttpSession session) {
        return requireRole(session, "ROLE_USER", "mall");
    }

    // 商品管理
    @GetMapping("/toProductList")
    public String toProductList(HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/";
        }
        String role = getCurrentRole(session);
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_FARMER".equals(role)) {
            return redirectHome(role);
        }
        return "productList";
    }

    @GetMapping("/toAddProduct")
    public String toAddProduct(HttpSession session) {
        if (!isLoggedIn(session)) {
            return "redirect:/";
        }
        String role = getCurrentRole(session);
        if (!"ROLE_ADMIN".equals(role) && !"ROLE_FARMER".equals(role)) {
            return redirectHome(role);
        }
        return "addProduct";
    }

    @GetMapping("/toProductCategory")
    public String toProductCategory(HttpSession session) {
        return requireLogin(session, "productCategory");
    }

    // 订单管理
    @GetMapping("/toOrderList")
    public String toOrderList(HttpSession session) {
        return requireLogin(session, "orderList");
    }

    @GetMapping("/toOrderStatistics")
    public String toOrderStatistics(HttpSession session) {
        return requireRole(session, "ROLE_ADMIN", "orderStatistics");
    }

    // 用户管理
    @GetMapping("/toUserList")
    public String toUserList(HttpSession session) {
        return requireRole(session, "ROLE_ADMIN", "userList");
    }

    @GetMapping("/toUser")
    public String toUser(HttpSession session) {
        return requireLogin(session, "user");
    }

    // 农户管理
    @GetMapping("/toFarmerList")
    public String toFarmerList(HttpSession session) {
        return requireRole(session, "ROLE_ADMIN", "farmerList");
    }

    // 数据统计
    @GetMapping("/toStatistics")
    public String toStatistics(HttpSession session) {
        return requireRole(session, "ROLE_ADMIN", "statistics");
    }

    // 系统设置
    @GetMapping("/toSettings")
    public String toSettings(HttpSession session) {
        return requireLogin(session, "settings");
    }

    // 设备管理
    @GetMapping("/toEquipment")
    public String toEquipment(HttpSession session) {
        return requireRole(session, "ROLE_ADMIN", "equipment");
    }

    private String requireLogin(HttpSession session, String viewName) {
        if (!isLoggedIn(session)) {
            return "redirect:/";
        }
        return viewName;
    }

    private String requireRole(HttpSession session, String expectedRole, String viewName) {
        if (!isLoggedIn(session)) {
            return "redirect:/";
        }
        String role = getCurrentRole(session);
        if (!expectedRole.equals(role)) {
            return redirectHome(role);
        }
        return viewName;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute(SESSION_USER_ID) != null;
    }

    private String getCurrentRole(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        return role == null ? null : role.toString();
    }

    private String redirectHome(String role) {
        if ("ROLE_ADMIN".equals(role)) {
            return "redirect:/toMain";
        }
        if ("ROLE_FARMER".equals(role)) {
            return "redirect:/toMainForFarmer";
        }
        if ("ROLE_USER".equals(role)) {
            return "redirect:/toMainForUser";
        }
        return "redirect:/";
    }
}
