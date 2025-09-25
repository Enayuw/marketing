package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PushDecisionsExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PushDecisionsExample() {
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

        public Criteria andDependencyTemplateIdIsNull() {
            addCriterion("dependency_template_id is null");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdIsNotNull() {
            addCriterion("dependency_template_id is not null");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdEqualTo(Long value) {
            addCriterion("dependency_template_id =", value, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdNotEqualTo(Long value) {
            addCriterion("dependency_template_id <>", value, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdGreaterThan(Long value) {
            addCriterion("dependency_template_id >", value, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdGreaterThanOrEqualTo(Long value) {
            addCriterion("dependency_template_id >=", value, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdLessThan(Long value) {
            addCriterion("dependency_template_id <", value, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdLessThanOrEqualTo(Long value) {
            addCriterion("dependency_template_id <=", value, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdIn(List<Long> values) {
            addCriterion("dependency_template_id in", values, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdNotIn(List<Long> values) {
            addCriterion("dependency_template_id not in", values, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdBetween(Long value1, Long value2) {
            addCriterion("dependency_template_id between", value1, value2, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andDependencyTemplateIdNotBetween(Long value1, Long value2) {
            addCriterion("dependency_template_id not between", value1, value2, "dependencyTemplateId");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andAutoTimeIsNull() {
            addCriterion("auto_time is null");
            return (Criteria) this;
        }

        public Criteria andAutoTimeIsNotNull() {
            addCriterion("auto_time is not null");
            return (Criteria) this;
        }

        public Criteria andAutoTimeEqualTo(String value) {
            addCriterion("auto_time =", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeNotEqualTo(String value) {
            addCriterion("auto_time <>", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeGreaterThan(String value) {
            addCriterion("auto_time >", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeGreaterThanOrEqualTo(String value) {
            addCriterion("auto_time >=", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeLessThan(String value) {
            addCriterion("auto_time <", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeLessThanOrEqualTo(String value) {
            addCriterion("auto_time <=", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeLike(String value) {
            addCriterion("auto_time like", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeNotLike(String value) {
            addCriterion("auto_time not like", value, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeIn(List<String> values) {
            addCriterion("auto_time in", values, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeNotIn(List<String> values) {
            addCriterion("auto_time not in", values, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeBetween(String value1, String value2) {
            addCriterion("auto_time between", value1, value2, "autoTime");
            return (Criteria) this;
        }

        public Criteria andAutoTimeNotBetween(String value1, String value2) {
            addCriterion("auto_time not between", value1, value2, "autoTime");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsIsNull() {
            addCriterion("push_datasets is null");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsIsNotNull() {
            addCriterion("push_datasets is not null");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsEqualTo(String value) {
            addCriterion("push_datasets =", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsNotEqualTo(String value) {
            addCriterion("push_datasets <>", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsGreaterThan(String value) {
            addCriterion("push_datasets >", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsGreaterThanOrEqualTo(String value) {
            addCriterion("push_datasets >=", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsLessThan(String value) {
            addCriterion("push_datasets <", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsLessThanOrEqualTo(String value) {
            addCriterion("push_datasets <=", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsLike(String value) {
            addCriterion("push_datasets like", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsNotLike(String value) {
            addCriterion("push_datasets not like", value, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsIn(List<String> values) {
            addCriterion("push_datasets in", values, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsNotIn(List<String> values) {
            addCriterion("push_datasets not in", values, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsBetween(String value1, String value2) {
            addCriterion("push_datasets between", value1, value2, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andPushDatasetsNotBetween(String value1, String value2) {
            addCriterion("push_datasets not between", value1, value2, "pushDatasets");
            return (Criteria) this;
        }

        public Criteria andReachStrategyIsNull() {
            addCriterion("reach_strategy is null");
            return (Criteria) this;
        }

        public Criteria andReachStrategyIsNotNull() {
            addCriterion("reach_strategy is not null");
            return (Criteria) this;
        }

        public Criteria andReachStrategyEqualTo(String value) {
            addCriterion("reach_strategy =", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyNotEqualTo(String value) {
            addCriterion("reach_strategy <>", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyGreaterThan(String value) {
            addCriterion("reach_strategy >", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyGreaterThanOrEqualTo(String value) {
            addCriterion("reach_strategy >=", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyLessThan(String value) {
            addCriterion("reach_strategy <", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyLessThanOrEqualTo(String value) {
            addCriterion("reach_strategy <=", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyLike(String value) {
            addCriterion("reach_strategy like", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyNotLike(String value) {
            addCriterion("reach_strategy not like", value, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyIn(List<String> values) {
            addCriterion("reach_strategy in", values, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyNotIn(List<String> values) {
            addCriterion("reach_strategy not in", values, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyBetween(String value1, String value2) {
            addCriterion("reach_strategy between", value1, value2, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andReachStrategyNotBetween(String value1, String value2) {
            addCriterion("reach_strategy not between", value1, value2, "reachStrategy");
            return (Criteria) this;
        }

        public Criteria andPushTargetIsNull() {
            addCriterion("push_target is null");
            return (Criteria) this;
        }

        public Criteria andPushTargetIsNotNull() {
            addCriterion("push_target is not null");
            return (Criteria) this;
        }

        public Criteria andPushTargetEqualTo(Integer value) {
            addCriterion("push_target =", value, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetNotEqualTo(Integer value) {
            addCriterion("push_target <>", value, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetGreaterThan(Integer value) {
            addCriterion("push_target >", value, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_target >=", value, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetLessThan(Integer value) {
            addCriterion("push_target <", value, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetLessThanOrEqualTo(Integer value) {
            addCriterion("push_target <=", value, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetIn(List<Integer> values) {
            addCriterion("push_target in", values, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetNotIn(List<Integer> values) {
            addCriterion("push_target not in", values, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetBetween(Integer value1, Integer value2) {
            addCriterion("push_target between", value1, value2, "pushTarget");
            return (Criteria) this;
        }

        public Criteria andPushTargetNotBetween(Integer value1, Integer value2) {
            addCriterion("push_target not between", value1, value2, "pushTarget");
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

        public Criteria andAutoRefreshIsNull() {
            addCriterion("auto_refresh is null");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshIsNotNull() {
            addCriterion("auto_refresh is not null");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshEqualTo(Integer value) {
            addCriterion("auto_refresh =", value, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshNotEqualTo(Integer value) {
            addCriterion("auto_refresh <>", value, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshGreaterThan(Integer value) {
            addCriterion("auto_refresh >", value, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshGreaterThanOrEqualTo(Integer value) {
            addCriterion("auto_refresh >=", value, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshLessThan(Integer value) {
            addCriterion("auto_refresh <", value, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshLessThanOrEqualTo(Integer value) {
            addCriterion("auto_refresh <=", value, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshIn(List<Integer> values) {
            addCriterion("auto_refresh in", values, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshNotIn(List<Integer> values) {
            addCriterion("auto_refresh not in", values, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshBetween(Integer value1, Integer value2) {
            addCriterion("auto_refresh between", value1, value2, "autoRefresh");
            return (Criteria) this;
        }

        public Criteria andAutoRefreshNotBetween(Integer value1, Integer value2) {
            addCriterion("auto_refresh not between", value1, value2, "autoRefresh");
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