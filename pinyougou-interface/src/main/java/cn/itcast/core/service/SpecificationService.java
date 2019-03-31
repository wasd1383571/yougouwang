package cn.itcast.core.service;


import cn.itcast.core.pojo.specification.Specification;
import entity.PageResult;
import entity.Result;
import vo.SpecificationVo;

import java.util.List;
import java.util.Map;

public interface SpecificationService {


    List<Specification> findAll();

   /* PageResult findPage(Integer page, Integer rows);*/

    PageResult search(Integer page, Integer rows, Specification specification);

    Result add(SpecificationVo vo);

    SpecificationVo findOne(Long id);

    void update(SpecificationVo vo);

    void delete(Long[] ids);

    List<Map> selectOptionList();
}