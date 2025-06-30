package com.ywtong.springboothtml.Dao;

import com.ywtong.springboothtml.entity.equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface equipmentDao extends JpaRepository<equipment,Long> {
    //sql语句；：name是输入框内的；u.name是数据库中的
    @Query("SELECT u FROM equipment u WHERE (:name IS NULL OR u.name LIKE %:name%)AND(:brand IS NULL OR :brand='' OR u.brand=:brand)")
    List<equipment> findByNameLikeAndBrand(@Param("name")String name, @Param("brand") String brand);
}