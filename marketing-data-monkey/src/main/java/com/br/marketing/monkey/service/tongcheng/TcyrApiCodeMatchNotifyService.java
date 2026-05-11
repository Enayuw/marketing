package com.br.marketing.monkey.service.tongcheng;

import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.middleheaven.MiddleHeavenTcyrApiCodeMatchClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.monkey.config.LingxiaoTcyrProperties;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 扫描待匹配 batch，调用灵霄 {@code tcapiCodeMatchOutPut}。
 */
@Slf4j
@Service
public class TcyrApiCodeMatchNotifyService {

    private static final String TITLE = "【monkey】TcyrApiCodeMatchNotify";

    @Resource
    private MarketingTcyrSyncRecordMapper marketingTcyrSyncRecordMapper;

    @Resource
    private LingxiaoTcyrProperties lingxiaoTcyrProperties;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MiddleHeavenTcyrApiCodeMatchClient middleHeavenTcyrApiCodeMatchClient;

    public void dispatchPendingBatches() {
        if (!lingxiaoTcyrProperties.isEnabled()) {
            log.debug("TcyrApiCodeMatchNotifyService: lingxiao tcyr 未启用，跳过");
            return;
        }
        List<String> candidates = marketingCommonConfig.getTcyrMatchCandidateApiCodes();
        if (CollectionUtils.isEmpty(candidates)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "任务已启用但 Speed 中 tcyrMatchCandidateApiCodes 为空", TITLE));
            return;
        }
        int limit = Math.max(1, lingxiaoTcyrProperties.getBatchLimit());
        List<MarketingTcyrSyncRecord> rows;
        try {
            rows = marketingTcyrSyncRecordMapper.selectPendingApiCodeMatchRecords(limit);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    "查询待匹配 sync_record 异常: " + e.getMessage(), TITLE), e);
            return;
        }
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (MarketingTcyrSyncRecord row : rows) {
            if (row == null || StringUtils.isBlank(row.getBatchNo())) {
                continue;
            }
            try {
                long total = resolveTotal(row);
                String pushTime = row.getCreateTime() != null ? sdf.format(row.getCreateTime()) : "";
                boolean ok = middleHeavenTcyrApiCodeMatchClient.postTcApiCodeMatchOutPut(
                        lingxiaoTcyrProperties.getBaseUrl(),
                        lingxiaoTcyrProperties.getMatchOutPutPath(),
                        lingxiaoTcyrProperties.getBearerToken(),
                        lingxiaoTcyrProperties.getConnectTimeoutMs(),
                        lingxiaoTcyrProperties.getReadTimeoutMs(),
                        candidates,
                        row.getBatchNo(),
                        total,
                        pushTime);
                if (ok) {
                    log.warn("tcapiCodeMatchOutPut 已调用 batchNo={} total={}", row.getBatchNo(), total);
                }
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                        "单条 batch 处理异常 batchNo=" + row.getBatchNo() + " err=" + e.getMessage(), TITLE), e);
            }
        }
    }

    private static long resolveTotal(MarketingTcyrSyncRecord row) {
        if (StringUtils.isBlank(row.getData())) {
            return 0L;
        }
        try {
            JSONObject obj = JSONObject.parseObject(row.getData());
            if (obj == null) {
                return 0L;
            }
            Long t = obj.getLong("total");
            return t != null ? t : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}
