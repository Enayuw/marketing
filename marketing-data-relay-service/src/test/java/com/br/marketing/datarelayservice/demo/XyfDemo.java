package com.br.marketing.datarelayservice.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.util.xyf.AESUtils;
import com.br.marketing.util.xyf.RSAUtils;

public class XyfDemo {

    private static final String ssPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjQuGH0k3C47Hc95BkCbEVI5dfb1GFKGElXgon6VtaMDPeJNw3ayPx+5jYLBIOed+kP362KEGekol/RGorgc2HMoR7XJUuvLtPbOd0TSkruSPQMgmgApgbUkejOIJY3SFCpdATG2eT2V3nGeaJxqsSuUdiMwK0LO732w4DqhK8zoQqT48ty0B9dZqENqNDYGqsy/xEcTBo4RL6jvybipM0nDtzlL01DtwlNeEuKCIdMyjj+nZNdQJPfln3fEmc735nw0txdxNqafMM6y1BvDVAvb+ZfgWs2naaa+E8xSe2gg85hrjMMaEHwhp9yV6Yhl+SSOBIPDSHVYFeGrIg2ipLQIDAQAB";
    private static final String ssXfPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCNC4YfSTcLjsdz3kGQJsRUjl19vUYUoYSVeCifpW1owM94k3DdrI/H7mNgsEg5536Q/frYoQZ6SiX9EaiuBzYcyhHtclS68u09s53RNKSu5I9AyCaACmBtSR6M4gljdIUKl0BMbZ5PZXecZ5onGqxK5R2IzArQs7vfbDgOqErzOhCpPjy3LQH11moQ2o0NgaqzL/ERxMGjhEvqO/JuKkzScO3OUvTUO3CU14S4oIh0zKOP6dk11Ak9+Wfd8SZzvfmfDS3F3E2pp8wzrLUG8NUC9v5l+Bazadppr4TzFJ7aCDzmGuMwxoQfCGn3JXpiGX5JI4Eg8NIdVgV4asiDaKktAgMBAAECggEAHz6sSYX4kbDhckOMFpjf6TNYdqBVRAC71Di4FxWYdRoxW66d1B2EBHBEx4iFl342xXvd89tIChMe8z7UNSkXzazd59Hp/zgNYhvGHDAjQzMSoPE1xiuGTO9juyXfmo6XYk87STMHXONHafhxZ7Q68qRqtIEu7CjEt6vLh0O0xsiBo4S0FP81NoXBFp7F9lO62goWy3BjBjwqLGRgE9hbS82GLiZqI5hbjMVYU2fXe8PGvQwvQMTiJFKqAnNoKcUsaiR08TivKUxQlbzjECDSq46oyi2bWXesHufZNwk3NyzlvASoeqRgR0bztjaCYTPNKHkCbpY0HJS5WS6PalkI6QKBgQDwGjkNAuhuikwz6NZBeEV6hFoyHdCfoVTs4U9VtSFiYhlAAncO+h5oyHwzb1Z1ecIfNM1CQq0oTpTkNVmNV4kYG+tDZv90zwzuLG+ZqniQfsEmgdvnoWEyfUQvjGfzLJXWdsMHRXPlu1Yao0XJXDMrTtxz6PDwjOeMPTK7/CnHYwKBgQCWYkLN2ECWSAtvYylHlBOlab+wiYiYz4VzIIIHQlrs+afyJg9z1cRQmpRuFfk6PT/d0AoRxDWsrx9r1X8uy8Z1QYV0Mq4lvD/quSr3Ovf0LClaU8rVDR0eKYBit1tXazbaqEqyJAQrbTJhbyGGEs3XwF3sdpYjvuXsM7VyQZ8aLwKBgHhtPqY3Ciu/c/8pTqWh1H9h+PdgGu6l9bmmPKuJYQg+mdmQIvVBIJV/0UPfGSds/Z+3VShh1wX7fBSGabdJ63pLLpItC/4jh6HCaetSJIyc9vYAdjLUr7KqP9P1gYHnn/Pkt2djP4E/mC1eLTeU5hlXwZzlfku48L5XR6YgEThvAoGALeP4XNZ0BygfDTOmYihmwmB91YJyw6b4I0S1ST/PfWdF/9freT6vdKVnTBy6fDI6rhaRLBDDDFo4bXCuKfqXKir/bZEVoYBLxPYSjXVaOxHK4LyXNeKVkyG5AubPIeMezyTx/mCNQS9GY4NFKKPOWttiDgm2EVxsz2KMKTyrL5MCgYEAmw4Nrhvz4i9SfiVAVq2LcF/BpvgELANtzHw7T77yKE2Nm+feFBk14vBBZNc5VptUMZ2GSKMGA5A4gDNf4a0UKKDGMF08UO8o3VxnXwly2OsJYlcCVRYDCheJPaiYcw2G6Nnh2o2pH+NZDs2QG6xSWU/1oIrnvvYrt7ZqtycV39U=";

    /**
     * 加密加签
     *
     * @param object
     * @return
     */
    public static EncryptionBody encryptAndSign(Object object) {
        JSONObject paramMap = (JSONObject) JSON.toJSON(object);
        //body中文转unicode
        String body = com.br.marketing.util.xyf.Utils.toUnicode(JSON.toJSONString(paramMap));
        //生成AES密钥
        String key = AESUtils.generateAESKey();
        //加密AES密钥
        String aesKey = RSAUtils.encryptByPublicKey(key, ssPublicKey);
        //加密业务信息
        String data = AESUtils.encrypt(body, key, false);
        //生成签名
        String sign = RSAUtils.signByPrivateKey(body, ssXfPrivateKey);
        EncryptionBody encryptionBody = new EncryptionBody();
        encryptionBody.setAesKey(aesKey);
        encryptionBody.setData(data);
        encryptionBody.setSign(sign);
        return encryptionBody;
    }

    private static String decryptAndCheckSign(EncryptionBody encryptionBody) {
        String aesKey = RSAUtils.decryptByPrivateKey(encryptionBody.getAesKey(), ssXfPrivateKey);
        String data = AESUtils.decrypt(encryptionBody.getData(), aesKey, false);
        boolean result = RSAUtils.verifySignByPublicKey(data, encryptionBody.getSign(), ssPublicKey);
        if (!result) {
            // 验签失败

        }
        //中文
        return com.br.marketing.util.xyf.Utils.toChinese(data);
    }

    public static void main(String[] args) {
        JSONObject json = new JSONObject();
        json.put("param", "1");
        json.put("param2", "真好");
        json.put("param3", "abc");
        // 加密
        EncryptionBody encryptionBody = encryptAndSign(json);
        System.out.println("加密：" + JSONObject.toJSONString(encryptionBody));
        // 解密
        System.out.println("解密：" + decryptAndCheckSign(encryptionBody));
    }
}



