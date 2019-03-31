package cn.itcast.core.controller;



import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.service.GoodsService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.GoodsVo;


/*
* 商品管理 新增商品
* */
@SuppressWarnings("all")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    /*
    * 新增商品
    * */
    @RequestMapping("add")
    public Result add(@RequestBody GoodsVo goodsVo){

        try {
            //获取当前登录人
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            //设置商户id
            goodsVo.getGoods().setSellerId(name);
            goodsService.add(goodsVo);
           return new Result(true,"新增成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"新增失败");
        }
    }
    /*
    * 分页查询 搜索
    * */
    @RequestMapping("search")
    public PageResult search(Integer page,Integer rows,@RequestBody Goods goods){
          //获取当前登录人
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        goods.setSellerId(name);
        return goodsService.search(page,rows,goods);
    }
    /*
    * 修改 数据回显
    *
    * */
    @RequestMapping("findOne")
    public GoodsVo findOne(Long id){

        return goodsService.findOne(id);
    }

    /*
    * 修改
    * */
    @RequestMapping("update")
    public Result update(@RequestBody GoodsVo goodsVo){

        try {
           goodsService.update(goodsVo);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }

    /*
    * 删除 批量删除
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
