package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.service.ItemCatService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
* 分类管理
* */
@SuppressWarnings("all")
@RestController
@RequestMapping("/itemCat")
public class ItemCatController {

    @Reference
    private ItemCatService itemCatService;

    /*
    * 根据父id查询分类
    * */
    @RequestMapping("findByParentId")
    public List<ItemCat> findByParentId(Long parentId){

        return itemCatService.findByParentId(parentId);
    }
   /*
   * 查询全部
   *
   * */
   @RequestMapping("findAll")
   public List<ItemCat> findAll(){

       return itemCatService.findAll();
   }

    /*
    * 新建 保存
    * */
    @RequestMapping("add")
    public Result add(@RequestBody ItemCat itemCat){

        try {
            itemCatService.add(itemCat);
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }
    }
    /*
    * 修改 数据回显
    * */
    @RequestMapping("findOne")
    public ItemCat findOne(Long id){

        return itemCatService.findOne(id);
    }
    /*
    * 修改 保存
    * */
    @RequestMapping("update")
    public Result update(@RequestBody ItemCat itemCat){

        try {
            itemCatService.update(itemCat);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    /*
    * 删除 批量删除
    * *//*
    @RequestMapping("delete")
    public Result delete(Long[] ids){

        try {
            itemCatService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }*/
}
