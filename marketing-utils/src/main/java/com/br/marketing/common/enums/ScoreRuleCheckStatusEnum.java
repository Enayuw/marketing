package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * b_score_rule_config.check_status 取值说明。
 * <p>用于规则级校验与「自动生成跑分任务」Job 拉取过滤；手动指定 ruleId 拉取配置不受 {@link #CATALOG_BLOCK_AUTO} 影响。</p>
 */
@Getter
@AllArgsConstructor
public enum ScoreRuleCheckStatusEnum {

    /** 正常，可参与 BuildTaskJob 等全量自动生成 */
    OK(0, "正常"),
    /** 产管目录与 product_info 校验不通过后写入；全量 Job 不再拉取该规则，直至配置保存或重新开启 */
    CATALOG_BLOCK_AUTO(1, "产管校验不通过暂停自动生成");

    private final Integer value;
    private final String desc;

    public static boolean isOk(Integer checkStatus) {
        if (checkStatus == null) {
            return true;
        }
        return OK.value.equals(checkStatus);
    }
}
