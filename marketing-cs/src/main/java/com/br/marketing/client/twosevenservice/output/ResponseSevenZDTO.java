package com.br.marketing.client.twosevenservice.output;

import lombok.Data;

import java.util.List;

@Data
public class ResponseSevenZDTO {
    private String ret;
    private String msg;
    private List<SevenDetailVO> volist;
}
