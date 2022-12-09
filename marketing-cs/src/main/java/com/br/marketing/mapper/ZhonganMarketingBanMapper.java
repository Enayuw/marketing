package com.br.marketing.mapper;

import com.br.marketing.entity.ZhonganMarketingBan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhonganMarketingBanMapper extends ZhonganMarketingBanMapperBase{

    List<ZhonganMarketingBan> getByZKData(@Param("date")String date,@Param("limitStart") Integer limitStart);
}