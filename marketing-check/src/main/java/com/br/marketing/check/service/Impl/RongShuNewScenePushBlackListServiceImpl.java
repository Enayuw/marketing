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
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
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

/**
 * 榕树新场景外呼黑名单推送。
 */
@Slf4j
@Service
public class RongShuNewScenePushBlackListServiceImpl implements RongShuNewScenePushBlackListService {

    private static final String USER_TYPE_NEW_SCENE = "202";
    private static final String APPLY_RESULT_PASS = "1";
    private static final String EXTEND_INFO_TAG = "RongShuNewSceneBlack";
    private static final int BATCH_PUSH_SIZE = 500;
    private static final int TRANSFER_PAGE_SIZE = 2000;
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
        List<String> apiCodes = marketingCommonConfig.getRongShuNewScenePushBlackListApiCodes();
        if (CollectionUtils.isEmpty(apiCodes)) {
            log.warn("榕树新场景外呼黑名单：rongShuNewScenePushBlackListApiCodes 为空，跳过执行");
            return;
        }
        for (String apiCodeRaw : apiCodes) {
            if (StringUtils.isBlank(apiCodeRaw)) {
                continue;
            }
            String apiCode = apiCodeRaw.trim();
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
        String pastRequestData = LocalDate.now().minusDays(offsetDays).format(REQUEST_DATA_DAY_FMT);

        List<MarketingSyncUser> uploadRows = loadUploadUserType202Today(apiCode, todayStr);
        pushBlackFromSyncUsers(uploadRows, apiCode, "upload202");

        List<MarketingTransferSyncUser> applyToday = loadTransferByRequestDataAndApply(tcId, apiCode, todayStr, APPLY_RESULT_PASS);
        pushBlackFromTransferUsers(applyToday, apiCode, "transferApply1");

        List<MarketingTransferSyncUser> registerOffset = loadTransferByRequestDataAndApply(tcId, apiCode, pastRequestData, null);
        pushBlackFromTransferUsers(registerOffset, apiCode, "transferRequestDataT-" + offsetDays);
    }

    private int registerOffsetDays() {
        Integer n = marketingCommonConfig.getRongShuNewScenePushBlackListRegisterOffsetDays();
        if (n == null || n < 0) {
            return 30;
        }
        return n;
    }

    private List<MarketingSyncUser> loadUploadUserType202Today(String apiCode, String appletDate) {
        List<MarketingSyncUser> all = new ArrayList<>();
        Long minId = null;
        for (; ; ) {
            List<MarketingSyncUser> batch = marketingSyncInfoMapper.getMarketingSyncByCondition(
                    apiCode, null, appletDate, USER_TYPE_NEW_SCENE, null, null, minId);
            if (CollectionUtils.isEmpty(batch)) {
                break;
            }
            all.addAll(batch);
            minId = batch.get(batch.size() - 1).getId();
            if (batch.size() < 2000) {
                break;
            }
        }
        return all;
    }

    private List<MarketingTransferSyncUser> loadTransferByRequestDataAndApply(
            String cid, String apiCode, String requestData, String applyResult) {
        List<MarketingTransferSyncUser> all = new ArrayList<>();
        int limitStart = 0;
        for (; ; ) {
            List<MarketingTransferSyncUser> batch = marketingTransferSyncUserMapper
                    .listRongShuPushBlackTransferByRequestDataAndApplyResult(
                            cid, apiCode, requestData, applyResult, limitStart);
            if (CollectionUtils.isEmpty(batch)) {
                break;
            }
            all.addAll(batch);
            if (batch.size() < TRANSFER_PAGE_SIZE) {
                break;
            }
            limitStart += TRANSFER_PAGE_SIZE;
        }
        return all;
    }

    private void pushBlackFromSyncUsers(List<MarketingSyncUser> rows, String apiCode, String sourceTag) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<BlackDetailDTO> details = new ArrayList<>();
        for (MarketingSyncUser row : rows) {
            BlackDetailDTO one = buildBlackDetailFromCustNum(row.getCustNum(), row.getId(), apiCode, sourceTag);
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
            d.setDataId(rowId != null ? String.valueOf(rowId) : custNum);
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
        int pageSize = BATCH_PUSH_SIZE;
        int totalCount = blackDetailList.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<BlackDetailDTO> subList;
            if (i == pageCount) {
                subList = blackDetailList.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = blackDetailList.subList((i - 1) * pageSize, pageSize * i);
            }
            BlackPhoneDTO<BlackDetailDTO> jsondata = new BlackPhoneDTO<>();
            jsondata.setMethod("blackData");
            jsondata.setData(subList);
            ReqBlackPhoneDTO dto = new ReqBlackPhoneDTO();
            dto.setApiCode(apiCode);
            dto.setJsonData(JSON.toJSONString(jsondata));
            ReqBlackPhoneParentDTO parentDTO = new ReqBlackPhoneParentDTO();
            parentDTO.setDto(dto);
            parentDTO.setBlackDetailDTOList(subList);
            parentDTO.setExtendInfo(EXTEND_INFO_TAG);
            Result<String> callResult = methodRetryHandlerService.callCustomerBlack(parentDTO, 0);
            if (!ResultCode.SUCCESS.getValue().equals(callResult.getCode())) {
                log.warn(
                        AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                                String.format(
                                        "榕树新场景推送黑名单失败 apiCode=%s source=%s batch=%s/%s code=%s data=%s",
                                        apiCode,
                                        sourceTag,
                                        i,
                                        pageCount,
                                        callResult.getCode(),
                                        callResult.getData())));
            }
        }
    }
}
