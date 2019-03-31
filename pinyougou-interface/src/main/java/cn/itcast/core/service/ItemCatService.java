package cn.itcast.core.service;

import cn.itcast.core.pojo.item.ItemCat;
import entity.PageResult;

import java.util.List;

public interface ItemCatService {




    List<ItemCat> findByParentId(Long parentId);



    void add(ItemCat itemCat);

    ItemCat findOne(Long id);

    void update(ItemCat itemCat);

    List<ItemCat> findAll();



/*
    void delete(Long[] ids);*/
}
