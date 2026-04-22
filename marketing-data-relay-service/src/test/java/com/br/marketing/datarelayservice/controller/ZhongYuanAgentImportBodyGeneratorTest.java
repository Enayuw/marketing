package com.br.marketing.datarelayservice.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.zhongyuan.MtStandardRequest;
import com.br.marketing.dto.zhongyuan.MtStandardResponse;
import com.br.marketing.util.aes.AesZhongYuan;
import com.br.marketing.utils.Encodes;
import com.br.marketing.utils.RsaUtil;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

/**
 * 本地生成「坐席批量导入」可验签、可解密的完整 HTTP Body（与 ZhongYuanAgentServiceImpl 算法一致）。
 * 运行：{@code mvn -pl marketing-cs -Dtest=ZhongYuanAgentImportBodyGeneratorTest#printBody test}
 */
public class ZhongYuanAgentImportBodyGeneratorTest {

    /** 与联调配置 zhongYuanAgentChannelRsa.publicKey 一致 */
    private static final String PUB_B64 = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCXYnjPhqrkUabqjBE1Z23I5Do7XWRJnJEZndRZBVE86wKcUJhb012mTSspGKMKa0BiSMSnJsoKUuDK+6bczVxCa4ua9SGJKbPPopFsOeTaDePgqmDZlAWzRZaJZGgStX6M/aA1kbG90rKXmQgsYyoq9ejmf/p0fDh9I46/8MUSRQIDAQAB";
    /** 与联调配置 zhongYuanAgentChannelRsa.privateKey 一致 */
    private static final String PRIV_B64 = "MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAJdieM+GquRRpuqMETVnbcjkOjtdZEmckRmd1FkFUTzrApxQmFvTXaZNKykYowprQGJIxKcmygpS4Mr7ptzNXEJri5r1IYkps8+ikWw55NoN4+CqYNmUBbNFlolkaBK1foz9oDWRsb3SspeZCCxjKir16OZ/+nR8OH0jjr/wxRJFAgMBAAECgYAqFnYJGGM1pB/YcMWuB8nlgtJPw+jIcG+E5DTv5QMPgTUSGXQqf2q3fBfmTpOxp1zYlmuaHzYnxZ/6vxlGpQ+jdeYwjdgf8L0XOhxwHWRJei74FbVy+ldU6INA2aYql/ZLIM4InFg32bilA0dsj31+YHk/lSmUOOmGgCsIXCtdZwJBAJ/uLR+WWBKLOixBT/c6ihSNUq8J5PDVcBTQdEpXs+OdPE3qNtpf0MTDoMf5oyRcGYfhKUWypdvZHziJ1GGNOyMCQQDyUiYy/tknGt8U9uVj3fguqoHcb5xmEPLDSwT7Dhyn9MmQqjjg05IUsQ5aBKPbgUsK8Kk8UMQEzJnEzzouVud3AkB/weM4BYDrp17cNXxs0c22J0Ly4yOJI+e6KN+M98yTOmsPDtes0LfURzZsKUai6BPZEQxISjLmkea34prPJuQjAkBh/2cDaJ1ZezyMtey/Hp2oAbzS8TBG9sO2xzgUb/iW1CBQKQcnpWiGhbRQI2BA/WDj48ANbHxZlIT7WvMkmnCxAkAj5NPgnzgF9rroSfqRPkRWXdjRIRZmnd8MFXJQnzZI3fUr30BWWr1tHLXzAYWOqCgZFc1WlCUyKMflGaH4YFq+";

    @Test
    public void printBody() throws Exception {
        PublicKey pub = RsaUtil.getPublicKey(PUB_B64);
        PrivateKey priv = RsaUtil.getPrivateKey(PRIV_B64);
        // 固定 16 字节 AES 密钥，便于样例可复现
        byte[] aesKey = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

        String keyCipher = RsaUtil.encrypt2Base64String(aesKey, pub, null, RsaUtil.PK_CS1);
        String requestNo = "LHH20260120120000sample01";
        String timestamp = "1737360000000";
        String bizJson = "{\"templateId\":\"TPL_DEMO\",\"details\":[{\"phone\":\"13800138000\",\"jobId\":\"10000001\",\"ivrParam\":{\"userType\":\"1\",\"name\":\"小李\"}}]}";
        String requestDataCipher = AesZhongYuan.encrypt2Base64String(bizJson, aesKey, null, AesZhongYuan.ECB_ALGORITHM_PADDING);

        MtStandardRequest req = new MtStandardRequest();
        req.setRequestNo(requestNo);
        req.setTimestamp(timestamp);
        req.setKey(keyCipher);
        req.setRequestData(requestDataCipher);
        String signPlain = req.signData();
        req.setSign(signSha1Rsa(signPlain, priv));

        Assert.assertTrue(verifySha1Rsa(signPlain, req.getSign(), pub));

        System.out.println("--- copy as HTTP raw body (UTF-8) ---");
        System.out.println(JSON.toJSONString(req));
    }

