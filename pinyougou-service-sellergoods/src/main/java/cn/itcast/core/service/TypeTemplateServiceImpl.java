package cn.itcast.core.service;


import cn.itcast.core.dao.specification.SpecificationOptionDao;
import cn.itcast.core.dao.template.TypeTemplateDao;
import cn.itcast.core.pojo.specification.SpecificationOption;
import cn.itcast.core.pojo.specification.SpecificationOptionQuery;
import cn.itcast.core.pojo.template.TypeTemplate;
import cn.itcast.core.pojo.template.TypeTemplateQuery;
import com.alibaba.dubbo.config.annotation.Service;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
//开始事务
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TypeTemplateDao typeTemplateDao;
    @Autowired
    private SpecificationOptionDao specificationOptionDao;
    @Autowired
    private RedisTemplate redisTemplate;

    /*
    * 模板管理
    * */
    @Override
    public List<TypeTemplate> findAll() {
        return typeTemplateDao.selectByExample(null);
    }
    /*
    * 分页查询 搜索
    * */
    @Override
    public PageResult search(Integer page, Integer rows,TypeTemplate typeTemplate) {
        //从数据库查询所有数据  放一份到redis缓存数据库中
        List<TypeTemplate> typeTemplates = typeTemplateDao.selectByExample(null);
        //遍历存入数据库 通过模板ID 查询
        for (TypeTemplate template : typeTemplates) {
            //品牌结果集 通过模板ID 查询  List<Map>  品牌结果集
            List<Map> brandList = JSON.parseArray(template.getBrandIds(), Map.class);
            redisTemplate.boundHashOps("brandList").put(template.getId(),brandList);
            //规格结果集 通过模板ID 查询  List<Map>  规格结果集
            //List<Map> specList = JSON.parseArray(template.getSpecIds(), Map.class);

            //根据模板ID获得规格的列表的数据：
            List<Map> specList = findBySpecList(template.getId());
            redisTemplate.boundHashOps("specList").put(template.getId(),specList);
        }


        //分页插件
        PageHelper.startPage(page,rows);

        //查询结果集
        //创建条件对象
        TypeTemplateQuery typeTemplateQuery = new TypeTemplateQuery();
        //获取内部对象
        TypeTemplateQuery.Criteria criteria = typeTemplateQuery.createCriteria();
        //判断是否有值
        if (null != typeTemplate.getName() && !"".equals(typeTemplate.getName().trim())){

            criteria.andNameLike("%"+typeTemplate.getName()+"%");
        }
        Page<TypeTemplate> page1 = (Page<TypeTemplate>) typeTemplateDao.selectByExample(typeTemplateQuery);

        return new PageResult(page1.getTotal(),page1.getResult());
    }
    /*
    * 新建，添加
    * */
    @Override
    public void add(TypeTemplate typeTemplate) {

        if (null!=typeTemplate.getName() && !"".equals(typeTemplate.getName().trim())){

            typeTemplateDao.insertSelective(typeTemplate);
        }
    }
    /*
    * 修改 数据回显
    * */
    @Override
    public TypeTemplate findOne(Long id) {

        return typeTemplateDao.selectByPrimaryKey(id);
    }
    /*
    * 修改 保存
    * */
    @Override
    public void update(TypeTemplate typeTemplate) {
        typeTemplateDao.updateByPrimaryKeySelective(typeTemplate);

    }
    /*
    * 删除 批量删除
    * */
    @Override
    public void delete(Long[] ids) {
        //遍历 数组ids
        for (Long id : ids) {

            typeTemplateDao.deleteByPrimaryKey(id);
        }

    }
    // 根据模板ID获得规格的列表的数据：
    @Override
    public List<Map> findBySpecList(Long id) {

        TypeTemplate typeTemplate = typeTemplateDao.selectByPrimaryKey(id);

        String specIds = typeTemplate.getSpecIds();

        //将json格式转换成字符串map  [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        List<Map> mapList = JSON.parseArray(specIds, Map.class);

        //遍历  [{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
        //              map1                       map2
        for (Map map : mapList){


            //map1:id
            //map1:text
            //添加一个集合  根据id去查找相应的规格选项
            SpecificationOptionQuery specificationOptionQuery = new SpecificationOptionQuery();

            SpecificationOptionQuery.Criteria criteria = specificationOptionQuery.createCriteria();
                     //Object 不能强转为长整型 Long
                     //只能想转换为基本数据类型，在转换为长整型
            criteria.andSpecIdEqualTo((long)(Integer)map.get("id"));

            List<SpecificationOption> specificationOptions = specificationOptionDao.selectByExample(specificationOptionQuery);

            map.put("options",specificationOptions);
        }
        /*遍历完之后
        List<Map>  长度2
          map1  3
            id:27
            text:网络
            options:List<SpecificationOption> options //从Mysql数据查询

          map2
            id:32
            text:机身内存
            options:List<SpecificationOption> options*/
        return mapList;
    }


}
