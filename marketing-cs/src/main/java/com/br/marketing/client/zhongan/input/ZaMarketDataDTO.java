package com.br.marketing.client.zhongan.input;

import lombok.Data;

import java.util.List;

@Data
public class ZaMarketDataDTO {

    private String reqNo;

    private List<ZaMarketDetail> data;
}
