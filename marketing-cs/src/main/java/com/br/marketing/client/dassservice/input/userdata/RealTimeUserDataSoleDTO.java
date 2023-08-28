package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.SourceData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实时推送用户名单 接口入参
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/23 17:13
 */
@Data
@EqualsAndHashCode
public class RealTimeUserDataSoleDTO extends SourceData {

    /**
     * 调用Dass 入参
     */
    private DassSingleImportAdapSoleDTO dassSingleImportAdapDTO;

    /**
     * 插入b_phone_sale_extend_info表入参
     */
    private PhoneSaleExtendInfo phoneSaleExtendInfo;

    /**
     * 2023-08-23 21:28
     * 数据来源,默认转化数据 TRANSFER
     */
    private DistributeSourceTypeEnum distributeSourceTypeEnum;

}
