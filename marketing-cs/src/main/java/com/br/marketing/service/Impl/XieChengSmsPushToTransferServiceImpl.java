package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferJsonDataDTO;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotDataVO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVt;
import com.br.marketing.entity.XieChengSmsCollidingDataLogVtExample;
import com.br.marketing.mapper.XieChengSmsCollidingDataLogVtMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.XieChengSmsPushToTransferService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Description XieChengSmsPushToTransferServiceImpl
 * @Author hong.chen
 * @CreateTime 2023/07/13
 */
@Service
@Slf4j
public class XieChengSmsPushToTransferServiceImpl implements XieChengSmsPushToTransferService {
    @Resource
    XieChengSmsCollidingDataLogVtMapper xieChengSmsCollidingDataLogVtMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public Result consumerXiechengSmsCollidingVtUser(String msg) {
        JSONArray jsonArray = JSON.parseArray(msg);
        List<ConversionData> conversionDataList = new ArrayList<>();
        Date nowDayStartTime = DateHelper.getNowDayStartTime();
        Date nowDayEndTime = DateHelper.getNowDayEndTime();
        String xieChengSmsApiCode = marketingCommonConfig.getXieChengSmsApiCode();
        String tcId = tableCreateService.getTcId(xieChengSmsApiCode);

        for (Object o : jsonArray) {
            String sha256Code = o.toString();
            XieChengSmsCollidingDataLogVtExample xieChengSmsCollidingDataLogVtExample = new XieChengSmsCollidingDataLogVtExample();
            xieChengSmsCollidingDataLogVtExample.createCriteria().andSha256CodeListEqualTo(sha256Code)
                    .andCreateTimeBetween(nowDayStartTime, nowDayEndTime);
            List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogs =
                    xieChengSmsCollidingDataLogVtMapper.selectByExample(xieChengSmsCollidingDataLogVtExample);

            if (CollectionUtils.isEmpty(xieChengSmsCollidingDataLogs)) {
                log.error("携程新场景短信撞库，根据sha256手机号查询查询为空：{}", sha256Code);
                continue;
            }
            Long id = xieChengSmsCollidingDataLogs.get(0).getId();

            ConversionData conversionData = new ConversionData();
            conversionData.setDataId(id.toString());
            conversionData.setCid(tcId);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String expireDate = dateFormat.format(nowDayEndTime);
            conversionData.setExpireDate(expireDate);
            conversionData.setInversionStatus("0");
            String query = RpcClientProxy.decode(sha256Code, "cell", "sha", "");
            conversionData.setPhone(query);
            conversionData.setInversionInfo("{}");
            conversionData.setPartnerProcessDate(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));

            conversionDataList.add(conversionData);
        }

        if (CollectionUtils.isEmpty(conversionDataList)) {
            // ack
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
        }
        // 每500条数据一个批次
        int pageSize = 500;
        int totalCount = conversionDataList.size();
        int pageCount = totalCount % pageSize == 0 ? (totalCount / pageSize) : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<ConversionData> subList;

            if (i == pageCount) {
                subList = conversionDataList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = conversionDataList.subList((i - 1) * pageSize, pageSize * (i));
            }

            HashMap<String, List<String>> hashMap = marketingCommonConfig.getXiechengSmsCustomerTransferApiCodes();
            // 同一批次数据，推送多个apiCode
            for (String pushApiCode : hashMap.get(xieChengSmsApiCode)) {
                TransferRobotOutboundDTO robotOutboundDTO = new TransferRobotOutboundDTO();
                robotOutboundDTO.setJsonData(new TransferJsonDataDTO(subList, null));
                robotOutboundDTO.setApiCode(pushApiCode);
                Result<TransferRobotOutboundVO<TransferRobotDataVO>> result =
                        methodRetryHandlerService.xieChengSmsCallCustomerTransfer(robotOutboundDTO, 0);

                if (result.getCode() == ResultCode.INTERNAL_SERVER_ERROR.getValue()) {
                    // unack
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
            }
        }
        // ack
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(Boolean.FALSE);
    }
}
