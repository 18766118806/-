package com.pinyougou.sellergoods.service.impl;

import java.util.List;

import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.pojo.TbSpecificationExample.Criteria;
import com.pinyougou.sellergoods.SpecificationService;

import entity.PageResult;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private TbSpecificationMapper specificationMapper;
    @Autowired
    private TbSpecificationOptionMapper specificationOptionMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSpecification> findAll() {
        return specificationMapper.selectByExample (null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);
        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample (null);
        return new PageResult (page.getTotal (), page.getResult ());
    }

    /**
     * 增加
     */
    @Override
    public void add(Specification specification) {
        //增加规格
        specificationMapper.insert (specification.getSpecification ());
        //返回生成的id
        Long id = specification.getSpecification ().getId ();
        //循换增加规格选项
        for (TbSpecificationOption specificationOption : specification.getSpecificationOptionList ()) {
            specificationOption.setSpecId (id);//设置id
            specificationOptionMapper.insert (specificationOption);
        }
    }


    /**
     * 修改
     */
    @Override
    public void update(Specification specification) {
        //修改规格名称
        specificationMapper.updateByPrimaryKey (specification.getSpecification ());
        //根据spec_id删除相关的规选项
        Long id = specification.getSpecification ().getId ();
        TbSpecificationOptionExample example = new TbSpecificationOptionExample ();
        example.createCriteria ().andSpecIdEqualTo (id);
        specificationOptionMapper.deleteByExample (example);
        //重新添加
        for (TbSpecificationOption option : specification.getSpecificationOptionList ()) {
            option.setSpecId (id);
            specificationOptionMapper.insert (option);
        }


    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Specification findOne(Long id) {
        Specification specification = new Specification ();
        TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey (id);
        TbSpecificationOptionExample example = new TbSpecificationOptionExample ();
        example.createCriteria ().andSpecIdEqualTo (id);
        List<TbSpecificationOption> specificationOptionList = specificationOptionMapper.selectByExample (example);
        specification.setSpecification (tbSpecification);
        specification.setSpecificationOptionList (specificationOptionList);
        return specification;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            specificationMapper.deleteByPrimaryKey (id);
        }
    }


    @Override
    public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
        PageHelper.startPage (pageNum, pageSize);

        TbSpecificationExample example = new TbSpecificationExample ();
        Criteria criteria = example.createCriteria ();

        if (specification != null) {
            if (specification.getSpecName () != null && specification.getSpecName ().length () > 0) {
                criteria.andSpecNameLike ("%" + specification.getSpecName () + "%");
            }

        }

        Page<TbSpecification> page = (Page<TbSpecification>) specificationMapper.selectByExample (example);
        return new PageResult (page.getTotal (), page.getResult ());
    }

}
