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
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 滴滴 AI 定制化上传场景的离线补偿任务，由 Elastic-Job 定时触发。
 *
 * <p>从汇总表读取整包明文，拆行后经 {@link DidiaiOfflinePreUserAssembler#buildMarketingPreUserByCleaningMapping}
 * 做清洗映射（不经规则引擎 {@code commonClean}），再 HTTP 调用营销标准上传入口写入前置表并回写 {@code sync_status}。
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

    @Value("${api.marketing.uploadUrl:00}")
    private String marketingPreUserUploadUrl;
//    private String marketingPreUserUploadUrl = "http://localhost:18704/marketingUserPre/receiveMarketingPreUser";

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
     * 分页查询待处理汇总记录并逐条处理。
     */
    private void runBatches() {
        String tCid = DidiaiFixedConfig.UPLOAD_TCID;
        String apiCode = DidiaiFixedConfig.UPLOAD_API_CODE;
        int pageSize = DidiaiFixedConfig.OFFLINE_JOB_PAGE_SIZE;
        Long minId = 0L;
        while (true) {
            List<DrsCustomizeUploadData> rows =
                    drsCustomizeUploadDataMapper.getDrsCustomizeUploadDataBySyncStatus(
                            tCid, 0, minId, pageSize);
            if (CollectionUtils.isEmpty(rows)) {
                break;
            }
            for (DrsCustomizeUploadData row : rows) {
                processOneRow(tCid, apiCode, row);
            }
            minId = rows.get(rows.size() - 1).getId();
        }
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
     * 将拆行后的明文列表经清洗映射组装为 {@link MarketingPreUserDTO}（不调用 {@code commonClean}）。
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

    private static long buildBackoffMillis(int attempt) {
        if (attempt <= 1) {
            return 100L;
        }
        if (attempt == 2) {
            return 300L;
        }
        return 1000L;
    }

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
     * @param status 目标 {@code sync_status}
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
