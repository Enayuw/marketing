package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BQifuUploadDataOriginalExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public BQifuUploadDataOriginalExample() {
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

        public Criteria andDrsIdIsNull() {
            addCriterion("drs_id is null");
            return (Criteria) this;
        }

        public Criteria andDrsIdIsNotNull() {
            addCriterion("drs_id is not null");
            return (Criteria) this;
        }

        public Criteria andDrsIdEqualTo(Long value) {
            addCriterion("drs_id =", value, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdNotEqualTo(Long value) {
            addCriterion("drs_id <>", value, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdGreaterThan(Long value) {
            addCriterion("drs_id >", value, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdGreaterThanOrEqualTo(Long value) {
            addCriterion("drs_id >=", value, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdLessThan(Long value) {
            addCriterion("drs_id <", value, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdLessThanOrEqualTo(Long value) {
            addCriterion("drs_id <=", value, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdIn(List<Long> values) {
            addCriterion("drs_id in", values, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdNotIn(List<Long> values) {
            addCriterion("drs_id not in", values, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdBetween(Long value1, Long value2) {
            addCriterion("drs_id between", value1, value2, "drsId");
            return (Criteria) this;
        }

        public Criteria andDrsIdNotBetween(Long value1, Long value2) {
            addCriterion("drs_id not between", value1, value2, "drsId");
            return (Criteria) this;
        }

        public Criteria andBatchNoIsNull() {
            addCriterion("batch_no is null");
            return (Criteria) this;
        }

        public Criteria andBatchNoIsNotNull() {
            addCriterion("batch_no is not null");
            return (Criteria) this;
        }

        public Criteria andBatchNoEqualTo(String value) {
            addCriterion("batch_no =", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotEqualTo(String value) {
            addCriterion("batch_no <>", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoGreaterThan(String value) {
            addCriterion("batch_no >", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoGreaterThanOrEqualTo(String value) {
            addCriterion("batch_no >=", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoLessThan(String value) {
            addCriterion("batch_no <", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoLessThanOrEqualTo(String value) {
            addCriterion("batch_no <=", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoLike(String value) {
            addCriterion("batch_no like", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotLike(String value) {
            addCriterion("batch_no not like", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoIn(List<String> values) {
            addCriterion("batch_no in", values, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotIn(List<String> values) {
            addCriterion("batch_no not in", values, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoBetween(String value1, String value2) {
            addCriterion("batch_no between", value1, value2, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotBetween(String value1, String value2) {
            addCriterion("batch_no not between", value1, value2, "batchNo");
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

        public Criteria andCallTimeRangeIsNull() {
            addCriterion("call_time_range is null");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeIsNotNull() {
            addCriterion("call_time_range is not null");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeEqualTo(String value) {
            addCriterion("call_time_range =", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeNotEqualTo(String value) {
            addCriterion("call_time_range <>", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeGreaterThan(String value) {
            addCriterion("call_time_range >", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeGreaterThanOrEqualTo(String value) {
            addCriterion("call_time_range >=", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeLessThan(String value) {
            addCriterion("call_time_range <", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeLessThanOrEqualTo(String value) {
            addCriterion("call_time_range <=", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeLike(String value) {
            addCriterion("call_time_range like", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeNotLike(String value) {
            addCriterion("call_time_range not like", value, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeIn(List<String> values) {
            addCriterion("call_time_range in", values, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeNotIn(List<String> values) {
            addCriterion("call_time_range not in", values, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeBetween(String value1, String value2) {
            addCriterion("call_time_range between", value1, value2, "callTimeRange");
            return (Criteria) this;
        }

        public Criteria andCallTimeRangeNotBetween(String value1, String value2) {
            addCriterion("call_time_range not between", value1, value2, "callTimeRange");
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

        public Criteria andPhoneNoMd5IsNull() {
            addCriterion("phone_no_md5 is null");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5IsNotNull() {
            addCriterion("phone_no_md5 is not null");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5EqualTo(String value) {
            addCriterion("phone_no_md5 =", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5NotEqualTo(String value) {
            addCriterion("phone_no_md5 <>", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5GreaterThan(String value) {
            addCriterion("phone_no_md5 >", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5GreaterThanOrEqualTo(String value) {
            addCriterion("phone_no_md5 >=", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5LessThan(String value) {
            addCriterion("phone_no_md5 <", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5LessThanOrEqualTo(String value) {
            addCriterion("phone_no_md5 <=", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5Like(String value) {
            addCriterion("phone_no_md5 like", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5NotLike(String value) {
            addCriterion("phone_no_md5 not like", value, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5In(List<String> values) {
            addCriterion("phone_no_md5 in", values, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5NotIn(List<String> values) {
            addCriterion("phone_no_md5 not in", values, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5Between(String value1, String value2) {
            addCriterion("phone_no_md5 between", value1, value2, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andPhoneNoMd5NotBetween(String value1, String value2) {
            addCriterion("phone_no_md5 not between", value1, value2, "phoneNoMd5");
            return (Criteria) this;
        }

        public Criteria andSerialNoIsNull() {
            addCriterion("serial_no is null");
            return (Criteria) this;
        }

        public Criteria andSerialNoIsNotNull() {
            addCriterion("serial_no is not null");
            return (Criteria) this;
        }

        public Criteria andSerialNoEqualTo(String value) {
            addCriterion("serial_no =", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotEqualTo(String value) {
            addCriterion("serial_no <>", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoGreaterThan(String value) {
            addCriterion("serial_no >", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoGreaterThanOrEqualTo(String value) {
            addCriterion("serial_no >=", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoLessThan(String value) {
            addCriterion("serial_no <", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoLessThanOrEqualTo(String value) {
            addCriterion("serial_no <=", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoLike(String value) {
            addCriterion("serial_no like", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotLike(String value) {
            addCriterion("serial_no not like", value, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoIn(List<String> values) {
            addCriterion("serial_no in", values, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotIn(List<String> values) {
            addCriterion("serial_no not in", values, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoBetween(String value1, String value2) {
            addCriterion("serial_no between", value1, value2, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSerialNoNotBetween(String value1, String value2) {
            addCriterion("serial_no not between", value1, value2, "serialNo");
            return (Criteria) this;
        }

        public Criteria andSurnameIsNull() {
            addCriterion("surname is null");
            return (Criteria) this;
        }

        public Criteria andSurnameIsNotNull() {
            addCriterion("surname is not null");
            return (Criteria) this;
        }

        public Criteria andSurnameEqualTo(String value) {
            addCriterion("surname =", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameNotEqualTo(String value) {
            addCriterion("surname <>", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameGreaterThan(String value) {
            addCriterion("surname >", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameGreaterThanOrEqualTo(String value) {
            addCriterion("surname >=", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameLessThan(String value) {
            addCriterion("surname <", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameLessThanOrEqualTo(String value) {
            addCriterion("surname <=", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameLike(String value) {
            addCriterion("surname like", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameNotLike(String value) {
            addCriterion("surname not like", value, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameIn(List<String> values) {
            addCriterion("surname in", values, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameNotIn(List<String> values) {
            addCriterion("surname not in", values, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameBetween(String value1, String value2) {
            addCriterion("surname between", value1, value2, "surname");
            return (Criteria) this;
        }

        public Criteria andSurnameNotBetween(String value1, String value2) {
            addCriterion("surname not between", value1, value2, "surname");
            return (Criteria) this;
        }

        public Criteria andSelectStatusIsNull() {
            addCriterion("select_status is null");
            return (Criteria) this;
        }

        public Criteria andSelectStatusIsNotNull() {
            addCriterion("select_status is not null");
            return (Criteria) this;
        }

        public Criteria andSelectStatusEqualTo(Integer value) {
            addCriterion("select_status =", value, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusNotEqualTo(Integer value) {
            addCriterion("select_status <>", value, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusGreaterThan(Integer value) {
            addCriterion("select_status >", value, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("select_status >=", value, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusLessThan(Integer value) {
            addCriterion("select_status <", value, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusLessThanOrEqualTo(Integer value) {
            addCriterion("select_status <=", value, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusIn(List<Integer> values) {
            addCriterion("select_status in", values, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusNotIn(List<Integer> values) {
            addCriterion("select_status not in", values, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusBetween(Integer value1, Integer value2) {
            addCriterion("select_status between", value1, value2, "selectStatus");
            return (Criteria) this;
        }

        public Criteria andSelectStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("select_status not between", value1, value2, "selectStatus");
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

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andFlowNoIsNull() {
            addCriterion("flow_no is null");
            return (Criteria) this;
        }

        public Criteria andFlowNoIsNotNull() {
            addCriterion("flow_no is not null");
            return (Criteria) this;
        }

        public Criteria andFlowNoEqualTo(String value) {
            addCriterion("flow_no =", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoNotEqualTo(String value) {
            addCriterion("flow_no <>", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoGreaterThan(String value) {
            addCriterion("flow_no >", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoGreaterThanOrEqualTo(String value) {
            addCriterion("flow_no >=", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoLessThan(String value) {
            addCriterion("flow_no <", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoLessThanOrEqualTo(String value) {
            addCriterion("flow_no <=", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoLike(String value) {
            addCriterion("flow_no like", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoNotLike(String value) {
            addCriterion("flow_no not like", value, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoIn(List<String> values) {
            addCriterion("flow_no in", values, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoNotIn(List<String> values) {
            addCriterion("flow_no not in", values, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoBetween(String value1, String value2) {
            addCriterion("flow_no between", value1, value2, "flowNo");
            return (Criteria) this;
        }

        public Criteria andFlowNoNotBetween(String value1, String value2) {
            addCriterion("flow_no not between", value1, value2, "flowNo");
            return (Criteria) this;
        }

        public Criteria andOperateSceneIsNull() {
            addCriterion("operate_scene is null");
            return (Criteria) this;
        }

        public Criteria andOperateSceneIsNotNull() {
            addCriterion("operate_scene is not null");
            return (Criteria) this;
        }

        public Criteria andOperateSceneEqualTo(String value) {
            addCriterion("operate_scene =", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneNotEqualTo(String value) {
            addCriterion("operate_scene <>", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneGreaterThan(String value) {
            addCriterion("operate_scene >", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneGreaterThanOrEqualTo(String value) {
            addCriterion("operate_scene >=", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneLessThan(String value) {
            addCriterion("operate_scene <", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneLessThanOrEqualTo(String value) {
            addCriterion("operate_scene <=", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneLike(String value) {
            addCriterion("operate_scene like", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneNotLike(String value) {
            addCriterion("operate_scene not like", value, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneIn(List<String> values) {
            addCriterion("operate_scene in", values, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneNotIn(List<String> values) {
            addCriterion("operate_scene not in", values, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneBetween(String value1, String value2) {
            addCriterion("operate_scene between", value1, value2, "operateScene");
            return (Criteria) this;
        }

        public Criteria andOperateSceneNotBetween(String value1, String value2) {
            addCriterion("operate_scene not between", value1, value2, "operateScene");
            return (Criteria) this;
        }

        public Criteria andTemplateNoIsNull() {
            addCriterion("template_no is null");
            return (Criteria) this;
        }

        public Criteria andTemplateNoIsNotNull() {
            addCriterion("template_no is not null");
            return (Criteria) this;
        }

        public Criteria andTemplateNoEqualTo(String value) {
            addCriterion("template_no =", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoNotEqualTo(String value) {
            addCriterion("template_no <>", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoGreaterThan(String value) {
            addCriterion("template_no >", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoGreaterThanOrEqualTo(String value) {
            addCriterion("template_no >=", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoLessThan(String value) {
            addCriterion("template_no <", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoLessThanOrEqualTo(String value) {
            addCriterion("template_no <=", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoLike(String value) {
            addCriterion("template_no like", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoNotLike(String value) {
            addCriterion("template_no not like", value, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoIn(List<String> values) {
            addCriterion("template_no in", values, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoNotIn(List<String> values) {
            addCriterion("template_no not in", values, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoBetween(String value1, String value2) {
            addCriterion("template_no between", value1, value2, "templateNo");
            return (Criteria) this;
        }

        public Criteria andTemplateNoNotBetween(String value1, String value2) {
            addCriterion("template_no not between", value1, value2, "templateNo");
            return (Criteria) this;
        }

        public Criteria andEventTypeIsNull() {
            addCriterion("event_type is null");
            return (Criteria) this;
        }

        public Criteria andEventTypeIsNotNull() {
            addCriterion("event_type is not null");
            return (Criteria) this;
        }

        public Criteria andEventTypeEqualTo(String value) {
            addCriterion("event_type =", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotEqualTo(String value) {
            addCriterion("event_type <>", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeGreaterThan(String value) {
            addCriterion("event_type >", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeGreaterThanOrEqualTo(String value) {
            addCriterion("event_type >=", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeLessThan(String value) {
            addCriterion("event_type <", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeLessThanOrEqualTo(String value) {
            addCriterion("event_type <=", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeLike(String value) {
            addCriterion("event_type like", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotLike(String value) {
            addCriterion("event_type not like", value, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeIn(List<String> values) {
            addCriterion("event_type in", values, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotIn(List<String> values) {
            addCriterion("event_type not in", values, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeBetween(String value1, String value2) {
            addCriterion("event_type between", value1, value2, "eventType");
            return (Criteria) this;
        }

        public Criteria andEventTypeNotBetween(String value1, String value2) {
            addCriterion("event_type not between", value1, value2, "eventType");
            return (Criteria) this;
        }

        public Criteria andSendMsgIsNull() {
            addCriterion("send_msg is null");
            return (Criteria) this;
        }

        public Criteria andSendMsgIsNotNull() {
            addCriterion("send_msg is not null");
            return (Criteria) this;
        }

        public Criteria andSendMsgEqualTo(String value) {
            addCriterion("send_msg =", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgNotEqualTo(String value) {
            addCriterion("send_msg <>", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgGreaterThan(String value) {
            addCriterion("send_msg >", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgGreaterThanOrEqualTo(String value) {
            addCriterion("send_msg >=", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgLessThan(String value) {
            addCriterion("send_msg <", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgLessThanOrEqualTo(String value) {
            addCriterion("send_msg <=", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgLike(String value) {
            addCriterion("send_msg like", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgNotLike(String value) {
            addCriterion("send_msg not like", value, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgIn(List<String> values) {
            addCriterion("send_msg in", values, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgNotIn(List<String> values) {
            addCriterion("send_msg not in", values, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgBetween(String value1, String value2) {
            addCriterion("send_msg between", value1, value2, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andSendMsgNotBetween(String value1, String value2) {
            addCriterion("send_msg not between", value1, value2, "sendMsg");
            return (Criteria) this;
        }

        public Criteria andRetryCallIsNull() {
            addCriterion("retry_call is null");
            return (Criteria) this;
        }

        public Criteria andRetryCallIsNotNull() {
            addCriterion("retry_call is not null");
            return (Criteria) this;
        }

        public Criteria andRetryCallEqualTo(String value) {
            addCriterion("retry_call =", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallNotEqualTo(String value) {
            addCriterion("retry_call <>", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallGreaterThan(String value) {
            addCriterion("retry_call >", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallGreaterThanOrEqualTo(String value) {
            addCriterion("retry_call >=", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallLessThan(String value) {
            addCriterion("retry_call <", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallLessThanOrEqualTo(String value) {
            addCriterion("retry_call <=", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallLike(String value) {
            addCriterion("retry_call like", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallNotLike(String value) {
            addCriterion("retry_call not like", value, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallIn(List<String> values) {
            addCriterion("retry_call in", values, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallNotIn(List<String> values) {
            addCriterion("retry_call not in", values, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallBetween(String value1, String value2) {
            addCriterion("retry_call between", value1, value2, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryCallNotBetween(String value1, String value2) {
            addCriterion("retry_call not between", value1, value2, "retryCall");
            return (Criteria) this;
        }

        public Criteria andRetryRangeIsNull() {
            addCriterion("retry_range is null");
            return (Criteria) this;
        }

        public Criteria andRetryRangeIsNotNull() {
            addCriterion("retry_range is not null");
            return (Criteria) this;
        }

        public Criteria andRetryRangeEqualTo(String value) {
            addCriterion("retry_range =", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeNotEqualTo(String value) {
            addCriterion("retry_range <>", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeGreaterThan(String value) {
            addCriterion("retry_range >", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeGreaterThanOrEqualTo(String value) {
            addCriterion("retry_range >=", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeLessThan(String value) {
            addCriterion("retry_range <", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeLessThanOrEqualTo(String value) {
            addCriterion("retry_range <=", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeLike(String value) {
            addCriterion("retry_range like", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeNotLike(String value) {
            addCriterion("retry_range not like", value, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeIn(List<String> values) {
            addCriterion("retry_range in", values, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeNotIn(List<String> values) {
            addCriterion("retry_range not in", values, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeBetween(String value1, String value2) {
            addCriterion("retry_range between", value1, value2, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryRangeNotBetween(String value1, String value2) {
            addCriterion("retry_range not between", value1, value2, "retryRange");
            return (Criteria) this;
        }

        public Criteria andRetryNumsIsNull() {
            addCriterion("retry_nums is null");
            return (Criteria) this;
        }

        public Criteria andRetryNumsIsNotNull() {
            addCriterion("retry_nums is not null");
            return (Criteria) this;
        }

        public Criteria andRetryNumsEqualTo(String value) {
            addCriterion("retry_nums =", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsNotEqualTo(String value) {
            addCriterion("retry_nums <>", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsGreaterThan(String value) {
            addCriterion("retry_nums >", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsGreaterThanOrEqualTo(String value) {
            addCriterion("retry_nums >=", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsLessThan(String value) {
            addCriterion("retry_nums <", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsLessThanOrEqualTo(String value) {
            addCriterion("retry_nums <=", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsLike(String value) {
            addCriterion("retry_nums like", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsNotLike(String value) {
            addCriterion("retry_nums not like", value, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsIn(List<String> values) {
            addCriterion("retry_nums in", values, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsNotIn(List<String> values) {
            addCriterion("retry_nums not in", values, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsBetween(String value1, String value2) {
            addCriterion("retry_nums between", value1, value2, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryNumsNotBetween(String value1, String value2) {
            addCriterion("retry_nums not between", value1, value2, "retryNums");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIsNull() {
            addCriterion("retry_interval is null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIsNotNull() {
            addCriterion("retry_interval is not null");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalEqualTo(String value) {
            addCriterion("retry_interval =", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotEqualTo(String value) {
            addCriterion("retry_interval <>", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalGreaterThan(String value) {
            addCriterion("retry_interval >", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalGreaterThanOrEqualTo(String value) {
            addCriterion("retry_interval >=", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalLessThan(String value) {
            addCriterion("retry_interval <", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalLessThanOrEqualTo(String value) {
            addCriterion("retry_interval <=", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalLike(String value) {
            addCriterion("retry_interval like", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotLike(String value) {
            addCriterion("retry_interval not like", value, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalIn(List<String> values) {
            addCriterion("retry_interval in", values, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotIn(List<String> values) {
            addCriterion("retry_interval not in", values, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalBetween(String value1, String value2) {
            addCriterion("retry_interval between", value1, value2, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryIntervalNotBetween(String value1, String value2) {
            addCriterion("retry_interval not between", value1, value2, "retryInterval");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyIsNull() {
            addCriterion("retry_call_strategy is null");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyIsNotNull() {
            addCriterion("retry_call_strategy is not null");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyEqualTo(String value) {
            addCriterion("retry_call_strategy =", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyNotEqualTo(String value) {
            addCriterion("retry_call_strategy <>", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyGreaterThan(String value) {
            addCriterion("retry_call_strategy >", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyGreaterThanOrEqualTo(String value) {
            addCriterion("retry_call_strategy >=", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyLessThan(String value) {
            addCriterion("retry_call_strategy <", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyLessThanOrEqualTo(String value) {
            addCriterion("retry_call_strategy <=", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyLike(String value) {
            addCriterion("retry_call_strategy like", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyNotLike(String value) {
            addCriterion("retry_call_strategy not like", value, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyIn(List<String> values) {
            addCriterion("retry_call_strategy in", values, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyNotIn(List<String> values) {
            addCriterion("retry_call_strategy not in", values, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyBetween(String value1, String value2) {
            addCriterion("retry_call_strategy between", value1, value2, "retryCallStrategy");
            return (Criteria) this;
        }

        public Criteria andRetryCallStrategyNotBetween(String value1, String value2) {
            addCriterion("retry_call_strategy not between", value1, value2, "retryCallStrategy");
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

        public Criteria andReceiveDateIsNull() {
            addCriterion("receive_date is null");
            return (Criteria) this;
        }

        public Criteria andReceiveDateIsNotNull() {
            addCriterion("receive_date is not null");
            return (Criteria) this;
        }

        public Criteria andReceiveDateEqualTo(String value) {
            addCriterion("receive_date =", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotEqualTo(String value) {
            addCriterion("receive_date <>", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateGreaterThan(String value) {
            addCriterion("receive_date >", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateGreaterThanOrEqualTo(String value) {
            addCriterion("receive_date >=", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLessThan(String value) {
            addCriterion("receive_date <", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLessThanOrEqualTo(String value) {
            addCriterion("receive_date <=", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLike(String value) {
            addCriterion("receive_date like", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotLike(String value) {
            addCriterion("receive_date not like", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateIn(List<String> values) {
            addCriterion("receive_date in", values, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotIn(List<String> values) {
            addCriterion("receive_date not in", values, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateBetween(String value1, String value2) {
            addCriterion("receive_date between", value1, value2, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotBetween(String value1, String value2) {
            addCriterion("receive_date not between", value1, value2, "receiveDate");
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

