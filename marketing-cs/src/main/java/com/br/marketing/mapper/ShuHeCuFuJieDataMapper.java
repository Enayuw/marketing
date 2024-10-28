package com.br.marketing.mapper;

import com.br.marketing.entity.ShuHeCuFuJieData;
import java.util.List;
import org.apache.ibatis.annotations.Param;


public interface ShuHeCuFuJieDataMapper extends ShuHeCuFuJieDataMapperBase{
    Long shuHeCuFuJieMatchDataOfMinId(@Param("date") String date);

    List<ShuHeCuFuJieData> shuHeCuFuJieMatchDataByMinId(@Param("date") String date, @Param("minId") Long minId, @Param("limit")Integer limit);
}
