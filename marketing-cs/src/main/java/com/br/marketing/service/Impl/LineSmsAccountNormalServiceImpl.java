package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.LineBaseFullInfoDto;
import com.br.marketing.dto.LineBaseShowInfoDto;
import com.br.marketing.dto.account.LineAccountDto;
import com.br.marketing.dto.account.LineCallerDto;
import com.br.marketing.dto.account.PriceDateDTO;
import com.br.marketing.mapper.LineAccountDetailNormalMapper;
import com.br.marketing.mapper.LineBaseInfoNormalMapper;
import com.br.marketing.mapper.LineSupplierInfoNormalMapper;
import com.br.marketing.mapper.MarketingLineAccountDetailMapper;
import com.br.marketing.service.LineSmsAccountDataNormalService;
import com.br.marketing.service.LineSmsAccountDataService;
import com.br.marketing.service.LineSmsAccountNormalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountNormalServiceImpl implements LineSmsAccountNormalService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountNormalServiceImpl.class);


    @Resource
    private LineSmsAccountDataNormalService lineSmsAccountDataNormalService;

    @Resource
    private LineBaseInfoNormalMapper lineBaseInfoNormalMapper;

    @Resource
    private MarketingLineAccountDetailMapper lineAccountDetailMapper;

    @Resource
    private LineAccountDetailNormalMapper lineAccountDetailNormalMapper;

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



    @Override
    public Result addLineAccount(LineAccountDto dto) throws JsonProcessingException {
        //1.校验线路有无存在的配置
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        List<Long> existGatewayIds = lineAccountDetailNormalMapper.selectLineIfExist(gatewayIds, dto.getGroupId());
        if (existGatewayIds.size() > 0) {
            List<String> callerFullnames = dto.getLines().stream()
                    .filter(line -> existGatewayIds.contains(line.getGatewayId()))
                    .map(LineCallerDto::getCallerFullname).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("主叫项目名称：" + String.join(",", callerFullnames) + "已存在配置，无法新增，请在列表页面变更对应主叫项目名称配置！");
        }
        //2.判断日期没有重复
        List<PriceDateDTO> priceDates = dto.getPriceDates();
        long esDateSize = priceDates.stream().map(PriceDateDTO::getEffectStartDate).distinct().count();
        if (esDateSize != priceDates.size()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("价格有效期不能重复！");
        }
        //3.校验短信单价
        if (checkPrice(priceDates)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("通话单价最大值为1元/分钟！");
        }
        //4.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //TODO 5.事务保存->要拆分 直接存储程 多个单条的明细
        lineSmsAccountDataNormalService.addLineAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
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

    private Boolean checkPrice(List<PriceDateDTO> priceDates) {
        return priceDates.stream()
                .anyMatch(priceDate -> priceDate.getPrice() != null && priceDate.getPrice().compareTo(BigDecimal.valueOf(1.0)) > 0);
    }

}
