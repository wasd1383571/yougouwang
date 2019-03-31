package cn.itcast.core.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.Collections;

import java.util.HashSet;
import java.util.Set;

/*
*
* 自定义认证
* */
public class UserDetailServiceImpl implements UserDetailsService{
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {



        Set<GrantedAuthority> grantedAuthority = new HashSet<>();

        grantedAuthority.add(new SimpleGrantedAuthority("ROLE_USER"));

        return new User(username,"",grantedAuthority);
    }
}
