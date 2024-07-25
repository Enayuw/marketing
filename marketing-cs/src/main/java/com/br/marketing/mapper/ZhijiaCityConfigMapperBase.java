package com.br.marketing.mapper;

import com.br.marketing.entity.ZhijiaCityConfig;
import com.br.marketing.entity.ZhijiaCityConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhijiaCityConfigMapperBase {
    long countByExample(ZhijiaCityConfigExample example);

    int deleteByExample(ZhijiaCityConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhijiaCityConfig record);

    int insertSelective(ZhijiaCityConfig record);

    List<ZhijiaCityConfig> selectByExample(ZhijiaCityConfigExample example);

    ZhijiaCityConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhijiaCityConfig record, @Param("example") ZhijiaCityConfigExample example);

    int updateByExample(@Param("record") ZhijiaCityConfig record, @Param("example") ZhijiaCityConfigExample example);

    int updateByPrimaryKeySelective(ZhijiaCityConfig record);

    int updateByPrimaryKey(ZhijiaCityConfig record);
}