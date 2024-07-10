package com.br.marketing.mapper;

import com.br.marketing.entity.ZhiJiaClueBackData;
import com.br.marketing.entity.ZhiJiaClueBackDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhiJiaClueBackDataMapperBase {
    int countByExample(ZhiJiaClueBackDataExample example);

    int deleteByExample(ZhiJiaClueBackDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhiJiaClueBackData record);

    int insertSelective(ZhiJiaClueBackData record);

    List<ZhiJiaClueBackData> selectByExample(ZhiJiaClueBackDataExample example);

    ZhiJiaClueBackData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhiJiaClueBackData record, @Param("example") ZhiJiaClueBackDataExample example);

    int updateByExample(@Param("record") ZhiJiaClueBackData record, @Param("example") ZhiJiaClueBackDataExample example);

    int updateByPrimaryKeySelective(ZhiJiaClueBackData record);

    int updateByPrimaryKey(ZhiJiaClueBackData record);
}