package com.br.marketing.monkeydata.entity.didi;

import com.br.marketing.monkeydata.entity.InputDataCondition;
import lombok.Data;

@Data
public class DiDiAllowCondition extends InputDataCondition {

    private Long localId;

    private Long dataId;

    private Integer pageSize;
}
