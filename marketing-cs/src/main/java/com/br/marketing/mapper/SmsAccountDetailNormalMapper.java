package com.br.marketing.mapper;

import com.br.marketing.dto.SmsAccountDetailDTO;
import org.apache.ibatis.annotations.Param;

import java.sql.Date;
import java.util.List;

public interface SmsAccountDetailNormalMapper extends SmsAccountDetailNormalMapperBase{

    Long selectCount(@Param("vendorId") Long vendorId, @Param("channelId") Long channelId);

    List<Long> selectChannelIfExist(@Param("channelIds") List<Long> channelIds,
                                    @Param("groupId") Long groupId);

    List<SmsAccountDetailDTO> selectListByGroupId(@Param("groupId") Long groupId);

    Long selectTotalCount(
            @Param("vendorId") Long vendorId,
            @Param("channelId") Long channelId,
            @Param("price") Double price,
            @Param("nowDate") Date nowDate);

    List<SmsAccountDetailDTO> selectList(
            @Param("vendorId") Long vendorId,
            @Param("channelId") Long channelId,
            @Param("price") Double price,
            @Param("nowDate") Date nowDate,
            @Param("limitSize") Integer limitSize,
            @Param("offset") Integer offset);
}