package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.datarelayservice.client.DidiaiResponseDTO;
import com.br.marketing.datarelayservice.constant.DidiaiConstants;
import com.br.marketing.datarelayservice.enums.DidiaiErrorCodeEnum;
import com.br.marketing.datarelayservice.service.DidiaiBizService;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 滴滴 AI 上传业务逻辑实现类。
 *
 * <p>主要职责包括：将解密后的明文解析为顶层 JSON 数组或带 dataItems 包装的对象；校验批量条数上限与单条必填字段；
 * 业务报文以需求约定字段为准（如 requestId、taskId、phone、properties.uid、properties.userType 等），不强制要求携带 bizLine；
 * 若调用方在批次外层或 properties 内可选传入 bizLine，则取值须为贷后或营销两种枚举之一；
 * 在指定分表创建表（若需要）并插入一条汇总行，整包明文存入 request_json_data 供离线任务再次消费；追溯依赖汇总表字段与 extend、应用日志，不使用独立审计物理表。
 *
 * <p>幂等性：当前实现不根据 requestId 做去重，同一 requestId 多次提交可能产生多条汇总记录，需由网络重试策略与
 * 对端约束共同保证业务语义。
 *
 * @author yueping.bai
 */
@Service
@Slf4j
public class DidiaiBizServiceImpl implements DidiaiBizService {

    @Resource
    private DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    /**
     * 消费单次上传对应的解密明文，完成解析、校验与持久化，行为与 DidiaiBizService 接口约定一致。
     *
     * <p>处理流程：先做明文空值与长度校验；再解析 JSON 得到记录数组及 dataItems 包装场景下可选的批次 bizLine；校验条数与逐行字段；
     * 持久化主表。
     */
    @Override
    public DidiaiResponseDTO ingest(
            String plaintext,
            String appKey,
            String clientIp,
            String effectiveApiCode,
            String drsTableSuffix) {
        DidiaiResponseDTO entryErr = validatePlaintextEntry(plaintext);
        if (entryErr != null) {
            return failValidate(entryErr, effectiveApiCode, drsTableSuffix, null);
        }
        ParseResult parsed = parsePlaintextRecords(plaintext);
        if (parsed.errorResponse != null) {
            return failValidate(parsed.errorResponse, effectiveApiCode, drsTableSuffix, null);
        }
        DidiaiResponseDTO batchErr = validateBatchAndRows(parsed.records, parsed.wrapBizLine);
        if (batchErr != null) {
            return failValidate(batchErr, effectiveApiCode, drsTableSuffix, parsed.records);
        }
        String apiCode = effectiveApiCode;
        String tCid = drsTableSuffix;
        String firstRequestId = parsed.records.getJSONObject(0).getString("requestId");
        DidiaiResponseDTO persistErr =
                persistCustomizeUploadRow(apiCode, tCid, firstRequestId, parsed, appKey);
        if (persistErr != null) {
            return persistErr;
        }
        return DidiaiResponseDTO.ok(extractRequestIds(parsed.records));
    }

    /**
     * 记录业务校验失败并返回错误响应。
     *
     * @param response 失败响应
     * @param apiCode  接口编号
     * @param tCid     分表后缀
     * @param records  已解析记录，可为 null
     * @return 原失败响应
     */
    private static DidiaiResponseDTO failValidate(
            DidiaiResponseDTO response, String apiCode, String tCid, JSONArray records) {
        if (records == null || records.isEmpty()) {
            log.warn(
                    "[DiDi-AI-API] 业务校验失败，errorCode={}，apiCode={}，tCid={}",
                    response.getErrorCode(),
                    apiCode,
                    tCid);
        } else {
            JSONObject first = records.getJSONObject(0);
            String requestId = first == null ? null : first.getString("requestId");
            log.warn(
                    "[DiDi-AI-API] 业务校验失败，errorCode={}，apiCode={}，tCid={}，batchSize={}，requestId={}",
                    response.getErrorCode(),
                    apiCode,
                    tCid,
                    records.size(),
                    requestId);
        }
        return response;
    }

