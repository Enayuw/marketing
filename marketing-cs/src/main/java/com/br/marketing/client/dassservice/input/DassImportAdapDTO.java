package com.br.marketing.client.dassservice.input;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import lombok.Data;

import java.util.List;

@Data
public class DassImportAdapDTO {
    String  interfaceExtendInfo;
    /**
     * 批量人工推
     */
    List<DassImportDataDTO> list;

    private Long transferInfoId;

    /**
     * 插入b_phone_sale_extend_info
     */
    private List<PhoneSaleExtendInfo> phoneSaleExtendInfos;

}
