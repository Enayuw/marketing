package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.service.XyfCustomizeService;
import com.br.marketing.dto.xyf.XyfEncryptionDTO;
import com.br.marketing.entity.XyfSubmitRecord;
import com.br.marketing.enums.XyfResultEnum;
import com.br.marketing.enums.XyfSyncStatusEnum;
import com.br.marketing.mapper.XyfSubmitRecordMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.xyf.AESUtils;
import com.br.marketing.util.xyf.RSAUtils;
import com.br.marketing.util.xyf.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.stream.Stream;

/**
 * XYF 数据中转：解密落库并返回加密响应
 */
@Slf4j
@Service
public class XyfCustomizeServiceImpl implements XyfCustomizeService {

    private final static String TITLE = "【信用飞:外呼批量提交接口】";

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XyfSubmitRecordMapper xyfSubmitRecordMapper;

    @Override
    public XyfEncryptionDTO batchSubmit(XyfEncryptionDTO requestDTO, String apiCode) {
        apiCode = StringUtils.isNotBlank(apiCode) ? apiCode : fetchApiCode();
        log.warn("{}上传数据，data:{}", TITLE, requestDTO);
        String[] keys = getXyfEncryptionKeys();
        String brPrivateKey = keys[0];
        String xyfPublicKey = keys[1];
        try {
            //1.解密aes密钥
            String plainAesKey = RSAUtils.decryptByPrivateKey(requestDTO.getAesKey(), brPrivateKey);
            if (StringUtils.isBlank(plainAesKey)) {
                return fail(XyfResultEnum.PARAMETERS_DECRYPTION_ERROR);
            }
            //2.使用aes密钥解密业务数据
            String data = AESUtils.decrypt(requestDTO.getData(), plainAesKey, false);
            if (StringUtils.isBlank(data)) {
                return fail(XyfResultEnum.PARAMETERS_DECRYPTION_ERROR);
            }
            //3.验签
            boolean result = RSAUtils.verifySignByPublicKey(data, requestDTO.getSign(), xyfPublicKey);
            if (!result) {
                return fail(XyfResultEnum.INVALID_SIGN);
            }
            //4.获取业务数据
            XyfSubmitRecord record = objectMapper.readValue(data, XyfSubmitRecord.class);
            //5.必填项校验
            if (!validate(record)) {
                return fail(XyfResultEnum.PARAMETERS_MISSING_ERROR);
            }
            record.setApiCode(apiCode);
            record.setSyncStatus(XyfSyncStatusEnum.SYNC_WAIT.getCode());
            try {
                xyfSubmitRecordMapper.insertSelective(record);
            } catch (DuplicateKeyException dke) {
                log.warn("{}幂等校验，batchId:{}", TITLE, record.getBatchId());
            }
            return success(record.getBatchId());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(), e.getMessage()
                    , TITLE + "数据接入异常！"), e);
            return fail(XyfResultEnum.BAD_REQUEST);
        }
    }

    /**
     * 根据枚举组装并加密响应（基础方法），data 结构在方法内固定：成功时为 {batchId, respBatchId}，失败时为 null
     * 调用时从 speed 获取 brPrivateKey、xyfPublicKey
     */
    private XyfEncryptionDTO buildEncryptedResponse(XyfResultEnum resultEnum, String batchId) {
        String[] keys = getXyfEncryptionKeys();
        String brPrivateKey = keys[0];
        String xyfPublicKey = keys[1];
        String aesKey = AESUtils.generateAESKey();
        String encryptAesKey = RSAUtils.encryptByPublicKey(aesKey, xyfPublicKey);
        JSONObject data = new JSONObject();
        data.put("status", resultEnum.getStatus());
        data.put("error", resultEnum.getError());
        data.put("msg", resultEnum.getMsg());
        JSONObject dataInner = null;
        if (resultEnum == XyfResultEnum.OK) {
            dataInner = new JSONObject();
            dataInner.put("batchId", batchId != null ? batchId : "");
            dataInner.put("respBatchId", batchId != null ? batchId : "");
        }
        data.put("data", dataInner);
        if (resultEnum != XyfResultEnum.OK) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(),
                    resultEnum.getMsg(), TITLE));
        }
        String body = Utils.toUnicode(JSON.toJSONString(data));
        String encryptData = AESUtils.encrypt(body, aesKey, true);
        String sign = RSAUtils.signByPrivateKey(body, brPrivateKey);
        return new XyfEncryptionDTO(encryptAesKey, encryptData, sign);
    }

    /**
     * 成功响应：data 固定为 {batchId, respBatchId}，由基础方法内部组装
     */
    private XyfEncryptionDTO success(String batchId) {
        return buildEncryptedResponse(XyfResultEnum.OK, batchId);
    }

    /**
     * 失败/异常响应：仅 status、error、msg，data 为 null
     */
    private XyfEncryptionDTO fail(XyfResultEnum resultEnum) {
        return buildEncryptedResponse(resultEnum, null);
    }

    private String fetchApiCode() {
        return marketingCommonConfig.getXyfApiCode();
    }

    private boolean validate(XyfSubmitRecord record) {
        return Stream.of(
                        record.getCorpCode(),
                        record.getAccessToken(),
                        record.getStrategyId(),
                        record.getBatchDate(),
                        record.getBatchId(),
                        record.getContactlist()
                )
                .noneMatch(StringUtils::isBlank);

    }

    /**
     * 接口调用时从 speed 获取加解密密钥，[0]=brPrivateKey, [1]=xyfPublicKey
     */
    private String[] getXyfEncryptionKeys() {
        JSONObject config = marketingCommonConfig.getXyfEncryptionConfig();
        if (config == null) {
            return new String[]{null, null};
        }
        String xyfPublicKey = config.getString("xyfPublicKey");
        String raw = config.getString("brPrivateKey");
        String brPrivateKey = raw != null ? raw.replace("*", "=") : null;
        return new String[]{brPrivateKey, xyfPublicKey};
    }
}
