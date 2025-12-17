package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.DiDiV5CollidingData;
import com.br.marketing.entity.DiDiV5CollidingDataLog;
import com.br.marketing.mapper.DiDiV5CollidingDataLogMapper;
import com.br.marketing.mapper.DiDiV5CollidingDataMapper;
import com.br.marketing.service.didi.DiDiCollidingDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import java.util.Date;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
public class DiDiCollidingDataServiceImpl implements DiDiCollidingDataService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DiDiV5CollidingDataMapper diDiV5CollidingDataMapper;

    @Resource
    private DiDiV5CollidingDataLogMapper diDiV5CollidingDataLogMapper;

    @Resource
    private DiDiV5Client diDiV5Client;

    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Override
    public void colliding(JobExecutionMultipleShardingContext context) {
        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.DIDI_V5_COLLIDING.getName(), 50, 50);
        boolean flag = true;
        while (flag) {
            JSONObject collidingConfig = marketingCommonConfig.getDiDiV5Config();
            int limit = collidingConfig.getInteger("limit") != null ? collidingConfig.getInteger("limit") : 2000;
            String mediaName = collidingConfig.getString("mediaName") != null ? collidingConfig.getString("mediaName") : "bairongC";
            String token = collidingConfig.getString("token") != null ? collidingConfig.getString("token") : "DK&SgWl!fZ%WVSXe";
            List<DiDiV5CollidingData> collidingDatas = diDiV5CollidingDataMapper.queryCollidingData(limit, DateUtil.beginOfDay(new Date()),
                    new Date());
            if (CollectionUtils.isEmpty(collidingDatas)) {
                flag = false;
                continue;
            }
            collidingDatas.forEach((DiDiV5CollidingData collidingData) -> pushPool.execute(() -> {
                DiDiV5CollidingRequestDTO requestDTO = new DiDiV5CollidingRequestDTO();
                requestDTO.setSign(collidingData.getCell());
                String timestamp = String.valueOf(System.currentTimeMillis());
                requestDTO.setTimestamp(timestamp);
                requestDTO.setSignature(MD5Util.encode(collidingData.getCell() + timestamp + token));
                Result<String> response = diDiV5Client.colliding(mediaName, requestDTO);
                JSONObject dto = new JSONObject();
                dto.put("dataId", collidingData.getId());
                dto.put("localId", collidingData.getLocalId());
                dto.put("returnContent", response.getData());
                rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC,
                        MarketingOutsideInterfaceConstants.MARKETING_DIDI_V5_COLLIDING_DATA, dto.toJSONString());
            }));
        }
        pushPool.shutdownAndAwaitTermination();
    }

    @Override
    public Result<Boolean> saveDiDiCollidingDataLog(String bodyString) {
        Result<Boolean> result = new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(false);
        JSONObject dto = JSONObject.parseObject(bodyString);
        Long dataId = dto.getLong("dataId");
        Long localId = dto.getLong("localId");
        String returnContent = dto.getString("returnContent");
        JSONObject jsonObject = JSONObject.parseObject(returnContent);
        String httpcode = jsonObject.getString("httpcode");
        String content = jsonObject.getString("content");
        DiDiV5CollidingDataLog diDiV5CollidingDataLog = new DiDiV5CollidingDataLog();
        diDiV5CollidingDataLog.setHttpCode(httpcode);
        diDiV5CollidingDataLog.setDataId(dataId);
        diDiV5CollidingDataLog.setLocalId(localId);
        diDiV5CollidingDataLog.setReturnContent(returnContent);
        if ("200".equals(httpcode) || StringUtils.isNotBlank(content)) {
            DiDiV5CollidingResultResponseDTO diDiV5CollidingResultResponseDTO = JSON.parseObject(content, DiDiV5CollidingResultResponseDTO.class);
            diDiV5CollidingDataLog.setErrorCode(diDiV5CollidingResultResponseDTO.getErrorCode());
            diDiV5CollidingDataLog.setErrorMessage(diDiV5CollidingResultResponseDTO.getErrorMessage());
            diDiV5CollidingDataLog.setResult(String.valueOf(diDiV5CollidingResultResponseDTO.getResult().getResult()));
            diDiV5CollidingDataLog.setFailReason(String.valueOf(diDiV5CollidingResultResponseDTO.getResult().getFailReason()));
            diDiV5CollidingDataLog.setUserGroup(String.valueOf(diDiV5CollidingResultResponseDTO.getResult().getUserGroup()));
            diDiV5CollidingDataLog.setNextTime(String.valueOf(diDiV5CollidingResultResponseDTO.getResult().getNextTime()));
        }
        diDiV5CollidingDataLogMapper.insertSelective(diDiV5CollidingDataLog);
        return result;
    }
}
