package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongyouFileData;
import com.br.marketing.entity.ZhongyouFileDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongyouFileDataMapperBase {
    int countByExample(ZhongyouFileDataExample example);

    int deleteByExample(ZhongyouFileDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongyouFileData record);

    int insertSelective(ZhongyouFileData record);

    List<ZhongyouFileData> selectByExampleWithBLOBs(ZhongyouFileDataExample example);

    List<ZhongyouFileData> selectByExample(ZhongyouFileDataExample example);

    ZhongyouFileData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongyouFileData record, @Param("example") ZhongyouFileDataExample example);

    int updateByExampleWithBLOBs(@Param("record") ZhongyouFileData record, @Param("example") ZhongyouFileDataExample example);

    int updateByExample(@Param("record") ZhongyouFileData record, @Param("example") ZhongyouFileDataExample example);

    int updateByPrimaryKeySelective(ZhongyouFileData record);

    int updateByPrimaryKeyWithBLOBs(ZhongyouFileData record);

    int updateByPrimaryKey(ZhongyouFileData record);
}