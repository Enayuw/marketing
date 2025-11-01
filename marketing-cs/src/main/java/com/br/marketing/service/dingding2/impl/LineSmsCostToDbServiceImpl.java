package com.br.marketing.service.dingding2.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.MiddleHeavenAviatorScriptApiClient;
import com.br.marketing.client.ibmpapi.IbmpApiServiceClient;
import com.br.marketing.client.ibmpapi.outpu.TransferIbmpOutboundVO;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.DdLineBaseInfoDto;
import com.br.marketing.dto.DdLinsSmsCostAlarmDto;
import com.br.marketing.dto.DdSmsBaseInfoDto;
import com.br.marketing.dto.account.*;
import com.br.marketing.entity.CostPriceExRecord;
import com.br.marketing.entity.DdDataLineCostPrice;
import com.br.marketing.entity.DdDataSmsCostPrice;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.*;
import com.br.marketing.service.LineSmsAccountService;
import com.br.marketing.service.dingding2.LineSmsCostToDbService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 短信线路-钉钉文档原始数据表同步到业务表
 */
@Component
@Slf4j
public class LineSmsCostToDbServiceImpl implements LineSmsCostToDbService {

    private final static String TITLE = "【短信/线路-钉钉文档配置入库任务】";

    private static final String smsMethod ="getSmsVendors";
    private static final String smsApiCode = "3710012";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LineSmsAccountService lineSmsAccountService;

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private IbmpApiServiceClient ibmpApiServiceClient;

    @Resource
    private MiddleHeavenAviatorScriptApiClient aviatorScriptApiClient;

    @Resource
    private DdDataSmsCostPriceMapper ddDataSmsCostPriceMapper;

    @Resource
    private DdDataLineCostPriceMapper ddDataLineCostPriceMapper;

    @Resource
    private MarketingSmsAccountDetailMapper smsAccountDetailMapper;


    @Resource
    private MarketingLineAccountDetailMapper lineAccountDetailMapper;


    @Resource
    private CostPriceExRecordMapper costPriceExRecordMapper;





    @Override
    public void process() {
        //1.短信cost处理
        DdLinsSmsCostAlarmDto smsCostAlarmDto = smsCostToDbDeal();

        //2.线路cost处理
        DdLinsSmsCostAlarmDto lineCostAlarmDto = lineCostToDbDeal();
        //TODO 3、报警通知
        dealAlarm(smsCostAlarmDto);
        dealAlarm(lineCostAlarmDto);
    }



    /**
     * 短信cost处理
     * 分页读取->循环处理
     */
    private DdLinsSmsCostAlarmDto smsCostToDbDeal() {
        //1、报警信息统计
        DdLinsSmsCostAlarmDto smsCostAlarmDto = new DdLinsSmsCostAlarmDto();
        smsCostAlarmDto.setCardTitle(marketingCommonConfig.getLinsSmsCostToDbConfig().getString("smsCardTitle"));
        //2、获取基础信息
        List<DdSmsBaseInfoDto>  smsBaseInfoList = getSmsBaseInfo();
        //3、查询原始数据
        Long searchId = 0L;
        while(true) {
            Integer searchSize =  marketingCommonConfig.getLinsSmsCostToDbConfig().getInteger("searchSize");
            List<DdDataSmsCostPrice> ddDataSmsCostPriceList = ddDataSmsCostPriceMapper.selectList(searchId,searchSize);
            if(ddDataSmsCostPriceList.isEmpty()) {
                break;
            }
            searchId = ddDataSmsCostPriceList.get(ddDataSmsCostPriceList.size()-1).getId();
            smsCostAlarmDto.setTotalCount(smsCostAlarmDto.getTotalCount() + ddDataSmsCostPriceList.size());
            //TODO 4分批次处理 校验->入库->报警统计
            smsCostCompareAndDbDeal(ddDataSmsCostPriceList,smsBaseInfoList,smsCostAlarmDto);
        }
        return smsCostAlarmDto;
    }

