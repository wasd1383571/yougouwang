package cn.itcast.core.service;

import cn.itcast.core.dao.specification.SpecificationDao;
import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.pojo.specification.Specification;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.specification.SpecificationQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import vo.SpecificationVo;

import java.util.List;
import java.util.Map;

/**
 * 规格管理
 */
@Service
//开启事务管理
@Transactional
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationDao specificationDao;

    @Autowired
    private SpecificationOptionDao specificationOptionDao;

    /*
    * 查询所有的商品规格
    * */
    @Override
    public List<Specification> findAll() {

        return specificationDao.selectByExample(null);

    }




    /* *//*
     *
     * 分页查询
     *
     * 不带搜索
     * *//*
    @Override
    public PageResult findPage(Integer page, Integer rows) {

        //分页插件
        PageHelper.startPage(page,rows);

        //查询结果集
        Page<Specification> page1 = (Page<Specification>) specificationDao.selectByExample(null);

        return new PageResult(page1.getTotal(),page1.getResult());
    }*/


    /*
    * 带搜索的分页查询
    * */
    @Override
    public PageResult search(Integer page, Integer rows, Specification specification) {

        //分页插件
        PageHelper.startPage(page,rows);
        //条件对象
        SpecificationQuery specificationQuery = new SpecificationQuery();
        //创建内部对象
        SpecificationQuery.Criteria criteria = specificationQuery.createCriteria();

        //判断是否有值
        if (null != specification.getSpecName() && !"".equals(specification.getSpecName().trim())){

            criteria.andSpecNameLike("%"+specification.getSpecName()+"%");

        }

        //查询结果集
        Page<Specification> page1 = (Page<Specification>) specificationDao.selectByExample(specificationQuery);

        return new PageResult(page1.getTotal(),page1.getResult());
    }

    /*
    * 新建，保存
    * */
    @Override
    public Result add(SpecificationVo vo) {
        //保存到规格
        specificationDao.insertSelective(vo.getSpecification());

        //保存新增规格表
        //获取到新增规格表
        List<SpecificationOption> specificationOptionList = vo.getSpecificationOptionList();
        //遍历
        for (SpecificationOption specificationOption : specificationOptionList) {

            //设置属性
            specificationOption.setSpecId(vo.getSpecification().getId());
            specificationOptionDao.insertSelective(specificationOption);
        }

        return null;
    }
    /*
    * 修改 数据回显
    * */
    @Override
    public SpecificationVo findOne(Long id) {

        SpecificationVo vo = new SpecificationVo();

        vo.setSpecification(specificationDao.selectByPrimaryKey(id));

        SpecificationOptionQuery query = new SpecificationOptionQuery();

        query.createCriteria().andSpecIdEqualTo(id);

        vo.setSpecificationOptionList(specificationOptionDao.selectByExample(query));

        return vo;
    }
    /*
    * 修改保存
    * */
    @Override
    public void update(SpecificationVo vo) {
       //保存规格表
       specificationDao.updateByPrimaryKeySelective(vo.getSpecification());
       //
        SpecificationOptionQuery query = new SpecificationOptionQuery();

        query.createCriteria().andSpecIdEqualTo(vo.getSpecification().getId());
        //先删除原有的数据
        specificationOptionDao.deleteByExample(query);

        //在保存到新增规格表中
        List<SpecificationOption> specificationOptionList = vo.getSpecificationOptionList();

        for (SpecificationOption specificationOption : specificationOptionList) {
             //设置规格表的ID  规格属性表的外键
            specificationOption.setSpecId(vo.getSpecification().getId());
            //添加修改后的数据到表中
            specificationOptionDao.insertSelective(specificationOption);
        }
    }

    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {

            specificationDao.deleteByPrimaryKey(id);


            SpecificationOptionQuery query = new SpecificationOptionQuery();

            query.createCriteria().andSpecIdEqualTo(id);

            specificationOptionDao.deleteByExample(query);


        }


    }
     /*
     * 关联规格
     * */
    @Override
    public List<Map> selectOptionList() {
        return specificationDao.selectOptionList();
    }
}
