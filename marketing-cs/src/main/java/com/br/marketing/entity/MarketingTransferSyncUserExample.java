package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingTransferSyncUserExample {

    private String tCid;

    public String gettCid() {
        return tCid;
    }

    public void settCid(String tCid) {
        this.tCid = tCid;
    }

    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingTransferSyncUserExample() {
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

        public Criteria andOrgNameIsNull() {
            addCriterion("org_name is null");
            return (Criteria) this;
        }

        public Criteria andOrgNameIsNotNull() {
            addCriterion("org_name is not null");
            return (Criteria) this;
        }

        public Criteria andOrgNameEqualTo(String value) {
            addCriterion("org_name =", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameNotEqualTo(String value) {
            addCriterion("org_name <>", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameGreaterThan(String value) {
            addCriterion("org_name >", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameGreaterThanOrEqualTo(String value) {
            addCriterion("org_name >=", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameLessThan(String value) {
            addCriterion("org_name <", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameLessThanOrEqualTo(String value) {
            addCriterion("org_name <=", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameLike(String value) {
            addCriterion("org_name like", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameNotLike(String value) {
            addCriterion("org_name not like", value, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameIn(List<String> values) {
            addCriterion("org_name in", values, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameNotIn(List<String> values) {
            addCriterion("org_name not in", values, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameBetween(String value1, String value2) {
            addCriterion("org_name between", value1, value2, "orgName");
            return (Criteria) this;
        }

        public Criteria andOrgNameNotBetween(String value1, String value2) {
            addCriterion("org_name not between", value1, value2, "orgName");
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

        public Criteria andSourceIsNull() {
            addCriterion("source is null");
            return (Criteria) this;
        }

        public Criteria andSourceIsNotNull() {
            addCriterion("source is not null");
            return (Criteria) this;
        }

        public Criteria andSourceEqualTo(String value) {
            addCriterion("source =", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotEqualTo(String value) {
            addCriterion("source <>", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceGreaterThan(String value) {
            addCriterion("source >", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceGreaterThanOrEqualTo(String value) {
            addCriterion("source >=", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLessThan(String value) {
            addCriterion("source <", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLessThanOrEqualTo(String value) {
            addCriterion("source <=", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceLike(String value) {
            addCriterion("source like", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotLike(String value) {
            addCriterion("source not like", value, "source");
            return (Criteria) this;
        }

        public Criteria andSourceIn(List<String> values) {
            addCriterion("source in", values, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotIn(List<String> values) {
            addCriterion("source not in", values, "source");
            return (Criteria) this;
        }

        public Criteria andSourceBetween(String value1, String value2) {
            addCriterion("source between", value1, value2, "source");
            return (Criteria) this;
        }

        public Criteria andSourceNotBetween(String value1, String value2) {
            addCriterion("source not between", value1, value2, "source");
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

        public Criteria andTypeIsNull() {
            addCriterion("type is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(String value) {
            addCriterion("type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(String value) {
            addCriterion("type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(String value) {
            addCriterion("type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(String value) {
            addCriterion("type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(String value) {
            addCriterion("type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(String value) {
            addCriterion("type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLike(String value) {
            addCriterion("type like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotLike(String value) {
            addCriterion("type not like", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<String> values) {
            addCriterion("type in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<String> values) {
            addCriterion("type not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(String value1, String value2) {
            addCriterion("type between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(String value1, String value2) {
            addCriterion("type not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andCustomNameIsNull() {
            addCriterion("custom_name is null");
            return (Criteria) this;
        }

        public Criteria andCustomNameIsNotNull() {
            addCriterion("custom_name is not null");
            return (Criteria) this;
        }

        public Criteria andCustomNameEqualTo(String value) {
            addCriterion("custom_name =", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameNotEqualTo(String value) {
            addCriterion("custom_name <>", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameGreaterThan(String value) {
            addCriterion("custom_name >", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameGreaterThanOrEqualTo(String value) {
            addCriterion("custom_name >=", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameLessThan(String value) {
            addCriterion("custom_name <", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameLessThanOrEqualTo(String value) {
            addCriterion("custom_name <=", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameLike(String value) {
            addCriterion("custom_name like", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameNotLike(String value) {
            addCriterion("custom_name not like", value, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameIn(List<String> values) {
            addCriterion("custom_name in", values, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameNotIn(List<String> values) {
            addCriterion("custom_name not in", values, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameBetween(String value1, String value2) {
            addCriterion("custom_name between", value1, value2, "customName");
            return (Criteria) this;
        }

        public Criteria andCustomNameNotBetween(String value1, String value2) {
            addCriterion("custom_name not between", value1, value2, "customName");
            return (Criteria) this;
        }

        public Criteria andIfRegisterIsNull() {
            addCriterion("if_register is null");
            return (Criteria) this;
        }

        public Criteria andIfRegisterIsNotNull() {
            addCriterion("if_register is not null");
            return (Criteria) this;
        }

        public Criteria andIfRegisterEqualTo(String value) {
            addCriterion("if_register =", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterNotEqualTo(String value) {
            addCriterion("if_register <>", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterGreaterThan(String value) {
            addCriterion("if_register >", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterGreaterThanOrEqualTo(String value) {
            addCriterion("if_register >=", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterLessThan(String value) {
            addCriterion("if_register <", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterLessThanOrEqualTo(String value) {
            addCriterion("if_register <=", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterLike(String value) {
            addCriterion("if_register like", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterNotLike(String value) {
            addCriterion("if_register not like", value, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterIn(List<String> values) {
            addCriterion("if_register in", values, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterNotIn(List<String> values) {
            addCriterion("if_register not in", values, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterBetween(String value1, String value2) {
            addCriterion("if_register between", value1, value2, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andIfRegisterNotBetween(String value1, String value2) {
            addCriterion("if_register not between", value1, value2, "ifRegister");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeIsNull() {
            addCriterion("register_time is null");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeIsNotNull() {
            addCriterion("register_time is not null");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeEqualTo(String value) {
            addCriterion("register_time =", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeNotEqualTo(String value) {
            addCriterion("register_time <>", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeGreaterThan(String value) {
            addCriterion("register_time >", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeGreaterThanOrEqualTo(String value) {
            addCriterion("register_time >=", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeLessThan(String value) {
            addCriterion("register_time <", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeLessThanOrEqualTo(String value) {
            addCriterion("register_time <=", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeLike(String value) {
            addCriterion("register_time like", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeNotLike(String value) {
            addCriterion("register_time not like", value, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeIn(List<String> values) {
            addCriterion("register_time in", values, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeNotIn(List<String> values) {
            addCriterion("register_time not in", values, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeBetween(String value1, String value2) {
            addCriterion("register_time between", value1, value2, "registerTime");
            return (Criteria) this;
        }

        public Criteria andRegisterTimeNotBetween(String value1, String value2) {
            addCriterion("register_time not between", value1, value2, "registerTime");
            return (Criteria) this;
        }

        public Criteria andIfLoginIsNull() {
            addCriterion("if_login is null");
            return (Criteria) this;
        }

        public Criteria andIfLoginIsNotNull() {
            addCriterion("if_login is not null");
            return (Criteria) this;
        }

        public Criteria andIfLoginEqualTo(String value) {
            addCriterion("if_login =", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginNotEqualTo(String value) {
            addCriterion("if_login <>", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginGreaterThan(String value) {
            addCriterion("if_login >", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginGreaterThanOrEqualTo(String value) {
            addCriterion("if_login >=", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginLessThan(String value) {
            addCriterion("if_login <", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginLessThanOrEqualTo(String value) {
            addCriterion("if_login <=", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginLike(String value) {
            addCriterion("if_login like", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginNotLike(String value) {
            addCriterion("if_login not like", value, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginIn(List<String> values) {
            addCriterion("if_login in", values, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginNotIn(List<String> values) {
            addCriterion("if_login not in", values, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginBetween(String value1, String value2) {
            addCriterion("if_login between", value1, value2, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andIfLoginNotBetween(String value1, String value2) {
            addCriterion("if_login not between", value1, value2, "ifLogin");
            return (Criteria) this;
        }

        public Criteria andLoginTimeIsNull() {
            addCriterion("login_time is null");
            return (Criteria) this;
        }

        public Criteria andLoginTimeIsNotNull() {
            addCriterion("login_time is not null");
            return (Criteria) this;
        }

        public Criteria andLoginTimeEqualTo(String value) {
            addCriterion("login_time =", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeNotEqualTo(String value) {
            addCriterion("login_time <>", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeGreaterThan(String value) {
            addCriterion("login_time >", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeGreaterThanOrEqualTo(String value) {
            addCriterion("login_time >=", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeLessThan(String value) {
            addCriterion("login_time <", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeLessThanOrEqualTo(String value) {
            addCriterion("login_time <=", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeLike(String value) {
            addCriterion("login_time like", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeNotLike(String value) {
            addCriterion("login_time not like", value, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeIn(List<String> values) {
            addCriterion("login_time in", values, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeNotIn(List<String> values) {
            addCriterion("login_time not in", values, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeBetween(String value1, String value2) {
            addCriterion("login_time between", value1, value2, "loginTime");
            return (Criteria) this;
        }

        public Criteria andLoginTimeNotBetween(String value1, String value2) {
            addCriterion("login_time not between", value1, value2, "loginTime");
            return (Criteria) this;
        }

        public Criteria andIfApplyIsNull() {
            addCriterion("if_apply is null");
            return (Criteria) this;
        }

        public Criteria andIfApplyIsNotNull() {
            addCriterion("if_apply is not null");
            return (Criteria) this;
        }

        public Criteria andIfApplyEqualTo(String value) {
            addCriterion("if_apply =", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyNotEqualTo(String value) {
            addCriterion("if_apply <>", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyGreaterThan(String value) {
            addCriterion("if_apply >", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyGreaterThanOrEqualTo(String value) {
            addCriterion("if_apply >=", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyLessThan(String value) {
            addCriterion("if_apply <", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyLessThanOrEqualTo(String value) {
            addCriterion("if_apply <=", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyLike(String value) {
            addCriterion("if_apply like", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyNotLike(String value) {
            addCriterion("if_apply not like", value, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyIn(List<String> values) {
            addCriterion("if_apply in", values, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyNotIn(List<String> values) {
            addCriterion("if_apply not in", values, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyBetween(String value1, String value2) {
            addCriterion("if_apply between", value1, value2, "ifApply");
            return (Criteria) this;
        }

        public Criteria andIfApplyNotBetween(String value1, String value2) {
            addCriterion("if_apply not between", value1, value2, "ifApply");
            return (Criteria) this;
        }

        public Criteria andApplyDtIsNull() {
            addCriterion("apply_dt is null");
            return (Criteria) this;
        }

        public Criteria andApplyDtIsNotNull() {
            addCriterion("apply_dt is not null");
            return (Criteria) this;
        }

        public Criteria andApplyDtEqualTo(String value) {
            addCriterion("apply_dt =", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtNotEqualTo(String value) {
            addCriterion("apply_dt <>", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtGreaterThan(String value) {
            addCriterion("apply_dt >", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtGreaterThanOrEqualTo(String value) {
            addCriterion("apply_dt >=", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtLessThan(String value) {
            addCriterion("apply_dt <", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtLessThanOrEqualTo(String value) {
            addCriterion("apply_dt <=", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtLike(String value) {
            addCriterion("apply_dt like", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtNotLike(String value) {
            addCriterion("apply_dt not like", value, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtIn(List<String> values) {
            addCriterion("apply_dt in", values, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtNotIn(List<String> values) {
            addCriterion("apply_dt not in", values, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtBetween(String value1, String value2) {
            addCriterion("apply_dt between", value1, value2, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyDtNotBetween(String value1, String value2) {
            addCriterion("apply_dt not between", value1, value2, "applyDt");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIsNull() {
            addCriterion("apply_time is null");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIsNotNull() {
            addCriterion("apply_time is not null");
            return (Criteria) this;
        }

        public Criteria andApplyTimeEqualTo(String value) {
            addCriterion("apply_time =", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotEqualTo(String value) {
            addCriterion("apply_time <>", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThan(String value) {
            addCriterion("apply_time >", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeGreaterThanOrEqualTo(String value) {
            addCriterion("apply_time >=", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThan(String value) {
            addCriterion("apply_time <", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeLessThanOrEqualTo(String value) {
            addCriterion("apply_time <=", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeLike(String value) {
            addCriterion("apply_time like", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotLike(String value) {
            addCriterion("apply_time not like", value, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeIn(List<String> values) {
            addCriterion("apply_time in", values, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotIn(List<String> values) {
            addCriterion("apply_time not in", values, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeBetween(String value1, String value2) {
            addCriterion("apply_time between", value1, value2, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyTimeNotBetween(String value1, String value2) {
            addCriterion("apply_time not between", value1, value2, "applyTime");
            return (Criteria) this;
        }

        public Criteria andApplyResultIsNull() {
            addCriterion("apply_result is null");
            return (Criteria) this;
        }

        public Criteria andApplyResultIsNotNull() {
            addCriterion("apply_result is not null");
            return (Criteria) this;
        }

        public Criteria andApplyResultEqualTo(String value) {
            addCriterion("apply_result =", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultNotEqualTo(String value) {
            addCriterion("apply_result <>", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultGreaterThan(String value) {
            addCriterion("apply_result >", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultGreaterThanOrEqualTo(String value) {
            addCriterion("apply_result >=", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultLessThan(String value) {
            addCriterion("apply_result <", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultLessThanOrEqualTo(String value) {
            addCriterion("apply_result <=", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultLike(String value) {
            addCriterion("apply_result like", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultNotLike(String value) {
            addCriterion("apply_result not like", value, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultIn(List<String> values) {
            addCriterion("apply_result in", values, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultNotIn(List<String> values) {
            addCriterion("apply_result not in", values, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultBetween(String value1, String value2) {
            addCriterion("apply_result between", value1, value2, "applyResult");
            return (Criteria) this;
        }

        public Criteria andApplyResultNotBetween(String value1, String value2) {
            addCriterion("apply_result not between", value1, value2, "applyResult");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeIsNull() {
            addCriterion("refuse_time is null");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeIsNotNull() {
            addCriterion("refuse_time is not null");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeEqualTo(String value) {
            addCriterion("refuse_time =", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeNotEqualTo(String value) {
            addCriterion("refuse_time <>", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeGreaterThan(String value) {
            addCriterion("refuse_time >", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeGreaterThanOrEqualTo(String value) {
            addCriterion("refuse_time >=", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeLessThan(String value) {
            addCriterion("refuse_time <", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeLessThanOrEqualTo(String value) {
            addCriterion("refuse_time <=", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeLike(String value) {
            addCriterion("refuse_time like", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeNotLike(String value) {
            addCriterion("refuse_time not like", value, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeIn(List<String> values) {
            addCriterion("refuse_time in", values, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeNotIn(List<String> values) {
            addCriterion("refuse_time not in", values, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeBetween(String value1, String value2) {
            addCriterion("refuse_time between", value1, value2, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andRefuseTimeNotBetween(String value1, String value2) {
            addCriterion("refuse_time not between", value1, value2, "refuseTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeIsNull() {
            addCriterion("audit_time is null");
            return (Criteria) this;
        }

        public Criteria andAuditTimeIsNotNull() {
            addCriterion("audit_time is not null");
            return (Criteria) this;
        }

        public Criteria andAuditTimeEqualTo(String value) {
            addCriterion("audit_time =", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeNotEqualTo(String value) {
            addCriterion("audit_time <>", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeGreaterThan(String value) {
            addCriterion("audit_time >", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeGreaterThanOrEqualTo(String value) {
            addCriterion("audit_time >=", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeLessThan(String value) {
            addCriterion("audit_time <", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeLessThanOrEqualTo(String value) {
            addCriterion("audit_time <=", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeLike(String value) {
            addCriterion("audit_time like", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeNotLike(String value) {
            addCriterion("audit_time not like", value, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeIn(List<String> values) {
            addCriterion("audit_time in", values, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeNotIn(List<String> values) {
            addCriterion("audit_time not in", values, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeBetween(String value1, String value2) {
            addCriterion("audit_time between", value1, value2, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditTimeNotBetween(String value1, String value2) {
            addCriterion("audit_time not between", value1, value2, "auditTime");
            return (Criteria) this;
        }

        public Criteria andAuditAmountIsNull() {
            addCriterion("audit_amount is null");
            return (Criteria) this;
        }

        public Criteria andAuditAmountIsNotNull() {
            addCriterion("audit_amount is not null");
            return (Criteria) this;
        }

        public Criteria andAuditAmountEqualTo(String value) {
            addCriterion("audit_amount =", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountNotEqualTo(String value) {
            addCriterion("audit_amount <>", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountGreaterThan(String value) {
            addCriterion("audit_amount >", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountGreaterThanOrEqualTo(String value) {
            addCriterion("audit_amount >=", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountLessThan(String value) {
            addCriterion("audit_amount <", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountLessThanOrEqualTo(String value) {
            addCriterion("audit_amount <=", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountLike(String value) {
            addCriterion("audit_amount like", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountNotLike(String value) {
            addCriterion("audit_amount not like", value, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountIn(List<String> values) {
            addCriterion("audit_amount in", values, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountNotIn(List<String> values) {
            addCriterion("audit_amount not in", values, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountBetween(String value1, String value2) {
            addCriterion("audit_amount between", value1, value2, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andAuditAmountNotBetween(String value1, String value2) {
            addCriterion("audit_amount not between", value1, value2, "auditAmount");
            return (Criteria) this;
        }

        public Criteria andIfLentIsNull() {
            addCriterion("if_lent is null");
            return (Criteria) this;
        }

        public Criteria andIfLentIsNotNull() {
            addCriterion("if_lent is not null");
            return (Criteria) this;
        }

        public Criteria andIfLentEqualTo(String value) {
            addCriterion("if_lent =", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentNotEqualTo(String value) {
            addCriterion("if_lent <>", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentGreaterThan(String value) {
            addCriterion("if_lent >", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentGreaterThanOrEqualTo(String value) {
            addCriterion("if_lent >=", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentLessThan(String value) {
            addCriterion("if_lent <", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentLessThanOrEqualTo(String value) {
            addCriterion("if_lent <=", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentLike(String value) {
            addCriterion("if_lent like", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentNotLike(String value) {
            addCriterion("if_lent not like", value, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentIn(List<String> values) {
            addCriterion("if_lent in", values, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentNotIn(List<String> values) {
            addCriterion("if_lent not in", values, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentBetween(String value1, String value2) {
            addCriterion("if_lent between", value1, value2, "ifLent");
            return (Criteria) this;
        }

        public Criteria andIfLentNotBetween(String value1, String value2) {
            addCriterion("if_lent not between", value1, value2, "ifLent");
            return (Criteria) this;
        }

        public Criteria andLentTimeIsNull() {
            addCriterion("lent_time is null");
            return (Criteria) this;
        }

        public Criteria andLentTimeIsNotNull() {
            addCriterion("lent_time is not null");
            return (Criteria) this;
        }

        public Criteria andLentTimeEqualTo(String value) {
            addCriterion("lent_time =", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeNotEqualTo(String value) {
            addCriterion("lent_time <>", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeGreaterThan(String value) {
            addCriterion("lent_time >", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeGreaterThanOrEqualTo(String value) {
            addCriterion("lent_time >=", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeLessThan(String value) {
            addCriterion("lent_time <", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeLessThanOrEqualTo(String value) {
            addCriterion("lent_time <=", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeLike(String value) {
            addCriterion("lent_time like", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeNotLike(String value) {
            addCriterion("lent_time not like", value, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeIn(List<String> values) {
            addCriterion("lent_time in", values, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeNotIn(List<String> values) {
            addCriterion("lent_time not in", values, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeBetween(String value1, String value2) {
            addCriterion("lent_time between", value1, value2, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentTimeNotBetween(String value1, String value2) {
            addCriterion("lent_time not between", value1, value2, "lentTime");
            return (Criteria) this;
        }

        public Criteria andLentAmountIsNull() {
            addCriterion("lent_amount is null");
            return (Criteria) this;
        }

        public Criteria andLentAmountIsNotNull() {
            addCriterion("lent_amount is not null");
            return (Criteria) this;
        }

        public Criteria andLentAmountEqualTo(String value) {
            addCriterion("lent_amount =", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountNotEqualTo(String value) {
            addCriterion("lent_amount <>", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountGreaterThan(String value) {
            addCriterion("lent_amount >", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountGreaterThanOrEqualTo(String value) {
            addCriterion("lent_amount >=", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountLessThan(String value) {
            addCriterion("lent_amount <", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountLessThanOrEqualTo(String value) {
            addCriterion("lent_amount <=", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountLike(String value) {
            addCriterion("lent_amount like", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountNotLike(String value) {
            addCriterion("lent_amount not like", value, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountIn(List<String> values) {
            addCriterion("lent_amount in", values, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountNotIn(List<String> values) {
            addCriterion("lent_amount not in", values, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountBetween(String value1, String value2) {
            addCriterion("lent_amount between", value1, value2, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andLentAmountNotBetween(String value1, String value2) {
            addCriterion("lent_amount not between", value1, value2, "lentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountIsNull() {
            addCriterion("unlent_amount is null");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountIsNotNull() {
            addCriterion("unlent_amount is not null");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountEqualTo(String value) {
            addCriterion("unlent_amount =", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountNotEqualTo(String value) {
            addCriterion("unlent_amount <>", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountGreaterThan(String value) {
            addCriterion("unlent_amount >", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountGreaterThanOrEqualTo(String value) {
            addCriterion("unlent_amount >=", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountLessThan(String value) {
            addCriterion("unlent_amount <", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountLessThanOrEqualTo(String value) {
            addCriterion("unlent_amount <=", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountLike(String value) {
            addCriterion("unlent_amount like", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountNotLike(String value) {
            addCriterion("unlent_amount not like", value, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountIn(List<String> values) {
            addCriterion("unlent_amount in", values, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountNotIn(List<String> values) {
            addCriterion("unlent_amount not in", values, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountBetween(String value1, String value2) {
            addCriterion("unlent_amount between", value1, value2, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andUnlentAmountNotBetween(String value1, String value2) {
            addCriterion("unlent_amount not between", value1, value2, "unlentAmount");
            return (Criteria) this;
        }

        public Criteria andIfSettleIsNull() {
            addCriterion("if_settle is null");
            return (Criteria) this;
        }

        public Criteria andIfSettleIsNotNull() {
            addCriterion("if_settle is not null");
            return (Criteria) this;
        }

        public Criteria andIfSettleEqualTo(String value) {
            addCriterion("if_settle =", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleNotEqualTo(String value) {
            addCriterion("if_settle <>", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleGreaterThan(String value) {
            addCriterion("if_settle >", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleGreaterThanOrEqualTo(String value) {
            addCriterion("if_settle >=", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleLessThan(String value) {
            addCriterion("if_settle <", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleLessThanOrEqualTo(String value) {
            addCriterion("if_settle <=", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleLike(String value) {
            addCriterion("if_settle like", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleNotLike(String value) {
            addCriterion("if_settle not like", value, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleIn(List<String> values) {
            addCriterion("if_settle in", values, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleNotIn(List<String> values) {
            addCriterion("if_settle not in", values, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleBetween(String value1, String value2) {
            addCriterion("if_settle between", value1, value2, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andIfSettleNotBetween(String value1, String value2) {
            addCriterion("if_settle not between", value1, value2, "ifSettle");
            return (Criteria) this;
        }

        public Criteria andSettleTimeIsNull() {
            addCriterion("settle_time is null");
            return (Criteria) this;
        }

        public Criteria andSettleTimeIsNotNull() {
            addCriterion("settle_time is not null");
            return (Criteria) this;
        }

        public Criteria andSettleTimeEqualTo(String value) {
            addCriterion("settle_time =", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeNotEqualTo(String value) {
            addCriterion("settle_time <>", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeGreaterThan(String value) {
            addCriterion("settle_time >", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeGreaterThanOrEqualTo(String value) {
            addCriterion("settle_time >=", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeLessThan(String value) {
            addCriterion("settle_time <", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeLessThanOrEqualTo(String value) {
            addCriterion("settle_time <=", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeLike(String value) {
            addCriterion("settle_time like", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeNotLike(String value) {
            addCriterion("settle_time not like", value, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeIn(List<String> values) {
            addCriterion("settle_time in", values, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeNotIn(List<String> values) {
            addCriterion("settle_time not in", values, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeBetween(String value1, String value2) {
            addCriterion("settle_time between", value1, value2, "settleTime");
            return (Criteria) this;
        }

        public Criteria andSettleTimeNotBetween(String value1, String value2) {
            addCriterion("settle_time not between", value1, value2, "settleTime");
            return (Criteria) this;
        }

        public Criteria andActivityIsNull() {
            addCriterion("activity is null");
            return (Criteria) this;
        }

        public Criteria andActivityIsNotNull() {
            addCriterion("activity is not null");
            return (Criteria) this;
        }

        public Criteria andActivityEqualTo(String value) {
            addCriterion("activity =", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityNotEqualTo(String value) {
            addCriterion("activity <>", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityGreaterThan(String value) {
            addCriterion("activity >", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityGreaterThanOrEqualTo(String value) {
            addCriterion("activity >=", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityLessThan(String value) {
            addCriterion("activity <", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityLessThanOrEqualTo(String value) {
            addCriterion("activity <=", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityLike(String value) {
            addCriterion("activity like", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityNotLike(String value) {
            addCriterion("activity not like", value, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityIn(List<String> values) {
            addCriterion("activity in", values, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityNotIn(List<String> values) {
            addCriterion("activity not in", values, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityBetween(String value1, String value2) {
            addCriterion("activity between", value1, value2, "activity");
            return (Criteria) this;
        }

        public Criteria andActivityNotBetween(String value1, String value2) {
            addCriterion("activity not between", value1, value2, "activity");
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

        public Criteria andCaseEffectiveIsNull() {
            addCriterion("case_effective is null");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveIsNotNull() {
            addCriterion("case_effective is not null");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveEqualTo(String value) {
            addCriterion("case_effective =", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveNotEqualTo(String value) {
            addCriterion("case_effective <>", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveGreaterThan(String value) {
            addCriterion("case_effective >", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveGreaterThanOrEqualTo(String value) {
            addCriterion("case_effective >=", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveLessThan(String value) {
            addCriterion("case_effective <", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveLessThanOrEqualTo(String value) {
            addCriterion("case_effective <=", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveLike(String value) {
            addCriterion("case_effective like", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveNotLike(String value) {
            addCriterion("case_effective not like", value, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveIn(List<String> values) {
            addCriterion("case_effective in", values, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveNotIn(List<String> values) {
            addCriterion("case_effective not in", values, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveBetween(String value1, String value2) {
            addCriterion("case_effective between", value1, value2, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andCaseEffectiveNotBetween(String value1, String value2) {
            addCriterion("case_effective not between", value1, value2, "caseEffective");
            return (Criteria) this;
        }

        public Criteria andIfTransformIsNull() {
            addCriterion("if_transform is null");
            return (Criteria) this;
        }

        public Criteria andIfTransformIsNotNull() {
            addCriterion("if_transform is not null");
            return (Criteria) this;
        }

        public Criteria andIfTransformEqualTo(String value) {
            addCriterion("if_transform =", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformNotEqualTo(String value) {
            addCriterion("if_transform <>", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformGreaterThan(String value) {
            addCriterion("if_transform >", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformGreaterThanOrEqualTo(String value) {
            addCriterion("if_transform >=", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformLessThan(String value) {
            addCriterion("if_transform <", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformLessThanOrEqualTo(String value) {
            addCriterion("if_transform <=", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformLike(String value) {
            addCriterion("if_transform like", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformNotLike(String value) {
            addCriterion("if_transform not like", value, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformIn(List<String> values) {
            addCriterion("if_transform in", values, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformNotIn(List<String> values) {
            addCriterion("if_transform not in", values, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformBetween(String value1, String value2) {
            addCriterion("if_transform between", value1, value2, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andIfTransformNotBetween(String value1, String value2) {
            addCriterion("if_transform not between", value1, value2, "ifTransform");
            return (Criteria) this;
        }

        public Criteria andTransformTimeIsNull() {
            addCriterion("transform_time is null");
            return (Criteria) this;
        }

        public Criteria andTransformTimeIsNotNull() {
            addCriterion("transform_time is not null");
            return (Criteria) this;
        }

        public Criteria andTransformTimeEqualTo(String value) {
            addCriterion("transform_time =", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeNotEqualTo(String value) {
            addCriterion("transform_time <>", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeGreaterThan(String value) {
            addCriterion("transform_time >", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeGreaterThanOrEqualTo(String value) {
            addCriterion("transform_time >=", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeLessThan(String value) {
            addCriterion("transform_time <", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeLessThanOrEqualTo(String value) {
            addCriterion("transform_time <=", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeLike(String value) {
            addCriterion("transform_time like", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeNotLike(String value) {
            addCriterion("transform_time not like", value, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeIn(List<String> values) {
            addCriterion("transform_time in", values, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeNotIn(List<String> values) {
            addCriterion("transform_time not in", values, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeBetween(String value1, String value2) {
            addCriterion("transform_time between", value1, value2, "transformTime");
            return (Criteria) this;
        }

        public Criteria andTransformTimeNotBetween(String value1, String value2) {
            addCriterion("transform_time not between", value1, value2, "transformTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeIsNull() {
            addCriterion("insert_time is null");
            return (Criteria) this;
        }

        public Criteria andInsertTimeIsNotNull() {
            addCriterion("insert_time is not null");
            return (Criteria) this;
        }

        public Criteria andInsertTimeEqualTo(String value) {
            addCriterion("insert_time =", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeNotEqualTo(String value) {
            addCriterion("insert_time <>", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeGreaterThan(String value) {
            addCriterion("insert_time >", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeGreaterThanOrEqualTo(String value) {
            addCriterion("insert_time >=", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeLessThan(String value) {
            addCriterion("insert_time <", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeLessThanOrEqualTo(String value) {
            addCriterion("insert_time <=", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeLike(String value) {
            addCriterion("insert_time like", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeNotLike(String value) {
            addCriterion("insert_time not like", value, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeIn(List<String> values) {
            addCriterion("insert_time in", values, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeNotIn(List<String> values) {
            addCriterion("insert_time not in", values, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeBetween(String value1, String value2) {
            addCriterion("insert_time between", value1, value2, "insertTime");
            return (Criteria) this;
        }

        public Criteria andInsertTimeNotBetween(String value1, String value2) {
            addCriterion("insert_time not between", value1, value2, "insertTime");
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

        public Criteria andRequestDataIsNull() {
            addCriterion("request_data is null");
            return (Criteria) this;
        }

        public Criteria andRequestDataIsNotNull() {
            addCriterion("request_data is not null");
            return (Criteria) this;
        }

        public Criteria andRequestDataEqualTo(String value) {
            addCriterion("request_data =", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataNotEqualTo(String value) {
            addCriterion("request_data <>", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataGreaterThan(String value) {
            addCriterion("request_data >", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataGreaterThanOrEqualTo(String value) {
            addCriterion("request_data >=", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataLessThan(String value) {
            addCriterion("request_data <", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataLessThanOrEqualTo(String value) {
            addCriterion("request_data <=", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataLike(String value) {
            addCriterion("request_data like", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataNotLike(String value) {
            addCriterion("request_data not like", value, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataIn(List<String> values) {
            addCriterion("request_data in", values, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataNotIn(List<String> values) {
            addCriterion("request_data not in", values, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataBetween(String value1, String value2) {
            addCriterion("request_data between", value1, value2, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestDataNotBetween(String value1, String value2) {
            addCriterion("request_data not between", value1, value2, "requestData");
            return (Criteria) this;
        }

        public Criteria andRequestTimeIsNull() {
            addCriterion("request_time is null");
            return (Criteria) this;
        }

        public Criteria andRequestTimeIsNotNull() {
            addCriterion("request_time is not null");
            return (Criteria) this;
        }

        public Criteria andRequestTimeEqualTo(String value) {
            addCriterion("request_time =", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeNotEqualTo(String value) {
            addCriterion("request_time <>", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeGreaterThan(String value) {
            addCriterion("request_time >", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeGreaterThanOrEqualTo(String value) {
            addCriterion("request_time >=", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeLessThan(String value) {
            addCriterion("request_time <", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeLessThanOrEqualTo(String value) {
            addCriterion("request_time <=", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeLike(String value) {
            addCriterion("request_time like", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeNotLike(String value) {
            addCriterion("request_time not like", value, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeIn(List<String> values) {
            addCriterion("request_time in", values, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeNotIn(List<String> values) {
            addCriterion("request_time not in", values, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeBetween(String value1, String value2) {
            addCriterion("request_time between", value1, value2, "requestTime");
            return (Criteria) this;
        }

        public Criteria andRequestTimeNotBetween(String value1, String value2) {
            addCriterion("request_time not between", value1, value2, "requestTime");
            return (Criteria) this;
        }

        public Criteria andTCidIsNull() {
            addCriterion("t_cid is null");
            return (Criteria) this;
        }

        public Criteria andTCidIsNotNull() {
            addCriterion("t_cid is not null");
            return (Criteria) this;
        }

        public Criteria andTCidEqualTo(String value) {
            addCriterion("t_cid =", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidNotEqualTo(String value) {
            addCriterion("t_cid <>", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidGreaterThan(String value) {
            addCriterion("t_cid >", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidGreaterThanOrEqualTo(String value) {
            addCriterion("t_cid >=", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidLessThan(String value) {
            addCriterion("t_cid <", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidLessThanOrEqualTo(String value) {
            addCriterion("t_cid <=", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidLike(String value) {
            addCriterion("t_cid like", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidNotLike(String value) {
            addCriterion("t_cid not like", value, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidIn(List<String> values) {
            addCriterion("t_cid in", values, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidNotIn(List<String> values) {
            addCriterion("t_cid not in", values, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidBetween(String value1, String value2) {
            addCriterion("t_cid between", value1, value2, "tCid");
            return (Criteria) this;
        }

        public Criteria andTCidNotBetween(String value1, String value2) {
            addCriterion("t_cid not between", value1, value2, "tCid");
            return (Criteria) this;
        }

        public Criteria andFingerprintIsNull() {
            addCriterion("fingerprint is null");
            return (Criteria) this;
        }

        public Criteria andFingerprintIsNotNull() {
            addCriterion("fingerprint is not null");
            return (Criteria) this;
        }

        public Criteria andFingerprintEqualTo(String value) {
            addCriterion("fingerprint =", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintNotEqualTo(String value) {
            addCriterion("fingerprint <>", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintGreaterThan(String value) {
            addCriterion("fingerprint >", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintGreaterThanOrEqualTo(String value) {
            addCriterion("fingerprint >=", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintLessThan(String value) {
            addCriterion("fingerprint <", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintLessThanOrEqualTo(String value) {
            addCriterion("fingerprint <=", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintLike(String value) {
            addCriterion("fingerprint like", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintNotLike(String value) {
            addCriterion("fingerprint not like", value, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintIn(List<String> values) {
            addCriterion("fingerprint in", values, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintNotIn(List<String> values) {
            addCriterion("fingerprint not in", values, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintBetween(String value1, String value2) {
            addCriterion("fingerprint between", value1, value2, "fingerprint");
            return (Criteria) this;
        }

        public Criteria andFingerprintNotBetween(String value1, String value2) {
            addCriterion("fingerprint not between", value1, value2, "fingerprint");
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