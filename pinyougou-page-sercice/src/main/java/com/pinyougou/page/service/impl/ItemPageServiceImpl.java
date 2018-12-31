package com.pinyougou.page.service.impl;


import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import freemarker.template.Configuration;
import freemarker.template.Template;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;

import java.io.Serializable;
import java.io.Writer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/28 16:05
 **/
@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value (value = "${pagedir}")
    private String pagedir;

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper ;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Override

    /**
     * 根据商品id生成静态页面
     * param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfig.getConfiguration ();
        try {
            Template template = configuration.getTemplate ("item.ftl");
            TbGoods goods = goodsMapper.selectByPrimaryKey (goodsId);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey (goodsId);
            String category1Name = itemCatMapper.selectByPrimaryKey (goods.getCategory1Id ()).getName ();
            String category2Name = itemCatMapper.selectByPrimaryKey (goods.getCategory2Id ()).getName ();
            String category3Name = itemCatMapper.selectByPrimaryKey (goods.getCategory3Id ()).getName ();

            TbItemExample example = new TbItemExample ();
            TbItemExample.Criteria criteria = example.createCriteria ();
            criteria.andStatusEqualTo ("1");
            criteria.andGoodsIdEqualTo (goodsId);
            example.setOrderByClause ("is_default DESC");
            List<TbItem> itemList = itemMapper.selectByExample (example);
            Map<String, java.io.Serializable> dataModel = new HashMap<> ();
            dataModel.put ("goods",goods);
            dataModel.put ("goodsDesc",goodsDesc);
            dataModel.put ("category1Name",category1Name);
            dataModel.put ("category2Name",category2Name);
            dataModel.put ("category3Name",category3Name);
            dataModel.put ("itemList", (Serializable) itemList);
            Writer writer = new FileWriter (pagedir+goodsId+".html");
            template.process (dataModel,writer);
            writer.close ();
            return true;
        } catch (Exception e) {
            e.printStackTrace ();
            return false;
        }



    }

    /**
     * 根据商品id删除静态页面
     * @param goodsIds
     * @return
     */
    @Override
    public boolean deleteHtml(Long[] goodsIds) {
        for (Long goodsId : goodsIds) {
            new File (pagedir+goodsId+".html").delete ();
            return false;
        }
        return false;

    }
}
