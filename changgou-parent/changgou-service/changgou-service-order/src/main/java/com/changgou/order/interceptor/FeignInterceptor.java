package com.changgou.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public class FeignInterceptor implements RequestInterceptor {
    /**
     * Called for every request. Add data using methods on the supplied {@link RequestTemplate}.
     *
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        try {
            if (attributes != null) {
                //取出request
                HttpServletRequest request = attributes.getRequest();
                //获取所有头文件信息的key
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()){
                        //头文件的key
                        String name=headerNames.nextElement();
                        //头文件的value
                        String values = request.getHeader(name);
                        //将令牌数据添加到头文件中
                        requestTemplate.header(name,values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
