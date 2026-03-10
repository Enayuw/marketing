package com.br.marketing.datarelayservice.util;

import org.junit.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

import static org.junit.Assert.assertNotNull;

/**
 * 生成两对 RSA 密钥（与 RSAUtils 相同算法：RSA 2048），用于信用飞加解密配置。
 * 运行该测试方法即可在控制台看到两对 公钥/私钥（Base64）。
 */
public class RsaKeyPairGenTest {

    private static final String RSA = "RSA";
    private static final int KEY_SIZE = 2048;

    @Test
    public void genTwoKeyPairs() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
        keyPairGen.initialize(KEY_SIZE);

        for (int i = 1; i <= 2; i++) {
            KeyPair keyPair = keyPairGen.generateKeyPair();
            String publicKeyStr = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            String privateKeyStr = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());

            assertNotNull(publicKeyStr);
            assertNotNull(privateKeyStr);

            System.out.println("======== 第" + i + "对 ========");
            System.out.println("公钥=" + publicKeyStr);
            System.out.println("私钥=" + privateKeyStr);
        }
    }
}