    /**
     * 线路cost处理
     * 分页读取->循环处理
     */
    private DdLinsSmsCostAlarmDto lineCostToDbDeal() {
        DdLinsSmsCostAlarmDto linsCostAlarmDto = new DdLinsSmsCostAlarmDto();
        linsCostAlarmDto.setCardTitle(marketingCommonConfig.getLinsSmsCostToDbConfig().getString("lineCardTitle"));
        //2、获取基础信息
        List<DdLineBaseInfoDto> ddLineBaseInfoDtoList =  getLineBaseInfo();
        //3、查询原始数据
        Long searchId = 0L;
        while(true) {
            Integer searchSize =  marketingCommonConfig.getLinsSmsCostToDbConfig().getInteger("searchSize");
            List<DdDataLineCostPrice> ddDataLineCostPriceList = ddDataLineCostPriceMapper.selectList(searchId,searchSize);
            if(ddDataLineCostPriceList.isEmpty()) {
                break;
            }
            searchId = ddDataLineCostPriceList.get(ddDataLineCostPriceList.size()-1).getId();
            linsCostAlarmDto.setTotalCount(linsCostAlarmDto.getTotalCount() + ddDataLineCostPriceList.size());
            //TODO 4、分批次处理 校验->入库->报警统计
            lineCostCompareAndDbDeal(ddDataLineCostPriceList,ddLineBaseInfoDtoList,linsCostAlarmDto);
        }
        return linsCostAlarmDto;
    }


    private void dealAlarm(DdLinsSmsCostAlarmDto smsCostAlarmDto) {
        JSONObject paramObj = new JSONObject();
        paramObj.put("totalCount", smsCostAlarmDto.getTotalCount());
        paramObj.put("successCount", smsCostAlarmDto.getSuccessCost());
        paramObj.put("errorCount", smsCostAlarmDto.getFailCount());
        List<JSONObject> resList = new ArrayList<>();
        List<CostPriceExRecord> costPriceExRecordList = smsCostAlarmDto.getCostPriceExRecordList();
        costPriceExRecordList.forEach(costPriceExRecord -> {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("异常信息", costPriceExRecord.getReason());
            resList.add(jsonObject);
        });
        paramObj.put("errorList", resList);

        //调用钉钉报警接口
        String aviatorScriptUrl = marketingCommonConfig.getLinsSmsCostToDbConfig().getString("aviatorScriptUrl");
        boolean isProxy = marketingCommonConfig.getLinsSmsCostToDbConfig().getBoolean("isProxy");
        aviatorScriptApiClient.dealAviatorScriptRequest(aviatorScriptUrl,paramObj,isProxy);
    }


    /**
     * 获取短信配置基础信息
     * baseInfo对象list
     * [
     *     {
     *         "channelDTOList": [
     *             {
     *                 "channelName": "微网-三网-批量",
     *                 "channelId": 401
     *             }
     *         ],
     *         "vendorId": 4,
     *         "vendorName": "百分"
     *     },
     *     {
     *         "channelDTOList": [
     *             {
     *                 "channelName": "智信-拉新",
     *                 "channelId": 2301
     *             }
     *         ],
     *         "vendorId": 28,
     *         "vendorName": "智信"
     *     }
     * ]
     *
     * DdSmsBaseInfoDto
     *     private Long vendorId;
     *     private String vendorName;
     *     private Long channelId;
     *     private String channelName;
     * @return
     */
    private List<DdSmsBaseInfoDto> getSmsBaseInfo() {
        List<DdSmsBaseInfoDto>  smsBaseInfoList = new ArrayList<>();
        JSONArray baseInfo = new JSONArray();
        TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
        TransferJsonDataDTO jsonDataDTO = new TransferJsonDataDTO();
        jsonDataDTO.setMethod(smsMethod);
        jsonDataDTO.setAccessNumber(UUID.randomUUID().toString());
        robotOutboundDTO.setApiCode(smsApiCode);
        robotOutboundDTO.setJsonData(jsonDataDTO);
        TransferRobotOutboundVO transferRobotOutboundVO = robotaiApiServiceClient.getSmsBaseInfo(robotOutboundDTO);
        if ("00".equals(transferRobotOutboundVO.getCode())) {
            baseInfo =  JSONArray.parseArray(transferRobotOutboundVO.getData().toString());
        }
        for (Object obj: baseInfo) {
            JSONObject jsonObject = (JSONObject) obj;
            Long vendorId = jsonObject.getLong("vendorId");
            String vendorName = jsonObject.getString("vendorName");
            if (jsonObject.containsKey("channelDTOList")) {
                JSONArray channelArr = jsonObject.getJSONArray("channelDTOList");
                for (Object channelObj : channelArr) {
                    JSONObject channelJson = (JSONObject) channelObj;
                    DdSmsBaseInfoDto dto = new DdSmsBaseInfoDto();
                    dto.setVendorId(vendorId);
                    dto.setVendorName(vendorName);
                    dto.setChannelId(channelJson.getLong("channelId"));
                    dto.setChannelName(channelJson.getString("channelName"));
                    smsBaseInfoList.add(dto);
                }
            }
        }
        return smsBaseInfoList;
    }


