package com.br.marketing.chain.xiecheng;

import com.br.marketing.context.XieChengReportContext;

/**
 * 携程上报责任链服务
 */
public abstract class AbstractXieChengReportHandler {

    private Integer order;

    private String bizMark;

    private AbstractXieChengReportHandler next;


    protected AbstractXieChengReportHandler (Integer order){
        this.order = order;
    }

    protected AbstractXieChengReportHandler (Integer order, String bizMark){
        this.order = order;
        this.bizMark = bizMark;
    }

    abstract void process(XieChengReportContext context);

    void handle(XieChengReportContext context){
        if (!context.isContinueFlag()) {
            return;
        }
        process(context);
        if (next != null) {
            next.handle(context);
        }
    }

    void setNext(AbstractXieChengReportHandler next){
        this.next = next;
    }

    public Integer getOrder() {
        return order;
    }

    public String getBizMark() {
        return bizMark;
    }
}
