package com.br.marketing.check.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.common.log.AlertLog;
import com.br.marketing.check.service.RongShuNewScenePushBlackListService;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.robotaiapi.input.BlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneParentDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 榕树新场景外呼黑名单推送实现（仅供定时 Job 调用）。
 * <p>
 * 仅覆盖两路：上传 userType=202（当天）、转化 {@code register_time}=T-N。
 * 「T 日转化 applyResult=1 永久拉黑」不在此 Service / Job 内实现，由实时或其它链路单独处理。
 * </p>
 */
@Slf4j
@Service
public class RongShuNewScenePushBlackListServiceImpl implements RongShuNewScenePushBlackListService {

    private static final String USER_TYPE_NEW_SCENE = "202";
    private static final DateTimeFormatter EFFECTIVE_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter REQUEST_DATA_DAY_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public void executePushBlackList() {
        List<String> apiCodes = marketingCommonConfig.getRongShuNewSceneApiCodes();
        if (CollectionUtils.isEmpty(apiCodes)) {
            log.warn("榕树新场景外呼黑名单：rongShuNewScenePushBlackListApiCodes 为空，跳过执行");
            return;
        }
        for (String apiCode : apiCodes) {
            if (StringUtils.isBlank(apiCode)) {
                continue;
            }
            try {
                pushBlackForOneApiCode(apiCode);
            } catch (Exception ex) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                                "榕树新场景外呼黑名单单 apiCode 执行异常 apiCode=" + apiCode + " " + ex.getMessage()),
                        ex);
            }
        }
    }

    private void pushBlackForOneApiCode(String apiCode) {
        String tcId = tableCreateService.getTcId(apiCode);
        if (StringUtils.isBlank(tcId)) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "榕树新场景外呼黑名单未解析到 tcId，跳过 apiCode=" + apiCode));
            return;
        }
        String todayStr = LocalDate.now().format(REQUEST_DATA_DAY_FMT);
        int offsetDays = registerOffsetDays();
        LocalDate registerDate = LocalDate.now().minusDays(offsetDays);
        String registerStartTime = registerDate + " 00:00:00";
        String registerEndTime = registerDate.plusDays(1) + " 00:00:00";

        TpDynamicExecutor threadPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.RONGSHU_NEW_SCENE_BLACKLIST.getName(), 5, 20);
        try {
            String uploadSourceTag = "upload202";
            List<CompletableFuture<Void>> uploadFutures = new ArrayList<>();
            Long uploadMinId = null;
            for (; ; ) {
                List<MarketingSyncUser> uploadBatch = marketingSyncInfoMapper.getMarketingSyncByCondition(
                        apiCode, null, todayStr, USER_TYPE_NEW_SCENE, null, null, uploadMinId);
                if (CollectionUtils.isEmpty(uploadBatch)) {
                    break;
                }
                uploadFutures.add(CompletableFuture.runAsync(
                        () -> pushBlackFromSyncUsers(uploadBatch, apiCode, uploadSourceTag), threadPool));
                if (uploadBatch.size() < 2000) {
                    break;
                }
                uploadMinId = uploadBatch.get(uploadBatch.size() - 1).getId();
            }

            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

            String transferSourceTag = "transferRegisterTimeT-" + offsetDays;
            List<CompletableFuture<Void>> transferFutures = new ArrayList<>();
            Long transferMinId = null;
            for (; ; ) {
                List<MarketingTransferSyncUser> transferBatch = marketingTransferSyncUserMapper
                        .getTransferByRegisterTimeDate(tcId, apiCode, registerStartTime, registerEndTime, transferMinId);
                if (CollectionUtils.isEmpty(transferBatch)) {
                    break;
                }
                transferFutures.add(CompletableFuture.runAsync(
                        () -> pushBlackFromTransferUsers(transferBatch, apiCode, transferSourceTag), threadPool));
                if (transferBatch.size() < 2000) {
                    break;
                }
                transferMinId = transferBatch.get(transferBatch.size() - 1).getId();
            }
        } finally {
            threadPool.shutdownAndAwaitTermination();
        }
    }

    private int registerOffsetDays() {
        Integer n = marketingCommonConfig.getRongShuNewScenePushBlackListRegisterOffsetDays();
        if (n == null || n < 0) {
            return 30;
        }
        return n;
    }

    private void pushBlackFromSyncUsers(List<MarketingSyncUser> rows, String apiCode, String sourceTag) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<BlackDetailDTO> details = new ArrayList<>();
        for (MarketingSyncUser row : rows) {
            BlackDetailDTO one = buildBlackDetailFromCellMd5(row.getCellMd5(), row.getId(), apiCode, sourceTag);
            if (one != null) {
                details.add(one);
            }
        }
        pushBlackInBatches(details, apiCode, sourceTag);
    }

    private void pushBlackFromTransferUsers(List<MarketingTransferSyncUser> rows, String apiCode, String sourceTag) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<BlackDetailDTO> details = new ArrayList<>();
        for (MarketingTransferSyncUser row : rows) {
            BlackDetailDTO one = buildBlackDetailFromCustNum(row.getCustNum(), row.getId(), apiCode, sourceTag);
            if (one != null) {
                details.add(one);
            }
        }
        pushBlackInBatches(details, apiCode, sourceTag);
    }

    private BlackDetailDTO buildBlackDetailFromCellMd5(String cellMd5, Long rowId, String apiCode, String sourceTag) {
        if (StringUtils.isBlank(cellMd5)) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "榕树新场景黑名单 cell_md5 为空 apiCode=" + apiCode + " source=" + sourceTag));
            return null;
        }
        try {
            String phone = RpcClientProxy.decode(cellMd5, "cell", "md5", "");
            if (StringUtils.isBlank(phone)) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                                "榕树新场景黑名单 cell_md5 解密结果为空 apiCode=" + apiCode + " source=" + sourceTag + " cellMd5=" + cellMd5));
                return null;
            }
            BlackDetailDTO d = new BlackDetailDTO();
            d.setDataId(rowId != null ? String.valueOf(rowId) : cellMd5);
            d.setPhone(phone);
            d.setEffectiveDate(LocalDateTime.now().format(EFFECTIVE_TIME_FMT));
            return d;
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "榕树新场景黑名单 cell_md5 解密异常 apiCode=" + apiCode + " source=" + sourceTag + " cellMd5=" + cellMd5 + " " + ex.getMessage()),
                    ex);
            return null;
        }
    }

    private BlackDetailDTO buildBlackDetailFromCustNum(String custNum, Long rowId, String apiCode, String sourceTag) {
        if (StringUtils.isBlank(custNum)) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "榕树新场景黑名单 cust_num 为空 apiCode=" + apiCode + " source=" + sourceTag));
            return null;
        }
        try {
            String phone = RpcClientProxy.decode(custNum, "cell", "md5", "");
            if (StringUtils.isBlank(phone)) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                                "榕树新场景黑名单 cust_num 解密结果为空 apiCode=" + apiCode + " source=" + sourceTag + " custNum=" + custNum));
                return null;
            }
            BlackDetailDTO d = new BlackDetailDTO();
            d.setDataId(String.valueOf(rowId));
            d.setPhone(phone);
            d.setEffectiveDate(LocalDateTime.now().format(EFFECTIVE_TIME_FMT));
            return d;
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "榕树新场景黑名单 cust_num 解密异常 apiCode=" + apiCode + " source=" + sourceTag + " custNum=" + custNum + " " + ex.getMessage()),
                    ex);
            return null;
        }
    }

    private void pushBlackInBatches(List<BlackDetailDTO> blackDetailList, String apiCode, String sourceTag) {
        if (CollectionUtils.isEmpty(blackDetailList)) {
            return;
        }
        BlackPhoneDTO<BlackDetailDTO> jsondata = new BlackPhoneDTO<>();
        jsondata.setMethod("blackData");
        jsondata.setData(blackDetailList);
        ReqBlackPhoneDTO dto = new ReqBlackPhoneDTO();
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(jsondata));
        ReqBlackPhoneParentDTO parentDTO = new ReqBlackPhoneParentDTO();
        parentDTO.setDto(dto);
        parentDTO.setBlackDetailDTOList(blackDetailList);
        Result<String> callResult = methodRetryHandlerService.callCustomerBlack(parentDTO, 0);
        if (!ResultCode.SUCCESS.getValue().equals(callResult.getCode())) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            String.format(
                                    "榕树新场景推送黑名单失败 apiCode=%s source=%s code=%s data=%s",
                                    apiCode,
                                    sourceTag,
                                    callResult.getCode(),
                                    callResult.getData())));
        }
    }
}
