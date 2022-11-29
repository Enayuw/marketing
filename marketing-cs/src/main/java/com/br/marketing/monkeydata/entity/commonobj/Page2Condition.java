package com.br.marketing.monkeydata.entity.commonobj;

import lombok.Data;

@Data
public class Page2Condition<T> extends PageCondition {
    private T param;
}
