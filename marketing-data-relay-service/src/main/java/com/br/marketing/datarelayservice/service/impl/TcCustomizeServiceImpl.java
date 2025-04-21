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
        String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA7WHvNMZTmglLbPripHENttwpIzlX3PELHxdpzS32ihH9/fj1rnTppOd4/OUwGshVuEHF54Y2kSuA0ypm/J0qsFCvC+TEJkPRQWZUiLv4RMCwVxR87PICp3BxeaZH8COLa551WbqhCkodhkLxo75Y59m+G6ySSKLVdEiUi/rek0ZEB1o/sfIyvHUMvCUw+Gz5Jla+WdG2TZeYdG13/LnrVVBf0LmJ0UVr4CoBtiZHLhI2w9xle5cAv8FbyyZFq39hwkzquQeqKwzFCwUyEifSqs5NbeLLFTn3u7bu8LG1Ej0Z0w7ZgHwdrJ1GrXF1uXtaHZXV50g7JLD/agGGG1H++wIDAQAB";
        //百融私钥加签
        String brPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC6JbKUPsahMZ/9S2ZRRc8d9sFOy+0CNyBQNurhUHOvEpONfVhx1BjE+HGZl64Cha8PObX2tD9Btx4wa03k+/AhPi7dNd38G9r+YbsRbSohzPSl9akCDaf1xiKw7m9lH/GN/Lk/vtpRHWoPJ5wu6SLfTRn+FzYqxhipp725OA3vEk7R2fB9mUXy4vW/E59hD9fuixbid1m7c1Aaejgwku+BKRfmtcLvaHsvTn3rDUSxdT4jkXMwWwIVA4M33ssi5Z+9s1VVuxsokN9g6XsT4K0M9GioBHDwnEiOshiRyM4oPFZp8Gmu5l1EJ73Sc6+4iS8q1cBW03ePELBon3i/h8knAgMBAAECggEBAKTlVg9amMwcULSpwUaHh5TsjItHvHl06ewE6gaUJRZWZ100R0/2aca6qq87nUrgr5XWMqoLO+nz1AtiUstgnnRkSFFvMWjuKA2l93fVczgj/iixuHh4Lmxai0qevREgvfNgh52/bFfkrZolJYaswVZ8T2U1nKdBeoF3dWqJDFbVJs7EMZ1ypEat0K437bU6JqxMpILQ2hSJdrmAmmm9AjBPEZiDdmxtJsQ9g9ZC5HwVcCKKAiTSHeapGNLN/Lkvr0W5rUADUy7Ek4CBOxMeX+zShMNFV2a2jp90spVoJ+4agXBoOrabCBqA+VcBMHs3tV+DhEt2L7tfBz/ohA/NDZkCgYEA5K2KuBidVfc5JxTgnNC52h3pzW0Jz+7tNngJ7v5iAjxnu92r5uOWR8GNqJHD6P3XIIHpH4r3+zRktCPvMdWN0ULATycnIrYzUqJMi3w2sJNK0wcKkJp3F8QrMpe8nKPSv01GC8m7/zRSLUZgkQgDKsD/jnaCq/WsY4nDv8Ze89sCgYEA0GNKu+mnrOXyEOWlntyfcDqqo/xq7+N28pMHrVUZo9acY8apDj3EJ8o/fqcC9bq+cQ+5p0wQiHfTcJx3l+UKaDOx0uCitgG38zXawZA5F1Z1Y8ZQ8y/2bxpw4GcMyqlyZfZ77/yAld83yhe6qlOxB8gJCdrNLSaLZca7/bn+56UCgYBBcRSIuKqWBmj5sTTSS71UGUlme3TaZ7LE6rdVCMF9iFHbZoWiTrEcGdzzR7u7+qDM8cCIQVnULts+3iW+qjGqmCK2xCqj+WZYmI+1PzfbcltwZsx0M3Avgfkmwlu8q/lMu8125CWD1DJMOJ68AoH9gzvfRjUBBw5tcehuAlP8DwKBgF+ZzcVbslL9wwnBcTPqXzLrlzFYMe8P2Zf7oAADFJo3cNPNZe1kpMLkZDDEifUV0RypbDC2Ereo0VXOUodaymV3odLuv3bkXvGy+ULn2Wk9fulhJ+4JSPM7nCE25YVsK1FfvQgiPROErmGGdVqCvqqlOJBO0uYt0rHEdKY4WBsJAoGAOb6Y+aY2YalJTfg4BgLuV2TRAmNOA5ITK2sAtPqfAIQZVT39EqJUCvc1bGaFt+EzlWXts171kcgcr7N2quZkT2L1syyP6TEwU1345brJtV4Ux/c4fBNxD8vrBeZAmEA9myl/41fFQSX5MXZzskoNG5aazFUpDnDl7F0Vhw4Q4/k=";
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
