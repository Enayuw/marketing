package com.br.marketing.mapper;

import com.br.marketing.entity.ZhiJieCarSeriesInfo;
import com.br.marketing.entity.ZhiJieCarSeriesInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhiJieCarSeriesInfoMapperBase {
    int countByExample(ZhiJieCarSeriesInfoExample example);

    int deleteByExample(ZhiJieCarSeriesInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhiJieCarSeriesInfo record);

    int insertSelective(ZhiJieCarSeriesInfo record);

    List<ZhiJieCarSeriesInfo> selectByExampleWithBLOBs(ZhiJieCarSeriesInfoExample example);

    List<ZhiJieCarSeriesInfo> selectByExample(ZhiJieCarSeriesInfoExample example);

    ZhiJieCarSeriesInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhiJieCarSeriesInfo record, @Param("example") ZhiJieCarSeriesInfoExample example);

    int updateByExampleWithBLOBs(@Param("record") ZhiJieCarSeriesInfo record, @Param("example") ZhiJieCarSeriesInfoExample example);

    int updateByExample(@Param("record") ZhiJieCarSeriesInfo record, @Param("example") ZhiJieCarSeriesInfoExample example);

    int updateByPrimaryKeySelective(ZhiJieCarSeriesInfo record);

    int updateByPrimaryKeyWithBLOBs(ZhiJieCarSeriesInfo record);

    int updateByPrimaryKey(ZhiJieCarSeriesInfo record);
}