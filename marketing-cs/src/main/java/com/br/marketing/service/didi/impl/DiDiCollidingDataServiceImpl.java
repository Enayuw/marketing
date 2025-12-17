package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.v5.DiDiV5CollidingRequestDTO;
import com.br.marketing.client.didi.output.v5.DiDiV5CollidingResultResponseDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.rocketmq.MarketingOutsideInterfaceConstants;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.entity.DiDiV5CollidingData;
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
                Result<DiDiV5CollidingResultResponseDTO> response = diDiV5Client.colliding(mediaName, requestDTO);
                DiDiV5CollidingResultResponseDTO result = response.getData();
                rocketMqSwitch.syncSend(MarketingOutsideInterfaceConstants.TOPIC,
                        MarketingOutsideInterfaceConstants.MARKETING_DIDI_V5_COLLIDING_LOG, JSONObject.toJSONString(result));
            }));
        }
        pushPool.shutdownAndAwaitTermination();
    }
}
