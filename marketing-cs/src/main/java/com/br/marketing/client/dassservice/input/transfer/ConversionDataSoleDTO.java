package com.br.marketing.client.dassservice.input.transfer;

import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.rule.SourceData;
import lombok.Data;

/**
 * 转化过滤
 *
 * @author Hua Qiang
 * @date 2024-08-27 22:15
 */

@Data
public class ConversionDataSoleDTO extends SourceData {

    /**
     * 2024-08-27 22:15
     * 转化过滤
     */
    private ConversionData conversionData;


    /**
     * 2023-08-24 17:31
     * 状态
     */
    private String status;
}
