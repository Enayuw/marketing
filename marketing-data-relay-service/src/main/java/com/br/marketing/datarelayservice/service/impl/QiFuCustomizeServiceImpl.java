package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.util.AESUtil;
import com.br.marketing.client.qifu.util.RSAUtil;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.client.QiFuActuationDTO;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.datarelayservice.service.QiFuCustomizeService;
import com.br.marketing.entity.QifuActuation;
import com.br.marketing.mapper.QifuActuationMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;

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
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private QifuActuationMapper qifuActuationMapper;

    public ApiResult handle(QiFuAiReqDTO requestBody) {
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
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(), requestStr,
                        "奇富促动支上传数据，客户提供未知appId，需要和业务方反馈！！！"));
            }
            // 服务端1. SHA256withRSA验签
            String signAgain = RSAUtil.generateContent(requestJson);
            boolean verifyResult = RSAUtil.verifySignByPublicKey(qiFuPublicKey, originSign, signAgain);
            if (!verifyResult) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(), requestStr,
                        "奇富促动支上传数据，验签失败！！！"));
                // 返回异常
                return new ApiResult().fail("奇富促动支上传数据，验签失败！！！");
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
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(), e.getMessage(),
                    "奇富促动支上传数据，验签解密失败！参数：" + JSONObject.toJSONString(requestBody)), e);
            // 返回异常
            return new ApiResult().fail("奇富促动支上传数据，验签解密失败！");
        }

        // 服务端解密后，会进行相应的业务处理
        return bizHandle(decryptData);
    }

    public ApiResult bizHandle(String decryptData) {
        String apiCode = marketingCommonConfig.getQiFuActuationApiCode();
        try {

            QiFuActuationDTO qiFuActuationDTO = JSONObject.parseObject(decryptData, QiFuActuationDTO.class);
            if (qiFuActuationDTO == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(), JSONObject.toJSONString(qiFuActuationDTO),
                        "奇富促动支上传数据，参数不能为空！！！"));
                return new ApiResult().fail("奇富促动支上传数据，参数不能为空！！！");
            }
            QifuActuation qifuActuation = new QifuActuation();
            qifuActuation.setApiCode(apiCode);
            qifuActuation.setIssueMonth(qiFuActuationDTO.getIssueMonth());
            qifuActuation.setIssueDate(qiFuActuationDTO.getIssueDate());
            qifuActuation.setUserType(qiFuActuationDTO.getUserType());
            qifuActuation.setSupplier(qiFuActuationDTO.getSupplier());
            qifuActuation.setValidDate(qiFuActuationDTO.getValidDate());
            qifuActuation.setCreditUserCount(qiFuActuationDTO.getCreditUserCount());
            qifuActuation.setAppLoginUserCount(qiFuActuationDTO.getAppLoginUserCount());
            qifuActuation.setStartUserCount(qiFuActuationDTO.getStartUserCount());
            qifuActuation.setUserLoanCount(qiFuActuationDTO.getUserLoanCount());
            qifuActuation.setAppLoginRate(String.valueOf(qiFuActuationDTO.getAppLoginRate()));
            qifuActuation.setUserStartRate(String.valueOf(qiFuActuationDTO.getUserStartRate()));
            qifuActuation.setUserLoanRate(String.valueOf(qiFuActuationDTO.getUserLoanRate()));
            qifuActuation.setCreateDate(LocalDate.now().toString());
            qifuActuation.setCreateTime(new Date());
            qifuActuation.setUpdateTime(new Date());
            qifuActuation.setIsDel(1);

            int i = qifuActuationMapper.insertSelective(qifuActuation);
            if (i != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(),
                        "jsonData:" + decryptData, "奇富促动支上传数据入库失败！！！"));
                return new ApiResult().fail("奇富促动支上传数据入库失败！！");
            }

            return new ApiResult().setCode("00").setMessage("推送成功");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.QIFUCUDONGZHIREPORT_SERVICEERROR.getCode(),
                    "jsonData:" + decryptData, "该apiCode:" + apiCode + "奇富促动支定制上传数据接入异常！！！"), e);
            return new ApiResult().fail("奇富促动支定制上传数据接入异常！！");
        }
    }

}
