package com.br.marketing.mapper;

import com.br.marketing.dto.SmsBaseFullInfoDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsBaseInfoNormalMapper extends SmsBaseInfoNormalMapperBase{

    List<SmsBaseFullInfoDTO> selectSmsBaeUseInfoList();

    void updateOnlyDbOpStatus(@Param("onlyInDbIdList") List<Long> onlyInDbIdList,
                              @Param("opeStatus") Integer opStatus);

    void updateBaseInfoById(@Param("id") Long id, @Param("channelName") String channelName,
                            @Param("vendorId") Long vendorId, @Param("opeStatus") Integer opeStatus);
}