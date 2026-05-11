package com.br.marketing.bridge.didiai;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.DrsCustomizeUploadData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 滴滴离线链路中，在「明文行列表」与「营销标准上传批次结构」之间做转换的纯静态工具。
 *
 * <p>按设计文档「清洗映射」口径：不经规则引擎 commonClean，由代码将 Drs 明文行映射为
 * MarketingPreUserDTO（手机号 UTF-8 MD5 小写 hex、reserveField1 等见 json-mapping-rule.md）。
 *
 * <p>顶层字段对齐设计 §34：标准上传顶层 taskId / requestId 由服务端生成，客户入参的 requestId
 * 逐条透传写入明细 custNum；接口 taskId 写入明细 reserveField1.taskIdDD。
 *
 * <p>reserveField1 组装对齐设计 §35：固定写入 userType、行级 taskId→taskIdDD、选填
 * strategyCode/productName；properties 其余键（含 userName、firstName 等）按原键名
 * 透传；taskIdDD 为保留键名，不接受 properties 覆盖。
 *
 * @author yueping.bai
 */
public final class DidiaiOfflinePreUserAssembler {

    private DidiaiOfflinePreUserAssembler() {}

    /** properties 中已由固定映射写入 reserveField1 的键，透传阶段跳过。 */
    private static final String PROP_USER_TYPE = "userType";

    private static final String PROP_STRATEGY_CODE = "strategyCode";
    private static final String PROP_PRODUCT_NAME = "productName";
    /** 仅允许来自行级 taskId，properties 内同名键不得覆盖。 */
    private static final String RESERVE_TASK_ID_DD = "taskIdDD";

    private static final DateTimeFormatter TASK_ID_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final AtomicLong LAST_UUID_MILLIS = new AtomicLong(-1L);
    private static final AtomicInteger UUID_SEQUENCE = new AtomicInteger(0);

    /**
     * 从滴滴明文行构造标准上传批次：逐行做清洗映射后填入 dataItems。
     *
     * <p>按 §34：顶层 taskId 服务端生成；顶层 requestId 服务端生成且格式为
     * apiCode_taskId；每行客户入参 requestId 透传至明细 custNum；每行接口 taskId
     * 写入 reserveField1.taskIdDD。
     *
     * @param apiCode 业务接口编号，用于生成顶层 requestId（格式 apiCode_taskId）
     * @param row     汇总表当前行（保留参数以兼容调用方；本节规则下不再依赖 row.requestId 生成顶层 requestId）
     * @param rows    非空明文行列表
     * @return 非空的批次对象，可序列化后走标准上传
     */
    public static MarketingPreUserDTO buildMarketingPreUserByCleaningMapping(
            String apiCode, DrsCustomizeUploadData row, List<JSONObject> rows) {
        String batchTaskId = generateBatchTaskId();
        String batchRequestId = generateBatchRequestId(apiCode, batchTaskId);
        List<MarketingPreUserDetailDTO> items = new ArrayList<>(rows.size());
        for (JSONObject r : rows) {
            items.add(mapPlainRowToDetailByCleaningMapping(r));
        }
        MarketingPreUserDTO dto = new MarketingPreUserDTO();
        dto.setTaskId(batchTaskId);
        dto.setRequestId(batchRequestId);
        dto.setDataItems(items);
        return dto;
    }

    /**
     * 生成本批次的标准上传 taskId。
     *
     * <p>功能说明：
     * <ul>
     *   <li>按对端最新协议生成批次级 taskId，用于营销标准上传的顶层字段。</li>
     *   <li>taskId 由「日期 + 下划线 + 毫秒级唯一 id」组成，格式为 yyyyMMdd_uuid。</li>
     * </ul>
     *
     * <p>生成规则：
     * <ul>
     *   <li>yyyyMMdd：取服务端当前日期（LocalDate.now），格式化为 BASIC_ISO_DATE。</li>
     *   <li>uuid：由 generateUuidFromMillis 基于毫秒时间戳生成，保证同进程内并发不碰撞。</li>
     * </ul>
     *
     * <p>返回值说明：返回非空字符串，例如 20260506_23542345235443。
     *
     * @return 本批次生成的 taskId
     */
    private static String generateBatchTaskId() {
        String date = LocalDate.now().format(TASK_ID_DATE_FORMAT);
        return date + "_" + generateUuidFromMillis();
    }

    /**
     * 生成本批次的标准上传 requestId：格式 apiCode_taskId。
     *
     * @param apiCode 业务接口编号
     * @param taskId  按 generateBatchTaskId 生成的 taskId
     * @return requestId
     */
    private static String generateBatchRequestId(String apiCode, String taskId) {
        if (StringUtils.isBlank(apiCode) || StringUtils.isBlank(taskId)) {
            throw new IllegalStateException(
                    "离线清洗映射失败：apiCode 或 taskId 为空，无法生成标准上传 requestId");
        }
        return apiCode.trim() + "_" + taskId;
    }

