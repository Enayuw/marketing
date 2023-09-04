package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongyouDataCountDTO;
import com.br.marketing.entity.ZhongyouFileData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhongyouFileDataMapper extends ZhongyouFileDataMapperBase{

    int saveBatch(List<ZhongyouFileData> zhongyouFileDataList);

    List<ZhongyouDataCountDTO>  selectZhongyouCount(Long fileId);


    List<ZhongyouFileData> selectZhongYouDataPage(@Param("fileId") Long fileId, @Param("minId") Long minId,@Param("strategyId") String strategyId);


    List<String> selectZhongYoustrategyIds(@Param("fileId") Long fileId);


}