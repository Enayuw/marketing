package com.br.marketing.bridge.job.didiai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.bridge.didiai.DidiaiOfflinePreUserAssembler;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.constant.DidiaiFixedConfig;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.didiai.DidiaiPlaintextParser;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 滴滴 AI 定制化上传场景的离线补偿任务，由 Elastic-Job 定时触发。
 *
 * 功能说明：
 * - 从 didiaiApicodeToCidMap 配置中读取所有 apiCode 到 cid 的映射
 * - 遍历所有 cid 对应的分表（按 cid 去重，避免同一张表被多次扫描）
 * - 从汇总表读取整包明文，拆行后经 DidiaiOfflinePreUserAssembler.buildMarketingPreUserByCleaningMapping
 *   做清洗映射（不经规则引擎 commonClean）
 * - HTTP 调用营销标准上传入口写入前置表并回写 sync_status
 * - 每条记录使用其自身存储的 api_code 字段推送，确保与同步接入时一致
 *
 * @author yueping.bai
 */
@Component
@Slf4j
public class DidiaiSyncPushJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【滴滴AI-上传清洗推送任务】";

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Value("${api.marketing.uploadUrl:00}")
    private String marketingPreUserUploadUrl;

    /**
     * 调度框架回调的入口方法，每次触发时执行一轮完整的「分页扫表并逐行处理」。
     *
     * @param shardingContext Elastic-Job 传入的分片上下文
     */
    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        if (!DidiaiFixedConfig.OFFLINE_CLEAN_PUSH_JOB_ENABLED) {
            return;
        }
        try {
            log.warn(TITLE + "调度开始");
            runBatches();
            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), e.getMessage(), TITLE),
                    e);
        }
    }

    /**
     * 遍历所有配置的 cid，分页查询各分表中待处理的汇总记录并逐条处理。
     *
     * 处理逻辑：
     * - 从 didiaiApicodeToCidMap 配置中获取所有 cid
     * - 按 cid 去重，避免同一张表被多次扫描（多个 apiCode 可能映射到同一个 cid）
     * - 依次处理每张 b_drs_customize_upload_data_{cid} 表中 sync_status=0 的数据
     */
    private void runBatches() {
        Map<String, String> apicodeToCidMap = getApicodeToCidMap();
        if (apicodeToCidMap == null || apicodeToCidMap.isEmpty()) {
            log.warn(TITLE + "didiaiApicodeToCidMap 为空，跳过处理");
            return;
        }
        int pageSize = DidiaiFixedConfig.OFFLINE_JOB_PAGE_SIZE;
        Set<String> processedCids = new HashSet<>();
        for (String cid : apicodeToCidMap.values()) {
            if (processedCids.contains(cid)) {
                continue;
            }
            processedCids.add(cid);
            String tCid = "_" + cid;
            log.info(TITLE + "开始处理 cid={}, tCid={}", cid, tCid);
            runBatchesForOneCid(tCid, pageSize);
            log.info(TITLE + "完成处理 cid={}", cid);
        }
    }

    /**
     * 获取 apiCode 到 cid 的映射配置。
     *
     * @return apiCode 到 cid 的映射表；配置为空时返回空 Map
     */
    private Map<String, String> getApicodeToCidMap() {
        Map<String, String> map = marketingCommonConfig.getDidiaiApicodeToCidMap();
        return map != null ? map : Collections.emptyMap();
    }

    /**
     * 处理单个 cid 对应分表中的所有待处理数据。
     *
     * 处理逻辑：
     * - 分页查询 sync_status=0 的记录
     * - 每条记录使用其自身存储的 api_code 字段（而非配置遍历的 apiCode）
     * - 确保推送到 b_marketing_sync_info 时 apiCode 与同步接入时一致
     *
     * @param tCid     分表后缀，如 "_9356"、"_22106"
     * @param pageSize 分页大小
     */
    private void runBatchesForOneCid(String tCid, int pageSize) {
        Long minId = 0L;
        while (true) {
            List<DrsCustomizeUploadData> rows =
                    drsCustomizeUploadDataMapper.getDrsCustomizeUploadDataBySyncStatus(
                            tCid, 0, minId, pageSize);
            if (CollectionUtils.isEmpty(rows)) {
                break;
            }
            for (DrsCustomizeUploadData row : rows) {
                String apiCode = resolveApiCodeFromRow(row);
                processOneRow(tCid, apiCode, row);
            }
            minId = rows.get(rows.size() - 1).getId();
        }
    }

    /**
     * 从汇总表记录中解析 apiCode。
     *
     * 解析规则：
     * - 优先使用记录中存储的 api_code 字段
     * - 如果 api_code 为空，使用 DidiaiFixedConfig.UPLOAD_API_CODE 作为兜底
     *
     * @param row 汇总表当前行
     * @return 业务接口编号
     */
    private String resolveApiCodeFromRow(DrsCustomizeUploadData row) {
        String apiCode = row.getApiCode();
        if (StringUtils.isNotBlank(apiCode)) {
            return apiCode;
        }
        log.warn(TITLE + "记录 api_code 为空，使用默认值 id={}", row.getId());
        return DidiaiFixedConfig.UPLOAD_API_CODE;
    }

    /**
     * 处理汇总表中的一条记录：拆行 → 清洗映射 → HTTP 标准上传 → 更新同步状态。
     *
     * @param tCid    物理分表使用的后缀
     * @param apiCode 业务接口编号
     * @param row     当前行实体
     */
    private void processOneRow(String tCid, String apiCode, DrsCustomizeUploadData row) {
        List<Long> idList = Collections.singletonList(row.getId());
        try {
            List<JSONObject> jsonRows =
                    DidiaiPlaintextParser.toCleanInputRows(row.getRequestJsonData());
            if (jsonRows.isEmpty()) {
                drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, 2);
                log.warn(TITLE + "明文无有效记录 id={}", row.getId());
                return;
            }
            MarketingPreUserDTO preUser = buildMarketingPreUserFromPlainRows(apiCode, row, jsonRows);
            String jsonData = JSON.toJSONString(preUser);
            boolean ok = callMarketingPreUserSyncWithRetry(apiCode, jsonData, 3);
            if (ok) {
                drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, 1);
            } else {
                drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, 3);
                log.warn(TITLE + "远程入库失败 id={}", row.getId());
            }
        } catch (Exception e) {
            markFailSafe(tCid, idList, 4, e);
        }
    }

    /**
     * 将拆行后的明文列表经清洗映射组装为 MarketingPreUserDTO（不调用 commonClean）。
     *
     * @param apiCode  业务接口编号
     * @param row      汇总表当前行
     * @param jsonRows 明文行列表
     * @return 标准上传批次对象
     */
    private static MarketingPreUserDTO buildMarketingPreUserFromPlainRows(
            String apiCode, DrsCustomizeUploadData row, List<JSONObject> jsonRows) {
        return DidiaiOfflinePreUserAssembler.buildMarketingPreUserByCleaningMapping(
                apiCode, row, jsonRows);
    }

    /**
     * 远程调用营销标准上传入口并带重试能力。
     *
     * @param apiCode    业务接口编号
     * @param jsonData   标准上传批次 JSON
     * @param maxRetries 最大尝试次数（包含首次调用）
     * @return 调用成功返回 true，否则 false
     */
    private boolean callMarketingPreUserSyncWithRetry(String apiCode, String jsonData, int maxRetries) {
        if (StringUtils.isBlank(marketingPreUserUploadUrl)) {
            log.warn(TITLE + "未配置 api.marketing.uploadUrl，无法远程调用入库");
            return false;
        }
        if (maxRetries <= 0) {
            maxRetries = 1;
        }
        RestClientException lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                ApiNoDataResult<?> resp = callMarketingPreUserSync(apiCode, jsonData);
                if (resp != null && "00".equals(resp.getCode())) {
                    return true;
                }
                String respCode = resp == null ? "null" : resp.getCode();
                String respMsg = resp == null ? "null" : resp.getMessage();
                log.warn(
                        TITLE + "远程入库返回失败 attempt={}/{} apiCode={}, code={}, msg={}",
                        attempt,
                        maxRetries,
                        apiCode,
                        respCode,
                        respMsg);
            } catch (RestClientException e) {
                lastException = e;
                log.warn(
                        TITLE + "远程入库调用异常 attempt={}/{} apiCode={}, err={}",
                        attempt,
                        maxRetries,
                        apiCode,
                        e.getMessage());
            }
            if (attempt < maxRetries) {
                sleepQuietly(buildBackoffMillis(attempt));
            }
        }
        if (lastException != null) {
            log.warn(TITLE + "远程入库最终失败 apiCode={}, err={}", apiCode, lastException.getMessage());
        }
        return false;
    }

    /**
     * 调用营销标准上传 Web 接口一次。
     *
     * @param apiCode  业务接口编号
     * @param jsonData 标准上传批次 JSON
     * @return 标准接口返回对象，可能为 null
     * @throws RestClientException 当 HTTP 调用失败时抛出
     */
    private ApiNoDataResult<?> callMarketingPreUserSync(String apiCode, String jsonData) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("apiCode", apiCode);
        form.add("jsonData", jsonData);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
        return restTemplate.postForObject(marketingPreUserUploadUrl, entity, ApiNoDataResult.class);
    }

    /**
     * 根据重试次数计算退避等待时间。
     *
     * 退避策略：
     * - 第 1 次重试：100ms
     * - 第 2 次重试：300ms
     * - 第 3 次及以后：1000ms
     *
     * @param attempt 当前重试次数（从 1 开始）
     * @return 退避等待时间（毫秒）
     */
    private static long buildBackoffMillis(int attempt) {
        if (attempt <= 1) {
            return 100L;
        }
        if (attempt == 2) {
            return 300L;
        }
        return 1000L;
    }

    /**
     * 静默休眠指定时间，捕获中断异常并恢复中断状态。
     *
     * @param millis 休眠时间（毫秒）
     */
    private static void sleepQuietly(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 单行处理异常时更新同步状态并打日志。
     *
     * @param tCid   分表后缀
     * @param idList 主键列表
     * @param status 目标 sync_status 值
     * @param e      原始异常
     */
    private void markFailSafe(String tCid, List<Long> idList, int status, Exception e) {
        try {
            drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, status);
        } catch (Exception ex) {
            log.warn(TITLE + "更新 sync_status 失败: {}", ex.getMessage());
        }
        log.warn(TITLE + "处理异常 idList={}, e={}", idList, e.getMessage(), e);
    }
}
