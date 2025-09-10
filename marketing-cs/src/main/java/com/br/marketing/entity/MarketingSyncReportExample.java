package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingSyncReportExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingSyncReportExample() {
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

        public Criteria andShortNameIsNull() {
            addCriterion("short_name is null");
            return (Criteria) this;
        }

        public Criteria andShortNameIsNotNull() {
            addCriterion("short_name is not null");
            return (Criteria) this;
        }

        public Criteria andShortNameEqualTo(String value) {
            addCriterion("short_name =", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotEqualTo(String value) {
            addCriterion("short_name <>", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameGreaterThan(String value) {
            addCriterion("short_name >", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameGreaterThanOrEqualTo(String value) {
            addCriterion("short_name >=", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameLessThan(String value) {
            addCriterion("short_name <", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameLessThanOrEqualTo(String value) {
            addCriterion("short_name <=", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameLike(String value) {
            addCriterion("short_name like", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotLike(String value) {
            addCriterion("short_name not like", value, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameIn(List<String> values) {
            addCriterion("short_name in", values, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotIn(List<String> values) {
            addCriterion("short_name not in", values, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameBetween(String value1, String value2) {
            addCriterion("short_name between", value1, value2, "shortName");
            return (Criteria) this;
        }

        public Criteria andShortNameNotBetween(String value1, String value2) {
            addCriterion("short_name not between", value1, value2, "shortName");
            return (Criteria) this;
        }

        public Criteria andAppletDateIsNull() {
            addCriterion("applet_date is null");
            return (Criteria) this;
        }

        public Criteria andAppletDateIsNotNull() {
            addCriterion("applet_date is not null");
            return (Criteria) this;
        }

        public Criteria andAppletDateEqualTo(String value) {
            addCriterion("applet_date =", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotEqualTo(String value) {
            addCriterion("applet_date <>", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThan(String value) {
            addCriterion("applet_date >", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThanOrEqualTo(String value) {
            addCriterion("applet_date >=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThan(String value) {
            addCriterion("applet_date <", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThanOrEqualTo(String value) {
            addCriterion("applet_date <=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLike(String value) {
            addCriterion("applet_date like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotLike(String value) {
            addCriterion("applet_date not like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateIn(List<String> values) {
            addCriterion("applet_date in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotIn(List<String> values) {
            addCriterion("applet_date not in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateBetween(String value1, String value2) {
            addCriterion("applet_date between", value1, value2, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotBetween(String value1, String value2) {
            addCriterion("applet_date not between", value1, value2, "appletDate");
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

        public Criteria andNormalNumIsNull() {
            addCriterion("normal_num is null");
            return (Criteria) this;
        }

        public Criteria andNormalNumIsNotNull() {
            addCriterion("normal_num is not null");
            return (Criteria) this;
        }

        public Criteria andNormalNumEqualTo(Integer value) {
            addCriterion("normal_num =", value, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumNotEqualTo(Integer value) {
            addCriterion("normal_num <>", value, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumGreaterThan(Integer value) {
            addCriterion("normal_num >", value, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("normal_num >=", value, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumLessThan(Integer value) {
            addCriterion("normal_num <", value, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumLessThanOrEqualTo(Integer value) {
            addCriterion("normal_num <=", value, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumIn(List<Integer> values) {
            addCriterion("normal_num in", values, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumNotIn(List<Integer> values) {
            addCriterion("normal_num not in", values, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumBetween(Integer value1, Integer value2) {
            addCriterion("normal_num between", value1, value2, "normalNum");
            return (Criteria) this;
        }

        public Criteria andNormalNumNotBetween(Integer value1, Integer value2) {
            addCriterion("normal_num not between", value1, value2, "normalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumIsNull() {
            addCriterion("duplicate_removal_num is null");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumIsNotNull() {
            addCriterion("duplicate_removal_num is not null");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumEqualTo(Integer value) {
            addCriterion("duplicate_removal_num =", value, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumNotEqualTo(Integer value) {
            addCriterion("duplicate_removal_num <>", value, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumGreaterThan(Integer value) {
            addCriterion("duplicate_removal_num >", value, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("duplicate_removal_num >=", value, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumLessThan(Integer value) {
            addCriterion("duplicate_removal_num <", value, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumLessThanOrEqualTo(Integer value) {
            addCriterion("duplicate_removal_num <=", value, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumIn(List<Integer> values) {
            addCriterion("duplicate_removal_num in", values, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumNotIn(List<Integer> values) {
            addCriterion("duplicate_removal_num not in", values, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumBetween(Integer value1, Integer value2) {
            addCriterion("duplicate_removal_num between", value1, value2, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andDuplicateRemovalNumNotBetween(Integer value1, Integer value2) {
            addCriterion("duplicate_removal_num not between", value1, value2, "duplicateRemovalNum");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeIsNull() {
            addCriterion("applet_begin_time is null");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeIsNotNull() {
            addCriterion("applet_begin_time is not null");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeEqualTo(Date value) {
            addCriterion("applet_begin_time =", value, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeNotEqualTo(Date value) {
            addCriterion("applet_begin_time <>", value, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeGreaterThan(Date value) {
            addCriterion("applet_begin_time >", value, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("applet_begin_time >=", value, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeLessThan(Date value) {
            addCriterion("applet_begin_time <", value, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeLessThanOrEqualTo(Date value) {
            addCriterion("applet_begin_time <=", value, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeIn(List<Date> values) {
            addCriterion("applet_begin_time in", values, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeNotIn(List<Date> values) {
            addCriterion("applet_begin_time not in", values, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeBetween(Date value1, Date value2) {
            addCriterion("applet_begin_time between", value1, value2, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletBeginTimeNotBetween(Date value1, Date value2) {
            addCriterion("applet_begin_time not between", value1, value2, "appletBeginTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeIsNull() {
            addCriterion("applet_end_time is null");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeIsNotNull() {
            addCriterion("applet_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeEqualTo(Date value) {
            addCriterion("applet_end_time =", value, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeNotEqualTo(Date value) {
            addCriterion("applet_end_time <>", value, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeGreaterThan(Date value) {
            addCriterion("applet_end_time >", value, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("applet_end_time >=", value, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeLessThan(Date value) {
            addCriterion("applet_end_time <", value, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("applet_end_time <=", value, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeIn(List<Date> values) {
            addCriterion("applet_end_time in", values, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeNotIn(List<Date> values) {
            addCriterion("applet_end_time not in", values, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeBetween(Date value1, Date value2) {
            addCriterion("applet_end_time between", value1, value2, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andAppletEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("applet_end_time not between", value1, value2, "appletEndTime");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyIsNull() {
            addCriterion("reserve_field1_key is null");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyIsNotNull() {
            addCriterion("reserve_field1_key is not null");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyEqualTo(String value) {
            addCriterion("reserve_field1_key =", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyNotEqualTo(String value) {
            addCriterion("reserve_field1_key <>", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyGreaterThan(String value) {
            addCriterion("reserve_field1_key >", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyGreaterThanOrEqualTo(String value) {
            addCriterion("reserve_field1_key >=", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyLessThan(String value) {
            addCriterion("reserve_field1_key <", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyLessThanOrEqualTo(String value) {
            addCriterion("reserve_field1_key <=", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyLike(String value) {
            addCriterion("reserve_field1_key like", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyNotLike(String value) {
            addCriterion("reserve_field1_key not like", value, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyIn(List<String> values) {
            addCriterion("reserve_field1_key in", values, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyNotIn(List<String> values) {
            addCriterion("reserve_field1_key not in", values, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyBetween(String value1, String value2) {
            addCriterion("reserve_field1_key between", value1, value2, "reserveField1Key");
            return (Criteria) this;
        }

        public Criteria andReserveField1KeyNotBetween(String value1, String value2) {
            addCriterion("reserve_field1_key not between", value1, value2, "reserveField1Key");
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

        public Criteria andLabelMessageIsNull() {
            addCriterion("label_message is null");
            return (Criteria) this;
        }

        public Criteria andLabelMessageIsNotNull() {
            addCriterion("label_message is not null");
            return (Criteria) this;
        }

        public Criteria andLabelMessageEqualTo(String value) {
            addCriterion("label_message =", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageNotEqualTo(String value) {
            addCriterion("label_message <>", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageGreaterThan(String value) {
            addCriterion("label_message >", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageGreaterThanOrEqualTo(String value) {
            addCriterion("label_message >=", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageLessThan(String value) {
            addCriterion("label_message <", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageLessThanOrEqualTo(String value) {
            addCriterion("label_message <=", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageLike(String value) {
            addCriterion("label_message like", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageNotLike(String value) {
            addCriterion("label_message not like", value, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageIn(List<String> values) {
            addCriterion("label_message in", values, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageNotIn(List<String> values) {
            addCriterion("label_message not in", values, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageBetween(String value1, String value2) {
            addCriterion("label_message between", value1, value2, "labelMessage");
            return (Criteria) this;
        }

        public Criteria andLabelMessageNotBetween(String value1, String value2) {
            addCriterion("label_message not between", value1, value2, "labelMessage");
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