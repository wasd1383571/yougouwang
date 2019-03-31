package cn.itcast.core.controller;


import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.service.TypeTemplateService;
import com.alibaba.dubbo.config.annotation.Reference;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SuppressWarnings("all")
@RestController
@RequestMapping("/typeTemplate")
public class TypeTemplateController {

    @Reference
    private TypeTemplateService typeTemplateService;
    /*
    * 模板管理
    * */
    @RequestMapping("findAll")
    public List<TypeTemplate> findAll(){

        return typeTemplateService.findAll();
    }
    /*
    * 分页查询 搜索
    * */
    @RequestMapping("search")
    public PageResult search(Integer page, Integer rows, @RequestBody TypeTemplate typeTemplate){

        return typeTemplateService.search(page,rows,typeTemplate);
    }

    /*
    * 添加 新建
    * */
    @RequestMapping("add")
    public Result add(@RequestBody TypeTemplate typeTemplate){

        try {
            typeTemplateService.add(typeTemplate);
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
    public TypeTemplate findOne(Long id){

        return typeTemplateService.findOne(id);
    }
    /*
    * 修改 保存
    * */
    @RequestMapping("update")
    public Result update(@RequestBody TypeTemplate typeTemplate){

        try {
            typeTemplateService.update(typeTemplate);

            return new Result(true,"修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"修改失败");
        }

    }


}
