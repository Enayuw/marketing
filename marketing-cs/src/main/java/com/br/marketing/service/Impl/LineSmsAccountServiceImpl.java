package com.br.marketing.service.Impl;

import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.PriceDateDTO;
import com.br.marketing.dto.account.SmsAccountDto;
import com.br.marketing.dto.account.SmsChannelDto;
import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountRecord;
import com.br.marketing.mapper.MarketingSmsAccountDetailMapper;
import com.br.marketing.mapper.MarketingSmsAccountLogMapper;
import com.br.marketing.mapper.MarketingSmsAccountRecordMapper;
import com.br.marketing.service.LineSmsAccountDataService;
import com.br.marketing.service.LineSmsAccountService;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountServiceImpl implements LineSmsAccountService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountServiceImpl.class);

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private MarketingSmsAccountRecordMapper smsAccountRecordMapper;

    @Resource
    private MarketingSmsAccountLogMapper smsAccountLogMapper;

    @Resource
    private MarketingSmsAccountDetailMapper smsAccountDetailMapper;

    @Resource
    private LineSmsAccountDataService lineSmsAccountDataService;


    @Override
    public Result addSmsAccount(SmsAccountDto dto) {
        //1.校验渠道有无存在的配置
        List<Integer> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        List<Integer> existChannelIds = smsAccountDetailMapper.selectChannelIfExist(channelIds);
        if (existChannelIds.size() > 0) {
            List<String> existChannelNames = dto.getChannels().stream()
                    .filter(channel -> existChannelIds.contains(channel.getChannelId()))
                    .map(SmsChannelDto::getChannelName).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("渠道：" + String.join(",", existChannelNames) + "已存在配置，无法新增，请在列表页面变更对应渠道配置！");
        }
        //2.判断日期没有重复
        List<PriceDateDTO> priceDates = dto.getPriceDates();
        long esDateSize = priceDates.stream().map(PriceDateDTO::getEffectStartDate).distinct().count();
        if (esDateSize != priceDates.size()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("价格有效期不能重复！");
        }
        //3.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectStartDate(priceDates.get(i+1).getEffectStartDate().minusDays(1));
            }
        }
        //4.保存
        lineSmsAccountDataService.addSmsAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public Result updSmsAccount(SmsAccountDto dto) {
        return null;
    }




    @Override
    public ApiResult getSmsAccountBasInfo() {
        ApiResult apiResult = new ApiResult().fail();
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        TransferJsonDataDTO jsonDataDTO = new TransferJsonDataDTO();
        jsonDataDTO.setMethod("getSmsVendors");
        jsonDataDTO.setAccessNumber(UUID.randomUUID().toString());
        robotOutboundDTO.setApiCode("7410733");
        robotOutboundDTO.setJsonData(jsonDataDTO);
        TransferRobotOutboundVO transferRobotOutboundVO = robotaiApiServiceClient.getSmsBaseInfo(robotOutboundDTO);
        if ("00".equals(transferRobotOutboundVO.getCode())) {
            apiResult =  new ApiResult().success(transferRobotOutboundVO.getData());
        }
        return apiResult;
    }

    @Override
    public PageResultReturn getSmsAccounts(Integer current, Integer size, String vendorName) {
        PageHelper.startPage(current, size);
        List<MarketingSmsAccountRecord> fastTaskRuleListVOS = smsAccountRecordMapper.selectList(vendorName);
        return PageResultReturn.setPageResult(fastTaskRuleListVOS, current, size);
    }

    @Override
    public PageResultReturn getSmsAccountLogs(Integer current, Integer size, Long recordId,String vendorName,String optUserName,Integer optType) {
        PageHelper.startPage(current, size);
        List<MarketingSmsAccountLog> fastTaskRuleListVOS = smsAccountLogMapper.selectSmsAccountLogs(recordId,vendorName,optUserName,optType);
        return PageResultReturn.setPageResult(fastTaskRuleListVOS, current, size);
    }
}
