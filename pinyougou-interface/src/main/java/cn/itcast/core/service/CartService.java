package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import vo.Cart;

import java.util.List;

public interface CartService {

    Item findItem(Long itemId);

    List<Cart> addOrderItem(List<Cart> cartList);

    void addcartListFromRedis(List<Cart> cartList, String name);

    List<Cart> findCartListFromRedis(String name);
}
