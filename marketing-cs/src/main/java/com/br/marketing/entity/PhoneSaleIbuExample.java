package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhoneSaleIbuExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PhoneSaleIbuExample() {
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

        public Criteria andLocalIdIsNull() {
            addCriterion("local_id is null");
            return (Criteria) this;
        }

        public Criteria andLocalIdIsNotNull() {
            addCriterion("local_id is not null");
            return (Criteria) this;
        }

        public Criteria andLocalIdEqualTo(String value) {
            addCriterion("local_id =", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotEqualTo(String value) {
            addCriterion("local_id <>", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThan(String value) {
            addCriterion("local_id >", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThanOrEqualTo(String value) {
            addCriterion("local_id >=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThan(String value) {
            addCriterion("local_id <", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThanOrEqualTo(String value) {
            addCriterion("local_id <=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLike(String value) {
            addCriterion("local_id like", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotLike(String value) {
            addCriterion("local_id not like", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdIn(List<String> values) {
            addCriterion("local_id in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotIn(List<String> values) {
            addCriterion("local_id not in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdBetween(String value1, String value2) {
            addCriterion("local_id between", value1, value2, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotBetween(String value1, String value2) {
            addCriterion("local_id not between", value1, value2, "localId");
            return (Criteria) this;
        }

        public Criteria andMStatusIsNull() {
            addCriterion("m_status is null");
            return (Criteria) this;
        }

        public Criteria andMStatusIsNotNull() {
            addCriterion("m_status is not null");
            return (Criteria) this;
        }

        public Criteria andMStatusEqualTo(Integer value) {
            addCriterion("m_status =", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusNotEqualTo(Integer value) {
            addCriterion("m_status <>", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusGreaterThan(Integer value) {
            addCriterion("m_status >", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_status >=", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusLessThan(Integer value) {
            addCriterion("m_status <", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusLessThanOrEqualTo(Integer value) {
            addCriterion("m_status <=", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusIn(List<Integer> values) {
            addCriterion("m_status in", values, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusNotIn(List<Integer> values) {
            addCriterion("m_status not in", values, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusBetween(Integer value1, Integer value2) {
            addCriterion("m_status between", value1, value2, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("m_status not between", value1, value2, "mStatus");
            return (Criteria) this;
        }

        public Criteria andDataMessageIsNull() {
            addCriterion("data_message is null");
            return (Criteria) this;
        }

        public Criteria andDataMessageIsNotNull() {
            addCriterion("data_message is not null");
            return (Criteria) this;
        }

        public Criteria andDataMessageEqualTo(String value) {
            addCriterion("data_message =", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotEqualTo(String value) {
            addCriterion("data_message <>", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageGreaterThan(String value) {
            addCriterion("data_message >", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageGreaterThanOrEqualTo(String value) {
            addCriterion("data_message >=", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageLessThan(String value) {
            addCriterion("data_message <", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageLessThanOrEqualTo(String value) {
            addCriterion("data_message <=", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageLike(String value) {
            addCriterion("data_message like", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotLike(String value) {
            addCriterion("data_message not like", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageIn(List<String> values) {
            addCriterion("data_message in", values, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotIn(List<String> values) {
            addCriterion("data_message not in", values, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageBetween(String value1, String value2) {
            addCriterion("data_message between", value1, value2, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotBetween(String value1, String value2) {
            addCriterion("data_message not between", value1, value2, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andUidIsNull() {
            addCriterion("`uid` is null");
            return (Criteria) this;
        }

        public Criteria andUidIsNotNull() {
            addCriterion("`uid` is not null");
            return (Criteria) this;
        }

        public Criteria andUidEqualTo(String value) {
            addCriterion("`uid` =", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotEqualTo(String value) {
            addCriterion("`uid` <>", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThan(String value) {
            addCriterion("`uid` >", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThanOrEqualTo(String value) {
            addCriterion("`uid` >=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThan(String value) {
            addCriterion("`uid` <", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThanOrEqualTo(String value) {
            addCriterion("`uid` <=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLike(String value) {
            addCriterion("`uid` like", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotLike(String value) {
            addCriterion("`uid` not like", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidIn(List<String> values) {
            addCriterion("`uid` in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotIn(List<String> values) {
            addCriterion("`uid` not in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidBetween(String value1, String value2) {
            addCriterion("`uid` between", value1, value2, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotBetween(String value1, String value2) {
            addCriterion("`uid` not between", value1, value2, "uid");
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

        public Criteria andPurposeIsNull() {
            addCriterion("purpose is null");
            return (Criteria) this;
        }

        public Criteria andPurposeIsNotNull() {
            addCriterion("purpose is not null");
            return (Criteria) this;
        }

        public Criteria andPurposeEqualTo(String value) {
            addCriterion("purpose =", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotEqualTo(String value) {
            addCriterion("purpose <>", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeGreaterThan(String value) {
            addCriterion("purpose >", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeGreaterThanOrEqualTo(String value) {
            addCriterion("purpose >=", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeLessThan(String value) {
            addCriterion("purpose <", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeLessThanOrEqualTo(String value) {
            addCriterion("purpose <=", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeLike(String value) {
            addCriterion("purpose like", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotLike(String value) {
            addCriterion("purpose not like", value, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeIn(List<String> values) {
            addCriterion("purpose in", values, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotIn(List<String> values) {
            addCriterion("purpose not in", values, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeBetween(String value1, String value2) {
            addCriterion("purpose between", value1, value2, "purpose");
            return (Criteria) this;
        }

        public Criteria andPurposeNotBetween(String value1, String value2) {
            addCriterion("purpose not between", value1, value2, "purpose");
            return (Criteria) this;
        }

        public Criteria andUserCodeIsNull() {
            addCriterion("user_code is null");
            return (Criteria) this;
        }

        public Criteria andUserCodeIsNotNull() {
            addCriterion("user_code is not null");
            return (Criteria) this;
        }

        public Criteria andUserCodeEqualTo(String value) {
            addCriterion("user_code =", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeNotEqualTo(String value) {
            addCriterion("user_code <>", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeGreaterThan(String value) {
            addCriterion("user_code >", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeGreaterThanOrEqualTo(String value) {
            addCriterion("user_code >=", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeLessThan(String value) {
            addCriterion("user_code <", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeLessThanOrEqualTo(String value) {
            addCriterion("user_code <=", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeLike(String value) {
            addCriterion("user_code like", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeNotLike(String value) {
            addCriterion("user_code not like", value, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeIn(List<String> values) {
            addCriterion("user_code in", values, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeNotIn(List<String> values) {
            addCriterion("user_code not in", values, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeBetween(String value1, String value2) {
            addCriterion("user_code between", value1, value2, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserCodeNotBetween(String value1, String value2) {
            addCriterion("user_code not between", value1, value2, "userCode");
            return (Criteria) this;
        }

        public Criteria andUserNameIsNull() {
            addCriterion("user_name is null");
            return (Criteria) this;
        }

        public Criteria andUserNameIsNotNull() {
            addCriterion("user_name is not null");
            return (Criteria) this;
        }

        public Criteria andUserNameEqualTo(String value) {
            addCriterion("user_name =", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotEqualTo(String value) {
            addCriterion("user_name <>", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameGreaterThan(String value) {
            addCriterion("user_name >", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("user_name >=", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameLessThan(String value) {
            addCriterion("user_name <", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameLessThanOrEqualTo(String value) {
            addCriterion("user_name <=", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameLike(String value) {
            addCriterion("user_name like", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotLike(String value) {
            addCriterion("user_name not like", value, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameIn(List<String> values) {
            addCriterion("user_name in", values, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotIn(List<String> values) {
            addCriterion("user_name not in", values, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameBetween(String value1, String value2) {
            addCriterion("user_name between", value1, value2, "userName");
            return (Criteria) this;
        }

        public Criteria andUserNameNotBetween(String value1, String value2) {
            addCriterion("user_name not between", value1, value2, "userName");
            return (Criteria) this;
        }

        public Criteria andGenderIsNull() {
            addCriterion("gender is null");
            return (Criteria) this;
        }

        public Criteria andGenderIsNotNull() {
            addCriterion("gender is not null");
            return (Criteria) this;
        }

        public Criteria andGenderEqualTo(String value) {
            addCriterion("gender =", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotEqualTo(String value) {
            addCriterion("gender <>", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderGreaterThan(String value) {
            addCriterion("gender >", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderGreaterThanOrEqualTo(String value) {
            addCriterion("gender >=", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderLessThan(String value) {
            addCriterion("gender <", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderLessThanOrEqualTo(String value) {
            addCriterion("gender <=", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderLike(String value) {
            addCriterion("gender like", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotLike(String value) {
            addCriterion("gender not like", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderIn(List<String> values) {
            addCriterion("gender in", values, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotIn(List<String> values) {
            addCriterion("gender not in", values, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderBetween(String value1, String value2) {
            addCriterion("gender between", value1, value2, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotBetween(String value1, String value2) {
            addCriterion("gender not between", value1, value2, "gender");
            return (Criteria) this;
        }

        public Criteria andPhoneIsNull() {
            addCriterion("phone is null");
            return (Criteria) this;
        }

        public Criteria andPhoneIsNotNull() {
            addCriterion("phone is not null");
            return (Criteria) this;
        }

        public Criteria andPhoneEqualTo(String value) {
            addCriterion("phone =", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotEqualTo(String value) {
            addCriterion("phone <>", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneGreaterThan(String value) {
            addCriterion("phone >", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("phone >=", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLessThan(String value) {
            addCriterion("phone <", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLessThanOrEqualTo(String value) {
            addCriterion("phone <=", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneLike(String value) {
            addCriterion("phone like", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotLike(String value) {
            addCriterion("phone not like", value, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneIn(List<String> values) {
            addCriterion("phone in", values, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotIn(List<String> values) {
            addCriterion("phone not in", values, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneBetween(String value1, String value2) {
            addCriterion("phone between", value1, value2, "phone");
            return (Criteria) this;
        }

        public Criteria andPhoneNotBetween(String value1, String value2) {
            addCriterion("phone not between", value1, value2, "phone");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrIsNull() {
            addCriterion("sign_in_time_str is null");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrIsNotNull() {
            addCriterion("sign_in_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrEqualTo(String value) {
            addCriterion("sign_in_time_str =", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrNotEqualTo(String value) {
            addCriterion("sign_in_time_str <>", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrGreaterThan(String value) {
            addCriterion("sign_in_time_str >", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("sign_in_time_str >=", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrLessThan(String value) {
            addCriterion("sign_in_time_str <", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrLessThanOrEqualTo(String value) {
            addCriterion("sign_in_time_str <=", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrLike(String value) {
            addCriterion("sign_in_time_str like", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrNotLike(String value) {
            addCriterion("sign_in_time_str not like", value, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrIn(List<String> values) {
            addCriterion("sign_in_time_str in", values, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrNotIn(List<String> values) {
            addCriterion("sign_in_time_str not in", values, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrBetween(String value1, String value2) {
            addCriterion("sign_in_time_str between", value1, value2, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andSignInTimeStrNotBetween(String value1, String value2) {
            addCriterion("sign_in_time_str not between", value1, value2, "signInTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickProductNameIsNull() {
            addCriterion("click_product_name is null");
            return (Criteria) this;
        }

        public Criteria andClickProductNameIsNotNull() {
            addCriterion("click_product_name is not null");
            return (Criteria) this;
        }

        public Criteria andClickProductNameEqualTo(String value) {
            addCriterion("click_product_name =", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameNotEqualTo(String value) {
            addCriterion("click_product_name <>", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameGreaterThan(String value) {
            addCriterion("click_product_name >", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameGreaterThanOrEqualTo(String value) {
            addCriterion("click_product_name >=", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameLessThan(String value) {
            addCriterion("click_product_name <", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameLessThanOrEqualTo(String value) {
            addCriterion("click_product_name <=", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameLike(String value) {
            addCriterion("click_product_name like", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameNotLike(String value) {
            addCriterion("click_product_name not like", value, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameIn(List<String> values) {
            addCriterion("click_product_name in", values, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameNotIn(List<String> values) {
            addCriterion("click_product_name not in", values, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameBetween(String value1, String value2) {
            addCriterion("click_product_name between", value1, value2, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickProductNameNotBetween(String value1, String value2) {
            addCriterion("click_product_name not between", value1, value2, "clickProductName");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrIsNull() {
            addCriterion("click_time_str is null");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrIsNotNull() {
            addCriterion("click_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrEqualTo(String value) {
            addCriterion("click_time_str =", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrNotEqualTo(String value) {
            addCriterion("click_time_str <>", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrGreaterThan(String value) {
            addCriterion("click_time_str >", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("click_time_str >=", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrLessThan(String value) {
            addCriterion("click_time_str <", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrLessThanOrEqualTo(String value) {
            addCriterion("click_time_str <=", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrLike(String value) {
            addCriterion("click_time_str like", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrNotLike(String value) {
            addCriterion("click_time_str not like", value, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrIn(List<String> values) {
            addCriterion("click_time_str in", values, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrNotIn(List<String> values) {
            addCriterion("click_time_str not in", values, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrBetween(String value1, String value2) {
            addCriterion("click_time_str between", value1, value2, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andClickTimeStrNotBetween(String value1, String value2) {
            addCriterion("click_time_str not between", value1, value2, "clickTimeStr");
            return (Criteria) this;
        }

        public Criteria andRecommendListIsNull() {
            addCriterion("recommend_list is null");
            return (Criteria) this;
        }

        public Criteria andRecommendListIsNotNull() {
            addCriterion("recommend_list is not null");
            return (Criteria) this;
        }

        public Criteria andRecommendListEqualTo(String value) {
            addCriterion("recommend_list =", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListNotEqualTo(String value) {
            addCriterion("recommend_list <>", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListGreaterThan(String value) {
            addCriterion("recommend_list >", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListGreaterThanOrEqualTo(String value) {
            addCriterion("recommend_list >=", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListLessThan(String value) {
            addCriterion("recommend_list <", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListLessThanOrEqualTo(String value) {
            addCriterion("recommend_list <=", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListLike(String value) {
            addCriterion("recommend_list like", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListNotLike(String value) {
            addCriterion("recommend_list not like", value, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListIn(List<String> values) {
            addCriterion("recommend_list in", values, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListNotIn(List<String> values) {
            addCriterion("recommend_list not in", values, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListBetween(String value1, String value2) {
            addCriterion("recommend_list between", value1, value2, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendListNotBetween(String value1, String value2) {
            addCriterion("recommend_list not between", value1, value2, "recommendList");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListIsNull() {
            addCriterion("recommend_h5_list is null");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListIsNotNull() {
            addCriterion("recommend_h5_list is not null");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListEqualTo(String value) {
            addCriterion("recommend_h5_list =", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListNotEqualTo(String value) {
            addCriterion("recommend_h5_list <>", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListGreaterThan(String value) {
            addCriterion("recommend_h5_list >", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListGreaterThanOrEqualTo(String value) {
            addCriterion("recommend_h5_list >=", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListLessThan(String value) {
            addCriterion("recommend_h5_list <", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListLessThanOrEqualTo(String value) {
            addCriterion("recommend_h5_list <=", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListLike(String value) {
            addCriterion("recommend_h5_list like", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListNotLike(String value) {
            addCriterion("recommend_h5_list not like", value, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListIn(List<String> values) {
            addCriterion("recommend_h5_list in", values, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListNotIn(List<String> values) {
            addCriterion("recommend_h5_list not in", values, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListBetween(String value1, String value2) {
            addCriterion("recommend_h5_list between", value1, value2, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andRecommendH5ListNotBetween(String value1, String value2) {
            addCriterion("recommend_h5_list not between", value1, value2, "recommendH5List");
            return (Criteria) this;
        }

        public Criteria andBasicInfoIsNull() {
            addCriterion("basic_info is null");
            return (Criteria) this;
        }

        public Criteria andBasicInfoIsNotNull() {
            addCriterion("basic_info is not null");
            return (Criteria) this;
        }

        public Criteria andBasicInfoEqualTo(String value) {
            addCriterion("basic_info =", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoNotEqualTo(String value) {
            addCriterion("basic_info <>", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoGreaterThan(String value) {
            addCriterion("basic_info >", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoGreaterThanOrEqualTo(String value) {
            addCriterion("basic_info >=", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoLessThan(String value) {
            addCriterion("basic_info <", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoLessThanOrEqualTo(String value) {
            addCriterion("basic_info <=", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoLike(String value) {
            addCriterion("basic_info like", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoNotLike(String value) {
            addCriterion("basic_info not like", value, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoIn(List<String> values) {
            addCriterion("basic_info in", values, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoNotIn(List<String> values) {
            addCriterion("basic_info not in", values, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoBetween(String value1, String value2) {
            addCriterion("basic_info between", value1, value2, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andBasicInfoNotBetween(String value1, String value2) {
            addCriterion("basic_info not between", value1, value2, "basicInfo");
            return (Criteria) this;
        }

        public Criteria andRealNameIsNull() {
            addCriterion("real_name is null");
            return (Criteria) this;
        }

        public Criteria andRealNameIsNotNull() {
            addCriterion("real_name is not null");
            return (Criteria) this;
        }

        public Criteria andRealNameEqualTo(String value) {
            addCriterion("real_name =", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameNotEqualTo(String value) {
            addCriterion("real_name <>", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameGreaterThan(String value) {
            addCriterion("real_name >", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameGreaterThanOrEqualTo(String value) {
            addCriterion("real_name >=", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameLessThan(String value) {
            addCriterion("real_name <", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameLessThanOrEqualTo(String value) {
            addCriterion("real_name <=", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameLike(String value) {
            addCriterion("real_name like", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameNotLike(String value) {
            addCriterion("real_name not like", value, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameIn(List<String> values) {
            addCriterion("real_name in", values, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameNotIn(List<String> values) {
            addCriterion("real_name not in", values, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameBetween(String value1, String value2) {
            addCriterion("real_name between", value1, value2, "realName");
            return (Criteria) this;
        }

        public Criteria andRealNameNotBetween(String value1, String value2) {
            addCriterion("real_name not between", value1, value2, "realName");
            return (Criteria) this;
        }

        public Criteria andSupplementIsNull() {
            addCriterion("supplement is null");
            return (Criteria) this;
        }

        public Criteria andSupplementIsNotNull() {
            addCriterion("supplement is not null");
            return (Criteria) this;
        }

        public Criteria andSupplementEqualTo(String value) {
            addCriterion("supplement =", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementNotEqualTo(String value) {
            addCriterion("supplement <>", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementGreaterThan(String value) {
            addCriterion("supplement >", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementGreaterThanOrEqualTo(String value) {
            addCriterion("supplement >=", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementLessThan(String value) {
            addCriterion("supplement <", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementLessThanOrEqualTo(String value) {
            addCriterion("supplement <=", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementLike(String value) {
            addCriterion("supplement like", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementNotLike(String value) {
            addCriterion("supplement not like", value, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementIn(List<String> values) {
            addCriterion("supplement in", values, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementNotIn(List<String> values) {
            addCriterion("supplement not in", values, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementBetween(String value1, String value2) {
            addCriterion("supplement between", value1, value2, "supplement");
            return (Criteria) this;
        }

        public Criteria andSupplementNotBetween(String value1, String value2) {
            addCriterion("supplement not between", value1, value2, "supplement");
            return (Criteria) this;
        }

        public Criteria andContractIsNull() {
            addCriterion("contract is null");
            return (Criteria) this;
        }

        public Criteria andContractIsNotNull() {
            addCriterion("contract is not null");
            return (Criteria) this;
        }

        public Criteria andContractEqualTo(String value) {
            addCriterion("contract =", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractNotEqualTo(String value) {
            addCriterion("contract <>", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractGreaterThan(String value) {
            addCriterion("contract >", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractGreaterThanOrEqualTo(String value) {
            addCriterion("contract >=", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractLessThan(String value) {
            addCriterion("contract <", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractLessThanOrEqualTo(String value) {
            addCriterion("contract <=", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractLike(String value) {
            addCriterion("contract like", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractNotLike(String value) {
            addCriterion("contract not like", value, "contract");
            return (Criteria) this;
        }

        public Criteria andContractIn(List<String> values) {
            addCriterion("contract in", values, "contract");
            return (Criteria) this;
        }

        public Criteria andContractNotIn(List<String> values) {
            addCriterion("contract not in", values, "contract");
            return (Criteria) this;
        }

        public Criteria andContractBetween(String value1, String value2) {
            addCriterion("contract between", value1, value2, "contract");
            return (Criteria) this;
        }

        public Criteria andContractNotBetween(String value1, String value2) {
            addCriterion("contract not between", value1, value2, "contract");
            return (Criteria) this;
        }

        public Criteria andOperatorIsNull() {
            addCriterion("`operator` is null");
            return (Criteria) this;
        }

        public Criteria andOperatorIsNotNull() {
            addCriterion("`operator` is not null");
            return (Criteria) this;
        }

        public Criteria andOperatorEqualTo(String value) {
            addCriterion("`operator` =", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorNotEqualTo(String value) {
            addCriterion("`operator` <>", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorGreaterThan(String value) {
            addCriterion("`operator` >", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorGreaterThanOrEqualTo(String value) {
            addCriterion("`operator` >=", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorLessThan(String value) {
            addCriterion("`operator` <", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorLessThanOrEqualTo(String value) {
            addCriterion("`operator` <=", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorLike(String value) {
            addCriterion("`operator` like", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorNotLike(String value) {
            addCriterion("`operator` not like", value, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorIn(List<String> values) {
            addCriterion("`operator` in", values, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorNotIn(List<String> values) {
            addCriterion("`operator` not in", values, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorBetween(String value1, String value2) {
            addCriterion("`operator` between", value1, value2, "operator");
            return (Criteria) this;
        }

        public Criteria andOperatorNotBetween(String value1, String value2) {
            addCriterion("`operator` not between", value1, value2, "operator");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameIsNull() {
            addCriterion("loan_product_name is null");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameIsNotNull() {
            addCriterion("loan_product_name is not null");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameEqualTo(String value) {
            addCriterion("loan_product_name =", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameNotEqualTo(String value) {
            addCriterion("loan_product_name <>", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameGreaterThan(String value) {
            addCriterion("loan_product_name >", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameGreaterThanOrEqualTo(String value) {
            addCriterion("loan_product_name >=", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameLessThan(String value) {
            addCriterion("loan_product_name <", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameLessThanOrEqualTo(String value) {
            addCriterion("loan_product_name <=", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameLike(String value) {
            addCriterion("loan_product_name like", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameNotLike(String value) {
            addCriterion("loan_product_name not like", value, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameIn(List<String> values) {
            addCriterion("loan_product_name in", values, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameNotIn(List<String> values) {
            addCriterion("loan_product_name not in", values, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameBetween(String value1, String value2) {
            addCriterion("loan_product_name between", value1, value2, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanProductNameNotBetween(String value1, String value2) {
            addCriterion("loan_product_name not between", value1, value2, "loanProductName");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrIsNull() {
            addCriterion("loan_time_str is null");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrIsNotNull() {
            addCriterion("loan_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrEqualTo(String value) {
            addCriterion("loan_time_str =", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrNotEqualTo(String value) {
            addCriterion("loan_time_str <>", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrGreaterThan(String value) {
            addCriterion("loan_time_str >", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("loan_time_str >=", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrLessThan(String value) {
            addCriterion("loan_time_str <", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrLessThanOrEqualTo(String value) {
            addCriterion("loan_time_str <=", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrLike(String value) {
            addCriterion("loan_time_str like", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrNotLike(String value) {
            addCriterion("loan_time_str not like", value, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrIn(List<String> values) {
            addCriterion("loan_time_str in", values, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrNotIn(List<String> values) {
            addCriterion("loan_time_str not in", values, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrBetween(String value1, String value2) {
            addCriterion("loan_time_str between", value1, value2, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanTimeStrNotBetween(String value1, String value2) {
            addCriterion("loan_time_str not between", value1, value2, "loanTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrIsNull() {
            addCriterion("create_time_str is null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrIsNotNull() {
            addCriterion("create_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrEqualTo(String value) {
            addCriterion("create_time_str =", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrNotEqualTo(String value) {
            addCriterion("create_time_str <>", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrGreaterThan(String value) {
            addCriterion("create_time_str >", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("create_time_str >=", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrLessThan(String value) {
            addCriterion("create_time_str <", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrLessThanOrEqualTo(String value) {
            addCriterion("create_time_str <=", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrLike(String value) {
            addCriterion("create_time_str like", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrNotLike(String value) {
            addCriterion("create_time_str not like", value, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrIn(List<String> values) {
            addCriterion("create_time_str in", values, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrNotIn(List<String> values) {
            addCriterion("create_time_str not in", values, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrBetween(String value1, String value2) {
            addCriterion("create_time_str between", value1, value2, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreateTimeStrNotBetween(String value1, String value2) {
            addCriterion("create_time_str not between", value1, value2, "createTimeStr");
            return (Criteria) this;
        }

        public Criteria andDiffAmountIsNull() {
            addCriterion("diff_amount is null");
            return (Criteria) this;
        }

        public Criteria andDiffAmountIsNotNull() {
            addCriterion("diff_amount is not null");
            return (Criteria) this;
        }

        public Criteria andDiffAmountEqualTo(String value) {
            addCriterion("diff_amount =", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountNotEqualTo(String value) {
            addCriterion("diff_amount <>", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountGreaterThan(String value) {
            addCriterion("diff_amount >", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountGreaterThanOrEqualTo(String value) {
            addCriterion("diff_amount >=", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountLessThan(String value) {
            addCriterion("diff_amount <", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountLessThanOrEqualTo(String value) {
            addCriterion("diff_amount <=", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountLike(String value) {
            addCriterion("diff_amount like", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountNotLike(String value) {
            addCriterion("diff_amount not like", value, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountIn(List<String> values) {
            addCriterion("diff_amount in", values, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountNotIn(List<String> values) {
            addCriterion("diff_amount not in", values, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountBetween(String value1, String value2) {
            addCriterion("diff_amount between", value1, value2, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andDiffAmountNotBetween(String value1, String value2) {
            addCriterion("diff_amount not between", value1, value2, "diffAmount");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionIsNull() {
            addCriterion("face_recognition is null");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionIsNotNull() {
            addCriterion("face_recognition is not null");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionEqualTo(String value) {
            addCriterion("face_recognition =", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionNotEqualTo(String value) {
            addCriterion("face_recognition <>", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionGreaterThan(String value) {
            addCriterion("face_recognition >", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionGreaterThanOrEqualTo(String value) {
            addCriterion("face_recognition >=", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionLessThan(String value) {
            addCriterion("face_recognition <", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionLessThanOrEqualTo(String value) {
            addCriterion("face_recognition <=", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionLike(String value) {
            addCriterion("face_recognition like", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionNotLike(String value) {
            addCriterion("face_recognition not like", value, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionIn(List<String> values) {
            addCriterion("face_recognition in", values, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionNotIn(List<String> values) {
            addCriterion("face_recognition not in", values, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionBetween(String value1, String value2) {
            addCriterion("face_recognition between", value1, value2, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFaceRecognitionNotBetween(String value1, String value2) {
            addCriterion("face_recognition not between", value1, value2, "faceRecognition");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultIsNull() {
            addCriterion("first_approve_result is null");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultIsNotNull() {
            addCriterion("first_approve_result is not null");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultEqualTo(String value) {
            addCriterion("first_approve_result =", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultNotEqualTo(String value) {
            addCriterion("first_approve_result <>", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultGreaterThan(String value) {
            addCriterion("first_approve_result >", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultGreaterThanOrEqualTo(String value) {
            addCriterion("first_approve_result >=", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultLessThan(String value) {
            addCriterion("first_approve_result <", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultLessThanOrEqualTo(String value) {
            addCriterion("first_approve_result <=", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultLike(String value) {
            addCriterion("first_approve_result like", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultNotLike(String value) {
            addCriterion("first_approve_result not like", value, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultIn(List<String> values) {
            addCriterion("first_approve_result in", values, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultNotIn(List<String> values) {
            addCriterion("first_approve_result not in", values, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultBetween(String value1, String value2) {
            addCriterion("first_approve_result between", value1, value2, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveResultNotBetween(String value1, String value2) {
            addCriterion("first_approve_result not between", value1, value2, "firstApproveResult");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrIsNull() {
            addCriterion("first_approve_time_str is null");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrIsNotNull() {
            addCriterion("first_approve_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrEqualTo(String value) {
            addCriterion("first_approve_time_str =", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrNotEqualTo(String value) {
            addCriterion("first_approve_time_str <>", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrGreaterThan(String value) {
            addCriterion("first_approve_time_str >", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("first_approve_time_str >=", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrLessThan(String value) {
            addCriterion("first_approve_time_str <", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrLessThanOrEqualTo(String value) {
            addCriterion("first_approve_time_str <=", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrLike(String value) {
            addCriterion("first_approve_time_str like", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrNotLike(String value) {
            addCriterion("first_approve_time_str not like", value, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrIn(List<String> values) {
            addCriterion("first_approve_time_str in", values, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrNotIn(List<String> values) {
            addCriterion("first_approve_time_str not in", values, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrBetween(String value1, String value2) {
            addCriterion("first_approve_time_str between", value1, value2, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstApproveTimeStrNotBetween(String value1, String value2) {
            addCriterion("first_approve_time_str not between", value1, value2, "firstApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andHasBindCardIsNull() {
            addCriterion("has_bind_card is null");
            return (Criteria) this;
        }

        public Criteria andHasBindCardIsNotNull() {
            addCriterion("has_bind_card is not null");
            return (Criteria) this;
        }

        public Criteria andHasBindCardEqualTo(String value) {
            addCriterion("has_bind_card =", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardNotEqualTo(String value) {
            addCriterion("has_bind_card <>", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardGreaterThan(String value) {
            addCriterion("has_bind_card >", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardGreaterThanOrEqualTo(String value) {
            addCriterion("has_bind_card >=", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardLessThan(String value) {
            addCriterion("has_bind_card <", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardLessThanOrEqualTo(String value) {
            addCriterion("has_bind_card <=", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardLike(String value) {
            addCriterion("has_bind_card like", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardNotLike(String value) {
            addCriterion("has_bind_card not like", value, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardIn(List<String> values) {
            addCriterion("has_bind_card in", values, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardNotIn(List<String> values) {
            addCriterion("has_bind_card not in", values, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardBetween(String value1, String value2) {
            addCriterion("has_bind_card between", value1, value2, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasBindCardNotBetween(String value1, String value2) {
            addCriterion("has_bind_card not between", value1, value2, "hasBindCard");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowIsNull() {
            addCriterion("has_ever_borrow is null");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowIsNotNull() {
            addCriterion("has_ever_borrow is not null");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowEqualTo(String value) {
            addCriterion("has_ever_borrow =", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowNotEqualTo(String value) {
            addCriterion("has_ever_borrow <>", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowGreaterThan(String value) {
            addCriterion("has_ever_borrow >", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowGreaterThanOrEqualTo(String value) {
            addCriterion("has_ever_borrow >=", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowLessThan(String value) {
            addCriterion("has_ever_borrow <", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowLessThanOrEqualTo(String value) {
            addCriterion("has_ever_borrow <=", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowLike(String value) {
            addCriterion("has_ever_borrow like", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowNotLike(String value) {
            addCriterion("has_ever_borrow not like", value, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowIn(List<String> values) {
            addCriterion("has_ever_borrow in", values, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowNotIn(List<String> values) {
            addCriterion("has_ever_borrow not in", values, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowBetween(String value1, String value2) {
            addCriterion("has_ever_borrow between", value1, value2, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasEverBorrowNotBetween(String value1, String value2) {
            addCriterion("has_ever_borrow not between", value1, value2, "hasEverBorrow");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawIsNull() {
            addCriterion("has_withdraw is null");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawIsNotNull() {
            addCriterion("has_withdraw is not null");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawEqualTo(String value) {
            addCriterion("has_withdraw =", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawNotEqualTo(String value) {
            addCriterion("has_withdraw <>", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawGreaterThan(String value) {
            addCriterion("has_withdraw >", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawGreaterThanOrEqualTo(String value) {
            addCriterion("has_withdraw >=", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawLessThan(String value) {
            addCriterion("has_withdraw <", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawLessThanOrEqualTo(String value) {
            addCriterion("has_withdraw <=", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawLike(String value) {
            addCriterion("has_withdraw like", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawNotLike(String value) {
            addCriterion("has_withdraw not like", value, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawIn(List<String> values) {
            addCriterion("has_withdraw in", values, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawNotIn(List<String> values) {
            addCriterion("has_withdraw not in", values, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawBetween(String value1, String value2) {
            addCriterion("has_withdraw between", value1, value2, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andHasWithdrawNotBetween(String value1, String value2) {
            addCriterion("has_withdraw not between", value1, value2, "hasWithdraw");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagIsNull() {
            addCriterion("instead_commit_flag is null");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagIsNotNull() {
            addCriterion("instead_commit_flag is not null");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagEqualTo(String value) {
            addCriterion("instead_commit_flag =", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagNotEqualTo(String value) {
            addCriterion("instead_commit_flag <>", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagGreaterThan(String value) {
            addCriterion("instead_commit_flag >", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagGreaterThanOrEqualTo(String value) {
            addCriterion("instead_commit_flag >=", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagLessThan(String value) {
            addCriterion("instead_commit_flag <", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagLessThanOrEqualTo(String value) {
            addCriterion("instead_commit_flag <=", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagLike(String value) {
            addCriterion("instead_commit_flag like", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagNotLike(String value) {
            addCriterion("instead_commit_flag not like", value, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagIn(List<String> values) {
            addCriterion("instead_commit_flag in", values, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagNotIn(List<String> values) {
            addCriterion("instead_commit_flag not in", values, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagBetween(String value1, String value2) {
            addCriterion("instead_commit_flag between", value1, value2, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitFlagNotBetween(String value1, String value2) {
            addCriterion("instead_commit_flag not between", value1, value2, "insteadCommitFlag");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameIsNull() {
            addCriterion("instead_commit_pname is null");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameIsNotNull() {
            addCriterion("instead_commit_pname is not null");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameEqualTo(String value) {
            addCriterion("instead_commit_pname =", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameNotEqualTo(String value) {
            addCriterion("instead_commit_pname <>", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameGreaterThan(String value) {
            addCriterion("instead_commit_pname >", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameGreaterThanOrEqualTo(String value) {
            addCriterion("instead_commit_pname >=", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameLessThan(String value) {
            addCriterion("instead_commit_pname <", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameLessThanOrEqualTo(String value) {
            addCriterion("instead_commit_pname <=", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameLike(String value) {
            addCriterion("instead_commit_pname like", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameNotLike(String value) {
            addCriterion("instead_commit_pname not like", value, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameIn(List<String> values) {
            addCriterion("instead_commit_pname in", values, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameNotIn(List<String> values) {
            addCriterion("instead_commit_pname not in", values, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameBetween(String value1, String value2) {
            addCriterion("instead_commit_pname between", value1, value2, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andInsteadCommitPnameNotBetween(String value1, String value2) {
            addCriterion("instead_commit_pname not between", value1, value2, "insteadCommitPname");
            return (Criteria) this;
        }

        public Criteria andIsTimelyIsNull() {
            addCriterion("is_timely is null");
            return (Criteria) this;
        }

        public Criteria andIsTimelyIsNotNull() {
            addCriterion("is_timely is not null");
            return (Criteria) this;
        }

        public Criteria andIsTimelyEqualTo(String value) {
            addCriterion("is_timely =", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyNotEqualTo(String value) {
            addCriterion("is_timely <>", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyGreaterThan(String value) {
            addCriterion("is_timely >", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyGreaterThanOrEqualTo(String value) {
            addCriterion("is_timely >=", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyLessThan(String value) {
            addCriterion("is_timely <", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyLessThanOrEqualTo(String value) {
            addCriterion("is_timely <=", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyLike(String value) {
            addCriterion("is_timely like", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyNotLike(String value) {
            addCriterion("is_timely not like", value, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyIn(List<String> values) {
            addCriterion("is_timely in", values, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyNotIn(List<String> values) {
            addCriterion("is_timely not in", values, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyBetween(String value1, String value2) {
            addCriterion("is_timely between", value1, value2, "isTimely");
            return (Criteria) this;
        }

        public Criteria andIsTimelyNotBetween(String value1, String value2) {
            addCriterion("is_timely not between", value1, value2, "isTimely");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrIsNull() {
            addCriterion("loan_failed_time_str is null");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrIsNotNull() {
            addCriterion("loan_failed_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrEqualTo(String value) {
            addCriterion("loan_failed_time_str =", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrNotEqualTo(String value) {
            addCriterion("loan_failed_time_str <>", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrGreaterThan(String value) {
            addCriterion("loan_failed_time_str >", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("loan_failed_time_str >=", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrLessThan(String value) {
            addCriterion("loan_failed_time_str <", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrLessThanOrEqualTo(String value) {
            addCriterion("loan_failed_time_str <=", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrLike(String value) {
            addCriterion("loan_failed_time_str like", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrNotLike(String value) {
            addCriterion("loan_failed_time_str not like", value, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrIn(List<String> values) {
            addCriterion("loan_failed_time_str in", values, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrNotIn(List<String> values) {
            addCriterion("loan_failed_time_str not in", values, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrBetween(String value1, String value2) {
            addCriterion("loan_failed_time_str between", value1, value2, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanFailedTimeStrNotBetween(String value1, String value2) {
            addCriterion("loan_failed_time_str not between", value1, value2, "loanFailedTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrIsNull() {
            addCriterion("loan_success_time_str is null");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrIsNotNull() {
            addCriterion("loan_success_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrEqualTo(String value) {
            addCriterion("loan_success_time_str =", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrNotEqualTo(String value) {
            addCriterion("loan_success_time_str <>", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrGreaterThan(String value) {
            addCriterion("loan_success_time_str >", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("loan_success_time_str >=", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrLessThan(String value) {
            addCriterion("loan_success_time_str <", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrLessThanOrEqualTo(String value) {
            addCriterion("loan_success_time_str <=", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrLike(String value) {
            addCriterion("loan_success_time_str like", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrNotLike(String value) {
            addCriterion("loan_success_time_str not like", value, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrIn(List<String> values) {
            addCriterion("loan_success_time_str in", values, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrNotIn(List<String> values) {
            addCriterion("loan_success_time_str not in", values, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrBetween(String value1, String value2) {
            addCriterion("loan_success_time_str between", value1, value2, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanSuccessTimeStrNotBetween(String value1, String value2) {
            addCriterion("loan_success_time_str not between", value1, value2, "loanSuccessTimeStr");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessIsNull() {
            addCriterion("loan_willingness is null");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessIsNotNull() {
            addCriterion("loan_willingness is not null");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessEqualTo(String value) {
            addCriterion("loan_willingness =", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessNotEqualTo(String value) {
            addCriterion("loan_willingness <>", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessGreaterThan(String value) {
            addCriterion("loan_willingness >", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessGreaterThanOrEqualTo(String value) {
            addCriterion("loan_willingness >=", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessLessThan(String value) {
            addCriterion("loan_willingness <", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessLessThanOrEqualTo(String value) {
            addCriterion("loan_willingness <=", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessLike(String value) {
            addCriterion("loan_willingness like", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessNotLike(String value) {
            addCriterion("loan_willingness not like", value, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessIn(List<String> values) {
            addCriterion("loan_willingness in", values, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessNotIn(List<String> values) {
            addCriterion("loan_willingness not in", values, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessBetween(String value1, String value2) {
            addCriterion("loan_willingness between", value1, value2, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andLoanWillingnessNotBetween(String value1, String value2) {
            addCriterion("loan_willingness not between", value1, value2, "loanWillingness");
            return (Criteria) this;
        }

        public Criteria andACardScoreIsNull() {
            addCriterion("a_card_score is null");
            return (Criteria) this;
        }

        public Criteria andACardScoreIsNotNull() {
            addCriterion("a_card_score is not null");
            return (Criteria) this;
        }

        public Criteria andACardScoreEqualTo(String value) {
            addCriterion("a_card_score =", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreNotEqualTo(String value) {
            addCriterion("a_card_score <>", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreGreaterThan(String value) {
            addCriterion("a_card_score >", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreGreaterThanOrEqualTo(String value) {
            addCriterion("a_card_score >=", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreLessThan(String value) {
            addCriterion("a_card_score <", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreLessThanOrEqualTo(String value) {
            addCriterion("a_card_score <=", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreLike(String value) {
            addCriterion("a_card_score like", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreNotLike(String value) {
            addCriterion("a_card_score not like", value, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreIn(List<String> values) {
            addCriterion("a_card_score in", values, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreNotIn(List<String> values) {
            addCriterion("a_card_score not in", values, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreBetween(String value1, String value2) {
            addCriterion("a_card_score between", value1, value2, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andACardScoreNotBetween(String value1, String value2) {
            addCriterion("a_card_score not between", value1, value2, "aCardScore");
            return (Criteria) this;
        }

        public Criteria andBucketNameIsNull() {
            addCriterion("bucket_name is null");
            return (Criteria) this;
        }

        public Criteria andBucketNameIsNotNull() {
            addCriterion("bucket_name is not null");
            return (Criteria) this;
        }

        public Criteria andBucketNameEqualTo(String value) {
            addCriterion("bucket_name =", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameNotEqualTo(String value) {
            addCriterion("bucket_name <>", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameGreaterThan(String value) {
            addCriterion("bucket_name >", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameGreaterThanOrEqualTo(String value) {
            addCriterion("bucket_name >=", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameLessThan(String value) {
            addCriterion("bucket_name <", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameLessThanOrEqualTo(String value) {
            addCriterion("bucket_name <=", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameLike(String value) {
            addCriterion("bucket_name like", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameNotLike(String value) {
            addCriterion("bucket_name not like", value, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameIn(List<String> values) {
            addCriterion("bucket_name in", values, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameNotIn(List<String> values) {
            addCriterion("bucket_name not in", values, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameBetween(String value1, String value2) {
            addCriterion("bucket_name between", value1, value2, "bucketName");
            return (Criteria) this;
        }

        public Criteria andBucketNameNotBetween(String value1, String value2) {
            addCriterion("bucket_name not between", value1, value2, "bucketName");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysIsNull() {
            addCriterion("overdue_days is null");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysIsNotNull() {
            addCriterion("overdue_days is not null");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysEqualTo(String value) {
            addCriterion("overdue_days =", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysNotEqualTo(String value) {
            addCriterion("overdue_days <>", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysGreaterThan(String value) {
            addCriterion("overdue_days >", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysGreaterThanOrEqualTo(String value) {
            addCriterion("overdue_days >=", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysLessThan(String value) {
            addCriterion("overdue_days <", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysLessThanOrEqualTo(String value) {
            addCriterion("overdue_days <=", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysLike(String value) {
            addCriterion("overdue_days like", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysNotLike(String value) {
            addCriterion("overdue_days not like", value, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysIn(List<String> values) {
            addCriterion("overdue_days in", values, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysNotIn(List<String> values) {
            addCriterion("overdue_days not in", values, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysBetween(String value1, String value2) {
            addCriterion("overdue_days between", value1, value2, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andOverdueDaysNotBetween(String value1, String value2) {
            addCriterion("overdue_days not between", value1, value2, "overdueDays");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountIsNull() {
            addCriterion("prepay_amount is null");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountIsNotNull() {
            addCriterion("prepay_amount is not null");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountEqualTo(String value) {
            addCriterion("prepay_amount =", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountNotEqualTo(String value) {
            addCriterion("prepay_amount <>", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountGreaterThan(String value) {
            addCriterion("prepay_amount >", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountGreaterThanOrEqualTo(String value) {
            addCriterion("prepay_amount >=", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountLessThan(String value) {
            addCriterion("prepay_amount <", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountLessThanOrEqualTo(String value) {
            addCriterion("prepay_amount <=", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountLike(String value) {
            addCriterion("prepay_amount like", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountNotLike(String value) {
            addCriterion("prepay_amount not like", value, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountIn(List<String> values) {
            addCriterion("prepay_amount in", values, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountNotIn(List<String> values) {
            addCriterion("prepay_amount not in", values, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountBetween(String value1, String value2) {
            addCriterion("prepay_amount between", value1, value2, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayAmountNotBetween(String value1, String value2) {
            addCriterion("prepay_amount not between", value1, value2, "prepayAmount");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameIsNull() {
            addCriterion("prepay_pname is null");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameIsNotNull() {
            addCriterion("prepay_pname is not null");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameEqualTo(String value) {
            addCriterion("prepay_pname =", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameNotEqualTo(String value) {
            addCriterion("prepay_pname <>", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameGreaterThan(String value) {
            addCriterion("prepay_pname >", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameGreaterThanOrEqualTo(String value) {
            addCriterion("prepay_pname >=", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameLessThan(String value) {
            addCriterion("prepay_pname <", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameLessThanOrEqualTo(String value) {
            addCriterion("prepay_pname <=", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameLike(String value) {
            addCriterion("prepay_pname like", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameNotLike(String value) {
            addCriterion("prepay_pname not like", value, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameIn(List<String> values) {
            addCriterion("prepay_pname in", values, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameNotIn(List<String> values) {
            addCriterion("prepay_pname not in", values, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameBetween(String value1, String value2) {
            addCriterion("prepay_pname between", value1, value2, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayPnameNotBetween(String value1, String value2) {
            addCriterion("prepay_pname not between", value1, value2, "prepayPname");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrIsNull() {
            addCriterion("prepay_time_str is null");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrIsNotNull() {
            addCriterion("prepay_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrEqualTo(String value) {
            addCriterion("prepay_time_str =", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrNotEqualTo(String value) {
            addCriterion("prepay_time_str <>", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrGreaterThan(String value) {
            addCriterion("prepay_time_str >", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("prepay_time_str >=", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrLessThan(String value) {
            addCriterion("prepay_time_str <", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrLessThanOrEqualTo(String value) {
            addCriterion("prepay_time_str <=", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrLike(String value) {
            addCriterion("prepay_time_str like", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrNotLike(String value) {
            addCriterion("prepay_time_str not like", value, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrIn(List<String> values) {
            addCriterion("prepay_time_str in", values, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrNotIn(List<String> values) {
            addCriterion("prepay_time_str not in", values, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrBetween(String value1, String value2) {
            addCriterion("prepay_time_str between", value1, value2, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andPrepayTimeStrNotBetween(String value1, String value2) {
            addCriterion("prepay_time_str not between", value1, value2, "prepayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayPnameIsNull() {
            addCriterion("repay_pname is null");
            return (Criteria) this;
        }

        public Criteria andRepayPnameIsNotNull() {
            addCriterion("repay_pname is not null");
            return (Criteria) this;
        }

        public Criteria andRepayPnameEqualTo(String value) {
            addCriterion("repay_pname =", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameNotEqualTo(String value) {
            addCriterion("repay_pname <>", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameGreaterThan(String value) {
            addCriterion("repay_pname >", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameGreaterThanOrEqualTo(String value) {
            addCriterion("repay_pname >=", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameLessThan(String value) {
            addCriterion("repay_pname <", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameLessThanOrEqualTo(String value) {
            addCriterion("repay_pname <=", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameLike(String value) {
            addCriterion("repay_pname like", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameNotLike(String value) {
            addCriterion("repay_pname not like", value, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameIn(List<String> values) {
            addCriterion("repay_pname in", values, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameNotIn(List<String> values) {
            addCriterion("repay_pname not in", values, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameBetween(String value1, String value2) {
            addCriterion("repay_pname between", value1, value2, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayPnameNotBetween(String value1, String value2) {
            addCriterion("repay_pname not between", value1, value2, "repayPname");
            return (Criteria) this;
        }

        public Criteria andRepayAmountIsNull() {
            addCriterion("repay_amount is null");
            return (Criteria) this;
        }

        public Criteria andRepayAmountIsNotNull() {
            addCriterion("repay_amount is not null");
            return (Criteria) this;
        }

        public Criteria andRepayAmountEqualTo(String value) {
            addCriterion("repay_amount =", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountNotEqualTo(String value) {
            addCriterion("repay_amount <>", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountGreaterThan(String value) {
            addCriterion("repay_amount >", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountGreaterThanOrEqualTo(String value) {
            addCriterion("repay_amount >=", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountLessThan(String value) {
            addCriterion("repay_amount <", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountLessThanOrEqualTo(String value) {
            addCriterion("repay_amount <=", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountLike(String value) {
            addCriterion("repay_amount like", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountNotLike(String value) {
            addCriterion("repay_amount not like", value, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountIn(List<String> values) {
            addCriterion("repay_amount in", values, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountNotIn(List<String> values) {
            addCriterion("repay_amount not in", values, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountBetween(String value1, String value2) {
            addCriterion("repay_amount between", value1, value2, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayAmountNotBetween(String value1, String value2) {
            addCriterion("repay_amount not between", value1, value2, "repayAmount");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrIsNull() {
            addCriterion("repay_time_str is null");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrIsNotNull() {
            addCriterion("repay_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrEqualTo(String value) {
            addCriterion("repay_time_str =", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrNotEqualTo(String value) {
            addCriterion("repay_time_str <>", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrGreaterThan(String value) {
            addCriterion("repay_time_str >", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("repay_time_str >=", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrLessThan(String value) {
            addCriterion("repay_time_str <", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrLessThanOrEqualTo(String value) {
            addCriterion("repay_time_str <=", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrLike(String value) {
            addCriterion("repay_time_str like", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrNotLike(String value) {
            addCriterion("repay_time_str not like", value, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrIn(List<String> values) {
            addCriterion("repay_time_str in", values, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrNotIn(List<String> values) {
            addCriterion("repay_time_str not in", values, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrBetween(String value1, String value2) {
            addCriterion("repay_time_str between", value1, value2, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andRepayTimeStrNotBetween(String value1, String value2) {
            addCriterion("repay_time_str not between", value1, value2, "repayTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultIsNull() {
            addCriterion("second_approve_result is null");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultIsNotNull() {
            addCriterion("second_approve_result is not null");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultEqualTo(String value) {
            addCriterion("second_approve_result =", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultNotEqualTo(String value) {
            addCriterion("second_approve_result <>", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultGreaterThan(String value) {
            addCriterion("second_approve_result >", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultGreaterThanOrEqualTo(String value) {
            addCriterion("second_approve_result >=", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultLessThan(String value) {
            addCriterion("second_approve_result <", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultLessThanOrEqualTo(String value) {
            addCriterion("second_approve_result <=", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultLike(String value) {
            addCriterion("second_approve_result like", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultNotLike(String value) {
            addCriterion("second_approve_result not like", value, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultIn(List<String> values) {
            addCriterion("second_approve_result in", values, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultNotIn(List<String> values) {
            addCriterion("second_approve_result not in", values, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultBetween(String value1, String value2) {
            addCriterion("second_approve_result between", value1, value2, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveResultNotBetween(String value1, String value2) {
            addCriterion("second_approve_result not between", value1, value2, "secondApproveResult");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrIsNull() {
            addCriterion("second_approve_time_str is null");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrIsNotNull() {
            addCriterion("second_approve_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrEqualTo(String value) {
            addCriterion("second_approve_time_str =", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrNotEqualTo(String value) {
            addCriterion("second_approve_time_str <>", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrGreaterThan(String value) {
            addCriterion("second_approve_time_str >", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("second_approve_time_str >=", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrLessThan(String value) {
            addCriterion("second_approve_time_str <", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrLessThanOrEqualTo(String value) {
            addCriterion("second_approve_time_str <=", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrLike(String value) {
            addCriterion("second_approve_time_str like", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrNotLike(String value) {
            addCriterion("second_approve_time_str not like", value, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrIn(List<String> values) {
            addCriterion("second_approve_time_str in", values, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrNotIn(List<String> values) {
            addCriterion("second_approve_time_str not in", values, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrBetween(String value1, String value2) {
            addCriterion("second_approve_time_str between", value1, value2, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andSecondApproveTimeStrNotBetween(String value1, String value2) {
            addCriterion("second_approve_time_str not between", value1, value2, "secondApproveTimeStr");
            return (Criteria) this;
        }

        public Criteria andApplyAmountIsNull() {
            addCriterion("apply_amount is null");
            return (Criteria) this;
        }

        public Criteria andApplyAmountIsNotNull() {
            addCriterion("apply_amount is not null");
            return (Criteria) this;
        }

        public Criteria andApplyAmountEqualTo(String value) {
            addCriterion("apply_amount =", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountNotEqualTo(String value) {
            addCriterion("apply_amount <>", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountGreaterThan(String value) {
            addCriterion("apply_amount >", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountGreaterThanOrEqualTo(String value) {
            addCriterion("apply_amount >=", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountLessThan(String value) {
            addCriterion("apply_amount <", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountLessThanOrEqualTo(String value) {
            addCriterion("apply_amount <=", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountLike(String value) {
            addCriterion("apply_amount like", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountNotLike(String value) {
            addCriterion("apply_amount not like", value, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountIn(List<String> values) {
            addCriterion("apply_amount in", values, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountNotIn(List<String> values) {
            addCriterion("apply_amount not in", values, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountBetween(String value1, String value2) {
            addCriterion("apply_amount between", value1, value2, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApplyAmountNotBetween(String value1, String value2) {
            addCriterion("apply_amount not between", value1, value2, "applyAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountIsNull() {
            addCriterion("approve_amount is null");
            return (Criteria) this;
        }

        public Criteria andApproveAmountIsNotNull() {
            addCriterion("approve_amount is not null");
            return (Criteria) this;
        }

        public Criteria andApproveAmountEqualTo(String value) {
            addCriterion("approve_amount =", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountNotEqualTo(String value) {
            addCriterion("approve_amount <>", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountGreaterThan(String value) {
            addCriterion("approve_amount >", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountGreaterThanOrEqualTo(String value) {
            addCriterion("approve_amount >=", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountLessThan(String value) {
            addCriterion("approve_amount <", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountLessThanOrEqualTo(String value) {
            addCriterion("approve_amount <=", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountLike(String value) {
            addCriterion("approve_amount like", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountNotLike(String value) {
            addCriterion("approve_amount not like", value, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountIn(List<String> values) {
            addCriterion("approve_amount in", values, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountNotIn(List<String> values) {
            addCriterion("approve_amount not in", values, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountBetween(String value1, String value2) {
            addCriterion("approve_amount between", value1, value2, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andApproveAmountNotBetween(String value1, String value2) {
            addCriterion("approve_amount not between", value1, value2, "approveAmount");
            return (Criteria) this;
        }

        public Criteria andSourceIsNull() {
            addCriterion("`source` is null");
            return (Criteria) this;
        }

        public Criteria andSourceIsNotNull() {
            addCriterion("`source` is not null");
            return (Criteria) this;
        }

        public Criteria andSourceEqualTo(String value) {
            addCriterion("`source` =", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotEqualTo(String value) {
            addCriterion("`source` <>", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceGreaterThan(String value) {
            addCriterion("`source` >", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceGreaterThanOrEqualTo(String value) {
            addCriterion("`source` >=", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLessThan(String value) {
            addCriterion("`source` <", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLessThanOrEqualTo(String value) {
            addCriterion("`source` <=", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLike(String value) {
            addCriterion("`source` like", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotLike(String value) {
            addCriterion("`source` not like", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceIn(List<String> values) {
            addCriterion("`source` in", values, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotIn(List<String> values) {
            addCriterion("`source` not in", values, "source");
            return (Criteria) this;
        }

        public Criteria andSourceBetween(String value1, String value2) {
            addCriterion("`source` between", value1, value2, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotBetween(String value1, String value2) {
            addCriterion("`source` not between", value1, value2, "source");
            return (Criteria) this;
        }

        public Criteria andProdTypeIsNull() {
            addCriterion("prod_type is null");
            return (Criteria) this;
        }

        public Criteria andProdTypeIsNotNull() {
            addCriterion("prod_type is not null");
            return (Criteria) this;
        }

        public Criteria andProdTypeEqualTo(String value) {
            addCriterion("prod_type =", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeNotEqualTo(String value) {
            addCriterion("prod_type <>", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeGreaterThan(String value) {
            addCriterion("prod_type >", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeGreaterThanOrEqualTo(String value) {
            addCriterion("prod_type >=", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeLessThan(String value) {
            addCriterion("prod_type <", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeLessThanOrEqualTo(String value) {
            addCriterion("prod_type <=", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeLike(String value) {
            addCriterion("prod_type like", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeNotLike(String value) {
            addCriterion("prod_type not like", value, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeIn(List<String> values) {
            addCriterion("prod_type in", values, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeNotIn(List<String> values) {
            addCriterion("prod_type not in", values, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeBetween(String value1, String value2) {
            addCriterion("prod_type between", value1, value2, "prodType");
            return (Criteria) this;
        }

        public Criteria andProdTypeNotBetween(String value1, String value2) {
            addCriterion("prod_type not between", value1, value2, "prodType");
            return (Criteria) this;
        }

        public Criteria andScoreIsNull() {
            addCriterion("score is null");
            return (Criteria) this;
        }

        public Criteria andScoreIsNotNull() {
            addCriterion("score is not null");
            return (Criteria) this;
        }

        public Criteria andScoreEqualTo(String value) {
            addCriterion("score =", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreNotEqualTo(String value) {
            addCriterion("score <>", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreGreaterThan(String value) {
            addCriterion("score >", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreGreaterThanOrEqualTo(String value) {
            addCriterion("score >=", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreLessThan(String value) {
            addCriterion("score <", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreLessThanOrEqualTo(String value) {
            addCriterion("score <=", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreLike(String value) {
            addCriterion("score like", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreNotLike(String value) {
            addCriterion("score not like", value, "score");
            return (Criteria) this;
        }

        public Criteria andScoreIn(List<String> values) {
            addCriterion("score in", values, "score");
            return (Criteria) this;
        }

        public Criteria andScoreNotIn(List<String> values) {
            addCriterion("score not in", values, "score");
            return (Criteria) this;
        }

        public Criteria andScoreBetween(String value1, String value2) {
            addCriterion("score between", value1, value2, "score");
            return (Criteria) this;
        }

        public Criteria andScoreNotBetween(String value1, String value2) {
            addCriterion("score not between", value1, value2, "score");
            return (Criteria) this;
        }

        public Criteria andCallTimesIsNull() {
            addCriterion("call_times is null");
            return (Criteria) this;
        }

        public Criteria andCallTimesIsNotNull() {
            addCriterion("call_times is not null");
            return (Criteria) this;
        }

        public Criteria andCallTimesEqualTo(String value) {
            addCriterion("call_times =", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesNotEqualTo(String value) {
            addCriterion("call_times <>", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesGreaterThan(String value) {
            addCriterion("call_times >", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesGreaterThanOrEqualTo(String value) {
            addCriterion("call_times >=", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesLessThan(String value) {
            addCriterion("call_times <", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesLessThanOrEqualTo(String value) {
            addCriterion("call_times <=", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesLike(String value) {
            addCriterion("call_times like", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesNotLike(String value) {
            addCriterion("call_times not like", value, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesIn(List<String> values) {
            addCriterion("call_times in", values, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesNotIn(List<String> values) {
            addCriterion("call_times not in", values, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesBetween(String value1, String value2) {
            addCriterion("call_times between", value1, value2, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallTimesNotBetween(String value1, String value2) {
            addCriterion("call_times not between", value1, value2, "callTimes");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreIsNull() {
            addCriterion("call_access_score is null");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreIsNotNull() {
            addCriterion("call_access_score is not null");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreEqualTo(String value) {
            addCriterion("call_access_score =", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreNotEqualTo(String value) {
            addCriterion("call_access_score <>", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreGreaterThan(String value) {
            addCriterion("call_access_score >", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreGreaterThanOrEqualTo(String value) {
            addCriterion("call_access_score >=", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreLessThan(String value) {
            addCriterion("call_access_score <", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreLessThanOrEqualTo(String value) {
            addCriterion("call_access_score <=", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreLike(String value) {
            addCriterion("call_access_score like", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreNotLike(String value) {
            addCriterion("call_access_score not like", value, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreIn(List<String> values) {
            addCriterion("call_access_score in", values, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreNotIn(List<String> values) {
            addCriterion("call_access_score not in", values, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreBetween(String value1, String value2) {
            addCriterion("call_access_score between", value1, value2, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andCallAccessScoreNotBetween(String value1, String value2) {
            addCriterion("call_access_score not between", value1, value2, "callAccessScore");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("remark is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("remark is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("remark =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("remark <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("remark >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("remark >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("remark <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("remark <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("remark like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("remark not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("remark in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("remark not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("remark between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("remark not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andGradeIsNull() {
            addCriterion("grade is null");
            return (Criteria) this;
        }

        public Criteria andGradeIsNotNull() {
            addCriterion("grade is not null");
            return (Criteria) this;
        }

        public Criteria andGradeEqualTo(String value) {
            addCriterion("grade =", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeNotEqualTo(String value) {
            addCriterion("grade <>", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeGreaterThan(String value) {
            addCriterion("grade >", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeGreaterThanOrEqualTo(String value) {
            addCriterion("grade >=", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeLessThan(String value) {
            addCriterion("grade <", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeLessThanOrEqualTo(String value) {
            addCriterion("grade <=", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeLike(String value) {
            addCriterion("grade like", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeNotLike(String value) {
            addCriterion("grade not like", value, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeIn(List<String> values) {
            addCriterion("grade in", values, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeNotIn(List<String> values) {
            addCriterion("grade not in", values, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeBetween(String value1, String value2) {
            addCriterion("grade between", value1, value2, "grade");
            return (Criteria) this;
        }

        public Criteria andGradeNotBetween(String value1, String value2) {
            addCriterion("grade not between", value1, value2, "grade");
            return (Criteria) this;
        }

        public Criteria andTotalAmountIsNull() {
            addCriterion("total_amount is null");
            return (Criteria) this;
        }

        public Criteria andTotalAmountIsNotNull() {
            addCriterion("total_amount is not null");
            return (Criteria) this;
        }

        public Criteria andTotalAmountEqualTo(String value) {
            addCriterion("total_amount =", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountNotEqualTo(String value) {
            addCriterion("total_amount <>", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountGreaterThan(String value) {
            addCriterion("total_amount >", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountGreaterThanOrEqualTo(String value) {
            addCriterion("total_amount >=", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountLessThan(String value) {
            addCriterion("total_amount <", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountLessThanOrEqualTo(String value) {
            addCriterion("total_amount <=", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountLike(String value) {
            addCriterion("total_amount like", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountNotLike(String value) {
            addCriterion("total_amount not like", value, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountIn(List<String> values) {
            addCriterion("total_amount in", values, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountNotIn(List<String> values) {
            addCriterion("total_amount not in", values, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountBetween(String value1, String value2) {
            addCriterion("total_amount between", value1, value2, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andTotalAmountNotBetween(String value1, String value2) {
            addCriterion("total_amount not between", value1, value2, "totalAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountIsNull() {
            addCriterion("surplus_amount is null");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountIsNotNull() {
            addCriterion("surplus_amount is not null");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountEqualTo(String value) {
            addCriterion("surplus_amount =", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountNotEqualTo(String value) {
            addCriterion("surplus_amount <>", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountGreaterThan(String value) {
            addCriterion("surplus_amount >", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountGreaterThanOrEqualTo(String value) {
            addCriterion("surplus_amount >=", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountLessThan(String value) {
            addCriterion("surplus_amount <", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountLessThanOrEqualTo(String value) {
            addCriterion("surplus_amount <=", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountLike(String value) {
            addCriterion("surplus_amount like", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountNotLike(String value) {
            addCriterion("surplus_amount not like", value, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountIn(List<String> values) {
            addCriterion("surplus_amount in", values, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountNotIn(List<String> values) {
            addCriterion("surplus_amount not in", values, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountBetween(String value1, String value2) {
            addCriterion("surplus_amount between", value1, value2, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andSurplusAmountNotBetween(String value1, String value2) {
            addCriterion("surplus_amount not between", value1, value2, "surplusAmount");
            return (Criteria) this;
        }

        public Criteria andPidIsNull() {
            addCriterion("pid is null");
            return (Criteria) this;
        }

        public Criteria andPidIsNotNull() {
            addCriterion("pid is not null");
            return (Criteria) this;
        }

        public Criteria andPidEqualTo(String value) {
            addCriterion("pid =", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotEqualTo(String value) {
            addCriterion("pid <>", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidGreaterThan(String value) {
            addCriterion("pid >", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidGreaterThanOrEqualTo(String value) {
            addCriterion("pid >=", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidLessThan(String value) {
            addCriterion("pid <", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidLessThanOrEqualTo(String value) {
            addCriterion("pid <=", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidLike(String value) {
            addCriterion("pid like", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotLike(String value) {
            addCriterion("pid not like", value, "pid");
            return (Criteria) this;
        }

        public Criteria andPidIn(List<String> values) {
            addCriterion("pid in", values, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotIn(List<String> values) {
            addCriterion("pid not in", values, "pid");
            return (Criteria) this;
        }

        public Criteria andPidBetween(String value1, String value2) {
            addCriterion("pid between", value1, value2, "pid");
            return (Criteria) this;
        }

        public Criteria andPidNotBetween(String value1, String value2) {
            addCriterion("pid not between", value1, value2, "pid");
            return (Criteria) this;
        }

        public Criteria andPchannelIsNull() {
            addCriterion("pchannel is null");
            return (Criteria) this;
        }

        public Criteria andPchannelIsNotNull() {
            addCriterion("pchannel is not null");
            return (Criteria) this;
        }

        public Criteria andPchannelEqualTo(String value) {
            addCriterion("pchannel =", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelNotEqualTo(String value) {
            addCriterion("pchannel <>", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelGreaterThan(String value) {
            addCriterion("pchannel >", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelGreaterThanOrEqualTo(String value) {
            addCriterion("pchannel >=", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelLessThan(String value) {
            addCriterion("pchannel <", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelLessThanOrEqualTo(String value) {
            addCriterion("pchannel <=", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelLike(String value) {
            addCriterion("pchannel like", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelNotLike(String value) {
            addCriterion("pchannel not like", value, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelIn(List<String> values) {
            addCriterion("pchannel in", values, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelNotIn(List<String> values) {
            addCriterion("pchannel not in", values, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelBetween(String value1, String value2) {
            addCriterion("pchannel between", value1, value2, "pchannel");
            return (Criteria) this;
        }

        public Criteria andPchannelNotBetween(String value1, String value2) {
            addCriterion("pchannel not between", value1, value2, "pchannel");
            return (Criteria) this;
        }

        public Criteria andChannelNameIsNull() {
            addCriterion("channel_name is null");
            return (Criteria) this;
        }

        public Criteria andChannelNameIsNotNull() {
            addCriterion("channel_name is not null");
            return (Criteria) this;
        }

        public Criteria andChannelNameEqualTo(String value) {
            addCriterion("channel_name =", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameNotEqualTo(String value) {
            addCriterion("channel_name <>", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameGreaterThan(String value) {
            addCriterion("channel_name >", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameGreaterThanOrEqualTo(String value) {
            addCriterion("channel_name >=", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameLessThan(String value) {
            addCriterion("channel_name <", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameLessThanOrEqualTo(String value) {
            addCriterion("channel_name <=", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameLike(String value) {
            addCriterion("channel_name like", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameNotLike(String value) {
            addCriterion("channel_name not like", value, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameIn(List<String> values) {
            addCriterion("channel_name in", values, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameNotIn(List<String> values) {
            addCriterion("channel_name not in", values, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameBetween(String value1, String value2) {
            addCriterion("channel_name between", value1, value2, "channelName");
            return (Criteria) this;
        }

        public Criteria andChannelNameNotBetween(String value1, String value2) {
            addCriterion("channel_name not between", value1, value2, "channelName");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeIsNull() {
            addCriterion("market_purpose is null");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeIsNotNull() {
            addCriterion("market_purpose is not null");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeEqualTo(String value) {
            addCriterion("market_purpose =", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeNotEqualTo(String value) {
            addCriterion("market_purpose <>", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeGreaterThan(String value) {
            addCriterion("market_purpose >", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeGreaterThanOrEqualTo(String value) {
            addCriterion("market_purpose >=", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeLessThan(String value) {
            addCriterion("market_purpose <", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeLessThanOrEqualTo(String value) {
            addCriterion("market_purpose <=", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeLike(String value) {
            addCriterion("market_purpose like", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeNotLike(String value) {
            addCriterion("market_purpose not like", value, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeIn(List<String> values) {
            addCriterion("market_purpose in", values, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeNotIn(List<String> values) {
            addCriterion("market_purpose not in", values, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeBetween(String value1, String value2) {
            addCriterion("market_purpose between", value1, value2, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andMarketPurposeNotBetween(String value1, String value2) {
            addCriterion("market_purpose not between", value1, value2, "marketPurpose");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelIsNull() {
            addCriterion("risk_control_label is null");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelIsNotNull() {
            addCriterion("risk_control_label is not null");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelEqualTo(String value) {
            addCriterion("risk_control_label =", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelNotEqualTo(String value) {
            addCriterion("risk_control_label <>", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelGreaterThan(String value) {
            addCriterion("risk_control_label >", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelGreaterThanOrEqualTo(String value) {
            addCriterion("risk_control_label >=", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelLessThan(String value) {
            addCriterion("risk_control_label <", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelLessThanOrEqualTo(String value) {
            addCriterion("risk_control_label <=", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelLike(String value) {
            addCriterion("risk_control_label like", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelNotLike(String value) {
            addCriterion("risk_control_label not like", value, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelIn(List<String> values) {
            addCriterion("risk_control_label in", values, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelNotIn(List<String> values) {
            addCriterion("risk_control_label not in", values, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelBetween(String value1, String value2) {
            addCriterion("risk_control_label between", value1, value2, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andRiskControlLabelNotBetween(String value1, String value2) {
            addCriterion("risk_control_label not between", value1, value2, "riskControlLabel");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrIsNull() {
            addCriterion("first_login_time_str is null");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrIsNotNull() {
            addCriterion("first_login_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrEqualTo(String value) {
            addCriterion("first_login_time_str =", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrNotEqualTo(String value) {
            addCriterion("first_login_time_str <>", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrGreaterThan(String value) {
            addCriterion("first_login_time_str >", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("first_login_time_str >=", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrLessThan(String value) {
            addCriterion("first_login_time_str <", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrLessThanOrEqualTo(String value) {
            addCriterion("first_login_time_str <=", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrLike(String value) {
            addCriterion("first_login_time_str like", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrNotLike(String value) {
            addCriterion("first_login_time_str not like", value, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrIn(List<String> values) {
            addCriterion("first_login_time_str in", values, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrNotIn(List<String> values) {
            addCriterion("first_login_time_str not in", values, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrBetween(String value1, String value2) {
            addCriterion("first_login_time_str between", value1, value2, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andFirstLoginTimeStrNotBetween(String value1, String value2) {
            addCriterion("first_login_time_str not between", value1, value2, "firstLoginTimeStr");
            return (Criteria) this;
        }

        public Criteria andPlanIdIsNull() {
            addCriterion("plan_id is null");
            return (Criteria) this;
        }

        public Criteria andPlanIdIsNotNull() {
            addCriterion("plan_id is not null");
            return (Criteria) this;
        }

        public Criteria andPlanIdEqualTo(String value) {
            addCriterion("plan_id =", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdNotEqualTo(String value) {
            addCriterion("plan_id <>", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdGreaterThan(String value) {
            addCriterion("plan_id >", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdGreaterThanOrEqualTo(String value) {
            addCriterion("plan_id >=", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdLessThan(String value) {
            addCriterion("plan_id <", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdLessThanOrEqualTo(String value) {
            addCriterion("plan_id <=", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdLike(String value) {
            addCriterion("plan_id like", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdNotLike(String value) {
            addCriterion("plan_id not like", value, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdIn(List<String> values) {
            addCriterion("plan_id in", values, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdNotIn(List<String> values) {
            addCriterion("plan_id not in", values, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdBetween(String value1, String value2) {
            addCriterion("plan_id between", value1, value2, "planId");
            return (Criteria) this;
        }

        public Criteria andPlanIdNotBetween(String value1, String value2) {
            addCriterion("plan_id not between", value1, value2, "planId");
            return (Criteria) this;
        }

        public Criteria andGoalsAppIsNull() {
            addCriterion("goals_app is null");
            return (Criteria) this;
        }

        public Criteria andGoalsAppIsNotNull() {
            addCriterion("goals_app is not null");
            return (Criteria) this;
        }

        public Criteria andGoalsAppEqualTo(String value) {
            addCriterion("goals_app =", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppNotEqualTo(String value) {
            addCriterion("goals_app <>", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppGreaterThan(String value) {
            addCriterion("goals_app >", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppGreaterThanOrEqualTo(String value) {
            addCriterion("goals_app >=", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppLessThan(String value) {
            addCriterion("goals_app <", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppLessThanOrEqualTo(String value) {
            addCriterion("goals_app <=", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppLike(String value) {
            addCriterion("goals_app like", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppNotLike(String value) {
            addCriterion("goals_app not like", value, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppIn(List<String> values) {
            addCriterion("goals_app in", values, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppNotIn(List<String> values) {
            addCriterion("goals_app not in", values, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppBetween(String value1, String value2) {
            addCriterion("goals_app between", value1, value2, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andGoalsAppNotBetween(String value1, String value2) {
            addCriterion("goals_app not between", value1, value2, "goalsApp");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameIsNull() {
            addCriterion("flow_side_name is null");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameIsNotNull() {
            addCriterion("flow_side_name is not null");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameEqualTo(String value) {
            addCriterion("flow_side_name =", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameNotEqualTo(String value) {
            addCriterion("flow_side_name <>", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameGreaterThan(String value) {
            addCriterion("flow_side_name >", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameGreaterThanOrEqualTo(String value) {
            addCriterion("flow_side_name >=", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameLessThan(String value) {
            addCriterion("flow_side_name <", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameLessThanOrEqualTo(String value) {
            addCriterion("flow_side_name <=", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameLike(String value) {
            addCriterion("flow_side_name like", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameNotLike(String value) {
            addCriterion("flow_side_name not like", value, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameIn(List<String> values) {
            addCriterion("flow_side_name in", values, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameNotIn(List<String> values) {
            addCriterion("flow_side_name not in", values, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameBetween(String value1, String value2) {
            addCriterion("flow_side_name between", value1, value2, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSideNameNotBetween(String value1, String value2) {
            addCriterion("flow_side_name not between", value1, value2, "flowSideName");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathIsNull() {
            addCriterion("flow_side_path is null");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathIsNotNull() {
            addCriterion("flow_side_path is not null");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathEqualTo(String value) {
            addCriterion("flow_side_path =", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathNotEqualTo(String value) {
            addCriterion("flow_side_path <>", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathGreaterThan(String value) {
            addCriterion("flow_side_path >", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathGreaterThanOrEqualTo(String value) {
            addCriterion("flow_side_path >=", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathLessThan(String value) {
            addCriterion("flow_side_path <", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathLessThanOrEqualTo(String value) {
            addCriterion("flow_side_path <=", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathLike(String value) {
            addCriterion("flow_side_path like", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathNotLike(String value) {
            addCriterion("flow_side_path not like", value, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathIn(List<String> values) {
            addCriterion("flow_side_path in", values, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathNotIn(List<String> values) {
            addCriterion("flow_side_path not in", values, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathBetween(String value1, String value2) {
            addCriterion("flow_side_path between", value1, value2, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andFlowSidePathNotBetween(String value1, String value2) {
            addCriterion("flow_side_path not between", value1, value2, "flowSidePath");
            return (Criteria) this;
        }

        public Criteria andCusTagIsNull() {
            addCriterion("cus_tag is null");
            return (Criteria) this;
        }

        public Criteria andCusTagIsNotNull() {
            addCriterion("cus_tag is not null");
            return (Criteria) this;
        }

        public Criteria andCusTagEqualTo(String value) {
            addCriterion("cus_tag =", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagNotEqualTo(String value) {
            addCriterion("cus_tag <>", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagGreaterThan(String value) {
            addCriterion("cus_tag >", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagGreaterThanOrEqualTo(String value) {
            addCriterion("cus_tag >=", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagLessThan(String value) {
            addCriterion("cus_tag <", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagLessThanOrEqualTo(String value) {
            addCriterion("cus_tag <=", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagLike(String value) {
            addCriterion("cus_tag like", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagNotLike(String value) {
            addCriterion("cus_tag not like", value, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagIn(List<String> values) {
            addCriterion("cus_tag in", values, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagNotIn(List<String> values) {
            addCriterion("cus_tag not in", values, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagBetween(String value1, String value2) {
            addCriterion("cus_tag between", value1, value2, "cusTag");
            return (Criteria) this;
        }

        public Criteria andCusTagNotBetween(String value1, String value2) {
            addCriterion("cus_tag not between", value1, value2, "cusTag");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrIsNull() {
            addCriterion("abgroup_push_offset_str is null");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrIsNotNull() {
            addCriterion("abgroup_push_offset_str is not null");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrEqualTo(String value) {
            addCriterion("abgroup_push_offset_str =", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrNotEqualTo(String value) {
            addCriterion("abgroup_push_offset_str <>", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrGreaterThan(String value) {
            addCriterion("abgroup_push_offset_str >", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrGreaterThanOrEqualTo(String value) {
            addCriterion("abgroup_push_offset_str >=", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrLessThan(String value) {
            addCriterion("abgroup_push_offset_str <", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrLessThanOrEqualTo(String value) {
            addCriterion("abgroup_push_offset_str <=", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrLike(String value) {
            addCriterion("abgroup_push_offset_str like", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrNotLike(String value) {
            addCriterion("abgroup_push_offset_str not like", value, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrIn(List<String> values) {
            addCriterion("abgroup_push_offset_str in", values, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrNotIn(List<String> values) {
            addCriterion("abgroup_push_offset_str not in", values, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrBetween(String value1, String value2) {
            addCriterion("abgroup_push_offset_str between", value1, value2, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andAbgroupPushOffsetStrNotBetween(String value1, String value2) {
            addCriterion("abgroup_push_offset_str not between", value1, value2, "abgroupPushOffsetStr");
            return (Criteria) this;
        }

        public Criteria andExtra1IsNull() {
            addCriterion("extra1 is null");
            return (Criteria) this;
        }

        public Criteria andExtra1IsNotNull() {
            addCriterion("extra1 is not null");
            return (Criteria) this;
        }

        public Criteria andExtra1EqualTo(String value) {
            addCriterion("extra1 =", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1NotEqualTo(String value) {
            addCriterion("extra1 <>", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1GreaterThan(String value) {
            addCriterion("extra1 >", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1GreaterThanOrEqualTo(String value) {
            addCriterion("extra1 >=", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1LessThan(String value) {
            addCriterion("extra1 <", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1LessThanOrEqualTo(String value) {
            addCriterion("extra1 <=", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1Like(String value) {
            addCriterion("extra1 like", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1NotLike(String value) {
            addCriterion("extra1 not like", value, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1In(List<String> values) {
            addCriterion("extra1 in", values, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1NotIn(List<String> values) {
            addCriterion("extra1 not in", values, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1Between(String value1, String value2) {
            addCriterion("extra1 between", value1, value2, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra1NotBetween(String value1, String value2) {
            addCriterion("extra1 not between", value1, value2, "extra1");
            return (Criteria) this;
        }

        public Criteria andExtra2IsNull() {
            addCriterion("extra2 is null");
            return (Criteria) this;
        }

        public Criteria andExtra2IsNotNull() {
            addCriterion("extra2 is not null");
            return (Criteria) this;
        }

        public Criteria andExtra2EqualTo(String value) {
            addCriterion("extra2 =", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2NotEqualTo(String value) {
            addCriterion("extra2 <>", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2GreaterThan(String value) {
            addCriterion("extra2 >", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2GreaterThanOrEqualTo(String value) {
            addCriterion("extra2 >=", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2LessThan(String value) {
            addCriterion("extra2 <", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2LessThanOrEqualTo(String value) {
            addCriterion("extra2 <=", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2Like(String value) {
            addCriterion("extra2 like", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2NotLike(String value) {
            addCriterion("extra2 not like", value, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2In(List<String> values) {
            addCriterion("extra2 in", values, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2NotIn(List<String> values) {
            addCriterion("extra2 not in", values, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2Between(String value1, String value2) {
            addCriterion("extra2 between", value1, value2, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra2NotBetween(String value1, String value2) {
            addCriterion("extra2 not between", value1, value2, "extra2");
            return (Criteria) this;
        }

        public Criteria andExtra3IsNull() {
            addCriterion("extra3 is null");
            return (Criteria) this;
        }

        public Criteria andExtra3IsNotNull() {
            addCriterion("extra3 is not null");
            return (Criteria) this;
        }

        public Criteria andExtra3EqualTo(String value) {
            addCriterion("extra3 =", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3NotEqualTo(String value) {
            addCriterion("extra3 <>", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3GreaterThan(String value) {
            addCriterion("extra3 >", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3GreaterThanOrEqualTo(String value) {
            addCriterion("extra3 >=", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3LessThan(String value) {
            addCriterion("extra3 <", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3LessThanOrEqualTo(String value) {
            addCriterion("extra3 <=", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3Like(String value) {
            addCriterion("extra3 like", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3NotLike(String value) {
            addCriterion("extra3 not like", value, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3In(List<String> values) {
            addCriterion("extra3 in", values, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3NotIn(List<String> values) {
            addCriterion("extra3 not in", values, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3Between(String value1, String value2) {
            addCriterion("extra3 between", value1, value2, "extra3");
            return (Criteria) this;
        }

        public Criteria andExtra3NotBetween(String value1, String value2) {
            addCriterion("extra3 not between", value1, value2, "extra3");
            return (Criteria) this;
        }

        public Criteria andReserveField1IsNull() {
            addCriterion("reserve_field1 is null");
            return (Criteria) this;
        }

        public Criteria andReserveField1IsNotNull() {
            addCriterion("reserve_field1 is not null");
            return (Criteria) this;
        }

        public Criteria andReserveField1EqualTo(String value) {
            addCriterion("reserve_field1 =", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotEqualTo(String value) {
            addCriterion("reserve_field1 <>", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1GreaterThan(String value) {
            addCriterion("reserve_field1 >", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1GreaterThanOrEqualTo(String value) {
            addCriterion("reserve_field1 >=", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1LessThan(String value) {
            addCriterion("reserve_field1 <", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1LessThanOrEqualTo(String value) {
            addCriterion("reserve_field1 <=", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1Like(String value) {
            addCriterion("reserve_field1 like", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotLike(String value) {
            addCriterion("reserve_field1 not like", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1In(List<String> values) {
            addCriterion("reserve_field1 in", values, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotIn(List<String> values) {
            addCriterion("reserve_field1 not in", values, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1Between(String value1, String value2) {
            addCriterion("reserve_field1 between", value1, value2, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotBetween(String value1, String value2) {
            addCriterion("reserve_field1 not between", value1, value2, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrIsNull() {
            addCriterion("credit_time_str is null");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrIsNotNull() {
            addCriterion("credit_time_str is not null");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrEqualTo(String value) {
            addCriterion("credit_time_str =", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrNotEqualTo(String value) {
            addCriterion("credit_time_str <>", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrGreaterThan(String value) {
            addCriterion("credit_time_str >", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrGreaterThanOrEqualTo(String value) {
            addCriterion("credit_time_str >=", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrLessThan(String value) {
            addCriterion("credit_time_str <", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrLessThanOrEqualTo(String value) {
            addCriterion("credit_time_str <=", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrLike(String value) {
            addCriterion("credit_time_str like", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrNotLike(String value) {
            addCriterion("credit_time_str not like", value, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrIn(List<String> values) {
            addCriterion("credit_time_str in", values, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrNotIn(List<String> values) {
            addCriterion("credit_time_str not in", values, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrBetween(String value1, String value2) {
            addCriterion("credit_time_str between", value1, value2, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditTimeStrNotBetween(String value1, String value2) {
            addCriterion("credit_time_str not between", value1, value2, "creditTimeStr");
            return (Criteria) this;
        }

        public Criteria andCreditChannelIsNull() {
            addCriterion("credit_channel is null");
            return (Criteria) this;
        }

        public Criteria andCreditChannelIsNotNull() {
            addCriterion("credit_channel is not null");
            return (Criteria) this;
        }

        public Criteria andCreditChannelEqualTo(String value) {
            addCriterion("credit_channel =", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelNotEqualTo(String value) {
            addCriterion("credit_channel <>", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelGreaterThan(String value) {
            addCriterion("credit_channel >", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelGreaterThanOrEqualTo(String value) {
            addCriterion("credit_channel >=", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelLessThan(String value) {
            addCriterion("credit_channel <", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelLessThanOrEqualTo(String value) {
            addCriterion("credit_channel <=", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelLike(String value) {
            addCriterion("credit_channel like", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelNotLike(String value) {
            addCriterion("credit_channel not like", value, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelIn(List<String> values) {
            addCriterion("credit_channel in", values, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelNotIn(List<String> values) {
            addCriterion("credit_channel not in", values, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelBetween(String value1, String value2) {
            addCriterion("credit_channel between", value1, value2, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andCreditChannelNotBetween(String value1, String value2) {
            addCriterion("credit_channel not between", value1, value2, "creditChannel");
            return (Criteria) this;
        }

        public Criteria andAmountStatusIsNull() {
            addCriterion("amount_status is null");
            return (Criteria) this;
        }

        public Criteria andAmountStatusIsNotNull() {
            addCriterion("amount_status is not null");
            return (Criteria) this;
        }

        public Criteria andAmountStatusEqualTo(String value) {
            addCriterion("amount_status =", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusNotEqualTo(String value) {
            addCriterion("amount_status <>", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusGreaterThan(String value) {
            addCriterion("amount_status >", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusGreaterThanOrEqualTo(String value) {
            addCriterion("amount_status >=", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusLessThan(String value) {
            addCriterion("amount_status <", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusLessThanOrEqualTo(String value) {
            addCriterion("amount_status <=", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusLike(String value) {
            addCriterion("amount_status like", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusNotLike(String value) {
            addCriterion("amount_status not like", value, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusIn(List<String> values) {
            addCriterion("amount_status in", values, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusNotIn(List<String> values) {
            addCriterion("amount_status not in", values, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusBetween(String value1, String value2) {
            addCriterion("amount_status between", value1, value2, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andAmountStatusNotBetween(String value1, String value2) {
            addCriterion("amount_status not between", value1, value2, "amountStatus");
            return (Criteria) this;
        }

        public Criteria andConnectTimesIsNull() {
            addCriterion("connect_times is null");
            return (Criteria) this;
        }

        public Criteria andConnectTimesIsNotNull() {
            addCriterion("connect_times is not null");
            return (Criteria) this;
        }

        public Criteria andConnectTimesEqualTo(String value) {
            addCriterion("connect_times =", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesNotEqualTo(String value) {
            addCriterion("connect_times <>", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesGreaterThan(String value) {
            addCriterion("connect_times >", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesGreaterThanOrEqualTo(String value) {
            addCriterion("connect_times >=", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesLessThan(String value) {
            addCriterion("connect_times <", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesLessThanOrEqualTo(String value) {
            addCriterion("connect_times <=", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesLike(String value) {
            addCriterion("connect_times like", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesNotLike(String value) {
            addCriterion("connect_times not like", value, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesIn(List<String> values) {
            addCriterion("connect_times in", values, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesNotIn(List<String> values) {
            addCriterion("connect_times not in", values, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesBetween(String value1, String value2) {
            addCriterion("connect_times between", value1, value2, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andConnectTimesNotBetween(String value1, String value2) {
            addCriterion("connect_times not between", value1, value2, "connectTimes");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagIsNull() {
            addCriterion("zy_apply_flag is null");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagIsNotNull() {
            addCriterion("zy_apply_flag is not null");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagEqualTo(String value) {
            addCriterion("zy_apply_flag =", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagNotEqualTo(String value) {
            addCriterion("zy_apply_flag <>", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagGreaterThan(String value) {
            addCriterion("zy_apply_flag >", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagGreaterThanOrEqualTo(String value) {
            addCriterion("zy_apply_flag >=", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagLessThan(String value) {
            addCriterion("zy_apply_flag <", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagLessThanOrEqualTo(String value) {
            addCriterion("zy_apply_flag <=", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagLike(String value) {
            addCriterion("zy_apply_flag like", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagNotLike(String value) {
            addCriterion("zy_apply_flag not like", value, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagIn(List<String> values) {
            addCriterion("zy_apply_flag in", values, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagNotIn(List<String> values) {
            addCriterion("zy_apply_flag not in", values, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagBetween(String value1, String value2) {
            addCriterion("zy_apply_flag between", value1, value2, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplyFlagNotBetween(String value1, String value2) {
            addCriterion("zy_apply_flag not between", value1, value2, "zyApplyFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagIsNull() {
            addCriterion("zy_apply_success_flag is null");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagIsNotNull() {
            addCriterion("zy_apply_success_flag is not null");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagEqualTo(String value) {
            addCriterion("zy_apply_success_flag =", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagNotEqualTo(String value) {
            addCriterion("zy_apply_success_flag <>", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagGreaterThan(String value) {
            addCriterion("zy_apply_success_flag >", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagGreaterThanOrEqualTo(String value) {
            addCriterion("zy_apply_success_flag >=", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagLessThan(String value) {
            addCriterion("zy_apply_success_flag <", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagLessThanOrEqualTo(String value) {
            addCriterion("zy_apply_success_flag <=", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagLike(String value) {
            addCriterion("zy_apply_success_flag like", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagNotLike(String value) {
            addCriterion("zy_apply_success_flag not like", value, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagIn(List<String> values) {
            addCriterion("zy_apply_success_flag in", values, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagNotIn(List<String> values) {
            addCriterion("zy_apply_success_flag not in", values, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagBetween(String value1, String value2) {
            addCriterion("zy_apply_success_flag between", value1, value2, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyApplySuccessFlagNotBetween(String value1, String value2) {
            addCriterion("zy_apply_success_flag not between", value1, value2, "zyApplySuccessFlag");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusIsNull() {
            addCriterion("zy_amount_status is null");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusIsNotNull() {
            addCriterion("zy_amount_status is not null");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusEqualTo(String value) {
            addCriterion("zy_amount_status =", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusNotEqualTo(String value) {
            addCriterion("zy_amount_status <>", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusGreaterThan(String value) {
            addCriterion("zy_amount_status >", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusGreaterThanOrEqualTo(String value) {
            addCriterion("zy_amount_status >=", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusLessThan(String value) {
            addCriterion("zy_amount_status <", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusLessThanOrEqualTo(String value) {
            addCriterion("zy_amount_status <=", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusLike(String value) {
            addCriterion("zy_amount_status like", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusNotLike(String value) {
            addCriterion("zy_amount_status not like", value, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusIn(List<String> values) {
            addCriterion("zy_amount_status in", values, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusNotIn(List<String> values) {
            addCriterion("zy_amount_status not in", values, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusBetween(String value1, String value2) {
            addCriterion("zy_amount_status between", value1, value2, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyAmountStatusNotBetween(String value1, String value2) {
            addCriterion("zy_amount_status not between", value1, value2, "zyAmountStatus");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountIsNull() {
            addCriterion("zy_total_usable_amount is null");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountIsNotNull() {
            addCriterion("zy_total_usable_amount is not null");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountEqualTo(String value) {
            addCriterion("zy_total_usable_amount =", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountNotEqualTo(String value) {
            addCriterion("zy_total_usable_amount <>", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountGreaterThan(String value) {
            addCriterion("zy_total_usable_amount >", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountGreaterThanOrEqualTo(String value) {
            addCriterion("zy_total_usable_amount >=", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountLessThan(String value) {
            addCriterion("zy_total_usable_amount <", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountLessThanOrEqualTo(String value) {
            addCriterion("zy_total_usable_amount <=", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountLike(String value) {
            addCriterion("zy_total_usable_amount like", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountNotLike(String value) {
            addCriterion("zy_total_usable_amount not like", value, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountIn(List<String> values) {
            addCriterion("zy_total_usable_amount in", values, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountNotIn(List<String> values) {
            addCriterion("zy_total_usable_amount not in", values, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountBetween(String value1, String value2) {
            addCriterion("zy_total_usable_amount between", value1, value2, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andZyTotalUsableAmountNotBetween(String value1, String value2) {
            addCriterion("zy_total_usable_amount not between", value1, value2, "zyTotalUsableAmount");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberIsNull() {
            addCriterion("is_idnumber is null");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberIsNotNull() {
            addCriterion("is_idnumber is not null");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberEqualTo(String value) {
            addCriterion("is_idnumber =", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberNotEqualTo(String value) {
            addCriterion("is_idnumber <>", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberGreaterThan(String value) {
            addCriterion("is_idnumber >", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberGreaterThanOrEqualTo(String value) {
            addCriterion("is_idnumber >=", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberLessThan(String value) {
            addCriterion("is_idnumber <", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberLessThanOrEqualTo(String value) {
            addCriterion("is_idnumber <=", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberLike(String value) {
            addCriterion("is_idnumber like", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberNotLike(String value) {
            addCriterion("is_idnumber not like", value, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberIn(List<String> values) {
            addCriterion("is_idnumber in", values, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberNotIn(List<String> values) {
            addCriterion("is_idnumber not in", values, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberBetween(String value1, String value2) {
            addCriterion("is_idnumber between", value1, value2, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsIdnumberNotBetween(String value1, String value2) {
            addCriterion("is_idnumber not between", value1, value2, "isIdnumber");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoIsNull() {
            addCriterion("is_taobao is null");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoIsNotNull() {
            addCriterion("is_taobao is not null");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoEqualTo(String value) {
            addCriterion("is_taobao =", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoNotEqualTo(String value) {
            addCriterion("is_taobao <>", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoGreaterThan(String value) {
            addCriterion("is_taobao >", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoGreaterThanOrEqualTo(String value) {
            addCriterion("is_taobao >=", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoLessThan(String value) {
            addCriterion("is_taobao <", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoLessThanOrEqualTo(String value) {
            addCriterion("is_taobao <=", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoLike(String value) {
            addCriterion("is_taobao like", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoNotLike(String value) {
            addCriterion("is_taobao not like", value, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoIn(List<String> values) {
            addCriterion("is_taobao in", values, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoNotIn(List<String> values) {
            addCriterion("is_taobao not in", values, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoBetween(String value1, String value2) {
            addCriterion("is_taobao between", value1, value2, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsTaobaoNotBetween(String value1, String value2) {
            addCriterion("is_taobao not between", value1, value2, "isTaobao");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalIsNull() {
            addCriterion("is_nuclearapproval is null");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalIsNotNull() {
            addCriterion("is_nuclearapproval is not null");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalEqualTo(String value) {
            addCriterion("is_nuclearapproval =", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalNotEqualTo(String value) {
            addCriterion("is_nuclearapproval <>", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalGreaterThan(String value) {
            addCriterion("is_nuclearapproval >", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalGreaterThanOrEqualTo(String value) {
            addCriterion("is_nuclearapproval >=", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalLessThan(String value) {
            addCriterion("is_nuclearapproval <", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalLessThanOrEqualTo(String value) {
            addCriterion("is_nuclearapproval <=", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalLike(String value) {
            addCriterion("is_nuclearapproval like", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalNotLike(String value) {
            addCriterion("is_nuclearapproval not like", value, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalIn(List<String> values) {
            addCriterion("is_nuclearapproval in", values, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalNotIn(List<String> values) {
            addCriterion("is_nuclearapproval not in", values, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalBetween(String value1, String value2) {
            addCriterion("is_nuclearapproval between", value1, value2, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andIsNuclearapprovalNotBetween(String value1, String value2) {
            addCriterion("is_nuclearapproval not between", value1, value2, "isNuclearapproval");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreIsNull() {
            addCriterion("callaccessscore is null");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreIsNotNull() {
            addCriterion("callaccessscore is not null");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreEqualTo(String value) {
            addCriterion("callaccessscore =", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreNotEqualTo(String value) {
            addCriterion("callaccessscore <>", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreGreaterThan(String value) {
            addCriterion("callaccessscore >", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreGreaterThanOrEqualTo(String value) {
            addCriterion("callaccessscore >=", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreLessThan(String value) {
            addCriterion("callaccessscore <", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreLessThanOrEqualTo(String value) {
            addCriterion("callaccessscore <=", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreLike(String value) {
            addCriterion("callaccessscore like", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreNotLike(String value) {
            addCriterion("callaccessscore not like", value, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreIn(List<String> values) {
            addCriterion("callaccessscore in", values, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreNotIn(List<String> values) {
            addCriterion("callaccessscore not in", values, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreBetween(String value1, String value2) {
            addCriterion("callaccessscore between", value1, value2, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andCallaccessscoreNotBetween(String value1, String value2) {
            addCriterion("callaccessscore not between", value1, value2, "callaccessscore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreIsNull() {
            addCriterion("marketing_score is null");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreIsNotNull() {
            addCriterion("marketing_score is not null");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreEqualTo(String value) {
            addCriterion("marketing_score =", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreNotEqualTo(String value) {
            addCriterion("marketing_score <>", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreGreaterThan(String value) {
            addCriterion("marketing_score >", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreGreaterThanOrEqualTo(String value) {
            addCriterion("marketing_score >=", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreLessThan(String value) {
            addCriterion("marketing_score <", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreLessThanOrEqualTo(String value) {
            addCriterion("marketing_score <=", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreLike(String value) {
            addCriterion("marketing_score like", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreNotLike(String value) {
            addCriterion("marketing_score not like", value, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreIn(List<String> values) {
            addCriterion("marketing_score in", values, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreNotIn(List<String> values) {
            addCriterion("marketing_score not in", values, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreBetween(String value1, String value2) {
            addCriterion("marketing_score between", value1, value2, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andMarketingScoreNotBetween(String value1, String value2) {
            addCriterion("marketing_score not between", value1, value2, "marketingScore");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersIsNull() {
            addCriterion("no_withdraw_orders is null");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersIsNotNull() {
            addCriterion("no_withdraw_orders is not null");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersEqualTo(String value) {
            addCriterion("no_withdraw_orders =", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersNotEqualTo(String value) {
            addCriterion("no_withdraw_orders <>", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersGreaterThan(String value) {
            addCriterion("no_withdraw_orders >", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersGreaterThanOrEqualTo(String value) {
            addCriterion("no_withdraw_orders >=", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersLessThan(String value) {
            addCriterion("no_withdraw_orders <", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersLessThanOrEqualTo(String value) {
            addCriterion("no_withdraw_orders <=", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersLike(String value) {
            addCriterion("no_withdraw_orders like", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersNotLike(String value) {
            addCriterion("no_withdraw_orders not like", value, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersIn(List<String> values) {
            addCriterion("no_withdraw_orders in", values, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersNotIn(List<String> values) {
            addCriterion("no_withdraw_orders not in", values, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersBetween(String value1, String value2) {
            addCriterion("no_withdraw_orders between", value1, value2, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andNoWithdrawOrdersNotBetween(String value1, String value2) {
            addCriterion("no_withdraw_orders not between", value1, value2, "noWithdrawOrders");
            return (Criteria) this;
        }

        public Criteria andPlanDataIsNull() {
            addCriterion("plan_data is null");
            return (Criteria) this;
        }

        public Criteria andPlanDataIsNotNull() {
            addCriterion("plan_data is not null");
            return (Criteria) this;
        }

        public Criteria andPlanDataEqualTo(String value) {
            addCriterion("plan_data =", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataNotEqualTo(String value) {
            addCriterion("plan_data <>", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataGreaterThan(String value) {
            addCriterion("plan_data >", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataGreaterThanOrEqualTo(String value) {
            addCriterion("plan_data >=", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataLessThan(String value) {
            addCriterion("plan_data <", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataLessThanOrEqualTo(String value) {
            addCriterion("plan_data <=", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataLike(String value) {
            addCriterion("plan_data like", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataNotLike(String value) {
            addCriterion("plan_data not like", value, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataIn(List<String> values) {
            addCriterion("plan_data in", values, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataNotIn(List<String> values) {
            addCriterion("plan_data not in", values, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataBetween(String value1, String value2) {
            addCriterion("plan_data between", value1, value2, "planData");
            return (Criteria) this;
        }

        public Criteria andPlanDataNotBetween(String value1, String value2) {
            addCriterion("plan_data not between", value1, value2, "planData");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreIsNull() {
            addCriterion("priority_score is null");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreIsNotNull() {
            addCriterion("priority_score is not null");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreEqualTo(String value) {
            addCriterion("priority_score =", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreNotEqualTo(String value) {
            addCriterion("priority_score <>", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreGreaterThan(String value) {
            addCriterion("priority_score >", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreGreaterThanOrEqualTo(String value) {
            addCriterion("priority_score >=", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreLessThan(String value) {
            addCriterion("priority_score <", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreLessThanOrEqualTo(String value) {
            addCriterion("priority_score <=", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreLike(String value) {
            addCriterion("priority_score like", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreNotLike(String value) {
            addCriterion("priority_score not like", value, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreIn(List<String> values) {
            addCriterion("priority_score in", values, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreNotIn(List<String> values) {
            addCriterion("priority_score not in", values, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreBetween(String value1, String value2) {
            addCriterion("priority_score between", value1, value2, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andPriorityScoreNotBetween(String value1, String value2) {
            addCriterion("priority_score not between", value1, value2, "priorityScore");
            return (Criteria) this;
        }

        public Criteria andCallTypeIsNull() {
            addCriterion("call_type is null");
            return (Criteria) this;
        }

        public Criteria andCallTypeIsNotNull() {
            addCriterion("call_type is not null");
            return (Criteria) this;
        }

        public Criteria andCallTypeEqualTo(String value) {
            addCriterion("call_type =", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeNotEqualTo(String value) {
            addCriterion("call_type <>", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeGreaterThan(String value) {
            addCriterion("call_type >", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeGreaterThanOrEqualTo(String value) {
            addCriterion("call_type >=", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeLessThan(String value) {
            addCriterion("call_type <", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeLessThanOrEqualTo(String value) {
            addCriterion("call_type <=", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeLike(String value) {
            addCriterion("call_type like", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeNotLike(String value) {
            addCriterion("call_type not like", value, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeIn(List<String> values) {
            addCriterion("call_type in", values, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeNotIn(List<String> values) {
            addCriterion("call_type not in", values, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeBetween(String value1, String value2) {
            addCriterion("call_type between", value1, value2, "callType");
            return (Criteria) this;
        }

        public Criteria andCallTypeNotBetween(String value1, String value2) {
            addCriterion("call_type not between", value1, value2, "callType");
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