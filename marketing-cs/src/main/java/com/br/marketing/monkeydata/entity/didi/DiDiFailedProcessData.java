package com.br.marketing.monkeydata.entity.didi;

import lombok.Data;

@Data
public class DiDiFailedProcessData {
    Integer bad;

    public DiDiFailedProcessData(Integer bad) {
        this.bad = bad;
    }
}