    /**
     * 合作方收到 {@link MtStandardResponse} 后：RSA 私钥解 {@code key} 得 AES 密钥，再 AES 解 {@code responseData}；
     * 与 {@code ZhongYuanAgentServiceImpl} 成功回包构造方式一致（SHA1WithRSA 验签不含 responseData）。
     */
    @Test
    public void decryptSuccessResponse_roundTrip() throws Exception {
        PublicKey pub = RsaUtil.getPublicKey(PUB_B64);
        PrivateKey priv = RsaUtil.getPrivateKey(PRIV_B64);
        byte[] aesKey = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);
        String batchNo = "550e8400-e29b-41d4-a716-446655440000";
        String bizJson = "{\"batchNo\":\"" + batchNo + "\"}";

        MtStandardResponse resp = new MtStandardResponse();
        resp.setResponseNo("LHH20260120120000sample01");
        resp.setErrorCode("000000");
        resp.setErrorMsg("成功");
        resp.setKey(RsaUtil.encrypt2Base64String(aesKey, pub, null, RsaUtil.PK_CS1));
        resp.setResponseData(AesZhongYuan.encrypt2Base64String(bizJson, aesKey, null, AesZhongYuan.ECB_ALGORITHM_PADDING));
        String signPlain = resp.signData();
        resp.setSign(signSha1Rsa(signPlain, priv));

        Assert.assertTrue(verifySha1Rsa(signPlain, resp.getSign(), pub));

        byte[] aesFromKey = RsaUtil.decryptBase64Content2Byte(resp.getKey(), priv);
        String plain = AesZhongYuan.decryptBase64Content2String(resp.getResponseData(), aesFromKey, null, AesZhongYuan.ECB_ALGORITHM_PADDING);

        JSONObject obj = JSON.parseObject(plain);
        Assert.assertEquals(batchNo, obj.getString("batchNo"));
        Assert.assertEquals(1, obj.size());
    }

    /**
     * 联调抓包（与 {@link #PUB_B64}/{@link #PRIV_B64} 为同一 RSA 密钥对且按 Mt 标准签名时可通过）。
     */
    @Test
    public void decryptCapturedSuccessSample() throws Exception {
        String json = "{"
                + "\"responseNo\":\"LHH20260120120000sample01\","
                + "\"errorCode\":\"000000\","
                + "\"errorMsg\":\"成功\","
                + "\"sign\":\"EPYeAivmVWRFf2TriCrrV+NvwoKRVv86DPqrgV9z9+zPGZjYJ0OCaUWTrs3kJ4gefHe4R//EE+7LO57zUs613obzb8SjXGj1LSVPQ+LCEpThQ4qdlYS0drqKyigK2BYuPWG6M9cyzGAhf2fouppr0sIDU1i9vS0ZFYFY0KS9smg=\","
                + "\"key\":\"GwovrzKPEXS2xVCFF4m609AYQXH4C5hpnjA3vkbYbrBy4pCPid/iEnILy4LUCZPmV5QbgRlsqQNeyVN40LhsS3vRH3bL517qHYVd1SvDibLtn8r9fM/diBbJSOtlVZ3dh/+vlRxr582vnhlklg2S10eTejIEQrFegRj1pJuX1kw=\","
                + "\"responseData\":\"Z8QQs/MY761Hnch4yIvFBthqnBmFqXmVziqP3s6TTks=\""
                + "}";
        MtStandardResponse resp = JSON.parseObject(json, MtStandardResponse.class);
        PrivateKey priv = RsaUtil.getPrivateKey(PRIV_B64);
        PublicKey pub = RsaUtil.getPublicKey(PUB_B64);

        Assert.assertTrue(verifySha1Rsa(resp.signData(), resp.getSign(), pub));

        byte[] aesFromKey = RsaUtil.decryptBase64Content2Byte(resp.getKey(), priv);
        String plain = AesZhongYuan.decryptBase64Content2String(resp.getResponseData(), aesFromKey, null, AesZhongYuan.ECB_ALGORITHM_PADDING);

        JSONObject obj = JSON.parseObject(plain);
        Assert.assertNotNull(obj.getString("batchNo"));
        Assert.assertEquals(1, obj.size());
    }

    private static String signSha1Rsa(String signData, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA1WithRSA");
        signature.initSign(privateKey);
        signature.update(signData.getBytes(StandardCharsets.UTF_8));
        return Encodes.encodeBase64(signature.sign());
    }

    private static boolean verifySha1Rsa(String signData, String sign, PublicKey publicKey) throws Exception {
        Signature v = Signature.getInstance("SHA1WithRSA");
        v.initVerify(publicKey);
        v.update(signData.getBytes(StandardCharsets.UTF_8));
        return v.verify(Encodes.decodeBase64(sign));
    }
}
