package com.br.marketing.mapper;

import com.br.marketing.entity.ZhiJieCarBrandInfo;
import com.br.marketing.entity.ZhiJieCarBrandInfoExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhiJieCarBrandInfoMapperBase {
    int countByExample(ZhiJieCarBrandInfoExample example);

    int deleteByExample(ZhiJieCarBrandInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhiJieCarBrandInfo record);

    int insertSelective(ZhiJieCarBrandInfo record);

    List<ZhiJieCarBrandInfo> selectByExampleWithBLOBs(ZhiJieCarBrandInfoExample example);

    List<ZhiJieCarBrandInfo> selectByExample(ZhiJieCarBrandInfoExample example);

    ZhiJieCarBrandInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhiJieCarBrandInfo record, @Param("example") ZhiJieCarBrandInfoExample example);

    int updateByExampleWithBLOBs(@Param("record") ZhiJieCarBrandInfo record, @Param("example") ZhiJieCarBrandInfoExample example);

    int updateByExample(@Param("record") ZhiJieCarBrandInfo record, @Param("example") ZhiJieCarBrandInfoExample example);

    int updateByPrimaryKeySelective(ZhiJieCarBrandInfo record);

    int updateByPrimaryKeyWithBLOBs(ZhiJieCarBrandInfo record);

    int updateByPrimaryKey(ZhiJieCarBrandInfo record);
}