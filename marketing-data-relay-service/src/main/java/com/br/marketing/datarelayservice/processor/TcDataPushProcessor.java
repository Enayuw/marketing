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
        MarketingTcyrSyncRecord record = new MarketingTcyrSyncRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrSyncRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrSyncRecord record = new MarketingTcyrSyncRecord();
        String scene = resolveSceneForRecord(batchNo);
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setScene(scene);
        record.setData(tcRequestDTO.getData());
        record.setStatus(0);
        record.setDownStatus(0);
        record.setIsDel(1);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try {
            tcyrSyncRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警 todo
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(2);
            tcyrSyncRecordMapper.insertSelective(record);
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
     * 推送钉钉告警
     */
    private void notice(String message) {
        Map<String, JSONObject> webHookInfo = marketingCommonConfig.getDingDingWebHookInfo();
        Map<String, Object> groupInfo = webHookInfo.get(DingDingAlarmFunctionEnum.TOCHENG_CPA_NOTICE.toString());
        dingDingRobotHookService.sendDingDingTextMessage(message, groupInfo);
    }

}
