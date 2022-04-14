package com.br.marketing.client.yiqianbao.output;

import lombok.Data;

import java.util.List;

@Data
public class ResponseYqbDTO {


    private String respCode;

    private String respMsg;

    private List<YqbResult> resultList;


    @Data
    class YqbResult {

        private String code;

        private String msg;

        private String outerApplyNo;
    }

}
