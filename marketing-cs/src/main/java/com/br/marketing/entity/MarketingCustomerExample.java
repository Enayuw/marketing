package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingCustomerExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingCustomerExample() {
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

        public Criteria andMessageIsNull() {
            addCriterion("message is null");
            return (Criteria) this;
        }

        public Criteria andMessageIsNotNull() {
            addCriterion("message is not null");
            return (Criteria) this;
        }

        public Criteria andMessageEqualTo(String value) {
            addCriterion("message =", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotEqualTo(String value) {
            addCriterion("message <>", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageGreaterThan(String value) {
            addCriterion("message >", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageGreaterThanOrEqualTo(String value) {
            addCriterion("message >=", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLessThan(String value) {
            addCriterion("message <", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLessThanOrEqualTo(String value) {
            addCriterion("message <=", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLike(String value) {
            addCriterion("message like", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotLike(String value) {
            addCriterion("message not like", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageIn(List<String> values) {
            addCriterion("message in", values, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotIn(List<String> values) {
            addCriterion("message not in", values, "message");
            return (Criteria) this;
        }

        public Criteria andMessageBetween(String value1, String value2) {
            addCriterion("message between", value1, value2, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotBetween(String value1, String value2) {
            addCriterion("message not between", value1, value2, "message");
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

        public Criteria andTypeEqualTo(String value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("`type` like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("`type` not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andThreadNumIsNull() {
            addCriterion("thread_num is null");
            return (Criteria) this;
        }

        public Criteria andThreadNumIsNotNull() {
            addCriterion("thread_num is not null");
            return (Criteria) this;
        }

        public Criteria andThreadNumEqualTo(Integer value) {
            addCriterion("thread_num =", value, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumNotEqualTo(Integer value) {
            addCriterion("thread_num <>", value, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumGreaterThan(Integer value) {
            addCriterion("thread_num >", value, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("thread_num >=", value, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumLessThan(Integer value) {
            addCriterion("thread_num <", value, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumLessThanOrEqualTo(Integer value) {
            addCriterion("thread_num <=", value, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumIn(List<Integer> values) {
            addCriterion("thread_num in", values, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumNotIn(List<Integer> values) {
            addCriterion("thread_num not in", values, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumBetween(Integer value1, Integer value2) {
            addCriterion("thread_num between", value1, value2, "threadNum");
            return (Criteria) this;
        }

        public Criteria andThreadNumNotBetween(Integer value1, Integer value2) {
            addCriterion("thread_num not between", value1, value2, "threadNum");
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

        public Criteria andTaskTimeEqualTo(Byte value) {
            addCriterion("task_time =", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotEqualTo(Byte value) {
            addCriterion("task_time <>", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeGreaterThan(Byte value) {
            addCriterion("task_time >", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeGreaterThanOrEqualTo(Byte value) {
            addCriterion("task_time >=", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeLessThan(Byte value) {
            addCriterion("task_time <", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeLessThanOrEqualTo(Byte value) {
            addCriterion("task_time <=", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeIn(List<Byte> values) {
            addCriterion("task_time in", values, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotIn(List<Byte> values) {
            addCriterion("task_time not in", values, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeBetween(Byte value1, Byte value2) {
            addCriterion("task_time between", value1, value2, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotBetween(Byte value1, Byte value2) {
            addCriterion("task_time not between", value1, value2, "taskTime");
            return (Criteria) this;
        }

        public Criteria andFinishDateIsNull() {
            addCriterion("finish_date is null");
            return (Criteria) this;
        }

        public Criteria andFinishDateIsNotNull() {
            addCriterion("finish_date is not null");
            return (Criteria) this;
        }

        public Criteria andFinishDateEqualTo(Byte value) {
            addCriterion("finish_date =", value, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateNotEqualTo(Byte value) {
            addCriterion("finish_date <>", value, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateGreaterThan(Byte value) {
            addCriterion("finish_date >", value, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateGreaterThanOrEqualTo(Byte value) {
            addCriterion("finish_date >=", value, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateLessThan(Byte value) {
            addCriterion("finish_date <", value, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateLessThanOrEqualTo(Byte value) {
            addCriterion("finish_date <=", value, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateIn(List<Byte> values) {
            addCriterion("finish_date in", values, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateNotIn(List<Byte> values) {
            addCriterion("finish_date not in", values, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateBetween(Byte value1, Byte value2) {
            addCriterion("finish_date between", value1, value2, "finishDate");
            return (Criteria) this;
        }

        public Criteria andFinishDateNotBetween(Byte value1, Byte value2) {
            addCriterion("finish_date not between", value1, value2, "finishDate");
            return (Criteria) this;
        }

        public Criteria andPushCustomerIsNull() {
            addCriterion("push_customer is null");
            return (Criteria) this;
        }

        public Criteria andPushCustomerIsNotNull() {
            addCriterion("push_customer is not null");
            return (Criteria) this;
        }

        public Criteria andPushCustomerEqualTo(Byte value) {
            addCriterion("push_customer =", value, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerNotEqualTo(Byte value) {
            addCriterion("push_customer <>", value, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerGreaterThan(Byte value) {
            addCriterion("push_customer >", value, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerGreaterThanOrEqualTo(Byte value) {
            addCriterion("push_customer >=", value, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerLessThan(Byte value) {
            addCriterion("push_customer <", value, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerLessThanOrEqualTo(Byte value) {
            addCriterion("push_customer <=", value, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerIn(List<Byte> values) {
            addCriterion("push_customer in", values, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerNotIn(List<Byte> values) {
            addCriterion("push_customer not in", values, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerBetween(Byte value1, Byte value2) {
            addCriterion("push_customer between", value1, value2, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andPushCustomerNotBetween(Byte value1, Byte value2) {
            addCriterion("push_customer not between", value1, value2, "pushCustomer");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListIsNull() {
            addCriterion("check_black_list is null");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListIsNotNull() {
            addCriterion("check_black_list is not null");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListEqualTo(Byte value) {
            addCriterion("check_black_list =", value, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListNotEqualTo(Byte value) {
            addCriterion("check_black_list <>", value, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListGreaterThan(Byte value) {
            addCriterion("check_black_list >", value, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListGreaterThanOrEqualTo(Byte value) {
            addCriterion("check_black_list >=", value, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListLessThan(Byte value) {
            addCriterion("check_black_list <", value, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListLessThanOrEqualTo(Byte value) {
            addCriterion("check_black_list <=", value, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListIn(List<Byte> values) {
            addCriterion("check_black_list in", values, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListNotIn(List<Byte> values) {
            addCriterion("check_black_list not in", values, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListBetween(Byte value1, Byte value2) {
            addCriterion("check_black_list between", value1, value2, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckBlackListNotBetween(Byte value1, Byte value2) {
            addCriterion("check_black_list not between", value1, value2, "checkBlackList");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberIsNull() {
            addCriterion("check_redis_number is null");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberIsNotNull() {
            addCriterion("check_redis_number is not null");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberEqualTo(Byte value) {
            addCriterion("check_redis_number =", value, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberNotEqualTo(Byte value) {
            addCriterion("check_redis_number <>", value, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberGreaterThan(Byte value) {
            addCriterion("check_redis_number >", value, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberGreaterThanOrEqualTo(Byte value) {
            addCriterion("check_redis_number >=", value, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberLessThan(Byte value) {
            addCriterion("check_redis_number <", value, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberLessThanOrEqualTo(Byte value) {
            addCriterion("check_redis_number <=", value, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberIn(List<Byte> values) {
            addCriterion("check_redis_number in", values, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberNotIn(List<Byte> values) {
            addCriterion("check_redis_number not in", values, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberBetween(Byte value1, Byte value2) {
            addCriterion("check_redis_number between", value1, value2, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andCheckRedisNumberNotBetween(Byte value1, Byte value2) {
            addCriterion("check_redis_number not between", value1, value2, "checkRedisNumber");
            return (Criteria) this;
        }

        public Criteria andSaveLogIsNull() {
            addCriterion("save_log is null");
            return (Criteria) this;
        }

        public Criteria andSaveLogIsNotNull() {
            addCriterion("save_log is not null");
            return (Criteria) this;
        }

        public Criteria andSaveLogEqualTo(Byte value) {
            addCriterion("save_log =", value, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogNotEqualTo(Byte value) {
            addCriterion("save_log <>", value, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogGreaterThan(Byte value) {
            addCriterion("save_log >", value, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogGreaterThanOrEqualTo(Byte value) {
            addCriterion("save_log >=", value, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogLessThan(Byte value) {
            addCriterion("save_log <", value, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogLessThanOrEqualTo(Byte value) {
            addCriterion("save_log <=", value, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogIn(List<Byte> values) {
            addCriterion("save_log in", values, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogNotIn(List<Byte> values) {
            addCriterion("save_log not in", values, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogBetween(Byte value1, Byte value2) {
            addCriterion("save_log between", value1, value2, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSaveLogNotBetween(Byte value1, Byte value2) {
            addCriterion("save_log not between", value1, value2, "saveLog");
            return (Criteria) this;
        }

        public Criteria andSortIsNull() {
            addCriterion("sort is null");
            return (Criteria) this;
        }

        public Criteria andSortIsNotNull() {
            addCriterion("sort is not null");
            return (Criteria) this;
        }

        public Criteria andSortEqualTo(Byte value) {
            addCriterion("sort =", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortNotEqualTo(Byte value) {
            addCriterion("sort <>", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortGreaterThan(Byte value) {
            addCriterion("sort >", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortGreaterThanOrEqualTo(Byte value) {
            addCriterion("sort >=", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortLessThan(Byte value) {
            addCriterion("sort <", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortLessThanOrEqualTo(Byte value) {
            addCriterion("sort <=", value, "sort");
            return (Criteria) this;
        }

        public Criteria andSortIn(List<Byte> values) {
            addCriterion("sort in", values, "sort");
            return (Criteria) this;
        }

        public Criteria andSortNotIn(List<Byte> values) {
            addCriterion("sort not in", values, "sort");
            return (Criteria) this;
        }

        public Criteria andSortBetween(Byte value1, Byte value2) {
            addCriterion("sort between", value1, value2, "sort");
            return (Criteria) this;
        }

        public Criteria andSortNotBetween(Byte value1, Byte value2) {
            addCriterion("sort not between", value1, value2, "sort");
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

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("`status` not between", value1, value2, "status");
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

        public Criteria andPushThreadNumIsNull() {
            addCriterion("push_thread_num is null");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumIsNotNull() {
            addCriterion("push_thread_num is not null");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumEqualTo(Integer value) {
            addCriterion("push_thread_num =", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumNotEqualTo(Integer value) {
            addCriterion("push_thread_num <>", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumGreaterThan(Integer value) {
            addCriterion("push_thread_num >", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_thread_num >=", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumLessThan(Integer value) {
            addCriterion("push_thread_num <", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumLessThanOrEqualTo(Integer value) {
            addCriterion("push_thread_num <=", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumIn(List<Integer> values) {
            addCriterion("push_thread_num in", values, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumNotIn(List<Integer> values) {
            addCriterion("push_thread_num not in", values, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumBetween(Integer value1, Integer value2) {
            addCriterion("push_thread_num between", value1, value2, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumNotBetween(Integer value1, Integer value2) {
            addCriterion("push_thread_num not between", value1, value2, "pushThreadNum");
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

        public Criteria andPushUrlIsNull() {
            addCriterion("push_url is null");
            return (Criteria) this;
        }

        public Criteria andPushUrlIsNotNull() {
            addCriterion("push_url is not null");
            return (Criteria) this;
        }

        public Criteria andPushUrlEqualTo(String value) {
            addCriterion("push_url =", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotEqualTo(String value) {
            addCriterion("push_url <>", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlGreaterThan(String value) {
            addCriterion("push_url >", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlGreaterThanOrEqualTo(String value) {
            addCriterion("push_url >=", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlLessThan(String value) {
            addCriterion("push_url <", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlLessThanOrEqualTo(String value) {
            addCriterion("push_url <=", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlLike(String value) {
            addCriterion("push_url like", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotLike(String value) {
            addCriterion("push_url not like", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlIn(List<String> values) {
            addCriterion("push_url in", values, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotIn(List<String> values) {
            addCriterion("push_url not in", values, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlBetween(String value1, String value2) {
            addCriterion("push_url between", value1, value2, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotBetween(String value1, String value2) {
            addCriterion("push_url not between", value1, value2, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andShortNameIsNull() {
            addCriterion("short_name is null");
            return (Criteria) this;
        }

        public Criteria andShortNameIsNotNull() {
            addCriterion("short_name is not null");
            return (Criteria) this;
        }

        public Criteria andShortNameEqualTo(String value) {
            addCriterion("short_name =", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotEqualTo(String value) {
            addCriterion("short_name <>", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameGreaterThan(String value) {
            addCriterion("short_name >", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameGreaterThanOrEqualTo(String value) {
            addCriterion("short_name >=", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameLessThan(String value) {
            addCriterion("short_name <", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameLessThanOrEqualTo(String value) {
            addCriterion("short_name <=", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameLike(String value) {
            addCriterion("short_name like", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotLike(String value) {
            addCriterion("short_name not like", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameIn(List<String> values) {
            addCriterion("short_name in", values, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotIn(List<String> values) {
            addCriterion("short_name not in", values, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameBetween(String value1, String value2) {
            addCriterion("short_name between", value1, value2, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotBetween(String value1, String value2) {
            addCriterion("short_name not between", value1, value2, "shortName");
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

        public Criteria andIsCheckIsNull() {
            addCriterion("is_check is null");
            return (Criteria) this;
        }

        public Criteria andIsCheckIsNotNull() {
            addCriterion("is_check is not null");
            return (Criteria) this;
        }

        public Criteria andIsCheckEqualTo(Byte value) {
            addCriterion("is_check =", value, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckNotEqualTo(Byte value) {
            addCriterion("is_check <>", value, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckGreaterThan(Byte value) {
            addCriterion("is_check >", value, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_check >=", value, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckLessThan(Byte value) {
            addCriterion("is_check <", value, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckLessThanOrEqualTo(Byte value) {
            addCriterion("is_check <=", value, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckIn(List<Byte> values) {
            addCriterion("is_check in", values, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckNotIn(List<Byte> values) {
            addCriterion("is_check not in", values, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckBetween(Byte value1, Byte value2) {
            addCriterion("is_check between", value1, value2, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsCheckNotBetween(Byte value1, Byte value2) {
            addCriterion("is_check not between", value1, value2, "isCheck");
            return (Criteria) this;
        }

        public Criteria andIsChargingIsNull() {
            addCriterion("is_charging is null");
            return (Criteria) this;
        }

        public Criteria andIsChargingIsNotNull() {
            addCriterion("is_charging is not null");
            return (Criteria) this;
        }

        public Criteria andIsChargingEqualTo(Byte value) {
            addCriterion("is_charging =", value, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingNotEqualTo(Byte value) {
            addCriterion("is_charging <>", value, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingGreaterThan(Byte value) {
            addCriterion("is_charging >", value, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_charging >=", value, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingLessThan(Byte value) {
            addCriterion("is_charging <", value, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingLessThanOrEqualTo(Byte value) {
            addCriterion("is_charging <=", value, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingIn(List<Byte> values) {
            addCriterion("is_charging in", values, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingNotIn(List<Byte> values) {
            addCriterion("is_charging not in", values, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingBetween(Byte value1, Byte value2) {
            addCriterion("is_charging between", value1, value2, "isCharging");
            return (Criteria) this;
        }

        public Criteria andIsChargingNotBetween(Byte value1, Byte value2) {
            addCriterion("is_charging not between", value1, value2, "isCharging");
            return (Criteria) this;
        }

        public Criteria andRequestCodeIsNull() {
            addCriterion("request_code is null");
            return (Criteria) this;
        }

        public Criteria andRequestCodeIsNotNull() {
            addCriterion("request_code is not null");
            return (Criteria) this;
        }

        public Criteria andRequestCodeEqualTo(String value) {
            addCriterion("request_code =", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeNotEqualTo(String value) {
            addCriterion("request_code <>", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeGreaterThan(String value) {
            addCriterion("request_code >", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeGreaterThanOrEqualTo(String value) {
            addCriterion("request_code >=", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeLessThan(String value) {
            addCriterion("request_code <", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeLessThanOrEqualTo(String value) {
            addCriterion("request_code <=", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeLike(String value) {
            addCriterion("request_code like", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeNotLike(String value) {
            addCriterion("request_code not like", value, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeIn(List<String> values) {
            addCriterion("request_code in", values, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeNotIn(List<String> values) {
            addCriterion("request_code not in", values, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeBetween(String value1, String value2) {
            addCriterion("request_code between", value1, value2, "requestCode");
            return (Criteria) this;
        }

        public Criteria andRequestCodeNotBetween(String value1, String value2) {
            addCriterion("request_code not between", value1, value2, "requestCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeIsNull() {
            addCriterion("response_code is null");
            return (Criteria) this;
        }

        public Criteria andResponseCodeIsNotNull() {
            addCriterion("response_code is not null");
            return (Criteria) this;
        }

        public Criteria andResponseCodeEqualTo(String value) {
            addCriterion("response_code =", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeNotEqualTo(String value) {
            addCriterion("response_code <>", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeGreaterThan(String value) {
            addCriterion("response_code >", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeGreaterThanOrEqualTo(String value) {
            addCriterion("response_code >=", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeLessThan(String value) {
            addCriterion("response_code <", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeLessThanOrEqualTo(String value) {
            addCriterion("response_code <=", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeLike(String value) {
            addCriterion("response_code like", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeNotLike(String value) {
            addCriterion("response_code not like", value, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeIn(List<String> values) {
            addCriterion("response_code in", values, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeNotIn(List<String> values) {
            addCriterion("response_code not in", values, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeBetween(String value1, String value2) {
            addCriterion("response_code between", value1, value2, "responseCode");
            return (Criteria) this;
        }

        public Criteria andResponseCodeNotBetween(String value1, String value2) {
            addCriterion("response_code not between", value1, value2, "responseCode");
            return (Criteria) this;
        }

        public Criteria andAccountTypeIsNull() {
            addCriterion("account_type is null");
            return (Criteria) this;
        }

        public Criteria andAccountTypeIsNotNull() {
            addCriterion("account_type is not null");
            return (Criteria) this;
        }

        public Criteria andAccountTypeEqualTo(Byte value) {
            addCriterion("account_type =", value, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeNotEqualTo(Byte value) {
            addCriterion("account_type <>", value, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeGreaterThan(Byte value) {
            addCriterion("account_type >", value, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("account_type >=", value, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeLessThan(Byte value) {
            addCriterion("account_type <", value, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeLessThanOrEqualTo(Byte value) {
            addCriterion("account_type <=", value, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeIn(List<Byte> values) {
            addCriterion("account_type in", values, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeNotIn(List<Byte> values) {
            addCriterion("account_type not in", values, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeBetween(Byte value1, Byte value2) {
            addCriterion("account_type between", value1, value2, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("account_type not between", value1, value2, "accountType");
            return (Criteria) this;
        }

        public Criteria andAccountStatusIsNull() {
            addCriterion("account_status is null");
            return (Criteria) this;
        }

        public Criteria andAccountStatusIsNotNull() {
            addCriterion("account_status is not null");
            return (Criteria) this;
        }

        public Criteria andAccountStatusEqualTo(Byte value) {
            addCriterion("account_status =", value, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusNotEqualTo(Byte value) {
            addCriterion("account_status <>", value, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusGreaterThan(Byte value) {
            addCriterion("account_status >", value, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("account_status >=", value, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusLessThan(Byte value) {
            addCriterion("account_status <", value, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusLessThanOrEqualTo(Byte value) {
            addCriterion("account_status <=", value, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusIn(List<Byte> values) {
            addCriterion("account_status in", values, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusNotIn(List<Byte> values) {
            addCriterion("account_status not in", values, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusBetween(Byte value1, Byte value2) {
            addCriterion("account_status between", value1, value2, "accountStatus");
            return (Criteria) this;
        }

        public Criteria andAccountStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("account_status not between", value1, value2, "accountStatus");
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

        public Criteria andStartTimeEqualTo(Date value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(Date value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(Date value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(Date value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<Date> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<Date> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(Date value1, Date value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNull() {
            addCriterion("end_time is null");
            return (Criteria) this;
        }

        public Criteria andEndTimeIsNotNull() {
            addCriterion("end_time is not null");
            return (Criteria) this;
        }

        public Criteria andEndTimeEqualTo(Date value) {
            addCriterion("end_time =", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotEqualTo(Date value) {
            addCriterion("end_time <>", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThan(Date value) {
            addCriterion("end_time >", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("end_time >=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThan(Date value) {
            addCriterion("end_time <", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("end_time <=", value, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeIn(List<Date> values) {
            addCriterion("end_time in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotIn(List<Date> values) {
            addCriterion("end_time not in", values, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeBetween(Date value1, Date value2) {
            addCriterion("end_time between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("end_time not between", value1, value2, "endTime");
            return (Criteria) this;
        }

        public Criteria andTransportIsNull() {
            addCriterion("transport is null");
            return (Criteria) this;
        }

        public Criteria andTransportIsNotNull() {
            addCriterion("transport is not null");
            return (Criteria) this;
        }

        public Criteria andTransportEqualTo(String value) {
            addCriterion("transport =", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportNotEqualTo(String value) {
            addCriterion("transport <>", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportGreaterThan(String value) {
            addCriterion("transport >", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportGreaterThanOrEqualTo(String value) {
            addCriterion("transport >=", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportLessThan(String value) {
            addCriterion("transport <", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportLessThanOrEqualTo(String value) {
            addCriterion("transport <=", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportLike(String value) {
            addCriterion("transport like", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportNotLike(String value) {
            addCriterion("transport not like", value, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportIn(List<String> values) {
            addCriterion("transport in", values, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportNotIn(List<String> values) {
            addCriterion("transport not in", values, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportBetween(String value1, String value2) {
            addCriterion("transport between", value1, value2, "transport");
            return (Criteria) this;
        }

        public Criteria andTransportNotBetween(String value1, String value2) {
            addCriterion("transport not between", value1, value2, "transport");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeIsNull() {
            addCriterion("official_time is null");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeIsNotNull() {
            addCriterion("official_time is not null");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeEqualTo(Date value) {
            addCriterion("official_time =", value, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeNotEqualTo(Date value) {
            addCriterion("official_time <>", value, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeGreaterThan(Date value) {
            addCriterion("official_time >", value, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("official_time >=", value, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeLessThan(Date value) {
            addCriterion("official_time <", value, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeLessThanOrEqualTo(Date value) {
            addCriterion("official_time <=", value, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeIn(List<Date> values) {
            addCriterion("official_time in", values, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeNotIn(List<Date> values) {
            addCriterion("official_time not in", values, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeBetween(Date value1, Date value2) {
            addCriterion("official_time between", value1, value2, "officialTime");
            return (Criteria) this;
        }

        public Criteria andOfficialTimeNotBetween(Date value1, Date value2) {
            addCriterion("official_time not between", value1, value2, "officialTime");
            return (Criteria) this;
        }

        public Criteria andModifyUserIsNull() {
            addCriterion("modify_user is null");
            return (Criteria) this;
        }

        public Criteria andModifyUserIsNotNull() {
            addCriterion("modify_user is not null");
            return (Criteria) this;
        }

        public Criteria andModifyUserEqualTo(String value) {
            addCriterion("modify_user =", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserNotEqualTo(String value) {
            addCriterion("modify_user <>", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserGreaterThan(String value) {
            addCriterion("modify_user >", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserGreaterThanOrEqualTo(String value) {
            addCriterion("modify_user >=", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserLessThan(String value) {
            addCriterion("modify_user <", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserLessThanOrEqualTo(String value) {
            addCriterion("modify_user <=", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserLike(String value) {
            addCriterion("modify_user like", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserNotLike(String value) {
            addCriterion("modify_user not like", value, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserIn(List<String> values) {
            addCriterion("modify_user in", values, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserNotIn(List<String> values) {
            addCriterion("modify_user not in", values, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserBetween(String value1, String value2) {
            addCriterion("modify_user between", value1, value2, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andModifyUserNotBetween(String value1, String value2) {
            addCriterion("modify_user not between", value1, value2, "modifyUser");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyIsNull() {
            addCriterion("encryption_key is null");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyIsNotNull() {
            addCriterion("encryption_key is not null");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyEqualTo(String value) {
            addCriterion("encryption_key =", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyNotEqualTo(String value) {
            addCriterion("encryption_key <>", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyGreaterThan(String value) {
            addCriterion("encryption_key >", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyGreaterThanOrEqualTo(String value) {
            addCriterion("encryption_key >=", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyLessThan(String value) {
            addCriterion("encryption_key <", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyLessThanOrEqualTo(String value) {
            addCriterion("encryption_key <=", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyLike(String value) {
            addCriterion("encryption_key like", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyNotLike(String value) {
            addCriterion("encryption_key not like", value, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyIn(List<String> values) {
            addCriterion("encryption_key in", values, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyNotIn(List<String> values) {
            addCriterion("encryption_key not in", values, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyBetween(String value1, String value2) {
            addCriterion("encryption_key between", value1, value2, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andEncryptionKeyNotBetween(String value1, String value2) {
            addCriterion("encryption_key not between", value1, value2, "encryptionKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyIsNull() {
            addCriterion("decrypt_key is null");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyIsNotNull() {
            addCriterion("decrypt_key is not null");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyEqualTo(String value) {
            addCriterion("decrypt_key =", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyNotEqualTo(String value) {
            addCriterion("decrypt_key <>", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyGreaterThan(String value) {
            addCriterion("decrypt_key >", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyGreaterThanOrEqualTo(String value) {
            addCriterion("decrypt_key >=", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyLessThan(String value) {
            addCriterion("decrypt_key <", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyLessThanOrEqualTo(String value) {
            addCriterion("decrypt_key <=", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyLike(String value) {
            addCriterion("decrypt_key like", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyNotLike(String value) {
            addCriterion("decrypt_key not like", value, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyIn(List<String> values) {
            addCriterion("decrypt_key in", values, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyNotIn(List<String> values) {
            addCriterion("decrypt_key not in", values, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyBetween(String value1, String value2) {
            addCriterion("decrypt_key between", value1, value2, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andDecryptKeyNotBetween(String value1, String value2) {
            addCriterion("decrypt_key not between", value1, value2, "decryptKey");
            return (Criteria) this;
        }

        public Criteria andSnVerIsNull() {
            addCriterion("sn_ver is null");
            return (Criteria) this;
        }

        public Criteria andSnVerIsNotNull() {
            addCriterion("sn_ver is not null");
            return (Criteria) this;
        }

        public Criteria andSnVerEqualTo(String value) {
            addCriterion("sn_ver =", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerNotEqualTo(String value) {
            addCriterion("sn_ver <>", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerGreaterThan(String value) {
            addCriterion("sn_ver >", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerGreaterThanOrEqualTo(String value) {
            addCriterion("sn_ver >=", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerLessThan(String value) {
            addCriterion("sn_ver <", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerLessThanOrEqualTo(String value) {
            addCriterion("sn_ver <=", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerLike(String value) {
            addCriterion("sn_ver like", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerNotLike(String value) {
            addCriterion("sn_ver not like", value, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerIn(List<String> values) {
            addCriterion("sn_ver in", values, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerNotIn(List<String> values) {
            addCriterion("sn_ver not in", values, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerBetween(String value1, String value2) {
            addCriterion("sn_ver between", value1, value2, "snVer");
            return (Criteria) this;
        }

        public Criteria andSnVerNotBetween(String value1, String value2) {
            addCriterion("sn_ver not between", value1, value2, "snVer");
            return (Criteria) this;
        }

        public Criteria andCallMethodIsNull() {
            addCriterion("call_method is null");
            return (Criteria) this;
        }

        public Criteria andCallMethodIsNotNull() {
            addCriterion("call_method is not null");
            return (Criteria) this;
        }

        public Criteria andCallMethodEqualTo(Integer value) {
            addCriterion("call_method =", value, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodNotEqualTo(Integer value) {
            addCriterion("call_method <>", value, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodGreaterThan(Integer value) {
            addCriterion("call_method >", value, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodGreaterThanOrEqualTo(Integer value) {
            addCriterion("call_method >=", value, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodLessThan(Integer value) {
            addCriterion("call_method <", value, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodLessThanOrEqualTo(Integer value) {
            addCriterion("call_method <=", value, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodIn(List<Integer> values) {
            addCriterion("call_method in", values, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodNotIn(List<Integer> values) {
            addCriterion("call_method not in", values, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodBetween(Integer value1, Integer value2) {
            addCriterion("call_method between", value1, value2, "callMethod");
            return (Criteria) this;
        }

        public Criteria andCallMethodNotBetween(Integer value1, Integer value2) {
            addCriterion("call_method not between", value1, value2, "callMethod");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsIsNull() {
            addCriterion("file_encryption_methods is null");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsIsNotNull() {
            addCriterion("file_encryption_methods is not null");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsEqualTo(Integer value) {
            addCriterion("file_encryption_methods =", value, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsNotEqualTo(Integer value) {
            addCriterion("file_encryption_methods <>", value, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsGreaterThan(Integer value) {
            addCriterion("file_encryption_methods >", value, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsGreaterThanOrEqualTo(Integer value) {
            addCriterion("file_encryption_methods >=", value, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsLessThan(Integer value) {
            addCriterion("file_encryption_methods <", value, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsLessThanOrEqualTo(Integer value) {
            addCriterion("file_encryption_methods <=", value, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsIn(List<Integer> values) {
            addCriterion("file_encryption_methods in", values, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsNotIn(List<Integer> values) {
            addCriterion("file_encryption_methods not in", values, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsBetween(Integer value1, Integer value2) {
            addCriterion("file_encryption_methods between", value1, value2, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionMethodsNotBetween(Integer value1, Integer value2) {
            addCriterion("file_encryption_methods not between", value1, value2, "fileEncryptionMethods");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmIsNull() {
            addCriterion("file_encryption_algorithm is null");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmIsNotNull() {
            addCriterion("file_encryption_algorithm is not null");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmEqualTo(Integer value) {
            addCriterion("file_encryption_algorithm =", value, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmNotEqualTo(Integer value) {
            addCriterion("file_encryption_algorithm <>", value, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmGreaterThan(Integer value) {
            addCriterion("file_encryption_algorithm >", value, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmGreaterThanOrEqualTo(Integer value) {
            addCriterion("file_encryption_algorithm >=", value, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmLessThan(Integer value) {
            addCriterion("file_encryption_algorithm <", value, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmLessThanOrEqualTo(Integer value) {
            addCriterion("file_encryption_algorithm <=", value, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmIn(List<Integer> values) {
            addCriterion("file_encryption_algorithm in", values, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmNotIn(List<Integer> values) {
            addCriterion("file_encryption_algorithm not in", values, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmBetween(Integer value1, Integer value2) {
            addCriterion("file_encryption_algorithm between", value1, value2, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionAlgorithmNotBetween(Integer value1, Integer value2) {
            addCriterion("file_encryption_algorithm not between", value1, value2, "fileEncryptionAlgorithm");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyIsNull() {
            addCriterion("file_encryption_key is null");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyIsNotNull() {
            addCriterion("file_encryption_key is not null");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyEqualTo(String value) {
            addCriterion("file_encryption_key =", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyNotEqualTo(String value) {
            addCriterion("file_encryption_key <>", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyGreaterThan(String value) {
            addCriterion("file_encryption_key >", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyGreaterThanOrEqualTo(String value) {
            addCriterion("file_encryption_key >=", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyLessThan(String value) {
            addCriterion("file_encryption_key <", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyLessThanOrEqualTo(String value) {
            addCriterion("file_encryption_key <=", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyLike(String value) {
            addCriterion("file_encryption_key like", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyNotLike(String value) {
            addCriterion("file_encryption_key not like", value, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyIn(List<String> values) {
            addCriterion("file_encryption_key in", values, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyNotIn(List<String> values) {
            addCriterion("file_encryption_key not in", values, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyBetween(String value1, String value2) {
            addCriterion("file_encryption_key between", value1, value2, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andFileEncryptionKeyNotBetween(String value1, String value2) {
            addCriterion("file_encryption_key not between", value1, value2, "fileEncryptionKey");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNull() {
            addCriterion("create_user is null");
            return (Criteria) this;
        }

        public Criteria andCreateUserIsNotNull() {
            addCriterion("create_user is not null");
            return (Criteria) this;
        }

        public Criteria andCreateUserEqualTo(String value) {
            addCriterion("create_user =", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotEqualTo(String value) {
            addCriterion("create_user <>", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThan(String value) {
            addCriterion("create_user >", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserGreaterThanOrEqualTo(String value) {
            addCriterion("create_user >=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThan(String value) {
            addCriterion("create_user <", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLessThanOrEqualTo(String value) {
            addCriterion("create_user <=", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserLike(String value) {
            addCriterion("create_user like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotLike(String value) {
            addCriterion("create_user not like", value, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserIn(List<String> values) {
            addCriterion("create_user in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotIn(List<String> values) {
            addCriterion("create_user not in", values, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserBetween(String value1, String value2) {
            addCriterion("create_user between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andCreateUserNotBetween(String value1, String value2) {
            addCriterion("create_user not between", value1, value2, "createUser");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductIsNull() {
            addCriterion("is_output_data_product is null");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductIsNotNull() {
            addCriterion("is_output_data_product is not null");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductEqualTo(Byte value) {
            addCriterion("is_output_data_product =", value, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductNotEqualTo(Byte value) {
            addCriterion("is_output_data_product <>", value, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductGreaterThan(Byte value) {
            addCriterion("is_output_data_product >", value, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_output_data_product >=", value, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductLessThan(Byte value) {
            addCriterion("is_output_data_product <", value, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductLessThanOrEqualTo(Byte value) {
            addCriterion("is_output_data_product <=", value, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductIn(List<Byte> values) {
            addCriterion("is_output_data_product in", values, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductNotIn(List<Byte> values) {
            addCriterion("is_output_data_product not in", values, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductBetween(Byte value1, Byte value2) {
            addCriterion("is_output_data_product between", value1, value2, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andIsOutputDataProductNotBetween(Byte value1, Byte value2) {
            addCriterion("is_output_data_product not between", value1, value2, "isOutputDataProduct");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeIsNull() {
            addCriterion("apply_loan_type is null");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeIsNotNull() {
            addCriterion("apply_loan_type is not null");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeEqualTo(Integer value) {
            addCriterion("apply_loan_type =", value, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeNotEqualTo(Integer value) {
            addCriterion("apply_loan_type <>", value, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeGreaterThan(Integer value) {
            addCriterion("apply_loan_type >", value, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("apply_loan_type >=", value, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeLessThan(Integer value) {
            addCriterion("apply_loan_type <", value, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeLessThanOrEqualTo(Integer value) {
            addCriterion("apply_loan_type <=", value, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeIn(List<Integer> values) {
            addCriterion("apply_loan_type in", values, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeNotIn(List<Integer> values) {
            addCriterion("apply_loan_type not in", values, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeBetween(Integer value1, Integer value2) {
            addCriterion("apply_loan_type between", value1, value2, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApplyLoanTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("apply_loan_type not between", value1, value2, "applyLoanType");
            return (Criteria) this;
        }

        public Criteria andApiTypeIsNull() {
            addCriterion("api_type is null");
            return (Criteria) this;
        }

        public Criteria andApiTypeIsNotNull() {
            addCriterion("api_type is not null");
            return (Criteria) this;
        }

        public Criteria andApiTypeEqualTo(String value) {
            addCriterion("api_type =", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeNotEqualTo(String value) {
            addCriterion("api_type <>", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeGreaterThan(String value) {
            addCriterion("api_type >", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeGreaterThanOrEqualTo(String value) {
            addCriterion("api_type >=", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeLessThan(String value) {
            addCriterion("api_type <", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeLessThanOrEqualTo(String value) {
            addCriterion("api_type <=", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeLike(String value) {
            addCriterion("api_type like", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeNotLike(String value) {
            addCriterion("api_type not like", value, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeIn(List<String> values) {
            addCriterion("api_type in", values, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeNotIn(List<String> values) {
            addCriterion("api_type not in", values, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeBetween(String value1, String value2) {
            addCriterion("api_type between", value1, value2, "apiType");
            return (Criteria) this;
        }

        public Criteria andApiTypeNotBetween(String value1, String value2) {
            addCriterion("api_type not between", value1, value2, "apiType");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryIsNull() {
            addCriterion("sms_category is null");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryIsNotNull() {
            addCriterion("sms_category is not null");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryEqualTo(String value) {
            addCriterion("sms_category =", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryNotEqualTo(String value) {
            addCriterion("sms_category <>", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryGreaterThan(String value) {
            addCriterion("sms_category >", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryGreaterThanOrEqualTo(String value) {
            addCriterion("sms_category >=", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryLessThan(String value) {
            addCriterion("sms_category <", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryLessThanOrEqualTo(String value) {
            addCriterion("sms_category <=", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryLike(String value) {
            addCriterion("sms_category like", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryNotLike(String value) {
            addCriterion("sms_category not like", value, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryIn(List<String> values) {
            addCriterion("sms_category in", values, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryNotIn(List<String> values) {
            addCriterion("sms_category not in", values, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryBetween(String value1, String value2) {
            addCriterion("sms_category between", value1, value2, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andSmsCategoryNotBetween(String value1, String value2) {
            addCriterion("sms_category not between", value1, value2, "smsCategory");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentIsNull() {
            addCriterion("first_department is null");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentIsNotNull() {
            addCriterion("first_department is not null");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentEqualTo(String value) {
            addCriterion("first_department =", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentNotEqualTo(String value) {
            addCriterion("first_department <>", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentGreaterThan(String value) {
            addCriterion("first_department >", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentGreaterThanOrEqualTo(String value) {
            addCriterion("first_department >=", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentLessThan(String value) {
            addCriterion("first_department <", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentLessThanOrEqualTo(String value) {
            addCriterion("first_department <=", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentLike(String value) {
            addCriterion("first_department like", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentNotLike(String value) {
            addCriterion("first_department not like", value, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentIn(List<String> values) {
            addCriterion("first_department in", values, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentNotIn(List<String> values) {
            addCriterion("first_department not in", values, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentBetween(String value1, String value2) {
            addCriterion("first_department between", value1, value2, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andFirstDepartmentNotBetween(String value1, String value2) {
            addCriterion("first_department not between", value1, value2, "firstDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentIsNull() {
            addCriterion("second_department is null");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentIsNotNull() {
            addCriterion("second_department is not null");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentEqualTo(String value) {
            addCriterion("second_department =", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentNotEqualTo(String value) {
            addCriterion("second_department <>", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentGreaterThan(String value) {
            addCriterion("second_department >", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentGreaterThanOrEqualTo(String value) {
            addCriterion("second_department >=", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentLessThan(String value) {
            addCriterion("second_department <", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentLessThanOrEqualTo(String value) {
            addCriterion("second_department <=", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentLike(String value) {
            addCriterion("second_department like", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentNotLike(String value) {
            addCriterion("second_department not like", value, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentIn(List<String> values) {
            addCriterion("second_department in", values, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentNotIn(List<String> values) {
            addCriterion("second_department not in", values, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentBetween(String value1, String value2) {
            addCriterion("second_department between", value1, value2, "secondDepartment");
            return (Criteria) this;
        }

        public Criteria andSecondDepartmentNotBetween(String value1, String value2) {
            addCriterion("second_department not between", value1, value2, "secondDepartment");
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