    /**
     * 获取线路配置基础信息
     * baseInfo 对象数组
     * [
     *     {
     *         "lineSupplier": "西南证券自备线",
     *         "channelDTOList": [
     *             {
     *                 "caller": "9527281",
     *                 "lineSupplier": "西南证券自备线",
     *                 "projectName": "西南证券自备线",
     *                 "outboundNumber": "9527281-自备",
     *                 "callerFullName": "西南证券自备线-9527281",
     *                 "gatewayId": 125017102
     *             }
     *         ]
     *     },
     *     {
     *         "lineSupplier": "烽火",
     *         "channelDTOList": [
     *             {
     *                 "caller": "97152",
     *                 "lineSupplier": "烽火",
     *                 "projectName": "还呗一线",
     *                 "outboundNumber": "烽火-还呗一线",
     *                 "callerFullName": "还呗一线-97152",
     *                 "gatewayId": 125008806
     *             }
     *         ]
     *     }
     * ]
     *    private Long gatewayId;
     *    private String caller;
     *    private String outboundNumber;
     *    private String lineSupplier;
     *    private String projectName;
     *
     * @return
     */
    private List<DdLineBaseInfoDto> getLineBaseInfo() {
        List<DdLineBaseInfoDto> ddLineBaseInfoDtoList = new ArrayList<>();
        JSONArray baseInfo = new JSONArray();
        TransferIbmpOutboundVO transferIbmpOutboundVO = ibmpApiServiceClient.getLineBaseInfo();
        if ("000000".equals(transferIbmpOutboundVO.getCode())) {
            baseInfo =  JSONArray.parseArray(transferIbmpOutboundVO.getData().toString());;
        }
        for (Object obj: baseInfo) {
            JSONObject jsonObject = (JSONObject) obj;
            String lineSupplier = jsonObject.getString("lineSupplier");
            if (jsonObject.containsKey("channelDTOList")) {
                JSONArray channelList = jsonObject.getJSONArray("channelDTOList");
                for (Object channelObj : channelList) {
                    JSONObject channel = (JSONObject) channelObj;
                    DdLineBaseInfoDto dto = new DdLineBaseInfoDto();
                    dto.setGatewayId(channel.getLong("gatewayId"));
                    dto.setCaller(channel.getString("caller"));
                    dto.setOutboundNumber(channel.getString("outboundNumber"));
                    dto.setLineSupplier(lineSupplier);
                    dto.setProjectName(channel.getString("projectName"));
                    ddLineBaseInfoDtoList.add(dto);
                }
            }
        }
        return ddLineBaseInfoDtoList;
    }


