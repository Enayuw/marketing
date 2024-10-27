package com.br.marketing.marketingdatarelayservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.client.qifu.enums.FlagEnum;
import com.br.marketing.client.qifu.util.RSAUtil;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.marketingdatarelayservice.client.QiFuAiBizDataDTO;
import com.br.marketing.marketingdatarelayservice.client.QiFuAiResDTO;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
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

    public Pair<CodeEnum, FlagEnum> bizHandle(String decryptData) {
        String apiCode = marketingCommonConfig.getQiFuAIUploadDataApiCode();
        String tCid = tableCreateService.getTcId(apiCode);
        DrsCustomizeUploadData uploadData = new DrsCustomizeUploadData();
        uploadData.setApiCode(apiCode);
        uploadData.setTCid(tCid);
        drsCustomizeUploadDataMapper.createDrsCustomizeUploadDataTable(uploadData.getTCid());

        QiFuAiBizDataDTO qiFuAiBizDataDTO;
        try {
            qiFuAiBizDataDTO = JSONObject.parseObject(decryptData, QiFuAiBizDataDTO.class);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), decryptData,
                    "奇富AI上传数据，JSON解析失败！！！"));

            String requestId =
                    yyyyMMdd.concat("_").concat(apiCode).concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
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
            return new Pair<>(CodeEnum.GWS200, FlagEnum.F);
        }

        try {
            uploadData.setRequestId(qiFuAiBizDataDTO.getFlowNo());
            List<QiFuAiBizDataDTO.DataList> dataList = qiFuAiBizDataDTO.getDataList();
            uploadData.setRequestJsonData(JSON.toJSONString(dataList));
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
                        "jsonData:" + decryptData), "奇富AI上传数据入库失败！！！");
                return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
            }

            return new Pair<>(CodeEnum.GWS100, FlagEnum.S);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                    "该apiCode:" + apiCode + "定制上传数据接入异常！！！，jsonData:" + decryptData), e);
            return new Pair<>(CodeEnum.GWS208, FlagEnum.F);
        }
    }

    public QiFuAiResDTO getResult(CodeEnum codeEnum, FlagEnum flagEnum) {
        JSONObject qiFuAIServerConfig = marketingCommonConfig.getQiFuAIServerConfig();

        QiFuAiResDTO qiFuAiResDTO = new QiFuAiResDTO();
        qiFuAiResDTO.setCode(codeEnum.getCode());
        qiFuAiResDTO.setMsg(codeEnum.getDesc());
        qiFuAiResDTO.setFlag(flagEnum.toString());
        System.out.println(flagEnum.toString());

        QiFuAiResDTO.DataResult dataResult = new QiFuAiResDTO.DataResult();
        dataResult.setAppId(qiFuAIServerConfig.getString("appId"));
        dataResult.setTimestamp(String.valueOf(System.currentTimeMillis()));
        // todo
        dataResult.setBizData("");

        String aesKey = RandomStringUtils.randomAlphanumeric(16);
        String iv = RandomStringUtils.randomAlphanumeric(16);
        String rsaEncryptKey = RSAUtil.encryptByPublicKey(qiFuAIServerConfig.getString("brPublicKey"), aesKey);
        String rsaEncryptIv = RSAUtil.encryptByPublicKey(qiFuAIServerConfig.getString("brPublicKey"), iv);
        dataResult.setEncryptKey(rsaEncryptKey);
        dataResult.setEncryptIV(rsaEncryptIv);

        JSONObject responseJson = JSONObject.parseObject(JSON.toJSONString(dataResult));
        String signature = RSAUtil.generateContent(responseJson);
        String sign = RSAUtil.signByPrivateKey(qiFuAIServerConfig.getString("brPrivateKey"), signature);

        dataResult.setSign(sign);
        qiFuAiResDTO.setData(dataResult);
        return qiFuAiResDTO;
    }
}
