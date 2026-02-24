package com.br.marketing.datarelayservice.demo;

import com.alibaba.fastjson.JSON;
import com.br.marketing.util.xyf.AESUtils;
import com.br.marketing.util.xyf.RSAUtils;
import com.br.marketing.util.xyf.Utils;
import org.apache.commons.lang3.StringUtils;

/**
 * 信用飞请求体解密：aesKey(RSA 加密) + data(AES 加密) + sign(对明文 data 的签名)。
 * 请在下方填写 BR_PRIVATE_KEY，运行 main 即可解密密文。
 */
public class XyfDecryptTest {

    /** 我方 RSA 私钥（用于解密 aesKey），请替换为实际私钥 */
    private static final String BR_PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCIyYEjULZtmp2udJCCwnAGLNIf0wPH9WnohMfHPgogrvPYLM1IcSp8XF9MfB5VTl1YEZa9g8O+kfv+dU1ZAn+7dQDhmzBt172K6XKJoluaF2KHweLCT9YAOGVp5rWn7KkoYW0dr7ukGUyLa64iXH2PWi35NrZpQv2aG8ZFdwKfv9/ZMEjlFRSUQDwWabOoaJNXkuhl8Ysv0gAGlyljQRUa/wNDE5lJk9ckRi6IK2g9//4snOCLtQYM57WZFaJx/tjL1jvdsKV29FMK3g7Sw0d6dHG4JHxJ6SMBVel1oe3Hx/DHpW/jd2c6IDzNf1YLaKC1dXLarSL7yEdejs+39wInAgMBAAECggEAGdU8b0f5+/8cUmsvM5K3B2OFl38JksT1aHVTKXy8+ukvoh7r8gd1R6FGWjKCpAe1t0EzH713xVDar3bF0c6YAvYY4YKmhcgdwtNNz1J7TEGoWQ0z3xCm0MnV1Uc9/WXDmJ21tsqOfHJOs3H1pAWlrOISFqPPlVeSf31JV6sfMr4JBTqjJ4i47u3eoaJ3AeoKxi5sMkVikFqaJmDbcS6aBmQTJ7rGHsvy9nwwbzntqKRdBJ4G/QeAbx1PLdrraSLRZ5CGpGXfamsVY7gxDFdhe5KPs5vyxa8PFGSv9vQP/QOLrp6wsunH9SpZMtBdpkmu79c4eR0tGKK+ucowKCPT+QKBgQDhgQe8F2wjDakfHUlh3n5XW0ol7yHz4/rOs763xHJKBLsFAkHD4x+e4co+DzAux6tgcB3z9/yWpjv9XmYEKfjq3gFKAzg3qfkdvUfiNJuNgCd3gxd29IN37Ef607CTGeurFgc4qHHJPgRXSY7Mv5QBFWQ3/MwbOcFD0ry3RC6DvQKBgQCbSRZR5KRSRRGmxuzjD92aZrJnPEO2M+1QSYmp+21+ZIJiUvs0KQsMi+MHF34M1ghiOu7ps2sMauqM1PETfmU6WMRfekgIhzSWmjpNiPIZBP5ADU3q+D3198rD7bS7ZMuQ9S7z1MX6AcUZ12LNnv38ZpWjrxRXfy54dYaPdWBJswKBgQCf6u0dSk75nNpuokpECZD3SloPBMLDPs3CgF2j3MBgFXK7Xppc3Fi0R9Mwp3y38Als3pWfSiHqhADO8X7PPSS1CrNHVXksoHF10aGEnWIiuttCOckOJuCEzx9MlYsHBr7GGV0NpQbse/qAHD4UnVfMXOsxeSkVtaOFAZGrNcL20QKBgGvbR/V5qei6sJf2c3MDAhjMW/34jc9wIdUyPrwiNtsjVKyWloSUPWhpgTbbymGACDKyXF9Wj3QAjwlink+76vOr1Dm85WuG/a/dT8Gq7P0TysUSeJxL8D137w5HfNDsIbh9lC8oCpqrwAtC3w2S8TbuyeD3KlnbrJtehif+jvKhAoGATrytYunwUoCwWC5LN1IinOyo1fHfac3sn3HFbQwFbkkZakYMvmSuNS6Dj4TiR8N3Qe1VxUu0F5H07Gh3B3zfGJEa5/VOesksdnd1NTPXpyu4lw4tMWLM/tqCH4p1CUBY+Lfpc25dNdq98Ed9lBMIJbYoljmB/eirU3gkTqpskL0=";

