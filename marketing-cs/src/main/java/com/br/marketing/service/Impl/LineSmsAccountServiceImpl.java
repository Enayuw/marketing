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
import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountRecord;
import com.br.marketing.mapper.MarketingSmsAccountLogMapper;
import com.br.marketing.mapper.MarketingSmsAccountRecordMapper;
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

@Service
public class LineSmsAccountServiceImpl implements LineSmsAccountService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountServiceImpl.class);

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private MarketingSmsAccountRecordMapper smsAccountRecordMapper;

    @Resource
    private MarketingSmsAccountLogMapper smsAccountLogMapper;


    @Override
    public Result addSmsAccount(SmsAccountDto dto) {
        //1.判断日期没有重复
        long esDateSize = dto.getPriceDates().stream().map(PriceDateDTO::getEffectStartDate).distinct().count();
        if (esDateSize != dto.getPriceDates().size()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("价格有效期不能重复！");
        }
        //2.日期排序，从低到高
        dto.getPriceDates().sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        //2.校验渠道下有无交叉价格

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
