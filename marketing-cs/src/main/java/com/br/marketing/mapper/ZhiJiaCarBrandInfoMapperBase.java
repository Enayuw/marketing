package com.br.marketing.mapper;

import java.util.List;

import com.br.marketing.entity.ZhiJiaCarBrandInfo;
import com.br.marketing.entity.ZhiJiaCarBrandInfoExample;
import org.apache.ibatis.annotations.Param;

public interface ZhiJiaCarBrandInfoMapperBase {
    int countByExample(ZhiJiaCarBrandInfoExample example);

    int deleteByExample(ZhiJiaCarBrandInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhiJiaCarBrandInfo record);

    int insertSelective(ZhiJiaCarBrandInfo record);

    List<ZhiJiaCarBrandInfo> selectByExample(ZhiJiaCarBrandInfoExample example);

    ZhiJiaCarBrandInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhiJiaCarBrandInfo record, @Param("example") ZhiJiaCarBrandInfoExample example);

    int updateByExample(@Param("record") ZhiJiaCarBrandInfo record, @Param("example") ZhiJiaCarBrandInfoExample example);

    int updateByPrimaryKeySelective(ZhiJiaCarBrandInfo record);

    int updateByPrimaryKey(ZhiJiaCarBrandInfo record);
}