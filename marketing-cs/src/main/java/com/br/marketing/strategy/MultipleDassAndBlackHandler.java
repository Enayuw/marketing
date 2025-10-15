package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.dassservice.input.DassImportAdapDTO;
import com.br.marketing.client.dassservice.input.DassImportAdapHaluoDTO;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.client.robotaiapi.input.BlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneDTO;
import com.br.marketing.client.robotaiapi.input.ReqBlackPhoneParentDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.MultipleDassAndCustomerBlackDTO;
import com.br.marketing.entity.PhoneSaleExtendHaluo;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.Impl.PhoneSaleExtendServiceImpl;
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

    @Resource
    PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Autowired
    PhoneSaleExtendServiceImpl phoneSaleExtendService;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    JSONObject call(List<MultipleDassAndCustomerBlackDTO> transferData, ProcessHandlerContext context) {

        List<List<MultipleDassAndCustomerBlackDTO>> partition = ListUtils.partition(transferData, 50);
        for (List<MultipleDassAndCustomerBlackDTO> multipleDassAndCustomerBlackDTOS : partition) {

            for (MultipleDassAndCustomerBlackDTO multipleDassAndCustomerBlackDTO : multipleDassAndCustomerBlackDTOS) {
                Result result = phoneSaleExtendService.savePhoneExtend(multipleDassAndCustomerBlackDTO.getPhoneSaleExtendHaluo());
                if(!ResultCode.SUCCESS.getValue().equals(result.getCode())){
                    multipleDassAndCustomerBlackDTOS.remove(multipleDassAndCustomerBlackDTO);
                }
            }

            //region push dass
            DassImportAdapHaluoDTO dassImportAdapDTO = new DassImportAdapHaluoDTO();
            dassImportAdapDTO.setTransferInfoId(context.getTransferInfoId());
            List<DassImportDataDTO> dataDTOS = multipleDassAndCustomerBlackDTOS.stream().map(batchData->batchData.getDassImportAdapDTO()).collect(Collectors.toList());
            List<PhoneSaleExtendHaluo> phoneSaleExtendHaluos = multipleDassAndCustomerBlackDTOS.stream().map(batchData->batchData.getPhoneSaleExtendHaluo()).collect(Collectors.toList());
            List<BlackDetailDTO> blackLists = multipleDassAndCustomerBlackDTOS.stream().map(t -> t.getReqBlackPhoneParentDTO()).collect(Collectors.toList());
            dassImportAdapDTO.setList(dataDTOS);
            dassImportAdapDTO.setPhoneSaleExtendHaluos(phoneSaleExtendHaluos);
            methodRetryHandlerService.callDassRealTimeBatchData(dassImportAdapDTO,0);
            //endregion

            //region push customer black
            BlackPhoneDTO<BlackDetailDTO> jsondata = new BlackPhoneDTO<>();
            jsondata.setMethod("blackData");
            jsondata.setData(blackLists);
            ReqBlackPhoneDTO dto = new ReqBlackPhoneDTO();
            dto.setApiCode(context.getApiCode());
            dto.setJsonData(JSON.toJSONString(jsondata));
            ReqBlackPhoneParentDTO parentDTO = new ReqBlackPhoneParentDTO();
            parentDTO.setDto(dto);
            parentDTO.setBlackDetailDTOList(blackLists);
            parentDTO.setTransferInfoId(context.getTransferInfoId());
            Result<String> callBalckResult = methodRetryHandlerService.callCustomerBlack(parentDTO,0);
            if (!ResultCode.SUCCESS.getValue().equals(callBalckResult.getCode())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                        String.format("推送黑名单报错：%s", callBalckResult.getData())));
            }
            //endregion
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.MULTIPLE_DASSBATCH_CUSTOMERBLACK;
    }
}
