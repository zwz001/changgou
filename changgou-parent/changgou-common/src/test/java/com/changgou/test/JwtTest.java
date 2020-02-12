package com.changgou.test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    /**
     * 创建jwt令牌
     */
    @Test
    public void testCreateJwt(){
        JwtBuilder builder = Jwts.builder()
                .setId("888")       //设置唯一编号
                .setSubject("小白")       //设置主题，可以是JSON数据
                .setIssuedAt(new Date())    //设置签发日期
                //.setExpiration(new Date())    //设置过期时间
                .signWith(SignatureAlgorithm.HS256,"itcast");   //设置签名，使用HS256算法，并设置SecretKey（字符串）
        //自定义数据
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name","王五");
        userInfo.put("age",27);
        userInfo.put("address","深圳黑马训练营程序中心");
        builder.addClaims(userInfo);
        //构建，并返回一个字符串
        System.out.println(builder.compact());
    }

    /**
     * 解析JWT令牌
     */
    @Test
    public void testParseJwt(){
        String compactJwt="eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiI4ODgiLCJzdWIiOiLlsI_nmb0iLCJpYXQiOjE1Nzg1MzMxNTUsImFkZHJlc3MiOiLmt7HlnLPpu5Hpqazorq3nu4PokKXnqIvluo_kuK3lv4MiLCJuYW1lIjoi546L5LqUIiwiYWdlIjoyN30.OunSW1uAkROcZKKTtiuG6Q3YELI44vbM2KApoPAobIE";
        Claims claims = Jwts.parser()
                .setSigningKey("itcast")
                .parseClaimsJws(compactJwt)
                .getBody();
        System.out.println(claims);


    }

}
