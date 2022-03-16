package com.br.marketing.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
@AllArgsConstructor
public enum AssembleTransferEnum {

    /**
     * 1-默认，2-需要配合syncuser处理
     */
    DEFAULT(1),WITHSYNCUSER(2);

    private Integer value;
}
