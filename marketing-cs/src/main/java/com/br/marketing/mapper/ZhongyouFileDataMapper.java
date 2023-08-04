package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongyouFileData;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ZhongyouFileDataMapper extends ZhongyouFileDataMapperBase{

    int saveBatch(List<ZhongyouFileData> zhongyouFileDataList);

}