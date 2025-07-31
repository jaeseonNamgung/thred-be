package com.thred.datingapp.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    private final int COOKIE_EXPIRATION_TIME=14 * 24 * 60 * 60;
    private final int COOKIE_EXPIRATION = 0;

    public void addCookie(HttpServletResponse response,String key,String value){
        Cookie cookie=new Cookie(key,value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRATION_TIME);
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletResponse response,String key){
        Cookie cookie = new Cookie(key,null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(COOKIE_EXPIRATION);
        response.addCookie(cookie);
    }
}
