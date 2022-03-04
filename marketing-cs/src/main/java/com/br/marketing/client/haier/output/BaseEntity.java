package com.br.marketing.client.haier.output;

import java.io.Serializable;

/**
 * 应答基础消息 Response
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/3 15:49
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1915077240390653166L;
    protected HeadEntity head;

    public BaseEntity() {
    }

    public BaseEntity(HeadEntity head) {
        this.head = head;
    }

    public HeadEntity getHead() {
        return head;
    }

    public void setHead(HeadEntity head) {
        this.head = head;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "head=" + head +
                '}';
    }
}
