package cn.itcast.core.service;

import cn.itcast.core.pojo.item.Item;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
@SuppressWarnings("all")
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    //$scope.searchMap={'keywords':'','category':'','brand':'','spec':{},'price':'','pageNo':1,'pageSize':40,'sort':'','sortField':''};

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {

        HashMap<String, Object> resultMap = new HashMap<>();

        //提前处理关键词
        String keywords = searchMap.get("keywords");
        searchMap.put("keywords",keywords.replaceAll(" ", ""));


        //查询分类结果集
        List<String> searchCategoryList = searchCategoryListByKeywords(searchMap);
        resultMap.put("categoryList",searchCategoryList);

        //商品规格分类
        //品牌结果集
        //规格结果集
        if (null != searchCategoryList && searchCategoryList.size()>0) {
            resultMap.putAll(searchBrandListAndSpecListByCategory(searchCategoryList.get(0)));
        }
        //商品列表结果集
        resultMap.putAll(search2(searchMap));
        return resultMap;

    }
    /*
    * 品牌和规格分类
    * */
    public Map<String,Object> searchBrandListAndSpecListByCategory(String Category){
        HashMap<String, Object> resultMap = new HashMap<>();

        //1:通过分类名称查询模板ID
        Object itemCat = redisTemplate.boundHashOps("itemCat").get(Category);
        //2:通过模板ID查询 品牌结果集
        List<Map> brandList = (List<Map>) redisTemplate.boundHashOps("brandList").get(itemCat);
        //3:通过模板ID查询 规格结果集
        List<Map> specList = (List<Map>) redisTemplate.boundHashOps("specList").get(itemCat);

        resultMap.put("brandList",brandList);
        resultMap.put("specList",specList);
        return resultMap;
    }

    /*
    *  分类
    * */
    public List<String> searchCategoryListByKeywords(Map<String, String> searchMap){
        //获取分词条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        SimpleQuery query = new SimpleQuery(criteria);

        //分类信息
        GroupOptions groupOptions = new GroupOptions();
        //分类条件
        groupOptions.addGroupByField("item_category");
         //设置到分词查询条件中
        query.setGroupOptions(groupOptions);

        List<String> categoryList = new ArrayList<>();
        //分类查询结果集
        GroupPage<Item> page = solrTemplate.queryForGroupPage(query, Item.class);
        //通过条件获取到分类的结果集
        GroupResult<Item> category = page.getGroupResult("item_category");
        Page<GroupEntry<Item>> groupEntries = category.getGroupEntries();
        List<GroupEntry<Item>> content = groupEntries.getContent();
        //判断
        if (null != content && content.size()>0){

            for (GroupEntry<Item> itemGroupEntry : content) {

                categoryList.add(itemGroupEntry.getGroupValue());
            }

        }
        return categoryList;
    }

     /*
     * 总条数  总页数  高亮结果集
     * */
    public Map<String, Object> search2(Map<String, String> searchMap) {

        HashMap<String, Object> resultMap = new HashMap<>();



        //获取分词条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //SimpleQuery query = new SimpleQuery(criteria);

        SimpleHighlightQuery query = new SimpleHighlightQuery(criteria);

        //将开始行，每页条数转换成Integer
        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        //开始行
        query.setOffset((Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
        //每页条数
        query.setRows(Integer.parseInt(pageSize));


        //设置高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置域
        highlightOptions.addField("item_title");
        //前缀
        highlightOptions.setSimplePrefix("<em style=color:red>");
        //后缀
        highlightOptions.setSimplePostfix("</em>");

        query.setHighlightOptions(highlightOptions);


        //$scope.searchMap={'category':'','brand':'','spec':{},'price':'','sort':'','sortField':''};
        //条件搜索
        //商品分类
        if (null != searchMap.get("category") && !"".equals(searchMap.get("category"))){
            FilterQuery filterQuery = new SimpleFilterQuery();

            filterQuery.addCriteria(new Criteria("item_category").is(searchMap.get("category")));

            query.addFilterQuery(filterQuery);
        }
        //品牌
        if (null != searchMap.get("brand") && !"".equals(searchMap.get("brand"))){
            FilterQuery filterQuery = new SimpleFilterQuery();

            filterQuery.addCriteria(new Criteria("item_brand").is(searchMap.get("brand")));

            query.addFilterQuery(filterQuery);
        }
        //规格 $scope.searchMap={'spec':{'网络':'移动3G','内存':'16G'},'price':'','sort':'','sortField':''};
        if (null != searchMap.get("spec") && searchMap.get("spec").length()>0){
            FilterQuery filterQuery = new SimpleFilterQuery();
            Map<String,String> spec = JSON.parseObject(searchMap.get("spec"), Map.class);
            Set<Map.Entry<String, String>> entries = spec.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                filterQuery.addCriteria(new Criteria("item_spec_"+key).is(value));
               query.addFilterQuery(filterQuery);
            }

        }

        //价格$scope.searchMap={'price':'','sort':'','sortField':''};
        //0-500, 3000-*
        if (null != searchMap.get("price") && !"".equals(searchMap.get("price"))){

            FilterQuery filterQuery = new SimpleFilterQuery();
            //去除-  转数组、
            String[] prices = searchMap.get("price").split("-");
            //判断是否带*
            if (searchMap.get("price").contains("*")){

                filterQuery.addCriteria(new Criteria("item_price").greaterThanEqual(prices[0]));
            }else{

                filterQuery.addCriteria(new Criteria("item_price").between(prices[0],prices[1],true,true));
            }
            query.addFilterQuery(filterQuery);
        }

        //价格排序
        //$scope.searchMap={'sort':ASC或DESC'','sortField':price或者updatetime''};
        if (null != searchMap.get("sort") && !"".equals(searchMap.get("sort"))){
            //判断是ASC？
            if ("ASC".equals(searchMap.get("sort"))){

                query.addSort(new Sort(Sort.Direction.ASC,"item_"+searchMap.get("sortField")));
            }else{

                query.addSort(new Sort(Sort.Direction.DESC,"item_"+searchMap.get("sortField")));
            }

        }

        //查询高亮结果集
        HighlightPage<Item> page = solrTemplate.queryForHighlightPage(query, Item.class);
        List<HighlightEntry<Item>> highlighted = page.getHighlighted();

        for (HighlightEntry<Item> itemHighlightEntry : highlighted) {
            //entity
            Item item = itemHighlightEntry.getEntity();
            //highlights
            List<HighlightEntry.Highlight> highlights = itemHighlightEntry.getHighlights();

            //判断
            if (null != highlights && highlights.size() > 0) {

                item.setTitle(highlights.get(0).getSnipplets().get(0));
            }
        }


        //分页结果集  默认10
        resultMap.put("rows", page.getContent());
        //总条数
        resultMap.put("total", page.getTotalElements());
        //总页数
        resultMap.put("totalPages", page.getTotalPages());

        return resultMap;

    }

    /*
     * 分页结果集  总条数  总页数
     * */

    public Map<String, Object> search1(Map<String, String> searchMap) {

        HashMap<String, Object> resultMap = new HashMap<>();
        //获取分词条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        SimpleQuery query = new SimpleQuery(criteria);

        //将开始行，每页条数转换成Integer
        String pageNo = searchMap.get("pageNo");
        String pageSize = searchMap.get("pageSize");
        //开始行
        query.setOffset((Integer.parseInt(pageNo) - 1) * Integer.parseInt(pageSize));
        //每页条数
        query.setRows(Integer.parseInt(pageSize));




        //查询结果集
        ScoredPage<Item> page = solrTemplate.queryForPage(query, Item.class);
        //分页结果集  默认10
        resultMap.put("rows", page.getContent());
        //总条数
        resultMap.put("total", page.getTotalElements());
        //总页数
        resultMap.put("totalPages", page.getTotalPages());

        return resultMap;


    }
}
