package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="goods")
@RequestMapping(value = "/sku")
public interface SkuFeign {

    /***
     * 根据审核状态查询Sku
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
    Result<List<Sku>> findByStatus(@PathVariable String status);

    @GetMapping(value = "/{id}")
    public Result<Sku> findById(@PathVariable(value = "id",required = true) Long id);

    @PostMapping(value = "/decr/count")
    Result decrCount(@RequestParam(value = "username") String username);
}
