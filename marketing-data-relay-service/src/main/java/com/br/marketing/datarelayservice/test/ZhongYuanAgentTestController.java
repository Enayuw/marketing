package com.br.marketing.datarelayservice.test;

import com.alibaba.fastjson.JSON;
import com.br.marketing.datarelayservice.controller.ZhongYuanAgentController;
import com.br.marketing.datarelayservice.enums.ZhongYuanAgentMtResponseCode;
import com.br.marketing.dto.zhongyuan.MtStandardRequest;
import com.br.marketing.dto.zhongyuan.MtStandardResponse;
import com.br.marketing.dto.zhongyuan.ZhongYuanAgentChannelRsaConfig;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.aes.AesZhongYuan;
import com.br.marketing.utils.Encodes;
import com.br.marketing.utils.RsaUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 中原坐席导入联调辅助接口（仅用于 Postman / 本地造数）
 */
@Tag(name = "中原坐席-联调工具")
@RestController
@RequestMapping("/v1/task/dev/zhongyuan-agent")
@Slf4j
public class ZhongYuanAgentTestController {

    private static final String RSA_PADDING = RsaUtil.PK_CS1;
    private static final String AES_PADDING = AesZhongYuan.ECB_ALGORITHM_PADDING;


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * Postman：Body 选 raw → JSON，内容只填与正式报文里 {@code requestData} 解密后一致的那段业务 JSON（不要包 requestNo/key 等外壳）。
     * 响应体：仅 {@link MtStandardRequest}，可直接作为 {@code POST /v1/task/importAgentCustomer} 的请求体。
     */
    @Operation(summary = "根据 requestData 明文生成 MtStandardRequest（响应体仅此对象）")
    @PostMapping(value = "/sample-import-body", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MtStandardRequest sampleImportBody(@RequestBody(required = false) String requestDataPlainBody) throws Exception {
        ZhongYuanAgentChannelRsaConfig rsaCfg = ZhongYuanAgentChannelRsaConfig.fromConfigJson(
                marketingCommonConfig.getZhongYuanAgentChannelRsa());
        if (rsaCfg == null || !StringUtils.hasText(rsaCfg.getPublicKey()) || !StringUtils.hasText(rsaCfg.getPrivateKey())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "未配置 zhongYuanAgentChannelRsa 或缺少 publicKey/privateKey");
        }

        try {
            JSON.parse(requestDataPlainBody);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestData 明文不是合法 JSON: " + e.getMessage());
        }

        PublicKey pub = RsaUtil.getPublicKey(rsaCfg.getPublicKey().trim());
        PrivateKey priv = RsaUtil.getPrivateKey(rsaCfg.getPrivateKey().trim());

        String requestNo = "LHH" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + "120000"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String timestamp = String.valueOf(System.currentTimeMillis());

        byte[] aesKey = AesZhongYuan.generateAesKey();
        String keyCipher = RsaUtil.encrypt2Base64String(aesKey, pub, null, RSA_PADDING);
        String requestDataCipher = AesZhongYuan.encrypt2Base64String(requestDataPlainBody, aesKey, null, AES_PADDING);

        MtStandardRequest req = new MtStandardRequest();
        req.setRequestNo(requestNo);
        req.setTimestamp(timestamp);
        req.setKey(keyCipher);
        req.setRequestData(requestDataCipher);
        String signPlain = req.signData();
        req.setSign(signSha1Rsa(signPlain, priv));

        log.warn("sample-import-body 已生成 requestNo={}", requestNo);
        return req;
    }

    /**
     * Postman：Body 为 {@code importAgentCustomer} 返回的 {@link MtStandardResponse} JSON（含 sign、key、responseData）。
     * 响应体：仅 {@code responseData} 解密后的明文（如 {@code {"batchNo":"..."}}），{@code text/plain}。
     */
    @Operation(summary = "解密 importAgentCustomer 成功回包中的 responseData（响应体仅明文字符串）")
    @PostMapping(value = "/decrypt-import-response", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "text/plain;charset=UTF-8")
    public String decryptImportResponse(@RequestBody MtStandardResponse resp) throws Exception {
        ZhongYuanAgentChannelRsaConfig rsaCfg = ZhongYuanAgentChannelRsaConfig.fromConfigJson(
                marketingCommonConfig.getZhongYuanAgentChannelRsa());
        if (rsaCfg == null || !StringUtils.hasText(rsaCfg.getPublicKey()) || !StringUtils.hasText(rsaCfg.getPrivateKey())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "未配置 zhongYuanAgentChannelRsa 或缺少 publicKey/privateKey");
        }

        if (!ZhongYuanAgentMtResponseCode.SUCCESS.getCode().equals(resp.getErrorCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "非成功响应(errorCode=" + resp.getErrorCode() + ")，通常无 responseData 密文: " + resp.getErrorMsg());
        }
        if (!StringUtils.hasText(resp.getKey()) || !StringUtils.hasText(resp.getResponseData())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "缺少 key 或 responseData，无法解密");
        }

        PublicKey pub = RsaUtil.getPublicKey(rsaCfg.getPublicKey().trim());
        PrivateKey priv = RsaUtil.getPrivateKey(rsaCfg.getPrivateKey().trim());

        if (!verifySha1Rsa(resp.signData(), resp.getSign(), pub)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "验签失败（请确认与当前 zhongYuanAgentChannelRsa 密钥一致）");
        }

        try {
            byte[] aesKey = RsaUtil.decryptBase64Content2Byte(resp.getKey(), priv, null, RSA_PADDING);
            return AesZhongYuan.decryptBase64Content2String(resp.getResponseData(), aesKey, null, AES_PADDING);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "解密失败: " + e.getMessage(), e);
        }
    }

    private static String signSha1Rsa(String signData, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privateKey);
        signature.update(signData.getBytes(StandardCharsets.UTF_8));
        return Encodes.encodeBase64(signature.sign());
    }

    private static boolean verifySha1Rsa(String signData, String sign, PublicKey publicKey) throws Exception {
        Signature v = Signature.getInstance("SHA1WithRSA");
        v.initVerify(publicKey);
        v.update(signData.getBytes(StandardCharsets.UTF_8));
        return v.verify(Encodes.decodeBase64(sign));
    }
}
