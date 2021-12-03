package com.br.marketing.client.haier.output;

/**
 * 应答消息
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/3 15:56
 */
public class Response2Entity extends BaseEntity {
    private static final long serialVersionUID = 8239124128755285376L;
    private String body;

    public Response2Entity() {
    }

    public Response2Entity(HeadEntity head, String body) {
        super(head);
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "Response2Entity{" +
                "head=" + head +
                ", body='" + body + '\'' +
                '}';
    }
}
