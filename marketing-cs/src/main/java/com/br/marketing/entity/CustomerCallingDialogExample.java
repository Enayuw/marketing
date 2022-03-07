package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerCallingDialogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CustomerCallingDialogExample() {
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

        public Criteria andUserTypeIsNull() {
            addCriterion("user_type is null");
            return (Criteria) this;
        }

        public Criteria andUserTypeIsNotNull() {
            addCriterion("user_type is not null");
            return (Criteria) this;
        }

        public Criteria andUserTypeEqualTo(Integer value) {
            addCriterion("user_type =", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotEqualTo(Integer value) {
            addCriterion("user_type <>", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThan(Integer value) {
            addCriterion("user_type >", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("user_type >=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThan(Integer value) {
            addCriterion("user_type <", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThanOrEqualTo(Integer value) {
            addCriterion("user_type <=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeIn(List<Integer> values) {
            addCriterion("user_type in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotIn(List<Integer> values) {
            addCriterion("user_type not in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeBetween(Integer value1, Integer value2) {
            addCriterion("user_type between", value1, value2, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("user_type not between", value1, value2, "userType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIsNull() {
            addCriterion("group_type is null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIsNotNull() {
            addCriterion("group_type is not null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeEqualTo(Integer value) {
            addCriterion("group_type =", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotEqualTo(Integer value) {
            addCriterion("group_type <>", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThan(Integer value) {
            addCriterion("group_type >", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("group_type >=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThan(Integer value) {
            addCriterion("group_type <", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThanOrEqualTo(Integer value) {
            addCriterion("group_type <=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIn(List<Integer> values) {
            addCriterion("group_type in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotIn(List<Integer> values) {
            addCriterion("group_type not in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeBetween(Integer value1, Integer value2) {
            addCriterion("group_type between", value1, value2, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("group_type not between", value1, value2, "groupType");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberIsNull() {
            addCriterion("swift_number is null");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberIsNotNull() {
            addCriterion("swift_number is not null");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberEqualTo(String value) {
            addCriterion("swift_number =", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberNotEqualTo(String value) {
            addCriterion("swift_number <>", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberGreaterThan(String value) {
            addCriterion("swift_number >", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberGreaterThanOrEqualTo(String value) {
            addCriterion("swift_number >=", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberLessThan(String value) {
            addCriterion("swift_number <", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberLessThanOrEqualTo(String value) {
            addCriterion("swift_number <=", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberLike(String value) {
            addCriterion("swift_number like", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberNotLike(String value) {
            addCriterion("swift_number not like", value, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberIn(List<String> values) {
            addCriterion("swift_number in", values, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberNotIn(List<String> values) {
            addCriterion("swift_number not in", values, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberBetween(String value1, String value2) {
            addCriterion("swift_number between", value1, value2, "swiftNumber");
            return (Criteria) this;
        }

        public Criteria andSwiftNumberNotBetween(String value1, String value2) {
            addCriterion("swift_number not between", value1, value2, "swiftNumber");
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

        public Criteria andCaseNumIsNull() {
            addCriterion("case_num is null");
            return (Criteria) this;
        }

        public Criteria andCaseNumIsNotNull() {
            addCriterion("case_num is not null");
            return (Criteria) this;
        }

        public Criteria andCaseNumEqualTo(String value) {
            addCriterion("case_num =", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotEqualTo(String value) {
            addCriterion("case_num <>", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumGreaterThan(String value) {
            addCriterion("case_num >", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumGreaterThanOrEqualTo(String value) {
            addCriterion("case_num >=", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumLessThan(String value) {
            addCriterion("case_num <", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumLessThanOrEqualTo(String value) {
            addCriterion("case_num <=", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumLike(String value) {
            addCriterion("case_num like", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotLike(String value) {
            addCriterion("case_num not like", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumIn(List<String> values) {
            addCriterion("case_num in", values, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotIn(List<String> values) {
            addCriterion("case_num not in", values, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumBetween(String value1, String value2) {
            addCriterion("case_num between", value1, value2, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotBetween(String value1, String value2) {
            addCriterion("case_num not between", value1, value2, "caseNum");
            return (Criteria) this;
        }

        public Criteria andDialogIdIsNull() {
            addCriterion("dialog_id is null");
            return (Criteria) this;
        }

        public Criteria andDialogIdIsNotNull() {
            addCriterion("dialog_id is not null");
            return (Criteria) this;
        }

        public Criteria andDialogIdEqualTo(String value) {
            addCriterion("dialog_id =", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdNotEqualTo(String value) {
            addCriterion("dialog_id <>", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdGreaterThan(String value) {
            addCriterion("dialog_id >", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdGreaterThanOrEqualTo(String value) {
            addCriterion("dialog_id >=", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdLessThan(String value) {
            addCriterion("dialog_id <", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdLessThanOrEqualTo(String value) {
            addCriterion("dialog_id <=", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdLike(String value) {
            addCriterion("dialog_id like", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdNotLike(String value) {
            addCriterion("dialog_id not like", value, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdIn(List<String> values) {
            addCriterion("dialog_id in", values, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdNotIn(List<String> values) {
            addCriterion("dialog_id not in", values, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdBetween(String value1, String value2) {
            addCriterion("dialog_id between", value1, value2, "dialogId");
            return (Criteria) this;
        }

        public Criteria andDialogIdNotBetween(String value1, String value2) {
            addCriterion("dialog_id not between", value1, value2, "dialogId");
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

        public Criteria andCaseStatusEqualTo(Integer value) {
            addCriterion("case_status =", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusNotEqualTo(Integer value) {
            addCriterion("case_status <>", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusGreaterThan(Integer value) {
            addCriterion("case_status >", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("case_status >=", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusLessThan(Integer value) {
            addCriterion("case_status <", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusLessThanOrEqualTo(Integer value) {
            addCriterion("case_status <=", value, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusIn(List<Integer> values) {
            addCriterion("case_status in", values, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusNotIn(List<Integer> values) {
            addCriterion("case_status not in", values, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusBetween(Integer value1, Integer value2) {
            addCriterion("case_status between", value1, value2, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andCaseStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("case_status not between", value1, value2, "caseStatus");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgIsNull() {
            addCriterion("is_send_msg is null");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgIsNotNull() {
            addCriterion("is_send_msg is not null");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgEqualTo(Integer value) {
            addCriterion("is_send_msg =", value, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgNotEqualTo(Integer value) {
            addCriterion("is_send_msg <>", value, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgGreaterThan(Integer value) {
            addCriterion("is_send_msg >", value, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_send_msg >=", value, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgLessThan(Integer value) {
            addCriterion("is_send_msg <", value, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgLessThanOrEqualTo(Integer value) {
            addCriterion("is_send_msg <=", value, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgIn(List<Integer> values) {
            addCriterion("is_send_msg in", values, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgNotIn(List<Integer> values) {
            addCriterion("is_send_msg not in", values, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgBetween(Integer value1, Integer value2) {
            addCriterion("is_send_msg between", value1, value2, "isSendMsg");
            return (Criteria) this;
        }

        public Criteria andIsSendMsgNotBetween(Integer value1, Integer value2) {
            addCriterion("is_send_msg not between", value1, value2, "isSendMsg");
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

        public Criteria andSendStatusIsNull() {
            addCriterion("send_status is null");
            return (Criteria) this;
        }

        public Criteria andSendStatusIsNotNull() {
            addCriterion("send_status is not null");
            return (Criteria) this;
        }

        public Criteria andSendStatusEqualTo(Integer value) {
            addCriterion("send_status =", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusNotEqualTo(Integer value) {
            addCriterion("send_status <>", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusGreaterThan(Integer value) {
            addCriterion("send_status >", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("send_status >=", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusLessThan(Integer value) {
            addCriterion("send_status <", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusLessThanOrEqualTo(Integer value) {
            addCriterion("send_status <=", value, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusIn(List<Integer> values) {
            addCriterion("send_status in", values, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusNotIn(List<Integer> values) {
            addCriterion("send_status not in", values, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusBetween(Integer value1, Integer value2) {
            addCriterion("send_status between", value1, value2, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andSendStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("send_status not between", value1, value2, "sendStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusIsNull() {
            addCriterion("drop_status is null");
            return (Criteria) this;
        }

        public Criteria andDropStatusIsNotNull() {
            addCriterion("drop_status is not null");
            return (Criteria) this;
        }

        public Criteria andDropStatusEqualTo(Integer value) {
            addCriterion("drop_status =", value, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusNotEqualTo(Integer value) {
            addCriterion("drop_status <>", value, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusGreaterThan(Integer value) {
            addCriterion("drop_status >", value, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("drop_status >=", value, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusLessThan(Integer value) {
            addCriterion("drop_status <", value, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusLessThanOrEqualTo(Integer value) {
            addCriterion("drop_status <=", value, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusIn(List<Integer> values) {
            addCriterion("drop_status in", values, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusNotIn(List<Integer> values) {
            addCriterion("drop_status not in", values, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusBetween(Integer value1, Integer value2) {
            addCriterion("drop_status between", value1, value2, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andDropStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("drop_status not between", value1, value2, "dropStatus");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeIsNull() {
            addCriterion("real_call_time is null");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeIsNotNull() {
            addCriterion("real_call_time is not null");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeEqualTo(Integer value) {
            addCriterion("real_call_time =", value, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeNotEqualTo(Integer value) {
            addCriterion("real_call_time <>", value, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeGreaterThan(Integer value) {
            addCriterion("real_call_time >", value, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeGreaterThanOrEqualTo(Integer value) {
            addCriterion("real_call_time >=", value, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeLessThan(Integer value) {
            addCriterion("real_call_time <", value, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeLessThanOrEqualTo(Integer value) {
            addCriterion("real_call_time <=", value, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeIn(List<Integer> values) {
            addCriterion("real_call_time in", values, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeNotIn(List<Integer> values) {
            addCriterion("real_call_time not in", values, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeBetween(Integer value1, Integer value2) {
            addCriterion("real_call_time between", value1, value2, "realCallTime");
            return (Criteria) this;
        }

        public Criteria andRealCallTimeNotBetween(Integer value1, Integer value2) {
            addCriterion("real_call_time not between", value1, value2, "realCallTime");
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

        public Criteria andCallStartTimeEqualTo(String value) {
            addCriterion("call_start_time =", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotEqualTo(String value) {
            addCriterion("call_start_time <>", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeGreaterThan(String value) {
            addCriterion("call_start_time >", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeGreaterThanOrEqualTo(String value) {
            addCriterion("call_start_time >=", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeLessThan(String value) {
            addCriterion("call_start_time <", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeLessThanOrEqualTo(String value) {
            addCriterion("call_start_time <=", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeLike(String value) {
            addCriterion("call_start_time like", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotLike(String value) {
            addCriterion("call_start_time not like", value, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeIn(List<String> values) {
            addCriterion("call_start_time in", values, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotIn(List<String> values) {
            addCriterion("call_start_time not in", values, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeBetween(String value1, String value2) {
            addCriterion("call_start_time between", value1, value2, "callStartTime");
            return (Criteria) this;
        }

        public Criteria andCallStartTimeNotBetween(String value1, String value2) {
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

        public Criteria andCallConnectTimeEqualTo(String value) {
            addCriterion("call_connect_time =", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotEqualTo(String value) {
            addCriterion("call_connect_time <>", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeGreaterThan(String value) {
            addCriterion("call_connect_time >", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeGreaterThanOrEqualTo(String value) {
            addCriterion("call_connect_time >=", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeLessThan(String value) {
            addCriterion("call_connect_time <", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeLessThanOrEqualTo(String value) {
            addCriterion("call_connect_time <=", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeLike(String value) {
            addCriterion("call_connect_time like", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotLike(String value) {
            addCriterion("call_connect_time not like", value, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeIn(List<String> values) {
            addCriterion("call_connect_time in", values, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotIn(List<String> values) {
            addCriterion("call_connect_time not in", values, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeBetween(String value1, String value2) {
            addCriterion("call_connect_time between", value1, value2, "callConnectTime");
            return (Criteria) this;
        }

        public Criteria andCallConnectTimeNotBetween(String value1, String value2) {
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

        public Criteria andCallEndTimeEqualTo(String value) {
            addCriterion("call_end_time =", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotEqualTo(String value) {
            addCriterion("call_end_time <>", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeGreaterThan(String value) {
            addCriterion("call_end_time >", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeGreaterThanOrEqualTo(String value) {
            addCriterion("call_end_time >=", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeLessThan(String value) {
            addCriterion("call_end_time <", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeLessThanOrEqualTo(String value) {
            addCriterion("call_end_time <=", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeLike(String value) {
            addCriterion("call_end_time like", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotLike(String value) {
            addCriterion("call_end_time not like", value, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeIn(List<String> values) {
            addCriterion("call_end_time in", values, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotIn(List<String> values) {
            addCriterion("call_end_time not in", values, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeBetween(String value1, String value2) {
            addCriterion("call_end_time between", value1, value2, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andCallEndTimeNotBetween(String value1, String value2) {
            addCriterion("call_end_time not between", value1, value2, "callEndTime");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsIsNull() {
            addCriterion("dialog_rounds is null");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsIsNotNull() {
            addCriterion("dialog_rounds is not null");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsEqualTo(Integer value) {
            addCriterion("dialog_rounds =", value, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsNotEqualTo(Integer value) {
            addCriterion("dialog_rounds <>", value, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsGreaterThan(Integer value) {
            addCriterion("dialog_rounds >", value, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsGreaterThanOrEqualTo(Integer value) {
            addCriterion("dialog_rounds >=", value, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsLessThan(Integer value) {
            addCriterion("dialog_rounds <", value, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsLessThanOrEqualTo(Integer value) {
            addCriterion("dialog_rounds <=", value, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsIn(List<Integer> values) {
            addCriterion("dialog_rounds in", values, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsNotIn(List<Integer> values) {
            addCriterion("dialog_rounds not in", values, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsBetween(Integer value1, Integer value2) {
            addCriterion("dialog_rounds between", value1, value2, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogRoundsNotBetween(Integer value1, Integer value2) {
            addCriterion("dialog_rounds not between", value1, value2, "dialogRounds");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsIsNull() {
            addCriterion("dialog_turns is null");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsIsNotNull() {
            addCriterion("dialog_turns is not null");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsEqualTo(Integer value) {
            addCriterion("dialog_turns =", value, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsNotEqualTo(Integer value) {
            addCriterion("dialog_turns <>", value, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsGreaterThan(Integer value) {
            addCriterion("dialog_turns >", value, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsGreaterThanOrEqualTo(Integer value) {
            addCriterion("dialog_turns >=", value, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsLessThan(Integer value) {
            addCriterion("dialog_turns <", value, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsLessThanOrEqualTo(Integer value) {
            addCriterion("dialog_turns <=", value, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsIn(List<Integer> values) {
            addCriterion("dialog_turns in", values, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsNotIn(List<Integer> values) {
            addCriterion("dialog_turns not in", values, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsBetween(Integer value1, Integer value2) {
            addCriterion("dialog_turns between", value1, value2, "dialogTurns");
            return (Criteria) this;
        }

        public Criteria andDialogTurnsNotBetween(Integer value1, Integer value2) {
            addCriterion("dialog_turns not between", value1, value2, "dialogTurns");
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

        public Criteria andDialogStatusIsNull() {
            addCriterion("dialog_status is null");
            return (Criteria) this;
        }

        public Criteria andDialogStatusIsNotNull() {
            addCriterion("dialog_status is not null");
            return (Criteria) this;
        }

        public Criteria andDialogStatusEqualTo(Integer value) {
            addCriterion("dialog_status =", value, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusNotEqualTo(Integer value) {
            addCriterion("dialog_status <>", value, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusGreaterThan(Integer value) {
            addCriterion("dialog_status >", value, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("dialog_status >=", value, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusLessThan(Integer value) {
            addCriterion("dialog_status <", value, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusLessThanOrEqualTo(Integer value) {
            addCriterion("dialog_status <=", value, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusIn(List<Integer> values) {
            addCriterion("dialog_status in", values, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusNotIn(List<Integer> values) {
            addCriterion("dialog_status not in", values, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusBetween(Integer value1, Integer value2) {
            addCriterion("dialog_status between", value1, value2, "dialogStatus");
            return (Criteria) this;
        }

        public Criteria andDialogStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("dialog_status not between", value1, value2, "dialogStatus");
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