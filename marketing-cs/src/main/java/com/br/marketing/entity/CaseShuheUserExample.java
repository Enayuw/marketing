package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CaseShuheUserExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CaseShuheUserExample() {
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

        public Criteria andUploadDateIsNull() {
            addCriterion("upload_date is null");
            return (Criteria) this;
        }

        public Criteria andUploadDateIsNotNull() {
            addCriterion("upload_date is not null");
            return (Criteria) this;
        }

        public Criteria andUploadDateEqualTo(String value) {
            addCriterion("upload_date =", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateNotEqualTo(String value) {
            addCriterion("upload_date <>", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateGreaterThan(String value) {
            addCriterion("upload_date >", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateGreaterThanOrEqualTo(String value) {
            addCriterion("upload_date >=", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateLessThan(String value) {
            addCriterion("upload_date <", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateLessThanOrEqualTo(String value) {
            addCriterion("upload_date <=", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateLike(String value) {
            addCriterion("upload_date like", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateNotLike(String value) {
            addCriterion("upload_date not like", value, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateIn(List<String> values) {
            addCriterion("upload_date in", values, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateNotIn(List<String> values) {
            addCriterion("upload_date not in", values, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateBetween(String value1, String value2) {
            addCriterion("upload_date between", value1, value2, "uploadDate");
            return (Criteria) this;
        }

        public Criteria andUploadDateNotBetween(String value1, String value2) {
            addCriterion("upload_date not between", value1, value2, "uploadDate");
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

        public Criteria andMobileIsNull() {
            addCriterion("mobile is null");
            return (Criteria) this;
        }

        public Criteria andMobileIsNotNull() {
            addCriterion("mobile is not null");
            return (Criteria) this;
        }

        public Criteria andMobileEqualTo(String value) {
            addCriterion("mobile =", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileNotEqualTo(String value) {
            addCriterion("mobile <>", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileGreaterThan(String value) {
            addCriterion("mobile >", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileGreaterThanOrEqualTo(String value) {
            addCriterion("mobile >=", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileLessThan(String value) {
            addCriterion("mobile <", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileLessThanOrEqualTo(String value) {
            addCriterion("mobile <=", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileLike(String value) {
            addCriterion("mobile like", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileNotLike(String value) {
            addCriterion("mobile not like", value, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileIn(List<String> values) {
            addCriterion("mobile in", values, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileNotIn(List<String> values) {
            addCriterion("mobile not in", values, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileBetween(String value1, String value2) {
            addCriterion("mobile between", value1, value2, "mobile");
            return (Criteria) this;
        }

        public Criteria andMobileNotBetween(String value1, String value2) {
            addCriterion("mobile not between", value1, value2, "mobile");
            return (Criteria) this;
        }

        public Criteria andBiztypeIsNull() {
            addCriterion("bizType is null");
            return (Criteria) this;
        }

        public Criteria andBiztypeIsNotNull() {
            addCriterion("bizType is not null");
            return (Criteria) this;
        }

        public Criteria andBiztypeEqualTo(String value) {
            addCriterion("bizType =", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeNotEqualTo(String value) {
            addCriterion("bizType <>", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeGreaterThan(String value) {
            addCriterion("bizType >", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeGreaterThanOrEqualTo(String value) {
            addCriterion("bizType >=", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeLessThan(String value) {
            addCriterion("bizType <", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeLessThanOrEqualTo(String value) {
            addCriterion("bizType <=", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeLike(String value) {
            addCriterion("bizType like", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeNotLike(String value) {
            addCriterion("bizType not like", value, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeIn(List<String> values) {
            addCriterion("bizType in", values, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeNotIn(List<String> values) {
            addCriterion("bizType not in", values, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeBetween(String value1, String value2) {
            addCriterion("bizType between", value1, value2, "biztype");
            return (Criteria) this;
        }

        public Criteria andBiztypeNotBetween(String value1, String value2) {
            addCriterion("bizType not between", value1, value2, "biztype");
            return (Criteria) this;
        }

        public Criteria andIsBlackIsNull() {
            addCriterion("is_black is null");
            return (Criteria) this;
        }

        public Criteria andIsBlackIsNotNull() {
            addCriterion("is_black is not null");
            return (Criteria) this;
        }

        public Criteria andIsBlackEqualTo(String value) {
            addCriterion("is_black =", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackNotEqualTo(String value) {
            addCriterion("is_black <>", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackGreaterThan(String value) {
            addCriterion("is_black >", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackGreaterThanOrEqualTo(String value) {
            addCriterion("is_black >=", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackLessThan(String value) {
            addCriterion("is_black <", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackLessThanOrEqualTo(String value) {
            addCriterion("is_black <=", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackLike(String value) {
            addCriterion("is_black like", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackNotLike(String value) {
            addCriterion("is_black not like", value, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackIn(List<String> values) {
            addCriterion("is_black in", values, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackNotIn(List<String> values) {
            addCriterion("is_black not in", values, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackBetween(String value1, String value2) {
            addCriterion("is_black between", value1, value2, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsBlackNotBetween(String value1, String value2) {
            addCriterion("is_black not between", value1, value2, "isBlack");
            return (Criteria) this;
        }

        public Criteria andIsTurnIsNull() {
            addCriterion("is_turn is null");
            return (Criteria) this;
        }

        public Criteria andIsTurnIsNotNull() {
            addCriterion("is_turn is not null");
            return (Criteria) this;
        }

        public Criteria andIsTurnEqualTo(String value) {
            addCriterion("is_turn =", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnNotEqualTo(String value) {
            addCriterion("is_turn <>", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnGreaterThan(String value) {
            addCriterion("is_turn >", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnGreaterThanOrEqualTo(String value) {
            addCriterion("is_turn >=", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnLessThan(String value) {
            addCriterion("is_turn <", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnLessThanOrEqualTo(String value) {
            addCriterion("is_turn <=", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnLike(String value) {
            addCriterion("is_turn like", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnNotLike(String value) {
            addCriterion("is_turn not like", value, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnIn(List<String> values) {
            addCriterion("is_turn in", values, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnNotIn(List<String> values) {
            addCriterion("is_turn not in", values, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnBetween(String value1, String value2) {
            addCriterion("is_turn between", value1, value2, "isTurn");
            return (Criteria) this;
        }

        public Criteria andIsTurnNotBetween(String value1, String value2) {
            addCriterion("is_turn not between", value1, value2, "isTurn");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllIsNull() {
            addCriterion("clc_usr_fst_log_tim_all is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllIsNotNull() {
            addCriterion("clc_usr_fst_log_tim_all is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllEqualTo(String value) {
            addCriterion("clc_usr_fst_log_tim_all =", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllNotEqualTo(String value) {
            addCriterion("clc_usr_fst_log_tim_all <>", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllGreaterThan(String value) {
            addCriterion("clc_usr_fst_log_tim_all >", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_fst_log_tim_all >=", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllLessThan(String value) {
            addCriterion("clc_usr_fst_log_tim_all <", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_fst_log_tim_all <=", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllLike(String value) {
            addCriterion("clc_usr_fst_log_tim_all like", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllNotLike(String value) {
            addCriterion("clc_usr_fst_log_tim_all not like", value, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllIn(List<String> values) {
            addCriterion("clc_usr_fst_log_tim_all in", values, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllNotIn(List<String> values) {
            addCriterion("clc_usr_fst_log_tim_all not in", values, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllBetween(String value1, String value2) {
            addCriterion("clc_usr_fst_log_tim_all between", value1, value2, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLogTimAllNotBetween(String value1, String value2) {
            addCriterion("clc_usr_fst_log_tim_all not between", value1, value2, "clcUsrFstLogTimAll");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimIsNull() {
            addCriterion("clc_usr_lst_app_sta_tim is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimIsNotNull() {
            addCriterion("clc_usr_lst_app_sta_tim is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimEqualTo(String value) {
            addCriterion("clc_usr_lst_app_sta_tim =", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimNotEqualTo(String value) {
            addCriterion("clc_usr_lst_app_sta_tim <>", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimGreaterThan(String value) {
            addCriterion("clc_usr_lst_app_sta_tim >", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_lst_app_sta_tim >=", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimLessThan(String value) {
            addCriterion("clc_usr_lst_app_sta_tim <", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_lst_app_sta_tim <=", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimLike(String value) {
            addCriterion("clc_usr_lst_app_sta_tim like", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimNotLike(String value) {
            addCriterion("clc_usr_lst_app_sta_tim not like", value, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimIn(List<String> values) {
            addCriterion("clc_usr_lst_app_sta_tim in", values, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimNotIn(List<String> values) {
            addCriterion("clc_usr_lst_app_sta_tim not in", values, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimBetween(String value1, String value2) {
            addCriterion("clc_usr_lst_app_sta_tim between", value1, value2, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrLstAppStaTimNotBetween(String value1, String value2) {
            addCriterion("clc_usr_lst_app_sta_tim not between", value1, value2, "clcUsrLstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimIsNull() {
            addCriterion("clc_usr_iso_pho_tim is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimIsNotNull() {
            addCriterion("clc_usr_iso_pho_tim is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimEqualTo(String value) {
            addCriterion("clc_usr_iso_pho_tim =", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimNotEqualTo(String value) {
            addCriterion("clc_usr_iso_pho_tim <>", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimGreaterThan(String value) {
            addCriterion("clc_usr_iso_pho_tim >", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_pho_tim >=", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimLessThan(String value) {
            addCriterion("clc_usr_iso_pho_tim <", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_pho_tim <=", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimLike(String value) {
            addCriterion("clc_usr_iso_pho_tim like", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimNotLike(String value) {
            addCriterion("clc_usr_iso_pho_tim not like", value, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimIn(List<String> values) {
            addCriterion("clc_usr_iso_pho_tim in", values, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimNotIn(List<String> values) {
            addCriterion("clc_usr_iso_pho_tim not in", values, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_pho_tim between", value1, value2, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoPhoTimNotBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_pho_tim not between", value1, value2, "clcUsrIsoPhoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimIsNull() {
            addCriterion("clc_usr_iso_idt_tim is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimIsNotNull() {
            addCriterion("clc_usr_iso_idt_tim is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimEqualTo(String value) {
            addCriterion("clc_usr_iso_idt_tim =", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimNotEqualTo(String value) {
            addCriterion("clc_usr_iso_idt_tim <>", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimGreaterThan(String value) {
            addCriterion("clc_usr_iso_idt_tim >", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_idt_tim >=", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimLessThan(String value) {
            addCriterion("clc_usr_iso_idt_tim <", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_idt_tim <=", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimLike(String value) {
            addCriterion("clc_usr_iso_idt_tim like", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimNotLike(String value) {
            addCriterion("clc_usr_iso_idt_tim not like", value, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimIn(List<String> values) {
            addCriterion("clc_usr_iso_idt_tim in", values, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimNotIn(List<String> values) {
            addCriterion("clc_usr_iso_idt_tim not in", values, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_idt_tim between", value1, value2, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoIdtTimNotBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_idt_tim not between", value1, value2, "clcUsrIsoIdtTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimIsNull() {
            addCriterion("clc_usr_iso_crd_tim is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimIsNotNull() {
            addCriterion("clc_usr_iso_crd_tim is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimEqualTo(String value) {
            addCriterion("clc_usr_iso_crd_tim =", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimNotEqualTo(String value) {
            addCriterion("clc_usr_iso_crd_tim <>", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimGreaterThan(String value) {
            addCriterion("clc_usr_iso_crd_tim >", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_crd_tim >=", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimLessThan(String value) {
            addCriterion("clc_usr_iso_crd_tim <", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_crd_tim <=", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimLike(String value) {
            addCriterion("clc_usr_iso_crd_tim like", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimNotLike(String value) {
            addCriterion("clc_usr_iso_crd_tim not like", value, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimIn(List<String> values) {
            addCriterion("clc_usr_iso_crd_tim in", values, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimNotIn(List<String> values) {
            addCriterion("clc_usr_iso_crd_tim not in", values, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_crd_tim between", value1, value2, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoCrdTimNotBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_crd_tim not between", value1, value2, "clcUsrIsoCrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimIsNull() {
            addCriterion("clc_usr_iso_inf_tim is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimIsNotNull() {
            addCriterion("clc_usr_iso_inf_tim is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimEqualTo(String value) {
            addCriterion("clc_usr_iso_inf_tim =", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimNotEqualTo(String value) {
            addCriterion("clc_usr_iso_inf_tim <>", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimGreaterThan(String value) {
            addCriterion("clc_usr_iso_inf_tim >", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_inf_tim >=", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimLessThan(String value) {
            addCriterion("clc_usr_iso_inf_tim <", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_inf_tim <=", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimLike(String value) {
            addCriterion("clc_usr_iso_inf_tim like", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimNotLike(String value) {
            addCriterion("clc_usr_iso_inf_tim not like", value, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimIn(List<String> values) {
            addCriterion("clc_usr_iso_inf_tim in", values, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimNotIn(List<String> values) {
            addCriterion("clc_usr_iso_inf_tim not in", values, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_inf_tim between", value1, value2, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoInfTimNotBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_inf_tim not between", value1, value2, "clcUsrIsoInfTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimIsNull() {
            addCriterion("clc_usr_iso_ato_tim is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimIsNotNull() {
            addCriterion("clc_usr_iso_ato_tim is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimEqualTo(String value) {
            addCriterion("clc_usr_iso_ato_tim =", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimNotEqualTo(String value) {
            addCriterion("clc_usr_iso_ato_tim <>", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimGreaterThan(String value) {
            addCriterion("clc_usr_iso_ato_tim >", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_ato_tim >=", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimLessThan(String value) {
            addCriterion("clc_usr_iso_ato_tim <", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_iso_ato_tim <=", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimLike(String value) {
            addCriterion("clc_usr_iso_ato_tim like", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimNotLike(String value) {
            addCriterion("clc_usr_iso_ato_tim not like", value, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimIn(List<String> values) {
            addCriterion("clc_usr_iso_ato_tim in", values, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimNotIn(List<String> values) {
            addCriterion("clc_usr_iso_ato_tim not in", values, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_ato_tim between", value1, value2, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrIsoAtoTimNotBetween(String value1, String value2) {
            addCriterion("clc_usr_iso_ato_tim not between", value1, value2, "clcUsrIsoAtoTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonIsNull() {
            addCriterion("clc_usr_adt_tim_rcn_lon is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonIsNotNull() {
            addCriterion("clc_usr_adt_tim_rcn_lon is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonEqualTo(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon =", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonNotEqualTo(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon <>", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonGreaterThan(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon >", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon >=", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonLessThan(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon <", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon <=", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonLike(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon like", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonNotLike(String value) {
            addCriterion("clc_usr_adt_tim_rcn_lon not like", value, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonIn(List<String> values) {
            addCriterion("clc_usr_adt_tim_rcn_lon in", values, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonNotIn(List<String> values) {
            addCriterion("clc_usr_adt_tim_rcn_lon not in", values, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonBetween(String value1, String value2) {
            addCriterion("clc_usr_adt_tim_rcn_lon between", value1, value2, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtTimRcnLonNotBetween(String value1, String value2) {
            addCriterion("clc_usr_adt_tim_rcn_lon not between", value1, value2, "clcUsrAdtTimRcnLon");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrIsNull() {
            addCriterion("clc_usr_adt_lmt_itr is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrIsNotNull() {
            addCriterion("clc_usr_adt_lmt_itr is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrEqualTo(String value) {
            addCriterion("clc_usr_adt_lmt_itr =", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrNotEqualTo(String value) {
            addCriterion("clc_usr_adt_lmt_itr <>", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrGreaterThan(String value) {
            addCriterion("clc_usr_adt_lmt_itr >", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_adt_lmt_itr >=", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrLessThan(String value) {
            addCriterion("clc_usr_adt_lmt_itr <", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_adt_lmt_itr <=", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrLike(String value) {
            addCriterion("clc_usr_adt_lmt_itr like", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrNotLike(String value) {
            addCriterion("clc_usr_adt_lmt_itr not like", value, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrIn(List<String> values) {
            addCriterion("clc_usr_adt_lmt_itr in", values, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrNotIn(List<String> values) {
            addCriterion("clc_usr_adt_lmt_itr not in", values, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrBetween(String value1, String value2) {
            addCriterion("clc_usr_adt_lmt_itr between", value1, value2, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrAdtLmtItrNotBetween(String value1, String value2) {
            addCriterion("clc_usr_adt_lmt_itr not between", value1, value2, "clcUsrAdtLmtItr");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimIsNull() {
            addCriterion("clc_usr_frt_fq_ord_tim is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimIsNotNull() {
            addCriterion("clc_usr_frt_fq_ord_tim is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimEqualTo(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim =", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimNotEqualTo(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim <>", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimGreaterThan(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim >", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim >=", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimLessThan(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim <", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim <=", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimLike(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim like", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimNotLike(String value) {
            addCriterion("clc_usr_frt_fq_ord_tim not like", value, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimIn(List<String> values) {
            addCriterion("clc_usr_frt_fq_ord_tim in", values, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimNotIn(List<String> values) {
            addCriterion("clc_usr_frt_fq_ord_tim not in", values, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimBetween(String value1, String value2) {
            addCriterion("clc_usr_frt_fq_ord_tim between", value1, value2, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFrtFqOrdTimNotBetween(String value1, String value2) {
            addCriterion("clc_usr_frt_fq_ord_tim not between", value1, value2, "clcUsrFrtFqOrdTim");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlIsNull() {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlIsNotNull() {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlEqualTo(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl =", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlNotEqualTo(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl <>", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlGreaterThan(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl >", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl >=", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlLessThan(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl <", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl <=", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlLike(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl like", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlNotLike(String value) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl not like", value, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlIn(List<String> values) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl in", values, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlNotIn(List<String> values) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl not in", values, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlBetween(String value1, String value2) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl between", value1, value2, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andClcUsrFstLndTimCshBtHlNotBetween(String value1, String value2) {
            addCriterion("clc_usr_fst_lnd_tim_csh_bt_hl not between", value1, value2, "clcUsrFstLndTimCshBtHl");
            return (Criteria) this;
        }

        public Criteria andJsonDataIsNull() {
            addCriterion("json_data is null");
            return (Criteria) this;
        }

        public Criteria andJsonDataIsNotNull() {
            addCriterion("json_data is not null");
            return (Criteria) this;
        }

        public Criteria andJsonDataEqualTo(String value) {
            addCriterion("json_data =", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataNotEqualTo(String value) {
            addCriterion("json_data <>", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataGreaterThan(String value) {
            addCriterion("json_data >", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataGreaterThanOrEqualTo(String value) {
            addCriterion("json_data >=", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataLessThan(String value) {
            addCriterion("json_data <", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataLessThanOrEqualTo(String value) {
            addCriterion("json_data <=", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataLike(String value) {
            addCriterion("json_data like", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataNotLike(String value) {
            addCriterion("json_data not like", value, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataIn(List<String> values) {
            addCriterion("json_data in", values, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataNotIn(List<String> values) {
            addCriterion("json_data not in", values, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataBetween(String value1, String value2) {
            addCriterion("json_data between", value1, value2, "jsonData");
            return (Criteria) this;
        }

        public Criteria andJsonDataNotBetween(String value1, String value2) {
            addCriterion("json_data not between", value1, value2, "jsonData");
            return (Criteria) this;
        }

        public Criteria andIsTransferIsNull() {
            addCriterion("is_transfer is null");
            return (Criteria) this;
        }

        public Criteria andIsTransferIsNotNull() {
            addCriterion("is_transfer is not null");
            return (Criteria) this;
        }

        public Criteria andIsTransferEqualTo(Integer value) {
            addCriterion("is_transfer =", value, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferNotEqualTo(Integer value) {
            addCriterion("is_transfer <>", value, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferGreaterThan(Integer value) {
            addCriterion("is_transfer >", value, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_transfer >=", value, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferLessThan(Integer value) {
            addCriterion("is_transfer <", value, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferLessThanOrEqualTo(Integer value) {
            addCriterion("is_transfer <=", value, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferIn(List<Integer> values) {
            addCriterion("is_transfer in", values, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferNotIn(List<Integer> values) {
            addCriterion("is_transfer not in", values, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferBetween(Integer value1, Integer value2) {
            addCriterion("is_transfer between", value1, value2, "isTransfer");
            return (Criteria) this;
        }

        public Criteria andIsTransferNotBetween(Integer value1, Integer value2) {
            addCriterion("is_transfer not between", value1, value2, "isTransfer");
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

        public Criteria andReserveField2IsNull() {
            addCriterion("reserve_field2 is null");
            return (Criteria) this;
        }

        public Criteria andReserveField2IsNotNull() {
            addCriterion("reserve_field2 is not null");
            return (Criteria) this;
        }

        public Criteria andReserveField2EqualTo(String value) {
            addCriterion("reserve_field2 =", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2NotEqualTo(String value) {
            addCriterion("reserve_field2 <>", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2GreaterThan(String value) {
            addCriterion("reserve_field2 >", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2GreaterThanOrEqualTo(String value) {
            addCriterion("reserve_field2 >=", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2LessThan(String value) {
            addCriterion("reserve_field2 <", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2LessThanOrEqualTo(String value) {
            addCriterion("reserve_field2 <=", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2Like(String value) {
            addCriterion("reserve_field2 like", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2NotLike(String value) {
            addCriterion("reserve_field2 not like", value, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2In(List<String> values) {
            addCriterion("reserve_field2 in", values, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2NotIn(List<String> values) {
            addCriterion("reserve_field2 not in", values, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2Between(String value1, String value2) {
            addCriterion("reserve_field2 between", value1, value2, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andReserveField2NotBetween(String value1, String value2) {
            addCriterion("reserve_field2 not between", value1, value2, "reserveField2");
            return (Criteria) this;
        }

        public Criteria andErrorInfoIsNull() {
            addCriterion("error_info is null");
            return (Criteria) this;
        }

        public Criteria andErrorInfoIsNotNull() {
            addCriterion("error_info is not null");
            return (Criteria) this;
        }

        public Criteria andErrorInfoEqualTo(String value) {
            addCriterion("error_info =", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoNotEqualTo(String value) {
            addCriterion("error_info <>", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoGreaterThan(String value) {
            addCriterion("error_info >", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoGreaterThanOrEqualTo(String value) {
            addCriterion("error_info >=", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoLessThan(String value) {
            addCriterion("error_info <", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoLessThanOrEqualTo(String value) {
            addCriterion("error_info <=", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoLike(String value) {
            addCriterion("error_info like", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoNotLike(String value) {
            addCriterion("error_info not like", value, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoIn(List<String> values) {
            addCriterion("error_info in", values, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoNotIn(List<String> values) {
            addCriterion("error_info not in", values, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoBetween(String value1, String value2) {
            addCriterion("error_info between", value1, value2, "errorInfo");
            return (Criteria) this;
        }

        public Criteria andErrorInfoNotBetween(String value1, String value2) {
            addCriterion("error_info not between", value1, value2, "errorInfo");
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

        public Criteria andSaveStatusIsNull() {
            addCriterion("save_status is null");
            return (Criteria) this;
        }

        public Criteria andSaveStatusIsNotNull() {
            addCriterion("save_status is not null");
            return (Criteria) this;
        }

        public Criteria andSaveStatusEqualTo(Integer value) {
            addCriterion("save_status =", value, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusNotEqualTo(Integer value) {
            addCriterion("save_status <>", value, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusGreaterThan(Integer value) {
            addCriterion("save_status >", value, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("save_status >=", value, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusLessThan(Integer value) {
            addCriterion("save_status <", value, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusLessThanOrEqualTo(Integer value) {
            addCriterion("save_status <=", value, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusIn(List<Integer> values) {
            addCriterion("save_status in", values, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusNotIn(List<Integer> values) {
            addCriterion("save_status not in", values, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusBetween(Integer value1, Integer value2) {
            addCriterion("save_status between", value1, value2, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andSaveStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("save_status not between", value1, value2, "saveStatus");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndIsNull() {
            addCriterion("clc_usr_max_dx_rrt_end is null");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndIsNotNull() {
            addCriterion("clc_usr_max_dx_rrt_end is not null");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndEqualTo(String value) {
            addCriterion("clc_usr_max_dx_rrt_end =", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndNotEqualTo(String value) {
            addCriterion("clc_usr_max_dx_rrt_end <>", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndGreaterThan(String value) {
            addCriterion("clc_usr_max_dx_rrt_end >", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndGreaterThanOrEqualTo(String value) {
            addCriterion("clc_usr_max_dx_rrt_end >=", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndLessThan(String value) {
            addCriterion("clc_usr_max_dx_rrt_end <", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndLessThanOrEqualTo(String value) {
            addCriterion("clc_usr_max_dx_rrt_end <=", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndLike(String value) {
            addCriterion("clc_usr_max_dx_rrt_end like", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndNotLike(String value) {
            addCriterion("clc_usr_max_dx_rrt_end not like", value, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndIn(List<String> values) {
            addCriterion("clc_usr_max_dx_rrt_end in", values, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndNotIn(List<String> values) {
            addCriterion("clc_usr_max_dx_rrt_end not in", values, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndBetween(String value1, String value2) {
            addCriterion("clc_usr_max_dx_rrt_end between", value1, value2, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andClcUsrMaxDxRrtEndNotBetween(String value1, String value2) {
            addCriterion("clc_usr_max_dx_rrt_end not between", value1, value2, "clcUsrMaxDxRrtEnd");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimIsNull() {
            addCriterion("usr_forbid_call_end_tim is null");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimIsNotNull() {
            addCriterion("usr_forbid_call_end_tim is not null");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimEqualTo(String value) {
            addCriterion("usr_forbid_call_end_tim =", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimNotEqualTo(String value) {
            addCriterion("usr_forbid_call_end_tim <>", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimGreaterThan(String value) {
            addCriterion("usr_forbid_call_end_tim >", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimGreaterThanOrEqualTo(String value) {
            addCriterion("usr_forbid_call_end_tim >=", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimLessThan(String value) {
            addCriterion("usr_forbid_call_end_tim <", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimLessThanOrEqualTo(String value) {
            addCriterion("usr_forbid_call_end_tim <=", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimLike(String value) {
            addCriterion("usr_forbid_call_end_tim like", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimNotLike(String value) {
            addCriterion("usr_forbid_call_end_tim not like", value, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimIn(List<String> values) {
            addCriterion("usr_forbid_call_end_tim in", values, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimNotIn(List<String> values) {
            addCriterion("usr_forbid_call_end_tim not in", values, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimBetween(String value1, String value2) {
            addCriterion("usr_forbid_call_end_tim between", value1, value2, "usrForbidCallEndTim");
            return (Criteria) this;
        }

        public Criteria andUsrForbidCallEndTimNotBetween(String value1, String value2) {
            addCriterion("usr_forbid_call_end_tim not between", value1, value2, "usrForbidCallEndTim");
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