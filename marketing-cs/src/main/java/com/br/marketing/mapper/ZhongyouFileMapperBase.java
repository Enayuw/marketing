package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongyouFile;
import com.br.marketing.entity.ZhongyouFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongyouFileMapperBase {
    int countByExample(ZhongyouFileExample example);

    int deleteByExample(ZhongyouFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongyouFile record);

    int insertSelective(ZhongyouFile record);

    List<ZhongyouFile> selectByExample(ZhongyouFileExample example);

    ZhongyouFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongyouFile record, @Param("example") ZhongyouFileExample example);

    int updateByExample(@Param("record") ZhongyouFile record, @Param("example") ZhongyouFileExample example);

    int updateByPrimaryKeySelective(ZhongyouFile record);

    int updateByPrimaryKey(ZhongyouFile record);
}