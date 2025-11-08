package com.br.marketing.webhook.dingding.msgtype;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * ActionCard 消息
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 10:28
 */
@Schema(description = "ActionCard 消息")
public class DingDingActionCardMessage extends AbstractRobotSendRequest {

    private static final long serialVersionUID = 8105948213103026617L;
    /**
     * 2023-08-17 18:06
     */
    private ActionCard actionCard;


    public DingDingActionCardMessage() {
        super(MsgType.ACTION_CARD);
    }

    public DingDingActionCardMessage(ActionCard actionCard) {
        super(MsgType.ACTION_CARD);
        this.actionCard = actionCard;
    }

    public ActionCard getActionCard() {
        return actionCard;
    }

    public void setActionCard(ActionCard actionCard) {
        this.actionCard = actionCard;
    }

    @Override
    public String toString() {
        return "DingDingActionCardMessage{" +
                "actionCard=" + actionCard +
                '}';
    }

    /**
     * 2023-08-17 18:05
     */
    public static class ActionCard implements Serializable {
        private static final long serialVersionUID = -2528482535169987688L;
        /**
         * 2023-08-17 17:48
         * 首屏会话透出的展示内容。
         * 必填
         */
        @Schema(description = "首屏会话透出的展示内容", required = true)
        private String title;

        /**
         * 2023-08-17 17:49
         * markdown格式的消息。
         * 必填
         */
        @Schema(description = "markdown格式的消息", required = true)
        private String text;

        /**
         * 2023-08-17 17:48
         * 整体跳转ActionCard类型 使用
         * 单个按钮的标题
         * 必填
         */
        @Schema(description = "单个按钮的标题", required = true)
        private String singleTitle;

        /**
         * 2023-08-17 17:49
         * 整体跳转ActionCard类型 使用
         * 点击消息跳转的URL
         * 必填
         */
        @Schema(description = "点击消息跳转的UR", required = true)
        private String singleURL;

        /**
         * 2023-08-17 19:10
         * 按钮 独立跳转ActionCard类型 使用
         */
        @Schema(description = "按钮 独立跳转ActionCard类型 使用", required = true)
        private List<Btn> btns;

        /**
         * 2023-08-17 17:49
         * 0：按钮竖直排列
         * 1：按钮横向排列
         * 非必填
         */
        @Schema(description = "0：按钮竖直排列\n" +
                "1：按钮横向排列")
        private String btnOrientation;

        public ActionCard() {
        }

        public ActionCard(String title, String text, String singleTitle, String singleURL) {
            this.title = title;
            this.text = text;
            this.singleTitle = singleTitle;
            this.singleURL = singleURL;
        }

        public ActionCard(String title, String text, List<Btn> btns) {
            this.title = title;
            this.text = text;
            this.btns = btns;
        }

        public ActionCard(String title, String text, String singleTitle, String singleURL, String btnOrientation) {
            this.title = title;
            this.text = text;
            this.singleTitle = singleTitle;
            this.singleURL = singleURL;
            this.btnOrientation = btnOrientation;
        }

        public ActionCard(String title, String text, List<Btn> btns, String btnOrientation) {
            this.title = title;
            this.text = text;
            this.btns = btns;
            this.btnOrientation = btnOrientation;
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

        public String getSingleTitle() {
            return singleTitle;
        }

        public void setSingleTitle(String singleTitle) {
            this.singleTitle = singleTitle;
        }

        public String getSingleURL() {
            return singleURL;
        }

        public void setSingleURL(String singleURL) {
            this.singleURL = singleURL;
        }

        public List<Btn> getBtns() {
            return btns;
        }

        public void setBtns(List<Btn> btns) {
            this.btns = btns;
        }

        public String getBtnOrientation() {
            return btnOrientation;
        }

        public void setBtnOrientation(String btnOrientation) {
            this.btnOrientation = btnOrientation;
        }

        @Override
        public String toString() {
            return "ActionCard{" +
                    "title='" + title + '\'' +
                    ", text='" + text + '\'' +
                    ", singleTitle='" + singleTitle + '\'' +
                    ", singleURL='" + singleURL + '\'' +
                    ", btns=" + btns +
                    ", btnOrientation='" + btnOrientation + '\'' +
                    '}';
        }
    }


    /**
     * 2023-08-17 19:10
     * 独立跳转ActionCard类型
     */
    public static class Btn implements Serializable {
        private static final long serialVersionUID = 869982469449289384L;
        /**
         * 2023-08-17 17:48
         * 按钮标题。
         * 必填
         */
        @Schema(description = "按钮标题", required = true)
        private String title;

        /**
         * 2023-08-17 17:49
         * 点击消息跳转的URL
         * 必填
         */
        @Schema(description = "点击消息跳转的URL", required = true)
        private String actionURL;

        public Btn() {
        }

        public Btn(String title, String actionURL) {
            this.title = title;
            this.actionURL = actionURL;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getActionURL() {
            return actionURL;
        }

        public void setActionURL(String actionURL) {
            this.actionURL = actionURL;
        }

        @Override
        public String toString() {
            return "Btns{" +
                    "title='" + title + '\'' +
                    ", actionURL='" + actionURL + '\'' +
                    '}';
        }
    }

}
