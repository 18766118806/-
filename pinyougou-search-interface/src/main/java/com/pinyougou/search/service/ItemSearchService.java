package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;


/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/24 12:15
 **/
public interface ItemSearchService {
    public Map search(Map searchMap);


    /**
     * 商品审核通过后更新到索引库
     * @param itemList
     */
    public void importList(List itemList);

    /**
     * 根据商品id 删除索引
     */
    public void deleteList( Long [] goodsIds);
}
