package com.br.marketing.mapper;


import com.br.marketing.dto.LineBaseFullInfoDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineBaseInfoNormalMapper extends LineBaseInfoNormalMapperBase{

    List<LineBaseFullInfoDto> selectLineBaeFullInfoList();

    void updateOnlyDbOpStatus(@Param("onlyInDbIdList") List<Long> onlyInDbIdList,
                              @Param("opeStatus") Integer opStatus);

    void updateBaseInfoById(@Param("id") Long id,
                             @Param("caller") String caller,
                             @Param("outboundNumber") String outboundNumber,
                             @Param("projectName") String projectName,
                             @Param("opeStatus") Integer opeStatus);

}