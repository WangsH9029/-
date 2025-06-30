package com.ywtong.springboothtml.controller.sys;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    private Long id;
    private String name;
    private String age;
    private String sex;
    private String date;
    private String photo;
    private String introduce;

    public User(Long id,String name,String age,String sex,String date,String photo,String introduce){
        this.id=id;
        this.name=name;
        this.age=age;
        this.sex=sex;
        this.date=date;
        this.photo=photo;
        this.introduce=introduce;
    }

    public User() {

    }

    public String getName() {
        return name;
    }
    public String getAge() {
        return age;
    }
    public String getSex(){
        return sex;
    }
    public String getDate(){
        return date;
    }
    public String getPhoto(){
        return photo;
    }
    public String getIntroduce(){
        return introduce;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setAge(String age) {
        this.age = age;
    }
    public void setSex(String sex){
        this.sex=sex;
    }
    public void setDate(String date){
        this.date=date;
    }
    public void setPhoto(String photo){
        this.photo=photo;
    }
    public void setIntroduce(String introduce){
        this.introduce=introduce;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
