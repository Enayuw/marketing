package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;

/**
 * 携程上报责任链抽象类
 */
public abstract class AbstractXieChengReportHandler {

    private String bizMark;

    public AbstractXieChengReportHandler (String bizMark){
        this.bizMark = bizMark;
    }

    abstract public String process(XieChengReportContext context);

    public String getBizMark() {
        return bizMark;
    }
}
