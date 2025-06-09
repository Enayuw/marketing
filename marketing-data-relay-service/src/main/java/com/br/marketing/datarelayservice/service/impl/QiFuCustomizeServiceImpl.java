package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.client.qifu.util.AESUtil;
import com.br.marketing.client.qifu.util.RSAUtil;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.datarelayservice.service.QiFuCustomizeService;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

/**
 * @ClassName QiFuCustomizeServiceImpl
 * @Description 奇富360促动接口
 * @Author kongbx
 * @Date 2025/6/9 14:16
 */
@Service
@Slf4j
public class QiFuCustomizeServiceImpl implements QiFuCustomizeService {

    @Resource
    DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public Pair<CodeEnum, FlagEnum> handle(QiFuAiReqDTO requestBody, String bizType) {
        String decryptData;
        try {
            // 奇富侧公钥
            String qiFuPublicKey = marketingCommonConfig.getQiFuActuationServerConfig().getString("qiFuPublicKey");
            // 百融侧私钥
            String brPrivateKey = marketingCommonConfig.getQiFuActuationServerConfig().getString("brPrivateKey").replace("*", "=");
            // appId配置
            String appId = marketingCommonConfig.getQiFuActuationServerConfig().getString("appId");
            String requestStr = JSON.toJSONString(requestBody);

            String originSign = requestBody.getSign();
            JSONObject requestJson = JSONObject.parseObject(requestStr);
            if (!Objects.equals(requestJson.getString("appId"), appId)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), requestStr,
                        "奇富促动支上传数据，客户提供未知appId，需要和业务方反馈！！！"));
            }
            // 服务端1. SHA256withRSA验签
            String signAgain = RSAUtil.generateContent(requestJson);
            boolean verifyResult = RSAUtil.verifySignByPublicKey(qiFuPublicKey, originSign, signAgain);
            if (!verifyResult) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), requestStr,
                        "奇富促动支上传数据，验签失败！！！"));
                // 返回异常
                return new Pair<>(CodeEnum.GWS209, FlagEnum.F);
            }

            // 服务端2. RSA解密（客户端公钥加密，服务端私钥解密）AESKey和IV
            String originKey = requestBody.getEncryptKey();
            String originIv = requestBody.getEncryptIV();
            String decryptKey = RSAUtil.decryptByPrivateKey(brPrivateKey, originKey);
            String decryptIv = RSAUtil.decryptByPrivateKey(brPrivateKey, originIv);

            // 服务端3. AES-CBC解密业务数据
            String originData = requestBody.getBizData();
            decryptData = new String(Base64.decodeBase64(AESUtil.decrypt(decryptKey, decryptIv, originData))
                    , StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage(),
                    "奇富促动支上传数据，验签解密失败！参数：" + JSONObject.toJSONString(requestBody)), e);
            // 返回异常
            return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
        }

        // 服务端解密后，会进行相应的业务处理
        return bizHandle(decryptData, bizType);
    }

    public Pair<CodeEnum, FlagEnum> bizHandle(String decryptData, String bizType) {
        String apiCode = marketingCommonConfig.getQiFuActuationApiCode();
        try {
            String suffix = "_" + bizType;
            DrsCustomizeUploadData uploadData = new DrsCustomizeUploadData();
            uploadData.setApiCode(apiCode);
            uploadData.setTCid(suffix);
            drsCustomizeUploadDataMapper.createDrsCustomizeUploadDataTable(suffix);

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String requestId = currentDate.concat("_").concat(apiCode).concat("_")
                    .concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
            uploadData.setRequestId(requestId);
            uploadData.setRequestJsonData(decryptData);
            uploadData.setBizDataNumber(1);
            uploadData.setReceiveDate(LocalDate.now().toString());
            uploadData.setCreateTime(new Date());
            uploadData.setUpdateTime(new Date());
            uploadData.setResponseCode(CodeEnum.GWS100.getCode());
            uploadData.setResponseData(null);
            uploadData.setExtend(null);
            uploadData.setStatus(1);
            // 保存前置数据
            int i = drsCustomizeUploadDataMapper.insertSelective(uploadData);
            if (i != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "jsonData:" + decryptData + ",bizType:" + bizType, "奇富促动支上传数据入库失败！！！"));
                return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
            }

            return new Pair<>(CodeEnum.GWS100, FlagEnum.S);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "jsonData:" + decryptData, "该apiCode:" + apiCode + ",bizType:" + bizType + "奇富促动支定制上传数据接入异常！！！"), e);
            return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
        }
    }

}
