package com.br.marketing.webhook.dingding.msgtype;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 只有在群内的成员才可被@
 *
 * @author Guo Zeqiang
 * @dateTime 2023-08-17 17:57
 */
@Schema(description = "群内的成员@")
public class At implements Serializable {

    private static final long serialVersionUID = 3345180510219107047L;
    /**
     * 2023-08-17 13:57
     * 被@人的手机号。
     * <p>
     * 注意
     * 在text内容里要有@人的手机号，只有在群内的成员才可被@，非群内成员手机号会被脱敏。
     */
    @Schema(description = "被@人的手机号")
    private List<String> atMobiles;

    /**
     * 2023-08-17 13:57
     * 被@人的用户userid。
     * <p>
     * 注意
     * 在content里添加@人的userid
     */
    @Schema(description = "被@人的用户userid")
    private List<String> atUserIds;

    /**
     * 2023-08-17 13:57
     * 是否@所有人。
     */
    @Schema(description = "是否@所有人")
    private Boolean isAtAll;

    public At(List<String> atMobiles, List<String> atUserIds, Boolean isAtAll) {
        this.atMobiles = atMobiles;
        this.atUserIds = atUserIds;
        this.isAtAll = isAtAll;
    }

    public At() {
    }

    public List<String> getAtMobiles() {
        return atMobiles;
    }

    public void setAtMobiles(List<String> atMobiles) {
        this.atMobiles = atMobiles;
    }

    public List<String> getAtUserIds() {
        return atUserIds;
    }

    public void setAtUserIds(List<String> atUserIds) {
        this.atUserIds = atUserIds;
    }

    public Boolean getAtAll() {
        return isAtAll;
    }

    public void setAtAll(Boolean atAll) {
        isAtAll = atAll;
    }

    @Override
    public String toString() {
        return "At{" +
                "atMobiles=" + atMobiles +
                ", atUserIds=" + atUserIds +
                ", isAtAll=" + isAtAll +
                '}';
    }
}
