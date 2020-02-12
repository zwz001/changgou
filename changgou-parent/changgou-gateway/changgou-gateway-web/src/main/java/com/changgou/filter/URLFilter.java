package com.changgou.filter;

public class URLFilter {
    private static final String noAuthorizeurls="/api/user/add,/api/user/login,/api/brand/search";

    /**
     * 判断当前的请求中的地址是否在已有的不拦截的地址中存在，如果存在，则返回true 表示不拦截，false表示拦截
     *
     * @param uri   获取到的当前的请求中的地址
     * @return
     */
    public static boolean hasAuthorize(String uri){
        String[] split = noAuthorizeurls.split(",");
        for (String s : split) {
            if (s.equals(uri)) {
                return true;
            }
        }
        return false;
    }
}
