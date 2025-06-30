package com.ywtong.springboothtml.Dao;

import com.ywtong.springboothtml.entity.UserWyj;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
//extends JpaRepository<User_Wyj, Long>:
//JpaRepository 是 Spring Data JPA 提供的一个接口，它提供了 CRUD（创建、读取、更新、删除）操作的默认实现。通过继承这个接口，BasUserDao将自动获得这些基本的数据库操作功能。
//User_Wyj 是这个 DAO 接口操作的实体类，即数据库中的表对应的Java类。
//Long 是实体类的主键类型。在这个例子中，User_Wyj实体的主键类型是 Long。
public interface UserWyjDao extends JpaRepository<UserWyj, Long> {
    @Query("SELECT u FROM UserWyj u WHERE (:name IS NULL OR u.name LIKE %:name%) AND (:sex IS NULL OR :sex='' OR u.sex = :sex)")
        //u是UserWyj别名,表示找所有符合where后面条件的u这个查询语句的含义是：
        //where后面的含义时：
        //如果 name 参数为空（NULL），则 u.name LIKE %:name% 条件将被忽略。
        //如果 sex 参数为空（NULL），则 u.sex = :sex 条件将被忽略。
        //用:name来引用name参数，
        // 如果 name 参数为 null，即没有提供 name 这个查询条件，那么这一部分的条件将为 true，不会对结果产生过滤作用；
        //如果传入模糊name，会进行模糊匹配
        //sex同理
    List<UserWyj> findByNameLikeAndSex(@Param("name")String name,@Param("sex")String sex);
    //这里 name 参数用 @Param("name") 注解标记。这意味着在 JPQL 查询中，可以通过 :name 来引用这个参数。
    //...
}
