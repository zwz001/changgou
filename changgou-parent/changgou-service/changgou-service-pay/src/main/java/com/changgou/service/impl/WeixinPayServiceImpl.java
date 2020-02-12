package com.changgou.service.impl;

import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${weixin.appid}")
    private String appid;

    @Value("${weixin.partner}")
    private String partner;

    @Value("${weixin.partnerkey}")
    private String partnerkey;

    @Value("${weixin.notifyurl}")
    private String notifyurl;
    /**
     * 创建二维码
     *
     * @return
     */
    @Override
    public Map<String, String> createNative(Map<String,String> parameter) {
        try {
            //1、封装参数
            Map param = new HashMap();
            param.put("appid", appid);                              //应用ID
            param.put("mch_id", partner);                           //商户ID号
            param.put("nonce_str", WXPayUtil.generateNonceStr());   //随机数
            param.put("body", "畅购");                            	//订单描述
            param.put("out_trade_no","outtradeno");                 //商户订单号
            param.put("total_fee", "money");                      //交易金额
            param.put("spbill_create_ip", "127.0.0.1");           //终端IP
            param.put("notify_url", notifyurl);                    //回调地址
            param.put("trade_type", "NATIVE");                     //交易类型(扫码支付)

            //2.将参数转成xml字符，并携带签名
            String paramXml=WXPayUtil.generateSignedXml(param,partnerkey);

            //3.执行请求
            HttpClient httpClient = new
                     HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();  //发送请求动作执行

            //4.获取参数    模拟浏览器接受微信的支付系统返回的响应
            String content = httpClient.getContent();
            System.out.println(content);
            //获取到微信支付系统返回的数据（code_url）
            Map<String, String> stringMap = WXPayUtil.xmlToMap(content);
            System.out.println("stringMap:"+stringMap);

            //5.获取部分页面所需参数
            HashMap<String, String> dataMap = new HashMap<String,String>();
            dataMap.put("code_url",stringMap.get("code_url"));
            System.out.println(stringMap.get("code_url"));
            dataMap.put("out_trade_no","outtradeno");
            dataMap.put("total_fee","money");

            return dataMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询订单状态
     *
     * @param outtradeno 客户端自定义订单编号
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(String outtradeno) {
        try {
            //1.封装参数
            Map param = new HashMap();
            param.put("appid",appid);                            //应用ID
            param.put("mch_id",partner);                         //商户号
            param.put("out_trade_no",outtradeno);              //商户订单编号
            param.put("nonce_str",WXPayUtil.generateNonceStr()); //随机字符

            //2.将参数转成xml字符，并携带签名
            String paramXml=WXPayUtil.generateSignedXml(param,partnerkey);

            //3.发送请求
            HttpClient httpClient = new
                    HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(paramXml);
            httpClient.post();

            //4.获取返回值，并将返回值转成Map
            String content = httpClient.getContent();
            return WXPayUtil.xmlToMap(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 关闭支付
     *关闭微信支付
     * @param orderId
     * @return
     */
    @Override
    public Map<String, String> closePay(Long orderId) throws Exception {
        //参数设置
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("appid",appid);    //应用id
        paramMap.put("mch_id",partner); //商户编号
        paramMap.put("nonce_str",WXPayUtil.generateNonceStr());  //随机字符
        paramMap.put("out_trade_no",String.valueOf(orderId));     //商家的唯一编号

        //将map数据装成XML字符
        String xmlParam = WXPayUtil.generateSignedXml(paramMap,partnerkey);

        //确定url
        String url = "https://api.mch.weixin.qq.com/pay/closeorder";

        //发送请求
        HttpClient httpClient = new HttpClient(url);

        //https
        httpClient.setHttps(true);
        //提交参数
        httpClient.setXmlParam(xmlParam);

        //提交
        httpClient.post();

        //获取返回数据
        String content = httpClient.getContent();

        //将返回数据解析成Map
        return WXPayUtil.xmlToMap(content);

    }
}
