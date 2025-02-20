package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CsosPhoneSaleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CsosPhoneSaleExample() {
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

        public Criteria andPhoneAesIsNull() {
            addCriterion("phone_aes is null");
            return (Criteria) this;
        }

        public Criteria andPhoneAesIsNotNull() {
            addCriterion("phone_aes is not null");
            return (Criteria) this;
        }

        public Criteria andPhoneAesEqualTo(String value) {
            addCriterion("phone_aes =", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesNotEqualTo(String value) {
            addCriterion("phone_aes <>", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesGreaterThan(String value) {
            addCriterion("phone_aes >", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesGreaterThanOrEqualTo(String value) {
            addCriterion("phone_aes >=", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesLessThan(String value) {
            addCriterion("phone_aes <", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesLessThanOrEqualTo(String value) {
            addCriterion("phone_aes <=", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesLike(String value) {
            addCriterion("phone_aes like", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesNotLike(String value) {
            addCriterion("phone_aes not like", value, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesIn(List<String> values) {
            addCriterion("phone_aes in", values, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesNotIn(List<String> values) {
            addCriterion("phone_aes not in", values, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesBetween(String value1, String value2) {
            addCriterion("phone_aes between", value1, value2, "phoneAes");
            return (Criteria) this;
        }

        public Criteria andPhoneAesNotBetween(String value1, String value2) {
            addCriterion("phone_aes not between", value1, value2, "phoneAes");
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

        public Criteria andNameAesIsNull() {
            addCriterion("name_aes is null");
            return (Criteria) this;
        }

        public Criteria andNameAesIsNotNull() {
            addCriterion("name_aes is not null");
            return (Criteria) this;
        }

        public Criteria andNameAesEqualTo(String value) {
            addCriterion("name_aes =", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesNotEqualTo(String value) {
            addCriterion("name_aes <>", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesGreaterThan(String value) {
            addCriterion("name_aes >", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesGreaterThanOrEqualTo(String value) {
            addCriterion("name_aes >=", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesLessThan(String value) {
            addCriterion("name_aes <", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesLessThanOrEqualTo(String value) {
            addCriterion("name_aes <=", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesLike(String value) {
            addCriterion("name_aes like", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesNotLike(String value) {
            addCriterion("name_aes not like", value, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesIn(List<String> values) {
            addCriterion("name_aes in", values, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesNotIn(List<String> values) {
            addCriterion("name_aes not in", values, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesBetween(String value1, String value2) {
            addCriterion("name_aes between", value1, value2, "nameAes");
            return (Criteria) this;
        }

        public Criteria andNameAesNotBetween(String value1, String value2) {
            addCriterion("name_aes not between", value1, value2, "nameAes");
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

        public Criteria andOrgnameIsNull() {
            addCriterion("orgname is null");
            return (Criteria) this;
        }

        public Criteria andOrgnameIsNotNull() {
            addCriterion("orgname is not null");
            return (Criteria) this;
        }

        public Criteria andOrgnameEqualTo(String value) {
            addCriterion("orgname =", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameNotEqualTo(String value) {
            addCriterion("orgname <>", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameGreaterThan(String value) {
            addCriterion("orgname >", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameGreaterThanOrEqualTo(String value) {
            addCriterion("orgname >=", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameLessThan(String value) {
            addCriterion("orgname <", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameLessThanOrEqualTo(String value) {
            addCriterion("orgname <=", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameLike(String value) {
            addCriterion("orgname like", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameNotLike(String value) {
            addCriterion("orgname not like", value, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameIn(List<String> values) {
            addCriterion("orgname in", values, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameNotIn(List<String> values) {
            addCriterion("orgname not in", values, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameBetween(String value1, String value2) {
            addCriterion("orgname between", value1, value2, "orgname");
            return (Criteria) this;
        }

        public Criteria andOrgnameNotBetween(String value1, String value2) {
            addCriterion("orgname not between", value1, value2, "orgname");
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

        public Criteria andHouseholdRegistrationIsNull() {
            addCriterion("household_registration is null");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationIsNotNull() {
            addCriterion("household_registration is not null");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationEqualTo(String value) {
            addCriterion("household_registration =", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationNotEqualTo(String value) {
            addCriterion("household_registration <>", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationGreaterThan(String value) {
            addCriterion("household_registration >", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationGreaterThanOrEqualTo(String value) {
            addCriterion("household_registration >=", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationLessThan(String value) {
            addCriterion("household_registration <", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationLessThanOrEqualTo(String value) {
            addCriterion("household_registration <=", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationLike(String value) {
            addCriterion("household_registration like", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationNotLike(String value) {
            addCriterion("household_registration not like", value, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationIn(List<String> values) {
            addCriterion("household_registration in", values, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationNotIn(List<String> values) {
            addCriterion("household_registration not in", values, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationBetween(String value1, String value2) {
            addCriterion("household_registration between", value1, value2, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andHouseholdRegistrationNotBetween(String value1, String value2) {
            addCriterion("household_registration not between", value1, value2, "householdRegistration");
            return (Criteria) this;
        }

        public Criteria andAgeIsNull() {
            addCriterion("age is null");
            return (Criteria) this;
        }

        public Criteria andAgeIsNotNull() {
            addCriterion("age is not null");
            return (Criteria) this;
        }

        public Criteria andAgeEqualTo(String value) {
            addCriterion("age =", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeNotEqualTo(String value) {
            addCriterion("age <>", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeGreaterThan(String value) {
            addCriterion("age >", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeGreaterThanOrEqualTo(String value) {
            addCriterion("age >=", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeLessThan(String value) {
            addCriterion("age <", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeLessThanOrEqualTo(String value) {
            addCriterion("age <=", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeLike(String value) {
            addCriterion("age like", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeNotLike(String value) {
            addCriterion("age not like", value, "age");
            return (Criteria) this;
        }

        public Criteria andAgeIn(List<String> values) {
            addCriterion("age in", values, "age");
            return (Criteria) this;
        }

        public Criteria andAgeNotIn(List<String> values) {
            addCriterion("age not in", values, "age");
            return (Criteria) this;
        }

        public Criteria andAgeBetween(String value1, String value2) {
            addCriterion("age between", value1, value2, "age");
            return (Criteria) this;
        }

        public Criteria andAgeNotBetween(String value1, String value2) {
            addCriterion("age not between", value1, value2, "age");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateIsNull() {
            addCriterion("account_open_date is null");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateIsNotNull() {
            addCriterion("account_open_date is not null");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateEqualTo(String value) {
            addCriterion("account_open_date =", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateNotEqualTo(String value) {
            addCriterion("account_open_date <>", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateGreaterThan(String value) {
            addCriterion("account_open_date >", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateGreaterThanOrEqualTo(String value) {
            addCriterion("account_open_date >=", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateLessThan(String value) {
            addCriterion("account_open_date <", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateLessThanOrEqualTo(String value) {
            addCriterion("account_open_date <=", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateLike(String value) {
            addCriterion("account_open_date like", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateNotLike(String value) {
            addCriterion("account_open_date not like", value, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateIn(List<String> values) {
            addCriterion("account_open_date in", values, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateNotIn(List<String> values) {
            addCriterion("account_open_date not in", values, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateBetween(String value1, String value2) {
            addCriterion("account_open_date between", value1, value2, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountOpenDateNotBetween(String value1, String value2) {
            addCriterion("account_open_date not between", value1, value2, "accountOpenDate");
            return (Criteria) this;
        }

        public Criteria andAccountChannelIsNull() {
            addCriterion("account_channel is null");
            return (Criteria) this;
        }

        public Criteria andAccountChannelIsNotNull() {
            addCriterion("account_channel is not null");
            return (Criteria) this;
        }

        public Criteria andAccountChannelEqualTo(String value) {
            addCriterion("account_channel =", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelNotEqualTo(String value) {
            addCriterion("account_channel <>", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelGreaterThan(String value) {
            addCriterion("account_channel >", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelGreaterThanOrEqualTo(String value) {
            addCriterion("account_channel >=", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelLessThan(String value) {
            addCriterion("account_channel <", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelLessThanOrEqualTo(String value) {
            addCriterion("account_channel <=", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelLike(String value) {
            addCriterion("account_channel like", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelNotLike(String value) {
            addCriterion("account_channel not like", value, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelIn(List<String> values) {
            addCriterion("account_channel in", values, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelNotIn(List<String> values) {
            addCriterion("account_channel not in", values, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelBetween(String value1, String value2) {
            addCriterion("account_channel between", value1, value2, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andAccountChannelNotBetween(String value1, String value2) {
            addCriterion("account_channel not between", value1, value2, "accountChannel");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppIsNull() {
            addCriterion("last_login_app is null");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppIsNotNull() {
            addCriterion("last_login_app is not null");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppEqualTo(String value) {
            addCriterion("last_login_app =", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppNotEqualTo(String value) {
            addCriterion("last_login_app <>", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppGreaterThan(String value) {
            addCriterion("last_login_app >", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppGreaterThanOrEqualTo(String value) {
            addCriterion("last_login_app >=", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppLessThan(String value) {
            addCriterion("last_login_app <", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppLessThanOrEqualTo(String value) {
            addCriterion("last_login_app <=", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppLike(String value) {
            addCriterion("last_login_app like", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppNotLike(String value) {
            addCriterion("last_login_app not like", value, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppIn(List<String> values) {
            addCriterion("last_login_app in", values, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppNotIn(List<String> values) {
            addCriterion("last_login_app not in", values, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppBetween(String value1, String value2) {
            addCriterion("last_login_app between", value1, value2, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andLastLoginAppNotBetween(String value1, String value2) {
            addCriterion("last_login_app not between", value1, value2, "lastLoginApp");
            return (Criteria) this;
        }

        public Criteria andInvestLevelIsNull() {
            addCriterion("invest_level is null");
            return (Criteria) this;
        }

        public Criteria andInvestLevelIsNotNull() {
            addCriterion("invest_level is not null");
            return (Criteria) this;
        }

        public Criteria andInvestLevelEqualTo(String value) {
            addCriterion("invest_level =", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelNotEqualTo(String value) {
            addCriterion("invest_level <>", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelGreaterThan(String value) {
            addCriterion("invest_level >", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelGreaterThanOrEqualTo(String value) {
            addCriterion("invest_level >=", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelLessThan(String value) {
            addCriterion("invest_level <", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelLessThanOrEqualTo(String value) {
            addCriterion("invest_level <=", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelLike(String value) {
            addCriterion("invest_level like", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelNotLike(String value) {
            addCriterion("invest_level not like", value, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelIn(List<String> values) {
            addCriterion("invest_level in", values, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelNotIn(List<String> values) {
            addCriterion("invest_level not in", values, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelBetween(String value1, String value2) {
            addCriterion("invest_level between", value1, value2, "investLevel");
            return (Criteria) this;
        }

        public Criteria andInvestLevelNotBetween(String value1, String value2) {
            addCriterion("invest_level not between", value1, value2, "investLevel");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreIsNull() {
            addCriterion("purchase_intent_score is null");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreIsNotNull() {
            addCriterion("purchase_intent_score is not null");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreEqualTo(String value) {
            addCriterion("purchase_intent_score =", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreNotEqualTo(String value) {
            addCriterion("purchase_intent_score <>", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreGreaterThan(String value) {
            addCriterion("purchase_intent_score >", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreGreaterThanOrEqualTo(String value) {
            addCriterion("purchase_intent_score >=", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreLessThan(String value) {
            addCriterion("purchase_intent_score <", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreLessThanOrEqualTo(String value) {
            addCriterion("purchase_intent_score <=", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreLike(String value) {
            addCriterion("purchase_intent_score like", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreNotLike(String value) {
            addCriterion("purchase_intent_score not like", value, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreIn(List<String> values) {
            addCriterion("purchase_intent_score in", values, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreNotIn(List<String> values) {
            addCriterion("purchase_intent_score not in", values, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreBetween(String value1, String value2) {
            addCriterion("purchase_intent_score between", value1, value2, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andPurchaseIntentScoreNotBetween(String value1, String value2) {
            addCriterion("purchase_intent_score not between", value1, value2, "purchaseIntentScore");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelIsNull() {
            addCriterion("marketing_acceptance_level is null");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelIsNotNull() {
            addCriterion("marketing_acceptance_level is not null");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelEqualTo(String value) {
            addCriterion("marketing_acceptance_level =", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelNotEqualTo(String value) {
            addCriterion("marketing_acceptance_level <>", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelGreaterThan(String value) {
            addCriterion("marketing_acceptance_level >", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelGreaterThanOrEqualTo(String value) {
            addCriterion("marketing_acceptance_level >=", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelLessThan(String value) {
            addCriterion("marketing_acceptance_level <", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelLessThanOrEqualTo(String value) {
            addCriterion("marketing_acceptance_level <=", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelLike(String value) {
            addCriterion("marketing_acceptance_level like", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelNotLike(String value) {
            addCriterion("marketing_acceptance_level not like", value, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelIn(List<String> values) {
            addCriterion("marketing_acceptance_level in", values, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelNotIn(List<String> values) {
            addCriterion("marketing_acceptance_level not in", values, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelBetween(String value1, String value2) {
            addCriterion("marketing_acceptance_level between", value1, value2, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMarketingAcceptanceLevelNotBetween(String value1, String value2) {
            addCriterion("marketing_acceptance_level not between", value1, value2, "marketingAcceptanceLevel");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountIsNull() {
            addCriterion("multi_position_count is null");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountIsNotNull() {
            addCriterion("multi_position_count is not null");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountEqualTo(String value) {
            addCriterion("multi_position_count =", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountNotEqualTo(String value) {
            addCriterion("multi_position_count <>", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountGreaterThan(String value) {
            addCriterion("multi_position_count >", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountGreaterThanOrEqualTo(String value) {
            addCriterion("multi_position_count >=", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountLessThan(String value) {
            addCriterion("multi_position_count <", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountLessThanOrEqualTo(String value) {
            addCriterion("multi_position_count <=", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountLike(String value) {
            addCriterion("multi_position_count like", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountNotLike(String value) {
            addCriterion("multi_position_count not like", value, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountIn(List<String> values) {
            addCriterion("multi_position_count in", values, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountNotIn(List<String> values) {
            addCriterion("multi_position_count not in", values, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountBetween(String value1, String value2) {
            addCriterion("multi_position_count between", value1, value2, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andMultiPositionCountNotBetween(String value1, String value2) {
            addCriterion("multi_position_count not between", value1, value2, "multiPositionCount");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceIsNull() {
            addCriterion("available_balance is null");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceIsNotNull() {
            addCriterion("available_balance is not null");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceEqualTo(String value) {
            addCriterion("available_balance =", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceNotEqualTo(String value) {
            addCriterion("available_balance <>", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceGreaterThan(String value) {
            addCriterion("available_balance >", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceGreaterThanOrEqualTo(String value) {
            addCriterion("available_balance >=", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceLessThan(String value) {
            addCriterion("available_balance <", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceLessThanOrEqualTo(String value) {
            addCriterion("available_balance <=", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceLike(String value) {
            addCriterion("available_balance like", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceNotLike(String value) {
            addCriterion("available_balance not like", value, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceIn(List<String> values) {
            addCriterion("available_balance in", values, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceNotIn(List<String> values) {
            addCriterion("available_balance not in", values, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceBetween(String value1, String value2) {
            addCriterion("available_balance between", value1, value2, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAvailableBalanceNotBetween(String value1, String value2) {
            addCriterion("available_balance not between", value1, value2, "availableBalance");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsIsNull() {
            addCriterion("account_assets is null");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsIsNotNull() {
            addCriterion("account_assets is not null");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsEqualTo(String value) {
            addCriterion("account_assets =", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsNotEqualTo(String value) {
            addCriterion("account_assets <>", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsGreaterThan(String value) {
            addCriterion("account_assets >", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsGreaterThanOrEqualTo(String value) {
            addCriterion("account_assets >=", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsLessThan(String value) {
            addCriterion("account_assets <", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsLessThanOrEqualTo(String value) {
            addCriterion("account_assets <=", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsLike(String value) {
            addCriterion("account_assets like", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsNotLike(String value) {
            addCriterion("account_assets not like", value, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsIn(List<String> values) {
            addCriterion("account_assets in", values, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsNotIn(List<String> values) {
            addCriterion("account_assets not in", values, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsBetween(String value1, String value2) {
            addCriterion("account_assets between", value1, value2, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andAccountAssetsNotBetween(String value1, String value2) {
            addCriterion("account_assets not between", value1, value2, "accountAssets");
            return (Criteria) this;
        }

        public Criteria andDepositPositionIsNull() {
            addCriterion("deposit_position is null");
            return (Criteria) this;
        }

        public Criteria andDepositPositionIsNotNull() {
            addCriterion("deposit_position is not null");
            return (Criteria) this;
        }

        public Criteria andDepositPositionEqualTo(String value) {
            addCriterion("deposit_position =", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionNotEqualTo(String value) {
            addCriterion("deposit_position <>", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionGreaterThan(String value) {
            addCriterion("deposit_position >", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionGreaterThanOrEqualTo(String value) {
            addCriterion("deposit_position >=", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionLessThan(String value) {
            addCriterion("deposit_position <", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionLessThanOrEqualTo(String value) {
            addCriterion("deposit_position <=", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionLike(String value) {
            addCriterion("deposit_position like", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionNotLike(String value) {
            addCriterion("deposit_position not like", value, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionIn(List<String> values) {
            addCriterion("deposit_position in", values, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionNotIn(List<String> values) {
            addCriterion("deposit_position not in", values, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionBetween(String value1, String value2) {
            addCriterion("deposit_position between", value1, value2, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andDepositPositionNotBetween(String value1, String value2) {
            addCriterion("deposit_position not between", value1, value2, "depositPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionIsNull() {
            addCriterion("wealth_position is null");
            return (Criteria) this;
        }

        public Criteria andWealthPositionIsNotNull() {
            addCriterion("wealth_position is not null");
            return (Criteria) this;
        }

        public Criteria andWealthPositionEqualTo(String value) {
            addCriterion("wealth_position =", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionNotEqualTo(String value) {
            addCriterion("wealth_position <>", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionGreaterThan(String value) {
            addCriterion("wealth_position >", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionGreaterThanOrEqualTo(String value) {
            addCriterion("wealth_position >=", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionLessThan(String value) {
            addCriterion("wealth_position <", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionLessThanOrEqualTo(String value) {
            addCriterion("wealth_position <=", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionLike(String value) {
            addCriterion("wealth_position like", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionNotLike(String value) {
            addCriterion("wealth_position not like", value, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionIn(List<String> values) {
            addCriterion("wealth_position in", values, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionNotIn(List<String> values) {
            addCriterion("wealth_position not in", values, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionBetween(String value1, String value2) {
            addCriterion("wealth_position between", value1, value2, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andWealthPositionNotBetween(String value1, String value2) {
            addCriterion("wealth_position not between", value1, value2, "wealthPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionIsNull() {
            addCriterion("fund_position is null");
            return (Criteria) this;
        }

        public Criteria andFundPositionIsNotNull() {
            addCriterion("fund_position is not null");
            return (Criteria) this;
        }

        public Criteria andFundPositionEqualTo(String value) {
            addCriterion("fund_position =", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionNotEqualTo(String value) {
            addCriterion("fund_position <>", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionGreaterThan(String value) {
            addCriterion("fund_position >", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionGreaterThanOrEqualTo(String value) {
            addCriterion("fund_position >=", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionLessThan(String value) {
            addCriterion("fund_position <", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionLessThanOrEqualTo(String value) {
            addCriterion("fund_position <=", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionLike(String value) {
            addCriterion("fund_position like", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionNotLike(String value) {
            addCriterion("fund_position not like", value, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionIn(List<String> values) {
            addCriterion("fund_position in", values, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionNotIn(List<String> values) {
            addCriterion("fund_position not in", values, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionBetween(String value1, String value2) {
            addCriterion("fund_position between", value1, value2, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andFundPositionNotBetween(String value1, String value2) {
            addCriterion("fund_position not between", value1, value2, "fundPosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionIsNull() {
            addCriterion("insurance_position is null");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionIsNotNull() {
            addCriterion("insurance_position is not null");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionEqualTo(String value) {
            addCriterion("insurance_position =", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionNotEqualTo(String value) {
            addCriterion("insurance_position <>", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionGreaterThan(String value) {
            addCriterion("insurance_position >", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionGreaterThanOrEqualTo(String value) {
            addCriterion("insurance_position >=", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionLessThan(String value) {
            addCriterion("insurance_position <", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionLessThanOrEqualTo(String value) {
            addCriterion("insurance_position <=", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionLike(String value) {
            addCriterion("insurance_position like", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionNotLike(String value) {
            addCriterion("insurance_position not like", value, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionIn(List<String> values) {
            addCriterion("insurance_position in", values, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionNotIn(List<String> values) {
            addCriterion("insurance_position not in", values, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionBetween(String value1, String value2) {
            addCriterion("insurance_position between", value1, value2, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andInsurancePositionNotBetween(String value1, String value2) {
            addCriterion("insurance_position not between", value1, value2, "insurancePosition");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordIsNull() {
            addCriterion("buy_and_redeem_record is null");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordIsNotNull() {
            addCriterion("buy_and_redeem_record is not null");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordEqualTo(String value) {
            addCriterion("buy_and_redeem_record =", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordNotEqualTo(String value) {
            addCriterion("buy_and_redeem_record <>", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordGreaterThan(String value) {
            addCriterion("buy_and_redeem_record >", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordGreaterThanOrEqualTo(String value) {
            addCriterion("buy_and_redeem_record >=", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordLessThan(String value) {
            addCriterion("buy_and_redeem_record <", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordLessThanOrEqualTo(String value) {
            addCriterion("buy_and_redeem_record <=", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordLike(String value) {
            addCriterion("buy_and_redeem_record like", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordNotLike(String value) {
            addCriterion("buy_and_redeem_record not like", value, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordIn(List<String> values) {
            addCriterion("buy_and_redeem_record in", values, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordNotIn(List<String> values) {
            addCriterion("buy_and_redeem_record not in", values, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordBetween(String value1, String value2) {
            addCriterion("buy_and_redeem_record between", value1, value2, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andBuyAndRedeemRecordNotBetween(String value1, String value2) {
            addCriterion("buy_and_redeem_record not between", value1, value2, "buyAndRedeemRecord");
            return (Criteria) this;
        }

        public Criteria andHasLoanIsNull() {
            addCriterion("has_loan is null");
            return (Criteria) this;
        }

        public Criteria andHasLoanIsNotNull() {
            addCriterion("has_loan is not null");
            return (Criteria) this;
        }

        public Criteria andHasLoanEqualTo(String value) {
            addCriterion("has_loan =", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanNotEqualTo(String value) {
            addCriterion("has_loan <>", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanGreaterThan(String value) {
            addCriterion("has_loan >", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanGreaterThanOrEqualTo(String value) {
            addCriterion("has_loan >=", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanLessThan(String value) {
            addCriterion("has_loan <", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanLessThanOrEqualTo(String value) {
            addCriterion("has_loan <=", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanLike(String value) {
            addCriterion("has_loan like", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanNotLike(String value) {
            addCriterion("has_loan not like", value, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanIn(List<String> values) {
            addCriterion("has_loan in", values, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanNotIn(List<String> values) {
            addCriterion("has_loan not in", values, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanBetween(String value1, String value2) {
            addCriterion("has_loan between", value1, value2, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasLoanNotBetween(String value1, String value2) {
            addCriterion("has_loan not between", value1, value2, "hasLoan");
            return (Criteria) this;
        }

        public Criteria andHasWechatIsNull() {
            addCriterion("has_wechat is null");
            return (Criteria) this;
        }

        public Criteria andHasWechatIsNotNull() {
            addCriterion("has_wechat is not null");
            return (Criteria) this;
        }

        public Criteria andHasWechatEqualTo(String value) {
            addCriterion("has_wechat =", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatNotEqualTo(String value) {
            addCriterion("has_wechat <>", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatGreaterThan(String value) {
            addCriterion("has_wechat >", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatGreaterThanOrEqualTo(String value) {
            addCriterion("has_wechat >=", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatLessThan(String value) {
            addCriterion("has_wechat <", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatLessThanOrEqualTo(String value) {
            addCriterion("has_wechat <=", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatLike(String value) {
            addCriterion("has_wechat like", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatNotLike(String value) {
            addCriterion("has_wechat not like", value, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatIn(List<String> values) {
            addCriterion("has_wechat in", values, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatNotIn(List<String> values) {
            addCriterion("has_wechat not in", values, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatBetween(String value1, String value2) {
            addCriterion("has_wechat between", value1, value2, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andHasWechatNotBetween(String value1, String value2) {
            addCriterion("has_wechat not between", value1, value2, "hasWechat");
            return (Criteria) this;
        }

        public Criteria andRiskLevelIsNull() {
            addCriterion("risk_level is null");
            return (Criteria) this;
        }

        public Criteria andRiskLevelIsNotNull() {
            addCriterion("risk_level is not null");
            return (Criteria) this;
        }

        public Criteria andRiskLevelEqualTo(String value) {
            addCriterion("risk_level =", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelNotEqualTo(String value) {
            addCriterion("risk_level <>", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelGreaterThan(String value) {
            addCriterion("risk_level >", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelGreaterThanOrEqualTo(String value) {
            addCriterion("risk_level >=", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelLessThan(String value) {
            addCriterion("risk_level <", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelLessThanOrEqualTo(String value) {
            addCriterion("risk_level <=", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelLike(String value) {
            addCriterion("risk_level like", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelNotLike(String value) {
            addCriterion("risk_level not like", value, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelIn(List<String> values) {
            addCriterion("risk_level in", values, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelNotIn(List<String> values) {
            addCriterion("risk_level not in", values, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelBetween(String value1, String value2) {
            addCriterion("risk_level between", value1, value2, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andRiskLevelNotBetween(String value1, String value2) {
            addCriterion("risk_level not between", value1, value2, "riskLevel");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyIsNull() {
            addCriterion("third_party_custody is null");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyIsNotNull() {
            addCriterion("third_party_custody is not null");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyEqualTo(String value) {
            addCriterion("third_party_custody =", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyNotEqualTo(String value) {
            addCriterion("third_party_custody <>", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyGreaterThan(String value) {
            addCriterion("third_party_custody >", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyGreaterThanOrEqualTo(String value) {
            addCriterion("third_party_custody >=", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyLessThan(String value) {
            addCriterion("third_party_custody <", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyLessThanOrEqualTo(String value) {
            addCriterion("third_party_custody <=", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyLike(String value) {
            addCriterion("third_party_custody like", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyNotLike(String value) {
            addCriterion("third_party_custody not like", value, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyIn(List<String> values) {
            addCriterion("third_party_custody in", values, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyNotIn(List<String> values) {
            addCriterion("third_party_custody not in", values, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyBetween(String value1, String value2) {
            addCriterion("third_party_custody between", value1, value2, "thirdPartyCustody");
            return (Criteria) this;
        }

        public Criteria andThirdPartyCustodyNotBetween(String value1, String value2) {
            addCriterion("third_party_custody not between", value1, value2, "thirdPartyCustody");
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