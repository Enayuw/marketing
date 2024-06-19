package com.br.marketing.monkeydata.entity.yixin;

import com.br.marketing.monkeydata.entity.commonobj.PageCondition;
import lombok.Data;

@Data
public class YiXinCondition extends PageCondition {

    private String apiCode;
    private String requestData;
    private String synApiCode;
    private String priority;

}
