package com.changgou.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.service.WeixinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/weixin/pay")
public class WeixinPayController {

    //队列交换机信息注入
    @Value("${mq.pay.exchange.order}")
    private String exchange;

    @Value("${mq.pay.queue.order}}")
    private String queue;

    @Value("${mq.pay.routing.key}")
    private String routing;

    @Autowired
    private WeixinPayService weixinPayService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment environment;

    /**
     * 创建二维码
     *
     * @return
     */
    @RequestMapping("/create/native")
    public Result createNative(@RequestParam Map<String,String> parameter){
        Map<String,String> resultMap = weixinPayService.createNative(parameter);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }

    /**
     * 查询支付状态
     * @param outtradeno
     * @return
     */
    @GetMapping(value = "/status/query")
    public Result queryStatus(String outtradeno){
        Map<String,String> resultMap = weixinPayService.queryPayStatus(outtradeno);
        return new Result(true,StatusCode.OK,"查询状态成功！",resultMap);
    }

    @RequestMapping("/notify/url")
    public String notifyUrl(HttpServletRequest request){
        InputStream inStream;
        try {
            //读取支付回调数据
            inStream = request.getInputStream();
            ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len=inStream.read(buffer))!=-1){
                outSteam.write(buffer,0,len);
            }
            outSteam.close();
            inStream.close();
            //将支付回掉数据转换成xml字符串
            String result = new String(outSteam.toByteArray(),"utf-8");
            //将xml字符串转换成map结构==>进行业务处理
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);

            String attach=map.get("attach");    //参数列表数据json
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
            String from = attachMap.get("from");    //获取标识
            switch (from){
                case "1":
                    System.out.println("发送普通队列");
                    //发送消息
                    rabbitTemplate.convertAndSend(
                            environment.getProperty("mq.pay.exchange.order"),
                            environment.getProperty("mq.pay.routing.key"),
                            JSON.toJSONString(map)
                    );
                    break;
                case "2":
                    System.out.println("发送秒杀队列");
                    rabbitTemplate.convertAndSend(
                            environment.getProperty("mq.pay.exchange.seckillorder"),
                            environment.getProperty("mq.pay.routing.seckillkey"),
                            JSON.toJSONString(map)
                    );
                    break;
                default:
                    System.out.println("错误的信息");
                    break;
            }

            //将消息发送给RabbitMQ
            rabbitTemplate.convertAndSend(exchange,routing, JSON.toJSONString(map));

            //响应数据设置
            Map respMap = new HashMap<>();
            respMap.put("return_code","SUCCESS");
            respMap.put("return_msg","OK");
            return WXPayUtil.mapToXml(respMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
