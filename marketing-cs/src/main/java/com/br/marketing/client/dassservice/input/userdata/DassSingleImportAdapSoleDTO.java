package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.dto.DataDistributeLogBase;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实时推送用户名单 组合参数
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/23 17:13
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DassSingleImportAdapSoleDTO extends DataDistributeLogBase<DassSingleImportDataDTO> {

    private DassSingleImportDataDTO dassSingleImportDataDTO;
    private Long transferInfoId;
    private String extendInfo;
    private InterfaceHandlerEnum interfaceHandlerEnum;

}
