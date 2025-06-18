package com.br.marketing.enums;

public enum HandlerStageEnum {

    PRE("pre"),
    THREAD("thread");

    HandlerStageEnum(String stage){
        this.stage = stage;
    }

    private String stage;
}
