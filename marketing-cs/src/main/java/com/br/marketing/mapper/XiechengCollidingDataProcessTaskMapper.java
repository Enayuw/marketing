package com.br.marketing.mapper;

import com.br.marketing.dto.rulecenter.XcDeleteMagnitudeDistDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface XiechengCollidingDataProcessTaskMapper extends XiechengCollidingDataProcessTaskMapperBase {

    List<XcDeleteMagnitudeDistDTO> selectReleaseTimeRanges(@Param("apiCode")String apiCode);
}