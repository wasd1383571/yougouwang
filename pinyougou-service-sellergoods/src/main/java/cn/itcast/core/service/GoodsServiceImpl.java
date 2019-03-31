package cn.itcast.core.service;

import cn.itcast.core.dao.good.BrandDao;
import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.dao.seller.SellerDao;

import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsQuery;
import cn.itcast.core.pojo.item.Item;
import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;

import org.apache.activemq.command.ActiveMQDestination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;
import vo.GoodsVo;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("all")
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private ItemCatDao itemCatDao;
    @Autowired
    private BrandDao brandDao;
    @Autowired
    private SellerDao sellerDao;
    @Autowired
    private ItemDao itemDao;





    @Override
    public void add(GoodsVo goodsVo) {



        //设置商品
        //设置状态
        goodsVo.getGoods().setAuditStatus("0");
        //回显商品id
        goodsDao.insertSelective(goodsVo.getGoods());

        //设置商品详情
        goodsVo.getGoodsDesc().setGoodsId(goodsVo.getGoods().getId());
        goodsDescDao.insertSelective(goodsVo.getGoodsDesc());


        //判断是否启用规格
        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())) {
            //设置规格
            //获取库存结果集
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {

                //获取 商品名称 设置标题
                String title = goodsVo.getGoods().getGoodsName();
                //获取规格  {"机身内存":"16G","网络":"联通3G","",""}
                String spec = item.getSpec();
                //转换成字符串
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                //遍历
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {

                    title += " " + entry.getValue();

                }
                item.setTitle(title);

                //设置图片
                //从商品表一堆图片中 第一张
                //[{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
                String itemImages = goodsVo.getGoodsDesc().getItemImages();
                List<Map> mapList = JSON.parseArray(itemImages, Map.class);
                //判断
                if (null != mapList && mapList.size() > 0) {
                    //获取第一张图片 的 url
                    Object url = mapList.get(0).get("url");
                    //设置到 商品规格中的图片中
                    item.setImage((String) url);
                }
                //设置三层分类id
                item.setCategoryid(goodsVo.getGoods().getCategory3Id());

                //设置创建时间
                item.setCreateTime(new Date());
                //设置更新时间
                item.setUpdateTime(new Date());
                //设置商品id
                item.setGoodsId(goodsVo.getGoods().getId());
                //设置商家id
                item.setSellerId(goodsVo.getGoods().getSellerId());
                //设置三级分类名称
                item.setCategory(itemCatDao.selectByPrimaryKey(goodsVo.getGoods().getCategory3Id()).getName());

                //设置品牌名
                       /* //获取品牌id
                        Long brandId = goodsVo.getGoods().getBrandId();
                        //查询数据库
                        Brand brand = brandDao.selectByPrimaryKey(brandId);
                        //获取品牌名称
                        String name = brand.getName();
                        //设置到库存中
                        item.setBrand(name);*/
                item.setBrand(brandDao.selectByPrimaryKey(goodsVo.getGoods().getBrandId()).getName());
                //设置商家名

                item.setSeller(sellerDao.selectByPrimaryKey(goodsVo.getGoods().getSellerId()).getName());

                //保存数据到库层中
                itemDao.insertSelective(item);
            }
        }else{

            //可写 可不写

        }
    }
    /*
     * 分页查询 搜索
     * */
    @Override
    public PageResult search(Integer page, Integer rows, Goods goods) {
        //分页插件
        PageHelper.startPage(page,rows);
        GoodsQuery goodsQuery= new GoodsQuery();
        GoodsQuery.Criteria criteria = goodsQuery.createCriteria();
        //判断
        if (null!=goods.getGoodsName() && !"".equals(goods.getGoodsName().trim())){

            criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
        }
        if (null!=goods.getAuditStatus() && !"".equals(goods.getAuditStatus().trim())){

            criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
        }

        //只查询是null的商品  没有删除的商品
        criteria.andIsDeleteIsNull();
        //只查询当前登录人的商品
        if (null != goods.getSellerId()){

            criteria.andSellerIdEqualTo(goods.getSellerId());
        }
        Page<Goods> page1 = (Page<Goods>) goodsDao.selectByExample(goodsQuery);
        return new PageResult(page1.getTotal(),page1.getResult());
    }
    /*
    * 修改  数据回显
    * */
    @Override
    public GoodsVo findOne(Long id) {

        GoodsVo goodsVo = new GoodsVo();

        //商品数据数显
        goodsVo.setGoods(goodsDao.selectByPrimaryKey(id));

        //商品详情数据回显
        goodsVo.setGoodsDesc(goodsDescDao.selectByPrimaryKey(id));

        //商品库存数据回显
        ItemQuery itemQuery = new ItemQuery();
        ItemQuery.Criteria criteria = itemQuery.createCriteria();
        criteria.andGoodsIdEqualTo(id);

        List<Item> items = itemDao.selectByExample(itemQuery);

        goodsVo.setItemList(items);

        return goodsVo;
    }
    /*
    * 修改
    * */
    @Override
    public void update(GoodsVo goodsVo) {

        //商品
        goodsDao.updateByPrimaryKeySelective(goodsVo.getGoods());

        //商品详情
        goodsDescDao.updateByPrimaryKeySelective(goodsVo.getGoodsDesc());

        //库存
        //先删除
        ItemQuery itemQuery = new ItemQuery();

        ItemQuery.Criteria criteria = itemQuery.createCriteria();

        criteria.andGoodsIdEqualTo(goodsVo.getGoods().getId());

        itemDao.deleteByExample(itemQuery);

        //判断是否启用规格
        if ("1".equals(goodsVo.getGoods().getIsEnableSpec())) {
            //设置规格
            //获取库存结果集
            List<Item> itemList = goodsVo.getItemList();
            for (Item item : itemList) {

                //获取 商品名称 设置标题
                String title = goodsVo.getGoods().getGoodsName();
                //获取规格  {"机身内存":"16G","网络":"联通3G","",""}
                String spec = item.getSpec();
                //转换成字符串
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                //遍历
                Set<Map.Entry<String, String>> entries = map.entrySet();
                for (Map.Entry<String, String> entry : entries) {

                    title += " " + entry.getValue();

                }
                item.setTitle(title);

                //设置图片
                //从商品表一堆图片中 第一张
                //[{"color":"粉色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXq2AFIs5AAgawLS1G5Y004.jpg"},{"color":"黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhVmOXrWAcIsOAAETwD7A1Is874.jpg"}]
                String itemImages = goodsVo.getGoodsDesc().getItemImages();
                List<Map> mapList = JSON.parseArray(itemImages, Map.class);
                //判断
                if (null != mapList && mapList.size() > 0) {
                    //获取第一张图片 的 url
                    Object url = mapList.get(0).get("url");
                    //设置到 商品规格中的图片中
                    item.setImage((String) url);
                }
                //设置三层分类id
                item.setCategoryid(goodsVo.getGoods().getCategory3Id());

                //设置创建时间
                item.setCreateTime(new Date());
                //设置更新时间
                item.setUpdateTime(new Date());
                //设置商品id
                item.setGoodsId(goodsVo.getGoods().getId());
                //设置商家id
                item.setSellerId(goodsVo.getGoods().getSellerId());
                //设置三级分类名称
                item.setCategory(itemCatDao.selectByPrimaryKey(goodsVo.getGoods().getCategory3Id()).getName());

                //设置品牌名
                       /* //获取品牌id
                        Long brandId = goodsVo.getGoods().getBrandId();
                        //查询数据库
                        Brand brand = brandDao.selectByPrimaryKey(brandId);
                        //获取品牌名称
                        String name = brand.getName();
                        //设置到库存中
                        item.setBrand(name);*/
                item.setBrand(brandDao.selectByPrimaryKey(goodsVo.getGoods().getBrandId()).getName());
                //设置商家名

                item.setSeller(sellerDao.selectByPrimaryKey(goodsVo.getGoods().getSellerId()).getName());

                //保存数据到库层中
                itemDao.insertSelective(item);
            }

        }
    }
    /*
     * 审核通过  驳回
     * */


    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private Destination topicPageAndSolrDestination;
    @Autowired
    private Destination queueSolrDeleteDestination;

    @Override
    public void updateStatus(Long[] ids, String status) {

        Goods goods = new Goods();
        goods.setAuditStatus(status);

        for (final Long id : ids) {

            goods.setId(id);

            goodsDao.updateByPrimaryKeySelective(goods);
            //1:判断只能在审核通过的情况下 才能保存商品信息到索引库
            if ("1".equals(status)){

                jmsTemplate.send(topicPageAndSolrDestination, new MessageCreator() {
                    @Override
                    public Message createMessage(Session session) throws JMSException {
                        return session.createTextMessage(String.valueOf(id));
                    }
                });

            }

        }
    }
    /*
     * 删除 批量删除
     * */
    @Override
    public void delete(Long[] ids) {

        Goods goods = new Goods();

        goods.setIsDelete("1");

        for (final Long id : ids) {
            goods.setId(id);
            goodsDao.updateByPrimaryKeySelective(goods);

            jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage(String.valueOf(id));
                }
            });
        }

    }


}
