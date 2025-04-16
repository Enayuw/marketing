package com.br.marketing.util.tc;

import com.alibaba.fastjson.JSON;

import java.util.LinkedHashMap;
import java.util.Map;

public class HttpDemo {

    private static final String tcPrivateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCz0d7j3WGnfMIjvSspB7v2ZcmCWJ8Zqzn1gvMe99bqWOnve7V3fpTZSdZVMg7B8viJDUtrUqDY/ZSWsUojW0N374bzjYlGNZYlWmYqiZfKZZ44ABK6+dky5MDhnaTpYihlhwxiKp8P48awjyMxgqWxv7dWhSEgVFvhDkIQr9XYtQf11EL2s1N6bqkaf1uvGARAb6yDz9vh4torzow+bmfe0HejH1Qyw5C3eza+WEmhFdBFEeH4GhkLwp3wQ6mopQDpyPPXzrNJIo7zaqbaJ+s+LTj2q8ECV8hHndwSHrai0fogkadAMF9tgTD2UwHZAtvK0K8IW1RL0E8kFIamflWVAgMBAAECggEASu+H0i+clX6RLPGVPekCNIFgg1hJHRpU8fIbPOmNf2WEP4+vJNf0UcTKdACDU+HcHskSh+wMKcErHc1OFwPeTunbtD1kWoTUSEau0sU6I1dLowyswYyDLglUM/FNGxETwpOP3ozicm26jDNqOCS4xiUd0wlxr5ZYH6agc3HDTSYXG6M+Q4q8jhHs0oEpr4b4aL0oDNHnEg7mPynS/LEritzr1D06dBXbF6UTlOVB/swnvsidAEHbdzIbmL+sH+pqljnkgk1wtrkqWTDJj4apzmQIuDiRwk8yWtPHiQTxhqk0EKSTsTVie0KaEuW7NrgcaGFhSFlrkCOhx+oEpWkWoQKBgQD40s73g8BR70LcG7N9V9ITh+7IkW6BptKJr8nKCm7+xTyGh7aInwT50PJBVJVmfVub4Bf5wVD6BE1IJ/Ss9WYJPZa2pK0QqwFElGKDzxx19NdBNqsR3W8t6CaM39iokUwaDLC4YUK41IJ1h9uFsHPJg4raX/ye1d/uQijedx1TrQKBgQC5AZKcWC2xU7Gz0GNjupkhfmt+H1+jXPybwM9kz1pScZ5z0bbZ8ZuiS0VC0eI+ILLEMa5UmeSik5ZEJJHLFwzQgQukNBQeU+llRRWqSmXyabkJD3zC85hQCm2kUbLUVzexgvB7CPL1hqQT6ayMItQf9+/2jgrkRrHUalD/hgCGiQKBgFKY0AFT5/SK4vvj6ioyi9bV6csEk9VQBlWUV/zMh9nkqVnTFSG2/9TZqoFLTajO9ikBM5RButqzsN/B+7OqZmus2SnZ8mU1Dt+wDh/JEZ6KXyYTuqfchLqNdLaQ2//g8402JzedeaOXT5MqPRHc6CK9msswz8/+GS6jIaPvkHmlAoGAWfPazivNo6+28l/7Q01CEVf/eeZVQQAATtazwCdVmkpmKZgpGNTxwDpq5a9ZGq4ZXW1ufvIIicfKwz0oqh99+o8UEvXDZm+URsoNW6wq32/qKO6f0cZRI3G+l6ulkLsLeELbHGdggmLBunDelZCFpTmPMkkkIJQC+O3sjiEgdkkCgYBUBxcHzomyZ/oUZNutF5qMBwzM6S7iLITPNNVAUYRHIGPebeUrTkS+kZgvfP9qLFq41kja8KdauPD1OX2FsE/Q7LpjbxsSfIkL5rdqwpICgX/DMEM9T1450BE0QipPLBR8euhCfxrUPNYiANEPrMmJOvHWgzAjBvV8M1ID1CP/VA==";
    private static final String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs9He491hp3zCI70rKQe79mXJglifGas59YLzHvfW6ljp73u1d36U2UnWVTIOwfL4iQ1La1Kg2P2UlrFKI1tDd++G842JRjWWJVpmKomXymWeOAASuvnZMuTA4Z2k6WIoZYcMYiqfD+PGsI8jMYKlsb+3VoUhIFRb4Q5CEK/V2LUH9dRC9rNTem6pGn9brxgEQG+sg8/b4eLaK86MPm5n3tB3ox9UMsOQt3s2vlhJoRXQRRHh+BoZC8Kd8EOpqKUA6cjz186zSSKO82qm2ifrPi049qvBAlfIR53cEh62otH6IJGnQDBfbYEw9lMB2QLbytCvCFtUS9BPJBSGpn5VlQIDAQAB";

    public static void main(String[] args) {
        String requestNo = "requestNo";
        String data = "{\"batchNo\":\"12123\"}";
        // 组装标准格式的入参
        Map<String, Object> reqData = new LinkedHashMap<>();
        reqData.put("requestNo", requestNo);
        reqData.put("timestamp", String.valueOf(System.currentTimeMillis()));
        reqData.put("data", data);
        // SHA256withRSA加签
        String signature = RSAUtil.generateContent(reqData);
        String sign = RSAUtil.signByPrivateKey(tcPrivateKey, signature);
        reqData.put("sign", sign);
//        SimpleHttpClient.HttpReq httpReq = new SimpleHttpClient.HttpReq();
//        httpReq.setUrl("http://test.com");
//        httpReq.addHeader("Content-Type", "application/json");
//        httpReq.setContent(JSON.toJSONString(reqData));
//        System.out.println("请求入参：" + httpReq.getContent());
//        try (SimpleHttpClient simpleHttpClient = new SimpleHttpClient()) {
//            SimpleHttpClient.HttpResp httpResp = simpleHttpClient.doPost(httpReq);
//        }
        // 接收到验签
        Map<String, Object> respContent = JSON.parseObject(JSON.toJSONString(reqData), LinkedHashMap.class);
        String verifySignContent = RSAUtil.generateContent(respContent);
        System.out.println("同程验签：" + RSAUtil.verifySignByPublicKey(tcPublicKey, sign, verifySignContent));
    }
}