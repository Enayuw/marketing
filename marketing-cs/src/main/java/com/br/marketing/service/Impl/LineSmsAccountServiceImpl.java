package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;
import com.br.marketing.client.ibmpapi.IbmpApiServiceClient;
import com.br.marketing.client.ibmpapi.outpu.TransferIbmpOutboundVO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.account.*;
import com.br.marketing.entity.*;
import com.br.marketing.enums.DictEnum;
import com.br.marketing.mapper.*;
import com.br.marketing.service.LineSmsAccountDataService;
import com.br.marketing.service.LineSmsAccountService;
import com.br.marketing.vo.MarketingLineAccountLogVO;
import com.br.marketing.vo.MarketingLineAccountRecordVO;
import com.br.marketing.vo.MarketingSmsAccountLogVo;
import com.br.marketing.vo.MarketingSmsAccountRecordVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LineSmsAccountServiceImpl implements LineSmsAccountService {

    private static final Logger log = LoggerFactory.getLogger(LineSmsAccountServiceImpl.class);

    private static final String smsMethod ="getSmsVendors";
    private static final String smsApiCode = "3710012";

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private IbmpApiServiceClient ibmpApiServiceClient;

    @Resource
    private MarketingSmsAccountRecordMapper smsAccountRecordMapper;

    @Resource
    private MarketingSmsAccountLogMapper smsAccountLogMapper;

    @Resource
    private MarketingSmsAccountDetailMapper smsAccountDetailMapper;

    @Resource
    private MarketingLineAccountRecordMapper lineAccountRecordMapper;

    @Resource
    private MarketingLineAccountLogMapper lineAccountLogMapper;

    @Resource
    private MarketingLineAccountDetailMapper lineAccountDetailMapper;

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
        //3.校验短信单价
        if (checkPrice(priceDates)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("短信单价最大值为1元！");
        }
        //4.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //5.事务保存
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
        //3.校验短信单价
        if (checkPrice(priceDates)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("短信单价最大值为1元！");
        }
        //4.日期排序，从低到高
        priceDates.sort(Comparator.comparing(PriceDateDTO::getEffectStartDate));
        for (int i = 0; i < priceDates.size(); i++) {
            if (i != priceDates.size() - 1) {
                priceDates.get(i).setEffectEndDate(priceDates.get(i + 1).getEffectStartDate().minusDays(1));
            }
        }
        //5.校验供应商是否变更，数据是否需要更新
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
        //6.事务保存
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
        jsonDataDTO.setMethod(smsMethod);
        jsonDataDTO.setAccessNumber(UUID.randomUUID().toString());
        robotOutboundDTO.setApiCode(smsApiCode);
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
    public Result addLineAccount(LineAccountDto dto) throws JsonProcessingException {
        //1.校验线路有无存在的配置
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        List<Long> existGatewayIds = lineAccountDetailMapper.selectLineIfExist(gatewayIds, dto.getConfigId());
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
        //5.事务保存
        lineSmsAccountDataService.addLineAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result updLineAccount(LineAccountDto dto) throws IOException {
        //1.校验渠道有无存在的配置
        List<Long> gatewayIds = dto.getLines().stream().map(LineCallerDto::getGatewayId).collect(Collectors.toList());
        List<Long> existGatewayIds = lineAccountDetailMapper.selectLineIfExist(gatewayIds, dto.getConfigId());
        if (existGatewayIds.size() > 0) {
            List<String> callerFullnames = dto.getLines().stream()
                    .filter(line -> existGatewayIds.contains(line.getGatewayId()))
                    .map(LineCallerDto::getCallerFullname).collect(Collectors.toList());
            return new Result<String>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("主叫项目名称：" + String.join(",", callerFullnames) + "已存在配置，无法变更，请在列表页面变更对应主叫项目名称配置！");
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
        //5.校验供应商是否变更，数据是否需要更新
        MarketingLineAccountLogExample accountLogExample = new MarketingLineAccountLogExample();
        accountLogExample.createCriteria().andConfigIdEqualTo(dto.getConfigId()).andIsDeleteEqualTo(0);
        accountLogExample.setOrderByClause("create_time desc limit 1");
        MarketingLineAccountLog oldAccountLog = lineAccountLogMapper.selectByExample(accountLogExample).get(0);

        JSONObject oldAccountLogDetail = JSONObject.parseObject(oldAccountLog.getDetail());
        List<Long> oldGatewayIds = JSON.parseArray(oldAccountLogDetail.getString("gatewayIds"), Long.class);
        boolean lineEqualFlag = new HashSet<>(oldGatewayIds).equals(new HashSet<>(gatewayIds));
        List<PriceDateDTO> oldPriceDates = JSON.parseArray(oldAccountLogDetail.getString("priceDates"), PriceDateDTO.class);
        boolean priceDateEqualFlag = new HashSet<>(oldPriceDates).equals(new HashSet<>(priceDates));
        if(lineEqualFlag && priceDateEqualFlag){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("配置无修改，无需变更");
        }
        //6.事务保存
        lineSmsAccountDataService.updLineAccount(dto);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result forbLineAccount(Long configId) {
        lineSmsAccountDataService.forbLineAccount(configId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result allowLineAccount(Long configId) {
        lineSmsAccountDataService.allowLineAccount(configId);
        return new Result<String>().setCode(ResultCode.SUCCESS.getValue());
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
        if (CollectionUtils.isEmpty(dictList)) {
            return Maps.newHashMap();
        }
        return dictList.stream().collect(Collectors.groupingBy(MarketingDict::getDictType));
    }

    @Override
    public ApiResult getLineAccountBasInfo() {
        ApiResult apiResult = new ApiResult().fail();
        TransferIbmpOutboundVO transferIbmpOutboundVO = ibmpApiServiceClient.getLineBaseInfo();
        if ("000000".equals(transferIbmpOutboundVO.getCode())) {
            apiResult = new ApiResult().success().setData(convertBaseInfo(transferIbmpOutboundVO.getData()));
        }
        return apiResult;
    }



    @Override
    public List<MarketingLineAccountRecordVO> getLineAccountsByConfigId(Long configId) {
        List<MarketingLineAccountRecord>  smsAccountRecordList = lineAccountRecordMapper.getLineAccountsByConfigId(configId);
        return convertToLineAccountRecordVoList(smsAccountRecordList);
    }



    @Override
    public PageResultReturn getLineAccounts(Integer current, Integer size, String lineSupplier, String callerFullName, Double price) {
        Date nowDate = new Date(System.currentTimeMillis());
        PageHelper.startPage(current, size);
        List<MarketingLineAccountRecord> lineAccountRecordList = lineAccountRecordMapper.selectList(lineSupplier,callerFullName,price,nowDate);
        Page<MarketingLineAccountRecord> page = (Page<MarketingLineAccountRecord>) lineAccountRecordList;
        List<MarketingLineAccountRecordVO> voList = convertToLineAccountRecordVoList(lineAccountRecordList);
        return PageResultReturn.setPageResult(voList, page.getPageNum(), page.getPageSize(), page.getTotal());
    }

    @Override
    public PageResultReturn getLineAccountLogs(Integer current, Integer size, Long configId) {
        PageHelper.startPage(current, size);
        List<MarketingLineAccountLog> lineAccountLogList = lineAccountLogMapper.getLineAccountLogs(configId);
        Page<MarketingLineAccountLog> page = (Page<MarketingLineAccountLog>) lineAccountLogList;
        List<MarketingLineAccountLogVO> voList = convertToLineAccountLogVoList(lineAccountLogList);
        return PageResultReturn.setPageResult(voList, page.getPageNum(), page.getPageSize(), page.getTotal());
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

    private List<MarketingLineAccountRecordVO> convertToLineAccountRecordVoList(List<MarketingLineAccountRecord> lineAccountRecordList) {
        if (lineAccountRecordList == null) {
            return Collections.emptyList();
        }
        List<MarketingLineAccountRecordVO> voList = new ArrayList<>();
        for (MarketingLineAccountRecord record : lineAccountRecordList) {
            MarketingLineAccountRecordVO vo = new MarketingLineAccountRecordVO();
            vo.setId(record.getId());
            vo.setConfigId(record.getConfigId() == null ? null : String.valueOf(record.getConfigId()));
            vo.setLineSupplier(record.getLineSupplier());
            vo.setLinesInfo(record.getLinesInfo());
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

    private List<MarketingLineAccountLogVO> convertToLineAccountLogVoList(List<MarketingLineAccountLog> lineAccountLogList) {
        if (lineAccountLogList == null) {
            return Collections.emptyList();
        }
        return lineAccountLogList.stream().map(log -> {
            MarketingLineAccountLogVO vo = new MarketingLineAccountLogVO();
            vo.setId(log.getId());
            vo.setConfigId(log.getConfigId() == null ? null : String.valueOf(log.getConfigId()));
            vo.setLineSupplier(log.getLineSupplier());
            vo.setDetail(log.getDetail());
            vo.setUserId(log.getUserId());
            vo.setUserName(log.getUserName());
            vo.setRealName(log.getRealName());
            vo.setOpeType(log.getOpeType());
            vo.setCreateTime(log.getCreateTime());
            vo.setUpdateTime(log.getUpdateTime());
            vo.setIsDelete(log.getIsDelete());
            return vo;
        }).collect(Collectors.toList());
    }

    private Object convertBaseInfo(Object data) {
        JSONArray dataArray = new JSONArray();
        if (data != null) {
            dataArray = (JSONArray) JSON.toJSON(data);
            for (int i = 0; i < dataArray.size(); i++) {
                JSONObject dataObject = dataArray.getJSONObject(i);
                if (dataObject.containsKey("channelDTOList")) {
                    JSONArray channelList = dataObject.getJSONArray("channelDTOList");
                    if (channelList != null) {
                        for (int j = 0; j < channelList.size(); j++) {
                            JSONObject channel = channelList.getJSONObject(j);
                            String projectName = channel.getString("projectName");
                            String caller = channel.getString("caller");
                            String callerFullName;
                            if (projectName != null && !projectName.isEmpty()) {
                                callerFullName = projectName + "-" + caller;
                            } else {
                                callerFullName = caller;
                            }
                            channel.put("callerFullName", callerFullName);
                        }
                    }
                }
            }
        }
        return dataArray;
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

    private Boolean checkPrice(List<PriceDateDTO> priceDates) {
        return priceDates.stream()
                .anyMatch(priceDate -> priceDate.getPrice() != null && priceDate.getPrice().compareTo(BigDecimal.valueOf(1.0)) > 0);
    }

}
