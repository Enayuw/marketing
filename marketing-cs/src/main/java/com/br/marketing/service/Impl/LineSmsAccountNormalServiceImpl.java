package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.LineBaseFullInfoDto;
import com.br.marketing.dto.LineBaseShowInfoDto;
import com.br.marketing.mapper.LineBaseInfoNormalMapper;
import com.br.marketing.mapper.LineSupplierInfoNormalMapper;
import com.br.marketing.service.LineSmsAccountNormalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountNormalServiceImpl implements LineSmsAccountNormalService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountNormalServiceImpl.class);

    @Resource
    private LineBaseInfoNormalMapper lineBaseInfoNormalMapper;

    @Resource
    private LineSupplierInfoNormalMapper    lineSmsAccountNormalService;

    @Override
    public ApiResult getLineAccountBasInfo() {
        ApiResult apiResult = new ApiResult().success();
        List<LineBaseFullInfoDto> lineBaseFullInfoDtoList = lineBaseInfoNormalMapper.selectLineBaeFullInfoList();
        List<LineBaseShowInfoDto> lineBaseShowInfoDtoList = lineBaseFullInfoDtoList.stream()
                .collect(Collectors.groupingBy(
                        LineBaseFullInfoDto::getLineSupplier,
                        Collectors.mapping(this::convertToLineBaseInfo, Collectors.toList())))
                .entrySet().stream()
                .map(entry -> {
                    LineBaseShowInfoDto dto = new LineBaseShowInfoDto();
                    dto.setLineSupplier(entry.getKey());
                    dto.setChannelDTOList(entry.getValue());
                    return dto;
                }).collect(Collectors.toList());

        return apiResult.setData(lineBaseShowInfoDtoList);
    }


    /**
     * 将 LineBaseFullInfoDto 转换为 LineBaseInfo
     */
        private LineBaseShowInfoDto.LineBaseInfo convertToLineBaseInfo(LineBaseFullInfoDto fullInfo) {
            LineBaseShowInfoDto.LineBaseInfo baseInfo = new LineBaseShowInfoDto.LineBaseInfo();
            baseInfo.setGatewayId(fullInfo.getGatewayId());
            baseInfo.setCaller(fullInfo.getCaller());
            baseInfo.setProjectName(fullInfo.getProjectName());
            baseInfo.setOutboundNumber(fullInfo.getOutboundNumber());
            baseInfo.setLineSupplier(fullInfo.getLineSupplier());
            baseInfo.setCallerFullName(fullInfo.getProjectName() + "-" + fullInfo.getCaller());
            return baseInfo;
        }
}
