package com.br.marketing.client.xiecheng.intput;

import com.br.marketing.entity.XieChengData;
import lombok.Data;

@Data
public class AdReqDTO extends XieChengData {
    private String conditionKey;
    private String mktMode;
    private String mktChannel;
    private String mktProductNo;
}
