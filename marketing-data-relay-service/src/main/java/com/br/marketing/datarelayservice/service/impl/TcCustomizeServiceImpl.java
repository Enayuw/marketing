package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.service.TcCustomizeService;
import com.br.marketing.dto.tc.*;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTcyrRevokeRecordMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.mapper.MarketingTcyrTransferRecordMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * @description: 同程易融实现
 * @author hedongshuo
 * @date 2025/4/15 15:04
 **/
@Service
@Slf4j
public class TcCustomizeServiceImpl implements TcCustomizeService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Resource
    private MarketingTcyrTransferRecordMapper tcyrTransferRecordMapper;

    @Resource
    private MarketingTcyrRevokeRecordMapper tcyrRevokeRecordMapper;

    private static final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * @description 数据推送
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @author hedongshuo
     * @date 2025/4/15 15:24
     **/
    @Override
    public TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO) {
        return process(tcRequestDTO, TcDataPushDto.class, this::handleMarketDataPush);
    }

    /**
     * @description 撤销营销
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @author hedongshuo
     * @date 2025/4/16 10:20
     **/
    @Override
    public TcResponseDTO marketRevoke(TcRequestDTO tcRequestDTO) {
        return process(tcRequestDTO, TcRevokeDto.class, this::handleMarketRevoke);
    }

    /**
     * @description 转化通知
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @author hedongshuo
     * @date 2025/4/16 11:30
     **/
    @Override
    public TcResponseDTO transformNotify(TcRequestDTO tcRequestDTO) {
        return process(tcRequestDTO, TcTransformNotifyDto.class, this::handleTransformNotify);
    }

    /**
     * @description 数据推送业务处理
     * @param dataPushDto
     * @return void
     * @author hedongshuo
     * @date 2025/4/16 15:37
     **/
    private void handleMarketDataPush(TcDataPushDto dataPushDto) {
        //todo
        System.out.println("正义必胜！");
    }

    /**
     * @description 撤销营销业务处理
     * @param revokeDto
     * @return void
     * @author hedongshuo
     * @date 2025/4/16 15:37
     **/
    private void handleMarketRevoke(TcRevokeDto revokeDto) {
        //todo
    }

    /**
     * @description 转化通知业务处理
     * @param transformNotifyDto
     * @return void
     * @author hedongshuo
     * @date 2025/4/16 15:38
     **/
    private void handleTransformNotify(TcTransformNotifyDto transformNotifyDto) {
        //todo
    }


    /**
     * 业务前置校验
     * @param tcRequestDTO
     * @param clazz
     * @param businessHandler
     * @param <T>
     */
    private <T extends TcDataDto> TcResponseDTO process(TcRequestDTO tcRequestDTO, Class<T> clazz, Consumer<T> businessHandler) {
        log.warn("接收到同程易融请求数据，clazz:{}，data:{}",clazz.getName(), tcRequestDTO);
        TcResponseDTO resdto = new TcResponseDTO();
//        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        //同程公钥验签
        String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkm/N0+KZrpd4HgopcsvCOjI4zyjolxjwfUB/xvSsY0trlDJ5+HjYcEbZAHE3/XdKWZ9tx0NbXze1pTynWaiBCixW2vkMNBNN3aSvft6bhYNh9opkDdHxmy9jFr23C1iFqDrAMqCiDCmXuImH20wY414C8VolUGyqiQNdw9nS7+iN/5kjo0+iAjQ+8Prc7P2BY4HFMXJJGHezZ5i0SDfq6f4NpEh2zoy7FyR5hEDFGd0+lee0h5WSbtkiFS89PodaeA0GOPSPI1aZGudy6MZEDXxQO0X512B6PFVCb7dZzk/1hVgUC4KD5Nff37YAiMaMQwafnNq7ylEnqPFEcCyN4wIDAQAB";
        //百融私钥加签
        String brPrivateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDRhuOUZ4BDizyz6o247ZlTONZJWOrYXl/bc6Fsxw975S79VWy6D8BsDBrZxlHmX6TLoO3r22P7536ewcNaNDSRBJGAKP6RbGjzMPYYn0QQcugwS2cJDN712VJWCzU6gbl9kh0fJ64TNQXG8Dj951MDN1WfOpWzsGUcYXhCC+JtyVQHwAgw7fIyNLfY6k0bxL4y+USsFXvLNF/Ymz0Tk1mKgb3aH6cgLfBPtTpo5HT7IIUg1ojPWZDzqnnPn6Kf5uZJSUMdMIcPJSf4LeEKJULcTKokIO+SQf7p3sxenD4/Kseq0cW/JGI4ExTTee5yU/NNuDqnCH/R//23HNWJXqnjAgMBAAECggEBALVomoYVDsJsPURw4f+pB8U8QpwCtmJbY5c3iB4MQ2W7ynFgkuCNXsatvFmtEZ/qU15SmWxJ6Uli5whBLwhcht7AG9HgrHfwavWUAJ7U1jN/qXSW9ECruicV3+nLjmx3gMgtx7T/wAG1OKlDt7Rtojv4ntNb/90x7nkiEuLsM0FlKzt0XJR2eJiUp2St+xnRfUyskT4YJLLaPQKtEX5vKLnNPp82jxCJaT0yxHZ9GkGw8zClDnQPenNnU3N7w7d6IIgS9H+zsbXT7K/DxhWMMWaiBq9ZSWJY6HkYIKY8hk1M5qLakbLCzS3UmV1kPsXF9jzKEInZMhCxs+7FeIcvw6ECgYEA8mb7LcF4wA5FhZ0G0x/wTY5Xe7yfPQjaNEedk0Yfb5zbAU33mIGmW/2Zy75CktimgH7oJQovzaKUdhDjkQx89fQs7gnTuMJfxtUM1t0WqNb+i2j0HKPhe3/qauwedMq7HE6n+OXeG6DyinRUYqN/r0OOIHV1dMthzYggHWJygFkCgYEA3UfNEsXsXu1iBI+eW+jzUAzaBwouwk4sS92beuI2Y0BSqh6uNIoLaSjX9STkF8rrCjqTxdbgTU5aWm2gtGCq4SLl+TA8eBD+SX0SO7Ls6dP8hDNyX0pLyHprXz5wjmzZ6PRDel6p5voDslhMu8syz9WTR+pDs5vPaFC6XXukFJsCgYAGpYY7ofNyqLGFUWHvhg+rwLxrWyeun8CD8HbEpAaWo+Fpbr7cQqnSGekOqh/fMOuX2GL7KQVYiR2zAxGKV6JRiCl3OXPBvCquJAdfGN7XMFX4cp9G7cNwBHjkB6dqImjxBMMcUwk9DrO30irCLaOBpcOO5kmbMzxyS0o+JAXpuQKBgQClBcYXaQm1ZZ1Cv4SfEKRcH6l9tIdYmwoH7hXk180tyauceVL7lbOa0j3z8XY1lDwjHbpUwcH3hSZ25+kAfFMToX4Wj3WZKTsqvR6a7P4oB9L7GI8EJ5lKwplOp3czkFQWmgu0t+JHgk69c4KOTqTvQF0dNcUVrm5IYmQKEYo1XwKBgQDiK9l7mYqpg4xSDYGZ8M853aisjtbcy2hMM/sedo+9tw40MoNGCPvQEiD4OgTooINIAp1SB3FPlWZ8PxjLSMKIIywGzeCyH3VuC/LQwskWdxSFmt5vSVZh7HkHYw0HW5Jgskv7AA1S1GKGi0BujPo3CdHa9wl66r8pb5LUv92TuQ==";
        try {
            TcDataDto tcDataDto;
            tcDataDto = objectMapper.readValue(tcRequestDTO.getData(), clazz);
            //1.保存记录，幂等校验
            MarketingTcyrCommonRecord tcyrCommonRecord = new MarketingTcyrCommonRecord();
            if (recordSave(tcRequestDTO, tcDataDto, tcyrCommonRecord)) {
                return resdto.idempotentFail(brPrivateKey);
            }
            //2.公共必填项校验
            if (StringUtils.isNotEmpty(tcRequestDTO.validate())) {
                return resdto.outterParamsFail(brPrivateKey, tcRequestDTO.validate());
            }
            //3.验签
            if (!RSAUtil.SignVf(tcRequestDTO, tcPublicKey)) {
                return resdto.signFail(brPrivateKey);
            }
            //4.data层必填项校验
            if (StringUtils.isNotEmpty(tcDataDto.validate())) {
                return resdto.innerParamsFail(brPrivateKey, tcDataDto.validate());
            }
            //4.业务处理-流水插入幂等校验
//            businessHandler.accept((T) tcDataDto);
        } catch (Exception e) {
            return resdto.systemFail(brPrivateKey);
        }
        return resdto.success(brPrivateKey);
    }

    private Boolean recordSave(TcRequestDTO tcRequestDTO, TcDataDto tcDataDto, MarketingTcyrCommonRecord tcyrCommonRecord) {
        String requestNo = tcRequestDTO.getRequestNo();
        Boolean isExist = false;
        if(tcDataDto instanceof TcDataPushDto){
            MarketingTcyrSyncRecordExample tcyrSyncRecordExample = new MarketingTcyrSyncRecordExample();
            tcyrSyncRecordExample.createCriteria().andRequestNoEqualTo(requestNo);
            List<MarketingTcyrSyncRecord> marketingTcyrSyncRecords = tcyrSyncRecordMapper.selectByExample(tcyrSyncRecordExample);
            isExist = marketingTcyrSyncRecords.size() > 0;
        } else if (tcDataDto instanceof TcTransformNotifyDto) {
            MarketingTcyrTransferRecordExample tcyrTransferRecordExample = new MarketingTcyrTransferRecordExample();
            tcyrTransferRecordExample.createCriteria().andRequestNoEqualTo(requestNo);
            List<MarketingTcyrTransferRecord> marketingTcyrTransferRecords = tcyrTransferRecordMapper.selectByExample(tcyrTransferRecordExample);
            isExist = marketingTcyrTransferRecords.size() > 0;
        } else if (tcDataDto instanceof TcRevokeDto) {
            MarketingTcyrRevokeRecordExample tcyrRevokeRecordExample = new MarketingTcyrRevokeRecordExample();
            tcyrRevokeRecordExample.createCriteria().andRequestNoEqualTo(requestNo);
            List<MarketingTcyrRevokeRecord> marketingTcyrSyncRecords = tcyrRevokeRecordMapper.selectByExample(tcyrRevokeRecordExample);
            isExist = marketingTcyrSyncRecords.size() > 0;
        }
        if(isExist){
            requestNo = requestNo + "_" + tcRequestDTO.getTimestamp();
        }
        tcyrCommonRecord.setApiCode(marketingCommonConfig.getTcyrApiCode());
        tcyrCommonRecord.setRequestNo(requestNo);
        tcyrCommonRecord.setBatchNo(tcDataDto.getBatchNo());
        tcyrCommonRecord.setData(tcRequestDTO.getData());
        tcyrCommonRecord.setCreateTime(new Date());
        if(tcDataDto instanceof TcDataPushDto){
            MarketingTcyrSyncRecord marketingTcyrSyncRecord = new MarketingTcyrSyncRecord();
            BeanUtils.copyProperties(tcyrCommonRecord, marketingTcyrSyncRecord);
            tcyrSyncRecordMapper.insert(marketingTcyrSyncRecord);
        } else if (tcDataDto instanceof TcTransformNotifyDto) {
            MarketingTcyrTransferRecord marketingTcyrTransferRecord = new MarketingTcyrTransferRecord();
            BeanUtils.copyProperties(tcyrCommonRecord, marketingTcyrTransferRecord);
            tcyrTransferRecordMapper.insert(marketingTcyrTransferRecord);
        } else if (tcDataDto instanceof TcRevokeDto) {
            MarketingTcyrRevokeRecord marketingTcyrRevokeRecord = new MarketingTcyrRevokeRecord();
            BeanUtils.copyProperties(tcyrCommonRecord, marketingTcyrRevokeRecord);
            tcyrRevokeRecordMapper.insert(marketingTcyrRevokeRecord);
        }
        return isExist;
    }

}
