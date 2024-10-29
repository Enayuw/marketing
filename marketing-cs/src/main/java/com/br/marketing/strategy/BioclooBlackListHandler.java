package com.br.marketing.strategy;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.biocloo.BioclooClient;
import com.br.marketing.client.biocloo.input.BlackDataSoleDTO;
import com.br.marketing.client.biocloo.input.DataDTO;
import com.br.marketing.client.biocloo.input.DataSoleDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BioclooBlackListHandler extends AbstractExternalInterfaceHandler<DataSoleDTO> {

    @Resource
    private BioclooClient bioclooClient;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<DataSoleDTO> dataSoles, ProcessHandlerContext context) {
        int pageSize = 500;
        
        int totalCount = dataSoles.size();
        String last = context.getLast();
        JSONObject userTypeJson = marketingCommonConfig.getShuHeToBioclooUserTypeAndApiCodeMapping();
        JSONObject proxyJson = userTypeJson.getJSONObject("百可录");
        String apiCode = proxyJson.getString(context.getApiCode());
        // 数据数组
        ArrayList<DataDTO> sendList = new ArrayList<>();
        // 数据日志数组
        ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
        int sum = 0;
        long start = System.currentTimeMillis();
        for (DataSoleDTO soleDTO : dataSoles) {
            DataDTO dataDTO = BeanUtil.copyProperties(soleDTO, DataDTO.class);
            sum++;
            sendList.add(dataDTO);
            // 把封装的日志插入到数组中
            logList.add(methodRetryHandlerService.dataJoinLogFix(dataDTO, DistributeTypeEnum.CUSTOMERTRANSFER, apiCode,
                dataDTO.getCaseNum(), dataDTO.getPhone(), Long.valueOf(soleDTO.getDataId()),
                soleDTO.getDistributeSourceTypeEnum() == null ? DistributeSourceTypeEnum.TRANSFER : soleDTO.getDistributeSourceTypeEnum(),
                soleDTO.getStatus(), soleDTO.getExpireEndDate()));
            if (sendList.size() == pageSize || sum == totalCount) {
                // 对象继承 DataDistributeLogBase
                BlackDataSoleDTO dto = new BlackDataSoleDTO();
                dto.setApiCode(apiCode);
                dto.setTransferInfoId(context.getTransferInfoId());
                dto.setData(sendList);
                dto.setDetailLogList(logList);
                if (sum == totalCount) {
                    dto.setLast(last);
                } else {
                    dto.setLast(last != null ? "0" : null);
                }
                // 传参去重
                // 去重字段维度,根据传入值赋值，默认为cell维度去重
                if (dataDTO.getSoleField() != null) {
                    dto.setSoleField(dataDTO.getSoleField());
                } else {
                    dto.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
                }
                // 去重范围,根据传入值赋值，默认当天去重
                if (dataDTO.getSoleType() != null) {
                    dto.setSoleDay(dataDTO.getSoleType());
                } else {
                    dto.setSoleDay(1);
                }
                dto.setIsSole(true);
                Result callBalckResult = bioclooClient.pushBlackDataToBiocloo(dto, 0);
                if (!ResultCode.SUCCESS.getValue().equals(callBalckResult.getCode())) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SHUHE_INTERFACEERROR.getCode(),
                        String.format("推送百可录黑名单报错：%s", callBalckResult.getData())));
                }
                sendList = new ArrayList<>();
                logList = new ArrayList<>();
            }
        }
        log.warn("推送百可录黑名单自定义去重耗时：" + (System.currentTimeMillis() - start));
        return null;
    }

    /**
     * 按照三方接口逻辑调用接口
     */
    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.BIOCLOO_BLACK_LIST;
    }
}
