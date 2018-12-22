package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojogroup.Goods;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 *
 * @author Administrator
 * '
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;

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
     * 增加到两个表
     *
     * @param goods 组合实体类
     * @return
     */
    @RequestMapping("/add")
    public Result add(@RequestBody Goods goods) {
        //设置sellerId
        String sellerId = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        goods.getGoods ().setSellerId (sellerId);
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
        String sellerId2 = goodsService.findOne (goods.getGoods ().getId ()).getGoods ().getSellerId ();
        String sellerId1 = goods.getGoods ().getSellerId ();
        String sellerId = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        if (!sellerId.equals (sellerId1) || !sellerId.equals (sellerId2)) {
            return new Result (false, "非法操作");
        }
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
    public Result delete(Long[] ids) {
        try {
            goodsService.delete (ids);
            return new Result (true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param rows
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        String sellerId = SecurityContextHolder.getContext ().getAuthentication ().getName ();
        goods.setSellerId (sellerId);

        return goodsService.findPage (goods, page, rows);
    }

    /**
     * 商品上下架
     *
     * @param ids
     * @return
     */
    @RequestMapping("/isMarketable")
    public Result isMarketable(Long[] ids, String status) {
        String sellerId = SecurityContextHolder.getContext ().getAuthentication ().getName ();  //获取登录商家ID

        for (Long id : ids) {
            boolean auditStatus = goodsService.findOne (id).getGoods ().getAuditStatus ().equals ("1");   //是否已通过审核
            String sellerId1 = goodsService.findOne (id).getGoods ().getSellerId ();
            if (!sellerId.equals (sellerId1)) {
                return new Result (false, "非法操作");
            } else if (!auditStatus) {
                return new Result (false, "有商品未通过审核");
            }
        }

        try {
            goodsService.isMarketable (ids, status);
            return new Result (true, "操作成功");
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false, "操作失败");
        }

    }

}
