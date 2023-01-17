package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XieChengSmsCollidingDataLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public XieChengSmsCollidingDataLogExample() {
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

        public Criteria andSmsCollidingDataIdIsNull() {
            addCriterion("sms_colliding_data_id is null");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdIsNotNull() {
            addCriterion("sms_colliding_data_id is not null");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdEqualTo(Long value) {
            addCriterion("sms_colliding_data_id =", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdNotEqualTo(Long value) {
            addCriterion("sms_colliding_data_id <>", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdGreaterThan(Long value) {
            addCriterion("sms_colliding_data_id >", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sms_colliding_data_id >=", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdLessThan(Long value) {
            addCriterion("sms_colliding_data_id <", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdLessThanOrEqualTo(Long value) {
            addCriterion("sms_colliding_data_id <=", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdIn(List<Long> values) {
            addCriterion("sms_colliding_data_id in", values, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdNotIn(List<Long> values) {
            addCriterion("sms_colliding_data_id not in", values, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdBetween(Long value1, Long value2) {
            addCriterion("sms_colliding_data_id between", value1, value2, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdNotBetween(Long value1, Long value2) {
            addCriterion("sms_colliding_data_id not between", value1, value2, "smsCollidingDataId");
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

        public Criteria andLocalIdEqualTo(Long value) {
            addCriterion("local_id =", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotEqualTo(Long value) {
            addCriterion("local_id <>", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThan(Long value) {
            addCriterion("local_id >", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThanOrEqualTo(Long value) {
            addCriterion("local_id >=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThan(Long value) {
            addCriterion("local_id <", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThanOrEqualTo(Long value) {
            addCriterion("local_id <=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdIn(List<Long> values) {
            addCriterion("local_id in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotIn(List<Long> values) {
            addCriterion("local_id not in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdBetween(Long value1, Long value2) {
            addCriterion("local_id between", value1, value2, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotBetween(Long value1, Long value2) {
            addCriterion("local_id not between", value1, value2, "localId");
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

        public Criteria andSha256CodeListIsNull() {
            addCriterion("sha256_code_list is null");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListIsNotNull() {
            addCriterion("sha256_code_list is not null");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListEqualTo(String value) {
            addCriterion("sha256_code_list =", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListNotEqualTo(String value) {
            addCriterion("sha256_code_list <>", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListGreaterThan(String value) {
            addCriterion("sha256_code_list >", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListGreaterThanOrEqualTo(String value) {
            addCriterion("sha256_code_list >=", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListLessThan(String value) {
            addCriterion("sha256_code_list <", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListLessThanOrEqualTo(String value) {
            addCriterion("sha256_code_list <=", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListLike(String value) {
            addCriterion("sha256_code_list like", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListNotLike(String value) {
            addCriterion("sha256_code_list not like", value, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListIn(List<String> values) {
            addCriterion("sha256_code_list in", values, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListNotIn(List<String> values) {
            addCriterion("sha256_code_list not in", values, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListBetween(String value1, String value2) {
            addCriterion("sha256_code_list between", value1, value2, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andSha256CodeListNotBetween(String value1, String value2) {
            addCriterion("sha256_code_list not between", value1, value2, "sha256CodeList");
            return (Criteria) this;
        }

        public Criteria andMd5CodeIsNull() {
            addCriterion("md5_code is null");
            return (Criteria) this;
        }

        public Criteria andMd5CodeIsNotNull() {
            addCriterion("md5_code is not null");
            return (Criteria) this;
        }

        public Criteria andMd5CodeEqualTo(String value) {
            addCriterion("md5_code =", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeNotEqualTo(String value) {
            addCriterion("md5_code <>", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeGreaterThan(String value) {
            addCriterion("md5_code >", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeGreaterThanOrEqualTo(String value) {
            addCriterion("md5_code >=", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeLessThan(String value) {
            addCriterion("md5_code <", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeLessThanOrEqualTo(String value) {
            addCriterion("md5_code <=", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeLike(String value) {
            addCriterion("md5_code like", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeNotLike(String value) {
            addCriterion("md5_code not like", value, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeIn(List<String> values) {
            addCriterion("md5_code in", values, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeNotIn(List<String> values) {
            addCriterion("md5_code not in", values, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeBetween(String value1, String value2) {
            addCriterion("md5_code between", value1, value2, "md5Code");
            return (Criteria) this;
        }

        public Criteria andMd5CodeNotBetween(String value1, String value2) {
            addCriterion("md5_code not between", value1, value2, "md5Code");
            return (Criteria) this;
        }

        public Criteria andParamIsNull() {
            addCriterion("param is null");
            return (Criteria) this;
        }

        public Criteria andParamIsNotNull() {
            addCriterion("param is not null");
            return (Criteria) this;
        }

        public Criteria andParamEqualTo(String value) {
            addCriterion("param =", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamNotEqualTo(String value) {
            addCriterion("param <>", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamGreaterThan(String value) {
            addCriterion("param >", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamGreaterThanOrEqualTo(String value) {
            addCriterion("param >=", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamLessThan(String value) {
            addCriterion("param <", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamLessThanOrEqualTo(String value) {
            addCriterion("param <=", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamLike(String value) {
            addCriterion("param like", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamNotLike(String value) {
            addCriterion("param not like", value, "param");
            return (Criteria) this;
        }

        public Criteria andParamIn(List<String> values) {
            addCriterion("param in", values, "param");
            return (Criteria) this;
        }

        public Criteria andParamNotIn(List<String> values) {
            addCriterion("param not in", values, "param");
            return (Criteria) this;
        }

        public Criteria andParamBetween(String value1, String value2) {
            addCriterion("param between", value1, value2, "param");
            return (Criteria) this;
        }

        public Criteria andParamNotBetween(String value1, String value2) {
            addCriterion("param not between", value1, value2, "param");
            return (Criteria) this;
        }

        public Criteria andParamSecretIsNull() {
            addCriterion("param_secret is null");
            return (Criteria) this;
        }

        public Criteria andParamSecretIsNotNull() {
            addCriterion("param_secret is not null");
            return (Criteria) this;
        }

        public Criteria andParamSecretEqualTo(String value) {
            addCriterion("param_secret =", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretNotEqualTo(String value) {
            addCriterion("param_secret <>", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretGreaterThan(String value) {
            addCriterion("param_secret >", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretGreaterThanOrEqualTo(String value) {
            addCriterion("param_secret >=", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretLessThan(String value) {
            addCriterion("param_secret <", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretLessThanOrEqualTo(String value) {
            addCriterion("param_secret <=", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretLike(String value) {
            addCriterion("param_secret like", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretNotLike(String value) {
            addCriterion("param_secret not like", value, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretIn(List<String> values) {
            addCriterion("param_secret in", values, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretNotIn(List<String> values) {
            addCriterion("param_secret not in", values, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretBetween(String value1, String value2) {
            addCriterion("param_secret between", value1, value2, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andParamSecretNotBetween(String value1, String value2) {
            addCriterion("param_secret not between", value1, value2, "paramSecret");
            return (Criteria) this;
        }

        public Criteria andOrgChannelIsNull() {
            addCriterion("org_channel is null");
            return (Criteria) this;
        }

        public Criteria andOrgChannelIsNotNull() {
            addCriterion("org_channel is not null");
            return (Criteria) this;
        }

        public Criteria andOrgChannelEqualTo(String value) {
            addCriterion("org_channel =", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotEqualTo(String value) {
            addCriterion("org_channel <>", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelGreaterThan(String value) {
            addCriterion("org_channel >", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelGreaterThanOrEqualTo(String value) {
            addCriterion("org_channel >=", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelLessThan(String value) {
            addCriterion("org_channel <", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelLessThanOrEqualTo(String value) {
            addCriterion("org_channel <=", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelLike(String value) {
            addCriterion("org_channel like", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotLike(String value) {
            addCriterion("org_channel not like", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelIn(List<String> values) {
            addCriterion("org_channel in", values, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotIn(List<String> values) {
            addCriterion("org_channel not in", values, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelBetween(String value1, String value2) {
            addCriterion("org_channel between", value1, value2, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotBetween(String value1, String value2) {
            addCriterion("org_channel not between", value1, value2, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andMktLevelIsNull() {
            addCriterion("mkt_level is null");
            return (Criteria) this;
        }

        public Criteria andMktLevelIsNotNull() {
            addCriterion("mkt_level is not null");
            return (Criteria) this;
        }

        public Criteria andMktLevelEqualTo(String value) {
            addCriterion("mkt_level =", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotEqualTo(String value) {
            addCriterion("mkt_level <>", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelGreaterThan(String value) {
            addCriterion("mkt_level >", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelGreaterThanOrEqualTo(String value) {
            addCriterion("mkt_level >=", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelLessThan(String value) {
            addCriterion("mkt_level <", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelLessThanOrEqualTo(String value) {
            addCriterion("mkt_level <=", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelLike(String value) {
            addCriterion("mkt_level like", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotLike(String value) {
            addCriterion("mkt_level not like", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelIn(List<String> values) {
            addCriterion("mkt_level in", values, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotIn(List<String> values) {
            addCriterion("mkt_level not in", values, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelBetween(String value1, String value2) {
            addCriterion("mkt_level between", value1, value2, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotBetween(String value1, String value2) {
            addCriterion("mkt_level not between", value1, value2, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andInfoIsNull() {
            addCriterion("info is null");
            return (Criteria) this;
        }

        public Criteria andInfoIsNotNull() {
            addCriterion("info is not null");
            return (Criteria) this;
        }

        public Criteria andInfoEqualTo(String value) {
            addCriterion("info =", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotEqualTo(String value) {
            addCriterion("info <>", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoGreaterThan(String value) {
            addCriterion("info >", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoGreaterThanOrEqualTo(String value) {
            addCriterion("info >=", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoLessThan(String value) {
            addCriterion("info <", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoLessThanOrEqualTo(String value) {
            addCriterion("info <=", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoLike(String value) {
            addCriterion("info like", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotLike(String value) {
            addCriterion("info not like", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoIn(List<String> values) {
            addCriterion("info in", values, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotIn(List<String> values) {
            addCriterion("info not in", values, "info");
            return (Criteria) this;
        }

        public Criteria andInfoBetween(String value1, String value2) {
            addCriterion("info between", value1, value2, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotBetween(String value1, String value2) {
            addCriterion("info not between", value1, value2, "info");
            return (Criteria) this;
        }

        public Criteria andResultIsNull() {
            addCriterion("result is null");
            return (Criteria) this;
        }

        public Criteria andResultIsNotNull() {
            addCriterion("result is not null");
            return (Criteria) this;
        }

        public Criteria andResultEqualTo(Boolean value) {
            addCriterion("result =", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotEqualTo(Boolean value) {
            addCriterion("result <>", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultGreaterThan(Boolean value) {
            addCriterion("result >", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultGreaterThanOrEqualTo(Boolean value) {
            addCriterion("result >=", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultLessThan(Boolean value) {
            addCriterion("result <", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultLessThanOrEqualTo(Boolean value) {
            addCriterion("result <=", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultIn(List<Boolean> values) {
            addCriterion("result in", values, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotIn(List<Boolean> values) {
            addCriterion("result not in", values, "result");
            return (Criteria) this;
        }

        public Criteria andResultBetween(Boolean value1, Boolean value2) {
            addCriterion("result between", value1, value2, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotBetween(Boolean value1, Boolean value2) {
            addCriterion("result not between", value1, value2, "result");
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