package com.pinyougou.sellergoods.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample (null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample (null);
        return new PageResult (page.getTotal (), page.getResult ());
    }


    /**
     * 增加到三个表
     */
    @Override
    public void add(Goods goods) {
        TbGoods tbGoods = goods.getGoods ();
        tbGoods.setAuditStatus ("0");//设置状态
        goodsMapper.insert (tbGoods);
        Long goodsId = tbGoods.getId ();
        TbGoodsDesc goodsDesc = goods.getGoodsDesc ();
        goodsDesc.setGoodsId (goodsId);
        goodsDescMapper.insert (goodsDesc);

        //启用spec
        if ("1".equals (tbGoods.getIsEnableSpec ())) {
            for (TbItem item : goods.getItemList ()) {
                //设置标题
                String title = goods.getGoods ().getGoodsName ();
                Map<String, Object> map = JSON.parseObject (item.getSpec ());
                for (Object key : map.keySet ()) {
                    title += " " + map.get (key);
                }
                item.setTitle (title);
                setItemValues (goods, item);
                itemMapper.insert (item);
            }
            //未启用spec
        } else {
            TbItem item = new TbItem ();  //前台不存在item ,需要new一个,所有属性需要手动赋值
            item.setTitle (goods.getGoods ().getGoodsName ());//商品 KPU+规格描述串作为
            item.setPrice (goods.getGoods ().getPrice ());//价格
            item.setStatus ("1");//状态
            item.setIsDefault ("1");//是否默认
            item.setNum (99999);//库存数量
            item.setSpec ("{}");
            setItemValues (goods, item);
            itemMapper.insert (item);
        }

    }

    /**
     * 设置item的值
     *
     * @param goods
     * @param item
     */
    private void setItemValues(Goods goods, TbItem item) {
        item.setGoodsId (goods.getGoods ().getId ());//商品 SPU 编号
        item.setSellerId (goods.getGoods ().getSellerId ());//商家编号
        item.setCategoryid (goods.getGoods ().getCategory3Id ());//商品分类编号（3 级）
        item.setCreateTime (new Date ());//创建日期
        item.setUpdateTime (new Date ());//修改日期

        //品牌名称
        TbBrand brand =
                brandMapper.selectByPrimaryKey (goods.getGoods ().getBrandId ());
        item.setBrand (brand.getName ());
        //分类名称
        TbItemCat itemCat =
                itemCatMapper.selectByPrimaryKey (goods.getGoods ().getCategory3Id ());
        item.setCategory (itemCat.getName ());
        //商家名称
        TbSeller seller =
                sellerMapper.selectByPrimaryKey (goods.getGoods ().getSellerId ());
        item.setSeller (seller.getNickName ());
        //图片地址（取 spu 的第一个图片）
        List<Map> imageList = JSON.parseArray (goods.getGoodsDesc ().getItemImages (),
                Map.class);
        if (imageList.size () > 0) {
            item.setImage ((String) imageList.get (0).get ("url"));
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {
        goodsMapper.updateByPrimaryKey (goods.getGoods ()); //商品表
        goodsDescMapper.updateByPrimaryKey (goods.getGoodsDesc ());
        TbItemExample tbItemExample = new TbItemExample ();
        tbItemExample.createCriteria ().andGoodsIdEqualTo (goods.getGoods ().getId ());//删除原来的
        itemMapper.deleteByExample (tbItemExample);
        setItemValue (goods);  //重新添加

    }

    /**
     * 设置item的值
     *
     * @param goods
     */
    private void setItemValue(Goods goods) {
        //启用spec
        if ("1".equals (goods.getGoods ().getIsEnableSpec ())) {
            for (TbItem item : goods.getItemList ()) {
                //设置标题
                String title = goods.getGoods ().getGoodsName ();
                Map<String, Object> map = JSON.parseObject (item.getSpec ());
                for (Object key : map.keySet ()) {
                    title += " " + map.get (key);
                }
                item.setTitle (title);
                setItemValues (goods, item);
                itemMapper.insert (item);
            }
            //未启用spec
        } else {
            TbItem item = new TbItem ();  //前台不存在item ,需要new一个,所有属性需要手动赋值
            item.setTitle (goods.getGoods ().getGoodsName ());//商品 KPU+规格描述串作为
            item.setPrice (goods.getGoods ().getPrice ());//价格
            item.setStatus ("1");//状态
            item.setIsDefault ("1");//是否默认
            item.setNum (99999);//库存数量
            item.setSpec ("{}");
            setItemValues (goods, item);
            itemMapper.insert (item);
        }
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods ();
        //商品表
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey (id);
        goods.setGoods (tbGoods);
        //商品扩展表
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey (id);
        goods.setGoodsDesc (tbGoodsDesc);
        //sku表
        TbItemExample tbItemExample = new TbItemExample ();
        tbItemExample.createCriteria ().andGoodsIdEqualTo (id);
        List<TbItem> items = itemMapper.selectByExample (tbItemExample);
        goods.setItemList (items);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey (id);
            goods.setIsDelete ("1");
            goodsMapper.updateByPrimaryKey (goods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample ();
        Criteria criteria = example.createCriteria ();
        criteria.andIsDeleteIsNull ();   //排除未删除的

        if (goods != null) {
            if (goods.getSellerId () != null && goods.getSellerId ().length () > 0) {
                //criteria.andSellerIdLike ("%" + goods.getSellerId () + "%");
                criteria.andSellerIdEqualTo (goods.getSellerId ());   //商家只能查询自己的商品

            }
            if (goods.getGoodsName () != null && goods.getGoodsName ().length () > 0) {
                criteria.andGoodsNameLike ("%" + goods.getGoodsName () + "%");
            }
            if (goods.getAuditStatus () != null && goods.getAuditStatus ().length () > 0) {
                criteria.andAuditStatusLike ("%" + goods.getAuditStatus () + "%");
            }
            if (goods.getIsMarketable () != null && goods.getIsMarketable ().length () > 0) {
                criteria.andIsMarketableLike ("%" + goods.getIsMarketable () + "%");
            }
            if (goods.getCaption () != null && goods.getCaption ().length () > 0) {
                criteria.andCaptionLike ("%" + goods.getCaption () + "%");
            }
            if (goods.getSmallPic () != null && goods.getSmallPic ().length () > 0) {
                criteria.andSmallPicLike ("%" + goods.getSmallPic () + "%");
            }
            if (goods.getIsEnableSpec () != null && goods.getIsEnableSpec ().length () > 0) {
                criteria.andIsEnableSpecLike ("%" + goods.getIsEnableSpec () + "%");
            }
            if (goods.getIsDelete () != null && goods.getIsDelete ().length () > 0) {
                criteria.andIsDeleteLike ("%" + goods.getIsDelete () + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample (example);
        return new PageResult (page.getTotal (), page.getResult ());
    }


    /**
     * 批量审核商品
     *
     * @param ids
     * @param status
     */
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey (id);
            goods.setAuditStatus (status);
            goodsMapper.updateByPrimaryKey (goods);
        }
    }


    /**
     * 是否上架
     *
     * @param ids
     */
    @Override
    public void isMarketable(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods goods = goodsMapper.selectByPrimaryKey (id);
            goods.setIsMarketable (status);
            goodsMapper.updateByPrimaryKey (goods);
        }
    }

    /**
     * 根据商品id查询sku列表
     *
     * @param ids
     * @return
     */
    @Override
    public List<TbItem> selectItemByGoodsId(Long[] ids) {
        List<TbItem> list = new ArrayList<> ();
        TbItemExample example = new TbItemExample ();

        for (Long goodsid : ids) {
            example.createCriteria ().andGoodsIdEqualTo (goodsid);
            List<TbItem> itemList = itemMapper.selectByExample (example);
            list.addAll (itemList);
        }
        return list;
    }

}
