package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.Resp;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/login")
    @ResponseBody
    public Resp<String> login(@RequestParam String account, @RequestParam String password) {
        // 管理员账号 admin/65535
        if ("admin".equals(account) && "65535".equals(password)) {
            return Resp.success("管理员身份登入！");
        }
        // 农户账号 farmer/65535
        else if ("farmer".equals(account) && "65535".equals(password)) {
            return Resp.success("农户身份登入！");
        }
        // 游客用户（任何错误账密）
        else {
            return Resp.success("游客身份登入！");
        }
    }

    // 主页面跳转
    @GetMapping("/toMain")
    public String toMain() {
        return "main";
    }

    @GetMapping("/toMainForFarmer")
    public String toMainForFarmer() {
        return "mainForFarmer";
    }

    @GetMapping("/toMainForUser")
    public String toMainForUser() {
        return "mainForUser";
    }

    // 商品管理
    @GetMapping("/toProductList")
    public String toProductList() {
        return "productList";
    }

    @GetMapping("/toAddProduct")
    public String toAddProduct() {
        return "addProduct";
    }

    @GetMapping("/toProductCategory")
    public String toProductCategory() {
        return "productCategory";
    }

    // 订单管理
    @GetMapping("/toOrderList")
    public String toOrderList() {
        return "orderList";
    }

    @GetMapping("/toOrderStatistics")
    public String toOrderStatistics() {
        return "orderStatistics";
    }

    // 用户管理
    @GetMapping("/toUserList")
    public String toUserList() {
        return "userList";
    }

    @GetMapping("/toUser")
    public String toUser() {
        return "user";
    }

    // 农户管理
    @GetMapping("/toFarmerList")
    public String toFarmerList() {
        return "farmerList";
    }

    // 数据统计
    @GetMapping("/toStatistics")
    public String toStatistics() {
        return "statistics";
    }

    // 系统设置
    @GetMapping("/toSettings")
    public String toSettings() {
        return "settings";
    }

    // 设备管理
    @GetMapping("/toEquipment")
    public String toEquipment() {
        return "equipment";
    }
}
