package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataSoleDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapSoleDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.PhoneSaleTransferInfo;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.PhoneSaleTransferInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 人工转化接口处理类 有去重功能
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/23 18:53
 */
@Slf4j
@Service
public class ArtificialTransferSoleHandler extends AbstractExternalInterfaceHandler<DassAssembleTransferDataSoleDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private PhoneSaleTransferInfoMapper phoneSaleTransferInfoMapper;

    @Override
    public JSONObject call(List<DassAssembleTransferDataSoleDTO> transferData, ProcessHandlerContext context) {
        /**
         * 电销转化接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferData.size();
        List<DassTransferDataDTO> datas = new ArrayList<>(pageSize);
        List<DataJoinLogDTO> logList = new ArrayList<>(pageSize);
        List<PhoneSaleTransferInfo> phoneSaleTransferInfoList = new ArrayList<>(pageSize);
        int sum = 0;
        for (DassAssembleTransferDataSoleDTO transferDatum : transferData) {
            sum++;
            DassTransferDataDTO dassTransferDataDTO = transferDatum.getDassTransferDataDTO();
            PhoneSaleTransferInfo phoneSaleTransferInfo = transferDatum.getPhoneSaleTransferInfo();
            datas.add(dassTransferDataDTO);
            Long sourceId = null;
            if (phoneSaleTransferInfo != null) {
                phoneSaleTransferInfoList.add(phoneSaleTransferInfo);
                sourceId = phoneSaleTransferInfo.getSourceId();
            }
            // 去重功能记录
            logList.add(methodRetryHandlerService.dataJoinLogFix(
                    dassTransferDataDTO
                    , DistributeTypeEnum.DAAS_TRANSFER
                    , context.getApiCode()
                    , dassTransferDataDTO.getUid()
                    , BrCipherMaker.getInstance().encode(dassTransferDataDTO.getPhone())
                    , sourceId
                    , transferDatum.getDistributeSourceTypeEnum()
                    , transferDatum.getStatus()
                    , transferDatum.getExpireEndDate()));
            if (datas.size() == pageSize || sum == totalCount) {
                DassTransferDataAdapSoleDTO dassTransferDataAdapDTO = new DassTransferDataAdapSoleDTO();
                dassTransferDataAdapDTO.setIsSole(true);
                dassTransferDataAdapDTO.setData(datas);
                dassTransferDataAdapDTO.setDetailLogList(logList);
                if (transferDatum.getSoleField() == null) {
                    // 默认手机号去重
                    dassTransferDataAdapDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
                } else {
                    dassTransferDataAdapDTO.setSoleField(transferDatum.getSoleField());
                }
                //去重范围,根据传入值赋值，默认当天去重
                if (transferDatum.getSoleType() == null) {
                    dassTransferDataAdapDTO.setSoleDay(1);
                } else {
                    dassTransferDataAdapDTO.setSoleDay(transferDatum.getSoleType());
                }
                if (phoneSaleTransferInfoList.size() > 0) {
                    phoneSaleTransferInfoMapper.insertBatch(phoneSaleTransferInfoList);
                }
                methodRetryHandlerService.callDassTransferDataSole(dassTransferDataAdapDTO, 0);
                datas = new ArrayList<>(pageSize);
                logList = new ArrayList<>(pageSize);
                phoneSaleTransferInfoList = new ArrayList<>(pageSize);
            }
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_TRANSFER_SOLE;
    }
}
