package com.changgou.oauth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 控制层，实现登录页面跳转
 */
@Controller
@RequestMapping("/oauth")
public class LoginRedirect {

    /**
     * 跳转到登录页面
     * @return
     */
    @GetMapping(value = "/login")
    public String login(@RequestParam(value = "FROM",required = false,defaultValue = "") String from, Model model){
        //存储from
        model.addAttribute("from",from);        //获取from参数，并将参数的值存储到Model中
        return "login";
    }
}
