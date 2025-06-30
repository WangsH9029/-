package com.ywtong.springboothtml.controller.sys;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
public class Controller_2 {
    @RequestMapping("/check")
    @ResponseBody
    public JsonMsg check(String account,int password){
        if("admin".equals(account)&&password==65535){
            return new JsonMsg(true,"成功登录",null);
        }else{
            return new JsonMsg(false,"用户名密码有误或不存在",null);
        }
    }

    @RequestMapping("/toMain2")
    public String toMain(Model model){
        model.addAttribute("name","ywtong");
        return "main";
    }
    /*点击选项卡，跳转到新页面p11.html：
    由前台发送选项卡里获取的value值p1.1
    由于前台静态页面不能直接跳转，需要发送到后台，后台再发送
     */
    @RequestMapping("/toPage")
    public String toPage(Model model,String url){
        model.addAttribute("name","ywtong");
        return url;
    }

    @RequestMapping("/saveUser")
    @ResponseBody
    public JsonMsg saveUser(User entity)
    {
        return new JsonMsg(true,"success!",entity);
    }

    @RequestMapping("/name")
    @ResponseBody
    public Name name(String name)
    {
        return new Name(name);
    }

}
