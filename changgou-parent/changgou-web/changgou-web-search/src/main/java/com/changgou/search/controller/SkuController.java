package com.changgou.search.controller;

import com.changgou.search.feign.SkuFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SkuController {
    @Autowired
    private SkuFeign skuFeign;

    @GetMapping("/list")
    public String search(@RequestParam(required = false) Map searchMap, Model model){
        //调用changgou-service-search微服务
        Map resultMap = skuFeign.search(searchMap);
        model.addAttribute("result",resultMap);
        model.addAttribute("searchMap",searchMap);

        //请求地址
        String url=url(searchMap);
        model.addAttribute("url",url);
        return "search";
    }

    public String url(Map<String,String> searchMap){
        //url地址
        String url="/search/list";
        if(searchMap!=null && searchMap.size()>0){
            url+="?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //分页跳过
                String key = entry.getKey();
                url+=key+"="+entry.getValue()+"&";
            }
            //去掉最后一个&
            url=url.substring(0,url.length()-1);
        }
        return url;
    }

}
