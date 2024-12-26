package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataGroupTaskDetailExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DataGroupTaskDetailExample() {
        oredCriteria = new ArrayList<>();
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
            criteria = new ArrayList<>();
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

        public Criteria andGroupTaskIdIsNull() {
            addCriterion("group_task_id is null");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdIsNotNull() {
            addCriterion("group_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdEqualTo(Long value) {
            addCriterion("group_task_id =", value, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdNotEqualTo(Long value) {
            addCriterion("group_task_id <>", value, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdGreaterThan(Long value) {
            addCriterion("group_task_id >", value, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdGreaterThanOrEqualTo(Long value) {
            addCriterion("group_task_id >=", value, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdLessThan(Long value) {
            addCriterion("group_task_id <", value, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdLessThanOrEqualTo(Long value) {
            addCriterion("group_task_id <=", value, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdIn(List<Long> values) {
            addCriterion("group_task_id in", values, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdNotIn(List<Long> values) {
            addCriterion("group_task_id not in", values, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdBetween(Long value1, Long value2) {
            addCriterion("group_task_id between", value1, value2, "groupTaskId");
            return (Criteria) this;
        }

        public Criteria andGroupTaskIdNotBetween(Long value1, Long value2) {
            addCriterion("group_task_id not between", value1, value2, "groupTaskId");
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

        public Criteria andExtendFieldIsNull() {
            addCriterion("extend_field is null");
            return (Criteria) this;
        }

        public Criteria andExtendFieldIsNotNull() {
            addCriterion("extend_field is not null");
            return (Criteria) this;
        }

        public Criteria andExtendFieldEqualTo(String value) {
            addCriterion("extend_field =", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldNotEqualTo(String value) {
            addCriterion("extend_field <>", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldGreaterThan(String value) {
            addCriterion("extend_field >", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldGreaterThanOrEqualTo(String value) {
            addCriterion("extend_field >=", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldLessThan(String value) {
            addCriterion("extend_field <", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldLessThanOrEqualTo(String value) {
            addCriterion("extend_field <=", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldLike(String value) {
            addCriterion("extend_field like", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldNotLike(String value) {
            addCriterion("extend_field not like", value, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldIn(List<String> values) {
            addCriterion("extend_field in", values, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldNotIn(List<String> values) {
            addCriterion("extend_field not in", values, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldBetween(String value1, String value2) {
            addCriterion("extend_field between", value1, value2, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldNotBetween(String value1, String value2) {
            addCriterion("extend_field not between", value1, value2, "extendField");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueIsNull() {
            addCriterion("extend_field_value is null");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueIsNotNull() {
            addCriterion("extend_field_value is not null");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueEqualTo(String value) {
            addCriterion("extend_field_value =", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueNotEqualTo(String value) {
            addCriterion("extend_field_value <>", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueGreaterThan(String value) {
            addCriterion("extend_field_value >", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueGreaterThanOrEqualTo(String value) {
            addCriterion("extend_field_value >=", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueLessThan(String value) {
            addCriterion("extend_field_value <", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueLessThanOrEqualTo(String value) {
            addCriterion("extend_field_value <=", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueLike(String value) {
            addCriterion("extend_field_value like", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueNotLike(String value) {
            addCriterion("extend_field_value not like", value, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueIn(List<String> values) {
            addCriterion("extend_field_value in", values, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueNotIn(List<String> values) {
            addCriterion("extend_field_value not in", values, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueBetween(String value1, String value2) {
            addCriterion("extend_field_value between", value1, value2, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andExtendFieldValueNotBetween(String value1, String value2) {
            addCriterion("extend_field_value not between", value1, value2, "extendFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldIsNull() {
            addCriterion("group_field is null");
            return (Criteria) this;
        }

        public Criteria andGroupFieldIsNotNull() {
            addCriterion("group_field is not null");
            return (Criteria) this;
        }

        public Criteria andGroupFieldEqualTo(String value) {
            addCriterion("group_field =", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldNotEqualTo(String value) {
            addCriterion("group_field <>", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldGreaterThan(String value) {
            addCriterion("group_field >", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldGreaterThanOrEqualTo(String value) {
            addCriterion("group_field >=", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldLessThan(String value) {
            addCriterion("group_field <", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldLessThanOrEqualTo(String value) {
            addCriterion("group_field <=", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldLike(String value) {
            addCriterion("group_field like", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldNotLike(String value) {
            addCriterion("group_field not like", value, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldIn(List<String> values) {
            addCriterion("group_field in", values, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldNotIn(List<String> values) {
            addCriterion("group_field not in", values, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldBetween(String value1, String value2) {
            addCriterion("group_field between", value1, value2, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldNotBetween(String value1, String value2) {
            addCriterion("group_field not between", value1, value2, "groupField");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueIsNull() {
            addCriterion("group_field_value is null");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueIsNotNull() {
            addCriterion("group_field_value is not null");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueEqualTo(String value) {
            addCriterion("group_field_value =", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueNotEqualTo(String value) {
            addCriterion("group_field_value <>", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueGreaterThan(String value) {
            addCriterion("group_field_value >", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueGreaterThanOrEqualTo(String value) {
            addCriterion("group_field_value >=", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueLessThan(String value) {
            addCriterion("group_field_value <", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueLessThanOrEqualTo(String value) {
            addCriterion("group_field_value <=", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueLike(String value) {
            addCriterion("group_field_value like", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueNotLike(String value) {
            addCriterion("group_field_value not like", value, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueIn(List<String> values) {
            addCriterion("group_field_value in", values, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueNotIn(List<String> values) {
            addCriterion("group_field_value not in", values, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueBetween(String value1, String value2) {
            addCriterion("group_field_value between", value1, value2, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupFieldValueNotBetween(String value1, String value2) {
            addCriterion("group_field_value not between", value1, value2, "groupFieldValue");
            return (Criteria) this;
        }

        public Criteria andGroupNumIsNull() {
            addCriterion("group_num is null");
            return (Criteria) this;
        }

        public Criteria andGroupNumIsNotNull() {
            addCriterion("group_num is not null");
            return (Criteria) this;
        }

        public Criteria andGroupNumEqualTo(Integer value) {
            addCriterion("group_num =", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumNotEqualTo(Integer value) {
            addCriterion("group_num <>", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumGreaterThan(Integer value) {
            addCriterion("group_num >", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("group_num >=", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumLessThan(Integer value) {
            addCriterion("group_num <", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumLessThanOrEqualTo(Integer value) {
            addCriterion("group_num <=", value, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumIn(List<Integer> values) {
            addCriterion("group_num in", values, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumNotIn(List<Integer> values) {
            addCriterion("group_num not in", values, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumBetween(Integer value1, Integer value2) {
            addCriterion("group_num between", value1, value2, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupNumNotBetween(Integer value1, Integer value2) {
            addCriterion("group_num not between", value1, value2, "groupNum");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdIsNull() {
            addCriterion("group_min_id is null");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdIsNotNull() {
            addCriterion("group_min_id is not null");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdEqualTo(Long value) {
            addCriterion("group_min_id =", value, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdNotEqualTo(Long value) {
            addCriterion("group_min_id <>", value, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdGreaterThan(Long value) {
            addCriterion("group_min_id >", value, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdGreaterThanOrEqualTo(Long value) {
            addCriterion("group_min_id >=", value, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdLessThan(Long value) {
            addCriterion("group_min_id <", value, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdLessThanOrEqualTo(Long value) {
            addCriterion("group_min_id <=", value, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdIn(List<Long> values) {
            addCriterion("group_min_id in", values, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdNotIn(List<Long> values) {
            addCriterion("group_min_id not in", values, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdBetween(Long value1, Long value2) {
            addCriterion("group_min_id between", value1, value2, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMinIdNotBetween(Long value1, Long value2) {
            addCriterion("group_min_id not between", value1, value2, "groupMinId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdIsNull() {
            addCriterion("group_max_id is null");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdIsNotNull() {
            addCriterion("group_max_id is not null");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdEqualTo(Long value) {
            addCriterion("group_max_id =", value, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdNotEqualTo(Long value) {
            addCriterion("group_max_id <>", value, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdGreaterThan(Long value) {
            addCriterion("group_max_id >", value, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdGreaterThanOrEqualTo(Long value) {
            addCriterion("group_max_id >=", value, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdLessThan(Long value) {
            addCriterion("group_max_id <", value, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdLessThanOrEqualTo(Long value) {
            addCriterion("group_max_id <=", value, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdIn(List<Long> values) {
            addCriterion("group_max_id in", values, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdNotIn(List<Long> values) {
            addCriterion("group_max_id not in", values, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdBetween(Long value1, Long value2) {
            addCriterion("group_max_id between", value1, value2, "groupMaxId");
            return (Criteria) this;
        }

        public Criteria andGroupMaxIdNotBetween(Long value1, Long value2) {
            addCriterion("group_max_id not between", value1, value2, "groupMaxId");
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

        public Criteria andUpdateConditionIsNull() {
            addCriterion("update_condition is null");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionIsNotNull() {
            addCriterion("update_condition is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionEqualTo(String value) {
            addCriterion("update_condition =", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionNotEqualTo(String value) {
            addCriterion("update_condition <>", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionGreaterThan(String value) {
            addCriterion("update_condition >", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionGreaterThanOrEqualTo(String value) {
            addCriterion("update_condition >=", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionLessThan(String value) {
            addCriterion("update_condition <", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionLessThanOrEqualTo(String value) {
            addCriterion("update_condition <=", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionLike(String value) {
            addCriterion("update_condition like", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionNotLike(String value) {
            addCriterion("update_condition not like", value, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionIn(List<String> values) {
            addCriterion("update_condition in", values, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionNotIn(List<String> values) {
            addCriterion("update_condition not in", values, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionBetween(String value1, String value2) {
            addCriterion("update_condition between", value1, value2, "updateCondition");
            return (Criteria) this;
        }

        public Criteria andUpdateConditionNotBetween(String value1, String value2) {
            addCriterion("update_condition not between", value1, value2, "updateCondition");
            return (Criteria) this;
        }
    }

    /**
     */
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