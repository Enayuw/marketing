package com.br.marketing.mapper;

import com.br.marketing.entity.ZhijiaCountyConfig;
import com.br.marketing.entity.ZhijiaCountyConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhijiaCountyConfigBMapperBase {
    long countByExample(ZhijiaCountyConfigExample example);

    int deleteByExample(ZhijiaCountyConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhijiaCountyConfig record);

    int insertSelective(ZhijiaCountyConfig record);

    List<ZhijiaCountyConfig> selectByExample(ZhijiaCountyConfigExample example);

    ZhijiaCountyConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhijiaCountyConfig record, @Param("example") ZhijiaCountyConfigExample example);

    int updateByExample(@Param("record") ZhijiaCountyConfig record, @Param("example") ZhijiaCountyConfigExample example);

    int updateByPrimaryKeySelective(ZhijiaCountyConfig record);

    int updateByPrimaryKey(ZhijiaCountyConfig record);
}