package com.changgou.search.service.impl;


import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuEsMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {
    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    /**
     * 导入sku数据到es
     */
    @Override
    public void importSku() {
        //调用changgou-service-goods微服务
        Result<List<Sku>> skuListResult = skuFeign.findByStatus("1");
        //将数据转成search.Sku
        String toSkuInfo = JSON.toJSONString(skuListResult.getData());
        List<SkuInfo> skuInfos = JSON.parseArray(toSkuInfo, SkuInfo.class);
        for (SkuInfo skuInfo : skuInfos) {
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec());
            skuInfo.setSpecMap(specMap);
        }
        skuEsMapper.saveAll(skuInfos);
    }

    /**
     * 搜索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map search(Map<String, String> searchMap) {


        //1.获取关键字的值
        String keywords = searchMap.get("keywords");
        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为";    //赋值给一个默认的值
        }
        //2.创建查询对象的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //3.设置查询的条件

        //设置分组条件 商品分类
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategoryGroup").field("categoryName").size(50));

        //设置分组条件，商品品牌
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandGroup").field("brandName").size(50));

        //设置分组条件，商品的规格
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpecGroup").field("spec.keyword").size(100));

        //设置高亮条件
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("name"));
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));

        //设置主关键字查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"name","brandName","categoryName"));

        //设置主关键字查询
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
        }

        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
        }

        //规格过滤查询
        if (searchMap != null) {
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                }
            }
        }

        //从请求参数中获取price的范围（0-500）
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            //以-切割字符串
            String[] split = price.split("-");
            //判断穿过来的参数最大值是不是*，如果是*则查询split[0]以上的数据
            if (!split[1].equalsIgnoreCase("*")) {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0], true).to(split[1], true));
            } else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }

        //构建过滤查询
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);

        //构建分页查询
        //判断分页是否是第一页，如果传入的参数为null，表示第一页
        Integer pageNum = StringUtils.isEmpty(searchMap.get("pageNum")) ? 1 : Integer.valueOf(searchMap.get("pageNum"));
        //每页显示的行，先写死在这里
        Integer pageSize = 10;
        //将分页条件添加到查询对象中
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum-1,pageSize));

        //构建排序查询
        String sortRule=searchMap.get("sortRule");  //排序规则，升降排序
        String sortField=searchMap.get("sortField");    //通过什么排序
        if(!StringUtils.isEmpty(sortRule)&&!StringUtils.isEmpty(sortField)){
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(sortRule.equalsIgnoreCase("DESC")? SortOrder.DESC:SortOrder.ASC));
        }


        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        //5.执行查询
        /*AggregatedPage<SkuInfo> skuPage = esTemplate.queryForPage(query, SkuInfo.class);*/
        AggregatedPage<SkuInfo> skuPage = esTemplate.queryForPage(query,SkuInfo.class,new SearchResultMapperImpl());
        //获取分组结果 商品品牌
        StringTerms stringTermsBrand = (StringTerms) skuPage.getAggregation("skuBrandGroup");

        StringTerms stringTermsCategory = (StringTerms) skuPage.getAggregation("skuCategoryGroup");

        StringTerms stringTermsSpec = (StringTerms) skuPage.getAggregation("skuSpecGroup");
        List<String> categoryList = getStringsCategoryList(stringTermsCategory);

        List<String> brandList = getStringsCategoryList(stringTermsBrand);

        Map<String, Set<String>> specMap = getStringSetMap(stringTermsSpec);


        //6.返回结果
        Map resultMap = new HashMap<>();
        resultMap.put("specMap", specMap);
        resultMap.put("brandList", brandList);
        resultMap.put("categoryList", categoryList);
        resultMap.put("rows", skuPage.getContent());
        resultMap.put("total", skuPage.getTotalElements());
        resultMap.put("totalPage", skuPage.getTotalPages());

        return resultMap;
    }

    /**
     * 获取分类列表数据
     *
     * @param stringTerms
     * @return
     */
    private List<String> getStringsCategoryList(StringTerms stringTerms) {
        List<String> categoryList = new ArrayList<>();
        if (stringTerms != null) {
            for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }

    /**
     * 分装获取规格分组
     *
     * @param stringTermsSpec
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(StringTerms stringTermsSpec) {
        Map<String, Set<String>> specMap = new HashMap<>();
        Set<String> specList = new HashSet<>();
        if (stringTermsSpec != null) {
            for (StringTerms.Bucket bucket : stringTermsSpec.getBuckets()) {
                specList.add(bucket.getKeyAsString());
            }
        }
        for (String specjson : specList) {
            Map<String, String> map = JSON.parseObject(specjson, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();        //规格名字
                String value = entry.getValue();      //规格选项值
                //获取当前规格名字对应的规格数量
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到spec集合中
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }

}
