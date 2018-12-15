package com.pinyougou.pojogroup;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

/*
 * @Author:  Yajun_Xu
 * 封装规格和规格选项
 * @Create: 2018/12/15 12:41
 **/
public class Specification implements Serializable {
    private TbSpecification specification;   //规格
    private List<TbSpecificationOption> specificationOptionList; //规格可选项

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public List<TbSpecificationOption> getSpecificationOptionList() {
        return specificationOptionList;
    }

    public void setSpecificationOptionList(List<TbSpecificationOption> specificationOptionList) {
        this.specificationOptionList = specificationOptionList;
    }
}
