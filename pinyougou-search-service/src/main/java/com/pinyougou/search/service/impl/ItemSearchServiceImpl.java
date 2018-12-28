package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/24 12:19
 **/
@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    /**
     * 关键字查询
     */
    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 主线方法
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map search(Map searchMap) {
        HashMap<String, Object> map = new HashMap<> ();
        //查询列表
        Map<String, Object> highLightList = searchHighLightList (searchMap);
        map.putAll (highLightList);
        //分组查询,查询分类列表
        Map<String, List<String>> categoryList = searchCategoryList (searchMap);
        map.putAll (categoryList);
        //从缓存中查询品牌列表和规格及规格选项
        String categoryName = (String) searchMap.get ("category");
        if (!"".equals (categoryName)) {//如果有分类名称
            Map<String, List<Object>> brandListAndSpecList = searchBrandListAndSpecList (categoryName);
            assert brandListAndSpecList != null;
            map.putAll (brandListAndSpecList);
        } else if (categoryList.get ("categoryList").size () > 0) {//没有分类名称,默认用第一个
            Map<String, List<Object>> brandListAndSpecList = searchBrandListAndSpecList (categoryList.get ("categoryList").get (0));
            assert brandListAndSpecList != null;
            map.putAll (brandListAndSpecList);
        }
        return map;

    }


    /**
     * 商品审核通过后更新到索引库
     *
     * @param itemList
     */
    @Override
    public void importList(List itemList) {
        solrTemplate.saveBeans (itemList);
        solrTemplate.commit ();
    }

    /**
     * 根据商品id删除索引
     */
    @Override
    public void deleteList(Long[] goodsIds) {
        SolrDataQuery query = new SimpleQuery ("*:*");
        Criteria criteria = new Criteria ("item_goodsid");
        criteria.in (goodsIds);
        query.addCriteria (criteria);
        solrTemplate.delete (query);
        solrTemplate.commit ();
    }

    /**
     * 查询列表
     *
     * @param searchMap
     * @return
     */
    private Map<String, Object> searchHighLightList(Map searchMap) {
        Map<String, Object> map = new HashMap<> ();
        HighlightQuery query = new SimpleHighlightQuery ();

        //-------------高亮设置-------------
        HighlightOptions highlightOptions = new HighlightOptions ().addField ("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix ("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix ("</em>");//高亮后缀
        query.setHighlightOptions (highlightOptions);//设置高亮选项

        //-------------关键字查询-----------
        String keywords = (String) searchMap.get ("keywords");
        if (!"".equals (keywords) && keywords != null) {
            keywords = keywords.replace (" ", "");
            Criteria criteria = new Criteria ("item_keywords").is (keywords);
            query.addCriteria (criteria);
        }

        //------------添加商品分类过滤条件------------
        if (!"".equals (searchMap.get ("category"))) {
            Criteria filterCriteria = new Criteria ("item_category").is (searchMap.get ("category"));
            FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
            query.addFilterQuery (filterQuery);
        }

        //-------------添加品牌过滤条件----------------
        if (!"".equals (searchMap.get ("brand"))) {
            Criteria filterCriteria = new
                    Criteria ("item_brand").is (searchMap.get ("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
            query.addFilterQuery (filterQuery);
        }

        //-------------添加规格过滤条件---------------
        if (searchMap.get ("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get ("spec");
            for (String key : specMap.keySet ()) {
                Criteria filterCriteria = new Criteria ("item_spec_" + key).is (specMap.get (key));
                FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
                query.addFilterQuery (filterQuery);
            }
        }
        //---------添加价格过滤条件---------------
        if (searchMap.get ("price") != null && !"".equals (searchMap.get ("price"))) {
            String price = (String) searchMap.get ("price");
            String[] prices = price.split ("-");
            if (!"0".equals (prices[0])) { //添加下限
                Integer firstPrice = Integer.parseInt (prices[0]);
                Criteria filterCriteria = new Criteria ("item_price").greaterThanEqual (firstPrice);
                FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
                query.addFilterQuery (filterQuery);
            }
            if (!"#".equals (prices[1])) { //上限
                Integer lastPrice = Integer.parseInt (prices[1]);
                Criteria filterCriteria = new Criteria ("item_price").lessThanEqual (lastPrice);
                FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
                query.addFilterQuery (filterQuery);
            }
        }

        //-------------分页查询-----------------------
        Integer pageNo = (Integer) searchMap.get ("pageNo");
        Integer pageSize = (Integer) searchMap.get ("pageSize");
        if ("".equals (pageNo) || pageNo == null || pageNo < 0) {
            pageNo = 1;
        }
        if ("".equals (pageSize) || pageSize == null) {
            pageSize = 20;
        }
        //开始索引
        query.setOffset ((pageNo - 1) * pageSize);
        //每页显示条数
        query.setRows (pageSize);


        //----------------排序-------------------
        String sortValue = (String) searchMap.get ("sort");//ASC DESC
        String sortField = (String) searchMap.get ("sortField");//排序字段
        if (sortValue != null && "".equals (sortValue)) {
            if (sortValue.equals ("ASC")) {
                Sort sort = new Sort (Sort.Direction.ASC, "item_" + sortField);
                query.addSort (sort);
            }
            if (sortValue.equals ("DESC")) {
                Sort sort = new Sort (Sort.Direction.DESC, "item_" + sortField);
                query.addSort (sort);
            }
        }


        //----------执行查询并获取结果集--------------------
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage (query, TbItem.class);
        for (HighlightEntry<TbItem> h : page.getHighlighted ()) {//循环高亮入口集合
            TbItem item = h.getEntity ();//获取原实体类
            if (h.getHighlights ().size () > 0 && h.getHighlights ().get (0).getSnipplets ().size () > 0) {
                item.setTitle (h.getHighlights ().get (0).getSnipplets ().get (0));//设置高亮的结果
            }
        }
        map.put ("rows", page.getContent ());
        map.put ("totalPages", page.getTotalPages ());
        map.put ("total", page.getTotalElements ());
        return map;
    }


    /**
     * 分组查询分类列表
     *
     * @param searchMap
     * @return
     */
    private Map<String, List<String>> searchCategoryList(Map searchMap) {
        Map<String, List<String>> map = new HashMap<String, List<String>> ();
        List<String> list = new ArrayList<> ();
        Query query = new SimpleQuery ();
        //按照关键字查询 ,相当于where 条件
        Criteria criteria = new Criteria ("item_keywords").is (searchMap.get ("keywords"));
        query.addCriteria (criteria);
        //设置分组选项 ,相当于group by 根据那个字段分组
        GroupOptions groupOptions = new GroupOptions ().addGroupByField ("item_category");
        query.setGroupOptions (groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage (query, TbItem.class);
        //根据域名得到分组结果集 ,域名必须是上面出现过的
        GroupResult<TbItem> groupResult = page.getGroupResult ("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries ();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent ();

        for (GroupEntry<TbItem> entry : content) {
            list.add (entry.getGroupValue ());//将分组结果的名称封装到返回值中
        }
        map.put ("categoryList", list);
        return map;
    }

    /**
     * 根据分类名称查询品牌列表以及规格选,规格选项
     *
     * @param categoryName
     */
    private Map<String, List<Object>> searchBrandListAndSpecList(String categoryName) {
        Map<String, List<Object>> map = new HashMap<> ();
        Long typeId = (Long) redisTemplate.boundHashOps ("categoryList").get (categoryName);
        if (typeId == null) {
            return null;
        }
        List<Object> brandList = (List<Object>) redisTemplate.boundHashOps ("brandList").get (typeId);
        map.put ("brandList", brandList);
        List<Object> specList = (List<Object>) redisTemplate.boundHashOps ("specList").get (typeId);
        map.put ("specList", specList);
        return map;
    }
}
