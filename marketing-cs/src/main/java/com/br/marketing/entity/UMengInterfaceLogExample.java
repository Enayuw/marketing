package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UMengInterfaceLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public UMengInterfaceLogExample() {
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

        public Criteria andLocalIdIsNull() {
            addCriterion("local_id is null");
            return (Criteria) this;
        }

        public Criteria andLocalIdIsNotNull() {
            addCriterion("local_id is not null");
            return (Criteria) this;
        }

        public Criteria andLocalIdEqualTo(Long value) {
            addCriterion("local_id =", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotEqualTo(Long value) {
            addCriterion("local_id <>", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThan(Long value) {
            addCriterion("local_id >", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThanOrEqualTo(Long value) {
            addCriterion("local_id >=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThan(Long value) {
            addCriterion("local_id <", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThanOrEqualTo(Long value) {
            addCriterion("local_id <=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdIn(List<Long> values) {
            addCriterion("local_id in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotIn(List<Long> values) {
            addCriterion("local_id not in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdBetween(Long value1, Long value2) {
            addCriterion("local_id between", value1, value2, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotBetween(Long value1, Long value2) {
            addCriterion("local_id not between", value1, value2, "localId");
            return (Criteria) this;
        }

        public Criteria andRequestTypeIsNull() {
            addCriterion("request_type is null");
            return (Criteria) this;
        }

        public Criteria andRequestTypeIsNotNull() {
            addCriterion("request_type is not null");
            return (Criteria) this;
        }

        public Criteria andRequestTypeEqualTo(Integer value) {
            addCriterion("request_type =", value, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeNotEqualTo(Integer value) {
            addCriterion("request_type <>", value, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeGreaterThan(Integer value) {
            addCriterion("request_type >", value, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("request_type >=", value, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeLessThan(Integer value) {
            addCriterion("request_type <", value, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeLessThanOrEqualTo(Integer value) {
            addCriterion("request_type <=", value, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeIn(List<Integer> values) {
            addCriterion("request_type in", values, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeNotIn(List<Integer> values) {
            addCriterion("request_type not in", values, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeBetween(Integer value1, Integer value2) {
            addCriterion("request_type between", value1, value2, "requestType");
            return (Criteria) this;
        }

        public Criteria andRequestTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("request_type not between", value1, value2, "requestType");
            return (Criteria) this;
        }

        public Criteria andEventTypeIsNull() {
            addCriterion("event_type is null");
            return (Criteria) this;
        }

        public Criteria andEventTypeIsNotNull() {
            addCriterion("event_type is not null");
            return (Criteria) this;
        }

        public Criteria andEventTypeEqualTo(String value) {
            addCriterion("event_type =", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotEqualTo(String value) {
            addCriterion("event_type <>", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeGreaterThan(String value) {
            addCriterion("event_type >", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeGreaterThanOrEqualTo(String value) {
            addCriterion("event_type >=", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeLessThan(String value) {
            addCriterion("event_type <", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeLessThanOrEqualTo(String value) {
            addCriterion("event_type <=", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeLike(String value) {
            addCriterion("event_type like", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotLike(String value) {
            addCriterion("event_type not like", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeIn(List<String> values) {
            addCriterion("event_type in", values, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotIn(List<String> values) {
            addCriterion("event_type not in", values, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeBetween(String value1, String value2) {
            addCriterion("event_type between", value1, value2, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotBetween(String value1, String value2) {
            addCriterion("event_type not between", value1, value2, "eventType");
            return (Criteria) this;
        }

        public Criteria andRequestIdIsNull() {
            addCriterion("request_id is null");
            return (Criteria) this;
        }

        public Criteria andRequestIdIsNotNull() {
            addCriterion("request_id is not null");
            return (Criteria) this;
        }

        public Criteria andRequestIdEqualTo(String value) {
            addCriterion("request_id =", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdNotEqualTo(String value) {
            addCriterion("request_id <>", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdGreaterThan(String value) {
            addCriterion("request_id >", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdGreaterThanOrEqualTo(String value) {
            addCriterion("request_id >=", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdLessThan(String value) {
            addCriterion("request_id <", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdLessThanOrEqualTo(String value) {
            addCriterion("request_id <=", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdLike(String value) {
            addCriterion("request_id like", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdNotLike(String value) {
            addCriterion("request_id not like", value, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdIn(List<String> values) {
            addCriterion("request_id in", values, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdNotIn(List<String> values) {
            addCriterion("request_id not in", values, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdBetween(String value1, String value2) {
            addCriterion("request_id between", value1, value2, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestIdNotBetween(String value1, String value2) {
            addCriterion("request_id not between", value1, value2, "requestId");
            return (Criteria) this;
        }

        public Criteria andRequestParamIsNull() {
            addCriterion("request_param is null");
            return (Criteria) this;
        }

        public Criteria andRequestParamIsNotNull() {
            addCriterion("request_param is not null");
            return (Criteria) this;
        }

        public Criteria andRequestParamEqualTo(String value) {
            addCriterion("request_param =", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamNotEqualTo(String value) {
            addCriterion("request_param <>", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamGreaterThan(String value) {
            addCriterion("request_param >", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamGreaterThanOrEqualTo(String value) {
            addCriterion("request_param >=", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamLessThan(String value) {
            addCriterion("request_param <", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamLessThanOrEqualTo(String value) {
            addCriterion("request_param <=", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamLike(String value) {
            addCriterion("request_param like", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamNotLike(String value) {
            addCriterion("request_param not like", value, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamIn(List<String> values) {
            addCriterion("request_param in", values, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamNotIn(List<String> values) {
            addCriterion("request_param not in", values, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamBetween(String value1, String value2) {
            addCriterion("request_param between", value1, value2, "requestParam");
            return (Criteria) this;
        }

        public Criteria andRequestParamNotBetween(String value1, String value2) {
            addCriterion("request_param not between", value1, value2, "requestParam");
            return (Criteria) this;
        }

        public Criteria andUrlIsNull() {
            addCriterion("url is null");
            return (Criteria) this;
        }

        public Criteria andUrlIsNotNull() {
            addCriterion("url is not null");
            return (Criteria) this;
        }

        public Criteria andUrlEqualTo(String value) {
            addCriterion("url =", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotEqualTo(String value) {
            addCriterion("url <>", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlGreaterThan(String value) {
            addCriterion("url >", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlGreaterThanOrEqualTo(String value) {
            addCriterion("url >=", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLessThan(String value) {
            addCriterion("url <", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLessThanOrEqualTo(String value) {
            addCriterion("url <=", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLike(String value) {
            addCriterion("url like", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotLike(String value) {
            addCriterion("url not like", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlIn(List<String> values) {
            addCriterion("url in", values, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotIn(List<String> values) {
            addCriterion("url not in", values, "url");
            return (Criteria) this;
        }

        public Criteria andUrlBetween(String value1, String value2) {
            addCriterion("url between", value1, value2, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotBetween(String value1, String value2) {
            addCriterion("url not between", value1, value2, "url");
            return (Criteria) this;
        }

        public Criteria andHeaderIsNull() {
            addCriterion("`header` is null");
            return (Criteria) this;
        }

        public Criteria andHeaderIsNotNull() {
            addCriterion("`header` is not null");
            return (Criteria) this;
        }

        public Criteria andHeaderEqualTo(String value) {
            addCriterion("`header` =", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderNotEqualTo(String value) {
            addCriterion("`header` <>", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderGreaterThan(String value) {
            addCriterion("`header` >", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderGreaterThanOrEqualTo(String value) {
            addCriterion("`header` >=", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderLessThan(String value) {
            addCriterion("`header` <", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderLessThanOrEqualTo(String value) {
            addCriterion("`header` <=", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderLike(String value) {
            addCriterion("`header` like", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderNotLike(String value) {
            addCriterion("`header` not like", value, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderIn(List<String> values) {
            addCriterion("`header` in", values, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderNotIn(List<String> values) {
            addCriterion("`header` not in", values, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderBetween(String value1, String value2) {
            addCriterion("`header` between", value1, value2, "header");
            return (Criteria) this;
        }

        public Criteria andHeaderNotBetween(String value1, String value2) {
            addCriterion("`header` not between", value1, value2, "header");
            return (Criteria) this;
        }

        public Criteria andResultIsNull() {
            addCriterion("`result` is null");
            return (Criteria) this;
        }

        public Criteria andResultIsNotNull() {
            addCriterion("`result` is not null");
            return (Criteria) this;
        }

        public Criteria andResultEqualTo(String value) {
            addCriterion("`result` =", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotEqualTo(String value) {
            addCriterion("`result` <>", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultGreaterThan(String value) {
            addCriterion("`result` >", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultGreaterThanOrEqualTo(String value) {
            addCriterion("`result` >=", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultLessThan(String value) {
            addCriterion("`result` <", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultLessThanOrEqualTo(String value) {
            addCriterion("`result` <=", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultLike(String value) {
            addCriterion("`result` like", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotLike(String value) {
            addCriterion("`result` not like", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultIn(List<String> values) {
            addCriterion("`result` in", values, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotIn(List<String> values) {
            addCriterion("`result` not in", values, "result");
            return (Criteria) this;
        }

        public Criteria andResultBetween(String value1, String value2) {
            addCriterion("`result` between", value1, value2, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotBetween(String value1, String value2) {
            addCriterion("`result` not between", value1, value2, "result");
            return (Criteria) this;
        }

        public Criteria andHttpCodeIsNull() {
            addCriterion("http_code is null");
            return (Criteria) this;
        }

        public Criteria andHttpCodeIsNotNull() {
            addCriterion("http_code is not null");
            return (Criteria) this;
        }

        public Criteria andHttpCodeEqualTo(Integer value) {
            addCriterion("http_code =", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeNotEqualTo(Integer value) {
            addCriterion("http_code <>", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeGreaterThan(Integer value) {
            addCriterion("http_code >", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeGreaterThanOrEqualTo(Integer value) {
            addCriterion("http_code >=", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeLessThan(Integer value) {
            addCriterion("http_code <", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeLessThanOrEqualTo(Integer value) {
            addCriterion("http_code <=", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeIn(List<Integer> values) {
            addCriterion("http_code in", values, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeNotIn(List<Integer> values) {
            addCriterion("http_code not in", values, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeBetween(Integer value1, Integer value2) {
            addCriterion("http_code between", value1, value2, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeNotBetween(Integer value1, Integer value2) {
            addCriterion("http_code not between", value1, value2, "httpCode");
            return (Criteria) this;
        }

        public Criteria andCallTimeIsNull() {
            addCriterion("call_time is null");
            return (Criteria) this;
        }

        public Criteria andCallTimeIsNotNull() {
            addCriterion("call_time is not null");
            return (Criteria) this;
        }

        public Criteria andCallTimeEqualTo(Integer value) {
            addCriterion("call_time =", value, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeNotEqualTo(Integer value) {
            addCriterion("call_time <>", value, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeGreaterThan(Integer value) {
            addCriterion("call_time >", value, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("call_time >=", value, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeLessThan(Integer value) {
            addCriterion("call_time <", value, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeLessThanOrEqualTo(Integer value) {
            addCriterion("call_time <=", value, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeIn(List<Integer> values) {
            addCriterion("call_time in", values, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeNotIn(List<Integer> values) {
            addCriterion("call_time not in", values, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeBetween(Integer value1, Integer value2) {
            addCriterion("call_time between", value1, value2, "callTime");
            return (Criteria) this;
        }

        public Criteria andCallTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("call_time not between", value1, value2, "callTime");
            return (Criteria) this;
        }

        public Criteria andExpireIsNull() {
            addCriterion("expire is null");
            return (Criteria) this;
        }

        public Criteria andExpireIsNotNull() {
            addCriterion("expire is not null");
            return (Criteria) this;
        }

        public Criteria andExpireEqualTo(String value) {
            addCriterion("expire =", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireNotEqualTo(String value) {
            addCriterion("expire <>", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireGreaterThan(String value) {
            addCriterion("expire >", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireGreaterThanOrEqualTo(String value) {
            addCriterion("expire >=", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireLessThan(String value) {
            addCriterion("expire <", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireLessThanOrEqualTo(String value) {
            addCriterion("expire <=", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireLike(String value) {
            addCriterion("expire like", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireNotLike(String value) {
            addCriterion("expire not like", value, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireIn(List<String> values) {
            addCriterion("expire in", values, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireNotIn(List<String> values) {
            addCriterion("expire not in", values, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireBetween(String value1, String value2) {
            addCriterion("expire between", value1, value2, "expire");
            return (Criteria) this;
        }

        public Criteria andExpireNotBetween(String value1, String value2) {
            addCriterion("expire not between", value1, value2, "expire");
            return (Criteria) this;
        }

        public Criteria andExtendInfoIsNull() {
            addCriterion("extend_info is null");
            return (Criteria) this;
        }

        public Criteria andExtendInfoIsNotNull() {
            addCriterion("extend_info is not null");
            return (Criteria) this;
        }

        public Criteria andExtendInfoEqualTo(String value) {
            addCriterion("extend_info =", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotEqualTo(String value) {
            addCriterion("extend_info <>", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoGreaterThan(String value) {
            addCriterion("extend_info >", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoGreaterThanOrEqualTo(String value) {
            addCriterion("extend_info >=", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoLessThan(String value) {
            addCriterion("extend_info <", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoLessThanOrEqualTo(String value) {
            addCriterion("extend_info <=", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoLike(String value) {
            addCriterion("extend_info like", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotLike(String value) {
            addCriterion("extend_info not like", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoIn(List<String> values) {
            addCriterion("extend_info in", values, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotIn(List<String> values) {
            addCriterion("extend_info not in", values, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoBetween(String value1, String value2) {
            addCriterion("extend_info between", value1, value2, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotBetween(String value1, String value2) {
            addCriterion("extend_info not between", value1, value2, "extendInfo");
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

        public Criteria andPhoneSha256IsNull() {
            addCriterion("phone_sha256 is null");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256IsNotNull() {
            addCriterion("phone_sha256 is not null");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256EqualTo(String value) {
            addCriterion("phone_sha256 =", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256NotEqualTo(String value) {
            addCriterion("phone_sha256 <>", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256GreaterThan(String value) {
            addCriterion("phone_sha256 >", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256GreaterThanOrEqualTo(String value) {
            addCriterion("phone_sha256 >=", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256LessThan(String value) {
            addCriterion("phone_sha256 <", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256LessThanOrEqualTo(String value) {
            addCriterion("phone_sha256 <=", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256Like(String value) {
            addCriterion("phone_sha256 like", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256NotLike(String value) {
            addCriterion("phone_sha256 not like", value, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256In(List<String> values) {
            addCriterion("phone_sha256 in", values, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256NotIn(List<String> values) {
            addCriterion("phone_sha256 not in", values, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256Between(String value1, String value2) {
            addCriterion("phone_sha256 between", value1, value2, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andPhoneSha256NotBetween(String value1, String value2) {
            addCriterion("phone_sha256 not between", value1, value2, "phoneSha256");
            return (Criteria) this;
        }

        public Criteria andEncryptParamIsNull() {
            addCriterion("encrypt_param is null");
            return (Criteria) this;
        }

        public Criteria andEncryptParamIsNotNull() {
            addCriterion("encrypt_param is not null");
            return (Criteria) this;
        }

        public Criteria andEncryptParamEqualTo(String value) {
            addCriterion("encrypt_param =", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamNotEqualTo(String value) {
            addCriterion("encrypt_param <>", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamGreaterThan(String value) {
            addCriterion("encrypt_param >", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamGreaterThanOrEqualTo(String value) {
            addCriterion("encrypt_param >=", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamLessThan(String value) {
            addCriterion("encrypt_param <", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamLessThanOrEqualTo(String value) {
            addCriterion("encrypt_param <=", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamLike(String value) {
            addCriterion("encrypt_param like", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamNotLike(String value) {
            addCriterion("encrypt_param not like", value, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamIn(List<String> values) {
            addCriterion("encrypt_param in", values, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamNotIn(List<String> values) {
            addCriterion("encrypt_param not in", values, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamBetween(String value1, String value2) {
            addCriterion("encrypt_param between", value1, value2, "encryptParam");
            return (Criteria) this;
        }

        public Criteria andEncryptParamNotBetween(String value1, String value2) {
            addCriterion("encrypt_param not between", value1, value2, "encryptParam");
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