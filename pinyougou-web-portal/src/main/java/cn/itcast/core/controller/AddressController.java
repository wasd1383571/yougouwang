package cn.itcast.core.controller;

import cn.itcast.core.pojo.address.Address;
import cn.itcast.core.service.AddressService;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
* 结算页
* */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Reference
    private AddressService addressService;
    /*
    * 结算页 获取当前登录人地址
    * */
    @RequestMapping("findListByLoginUser")
    public List<Address> findListByLoginUser(){
           //获取当前登录人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        return addressService.findAddressLoginUser(name);


    }
}
