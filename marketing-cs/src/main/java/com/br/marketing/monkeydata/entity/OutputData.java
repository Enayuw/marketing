package com.br.marketing.monkeydata.entity;

import lombok.Data;

@Data
public class OutputData<T> {
    private T data;
}
