package com.br.marketing.marketingdatarelayservice.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.client.qifu.util.AESUtil;
import com.br.marketing.client.qifu.util.RSAUtil;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.marketingdatarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.marketingdatarelayservice.client.QiFuAiResDTO;
import com.br.marketing.marketingdatarelayservice.service.QiFuAiUploadDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

/**
 * @Description UploadDataController
 * @Author hong.chen
 * @CreateTime 2024/10/25
 */
@Api(value = "UploadDataController")
@RequestMapping("/marketing/v1")
@RestController
@Slf4j
public class UploadDataController {
    @Resource
    private QiFuAiUploadDataService qiFuAiUploadDataService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @ApiOperation(value = "奇富AI上传数据接入接口")
    @PostMapping("/uploadData/24152")
    @LogAnnotation
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS, to = 0)
    public QiFuAiResDTO qiFuAiUploadData(@RequestBody QiFuAiReqDTO requestBody) {
        String qiFuPublicKey = marketingCommonConfig.getQiFuAIServerConfig().getString("qiFuPublicKey");
        String brPrivateKey = marketingCommonConfig.getQiFuAIServerConfig().getString("brPrivateKey");
        System.out.println("==================== 解密开始 ====================");
        String requestStr = JSON.toJSONString(requestBody);
        // 服务端1. SHA256withRSA验签
        String originSign = requestBody.getSign();
        JSONObject requestJson = JSONObject.parseObject(requestStr);
        String signAgain = RSAUtil.generateContent(requestJson);
        boolean verifyResult = RSAUtil.verifySignByPublicKey(qiFuPublicKey, originSign, signAgain);
        if (!verifyResult) {
            System.out.println("验签结果：失败！！！");
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), requestStr,
                    "奇富AI上传数据，验签失败！！！"));
            // 返回异常
            return qiFuAiUploadDataService.getResult(CodeEnum.GWS209, FlagEnum.F);
        }
        System.out.println("验签结果：成功");

        // 服务端2. RSA解密（客户端公钥加密，服务端私钥解密）AESKey和IV
        String originKey = requestBody.getEncryptKey();
        String originIv = requestBody.getEncryptIV();
        String decryptKey = RSAUtil.decryptByPrivateKey(brPrivateKey, originKey);
        String decryptIv = RSAUtil.decryptByPrivateKey(brPrivateKey, originIv);
        System.out.println("经过RSA解密后的AESKey：" + decryptKey);
        System.out.println("经过RSA解密后的IV：" + decryptIv);

        // 服务端3. AES-CBC解密业务数据
        String originData = requestBody.getBizData();
//        String decryptData = AESUtil.decrypt(decryptKey, decryptIv, originData);
        String decryptData = new String(Base64.decodeBase64(AESUtil.decrypt(decryptKey, decryptIv, originData))
                , StandardCharsets.UTF_8);
        System.out.println("经过AES解密后的业务数据：" + decryptData);

        System.out.println("==================== 解密结束 ====================");
        // ******************** 解密 结束 ********************

        // 服务端解密后，会进行相应的业务处理
        // 处理完业务后，服务端会对最终的业务响应数据进行加密（加密过程同客户端加密过程，所以客户端处理服务端的Response可以参考上述服务端解密过程）
        Pair<CodeEnum, FlagEnum> pair = qiFuAiUploadDataService.bizHandle(decryptData);
        return qiFuAiUploadDataService.getResult(pair.getKey(), pair.getValue());
    }
}
