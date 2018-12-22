package com.pinyougou.content.service.impl;

import java.util.List;

import com.pinyougou.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.pojo.TbContentExample;
import com.pinyougou.pojo.TbContentExample.Criteria;

import entity.PageResult;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private TbContentMapper contentMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 查询全部
     */
    @Override
    public List<TbContent> findAll() {
        return contentMapper.selectByExample (null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);
        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample (null);
        return new PageResult (page.getTotal (), page.getResult ());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbContent content) {
        //增加时清除缓存
        redisTemplate.boundHashOps ("content").delete (content.getCategoryId ());

        contentMapper.insert (content);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbContent content) {       //有可能修改CategoryId,所以修改前修改后都要删除
        //获取修改前oldCategoryId
        Long oldCategoryId = contentMapper.selectByPrimaryKey (content.getId ()).getCategoryId ();
        //删除修改前的缓存
        redisTemplate.boundHashOps ("content").delete (oldCategoryId);
        //修改content
        contentMapper.updateByPrimaryKey (content);
        //获取修改后的oldCategoryId
        Long newCategoryId = contentMapper.selectByPrimaryKey (content.getId ()).getCategoryId ();
        //删除修改好后的缓存
        redisTemplate.boundHashOps ("content").delete (newCategoryId);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbContent findOne(Long id) {
        return contentMapper.selectByPrimaryKey (id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            Long categoryId = contentMapper.selectByPrimaryKey (id).getCategoryId ();
            //删除时清除缓存
            redisTemplate.boundHashOps ("content").delete (categoryId);
            contentMapper.deleteByPrimaryKey (id);
        }
    }


    @Override
    public PageResult findPage(TbContent content, int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);

        TbContentExample example = new TbContentExample ();
        Criteria criteria = example.createCriteria ();

        if (content != null) {
            if (content.getTitle () != null && content.getTitle ().length () > 0) {
                criteria.andTitleLike ("%" + content.getTitle () + "%");
            }
            if (content.getUrl () != null && content.getUrl ().length () > 0) {
                criteria.andUrlLike ("%" + content.getUrl () + "%");
            }
            if (content.getPic () != null && content.getPic ().length () > 0) {
                criteria.andPicLike ("%" + content.getPic () + "%");
            }
            if (content.getStatus () != null && content.getStatus ().length () > 0) {
                criteria.andStatusLike ("%" + content.getStatus () + "%");
            }

        }

        Page<TbContent> page = (Page<TbContent>) contentMapper.selectByExample (example);
        return new PageResult (page.getTotal (), page.getResult ());
    }


    /**
     * 根据 categoryId 查询
     *
     * @param categoryId
     * @return
     */
    @Override
    public List<TbContent> findByCategoryId(Long categoryId) {
        List<TbContent> contentList = null;
        contentList = (List<TbContent>) redisTemplate.boundHashOps ("content").get (categoryId);
        if (contentList != null && contentList.size () > 0) {
            //有缓存
            return contentList;
        } else {
            //从数据查询
            TbContentExample example = new TbContentExample ();
            Criteria criteria = example.createCriteria ();
            criteria.andCategoryIdEqualTo (categoryId);
            criteria.andStatusEqualTo ("1");
            example.setOrderByClause ("sort_order"); //排序
            contentList = contentMapper.selectByExample (example);

            //存入缓存
            redisTemplate.boundHashOps ("content").put (categoryId, contentList);
            return contentList;
        }

    }

}
