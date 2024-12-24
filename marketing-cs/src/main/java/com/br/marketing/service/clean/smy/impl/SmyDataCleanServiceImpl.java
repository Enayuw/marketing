package com.br.marketing.service.clean.smy.impl;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.dto.smy.request.NameValueDTO;
import com.br.marketing.dto.smy.request.SmyTransferRequestDTO;
import com.br.marketing.dto.smy.request.SmyUploadRequestDTO;
import com.br.marketing.entity.CustomizeTransferDataSmy;
import com.br.marketing.entity.CustomizeUploadDataSmy;
import com.br.marketing.mapper.CustomizeTransferDataSmyMapper;
import com.br.marketing.mapper.CustomizeUploadDataSmyMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.clean.smy.SmyDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmyDataCleanServiceImpl implements SmyDataCleanService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private CustomizeUploadDataSmyMapper customizeUploadDataSmyMapper;
    @Resource
    private CustomizeTransferDataSmyMapper customizeTransferDataSmyMapper;

    @Resource
    private PushInfoService pushInfoService;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public void cleanCustomizedUploadData(String apiCode, String date) {
        String tCid = tableCreateService.getTcId(apiCode);
        JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataCleanConfig();
        Integer limit = smyCustomizeDataConfig.getInteger("limit");
        Integer threadNum = smyCustomizeDataConfig.getInteger("threadNum");
        String userType = smyCustomizeDataConfig.getString("userType");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "SMY_CUSTOMIZED_UPLOAD_DATA_CLEAN_THREAD_POOL", 200);
        boolean mark = Boolean.TRUE;
        Long minId = customizeUploadDataSmyMapper.smyCleanCustomizedUploadDataOfMinId(tCid, apiCode, date);
        if (minId == null) {
            return;
        }
        minId = minId - 1;
        while (mark) {
            List<CustomizeUploadDataSmy> uploadDataSmyList =
                customizeUploadDataSmyMapper.smyCleanCustomizedUploadDataByMinId(tCid, apiCode, date, minId, limit);
            if (uploadDataSmyList.size() <= 0) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = uploadDataSmyList.get(uploadDataSmyList.size() - 1).getId();
            threadPool.submit(() -> {
                for (CustomizeUploadDataSmy customizeUploadDataSmy : uploadDataSmyList) {
                    SmyUploadRequestDTO smyUploadRequestDTO =
                        JSONObject.parseObject(customizeUploadDataSmy.getRequestJsonData(), SmyUploadRequestDTO.class);
                    Result<Boolean> result;
                    try {
                        MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
                        userDTO.setTaskId(smyUploadRequestDTO.getCaseType());
                        userDTO.setRequestId(apiCode + "_" + smyUploadRequestDTO.getCaseType() + "_" + UUID.fastUUID().toString(true));
                        List<MarketingPreUserDetailDTO> dataUploadItems = buildUploadDataItems(userType,smyUploadRequestDTO);
                        userDTO.setDataItems(dataUploadItems);
                        UploadDataDTO uploadDataDTO = new UploadDataDTO();
                        uploadDataDTO.setApiCode(apiCode);
                        uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
                        result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            customizeUploadDataSmyMapper.updateSyncStatusById(tCid, customizeUploadDataSmy.getId(), 1);
                        }
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(),
                            "萨摩耶定制上传数据清洗，子线程处理异常，前置表id：" + customizeUploadDataSmy.getId()), e);
                    }
                }
            });
        }
        shutDownThreadPool(threadPool);
    }

    private List<MarketingPreUserDetailDTO> buildUploadDataItems(String userType, SmyUploadRequestDTO uploadRequestDTO) {
        List<NameValueDTO> nameList = uploadRequestDTO.getNameList();
        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
        nameList.forEach((NameValueDTO nameValueDTO) -> {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            marketingPreUserDetailDTO.setCell(nameValueDTO.getMidMd5());
            marketingPreUserDetailDTO.setCustNum(nameValueDTO.getCidMd5());
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("userType", userType);
            reserveField1.put("registerTime", nameValueDTO.getRegisterDatetime());
            if (nameValueDTO.getExtendFields() != null && !nameValueDTO.getExtendFields().isEmpty()) {
                reserveField1.putAll(nameValueDTO.getExtendFields());
            }
            marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
            dataItems.add(marketingPreUserDetailDTO);
        });
        return dataItems;
    }

    @Override
    public void cleanCustomizedTransferData(String apiCode, String date) {
        String tCid = tableCreateService.getTcId(apiCode);
        JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataCleanConfig();
        Integer limit = smyCustomizeDataConfig.getInteger("limit");
        Integer threadNum = smyCustomizeDataConfig.getInteger("threadNum");
        String userType = smyCustomizeDataConfig.getString("userType");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "SMY_CUSTOMIZED_TRANSFER_DATA_CLEAN_THREAD_POOL", 200);
        boolean mark = Boolean.TRUE;
        Long minId = customizeTransferDataSmyMapper.smyCleanCustomizedUploadDataOfMinId(tCid, apiCode, date);
        if (minId == null) {
            return;
        }
        minId = minId - 1;
        while (mark) {
            List<CustomizeTransferDataSmy> transferDataSmyList =
                customizeTransferDataSmyMapper.smyCleanCustomizedUploadDataByMinId(tCid, apiCode, date, minId, limit);
            if (transferDataSmyList.size() <= 0) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = transferDataSmyList.get(transferDataSmyList.size() - 1).getId();
            threadPool.submit(() -> {
                for (CustomizeTransferDataSmy customizeTransferDataSmy : transferDataSmyList) {
                    SmyTransferRequestDTO smyUploadRequestDTO =
                        JSONObject.parseObject(customizeTransferDataSmy.getRequestJsonData(), SmyTransferRequestDTO.class);
                    Result<Boolean> result;
                    try {
                        TransferDataDTO<TransferDataItemDTO> transferDataDTO = new TransferDataDTO<>();
                        transferDataDTO.setRequestId(apiCode + "_" + UUID.fastUUID().toString(true));
                        List<TransferDataItemDTO> dataTransferItems = buildTransferDataItems(userType, smyUploadRequestDTO);
                        transferDataDTO.setDataItems(dataTransferItems);
                        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
                        dto.setApiCode(apiCode);
                        dto.setJsonData(JSON.toJSONString(transferDataDTO));
                        result = pushInfoService.pushTransferByRetry(dto, null);
                        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            customizeTransferDataSmyMapper.updateSyncStatusById(tCid, customizeTransferDataSmy.getId(), 1);
                        }
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(),
                            "萨摩耶定制转化数据清洗，子线程处理异常，前置表id：" + customizeTransferDataSmy.getId()), e);
                    }
                }
            });
        }
        shutDownThreadPool(threadPool);
    }

    private List<TransferDataItemDTO> buildTransferDataItems(String userType, SmyTransferRequestDTO smyUploadRequestDTO) {
        List<TransferDataItemDTO> dataItems = Lists.newArrayList();
        TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
        transferDataItemDTO.setCustNum(smyUploadRequestDTO.getCid());
        transferDataItemDTO.setUserType(userType);
        DateTime date = DateUtil.date(smyUploadRequestDTO.getEventTime());
        String formatDateTime = DateUtil.formatDateTime(date);
        if ("login".equals(smyUploadRequestDTO.getEventType())) {
            transferDataItemDTO.setIfLogin("1");
            transferDataItemDTO.setLoginTime(formatDateTime);
        }
        if ("finish".equals(smyUploadRequestDTO.getEventType())) {
            transferDataItemDTO.setIfApply("1");
            transferDataItemDTO.setApplyDt(formatDateTime);
        }
        if ("approve".equals(smyUploadRequestDTO.getEventType())) {
            transferDataItemDTO.setApplyResult("1");
            transferDataItemDTO.setApplyTime(formatDateTime);
        }
        if ("approve".equals(smyUploadRequestDTO.getEventType())) {
            transferDataItemDTO.setApplyResult("1");
            transferDataItemDTO.setAuditTime(formatDateTime);
        }
        if ("loan".equals(smyUploadRequestDTO.getEventType())) {
            transferDataItemDTO.setIfLent("1");
            transferDataItemDTO.setLentTime(formatDateTime);
        }
        JSONObject reserveField1 = new JSONObject();
        if ("blacklist".equals(smyUploadRequestDTO.getEventType())) {
            reserveField1.put("isBlack", "1");
        }
        if ("F1".equals(smyUploadRequestDTO.getEventType())) {
            reserveField1.put("F1", "1");
        }
        if ("F2".equals(smyUploadRequestDTO.getEventType())) {
            reserveField1.put("F2", "1");
        }
        if ("F3".equals(smyUploadRequestDTO.getEventType())) {
            reserveField1.put("F3", "1");
        }
        reserveField1.putAll(JSONObject.parseObject(smyUploadRequestDTO.getExtendFields()));
        dataItems.add(transferDataItemDTO);
        return dataItems;
    }

    public void shutDownThreadPool(ThreadPoolExecutor threadPool) {
        // 关闭线程池
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("萨摩耶数据清洗 等待线程池结束");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            log.error("萨摩耶数据清洗 线程池关闭异常,直接关闭线程池", e);
        }
    }
}
