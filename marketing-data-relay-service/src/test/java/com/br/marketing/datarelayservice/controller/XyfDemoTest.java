package com.br.marketing.datarelayservice.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.datarelayservice.demo.EncryptionBody;
import com.br.marketing.datarelayservice.demo.XyfDemo;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.*;

/**
 * XyfDemo 测试类
 */
public class XyfDemoTest {

    @Test
    public void testEncryptAndSign_returnsNonNullBody() {
        JSONObject json = new JSONObject();
        json.put("param", "1");
        json.put("param2", "真好");
        json.put("param3", "abc");

        EncryptionBody result = XyfDemo.encryptAndSign(json);

        assertNotNull(result);
        assertNotNull(result.getAesKey());
        assertFalse(result.getAesKey().isEmpty());
        assertNotNull(result.getData());
        assertFalse(result.getData().isEmpty());
        assertNotNull(result.getSign());
        assertFalse(result.getSign().isEmpty());
    }

    @Test
    public void testEncryptAndSign_differentInput_producesDifferentCipher() {
        JSONObject json1 = new JSONObject();
        json1.put("a", "1");
        JSONObject json2 = new JSONObject();
        json2.put("a", "2");

        EncryptionBody result1 = XyfDemo.encryptAndSign(json1);
        EncryptionBody result2 = XyfDemo.encryptAndSign(json2);

        assertNotNull(result1.getData());
        assertNotNull(result2.getData());
        assertNotEquals(result1.getData(), result2.getData());
        assertNotEquals(result1.getAesKey(), result2.getAesKey());
    }

    @Test
    public void testEncryptAndSign_decryptAndCheckSign_roundTrip() throws Exception {
        JSONObject json = new JSONObject();
        json.put("param", "1");
        json.put("param2", "真好");
        json.put("param3", "abc");
        String originalJsonString = JSON.toJSONString(json);

        EncryptionBody encrypted = XyfDemo.encryptAndSign(json);
        String decrypted = invokeDecryptAndCheckSign(encrypted);

        assertNotNull(decrypted);
        JSONObject decryptedJson = JSONObject.parseObject(decrypted);
        assertEquals(json.getString("param"), decryptedJson.getString("param"));
        assertEquals(json.getString("param2"), decryptedJson.getString("param2"));
        assertEquals(json.getString("param3"), decryptedJson.getString("param3"));
    }

    /**
     * 通过反射调用 private 方法 decryptAndCheckSign
     */
    private String invokeDecryptAndCheckSign(EncryptionBody encryptionBody) throws Exception {
        Method method = XyfDemo.class.getDeclaredMethod("decryptAndCheckSign", EncryptionBody.class);
        method.setAccessible(true);
        return (String) method.invoke(null, encryptionBody);
    }
}
