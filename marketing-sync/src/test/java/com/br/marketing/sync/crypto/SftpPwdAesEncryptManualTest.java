package com.br.marketing.sync.crypto;

import com.br.common.util.AESAlgorithmUtil;
import com.br.marketing.common.utils.Constants;
import org.junit.jupiter.api.Test;

/**
 * 本地手动：把 SFTP 明文密码加密成与库表一致的密文（与 {@link com.br.marketing.sync.service.impl.SyncServiceImpl#insertConfig} 相同算法与密钥）。
 * <p>
 * 运行示例（任选其一）：
 * <pre>
 *   mvn -pl marketing-sync test -Dtest=SftpPwdAesEncryptManualTest#encryptPlainPassword -Dsftp.pwd.plain='你的明文'
 * </pre>
 * 或在下面修改 {@link #DEFAULT_PLAIN} 后运行该测试方法。
 */
class SftpPwdAesEncryptManualTest {

    /** 不配 -Dsftp.pwd.plain 时使用此占位，请改成你的明文或仅用 JVM 参数 */
    private static final String DEFAULT_PLAIN = "ZYWB3760053578";

    @Test
    void encryptPlainPassword() {
        String plain = System.getProperty("sftp.pwd.plain");
        if (plain == null || plain.isEmpty()) {
            plain = DEFAULT_PLAIN;
        }
        String encrypted = AESAlgorithmUtil.encrypt(plain, Constants.SFTP_P_SECRET_KEY);
        System.out.println("======== SFTP 密码 AES 加密结果（可粘贴到库/配置）========");
        System.out.println(encrypted);
        System.out.println("========================================================");
        String roundTrip = AESAlgorithmUtil.decrypt(encrypted, Constants.SFTP_P_SECRET_KEY);
        org.junit.jupiter.api.Assertions.assertEquals(plain, roundTrip, "加解密往返应一致");
    }
}
