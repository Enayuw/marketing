package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScoreRuleConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ScoreRuleConfigExample() {
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

        public Criteria andRuleNameShortIsNull() {
            addCriterion("rule_name_short is null");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortIsNotNull() {
            addCriterion("rule_name_short is not null");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortEqualTo(String value) {
            addCriterion("rule_name_short =", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortNotEqualTo(String value) {
            addCriterion("rule_name_short <>", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortGreaterThan(String value) {
            addCriterion("rule_name_short >", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortGreaterThanOrEqualTo(String value) {
            addCriterion("rule_name_short >=", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortLessThan(String value) {
            addCriterion("rule_name_short <", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortLessThanOrEqualTo(String value) {
            addCriterion("rule_name_short <=", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortLike(String value) {
            addCriterion("rule_name_short like", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortNotLike(String value) {
            addCriterion("rule_name_short not like", value, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortIn(List<String> values) {
            addCriterion("rule_name_short in", values, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortNotIn(List<String> values) {
            addCriterion("rule_name_short not in", values, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortBetween(String value1, String value2) {
            addCriterion("rule_name_short between", value1, value2, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andRuleNameShortNotBetween(String value1, String value2) {
            addCriterion("rule_name_short not between", value1, value2, "ruleNameShort");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(String value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(String value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(String value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(String value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(String value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(String value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLike(String value) {
            addCriterion("start_time like", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotLike(String value) {
            addCriterion("start_time not like", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<String> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<String> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(String value1, String value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(String value1, String value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonIsNull() {
            addCriterion("strategy_product_json is null");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonIsNotNull() {
            addCriterion("strategy_product_json is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonEqualTo(String value) {
            addCriterion("strategy_product_json =", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonNotEqualTo(String value) {
            addCriterion("strategy_product_json <>", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonGreaterThan(String value) {
            addCriterion("strategy_product_json >", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_product_json >=", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonLessThan(String value) {
            addCriterion("strategy_product_json <", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonLessThanOrEqualTo(String value) {
            addCriterion("strategy_product_json <=", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonLike(String value) {
            addCriterion("strategy_product_json like", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonNotLike(String value) {
            addCriterion("strategy_product_json not like", value, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonIn(List<String> values) {
            addCriterion("strategy_product_json in", values, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonNotIn(List<String> values) {
            addCriterion("strategy_product_json not in", values, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonBetween(String value1, String value2) {
            addCriterion("strategy_product_json between", value1, value2, "strategyProductJson");
            return (Criteria) this;
        }

        public Criteria andStrategyProductJsonNotBetween(String value1, String value2) {
            addCriterion("strategy_product_json not between", value1, value2, "strategyProductJson");
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

        public Criteria andBaseInfoIsNull() {
            addCriterion("base_info is null");
            return (Criteria) this;
        }

        public Criteria andBaseInfoIsNotNull() {
            addCriterion("base_info is not null");
            return (Criteria) this;
        }

        public Criteria andBaseInfoEqualTo(String value) {
            addCriterion("base_info =", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotEqualTo(String value) {
            addCriterion("base_info <>", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoGreaterThan(String value) {
            addCriterion("base_info >", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoGreaterThanOrEqualTo(String value) {
            addCriterion("base_info >=", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoLessThan(String value) {
            addCriterion("base_info <", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoLessThanOrEqualTo(String value) {
            addCriterion("base_info <=", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoLike(String value) {
            addCriterion("base_info like", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotLike(String value) {
            addCriterion("base_info not like", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoIn(List<String> values) {
            addCriterion("base_info in", values, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotIn(List<String> values) {
            addCriterion("base_info not in", values, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoBetween(String value1, String value2) {
            addCriterion("base_info between", value1, value2, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotBetween(String value1, String value2) {
            addCriterion("base_info not between", value1, value2, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andRuleTypeIsNull() {
            addCriterion("rule_type is null");
            return (Criteria) this;
        }

        public Criteria andRuleTypeIsNotNull() {
            addCriterion("rule_type is not null");
            return (Criteria) this;
        }

        public Criteria andRuleTypeEqualTo(Integer value) {
            addCriterion("rule_type =", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeNotEqualTo(Integer value) {
            addCriterion("rule_type <>", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeGreaterThan(Integer value) {
            addCriterion("rule_type >", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("rule_type >=", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeLessThan(Integer value) {
            addCriterion("rule_type <", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeLessThanOrEqualTo(Integer value) {
            addCriterion("rule_type <=", value, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeIn(List<Integer> values) {
            addCriterion("rule_type in", values, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeNotIn(List<Integer> values) {
            addCriterion("rule_type not in", values, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeBetween(Integer value1, Integer value2) {
            addCriterion("rule_type between", value1, value2, "ruleType");
            return (Criteria) this;
        }

        public Criteria andRuleTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("rule_type not between", value1, value2, "ruleType");
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

        public Criteria andExecTypeIsNull() {
            addCriterion("exec_type is null");
            return (Criteria) this;
        }

        public Criteria andExecTypeIsNotNull() {
            addCriterion("exec_type is not null");
            return (Criteria) this;
        }

        public Criteria andExecTypeEqualTo(Integer value) {
            addCriterion("exec_type =", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeNotEqualTo(Integer value) {
            addCriterion("exec_type <>", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeGreaterThan(Integer value) {
            addCriterion("exec_type >", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("exec_type >=", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeLessThan(Integer value) {
            addCriterion("exec_type <", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeLessThanOrEqualTo(Integer value) {
            addCriterion("exec_type <=", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeIn(List<Integer> values) {
            addCriterion("exec_type in", values, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeNotIn(List<Integer> values) {
            addCriterion("exec_type not in", values, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeBetween(Integer value1, Integer value2) {
            addCriterion("exec_type between", value1, value2, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("exec_type not between", value1, value2, "execType");
            return (Criteria) this;
        }

        public Criteria andAutoBuildIsNull() {
            addCriterion("auto_build is null");
            return (Criteria) this;
        }

        public Criteria andAutoBuildIsNotNull() {
            addCriterion("auto_build is not null");
            return (Criteria) this;
        }

        public Criteria andAutoBuildEqualTo(Integer value) {
            addCriterion("auto_build =", value, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildNotEqualTo(Integer value) {
            addCriterion("auto_build <>", value, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildGreaterThan(Integer value) {
            addCriterion("auto_build >", value, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildGreaterThanOrEqualTo(Integer value) {
            addCriterion("auto_build >=", value, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildLessThan(Integer value) {
            addCriterion("auto_build <", value, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildLessThanOrEqualTo(Integer value) {
            addCriterion("auto_build <=", value, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildIn(List<Integer> values) {
            addCriterion("auto_build in", values, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildNotIn(List<Integer> values) {
            addCriterion("auto_build not in", values, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildBetween(Integer value1, Integer value2) {
            addCriterion("auto_build between", value1, value2, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andAutoBuildNotBetween(Integer value1, Integer value2) {
            addCriterion("auto_build not between", value1, value2, "autoBuild");
            return (Criteria) this;
        }

        public Criteria andCycleDayIsNull() {
            addCriterion("cycle_day is null");
            return (Criteria) this;
        }

        public Criteria andCycleDayIsNotNull() {
            addCriterion("cycle_day is not null");
            return (Criteria) this;
        }

        public Criteria andCycleDayEqualTo(Integer value) {
            addCriterion("cycle_day =", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayNotEqualTo(Integer value) {
            addCriterion("cycle_day <>", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayGreaterThan(Integer value) {
            addCriterion("cycle_day >", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayGreaterThanOrEqualTo(Integer value) {
            addCriterion("cycle_day >=", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayLessThan(Integer value) {
            addCriterion("cycle_day <", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayLessThanOrEqualTo(Integer value) {
            addCriterion("cycle_day <=", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayIn(List<Integer> values) {
            addCriterion("cycle_day in", values, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayNotIn(List<Integer> values) {
            addCriterion("cycle_day not in", values, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayBetween(Integer value1, Integer value2) {
            addCriterion("cycle_day between", value1, value2, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayNotBetween(Integer value1, Integer value2) {
            addCriterion("cycle_day not between", value1, value2, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayIsNull() {
            addCriterion("cycle_end_day is null");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayIsNotNull() {
            addCriterion("cycle_end_day is not null");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayEqualTo(String value) {
            addCriterion("cycle_end_day =", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotEqualTo(String value) {
            addCriterion("cycle_end_day <>", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayGreaterThan(String value) {
            addCriterion("cycle_end_day >", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayGreaterThanOrEqualTo(String value) {
            addCriterion("cycle_end_day >=", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayLessThan(String value) {
            addCriterion("cycle_end_day <", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayLessThanOrEqualTo(String value) {
            addCriterion("cycle_end_day <=", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayLike(String value) {
            addCriterion("cycle_end_day like", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotLike(String value) {
            addCriterion("cycle_end_day not like", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayIn(List<String> values) {
            addCriterion("cycle_end_day in", values, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotIn(List<String> values) {
            addCriterion("cycle_end_day not in", values, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayBetween(String value1, String value2) {
            addCriterion("cycle_end_day between", value1, value2, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotBetween(String value1, String value2) {
            addCriterion("cycle_end_day not between", value1, value2, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNull() {
            addCriterion("push_type is null");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNotNull() {
            addCriterion("push_type is not null");
            return (Criteria) this;
        }

        public Criteria andPushTypeEqualTo(Integer value) {
            addCriterion("push_type =", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotEqualTo(Integer value) {
            addCriterion("push_type <>", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThan(Integer value) {
            addCriterion("push_type >", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_type >=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThan(Integer value) {
            addCriterion("push_type <", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThanOrEqualTo(Integer value) {
            addCriterion("push_type <=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeIn(List<Integer> values) {
            addCriterion("push_type in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotIn(List<Integer> values) {
            addCriterion("push_type not in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeBetween(Integer value1, Integer value2) {
            addCriterion("push_type between", value1, value2, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("push_type not between", value1, value2, "pushType");
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

        public Criteria andStrategyProductShowIsNull() {
            addCriterion("strategy_product_show is null");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowIsNotNull() {
            addCriterion("strategy_product_show is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowEqualTo(String value) {
            addCriterion("strategy_product_show =", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowNotEqualTo(String value) {
            addCriterion("strategy_product_show <>", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowGreaterThan(String value) {
            addCriterion("strategy_product_show >", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_product_show >=", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowLessThan(String value) {
            addCriterion("strategy_product_show <", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowLessThanOrEqualTo(String value) {
            addCriterion("strategy_product_show <=", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowLike(String value) {
            addCriterion("strategy_product_show like", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowNotLike(String value) {
            addCriterion("strategy_product_show not like", value, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowIn(List<String> values) {
            addCriterion("strategy_product_show in", values, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowNotIn(List<String> values) {
            addCriterion("strategy_product_show not in", values, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowBetween(String value1, String value2) {
            addCriterion("strategy_product_show between", value1, value2, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andStrategyProductShowNotBetween(String value1, String value2) {
            addCriterion("strategy_product_show not between", value1, value2, "strategyProductShow");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeIsNull() {
            addCriterion("threek_encrypt_type is null");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeIsNotNull() {
            addCriterion("threek_encrypt_type is not null");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeEqualTo(Integer value) {
            addCriterion("threek_encrypt_type =", value, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeNotEqualTo(Integer value) {
            addCriterion("threek_encrypt_type <>", value, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeGreaterThan(Integer value) {
            addCriterion("threek_encrypt_type >", value, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("threek_encrypt_type >=", value, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeLessThan(Integer value) {
            addCriterion("threek_encrypt_type <", value, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeLessThanOrEqualTo(Integer value) {
            addCriterion("threek_encrypt_type <=", value, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeIn(List<Integer> values) {
            addCriterion("threek_encrypt_type in", values, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeNotIn(List<Integer> values) {
            addCriterion("threek_encrypt_type not in", values, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeBetween(Integer value1, Integer value2) {
            addCriterion("threek_encrypt_type between", value1, value2, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreekEncryptTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("threek_encrypt_type not between", value1, value2, "threekEncryptType");
            return (Criteria) this;
        }

        public Criteria andIsOnlineIsNull() {
            addCriterion("is_online is null");
            return (Criteria) this;
        }

        public Criteria andIsOnlineIsNotNull() {
            addCriterion("is_online is not null");
            return (Criteria) this;
        }

        public Criteria andIsOnlineEqualTo(Integer value) {
            addCriterion("is_online =", value, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineNotEqualTo(Integer value) {
            addCriterion("is_online <>", value, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineGreaterThan(Integer value) {
            addCriterion("is_online >", value, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_online >=", value, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineLessThan(Integer value) {
            addCriterion("is_online <", value, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineLessThanOrEqualTo(Integer value) {
            addCriterion("is_online <=", value, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineIn(List<Integer> values) {
            addCriterion("is_online in", values, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineNotIn(List<Integer> values) {
            addCriterion("is_online not in", values, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineBetween(Integer value1, Integer value2) {
            addCriterion("is_online between", value1, value2, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsOnlineNotBetween(Integer value1, Integer value2) {
            addCriterion("is_online not between", value1, value2, "isOnline");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityIsNull() {
            addCriterion("is_stack_validity is null");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityIsNotNull() {
            addCriterion("is_stack_validity is not null");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityEqualTo(Integer value) {
            addCriterion("is_stack_validity =", value, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityNotEqualTo(Integer value) {
            addCriterion("is_stack_validity <>", value, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityGreaterThan(Integer value) {
            addCriterion("is_stack_validity >", value, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_stack_validity >=", value, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityLessThan(Integer value) {
            addCriterion("is_stack_validity <", value, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityLessThanOrEqualTo(Integer value) {
            addCriterion("is_stack_validity <=", value, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityIn(List<Integer> values) {
            addCriterion("is_stack_validity in", values, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityNotIn(List<Integer> values) {
            addCriterion("is_stack_validity not in", values, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityBetween(Integer value1, Integer value2) {
            addCriterion("is_stack_validity between", value1, value2, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andIsStackValidityNotBetween(Integer value1, Integer value2) {
            addCriterion("is_stack_validity not between", value1, value2, "isStackValidity");
            return (Criteria) this;
        }

        public Criteria andPriorityIsNull() {
            addCriterion("priority is null");
            return (Criteria) this;
        }

        public Criteria andPriorityIsNotNull() {
            addCriterion("priority is not null");
            return (Criteria) this;
        }

        public Criteria andPriorityEqualTo(Integer value) {
            addCriterion("priority =", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotEqualTo(Integer value) {
            addCriterion("priority <>", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThan(Integer value) {
            addCriterion("priority >", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThanOrEqualTo(Integer value) {
            addCriterion("priority >=", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityLessThan(Integer value) {
            addCriterion("priority <", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityLessThanOrEqualTo(Integer value) {
            addCriterion("priority <=", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityIn(List<Integer> values) {
            addCriterion("priority in", values, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotIn(List<Integer> values) {
            addCriterion("priority not in", values, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityBetween(Integer value1, Integer value2) {
            addCriterion("priority between", value1, value2, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotBetween(Integer value1, Integer value2) {
            addCriterion("priority not between", value1, value2, "priority");
            return (Criteria) this;
        }

        public Criteria andCheckStatusIsNull() {
            addCriterion("check_status is null");
            return (Criteria) this;
        }

        public Criteria andCheckStatusIsNotNull() {
            addCriterion("check_status is not null");
            return (Criteria) this;
        }

        public Criteria andCheckStatusEqualTo(Integer value) {
            addCriterion("check_status =", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusNotEqualTo(Integer value) {
            addCriterion("check_status <>", value, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusIn(List<Integer> values) {
            addCriterion("check_status in", values, "checkStatus");
            return (Criteria) this;
        }

        public Criteria andCheckStatusNotIn(List<Integer> values) {
            addCriterion("check_status not in", values, "checkStatus");
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