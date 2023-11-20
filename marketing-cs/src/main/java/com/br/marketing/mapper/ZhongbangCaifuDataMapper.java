package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongbangCaifuData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhongbangCaifuDataMapper extends ZhongbangCaifuDataBaseMapper{


    List<String> selectZhongBangTaskIds(@Param("localId")Long localId);

    List<ZhongbangCaifuData> zhongBangLabelDataPage(@Param("localId") Long localId, @Param("minId") Long minId);




}
