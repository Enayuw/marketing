package com.br.marketing.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TcyrCpaBatchCleanInfo {

    private String batchNumber;

    private boolean isOut;

    private boolean isInner;

    private String errorMsg;

    public TcyrCpaBatchCleanInfo(String batchNumber, boolean isOut, boolean isInner, String errorMsg) {
        this.batchNumber = batchNumber;
        this.isOut = isOut;
        this.isInner = isInner;
        this.errorMsg = errorMsg;
    }
}