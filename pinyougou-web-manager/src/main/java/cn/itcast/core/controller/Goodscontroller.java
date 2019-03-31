package cn.itcast.core.controller;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;

import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.GoodsVo;




/*
* 运营后台  商品管理
* */
@SuppressWarnings("all")
@RestController
@RequestMapping("/goods")
public class Goodscontroller {

    @Reference
    private GoodsService goodsService;

    /*
    * 分页  搜索
    * */
    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody Goods goods){

        return goodsService.search(page,rows,goods);

    }
    /*
    * 详情 数据回显
    * */
    @RequestMapping("findOne")
    public GoodsVo fndOne(Long id){

        return goodsService.findOne(id);
    }
    /*
    * 审核通过  驳回
    * */
    @RequestMapping("updateStatus")
    public Result updateStatus(Long[] ids,String status){

        try {
            goodsService.updateStatus(ids,status);
           return new Result(true,"审核通过");
        } catch (Exception e) {
            e.printStackTrace();
           return new Result(false,"审核未通过");
        }
    }
    /*
    * 删除
    * */
    @RequestMapping("delete")
    public Result delete(Long[] ids){

        try {

            goodsService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }

    }
}


