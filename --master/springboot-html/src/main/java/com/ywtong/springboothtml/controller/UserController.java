package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.LoginUserInfo;
import com.ywtong.springboothtml.entity.Resp;
import com.ywtong.springboothtml.entity.ResetPasswordRequest;
import com.ywtong.springboothtml.entity.User;
import com.ywtong.springboothtml.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final String SESSION_USER_ID = "currentUserId";
    private static final String SESSION_USERNAME = "currentUsername";
    private static final String SESSION_ROLE = "currentUserRole";

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<User> getUserList(@RequestParam(required = false) String role, HttpSession session) {
        requireAdmin(session);
        if (role != null && !role.isEmpty()) {
            return userService.findByRole(role);
        }
        return userService.findAll();
    }

    @GetMapping("/current")
    public Resp<LoginUserInfo> getCurrentUser(HttpSession session) {
        User user = getCurrentUserEntity(session);
        return Resp.success(new LoginUserInfo(user.getId(), user.getUsername(), user.getNickname(), user.getRole()));
    }

    @PostMapping("/logout")
    public Resp<String> logout(HttpSession session) {
        session.invalidate();
        return Resp.success("退出成功");
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUserEntity(session);
        if (!isAdmin(session) && !currentUser.getId().equals(id)) {
            throw new RuntimeException("无权限查看该用户信息");
        }
        return userService.getUserById(id);
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User user, HttpSession session) {
        User currentUser = getCurrentUserEntity(session);
        if (!isAdmin(session) && !currentUser.getId().equals(id)) {
            throw new RuntimeException("无权限修改该用户信息");
        }
        user.setId(id);
        if (!isAdmin(session)) {
            user.setRole(currentUser.getRole());
            user.setIsVerified(currentUser.getIsVerified());
        }
        return userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        userService.deleteUser(id);
    }

    @PutMapping("/{id}/role")
    public User updateUserRole(@PathVariable Long id, @RequestParam String role, HttpSession session) {
        requireAdmin(session);
        return userService.updateUserRole(id, role);
    }

    @PutMapping("/{id}/verify")
    public User verifyUser(@PathVariable Long id, @RequestParam boolean isVerified, HttpSession session) {
        requireAdmin(session);
        return userService.updateVerificationStatus(id, isVerified);
    }

    @GetMapping("/search")
    public List<User> searchUsers(@RequestParam String keyword, HttpSession session) {
        requireAdmin(session);
        return userService.searchUsers(keyword);
    }

    @PostMapping("/send-code")
    public Resp<String> sendVerificationCode(@RequestParam String phone) {
        try {
            String code = userService.sendVerificationCode(phone);
            // 实际应该发送短信，这里暂时返回验证码供测试
            return Resp.success(code);
        } catch (Exception e) {
            return Resp.fail("500", e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public Resp<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            userService.resetPassword(request.getPhone(), request.getCode(), request.getNewPassword());
            return Resp.success("密码重置成功");
        } catch (Exception e) {
            return Resp.fail("500", e.getMessage());
        }
    }

    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        return "ROLE_ADMIN".equals(role);
    }

    private void requireAdmin(HttpSession session) {
        if (!isAdmin(session)) {
            throw new RuntimeException("无权限访问");
        }
    }

    private User getCurrentUserEntity(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        return userService.getUserById(Long.valueOf(userId.toString()));
    }
}
