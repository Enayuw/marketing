package com.br.marketing.mapper;

import com.br.marketing.entity.XyfSubmitDetail;
import com.br.marketing.entity.XyfSubmitDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XyfSubmitDetailMapperBase {
    int countByExample(XyfSubmitDetailExample example);

    int deleteByExample(XyfSubmitDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XyfSubmitDetail record);

    int insertSelective(XyfSubmitDetail record);

    List<XyfSubmitDetail> selectByExample(XyfSubmitDetailExample example);

    XyfSubmitDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XyfSubmitDetail record, @Param("example") XyfSubmitDetailExample example);

    int updateByExample(@Param("record") XyfSubmitDetail record, @Param("example") XyfSubmitDetailExample example);

    int updateByPrimaryKeySelective(XyfSubmitDetail record);

    int updateByPrimaryKey(XyfSubmitDetail record);
}