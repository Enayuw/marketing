package com.br.marketing.service.didi.impl;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.didi.DiDiV5Client;
import com.br.marketing.client.didi.input.v5.DiDiV5BlackDataRequestDTO;
import com.br.marketing.client.didi.utils.MD5Util;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.DidiV5BlackData;
import com.br.marketing.entity.DidiV5BlackDataLog;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.DiDiV5BlackDataLogMapper;
import com.br.marketing.mapper.DiDiV5BlackDataMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.didi.DiDiBlackDataPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.api.client.util.Lists;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DiDiBlackDataPushServiceImpl implements DiDiBlackDataPushService {

    @Resource
    private DiDiV5Client diDiV5Client;

    @Resource
    private DiDiV5BlackDataMapper didiV5BlackDataMapper;

    @Resource
    private DiDiV5BlackDataLogMapper didiV5BlackDataLogMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private LocalFileMapper localFileMapper;

    @Override
    public void push() {
        JSONObject pushConfig = marketingCommonConfig.getDiDiV5Config();

        String mediaName = pushConfig.getString("mediaName") != null ? pushConfig.getString("mediaName") : "bairongC";
        String token = pushConfig.getString("token") != null ? pushConfig.getString("token") : "9Hqeoi36CJfdA7n4";

        LocalFileExample fileExample = new LocalFileExample();
        fileExample.createCriteria().andFileTypeEqualTo(SftpFileTypeEnum.DD_BLACK.getValue())
                .andPushStartTimeIsNull();
        List<LocalFile> localFiles = localFileMapper.selectByExample(fileExample);
        List<Long> fileIds = Lists.newArrayList();
        if(!CollectionUtils.isEmpty(localFiles)) {
            fileIds = localFiles.stream().map(LocalFile::getId).collect(Collectors.toList());
            localFileMapper.updateUploadStartTimeById(fileIds, new Date());
        }

        TpDynamicExecutor pushPool = TpDynamicExecutorFactory.getThreadPool(ThreadPoolNameEnum.DIDI_V5_BLACK_DATA.getName(), 50, 50);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        while (true) {
            JSONObject collidingConfig2 = marketingCommonConfig.getDiDiV5Config();
            boolean blackSwitch = collidingConfig2.getBoolean("blackSwitch");
            if (blackSwitch) {
                break;
            }
            int limit = collidingConfig2.getInteger("limit") != null ? collidingConfig2.getInteger("limit") : 2000;
            List<DidiV5BlackData> dataList = didiV5BlackDataMapper.queryData(limit);
            markAsPushing(dataList);
            dataList.forEach(data -> {
                CompletableFuture<Void> future = CompletableFuture.runAsync(
                        () -> pushData(data, mediaName, token),
                        pushPool
                );
                futures.add(future);
            });
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        updateLocalFiles(fileIds);
    }

    private void updateLocalFiles(List<Long> fileIds) {
        for (Long fileId : fileIds) {
            int count = didiV5BlackDataMapper.getPushStatusCountByLocalId(fileId, 0);
            if (count > 0) {
                continue;
            }
            int successCount = didiV5BlackDataMapper.getPushStatusCountByLocalId(fileId, 3);
            localFileMapper.updatePushEndTimeById(fileId, successCount, new Date());
        }
    }

    private void markAsPushing(List<DidiV5BlackData> dataList) {
        List<Long> ids = dataList.stream().map(DidiV5BlackData::getId).toList();
        didiV5BlackDataMapper.updatePushingByIds(ids);
    }

    private void pushData(DidiV5BlackData data, String mediaName, String token) {
        // 单个黑名单推送不影响其他撞库
        try {
            DiDiV5BlackDataRequestDTO requestTO = buildRequest(mediaName, data.getCell(), token);
            Result<String> response = diDiV5Client.blackData(mediaName, requestTO);
            String resData = response.getData();
            JSONObject resJson = JSONObject.parseObject(resData);
            String httpcode = resJson.getString("httpcode");
            String content = resJson.getString("content");
            boolean success = "200".equals(httpcode);
            int pushStatus = success ? 2 : 3;
            // 更新推送状态
            updateCallbackDataPushStatus(data.getId(), pushStatus);
            saveCallbackDataLog(data, httpcode, content, requestTO);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(),
                    "该手机号黑名单推送异常：" + data.getCell() + "id:" + data.getId()), e);
        }
    }

    /**
     * 更新回调数据推送状态
     */
    private void updateCallbackDataPushStatus(Long id, int pushStatus) {
        DidiV5BlackData updateData = new DidiV5BlackData();
        updateData.setId(id);
        updateData.setPushStatus(pushStatus);
        updateData.setUpdateTime(new Date());
        didiV5BlackDataMapper.updateByPrimaryKeySelective(updateData);
    }

    private void saveCallbackDataLog(DidiV5BlackData data, String httpcode, String content,
                                     DiDiV5BlackDataRequestDTO requestTO) {
        DidiV5BlackDataLog logEntity = new DidiV5BlackDataLog();
        logEntity.setDataId(data.getId());
        logEntity.setCell(data.getCell());
        logEntity.setHttpCode(httpcode);
        logEntity.setReturnContent(content);
        logEntity.setApiCode(data.getApiCode());
        logEntity.setIsDelete(1);
        logEntity.setCreateTime(new Date());
        logEntity.setLocalId(data.getLocalId());

        JSONObject contentJson = JSONObject.parseObject(content);
        logEntity.setErrorCode(contentJson.getString("errorCode"));
        logEntity.setErrorMessage(contentJson.getString("errorMessage"));
        if (Objects.nonNull(requestTO)) {
            logEntity.setSign(requestTO.getSign());
            logEntity.setSignature(requestTO.getSignature());
            logEntity.setTimestamp(requestTO.getTimestamp());
            logEntity.setMediaName(requestTO.getMediaName());
        }
        didiV5BlackDataLogMapper.insertSelective(logEntity);
    }


    private DiDiV5BlackDataRequestDTO buildRequest(String mediaName, String cell, String token) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return new DiDiV5BlackDataRequestDTO().setSign(cell).setMediaName(mediaName)
                .setTimestamp(timestamp).setSignature(MD5Util.encode(cell + timestamp + token));
    }
}
