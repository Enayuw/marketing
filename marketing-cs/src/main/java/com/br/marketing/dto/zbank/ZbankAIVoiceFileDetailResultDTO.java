package com.br.marketing.dto.zbank;

import com.br.marketing.client.zbank.ZbankResult;
import lombok.Data;

@Data
public class ZbankAIVoiceFileDetailResultDTO extends ZbankResult {

    /**
     * 错误码
     */
    private String RetCd;
    /**
     * 错误描述
     */
    private String RetInf;


}
