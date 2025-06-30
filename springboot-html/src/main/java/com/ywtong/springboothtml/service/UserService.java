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
        
        // 如果密码被修改，需要重新加密
        if (user.getPassword() != null && !user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(md5(user.getPassword()));
        }
        
        // 保留原有的一些字段
        user.setCreateTime(existingUser.getCreateTime());
        user.setUpdateTime(new Date());
        user.setRole(existingUser.getRole()); // 角色不允许通过普通更新修改
        
        return userRepository.save(user);
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
} 