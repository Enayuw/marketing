package com.br.marketing.client.dassservice.input.userdata;

import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import lombok.Data;

@Data
public class DassSingleImportDataDTO extends DassImportDataDTO {

    /**
     * 拨打优先级（枚举值：1、2、3）
     */
    private String prioritySymbol;
    /**
     * 筛选项1
     */
    private String filterItem1;

    /**
     * 筛选项2
     */
    private String filterItem2;


}
