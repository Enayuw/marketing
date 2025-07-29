package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongAnCollidingConfig;
import com.br.marketing.entity.ZhongAnCollidingConfigExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongAnCollidingConfigMapperBase {
    int countByExample(ZhongAnCollidingConfigExample example);

    int deleteByExample(ZhongAnCollidingConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongAnCollidingConfig record);

    int insertSelective(ZhongAnCollidingConfig record);

    List<ZhongAnCollidingConfig> selectByExampleWithBLOBs(ZhongAnCollidingConfigExample example);

    List<ZhongAnCollidingConfig> selectByExample(ZhongAnCollidingConfigExample example);

    ZhongAnCollidingConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongAnCollidingConfig record, @Param("example") ZhongAnCollidingConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") ZhongAnCollidingConfig record, @Param("example") ZhongAnCollidingConfigExample example);

    int updateByExample(@Param("record") ZhongAnCollidingConfig record, @Param("example") ZhongAnCollidingConfigExample example);

    int updateByPrimaryKeySelective(ZhongAnCollidingConfig record);

    int updateByPrimaryKeyWithBLOBs(ZhongAnCollidingConfig record);

    int updateByPrimaryKey(ZhongAnCollidingConfig record);
}