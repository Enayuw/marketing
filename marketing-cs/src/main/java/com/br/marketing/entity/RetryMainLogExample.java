package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RetryMainLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RetryMainLogExample() {
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

        public Criteria andIncrIdIsNull() {
            addCriterion("incr_id is null");
            return (Criteria) this;
        }

        public Criteria andIncrIdIsNotNull() {
            addCriterion("incr_id is not null");
            return (Criteria) this;
        }

        public Criteria andIncrIdEqualTo(Long value) {
            addCriterion("incr_id =", value, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdNotEqualTo(Long value) {
            addCriterion("incr_id <>", value, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdGreaterThan(Long value) {
            addCriterion("incr_id >", value, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdGreaterThanOrEqualTo(Long value) {
            addCriterion("incr_id >=", value, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdLessThan(Long value) {
            addCriterion("incr_id <", value, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdLessThanOrEqualTo(Long value) {
            addCriterion("incr_id <=", value, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdIn(List<Long> values) {
            addCriterion("incr_id in", values, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdNotIn(List<Long> values) {
            addCriterion("incr_id not in", values, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdBetween(Long value1, Long value2) {
            addCriterion("incr_id between", value1, value2, "incrId");
            return (Criteria) this;
        }

        public Criteria andIncrIdNotBetween(Long value1, Long value2) {
            addCriterion("incr_id not between", value1, value2, "incrId");
            return (Criteria) this;
        }

        public Criteria andRetryTypeIsNull() {
            addCriterion("retry_type is null");
            return (Criteria) this;
        }

        public Criteria andRetryTypeIsNotNull() {
            addCriterion("retry_type is not null");
            return (Criteria) this;
        }

        public Criteria andRetryTypeEqualTo(Integer value) {
            addCriterion("retry_type =", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeNotEqualTo(Integer value) {
            addCriterion("retry_type <>", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeGreaterThan(Integer value) {
            addCriterion("retry_type >", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_type >=", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeLessThan(Integer value) {
            addCriterion("retry_type <", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeLessThanOrEqualTo(Integer value) {
            addCriterion("retry_type <=", value, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeIn(List<Integer> values) {
            addCriterion("retry_type in", values, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeNotIn(List<Integer> values) {
            addCriterion("retry_type not in", values, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeBetween(Integer value1, Integer value2) {
            addCriterion("retry_type between", value1, value2, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_type not between", value1, value2, "retryType");
            return (Criteria) this;
        }

        public Criteria andRetryParamIsNull() {
            addCriterion("retry_param is null");
            return (Criteria) this;
        }

        public Criteria andRetryParamIsNotNull() {
            addCriterion("retry_param is not null");
            return (Criteria) this;
        }

        public Criteria andRetryParamEqualTo(String value) {
            addCriterion("retry_param =", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamNotEqualTo(String value) {
            addCriterion("retry_param <>", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamGreaterThan(String value) {
            addCriterion("retry_param >", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamGreaterThanOrEqualTo(String value) {
            addCriterion("retry_param >=", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamLessThan(String value) {
            addCriterion("retry_param <", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamLessThanOrEqualTo(String value) {
            addCriterion("retry_param <=", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamLike(String value) {
            addCriterion("retry_param like", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamNotLike(String value) {
            addCriterion("retry_param not like", value, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamIn(List<String> values) {
            addCriterion("retry_param in", values, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamNotIn(List<String> values) {
            addCriterion("retry_param not in", values, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamBetween(String value1, String value2) {
            addCriterion("retry_param between", value1, value2, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamNotBetween(String value1, String value2) {
            addCriterion("retry_param not between", value1, value2, "retryParam");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeIsNull() {
            addCriterion("retry_param_type is null");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeIsNotNull() {
            addCriterion("retry_param_type is not null");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeEqualTo(String value) {
            addCriterion("retry_param_type =", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeNotEqualTo(String value) {
            addCriterion("retry_param_type <>", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeGreaterThan(String value) {
            addCriterion("retry_param_type >", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeGreaterThanOrEqualTo(String value) {
            addCriterion("retry_param_type >=", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeLessThan(String value) {
            addCriterion("retry_param_type <", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeLessThanOrEqualTo(String value) {
            addCriterion("retry_param_type <=", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeLike(String value) {
            addCriterion("retry_param_type like", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeNotLike(String value) {
            addCriterion("retry_param_type not like", value, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeIn(List<String> values) {
            addCriterion("retry_param_type in", values, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeNotIn(List<String> values) {
            addCriterion("retry_param_type not in", values, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeBetween(String value1, String value2) {
            addCriterion("retry_param_type between", value1, value2, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryParamTypeNotBetween(String value1, String value2) {
            addCriterion("retry_param_type not between", value1, value2, "retryParamType");
            return (Criteria) this;
        }

        public Criteria andRetryServiceIsNull() {
            addCriterion("retry_service is null");
            return (Criteria) this;
        }

        public Criteria andRetryServiceIsNotNull() {
            addCriterion("retry_service is not null");
            return (Criteria) this;
        }

        public Criteria andRetryServiceEqualTo(String value) {
            addCriterion("retry_service =", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceNotEqualTo(String value) {
            addCriterion("retry_service <>", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceGreaterThan(String value) {
            addCriterion("retry_service >", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceGreaterThanOrEqualTo(String value) {
            addCriterion("retry_service >=", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceLessThan(String value) {
            addCriterion("retry_service <", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceLessThanOrEqualTo(String value) {
            addCriterion("retry_service <=", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceLike(String value) {
            addCriterion("retry_service like", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceNotLike(String value) {
            addCriterion("retry_service not like", value, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceIn(List<String> values) {
            addCriterion("retry_service in", values, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceNotIn(List<String> values) {
            addCriterion("retry_service not in", values, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceBetween(String value1, String value2) {
            addCriterion("retry_service between", value1, value2, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryServiceNotBetween(String value1, String value2) {
            addCriterion("retry_service not between", value1, value2, "retryService");
            return (Criteria) this;
        }

        public Criteria andRetryMethodIsNull() {
            addCriterion("retry_method is null");
            return (Criteria) this;
        }

        public Criteria andRetryMethodIsNotNull() {
            addCriterion("retry_method is not null");
            return (Criteria) this;
        }

        public Criteria andRetryMethodEqualTo(String value) {
            addCriterion("retry_method =", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodNotEqualTo(String value) {
            addCriterion("retry_method <>", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodGreaterThan(String value) {
            addCriterion("retry_method >", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodGreaterThanOrEqualTo(String value) {
            addCriterion("retry_method >=", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodLessThan(String value) {
            addCriterion("retry_method <", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodLessThanOrEqualTo(String value) {
            addCriterion("retry_method <=", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodLike(String value) {
            addCriterion("retry_method like", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodNotLike(String value) {
            addCriterion("retry_method not like", value, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodIn(List<String> values) {
            addCriterion("retry_method in", values, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodNotIn(List<String> values) {
            addCriterion("retry_method not in", values, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodBetween(String value1, String value2) {
            addCriterion("retry_method between", value1, value2, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryMethodNotBetween(String value1, String value2) {
            addCriterion("retry_method not between", value1, value2, "retryMethod");
            return (Criteria) this;
        }

        public Criteria andRetryNumIsNull() {
            addCriterion("retry_num is null");
            return (Criteria) this;
        }

        public Criteria andRetryNumIsNotNull() {
            addCriterion("retry_num is not null");
            return (Criteria) this;
        }

        public Criteria andRetryNumEqualTo(Integer value) {
            addCriterion("retry_num =", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumNotEqualTo(Integer value) {
            addCriterion("retry_num <>", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumGreaterThan(Integer value) {
            addCriterion("retry_num >", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_num >=", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumLessThan(Integer value) {
            addCriterion("retry_num <", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumLessThanOrEqualTo(Integer value) {
            addCriterion("retry_num <=", value, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumIn(List<Integer> values) {
            addCriterion("retry_num in", values, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumNotIn(List<Integer> values) {
            addCriterion("retry_num not in", values, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumBetween(Integer value1, Integer value2) {
            addCriterion("retry_num between", value1, value2, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryNumNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_num not between", value1, value2, "retryNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumIsNull() {
            addCriterion("retry_max_num is null");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumIsNotNull() {
            addCriterion("retry_max_num is not null");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumEqualTo(Integer value) {
            addCriterion("retry_max_num =", value, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumNotEqualTo(Integer value) {
            addCriterion("retry_max_num <>", value, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumGreaterThan(Integer value) {
            addCriterion("retry_max_num >", value, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_max_num >=", value, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumLessThan(Integer value) {
            addCriterion("retry_max_num <", value, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumLessThanOrEqualTo(Integer value) {
            addCriterion("retry_max_num <=", value, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumIn(List<Integer> values) {
            addCriterion("retry_max_num in", values, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumNotIn(List<Integer> values) {
            addCriterion("retry_max_num not in", values, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumBetween(Integer value1, Integer value2) {
            addCriterion("retry_max_num between", value1, value2, "retryMaxNum");
            return (Criteria) this;
        }

        public Criteria andRetryMaxNumNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_max_num not between", value1, value2, "retryMaxNum");
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

        public Criteria andServiceTypeIsNull() {
            addCriterion("service_type is null");
            return (Criteria) this;
        }

        public Criteria andServiceTypeIsNotNull() {
            addCriterion("service_type is not null");
            return (Criteria) this;
        }

        public Criteria andServiceTypeEqualTo(Integer value) {
            addCriterion("service_type =", value, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeNotEqualTo(Integer value) {
            addCriterion("service_type <>", value, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeGreaterThan(Integer value) {
            addCriterion("service_type >", value, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("service_type >=", value, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeLessThan(Integer value) {
            addCriterion("service_type <", value, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeLessThanOrEqualTo(Integer value) {
            addCriterion("service_type <=", value, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeIn(List<Integer> values) {
            addCriterion("service_type in", values, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeNotIn(List<Integer> values) {
            addCriterion("service_type not in", values, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeBetween(Integer value1, Integer value2) {
            addCriterion("service_type between", value1, value2, "serviceType");
            return (Criteria) this;
        }

        public Criteria andServiceTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("service_type not between", value1, value2, "serviceType");
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