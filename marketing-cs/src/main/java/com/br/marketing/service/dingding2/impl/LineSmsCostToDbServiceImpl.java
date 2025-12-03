package com.br.marketing.service.dingding2.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.MiddleHeavenAviatorScriptApiClient;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.*;
import com.br.marketing.dto.account.*;
import com.br.marketing.entity.CostPriceExRecord;
import com.br.marketing.entity.DdDataLineCostPrice;
import com.br.marketing.entity.DdDataSmsCostPrice;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.*;
import com.br.marketing.service.LineSmsAccountNormalService;
import com.br.marketing.service.LineSmsAccountService;
import com.br.marketing.service.dingding2.LineSmsCostToDbService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
    private LineSmsAccountNormalService lineSmsAccountNormalService;

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private MiddleHeavenAviatorScriptApiClient aviatorScriptApiClient;

    @Resource
    private DdDataSmsCostPriceMapper ddDataSmsCostPriceMapper;

    @Resource
    private DdDataLineCostPriceMapper ddDataLineCostPriceMapper;

    @Resource
    private MarketingSmsAccountDetailMapper smsAccountDetailMapper;


    @Resource
    private CostPriceExRecordMapper costPriceExRecordMapper;

    @Resource
    private LineBaseInfoNormalMapper lineBaseInfoNormalMapper;

    @Resource
    private LineSupplierInfoNormalMapper lineSupplierInfoNormalMapper;

    @Resource
    private LineAccountDetailNormalMapper lineAccountDetailNormalMapper;


    @Resource
    private SmsBaseInfoNormalMapper smsBaseInfoNormalMapper;

    @Resource
    private SmsAccountDetailNormalMapper smsAccountDetailNormalMapper;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 短信处理:获取基础信息->批量查询->item处理:校验参数->三方短信接口过滤->短信配置表判重->下游接口数据封装调用
     * 线路处理:获取基础信息->批量查询->item处理:校验参数->三方线路接口过滤->线路配置表判重->下游接口数据封装调用
     */
    @Override
    public void process() {
        //1.短信cost处理
        DdLinsSmsCostAlarmDto smsCostAlarmDto = smsCostToDbDeal();
        //2.线路cost处理
        DdLinsSmsCostAlarmDto lineCostAlarmDto = lineCostToDbDeal();
        //3.报警通知
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
        List<DdSmsBaseInfoDto>  smsBaseInfoList = getSmsBaseInfoByDb();
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
            //4.分批次处理 校验->入库->报警统计
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
        //2.获取基础信息
        List<DdLineBaseInfoDto> ddLineBaseInfoDtoList = getLineBaseInfoByDb();
        //3.查询原始数据
        Long searchId = 0L;
        while(true) {
            Integer searchSize =  marketingCommonConfig.getLinsSmsCostToDbConfig().getInteger("searchSize");
            List<DdDataLineCostPrice> ddDataLineCostPriceList = ddDataLineCostPriceMapper.selectList(searchId,searchSize);
            if(ddDataLineCostPriceList.isEmpty()) {
                break;
            }
            searchId = ddDataLineCostPriceList.get(ddDataLineCostPriceList.size()-1).getId();
            linsCostAlarmDto.setTotalCount(linsCostAlarmDto.getTotalCount() + ddDataLineCostPriceList.size());
            //4.分批次处理 校验->入库->报警统计
            lineCostCompareAndDbDeal(ddDataLineCostPriceList,ddLineBaseInfoDtoList,linsCostAlarmDto);
        }
        return linsCostAlarmDto;
    }




    private void dealAlarm(DdLinsSmsCostAlarmDto smsCostAlarmDto) {
        JSONObject requstObj = new JSONObject();
        JSONObject paramObj = new JSONObject();
        paramObj.put("title", smsCostAlarmDto.getCardTitle());
        paramObj.put("totalCount", smsCostAlarmDto.getTotalCount());
        paramObj.put("existCount",smsCostAlarmDto.getExistCount());
        paramObj.put("successCount", smsCostAlarmDto.getSuccessCost());
        paramObj.put("errorCount", smsCostAlarmDto.getFailCount());
        try {
            String errorListJson = objectMapper.writeValueAsString(
                    smsCostAlarmDto.getCostPriceExRecordDtoList().stream()
                            .map(CostPriceExRecordDto::getDdReason)
                            .collect(Collectors.toList())
            );
            paramObj.put("errorList", errorListJson);
            requstObj.put("param", paramObj);
            requstObj.put("scriptCode",marketingCommonConfig.getLinsSmsCostToDbConfig().getString("scriptCode"));
            //调用钉钉报警接口
            String aviatorScriptUrl = marketingCommonConfig.getLinsSmsCostToDbConfig().getString("aviatorScriptUrl");
            boolean isProxy = marketingCommonConfig.getLinsSmsCostToDbConfig().getBoolean("isProxy");
            aviatorScriptApiClient.dealAviatorScriptRequest(aviatorScriptUrl,requstObj,isProxy);
        } catch (JsonProcessingException e) {
            // 异常了钉钉报警 不推送钉钉通知
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }

    private List<DdSmsBaseInfoDto> getSmsBaseInfoByDb() {
        List<DdSmsBaseInfoDto>  smsBaseInfoList = new ArrayList<>();
        List<SmsBaseFullInfoDTO> lineBaseFullInfoDtoList = smsBaseInfoNormalMapper.selectSmsBaseUseInfoList();
        lineBaseFullInfoDtoList.forEach(smsBaseFullInfoDTO -> {
            DdSmsBaseInfoDto dto = new DdSmsBaseInfoDto();
            dto.setVendorId(smsBaseFullInfoDTO.getVendorId());
            dto.setVendorName(smsBaseFullInfoDTO.getVendorName());
            dto.setChannelId(smsBaseFullInfoDTO.getChannelId());
            dto.setChannelName(smsBaseFullInfoDTO.getChannelName());
            smsBaseInfoList.add(dto);
        });
        return smsBaseInfoList;
    }

    /**
     * 调用下游方法 从db获取 三方配置信息
     * DdLineBaseInfoDto
     *    private Long gatewayId;
     *    private String caller;
     *    private String outboundNumber;
     *    private String lineSupplier;
     *    private String projectName;
     * @return
     */
    private List<DdLineBaseInfoDto> getLineBaseInfoByDb() {
        List<DdLineBaseInfoDto> ddLineBaseInfoDtoList = new ArrayList<>();
        List<LineBaseFullInfoDTO> lineBaseFullInfoDtoList = lineBaseInfoNormalMapper.selectLineBaseUseInfoList();
        lineBaseFullInfoDtoList.forEach(lineBaseFullInfoDto -> {
            DdLineBaseInfoDto dto = new DdLineBaseInfoDto();
            dto.setGatewayId(lineBaseFullInfoDto.getGatewayId());
            dto.setCaller(lineBaseFullInfoDto.getCaller());
            dto.setOutboundNumber(lineBaseFullInfoDto.getOutboundNumber());
            dto.setLineSupplier(lineBaseFullInfoDto.getLineSupplier());
            dto.setProjectName(lineBaseFullInfoDto.getProjectName());
            ddLineBaseInfoDtoList.add(dto);
        });
        return ddLineBaseInfoDtoList;
    }


    private void smsCostCompareAndDbDeal(List<DdDataSmsCostPrice> ddDataSmsCostPriceList,
                                         List<DdSmsBaseInfoDto>  smsBaseInfoList, DdLinsSmsCostAlarmDto smsCostAlarmDto) {
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
                    String ddReason = "供应商["+smsCost.getLineSupplier()+"]线路["+smsCost.getLineName()+"],在短信侧不存在";
                    JSONObject reasonObj = new JSONObject();
                    reasonObj.put("ddReason", ddReason);
                    costPriceExRecord.setReason(JSONObject.toJSONString(reasonObj));
                    costPriceExRecordMapper.insertSelective(costPriceExRecord);
                    List<CostPriceExRecordDto> costPriceExRecordList = smsCostAlarmDto.getCostPriceExRecordDtoList();
                    costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,ddReason));
                    smsCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
                    smsCostAlarmDto.setFailCount(smsCostAlarmDto.getFailCount() + 1);
                    return;
                }
                filterList.forEach(smsDto -> {
                    // 4.判断数据库配置 是否存在(存在跳过，不存在插入)
                    Long count = smsAccountDetailNormalMapper.selectCount(smsDto.getVendorId(),smsDto.getChannelId());
                    if (count == 0) {
                        fillThreadLocalUserInfo(0,smsCost.getLastModifiedUserName(),smsCost.getLastModifiedUserId());
                        SmsAccountDto smsAccountDto = fillSmsAccountInfo(smsCost,smsDto);
                        try {
                            Result result = lineSmsAccountNormalService.addSmsAccount(smsAccountDto);
                            if (result.isSuccess()) {
                                smsCostAlarmDto.setSuccessCost(smsCostAlarmDto.getSuccessCost() + 1);
                            }else {
                                //smsCost下游失败
                                CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
                                costPriceExRecord.setJsonData(JSONObject.toJSONString(smsCost));
                                costPriceExRecord.setType(1);
                                String ddReason = "供应商["+smsCost.getLineSupplier()+"]线路["+smsCost.getLineName()+"],新增失败("+result.getMessage()+"),请检查";
                                JSONObject reasonObj = new JSONObject();
                                reasonObj.put("ddReason", ddReason);
                                reasonObj.put("smsDto", smsDto);
                                costPriceExRecord.setReason(JSONObject.toJSONString(reasonObj));
                                JSONObject extendObj =JSONObject.parseObject(JSONObject.toJSONString(smsDto));
                                extendObj.put("failMsg",result.getMessage());
                                costPriceExRecord.setExtend(extendObj.toJSONString());
                                costPriceExRecordMapper.insertSelective(costPriceExRecord);
                                List<CostPriceExRecordDto> costPriceExRecordList = smsCostAlarmDto.getCostPriceExRecordDtoList();
                                costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,ddReason));
                                smsCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
                                smsCostAlarmDto.setFailCount(smsCostAlarmDto.getFailCount() + 1);
                            }
                            ThreadContextInfo.removeUser();
                        } catch (Exception e) {
                            //smsCost下游异常
                            dealExceptionReason(smsCostAlarmDto,smsCost,null,e,1);
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                                    JSONObject.toJSONString(smsCost)+e.getMessage(), TITLE), e);
                        }
                    }else if (count >0){
                        //记录已存在条数
                        smsCostAlarmDto.setExistCount(smsCostAlarmDto.getExistCount() + 1);
                    }
                });
            }catch (Exception e){
                // 单个smsCost处理异常
                dealExceptionReason(smsCostAlarmDto,smsCost,null,e,1);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                        JSONObject.toJSONString(smsCost)+e.getMessage(), TITLE), e);
            }
        });
    }


    private void lineCostCompareAndDbDeal(List<DdDataLineCostPrice> ddDataLineCostPriceList,
                                          List<DdLineBaseInfoDto> ddLineBaseInfoDtoList, DdLinsSmsCostAlarmDto linsCostAlarmDto) {
        ddDataLineCostPriceList.forEach(lineCost -> {
            try{
                // 1.钉钉文档参数校验
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
                    JSONObject reasonObj = new JSONObject();
                    String ddReason = "供应商[" + lineCost.getLineSupplier() + "]主叫号码[" + lineCost.getCaller()+"],在线路侧不存在";
                    reasonObj.put("ddReason", ddReason);
                    costPriceExRecord.setReason(JSONObject.toJSONString(reasonObj));
                    costPriceExRecordMapper.insertSelective(costPriceExRecord);
                    List<CostPriceExRecordDto> costPriceExRecordList = linsCostAlarmDto.getCostPriceExRecordDtoList();
                    costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,ddReason));
                    linsCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
                    linsCostAlarmDto.setFailCount(linsCostAlarmDto.getFailCount() + 1);
                    return;
                }

                // 4. 判断数据库配置 是否存在(存在跳过，不存在插入)
                filterList.forEach(lineDto -> {
                    Long lineSupplierId = lineSupplierInfoNormalMapper.selectIdByLineSupplier(lineDto.getLineSupplier());
                    Long count = lineAccountDetailNormalMapper.selectCount(lineSupplierId,lineDto.getGatewayId());
                    if (count == 0) {
                        fillThreadLocalUserInfo(0,lineCost.getLastModifiedUserName(),lineCost.getLastModifiedUserId());
                        LineAccountDto lineAccountDto = fillLineAccountInfo(lineCost, lineDto);
                        try {
                            Result result = lineSmsAccountNormalService.addLineAccount(lineAccountDto);
                            if (result.isSuccess()) {
                                linsCostAlarmDto.setSuccessCost(linsCostAlarmDto.getSuccessCost() + 1);
                            } else {
                                //lineCost下游失败
                                CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
                                costPriceExRecord.setJsonData(JSONObject.toJSONString(lineCost));
                                costPriceExRecord.setType(2);
                                JSONObject reasonObj = new JSONObject();
                                String ddReason = "供应商[" + lineCost.getLineSupplier() + "]主叫号码[" + lineCost.getCaller()
                                        + "],新增失败("+result.getMessage()+"),请检查";
                                reasonObj.put("ddReason", ddReason);
                                reasonObj.put("lineDto", JSONObject.toJSONString(lineDto));
                                reasonObj.put("failMsg",result.getMessage());
                                costPriceExRecord.setReason(JSONObject.toJSONString(reasonObj));
                                costPriceExRecordMapper.insertSelective(costPriceExRecord);
                                List<CostPriceExRecordDto> costPriceExRecordList = linsCostAlarmDto.getCostPriceExRecordDtoList();
                                costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,ddReason));
                                linsCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
                                linsCostAlarmDto.setFailCount(linsCostAlarmDto.getFailCount() + 1);
                            }
                            ThreadContextInfo.removeUser();
                        } catch (Exception e) {
                            //lineCost下游异常
                            dealExceptionReason(linsCostAlarmDto,null,lineCost,e,2);
                            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                                    JSONObject.toJSONString(lineCost)+e.getMessage(), TITLE), e);
                        }
                    }else if (count >0){
                        //存在 设置存在条数
                        linsCostAlarmDto.setExistCount(linsCostAlarmDto.getExistCount() + 1);
                    }
                });
            }catch (Exception e){
                //单个lineCost处理异常
                dealExceptionReason(linsCostAlarmDto,null,lineCost,e,2);
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINESMS_ERROR.getCode(),
                        JSONObject.toJSONString(lineCost)+e.getMessage(), TITLE), e);
            }
        });
    }

    private void dealExceptionReason(DdLinsSmsCostAlarmDto smsLineCostAlarmDto,DdDataSmsCostPrice smsCost,
                                     DdDataLineCostPrice lineCost, Exception e, Integer type) {
        CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
        costPriceExRecord.setType(type);

        if(type == 1){
            costPriceExRecord.setJsonData(JSONObject.toJSONString(smsCost));
            String ddReason = "供应商["+smsCost.getLineSupplier()+"]线路["+smsCost.getLineName()+"],新增失败,请检查";
            JSONObject reasonObj = new JSONObject();
            reasonObj.put("ddReason", ddReason);
            reasonObj.put("failMsg", e.getMessage());
            costPriceExRecord.setReason(JSONObject.toJSONString(reasonObj));
            List<CostPriceExRecordDto> costPriceExRecordList = smsLineCostAlarmDto.getCostPriceExRecordDtoList();
            costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,ddReason));
            smsLineCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
            smsLineCostAlarmDto.setFailCount(smsLineCostAlarmDto.getFailCount() + 1);
        }else {
            costPriceExRecord.setJsonData(JSONObject.toJSONString(lineCost));
            JSONObject reasonObj = new JSONObject();
            String ddReason = "供应商[" + lineCost.getLineSupplier() + "]主叫号码[" + lineCost.getCaller()  + "],新增失败,请检查";
            reasonObj.put("ddReason", ddReason);
            reasonObj.put("failMsg",e.getMessage());
            costPriceExRecord.setReason(JSONObject.toJSONString(reasonObj));
            List<CostPriceExRecordDto> costPriceExRecordList = smsLineCostAlarmDto.getCostPriceExRecordDtoList();
            costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,ddReason));
            smsLineCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
            smsLineCostAlarmDto.setFailCount(smsLineCostAlarmDto.getFailCount() + 1);
        }


    }

    private LineAccountDto fillLineAccountInfo(DdDataLineCostPrice lineCost, DdLineBaseInfoDto lineDto) {
        LineAccountDto lineAccountDto = new LineAccountDto();
        lineAccountDto.setLineSupplier(lineDto.getLineSupplier());

        List<LineCallerDto> lines = new ArrayList<>();
        LineCallerDto lineCallerDto = new LineCallerDto();
        lineCallerDto.setGatewayId(lineDto.getGatewayId());
        lineCallerDto.setCallerFullname(lineDto.getProjectName()+"-"+lineDto.getCaller());
        lines.add(lineCallerDto);
        lineAccountDto.setLines(lines);

        List<PriceDateDTO> priceDates = new ArrayList<>();
        PriceDateDTO priceDateDTO = new PriceDateDTO();
        priceDateDTO.setEffectStartDate((LocalDate.parse(lineCost.getEffectDate())));
        if (lineCost.getIsCalcCost()==null || lineCost.getIsCalcCost().isEmpty() || lineCost.getIsCalcCost().equals("0")) {
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
        priceDateDTO.setEffectEndDate(LocalDate.parse("9999-12-31")); //TODO 有效期结束时间
        if (smsCost.getIsCalcCost() == null || smsCost.getIsCalcCost().isEmpty() || smsCost.getIsCalcCost().equals("0")) {
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

        boolean lineSupplierInvalid = StringUtils.isEmpty(smsCost.getLineSupplier());
        boolean lineNameInvalid = StringUtils.isEmpty(smsCost.getLineName());
        boolean effectDateInvalid = StringUtils.isEmpty(smsCost.getEffectDate()) || !isValidDateFormat(smsCost.getEffectDate());
        boolean priceInvalid = "1".equals(smsCost.getIsCalcCost()) && StringUtils.isEmpty(smsCost.getPrice());

        if (lineSupplierInvalid || lineNameInvalid || effectDateInvalid || priceInvalid) {
            StringBuilder reason = new StringBuilder();
            if(lineSupplierInvalid && lineNameInvalid){
                reason.append("供应商和短信线路名称为空");
            } else {
                StringBuilder prefixBuilder = new StringBuilder();
                if (!lineSupplierInvalid && !lineNameInvalid) {
                    prefixBuilder.append("供应商[").append(smsCost.getLineSupplier()).append("]短信线路名称[").append(smsCost.getLineName()).append("]");
                    if (priceInvalid){
                        prefixBuilder.append("单价/");
                    }
                } else if (!lineSupplierInvalid) {
                    prefixBuilder.append("供应商[").append(smsCost.getLineSupplier()).append("]短信线路名称/");
                    if (priceInvalid) {
                        prefixBuilder.append("单价/");
                    }
                } else {
                    prefixBuilder.append("短信线路名称[").append(smsCost.getLineName()).append("]供应商/");
                    if (priceInvalid) {
                        prefixBuilder.append("单价/");
                    }
                }

                reason.append(prefixBuilder);
                // 整理格式并添加提示
                if (reason.charAt(reason.length() - 1) == '/') {
                    reason.setLength(reason.length() - 1);
                    reason.append("为空");
                    if (effectDateInvalid) {
                        if(StringUtils.isEmpty(smsCost.getEffectDate())) {
                            reason.append(",有效期为空");
                        }else if(!isValidDateFormat(smsCost.getEffectDate())) {
                            reason.append(",有效期格式错误");
                        }
                    }
                }else {
                    if (effectDateInvalid) {
                        if(StringUtils.isEmpty(smsCost.getEffectDate())) {
                            reason.append("有效期为空");
                        }else if(!isValidDateFormat(smsCost.getEffectDate())) {
                            reason.append("有效期格式错误");
                        }
                    }
                }
                reason.append(",请检查");
            }
            JSONObject reasonObj = new JSONObject();
            reasonObj.put("ddReason", reason.toString());
            costPriceExRecord.setReason(reasonObj.toJSONString());
            Date nowDate = new Date();
            costPriceExRecord.setCreateTime(nowDate);
            costPriceExRecord.setUpdateTime(nowDate);
            costPriceExRecordMapper.insertSelective(costPriceExRecord);
            smsCostAlarmDto.setFailCount(smsCostAlarmDto.getFailCount() + 1);
            List<CostPriceExRecordDto> costPriceExRecordList = smsCostAlarmDto.getCostPriceExRecordDtoList();
            costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,reason.toString()));
            smsCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
            return false;
        }
        return true;
    }

    private boolean lineDdParamCheck(DdDataLineCostPrice lineCost, DdLinsSmsCostAlarmDto linsCostAlarmDto) {
        CostPriceExRecord costPriceExRecord = new CostPriceExRecord();
        costPriceExRecord.setType(2);
        costPriceExRecord.setJsonData(JSONObject.toJSONString(lineCost));

        boolean supplierInvalid = StringUtils.isEmpty(lineCost.getLineSupplier());
        boolean callerInvalid = StringUtils.isEmpty(lineCost.getCaller());
        boolean dateInvalid = StringUtils.isEmpty(lineCost.getEffectDate()) || !isValidDateFormat(lineCost.getEffectDate());
        boolean priceInvalid = "1".equals(lineCost.getIsCalcCost()) && StringUtils.isEmpty(lineCost.getPrice());

        if (supplierInvalid || callerInvalid || dateInvalid || priceInvalid) {
            StringBuilder reason = new StringBuilder();
            if (supplierInvalid && callerInvalid) {
                reason.append("供应商和主叫号码为空");
            } else {
                StringBuilder baseDescBuilder = new StringBuilder();
                if (!supplierInvalid && !callerInvalid) {
                    baseDescBuilder.append("供应商[").append(lineCost.getLineSupplier()).append("]主叫号码[").append(lineCost.getCaller()).append("]");
                    if (priceInvalid) {
                        baseDescBuilder.append("单价/");
                    }
                } else if (!supplierInvalid) {
                    baseDescBuilder.append("供应商[").append(lineCost.getLineSupplier()).append("]主叫号码/");
                    if (priceInvalid) {
                        baseDescBuilder.append("单价/");
                    }
                } else {
                    baseDescBuilder.append("主叫号码[").append(lineCost.getCaller()).append("]供应商/");
                    if (priceInvalid) {
                        baseDescBuilder.append("单价/");
                    }
                }
                reason.append(baseDescBuilder);
                // 整理格式并添加提示
                if (reason.charAt(reason.length() - 1) == '/') {
                    reason.setLength(reason.length() - 1);
                    reason.append("为空");
                    if (dateInvalid) {
                        if(StringUtils.isEmpty(lineCost.getEffectDate())) {
                            reason.append(",有效期为空");
                        }else if(!isValidDateFormat(lineCost.getEffectDate())){
                            reason.append(",有效期格式错误");
                        }
                    }
                }else {
                    if (dateInvalid) {
                        if(StringUtils.isEmpty(lineCost.getEffectDate())) {
                            reason.append("有效期为空");
                        }else if(!isValidDateFormat(lineCost.getEffectDate())){
                            reason.append("有效期格式错误");
                        }
                    }
                }
                reason.append(",请检查");
            }
            JSONObject reasonObj = new JSONObject();
            reasonObj.put("ddReason", reason.toString());
            costPriceExRecord.setReason(reasonObj.toJSONString());

            costPriceExRecordMapper.insertSelective(costPriceExRecord);
            List<CostPriceExRecordDto> costPriceExRecordList = linsCostAlarmDto.getCostPriceExRecordDtoList();
            costPriceExRecordList.add(convertPriceExRecordDto(costPriceExRecord,reason.toString()));
            linsCostAlarmDto.setCostPriceExRecordDtoList(costPriceExRecordList);
            linsCostAlarmDto.setFailCount(linsCostAlarmDto.getFailCount() + 1);
            return false;
        }
        return true;
    }

    private CostPriceExRecordDto convertPriceExRecordDto(CostPriceExRecord costPriceExRecord,String ddReason) {
        CostPriceExRecordDto costPriceExRecordDto = new CostPriceExRecordDto();
        BeanUtils.copyProperties(costPriceExRecord, costPriceExRecordDto);
        costPriceExRecordDto.setDdReason(ddReason);
        return costPriceExRecordDto;
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

