package com.br.marketing.util;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.es.util.es.EsHandleUtil;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 跑分历史 ES 新索引判定：StraHisFile.createTime 晚于配置的上线时间则走新索引；
 * {@link #indexForModify} 供 RpcClientProxy.modify 等与 {@link #resolve} 使用同一套新旧索引名。
 */
@Slf4j
public final class EsNewIndexRuleUtils {

    private EsNewIndexRuleUtils() {
    }

    /**
     * 返回 boolean 值，用于 MarketingHistory.setUseNewIndexRule(Boolean)
     */
    public static boolean resolve(StraHisFile straHisFile, MarketingCommonConfig config) {
        if (straHisFile == null || straHisFile.getCreateTime() == null || config == null) {
            return false;
        }
        return resolveMillis(straHisFile.getCreateTime().getTime(), config.getEsNewIndexOnlineTime());
    }

    /**
     * 返回 boolean 值，用于 MarketingHistory.setUseNewIndexRule(Boolean)
     */
    public static boolean resolve(Long straHisFileCreateTimeMillis, MarketingCommonConfig config) {
        if (straHisFileCreateTimeMillis == null || config == null) {
            return false;
        }
        return resolveMillis(straHisFileCreateTimeMillis, config.getEsNewIndexOnlineTime());
    }

    /**
     * 返回 Map&lt;batchNumber, Boolean&gt;，用于 QueryBaseBean.setUseNewIndexRule(Map&lt;String,Boolean&gt;)
     * 遍历所有 StraHisFile，以 batchNumber 为 key，比较结果为 value。
     */
    public static Map<String, Boolean> resolveAsMap(List<StraHisFile> straHisFiles, MarketingCommonConfig config) {
        Map<String, Boolean> map = new HashMap<>();
        if (straHisFiles == null || config == null) {
            return map;
        }
        for (StraHisFile file : straHisFiles) {
            if (file != null && file.getBatchNumber() != null) {
                map.put(file.getBatchNumber(), resolve(file, config));
            }
        }
        return map;
    }

    /**
     * RpcClientProxy.modify 等写 ES 时的索引名：与 {@link #resolve(StraHisFile, MarketingCommonConfig)} 一致，
     * 新规则用 {@link EsHandleUtil#getDateFromBatchNumberNew}，否则 {@link EsHandleUtil#getDateFromBatchNumberLegacy}。
     *
     * @param batchNumber 跑分批次号
     * @param straHisFile 可为 null（按旧索引名）
     */
    public static String indexForModify(String batchNumber, StraHisFile straHisFile, MarketingCommonConfig config) {
        if (resolve(straHisFile, config)) {
            return EsHandleUtil.getDateFromBatchNumberNew(batchNumber);
        }
        return EsHandleUtil.getDateFromBatchNumberLegacy(batchNumber);
    }

    private static boolean resolveMillis(long createTimeMillis, String onlineTimeStr) {
        if (StringUtils.isBlank(onlineTimeStr)) {
            return false;
        }
        try {
            Date online = DateHelper.parseDate(onlineTimeStr.trim());
            return createTimeMillis > online.getTime();
        } catch (Exception e) {
            log.error("esNewIndexOnlineTime 解析失败，按旧索引: {}", onlineTimeStr, e);
            return false;
        }
    }
}
