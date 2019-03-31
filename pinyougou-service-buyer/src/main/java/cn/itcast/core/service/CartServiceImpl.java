package cn.itcast.core.service;

import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import vo.Cart;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemDao itemDao;

   /*
   * 查询商品详情
   * */
    @Override
    public Item findItem(Long itemId) {

        return itemDao.selectByPrimaryKey(itemId);
    }
    /*
    * 添加商品到购物车中  购物车页面初始化用
    * */
    @Override
    public List<Cart> addOrderItem(List<Cart> cartList) {
        //遍历购物车 给每个购物车填充数据
        for (Cart cart : cartList) {
            //获取购物车中的结果集
            List<OrderItem> orderItemList = cart.getOrderItemList();
            //循环遍历商品结果集 设置商品需要的东西
            for (OrderItem orderItem : orderItemList) {
                //调上边方法 获取商品详情
                Item item = findItem(orderItem.getItemId());
                //图片
                orderItem.setPicPath(item.getImage());
                //标题
                orderItem.setTitle(item.getTitle());
                //价格
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * orderItem.getNum()));

                //商家名称
                cart.setSellerName(item.getSeller());
            }

        }

       //返回购物车
        return cartList;
    }
    /*
    * 合并 新老购物车 到缓存中
    *
    * */
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void addcartListFromRedis(List<Cart> newcartList, String name) {
        //获取缓存中的老购物车
        List<Cart> oldcartList = (List<Cart>) redisTemplate.boundHashOps("cart").get(name);
        //合并
        oldcartList = mergeCart(newcartList,oldcartList);
        //重新写会缓存
        redisTemplate.boundHashOps("cart").put(name,oldcartList);
    }
   /*
   * 获取缓存中的购物车
   * */
    @Override
    public List<Cart> findCartListFromRedis(String name) {

        //通过当前登录人来回去缓存中的购物车
        return (List<Cart>) redisTemplate.boundHashOps("cart").get(name);
    }


    //新老车 合并
    public List<Cart> mergeCart(List<Cart> newCartList,List<Cart> oldCartList){
        //判断新车不为空
        if (null != newCartList && newCartList.size()>0) {
            //判断老车不为空
            if (null != oldCartList && oldCartList.size() > 0) {
                // 遍历新车集
                for (Cart newcart : newCartList) {
                    //1)判断当前款商品的商家  是否在购物车集合中 众多商家中已存在
                    int newindex = oldCartList.indexOf(newcart);
                    //--1:存在 >=0 存在 同时获取存在的角标位置   -1 表示不存在
                    if (newindex != -1){
                        //2)判断当前款商品在此商家下众多商品中已存在
                        Cart oldcart = oldCartList.get(newindex);
                        //获取老车 中的商品结果集
                        List<OrderItem> orderItemList = oldcart.getOrderItemList();
                        //取出新车 中的结果集
                        List<OrderItem> neworderItemList = newcart.getOrderItemList();
                        //遍历新车结果集
                        for (OrderItem neworderItem : neworderItemList) {
                            //判断老车的商品结果集中是否存在新车商品结果集中的商品
                            int indexOf = orderItemList.indexOf(neworderItem);
                            //--1:存在 >=0 存在 同时获取存在的角标位置   -1 表示不存在
                            if (indexOf != -1){
                                //获取老车 结果集的角标位置 已存在商品
                                OrderItem orderItem = orderItemList.get(indexOf);
                                ////追加商品的数量 （新商品数量 + 老商品数量）
                                orderItem.setNum(orderItem.getNum() + neworderItem.getNum());
                            }else{
                                //在老车结果集下添加新的商品
                                orderItemList.add(neworderItem);
                            }
                        }

                    }else{
                        //老车下边添加新的购物车
                        oldCartList.add(newcart);
                    }
                }

            } else {
                // 老车为空 返回新车
                return newCartList;
            }
        }
        //新车为空 或者 商品追加到老车中 返回老车
        return oldCartList;
    }
}
