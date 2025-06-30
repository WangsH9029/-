package com.ywtong.springboothtml.entity;

import javax.persistence.*;

@Entity//表明是实体类
@Table(name = "EQUIPMENT_WYJ")//在这里引入数据库
public class equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,generator = "EQUIPMENT_WYJ")//主键生成策略：
    @TableGenerator(name = "EQUIPMENT_WYJ",allocationSize = 20)//主键生成器
    private Long id;
    private String name;
    private String ps;
    private String brand;
    private String storage_date;

    public equipment(Long id, String name, String ps, String brand,String storage_date) {
        this.id = id;
        this.name = name;
        this.ps = ps;
        this.brand = brand;
        this.storage_date = storage_date;
    }
    public equipment() {}

    //每个get/set前写数据库表中列映射
    @Column(name = "storage_date")
    public String getStorage_date() {return storage_date;}
    public void setStorage_date(String storage_date) {this.storage_date = storage_date;}
    @Column(name = "name")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    @Column(name = "ps")
    public String getPs() {
        return ps;
    }
    public void setPs(String ps) {
        this.ps = ps;
    }
    @Column(name = "brand")
    public String getBrand() {
        return brand;
    }
    public void setBrand(String brand) {
        this.brand = brand;
    }
    @Column(name = "id")
    public void setId(Long id) {
        this.id = id;
    }
    public Long getId() {
        return id;
    }
}
