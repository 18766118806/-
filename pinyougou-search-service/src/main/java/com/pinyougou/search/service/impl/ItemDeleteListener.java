package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.*;
import java.io.Serializable;
import java.util.List;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/30 19:18
 **/
public class ItemDeleteListener  implements MessageListener{
    @Autowired
   private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
       ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] ids = (Long[]) objectMessage.getObject ();
            SolrDataQuery query = new SimpleQuery ("*:*");
            Criteria criteria = new Criteria ("item_goodsid");
            criteria.in (ids);
            query.addCriteria (criteria);
            solrTemplate.delete (query);
            solrTemplate.commit ();
        } catch (JMSException e) {
            e.printStackTrace ();
        }

    }
}
