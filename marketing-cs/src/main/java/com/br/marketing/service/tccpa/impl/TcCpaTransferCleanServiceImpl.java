package com.br.marketing.service.tccpa.impl;

import com.alibaba.excel.util.CollectionUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ThreadPoolNameEnum;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingTcyrCpaTransferRecord;
import com.br.marketing.mapper.MarketingTcyrCpaTransferRecordMapper;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.common.GeneralDataCleanService;
import com.br.marketing.service.tccpa.TcCpaTransferCleanService;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutor;
import com.middleheaven.tpdynamicmetric.executor.TpDynamicExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TcCpaTransferCleanServiceImpl implements TcCpaTransferCleanService {

    private final static String TITLE = "【同程易融CPA-转化数据清洗任务】";

    @Resource
    private GeneralDataCleanService generalDataCleanService;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    private MarketingTcyrCpaTransferRecordMapper tcyrCpaTransferRecordMapper;


    @Override
    public void process(String tcyrCpaApiCode) {
        Long lastSearchId = 0L;
        TpDynamicExecutor actionPool = TpDynamicExecutorFactory.getThreadPool(
                ThreadPoolNameEnum.TCYR_CPA_TRANSFER_DEAL.getName(), 2, 2);
        try {
            while (true) {
                List<MarketingTcyrCpaTransferRecord> tcyrCpaTransferRecordList = tcyrCpaTransferRecordMapper.selectTcyrTransforRecordList(
                        tcyrCpaApiCode, 1,lastSearchId,20000);
                if (CollectionUtils.isEmpty(tcyrCpaTransferRecordList)) {
                    break;
                }
                actionPool.submit(()->
                        processList(tcyrCpaApiCode,tcyrCpaTransferRecordList)
                );
                lastSearchId = tcyrCpaTransferRecordList.get(tcyrCpaTransferRecordList.size()-1).getId();
            }
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }finally {
            actionPool.shutdownAndAwaitTermination();
        }
    }

    private void processList(String tcyrCpaApiCode, List<MarketingTcyrCpaTransferRecord> tcyrCpaTransferRecordList) {
        List<List<MarketingTcyrCpaTransferRecord>> partitionList = ListUtils.partition(tcyrCpaTransferRecordList, 1000);
        for (List<MarketingTcyrCpaTransferRecord> itemList : partitionList) {
            List<Long> idList = itemList.stream().map(MarketingTcyrCpaTransferRecord::getId).collect(Collectors.toList());
            try {
                List<JSONObject> jsonObjectList = itemList.stream().map(
                        m->JSONObject.parseObject(m.getData())).collect(Collectors.toList());
                Result transferResult = generalDataCleanService.transferClean(jsonObjectList,tcyrCpaApiCode);
                log.warn("{},调用transfer方法 code:{},isSuccess:{},msg:{}",TITLE,transferResult.getCode(),transferResult.isSuccess(),transferResult.getMessage());
                if (transferResult !=null && transferResult.isSuccess()) {
                    List<TransferDataItemDTO> transferDataItemDTOS = (List<TransferDataItemDTO>) transferResult.getData();
                    if (CollectionUtils.isEmpty(transferDataItemDTOS)) {
                        continue;
                    }
                    PushTransferDataDetailDTO dto = initTransferData(tcyrCpaApiCode,transferDataItemDTOS);
                    Result pushResult = pushInfoService.pushTransferByRetry(dto, null);
                    log.warn("{},调用push接口 code:{},isSuccess:{},msg:{}",TITLE,pushResult.getCode(),pushResult.isSuccess(),pushResult.getMessage());
                    if (pushResult!=null && pushResult.isSuccess()) {
                        tcyrCpaTransferRecordMapper.updateCleanStatus(idList,1);
                    }else {
                        tcyrCpaTransferRecordMapper.updateCleanStatus(idList,3);
                        return;
                    }
                }else {
                    tcyrCpaTransferRecordMapper.updateCleanStatus(idList,2);
                    return;
                }
            }catch (Exception e) {
                tcyrCpaTransferRecordMapper.updateCleanStatus(idList,4);
                log.error(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
            }
        }
    }

    private PushTransferDataDetailDTO initTransferData(String apiCode, List<TransferDataItemDTO> transferDataItems) {
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(transferDataItems);
        Random random = new Random();
        int randomNumber = 10000 + random.nextInt(90000);
        String requestId = apiCode+"_"+System.currentTimeMillis()+"_"+randomNumber;
        transferDataDTO.setRequestId(requestId);
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        return dto;
    }
}
