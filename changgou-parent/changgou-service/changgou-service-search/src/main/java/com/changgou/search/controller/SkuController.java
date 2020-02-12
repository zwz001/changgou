package com.changgou.search.controller;

import com.changgou.goods.pojo.Sku;
import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/search")
public class SkuController {

    @Autowired
    private SkuService skuService;

    @GetMapping("/import")
    public Result search(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map searchMap) {
        return skuService.search(searchMap);
    }
}
