package com.changgou.search.dao;


import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {
}
