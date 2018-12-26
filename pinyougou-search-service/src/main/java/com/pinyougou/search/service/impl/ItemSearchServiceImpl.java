package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.omg.CORBA.Object;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map search(Map searchMap) {
        Map<String, java.lang.Object> map = new HashMap<> ();
        //查询列表
        Map<String, List<TbItem>> highLightList = searchHighLightList (searchMap);
        map.putAll (highLightList);
        //分组查询
        Map<String, List<String>> categoryList = searchCategoryList (searchMap);
        map.putAll (categoryList);
        //从缓存中查询品牌列表和规格及规格选项
        String categoryName = (String) searchMap.get ("category");
        if (!"".equals (categoryName)) {//如果有分类名称

        } else if (categoryList.get ("categoryList").size () > 0) {
            Map<String, List<Object>> brandListAndSpecList = searchBrandListAndSpecList (categoryList.get ("categoryList").get (0));
            map.putAll (brandListAndSpecList);
        }
        return map;

    }

    /**
     * 高亮显示
     *
     * @param searchMap
     * @return
     */
    private Map<String, List<TbItem>> searchHighLightList(Map searchMap) {
        Map<String, List<TbItem>> map = new HashMap<String, List<TbItem>> ();
        HighlightQuery query = new SimpleHighlightQuery ();

        //-------------高亮设置-------------
        HighlightOptions highlightOptions = new HighlightOptions ().addField ("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix ("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix ("</em>");//高亮后缀
        query.setHighlightOptions (highlightOptions);//设置高亮选项

        //-------------关键字查询-----------
        Criteria criteria = new Criteria ("item_keywords").is (searchMap.get ("keywords"));
        query.addCriteria (criteria);

        //--------------过滤查询(商品分类)------------
        if (!"".equals (searchMap.get ("category"))) {
            Criteria filterCriteria = new Criteria ("item_category").is (searchMap.get ("category"));
            FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
            query.addFilterQuery (filterQuery);
        }

        //---------过滤查询(品牌)----------------
        if (!"".equals (searchMap.get ("brand"))) {
            Criteria filterCriteria = new
                    Criteria ("item_brand").is (searchMap.get ("brand"));
            FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
            query.addFilterQuery (filterQuery);
        }

        //-------------过滤查询(规格过滤)---------------
        if (searchMap.get ("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get ("spec");
            for (String key : specMap.keySet ()) {
                Criteria filterCriteria = new Criteria ("item_spec_" + key).is (specMap.get (key));
                FilterQuery filterQuery = new SimpleFilterQuery (filterCriteria);
                query.addFilterQuery (filterQuery);
            }
        }

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage (query, TbItem.class);
        for (HighlightEntry<TbItem> h : page.getHighlighted ()) {//循环高亮入口集合
            TbItem item = h.getEntity ();//获取原实体类
            if (h.getHighlights ().size () > 0 && h.getHighlights ().get (0).getSnipplets ().size () > 0) {
                item.setTitle (h.getHighlights ().get (0).getSnipplets ().get (0));//设置高亮的结果
            }
        }
        map.put ("rows", page.getContent ());
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
