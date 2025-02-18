package com.br.marketing.client.carclue.vo;

import lombok.Data;

@Data
public class ClueResVO<T> {
    private Integer code;

    private String message;

    private T data;
}
