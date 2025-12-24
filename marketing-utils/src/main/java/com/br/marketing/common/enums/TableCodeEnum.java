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
    SAVE_OR_UPDATE_DATA_VALID_CONFIG_DEFAULT("07","b_marketing_data_valid_config_default","MarketingDataValidConfigDefault"),
    SAVE_OR_UPDATE_DATA_VARIABLE_ALLOCATION("08","variable_allocation","VariableAllocation"),
    SAVE_OR_UPDATE_CAR_CLUE_INFO("09","b_car_clue_info", "CarClueInfo"),
    SAVE_OR_UPDATE_TAG_INFO("10","t_tag_data_rule", "TagDataRule"),
    SAVE_OR_UPDATE_DATA_CLEANING_INFO("11","b_marketing_data_clean_general_rule_config", "MarketingDataCleanGeneralRuleConfig"),
    SAVE_OR_UPDATE_CLUE_FILE_RECORDING("12","b_car_clue_file_recording", "ClueFileRecording"),
    SAVE_OR_UPDATE_CLUE_EXECUTE_RECORDING("13","b_car_clue_execute_recording", "CarClueExecuteRecording"),
    SAVE_OR_UPDATE_MANAGE_CONFIG("14","b_car_clue_manage_config", "CarClueManageConfig"),
    SAVE_OR_UPDATE_VALID_CONFIG("15","b_marketing_customize_data_valid_config","MarketingCustomizeDataValidConfig"),
    SAVE_OR_UPDATE_SYNC_CONFIG("16","b_sync_config","SyncConfig"),
    SAVE_OR_UPDATE_MOCK_POLICY_CONFIG("17","b_marketing_mock_policy", "MockPolicy"),
    SAVE_OR_UPDATE_MOCK_CASE_CONFIG("18","b_marketing_mock_case", "MockCase"),
    SAVE_OR_UPDATE_TEMPLATE_CONFIG("19","b_marketing_industry_template", "MarketingIndustryTemplate"),
    SAVE_OR_UPDATE_TEMPLATE_NODE_JSON_CONFIG("20","b_marketing_industry_template_json_parse", "MarketingIndustryTemplateJsonParse"),
    ;

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
