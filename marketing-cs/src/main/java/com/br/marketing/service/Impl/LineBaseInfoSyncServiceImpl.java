package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.MiddleHeavenAviatorScriptApiClient;
import com.br.marketing.client.ibmpapi.IbmpApiServiceClient;
import com.br.marketing.client.ibmpapi.outpu.TransferIbmpOutboundVO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.*;
import com.br.marketing.entity.LineBaseInfoNormal;
import com.br.marketing.entity.LineSupplierInfoNormal;
import com.br.marketing.mapper.LineBaseInfoNormalMapper;
import com.br.marketing.mapper.LineSupplierInfoNormalMapper;
import com.br.marketing.service.LineBaseInfoSyncService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 线路侧-基础信息同步任务任务
 */
@Component
@Slf4j
public class LineBaseInfoSyncServiceImpl implements LineBaseInfoSyncService {

    private final static String TITLE = "【线路侧-基础信息同步任务任务】";

    @Resource
    private IbmpApiServiceClient ibmpApiServiceClient;

    @Resource
    private LineBaseInfoNormalMapper lineBaseInfoNormalMapper;

    @Resource
    private LineSupplierInfoNormalMapper lineSupplierInfoNormalMapper;

    /**
     * 基础信息表(b_marketing_line_base_info_normal) ope_status
     * 0: 未修改
     * 1: line_supplier修改 导致的数据ope_status状态修改
     * 2: projectName 其它字段导致的修改
     * 3: 三方外呼侧删除
     *
     * 供应商组合表(b_marketing_line_supplier_info_normal) ope_status字段枚举
     * 0: 未修改
     * 1: line_supplier修改 导致的数据ope_status状态修改
     */
    @Override
    public void process() {
        // 获取三方接口返回 基础信息
        List<DdLineBaseInfoDto> ddLineBaseInfoDtoList =  getLineBaseInfo();

        // db-库表数据组合查询: b_marketing_line_supplier_info_normal 里面 ope_status = 0
        // ---> b_marketing_line_supplier_info_normal 里面 ope_status in(0,2) ,的配置进行比较
        List<LineBaseFullInfoDTO> lineBaseFullInfoDtoList = lineBaseInfoNormalMapper.selectLineBaseUseInfoList();

        // 场景1-差集剔除(库表有,三方接口没有)
        dealSceneOne(ddLineBaseInfoDtoList,lineBaseFullInfoDtoList);

        // 场景2-新增集处理(库表没有,三方接口有)
        dealSceneTwo(ddLineBaseInfoDtoList,lineBaseFullInfoDtoList);

        // 场景3-历史集合修改(库表有,三方接口有)
        dealSceneThree(ddLineBaseInfoDtoList,lineBaseFullInfoDtoList);
    }

