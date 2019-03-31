package cn.itcast.core.controller;

import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.order.OrderItem;
import cn.itcast.core.service.CartService;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vo.Cart;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/*
* 加入购物车
*
* */
@SuppressWarnings("all")
@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;



    @RequestMapping("addGoodsToCartList")
    //@CrossOrigin(origins="http://localhost:8088",allowCredentials="true")
    //@CrossOrigin：用来解决页面跨域问题
    @CrossOrigin(origins="http://localhost:9103")
    public Result addGoodsToCartList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response){

        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:9003");
        //response.setHeader("Access-Control-Allow-Credentials", "true");

        List<Cart> cartList = null;//用于提升作用域
        try {
           // 1:获取Cookie
            Cookie[] cookies = request.getCookies();
            //判断是否为空
            if (null != cookies && cookies.length>0){
                //遍历
                for (Cookie cookie : cookies) {
                    //判断  cookie中是否有 购物车 ："CART"
                    if ("CART".equals(cookie.getName())){
                        //2：获取Cookie中购物车 也就是cookie的值
                        String value = cookie.getValue();
                        //取出cookie值后 ，对值进行编码
                        String decode = URLDecoder.decode(value, "UTF-8");
                        //Cookie只能保存String类型 不能保存对象 将对象转成JSon格式字符串 取出串换回对象
                        cartList = JSON.parseArray(decode, Cart.class);
                    }

                }
            }

            // 3:没有 创建购物车
            if (null == cartList){
                cartList = new ArrayList<>();
            }

           //4：追加当前款
             //创建新的购物车
            Cart newcart = new Cart();
             //通过商品id获取商品详情
            Item item = cartService.findItem(itemId);
             //新购物车需要 商家id  商家名称（不给）  商品结果集
             //设置商家id
            newcart.setSellerId(item.getSellerId());
             //创建商品
            OrderItem neworderItem = new OrderItem();
             //设置商品数量
            neworderItem.setNum(num);
             //设置商品id
            neworderItem.setItemId(itemId);
             //创建商品结果集
            List<OrderItem> newOrderList = new ArrayList<>();
             //将新商品添加到商品结果集中
            newOrderList.add(neworderItem);
             //商品结果集设置到新购物车中
            newcart.setOrderItemList(newOrderList);
            //1)判断当前款商品的商家  是否在购物车集合中 众多商家中已存在
            int newindex = cartList.indexOf(newcart);
            //--1:存在 >=0 存在 同时获取存在的角标位置   -1 表示不存在
            if (newindex != -1){

                //2)判断当前款商品在此商家下众多商品中已存在
                Cart oldcart = cartList.get(newindex);
                //获取老购物车中的商品结果集
                List<OrderItem> oldorderItemList = oldcart.getOrderItemList();
                //判断老商品结果集中有没有新商品
                int index = oldorderItemList.indexOf(neworderItem);
                //--1:存在  >=0 存在 同时获取存在的角标位置   -1 表示不存在
                if (index != -1){
                    //获取老商品结果集中已存在的商品角标
                    OrderItem orderItem = oldorderItemList.get(index);
                    //追加商品的数量 （新商品数量 + 老商品数量）
                    orderItem.setNum(neworderItem.getNum()+orderItem.getNum());

                }else{
                    //--2：不存在 新建一个商品并放到此商家下
                    oldorderItemList.add(neworderItem);
                }

            }else{
                //--2:不存在  直接创建新的购物车（因为一个购物车对应一个商家，并在此商家下创建新商品）
                  cartList.add(newcart);
            }

            //判断是否登录
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!"anonymousUser" .equals(name)){
                //5：保存以上数据到Redis缓存中
                //获取缓存中数据 合并之前 再保存到缓存中替换到之前缓存中的数据
                cartService.addcartListFromRedis(cartList,name);

                //        清空Cookie
                Cookie cookie = new Cookie("CART", null);
                cookie.setMaxAge(0);
                cookie.setPath("/");
                response.addCookie(cookie);


            }else{

                // 5:创建Cookie 保存购物车到Cookie 回写Cookie到浏览器
                //设置cookie值时，对其进行编码
                String encode = URLEncoder.encode(JSON.toJSONString(cartList), "UTF-8");
                Cookie cookie = new Cookie("CART",encode);
                cookie.setMaxAge(60*60*24*3);
                cookie.setPath("/");
                response.addCookie(cookie);
            }


            // 6：new Result(true,加入购物车成功
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"添加失败");
        }

    }
    @RequestMapping("findCartList")
    public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) throws UnsupportedEncodingException {

        List<Cart> cartList = null;//提升作用域
        //1：获取Cookie
        Cookie[] cookies = request.getCookies();
        //常规判断  确定cookie不为空 并且长度大于0
        if (null != cookies && cookies.length>0){
            //循环遍历数组 cookies
            for (Cookie cookie : cookies) {

                //判断  cookie中是否有 购物车 ："CART"
                if ("CART".equals(cookie.getName())){
                    //2：获取Cookie中购物车 也就是cookie的值
                    String value = cookie.getValue();
                    //取出cookie值后 ，对值进行编码
                    String decode = URLDecoder.decode(value, "UTF-8");
                    //Cookie只能保存String类型 不能保存对象 将对象转成JSon格式字符串 取出串换回对象
                    cartList = JSON.parseArray(decode, Cart.class);

                }
            }
        }
        //判断是否登录
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        //anonymousUser：安全框架 在没有登录人登录的情况下提供的 匿名用户登录，确保获取登录人时，不会报空指针异常
        if (!"anonymousUser".equals(name)){
           //已登录
           //3:有 追加购物车到缓存中
            //判断购物车不为空
            if (null != cartList){
               //追加购物车到缓存中
               cartService.addcartListFromRedis(cartList,name);
               //清空Cookie
                 //创建cookie
                Cookie cookie = new Cookie("CART", null);
                 //设置时间 0：表示立即清空
                cookie.setMaxAge(0);
                 //设置路径
                cookie.setPath("/");
                 //将新的cookie响应回浏览器，
                response.addCookie(cookie);
            }

           //4：获取缓存中完整的购物车
            cartList = cartService.findCartListFromRedis(name);


        }


        //未登陆
        //5：有  装满

        if (null != cartList){
            cartList = cartService.addOrderItem(cartList);

        }
        //6：回显
        return cartList;
    }

}
