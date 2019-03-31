package cn.itcast.core.controller;


import cn.itcast.core.pojo.seller.Seller;
import cn.itcast.core.service.SellerService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
* 商家审核
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

    /*
    * 分页查询
    * */
    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody Seller seller){

        return sellerService.search(page,rows,seller);
    }
    /*
    * 详情 数据回显
    * */
    @RequestMapping("findOne")
    public Seller findOne(String id){

        return sellerService.findOne(id);
    }

    /*
    * 商家审核 状态
    * */
    @RequestMapping("updateStatus")
    public Result updateStatus(String sellerId, String status){

        try {
            sellerService.updateStatus(sellerId,status);
            return new Result(true,"审核通过");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"审核未通过");
        }

    }
}
