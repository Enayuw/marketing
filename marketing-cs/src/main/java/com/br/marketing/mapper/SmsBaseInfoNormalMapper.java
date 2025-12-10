package com.br.marketing.mapper;

import com.br.marketing.dto.SmsBaseFullInfoDTO;
import com.br.marketing.dto.SmsBaseShowInfoDTO;
import com.br.marketing.entity.SmsBaseInfoNormal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsBaseInfoNormalMapper extends SmsBaseInfoNormalMapperBase{

    List<SmsBaseInfoNormal> selectList();

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

    List<SmsBaseInfoNormal> selectByChannelIdListtikv_(
            @Param("channelIdList") List<Long> channelIdList);

    SmsBaseInfoNormal selectByChannelId(
            @Param("vendorId") Long vendorId,
            @Param("channelId") Long channelId);
}