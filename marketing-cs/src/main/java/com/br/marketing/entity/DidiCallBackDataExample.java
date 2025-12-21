package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DidiCallBackDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DidiCallBackDataExample() {
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

        public Criteria andCallbackTypeIsNull() {
            addCriterion("callback_type is null");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeIsNotNull() {
            addCriterion("callback_type is not null");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeEqualTo(Integer value) {
            addCriterion("callback_type =", value, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeNotEqualTo(Integer value) {
            addCriterion("callback_type <>", value, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeGreaterThan(Integer value) {
            addCriterion("callback_type >", value, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("callback_type >=", value, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeLessThan(Integer value) {
            addCriterion("callback_type <", value, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeLessThanOrEqualTo(Integer value) {
            addCriterion("callback_type <=", value, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeIn(List<Integer> values) {
            addCriterion("callback_type in", values, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeNotIn(List<Integer> values) {
            addCriterion("callback_type not in", values, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeBetween(Integer value1, Integer value2) {
            addCriterion("callback_type between", value1, value2, "callbackType");
            return (Criteria) this;
        }

        public Criteria andCallbackTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("callback_type not between", value1, value2, "callbackType");
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

        public Criteria andTaskIdEqualTo(String value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(String value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(String value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(String value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(String value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLike(String value) {
            addCriterion("task_id like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotLike(String value) {
            addCriterion("task_id not like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<String> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<String> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(String value1, String value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(String value1, String value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andCellIsNull() {
            addCriterion("cell is null");
            return (Criteria) this;
        }

        public Criteria andCellIsNotNull() {
            addCriterion("cell is not null");
            return (Criteria) this;
        }

        public Criteria andCellEqualTo(String value) {
            addCriterion("cell =", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotEqualTo(String value) {
            addCriterion("cell <>", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellGreaterThan(String value) {
            addCriterion("cell >", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellGreaterThanOrEqualTo(String value) {
            addCriterion("cell >=", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLessThan(String value) {
            addCriterion("cell <", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLessThanOrEqualTo(String value) {
            addCriterion("cell <=", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLike(String value) {
            addCriterion("cell like", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotLike(String value) {
            addCriterion("cell not like", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellIn(List<String> values) {
            addCriterion("cell in", values, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotIn(List<String> values) {
            addCriterion("cell not in", values, "cell");
            return (Criteria) this;
        }

        public Criteria andCellBetween(String value1, String value2) {
            addCriterion("cell between", value1, value2, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotBetween(String value1, String value2) {
            addCriterion("cell not between", value1, value2, "cell");
            return (Criteria) this;
        }

        public Criteria andCaseStatusIsNull() {
            addCriterion("case_status is null");
            return (Criteria) this;
        }

        public Criteria andCaseStatusIsNotNull() {
            addCriterion("case_status is not null");
            return (Criteria) this;
        }

        public Criteria andCaseStatusEqualTo(String value) {
            addCriterion("case_status =", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusNotEqualTo(String value) {
            addCriterion("case_status <>", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusGreaterThan(String value) {
            addCriterion("case_status >", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusGreaterThanOrEqualTo(String value) {
            addCriterion("case_status >=", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusLessThan(String value) {
            addCriterion("case_status <", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusLessThanOrEqualTo(String value) {
            addCriterion("case_status <=", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusLike(String value) {
            addCriterion("case_status like", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusNotLike(String value) {
            addCriterion("case_status not like", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusIn(List<String> values) {
            addCriterion("case_status in", values, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusNotIn(List<String> values) {
            addCriterion("case_status not in", values, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusBetween(String value1, String value2) {
            addCriterion("case_status between", value1, value2, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusNotBetween(String value1, String value2) {
            addCriterion("case_status not between", value1, value2, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andDialCountIsNull() {
            addCriterion("dial_count is null");
            return (Criteria) this;
        }

        public Criteria andDialCountIsNotNull() {
            addCriterion("dial_count is not null");
            return (Criteria) this;
        }

        public Criteria andDialCountEqualTo(Integer value) {
            addCriterion("dial_count =", value, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountNotEqualTo(Integer value) {
            addCriterion("dial_count <>", value, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountGreaterThan(Integer value) {
            addCriterion("dial_count >", value, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("dial_count >=", value, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountLessThan(Integer value) {
            addCriterion("dial_count <", value, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountLessThanOrEqualTo(Integer value) {
            addCriterion("dial_count <=", value, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountIn(List<Integer> values) {
            addCriterion("dial_count in", values, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountNotIn(List<Integer> values) {
            addCriterion("dial_count not in", values, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountBetween(Integer value1, Integer value2) {
            addCriterion("dial_count between", value1, value2, "dialCount");
            return (Criteria) this;
        }

        public Criteria andDialCountNotBetween(Integer value1, Integer value2) {
            addCriterion("dial_count not between", value1, value2, "dialCount");
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

        public Criteria andDetailEqualTo(String value) {
            addCriterion("detail =", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailNotEqualTo(String value) {
            addCriterion("detail <>", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailGreaterThan(String value) {
            addCriterion("detail >", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailGreaterThanOrEqualTo(String value) {
            addCriterion("detail >=", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailLessThan(String value) {
            addCriterion("detail <", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailLessThanOrEqualTo(String value) {
            addCriterion("detail <=", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailLike(String value) {
            addCriterion("detail like", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailNotLike(String value) {
            addCriterion("detail not like", value, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailIn(List<String> values) {
            addCriterion("detail in", values, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailNotIn(List<String> values) {
            addCriterion("detail not in", values, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailBetween(String value1, String value2) {
            addCriterion("detail between", value1, value2, "detail");
            return (Criteria) this;
        }

        public Criteria andDetailNotBetween(String value1, String value2) {
            addCriterion("detail not between", value1, value2, "detail");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdIsNull() {
            addCriterion("call_record_id is null");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdIsNotNull() {
            addCriterion("call_record_id is not null");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdEqualTo(String value) {
            addCriterion("call_record_id =", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdNotEqualTo(String value) {
            addCriterion("call_record_id <>", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdGreaterThan(String value) {
            addCriterion("call_record_id >", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdGreaterThanOrEqualTo(String value) {
            addCriterion("call_record_id >=", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdLessThan(String value) {
            addCriterion("call_record_id <", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdLessThanOrEqualTo(String value) {
            addCriterion("call_record_id <=", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdLike(String value) {
            addCriterion("call_record_id like", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdNotLike(String value) {
            addCriterion("call_record_id not like", value, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdIn(List<String> values) {
            addCriterion("call_record_id in", values, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdNotIn(List<String> values) {
            addCriterion("call_record_id not in", values, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdBetween(String value1, String value2) {
            addCriterion("call_record_id between", value1, value2, "callRecordId");
            return (Criteria) this;
        }

        public Criteria andCallRecordIdNotBetween(String value1, String value2) {
            addCriterion("call_record_id not between", value1, value2, "callRecordId");
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

        public Criteria andCallStartTimeEqualTo(Date value) {
            addCriterion("call_start_time =", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotEqualTo(Date value) {
            addCriterion("call_start_time <>", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeGreaterThan(Date value) {
            addCriterion("call_start_time >", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("call_start_time >=", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeLessThan(Date value) {
            addCriterion("call_start_time <", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("call_start_time <=", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeIn(List<Date> values) {
            addCriterion("call_start_time in", values, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotIn(List<Date> values) {
            addCriterion("call_start_time not in", values, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeBetween(Date value1, Date value2) {
            addCriterion("call_start_time between", value1, value2, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotBetween(Date value1, Date value2) {
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

        public Criteria andCallConnectTimeEqualTo(Date value) {
            addCriterion("call_connect_time =", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotEqualTo(Date value) {
            addCriterion("call_connect_time <>", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeGreaterThan(Date value) {
            addCriterion("call_connect_time >", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("call_connect_time >=", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeLessThan(Date value) {
            addCriterion("call_connect_time <", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeLessThanOrEqualTo(Date value) {
            addCriterion("call_connect_time <=", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeIn(List<Date> values) {
            addCriterion("call_connect_time in", values, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotIn(List<Date> values) {
            addCriterion("call_connect_time not in", values, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeBetween(Date value1, Date value2) {
            addCriterion("call_connect_time between", value1, value2, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotBetween(Date value1, Date value2) {
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

        public Criteria andCallEndTimeEqualTo(Date value) {
            addCriterion("call_end_time =", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotEqualTo(Date value) {
            addCriterion("call_end_time <>", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeGreaterThan(Date value) {
            addCriterion("call_end_time >", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("call_end_time >=", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeLessThan(Date value) {
            addCriterion("call_end_time <", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("call_end_time <=", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeIn(List<Date> values) {
            addCriterion("call_end_time in", values, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotIn(List<Date> values) {
            addCriterion("call_end_time not in", values, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeBetween(Date value1, Date value2) {
            addCriterion("call_end_time between", value1, value2, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotBetween(Date value1, Date value2) {
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

        public Criteria andCallStatusEqualTo(String value) {
            addCriterion("call_status =", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusNotEqualTo(String value) {
            addCriterion("call_status <>", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusGreaterThan(String value) {
            addCriterion("call_status >", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusGreaterThanOrEqualTo(String value) {
            addCriterion("call_status >=", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusLessThan(String value) {
            addCriterion("call_status <", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusLessThanOrEqualTo(String value) {
            addCriterion("call_status <=", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusLike(String value) {
            addCriterion("call_status like", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusNotLike(String value) {
            addCriterion("call_status not like", value, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusIn(List<String> values) {
            addCriterion("call_status in", values, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusNotIn(List<String> values) {
            addCriterion("call_status not in", values, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusBetween(String value1, String value2) {
            addCriterion("call_status between", value1, value2, "callStatus");
            return (Criteria) this;
        }

        public Criteria andCallStatusNotBetween(String value1, String value2) {
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

        public Criteria andSmsSendStatusIsNull() {
            addCriterion("sms_send_status is null");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusIsNotNull() {
            addCriterion("sms_send_status is not null");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusEqualTo(Integer value) {
            addCriterion("sms_send_status =", value, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusNotEqualTo(Integer value) {
            addCriterion("sms_send_status <>", value, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusGreaterThan(Integer value) {
            addCriterion("sms_send_status >", value, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("sms_send_status >=", value, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusLessThan(Integer value) {
            addCriterion("sms_send_status <", value, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusLessThanOrEqualTo(Integer value) {
            addCriterion("sms_send_status <=", value, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusIn(List<Integer> values) {
            addCriterion("sms_send_status in", values, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusNotIn(List<Integer> values) {
            addCriterion("sms_send_status not in", values, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusBetween(Integer value1, Integer value2) {
            addCriterion("sms_send_status between", value1, value2, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andSmsSendStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("sms_send_status not between", value1, value2, "smsSendStatus");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesIsNull() {
            addCriterion("user_properties is null");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesIsNotNull() {
            addCriterion("user_properties is not null");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesEqualTo(String value) {
            addCriterion("user_properties =", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesNotEqualTo(String value) {
            addCriterion("user_properties <>", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesGreaterThan(String value) {
            addCriterion("user_properties >", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesGreaterThanOrEqualTo(String value) {
            addCriterion("user_properties >=", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesLessThan(String value) {
            addCriterion("user_properties <", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesLessThanOrEqualTo(String value) {
            addCriterion("user_properties <=", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesLike(String value) {
            addCriterion("user_properties like", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesNotLike(String value) {
            addCriterion("user_properties not like", value, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesIn(List<String> values) {
            addCriterion("user_properties in", values, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesNotIn(List<String> values) {
            addCriterion("user_properties not in", values, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesBetween(String value1, String value2) {
            addCriterion("user_properties between", value1, value2, "userProperties");
            return (Criteria) this;
        }

        public Criteria andUserPropertiesNotBetween(String value1, String value2) {
            addCriterion("user_properties not between", value1, value2, "userProperties");
            return (Criteria) this;
        }

        public Criteria andDialRoundsIsNull() {
            addCriterion("dial_rounds is null");
            return (Criteria) this;
        }

        public Criteria andDialRoundsIsNotNull() {
            addCriterion("dial_rounds is not null");
            return (Criteria) this;
        }

        public Criteria andDialRoundsEqualTo(Integer value) {
            addCriterion("dial_rounds =", value, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsNotEqualTo(Integer value) {
            addCriterion("dial_rounds <>", value, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsGreaterThan(Integer value) {
            addCriterion("dial_rounds >", value, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsGreaterThanOrEqualTo(Integer value) {
            addCriterion("dial_rounds >=", value, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsLessThan(Integer value) {
            addCriterion("dial_rounds <", value, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsLessThanOrEqualTo(Integer value) {
            addCriterion("dial_rounds <=", value, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsIn(List<Integer> values) {
            addCriterion("dial_rounds in", values, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsNotIn(List<Integer> values) {
            addCriterion("dial_rounds not in", values, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsBetween(Integer value1, Integer value2) {
            addCriterion("dial_rounds between", value1, value2, "dialRounds");
            return (Criteria) this;
        }

        public Criteria andDialRoundsNotBetween(Integer value1, Integer value2) {
            addCriterion("dial_rounds not between", value1, value2, "dialRounds");
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

        public Criteria andPushStatusIsNull() {
            addCriterion("push_status is null");
            return (Criteria) this;
        }

        public Criteria andPushStatusIsNotNull() {
            addCriterion("push_status is not null");
            return (Criteria) this;
        }

        public Criteria andPushStatusEqualTo(Integer value) {
            addCriterion("push_status =", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotEqualTo(Integer value) {
            addCriterion("push_status <>", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThan(Integer value) {
            addCriterion("push_status >", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_status >=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThan(Integer value) {
            addCriterion("push_status <", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThanOrEqualTo(Integer value) {
            addCriterion("push_status <=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusIn(List<Integer> values) {
            addCriterion("push_status in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotIn(List<Integer> values) {
            addCriterion("push_status not in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusBetween(Integer value1, Integer value2) {
            addCriterion("push_status between", value1, value2, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("push_status not between", value1, value2, "pushStatus");
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

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andExtendIsNull() {
            addCriterion("extend is null");
            return (Criteria) this;
        }

        public Criteria andExtendIsNotNull() {
            addCriterion("extend is not null");
            return (Criteria) this;
        }

        public Criteria andExtendEqualTo(String value) {
            addCriterion("extend =", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotEqualTo(String value) {
            addCriterion("extend <>", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThan(String value) {
            addCriterion("extend >", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThanOrEqualTo(String value) {
            addCriterion("extend >=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThan(String value) {
            addCriterion("extend <", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThanOrEqualTo(String value) {
            addCriterion("extend <=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLike(String value) {
            addCriterion("extend like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotLike(String value) {
            addCriterion("extend not like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendIn(List<String> values) {
            addCriterion("extend in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotIn(List<String> values) {
            addCriterion("extend not in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendBetween(String value1, String value2) {
            addCriterion("extend between", value1, value2, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotBetween(String value1, String value2) {
            addCriterion("extend not between", value1, value2, "extend");
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