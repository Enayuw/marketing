package com.br.marketing.client.ibmpapi.outpu;

import lombok.Data;

@Data
public class TransferIbmpOutboundVO<T> extends IbmpParentVo{
    private String accessNumber;
    private T data;
}
