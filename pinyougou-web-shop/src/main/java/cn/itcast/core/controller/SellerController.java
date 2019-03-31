package cn.itcast.core.controller;

import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.pojo.user.User;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.Result;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
* 商家管理
* */
@SuppressWarnings("all")
@RestController
@RequestMapping("/seller")
public class SellerController {

    @Reference
     private SellerService sellerService;
    /*
    * 商家申请入驻 添加商家
    * */
    @RequestMapping("add")
    public Result add(@RequestBody Seller seller){

        try {
             sellerService.add(seller);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }

}
