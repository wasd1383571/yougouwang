package cn.itcast.core.service;

import cn.itcast.core.dao.ad.ContentDao;
import cn.itcast.core.pojo.ad.Content;
import cn.itcast.core.pojo.ad.ContentQuery;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ContentServiceImpl implements ContentService {


        @Autowired
        private ContentDao contentDao;
        @Autowired
        private RedisTemplate redisTemplate;

        @Override
        public List<Content> findAll() {
            List<Content> list = contentDao.selectByExample(null);
            return list;
        }

        @Override
        public PageResult findPage(Content content, Integer pageNum, Integer pageSize) {
            PageHelper.startPage(pageNum, pageSize);
            Page<Content> page = (Page<Content>)contentDao.selectByExample(null);
            return new PageResult(page.getTotal(), page.getResult());
        }
        /*
        * 添加 更新广告
        * 更新缓存中的广告
        * */
        @Override
        public void add(Content content) {
            //新建或添加 广告时  清除缓存对应id的所有广告
            //进入页面后 回去数据库查 并缓存一份到redis

            redisTemplate.boundHashOps("context").delete(content.getCategoryId());
            contentDao.insertSelective(content);

        }
        /*
        * 修改广告
        * */
        @Override
        public void edit(Content content) {
            //先通过id 获取到所有的图片的对应的CategoryId
            Content content1 = contentDao.selectByPrimaryKey(content.getId());
            //判断 获取到的CategoryId与要被修改的广告id是否一致
           /*  未简化之前的代码
           if (content1.getCategoryId().equals(content.getCategoryId())){

                //广告被修改后  清除缓存中对应id下的所有广告
                redisTemplate.boundHashOps("content").delete(content.getCategoryId());

            }else{
                //先清除被修改的内容标题里的所有图片缓存
                redisTemplate.boundHashOps("content").delete(content1.getCategoryId());

                //广告被修改后  清除缓存中对应id下的所有广告
                redisTemplate.boundHashOps("content").delete(content.getCategoryId());
            }*/
           //简化之后的代码
            if (!content1.getCategoryId().equals(content.getCategoryId())){
                //先清除被修改的内容标题里的所有图片缓存
                redisTemplate.boundHashOps("content").delete(content1.getCategoryId());
            }

            //广告被修改后  清除缓存中对应id下的所有广告
            redisTemplate.boundHashOps("content").delete(content.getCategoryId());

            contentDao.updateByPrimaryKeySelective(content);
        }

        @Override
        public Content findOne(Long id) {
            Content content = contentDao.selectByPrimaryKey(id);
            return content;
        }
        /*
        * 删除广告  删除缓存中的广告
        * */
        @Override
        public void delAll(Long[] ids) {
            Content content = new Content();
            if(ids != null){
                for(Long id : ids){

                    //删除广告 同时删除缓存中对应id的广告
                    //当进入页面时，回去数据库查找，并保存一份到redis数据库缓存
                    redisTemplate.boundHashOps("content").delete(content.getCategoryId());
                    contentDao.deleteByPrimaryKey(id);
                }
            }
        }
     /*
     * 轮播图
     * */
    @Override
    public List<Content> findByCategoryId(Long categoryId) {

        //当页面加载时  先去缓存中找图片
        List<Content> contents = (List<Content>) redisTemplate.boundHashOps("content").get(categoryId);
        //如果没有  再去数据库查
        if (null == contents || contents.size() == 0){
        ContentQuery contentQuery = new ContentQuery();

        ContentQuery.Criteria criteria = contentQuery.createCriteria();

        criteria.andCategoryIdEqualTo(categoryId).andStatusEqualTo("1");
        //排序
        contentQuery.setOrderByClause("sort_order desc");

        contents = contentDao.selectByExample(contentQuery);
            //3:查询出来之后放到缓存中一份
        redisTemplate.boundHashOps("content").put(categoryId,contents);
            //存活时间
        redisTemplate.boundHashOps("content").expire(8, TimeUnit.HOURS);

    }
        return contents;
    }

}
