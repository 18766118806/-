package com.pinyougou.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/17 12:03
 **/
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/username")
    public Map userName() {
        String userName = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        Map map = new HashMap ();
        map.put ("username", userName);
        return map;
    }
}
