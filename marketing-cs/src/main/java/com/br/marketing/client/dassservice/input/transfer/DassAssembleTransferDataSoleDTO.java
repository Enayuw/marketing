package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.entity.PhoneSaleTransferInfo;
import com.br.marketing.rule.SourceData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 调电销推转化数据入参
 *
 * @author zeqiang.guo
 * @dateTime 2023/08/23 17:13
 */

@Data
@EqualsAndHashCode(callSuper = true)
public class DassAssembleTransferDataSoleDTO extends SourceData {

    /**
     * 调用电销转化入参
     */
    private DassTransferDataDTO dassTransferDataDTO;


    /**
     * 2022-8-10 10:53:00
     * 电销转化信息
     * 插入b_phone_sale_transfer_info表入参
     */
    private PhoneSaleTransferInfo phoneSaleTransferInfo;

    /**
     * 2023-08-24 17:31
     * 状态
     */
    private String status;


    public DassAssembleTransferDataSoleDTO() {
    }

    public DassAssembleTransferDataSoleDTO(DassTransferDataDTO dassTransferDataDTO
            , PhoneSaleTransferInfo phoneSaleTransferInfo) {
        this.dassTransferDataDTO = dassTransferDataDTO;
        this.phoneSaleTransferInfo = phoneSaleTransferInfo;
    }
}
