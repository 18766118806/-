package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/23 19:29
 **/

@Component
public class SolrUtil {
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
private SolrTemplate solrTemplate;
    public void importItemData() {
        TbItemExample example = new TbItemExample ();
        example.createCriteria ().andStatusEqualTo ("1");  //审核通过
        List<TbItem> items = itemMapper.selectByExample (example);
        for (TbItem item : items) {
            System.out.println (item.getTitle ());
        }

        for (TbItem item : items) {
            String spec = item.getSpec ();
            Map map = JSON.parseObject (spec, Map.class);
            item.setSpecMap (map);
        }
    /* solrTemplate.saveBeans (items);*/
       Query query=new SimpleQuery ("*:*");
       solrTemplate.delete (query);
        solrTemplate.commit ();
        ScoredPage<TbItem> tbItems = solrTemplate.queryForPage (query, TbItem.class);
        System.out.println (tbItems.getTotalElements ());


    }
    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext ("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil = (SolrUtil) context.getBean ("solrUtil");
        solrUtil.importItemData ();
    }


}
