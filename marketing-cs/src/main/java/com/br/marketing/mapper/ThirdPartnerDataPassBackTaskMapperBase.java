package com.br.marketing.mapper;

import com.br.marketing.entity.ThirdPartnerDataPassBackTask;
import com.br.marketing.entity.ThirdPartnerDataPassBackTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ThirdPartnerDataPassBackTaskMapperBase {
    int countByExample(ThirdPartnerDataPassBackTaskExample example);

    int deleteByExample(ThirdPartnerDataPassBackTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ThirdPartnerDataPassBackTask record);

    int insertSelective(ThirdPartnerDataPassBackTask record);

    List<ThirdPartnerDataPassBackTask> selectByExample(ThirdPartnerDataPassBackTaskExample example);

    ThirdPartnerDataPassBackTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ThirdPartnerDataPassBackTask record, @Param("example") ThirdPartnerDataPassBackTaskExample example);

    int updateByExample(@Param("record") ThirdPartnerDataPassBackTask record, @Param("example") ThirdPartnerDataPassBackTaskExample example);

    int updateByPrimaryKeySelective(ThirdPartnerDataPassBackTask record);

    int updateByPrimaryKey(ThirdPartnerDataPassBackTask record);
}