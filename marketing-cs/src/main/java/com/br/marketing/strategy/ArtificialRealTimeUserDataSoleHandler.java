package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.es.util.BrCipherMaker;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 人工实时推送用户名单(单条)处理 有去重功能
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/23 17:13
 */
@Service
public class ArtificialRealTimeUserDataSoleHandler extends AbstractExternalInterfaceHandler<RealTimeUserDataSoleDTO> {

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<RealTimeUserDataSoleDTO> transferData, ProcessHandlerContext context) {
        for (RealTimeUserDataSoleDTO realTimeUserDataDTO : transferData) {
            PhoneSaleExtendInfo phoneSaleExtendInfo = realTimeUserDataDTO.getPhoneSaleExtendInfo();
            //调用Dass
            DassSingleImportAdapSoleDTO dassImportAdapDTO = realTimeUserDataDTO.getDassSingleImportAdapDTO();
            dassImportAdapDTO.setTransferInfoId(context.getTransferInfoId());
            // 组装去重内容
            makeDistribute(dassImportAdapDTO, phoneSaleExtendInfo, realTimeUserDataDTO, context.getApiCode());
            methodRetryHandlerService.callDassRealTimeUserDataSole(dassImportAdapDTO, 0, phoneSaleExtendInfo);
        }
        return null;
    }

    @Override
    InterfaceHandlerEnum handlerEnum() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_SOLE;
    }

    /**
     * 2023-08-24 13:19
     * 组装去重内容
     */
    private void makeDistribute(DassSingleImportAdapSoleDTO dassImportAdapDTO
            , PhoneSaleExtendInfo phoneSaleExtendInfo
            , RealTimeUserDataSoleDTO realTimeUserDataDTO, String apiCode) {
        DassSingleImportDataDTO dassSingleImportDataDTO = dassImportAdapDTO.getDassSingleImportDataDTO();
        if (realTimeUserDataDTO.getSoleField() == null) {
            // 默认手机号+状态去重
            dassImportAdapDTO.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
        } else {
            dassImportAdapDTO.setSoleField(realTimeUserDataDTO.getSoleField());
        }
        //去重范围,根据传入值赋值，默认当天去重
        if (realTimeUserDataDTO.getSoleType() == null) {
            dassImportAdapDTO.setSoleDay(1);
        } else {
            dassImportAdapDTO.setSoleDay(realTimeUserDataDTO.getSoleType());
        }
        List<DataJoinLogDTO> logDTOList = new ArrayList<>();
        logDTOList.add(methodRetryHandlerService.dataJoinLogFix(
                dassSingleImportDataDTO
                , DistributeTypeEnum.DAAS_REAL_TIME_USER_ONE
                , apiCode
                , dassSingleImportDataDTO.getUid()
                , BrCipherMaker.getInstance().encode(dassSingleImportDataDTO.getPhone())
                , phoneSaleExtendInfo == null ? null : phoneSaleExtendInfo.getSourceId()
                , realTimeUserDataDTO.getDistributeSourceTypeEnum() == null
                        ? DistributeSourceTypeEnum.TRANSFER : realTimeUserDataDTO.getDistributeSourceTypeEnum()
                , phoneSaleExtendInfo == null ? null : phoneSaleExtendInfo.getStatus()
                , dassSingleImportDataDTO.getExtend()));
        // 去重功能记录
        dassImportAdapDTO.setDetailLogList(logDTOList);
        dassImportAdapDTO.setIsSole(true);
        List<DassSingleImportDataDTO> list = new ArrayList<>();
        list.add(dassSingleImportDataDTO);
        dassImportAdapDTO.setData(list);
    }
}
