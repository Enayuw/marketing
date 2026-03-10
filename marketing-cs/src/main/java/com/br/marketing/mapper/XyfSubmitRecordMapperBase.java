package com.br.marketing.mapper;

import com.br.marketing.entity.XyfSubmitRecord;
import com.br.marketing.entity.XyfSubmitRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XyfSubmitRecordMapperBase {
    int countByExample(XyfSubmitRecordExample example);

    int deleteByExample(XyfSubmitRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XyfSubmitRecord record);

    int insertSelective(XyfSubmitRecord record);

    List<XyfSubmitRecord> selectByExample(XyfSubmitRecordExample example);

    XyfSubmitRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XyfSubmitRecord record, @Param("example") XyfSubmitRecordExample example);

    int updateByExample(@Param("record") XyfSubmitRecord record, @Param("example") XyfSubmitRecordExample example);

    int updateByPrimaryKeySelective(XyfSubmitRecord record);

    int updateByPrimaryKey(XyfSubmitRecord record);
}