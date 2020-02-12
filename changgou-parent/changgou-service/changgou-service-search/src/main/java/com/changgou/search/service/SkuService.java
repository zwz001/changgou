package com.changgou.search.service;

import com.changgou.goods.pojo.Sku;

import java.util.List;
import java.util.Map;

public interface SkuService {

    /**
     * 导入SKU数据
     */
    void importSku();

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    Map search(Map<String,String> searchMap);
}
