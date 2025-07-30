package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;

/**
 * 携程上报责任链抽象类
 */
public abstract class AbstractXieChengReportHandler {

    private String name;

    private String bizMark;

    private String stage;

    public AbstractXieChengReportHandler (String name, String bizMark, String stage){
        this.name = name;
        this.bizMark = bizMark;
        this.stage = stage;
    }

    abstract public String process(XieChengReportContext context);

    public String getBizMark() {
        return bizMark;
    }

    public String getName() {
        return name;
    }

    public String getStage() {
        return stage;
    }
}
