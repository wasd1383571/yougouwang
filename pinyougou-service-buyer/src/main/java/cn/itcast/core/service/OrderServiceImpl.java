package cn.itcast.core.service;

import cn.itcast.common.utils.IdWorker;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.log.PayLogDao;
import cn.itcast.core.dao.order.OrderDao;
import cn.itcast.core.dao.order.OrderItemDao;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.log.PayLog;
import cn.itcast.core.pojo.order.Order;
import cn.itcast.core.pojo.order.OrderItem;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import vo.Cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;
    @Autowired
    private ItemDao itemDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private PayLogDao payLogDao;

    /*
    *提交订单
    * */
    @Override
    public void submitOrder(Order order) {

        long payMoney = 0;
        List<Long> ids = new ArrayList<>();

        //获取缓存中的购物车
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cart").get(order.getUserId());
        //遍历购物车
        for (Cart cart : cartList) {
        //设置订单id
        long id = idWorker.nextId();
        ids.add(id);
        order.setOrderId(id);

        //实付金额
        double money = 0 ;
        //支付状态
        order.setStatus("1");
        //订单创建时间
        order.setCreateTime(new Date());
        //订单更新时间
        order.setUpdateTime(new Date());

        //订单来源
        order.setSourceType("2");
        //商家id
        order.setSellerId(cart.getSellerId());

            //获取购物车中的商品结果集

            List<OrderItem> orderItemList = cart.getOrderItemList();
             //遍历商品结果集
            for (OrderItem orderItem : orderItemList) {

                //查询商品详情
                Item item = itemDao.selectByPrimaryKey(orderItem.getItemId());
                //商品id
                long itemId = idWorker.nextId();
                orderItem.setId(itemId);
                //设置库存id
                orderItem.setGoodsId(item.getGoodsId());
                //设置订单id
                orderItem.setOrderId(id);
                //设置标题
                orderItem.setTitle(item.getTitle());
                //设置单价
                orderItem.setPrice(item.getPrice());
                //小计
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                //追加金额
                money += orderItem.getTotalFee().doubleValue();
                //图片
                orderItem.setPicPath(item.getImage());
                //商家id
                orderItem.setSellerId(item.getSellerId());

                //保存
                orderItemDao.insertSelective(orderItem);
            }

            //实付金额
            order.setPayment(new BigDecimal(money));

            payMoney += order.getPayment().longValue();
            //保存
            orderDao.insertSelective(order);
        }

        //生成支付日志 多个订单统一支付
        PayLog payLog = new PayLog();
        //设置订单号
        payLog.setOutTradeNo(String.valueOf(idWorker.nextId()));
        //订单创建时间
        payLog.setCreateTime(new Date());
        //支付金额
        payLog.setTotalFee(payMoney * 100);
        //用户ID
        payLog.setUserId(order.getUserId());
        //交易状态
        payLog.setTradeState("0");
        //订单编号 [986096985763217408, 986096985784188928]
        payLog.setOrderList(String.valueOf(ids).replace("[","").replace("]",""));
        //支付类型
        payLog.setPayType("1");

        //保存
        payLogDao.insertSelective(payLog);

        //存redis缓存中一份
        redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
    }
}
