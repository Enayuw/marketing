package com.br.marketing.datarelayservice.test.tc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 同程侧联调工具：对发往 relay 的 {@link TcRequestDTO} 做请求加签（与 {@link RSAUtil#SignVf} 规则一致），
 * 以及对 relay 返回的 {@link TcResponseDTO} 做响应验签（与 {@link RSAUtil#sign(TcResponseDTO, String)} 规则一致，模拟同程用百融公钥验签）。
 * <p>
 * 使用前请在下方两个常量中填入 Base64 密钥串（与 Speed {@code tcyrServerConfig} 中配置成对）。
 * <p>
 * 本类为工具类（私有构造），不要在此类上写 {@code @Test}，否则 JUnit 无法实例化；控制台样例与响应验签测试见 {@link TcRelaySignAndVerifyHelperTest}。
 */
public final class TcRelaySignAndVerifyHelper {

    /** 同程侧私钥（PKCS#8 Base64）：用于请求体加签，与 relay 配置的 {@code tcPublicKey} 成对 */
    private static final String tcPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCz0d7j3WGnfMIjvSspB7v2ZcmCWJ8Zqzn1gvMe99bqWOnve7V3fpTZSdZVMg7B8viJDUtrUqDY/ZSWsUojW0N374bzjYlGNZYlWmYqiZfKZZ44ABK6+dky5MDhnaTpYihlhwxiKp8P48awjyMxgqWxv7dWhSEgVFvhDkIQr9XYtQf11EL2s1N6bqkaf1uvGARAb6yDz9vh4torzow+bmfe0HejH1Qyw5C3eza+WEmhFdBFEeH4GhkLwp3wQ6mopQDpyPPXzrNJIo7zaqbaJ+s+LTj2q8ECV8hHndwSHrai0fogkadAMF9tgTD2UwHZAtvK0K8IW1RL0E8kFIamflWVAgMBAAECggEASu+H0i+clX6RLPGVPekCNIFgg1hJHRpU8fIbPOmNf2WEP4+vJNf0UcTKdACDU+HcHskSh+wMKcErHc1OFwPeTunbtD1kWoTUSEau0sU6I1dLowyswYyDLglUM/FNGxETwpOP3ozicm26jDNqOCS4xiUd0wlxr5ZYH6agc3HDTSYXG6M+Q4q8jhHs0oEpr4b4aL0oDNHnEg7mPynS/LEritzr1D06dBXbF6UTlOVB/swnvsidAEHbdzIbmL+sH+pqljnkgk1wtrkqWTDJj4apzmQIuDiRwk8yWtPHiQTxhqk0EKSTsTVie0KaEuW7NrgcaGFhSFlrkCOhx+oEpWkWoQKBgQD40s73g8BR70LcG7N9V9ITh+7IkW6BptKJr8nKCm7+xTyGh7aInwT50PJBVJVmfVub4Bf5wVD6BE1IJ/Ss9WYJPZa2pK0QqwFElGKDzxx19NdBNqsR3W8t6CaM39iokUwaDLC4YUK41IJ1h9uFsHPJg4raX/ye1d/uQijedx1TrQKBgQC5AZKcWC2xU7Gz0GNjupkhfmt+H1+jXPybwM9kz1pScZ5z0bbZ8ZuiS0VC0eI+ILLEMa5UmeSik5ZEJJHLFwzQgQukNBQeU+llRRWqSmXyabkJD3zC85hQCm2kUbLUVzexgvB7CPL1hqQT6ayMItQf9+/2jgrkRrHUalD/hgCGiQKBgFKY0AFT5/SK4vvj6ioyi9bV6csEk9VQBlWUV/zMh9nkqVnTFSG2/9TZqoFLTajO9ikBM5RButqzsN/B+7OqZmus2SnZ8mU1Dt+wDh/JEZ6KXyYTuqfchLqNdLaQ2//g8402JzedeaOXT5MqPRHc6CK9msswz8/+GS6jIaPvkHmlAoGAWfPazivNo6+28l/7Q01CEVf/eeZVQQAATtazwCdVmkpmKZgpGNTxwDpq5a9ZGq4ZXW1ufvIIicfKwz0oqh99+o8UEvXDZm+URsoNW6wq32/qKO6f0cZRI3G+l6ulkLsLeELbHGdggmLBunDelZCFpTmPMkkkIJQC+O3sjiEgdkkCgYBUBxcHzomyZ/oUZNutF5qMBwzM6S7iLITPNNVAUYRHIGPebeUrTkS+kZgvfP9qLFq41kja8KdauPD1OX2FsE/Q7LpjbxsSfIkL5rdqwpICgX/DMEM9T1450BE0QipPLBR8euhCfxrUPNYiANEPrMmJOvHWgzAjBvV8M1ID1CP/VA==";

    /** 百融侧公钥（X509 Base64）：用于校验 relay 返回 JSON，与 relay 签名使用的 {@code brPrivateKey} 成对 */
    private static final String brPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuiWylD7GoTGf/UtmUUXPHfbBTsvtAjcgUDbq4VBzrxKTjX1YcdQYxPhxmZeuAoWvDzm19rQ/QbceMGtN5PvwIT4u3TXd/Bva/mG7EW0qIcz0pfWpAg2n9cYisO5vZR/xjfy5P77aUR1qDyecLuki300Z/hc2KsYYqae9uTgN7xJO0dnwfZlF8uL1vxOfYQ/X7osW4ndZu3NQGno4MJLvgSkX5rXC72h7L0596w1EsXU+I5FzMFsCFQODN97LIuWfvbNVVbsbKJDfYOl7E+CtDPRoqARw8JxIjrIYkcjOKDxWafBpruZdRCe90nOvuIkvKtXAVtN3jxCwaJ94v4fJJwIDAQAB";

    private TcRelaySignAndVerifyHelper() {
    }

    /**
     * 使用类常量 {@link #tcPrivateKey} 为请求加签并写入 {@code dto.sign}（待签串与 {@link RSAUtil#SignVf} 一致：Fastjson 序列化 + {@link RSAUtil#generateContent}）。
     */
    public static void signTcRequest(TcRequestDTO dto) {
        if (tcPrivateKey.isBlank()) {
            throw new IllegalStateException("请先在 TcRelaySignAndVerifyHelper 中配置 tcPrivateKey");
        }
        signTcRequest(dto, tcPrivateKey);
    }

    /**
     * 使用指定同程私钥为请求加签并写入 {@code dto.sign}（加签前会清空原 sign，避免把旧 sign 拼进待签串）。
     */
    public static void signTcRequest(TcRequestDTO dto, String tcPrivateKeyMaterial) {
        dto.setSign("");
        dto.setSign(computeRequestSign(dto, tcPrivateKeyMaterial));
    }

    @SuppressWarnings("unchecked")
    private static String computeRequestSign(TcRequestDTO dto, String tcPrivateKeyMaterial) {
        Map<String, Object> reqContent = JSON.parseObject(JSON.toJSONString(dto), LinkedHashMap.class);
        String plain = RSAUtil.generateContent(reqContent);
        return RSAUtil.signByPrivateKey(tcPrivateKeyMaterial, plain);
    }

    /**
     * 模拟同程侧：校验 relay 返回是否由百融私钥签发，使用类常量 {@link #brPublicKey}。
     *
     * @return 验签是否通过
     */
    public static boolean verifyRelayResponseAsTcPartner(TcResponseDTO response) {
        if (brPublicKey.isBlank()) {
            throw new IllegalStateException("请先在 TcRelaySignAndVerifyHelper 中配置 brPublicKey");
        }
        return verifyRelayResponseAsTcPartner(response, brPublicKey);
    }

    /**
     * 模拟同程侧：使用指定百融公钥校验 relay 返回。
     * <p>
     * 与 {@link RSAUtil#sign(TcResponseDTO, String)} 对称：Jackson {@code convertValue} 成 Map 后 {@link RSAUtil#generateContent}，再公钥验签。
     */
    @SuppressWarnings("unchecked")
    public static boolean verifyRelayResponseAsTcPartner(TcResponseDTO response, String brPublicKeyMaterial) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> convert = mapper.convertValue(response, Map.class);
        String content = RSAUtil.generateContent(convert);
        return RSAUtil.verifySignByPublicKey(brPublicKeyMaterial, response.getSign(), content);
    }

    /**
     * 将 Postman / 日志中的响应 JSON 反序列化后验签（模拟同程），使用类常量 {@link #brPublicKey}。
     */
    public static boolean verifyRelayResponseJsonAsTcPartner(String responseJson) {
        TcResponseDTO dto = JSON.parseObject(responseJson, TcResponseDTO.class);
        return verifyRelayResponseAsTcPartner(dto);
    }

    /**
     * 控制台打印 {@code marketDataPush} 的 Postman Body 示例（withoutSign 与正式加签各一段）。
     */
    public static void printPostmanMarketDataPushSamples() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        var pretty = mapper.writerWithDefaultPrettyPrinter();
        String requestNo = "POSTMAN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String timestamp = String.valueOf(System.currentTimeMillis());

        JSONObject dataJson = new JSONObject(true);
        dataJson.put("batchNo", "BR_POSTMAN_BATCH_" + System.currentTimeMillis());
        dataJson.put("fileUrl", "https://example.com/marketing/test-file.csv");
        dataJson.put("fileExpirationTime", "2099-12-31 23:59:59");
        dataJson.put("startDate", "20260101");
        dataJson.put("endDate", "20261231");
        dataJson.put("total", 10L);
        String dataStr = dataJson.toJSONString();

        System.out.println("======== Postman 说明 ========");
        System.out.println("Header: Content-Type = application/json");
        System.out.println();
        System.out.println("【正式验签】POST .../marketing/v1/api/marketDataPush");
        System.out.println("【免客户端签】POST .../marketing/v1/api/withoutSign/marketDataPush （服务端会重写 timestamp 与 sign）");
        System.out.println();

        TcRequestDTO withoutSign = new TcRequestDTO();
        withoutSign.setRequestNo(requestNo + "-WS");
        withoutSign.setTimestamp("0");
        withoutSign.setSign("will-be-replaced");
        withoutSign.setData(dataJson.toJSONString());
        System.out.println("======== Body: withoutSign/marketDataPush（复制到 Postman）========");
        System.out.println(pretty.writeValueAsString(withoutSign));
        System.out.println();

        if (tcPrivateKey.isBlank()) {
            System.err.println("未配置 tcPrivateKey，跳过「正式 marketDataPush」已加签 Body。");
            return;
        }

        TcRequestDTO signed = new TcRequestDTO();
        signed.setRequestNo(requestNo + "-SIGNED");
        signed.setTimestamp(timestamp);
        signed.setData(dataStr);
        signed.setSign("");
        signTcRequest(signed);

        System.out.println("======== Body: marketing/v1/api/marketDataPush（复制到 Postman）========");
        System.out.println(pretty.writeValueAsString(signed));
    }

    public static void main(String[] args) throws Exception {
        printPostmanMarketDataPushSamples();
    }
}
