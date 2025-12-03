package com.br.marketing.mapper;


import com.br.marketing.dto.LineBaseFullInfoDTO;
import com.br.marketing.entity.LineBaseInfoNormal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineBaseInfoNormalMapper extends LineBaseInfoNormalMapperBase{

    List<LineBaseFullInfoDTO> selectLineBaeFullInfoList();

    void updateOnlyDbOpStatus(@Param("onlyInDbIdList") List<Long> onlyInDbIdList,
                              @Param("opeStatus") Integer opStatus);

    void updateBaseInfoById(@Param("id") Long id,
                             @Param("caller") String caller,
                             @Param("outboundNumber") String outboundNumber,
                             @Param("projectName") String projectName,
                             @Param("opeStatus") Integer opeStatus);

    List<Long> selectGatewayIdByFiled(@Param("lineSupplierId")Long lineSupplierId,
                                      @Param("projectName") String projectName,
                                      @Param("caller") String caller);

    List<LineBaseInfoNormal> selectByIdList(@Param("gatewayIdList") List<Long> gatewayIdList);
}