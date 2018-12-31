package com.pinyougou.page.service;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/28 15:52
 **/
public interface ItemPageService {
    /**
     * 根据商品id生成静态页面
     * param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);

    /**
     * 根据商品id删除静态页面
     * @param goodsIds
     * @return
     */
    public boolean deleteHtml(Long[]goodsIds);
}
