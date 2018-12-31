package com.pinyougou.page.service.impl;

import com.pinyougou.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/30 21:21
 **/
public class ItemAddListener implements MessageListener {

    @Autowired  //本地注入
    private ItemPageService itemPageService;


    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            Long goodsId = Long.valueOf (textMessage.getText ());
            itemPageService.genItemHtml (goodsId);
        } catch (JMSException e) {
            e.printStackTrace ();
        }
    }
}
