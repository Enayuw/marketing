package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;

/**
 * 携程上报责任链抽象类
 */
public abstract class AbstractXieChengReportHandler {

    private String bizMark;

    private String stage;

    public AbstractXieChengReportHandler (String bizMark, String stage){
        this.bizMark = bizMark;
        this.stage = stage;
    }

    abstract public String process(XieChengReportContext context);

    public String getBizMark() {
        return bizMark;
    }

    public String getStage() {
        return stage;
    }
}
