package com.ywtong.springboothtml.service;

import com.ywtong.springboothtml.entity.User;
import com.ywtong.springboothtml.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    // 简单的MD5加密方法
    private String md5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    // 用户注册
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        // 使用MD5加密密码
        user.setPassword(md5(user.getPassword()));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        // 设置默认角色为普通用户
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        return userRepository.save(user);
    }
    
    // 用户登录
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(md5(password))) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }
    
    // 获取所有用户（分页）
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    // 根据ID获取用户
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }
    
    // 更新用户信息
    public User updateUser(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        existingUser.setUsername(user.getUsername());
        existingUser.setNickname(user.getNickname());
        existingUser.setPhone(user.getPhone());
        existingUser.setEmail(user.getEmail());
        existingUser.setAddress(user.getAddress());
        existingUser.setUpdateTime(new Date());

        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getIsVerified() != null) {
            existingUser.setIsVerified(user.getIsVerified());
        }
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            existingUser.setPassword(md5(user.getPassword()));
        }

        return userRepository.save(existingUser);
    }
    
    // 删除用户
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }
    
    // 修改用户角色（管理员功能）
    public User updateUserRole(Long id, String role) {
        User user = getUserById(id);
        user.setRole(role);
        user.setUpdateTime(new Date());
        return userRepository.save(user);
    }
    
    // 验证用户是否为农户
    public boolean isFarmer(Long id) {
        User user = getUserById(id);
        return "ROLE_FARMER".equals(user.getRole());
    }
    
    // 验证用户是否为管理员
    public boolean isAdmin(Long id) {
        User user = getUserById(id);
        return "ROLE_ADMIN".equals(user.getRole());
    }
    
    // 更新用户认证状态（农户认证）
    public User updateVerificationStatus(Long id, boolean isVerified) {
        User user = getUserById(id);
        user.setIsVerified(isVerified);
        user.setUpdateTime(new Date());
        return userRepository.save(user);
    }
    
    // 搜索用户
    public List<User> searchUsers(String keyword) {
        return userRepository.findByUsernameContainingOrNicknameContaining(keyword, keyword);
    }

    public List<User> findByRole(String role) {
        return userRepository.findByRole(role);
    }
    public List<User> findAll() {
        return userRepository.findAll();
    }

    // 发送验证码（模拟，暂不真实发送）
    public String sendVerificationCode(String phone) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码格式不正确");
        }
        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }
        // 模拟生成6位验证码
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        // TODO: 实际应该调用短信接口发送验证码，这里暂时只返回验证码供测试
        return code;
    }

    // 重置密码
    public void resetPassword(String phone, String code, String newPassword) {
        if (phone == null || !phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码格式不正确");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("验证码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new RuntimeException("新密码不能为空");
        }

        User user = userRepository.findByPhone(phone);
        if (user == null) {
            throw new RuntimeException("该手机号未注册");
        }

        // TODO: 实际应该验证验证码是否正确且未过期，这里暂时跳过验证
        // 为了演示，暂时接受任何6位数字验证码
        if (!code.matches("^\\d{6}$")) {
            throw new RuntimeException("验证码格式不正确");
        }

        user.setPassword(md5(newPassword));
        user.setUpdateTime(new Date());
        userRepository.save(user);
    }
} 