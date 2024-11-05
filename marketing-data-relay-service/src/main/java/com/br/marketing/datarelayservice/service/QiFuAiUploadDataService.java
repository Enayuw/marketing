package com.br.marketing.datarelayservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.client.qifu.util.AESUtil;
import com.br.marketing.client.qifu.util.RSAUtil;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.datarelayservice.client.QiFuAiReqDTO;
import com.br.marketing.datarelayservice.client.QiFuAiRobotReportBizDataDTO;
import com.br.marketing.datarelayservice.enums.QiFuAiBizTypeEnum;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.datarelayservice.client.QiFuAiBizDataDTO;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static com.br.common.util.DateUtils.yyyyMMdd;

/**
 * @Description UploadDataService
 * @Author hong.chen
 * @CreateTime 2024/10/26
 */
@Service
@Slf4j
public class QiFuAiUploadDataService {
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public Pair<CodeEnum, FlagEnum> handle(QiFuAiReqDTO requestBody, String bizType) {
        String decryptData;
        try {
            // 奇富侧公钥
            String qiFuPublicKey = marketingCommonConfig.getQiFuAIServerConfig().getString("qiFuPublicKey");
            // 百融侧私钥
            String brPrivateKey = marketingCommonConfig.getQiFuAIServerConfig().getString("brPrivateKey");
            // appId配置
            String appId = marketingCommonConfig.getQiFuAIServerConfig().getString("appId");
            String requestStr = JSON.toJSONString(requestBody);

            String originSign = requestBody.getSign();
            JSONObject requestJson = JSONObject.parseObject(requestStr);
            if (!Objects.equals(requestJson.get("appId"), appId)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), requestStr,
                        "奇富AI上传数据，客户提供未知appId，需要和业务方反馈！！！"));
            }
            // 服务端1. SHA256withRSA验签
            String signAgain = RSAUtil.generateContent(requestJson);
            boolean verifyResult = RSAUtil.verifySignByPublicKey(qiFuPublicKey, originSign, signAgain);
            if (!verifyResult) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), requestStr,
                        "奇富AI上传数据，验签失败！！！"));
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
                    "奇富AI上传数据，验签解密失败！！！"), e);
            // 返回异常
            return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
        }

        // 服务端解密后，会进行相应的业务处理
        return bizHandle(decryptData, bizType);
    }

    public Pair<CodeEnum, FlagEnum> bizHandle(String decryptData, String bizType) {
        String apiCode = marketingCommonConfig.getQiFuAIUploadDataApiCode();
        try {
            String suffix = "_" + bizType;
            DrsCustomizeUploadData uploadData = new DrsCustomizeUploadData();
            uploadData.setApiCode(apiCode);
            uploadData.setTCid(suffix);
            drsCustomizeUploadDataMapper.createDrsCustomizeUploadDataTable(suffix);

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String requestId = currentDate.concat("_").concat(apiCode).concat("_")
                    .concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
            Object classObject = QiFuAiBizTypeEnum.getClassObject(bizType);
            if (classObject instanceof QiFuAiBizDataDTO) {
                Pair<CodeEnum, FlagEnum> pair = uploadBiz(decryptData, bizType, uploadData, requestId);
                if (pair != null) {
                    return pair;
                }
            } else if (classObject instanceof QiFuAiRobotReportBizDataDTO) {
                Pair<CodeEnum, FlagEnum> pair = robotReportBiz(decryptData, bizType, uploadData, requestId);
                if (pair != null) {
                    return pair;
                }
            }

            return new Pair<>(CodeEnum.GWS100, FlagEnum.S);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "jsonData:" + decryptData, "该apiCode:" + apiCode + ",bizType:" + bizType + "定制上传数据接入异常！！！"), e);
            return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
        }
    }

    private Pair<CodeEnum, FlagEnum> robotReportBiz(String decryptData, String bizType, DrsCustomizeUploadData uploadData, String requestId) {
        QiFuAiRobotReportBizDataDTO qiFuAiRobotReportBizDataDTO;
        try {
            qiFuAiRobotReportBizDataDTO = JSONObject.parseObject(decryptData, QiFuAiRobotReportBizDataDTO.class);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), decryptData,
                    "奇富AI上传数据,bizType:" + bizType + "，JSON解析失败！！！"));

            uploadData.setRequestId(requestId);
            uploadData.setRequestJsonData(decryptData);
            uploadData.setBizDataNumber(0);
            uploadData.setReceiveDate(LocalDate.now().toString());
            uploadData.setCreateTime(new Date());
            uploadData.setUpdateTime(new Date());
            uploadData.setResponseCode(CodeEnum.GWS200.getCode());
            uploadData.setResponseData(null);
            uploadData.setExtend("JSON解析失败");
            uploadData.setStatus(0);
            // 保存前置数据
            int i = drsCustomizeUploadDataMapper.insertSelective(uploadData);
            if (i != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "jsonData:" + decryptData + ",bizType:" + bizType, "奇富AI上传数据入库失败！！！"));
            }
            return new Pair<>(CodeEnum.GWS200, FlagEnum.F);
        }

        uploadData.setRequestId(requestId);
        List<QiFuAiRobotReportBizDataDTO.BillReportList> dataList = qiFuAiRobotReportBizDataDTO.getBillReportList();
        uploadData.setRequestJsonData(decryptData);
        uploadData.setBizDataNumber(dataList == null ? 0 : dataList.size());
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
                    "jsonData:" + decryptData + ",bizType:" + bizType, "奇富AI上传数据入库失败！！！"));
            return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
        }
        return null;
    }

    private Pair<CodeEnum, FlagEnum> uploadBiz(String decryptData, String bizType, DrsCustomizeUploadData uploadData, String requestId) {
        QiFuAiBizDataDTO qiFuAiBizDataDTO;
        try {
            qiFuAiBizDataDTO = JSONObject.parseObject(decryptData, QiFuAiBizDataDTO.class);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), decryptData,
                    "奇富AI上传数据,bizType:" + bizType + "，JSON解析失败！！！"));

            uploadData.setRequestId(requestId);
            uploadData.setRequestJsonData(decryptData);
            uploadData.setBizDataNumber(0);
            uploadData.setReceiveDate(LocalDate.now().toString());
            uploadData.setCreateTime(new Date());
            uploadData.setUpdateTime(new Date());
            uploadData.setResponseCode(CodeEnum.GWS200.getCode());
            uploadData.setResponseData(null);
            uploadData.setExtend("JSON解析失败");
            uploadData.setStatus(0);
            // 保存前置数据
            int i = drsCustomizeUploadDataMapper.insertSelective(uploadData);
            if (i != 1) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        "jsonData:" + decryptData + ",bizType:" + bizType, "奇富AI上传数据入库失败！！！"));
            }
            return new Pair<>(CodeEnum.GWS200, FlagEnum.F);
        }

        uploadData.setRequestId(qiFuAiBizDataDTO.getFlowNo());
        List<QiFuAiBizDataDTO.DataList> dataList = qiFuAiBizDataDTO.getDataList();
        uploadData.setRequestJsonData(decryptData);
        uploadData.setBizDataNumber(dataList == null ? 0 : dataList.size());
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
                    "jsonData:" + decryptData + ",bizType:" + bizType, "奇富AI上传数据入库失败！！！"));
            return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
        }
        return null;
    }
}
