package com.br.marketing.mapper;

import com.br.marketing.entity.SmsCallback;
import com.br.marketing.entity.SmsCallbackExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsCallbackMapperBase {
    int countByExample(SmsCallbackExample example);

    int deleteByExample(SmsCallbackExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsCallback record);

    int insertSelective(SmsCallback record);

    List<SmsCallback> selectByExample(SmsCallbackExample example);

    SmsCallback selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmsCallback record, @Param("example") SmsCallbackExample example);

    int updateByExample(@Param("record") SmsCallback record, @Param("example") SmsCallbackExample example);

    int updateByPrimaryKeySelective(SmsCallback record);

    int updateByPrimaryKey(SmsCallback record);
}