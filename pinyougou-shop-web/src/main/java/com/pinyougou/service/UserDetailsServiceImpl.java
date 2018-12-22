package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/17 20:45
 **/
//    在配置文件中注入
public class UserDetailsServiceImpl implements UserDetailsService {
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority> ();
        authorities.add (new SimpleGrantedAuthority ("ROLE_USER"));//添加角色,实际应该从数据库查询
        TbSeller seller = sellerService.findOne (username);
        if (seller != null && seller.getStatus ().equals ("1")) {
            return new User (username, seller.getPassword (), authorities);
        } else {
            return null;
        }
    }
}
