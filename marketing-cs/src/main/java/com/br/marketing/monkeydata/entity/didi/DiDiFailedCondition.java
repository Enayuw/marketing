package com.br.marketing.monkeydata.entity.didi;

import com.br.marketing.monkeydata.entity.InputDataCondition;
import lombok.Data;

@Data
public class DiDiFailedCondition extends InputDataCondition {

    private Long localId;

    private Long dataId;

    private String day;

    private Integer pageSize;
}
