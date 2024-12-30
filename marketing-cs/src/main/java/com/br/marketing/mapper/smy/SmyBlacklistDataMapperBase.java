package com.br.marketing.mapper.smy;

import com.br.marketing.entity.smy.SmyBlacklistData;
import com.br.marketing.entity.smy.SmyBlacklistDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmyBlacklistDataMapperBase {
    int countByExample(SmyBlacklistDataExample example);

    int deleteByExample(SmyBlacklistDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmyBlacklistData record);

    int insertSelective(SmyBlacklistData record);

    List<SmyBlacklistData> selectByExample(SmyBlacklistDataExample example);

    SmyBlacklistData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmyBlacklistData record, @Param("example") SmyBlacklistDataExample example);

    int updateByExample(@Param("record") SmyBlacklistData record, @Param("example") SmyBlacklistDataExample example);

    int updateByPrimaryKeySelective(SmyBlacklistData record);

    int updateByPrimaryKey(SmyBlacklistData record);
}