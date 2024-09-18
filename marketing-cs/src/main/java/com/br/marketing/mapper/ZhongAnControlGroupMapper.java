package com.br.marketing.mapper;

import com.br.marketing.dto.report.zhongan.ZhongAnControlGroupDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ZhongAnControlGroupMapper {

    List<ZhongAnControlGroupDTO> getCustomInfoListbI_(@Param("reportDate") String reportDate);

    int saveCustomInfobI_(@Param("list") List<ZhongAnControlGroupDTO> list);

}
