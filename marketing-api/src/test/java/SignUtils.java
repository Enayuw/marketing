import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class SignUtils {

    private static final String KEY_ALGORITHM = "RSA";

    public static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

    private static final String ENCODING = "UTF-8";

    private static final String tcPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCz0d7j3WGnfMIjvSspB7v2ZcmCWJ8Zqzn1gvMe99bqWOnve7V3fpTZSdZVMg7B8viJDUtrUqDY/ZSWsUojW0N374bzjYlGNZYlWmYqiZfKZZ44ABK6+dky5MDhnaTpYihlhwxiKp8P48awjyMxgqWxv7dWhSEgVFvhDkIQr9XYtQf11EL2s1N6bqkaf1uvGARAb6yDz9vh4torzow+bmfe0HejH1Qyw5C3eza+WEmhFdBFEeH4GhkLwp3wQ6mopQDpyPPXzrNJIo7zaqbaJ+s+LTj2q8ECV8hHndwSHrai0fogkadAMF9tgTD2UwHZAtvK0K8IW1RL0E8kFIamflWVAgMBAAECggEASu+H0i+clX6RLPGVPekCNIFgg1hJHRpU8fIbPOmNf2WEP4+vJNf0UcTKdACDU+HcHskSh+wMKcErHc1OFwPeTunbtD1kWoTUSEau0sU6I1dLowyswYyDLglUM/FNGxETwpOP3ozicm26jDNqOCS4xiUd0wlxr5ZYH6agc3HDTSYXG6M+Q4q8jhHs0oEpr4b4aL0oDNHnEg7mPynS/LEritzr1D06dBXbF6UTlOVB/swnvsidAEHbdzIbmL+sH+pqljnkgk1wtrkqWTDJj4apzmQIuDiRwk8yWtPHiQTxhqk0EKSTsTVie0KaEuW7NrgcaGFhSFlrkCOhx+oEpWkWoQKBgQD40s73g8BR70LcG7N9V9ITh+7IkW6BptKJr8nKCm7+xTyGh7aInwT50PJBVJVmfVub4Bf5wVD6BE1IJ/Ss9WYJPZa2pK0QqwFElGKDzxx19NdBNqsR3W8t6CaM39iokUwaDLC4YUK41IJ1h9uFsHPJg4raX/ye1d/uQijedx1TrQKBgQC5AZKcWC2xU7Gz0GNjupkhfmt+H1+jXPybwM9kz1pScZ5z0bbZ8ZuiS0VC0eI+ILLEMa5UmeSik5ZEJJHLFwzQgQukNBQeU+llRRWqSmXyabkJD3zC85hQCm2kUbLUVzexgvB7CPL1hqQT6ayMItQf9+/2jgrkRrHUalD/hgCGiQKBgFKY0AFT5/SK4vvj6ioyi9bV6csEk9VQBlWUV/zMh9nkqVnTFSG2/9TZqoFLTajO9ikBM5RButqzsN/B+7OqZmus2SnZ8mU1Dt+wDh/JEZ6KXyYTuqfchLqNdLaQ2//g8402JzedeaOXT5MqPRHc6CK9msswz8/+GS6jIaPvkHmlAoGAWfPazivNo6+28l/7Q01CEVf/eeZVQQAATtazwCdVmkpmKZgpGNTxwDpq5a9ZGq4ZXW1ufvIIicfKwz0oqh99+o8UEvXDZm+URsoNW6wq32/qKO6f0cZRI3G+l6ulkLsLeELbHGdggmLBunDelZCFpTmPMkkkIJQC+O3sjiEgdkkCgYBUBxcHzomyZ/oUZNutF5qMBwzM6S7iLITPNNVAUYRHIGPebeUrTkS+kZgvfP9qLFq41kja8KdauPD1OX2FsE/Q7LpjbxsSfIkL5rdqwpICgX/DMEM9T1450BE0QipPLBR8euhCfxrUPNYiANEPrMmJOvHWgzAjBvV8M1ID1CP/VA==";

    public static void main(String[] args) {

    }

    public void test() {
        String data = "{\n" +
                "    \"requestNo\":\"123\",\n" +
                "    \"data\":\"{\\\"batchNo\\\":\\\"123\\\",\\\"fileUrl\\\":\\\"https://oss.17usoft.com/public-nova/xz8KKPBV-AgencyCP20250411000000000041044.gz\\\",\\\"fileExpirationTime\\\":\\\"2025-03-14 13:58:47\\\",\\\"startDate\\\":\\\"2025-03-14\\\",\\\"endDate\\\":\\\"2025-04-14\\\",\\\"total\\\":100}\"\n" +
                "}";
        String sign = sign(data);
    }

    public static String sign(String param) {
        JSONObject jsonObject = JSONObject.parseObject(param);
        Map<String, Object> map = new HashMap<>(jsonObject);
        String signature = generateContent(map);
        String sign = signByPrivateKey(tcPrivateKey, signature);
        return sign;
    }

    public static String test01() {
        return "test01";
    }

    public String test02() {
        return "test02";
    }

    public static String signVt(String requestNo, String timestamp, String data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("requestNo", requestNo);
        jsonObject.put("timestamp", timestamp);
        jsonObject.put("data", data);
        Map<String, Object> map = new HashMap<>(jsonObject);
        String signature = generateContent(map);
        String sign = signByPrivateKey(tcPrivateKey, signature);
        return sign;
    }

    private static String generateContent(Map<String, Object> params) {
        Map<String, Object> kvMap = new TreeMap();
        params.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (!"sign".equalsIgnoreCase(key)
                    && !"signType".equalsIgnoreCase(key)
                    && Objects.nonNull(value)
                    && StringUtils.isNotBlank(value.toString())) {
                kvMap.put(key, value);
            }
        });
        return Joiner.on("&").withKeyValueSeparator("=").join(kvMap);
    }

    private static String signByPrivateKey(String key, String data) {
        try {
            PrivateKey privateKey = getPrivateKey(key);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data.getBytes(ENCODING));
            byte[] bs = signature.sign();
            return new String(Base64.encodeBase64(bs), ENCODING);
        } catch (Exception e) {
            throw new RuntimeException("RSA sign error!", e);
        }
    }

    private static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] bs = Base64.decodeBase64(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bs);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }

}
