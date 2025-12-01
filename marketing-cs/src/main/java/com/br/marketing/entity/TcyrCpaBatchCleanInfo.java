package com.br.marketing.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TcyrCpaBatchCleanInfo {

    private String batchNumber;

    private boolean isOut;

    private boolean isInner;

    private String errorMsg;
}