    /**
     * 基于毫秒级时间戳生成唯一 id 字符串（批次级 uuid 部分）。
     *
     * <p>功能说明：
     * <ul>
     *   <li>以 System.currentTimeMillis 作为主干，生成具备单调时间特征的字符串 id。</li>
     *   <li>在同一毫秒内可能存在并发生成的场景，通过追加两位序列号避免碰撞。</li>
     * </ul>
     *
     * <p>规则说明：
     * <ul>
     *   <li>当本次毫秒值 now 与上次生成毫秒值相同：在 now 后追加两位序列号（00-99）。</li>
     *   <li>当毫秒发生变化：直接返回 now 的十进制字符串，并重置序列号。</li>
     * </ul>
     *
     * <p>并发与唯一性约束：
     * <ul>
     *   <li>该方法仅保证<strong>同 JVM 进程</strong>内并发调用不碰撞。</li>
     *   <li>序列号上限为 99：若同一毫秒内超过 100 次生成请求，会发生回绕（实现上回到 00），可能存在碰撞风险；
     *       若该风险不可接受，应在后续实现中替换为更强的全局唯一方案（如 Snowflake）。</li>
     * </ul>
     *
     * <p>返回值说明：返回仅包含数字的字符串（可能带两位序列后缀），例如 23542345235443 或 2354234523544307。
     *
     * @return 毫秒级唯一 id 字符串
     */
    private static String generateUuidFromMillis() {
        long now = System.currentTimeMillis();
        long last = LAST_UUID_MILLIS.getAndSet(now);
        if (last == now) {
            int seq = UUID_SEQUENCE.updateAndGet(v -> (v >= 99 ? 0 : v + 1));
            return now + String.format("%02d", seq);
        }
        UUID_SEQUENCE.set(0);
        return String.valueOf(now);
    }

    /**
     * 将单条滴滴明文 JSON 清洗映射为一条营销标准明细。
     *
     * <p>映射规则对齐设计 §35：
     * <ul>
     *   <li>phone → UTF-8 MD5 小写 hex → cell</li>
     *   <li>接口字段 requestId（客户原始 requestId）→ custNum（§34）</li>
     *   <li>固定写入 reserveField1：properties.userType（必填）、行级 taskId→taskIdDD、
     *       选填 strategyCode/productName（非空才写入）</li>
     *   <li>properties 其余键按原键名、原值透传（含 userName、firstName、扩展话术变量等）；null 值跳过</li>
     *   <li>保留键 taskIdDD：仅来自行级 taskId，忽略 properties.taskIdDD</li>
     * </ul>
     */
    private static MarketingPreUserDetailDTO mapPlainRowToDetailByCleaningMapping(JSONObject r) {
        MarketingPreUserDetailDTO d = new MarketingPreUserDetailDTO();
        String phone = r.getString("phone");
        if (StringUtils.isNotBlank(phone)) {
            d.setCell(DigestUtils.md5DigestAsHex(phone.getBytes(StandardCharsets.UTF_8)));
        }
        String clientRequestId = StringUtils.trimToNull(r.getString("requestId"));
        if (clientRequestId == null) {
            throw new IllegalStateException(
                    "离线清洗映射失败：requestId 缺失或空白，无法透传至 custNum");
        }
        d.setCustNum(clientRequestId);
        JSONObject properties = r.getJSONObject("properties");
        d.setOperateType("3");
        String strategyCode = properties == null ? null : properties.getString(PROP_STRATEGY_CODE);
        String productName = properties == null ? null : properties.getString(PROP_PRODUCT_NAME);
        String userType =
                properties == null ? null : StringUtils.trimToNull(properties.getString(PROP_USER_TYPE));
        if (userType == null) {
            throw new IllegalStateException(
                    "离线清洗映射失败：properties.userType 缺失或空白，无法写入 reserveField1.userType");
        }
        JSONObject reserve = new JSONObject();
        reserve.put(PROP_USER_TYPE, userType);
        Object interfaceTaskId = r.get("taskId");
        if (interfaceTaskId != null) {
            reserve.put(RESERVE_TASK_ID_DD, String.valueOf(interfaceTaskId));
        }
        if (StringUtils.isNotBlank(strategyCode)) {
            reserve.put(PROP_STRATEGY_CODE, strategyCode);
        }
        if (StringUtils.isNotBlank(productName)) {
            reserve.put(PROP_PRODUCT_NAME, productName);
        }
        mergePropertiesPassthrough(reserve, properties);
        d.setReserveField1(reserve.toJSONString());
        return d;
    }

    /**
     * 将 properties 中未参与固定映射的键写入 reserve：键名不变；跳过 null；
     * 跳过已由固定映射处理的 userType/strategyCode/productName；跳过保留键 taskIdDD。
     *
     * @param reserve    已写入固定映射字段的目标对象
     * @param properties 单条明文行的 properties，可为 null
     */
    private static void mergePropertiesPassthrough(JSONObject reserve, JSONObject properties) {
        if (properties == null || properties.isEmpty()) {
            return;
        }
        for (String key : properties.keySet()) {
            if (isFixedMappingPassthroughSkip(key)) {
                continue;
            }
            if (RESERVE_TASK_ID_DD.equals(key)) {
                continue;
            }
            Object value = properties.get(key);
            if (value == null) {
                continue;
            }
            reserve.put(key, value);
        }
    }

    /**
     * 判断 properties 中的键是否已由固定映射写入 reserveField1，透传阶段应跳过。
     *
     * @param key properties 的键名
     * @return 为 true 时不做透传
     */
    private static boolean isFixedMappingPassthroughSkip(String key) {
        return PROP_USER_TYPE.equals(key)
                || PROP_STRATEGY_CODE.equals(key)
                || PROP_PRODUCT_NAME.equals(key);
    }
}
