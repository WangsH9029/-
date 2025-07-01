package com.ywtong.springboothtml.controller.sys;

import com.ywtong.springboothtml.entity.Resp;
import com.ywtong.springboothtml.service.service;
import com.ywtong.springboothtml.entity.equipment;
import com.ywtong.springboothtml.Dao.equipmentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Controller
public class IndexController {
    @Autowired
    private service service;
    
    @Autowired
    private equipmentDao equipmentDao;

    @RequestMapping("/login")
    @ResponseBody
    public Resp<String> login(String account, int password) {
        if ("admin".equals(account) && password == 65535) {
            return Resp.success("管理员身份登入！");
        }else if("farmer".equals(account) && password == 65535){
            return Resp.success("农户身份登入！");
        } else {
            return Resp.fail("400", "游客身份登入！");
        }
    }

    @RequestMapping("/")
    public String index(Model model) {
        model.addAttribute("name", "ywtong");
        return "index";
    }

    @RequestMapping("/toMain")
    public String toMain(Model model) {
        model.addAttribute("name", "ywtong");
        return "main";
    }

    @RequestMapping("/toMainForUser")
    public String toMainForUser(Model model) {
        model.addAttribute("name", "ywtong");
        return "mainForUser";
    }

    @RequestMapping("/toMainForFarmer")
    public String toMainForFarmer(Model model) {
        model.addAttribute("name", "ywtong");
        return "mainForFarmer";
    }

    @RequestMapping("/toProductList")
    public String toProductList(Model model) {
        return "productList";
    }

    @RequestMapping("/toOrderList")
    public String toOrderList(Model model) {
        return "orderList";
    }

    @RequestMapping("/toUserList")
    public String toUserList(Model model) {
        return "userList";
    }

    @RequestMapping("/toFarmerList")
    public String toFarmerList(Model model) {
        return "farmerList";
    }

    @RequestMapping("/toStatistics")
    public String toStatistics(Model model) {
        return "statistics";
    }

    @RequestMapping("/toSettings")
    public String toSettings(Model model) {
        return "settings";
    }

    @RequestMapping("/toAddProduct")
    public String toAddProduct(Model model) {
        return "addProduct";
    }

    @RequestMapping("/toProductCategory")
    public String toProductCategory(Model model) {
        return "productCategory";
    }

    @RequestMapping("/toOrderStatistics")
    public String toOrderStatistics(Model model) {
        return "orderStatistics";
    }

    @RequestMapping("/toIndex")
    public String toIndex(Model model) {
        model.addAttribute("name", "ywtong");
        return "index";
    }

    @RequestMapping("/toIntroduce")
    public String toIntroduce(Model model) {
        model.addAttribute("name", "ywtong");
        return "introduce";
    }

    @RequestMapping("/toEquipment")
    public String toEquipment(Model model) {
        model.addAttribute("name", "ywtong");
        return "equipment";
    }

    @RequestMapping("/toFunction")
    public String toFunction(Model model) {
        model.addAttribute("name", "ywtong");
        return "function";
    }

    @RequestMapping("/findAll")
    @ResponseBody
    public List<equipment> findAll(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "brand", required = false) String brand) {
        return service.searchEquipment(name, brand);
    }

    @RequestMapping(value = "/saveAll")
    @ResponseBody
    public equipment saveAll(equipment entity) {
        return service.save(entity);
    }

    @RequestMapping(value = "delete")
    @ResponseBody
    public void delete(Long id) {
        service.del(id);
    }

    @PostMapping("/update")
    @ResponseBody
    public Resp<String> update(@RequestParam("file") MultipartFile file) {
        return service.upload(file);
    }
}
