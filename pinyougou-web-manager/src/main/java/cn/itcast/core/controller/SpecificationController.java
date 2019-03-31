package cn.itcast.core.controller;


import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.service.SpecificationService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.SpecificationVo;

import java.util.List;
import java.util.Map;

/**
 * 规格管理
 *
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {

    @Reference
    private SpecificationService specificationService;

    //查询所有商品规格
    @RequestMapping("findAll")
    public List<Specification> findAll(){

        return specificationService.findAll();
    }

   /* *//*
    * 分页查询
    *
    * *//*
    @RequestMapping("findPage")
    public PageResult findPage(Integer page,Integer rows){

        return specificationService.findPage(page,rows);
    }*/

    /*
     * 带搜索的分页查询
     *
     * */
    @RequestMapping("search")
    public PageResult search(Integer page,Integer rows,@RequestBody Specification specification){

        return specificationService.search(page,rows,specification);

    }
    /*
    * 新建,保存
    *
    * */
    @RequestMapping("add")
    public Result add(@RequestBody SpecificationVo vo){

        try {
            specificationService.add(vo);
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
    public SpecificationVo  findOne(Long id){

     return specificationService.findOne(id);
    }

    /*
    * 修改保存
    * */
    @RequestMapping("update")
    public Result update(@RequestBody SpecificationVo vo){
        try {
            specificationService.update(vo);
            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }
    }
    /*
    * 删除，批量删除
    * */
    @RequestMapping("delete")
    public Result delete(Long ids[]){
        try {
            specificationService.delete(ids);
            return new Result(true,"删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"删除失败");
        }
    }
    /*
    * 关联规格
    * */
    @RequestMapping("selectOptionList")
    public List<Map> selectOptionList(){

        return specificationService.selectOptionList();
    }

}