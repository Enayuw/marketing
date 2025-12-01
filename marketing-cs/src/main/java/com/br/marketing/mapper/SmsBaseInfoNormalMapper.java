package com.br.marketing.mapper;

import com.br.marketing.dto.SmsBaseFullInfoDTO;
import com.br.marketing.dto.SmsBaseShowInfoDTO;
import com.br.marketing.entity.SmsBaseInfoNormal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsBaseInfoNormalMapper extends SmsBaseInfoNormalMapperBase{

    List<SmsBaseFullInfoDTO> selectSmsBaseFullInfoList();

    List<SmsBaseFullInfoDTO> selectSmsBaseUseInfoList();

    void updateOnlyDbOpStatus(
            @Param("onlyInDbIdList") List<Long> onlyInDbIdList,
            @Param("opeStatus") Integer opStatus);

    void updateBaseInfoById(
            @Param("id") Long id,
            @Param("channelName") String channelName,
            @Param("vendorId") Long vendorId,
            @Param("opeStatus") Integer opeStatus);

    List<SmsBaseInfoNormal> selectByChannelIdList(
            @Param("channelIdList") List<Long> channelIdList);

    List<Long> selectChannelIdListByFiled(
            @Param("vendorId") Long vendorId,
            @Param("channelsName") String channelsName);
}