    /**
     * 从解析后的 records 数组中提取 requestId 列表，保持与入参逐条对应顺序一致。
     *
     * @param records 解析得到的 JSON 数组
     * @return requestId 列表；若 records 为空则返回空列表
     */
    private static List<String> extractRequestIds(JSONArray records) {
        if (records == null || records.isEmpty()) {
            return new ArrayList<>(0);
        }
        List<String> ids = new ArrayList<>(records.size());
        for (int i = 0; i < records.size(); i++) {
            JSONObject row = records.getJSONObject(i);
            ids.add(row == null ? null : row.getString("requestId"));
        }
        return ids;
    }

    /**
     * 校验明文是否允许进入后续解析：禁止 null，且 UTF-8 字节长度不得超过配置上限以防恶意超大负载。
     *
     * @param plaintext 解密后的原始字符串
     * @return 若校验失败则返回封装好的失败响应；通过则返回 null
     */
    private static DidiaiResponseDTO validatePlaintextEntry(String plaintext) {
        if (plaintext == null) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.PLAINTEXT_EMPTY.getCode(),
                    DidiaiErrorCodeEnum.PLAINTEXT_EMPTY.getMessage());
        }
        if (plaintext.getBytes(StandardCharsets.UTF_8).length > DidiaiConstants.MAX_PLAINTEXT_BYTES) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.BODY_TOO_LARGE.getCode(),
                    DidiaiErrorCodeEnum.BODY_TOO_LARGE.getMessage());
        }
        return null;
    }

    /**
     * 校验批量记录数量是否在允许范围内，并对每一行执行业务字段校验。
     *
     * @param records     解析得到的 JSON 数组，元素为单条业务对象
     * @param wrapBizLine   dataItems 包装时外层可选的 bizLine，可为 null 或空白
     * @return 若校验失败则返回失败响应；全部通过返回 null
     */
    private static DidiaiResponseDTO validateBatchAndRows(
            JSONArray records, String wrapBizLine) {
        if (records == null || records.isEmpty()) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.BATCH_EMPTY.getCode(),
                    DidiaiErrorCodeEnum.BATCH_EMPTY.getMessage());
        }
        if (records.size() > DidiaiConstants.MAX_BATCH_SIZE) {
            return DidiaiResponseDTO.fail(
                    DidiaiErrorCodeEnum.BATCH_TOO_LARGE.getCode(),
                    DidiaiErrorCodeEnum.BATCH_TOO_LARGE.getMessage());
        }
        for (int i = 0; i < records.size(); i++) {
            JSONObject row = records.getJSONObject(i);
            String err = validateOneRecord(row, i, wrapBizLine);
            if (err != null) {
                DidiaiErrorCodeEnum code = resolveRowValidationErrorCode(err);
                return DidiaiResponseDTO.fail(code.getCode(), err);
            }
        }
        return null;
    }

    /**
     * 根据单条校验错误文案选择对应错误码：bizLine 非法、userType 缺失与其它字段缺失区分。
     *
     * @param err 由 {@link #validateOneRecord} 返回的中文错误短句
     * @return 与文案匹配的业务错误码枚举
     */
    private static DidiaiErrorCodeEnum resolveRowValidationErrorCode(String err) {
        if (err.contains("bizLine 非法")) {
            return DidiaiErrorCodeEnum.BIZ_LINE_INVALID;
        }
        if (err.contains("userType")) {
            return DidiaiErrorCodeEnum.USER_TYPE_MISSING;
        }
        return DidiaiErrorCodeEnum.RECORD_FIELD_MISSING;
    }

    /**
     * 在定制化上传分表中创建物理表（若底层实现需要），并插入一条汇总记录。
     *
     * @param apiCode        接口编号，写入汇总行
     * @param tCid           分表后缀片段
     * @param firstRequestId 批量中第一条的 requestId，作为汇总行主键语义上的代表
     * @param parsed         已解析的明文与记录集合
     * @param appKey         调用方应用标识
     * @return 成功时返回 null；任一步失败时返回错误响应且不应视为已落主表成功
     */
    private DidiaiResponseDTO persistCustomizeUploadRow(
            String apiCode,
            String tCid,
            String firstRequestId,
            ParseResult parsed,
            String appKey) {
        try {
            drsCustomizeUploadDataMapper.createDrsCustomizeUploadDataTable(tCid);
        } catch (Exception e) {
            return failPersist(
                    apiCode,
                    tCid,
                    parsed.records.size(),
                    firstRequestId,
                    DidiaiErrorCodeEnum.CREATE_DRS_TABLE_FAILED);
        }
        DrsCustomizeUploadData upload =
                buildUploadRow(
                        apiCode,
                        tCid,
                        firstRequestId,
                        parsed.trimmedPlaintext,
                        parsed.records.size(),
                        appKey,
                        parsed.wrapBizLine);
        try {
            int n = drsCustomizeUploadDataMapper.insertSelective(upload);
            if (n != 1) {
                return failPersist(
                        apiCode,
                        tCid,
                        parsed.records.size(),
                        firstRequestId,
                        DidiaiErrorCodeEnum.PERSIST_DRS_ROW_FAILED);
            }
        } catch (Exception e) {
            return failPersist(
                    apiCode,
                    tCid,
                    parsed.records.size(),
                    firstRequestId,
                    DidiaiErrorCodeEnum.PERSIST_DRS_ROW_FAILED);
        }
        log.warn(
                "[DiDi-AI-API] 汇总落库成功，apiCode={}，tCid={}，batchSize={}，requestId={}，drsId={}",
                apiCode,
                tCid,
                parsed.records.size(),
                firstRequestId,
                upload.getId());
        return null;
    }

    /**
     * 记录汇总落库失败并返回错误响应。
     *
     * @param apiCode   接口编号
     * @param tCid      分表后缀
     * @param batchSize 批次条数
     * @param requestId 首条 requestId
     * @param code      错误码枚举
     * @return 失败响应
     */
    private static DidiaiResponseDTO failPersist(
            String apiCode,
            String tCid,
            int batchSize,
            String requestId,
            DidiaiErrorCodeEnum code) {
        log.warn(
                "[DiDi-AI-API] 汇总落库失败，apiCode={}，tCid={}，batchSize={}，requestId={}，errorCode={}",
                apiCode,
                tCid,
                batchSize,
                requestId,
                code.getCode());
        return DidiaiResponseDTO.fail(code.getCode(), code.getMessage());
    }

    /**
     * 明文解析阶段的中间结果容器，用于在解析失败时携带错误响应，避免使用异常控制流。
     */
    private static final class ParseResult {
        /** 解析出的业务记录数组，解析失败时可为 null。 */
        final JSONArray records;
        /** 当 JSON 为带 dataItems 的对象时，外层可选扩展字段 bizLine 的取值。 */
        final String wrapBizLine;
        /** 经 trim 后的完整明文字符串，用于原样落库。 */
        final String trimmedPlaintext;
        /** 当 JSON 语法非法时非空，为可直接返回给客户端的失败响应。 */
        final DidiaiResponseDTO errorResponse;

        ParseResult(
                JSONArray records,
                String wrapBizLine,
                String trimmedPlaintext,
                DidiaiResponseDTO errorResponse) {
            this.records = records;
            this.wrapBizLine = wrapBizLine;
            this.trimmedPlaintext = trimmedPlaintext;
            this.errorResponse = errorResponse;
        }
    }

    /**
     * 将明文字符串解析为 Fastjson 结构：若以左方括号字符开头则按数组解析；否则按对象解析，若含 dataItems 键则取数组
     * 及外层可选 bizLine；否则将单个对象包装成长度为 1 的数组。
     *
     * @param plaintext 非空明文
     * @return 成功时 errorResponse 为 null；JSON 无法解析时 records 为 null 且 errorResponse 非空
     */
    private static ParseResult parsePlaintextRecords(String plaintext) {
        JSONArray records;
        String wrapBizLine = null;
        String trimmed = plaintext.trim();
        try {
            if (trimmed.startsWith("[")) {
                records = JSON.parseArray(trimmed);
            } else {
                JSONObject root = JSON.parseObject(trimmed);
                if (root.containsKey("dataItems")) {
                    records = root.getJSONArray("dataItems");
                    wrapBizLine = root.getString("bizLine");
                } else {
                    records = new JSONArray();
                    records.add(root);
                }
            }
        } catch (Exception e) {
            return new ParseResult(
                    null,
                    null,
                    null,
                    DidiaiResponseDTO.fail(
                            DidiaiErrorCodeEnum.JSON_INVALID.getCode(),
                            DidiaiErrorCodeEnum.JSON_INVALID.getMessage()));
        }
        return new ParseResult(records, wrapBizLine, trimmed, null);
    }

    /**
     * 构造待插入的定制化上传汇总实体，字段含义与表结构及下游离线任务约定一致。
     *
     * @param apiCode        接口编号
     * @param tCid           分表后缀
     * @param firstRequestId 首条明文行的 requestId（直接透传客户值）
     * @param trimmed        完整明文字符串
     * @param batchSize      本批次条数
     * @param appKey         应用标识，写入 extend JSON
     * @param wrapBizLine    批次侧可选 bizLine，写入 extend 便于排查
     * @return 已填充必要字段的实体，尚未执行数据库插入
     */
    private static DrsCustomizeUploadData buildUploadRow(
            String apiCode,
            String tCid,
            String firstRequestId,
            String trimmed,
            int batchSize,
            String appKey,
            String wrapBizLine) {
        DrsCustomizeUploadData upload = new DrsCustomizeUploadData();
        upload.setApiCode(apiCode);
        upload.setTCid(tCid);
        upload.setRequestId(firstRequestId);
        upload.setRequestJsonData(trimmed);
        upload.setBizDataNumber(batchSize);
        upload.setReceiveDate(LocalDate.now().toString());
        upload.setCreateTime(new Date());
        upload.setUpdateTime(new Date());
        upload.setResponseCode(String.valueOf(DidiaiErrorCodeEnum.SUCCESS.getCode()));
        upload.setResponseData(null);
        JSONObject ext = new JSONObject();
        ext.put("appKey", appKey);
        ext.put("batchSize", batchSize);
        ext.put("bizLineHint", wrapBizLine);
        upload.setExtend(ext.toJSONString());
        upload.setStatus(1);
        upload.setSyncStatus(0);
        upload.setFlattenStatus(0);
        return upload;
    }

    /**
     * 校验单条业务 JSON 是否包含约定必填字段；对可选的 bizLine 仅做取值合法性校验。
     *
     * <p>规则说明：每条必须含非空 requestId、可解析的 taskId、非空 phone、非空 properties 对象；properties 内须含非空
     * {@code userType}（映射百融 {@code reserveField1.userType}，不提供服务端常量兜底）。
     * bizLine 为可选扩展：
     * 若 dataItems 包装下外层提供了非空 wrapBizLine，则须为贷后或营销两种允许值之一；若未提供批次级 bizLine，则
     * properties 内 bizLine 可缺省，若填写则同样须为上述两种允许值之一。
     *
     * @param row         当前条目的 JSON 对象
     * @param index       从 0 开始的下标，用于生成人类可读的错误前缀
     * @param wrapBizLine dataItems 场景下外层可选 bizLine，可为空白
     * @return 校验失败时的中文错误短句；通过时返回 null
     */
    private static String validateOneRecord(JSONObject row, int index, String wrapBizLine) {
        if (row == null) {
            return "第 " + (index + 1) + " 条为空对象";
        }
        String requestId = row.getString("requestId");
        if (StringUtils.isBlank(requestId)) {
            return "第 " + (index + 1) + " 条缺少 requestId";
        }
        Long taskId = row.getLong("taskId");
        if (taskId == null) {
            return "第 " + (index + 1) + " 条缺少 taskId";
        }
        String phone = row.getString("phone");
        if (StringUtils.isBlank(phone)) {
            return "第 " + (index + 1) + " 条缺少 phone";
        }
        JSONObject props = row.getJSONObject("properties");
        if (props == null) {
            return "第 " + (index + 1) + " 条缺少 properties";
        }
        String userType = StringUtils.trimToEmpty(props.getString("userType"));
        if (StringUtils.isBlank(userType)) {
            return "第 " + (index + 1) + " 条缺少非空的 properties.userType";
        }
        if (StringUtils.isNotBlank(wrapBizLine)) {
            if (!DidiaiConstants.isAllowedBizLine(wrapBizLine)) {
                return "批次级 bizLine 非法，须为 "
                        + DidiaiConstants.BIZ_LINE_POST_LOAN
                        + " 或 "
                        + DidiaiConstants.BIZ_LINE_MARKETING;
            }
            return null;
        }
        String propsLine = props.getString("bizLine");
        if (StringUtils.isBlank(propsLine)) {
            return null;
        }
        if (!DidiaiConstants.isAllowedBizLine(propsLine)) {
            return "第 " + (index + 1) + " 条 properties.bizLine 非法，须为 POST_LOAN 或 MARKETING";
        }
        return null;
    }
}
