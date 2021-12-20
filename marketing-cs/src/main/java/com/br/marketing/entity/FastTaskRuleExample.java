package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FastTaskRuleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FastTaskRuleExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andRuleNameIsNull() {
            addCriterion("rule_name is null");
            return (Criteria) this;
        }

        public Criteria andRuleNameIsNotNull() {
            addCriterion("rule_name is not null");
            return (Criteria) this;
        }

        public Criteria andRuleNameEqualTo(String value) {
            addCriterion("rule_name =", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotEqualTo(String value) {
            addCriterion("rule_name <>", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameGreaterThan(String value) {
            addCriterion("rule_name >", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameGreaterThanOrEqualTo(String value) {
            addCriterion("rule_name >=", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLessThan(String value) {
            addCriterion("rule_name <", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLessThanOrEqualTo(String value) {
            addCriterion("rule_name <=", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLike(String value) {
            addCriterion("rule_name like", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotLike(String value) {
            addCriterion("rule_name not like", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameIn(List<String> values) {
            addCriterion("rule_name in", values, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotIn(List<String> values) {
            addCriterion("rule_name not in", values, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameBetween(String value1, String value2) {
            addCriterion("rule_name between", value1, value2, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotBetween(String value1, String value2) {
            addCriterion("rule_name not between", value1, value2, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNumberIsNull() {
            addCriterion("rule_number is null");
            return (Criteria) this;
        }

        public Criteria andRuleNumberIsNotNull() {
            addCriterion("rule_number is not null");
            return (Criteria) this;
        }

        public Criteria andRuleNumberEqualTo(String value) {
            addCriterion("rule_number =", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberNotEqualTo(String value) {
            addCriterion("rule_number <>", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberGreaterThan(String value) {
            addCriterion("rule_number >", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberGreaterThanOrEqualTo(String value) {
            addCriterion("rule_number >=", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberLessThan(String value) {
            addCriterion("rule_number <", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberLessThanOrEqualTo(String value) {
            addCriterion("rule_number <=", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberLike(String value) {
            addCriterion("rule_number like", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberNotLike(String value) {
            addCriterion("rule_number not like", value, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberIn(List<String> values) {
            addCriterion("rule_number in", values, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberNotIn(List<String> values) {
            addCriterion("rule_number not in", values, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberBetween(String value1, String value2) {
            addCriterion("rule_number between", value1, value2, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andRuleNumberNotBetween(String value1, String value2) {
            addCriterion("rule_number not between", value1, value2, "ruleNumber");
            return (Criteria) this;
        }

        public Criteria andApiCodeIsNull() {
            addCriterion("api_code is null");
            return (Criteria) this;
        }

        public Criteria andApiCodeIsNotNull() {
            addCriterion("api_code is not null");
            return (Criteria) this;
        }

        public Criteria andApiCodeEqualTo(String value) {
            addCriterion("api_code =", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeNotEqualTo(String value) {
            addCriterion("api_code <>", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeGreaterThan(String value) {
            addCriterion("api_code >", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("api_code >=", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeLessThan(String value) {
            addCriterion("api_code <", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeLessThanOrEqualTo(String value) {
            addCriterion("api_code <=", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeLike(String value) {
            addCriterion("api_code like", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeNotLike(String value) {
            addCriterion("api_code not like", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeIn(List<String> values) {
            addCriterion("api_code in", values, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeNotIn(List<String> values) {
            addCriterion("api_code not in", values, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeBetween(String value1, String value2) {
            addCriterion("api_code between", value1, value2, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeNotBetween(String value1, String value2) {
            addCriterion("api_code not between", value1, value2, "apiCode");
            return (Criteria) this;
        }

        public Criteria andDataConditionIsNull() {
            addCriterion("data_condition is null");
            return (Criteria) this;
        }

        public Criteria andDataConditionIsNotNull() {
            addCriterion("data_condition is not null");
            return (Criteria) this;
        }

        public Criteria andDataConditionEqualTo(String value) {
            addCriterion("data_condition =", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotEqualTo(String value) {
            addCriterion("data_condition <>", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionGreaterThan(String value) {
            addCriterion("data_condition >", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionGreaterThanOrEqualTo(String value) {
            addCriterion("data_condition >=", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionLessThan(String value) {
            addCriterion("data_condition <", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionLessThanOrEqualTo(String value) {
            addCriterion("data_condition <=", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionLike(String value) {
            addCriterion("data_condition like", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotLike(String value) {
            addCriterion("data_condition not like", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionIn(List<String> values) {
            addCriterion("data_condition in", values, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotIn(List<String> values) {
            addCriterion("data_condition not in", values, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionBetween(String value1, String value2) {
            addCriterion("data_condition between", value1, value2, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotBetween(String value1, String value2) {
            addCriterion("data_condition not between", value1, value2, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataTypeIsNull() {
            addCriterion("data_type is null");
            return (Criteria) this;
        }

        public Criteria andDataTypeIsNotNull() {
            addCriterion("data_type is not null");
            return (Criteria) this;
        }

        public Criteria andDataTypeEqualTo(Integer value) {
            addCriterion("data_type =", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotEqualTo(Integer value) {
            addCriterion("data_type <>", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeGreaterThan(Integer value) {
            addCriterion("data_type >", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("data_type >=", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeLessThan(Integer value) {
            addCriterion("data_type <", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeLessThanOrEqualTo(Integer value) {
            addCriterion("data_type <=", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeIn(List<Integer> values) {
            addCriterion("data_type in", values, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotIn(List<Integer> values) {
            addCriterion("data_type not in", values, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeBetween(Integer value1, Integer value2) {
            addCriterion("data_type between", value1, value2, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("data_type not between", value1, value2, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataIdDescIsNull() {
            addCriterion("data_id_desc is null");
            return (Criteria) this;
        }

        public Criteria andDataIdDescIsNotNull() {
            addCriterion("data_id_desc is not null");
            return (Criteria) this;
        }

        public Criteria andDataIdDescEqualTo(String value) {
            addCriterion("data_id_desc =", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescNotEqualTo(String value) {
            addCriterion("data_id_desc <>", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescGreaterThan(String value) {
            addCriterion("data_id_desc >", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescGreaterThanOrEqualTo(String value) {
            addCriterion("data_id_desc >=", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescLessThan(String value) {
            addCriterion("data_id_desc <", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescLessThanOrEqualTo(String value) {
            addCriterion("data_id_desc <=", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescLike(String value) {
            addCriterion("data_id_desc like", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescNotLike(String value) {
            addCriterion("data_id_desc not like", value, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescIn(List<String> values) {
            addCriterion("data_id_desc in", values, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescNotIn(List<String> values) {
            addCriterion("data_id_desc not in", values, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescBetween(String value1, String value2) {
            addCriterion("data_id_desc between", value1, value2, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andDataIdDescNotBetween(String value1, String value2) {
            addCriterion("data_id_desc not between", value1, value2, "dataIdDesc");
            return (Criteria) this;
        }

        public Criteria andRuleIdIsNull() {
            addCriterion("rule_id is null");
            return (Criteria) this;
        }

        public Criteria andRuleIdIsNotNull() {
            addCriterion("rule_id is not null");
            return (Criteria) this;
        }

        public Criteria andRuleIdEqualTo(Long value) {
            addCriterion("rule_id =", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdNotEqualTo(Long value) {
            addCriterion("rule_id <>", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdGreaterThan(Long value) {
            addCriterion("rule_id >", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdGreaterThanOrEqualTo(Long value) {
            addCriterion("rule_id >=", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdLessThan(Long value) {
            addCriterion("rule_id <", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdLessThanOrEqualTo(Long value) {
            addCriterion("rule_id <=", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdIn(List<Long> values) {
            addCriterion("rule_id in", values, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdNotIn(List<Long> values) {
            addCriterion("rule_id not in", values, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdBetween(Long value1, Long value2) {
            addCriterion("rule_id between", value1, value2, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdNotBetween(Long value1, Long value2) {
            addCriterion("rule_id not between", value1, value2, "ruleId");
            return (Criteria) this;
        }

        public Criteria andTaskTypeIsNull() {
            addCriterion("task_type is null");
            return (Criteria) this;
        }

        public Criteria andTaskTypeIsNotNull() {
            addCriterion("task_type is not null");
            return (Criteria) this;
        }

        public Criteria andTaskTypeEqualTo(Integer value) {
            addCriterion("task_type =", value, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeNotEqualTo(Integer value) {
            addCriterion("task_type <>", value, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeGreaterThan(Integer value) {
            addCriterion("task_type >", value, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("task_type >=", value, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeLessThan(Integer value) {
            addCriterion("task_type <", value, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeLessThanOrEqualTo(Integer value) {
            addCriterion("task_type <=", value, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeIn(List<Integer> values) {
            addCriterion("task_type in", values, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeNotIn(List<Integer> values) {
            addCriterion("task_type not in", values, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeBetween(Integer value1, Integer value2) {
            addCriterion("task_type between", value1, value2, "taskType");
            return (Criteria) this;
        }

        public Criteria andTaskTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("task_type not between", value1, value2, "taskType");
            return (Criteria) this;
        }

        public Criteria andStrategyIdIsNull() {
            addCriterion("strategy_id is null");
            return (Criteria) this;
        }

        public Criteria andStrategyIdIsNotNull() {
            addCriterion("strategy_id is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyIdEqualTo(String value) {
            addCriterion("strategy_id =", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotEqualTo(String value) {
            addCriterion("strategy_id <>", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdGreaterThan(String value) {
            addCriterion("strategy_id >", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_id >=", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdLessThan(String value) {
            addCriterion("strategy_id <", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdLessThanOrEqualTo(String value) {
            addCriterion("strategy_id <=", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdLike(String value) {
            addCriterion("strategy_id like", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotLike(String value) {
            addCriterion("strategy_id not like", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdIn(List<String> values) {
            addCriterion("strategy_id in", values, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotIn(List<String> values) {
            addCriterion("strategy_id not in", values, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdBetween(String value1, String value2) {
            addCriterion("strategy_id between", value1, value2, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotBetween(String value1, String value2) {
            addCriterion("strategy_id not between", value1, value2, "strategyId");
            return (Criteria) this;
        }

        public Criteria andProductInfoIsNull() {
            addCriterion("product_info is null");
            return (Criteria) this;
        }

        public Criteria andProductInfoIsNotNull() {
            addCriterion("product_info is not null");
            return (Criteria) this;
        }

        public Criteria andProductInfoEqualTo(String value) {
            addCriterion("product_info =", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoNotEqualTo(String value) {
            addCriterion("product_info <>", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoGreaterThan(String value) {
            addCriterion("product_info >", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoGreaterThanOrEqualTo(String value) {
            addCriterion("product_info >=", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoLessThan(String value) {
            addCriterion("product_info <", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoLessThanOrEqualTo(String value) {
            addCriterion("product_info <=", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoLike(String value) {
            addCriterion("product_info like", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoNotLike(String value) {
            addCriterion("product_info not like", value, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoIn(List<String> values) {
            addCriterion("product_info in", values, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoNotIn(List<String> values) {
            addCriterion("product_info not in", values, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoBetween(String value1, String value2) {
            addCriterion("product_info between", value1, value2, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductInfoNotBetween(String value1, String value2) {
            addCriterion("product_info not between", value1, value2, "productInfo");
            return (Criteria) this;
        }

        public Criteria andProductFieldIsNull() {
            addCriterion("product_field is null");
            return (Criteria) this;
        }

        public Criteria andProductFieldIsNotNull() {
            addCriterion("product_field is not null");
            return (Criteria) this;
        }

        public Criteria andProductFieldEqualTo(String value) {
            addCriterion("product_field =", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldNotEqualTo(String value) {
            addCriterion("product_field <>", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldGreaterThan(String value) {
            addCriterion("product_field >", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldGreaterThanOrEqualTo(String value) {
            addCriterion("product_field >=", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldLessThan(String value) {
            addCriterion("product_field <", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldLessThanOrEqualTo(String value) {
            addCriterion("product_field <=", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldLike(String value) {
            addCriterion("product_field like", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldNotLike(String value) {
            addCriterion("product_field not like", value, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldIn(List<String> values) {
            addCriterion("product_field in", values, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldNotIn(List<String> values) {
            addCriterion("product_field not in", values, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldBetween(String value1, String value2) {
            addCriterion("product_field between", value1, value2, "productField");
            return (Criteria) this;
        }

        public Criteria andProductFieldNotBetween(String value1, String value2) {
            addCriterion("product_field not between", value1, value2, "productField");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoIsNull() {
            addCriterion("callback_info is null");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoIsNotNull() {
            addCriterion("callback_info is not null");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoEqualTo(String value) {
            addCriterion("callback_info =", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoNotEqualTo(String value) {
            addCriterion("callback_info <>", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoGreaterThan(String value) {
            addCriterion("callback_info >", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoGreaterThanOrEqualTo(String value) {
            addCriterion("callback_info >=", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoLessThan(String value) {
            addCriterion("callback_info <", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoLessThanOrEqualTo(String value) {
            addCriterion("callback_info <=", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoLike(String value) {
            addCriterion("callback_info like", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoNotLike(String value) {
            addCriterion("callback_info not like", value, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoIn(List<String> values) {
            addCriterion("callback_info in", values, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoNotIn(List<String> values) {
            addCriterion("callback_info not in", values, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoBetween(String value1, String value2) {
            addCriterion("callback_info between", value1, value2, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andCallbackInfoNotBetween(String value1, String value2) {
            addCriterion("callback_info not between", value1, value2, "callbackInfo");
            return (Criteria) this;
        }

        public Criteria andTaskTimeIsNull() {
            addCriterion("task_time is null");
            return (Criteria) this;
        }

        public Criteria andTaskTimeIsNotNull() {
            addCriterion("task_time is not null");
            return (Criteria) this;
        }

        public Criteria andTaskTimeEqualTo(String value) {
            addCriterion("task_time =", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotEqualTo(String value) {
            addCriterion("task_time <>", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeGreaterThan(String value) {
            addCriterion("task_time >", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeGreaterThanOrEqualTo(String value) {
            addCriterion("task_time >=", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeLessThan(String value) {
            addCriterion("task_time <", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeLessThanOrEqualTo(String value) {
            addCriterion("task_time <=", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeLike(String value) {
            addCriterion("task_time like", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotLike(String value) {
            addCriterion("task_time not like", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeIn(List<String> values) {
            addCriterion("task_time in", values, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotIn(List<String> values) {
            addCriterion("task_time not in", values, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeBetween(String value1, String value2) {
            addCriterion("task_time between", value1, value2, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotBetween(String value1, String value2) {
            addCriterion("task_time not between", value1, value2, "taskTime");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andOptIdIsNull() {
            addCriterion("opt_id is null");
            return (Criteria) this;
        }

        public Criteria andOptIdIsNotNull() {
            addCriterion("opt_id is not null");
            return (Criteria) this;
        }

        public Criteria andOptIdEqualTo(String value) {
            addCriterion("opt_id =", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdNotEqualTo(String value) {
            addCriterion("opt_id <>", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdGreaterThan(String value) {
            addCriterion("opt_id >", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdGreaterThanOrEqualTo(String value) {
            addCriterion("opt_id >=", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdLessThan(String value) {
            addCriterion("opt_id <", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdLessThanOrEqualTo(String value) {
            addCriterion("opt_id <=", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdLike(String value) {
            addCriterion("opt_id like", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdNotLike(String value) {
            addCriterion("opt_id not like", value, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdIn(List<String> values) {
            addCriterion("opt_id in", values, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdNotIn(List<String> values) {
            addCriterion("opt_id not in", values, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdBetween(String value1, String value2) {
            addCriterion("opt_id between", value1, value2, "optId");
            return (Criteria) this;
        }

        public Criteria andOptIdNotBetween(String value1, String value2) {
            addCriterion("opt_id not between", value1, value2, "optId");
            return (Criteria) this;
        }

        public Criteria andOptNameIsNull() {
            addCriterion("opt_name is null");
            return (Criteria) this;
        }

        public Criteria andOptNameIsNotNull() {
            addCriterion("opt_name is not null");
            return (Criteria) this;
        }

        public Criteria andOptNameEqualTo(String value) {
            addCriterion("opt_name =", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameNotEqualTo(String value) {
            addCriterion("opt_name <>", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameGreaterThan(String value) {
            addCriterion("opt_name >", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameGreaterThanOrEqualTo(String value) {
            addCriterion("opt_name >=", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameLessThan(String value) {
            addCriterion("opt_name <", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameLessThanOrEqualTo(String value) {
            addCriterion("opt_name <=", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameLike(String value) {
            addCriterion("opt_name like", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameNotLike(String value) {
            addCriterion("opt_name not like", value, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameIn(List<String> values) {
            addCriterion("opt_name in", values, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameNotIn(List<String> values) {
            addCriterion("opt_name not in", values, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameBetween(String value1, String value2) {
            addCriterion("opt_name between", value1, value2, "optName");
            return (Criteria) this;
        }

        public Criteria andOptNameNotBetween(String value1, String value2) {
            addCriterion("opt_name not between", value1, value2, "optName");
            return (Criteria) this;
        }

        public Criteria andIsDelIsNull() {
            addCriterion("is_del is null");
            return (Criteria) this;
        }

        public Criteria andIsDelIsNotNull() {
            addCriterion("is_del is not null");
            return (Criteria) this;
        }

        public Criteria andIsDelEqualTo(Integer value) {
            addCriterion("is_del =", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotEqualTo(Integer value) {
            addCriterion("is_del <>", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelGreaterThan(Integer value) {
            addCriterion("is_del >", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_del >=", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelLessThan(Integer value) {
            addCriterion("is_del <", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelLessThanOrEqualTo(Integer value) {
            addCriterion("is_del <=", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelIn(List<Integer> values) {
            addCriterion("is_del in", values, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotIn(List<Integer> values) {
            addCriterion("is_del not in", values, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelBetween(Integer value1, Integer value2) {
            addCriterion("is_del between", value1, value2, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotBetween(Integer value1, Integer value2) {
            addCriterion("is_del not between", value1, value2, "isDel");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUntaskNumIsNull() {
            addCriterion("untask_num is null");
            return (Criteria) this;
        }

        public Criteria andUntaskNumIsNotNull() {
            addCriterion("untask_num is not null");
            return (Criteria) this;
        }

        public Criteria andUntaskNumEqualTo(Integer value) {
            addCriterion("untask_num =", value, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumNotEqualTo(Integer value) {
            addCriterion("untask_num <>", value, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumGreaterThan(Integer value) {
            addCriterion("untask_num >", value, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("untask_num >=", value, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumLessThan(Integer value) {
            addCriterion("untask_num <", value, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumLessThanOrEqualTo(Integer value) {
            addCriterion("untask_num <=", value, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumIn(List<Integer> values) {
            addCriterion("untask_num in", values, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumNotIn(List<Integer> values) {
            addCriterion("untask_num not in", values, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumBetween(Integer value1, Integer value2) {
            addCriterion("untask_num between", value1, value2, "untaskNum");
            return (Criteria) this;
        }

        public Criteria andUntaskNumNotBetween(Integer value1, Integer value2) {
            addCriterion("untask_num not between", value1, value2, "untaskNum");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}