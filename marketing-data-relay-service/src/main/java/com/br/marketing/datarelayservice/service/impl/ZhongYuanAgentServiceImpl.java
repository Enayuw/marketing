package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.datarelayservice.enums.ZhongYuanAgentMtResponseCode;
import com.br.marketing.datarelayservice.service.ZhongYuanAgentService;
import com.br.marketing.dto.dataclean.mq.MqDataJsonParse;
import com.br.marketing.dto.zhongyuan.MtStandardRequest;
import com.br.marketing.dto.zhongyuan.MtStandardResponse;
import com.br.marketing.dto.zhongyuan.ZhongYuanAgentChannelRsaConfig;
import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.entity.ZhongYuanAgent;
import com.br.marketing.enums.clean.DataProcessEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.aes.AesZhongYuan;
import com.br.marketing.utils.Encodes;
import com.br.marketing.utils.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;


@Service
@Slf4j
public class ZhongYuanAgentServiceImpl implements ZhongYuanAgentService {

    private static final String RSA_PADDING = "RSA/ECB/PKCS1Padding";
    private static final String AES_PADDING = AesZhongYuan.ECB_ALGORITHM_PADDING;
    private static final SecureRandom REQUEST_ID_RANDOM = new SecureRandom();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private ZhongYuanAgentImportPersistService zhongYuanAgentImportPersistService;
    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Override
    public MtStandardResponse importAgentCustomer(String jsonData, HttpServletRequest request) {
        String responseNo = "";
        try {
            log.warn("中原坐席批量导入，body长度={}", jsonData == null ? 0 : jsonData.length());
            if (!StringUtils.hasText(jsonData)) {
                return fail(ZhongYuanAgentMtResponseCode.ERR_PARAM, "请求体为空", responseNo);
            }

            ZhongYuanAgentChannelRsaConfig rsaCfg = ZhongYuanAgentChannelRsaConfig.fromConfigJson(
                    marketingCommonConfig.getZhongYuanAgentChannelRsa());
            if (rsaCfg == null || !StringUtils.hasText(rsaCfg.getPublicKey()) || !StringUtils.hasText(rsaCfg.getPrivateKey())) {
                return fail(ZhongYuanAgentMtResponseCode.ERR_CONFIG, "未配置 zhongYuanAgentChannelRsa 或缺少 publicKey/privateKey", responseNo);
            }

            String publicKey = rsaCfg.getPublicKey().trim().replace("*","=");
            String privateKey = rsaCfg.getPrivateKey().trim().replace("*","=");

            PublicKey zyPublicKey = RsaUtil.getPublicKey(publicKey);
            PrivateKey channelPrivateKey = RsaUtil.getPrivateKey(privateKey);

            MtStandardRequest req = JSON.parseObject(jsonData, MtStandardRequest.class);
            if (req == null || !StringUtils.hasText(req.getRequestNo())) {
                return fail(ZhongYuanAgentMtResponseCode.ERR_PARAM, "请求解析失败或缺少requestNo", responseNo);
            }
            responseNo = req.getRequestNo();

            if (!StringUtils.hasText(req.getKey()) || !StringUtils.hasText(req.getSign()) || !StringUtils.hasText(req.getRequestData())) {
                return fail(ZhongYuanAgentMtResponseCode.ERR_PARAM, "key/sign/requestData 不能为空", responseNo);
            }

            if (!verifySign(req.signData(), req.getSign(), zyPublicKey)) {
                log.warn("中原坐席批量导入验签失败, requestNo={}", req.getRequestNo());
                return fail(ZhongYuanAgentMtResponseCode.ERR_SIGN, responseNo);
            }

            String plainRequestData;
            try {
                byte[] requestAesKey = RsaUtil.decryptBase64Content2Byte(req.getKey(), channelPrivateKey);
                plainRequestData = AesZhongYuan.decryptBase64Content2String(req.getRequestData(), requestAesKey, null, AES_PADDING);
            } catch (Exception ex) {
                log.warn("中原坐席批量导入解密失败, requestNo={}", req.getRequestNo(), ex);
                return fail(ZhongYuanAgentMtResponseCode.ERR_DECRYPT, responseNo);
            }

            String testApiCode = request.getHeader("Test-ApiCode");
            String apiCode = testApiCode != null ? testApiCode : rsaCfg.getApiCode();
            if (!StringUtils.hasText(apiCode)) {
                return fail(ZhongYuanAgentMtResponseCode.ERR_CONFIG, "未配置 apiCode（请求头 Test-ApiCode 或 zhongYuanAgentChannelRsa.apiCode）", responseNo);
            }

            JSONObject inner = parseInner(plainRequestData);
            JSONArray details = inner == null ? new JSONArray() : inner.getJSONArray("details");
            if (details == null && inner != null && inner.containsKey("list")) {
                details = inner.getJSONArray("list");
            }
            if (details == null) {
                details = new JSONArray();
            }
            String templateId = inner == null ? null : inner.getString("templateId");

            Date now = new Date();
            ZhongYuanAgent agent = new ZhongYuanAgent();
            agent.setApiCode(apiCode);
            agent.setRequestNo(req.getRequestNo());
            agent.setTimestamp(req.getTimestamp());
            agent.setSign(req.getSign());
            agent.setKeyAes(req.getKey());
            agent.setTemplateId(templateId);
            agent.setDetails(plainRequestData);
            agent.setCreateTime(now);
            agent.setUpdateTime(now);
            boolean emptyDetails = details.isEmpty();
            String requestId = buildRequestId(apiCode);
            int dataItemCount = emptyDetails ? 1 : details.size();

            String batchNo = UUID.randomUUID().toString();
            String jsonDataWithBatchNo = mergeBatchNoIntoPlainJson(plainRequestData,
                    batchNo, req.getRequestNo());
            MarketingCustomerOriginalData original = new MarketingCustomerOriginalData();
            original.setApiCode(apiCode);
            original.setRequestId(requestId);
            original.setJsonData(jsonDataWithBatchNo);
            original.setActualNum(dataItemCount);
            original.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            original.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
            original.setReceiveDate(LocalDate.now().toString());
            original.setStatus(1);
            original.setCleanStatus(0);
            original.setCreateTime(now);
            original.setUpdateTime(now);
            Long originalDataId = zhongYuanAgentImportPersistService.insertAgentAndOriginal(agent, original);
            sendCustomerOriginalDataJsonParseMq(apiCode, originalDataId);

            // 成功时 responseData 明文仅含合作方批次号 batchNo（与落库 jsonData 中 batchNo 一致）
            String bizJson = JSON.toJSONString(Collections.singletonMap("batchNo", batchNo));

            MtStandardResponse resp = new MtStandardResponse();
            resp.setErrorCode(ZhongYuanAgentMtResponseCode.SUCCESS.getCode());
            resp.setErrorMsg(ZhongYuanAgentMtResponseCode.SUCCESS.getDefaultMessage());
            resp.setResponseNo(req.getRequestNo());

            byte[] responseAesKey = AesZhongYuan.generateAesKey();
            String responseKey = RsaUtil.encrypt2Base64String(responseAesKey, zyPublicKey, null, RSA_PADDING);
            resp.setKey(responseKey);
            resp.setSign(sign(resp.signData(), channelPrivateKey));
            String encryptedResponseData = AesZhongYuan.encrypt2Base64String(bizJson, responseAesKey, null, AES_PADDING);
            resp.setResponseData(encryptedResponseData);
            return resp;
        } catch (Exception e) {
            log.error("中原坐席批量导入异常", e);
            return fail(ZhongYuanAgentMtResponseCode.ERR_SYSTEM, responseNo);
        }
    }

