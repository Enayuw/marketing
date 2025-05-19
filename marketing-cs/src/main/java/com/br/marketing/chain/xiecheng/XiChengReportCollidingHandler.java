package com.br.marketing.chain.xiecheng;

import com.alibaba.fastjson.JSONArray;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.XieChengCollidingDataLogMapper;
import com.br.marketing.mapper.XieChengSmsCollidingDataLogVtMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
public class XiChengReportCollidingHandler extends AbstractXieChengReportHandler {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private XieChengCollidingDataLogMapper xieChengCollidingDataLogMapper;

    @Resource
    private XieChengSmsCollidingDataLogVtMapper xieChengSmsCollidingDataLogVtMapper;

    @Override
    void process(XieChengReportContext context) {
        String orgChannel;
        if ("1".equals(context.getPushConfig().getConditionKey())) {
            boolean hasConvType = hasConvType(
                    context.getPushConfig().getMainApiCode(),
                    context.getPushConfig().getConvTypeApiCodes(),
                    context.getTcId(),
                    context.getSha256Tel());
            if (hasConvType) {
                context.setError("有效期内命中convType106或107或110");
                return;
            }
            XieChengCollidingDataLog dataLog = xieChengCollidingDataLogMapper.selectlog(context.getSha256Tel());
            if (dataLog == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XIECHENG_SERVICEERROR.getCode()
                        , "当前数据在日志表中未查到"));
                context.setError("当前数据在日志表中未查到");
                return;
            }
            orgChannel = dataLog.getOrgChannel();
        } else {
            Integer day = Integer.valueOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            XieChengSmsCollidingDataLogVtExample vtExample = new XieChengSmsCollidingDataLogVtExample();
            vtExample.createCriteria()
                    .andSha256CodeListEqualTo(context.getSha256Tel())
                    .andStatusEqualTo(2)
                    .andSendDateEqualTo(day);
            List<XieChengSmsCollidingDataLogVt> xieChengSmsCollidingDataLogVts = xieChengSmsCollidingDataLogVtMapper.selectByExample(vtExample);
            if (xieChengSmsCollidingDataLogVts.size() == 0) {
                context.setError("没有获取到当日撞库结果");
                return;
            }
            XieChengSmsCollidingDataLogVt dataLogVt = xieChengSmsCollidingDataLogVts.get(0);
            if(!dataLogVt.getResult()){
                context.setError("命中当日撞库结果为false");
                return;
            }
            if (StringUtils.isBlank(dataLogVt.getOrgChannel())) {
                context.setError("命中当日OrgChannel为空,id="+dataLogVt.getSmsCollidingDataVtId());
                return;
            }
            orgChannel = dataLogVt.getOrgChannel();
        }
        context.getAdReqDTO().setMktChannel(orgChannel);
    }

    private boolean hasConvType(String apiCode, JSONArray convTypeApiCodes, String tcId, String sha256Tel) {
        Set<String> syncCustNumSet = new HashSet<>();
        // sha256解密，log加密
        String phone = RpcClientProxy.decode(sha256Tel, "cell", "sha", "");
        String encode = BrCipherMaker.getInstance().encode(phone);
        syncCustNumSet.add(encode);
        Map<String, SyncUserValidityPeriodBO> syncUser =
                transferDataValidityPeriodService.getValidityPeriodCellBatchFirstVersion(syncCustNumSet, apiCode, new Date());
        SyncUserValidityPeriodBO bo = syncUser.get(encode);
        if (bo != null) {
            Pair<String, String> validityRange =
                    validityPeriodDataService.getMarketingTransferDataWithValidityRange(apiCode);
            if (null == validityRange) {
                log.error("携程所有配置在有效期配置表中的上传数据均已失效！");
                return false;
            }
            String startDate = validityRange.getKey();
            String endDate = validityRange.getValue();
            Set<String> custNumSet = new HashSet<>();
            custNumSet.add(sha256Tel);
            List<XieChengJudgeConvTypeValue> xieChengJudgeConvType = marketingTransferSyncUserMapper.getXieChengJudgeConvType(tcId,
                    convTypeApiCodes,
                    startDate, endDate, custNumSet);

            if (CollectionUtils.isEmpty(xieChengJudgeConvType)) {
                return false;
            }
            XieChengJudgeConvTypeValue convTypeValue = xieChengJudgeConvType.get(0);
            // 命中convType=106或107或110
            if (convTypeValue.getHasApplySuccess() || convTypeValue.getHasInputSuccess() || convTypeValue.getHasRiskControl()) {
                return true;
            }
        }
        return false;
    }

    public XiChengReportCollidingHandler() {
        super(4);
    }
}
