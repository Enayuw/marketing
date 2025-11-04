package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XiechengCollidingDataProcessTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public XiechengCollidingDataProcessTaskExample() {
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

        public Criteria andTaskStatusIsNull() {
            addCriterion("task_status is null");
            return (Criteria) this;
        }

        public Criteria andTaskStatusIsNotNull() {
            addCriterion("task_status is not null");
            return (Criteria) this;
        }

        public Criteria andTaskStatusEqualTo(Integer value) {
            addCriterion("task_status =", value, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusNotEqualTo(Integer value) {
            addCriterion("task_status <>", value, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusGreaterThan(Integer value) {
            addCriterion("task_status >", value, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("task_status >=", value, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusLessThan(Integer value) {
            addCriterion("task_status <", value, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusLessThanOrEqualTo(Integer value) {
            addCriterion("task_status <=", value, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusIn(List<Integer> values) {
            addCriterion("task_status in", values, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusNotIn(List<Integer> values) {
            addCriterion("task_status not in", values, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusBetween(Integer value1, Integer value2) {
            addCriterion("task_status between", value1, value2, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andTaskStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("task_status not between", value1, value2, "taskStatus");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberIsNull() {
            addCriterion("discreet_number is null");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberIsNotNull() {
            addCriterion("discreet_number is not null");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberEqualTo(Integer value) {
            addCriterion("discreet_number =", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberNotEqualTo(Integer value) {
            addCriterion("discreet_number <>", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberGreaterThan(Integer value) {
            addCriterion("discreet_number >", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("discreet_number >=", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberLessThan(Integer value) {
            addCriterion("discreet_number <", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberLessThanOrEqualTo(Integer value) {
            addCriterion("discreet_number <=", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberIn(List<Integer> values) {
            addCriterion("discreet_number in", values, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberNotIn(List<Integer> values) {
            addCriterion("discreet_number not in", values, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberBetween(Integer value1, Integer value2) {
            addCriterion("discreet_number between", value1, value2, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("discreet_number not between", value1, value2, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberIsNull() {
            addCriterion("actual_number is null");
            return (Criteria) this;
        }

        public Criteria andActualNumberIsNotNull() {
            addCriterion("actual_number is not null");
            return (Criteria) this;
        }

        public Criteria andActualNumberEqualTo(Integer value) {
            addCriterion("actual_number =", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotEqualTo(Integer value) {
            addCriterion("actual_number <>", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberGreaterThan(Integer value) {
            addCriterion("actual_number >", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_number >=", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberLessThan(Integer value) {
            addCriterion("actual_number <", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberLessThanOrEqualTo(Integer value) {
            addCriterion("actual_number <=", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberIn(List<Integer> values) {
            addCriterion("actual_number in", values, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotIn(List<Integer> values) {
            addCriterion("actual_number not in", values, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberBetween(Integer value1, Integer value2) {
            addCriterion("actual_number between", value1, value2, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_number not between", value1, value2, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andRemainingNumIsNull() {
            addCriterion("remaining_num is null");
            return (Criteria) this;
        }

        public Criteria andRemainingNumIsNotNull() {
            addCriterion("remaining_num is not null");
            return (Criteria) this;
        }

        public Criteria andRemainingNumEqualTo(Integer value) {
            addCriterion("remaining_num =", value, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumNotEqualTo(Integer value) {
            addCriterion("remaining_num <>", value, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumGreaterThan(Integer value) {
            addCriterion("remaining_num >", value, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("remaining_num >=", value, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumLessThan(Integer value) {
            addCriterion("remaining_num <", value, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumLessThanOrEqualTo(Integer value) {
            addCriterion("remaining_num <=", value, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumIn(List<Integer> values) {
            addCriterion("remaining_num in", values, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumNotIn(List<Integer> values) {
            addCriterion("remaining_num not in", values, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumBetween(Integer value1, Integer value2) {
            addCriterion("remaining_num between", value1, value2, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andRemainingNumNotBetween(Integer value1, Integer value2) {
            addCriterion("remaining_num not between", value1, value2, "remainingNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumIsNull() {
            addCriterion("free_num is null");
            return (Criteria) this;
        }

        public Criteria andFreeNumIsNotNull() {
            addCriterion("free_num is not null");
            return (Criteria) this;
        }

        public Criteria andFreeNumEqualTo(Integer value) {
            addCriterion("free_num =", value, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumNotEqualTo(Integer value) {
            addCriterion("free_num <>", value, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumGreaterThan(Integer value) {
            addCriterion("free_num >", value, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("free_num >=", value, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumLessThan(Integer value) {
            addCriterion("free_num <", value, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumLessThanOrEqualTo(Integer value) {
            addCriterion("free_num <=", value, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumIn(List<Integer> values) {
            addCriterion("free_num in", values, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumNotIn(List<Integer> values) {
            addCriterion("free_num not in", values, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumBetween(Integer value1, Integer value2) {
            addCriterion("free_num between", value1, value2, "freeNum");
            return (Criteria) this;
        }

        public Criteria andFreeNumNotBetween(Integer value1, Integer value2) {
            addCriterion("free_num not between", value1, value2, "freeNum");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeIsNull() {
            addCriterion("task_start_time is null");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeIsNotNull() {
            addCriterion("task_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeEqualTo(Date value) {
            addCriterion("task_start_time =", value, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeNotEqualTo(Date value) {
            addCriterion("task_start_time <>", value, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeGreaterThan(Date value) {
            addCriterion("task_start_time >", value, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("task_start_time >=", value, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeLessThan(Date value) {
            addCriterion("task_start_time <", value, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("task_start_time <=", value, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeIn(List<Date> values) {
            addCriterion("task_start_time in", values, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeNotIn(List<Date> values) {
            addCriterion("task_start_time not in", values, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeBetween(Date value1, Date value2) {
            addCriterion("task_start_time between", value1, value2, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("task_start_time not between", value1, value2, "taskStartTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeIsNull() {
            addCriterion("task_end_time is null");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeIsNotNull() {
            addCriterion("task_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeEqualTo(Date value) {
            addCriterion("task_end_time =", value, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeNotEqualTo(Date value) {
            addCriterion("task_end_time <>", value, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeGreaterThan(Date value) {
            addCriterion("task_end_time >", value, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("task_end_time >=", value, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeLessThan(Date value) {
            addCriterion("task_end_time <", value, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("task_end_time <=", value, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeIn(List<Date> values) {
            addCriterion("task_end_time in", values, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeNotIn(List<Date> values) {
            addCriterion("task_end_time not in", values, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeBetween(Date value1, Date value2) {
            addCriterion("task_end_time between", value1, value2, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("task_end_time not between", value1, value2, "taskEndTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeIsNull() {
            addCriterion("task_execute_time is null");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeIsNotNull() {
            addCriterion("task_execute_time is not null");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeEqualTo(Date value) {
            addCriterion("task_execute_time =", value, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeNotEqualTo(Date value) {
            addCriterion("task_execute_time <>", value, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeGreaterThan(Date value) {
            addCriterion("task_execute_time >", value, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("task_execute_time >=", value, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeLessThan(Date value) {
            addCriterion("task_execute_time <", value, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeLessThanOrEqualTo(Date value) {
            addCriterion("task_execute_time <=", value, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeIn(List<Date> values) {
            addCriterion("task_execute_time in", values, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeNotIn(List<Date> values) {
            addCriterion("task_execute_time not in", values, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeBetween(Date value1, Date value2) {
            addCriterion("task_execute_time between", value1, value2, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andTaskExecuteTimeNotBetween(Date value1, Date value2) {
            addCriterion("task_execute_time not between", value1, value2, "taskExecuteTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginIsNull() {
            addCriterion("release_time_begin is null");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginIsNotNull() {
            addCriterion("release_time_begin is not null");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginEqualTo(Date value) {
            addCriterion("release_time_begin =", value, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginNotEqualTo(Date value) {
            addCriterion("release_time_begin <>", value, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginGreaterThan(Date value) {
            addCriterion("release_time_begin >", value, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginGreaterThanOrEqualTo(Date value) {
            addCriterion("release_time_begin >=", value, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginLessThan(Date value) {
            addCriterion("release_time_begin <", value, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginLessThanOrEqualTo(Date value) {
            addCriterion("release_time_begin <=", value, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginIn(List<Date> values) {
            addCriterion("release_time_begin in", values, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginNotIn(List<Date> values) {
            addCriterion("release_time_begin not in", values, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginBetween(Date value1, Date value2) {
            addCriterion("release_time_begin between", value1, value2, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBeginNotBetween(Date value1, Date value2) {
            addCriterion("release_time_begin not between", value1, value2, "releaseTimeBegin");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndIsNull() {
            addCriterion("release_time_end is null");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndIsNotNull() {
            addCriterion("release_time_end is not null");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndEqualTo(Date value) {
            addCriterion("release_time_end =", value, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndNotEqualTo(Date value) {
            addCriterion("release_time_end <>", value, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndGreaterThan(Date value) {
            addCriterion("release_time_end >", value, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndGreaterThanOrEqualTo(Date value) {
            addCriterion("release_time_end >=", value, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndLessThan(Date value) {
            addCriterion("release_time_end <", value, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndLessThanOrEqualTo(Date value) {
            addCriterion("release_time_end <=", value, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndIn(List<Date> values) {
            addCriterion("release_time_end in", values, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndNotIn(List<Date> values) {
            addCriterion("release_time_end not in", values, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndBetween(Date value1, Date value2) {
            addCriterion("release_time_end between", value1, value2, "releaseTimeEnd");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEndNotBetween(Date value1, Date value2) {
            addCriterion("release_time_end not between", value1, value2, "releaseTimeEnd");
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

        public Criteria andTaskExecutionConditionsIsNull() {
            addCriterion("task_execution_conditions is null");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsIsNotNull() {
            addCriterion("task_execution_conditions is not null");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsEqualTo(String value) {
            addCriterion("task_execution_conditions =", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsNotEqualTo(String value) {
            addCriterion("task_execution_conditions <>", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsGreaterThan(String value) {
            addCriterion("task_execution_conditions >", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsGreaterThanOrEqualTo(String value) {
            addCriterion("task_execution_conditions >=", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsLessThan(String value) {
            addCriterion("task_execution_conditions <", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsLessThanOrEqualTo(String value) {
            addCriterion("task_execution_conditions <=", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsLike(String value) {
            addCriterion("task_execution_conditions like", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsNotLike(String value) {
            addCriterion("task_execution_conditions not like", value, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsIn(List<String> values) {
            addCriterion("task_execution_conditions in", values, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsNotIn(List<String> values) {
            addCriterion("task_execution_conditions not in", values, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsBetween(String value1, String value2) {
            addCriterion("task_execution_conditions between", value1, value2, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionConditionsNotBetween(String value1, String value2) {
            addCriterion("task_execution_conditions not between", value1, value2, "taskExecutionConditions");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlIsNull() {
            addCriterion("task_execution_sql is null");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlIsNotNull() {
            addCriterion("task_execution_sql is not null");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlEqualTo(String value) {
            addCriterion("task_execution_sql =", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlNotEqualTo(String value) {
            addCriterion("task_execution_sql <>", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlGreaterThan(String value) {
            addCriterion("task_execution_sql >", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlGreaterThanOrEqualTo(String value) {
            addCriterion("task_execution_sql >=", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlLessThan(String value) {
            addCriterion("task_execution_sql <", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlLessThanOrEqualTo(String value) {
            addCriterion("task_execution_sql <=", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlLike(String value) {
            addCriterion("task_execution_sql like", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlNotLike(String value) {
            addCriterion("task_execution_sql not like", value, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlIn(List<String> values) {
            addCriterion("task_execution_sql in", values, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlNotIn(List<String> values) {
            addCriterion("task_execution_sql not in", values, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlBetween(String value1, String value2) {
            addCriterion("task_execution_sql between", value1, value2, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andTaskExecutionSqlNotBetween(String value1, String value2) {
            addCriterion("task_execution_sql not between", value1, value2, "taskExecutionSql");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIsNull() {
            addCriterion("error_message is null");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIsNotNull() {
            addCriterion("error_message is not null");
            return (Criteria) this;
        }

        public Criteria andErrorMessageEqualTo(String value) {
            addCriterion("error_message =", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotEqualTo(String value) {
            addCriterion("error_message <>", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageGreaterThan(String value) {
            addCriterion("error_message >", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageGreaterThanOrEqualTo(String value) {
            addCriterion("error_message >=", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLessThan(String value) {
            addCriterion("error_message <", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLessThanOrEqualTo(String value) {
            addCriterion("error_message <=", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLike(String value) {
            addCriterion("error_message like", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotLike(String value) {
            addCriterion("error_message not like", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIn(List<String> values) {
            addCriterion("error_message in", values, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotIn(List<String> values) {
            addCriterion("error_message not in", values, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageBetween(String value1, String value2) {
            addCriterion("error_message between", value1, value2, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotBetween(String value1, String value2) {
            addCriterion("error_message not between", value1, value2, "errorMessage");
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

        public Criteria andIsDeleteIsNull() {
            addCriterion("is_delete is null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIsNotNull() {
            addCriterion("is_delete is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteEqualTo(Integer value) {
            addCriterion("is_delete =", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotEqualTo(Integer value) {
            addCriterion("is_delete <>", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThan(Integer value) {
            addCriterion("is_delete >", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_delete >=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThan(Integer value) {
            addCriterion("is_delete <", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThanOrEqualTo(Integer value) {
            addCriterion("is_delete <=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIn(List<Integer> values) {
            addCriterion("is_delete in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotIn(List<Integer> values) {
            addCriterion("is_delete not in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteBetween(Integer value1, Integer value2) {
            addCriterion("is_delete between", value1, value2, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotBetween(Integer value1, Integer value2) {
            addCriterion("is_delete not between", value1, value2, "isDelete");
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