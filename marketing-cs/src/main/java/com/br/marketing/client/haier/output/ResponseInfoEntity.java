package com.br.marketing.client.haier.output;

/**
 * 应答消息
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/3 15:56
 */
public class ResponseInfoEntity extends BaseEntity {
    private static final long serialVersionUID = 8239124128755285376L;
    private BodyEntity body;

    public ResponseInfoEntity() {
    }

    public ResponseInfoEntity(HeadEntity head, BodyEntity body) {
        super(head);
        this.body = body;
    }

    public BodyEntity getBody() {
        return body;
    }

    public void setBody(BodyEntity body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ResponseInfoEntity{" +
                "head=" + head +
                ", body=" + body +
                '}';
    }
}
