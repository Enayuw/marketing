package com.br.marketing.chain.xiecheng.cpa;

import com.alibaba.fastjson.JSONArray;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.chain.xiecheng.AbstractXieChengReportHandler;
import com.br.marketing.context.XieChengReportContext;
import com.br.marketing.entity.XieChengJudgeConvTypeValue;
import com.br.marketing.enums.HandlerStageEnum;
import com.br.marketing.enums.XieChengBizMarkEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ValidityPeriodDataService;
import cn.hutool.core.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class XieChengReportConvTypeCpaHandler extends AbstractXieChengReportHandler {

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private ValidityPeriodDataService validityPeriodDataService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public String process(XieChengReportContext context) {
        boolean hasConvType = hasConvType(
                context.getPushConfig().getMainApiCode(),
                context.getPushConfig().getConvTypeApiCodes(),
                context.getTcId(),
                context.getSha256Tel());
        if (hasConvType) return "有效期内命中convType106或107或110";
        return null;
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

    protected XieChengReportConvTypeCpaHandler() {
        super("xieChengReportConvTypeCpa", XieChengBizMarkEnum.CPA.name(), HandlerStageEnum.THREAD.name());
    }
}
