package com.br.marketing.mapper;

import java.util.List;

import com.br.marketing.entity.ZhiJiaCarSeriesInfo;
import com.br.marketing.entity.ZhiJiaCarSeriesInfoExample;
import org.apache.ibatis.annotations.Param;

public interface ZhiJiaCarSeriesInfoMapperBase {
    int countByExample(ZhiJiaCarSeriesInfoExample example);

    int deleteByExample(ZhiJiaCarSeriesInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhiJiaCarSeriesInfo record);

    int insertSelective(ZhiJiaCarSeriesInfo record);

    List<ZhiJiaCarSeriesInfo> selectByExample(ZhiJiaCarSeriesInfoExample example);

    ZhiJiaCarSeriesInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhiJiaCarSeriesInfo record, @Param("example") ZhiJiaCarSeriesInfoExample example);

    int updateByExample(@Param("record") ZhiJiaCarSeriesInfo record, @Param("example") ZhiJiaCarSeriesInfoExample example);

    int updateByPrimaryKeySelective(ZhiJiaCarSeriesInfo record);

    int updateByPrimaryKey(ZhiJiaCarSeriesInfo record);
}