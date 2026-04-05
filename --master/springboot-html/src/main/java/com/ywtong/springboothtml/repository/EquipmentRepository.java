package com.ywtong.springboothtml.repository;

import com.ywtong.springboothtml.entity.equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<equipment, Long> {

    @Query("SELECT e FROM equipment e WHERE e.name LIKE %:name% AND e.brand LIKE %:brand%")
    List<equipment> findByNameLikeAndBrand(@Param("name") String name, @Param("brand") String brand);
}