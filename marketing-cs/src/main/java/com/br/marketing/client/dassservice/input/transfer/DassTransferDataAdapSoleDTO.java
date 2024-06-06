package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.dto.DataDistributeLogBase;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 调电销推转化数据入参 数据组合
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/23 17:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DassTransferDataAdapSoleDTO extends DataDistributeLogBase<DassAssembleTransferDataSoleDTO> {

    private List<PhoneSaleExtendInfo> phoneSaleExtendInfoList;

    private Long transferInfoId;

    private InterfaceHandlerEnum interfaceHandlerEnum;


}
