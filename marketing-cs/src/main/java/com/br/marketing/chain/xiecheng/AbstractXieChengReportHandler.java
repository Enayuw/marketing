package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;

/**
 * 携程上报责任链服务
 */
public abstract class AbstractXieChengReportHandler {

    private Integer order;

    private String bizMark;

    protected AbstractXieChengReportHandler (Integer order, String bizMark){
        this.order = order;
        this.bizMark = bizMark;
    }

    abstract void process(XieChengReportContext context);

    public Integer getOrder() {return order;}

    public String getBizMark() {
        return bizMark;
    }
}
