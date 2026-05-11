package com.br.marketing.datarelayservice.processor;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.datarelayservice.context.TcMarketDataPushContext;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.webhook.dingding.service.DingDingRobotHookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@Service
public class TcDataPushProcessor extends AbstractTcCustomizeProcessor {

    private static final Logger log = LoggerFactory.getLogger(TcDataPushProcessor.class);

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DingDingRobotHookService dingDingRobotHookService;

    @Override
    protected String fetchApiCode() {
        return apiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrSyncRecord row = new MarketingTcyrSyncRecord();
        row.setId(recordId);
        row.setStatus(status);
        row.setMsg(msg);
        tcyrSyncRecordMapper.updateByPrimaryKeySelective(row);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrSyncRecord row = new MarketingTcyrSyncRecord();
        String scene = resolveSceneForRecord(batchNo);
        row.setApiCode(resolveApiCodeForRecord(apiCode));
        row.setRequestNo(tcRequestDTO.getRequestNo());
        row.setBatchNo(batchNo);
        row.setScene(scene);
        row.setData(tcRequestDTO.getData());
        row.setStatus(0);
        row.setDownStatus(0);
        row.setIsDel(1);
        row.setCreateTime(new Date());
        row.setUpdateTime(new Date());
        try {
            tcyrSyncRecordMapper.insertSelective(row);
            return row.getId();
        } catch (DuplicateKeyException e) {
            //告警 todo
            row.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            row.setStatus(2);
            tcyrSyncRecordMapper.insertSelective(row);
            return null;
        }
    }

    /**
     * 按 HTTP 入口决定 sync_record.scene：标准 marketDataPush 固定 null，CPA 回落至此前缀解析逻辑。
     */
    private String resolveSceneForRecord(String batchNo) {
        TcMarketDataPushContext.Entry entry = TcMarketDataPushContext.get();
        if (TcMarketDataPushContext.Entry.STANDARD_SYNC.equals(entry)) {
            return null;
        }
        if (entry == null) {
            log.warn("TcDataPushProcessor recordSave: TcMarketDataPushContext 未设置，按 CPA 回落语义解析 scene，batchNo={}",
                    batchNo);
        }
        return resolveScene(batchNo);
    }

    private String resolveScene(String batchNo) {
        Map<String, String> sceneMap = marketingCommonConfig.getTcBatchNoSuffixToSceneConfig();
        if (sceneMap == null || sceneMap.isEmpty()) {
            return "NEW";
        }
        for (Map.Entry<String, String> entry : sceneMap.entrySet()) {
            String prefix = entry.getKey();
            if (StringUtils.isBlank(prefix)) {
                continue;
            }
            if (batchNo.startsWith(prefix)) {
                return entry.getValue();
            }
        }
        return "NEW";
    }

    /**
     * 标准 /marketDataPush：api_code 先置空，后续由 batchNo 匹配回填（3710228/3710229）。
     * /cpa/marketDataPush 回落至标准 Processor：沿用传入的配置 api_code（与现网一致）。
     */
    private String resolveApiCodeForRecord(String passedApiCode) {
        TcMarketDataPushContext.Entry entry = TcMarketDataPushContext.get();
        if (TcMarketDataPushContext.Entry.STANDARD_SYNC.equals(entry)) {
            return null;
        }
        if (TcMarketDataPushContext.Entry.CPA_SYNC_FALLBACK.equals(entry)) {
            return passedApiCode;
        }
        if (StringUtils.isNotBlank(passedApiCode)) {
            return passedApiCode;
        }
        return null;
    }

    /**
     * 推送钉钉告警
     */
    private void notice(String message) {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> groupInfo = webHookInfo.get(DingDingAlarmFunctionEnum.TOCHENG_CPA_NOTICE.toString());
        dingDingRobotHookService.sendDingDingTextMessage(message, groupInfo);
    }

}
