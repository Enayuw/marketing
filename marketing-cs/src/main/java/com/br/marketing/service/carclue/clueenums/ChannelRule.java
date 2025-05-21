package com.br.marketing.service.carclue.clueenums;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ChannelRule {

    @Getter
    @AllArgsConstructor
    public enum MatchChannelRuleEnum {
        YC_KA("yc-ka"),
        YC_MEMBER("yc-member"),
        ZJ("zj"),
        DAILY_LIMITED("daily_limited");
        private String label;
    }

    @Getter
    @AllArgsConstructor
    public enum FilterChannelRuleEnum {
        YC_KA_FILTER("yc-ka-filter"),
        YC_MEMBER_FILTER("yc-member-filter"),
        ZJ_FILTER("zj-filter"),
        BLACK_LIST_FILTER("black_list_filter");
        private String label;
    }

    @Getter
    @AllArgsConstructor
    public enum PushChannelRuleEnum {
        YC_KA_PUSH("yc-ka-push"),
        YC_MEMBER_PUSH("yc-member-push"),
        ZJ_PUSH("zj-push");
        private String label;
    }

    @Getter
    @AllArgsConstructor
    public enum CallBackChannelRuleEnum {
        YC_KA_CALLBACK("yc-ka-callback"),
        YC_MEMBER_CALLBACK("yc-member-callback"),
        ZJ_CALLBACK("zj-callback");
        private String label;
    }

    @Getter
    @AllArgsConstructor
    public enum ConfigChannelRuleEnum {
        YC_KA_CONFIG("yc-ka-config"),
        YC_MEMBER_CONFIG("yc-member-config"),
        ZJ_CONFIG("zj-config");
        private String label;
    }

}
