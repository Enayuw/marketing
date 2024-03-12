package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 一个表对应的一个code
 *
 * @author songjuanjuan
 * @dateTime 2021/10/26 17:30
 */
@Getter
@AllArgsConstructor
public enum TableCodeEnum {


    MARKETING_CUSTOMER("01", "b_marketing_customer","MarketingCustomer"),
    SCORE_SEARCH_CONDITION("02", "b_score_search_condition","ScoreSearchCondition"),
    SCORE_SEARCH_CONDITION_MAPPING("03", "b_score_search_condition_mapping","ScoreSearchConditionMapping"),
    MARKETING_TASK("04", "b_marketing_task","MarketingTask"),
    DATA_VALIDITY_PERIOD_CHANGE("05", "b_marketing_data_valid_config","MarketingDataValidConfig"),
    SAVE_OR_UPDATE_VARIABLE_DIC("06","b_variable_dic","VariableDic"),
    SAVE_OR_UPDATE_DATA_VALID_CONFIG_DEFAULT("07","b_marketing_data_valid_config_default","MarketingDataValidConfigDefault");

    /**
     * 表对应的码值
     */
    private final String tableCode;

    /**
     * 表名
     */
    private final String tableName;

    /**
     * 表对应的实体
     */
    private final String tableEntity;

}
