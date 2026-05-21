package com.br.marketing.innerapi.controller.test;

import com.br.marketing.dto.AesGeneralDTO;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.util.sm4.Sm4Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.Security;

@Slf4j
@RestController
@RequestMapping("/test/sm-crypto")
@Tag(name = "国密SM3/SM4加密测试", description = "用于手动造加密测试数据，仅dev环境启用")
public class SmCryptoTestController {


    @Autowired
    private PushRuleService pushRuleService;

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
    // ======================== SM3 哈希 ========================

    @GetMapping("/sendMq")
    public void sendMq(@RequestParam Long infoId) {
        pushRuleService.insertMarketingPreUserSync(infoId);
    }


    @Operation(summary = "SM3哈希", description = "输入明文，返回SM3摘要(64字符hex)")
    @PostMapping("/sm3/hash")
    public SmCryptoResult sm3Hash(@RequestBody Sm3Request request) {
        SmCryptoResult result = new SmCryptoResult();
        result.setPlainText(request.getPlainText());
        try {
            MessageDigest digest = MessageDigest.getInstance("SM3", "BC");
            byte[] hashBytes = digest.digest(request.getPlainText().getBytes(StandardCharsets.UTF_8));
            String hashHex = Hex.toHexString(hashBytes);
            result.setEncryptedText(hashHex);
            result.setAlgorithm("SM3");
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("SM3哈希失败", e);
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    @Operation(summary = "SM3批量哈希", description = "批量生成SM3摘要，多个明文用逗号分隔")
    @PostMapping("/sm3/hash-batch")
    public SmCryptoBatchResult sm3HashBatch(@RequestBody Sm3BatchRequest request) {
        SmCryptoBatchResult batchResult = new SmCryptoBatchResult();
        String[] items = request.getPlainTexts().split(",");
        try {
            MessageDigest digest = MessageDigest.getInstance("SM3", "BC");
            StringBuilder sb = new StringBuilder();
            for (String item : items) {
                String trimmed = item.trim();
                byte[] hashBytes = digest.digest(trimmed.getBytes(StandardCharsets.UTF_8));
                String hashHex = Hex.toHexString(hashBytes);
                sb.append(trimmed).append(" -> ").append(hashHex).append("\n");
                digest.reset();
            }
            batchResult.setResults(sb.toString());
            batchResult.setCount(items.length);
            batchResult.setSuccess(true);
        } catch (Exception e) {
            log.error("SM3批量哈希失败", e);
            batchResult.setSuccess(false);
            batchResult.setErrorMsg(e.getMessage());
        }
        return batchResult;
    }

    // ======================== SM4 加解密 ========================

    @Operation(summary = "SM4加密", description = "输入明文和参数，返回SM4密文(Base64)")
    @PostMapping("/sm4/encrypt")
    public SmCryptoResult sm4Encrypt(@RequestBody Sm4Request request) {
        SmCryptoResult result = new SmCryptoResult();
        result.setPlainText(request.getPlainText());
        try {
            AesGeneralDTO dto = buildSm4Dto(request);
            dto.setText(request.getPlainText());
            String encrypted = Sm4Util.encrypt(dto);
            result.setEncryptedText(encrypted);
            result.setAlgorithm("SM4/" + request.getCipherMode() + "/" + request.getPaddingScheme());
            result.setSuccess(encrypted != null);
            if (encrypted == null) {
                result.setErrorMsg("加密返回null，请检查参数");
            }
        } catch (Exception e) {
            log.error("SM4加密失败", e);
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    @Operation(summary = "SM4解密", description = "输入密文(Base64)和参数，返回明文")
    @PostMapping("/sm4/decrypt")
    public SmCryptoResult sm4Decrypt(@RequestBody Sm4Request request) {
        SmCryptoResult result = new SmCryptoResult();
        result.setEncryptedText(request.getEncryptedText());
        try {
            AesGeneralDTO dto = buildSm4Dto(request);
            dto.setText(request.getEncryptedText());
            String decrypted = Sm4Util.decrypt(dto);
            result.setPlainText(decrypted);
            result.setAlgorithm("SM4/" + request.getCipherMode() + "/" + request.getPaddingScheme());
            result.setSuccess(decrypted != null);
            if (decrypted == null) {
                result.setErrorMsg("解密返回null，请检查参数和密文");
            }
        } catch (Exception e) {
            log.error("SM4解密失败", e);
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    @Operation(summary = "SM4加密+解密验证", description = "加密后立即解密，验证结果一致性")
    @PostMapping("/sm4/verify")
    public Sm4VerifyResult sm4Verify(@RequestBody Sm4Request request) {
        Sm4VerifyResult result = new Sm4VerifyResult();
        result.setPlainText(request.getPlainText());
        try {
            AesGeneralDTO encDto = buildSm4Dto(request);
            encDto.setText(request.getPlainText());
            String encrypted = Sm4Util.encrypt(encDto);
            result.setEncryptedText(encrypted);

            AesGeneralDTO decDto = buildSm4Dto(request);
            decDto.setText(encrypted);
            String decrypted = Sm4Util.decrypt(decDto);
            result.setDecryptedText(decrypted);

            boolean match = request.getPlainText().equals(decrypted);
            result.setMatch(match);
            result.setSuccess(true);
        } catch (Exception e) {
            log.error("SM4验证失败", e);
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    private AesGeneralDTO buildSm4Dto(Sm4Request request) {
        AesGeneralDTO dto = new AesGeneralDTO();
        dto.setCipherMode(request.getCipherMode() != null ? request.getCipherMode() : "ECB");
        dto.setPaddingScheme(request.getPaddingScheme() != null ? request.getPaddingScheme() : "PKCS5Padding");
        dto.setCharset(request.getCharset());
        dto.setDynamicKeys(request.getKey());
        dto.setIv(request.getIv());
        return dto;
    }

    // ======================== Request/Response DTO ========================

    @Data
    public static class Sm3Request {
        private String plainText;
    }

    @Data
    public static class Sm3BatchRequest {
        private String plainTexts;
    }

    @Data
    public static class Sm4Request {
        private String plainText;
        private String encryptedText;
        private String key;
        private String cipherMode;
        private String paddingScheme;
        private String charset;
        private String iv;
    }

    @Data
    public static class SmCryptoResult {
        private boolean success;
        private String algorithm;
        private String plainText;
        private String encryptedText;
        private String errorMsg;
    }

    @Data
    public static class SmCryptoBatchResult {
        private boolean success;
        private int count;
        private String results;
        private String errorMsg;
    }

    @Data
    public static class Sm4VerifyResult {
        private boolean success;
        private String plainText;
        private String encryptedText;
        private String decryptedText;
        private boolean match;
        private String errorMsg;
    }
}
