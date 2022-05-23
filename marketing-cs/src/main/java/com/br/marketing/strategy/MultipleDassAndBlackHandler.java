package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.MultipleDassAndCustomerBlackDTO;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
/**
 * dass和客服黑名单接口
 */
public class MultipleDassAndBlackHandler extends AbstractExternalInterfaceHandler<MultipleDassAndCustomerBlackDTO> {

    @Autowired
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    JSONObject call(List<MultipleDassAndCustomerBlackDTO> transferData, ProcessHandlerContext context) {
        List<List<MultipleDassAndCustomerBlackDTO>> partition = ListUtils.partition(transferData, 50);
        for (List<MultipleDassAndCustomerBlackDTO> multipleDassAndCustomerBlackDTOS : partition) {
            DassImportAdapDTO dassImportAdapDTO = new DassImportAdapDTO();
            dassImportAdapDTO.setTransferInfoId(context.getTransferInfoId());

            List<DassImportDataDTO> dataDTOS = multipleDassAndCustomerBlackDTOS.stream().map(batchData->batchData.getDassImportAdapDTO()).collect(Collectors.toList());
            List<PhoneSaleExtendInfo> phoneSaleExtendInfos = multipleDassAndCustomerBlackDTOS.stream().map(batchData->batchData.getPhoneSaleExtendInfo()).collect(Collectors.toList());
            dassImportAdapDTO.setList(dataDTOS);
            dassImportAdapDTO.setPhoneSaleExtendInfos(phoneSaleExtendInfos);
            phoneSaleExtendInfoMapper.saveBatch(dassImportAdapDTO.getPhoneSaleExtendInfos());
            methodRetryHandlerService.callDassRealTimeBatchData(dassImportAdapDTO,0);


        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA;
    }
}
