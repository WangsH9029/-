package com.ywtong.springboothtml.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "USER_WYJ")//数据库名称
public class UserWyj {//实体类名称
    private Long id;
    private String name;
    private String age;
    private String sex;
    private Date dateBirth;
    //private String photo;
    private String introduce;
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,generator="USER_WYJ")
    //功能：指定主键生成策略。GenerationType.TABLE 表示使用表生成策略生成主键。
    //属性：
    //strategy: 定义主键生成的策略。这里使用 GenerationType.TABLE 表示使用一个独立的表来生成主键。
    //generator: 指定生成器的名称，这里是 "W_USER"，对应于 @TableGenerator 注解中定义的生成器。
    @TableGenerator(name="USER_WYJ",allocationSize=20)
    //功能：定义一个主键生成器，使用一个表来生成主键值。
    //属性：
    //name: 生成器的名称，这里是 "W_USER"，需要与 @GeneratedValue 中的 generator 属性一致。
    //allocationSize: 预分配的主键数量，默认值为 50。这里设置为 20，表示每次从数据库表中获取主键值时，会预分配 20 个值，这有助于减少数据库的访问次数，提高性能。

    @Column(name ="ID")
    //每个get/set前都要写
    //功能：指定实体类中的字段与数据库表中的列之间的映射关系。
    //属性：
    //name: 数据库表中列的名称，这里是 "ID"。这表示实体类中的该字段映射到数据库表中的 ID 列。
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name="NAME")
    public String getName() {
        return name;
    }
    public void setName(String name) {this.name = name;}

    @Column(name="AGE")
    public String getAge() {
        return age;
    }
    public void setAge(String age) {
        this.age = age;
    }

    @Column(name="SEX")
    public String getSex() {return sex;}
    public void setSex(String sex) {
        this.sex = sex;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")//后台接受的Date类型转化成该格式String类型
    @DateTimeFormat(pattern = "yyyy-MM-dd")//前台接受字符串转化成该格式的Date类型
    @Column(name="DATE_BIRTH")
    public Date getDateBirth() {
        return dateBirth;
    }
    public void setDateBirth(Date dateBirth) {
        this.dateBirth = dateBirth;
    }

    /*@Column(name="PHOTO")
    public String getPhoto() {
        return photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }*/

    @Column(name="INTRODUCE")
    public String getIntroduce() {
        return introduce;
    }
    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }
}
