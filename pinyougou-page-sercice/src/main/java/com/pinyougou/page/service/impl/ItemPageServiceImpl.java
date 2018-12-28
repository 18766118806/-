package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
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
    @Override
    public boolean genItemHtml(Long goodsId) {
        Configuration configuration = freeMarkerConfig.getConfiguration ();
        try {
            Template template = configuration.getTemplate ("item.ftl");
            TbGoods goods = goodsMapper.selectByPrimaryKey (goodsId);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey (goodsId);
            Map<String, java.io.Serializable> dataModel = new HashMap<> ();
            dataModel.put ("goods",goods);
            dataModel.put ("goodsDesc",goodsDesc);
            Writer writer = new FileWriter (pagedir+goodsId+".html");
            template.process (dataModel,writer);
            writer.close ();
            return true;
        } catch (Exception e) {
            e.printStackTrace ();
        }


        return false;
    }
}
