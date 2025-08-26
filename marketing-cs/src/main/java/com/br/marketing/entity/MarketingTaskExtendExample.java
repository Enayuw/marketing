package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingTaskExtendExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingTaskExtendExample() {
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

        public Criteria andTaskIdIsNull() {
            addCriterion("task_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNotNull() {
            addCriterion("task_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdEqualTo(Long value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(Long value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(Long value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(Long value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(Long value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(Long value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<Long> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<Long> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(Long value1, Long value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(Long value1, Long value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdIsNull() {
            addCriterion("cus_task_id is null");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdIsNotNull() {
            addCriterion("cus_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdEqualTo(String value) {
            addCriterion("cus_task_id =", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdNotEqualTo(String value) {
            addCriterion("cus_task_id <>", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdGreaterThan(String value) {
            addCriterion("cus_task_id >", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("cus_task_id >=", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdLessThan(String value) {
            addCriterion("cus_task_id <", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdLessThanOrEqualTo(String value) {
            addCriterion("cus_task_id <=", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdLike(String value) {
            addCriterion("cus_task_id like", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdNotLike(String value) {
            addCriterion("cus_task_id not like", value, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdIn(List<String> values) {
            addCriterion("cus_task_id in", values, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdNotIn(List<String> values) {
            addCriterion("cus_task_id not in", values, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdBetween(String value1, String value2) {
            addCriterion("cus_task_id between", value1, value2, "cusTaskId");
            return (Criteria) this;
        }

        public Criteria andCusTaskIdNotBetween(String value1, String value2) {
            addCriterion("cus_task_id not between", value1, value2, "cusTaskId");
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

        public Criteria andGroupTypeIsNull() {
            addCriterion("group_type is null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIsNotNull() {
            addCriterion("group_type is not null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeEqualTo(String value) {
            addCriterion("group_type =", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotEqualTo(String value) {
            addCriterion("group_type <>", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThan(String value) {
            addCriterion("group_type >", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThanOrEqualTo(String value) {
            addCriterion("group_type >=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThan(String value) {
            addCriterion("group_type <", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThanOrEqualTo(String value) {
            addCriterion("group_type <=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLike(String value) {
            addCriterion("group_type like", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotLike(String value) {
            addCriterion("group_type not like", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIn(List<String> values) {
            addCriterion("group_type in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotIn(List<String> values) {
            addCriterion("group_type not in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeBetween(String value1, String value2) {
            addCriterion("group_type between", value1, value2, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotBetween(String value1, String value2) {
            addCriterion("group_type not between", value1, value2, "groupType");
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

        public Criteria andUploadTimeIsNull() {
            addCriterion("upload_time is null");
            return (Criteria) this;
        }

        public Criteria andUploadTimeIsNotNull() {
            addCriterion("upload_time is not null");
            return (Criteria) this;
        }

        public Criteria andUploadTimeEqualTo(String value) {
            addCriterion("upload_time =", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotEqualTo(String value) {
            addCriterion("upload_time <>", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeGreaterThan(String value) {
            addCriterion("upload_time >", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeGreaterThanOrEqualTo(String value) {
            addCriterion("upload_time >=", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeLessThan(String value) {
            addCriterion("upload_time <", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeLessThanOrEqualTo(String value) {
            addCriterion("upload_time <=", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeLike(String value) {
            addCriterion("upload_time like", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotLike(String value) {
            addCriterion("upload_time not like", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeIn(List<String> values) {
            addCriterion("upload_time in", values, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotIn(List<String> values) {
            addCriterion("upload_time not in", values, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeBetween(String value1, String value2) {
            addCriterion("upload_time between", value1, value2, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotBetween(String value1, String value2) {
            addCriterion("upload_time not between", value1, value2, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleIsNull() {
            addCriterion("extend_show_title is null");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleIsNotNull() {
            addCriterion("extend_show_title is not null");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleEqualTo(String value) {
            addCriterion("extend_show_title =", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleNotEqualTo(String value) {
            addCriterion("extend_show_title <>", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleGreaterThan(String value) {
            addCriterion("extend_show_title >", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleGreaterThanOrEqualTo(String value) {
            addCriterion("extend_show_title >=", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleLessThan(String value) {
            addCriterion("extend_show_title <", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleLessThanOrEqualTo(String value) {
            addCriterion("extend_show_title <=", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleLike(String value) {
            addCriterion("extend_show_title like", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleNotLike(String value) {
            addCriterion("extend_show_title not like", value, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleIn(List<String> values) {
            addCriterion("extend_show_title in", values, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleNotIn(List<String> values) {
            addCriterion("extend_show_title not in", values, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleBetween(String value1, String value2) {
            addCriterion("extend_show_title between", value1, value2, "extendShowTitle");
            return (Criteria) this;
        }

        public Criteria andExtendShowTitleNotBetween(String value1, String value2) {
            addCriterion("extend_show_title not between", value1, value2, "extendShowTitle");
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

        public Criteria andConditionTypeIsNull() {
            addCriterion("condition_type is null");
            return (Criteria) this;
        }

        public Criteria andConditionTypeIsNotNull() {
            addCriterion("condition_type is not null");
            return (Criteria) this;
        }

        public Criteria andConditionTypeEqualTo(Integer value) {
            addCriterion("condition_type =", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotEqualTo(Integer value) {
            addCriterion("condition_type <>", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeGreaterThan(Integer value) {
            addCriterion("condition_type >", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("condition_type >=", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeLessThan(Integer value) {
            addCriterion("condition_type <", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeLessThanOrEqualTo(Integer value) {
            addCriterion("condition_type <=", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeIn(List<Integer> values) {
            addCriterion("condition_type in", values, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotIn(List<Integer> values) {
            addCriterion("condition_type not in", values, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeBetween(Integer value1, Integer value2) {
            addCriterion("condition_type between", value1, value2, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("condition_type not between", value1, value2, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowIsNull() {
            addCriterion("condition_info_show is null");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowIsNotNull() {
            addCriterion("condition_info_show is not null");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowEqualTo(String value) {
            addCriterion("condition_info_show =", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowNotEqualTo(String value) {
            addCriterion("condition_info_show <>", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowGreaterThan(String value) {
            addCriterion("condition_info_show >", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowGreaterThanOrEqualTo(String value) {
            addCriterion("condition_info_show >=", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowLessThan(String value) {
            addCriterion("condition_info_show <", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowLessThanOrEqualTo(String value) {
            addCriterion("condition_info_show <=", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowLike(String value) {
            addCriterion("condition_info_show like", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowNotLike(String value) {
            addCriterion("condition_info_show not like", value, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowIn(List<String> values) {
            addCriterion("condition_info_show in", values, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowNotIn(List<String> values) {
            addCriterion("condition_info_show not in", values, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowBetween(String value1, String value2) {
            addCriterion("condition_info_show between", value1, value2, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andConditionInfoShowNotBetween(String value1, String value2) {
            addCriterion("condition_info_show not between", value1, value2, "conditionInfoShow");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoIsNull() {
            addCriterion("extend_config_info is null");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoIsNotNull() {
            addCriterion("extend_config_info is not null");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoEqualTo(String value) {
            addCriterion("extend_config_info =", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotEqualTo(String value) {
            addCriterion("extend_config_info <>", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoGreaterThan(String value) {
            addCriterion("extend_config_info >", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoGreaterThanOrEqualTo(String value) {
            addCriterion("extend_config_info >=", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoLessThan(String value) {
            addCriterion("extend_config_info <", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoLessThanOrEqualTo(String value) {
            addCriterion("extend_config_info <=", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoLike(String value) {
            addCriterion("extend_config_info like", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotLike(String value) {
            addCriterion("extend_config_info not like", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoIn(List<String> values) {
            addCriterion("extend_config_info in", values, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotIn(List<String> values) {
            addCriterion("extend_config_info not in", values, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoBetween(String value1, String value2) {
            addCriterion("extend_config_info between", value1, value2, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotBetween(String value1, String value2) {
            addCriterion("extend_config_info not between", value1, value2, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andLabelNameIsNull() {
            addCriterion("label_name is null");
            return (Criteria) this;
        }

        public Criteria andLabelNameIsNotNull() {
            addCriterion("label_name is not null");
            return (Criteria) this;
        }

        public Criteria andLabelNameEqualTo(String value) {
            addCriterion("label_name =", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotEqualTo(String value) {
            addCriterion("label_name <>", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameGreaterThan(String value) {
            addCriterion("label_name >", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameGreaterThanOrEqualTo(String value) {
            addCriterion("label_name >=", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameLessThan(String value) {
            addCriterion("label_name <", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameLessThanOrEqualTo(String value) {
            addCriterion("label_name <=", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameLike(String value) {
            addCriterion("label_name like", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotLike(String value) {
            addCriterion("label_name not like", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameIn(List<String> values) {
            addCriterion("label_name in", values, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotIn(List<String> values) {
            addCriterion("label_name not in", values, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameBetween(String value1, String value2) {
            addCriterion("label_name between", value1, value2, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotBetween(String value1, String value2) {
            addCriterion("label_name not between", value1, value2, "labelName");
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