    private static MtStandardResponse fail(ZhongYuanAgentMtResponseCode code, String responseNo) {
        return fail(code, code.getDefaultMessage(), responseNo);
    }

    private static MtStandardResponse fail(ZhongYuanAgentMtResponseCode code, String message, String responseNo) {
        MtStandardResponse r = new MtStandardResponse();
        r.setErrorCode(code.getCode());
        r.setErrorMsg(message);
        r.setResponseNo(responseNo == null ? "" : responseNo);
        return r;
    }

    private JSONObject parseInner(String plainRequestData) {
        if (!StringUtils.hasText(plainRequestData)) {
            return null;
        }
        String t = plainRequestData.trim();
        if (t.startsWith("[")) {
            JSONObject wrap = new JSONObject();
            wrap.put("details", JSON.parseArray(t));
            return wrap;
        }
        return JSON.parseObject(plainRequestData);
    }

    /**
     * 在原始明文 JSON 根上增加 {@code batchNo}、入参 {@code requestNo}（与二者同级）；根为数组时包一层 {@code details} 再写上述字段，与 {@link #parseInner} 语义一致。
     */
    private static String mergeBatchNoIntoPlainJson(String plainRequestData, String batchNo, String requestNo) {
        if (!StringUtils.hasText(plainRequestData)) {
            JSONObject root = new JSONObject();
            root.put("batchNo", batchNo);
            root.put("requestNo", requestNo);
            return root.toJSONString();
        }
        String t = plainRequestData.trim();
        if (t.startsWith("[")) {
            JSONObject wrap = new JSONObject();
            wrap.put("details", JSON.parseArray(t));
            wrap.put("batchNo", batchNo);
            wrap.put("requestNo", requestNo);
            wrap.put("operateType", "3");
            addDefaultUserTypeOnDetails(wrap);
            return wrap.toJSONString();
        }
        JSONObject obj = JSON.parseObject(plainRequestData);
        obj.put("batchNo", batchNo);
        obj.put("requestNo", requestNo);
        obj.put("operateType", "3");
        addDefaultUserTypeOnDetails(obj);
        return obj.toJSONString();
    }

