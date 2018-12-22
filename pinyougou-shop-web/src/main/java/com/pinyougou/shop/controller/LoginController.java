package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/17 23:28
 **/
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/username")
    public Map username (){
        Map map = new HashMap ();
        String name = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        map.put ("username",name);
        return map;
    }
}
