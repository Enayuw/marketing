package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongAnCollidingDataLog;
import com.br.marketing.entity.ZhongAnCollidingDataLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongAnCollidingDataLogMapperBase {
    int countByExample(ZhongAnCollidingDataLogExample example);

    int deleteByExample(ZhongAnCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongAnCollidingDataLog record);

    int insertSelective(ZhongAnCollidingDataLog record);

    List<ZhongAnCollidingDataLog> selectByExample(ZhongAnCollidingDataLogExample example);

    ZhongAnCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongAnCollidingDataLog record, @Param("example") ZhongAnCollidingDataLogExample example);

    int updateByExample(@Param("record") ZhongAnCollidingDataLog record, @Param("example") ZhongAnCollidingDataLogExample example);

    int updateByPrimaryKeySelective(ZhongAnCollidingDataLog record);

    int updateByPrimaryKey(ZhongAnCollidingDataLog record);
}