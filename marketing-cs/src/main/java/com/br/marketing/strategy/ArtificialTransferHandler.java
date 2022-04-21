package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassAssembleTransferDataDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataAdapDTO;
import com.br.marketing.client.dassservice.input.transfer.DassTransferDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 人工转化接口处理类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/2/28 18:11
 */
@Slf4j
@Service
public class ArtificialTransferHandler extends AbstractExternalInterfaceHandler<DassAssembleTransferDataDTO>{

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Override
    public JSONObject call(List<DassAssembleTransferDataDTO> transferData, ProcessHandlerContext context) {
        /**
         * 电销转化接口 每500条数据一个批次
         */
        int pageSize = 500;
        int totalCount = transferData.size();
        int pageCount = totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
        for (int i = 1; i <= pageCount; i++) {
            List<DassAssembleTransferDataDTO> subList = new ArrayList<>();
            if (i == pageCount) {
                subList = transferData.subList((i - 1) * pageSize, totalCount);
            } else {
                subList = transferData.subList((i - 1) * pageSize, pageSize * (i));
            }
            DassTransferDataAdapDTO dassTransferDataAdapDTO = new DassTransferDataAdapDTO();
            dassTransferDataAdapDTO.setTransferInfoId(context.getTransferInfoId());

            List<DassTransferDataDTO> dataDTOS = subList.stream().map(batchData->batchData.getDassTransferDataDTO()).collect(Collectors.toList());
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos = subList.stream().map(batchData->batchData.getPhoneSaleExtendInfo()).collect(Collectors.toList());
            dassTransferDataAdapDTO.setDassTransferDataDTOList(dataDTOS);
            dassTransferDataAdapDTO.setPhoneSaleExtendInfoList(phoneSaleExtendInfos);
            phoneSaleExtendInfoMapper.saveBatch(dassTransferDataAdapDTO.getPhoneSaleExtendInfoList());
            dassTransferDataAdapDTO.setPhoneSaleExtendInfoList(null);
            methodRetryHandlerService.callDassTransferData(dassTransferDataAdapDTO,0);
        }
        return null;
    }

    @Override
    public InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_TRANSFER;
    }
}