    /**
     * 场景1-差集剔除
     * (库表有,三方接口没有)
     */
    private void dealSceneOne(List<DdLineBaseInfoDto> ddLineBaseInfoDtoList, List<LineBaseFullInfoDTO> lineBaseFullInfoDtoList) {
        try {
            List<Long> gatewayIdList = ddLineBaseInfoDtoList.stream().map(DdLineBaseInfoDto::getGatewayId).toList();
            List<Long> dbGatewayIdList = lineBaseFullInfoDtoList.stream().map(LineBaseFullInfoDTO::getGatewayId).toList();
            List<Long> onlyInDbIdList = dbGatewayIdList.stream()
                    .filter(id -> !gatewayIdList.contains(id)).toList();
            // 修改这些记录 ope_status = 3
            if (!onlyInDbIdList.isEmpty()) {
                lineBaseInfoNormalMapper.updateOnlyDbOpStatus(onlyInDbIdList,3);
            }
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINE_CHANGE_ERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }

    /**
     * 场景2-新增集处理
     * (库表没有, 三方接口有)
     */
    private void dealSceneTwo(List<DdLineBaseInfoDto> ddLineBaseInfoDtoList, List<LineBaseFullInfoDTO> lineBaseFullInfoDtoList) {
        try {
            Set<Long> dbGatewayIds = lineBaseFullInfoDtoList.stream()
                    .map(LineBaseFullInfoDTO::getGatewayId).collect(Collectors.toSet());
            List<DdLineBaseInfoDto> onlyInGatewayDtoList = ddLineBaseInfoDtoList.stream()
                    .filter(dto -> !dbGatewayIds.contains(dto.getGatewayId())).toList();
            // 添加记录
            onlyInGatewayDtoList.forEach(dto -> {
                Long lineSupplierId = selectLineSupplierId(dto.getLineSupplier());
                lineBaseInfoNormalMapper.insertSelective(fillLineBaseInfo(dto,lineSupplierId));
            });
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINE_CHANGE_ERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }


    /**
     * 场景3-历史集处理
     * (库表有,三方接口有)
     * DdLineBaseInfoDto
     *    private Long gatewayId;
     *    private String caller;
     *    private String outboundNumber;
     *    private String lineSupplier;
     *    private String projectName;
     */
    private void dealSceneThree(List<DdLineBaseInfoDto> ddLineBaseInfoDtoList, List<LineBaseFullInfoDTO> lineBaseFullInfoDtoList) {
        Map<Long, DdLineBaseInfoDto> ddLineMap = ddLineBaseInfoDtoList.stream()
                                    .collect(Collectors.toMap(DdLineBaseInfoDto::getGatewayId, dto -> dto));
        Map<Long, LineBaseFullInfoDTO> fullDbInfoMap = lineBaseFullInfoDtoList.stream()
                                    .collect(Collectors.toMap(LineBaseFullInfoDTO::getGatewayId, dto -> dto));
        Map<String,Integer> existChangeMap = new HashMap<>();
        //子场景 判断修改
        ddLineMap.forEach((gatewayId, ddLineInfoItem) -> {
            try {
                if (fullDbInfoMap.containsKey(gatewayId)) {
                    LineBaseFullInfoDTO dbFullInfoItem = fullDbInfoMap.get(gatewayId);
                    //3.1 场景 lineSupplier修改 //3.2 场景 其它字段发生修改
                    if (!ddLineInfoItem.getLineSupplier().equals(dbFullInfoItem.getLineSupplier())) {
                        //3.1场景 lineSupplier修改
                        //dbFullInfoItem->gatewayId 对应b_marketing_line_base_info_normal记录  修改ope_status = 1
                        lineBaseInfoNormalMapper.updateBaseInfoById(dbFullInfoItem.getId(),ddLineInfoItem.getCaller(),
                                ddLineInfoItem.getOutboundNumber(),ddLineInfoItem.getProjectName(),1);

                        //添加新的lineSupplier->lineSupplierId->对应的新的gatewayId记录
                        Long newLineSupplierId = selectLineSupplierId(ddLineInfoItem.getLineSupplier());
                        lineBaseInfoNormalMapper.insertSelective(fillLineBaseInfo(ddLineInfoItem,newLineSupplierId));
                    }else  {
                        // 3.2 场景 其它字段发生修改 一个update修改搞定
                        if (!ddLineInfoItem.getCaller().equals(dbFullInfoItem.getCaller()) ||
                                !ddLineInfoItem.getProjectName().equals(dbFullInfoItem.getProjectName())) {
                            lineBaseInfoNormalMapper.updateBaseInfoById(dbFullInfoItem.getId(),ddLineInfoItem.getCaller(),
                                    ddLineInfoItem.getOutboundNumber(),ddLineInfoItem.getProjectName(),2);
                        }
                    }
                }
            }catch (Exception e){
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MARKETING_AVIATORSCRIPT_LINE_CHANGE_ERROR.getCode(),
                        e.getMessage(), TITLE), e);
            }
        });
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
     *     }
     * ]
     * @return
     */
    private List<DdLineBaseInfoDto> getLineBaseInfo() {
        List<DdLineBaseInfoDto> ddLineBaseInfoDtoList = new ArrayList<>();
        JSONArray baseInfo = new JSONArray();
        TransferIbmpOutboundVO transferIbmpOutboundVO = ibmpApiServiceClient.getLineBaseInfo();
        log.warn("TITLE:{},getLineBaseInfo:{}",TITLE,JSONObject.toJSONString(transferIbmpOutboundVO));
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


    /**
     * 获取lineSupplier对应组合id
     * -存在查询返回
     * -不存在插入后返回
     * @param lineSupplier
     * @return
     */
    private Long selectLineSupplierId(String lineSupplier) {
        Long  lineSupplierId = lineSupplierInfoNormalMapper.selectIdByLineSupplier(lineSupplier);
        if (ObjectUtil.isEmpty(lineSupplierId)) {
            LineSupplierInfoNormal itemObj = new LineSupplierInfoNormal();
            itemObj.setLineSupplier(lineSupplier);
            lineSupplierInfoNormalMapper.insertSelective(itemObj);
            lineSupplierId =  itemObj.getId();
        }
        return lineSupplierId;
    }

    /**
     * 封装LineBaseInfoNormal 对象信息
     * @param dto
     * @param lineSupplierId
     * @return
     */
    private LineBaseInfoNormal fillLineBaseInfo(DdLineBaseInfoDto dto, Long lineSupplierId) {
        LineBaseInfoNormal itemObj = new LineBaseInfoNormal();
        itemObj.setGatewayId(dto.getGatewayId());
        itemObj.setCaller(dto.getCaller());
        itemObj.setOutboundNumber(dto.getOutboundNumber());
        itemObj.setLineSupplierId(lineSupplierId);
        itemObj.setProjectName(dto.getProjectName());
        return itemObj;
    }

}

