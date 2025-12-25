package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.DdSmsBaseInfoDto;
import com.br.marketing.dto.SmsBaseFullInfoDTO;
import com.br.marketing.entity.SmsBaseInfoNormal;
import com.br.marketing.entity.SmsVendorInfoNormal;
import com.br.marketing.mapper.LineBaseInfoNormalMapper;
import com.br.marketing.mapper.SmsBaseInfoNormalMapper;
import com.br.marketing.mapper.SmsVendorInfoNormalMapper;
import com.br.marketing.service.SmsBaseInfoSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 短信侧-基础信息同步任务任务
 */
@Component
@Slf4j
public class SmsBaseInfoSyncServiceImpl implements SmsBaseInfoSyncService {

    private final static String TITLE = "【短信侧-基础信息同步任务任务】";

    private static final String smsMethod ="getSmsVendors";
    private static final String smsApiCode = "3710012";

    @Resource
    private RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private SmsBaseInfoNormalMapper smsBaseInfoNormalMapper;

    @Resource
    private SmsVendorInfoNormalMapper smsVendorInfoNormalMapper;

    /**
     * 基础信息表(b_marketing_sms_base_info_normal) ope_status
     * 0: 未修改
     * 1: channelId->vendorId发生改变
     * 2: channelName 字段修改
     * 3: 三方短信侧删除
     *
     * 供应商组合表(b_marketing_sms_vendor_info_normal) ope_status字段枚举
     * 0: 未修改
     * 1: ---
     * 2: vendorName修改
     * 3: 删除
     */
    @Override
    public void process() {
        // 获取三方接口返回 基础信息
        List<DdSmsBaseInfoDto> ddSmsBaseInfoDtoList  = getSmsBaseInfo();

        // db-库表数据组合查询: b_marketing_sms_supplier_info_normal 里面 ope_status (0,2)
        // ---> b_marketing_sms_base_info_normal 里面 ope_status in(0,2) ,的配置进行比较
        List<SmsBaseFullInfoDTO> smsBaseFullInfoDtoList = smsBaseInfoNormalMapper.selectSmsBaseUseInfoList();

        // 场景1-差集剔除(库表有,三方接口没有)
        dealSceneOne(ddSmsBaseInfoDtoList,smsBaseFullInfoDtoList);

        // 场景2-新增集处理(库表没有,三方接口有)
        dealSceneTwo(ddSmsBaseInfoDtoList,smsBaseFullInfoDtoList);

        // 场景3-历史集合修改(库表有,三方接口有)
        dealSceneThree(ddSmsBaseInfoDtoList,smsBaseFullInfoDtoList);
    }

