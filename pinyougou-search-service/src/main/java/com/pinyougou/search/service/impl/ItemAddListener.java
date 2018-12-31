package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/30 18:30
 **/
public class ItemAddListener implements MessageListener {
    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {   //message 为监听到的消息
        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText ();
            List<TbItem> itemList = JSON.parseArray (text, TbItem.class);
            solrTemplate.saveBeans (itemList);
            solrTemplate.commit ();
        } catch (JMSException e) {
            e.printStackTrace ();
        }

    }
}
