package com.br.marketing.bridge.didiai;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 滴滴 AI 离线链路中，将清洗结果组装为内部网关推送所需 UploadDataDTO 的构建器。
 *
 * <p>背景：通用清洗服务返回的明细列表需要封装进 MarketingPreUserDTO，再序列化为 jsonData 字段，
 * 并配合 apiCode 组成上传 DTO，供 PushInfoService.pushUploadByRetry 方法使用。本类负责生成符合下游约定的
 * requestId 格式与批次 taskId。
 *
 * @author yueping.bai
 */
public final class DidiaiUploadDataBuilder {

    private DidiaiUploadDataBuilder() {}

    private static final DateTimeFormatter TASK_ID_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final AtomicLong LAST_UUID_MILLIS = new AtomicLong(-1L);
    private static final AtomicInteger UUID_SEQUENCE = new AtomicInteger(0);

    /**
     * 根据接口编号、业务侧任务标识与清洗后的用户明细列表构造上传数据对象。
     *
     * <p>按最新对端协议：标准上传顶层 taskId / requestId 由服务端生成；requestId 格式为 {@code {apiCode}_{taskId}}。
     *
     * @param apiCode 接口编号，与配置及上游约定一致
     * @param taskId  业务任务或请求标识；为空时由本方法按 {@code yyyyMMdd_{uuid}} 生成
     * @param details 清洗服务输出的用户明细列表，写入 dataItems
     * @return 已设置 apiCode 与 jsonData 的 UploadDataDTO，可直接用于重试推送
     */
    public static UploadDataDTO build(
            String apiCode, String taskId, List<MarketingPreUserDetailDTO> details) {
        String resolvedTaskId = StringUtils.isBlank(taskId) ? generateBatchTaskId() : taskId;
        String requestId = apiCode + "_" + resolvedTaskId;
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(resolvedTaskId);
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(details);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    /**
     * 生成标准上传批次的 taskId。
     *
     * <p>功能说明：
     * <ul>
     *   <li>当调用方未显式提供 taskId 时，按对端最新协议生成批次级 taskId。</li>
     *   <li>taskId 格式为 {@code yyyyMMdd_{uuid}}，其中 uuid 由毫秒级时间戳派生。</li>
     * </ul>
     *
     * <p>返回值说明：返回非空字符串，例如 {@code 20260506_23542345235443}。
     *
     * @return 生成的批次 taskId
     */
    private static String generateBatchTaskId() {
        String date = LocalDate.now().format(TASK_ID_DATE_FORMAT);
        return date + "_" + generateUuidFromMillis();
    }

    /**
     * 基于毫秒级时间戳生成唯一 id 字符串（供批次 taskId 的 uuid 部分使用）。
     *
     * <p>功能说明：
     * <ul>
     *   <li>以 {@link System#currentTimeMillis()} 作为主干，得到十进制字符串。</li>
     *   <li>同一毫秒并发生成时追加两位序列号（{@code 00-99}）以降低碰撞概率。</li>
     * </ul>
     *
     * <p>并发与唯一性说明：
     * <ul>
     *   <li>仅保证同 JVM 进程内并发调用的“实用唯一性”。</li>
     *   <li>若同一毫秒内生成次数超过 100，序列号回绕会带来碰撞风险；如需更强保证，应替换为 Snowflake 等全局唯一方案。</li>
     * </ul>
     *
     * @return 毫秒级唯一 id 字符串
     */
    private static String generateUuidFromMillis() {
        long now = System.currentTimeMillis();
        long last = LAST_UUID_MILLIS.getAndSet(now);
        if (last == now) {
            int seq = UUID_SEQUENCE.updateAndGet(v -> (v >= 99) ? 0 : (v + 1));
            return now + String.format("%02d", seq);
        }
        UUID_SEQUENCE.set(0);
        return String.valueOf(now);
    }
}
