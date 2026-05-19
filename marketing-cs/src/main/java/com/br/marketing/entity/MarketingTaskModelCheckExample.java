package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingTaskModelCheckExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingTaskModelCheckExample() {
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
        public Criteria andBatchNumberIsNull() {
            addCriterion("batch_number is null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberIsNotNull() {
            addCriterion("batch_number is not null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberEqualTo(String value) {
            addCriterion("batch_number =", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotEqualTo(String value) {
            addCriterion("batch_number <>", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberGreaterThan(String value) {
            addCriterion("batch_number >", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("batch_number >=", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLessThan(String value) {
            addCriterion("batch_number <", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("batch_number <=", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLike(String value) {
            addCriterion("batch_number like", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotLike(String value) {
            addCriterion("batch_number not like", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberIn(List<String> values) {
            addCriterion("batch_number in", values, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotIn(List<String> values) {
            addCriterion("batch_number not in", values, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberBetween(String value1, String value2) {
            addCriterion("batch_number between", value1, value2, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotBetween(String value1, String value2) {
            addCriterion("batch_number not between", value1, value2, "batchNumber");
            return (Criteria) this;
        }
        public Criteria andCusBatchIsNull() {
            addCriterion("cus_batch is null");
            return (Criteria) this;
        }

        public Criteria andCusBatchIsNotNull() {
            addCriterion("cus_batch is not null");
            return (Criteria) this;
        }

        public Criteria andCusBatchEqualTo(String value) {
            addCriterion("cus_batch =", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotEqualTo(String value) {
            addCriterion("cus_batch <>", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchGreaterThan(String value) {
            addCriterion("cus_batch >", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchGreaterThanOrEqualTo(String value) {
            addCriterion("cus_batch >=", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchLessThan(String value) {
            addCriterion("cus_batch <", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchLessThanOrEqualTo(String value) {
            addCriterion("cus_batch <=", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchLike(String value) {
            addCriterion("cus_batch like", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotLike(String value) {
            addCriterion("cus_batch not like", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchIn(List<String> values) {
            addCriterion("cus_batch in", values, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotIn(List<String> values) {
            addCriterion("cus_batch not in", values, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchBetween(String value1, String value2) {
            addCriterion("cus_batch between", value1, value2, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotBetween(String value1, String value2) {
            addCriterion("cus_batch not between", value1, value2, "cusBatch");
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
        public Criteria andModelCheckStatusIsNull() {
            addCriterion("model_check_status is null");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusIsNotNull() {
            addCriterion("model_check_status is not null");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusEqualTo(Integer value) {
            addCriterion("model_check_status =", value, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusNotEqualTo(Integer value) {
            addCriterion("model_check_status <>", value, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusGreaterThan(Integer value) {
            addCriterion("model_check_status >", value, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("model_check_status >=", value, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusLessThan(Integer value) {
            addCriterion("model_check_status <", value, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusLessThanOrEqualTo(Integer value) {
            addCriterion("model_check_status <=", value, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusIn(List<Integer> values) {
            addCriterion("model_check_status in", values, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusNotIn(List<Integer> values) {
            addCriterion("model_check_status not in", values, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusBetween(Integer value1, Integer value2) {
            addCriterion("model_check_status between", value1, value2, "modelCheckStatus");
            return (Criteria) this;
        }

        public Criteria andModelCheckStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("model_check_status not between", value1, value2, "modelCheckStatus");
            return (Criteria) this;
        }
        public Criteria andFailedModelInfoIsNull() {
            addCriterion("failed_model_info is null");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoIsNotNull() {
            addCriterion("failed_model_info is not null");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoEqualTo(String value) {
            addCriterion("failed_model_info =", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoNotEqualTo(String value) {
            addCriterion("failed_model_info <>", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoGreaterThan(String value) {
            addCriterion("failed_model_info >", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoGreaterThanOrEqualTo(String value) {
            addCriterion("failed_model_info >=", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoLessThan(String value) {
            addCriterion("failed_model_info <", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoLessThanOrEqualTo(String value) {
            addCriterion("failed_model_info <=", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoLike(String value) {
            addCriterion("failed_model_info like", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoNotLike(String value) {
            addCriterion("failed_model_info not like", value, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoIn(List<String> values) {
            addCriterion("failed_model_info in", values, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoNotIn(List<String> values) {
            addCriterion("failed_model_info not in", values, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoBetween(String value1, String value2) {
            addCriterion("failed_model_info between", value1, value2, "failedModelInfo");
            return (Criteria) this;
        }

        public Criteria andFailedModelInfoNotBetween(String value1, String value2) {
            addCriterion("failed_model_info not between", value1, value2, "failedModelInfo");
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
