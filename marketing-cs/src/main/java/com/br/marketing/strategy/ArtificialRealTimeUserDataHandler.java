package com.br.marketing.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.DassServiceClient;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.PhoneSaleExtendShuhe;
import com.br.marketing.mapper.PhoneSaleExtendShuheMapper;
import com.br.marketing.origin.ProcessHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
/**
 * @Description : 人工实时推送用户名单(单条)处理
 * @Author : lizhen
 * @Date : Create in 2022/03/17 16:11
 */
public class ArtificialRealTimeUserDataHandler extends AbstractExternalInterfaceHandler<RealTimeUserDataDTO> {

    @Autowired
    PhoneSaleExtendShuheMapper phoneSaleExtendShuheMapper;

    @Autowired
    private DassServiceClient dassServiceClient;

    @Override
    JSONObject call(List<RealTimeUserDataDTO> transferData, ProcessHandlerContext context) {
        for (RealTimeUserDataDTO realTimeUserDataDTO : transferData) {
            PhoneSaleExtendShuhe phoneSaleExtendShuhe = realTimeUserDataDTO.getPhoneSaleExtendShuhe();
            //插入b_phone_sale_extend_shuhe
            //后续不同商户考虑抽出来
            phoneSaleExtendShuheMapper.insertSelective(phoneSaleExtendShuhe);
            //调用Dass
            DassSingleImportAdapDTO dassImportAdapDTO = realTimeUserDataDTO.getDassSingleImportAdapDTO();
            dassImportAdapDTO.setExtendInfo(phoneSaleExtendShuhe.getId().toString());
            dassImportAdapDTO.setTransferInfoId(context.getTransferInfoId());
            callDassRealTimeUserData(dassImportAdapDTO, 0);
        }
        return null;
    }

    /**
     * 调用Dass接口
     * 调用成功，将该批数据记录到数据库中以便数据对比
     *
     * @param dassImportAdapDTO
     * @return
     */
    @RetryMethod
    public Result callDassRealTimeUserData(DassSingleImportAdapDTO dassImportAdapDTO, Integer retry) {

        Result result = dassServiceClient.postRealTimeUserData(dassImportAdapDTO);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            saveBizLog(dassImportAdapDTO.getExtendInfo(), handlerEnum().getCode(), dassImportAdapDTO.getTransferInfoId());
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        log.error("调用人工实时推送用户名单失败 -- {}", JSON.toJSONString(result));
        return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA;
    }
}
