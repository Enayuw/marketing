package com.br.marketing.innerapi.controller.test;

import com.br.marketing.dto.AesGeneralDTO;
import com.br.marketing.util.aes.AesUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 与 {@link AesUtil} / 上传校验一致：密文为 Base64，密钥为字符串（UTF-8 字节长度须为 16/24/32）。
 */
@Slf4j
@RestController
@RequestMapping("/test/aes-general")
@Tag(name = "通用AES加解密测试", description = "入参同 AesGeneralDTO，须传齐 text、cipherMode、paddingScheme、charset、dynamicKeys、iv")
public class AesGeneralCryptoTestController {

    @Operation(summary = "AES加密", description = "text=明文；返回 Base64 密文")
    @PostMapping("/encrypt")
    public AesGeneralCryptoResult encrypt(@RequestBody AesGeneralDTO dto) {
        AesGeneralCryptoResult result = new AesGeneralCryptoResult();
        try {
            String cipher = AesUtil.encrypt(dto);
            result.setPlainText(dto.getText());
            result.setEncryptedText(cipher);
            result.setSuccess(StringUtils.isNotBlank(cipher));
            if (!result.isSuccess()) {
                result.setErrorMsg("加密返回空，请检查密钥长度(16/24/32 字节)、模式、填充与明文编码");
            }
        } catch (IllegalArgumentException e) {
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        } catch (Exception e) {
            log.error("AES加密失败", e);
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    @Operation(summary = "AES解密", description = "text=Base64 密文；返回明文")
    @PostMapping("/decrypt")
    public AesGeneralCryptoResult decrypt(@RequestBody AesGeneralDTO dto) {
        AesGeneralCryptoResult result = new AesGeneralCryptoResult();
        try {
            String plain = AesUtil.decrypt(dto);
            result.setEncryptedText(dto.getText());
            result.setPlainText(plain);
            result.setSuccess(StringUtils.isNotBlank(plain));
            if (!result.isSuccess()) {
                result.setErrorMsg("解密返回空，请检查密文是否为合法 Base64、密钥与模式是否与加密一致");
            }
        } catch (IllegalArgumentException e) {
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        } catch (Exception e) {
            log.error("AES解密失败", e);
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    @Data
    public static class AesGeneralCryptoResult {
        private boolean success;
        private String plainText;
        /** 加密时为 Base64 输出；解密时为入参 text（Base64 密文） */
        private String encryptedText;
        private String errorMsg;
    }
}
