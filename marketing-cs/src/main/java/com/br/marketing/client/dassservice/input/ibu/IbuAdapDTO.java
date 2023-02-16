package com.br.marketing.client.dassservice.input.ibu;

import com.br.marketing.client.dassservice.input.IbuReqDTO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

import java.util.List;

/**
 * @Description : 调用ibu人工定制接口 入参
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2023/02/15 15:35
 */
@Data
public class IbuAdapDTO extends InterfaceParams {

    /**
     * 调用ibu人工定制接口 入参
     */
    private IbuReqDTO.Datum datum;
    /**
     * 电销扩展表 入参
     */
    private PhoneSaleExtendInfo phoneSaleExtendInfo;
    /**
     * 调用客服转化接口 入参
     */
    private ConversionData conversionData;

}
