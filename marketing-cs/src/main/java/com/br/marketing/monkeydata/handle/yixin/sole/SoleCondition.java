package com.br.marketing.monkeydata.handle.yixin.sole;

import com.br.marketing.monkeydata.entity.commonobj.PageCondition;
import lombok.Data;

@Data
public class SoleCondition extends PageCondition {

    private String apiCode;
    private Integer distributeType;
    private Integer soleDay;

}