    /**
     * 在根对象的 {@code details} 数组中，每条与 {@code jobId} 同级补充 {@code userType}，缺省或空串时为 {@code "1"}。
     */
    private static void addDefaultUserTypeOnDetails(JSONObject root) {
        if (root == null) {
            return;
        }
        JSONArray details = root.getJSONArray("details");
        if (details == null || details.isEmpty()) {
            return;
        }
        for (int i = 0; i < details.size(); i++) {
            Object el = details.get(i);
            if (!(el instanceof JSONObject row)) {
                continue;
            }
            Object ut = row.get("userType");
            if (ut == null || (ut instanceof String && !StringUtils.hasText((String) ut))) {
                row.put("userType", "1");
            }
        }
    }

    private String buildRequestId(String apiCode) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random5Digits = String.format("%05d", REQUEST_ID_RANDOM.nextInt(100000));
        return dateStr + "_" + apiCode + "_" + random5Digits + System.currentTimeMillis();
    }

    /**
     * 与 marketing-data-bridge 中 {@code ZhongYuanUploadDataJob.sendMqMessage} 一致；须在
     * {@link ZhongYuanAgentImportPersistService#insertAgentAndOriginal} 返回之后调用（此时落库事务已提交）。
     */
    private void sendCustomerOriginalDataJsonParseMq(String apiCode, Long dataId) {
        try {
            MqDataJsonParse mqDataJsonParse = new MqDataJsonParse();
            mqDataJsonParse.setDataId(dataId);
            mqDataJsonParse.setSystemType(DataProcessEnum.SystemTypeEnum.MARKETING.getCode());
            mqDataJsonParse.setDataType(DataProcessEnum.DataTypeEnum.UPLOAD.getCode());
            mqDataJsonParse.setAcceptType(DataProcessEnum.AcceptTypeEnum.CUSTOM.getCode());
            rocketMqSwitch.sendMessage(apiCode, MarketingAssistConstants.TOPIC,
                    MarketingAssistConstants.TAG_MARKETING_CUSTOMER_DATA_JSON_PARSE,
                    JSON.toJSONString(mqDataJsonParse),
                    MQConstants.ROUTING_KEY_MARKETING_CUSTOMER_DATA_JSON_PARSE);
            log.warn("中原坐席批量导入已发送原始数据解析MQ, apiCode={}, dataId={}, mq={}", apiCode, dataId, JSON.toJSONString(mqDataJsonParse));
        } catch (Exception e) {
            log.error("中原坐席批量导入发送原始数据解析MQ失败, apiCode={}, dataId={}", apiCode, dataId, e);
        }
    }

    public static boolean verifySign(String signData, String sign, PublicKey publicKey) throws Exception {
        Signature verifySign = Signature.getInstance("SHA1WithRSA");
        verifySign.initVerify(publicKey);
        verifySign.update(signData.getBytes(StandardCharsets.UTF_8));
        return verifySign.verify(Encodes.decodeBase64(sign));
    }

    public static String sign(String signData, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privateKey);
        signature.update(signData.getBytes(StandardCharsets.UTF_8));
        return Encodes.encodeBase64(signature.sign());
    }
}
