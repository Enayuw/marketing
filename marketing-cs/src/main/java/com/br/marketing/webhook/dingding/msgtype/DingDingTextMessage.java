package com.br.marketing.webhook.dingding.msgtype;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 文本 (text)消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 10:28
 */
@ApiModel(value = "文本 (text)消息")
public class DingDingTextMessage extends AbstractRobotSendRequest {

    private static final long serialVersionUID = -1553310938306395447L;
    /**
     * 2023-08-17 13:56
     * 文本消息
     */
    @ApiModelProperty(value = "文本消息")
    private Text text;

    /**
     * 2023-08-17 13:56
     * 只有在群内的成员才可被@
     */
    @ApiModelProperty(value = "只有在群内的成员才可被@")
    private At at;

    public DingDingTextMessage() {
        super(MsgType.TEXT);
    }

    public DingDingTextMessage(Text text) {
        super(MsgType.TEXT);
        this.text = text;
    }

    public DingDingTextMessage(Text text, At at) {
        super(MsgType.TEXT);
        this.text = text;
        this.at = at;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public At getAt() {
        return at;
    }

    public void setAt(At at) {
        this.at = at;
    }

    @Override
    public String toString() {
        return "DingDingTextMessage{" +
                "text=" + text +
                ", at=" + at +
                '}';
    }

    public static class Text implements Serializable {
        private static final long serialVersionUID = -7746259722316543385L;
        /**
         * 2023-08-17 17:28
         * 消息内容
         * 必填
         */
        @ApiModelProperty(value = "消息内容", required = true)
        private String content;

        public Text() {
        }

        public Text(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "Text{" +
                    "content='" + content + '\'' +
                    '}';
        }
    }
}
