package com.changgou.order.config;


import com.alibaba.fastjson.JSON;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TokenDecode {
    //公钥
    private static final String PUBLIC_KEY="public.key";

    private static String publickey="";

    /**
     * 获取非对称加密公钥key
     * @return  公钥key
     */
    public String getPubkey(){
        if(!StringUtils.isEmpty(publickey)){
            return publickey;
        }
        Resource resource = new ClassPathResource(PUBLIC_KEY);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(resource.getInputStream());
            BufferedReader br = new BufferedReader(inputStreamReader);
            String publickey = br.lines().collect(Collectors.joining("\n"));
            return publickey;
        } catch (IOException e) {
            return null;
        }
    }

    public Map<String,String> decodeToken(String token){
        //校验Jwt
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(getPubkey()));
        //获取Jwt原始内容
        String claims = jwt.getClaims();
        return JSON.parseObject(claims,Map.class);
    }

    /**
     * 获取用户信息
     * @return
     */
    public Map<String,String> getUserInfo(){
        //获取授权信息
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();

        //令牌解码
        return decodeToken(details.getTokenValue());

    }
}
