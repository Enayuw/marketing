package com.br.marketing.strategy;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.es.util.BrCipherMaker;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Date;
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
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Override
    public JSONObject call(List<RealTimeUserDataSoleDTO> transferData, ProcessHandlerContext context) {
        for (RealTimeUserDataSoleDTO realTimeUserDataDTO : transferData) {
            Date date = new Date();
            PhoneSaleExtendInfo phoneSaleExtendInfo = realTimeUserDataDTO.getPhoneSaleExtendInfo();
            //调用Dass
            DassSingleImportAdapSoleDTO dassImportAdapDTO = realTimeUserDataDTO.getDassSingleImportAdapDTO();
            dassImportAdapDTO.setExtendInfo(phoneSaleExtendInfo == null ? null : phoneSaleExtendInfo.getId().toString());
            dassImportAdapDTO.setTransferInfoId(context.getTransferInfoId());
            // 组装去重内容，如果内容去重
            makeDistribute(dassImportAdapDTO, phoneSaleExtendInfo, realTimeUserDataDTO, context.getApiCode());
            try {
                methodRetryHandlerService.callDassRealTimeUserDataSole(dassImportAdapDTO, 0);
            } catch (Exception ignored) {
            }
            if (CollectionUtils.isEmpty(dassImportAdapDTO.getData())) {
                return null;
            }
            //插入b_phone_sale_extend_info
            if (phoneSaleExtendInfo != null) {
                phoneSaleExtendInfo.setCreateTime(date);
                phoneSaleExtendInfoMapper.insertSelective(phoneSaleExtendInfo);
            }
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
        // 去重功能记录
        dassImportAdapDTO.setDetailLogList(Collections.singletonList(methodRetryHandlerService.dataJoinLogFix(
                dassSingleImportDataDTO
                , DistributeTypeEnum.DAAS_REAL_TIME_USER_ONE
                , apiCode
                , dassSingleImportDataDTO.getUid()
                , BrCipherMaker.getInstance().encode(dassSingleImportDataDTO.getPhone())
                , phoneSaleExtendInfo == null ? null : phoneSaleExtendInfo.getSourceId()
                , realTimeUserDataDTO.getDistributeSourceTypeEnum()
                , phoneSaleExtendInfo == null ? null : phoneSaleExtendInfo.getStatus()
                , dassSingleImportDataDTO.getExtend())));
        dassImportAdapDTO.setIsSole(true);
        dassImportAdapDTO.setSoleField(realTimeUserDataDTO.getSoleField());
        dassImportAdapDTO.setSoleDay(realTimeUserDataDTO.getSoleType());
        dassImportAdapDTO.setData(Collections.singletonList(dassSingleImportDataDTO));
    }
}
