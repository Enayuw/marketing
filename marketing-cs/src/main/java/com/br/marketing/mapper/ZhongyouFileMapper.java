package com.br.marketing.mapper;

import com.br.marketing.entity.ShuheBlackPhoneRecord;
import com.br.marketing.entity.ZhongyouFile;
import com.br.marketing.entity.ZhongyouFileExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhongyouFileMapper extends ZhongyouFileMapperBase{

    void saveBatch(@Param("list") List<ZhongyouFile> list);
}