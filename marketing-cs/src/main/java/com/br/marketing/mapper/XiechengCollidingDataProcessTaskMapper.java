package com.br.marketing.mapper;

import com.br.marketing.dto.rulecenter.XcDeleteMagnitudeDistDTO;
import com.br.marketing.dto.rulecenter.XcDeleteTaskVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface XiechengCollidingDataProcessTaskMapper extends XiechengCollidingDataProcessTaskMapperBase {

    List<XcDeleteMagnitudeDistDTO> selectReleaseTimeRanges(@Param("apiCode")String apiCode);

    List<XcDeleteTaskVO> getCollidingDataDeleteTaskList(@Param("releaseTimeBegin") LocalDateTime releaseTimeBegin,
                                                        @Param("releaseTimeEnd") LocalDateTime releaseTimeEnd,
                                                        @Param("taskType") Integer taskType,
                                                        @Param("taskStatus") Integer taskStatus);
}