    private void smsCostCompareAndDbDeal(List<DdDataSmsCostPrice> ddDataSmsCostPriceList, List<DdSmsBaseInfoDto>  smsBaseInfoList, DdLinsSmsCostAlarmDto smsCostAlarmDto) {
        ddDataSmsCostPriceList.forEach(smsCost -> {
            try{
                //1.钉钉文档参数校验
                boolean smsDdParamCheck = smsDdParamCheck(smsCost,smsCostAlarmDto);
                if (!smsDdParamCheck) {
                    return;
                }

                //2.数据过滤
                List<DdSmsBaseInfoDto> filterList = smsBaseInfoList.stream()
                        .filter(dto -> smsCost.getLineSupplier().equals(dto.getVendorName())
                                && smsCost.getLineName().equals(dto.getChannelName()))
                        .collect(Collectors.toList());
                //3.配置数据在"短信基础信息接口"不存在
                if (filterList.isEmpty()) {
                    CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
                    costPriceExRecord.setJsonData(JSONObject.toJSONString(smsCost));
                    costPriceExRecord.setType(1);
                    costPriceExRecord.setReason("供应商:"+smsCost.getLineSupplier()+"线路:"+smsCost.getLineName()+",在短信侧不存在");
                    costPriceExRecordMapper.insert(costPriceExRecord);
                    List<CostPriceExRecord> costPriceExRecordList = smsCostAlarmDto.getCostPriceExRecordList();
                    costPriceExRecordList.add(costPriceExRecord);
                    smsCostAlarmDto.setCostPriceExRecordList(costPriceExRecordList);
                    smsCostAlarmDto.setFailCount(smsCostAlarmDto.getFailCount() + 1);
                    return;
                }
                filterList.forEach(smsDto -> {
                    // 4.判断数据库配置 是否存在(存在跳过，不存在插入)
                    Long count = smsAccountDetailMapper.selectCount(smsDto.getVendorId(),smsDto.getChannelId());
                    if (count == 0) {
                        fillThreadLocalUserInfo(0,smsCost.getLastModifiedUserName(),smsCost.getLastModifiedUserId());
                        SmsAccountDto smsAccountDto = fillSmsAccountInfo(smsCost,smsDto);
                        try {
                            Result result = lineSmsAccountService.addSmsAccount(smsAccountDto);
                            if (result.isSuccess()) {
                                smsCostAlarmDto.setSuccessCost(smsCostAlarmDto.getSuccessCost() + 1);
                            }else {
                                // TODO 记录因为保存失败导致存储失败
                                CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
                                costPriceExRecord.setJsonData(JSONObject.toJSONString(smsCost));
                                costPriceExRecord.setType(1);
                                costPriceExRecord.setReason("供应商:"+smsCost.getLineSupplier()+"线路:"+smsCost.getLineName()+"新增失败,请检查");
                                JSONObject extendObj =JSONObject.parseObject(JSONObject.toJSONString(smsDto));
                                extendObj.put("failMsg",result.getMessage());
                                costPriceExRecord.setExtend(extendObj.toJSONString());
                                //costPriceExRecordMapper.insert(costPriceExRecord);
                                List<CostPriceExRecord> costPriceExRecordList = smsCostAlarmDto.getCostPriceExRecordList();
                                costPriceExRecordList.add(costPriceExRecord);
                                smsCostAlarmDto.setCostPriceExRecordList(costPriceExRecordList);
                                smsCostAlarmDto.setFailCount(smsCostAlarmDto.getFailCount() + 1);
                            }
                            ThreadContextInfo.removeUser();
                        } catch (Exception e) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                                    JSONObject.toJSONString(smsCost)+e.getMessage(), TITLE), e);
                        }
                    }
                });
            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                        JSONObject.toJSONString(smsCost)+e.getMessage(), TITLE), e);
            }
        });
    }



    private void lineCostCompareAndDbDeal(List<DdDataLineCostPrice> ddDataLineCostPriceList, List<DdLineBaseInfoDto> ddLineBaseInfoDtoList, DdLinsSmsCostAlarmDto linsCostAlarmDto) {
        ddDataLineCostPriceList.forEach(lineCost -> {
            try{
                // 1. 钉钉文档参数校验
                boolean lineDdParamCheck = lineDdParamCheck(lineCost, linsCostAlarmDto);
                if (!lineDdParamCheck) {
                    return;
                }
                // 2. 数据过滤
                List<DdLineBaseInfoDto> filterList = ddLineBaseInfoDtoList.stream()
                        .filter(dto -> lineCost.getLineSupplier().equals(dto.getLineSupplier())
                                && lineCost.getCaller().equals(dto.getCaller())
                                && (lineCost.getProjectName() == null || lineCost.getProjectName().equals("") ||
                                lineCost.getProjectName().equals(dto.getProjectName())))
                        .collect(Collectors.toList());

                // 3. 配置数据在"线路基础信息接口"不存在
                if (filterList.isEmpty()) {
                    CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
                    costPriceExRecord.setJsonData(JSONObject.toJSONString(lineCost));
                    costPriceExRecord.setType(2);
                    costPriceExRecord.setReason("供应商" + lineCost.getLineSupplier() + "主叫号码:" + lineCost.getCaller()+",在线路侧不存在");
                    costPriceExRecordMapper.insert(costPriceExRecord);
                    List<CostPriceExRecord> costPriceExRecordList = linsCostAlarmDto.getCostPriceExRecordList();
                    costPriceExRecordList.add(costPriceExRecord);
                    linsCostAlarmDto.setCostPriceExRecordList(costPriceExRecordList);
                    linsCostAlarmDto.setFailCount(linsCostAlarmDto.getFailCount() + 1);
                    return;
                }

                // 4. 判断数据库配置 是否存在(存在跳过，不存在插入)
                filterList.forEach(lineDto -> {
                    Long count = lineAccountDetailMapper.selectCount(lineDto.getGatewayId());
                    if (count == 0) {
                        fillThreadLocalUserInfo(0,lineCost.getLastModifiedUserName(),lineCost.getLastModifiedUserId());
                        LineAccountDto lineAccountDto = fillLineAccountInfo(lineCost, lineDto);
                        try {
                            Result result = lineSmsAccountService.addLineAccount(lineAccountDto);
                            if (result.isSuccess()) {
                                linsCostAlarmDto.setSuccessCost(linsCostAlarmDto.getSuccessCost() + 1);
                            } else {
                                // 记录因为保存失败导致存储失败
                                CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
                                costPriceExRecord.setJsonData(JSONObject.toJSONString(lineCost));
                                costPriceExRecord.setType(2);
                                costPriceExRecord.setReason("供应商:" + lineCost.getLineSupplier() + "主叫项目:" + lineCost.getCaller()  + ",新增失败,请检查");
                                JSONObject extendObj = JSONObject.parseObject(JSONObject.toJSONString(lineDto));
                                extendObj.put("failMsg", result.getMessage());
                                costPriceExRecord.setExtend(extendObj.toJSONString());
                                //costPriceExRecordMapper.insert(costPriceExRecord);
                                List<CostPriceExRecord> costPriceExRecordList = linsCostAlarmDto.getCostPriceExRecordList();
                                costPriceExRecordList.add(costPriceExRecord);
                                linsCostAlarmDto.setCostPriceExRecordList(costPriceExRecordList);
                                linsCostAlarmDto.setFailCount(linsCostAlarmDto.getFailCount() + 1);
                            }
                            ThreadContextInfo.removeUser();
                        } catch (Exception e) {
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                                    JSONObject.toJSONString(lineCost)+e.getMessage(), TITLE), e);
                        }
                    }
                });
            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                        JSONObject.toJSONString(lineCost)+e.getMessage(), TITLE), e);
            }

        });
    }

    private LineAccountDto fillLineAccountInfo(DdDataLineCostPrice lineCost, DdLineBaseInfoDto lineDto) {
        LineAccountDto lineAccountDto = new LineAccountDto();
        lineAccountDto.setLineSupplier(lineDto.getLineSupplier());

        List<LineCallerDto> lines = new ArrayList<>();
        LineCallerDto lineCallerDto = new LineCallerDto();
        lineCallerDto.setGatewayId(lineDto.getGatewayId());
        lineCallerDto.setCallerFullname(lineDto.getProjectName()+"_"+lineDto.getCaller());
        lines.add(lineCallerDto);
        lineAccountDto.setLines(lines);

        List<PriceDateDTO> priceDates = new ArrayList<>();
        PriceDateDTO priceDateDTO = new PriceDateDTO();
        priceDateDTO.setEffectStartDate((LocalDate.parse(lineCost.getEffectDate())));
        if (lineCost.getIsCalcCost()==null || lineCost.getIsCalcCost().equals("")) {
            priceDateDTO.setPrice(BigDecimal.ZERO);
        }else {
            priceDateDTO.setPrice(new BigDecimal(lineCost.getPrice()));
        }
        priceDates.add(priceDateDTO);
        lineAccountDto.setPriceDates(priceDates);
        return lineAccountDto;
    }


    private SmsAccountDto fillSmsAccountInfo(DdDataSmsCostPrice smsCost, DdSmsBaseInfoDto smsDto) {
        SmsAccountDto accountDto = new SmsAccountDto();
        accountDto.setVendorId(smsDto.getVendorId());
        accountDto.setVendorName(smsDto.getVendorName());

        List<SmsChannelDto> smsChannelDtoList = new ArrayList<>();
        SmsChannelDto channelDto = new SmsChannelDto();
        channelDto.setChannelId(smsDto.getChannelId());
        channelDto.setChannelName(smsDto.getChannelName());
        smsChannelDtoList.add(channelDto);
        accountDto.setChannels(smsChannelDtoList);

        List<PriceDateDTO> priceDates = new ArrayList<>();
        PriceDateDTO priceDateDTO = new PriceDateDTO();
        priceDateDTO.setEffectStartDate(LocalDate.parse(smsCost.getEffectDate()));
        priceDateDTO.setEffectEndDate(LocalDate.parse("9999-12-31"));
        if (smsCost.getIsCalcCost() == null || smsCost.getIsCalcCost().equals("0")) {
            priceDateDTO.setPrice(BigDecimal.ZERO);
        }else {
            priceDateDTO.setPrice(new BigDecimal(smsCost.getPrice()));
        }
        priceDates.add(priceDateDTO);
        accountDto.setPriceDates(priceDates);
        return accountDto;
    }

    private boolean smsDdParamCheck(DdDataSmsCostPrice smsCost, DdLinsSmsCostAlarmDto smsCostAlarmDto) {
        CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
        costPriceExRecord.setType(1);
        costPriceExRecord.setJsonData(JSONObject.toJSONString(smsCost));

        // 验证各个字段的有效性
        boolean lineSupplierInvalid = StringUtils.isEmpty(smsCost.getLineSupplier());//供应商非空
        boolean lineNameInvalid = StringUtils.isEmpty(smsCost.getLineName());//短信线路名称非空
        boolean effectDateInvalid = StringUtils.isEmpty(smsCost.getEffectDate()) || !isValidDateFormat(smsCost.getEffectDate());  //日期非空及格式化
        boolean priceInvalid = "1".equals(smsCost.getIsCalcCost()) && StringUtils.isEmpty(smsCost.getPrice()); //价格为空

        if (lineSupplierInvalid || lineNameInvalid || effectDateInvalid || priceInvalid) {
            StringBuilder reasonBuilder = new StringBuilder();
            if (lineSupplierInvalid) {
                if (lineNameInvalid) {
                    reasonBuilder.append("供应商/短信线路名称为空");
                } else {
                    reasonBuilder.append("短信线路").append(smsCost.getLineName()).append("的供应商");
                    if (effectDateInvalid) {
                        reasonBuilder.append("/有效期");
                    }
                    if (priceInvalid) {
                        reasonBuilder.append("/单价为空或格式不合法");
                    }
                }
            } else {
                reasonBuilder.append("供应商").append(smsCost.getLineSupplier()).append("的");
                if (lineNameInvalid) {
                    reasonBuilder.append("短信线路名称");
                }
                if (priceInvalid) {
                    reasonBuilder.append("/单价");
                }
                if (effectDateInvalid) {
                    reasonBuilder.append("/有效期为空或格式不合法");
                }
                reasonBuilder.append(",请检查");
            }
            costPriceExRecord.setReason(reasonBuilder.toString());
            costPriceExRecordMapper.insertSelective(costPriceExRecord);
            smsCostAlarmDto.setFailCount(smsCostAlarmDto.getFailCount() + 1);
            List<CostPriceExRecord> costPriceExRecordList = smsCostAlarmDto.getCostPriceExRecordList();
            costPriceExRecordList.add(costPriceExRecord);
            smsCostAlarmDto.setCostPriceExRecordList(costPriceExRecordList);
        }
        return true;
    }

    // 新增线路cost参数校验，与短信类似
    private boolean lineDdParamCheck(DdDataLineCostPrice lineCost, DdLinsSmsCostAlarmDto linsCostAlarmDto) {
        CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
        costPriceExRecord.setType(2);
        costPriceExRecord.setJsonData(JSONObject.toJSONString(lineCost));
        boolean supplierInvalid = StringUtils.isEmpty(lineCost.getLineSupplier());
        boolean callerInvalid = StringUtils.isEmpty(lineCost.getCaller());
        boolean dateInvalid = StringUtils.isEmpty(lineCost.getEffectDate());
        boolean priceInvalid = "1".equals(lineCost.getIsCalcCost()) && StringUtils.isEmpty(lineCost.getPrice()); //价格为空

        if (supplierInvalid || callerInvalid || dateInvalid || priceInvalid) {
            StringBuilder reasonBuilder = new StringBuilder();
            if (supplierInvalid) {
                if (callerInvalid) {
                    reasonBuilder.append("供应商/主叫号码为空");
                }else {
                    reasonBuilder.append("主叫号码").append(lineCost.getCaller()).append("的供应商");
                    if (priceInvalid) {
                        reasonBuilder.append("/单价");
                    }
                    if (dateInvalid) {
                        reasonBuilder.append("/有效期为空或不合法");
                    }
                }
            }else {
                if (callerInvalid) {
                    reasonBuilder.append("供应商").append(lineCost.getLineSupplier()).append("的主叫号码");
                }
                if (priceInvalid) {
                    reasonBuilder.append("/单价");
                }
                if (dateInvalid) {
                    reasonBuilder.append("/有效期为空或不合法");
                }
            }
            reasonBuilder.append(",请检查");

            costPriceExRecord.setReason(reasonBuilder.toString());
            costPriceExRecordMapper.insert(costPriceExRecord);
            List<CostPriceExRecord> costPriceExRecordList = linsCostAlarmDto.getCostPriceExRecordList();
            costPriceExRecordList.add(costPriceExRecord);
            linsCostAlarmDto.setCostPriceExRecordList(costPriceExRecordList);
            linsCostAlarmDto.setFailCount(linsCostAlarmDto.getFailCount() + 1);
            return false;
        }
        return true;
    }

    /**
     * ThreadContextInfo-保存操作人信息
     * @param userId
     * @param lastModifiedUserName
     * @param lastModifiedUserId
     */
    private void fillThreadLocalUserInfo(Integer userId, String lastModifiedUserName, String lastModifiedUserId) {
        MarketingUserDetail userDetail = new MarketingUserDetail();
        userDetail.setId(userId);
        userDetail.setUserName(lastModifiedUserName);// TODO 和正常操作反着来 userName展示陈宏
        userDetail.setRealName(lastModifiedUserId); //TODO 和正常操作反着来 realName展示hong.chen,userName展示
        ThreadContextInfo.setUser(userDetail);
    }

    private boolean isValidDateFormat(String dateStr) {
        if (dateStr == null) {
            return false;
        }
        try {
            LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // 验证格式化后的字符串是否与原始字符串一致
            return dateStr.equals(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}

