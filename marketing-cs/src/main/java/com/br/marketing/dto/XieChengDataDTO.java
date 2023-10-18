package com.br.marketing.dto;

import com.br.marketing.entity.XieChengData;
import com.br.marketing.rule.SourceData;

/**
 * 携程传输对象
 *
 * @author Guo Zeqiang
 * @dateTime 2022/12/1 19:40
 */
public class XieChengDataDTO extends SourceData {
    private XieChengData xieChengData;

    // 是否要进延迟队列
    private Boolean toDelay;

    public XieChengDataDTO(XieChengData xieChengData) {
        this.xieChengData = xieChengData;
    }

    public XieChengDataDTO() {
    }

    public XieChengData getXieChengData() {
        return xieChengData;
    }

    public void setXieChengData(XieChengData xieChengData) {
        this.xieChengData = xieChengData;
    }

    public Boolean getToDelay() {
        return toDelay;
    }

    public void setToDelay(Boolean toDelay) {
        this.toDelay = toDelay;
    }
}
