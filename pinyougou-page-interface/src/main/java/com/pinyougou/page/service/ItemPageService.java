package com.pinyougou.page.service;

/*
 * @Author:  Yajun_Xu
 * @Create: 2018/12/28 15:52
 **/
public interface ItemPageService {
    /**
     * 根据商品id查询数据
     * param goodsId
     * @return
     */
    public boolean genItemHtml(Long goodsId);
}
