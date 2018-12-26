package com.pinyougou.sellergoods.service.impl;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbTypeTemplateMapper;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.pojo.TbTypeTemplateExample;
import com.pinyougou.pojo.TbTypeTemplateExample.Criteria;
import com.pinyougou.sellergoods.service.TypeTemplateService;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class TypeTemplateServiceImpl implements TypeTemplateService {

    @Autowired
    private TbTypeTemplateMapper typeTemplateMapper;

    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;
    @Autowired
    private RedisTemplate redisTemplate ;


    /**
     * 查询全部
     */
    @Override
    public List<TbTypeTemplate> findAll() {
        return typeTemplateMapper.selectByExample (null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);
        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample (null);
        return new PageResult (page.getTotal (), page.getResult ());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.insert (typeTemplate);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbTypeTemplate typeTemplate) {
        typeTemplateMapper.updateByPrimaryKey (typeTemplate);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbTypeTemplate findOne(Long id) {
        return typeTemplateMapper.selectByPrimaryKey (id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            typeTemplateMapper.deleteByPrimaryKey (id);
        }
    }


    @Override
    public PageResult findPage(TbTypeTemplate typeTemplate, int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);

        TbTypeTemplateExample example = new TbTypeTemplateExample ();
        Criteria criteria = example.createCriteria ();

        if (typeTemplate != null) {
            if (typeTemplate.getName () != null && typeTemplate.getName ().length () > 0) {
                criteria.andNameLike ("%" + typeTemplate.getName () + "%");
            }
            if (typeTemplate.getSpecIds () != null && typeTemplate.getSpecIds ().length () > 0) {
                criteria.andSpecIdsLike ("%" + typeTemplate.getSpecIds () + "%");
            }
            if (typeTemplate.getBrandIds () != null && typeTemplate.getBrandIds ().length () > 0) {
                criteria.andBrandIdsLike ("%" + typeTemplate.getBrandIds () + "%");
            }
            if (typeTemplate.getCustomAttributeItems () != null && typeTemplate.getCustomAttributeItems ().length () > 0) {
                criteria.andCustomAttributeItemsLike ("%" + typeTemplate.getCustomAttributeItems () + "%");
            }

        }

        Page<TbTypeTemplate> page = (Page<TbTypeTemplate>) typeTemplateMapper.selectByExample (example);
        //每次执行此方法时把所有品牌及规格选项存入缓存
        saveToRedis ();
        return new PageResult (page.getTotal (), page.getResult ());
    }

    /**
     * 查询规格 , 加入规格选项
     *
     * @return
     */
    @Override
    public List<Map> selectSpecIds(Long id) {
        TbTypeTemplate typeTemplate = typeTemplateMapper.selectByPrimaryKey (id);
        String specIds = typeTemplate.getSpecIds ();
        List<Map> specList = JSON.parseArray (specIds, Map.class);
        for (Map spec : specList) {
            Long specId = new Long ((Integer) spec.get ("id"));
            TbSpecificationOptionExample example = new TbSpecificationOptionExample ();
            example.createCriteria ().andSpecIdEqualTo (specId);
            List options = specificationOptionMapper.selectByExample (example);
            spec.put ("options", options);
        }

        return specList;
    }

    /**
     * 将模板对应品牌及规格,规格详细存入缓存
     */
    private void saveToRedis (){
        List<TbTypeTemplate> templateList = findAll ();
        for (TbTypeTemplate typeTemplate : templateList) {
            //存储品牌信息到缓存
            String brandIds = typeTemplate.getBrandIds ();
            List<Map> brandList = JSON.parseArray (brandIds,Map.class);
            redisTemplate.boundHashOps ("brandList").put (typeTemplate.getId (),brandList);
            //存储规格选项到缓存
            List<Map> specList = selectSpecIds (typeTemplate.getId ());
            redisTemplate.boundHashOps ("specList").put (typeTemplate.getId (),specList);

        }

    }
}
