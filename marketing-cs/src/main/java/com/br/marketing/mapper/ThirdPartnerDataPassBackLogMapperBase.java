package com.br.marketing.mapper;

import com.br.marketing.entity.ThirdPartnerDataPassBackLog;
import com.br.marketing.entity.ThirdPartnerDataPassBackLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ThirdPartnerDataPassBackLogMapperBase {
    int countByExample(ThirdPartnerDataPassBackLogExample example);

    int deleteByExample(ThirdPartnerDataPassBackLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ThirdPartnerDataPassBackLog record);

    int insertSelective(ThirdPartnerDataPassBackLog record);

    List<ThirdPartnerDataPassBackLog> selectByExample(ThirdPartnerDataPassBackLogExample example);

    ThirdPartnerDataPassBackLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ThirdPartnerDataPassBackLog record, @Param("example") ThirdPartnerDataPassBackLogExample example);

    int updateByExample(@Param("record") ThirdPartnerDataPassBackLog record, @Param("example") ThirdPartnerDataPassBackLogExample example);

    int updateByPrimaryKeySelective(ThirdPartnerDataPassBackLog record);

    int updateByPrimaryKey(ThirdPartnerDataPassBackLog record);
}