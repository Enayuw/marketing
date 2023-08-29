package com.br.marketing.client;

import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 电销数据与外呼数据
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-24 16:08
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DaasAndConversionData extends InterfaceParams {
    /**
     * 2023-08-24 16:12
     * 实时推送用户名单 对应单条接口
     */
    private RealTimeUserDataSoleDTO realTimeUserDataSoleDTO;

    /**
     * 2023-08-24 16:12
     * 客服转化
     */
    private ConversionData conversionData;
}
