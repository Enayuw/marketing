package com.br.marketing.monkeydata.entity.commonobj;

import com.br.marketing.monkeydata.entity.InputDataCondition;
import lombok.Data;

@Data
public class InitByRequestIdDTO extends InputDataCondition {
    private String apiCode;
    private Long requestId;
}
