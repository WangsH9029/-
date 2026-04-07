package com.ywtong.springboothtml.repository;

import com.ywtong.springboothtml.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByPhone(String phone);
    boolean existsByUsername(String username);
    List<User> findByUsernameContainingOrNicknameContaining(String username, String nickname);
    List<User> findByRole(String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") String role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createTime >= :startDate")
    Long countByRoleAndCreateTimeAfter(@Param("role") String role, @Param("startDate") Date startDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', u.createTime, '%Y-%m-%d') as date, COUNT(u) as count FROM User u WHERE u.role = :role AND u.createTime >= :startDate GROUP BY FUNCTION('DATE_FORMAT', u.createTime, '%Y-%m-%d') ORDER BY date")
    List<Object[]> getUserGrowthByDateRange(@Param("role") String role, @Param("startDate") Date startDate);
} 