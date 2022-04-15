package com.br.marketing.mapper;

import com.br.marketing.entity.YiqianbaoData;
import com.br.marketing.entity.YiqianbaoDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface YiqianbaoDataMapperBase {
    long countByExample(YiqianbaoDataExample example);

    int deleteByExample(YiqianbaoDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(YiqianbaoData record);

    int insertSelective(YiqianbaoData record);

    List<YiqianbaoData> selectByExample(YiqianbaoDataExample example);

    YiqianbaoData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") YiqianbaoData record, @Param("example") YiqianbaoDataExample example);

    int updateByExample(@Param("record") YiqianbaoData record, @Param("example") YiqianbaoDataExample example);

    int updateByPrimaryKeySelective(YiqianbaoData record);

    int updateByPrimaryKey(YiqianbaoData record);
}