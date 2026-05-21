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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
 * - 从汇总表读取整包明文，拆行后经离线映射组装为标准上传载荷（不经在线通用清洗）
 * - HTTP 调用营销标准上传入口写入前置表并回写 sync_status
 * - 每条记录使用其自身存储的 api_code 字段推送，确保与同步接入时一致
 *
 * 日志：统一使用前缀 [DiDi-AI-Job]；无待同步数据时仅输出调度首尾；有待处理行时输出条数、drsRequestId、uploadRequestId 等可观测字段，不输出完整 jsonData。
 *
 * @author yueping.bai
 */
@Component
@Slf4j
public class DidiaiSyncPushJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "[DiDi-AI-Job] ";
    private static final int PAYLOAD_SHA256_PREFIX_HEX_LEN = 16;
    private static final long ID_UNKNOWN_FOR_LOG = 0L;
    private static final int MAX_HTTP_ATTEMPTS = 3;

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Value("${api.marketing.uploadUrl:00}")
    private String marketingPreUserUploadUrl;

    /**
     * Elastic-Job 触发的主入口。
     *
     * 功能说明：
     * 读取批量补偿开关；若开关关闭则仅写一条跳过日志后直接返回。
     * 开关开启时顺序执行分页扫表与逐行上报，外层捕获未处理异常并告警。
     *
     * @param shardingContext Elastic-Job 下发的分片上下文，用于框架约定入参，本任务未读取分片字段。
     */
    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        if (!DidiaiFixedConfig.OFFLINE_CLEAN_PUSH_JOB_ENABLED) {
            log.warn(
                    TITLE
                            + "任务未执行：OFFLINE_CLEAN_PUSH_JOB_ENABLED=false，本调度周期跳过（与未调度的任务区分依赖此条日志）");
            return;
        }
        try {
            log.warn(TITLE + "调度开始");
            runBatches();
            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.error(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.SERVICEERROR_UNKNOWN.getCode(), e.getMessage(), TITLE),
                    e);
        }
    }

    /**
     * 按配置中所有不重复 cid 驱动多表离线补偿扫描。
     *
     * 功能说明：
     * 读取映射后逐 cid 调用分页处理；同一 cid 只处理一次以避免重复扫表。
     *
     * 处理逻辑：
     * 从 didiaiApicodeToCidMap 中取 cid，去重后对每张 b_drs_customize_upload_data 分表中
     * sync_status 为待处理的记录分页拉取并逐条处理。
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
            int processedRows = runBatchesForOneCid(cid, tCid, pageSize);
            if (processedRows > 0) {
                log.warn(
                        TITLE + "完成处理待同步数据，cid={}，tCid={}，processedRows={}",
                        cid,
                        tCid,
                        processedRows);
            }
        }
    }

    /**
     * 从公共配置读取滴滴 AI 业务编码与客户 cid 的映射关系。
     *
     * 功能说明：
     * 配置为空指针时转为空映射，避免调用方判空分支分散。
     *
     * @return apiCode 到 cid 字符串的映射表，永不为 null，可能为空表
     */
    private Map<String, String> getApicodeToCidMap() {
        Map<String, String> map = marketingCommonConfig.getDidiaiApicodeToCidMap();
        return map != null ? map : Collections.emptyMap();
    }

    /**
     * 在指定分表上按主键递增分页捞取待同步记录并逐行处理。
     *
     * 功能说明：
     * 使用 minId 游标翻页直到无数据；每页结束时输出各 sync_status 计数与首尾主键摘要。
     * api_code 为空的行单独打错误并回写异常状态，不进入单行处理管道。
     *
     * 处理逻辑：
     * 每页内对每行解析 api_code，调用单行处理并累计成功、跳过、远程失败、异常四类数量。
     *
     * @param cid      客户 cid，不含下划线前缀
     * @param tCid     物理分表后缀，含下划线前缀
     * @param pageSize 每页最大行数，来自离线任务分页配置
     * @return 本调度周期内进入 {@link #processOneRow} 的汇总行数
     */
    private int runBatchesForOneCid(String cid, String tCid, int pageSize) {
        Long minId = 0L;
        int processedRows = 0;
        boolean syncStarted = false;
        while (true) {
            List<DrsCustomizeUploadData> rows =
                    drsCustomizeUploadDataMapper.getDrsCustomizeUploadDataBySyncStatus(
                            tCid, 0, minId, pageSize);
            if (CollectionUtils.isEmpty(rows)) {
                break;
            }
            if (!syncStarted) {
                log.warn(TITLE + "开始处理待同步数据，cid={}，tCid={}", cid, tCid);
                syncStarted = true;
            }
            int success = 0;
            int skipEmpty = 0;
            int remoteFail = 0;
            int processException = 0;
            Long firstId = null;
            Long lastId = null;
            for (DrsCustomizeUploadData row : rows) {
                if (firstId == null) {
                    firstId = row.getId();
                }
                lastId = row.getId();
                String apiCode = resolveApiCodeFromRow(row);
                if (apiCode == null) {
                    processException++;
                    log.error(
                            TITLE
                                    + "记录 api_code 为空，无法处理，将回写 sync_status=4，id={}, tCid={}",
                            row.getId(),
                            tCid);
                    markFailSafe(
                            tCid,
                            Collections.singletonList(row.getId()),
                            4,
                            new IllegalStateException("api_code 为空"));
                    continue;
                }
                processedRows++;
                RowProcessOutcome out = processOneRow(tCid, apiCode, row);
                switch (out) {
                    case SUCCESS:
                        success++;
                        break;
                    case SKIP_EMPTY:
                        skipEmpty++;
                        break;
                    case REMOTE_FAIL:
                        remoteFail++;
                        break;
                    case EXCEPTION:
                        processException++;
                        break;
                }
            }
            log.warn(
                    TITLE
                            + "本页批处理汇总 tCid={} pageSize={} count={} sync_status=1:{} "
                            + "sync_status=2:{} sync_status=3:{} sync_status=4/异常:{} firstId={} lastId={}",
                    tCid,
                    pageSize,
                    rows.size(),
                    success,
                    skipEmpty,
                    remoteFail,
                    processException,
                    firstId,
                    lastId);
            minId = rows.get(rows.size() - 1).getId();
        }
        return processedRows;
    }

    /**
     * 单行汇总记录在处理完成后落入的业务状态枚举。
     *
     * 功能说明：
     * 统计各 sync_status 数量并驱动本页汇总日志；枚举值与数据库 sync_status 含义一致。
     */
    private enum RowProcessOutcome {
        /** 远程调用成功并已回写成功状态 */
        SUCCESS,
        /** 明文拆行后无有效数据，已回写跳过状态 */
        SKIP_EMPTY,
        /** 远程调用未得到业务成功码，已回写失败状态 */
        REMOTE_FAIL,
        /** 处理过程抛出异常，已走异常回写分支 */
        EXCEPTION
    }

    /**
     * 读取汇总表行上持久化的业务接口编码字段。
     *
     * 功能说明：
     * 仅信任行内存储的 api_code，与配置映射遍历顺序无关；空白则返回 null 交由上层标记异常。
     *
     * @param row 汇总表一行实体
     * @return 非空业务编码；字段缺失或空白时为 null
     */
    private String resolveApiCodeFromRow(DrsCustomizeUploadData row) {
        String apiCode = row.getApiCode();
        if (StringUtils.isNotBlank(apiCode)) {
            return apiCode;
        }
        return null;
    }

    /**
     * 处理汇总表单行：拆分明文、组装标准上传载荷、调用远程接口并按结果回写 sync_status。
     *
     * 功能说明：
     * 明文无有效行时回写跳过状态并返回跳过枚举。
     * 远程成功则回写成功状态并写成功日志；远程失败则回写失败状态并写错误日志。
     * 任意步骤抛出异常时调用异常回写并返回异常枚举。
     *
     * @param tCid    当前物理分表后缀，以下划线与数字组成
     * @param apiCode 该行存储的业务接口编码，用于表单与日志
     * @param row     汇总表一行数据，包含明文与主键
     * @return 单行终态枚举，供外层累计本页各类 sync_status 条数
     */
    private RowProcessOutcome processOneRow(String tCid, String apiCode, DrsCustomizeUploadData row) {
        List<Long> idList = Collections.singletonList(row.getId());
        long recordId = row.getId();
        String drsRequestId = row.getRequestId();
        try {
            List<JSONObject> jsonRows =
                    DidiaiPlaintextParser.toCleanInputRows(row.getRequestJsonData());
            if (jsonRows.isEmpty()) {
                drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, 2);
                log.warn(
                        TITLE
                                + "明文无有效行，回写 sync_status=2，id={} tCid={} apiCode={} drsRequestId={}",
                        recordId,
                        tCid,
                        apiCode,
                        drsRequestId);
                return RowProcessOutcome.SKIP_EMPTY;
            }
            int rowCount = jsonRows.size();
            MarketingPreUserDTO preUser = buildMarketingPreUserFromPlainRows(apiCode, jsonRows);
            String uploadRequestId = preUser.getRequestId();
            log.warn(
                    TITLE
                            + "数据组装完成，准备调用上传接口，drsId={}，tCid={}，apiCode={}，rowCount={}，drsRequestId={}，uploadRequestId={}",
                    recordId,
                    tCid,
                    apiCode,
                    rowCount,
                    drsRequestId,
                    uploadRequestId);
            String jsonData = JSON.toJSONString(preUser);
            boolean ok = callMarketingPreUserSyncWithRetry(
                    apiCode, jsonData, MAX_HTTP_ATTEMPTS, recordId, tCid);
            if (ok) {
                drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, 1);
                log.warn(
                        TITLE
                                + "回写 sync_status=1，id={} tCid={} apiCode={} drsRequestId={} uploadRequestId={} rowCount={} {}",
                        recordId,
                        tCid,
                        apiCode,
                        drsRequestId,
                        uploadRequestId,
                        rowCount,
                        describePayloadMeta(jsonData));
            } else {
                drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, 3);
                log.error(
                        TITLE
                                + "远程入库未成功，回写 sync_status=3，id={} tCid={} apiCode={} drsRequestId={} uploadRequestId={} {}",
                        recordId,
                        tCid,
                        apiCode,
                        drsRequestId,
                        uploadRequestId,
                        describePayloadMeta(jsonData));
            }
            return ok ? RowProcessOutcome.SUCCESS : RowProcessOutcome.REMOTE_FAIL;
        } catch (Exception e) {
            markFailSafe(tCid, idList, 4, e);
            return RowProcessOutcome.EXCEPTION;
        }
    }

    /**
     * 将拆行明文通过离线清洗映射组装为标准前置用户上传批次对象。
     *
     * 功能说明：
     * 通过离线映射组装逻辑完成字段映射，不经过在线通用清洗流程。
     *
     * @param apiCode  业务接口编号，决定映射策略
     * @param jsonRows 明文行列表，每项为一条待映射 JSON
     * @return 可供序列化并 POST 的营销前置用户 DTO
     */
    private static MarketingPreUserDTO buildMarketingPreUserFromPlainRows(
            String apiCode, List<JSONObject> jsonRows) {
        return DidiaiOfflinePreUserAssembler.buildMarketingPreUserByCleaningMapping(
                apiCode, jsonRows);
    }

    /**
     * 带重试地调用营销标准上传地址，并在每轮请求前后输出可检索日志。
     *
     * 功能说明：
     * 先校验上传地址配置；无效时打错误日志并返回 false。
     * 有效时构造载荷摘要后进入重试循环，不在此输出完整请求体正文。
     * 最大重试次数小于等于 0 时按单次请求处理。
     *
     * @param apiCode     业务接口编号，随表单提交
     * @param jsonData    序列化后的标准上传 JSON 字符串
     * @param maxRetries  最大尝试次数，包含第一次请求
     * @param recordId    当前汇总表主键，用于日志与问题定位
     * @param tCid        分表后缀，与汇总表路由一致
     * @return 任意一次调用得到业务码 00 时为 true，全部失败为 false
     */
    private boolean callMarketingPreUserSyncWithRetry(
            String apiCode,
            String jsonData,
            int maxRetries,
            long recordId,
            String tCid) {
        if (StringUtils.isBlank(marketingPreUserUploadUrl)) {
            log.error(
                    TITLE
                            + "未配置 api.marketing.uploadUrl，无法远程调用入库，id={} tCid={} apiCode={}",
                    recordId,
                    tCid,
                    apiCode);
            return false;
        }
        if (maxRetries <= 0) {
            maxRetries = 1;
        }
        String targetUrl = marketingPreUserUploadUrl;
        String payloadDesc = describePayloadMeta(jsonData);
        return executeHttpRetryLoop(
                apiCode, jsonData, maxRetries, recordId, tCid, targetUrl, payloadDesc);
    }

    /**
     * 执行多次 HTTP 尝试直至成功或次数用尽。
     *
     * 功能说明：
     * 每轮先记录请求开始信息，再发起调用并根据响应或异常写 INFO、WARN。
     * 业务码为 00 时立即返回 true；轮次未满且失败时按策略休眠后继续。
     * 全部失败后根据最后一次是否为客户端异常输出不同的 ERROR 摘要。
     *
     * @param apiCode     业务接口编号
     * @param jsonData    请求体中的 JSON 字符串，与表单字段一致
     * @param maxRetries  最大尝试次数
     * @param recordId    汇总表主键
     * @param tCid        分表后缀
     * @param targetUrl   完整请求地址，与配置项一致
     * @param payloadDesc 载荷长度与摘要串，避免在日志中重复计算
     * @return 任一轮业务成功为 true，否则 false
     */
    private boolean executeHttpRetryLoop(
            String apiCode,
            String jsonData,
            int maxRetries,
            long recordId,
            String tCid,
            String targetUrl,
            String payloadDesc) {
        RestClientException lastException = null;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            long startNanos = System.nanoTime();
            logHttpRequestStart(
                    targetUrl, attempt, maxRetries, recordId, tCid, apiCode, payloadDesc);
            try {
                ApiNoDataResult<?> resp = callMarketingPreUserSync(apiCode, jsonData);
                long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                if (resp != null && "00".equals(resp.getCode())) {
                    logHttpRequestSuccess(
                            costMs, attempt, recordId, tCid, apiCode, payloadDesc);
                    return true;
                }
                logHttpBusinessNotSuccess(
                        resp, costMs, attempt, maxRetries, recordId, tCid, apiCode, payloadDesc);
            } catch (RestClientException e) {
                lastException = e;
                long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
                logHttpClientException(
                        e, costMs, attempt, maxRetries, recordId, tCid, apiCode, payloadDesc);
            }
            if (attempt < maxRetries) {
                sleepQuietly(buildBackoffMillis(attempt));
            }
        }
        logHttpRetryExhausted(lastException, recordId, tCid, apiCode, payloadDesc);
        return false;
    }

    /**
     * 在发起单次 HTTP 调用前写入 INFO 级别日志。
     *
     * 功能说明：
     * 记录目标地址、当前为第几次尝试、总尝试次数、业务主键与载荷摘要，便于检索单条追踪。
     *
     * @param targetUrl   本次 POST 的目标 URL
     * @param attempt     当前尝试序号，从 1 开始
     * @param maxRetries  配置的最大尝试次数
     * @param recordId    汇总表主键
     * @param tCid        分表后缀
     * @param apiCode     业务接口编号
     * @param payloadDesc 载荷字节长度与哈希前缀描述，不含正文
     */
    private void logHttpRequestStart(
            String targetUrl,
            int attempt,
            int maxRetries,
            long recordId,
            String tCid,
            String apiCode,
            String payloadDesc) {
        log.warn(
                TITLE
                        + "HTTP 请求开始 url={} attempt={}/{} id={} tCid={} apiCode={} {}",
                targetUrl,
                attempt,
                maxRetries,
                recordId,
                tCid,
                apiCode,
                payloadDesc);
    }

    /**
     * 当接口返回业务码 00 时写入 INFO 级别成功日志。
     *
     * 功能说明：
     * 记录耗时与尝试序号，与请求开始日志成对出现，便于计算单次调用耗时。
     *
     * @param costMs      从发起调用到收到响应的耗时，单位毫秒
     * @param attempt     成功的尝试序号
     * @param recordId    汇总表主键
     * @param tCid        分表后缀
     * @param apiCode     业务接口编号
     * @param payloadDesc 载荷摘要描述
     */
    private void logHttpRequestSuccess(
            long costMs,
            int attempt,
            long recordId,
            String tCid,
            String apiCode,
            String payloadDesc) {
        log.warn(
                TITLE
                        + "HTTP 请求成功 businessCode=00 durationMs={} attempt={} id={} tCid={} apiCode={} {}",
                costMs,
                attempt,
                recordId,
                tCid,
                apiCode,
                payloadDesc);
    }

    /**
     * 当 HTTP 调用返回但业务码不是 00 时写入 WARN 级别日志。
     *
     * 功能说明：
     * 打印服务端返回的业务码与提示文案，标记为可重试或最终以 sync_status 落库的场景。
     *
     * @param resp        接口返回体，可为空
     * @param costMs      本轮耗时毫秒数
     * @param attempt     当前尝试序号
     * @param maxRetries  最大尝试次数
     * @param recordId    汇总表主键
     * @param tCid        分表后缀
     * @param apiCode     业务接口编号
     * @param payloadDesc 载荷摘要描述
     */
    private void logHttpBusinessNotSuccess(
            ApiNoDataResult<?> resp,
            long costMs,
            int attempt,
            int maxRetries,
            long recordId,
            String tCid,
            String apiCode,
            String payloadDesc) {
        String respCode = resp == null ? "null" : resp.getCode();
        String respMsg = resp == null ? "null" : resp.getMessage();
        log.warn(
                TITLE
                        + "HTTP 请求返回非成功 businessCode={} businessMsg={} durationMs={} attempt={}/{} id={} tCid={} apiCode={} {}（可重试或终态由上层回写）",
                respCode,
                respMsg,
                costMs,
                attempt,
                maxRetries,
                recordId,
                tCid,
                apiCode,
                payloadDesc);
    }

    /**
     * 当 RestTemplate 抛出客户端异常时写入 WARN 级别日志。
     *
     * 功能说明：
     * 记录网络或服务端不可用等可恢复错误，供结合退避重试策略排查。
     *
     * @param e           RestTemplate 抛出的异常
     * @param costMs      本轮耗时毫秒数
     * @param attempt     当前尝试序号
     * @param maxRetries  最大尝试次数
     * @param recordId    汇总表主键
     * @param tCid        分表后缀
     * @param apiCode     业务接口编号
     * @param payloadDesc 载荷摘要描述
     */
    private void logHttpClientException(
            RestClientException e,
            long costMs,
            int attempt,
            int maxRetries,
            long recordId,
            String tCid,
            String apiCode,
            String payloadDesc) {
        log.warn(
                TITLE
                        + "HTTP 请求异常 durationMs={} attempt={}/{} id={} tCid={} apiCode={} err={} {}",
                costMs,
                attempt,
                maxRetries,
                recordId,
                tCid,
                apiCode,
                e.getMessage(),
                payloadDesc);
    }

    /**
     * 在所有尝试仍失败时写入 ERROR 级别终态日志。
     *
     * 功能说明：
     * 若存在最后一次 RestClientException 则打出异常摘要；否则说明多次均为业务码失败。
     *
     * @param lastException 最后一次 HTTP 客户端异常，无则为 null
     * @param recordId      汇总表主键
     * @param tCid          分表后缀
     * @param apiCode       业务接口编号
     * @param payloadDesc   载荷摘要描述
     */
    private void logHttpRetryExhausted(
            RestClientException lastException,
            long recordId,
            String tCid,
            String apiCode,
            String payloadDesc) {
        if (lastException != null) {
            log.error(
                    TITLE
                            + "HTTP 请求在重试耗尽后仍失败 id={} tCid={} apiCode={} err={} {}",
                    recordId,
                    tCid,
                    apiCode,
                    lastException.getMessage(),
                    payloadDesc);
        } else {
            log.error(
                    TITLE
                            + "HTTP 请求在重试耗尽后仍因业务码失败 id={} tCid={} apiCode={} {}",
                    recordId,
                    tCid,
                    apiCode,
                    payloadDesc);
        }
    }

    /**
     * 使用表单编码方式向营销上传地址发起单次 POST。
     *
     * 功能说明：
     * 将 apiCode 与 jsonData 放入 application/x-www-form-urlencoded 表单并解析为统一结果类型。
     * 本方法不写业务日志，由外层重试与摘要方法负责观测。
     *
     * @param apiCode  表单字段 apiCode
     * @param jsonData 表单字段 jsonData，为整包 JSON 字符串
     * @return 解析后的 ApiNoDataResult，网络异常时由 RestTemplate 抛出而不返回
     * @throws RestClientException HTTP 层错误时抛出
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
     * 按当前尝试序号计算两次 HTTP 调用之间的休眠毫秒数。
     *
     * 功能说明：
     * 首次重试间隔较短，后续拉长以降低对下游的瞬间压力。
     *
     * 退避策略：
     * 序号 1 对应 100ms，序号 2 对应 300ms，更大序号对应 1000ms。
     *
     * @param attempt 当前已完成请求后的尝试序号，最小为 1
     * @return 建议在下次请求前休眠的毫秒数
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
     * 在当前线程休眠指定毫秒，吞掉中断并重新设置中断标记。
     *
     * 功能说明：
     * 用于重试退避等待；中断时不向外抛出，仅保留线程中断状态供上层感知。
     *
     * @param millis 休眠时长，单位毫秒，非负
     */
    private static void sleepQuietly(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 将待 POST 的 JSON 正文转换为可安全写入日志的简短描述串。
     *
     * 功能说明：
     * 输出 UTF-8 字节长度以及 SHA-256 摘要的前若干位十六进制字符，不回显正文。
     *
     * @param jsonData 序列化后的上传 JSON，允许为 null
     * @return 形如 jsonBytes 与 sha256prefix 拼接的固定格式字符串，便于检索与比对
     */
    private static String describePayloadMeta(String jsonData) {
        if (jsonData == null) {
            return "jsonBytes=0,sha256prefix=";
        }
        byte[] raw = jsonData.getBytes(StandardCharsets.UTF_8);
        return "jsonBytes=" + raw.length + ",sha256prefix=" + sha256FirstHexPrefix(raw);
    }

    /**
     * 计算字节数组 SHA-256 摘要并截取前缀十六进制字符串。
     *
     * 功能说明：
     * 前缀长度由类常量控制，用于日志中标识同一载荷而不暴露完整内容。
     * 算法在当前运行环境不可用时返回固定占位串。
     *
     * @param data 原文 UTF-8 字节，空数组时返回空字符串
     * @return 小写十六进制前缀，长度不超过类常量规定位数
     */
    private static String sha256FirstHexPrefix(byte[] data) {
        if (data == null || data.length == 0) {
            return "";
        }
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            int nibbleLen = PAYLOAD_SHA256_PREFIX_HEX_LEN;
            int byteLen = (nibbleLen + 1) / 2;
            StringBuilder sb = new StringBuilder(nibbleLen);
            for (int i = 0; i < byteLen && i < hash.length; i++) {
                sb.append(String.format("%02x", hash[i]));
            }
            if (sb.length() > nibbleLen) {
                return sb.substring(0, nibbleLen);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return "digest_error";
        }
    }

    /**
     * 单行处理失败时尽力回写 sync_status，并输出错误与数据库异常日志。
     *
     * 功能说明：
     * 先尝试按主键列表更新为目标状态；更新失败时单独打错误日志且不掩藏原异常。
     * 随后始终记录行级错误信息，主键列表为空时在日志中使用占位主键值。
     *
     * @param tCid   分表后缀
     * @param idList 待更新的主键列表，通常仅含一个 id
     * @param status 目标 sync_status
     * @param e      捕获到的业务或系统异常，用于栈与消息输出
     */
    private void markFailSafe(String tCid, List<Long> idList, int status, Exception e) {
        long id0 = idList == null || idList.isEmpty() ? ID_UNKNOWN_FOR_LOG : idList.get(0);
        try {
            drsCustomizeUploadDataMapper.updateSyncStatusByIds(tCid, idList, status);
        } catch (Exception ex) {
            log.error(
                    TITLE
                            + "回写 sync_status 失败 tCid={} idList={} targetStatus={} err={}",
                    tCid,
                    idList,
                    status,
                    ex.getMessage());
        }
        log.error(
                TITLE + "行处理异常，将回写 sync_status={} id={} tCid={} err={}", status, id0, tCid, e.getMessage(), e);
    }
}
