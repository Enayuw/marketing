package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ThirdPartnerUploadDataCleanExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ThirdPartnerUploadDataCleanExample() {
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

        public Criteria andAccessNumberIsNull() {
            addCriterion("access_number is null");
            return (Criteria) this;
        }

        public Criteria andAccessNumberIsNotNull() {
            addCriterion("access_number is not null");
            return (Criteria) this;
        }

        public Criteria andAccessNumberEqualTo(String value) {
            addCriterion("access_number =", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotEqualTo(String value) {
            addCriterion("access_number <>", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberGreaterThan(String value) {
            addCriterion("access_number >", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberGreaterThanOrEqualTo(String value) {
            addCriterion("access_number >=", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberLessThan(String value) {
            addCriterion("access_number <", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberLessThanOrEqualTo(String value) {
            addCriterion("access_number <=", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberLike(String value) {
            addCriterion("access_number like", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotLike(String value) {
            addCriterion("access_number not like", value, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberIn(List<String> values) {
            addCriterion("access_number in", values, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotIn(List<String> values) {
            addCriterion("access_number not in", values, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberBetween(String value1, String value2) {
            addCriterion("access_number between", value1, value2, "accessNumber");
            return (Criteria) this;
        }

        public Criteria andAccessNumberNotBetween(String value1, String value2) {
            addCriterion("access_number not between", value1, value2, "accessNumber");
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

        public Criteria andOrgApiCodeIsNull() {
            addCriterion("org_api_code is null");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeIsNotNull() {
            addCriterion("org_api_code is not null");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeEqualTo(String value) {
            addCriterion("org_api_code =", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeNotEqualTo(String value) {
            addCriterion("org_api_code <>", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeGreaterThan(String value) {
            addCriterion("org_api_code >", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("org_api_code >=", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeLessThan(String value) {
            addCriterion("org_api_code <", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeLessThanOrEqualTo(String value) {
            addCriterion("org_api_code <=", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeLike(String value) {
            addCriterion("org_api_code like", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeNotLike(String value) {
            addCriterion("org_api_code not like", value, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeIn(List<String> values) {
            addCriterion("org_api_code in", values, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeNotIn(List<String> values) {
            addCriterion("org_api_code not in", values, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeBetween(String value1, String value2) {
            addCriterion("org_api_code between", value1, value2, "orgApiCode");
            return (Criteria) this;
        }

        public Criteria andOrgApiCodeNotBetween(String value1, String value2) {
            addCriterion("org_api_code not between", value1, value2, "orgApiCode");
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

        public Criteria andCustomNameTypeIsNull() {
            addCriterion("custom_name_type is null");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeIsNotNull() {
            addCriterion("custom_name_type is not null");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeEqualTo(String value) {
            addCriterion("custom_name_type =", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeNotEqualTo(String value) {
            addCriterion("custom_name_type <>", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeGreaterThan(String value) {
            addCriterion("custom_name_type >", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeGreaterThanOrEqualTo(String value) {
            addCriterion("custom_name_type >=", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeLessThan(String value) {
            addCriterion("custom_name_type <", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeLessThanOrEqualTo(String value) {
            addCriterion("custom_name_type <=", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeLike(String value) {
            addCriterion("custom_name_type like", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeNotLike(String value) {
            addCriterion("custom_name_type not like", value, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeIn(List<String> values) {
            addCriterion("custom_name_type in", values, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeNotIn(List<String> values) {
            addCriterion("custom_name_type not in", values, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeBetween(String value1, String value2) {
            addCriterion("custom_name_type between", value1, value2, "customNameType");
            return (Criteria) this;
        }

        public Criteria andCustomNameTypeNotBetween(String value1, String value2) {
            addCriterion("custom_name_type not between", value1, value2, "customNameType");
            return (Criteria) this;
        }

        public Criteria andResourceChannelIsNull() {
            addCriterion("resource_channel is null");
            return (Criteria) this;
        }

        public Criteria andResourceChannelIsNotNull() {
            addCriterion("resource_channel is not null");
            return (Criteria) this;
        }

        public Criteria andResourceChannelEqualTo(Integer value) {
            addCriterion("resource_channel =", value, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelNotEqualTo(Integer value) {
            addCriterion("resource_channel <>", value, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelGreaterThan(Integer value) {
            addCriterion("resource_channel >", value, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelGreaterThanOrEqualTo(Integer value) {
            addCriterion("resource_channel >=", value, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelLessThan(Integer value) {
            addCriterion("resource_channel <", value, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelLessThanOrEqualTo(Integer value) {
            addCriterion("resource_channel <=", value, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelIn(List<Integer> values) {
            addCriterion("resource_channel in", values, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelNotIn(List<Integer> values) {
            addCriterion("resource_channel not in", values, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelBetween(Integer value1, Integer value2) {
            addCriterion("resource_channel between", value1, value2, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andResourceChannelNotBetween(Integer value1, Integer value2) {
            addCriterion("resource_channel not between", value1, value2, "resourceChannel");
            return (Criteria) this;
        }

        public Criteria andValidStartDateIsNull() {
            addCriterion("valid_start_date is null");
            return (Criteria) this;
        }

        public Criteria andValidStartDateIsNotNull() {
            addCriterion("valid_start_date is not null");
            return (Criteria) this;
        }

        public Criteria andValidStartDateEqualTo(String value) {
            addCriterion("valid_start_date =", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateNotEqualTo(String value) {
            addCriterion("valid_start_date <>", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateGreaterThan(String value) {
            addCriterion("valid_start_date >", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateGreaterThanOrEqualTo(String value) {
            addCriterion("valid_start_date >=", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateLessThan(String value) {
            addCriterion("valid_start_date <", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateLessThanOrEqualTo(String value) {
            addCriterion("valid_start_date <=", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateLike(String value) {
            addCriterion("valid_start_date like", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateNotLike(String value) {
            addCriterion("valid_start_date not like", value, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateIn(List<String> values) {
            addCriterion("valid_start_date in", values, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateNotIn(List<String> values) {
            addCriterion("valid_start_date not in", values, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateBetween(String value1, String value2) {
            addCriterion("valid_start_date between", value1, value2, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidStartDateNotBetween(String value1, String value2) {
            addCriterion("valid_start_date not between", value1, value2, "validStartDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateIsNull() {
            addCriterion("valid_end_date is null");
            return (Criteria) this;
        }

        public Criteria andValidEndDateIsNotNull() {
            addCriterion("valid_end_date is not null");
            return (Criteria) this;
        }

        public Criteria andValidEndDateEqualTo(String value) {
            addCriterion("valid_end_date =", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateNotEqualTo(String value) {
            addCriterion("valid_end_date <>", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateGreaterThan(String value) {
            addCriterion("valid_end_date >", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateGreaterThanOrEqualTo(String value) {
            addCriterion("valid_end_date >=", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateLessThan(String value) {
            addCriterion("valid_end_date <", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateLessThanOrEqualTo(String value) {
            addCriterion("valid_end_date <=", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateLike(String value) {
            addCriterion("valid_end_date like", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateNotLike(String value) {
            addCriterion("valid_end_date not like", value, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateIn(List<String> values) {
            addCriterion("valid_end_date in", values, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateNotIn(List<String> values) {
            addCriterion("valid_end_date not in", values, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateBetween(String value1, String value2) {
            addCriterion("valid_end_date between", value1, value2, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andValidEndDateNotBetween(String value1, String value2) {
            addCriterion("valid_end_date not between", value1, value2, "validEndDate");
            return (Criteria) this;
        }

        public Criteria andCleanStatusIsNull() {
            addCriterion("clean_status is null");
            return (Criteria) this;
        }

        public Criteria andCleanStatusIsNotNull() {
            addCriterion("clean_status is not null");
            return (Criteria) this;
        }

        public Criteria andCleanStatusEqualTo(Integer value) {
            addCriterion("clean_status =", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusNotEqualTo(Integer value) {
            addCriterion("clean_status <>", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusGreaterThan(Integer value) {
            addCriterion("clean_status >", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("clean_status >=", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusLessThan(Integer value) {
            addCriterion("clean_status <", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusLessThanOrEqualTo(Integer value) {
            addCriterion("clean_status <=", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusIn(List<Integer> values) {
            addCriterion("clean_status in", values, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusNotIn(List<Integer> values) {
            addCriterion("clean_status not in", values, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusBetween(Integer value1, Integer value2) {
            addCriterion("clean_status between", value1, value2, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("clean_status not between", value1, value2, "cleanStatus");
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

        public Criteria andTaskIdEqualTo(Long value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(Long value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(Long value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(Long value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(Long value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(Long value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<Long> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<Long> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(Long value1, Long value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(Long value1, Long value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
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

        public Criteria andIsDeletedIsNull() {
            addCriterion("is_deleted is null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNotNull() {
            addCriterion("is_deleted is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedEqualTo(Integer value) {
            addCriterion("is_deleted =", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotEqualTo(Integer value) {
            addCriterion("is_deleted <>", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThan(Integer value) {
            addCriterion("is_deleted >", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_deleted >=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThan(Integer value) {
            addCriterion("is_deleted <", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThanOrEqualTo(Integer value) {
            addCriterion("is_deleted <=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIn(List<Integer> values) {
            addCriterion("is_deleted in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotIn(List<Integer> values) {
            addCriterion("is_deleted not in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted between", value1, value2, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted not between", value1, value2, "isDeleted");
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