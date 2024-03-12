package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PushCustomerDetailExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PushCustomerDetailExample() {
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

        public Criteria andFileIdIsNull() {
            addCriterion("file_id is null");
            return (Criteria) this;
        }

        public Criteria andFileIdIsNotNull() {
            addCriterion("file_id is not null");
            return (Criteria) this;
        }

        public Criteria andFileIdEqualTo(Long value) {
            addCriterion("file_id =", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotEqualTo(Long value) {
            addCriterion("file_id <>", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThan(Long value) {
            addCriterion("file_id >", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThanOrEqualTo(Long value) {
            addCriterion("file_id >=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThan(Long value) {
            addCriterion("file_id <", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThanOrEqualTo(Long value) {
            addCriterion("file_id <=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdIn(List<Long> values) {
            addCriterion("file_id in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotIn(List<Long> values) {
            addCriterion("file_id not in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdBetween(Long value1, Long value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotBetween(Long value1, Long value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
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

        public Criteria andUserTypeIsNull() {
            addCriterion("user_type is null");
            return (Criteria) this;
        }

        public Criteria andUserTypeIsNotNull() {
            addCriterion("user_type is not null");
            return (Criteria) this;
        }

        public Criteria andUserTypeEqualTo(String value) {
            addCriterion("user_type =", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotEqualTo(String value) {
            addCriterion("user_type <>", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThan(String value) {
            addCriterion("user_type >", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThanOrEqualTo(String value) {
            addCriterion("user_type >=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThan(String value) {
            addCriterion("user_type <", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThanOrEqualTo(String value) {
            addCriterion("user_type <=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLike(String value) {
            addCriterion("user_type like", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotLike(String value) {
            addCriterion("user_type not like", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeIn(List<String> values) {
            addCriterion("user_type in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotIn(List<String> values) {
            addCriterion("user_type not in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeBetween(String value1, String value2) {
            addCriterion("user_type between", value1, value2, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotBetween(String value1, String value2) {
            addCriterion("user_type not between", value1, value2, "userType");
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

        public Criteria andPushJsonIsNull() {
            addCriterion("push_json is null");
            return (Criteria) this;
        }

        public Criteria andPushJsonIsNotNull() {
            addCriterion("push_json is not null");
            return (Criteria) this;
        }

        public Criteria andPushJsonEqualTo(String value) {
            addCriterion("push_json =", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonNotEqualTo(String value) {
            addCriterion("push_json <>", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonGreaterThan(String value) {
            addCriterion("push_json >", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonGreaterThanOrEqualTo(String value) {
            addCriterion("push_json >=", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonLessThan(String value) {
            addCriterion("push_json <", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonLessThanOrEqualTo(String value) {
            addCriterion("push_json <=", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonLike(String value) {
            addCriterion("push_json like", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonNotLike(String value) {
            addCriterion("push_json not like", value, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonIn(List<String> values) {
            addCriterion("push_json in", values, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonNotIn(List<String> values) {
            addCriterion("push_json not in", values, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonBetween(String value1, String value2) {
            addCriterion("push_json between", value1, value2, "pushJson");
            return (Criteria) this;
        }

        public Criteria andPushJsonNotBetween(String value1, String value2) {
            addCriterion("push_json not between", value1, value2, "pushJson");
            return (Criteria) this;
        }

        public Criteria andScoreSort1IsNull() {
            addCriterion("score_sort1 is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort1IsNotNull() {
            addCriterion("score_sort1 is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort1EqualTo(String value) {
            addCriterion("score_sort1 =", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1NotEqualTo(String value) {
            addCriterion("score_sort1 <>", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1GreaterThan(String value) {
            addCriterion("score_sort1 >", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1GreaterThanOrEqualTo(String value) {
            addCriterion("score_sort1 >=", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1LessThan(String value) {
            addCriterion("score_sort1 <", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1LessThanOrEqualTo(String value) {
            addCriterion("score_sort1 <=", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1Like(String value) {
            addCriterion("score_sort1 like", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1NotLike(String value) {
            addCriterion("score_sort1 not like", value, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1In(List<String> values) {
            addCriterion("score_sort1 in", values, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1NotIn(List<String> values) {
            addCriterion("score_sort1 not in", values, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1Between(String value1, String value2) {
            addCriterion("score_sort1 between", value1, value2, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort1NotBetween(String value1, String value2) {
            addCriterion("score_sort1 not between", value1, value2, "scoreSort1");
            return (Criteria) this;
        }

        public Criteria andScoreSort2IsNull() {
            addCriterion("score_sort2 is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort2IsNotNull() {
            addCriterion("score_sort2 is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort2EqualTo(String value) {
            addCriterion("score_sort2 =", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2NotEqualTo(String value) {
            addCriterion("score_sort2 <>", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2GreaterThan(String value) {
            addCriterion("score_sort2 >", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2GreaterThanOrEqualTo(String value) {
            addCriterion("score_sort2 >=", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2LessThan(String value) {
            addCriterion("score_sort2 <", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2LessThanOrEqualTo(String value) {
            addCriterion("score_sort2 <=", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2Like(String value) {
            addCriterion("score_sort2 like", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2NotLike(String value) {
            addCriterion("score_sort2 not like", value, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2In(List<String> values) {
            addCriterion("score_sort2 in", values, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2NotIn(List<String> values) {
            addCriterion("score_sort2 not in", values, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2Between(String value1, String value2) {
            addCriterion("score_sort2 between", value1, value2, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort2NotBetween(String value1, String value2) {
            addCriterion("score_sort2 not between", value1, value2, "scoreSort2");
            return (Criteria) this;
        }

        public Criteria andScoreSort3IsNull() {
            addCriterion("score_sort3 is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort3IsNotNull() {
            addCriterion("score_sort3 is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort3EqualTo(String value) {
            addCriterion("score_sort3 =", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3NotEqualTo(String value) {
            addCriterion("score_sort3 <>", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3GreaterThan(String value) {
            addCriterion("score_sort3 >", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3GreaterThanOrEqualTo(String value) {
            addCriterion("score_sort3 >=", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3LessThan(String value) {
            addCriterion("score_sort3 <", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3LessThanOrEqualTo(String value) {
            addCriterion("score_sort3 <=", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3Like(String value) {
            addCriterion("score_sort3 like", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3NotLike(String value) {
            addCriterion("score_sort3 not like", value, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3In(List<String> values) {
            addCriterion("score_sort3 in", values, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3NotIn(List<String> values) {
            addCriterion("score_sort3 not in", values, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3Between(String value1, String value2) {
            addCriterion("score_sort3 between", value1, value2, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort3NotBetween(String value1, String value2) {
            addCriterion("score_sort3 not between", value1, value2, "scoreSort3");
            return (Criteria) this;
        }

        public Criteria andScoreSort4IsNull() {
            addCriterion("score_sort4 is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort4IsNotNull() {
            addCriterion("score_sort4 is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort4EqualTo(String value) {
            addCriterion("score_sort4 =", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4NotEqualTo(String value) {
            addCriterion("score_sort4 <>", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4GreaterThan(String value) {
            addCriterion("score_sort4 >", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4GreaterThanOrEqualTo(String value) {
            addCriterion("score_sort4 >=", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4LessThan(String value) {
            addCriterion("score_sort4 <", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4LessThanOrEqualTo(String value) {
            addCriterion("score_sort4 <=", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4Like(String value) {
            addCriterion("score_sort4 like", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4NotLike(String value) {
            addCriterion("score_sort4 not like", value, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4In(List<String> values) {
            addCriterion("score_sort4 in", values, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4NotIn(List<String> values) {
            addCriterion("score_sort4 not in", values, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4Between(String value1, String value2) {
            addCriterion("score_sort4 between", value1, value2, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andScoreSort4NotBetween(String value1, String value2) {
            addCriterion("score_sort4 not between", value1, value2, "scoreSort4");
            return (Criteria) this;
        }

        public Criteria andIsDelIsNull() {
            addCriterion("is_del is null");
            return (Criteria) this;
        }

        public Criteria andIsDelIsNotNull() {
            addCriterion("is_del is not null");
            return (Criteria) this;
        }

        public Criteria andIsDelEqualTo(Integer value) {
            addCriterion("is_del =", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotEqualTo(Integer value) {
            addCriterion("is_del <>", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelGreaterThan(Integer value) {
            addCriterion("is_del >", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_del >=", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelLessThan(Integer value) {
            addCriterion("is_del <", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelLessThanOrEqualTo(Integer value) {
            addCriterion("is_del <=", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelIn(List<Integer> values) {
            addCriterion("is_del in", values, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotIn(List<Integer> values) {
            addCriterion("is_del not in", values, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelBetween(Integer value1, Integer value2) {
            addCriterion("is_del between", value1, value2, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotBetween(Integer value1, Integer value2) {
            addCriterion("is_del not between", value1, value2, "isDel");
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