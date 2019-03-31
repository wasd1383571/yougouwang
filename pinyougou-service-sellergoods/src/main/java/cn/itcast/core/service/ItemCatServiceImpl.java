package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.pojo.item.ItemCat;
import cn.itcast.core.pojo.item.ItemCatQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.opensaml.xml.encryption.P;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ItemCatServiceImpl implements ItemCatService {

    @Autowired
    private ItemCatDao itemCatDao;
    //注入缓存
    @Autowired
    private RedisTemplate redisTemplate;



    /*
    * 根据父id查询分类
    * */
    @Override
    public List<ItemCat> findByParentId(Long parentId) {
        //从数据库查出分类数据  放入一份到redis缓存库
        List<ItemCat> itemCats = itemCatDao.selectByExample(null);
        //遍历数据  放入缓存
        for (ItemCat itemCat : itemCats) {

            redisTemplate.boundHashOps("itemCat").put(itemCat.getName(),itemCat.getTypeId());

        }


        //创建条件对象
        ItemCatQuery itemCatQuery = new ItemCatQuery();
        //获取内部对象
        ItemCatQuery.Criteria criteria = itemCatQuery.createCriteria();

        criteria.andParentIdEqualTo(parentId);

        return itemCatDao.selectByExample(itemCatQuery);
    }

    /*
    * 添加  保存  新建
    * */
    @Override
    public void add(ItemCat itemCat) {

        itemCatDao.insertSelective(itemCat);
    }
    /*
    * 修改 数据回显
    * */
    @Override
    public ItemCat findOne(Long id) {

        return itemCatDao.selectByPrimaryKey(id);
    }
    /*
    * 修改 保存
    * */
    @Override
    public void update(ItemCat itemCat) {

        itemCatDao.updateByPrimaryKeySelective(itemCat);
    }

    @Override
    public List<ItemCat> findAll() {

        return itemCatDao.selectByExample(null);
    }


    /*
    * 删除 批量删除
    * *//*
    @Override
    public void delete(Long[] ids) {
        //遍历
        for (Long id : ids) {

            itemCatDao.deleteByPrimaryKey(id);
        }

    }*/
}
