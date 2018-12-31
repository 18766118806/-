package com.pinyougou.manager.controller;


import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojogroup.Goods;
import org.apache.activemq.command.ActiveMQDestination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * controller
 *
 * @author Administrator
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    @Autowired
    private JmsTemplate jmsTemplate;
    @Autowired
    private ActiveMQDestination queueSolrAddDestination;  //点对点 ,solr 添加
    @Autowired
    private ActiveMQDestination queueSolrDeleteDestination;  //点对点 ,solr 删除
    @Autowired
    private ActiveMQDestination topicFreemarkerAddDestination;   //静态页面生成
    @Autowired
    private ActiveMQDestination topicFreemarkerDeleteDestination;   //静态页面删除

    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll ();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage (page, rows);
    }

    /**
     * 增加
     *
     * @param goods
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        try {
            goodsService.add (goods);
            return new Result (true, "增加成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "增加失败");
        }
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update (goods);
            return new Result (true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {
        return goodsService.findOne (id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(final Long[] ids) {
        try {
            //更新数据库
            goodsService.delete (ids);
            // 更新索引库 , 一对一
            jmsTemplate.send (queueSolrDeleteDestination, new MessageCreator () {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createObjectMessage (ids);
                }
            });
            //删除静态页面  , 广播
            jmsTemplate.send (topicFreemarkerDeleteDestination, new MessageCreator () {
                @Override
                public Message createMessage(Session session) throws JMSException {
                    return session.createTextMessage (Arrays.toString (ids));
                }
            });
            return new Result (true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage (goods, page, rows);
    }

    /**
     * 运营商批量审核
     *
     * @param ids
     * @param status
     * @return
     */
    @RequestMapping("/updateStatus")
    public Result updateStatus(final Long[] ids, String status) {
        try {
            goodsService.updateStatus (ids, status);
            //更新到索引库
            if ("1".equals (status)) {
                final List<TbItem> itemList = goodsService.selectItemByGoodsId (ids);
                if (itemList.size () > 0) {
                    final String itemListStr = JSON.toJSONString (itemList);
                    jmsTemplate.send (queueSolrAddDestination, new MessageCreator () {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage (itemListStr);
                        }
                    });

                    //生成静态页面
                    for (final Long goodsId : ids) {
                        //  itemPageService.genItemHtml (goodsId);
                        jmsTemplate.send (topicFreemarkerAddDestination, new MessageCreator () {
                            @Override
                            public Message createMessage(Session session) throws JMSException {
                                return session.createTextMessage (goodsId + "");
                            }
                        });
                    }
                }


            }


            return new Result (true, "审核成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "审核失败");
        }

    }


    /**
     * 测试
     *
     * @param goodsId
     */
    @RequestMapping("/genHtml")
    public void genHtml(Long goodsId) {
        // itemPageService.genItemHtml (goodsId);
    }
}


