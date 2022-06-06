package com.br.marketing.mapper;

import com.br.marketing.entity.ShuheBlackPhoneRecord;
import com.br.marketing.entity.ShuheBlackPhoneRecordExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ShuheBlackPhoneRecordMapperBase {
    long countByExample(ShuheBlackPhoneRecordExample example);

    int deleteByExample(ShuheBlackPhoneRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ShuheBlackPhoneRecord record);

    int insertSelective(ShuheBlackPhoneRecord record);

    List<ShuheBlackPhoneRecord> selectByExample(ShuheBlackPhoneRecordExample example);

    ShuheBlackPhoneRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ShuheBlackPhoneRecord record, @Param("example") ShuheBlackPhoneRecordExample example);

    int updateByExample(@Param("record") ShuheBlackPhoneRecord record, @Param("example") ShuheBlackPhoneRecordExample example);

    int updateByPrimaryKeySelective(ShuheBlackPhoneRecord record);

    int updateByPrimaryKey(ShuheBlackPhoneRecord record);
}