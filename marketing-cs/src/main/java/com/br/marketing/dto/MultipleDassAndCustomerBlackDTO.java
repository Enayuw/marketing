package com.br.marketing.dto;

import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.robotaiapi.input.BlackDetailDTO;
import com.br.marketing.entity.PhoneSaleExtendHaluo;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.InterfaceParams;
import lombok.Data;

@Data
public class MultipleDassAndCustomerBlackDTO extends InterfaceParams {

    private PhoneSaleExtendHaluo phoneSaleExtendHaluo;

    private DassImportDataDTO dassImportAdapDTO;

    private BlackDetailDTO reqBlackPhoneParentDTO;
}
