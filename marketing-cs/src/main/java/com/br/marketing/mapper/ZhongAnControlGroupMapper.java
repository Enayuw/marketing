package com.br.marketing.mapper;

import com.br.marketing.dto.report.zhongan.ZhongAnControlGroupDTO;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.br.marketing.vo.zhongan.param.ControlGroupDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface ZhongAnControlGroupMapper {

    List<ZhongAnCustomInfoVO> getCustomInfoListbI_(ControlGroupDTO dto);

    int saveCustomInfobI_(@Param("list") List<ZhongAnControlGroupDTO> list);

    List<String> selectConfigStatusbI_(@Param("startDate") String startDate, @Param("endDate") String endDate);

    List<ZhongAnControlGroupDTO> selectConfigTypeAndDatebI_(@Param("userTypes") List<Integer> userTypes,
                                            @Param("reportDate") String reportDate);

    List<ZhongAnCustomInfoVO> selectConfigByGroupbI_(@Param("list") List<String> reportDateList,
                                                   @Param("userType") String userType,@Param("constituencies") String constituencies);

}
