package com.br.marketing.mapper;


import com.br.marketing.dto.LineBaseFullInfoDTO;
import com.br.marketing.entity.LineBaseInfoNormal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineBaseInfoNormalMapper extends LineBaseInfoNormalMapperBase{


    //页面查询->查询所有的数据->查询历史数据
    List<LineBaseFullInfoDTO> selectLineBaseFullInfoList();

    //三方数据同步->有效的数据和三方比较
    List<LineBaseFullInfoDTO> selectLineBaseUseInfoList();

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

    List<LineBaseInfoNormal> selectByGatewayIdListtikv_(@Param("gatewayIdList") List<Long> gatewayIdList);
}