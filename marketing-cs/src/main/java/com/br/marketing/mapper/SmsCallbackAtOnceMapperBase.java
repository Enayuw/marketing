package com.br.marketing.mapper;

import com.br.marketing.entity.SmsCallbackAtOnce;
import com.br.marketing.entity.SmsCallbackAtOnceExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsCallbackAtOnceMapperBase {
    int countByExample(SmsCallbackAtOnceExample example);

    int deleteByExample(SmsCallbackAtOnceExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsCallbackAtOnce record);

    int insertSelective(SmsCallbackAtOnce record);

    List<SmsCallbackAtOnce> selectByExample(SmsCallbackAtOnceExample example);

    SmsCallbackAtOnce selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmsCallbackAtOnce record, @Param("example") SmsCallbackAtOnceExample example);

    int updateByExample(@Param("record") SmsCallbackAtOnce record, @Param("example") SmsCallbackAtOnceExample example);

    int updateByPrimaryKeySelective(SmsCallbackAtOnce record);

    int updateByPrimaryKey(SmsCallbackAtOnce record);
}