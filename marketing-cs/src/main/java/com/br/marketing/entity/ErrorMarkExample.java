package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ErrorMarkExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ErrorMarkExample() {
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

        public Criteria andMIdIsNull() {
            addCriterion("m_id is null");
            return (Criteria) this;
        }

        public Criteria andMIdIsNotNull() {
            addCriterion("m_id is not null");
            return (Criteria) this;
        }

        public Criteria andMIdEqualTo(Long value) {
            addCriterion("m_id =", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdNotEqualTo(Long value) {
            addCriterion("m_id <>", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdGreaterThan(Long value) {
            addCriterion("m_id >", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdGreaterThanOrEqualTo(Long value) {
            addCriterion("m_id >=", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdLessThan(Long value) {
            addCriterion("m_id <", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdLessThanOrEqualTo(Long value) {
            addCriterion("m_id <=", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdIn(List<Long> values) {
            addCriterion("m_id in", values, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdNotIn(List<Long> values) {
            addCriterion("m_id not in", values, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdBetween(Long value1, Long value2) {
            addCriterion("m_id between", value1, value2, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdNotBetween(Long value1, Long value2) {
            addCriterion("m_id not between", value1, value2, "mId");
            return (Criteria) this;
        }

        public Criteria andPartIsNull() {
            addCriterion("`part` is null");
            return (Criteria) this;
        }

        public Criteria andPartIsNotNull() {
            addCriterion("`part` is not null");
            return (Criteria) this;
        }

        public Criteria andPartEqualTo(String value) {
            addCriterion("`part` =", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartNotEqualTo(String value) {
            addCriterion("`part` <>", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartGreaterThan(String value) {
            addCriterion("`part` >", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartGreaterThanOrEqualTo(String value) {
            addCriterion("`part` >=", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartLessThan(String value) {
            addCriterion("`part` <", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartLessThanOrEqualTo(String value) {
            addCriterion("`part` <=", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartLike(String value) {
            addCriterion("`part` like", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartNotLike(String value) {
            addCriterion("`part` not like", value, "part");
            return (Criteria) this;
        }

        public Criteria andPartIn(List<String> values) {
            addCriterion("`part` in", values, "part");
            return (Criteria) this;
        }

        public Criteria andPartNotIn(List<String> values) {
            addCriterion("`part` not in", values, "part");
            return (Criteria) this;
        }

        public Criteria andPartBetween(String value1, String value2) {
            addCriterion("`part` between", value1, value2, "part");
            return (Criteria) this;
        }

        public Criteria andPartNotBetween(String value1, String value2) {
            addCriterion("`part` not between", value1, value2, "part");
            return (Criteria) this;
        }

        public Criteria andPageSizeIsNull() {
            addCriterion("page_size is null");
            return (Criteria) this;
        }

        public Criteria andPageSizeIsNotNull() {
            addCriterion("page_size is not null");
            return (Criteria) this;
        }

        public Criteria andPageSizeEqualTo(Integer value) {
            addCriterion("page_size =", value, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeNotEqualTo(Integer value) {
            addCriterion("page_size <>", value, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeGreaterThan(Integer value) {
            addCriterion("page_size >", value, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("page_size >=", value, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeLessThan(Integer value) {
            addCriterion("page_size <", value, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeLessThanOrEqualTo(Integer value) {
            addCriterion("page_size <=", value, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeIn(List<Integer> values) {
            addCriterion("page_size in", values, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeNotIn(List<Integer> values) {
            addCriterion("page_size not in", values, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeBetween(Integer value1, Integer value2) {
            addCriterion("page_size between", value1, value2, "pageSize");
            return (Criteria) this;
        }

        public Criteria andPageSizeNotBetween(Integer value1, Integer value2) {
            addCriterion("page_size not between", value1, value2, "pageSize");
            return (Criteria) this;
        }

        public Criteria andSearchAfterIsNull() {
            addCriterion("search_after is null");
            return (Criteria) this;
        }

        public Criteria andSearchAfterIsNotNull() {
            addCriterion("search_after is not null");
            return (Criteria) this;
        }

        public Criteria andSearchAfterEqualTo(String value) {
            addCriterion("search_after =", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterNotEqualTo(String value) {
            addCriterion("search_after <>", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterGreaterThan(String value) {
            addCriterion("search_after >", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterGreaterThanOrEqualTo(String value) {
            addCriterion("search_after >=", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterLessThan(String value) {
            addCriterion("search_after <", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterLessThanOrEqualTo(String value) {
            addCriterion("search_after <=", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterLike(String value) {
            addCriterion("search_after like", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterNotLike(String value) {
            addCriterion("search_after not like", value, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterIn(List<String> values) {
            addCriterion("search_after in", values, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterNotIn(List<String> values) {
            addCriterion("search_after not in", values, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterBetween(String value1, String value2) {
            addCriterion("search_after between", value1, value2, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andSearchAfterNotBetween(String value1, String value2) {
            addCriterion("search_after not between", value1, value2, "searchAfter");
            return (Criteria) this;
        }

        public Criteria andEsConditionIsNull() {
            addCriterion("es_condition is null");
            return (Criteria) this;
        }

        public Criteria andEsConditionIsNotNull() {
            addCriterion("es_condition is not null");
            return (Criteria) this;
        }

        public Criteria andEsConditionEqualTo(String value) {
            addCriterion("es_condition =", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionNotEqualTo(String value) {
            addCriterion("es_condition <>", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionGreaterThan(String value) {
            addCriterion("es_condition >", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionGreaterThanOrEqualTo(String value) {
            addCriterion("es_condition >=", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionLessThan(String value) {
            addCriterion("es_condition <", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionLessThanOrEqualTo(String value) {
            addCriterion("es_condition <=", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionLike(String value) {
            addCriterion("es_condition like", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionNotLike(String value) {
            addCriterion("es_condition not like", value, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionIn(List<String> values) {
            addCriterion("es_condition in", values, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionNotIn(List<String> values) {
            addCriterion("es_condition not in", values, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionBetween(String value1, String value2) {
            addCriterion("es_condition between", value1, value2, "esCondition");
            return (Criteria) this;
        }

        public Criteria andEsConditionNotBetween(String value1, String value2) {
            addCriterion("es_condition not between", value1, value2, "esCondition");
            return (Criteria) this;
        }

        public Criteria andAccessNumberIsNull() {
            addCriterion("access_number is null");
            return (Criteria) this;
        }

        public Criteria andAccessNumberIsNotNull() {
            addCriterion("access_number is not null");
            return (Criteria) this;
        }

        public Criteria andAccessNumberEqualTo(String value) {
            addCriterion("access_number =", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotEqualTo(String value) {
            addCriterion("access_number <>", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberGreaterThan(String value) {
            addCriterion("access_number >", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberGreaterThanOrEqualTo(String value) {
            addCriterion("access_number >=", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberLessThan(String value) {
            addCriterion("access_number <", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberLessThanOrEqualTo(String value) {
            addCriterion("access_number <=", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberLike(String value) {
            addCriterion("access_number like", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotLike(String value) {
            addCriterion("access_number not like", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberIn(List<String> values) {
            addCriterion("access_number in", values, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotIn(List<String> values) {
            addCriterion("access_number not in", values, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberBetween(String value1, String value2) {
            addCriterion("access_number between", value1, value2, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotBetween(String value1, String value2) {
            addCriterion("access_number not between", value1, value2, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andPushSizeIsNull() {
            addCriterion("push_size is null");
            return (Criteria) this;
        }

        public Criteria andPushSizeIsNotNull() {
            addCriterion("push_size is not null");
            return (Criteria) this;
        }

        public Criteria andPushSizeEqualTo(Integer value) {
            addCriterion("push_size =", value, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeNotEqualTo(Integer value) {
            addCriterion("push_size <>", value, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeGreaterThan(Integer value) {
            addCriterion("push_size >", value, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_size >=", value, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeLessThan(Integer value) {
            addCriterion("push_size <", value, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeLessThanOrEqualTo(Integer value) {
            addCriterion("push_size <=", value, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeIn(List<Integer> values) {
            addCriterion("push_size in", values, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeNotIn(List<Integer> values) {
            addCriterion("push_size not in", values, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeBetween(Integer value1, Integer value2) {
            addCriterion("push_size between", value1, value2, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPushSizeNotBetween(Integer value1, Integer value2) {
            addCriterion("push_size not between", value1, value2, "pushSize");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionIsNull() {
            addCriterion("policy_condition is null");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionIsNotNull() {
            addCriterion("policy_condition is not null");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionEqualTo(String value) {
            addCriterion("policy_condition =", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionNotEqualTo(String value) {
            addCriterion("policy_condition <>", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionGreaterThan(String value) {
            addCriterion("policy_condition >", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionGreaterThanOrEqualTo(String value) {
            addCriterion("policy_condition >=", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionLessThan(String value) {
            addCriterion("policy_condition <", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionLessThanOrEqualTo(String value) {
            addCriterion("policy_condition <=", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionLike(String value) {
            addCriterion("policy_condition like", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionNotLike(String value) {
            addCriterion("policy_condition not like", value, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionIn(List<String> values) {
            addCriterion("policy_condition in", values, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionNotIn(List<String> values) {
            addCriterion("policy_condition not in", values, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionBetween(String value1, String value2) {
            addCriterion("policy_condition between", value1, value2, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andPolicyConditionNotBetween(String value1, String value2) {
            addCriterion("policy_condition not between", value1, value2, "policyCondition");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsIsNull() {
            addCriterion("retry_total_attempts is null");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsIsNotNull() {
            addCriterion("retry_total_attempts is not null");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsEqualTo(Integer value) {
            addCriterion("retry_total_attempts =", value, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsNotEqualTo(Integer value) {
            addCriterion("retry_total_attempts <>", value, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsGreaterThan(Integer value) {
            addCriterion("retry_total_attempts >", value, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_total_attempts >=", value, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsLessThan(Integer value) {
            addCriterion("retry_total_attempts <", value, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsLessThanOrEqualTo(Integer value) {
            addCriterion("retry_total_attempts <=", value, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsIn(List<Integer> values) {
            addCriterion("retry_total_attempts in", values, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsNotIn(List<Integer> values) {
            addCriterion("retry_total_attempts not in", values, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsBetween(Integer value1, Integer value2) {
            addCriterion("retry_total_attempts between", value1, value2, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryTotalAttemptsNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_total_attempts not between", value1, value2, "retryTotalAttempts");
            return (Criteria) this;
        }

        public Criteria andRetryStatusIsNull() {
            addCriterion("retry_status is null");
            return (Criteria) this;
        }

        public Criteria andRetryStatusIsNotNull() {
            addCriterion("retry_status is not null");
            return (Criteria) this;
        }

        public Criteria andRetryStatusEqualTo(Integer value) {
            addCriterion("retry_status =", value, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusNotEqualTo(Integer value) {
            addCriterion("retry_status <>", value, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusGreaterThan(Integer value) {
            addCriterion("retry_status >", value, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_status >=", value, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusLessThan(Integer value) {
            addCriterion("retry_status <", value, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusLessThanOrEqualTo(Integer value) {
            addCriterion("retry_status <=", value, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusIn(List<Integer> values) {
            addCriterion("retry_status in", values, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusNotIn(List<Integer> values) {
            addCriterion("retry_status not in", values, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusBetween(Integer value1, Integer value2) {
            addCriterion("retry_status between", value1, value2, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andRetryStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_status not between", value1, value2, "retryStatus");
            return (Criteria) this;
        }

        public Criteria andFilterTypeIsNull() {
            addCriterion("filter_type is null");
            return (Criteria) this;
        }

        public Criteria andFilterTypeIsNotNull() {
            addCriterion("filter_type is not null");
            return (Criteria) this;
        }

        public Criteria andFilterTypeEqualTo(Integer value) {
            addCriterion("filter_type =", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeNotEqualTo(Integer value) {
            addCriterion("filter_type <>", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeGreaterThan(Integer value) {
            addCriterion("filter_type >", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("filter_type >=", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeLessThan(Integer value) {
            addCriterion("filter_type <", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeLessThanOrEqualTo(Integer value) {
            addCriterion("filter_type <=", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeIn(List<Integer> values) {
            addCriterion("filter_type in", values, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeNotIn(List<Integer> values) {
            addCriterion("filter_type not in", values, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeBetween(Integer value1, Integer value2) {
            addCriterion("filter_type between", value1, value2, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("filter_type not between", value1, value2, "filterType");
            return (Criteria) this;
        }

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(Integer value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(Integer value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(Integer value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(Integer value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(Integer value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<Integer> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<Integer> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(Integer value1, Integer value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andAppletDateIsNull() {
            addCriterion("applet_date is null");
            return (Criteria) this;
        }

        public Criteria andAppletDateIsNotNull() {
            addCriterion("applet_date is not null");
            return (Criteria) this;
        }

        public Criteria andAppletDateEqualTo(String value) {
            addCriterion("applet_date =", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotEqualTo(String value) {
            addCriterion("applet_date <>", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThan(String value) {
            addCriterion("applet_date >", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThanOrEqualTo(String value) {
            addCriterion("applet_date >=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThan(String value) {
            addCriterion("applet_date <", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThanOrEqualTo(String value) {
            addCriterion("applet_date <=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLike(String value) {
            addCriterion("applet_date like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotLike(String value) {
            addCriterion("applet_date not like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateIn(List<String> values) {
            addCriterion("applet_date in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotIn(List<String> values) {
            addCriterion("applet_date not in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateBetween(String value1, String value2) {
            addCriterion("applet_date between", value1, value2, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotBetween(String value1, String value2) {
            addCriterion("applet_date not between", value1, value2, "appletDate");
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