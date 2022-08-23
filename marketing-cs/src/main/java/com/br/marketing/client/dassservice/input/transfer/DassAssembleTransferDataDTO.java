package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleTransferInfo;
import com.br.marketing.enums.PhoneSaleTransferDataTypeEnum;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

/**
 *
 * @Description :调电销推转化数据入参
 * ---------------------------------
 * @Author : lizhen
 * @Date : Create in 2022/4/21 16:39
 */

@Data
public class DassAssembleTransferDataDTO extends InterfaceParams {

    /**
     * 调用电销转化入参
     */
    private DassTransferDataDTO dassTransferDataDTO;

    /**
     * 插入b_phone_sale_extend_info表入参
     * 属性已弃用
     */
    @Deprecated
    private PhoneSaleExtendInfo phoneSaleExtendInfo;

    /**
     * 2022-8-10 10:53:00
     * 电销转化信息
     * 插入b_phone_sale_transfer_info表入参
     */
    private PhoneSaleTransferInfo phoneSaleTransferInfo;

    @Deprecated
    public void setPhoneSaleExtendInfo(PhoneSaleExtendInfo phoneSaleExtendInfo) {
        this.phoneSaleExtendInfo = phoneSaleExtendInfo;
        if (phoneSaleExtendInfo == null) {
            return;
        }
        this.phoneSaleTransferInfo = new PhoneSaleTransferInfo();
        this.phoneSaleTransferInfo.setApiCode(phoneSaleExtendInfo.getApiCode());
        this.phoneSaleTransferInfo.setCustNum(phoneSaleExtendInfo.getCustNum());
        this.phoneSaleTransferInfo.setUserType(phoneSaleExtendInfo.getUserType());
        this.phoneSaleTransferInfo.setAppletDate(phoneSaleExtendInfo.getAppletDate());
        this.phoneSaleTransferInfo.setDataType(PhoneSaleTransferDataTypeEnum.TRANSFER.getValue());
        this.phoneSaleTransferInfo.setCreateTime(phoneSaleExtendInfo.getCreateTime());
        this.phoneSaleTransferInfo.setUpdateTime(phoneSaleExtendInfo.getUpdateTime());
        this.phoneSaleTransferInfo.setSourceId(phoneSaleExtendInfo.getSourceId());
        if (this.dassTransferDataDTO == null) {
            return;
        }
        this.phoneSaleTransferInfo.setTransformStatus(this.dassTransferDataDTO.getTransformStatus());
        this.phoneSaleTransferInfo.setOrgName(this.dassTransferDataDTO.getOrgName());
    }

    public DassAssembleTransferDataDTO() {
    }

    public DassAssembleTransferDataDTO(DassTransferDataDTO dassTransferDataDTO
            , PhoneSaleTransferInfo phoneSaleTransferInfo) {
        this.dassTransferDataDTO = dassTransferDataDTO;
        this.phoneSaleTransferInfo = phoneSaleTransferInfo;
    }
}
