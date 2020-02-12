package com.changgou.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     * 创建二维码

     * @return
     */
    Map<String, String> createNative(Map<String,String> parameter);

    /**
     * 查询订单状态
     * @param outtradeno    客户端自定义订单编号
     * @return
     */
    Map<String, String> queryPayStatus(String outtradeno);

    /**
     * 关闭支付
     * @param orderId
     * @return
     */
    Map<String,String> closePay(Long orderId) throws Exception;
}
