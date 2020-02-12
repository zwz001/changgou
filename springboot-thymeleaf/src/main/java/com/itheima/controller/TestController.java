package com.itheima.controller;

import com.itheima.pojo.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;

@Controller
@RequestMapping("/test")
public class TestController {

    @RequestMapping("/hello")
    public String hello(Model model){
        model.addAttribute("hello","hello welcome");
        //集合数据
        ArrayList<User> users = new ArrayList<>();
        users.add(new User(1,"张三","深圳"));
        users.add(new User(2,"李四","北京"));
        users.add(new User(3,"王五","武汉"));
        model.addAttribute("users",users);
        return "demo1";
    }

}
