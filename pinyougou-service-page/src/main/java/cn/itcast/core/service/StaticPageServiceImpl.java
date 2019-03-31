package cn.itcast.core.service;


import cn.itcast.core.dao.good.GoodsDao;
import cn.itcast.core.dao.good.GoodsDescDao;
import cn.itcast.core.dao.item.ItemCatDao;
import cn.itcast.core.dao.item.ItemDao;
import cn.itcast.core.pojo.good.Goods;
import cn.itcast.core.pojo.good.GoodsDesc;
import cn.itcast.core.pojo.item.Item;

import cn.itcast.core.pojo.item.ItemQuery;
import com.alibaba.dubbo.config.annotation.Service;
import freemarker.template.Configuration;
import freemarker.template.Template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.servlet.ServletContext;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Service
public class StaticPageServiceImpl implements StaticPageService,ServletContextAware{

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Autowired
    private ItemDao itemDao;
    @Autowired
    private GoodsDescDao goodsDescDao;
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private ItemCatDao itemCatDao;




    public void index(Long id){
        // 1：创建Freemarker实现类
        Configuration configuration = freeMarkerConfigurer.getConfiguration();
        Writer out = null;
        /*绝对路径*/
        String path = getPath("/"+id+".html");

        try {

            // 3:加载模板  页面上有标签 读取到内存中  磁盘上  读取到内存 IO流 编码读取的啊
            Template template = configuration.getTemplate("item.ftl");
            //输出流  从内存到磁盘  UTF-8
            out = new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
            //数据
            Map<String,Object> map = new HashMap<>();
            //1:根据商品表的ID查询库存结果集
            ItemQuery itemQuery = new ItemQuery();
            itemQuery.createCriteria().andGoodsIdEqualTo(id);
            List<Item> itemList = itemDao.selectByExample(itemQuery);

            map.put("itemList",itemList);

            //2:查询商品详情表 ID
            GoodsDesc goodsDesc = goodsDescDao.selectByPrimaryKey(id);

            map.put("goodsDesc",goodsDesc);

            //3:商品表
            Goods goods = goodsDao.selectByPrimaryKey(id);

            map.put("goods",goods);

            //4:三级分类 商品分类名称
            map.put("itemCat1",itemCatDao.selectByPrimaryKey(goods.getCategory1Id()).getName());
            map.put("itemCat2",itemCatDao.selectByPrimaryKey(goods.getCategory2Id()).getName());
            map.put("itemCat3",itemCatDao.selectByPrimaryKey(goods.getCategory3Id()).getName());

            //4：处理
            template.process(map,out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null){
                out.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public String getPath(String p){
        return servletContext.getRealPath(p);
    }

    private ServletContext servletContext;
    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;

    }
}
