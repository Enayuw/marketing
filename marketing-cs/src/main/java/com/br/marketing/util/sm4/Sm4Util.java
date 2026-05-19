package com.br.marketing.util.sm4;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.AesGeneralDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Security;

/**
 * SM4国密对称加密工具类
 * <p>
 * 复用 AesGeneralDTO 作为参数载体，密钥长度固定为16字节(128bit)。
 * 支持ECB/CBC模式，填充方式由页面配置。
 */
@Slf4j
public class Sm4Util {

    private static final String KEY_ALGORITHM = "SM4";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    public static String encrypt(AesGeneralDTO dto) {
        try {
            String text = dto.getText();
            String cipherMode = dto.getCipherMode();
            String paddingScheme = dto.getPaddingScheme();
            Charset charset = getCharset(dto.getCharset());
            String dynamicKeys = dto.getDynamicKeys();
            String iv = dto.getIv();

            checkKeyLength(dynamicKeys);

            String transformation = KEY_ALGORITHM + "/" + cipherMode + "/" + paddingScheme;
            SecretKeySpec skey = new SecretKeySpec(dynamicKeys.getBytes(charset), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);

            if (StringUtils.isEmpty(iv)) {
                cipher.init(Cipher.ENCRYPT_MODE, skey);
            } else {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(charset));
                cipher.init(Cipher.ENCRYPT_MODE, skey, ivParameterSpec);
            }

            byte[] crypted = cipher.doFinal(text.getBytes(charset));
            return Base64.encodeBase64String(crypted);
        } catch (Exception e) {
            log.error("SM4加密失败", e);
            return null;
        }
    }

    public static String decrypt(AesGeneralDTO dto) {
        try {
            String text = dto.getText();
            String cipherMode = dto.getCipherMode();
            String paddingScheme = dto.getPaddingScheme();
            Charset charset = getCharset(dto.getCharset());
            String dynamicKeys = dto.getDynamicKeys();
            String iv = dto.getIv();

            checkKeyLength(dynamicKeys);

            String transformation = KEY_ALGORITHM + "/" + cipherMode + "/" + paddingScheme;
            SecretKeySpec skey = new SecretKeySpec(dynamicKeys.getBytes(charset), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(transformation, BouncyCastleProvider.PROVIDER_NAME);

            if (StringUtils.isEmpty(iv)) {
                cipher.init(Cipher.DECRYPT_MODE, skey);
            } else {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes(charset));
                cipher.init(Cipher.DECRYPT_MODE, skey, ivParameterSpec);
            }

            byte[] decoded = Base64.decodeBase64(text);
            byte[] output = cipher.doFinal(decoded);
            return new String(output, charset);
        } catch (Exception e) {
            log.error("SM4解密失败", e);
            return null;
        }
    }

    private static void checkKeyLength(String key) {
        int length = key.getBytes(StandardCharsets.UTF_8).length;
        if (length != 16) {
            throw new IllegalArgumentException("SM4密钥长度必须为16字节(128bit)，当前为: " + length);
        }
    }

    private static Charset getCharset(String charset) {
        if (charset == null || charset.isEmpty()) {
            return StandardCharsets.UTF_8;
        }
        return Charset.forName(charset);
    }
}