    /**
     * 场景1-差集剔除
     * (库表有,三方接口没有)
     */
    private void dealSceneOne(List<DdSmsBaseInfoDto> ddSmsBaseInfoDtoList, List<SmsBaseFullInfoDTO> smsBaseFullInfoDtoList) {
        try {
            List<Long> channelIdList = ddSmsBaseInfoDtoList.stream().map(DdSmsBaseInfoDto::getChannelId).toList();
            List<Long> dbChannelIdList = smsBaseFullInfoDtoList.stream().map(SmsBaseFullInfoDTO::getChannelId).toList();
            List<Long> onlyInDbIdList = dbChannelIdList.stream()
                    .filter(id -> !channelIdList.contains(id)).toList();
            // 修改这些记录 ope_status = 3
            if (!onlyInDbIdList.isEmpty()) {
                smsBaseInfoNormalMapper.updateOnlyDbOpStatus(onlyInDbIdList,3);
            }
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_SMS_CHANGE_ERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }

    /**
     * 场景2-新增集处理
     * (库表没有, 三方接口有)
     */
    private void dealSceneTwo(List<DdSmsBaseInfoDto> ddSmsBaseInfoDtoList, List<SmsBaseFullInfoDTO> smsBaseFullInfoDtoList) {
        try {
            Set<Long> dbChannelIdIds = smsBaseFullInfoDtoList.stream()
                    .map(SmsBaseFullInfoDTO::getChannelId).collect(Collectors.toSet());
            List<DdSmsBaseInfoDto> onlyInGatewayDtoList = ddSmsBaseInfoDtoList.stream()
                    .filter(dto -> !dbChannelIdIds.contains(dto.getChannelId())).toList();
            // 添加记录
            onlyInGatewayDtoList.forEach(dto -> {
                checkSmsVendorExist(dto.getVendorId(),dto.getVendorName());
                SmsBaseInfoNormal smsBaseInfoNormal = smsBaseInfoNormalMapper.selectByChannelId(dto.getVendorId(),dto.getChannelId());
                if (smsBaseInfoNormal == null) {
                    smsBaseInfoNormalMapper.insertSelective(fillSmsBaseInfo(dto));
                }
            });
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_SMS_CHANGE_ERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }


    /**
     * 场景3-历史集处理
     * (库表有,三方接口有)
     */
    private void dealSceneThree(List<DdSmsBaseInfoDto> ddSmsBaseInfoDtoList, List<SmsBaseFullInfoDTO> smsBaseFullInfoDtoList) {
        Map<Long, DdSmsBaseInfoDto> ddSmsMap = ddSmsBaseInfoDtoList.stream()
                                    .collect(Collectors.toMap(DdSmsBaseInfoDto::getChannelId, dto -> dto));
        Map<Long, SmsBaseFullInfoDTO> fullDbInfoMap = smsBaseFullInfoDtoList.stream()
                                    .collect(Collectors.toMap(SmsBaseFullInfoDTO::getChannelId, dto -> dto));
        //子场景 判断修改
        ddSmsMap.forEach((channelId, ddSmsInfoItem) -> {
            try {
                if (fullDbInfoMap.containsKey(channelId)) {
                    SmsBaseFullInfoDTO dbFullInfoItem = fullDbInfoMap.get(channelId);
                    //3.1 channelId->vendorId发生了变化
                    if (!Objects.equals(ddSmsInfoItem.getVendorId(), dbFullInfoItem.getVendorId())){
                        checkSmsVendorExist(ddSmsInfoItem.getVendorId(),ddSmsInfoItem.getVendorName());
                        smsBaseInfoNormalMapper.updateBaseInfoById(dbFullInfoItem.getId(),ddSmsInfoItem.getChannelName(),
                                dbFullInfoItem.getVendorId(),1);
                        smsBaseInfoNormalMapper.insertSelective(fillSmsBaseInfo(ddSmsInfoItem));
                    }else {
                        //3.2 vendorName修改
                        if (!ddSmsInfoItem.getVendorName().equals(dbFullInfoItem.getVendorName())) {
                            smsVendorInfoNormalMapper.updateInfoById(dbFullInfoItem.getVendorPrimaryId(),
                                    ddSmsInfoItem.getVendorName(),2);
                        }
                        // 3.3 场景 channelName发生了改变
                        if (!ddSmsInfoItem.getChannelName().equals(dbFullInfoItem.getChannelName())) {
                            smsBaseInfoNormalMapper.updateBaseInfoById(dbFullInfoItem.getId(),ddSmsInfoItem.getChannelName(),
                                    dbFullInfoItem.getVendorId(),2);
                        }
                    }
                }
            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_SMS_CHANGE_ERROR.getCode(),
                        e.getMessage(), TITLE), e);
            }
        });
    }
    /**
     * 判断 b_marketing_sms_vendor_info_normal
     * - vendorId,vendorName指定的记录是否存在,不存在插入后返回
     * @return
     */
    private void checkSmsVendorExist(Long vendorId,String vendorName) {
        SmsVendorInfoNormal smsVendorInfoNormal = smsVendorInfoNormalMapper.selectByVendorId(vendorId);
        if (smsVendorInfoNormal == null) {
            SmsVendorInfoNormal itemObj = new SmsVendorInfoNormal();
            itemObj.setVendorId(vendorId);
            itemObj.setVendorName(vendorName);
            smsVendorInfoNormalMapper.insertSelective(itemObj);
        }else if (!smsVendorInfoNormal.getVendorName().equals(vendorName)) {
            smsVendorInfoNormalMapper.updateInfoById(smsVendorInfoNormal.getId(),vendorName,2);
        }
    }


    /**
     * 封装LineBaseInfoNormal 对象信息
     * @param dto
     *  private Long id
     */
    private SmsBaseInfoNormal fillSmsBaseInfo(DdSmsBaseInfoDto dto) {
        SmsBaseInfoNormal itemObj = new SmsBaseInfoNormal();
        itemObj.setChannelId(dto.getChannelId());
        itemObj.setChannelName(dto.getChannelName());
        itemObj.setVendorId(dto.getVendorId());
        return itemObj;
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
        log.warn("TITLE:{},getSmsBaseInfo:{},result:{}",TITLE,JSONObject.toJSONString(robotOutboundDTO),
                JSONObject.toJSONString(transferRobotOutboundVO));
        if ("00".equals(transferRobotOutboundVO.getCode())) {
            baseInfo =  JSONArray.parseArray(transferRobotOutboundVO.getData().toString());
        }
        for (Object obj: baseInfo) {
            JSONObject jsonObject = (JSONObject) obj;
            Long vendorId = jsonObject.getLong("vendorId");
            String vendorName = jsonObject.getString("vendorName");
            if (jsonObject.containsKey("channelDTOList")) {
                JSONArray channelArr = jsonObject.getJSONArray("channelDTOList");
                if (!channelArr.isEmpty()) {
                    for (Object channelObj : channelArr) {
                        JSONObject channelJson = (JSONObject) channelObj;
                        DdSmsBaseInfoDto dto = new DdSmsBaseInfoDto();
                        dto.setVendorId(vendorId);
                        dto.setVendorName(vendorName);
                        dto.setChannelId(channelJson.getLong("channelId"));
                        dto.setChannelName(channelJson.getString("channelName"));
                        smsBaseInfoList.add(dto);
                    }
                }else {
                    //处理"channelDTOList": []  vendorInfo的新增及修改
                    checkSmsVendorExist(vendorId,vendorName);
                }
            }
        }
        return smsBaseInfoList;
    }

}