    /** 信用飞方公钥（仅验签用，不验签可留空） */
    private static final String XYF_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyh3eWUV5I2bCUhTVIrOS+mFVN/BFAQ46mz0jGMoLm2cOvNkU8/VYWUJYVmErkrUuFpRb6DTMZSzFNCe8SeadSpkEefoDOa3ow/0RJVv+uKuBTPknsZJ52KN1l822Axr4YhouJ7B4Slw8ER2K08JpguGGld1T9hbjGjXx5or+M+Egscecg4hyxdS+p78qPcdNemxz5qQw6FPg5CFAECMs6Dj3zvVQRG5ZqEv440LqfISndkDJywecYhcY7oeM32t4eZYqumpooj6kjDCpFadTUQzDCcENuZ2KMG1mk/s6rI3M9Dpb6CgQC4XY+oKduAIpCWzffMgJE6jGhvhrovWjQQIDAQAB";

    /** 待解密的请求体 JSON */
    private static final String ENCRYPTED_JSON = "{\n" +
            "    \"aesKey\": \"IuV+uVAZ8lurXNzchObKwHLwEsCKkHBV9Tpgwr069V8+y/r/CrKqsrcMzFG7uJbdhp7pQKBTdPLv+XaqAnJgTMBikUJaq9FsoPXmvZf+rPf4NAfCg+CJuAmedWh4FC4EbMZfcGjFHPaSf/Lrwe7Z95nOhs36WPQfFA1ZYVW7KkBJ7AXp2fCzYBe5e0p4T4Jf/fala4wbppq1q1op43cacvSq9mmQWPSBASQLEsfcbtU42uO6iUC209bxoUOiMMhzuwGV5vO/KYw9stebpaiy1rA8IB1SlEdSsZlFs7Nwd1O7huQmPDmIMt39wysiTYJsdjJBRfBzlpCX4TMZxb2y4w==\",\n" +
            "    \"data\": \"Ne0Jy6xSWmU9j4ZTR5pto6b7wRCnDrCN5NXU1SzEdDT2vDRFadmvxBqIl9xUhPFJI3S31ckecz6TQXndFYFDDMowhQpMABlcKCqmDg8v0lI4Ch4UFf4TSL7CP10XBS3rk2lom54u6NLJ5lWDHnNSi8GjhvLPWu/ji8Grk5p+UOEo8h1ZPoDOtS1kUjZ6zd5p\",\n" +
            "    \"sign\": \"bxVcSgvbYHijm9JjX+krco2E5Wn95ssxakHEdwpW4m9pYmPw+0XgsijG1CpDz9RJGZWsZPe5hDkOoQHP85fBciIrGjRYHQ/kylipsbXExOqu9egKCGLsqTmgItm94zXi4RIOss8+wArYdDWlWJMuDdZUnb87VYoqs6aC8A2PuaQj+SLQlBvkrG7qrdTSPhPydSVJ/yerqEFKHjeqQDZjhKjOenaANubchYJ/+o3jq7c754Zqd868PWg3qGn7gmVoi4jDQav+WG5ClYCNh9+UHuvBeuCABUs4s9IlmFvAjp87k5mu4xNkn1eTEwcGAfMFdcpQFJheoZIGGVMiXIkOKQ==\"\n" +
            "}";

    public static void main(String[] args) {
        if (StringUtils.isBlank(BR_PRIVATE_KEY)) {
            System.err.println("请先填写 BR_PRIVATE_KEY");
            return;
        }
        try {
            com.alibaba.fastjson.JSONObject json = JSON.parseObject(ENCRYPTED_JSON);
            String aesKeyEnc = json.getString("aesKey");
            String dataEnc = json.getString("data");
            String sign = json.getString("sign");

            // 1. RSA 私钥解密得到明文 AES 密钥
            String plainAesKey = RSAUtils.decryptByPrivateKey(aesKeyEnc, BR_PRIVATE_KEY);
            if (StringUtils.isBlank(plainAesKey)) {
                System.err.println("aesKey 解密失败，请检查私钥或密文");
                return;
            }

            // 2. 用明文 AES 密钥解密 data
            String plainData = AESUtils.decrypt(dataEnc, plainAesKey, false);
            if (StringUtils.isBlank(plainData)) {
                System.err.println("data 解密失败，请检查 AES 密钥或密文");
                return;
            }

            // 3. 若配置了公钥则验签
            if (StringUtils.isNotBlank(XYF_PUBLIC_KEY)) {
                boolean signOk = RSAUtils.verifySignByPublicKey(plainData, sign, XYF_PUBLIC_KEY);
                System.out.println("验签结果: " + (signOk ? "通过" : "失败"));
            }

            // 4. 若明文为 unicode 编码（如 \u4e2d\u6587），可转回中文
            String display = Utils.toChinese(plainData);
            System.out.println("解密结果:\n" + display);
        } catch (Exception e) {
            System.err.println("解密异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
