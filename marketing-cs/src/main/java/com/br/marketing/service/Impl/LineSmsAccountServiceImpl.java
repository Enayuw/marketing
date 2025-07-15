package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import com.br.marketing.entity.MarketingDict;
import com.br.marketing.entity.MarketingSmsAccountLog;
import com.br.marketing.entity.MarketingSmsAccountLogExample;
import com.br.marketing.entity.MarketingSmsAccountRecord;
import com.br.marketing.enums.DictEnum;
import com.br.marketing.mapper.MarketingDictMapper;
import com.br.marketing.mapper.MarketingSmsAccountDetailMapper;
import com.br.marketing.mapper.MarketingSmsAccountLogMapper;
import com.br.marketing.mapper.MarketingSmsAccountRecordMapper;
import com.br.marketing.service.LineSmsAccountDataService;
import com.br.marketing.service.LineSmsAccountService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.MarketingSmsAccountLogVo;
import com.br.marketing.vo.MarketingSmsAccountRecordVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountServiceImpl implements LineSmsAccountService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountServiceImpl.class);

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingSmsAccountRecordMapper smsAccountRecordMapper;

    @Resource
    private MarketingSmsAccountLogMapper smsAccountLogMapper;

    @Resource
    private MarketingSmsAccountDetailMapper smsAccountDetailMapper;

    @Resource
    private LineSmsAccountDataService lineSmsAccountDataService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private MarketingDictMapper marketingDictMapper;

    @Override
    public Result addSmsAccount(SmsAccountDto dto) throws JsonProcessingException {
        //1.校验渠道有无存在的配置
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        List<Long> existChannelIds = smsAccountDetailMapper.selectChannelIfExist(channelIds, dto.getConfigId());
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
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //4.事务保存
        lineSmsAccountDataService.addSmsAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public Result updSmsAccount(SmsAccountDto dto) throws IOException {
        //1.校验渠道有无存在的配置
        List<Long> channelIds = dto.getChannels().stream().map(SmsChannelDto::getChannelId).collect(Collectors.toList());
        List<Long> existChannelIds = smsAccountDetailMapper.selectChannelIfExist(channelIds, dto.getConfigId());
        if (existChannelIds.size() > 0) {
            List<String> existChannelNames = dto.getChannels().stream()
                    .filter(channel -> existChannelIds.contains(channel.getChannelId()))
                    .map(SmsChannelDto::getChannelName).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("渠道：" + String.join(",", existChannelNames) + "已存在配置，无法变更，请在列表页面变更对应渠道配置！");
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
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //4.校验供应商是否变更，数据是否需要更新
        MarketingSmsAccountLogExample accountLogExample = new MarketingSmsAccountLogExample();
        accountLogExample.createCriteria().andConfigIdEqualTo(dto.getConfigId()).andIsDeleteEqualTo(0);
        accountLogExample.setOrderByClause("create_time desc limit 1");
        MarketingSmsAccountLog oldAccountLog = smsAccountLogMapper.selectByExample(accountLogExample).get(0);
        if (!oldAccountLog.getVendorId().equals(dto.getVendorId())) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("供应商不允许变更，请重新配置！");
        }
        JSONObject oldAccountLogDetail = JSONObject.parseObject(oldAccountLog.getDetail());
        List<Long> oldChannelIds = JSON.parseArray(oldAccountLogDetail.getString("channelIds"), Long.class);
        boolean channelEqualFlag = new HashSet<>(oldChannelIds).equals(new HashSet<>(channelIds));
        List<PriceDateDTO> oldPriceDates = JSON.parseArray(oldAccountLogDetail.getString("priceDates"), PriceDateDTO.class);
        boolean priceDateEqualFlag = new HashSet<>(oldPriceDates).equals(new HashSet<>(priceDates));
        if(channelEqualFlag && priceDateEqualFlag){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("配置无修改，无需变更");
        }
        //4.事务保存
        lineSmsAccountDataService.updSmsAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result forbSmsAccount(Long configId) {
        lineSmsAccountDataService.forbSmsAccount(configId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result allowSmsAccount(Long configId) {
        lineSmsAccountDataService.allowSmsAccount(configId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }


    @Override
    public ApiResult getSmsAccountBasInfo() {
        ApiResult apiResult = new ApiResult().fail();
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        TransferJsonDataDTO jsonDataDTO = new TransferJsonDataDTO();
        jsonDataDTO.setMethod(marketingCommonConfig.getAccountSmsConfig().getString("smsMethod"));
        jsonDataDTO.setAccessNumber(UUID.randomUUID().toString());
        robotOutboundDTO.setApiCode(marketingCommonConfig.getAccountSmsConfig().getString("apiCode"));
        robotOutboundDTO.setJsonData(jsonDataDTO);
        TransferRobotOutboundVO transferRobotOutboundVO = robotaiApiServiceClient.getSmsBaseInfo(robotOutboundDTO);
        if ("00".equals(transferRobotOutboundVO.getCode())) {
            apiResult =  new ApiResult().success(transferRobotOutboundVO.getData());
        }
        return apiResult;
    }

    @Override
    public PageResultReturn getSmsAccounts(Integer current, Integer size, String vendorName,String channelsName,Double price) {
        PageHelper.startPage(current, size);
        Date nowDate = new Date(System.currentTimeMillis());
        List<MarketingSmsAccountRecord> smsAccountRecordList = smsAccountRecordMapper.selectList(vendorName,channelsName,price,nowDate);
        Page<MarketingSmsAccountRecord> page = (Page<MarketingSmsAccountRecord>) smsAccountRecordList;
        List<MarketingSmsAccountRecordVo> voList = convertToSmsAccountRecordVoList(smsAccountRecordList);
        return PageResultReturn.setPageResult(voList, page.getPageNum(), page.getPageSize(), page.getTotal());
    }

    @Override
    public List<MarketingSmsAccountRecordVo> getSmsAccountsByConfigId(Long configId) {
        List<MarketingSmsAccountRecord>  smsAccountRecordList = smsAccountRecordMapper.getSmsAccountsByConfigId(configId);
        return convertToSmsAccountRecordVoList(smsAccountRecordList);
    }


    @Override
    public PageResultReturn getSmsAccountLogs(Integer current,Integer size,Long configId) {
        PageHelper.startPage(current, size);
        List<MarketingSmsAccountLog> smsAccountLogList = smsAccountLogMapper.selectSmsAccountLogs(configId);
        Page<MarketingSmsAccountLog> page = (Page<MarketingSmsAccountLog>) smsAccountLogList;
        List<MarketingSmsAccountLogVo> voList = convertSmsAccountLogVoList(smsAccountLogList);
        return PageResultReturn.setPageResult(voList, page.getPageNum(), page.getPageSize(), page.getTotal());
    }


    @Override
    public Map<String, List<MarketingDict>> getDictInfo(String dictType) {
        List<MarketingDict> dictList = marketingDictMapper.getDictInfo(dictType);
        Map<String, List<MarketingDict>> result = new HashMap<>();
        for (DictEnum dictEnum : DictEnum.values()) {
            String dictTypeItem = dictEnum.getDictType();
            List<MarketingDict> dictItemList = dictList.stream().filter(
                    dictItem -> dictItem.getDictType().equals(dictTypeItem)).collect(Collectors.toList());
            result.put(dictTypeItem, dictItemList);
        }
        return result;
    }


    private List<MarketingSmsAccountRecordVo> convertToSmsAccountRecordVoList(List<MarketingSmsAccountRecord> recordList) {
        if (recordList == null) {
            return Collections.emptyList();
        }
        List<MarketingSmsAccountRecordVo> voList = new ArrayList<>();
        for (MarketingSmsAccountRecord record : recordList) {
            MarketingSmsAccountRecordVo vo = new MarketingSmsAccountRecordVo();
            vo.setId(record.getId());
            vo.setConfigId(record.getConfigId() == null ? null : String.valueOf(record.getConfigId()));
            vo.setVendorId(record.getVendorId());
            vo.setVendorName(record.getVendorName());
            vo.setChannelsInfo(record.getChannelsInfo());
            vo.setPrice(record.getPrice());
            vo.setEffectStartDate(record.getEffectStartDate());
            vo.setEffectEndDate(record.getEffectEndDate());
            vo.setEnabled(record.getEnabled());
            vo.setCreateTime(record.getCreateTime());
            vo.setUpdateTime(record.getUpdateTime());
            vo.setIsDelete(record.getIsDelete());
            voList.add(vo);
        }
        return voList;
    }

    private List<MarketingSmsAccountLogVo> convertSmsAccountLogVoList(List<MarketingSmsAccountLog> marketingSmsAccountLogs) {
        if (marketingSmsAccountLogs == null) {
            return Collections.emptyList();
        }
        List<MarketingSmsAccountLogVo> voList = new ArrayList<>();
        for (MarketingSmsAccountLog smsAccountLog : marketingSmsAccountLogs) {
            MarketingSmsAccountLogVo vo = new MarketingSmsAccountLogVo();
            vo.setId(smsAccountLog.getId());
            vo.setConfigId(smsAccountLog.getConfigId() == null ? null : String.valueOf(smsAccountLog.getConfigId()));
            vo.setVendorId(smsAccountLog.getVendorId());
            vo.setVendorName(smsAccountLog.getVendorName());
            vo.setDetail(smsAccountLog.getDetail());
            vo.setUserId(smsAccountLog.getUserId());
            vo.setUserName(smsAccountLog.getUserName());
            vo.setRealName(smsAccountLog.getRealName());
            vo.setOpeType(smsAccountLog.getOpeType());
            vo.setCreateTime(smsAccountLog.getCreateTime());
            vo.setUpdateTime(smsAccountLog.getUpdateTime());
            vo.setIsDelete(smsAccountLog.getIsDelete());
            voList.add(vo);
        }
        return voList;
    }

}
