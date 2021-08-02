package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.List;

public class MarketingCustomerExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public MarketingCustomerExample() {
        oredCriteria = new ArrayList<BaseCriteria>();
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

    public List<BaseCriteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(BaseCriteria criteria) {
        oredCriteria.add(criteria);
    }

    public BaseCriteria or() {
        BaseCriteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public BaseCriteria createCriteria() {
        BaseCriteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected BaseCriteria createCriteriaInternal() {
        BaseCriteria criteria = new BaseCriteria();
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

        public BaseCriteria andIdIsNull() {
            addCriterion("id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIsNull() {
            addCriterion("api_code is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIsNotNull() {
            addCriterion("api_code is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeEqualTo(String value) {
            addCriterion("api_code =", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotEqualTo(String value) {
            addCriterion("api_code <>", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeGreaterThan(String value) {
            addCriterion("api_code >", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("api_code >=", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLessThan(String value) {
            addCriterion("api_code <", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLessThanOrEqualTo(String value) {
            addCriterion("api_code <=", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLike(String value) {
            addCriterion("api_code like", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotLike(String value) {
            addCriterion("api_code not like", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIn(List<String> values) {
            addCriterion("api_code in", values, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotIn(List<String> values) {
            addCriterion("api_code not in", values, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeBetween(String value1, String value2) {
            addCriterion("api_code between", value1, value2, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotBetween(String value1, String value2) {
            addCriterion("api_code not between", value1, value2, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageIsNull() {
            addCriterion("message is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageIsNotNull() {
            addCriterion("message is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageEqualTo(String value) {
            addCriterion("message =", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageNotEqualTo(String value) {
            addCriterion("message <>", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageGreaterThan(String value) {
            addCriterion("message >", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageGreaterThanOrEqualTo(String value) {
            addCriterion("message >=", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageLessThan(String value) {
            addCriterion("message <", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageLessThanOrEqualTo(String value) {
            addCriterion("message <=", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageLike(String value) {
            addCriterion("message like", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageNotLike(String value) {
            addCriterion("message not like", value, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageIn(List<String> values) {
            addCriterion("message in", values, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageNotIn(List<String> values) {
            addCriterion("message not in", values, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageBetween(String value1, String value2) {
            addCriterion("message between", value1, value2, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMessageNotBetween(String value1, String value2) {
            addCriterion("message not between", value1, value2, "message");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeIsNull() {
            addCriterion("type is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeEqualTo(String value) {
            addCriterion("type =", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeNotEqualTo(String value) {
            addCriterion("type <>", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeGreaterThan(String value) {
            addCriterion("type >", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("type >=", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeLessThan(String value) {
            addCriterion("type <", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("type <=", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeLike(String value) {
            addCriterion("type like", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeNotLike(String value) {
            addCriterion("type not like", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeIn(List<String> values) {
            addCriterion("type in", values, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeNotIn(List<String> values) {
            addCriterion("type not in", values, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeBetween(String value1, String value2) {
            addCriterion("type between", value1, value2, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeNotBetween(String value1, String value2) {
            addCriterion("type not between", value1, value2, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumIsNull() {
            addCriterion("thread_num is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumIsNotNull() {
            addCriterion("thread_num is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumEqualTo(Integer value) {
            addCriterion("thread_num =", value, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumNotEqualTo(Integer value) {
            addCriterion("thread_num <>", value, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumGreaterThan(Integer value) {
            addCriterion("thread_num >", value, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("thread_num >=", value, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumLessThan(Integer value) {
            addCriterion("thread_num <", value, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumLessThanOrEqualTo(Integer value) {
            addCriterion("thread_num <=", value, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumIn(List<Integer> values) {
            addCriterion("thread_num in", values, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumNotIn(List<Integer> values) {
            addCriterion("thread_num not in", values, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumBetween(Integer value1, Integer value2) {
            addCriterion("thread_num between", value1, value2, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andThreadNumNotBetween(Integer value1, Integer value2) {
            addCriterion("thread_num not between", value1, value2, "threadNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeIsNull() {
            addCriterion("task_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeIsNotNull() {
            addCriterion("task_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeEqualTo(Byte value) {
            addCriterion("task_time =", value, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeNotEqualTo(Byte value) {
            addCriterion("task_time <>", value, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeGreaterThan(Byte value) {
            addCriterion("task_time >", value, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeGreaterThanOrEqualTo(Byte value) {
            addCriterion("task_time >=", value, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeLessThan(Byte value) {
            addCriterion("task_time <", value, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeLessThanOrEqualTo(Byte value) {
            addCriterion("task_time <=", value, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeIn(List<Byte> values) {
            addCriterion("task_time in", values, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeNotIn(List<Byte> values) {
            addCriterion("task_time not in", values, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeBetween(Byte value1, Byte value2) {
            addCriterion("task_time between", value1, value2, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskTimeNotBetween(Byte value1, Byte value2) {
            addCriterion("task_time not between", value1, value2, "taskTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateIsNull() {
            addCriterion("finish_date is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateIsNotNull() {
            addCriterion("finish_date is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateEqualTo(Byte value) {
            addCriterion("finish_date =", value, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateNotEqualTo(Byte value) {
            addCriterion("finish_date <>", value, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateGreaterThan(Byte value) {
            addCriterion("finish_date >", value, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateGreaterThanOrEqualTo(Byte value) {
            addCriterion("finish_date >=", value, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateLessThan(Byte value) {
            addCriterion("finish_date <", value, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateLessThanOrEqualTo(Byte value) {
            addCriterion("finish_date <=", value, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateIn(List<Byte> values) {
            addCriterion("finish_date in", values, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateNotIn(List<Byte> values) {
            addCriterion("finish_date not in", values, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateBetween(Byte value1, Byte value2) {
            addCriterion("finish_date between", value1, value2, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishDateNotBetween(Byte value1, Byte value2) {
            addCriterion("finish_date not between", value1, value2, "finishDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerIsNull() {
            addCriterion("push_customer is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerIsNotNull() {
            addCriterion("push_customer is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerEqualTo(Byte value) {
            addCriterion("push_customer =", value, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerNotEqualTo(Byte value) {
            addCriterion("push_customer <>", value, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerGreaterThan(Byte value) {
            addCriterion("push_customer >", value, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerGreaterThanOrEqualTo(Byte value) {
            addCriterion("push_customer >=", value, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerLessThan(Byte value) {
            addCriterion("push_customer <", value, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerLessThanOrEqualTo(Byte value) {
            addCriterion("push_customer <=", value, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerIn(List<Byte> values) {
            addCriterion("push_customer in", values, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerNotIn(List<Byte> values) {
            addCriterion("push_customer not in", values, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerBetween(Byte value1, Byte value2) {
            addCriterion("push_customer between", value1, value2, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andPushCustomerNotBetween(Byte value1, Byte value2) {
            addCriterion("push_customer not between", value1, value2, "pushCustomer");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListIsNull() {
            addCriterion("check_black_list is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListIsNotNull() {
            addCriterion("check_black_list is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListEqualTo(Byte value) {
            addCriterion("check_black_list =", value, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListNotEqualTo(Byte value) {
            addCriterion("check_black_list <>", value, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListGreaterThan(Byte value) {
            addCriterion("check_black_list >", value, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListGreaterThanOrEqualTo(Byte value) {
            addCriterion("check_black_list >=", value, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListLessThan(Byte value) {
            addCriterion("check_black_list <", value, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListLessThanOrEqualTo(Byte value) {
            addCriterion("check_black_list <=", value, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListIn(List<Byte> values) {
            addCriterion("check_black_list in", values, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListNotIn(List<Byte> values) {
            addCriterion("check_black_list not in", values, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListBetween(Byte value1, Byte value2) {
            addCriterion("check_black_list between", value1, value2, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckBlackListNotBetween(Byte value1, Byte value2) {
            addCriterion("check_black_list not between", value1, value2, "checkBlackList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberIsNull() {
            addCriterion("check_redis_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberIsNotNull() {
            addCriterion("check_redis_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberEqualTo(Byte value) {
            addCriterion("check_redis_number =", value, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberNotEqualTo(Byte value) {
            addCriterion("check_redis_number <>", value, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberGreaterThan(Byte value) {
            addCriterion("check_redis_number >", value, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberGreaterThanOrEqualTo(Byte value) {
            addCriterion("check_redis_number >=", value, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberLessThan(Byte value) {
            addCriterion("check_redis_number <", value, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberLessThanOrEqualTo(Byte value) {
            addCriterion("check_redis_number <=", value, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberIn(List<Byte> values) {
            addCriterion("check_redis_number in", values, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberNotIn(List<Byte> values) {
            addCriterion("check_redis_number not in", values, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberBetween(Byte value1, Byte value2) {
            addCriterion("check_redis_number between", value1, value2, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCheckRedisNumberNotBetween(Byte value1, Byte value2) {
            addCriterion("check_redis_number not between", value1, value2, "checkRedisNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogIsNull() {
            addCriterion("save_log is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogIsNotNull() {
            addCriterion("save_log is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogEqualTo(Byte value) {
            addCriterion("save_log =", value, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogNotEqualTo(Byte value) {
            addCriterion("save_log <>", value, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogGreaterThan(Byte value) {
            addCriterion("save_log >", value, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogGreaterThanOrEqualTo(Byte value) {
            addCriterion("save_log >=", value, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogLessThan(Byte value) {
            addCriterion("save_log <", value, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogLessThanOrEqualTo(Byte value) {
            addCriterion("save_log <=", value, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogIn(List<Byte> values) {
            addCriterion("save_log in", values, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogNotIn(List<Byte> values) {
            addCriterion("save_log not in", values, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogBetween(Byte value1, Byte value2) {
            addCriterion("save_log between", value1, value2, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSaveLogNotBetween(Byte value1, Byte value2) {
            addCriterion("save_log not between", value1, value2, "saveLog");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortIsNull() {
            addCriterion("sort is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortIsNotNull() {
            addCriterion("sort is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortEqualTo(Byte value) {
            addCriterion("sort =", value, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortNotEqualTo(Byte value) {
            addCriterion("sort <>", value, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortGreaterThan(Byte value) {
            addCriterion("sort >", value, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortGreaterThanOrEqualTo(Byte value) {
            addCriterion("sort >=", value, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortLessThan(Byte value) {
            addCriterion("sort <", value, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortLessThanOrEqualTo(Byte value) {
            addCriterion("sort <=", value, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortIn(List<Byte> values) {
            addCriterion("sort in", values, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortNotIn(List<Byte> values) {
            addCriterion("sort not in", values, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortBetween(Byte value1, Byte value2) {
            addCriterion("sort between", value1, value2, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSortNotBetween(Byte value1, Byte value2) {
            addCriterion("sort not between", value1, value2, "sort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIsNull() {
            addCriterion("status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusEqualTo(Byte value) {
            addCriterion("status =", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotEqualTo(Byte value) {
            addCriterion("status <>", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusGreaterThan(Byte value) {
            addCriterion("status >", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("status >=", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusLessThan(Byte value) {
            addCriterion("status <", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("status <=", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIn(List<Byte> values) {
            addCriterion("status in", values, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotIn(List<Byte> values) {
            addCriterion("status not in", values, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("status between", value1, value2, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("status not between", value1, value2, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoIsNull() {
            addCriterion("extend_config_info is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoIsNotNull() {
            addCriterion("extend_config_info is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoEqualTo(String value) {
            addCriterion("extend_config_info =", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoNotEqualTo(String value) {
            addCriterion("extend_config_info <>", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoGreaterThan(String value) {
            addCriterion("extend_config_info >", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoGreaterThanOrEqualTo(String value) {
            addCriterion("extend_config_info >=", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoLessThan(String value) {
            addCriterion("extend_config_info <", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoLessThanOrEqualTo(String value) {
            addCriterion("extend_config_info <=", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoLike(String value) {
            addCriterion("extend_config_info like", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoNotLike(String value) {
            addCriterion("extend_config_info not like", value, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoIn(List<String> values) {
            addCriterion("extend_config_info in", values, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoNotIn(List<String> values) {
            addCriterion("extend_config_info not in", values, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoBetween(String value1, String value2) {
            addCriterion("extend_config_info between", value1, value2, "extendConfigInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExtendConfigInfoNotBetween(String value1, String value2) {
            addCriterion("extend_config_info not between", value1, value2, "extendConfigInfo");
            return (BaseCriteria) this;
        }
    }

    public static class BaseCriteria extends GeneratedCriteria {

        protected BaseCriteria() {
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