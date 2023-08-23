package com.br.marketing.webhook.dingding.msgtype;

/**
 * markdown(markdown) 消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 10:28
 */
public class DingDingMarkdownMessage extends AbstractRobotSendRequest {
    /**
     * 2023-08-17 17:58
     * markdown
     */
    private Markdown markdown;

    /**
     * 2023-08-17 13:56
     * 只有在群内的成员才可被@
     */
    private At at;

    public DingDingMarkdownMessage(Markdown markdown) {
        super(MsgType.MARKDOWN);
        this.markdown = markdown;
    }

    public DingDingMarkdownMessage(Markdown markdown, At at) {
        super(MsgType.MARKDOWN);
        this.markdown = markdown;
        this.at = at;
    }

    public Markdown getMarkdown() {
        return markdown;
    }

    public void setMarkdown(Markdown markdown) {
        this.markdown = markdown;
    }

    public At getAt() {
        return at;
    }

    public void setAt(At at) {
        this.at = at;
    }

    @Override
    public String toString() {
        return "DingDingMarkdownMessage{" +
                "markdown=" + markdown +
                ", at=" + at +
                '}';
    }

    public static class Markdown {
        /**
         * 2023-08-17 18:00
         * 首屏会话透出的展示内容。
         * 必填
         */
        private String title;
        /**
         * 2023-08-17 18:00
         * markdown格式的消息。
         * 必填
         */
        private String text;

        public Markdown(String title, String text) {
            this.title = title;
            this.text = text;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "Markdown{" +
                    "title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

}
