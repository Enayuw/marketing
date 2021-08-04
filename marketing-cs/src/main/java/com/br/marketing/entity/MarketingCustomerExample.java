package com.br.marketing.entity;

import java.util.ArrayList;
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
            addCriterion("type is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("type like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("type not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("type in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("type not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("type between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("type not between", value1, value2, "type");
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
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("status not between", value1, value2, "status");
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