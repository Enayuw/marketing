package com.br.marketing.monkeydata.entity.commonobj;

import com.br.marketing.monkeydata.entity.InputDataCondition;
import lombok.Data;

@Data
public class PageCondition extends InputDataCondition {
    private Integer pageIndex;
    private Integer pageSize;
}
