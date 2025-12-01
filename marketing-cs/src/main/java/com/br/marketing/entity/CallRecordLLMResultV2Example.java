package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallRecordLLMResultV2Example {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CallRecordLLMResultV2Example() {
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

        public Criteria andCidIsNull() {
            addCriterion("cid is null");
            return (Criteria) this;
        }

        public Criteria andCidIsNotNull() {
            addCriterion("cid is not null");
            return (Criteria) this;
        }

        public Criteria andCidEqualTo(String value) {
            addCriterion("cid =", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotEqualTo(String value) {
            addCriterion("cid <>", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidGreaterThan(String value) {
            addCriterion("cid >", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidGreaterThanOrEqualTo(String value) {
            addCriterion("cid >=", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLessThan(String value) {
            addCriterion("cid <", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLessThanOrEqualTo(String value) {
            addCriterion("cid <=", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLike(String value) {
            addCriterion("cid like", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotLike(String value) {
            addCriterion("cid not like", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidIn(List<String> values) {
            addCriterion("cid in", values, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotIn(List<String> values) {
            addCriterion("cid not in", values, "cid");
            return (Criteria) this;
        }

        public Criteria andCidBetween(String value1, String value2) {
            addCriterion("cid between", value1, value2, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotBetween(String value1, String value2) {
            addCriterion("cid not between", value1, value2, "cid");
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

        public Criteria andCallBackTypeIsNull() {
            addCriterion("call_back_type is null");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeIsNotNull() {
            addCriterion("call_back_type is not null");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeEqualTo(Integer value) {
            addCriterion("call_back_type =", value, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeNotEqualTo(Integer value) {
            addCriterion("call_back_type <>", value, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeGreaterThan(Integer value) {
            addCriterion("call_back_type >", value, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("call_back_type >=", value, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeLessThan(Integer value) {
            addCriterion("call_back_type <", value, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeLessThanOrEqualTo(Integer value) {
            addCriterion("call_back_type <=", value, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeIn(List<Integer> values) {
            addCriterion("call_back_type in", values, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeNotIn(List<Integer> values) {
            addCriterion("call_back_type not in", values, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeBetween(Integer value1, Integer value2) {
            addCriterion("call_back_type between", value1, value2, "callBackType");
            return (Criteria) this;
        }

        public Criteria andCallBackTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("call_back_type not between", value1, value2, "callBackType");
            return (Criteria) this;
        }

        public Criteria andTaskNameIsNull() {
            addCriterion("task_name is null");
            return (Criteria) this;
        }

        public Criteria andTaskNameIsNotNull() {
            addCriterion("task_name is not null");
            return (Criteria) this;
        }

        public Criteria andTaskNameEqualTo(String value) {
            addCriterion("task_name =", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotEqualTo(String value) {
            addCriterion("task_name <>", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThan(String value) {
            addCriterion("task_name >", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThanOrEqualTo(String value) {
            addCriterion("task_name >=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThan(String value) {
            addCriterion("task_name <", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThanOrEqualTo(String value) {
            addCriterion("task_name <=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLike(String value) {
            addCriterion("task_name like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotLike(String value) {
            addCriterion("task_name not like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameIn(List<String> values) {
            addCriterion("task_name in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotIn(List<String> values) {
            addCriterion("task_name not in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameBetween(String value1, String value2) {
            addCriterion("task_name between", value1, value2, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotBetween(String value1, String value2) {
            addCriterion("task_name not between", value1, value2, "taskName");
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

        public Criteria andTaskIdEqualTo(Integer value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(Integer value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(Integer value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(Integer value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(Integer value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<Integer> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<Integer> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(Integer value1, Integer value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(Integer value1, Integer value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andCustNumIsNull() {
            addCriterion("cust_num is null");
            return (Criteria) this;
        }

        public Criteria andCustNumIsNotNull() {
            addCriterion("cust_num is not null");
            return (Criteria) this;
        }

        public Criteria andCustNumEqualTo(String value) {
            addCriterion("cust_num =", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotEqualTo(String value) {
            addCriterion("cust_num <>", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumGreaterThan(String value) {
            addCriterion("cust_num >", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumGreaterThanOrEqualTo(String value) {
            addCriterion("cust_num >=", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLessThan(String value) {
            addCriterion("cust_num <", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLessThanOrEqualTo(String value) {
            addCriterion("cust_num <=", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLike(String value) {
            addCriterion("cust_num like", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotLike(String value) {
            addCriterion("cust_num not like", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumIn(List<String> values) {
            addCriterion("cust_num in", values, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotIn(List<String> values) {
            addCriterion("cust_num not in", values, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumBetween(String value1, String value2) {
            addCriterion("cust_num between", value1, value2, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotBetween(String value1, String value2) {
            addCriterion("cust_num not between", value1, value2, "custNum");
            return (Criteria) this;
        }

        public Criteria andDetailIsNull() {
            addCriterion("detail is null");
            return (Criteria) this;
        }

        public Criteria andDetailIsNotNull() {
            addCriterion("detail is not null");
            return (Criteria) this;
        }

        public Criteria andDetailEqualTo(Object value) {
            addCriterion("detail =", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailNotEqualTo(Object value) {
            addCriterion("detail <>", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailGreaterThan(Object value) {
            addCriterion("detail >", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailGreaterThanOrEqualTo(Object value) {
            addCriterion("detail >=", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailLessThan(Object value) {
            addCriterion("detail <", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailLessThanOrEqualTo(Object value) {
            addCriterion("detail <=", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailIn(List<Object> values) {
            addCriterion("detail in", values, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailNotIn(List<Object> values) {
            addCriterion("detail not in", values, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailBetween(Object value1, Object value2) {
            addCriterion("detail between", value1, value2, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailNotBetween(Object value1, Object value2) {
            addCriterion("detail not between", value1, value2, "detail");
            return (Criteria) this;
        }

        public Criteria andSessionIdIsNull() {
            addCriterion("session_id is null");
            return (Criteria) this;
        }

        public Criteria andSessionIdIsNotNull() {
            addCriterion("session_id is not null");
            return (Criteria) this;
        }

        public Criteria andSessionIdEqualTo(String value) {
            addCriterion("session_id =", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdNotEqualTo(String value) {
            addCriterion("session_id <>", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdGreaterThan(String value) {
            addCriterion("session_id >", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdGreaterThanOrEqualTo(String value) {
            addCriterion("session_id >=", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdLessThan(String value) {
            addCriterion("session_id <", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdLessThanOrEqualTo(String value) {
            addCriterion("session_id <=", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdLike(String value) {
            addCriterion("session_id like", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdNotLike(String value) {
            addCriterion("session_id not like", value, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdIn(List<String> values) {
            addCriterion("session_id in", values, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdNotIn(List<String> values) {
            addCriterion("session_id not in", values, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdBetween(String value1, String value2) {
            addCriterion("session_id between", value1, value2, "sessionId");
            return (Criteria) this;
        }

        public Criteria andSessionIdNotBetween(String value1, String value2) {
            addCriterion("session_id not between", value1, value2, "sessionId");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeIsNull() {
            addCriterion("call_start_time is null");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeIsNotNull() {
            addCriterion("call_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeEqualTo(Long value) {
            addCriterion("call_start_time =", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotEqualTo(Long value) {
            addCriterion("call_start_time <>", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeGreaterThan(Long value) {
            addCriterion("call_start_time >", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("call_start_time >=", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeLessThan(Long value) {
            addCriterion("call_start_time <", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeLessThanOrEqualTo(Long value) {
            addCriterion("call_start_time <=", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeIn(List<Long> values) {
            addCriterion("call_start_time in", values, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotIn(List<Long> values) {
            addCriterion("call_start_time not in", values, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeBetween(Long value1, Long value2) {
            addCriterion("call_start_time between", value1, value2, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotBetween(Long value1, Long value2) {
            addCriterion("call_start_time not between", value1, value2, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeIsNull() {
            addCriterion("call_connect_time is null");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeIsNotNull() {
            addCriterion("call_connect_time is not null");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeEqualTo(Long value) {
            addCriterion("call_connect_time =", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotEqualTo(Long value) {
            addCriterion("call_connect_time <>", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeGreaterThan(Long value) {
            addCriterion("call_connect_time >", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("call_connect_time >=", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeLessThan(Long value) {
            addCriterion("call_connect_time <", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeLessThanOrEqualTo(Long value) {
            addCriterion("call_connect_time <=", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeIn(List<Long> values) {
            addCriterion("call_connect_time in", values, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotIn(List<Long> values) {
            addCriterion("call_connect_time not in", values, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeBetween(Long value1, Long value2) {
            addCriterion("call_connect_time between", value1, value2, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotBetween(Long value1, Long value2) {
            addCriterion("call_connect_time not between", value1, value2, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeIsNull() {
            addCriterion("call_end_time is null");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeIsNotNull() {
            addCriterion("call_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeEqualTo(Long value) {
            addCriterion("call_end_time =", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotEqualTo(Long value) {
            addCriterion("call_end_time <>", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeGreaterThan(Long value) {
            addCriterion("call_end_time >", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeGreaterThanOrEqualTo(Long value) {
            addCriterion("call_end_time >=", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeLessThan(Long value) {
            addCriterion("call_end_time <", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeLessThanOrEqualTo(Long value) {
            addCriterion("call_end_time <=", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeIn(List<Long> values) {
            addCriterion("call_end_time in", values, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotIn(List<Long> values) {
            addCriterion("call_end_time not in", values, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeBetween(Long value1, Long value2) {
            addCriterion("call_end_time between", value1, value2, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotBetween(Long value1, Long value2) {
            addCriterion("call_end_time not between", value1, value2, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andDialogTurnIsNull() {
            addCriterion("dialog_turn is null");
            return (Criteria) this;
        }

        public Criteria andDialogTurnIsNotNull() {
            addCriterion("dialog_turn is not null");
            return (Criteria) this;
        }

        public Criteria andDialogTurnEqualTo(Integer value) {
            addCriterion("dialog_turn =", value, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnNotEqualTo(Integer value) {
            addCriterion("dialog_turn <>", value, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnGreaterThan(Integer value) {
            addCriterion("dialog_turn >", value, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnGreaterThanOrEqualTo(Integer value) {
            addCriterion("dialog_turn >=", value, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnLessThan(Integer value) {
            addCriterion("dialog_turn <", value, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnLessThanOrEqualTo(Integer value) {
            addCriterion("dialog_turn <=", value, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnIn(List<Integer> values) {
            addCriterion("dialog_turn in", values, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnNotIn(List<Integer> values) {
            addCriterion("dialog_turn not in", values, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnBetween(Integer value1, Integer value2) {
            addCriterion("dialog_turn between", value1, value2, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andDialogTurnNotBetween(Integer value1, Integer value2) {
            addCriterion("dialog_turn not between", value1, value2, "dialogTurn");
            return (Criteria) this;
        }

        public Criteria andCallStatusIsNull() {
            addCriterion("call_status is null");
            return (Criteria) this;
        }

        public Criteria andCallStatusIsNotNull() {
            addCriterion("call_status is not null");
            return (Criteria) this;
        }

        public Criteria andCallStatusEqualTo(Integer value) {
            addCriterion("call_status =", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusNotEqualTo(Integer value) {
            addCriterion("call_status <>", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusGreaterThan(Integer value) {
            addCriterion("call_status >", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("call_status >=", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusLessThan(Integer value) {
            addCriterion("call_status <", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusLessThanOrEqualTo(Integer value) {
            addCriterion("call_status <=", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusIn(List<Integer> values) {
            addCriterion("call_status in", values, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusNotIn(List<Integer> values) {
            addCriterion("call_status not in", values, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusBetween(Integer value1, Integer value2) {
            addCriterion("call_status between", value1, value2, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("call_status not between", value1, value2, "callStatus");
            return (Criteria) this;
        }

        public Criteria andIsConnectIsNull() {
            addCriterion("is_connect is null");
            return (Criteria) this;
        }

        public Criteria andIsConnectIsNotNull() {
            addCriterion("is_connect is not null");
            return (Criteria) this;
        }

        public Criteria andIsConnectEqualTo(Integer value) {
            addCriterion("is_connect =", value, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectNotEqualTo(Integer value) {
            addCriterion("is_connect <>", value, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectGreaterThan(Integer value) {
            addCriterion("is_connect >", value, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_connect >=", value, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectLessThan(Integer value) {
            addCriterion("is_connect <", value, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectLessThanOrEqualTo(Integer value) {
            addCriterion("is_connect <=", value, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectIn(List<Integer> values) {
            addCriterion("is_connect in", values, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectNotIn(List<Integer> values) {
            addCriterion("is_connect not in", values, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectBetween(Integer value1, Integer value2) {
            addCriterion("is_connect between", value1, value2, "isConnect");
            return (Criteria) this;
        }

        public Criteria andIsConnectNotBetween(Integer value1, Integer value2) {
            addCriterion("is_connect not between", value1, value2, "isConnect");
            return (Criteria) this;
        }

        public Criteria andCallDialogIsNull() {
            addCriterion("call_dialog is null");
            return (Criteria) this;
        }

        public Criteria andCallDialogIsNotNull() {
            addCriterion("call_dialog is not null");
            return (Criteria) this;
        }

        public Criteria andCallDialogEqualTo(String value) {
            addCriterion("call_dialog =", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotEqualTo(String value) {
            addCriterion("call_dialog <>", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogGreaterThan(String value) {
            addCriterion("call_dialog >", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogGreaterThanOrEqualTo(String value) {
            addCriterion("call_dialog >=", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogLessThan(String value) {
            addCriterion("call_dialog <", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogLessThanOrEqualTo(String value) {
            addCriterion("call_dialog <=", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogLike(String value) {
            addCriterion("call_dialog like", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotLike(String value) {
            addCriterion("call_dialog not like", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogIn(List<String> values) {
            addCriterion("call_dialog in", values, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotIn(List<String> values) {
            addCriterion("call_dialog not in", values, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogBetween(String value1, String value2) {
            addCriterion("call_dialog between", value1, value2, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotBetween(String value1, String value2) {
            addCriterion("call_dialog not between", value1, value2, "callDialog");
            return (Criteria) this;
        }

        public Criteria andRecordingPathIsNull() {
            addCriterion("recording_path is null");
            return (Criteria) this;
        }

        public Criteria andRecordingPathIsNotNull() {
            addCriterion("recording_path is not null");
            return (Criteria) this;
        }

        public Criteria andRecordingPathEqualTo(String value) {
            addCriterion("recording_path =", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotEqualTo(String value) {
            addCriterion("recording_path <>", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathGreaterThan(String value) {
            addCriterion("recording_path >", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathGreaterThanOrEqualTo(String value) {
            addCriterion("recording_path >=", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathLessThan(String value) {
            addCriterion("recording_path <", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathLessThanOrEqualTo(String value) {
            addCriterion("recording_path <=", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathLike(String value) {
            addCriterion("recording_path like", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotLike(String value) {
            addCriterion("recording_path not like", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathIn(List<String> values) {
            addCriterion("recording_path in", values, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotIn(List<String> values) {
            addCriterion("recording_path not in", values, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathBetween(String value1, String value2) {
            addCriterion("recording_path between", value1, value2, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotBetween(String value1, String value2) {
            addCriterion("recording_path not between", value1, value2, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeIsNull() {
            addCriterion("intention_grade is null");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeIsNotNull() {
            addCriterion("intention_grade is not null");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeEqualTo(String value) {
            addCriterion("intention_grade =", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotEqualTo(String value) {
            addCriterion("intention_grade <>", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeGreaterThan(String value) {
            addCriterion("intention_grade >", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeGreaterThanOrEqualTo(String value) {
            addCriterion("intention_grade >=", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeLessThan(String value) {
            addCriterion("intention_grade <", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeLessThanOrEqualTo(String value) {
            addCriterion("intention_grade <=", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeLike(String value) {
            addCriterion("intention_grade like", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotLike(String value) {
            addCriterion("intention_grade not like", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeIn(List<String> values) {
            addCriterion("intention_grade in", values, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotIn(List<String> values) {
            addCriterion("intention_grade not in", values, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeBetween(String value1, String value2) {
            addCriterion("intention_grade between", value1, value2, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotBetween(String value1, String value2) {
            addCriterion("intention_grade not between", value1, value2, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andTagListIsNull() {
            addCriterion("tag_list is null");
            return (Criteria) this;
        }

        public Criteria andTagListIsNotNull() {
            addCriterion("tag_list is not null");
            return (Criteria) this;
        }

        public Criteria andTagListEqualTo(String value) {
            addCriterion("tag_list =", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListNotEqualTo(String value) {
            addCriterion("tag_list <>", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListGreaterThan(String value) {
            addCriterion("tag_list >", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListGreaterThanOrEqualTo(String value) {
            addCriterion("tag_list >=", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListLessThan(String value) {
            addCriterion("tag_list <", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListLessThanOrEqualTo(String value) {
            addCriterion("tag_list <=", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListLike(String value) {
            addCriterion("tag_list like", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListNotLike(String value) {
            addCriterion("tag_list not like", value, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListIn(List<String> values) {
            addCriterion("tag_list in", values, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListNotIn(List<String> values) {
            addCriterion("tag_list not in", values, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListBetween(String value1, String value2) {
            addCriterion("tag_list between", value1, value2, "tagList");
            return (Criteria) this;
        }

        public Criteria andTagListNotBetween(String value1, String value2) {
            addCriterion("tag_list not between", value1, value2, "tagList");
            return (Criteria) this;
        }

        public Criteria andReserveField1IsNull() {
            addCriterion("reserve_field1 is null");
            return (Criteria) this;
        }

        public Criteria andReserveField1IsNotNull() {
            addCriterion("reserve_field1 is not null");
            return (Criteria) this;
        }

        public Criteria andReserveField1EqualTo(String value) {
            addCriterion("reserve_field1 =", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotEqualTo(String value) {
            addCriterion("reserve_field1 <>", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1GreaterThan(String value) {
            addCriterion("reserve_field1 >", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1GreaterThanOrEqualTo(String value) {
            addCriterion("reserve_field1 >=", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1LessThan(String value) {
            addCriterion("reserve_field1 <", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1LessThanOrEqualTo(String value) {
            addCriterion("reserve_field1 <=", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1Like(String value) {
            addCriterion("reserve_field1 like", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotLike(String value) {
            addCriterion("reserve_field1 not like", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1In(List<String> values) {
            addCriterion("reserve_field1 in", values, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotIn(List<String> values) {
            addCriterion("reserve_field1 not in", values, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1Between(String value1, String value2) {
            addCriterion("reserve_field1 between", value1, value2, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotBetween(String value1, String value2) {
            addCriterion("reserve_field1 not between", value1, value2, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(String value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(String value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(String value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(String value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(String value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(String value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLike(String value) {
            addCriterion("version like", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotLike(String value) {
            addCriterion("version not like", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<String> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<String> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(String value1, String value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(String value1, String value2) {
            addCriterion("version not between", value1, value2, "version");
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

        public Criteria andReceiveDateIsNull() {
            addCriterion("receive_date is null");
            return (Criteria) this;
        }

        public Criteria andReceiveDateIsNotNull() {
            addCriterion("receive_date is not null");
            return (Criteria) this;
        }

        public Criteria andReceiveDateEqualTo(String value) {
            addCriterion("receive_date =", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotEqualTo(String value) {
            addCriterion("receive_date <>", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateGreaterThan(String value) {
            addCriterion("receive_date >", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateGreaterThanOrEqualTo(String value) {
            addCriterion("receive_date >=", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLessThan(String value) {
            addCriterion("receive_date <", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLessThanOrEqualTo(String value) {
            addCriterion("receive_date <=", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLike(String value) {
            addCriterion("receive_date like", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotLike(String value) {
            addCriterion("receive_date not like", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateIn(List<String> values) {
            addCriterion("receive_date in", values, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotIn(List<String> values) {
            addCriterion("receive_date not in", values, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateBetween(String value1, String value2) {
            addCriterion("receive_date between", value1, value2, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotBetween(String value1, String value2) {
            addCriterion("receive_date not between", value1, value2, "receiveDate");
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