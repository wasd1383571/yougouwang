package cn.itcast.core.service;


import cn.itcast.core.pojo.seller.Seller;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;
import java.util.Set;

/*
* 自定义认证类
* 定义UserDetailServiceImpl 实现 UserDetailsService 接口
* */
public class UserDetailServiceImpl implements UserDetailsService {


    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {

        //需要从数据库获取到用户名，密码
        Seller seller = sellerService.findOne(id);

        //判断 用户不为空
        if (null != seller){
           //判断 用户商家状态 为审核通过 1
           if("1".equals(seller.getStatus())){
               //获取 权限对象
               Set<GrantedAuthority> authorities = new HashSet<>();
               //设置权限
               authorities.add(new SimpleGrantedAuthority("ROLE_SELLER"));

               User user = new User(seller.getSellerId(),seller.getPassword(),authorities);

            return user;
           }
        }

        return null;
    }
}
