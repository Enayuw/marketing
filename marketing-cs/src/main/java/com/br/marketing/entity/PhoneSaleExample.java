package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhoneSaleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PhoneSaleExample() {
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

        public Criteria andApiCidIsNull() {
            addCriterion("api_cid is null");
            return (Criteria) this;
        }

        public Criteria andApiCidIsNotNull() {
            addCriterion("api_cid is not null");
            return (Criteria) this;
        }

        public Criteria andApiCidEqualTo(String value) {
            addCriterion("api_cid =", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidNotEqualTo(String value) {
            addCriterion("api_cid <>", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidGreaterThan(String value) {
            addCriterion("api_cid >", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidGreaterThanOrEqualTo(String value) {
            addCriterion("api_cid >=", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidLessThan(String value) {
            addCriterion("api_cid <", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidLessThanOrEqualTo(String value) {
            addCriterion("api_cid <=", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidLike(String value) {
            addCriterion("api_cid like", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidNotLike(String value) {
            addCriterion("api_cid not like", value, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidIn(List<String> values) {
            addCriterion("api_cid in", values, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidNotIn(List<String> values) {
            addCriterion("api_cid not in", values, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidBetween(String value1, String value2) {
            addCriterion("api_cid between", value1, value2, "apiCid");
            return (Criteria) this;
        }

        public Criteria andApiCidNotBetween(String value1, String value2) {
            addCriterion("api_cid not between", value1, value2, "apiCid");
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

        public Criteria andSyncLogIdIsNull() {
            addCriterion("sync_log_id is null");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdIsNotNull() {
            addCriterion("sync_log_id is not null");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdEqualTo(String value) {
            addCriterion("sync_log_id =", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdNotEqualTo(String value) {
            addCriterion("sync_log_id <>", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdGreaterThan(String value) {
            addCriterion("sync_log_id >", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdGreaterThanOrEqualTo(String value) {
            addCriterion("sync_log_id >=", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdLessThan(String value) {
            addCriterion("sync_log_id <", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdLessThanOrEqualTo(String value) {
            addCriterion("sync_log_id <=", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdLike(String value) {
            addCriterion("sync_log_id like", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdNotLike(String value) {
            addCriterion("sync_log_id not like", value, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdIn(List<String> values) {
            addCriterion("sync_log_id in", values, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdNotIn(List<String> values) {
            addCriterion("sync_log_id not in", values, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdBetween(String value1, String value2) {
            addCriterion("sync_log_id between", value1, value2, "syncLogId");
            return (Criteria) this;
        }

        public Criteria andSyncLogIdNotBetween(String value1, String value2) {
            addCriterion("sync_log_id not between", value1, value2, "syncLogId");
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

        public Criteria andUidIsNull() {
            addCriterion("uid is null");
            return (Criteria) this;
        }

        public Criteria andUidIsNotNull() {
            addCriterion("uid is not null");
            return (Criteria) this;
        }

        public Criteria andUidEqualTo(String value) {
            addCriterion("uid =", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotEqualTo(String value) {
            addCriterion("uid <>", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThan(String value) {
            addCriterion("uid >", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThanOrEqualTo(String value) {
            addCriterion("uid >=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThan(String value) {
            addCriterion("uid <", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThanOrEqualTo(String value) {
            addCriterion("uid <=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLike(String value) {
            addCriterion("uid like", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotLike(String value) {
            addCriterion("uid not like", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidIn(List<String> values) {
            addCriterion("uid in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotIn(List<String> values) {
            addCriterion("uid not in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidBetween(String value1, String value2) {
            addCriterion("uid between", value1, value2, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotBetween(String value1, String value2) {
            addCriterion("uid not between", value1, value2, "uid");
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
            addCriterion("name is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
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

        public Criteria andMarketscoreIsNull() {
            addCriterion("marketscore is null");
            return (Criteria) this;
        }

        public Criteria andMarketscoreIsNotNull() {
            addCriterion("marketscore is not null");
            return (Criteria) this;
        }

        public Criteria andMarketscoreEqualTo(String value) {
            addCriterion("marketscore =", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreNotEqualTo(String value) {
            addCriterion("marketscore <>", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreGreaterThan(String value) {
            addCriterion("marketscore >", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreGreaterThanOrEqualTo(String value) {
            addCriterion("marketscore >=", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreLessThan(String value) {
            addCriterion("marketscore <", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreLessThanOrEqualTo(String value) {
            addCriterion("marketscore <=", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreLike(String value) {
            addCriterion("marketscore like", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreNotLike(String value) {
            addCriterion("marketscore not like", value, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreIn(List<String> values) {
            addCriterion("marketscore in", values, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreNotIn(List<String> values) {
            addCriterion("marketscore not in", values, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreBetween(String value1, String value2) {
            addCriterion("marketscore between", value1, value2, "marketscore");
            return (Criteria) this;
        }

        public Criteria andMarketscoreNotBetween(String value1, String value2) {
            addCriterion("marketscore not between", value1, value2, "marketscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreIsNull() {
            addCriterion("riskscore is null");
            return (Criteria) this;
        }

        public Criteria andRiskscoreIsNotNull() {
            addCriterion("riskscore is not null");
            return (Criteria) this;
        }

        public Criteria andRiskscoreEqualTo(String value) {
            addCriterion("riskscore =", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreNotEqualTo(String value) {
            addCriterion("riskscore <>", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreGreaterThan(String value) {
            addCriterion("riskscore >", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreGreaterThanOrEqualTo(String value) {
            addCriterion("riskscore >=", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreLessThan(String value) {
            addCriterion("riskscore <", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreLessThanOrEqualTo(String value) {
            addCriterion("riskscore <=", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreLike(String value) {
            addCriterion("riskscore like", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreNotLike(String value) {
            addCriterion("riskscore not like", value, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreIn(List<String> values) {
            addCriterion("riskscore in", values, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreNotIn(List<String> values) {
            addCriterion("riskscore not in", values, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreBetween(String value1, String value2) {
            addCriterion("riskscore between", value1, value2, "riskscore");
            return (Criteria) this;
        }

        public Criteria andRiskscoreNotBetween(String value1, String value2) {
            addCriterion("riskscore not between", value1, value2, "riskscore");
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

        public Criteria andProductNameIsNull() {
            addCriterion("product_name is null");
            return (Criteria) this;
        }

        public Criteria andProductNameIsNotNull() {
            addCriterion("product_name is not null");
            return (Criteria) this;
        }

        public Criteria andProductNameEqualTo(String value) {
            addCriterion("product_name =", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameNotEqualTo(String value) {
            addCriterion("product_name <>", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameGreaterThan(String value) {
            addCriterion("product_name >", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameGreaterThanOrEqualTo(String value) {
            addCriterion("product_name >=", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameLessThan(String value) {
            addCriterion("product_name <", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameLessThanOrEqualTo(String value) {
            addCriterion("product_name <=", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameLike(String value) {
            addCriterion("product_name like", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameNotLike(String value) {
            addCriterion("product_name not like", value, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameIn(List<String> values) {
            addCriterion("product_name in", values, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameNotIn(List<String> values) {
            addCriterion("product_name not in", values, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameBetween(String value1, String value2) {
            addCriterion("product_name between", value1, value2, "productName");
            return (Criteria) this;
        }

        public Criteria andProductNameNotBetween(String value1, String value2) {
            addCriterion("product_name not between", value1, value2, "productName");
            return (Criteria) this;
        }

        public Criteria andFlagTypeIsNull() {
            addCriterion("flag_type is null");
            return (Criteria) this;
        }

        public Criteria andFlagTypeIsNotNull() {
            addCriterion("flag_type is not null");
            return (Criteria) this;
        }

        public Criteria andFlagTypeEqualTo(String value) {
            addCriterion("flag_type =", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeNotEqualTo(String value) {
            addCriterion("flag_type <>", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeGreaterThan(String value) {
            addCriterion("flag_type >", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeGreaterThanOrEqualTo(String value) {
            addCriterion("flag_type >=", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeLessThan(String value) {
            addCriterion("flag_type <", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeLessThanOrEqualTo(String value) {
            addCriterion("flag_type <=", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeLike(String value) {
            addCriterion("flag_type like", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeNotLike(String value) {
            addCriterion("flag_type not like", value, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeIn(List<String> values) {
            addCriterion("flag_type in", values, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeNotIn(List<String> values) {
            addCriterion("flag_type not in", values, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeBetween(String value1, String value2) {
            addCriterion("flag_type between", value1, value2, "flagType");
            return (Criteria) this;
        }

        public Criteria andFlagTypeNotBetween(String value1, String value2) {
            addCriterion("flag_type not between", value1, value2, "flagType");
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

        public Criteria andLevelIsNull() {
            addCriterion("level is null");
            return (Criteria) this;
        }

        public Criteria andLevelIsNotNull() {
            addCriterion("level is not null");
            return (Criteria) this;
        }

        public Criteria andLevelEqualTo(String value) {
            addCriterion("level =", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotEqualTo(String value) {
            addCriterion("level <>", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThan(String value) {
            addCriterion("level >", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThanOrEqualTo(String value) {
            addCriterion("level >=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThan(String value) {
            addCriterion("level <", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThanOrEqualTo(String value) {
            addCriterion("level <=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLike(String value) {
            addCriterion("level like", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotLike(String value) {
            addCriterion("level not like", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelIn(List<String> values) {
            addCriterion("level in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotIn(List<String> values) {
            addCriterion("level not in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelBetween(String value1, String value2) {
            addCriterion("level between", value1, value2, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotBetween(String value1, String value2) {
            addCriterion("level not between", value1, value2, "level");
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

        public Criteria andPagenodeIsNull() {
            addCriterion("pagenode is null");
            return (Criteria) this;
        }

        public Criteria andPagenodeIsNotNull() {
            addCriterion("pagenode is not null");
            return (Criteria) this;
        }

        public Criteria andPagenodeEqualTo(String value) {
            addCriterion("pagenode =", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeNotEqualTo(String value) {
            addCriterion("pagenode <>", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeGreaterThan(String value) {
            addCriterion("pagenode >", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeGreaterThanOrEqualTo(String value) {
            addCriterion("pagenode >=", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeLessThan(String value) {
            addCriterion("pagenode <", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeLessThanOrEqualTo(String value) {
            addCriterion("pagenode <=", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeLike(String value) {
            addCriterion("pagenode like", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeNotLike(String value) {
            addCriterion("pagenode not like", value, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeIn(List<String> values) {
            addCriterion("pagenode in", values, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeNotIn(List<String> values) {
            addCriterion("pagenode not in", values, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeBetween(String value1, String value2) {
            addCriterion("pagenode between", value1, value2, "pagenode");
            return (Criteria) this;
        }

        public Criteria andPagenodeNotBetween(String value1, String value2) {
            addCriterion("pagenode not between", value1, value2, "pagenode");
            return (Criteria) this;
        }

        public Criteria andOptypeIsNull() {
            addCriterion("optype is null");
            return (Criteria) this;
        }

        public Criteria andOptypeIsNotNull() {
            addCriterion("optype is not null");
            return (Criteria) this;
        }

        public Criteria andOptypeEqualTo(String value) {
            addCriterion("optype =", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeNotEqualTo(String value) {
            addCriterion("optype <>", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeGreaterThan(String value) {
            addCriterion("optype >", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeGreaterThanOrEqualTo(String value) {
            addCriterion("optype >=", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeLessThan(String value) {
            addCriterion("optype <", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeLessThanOrEqualTo(String value) {
            addCriterion("optype <=", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeLike(String value) {
            addCriterion("optype like", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeNotLike(String value) {
            addCriterion("optype not like", value, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeIn(List<String> values) {
            addCriterion("optype in", values, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeNotIn(List<String> values) {
            addCriterion("optype not in", values, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeBetween(String value1, String value2) {
            addCriterion("optype between", value1, value2, "optype");
            return (Criteria) this;
        }

        public Criteria andOptypeNotBetween(String value1, String value2) {
            addCriterion("optype not between", value1, value2, "optype");
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

        public Criteria andProductionIsNull() {
            addCriterion("production is null");
            return (Criteria) this;
        }

        public Criteria andProductionIsNotNull() {
            addCriterion("production is not null");
            return (Criteria) this;
        }

        public Criteria andProductionEqualTo(String value) {
            addCriterion("production =", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionNotEqualTo(String value) {
            addCriterion("production <>", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionGreaterThan(String value) {
            addCriterion("production >", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionGreaterThanOrEqualTo(String value) {
            addCriterion("production >=", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionLessThan(String value) {
            addCriterion("production <", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionLessThanOrEqualTo(String value) {
            addCriterion("production <=", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionLike(String value) {
            addCriterion("production like", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionNotLike(String value) {
            addCriterion("production not like", value, "production");
            return (Criteria) this;
        }

        public Criteria andProductionIn(List<String> values) {
            addCriterion("production in", values, "production");
            return (Criteria) this;
        }

        public Criteria andProductionNotIn(List<String> values) {
            addCriterion("production not in", values, "production");
            return (Criteria) this;
        }

        public Criteria andProductionBetween(String value1, String value2) {
            addCriterion("production between", value1, value2, "production");
            return (Criteria) this;
        }

        public Criteria andProductionNotBetween(String value1, String value2) {
            addCriterion("production not between", value1, value2, "production");
            return (Criteria) this;
        }

        public Criteria andRegionIsNull() {
            addCriterion("region is null");
            return (Criteria) this;
        }

        public Criteria andRegionIsNotNull() {
            addCriterion("region is not null");
            return (Criteria) this;
        }

        public Criteria andRegionEqualTo(String value) {
            addCriterion("region =", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionNotEqualTo(String value) {
            addCriterion("region <>", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionGreaterThan(String value) {
            addCriterion("region >", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionGreaterThanOrEqualTo(String value) {
            addCriterion("region >=", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionLessThan(String value) {
            addCriterion("region <", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionLessThanOrEqualTo(String value) {
            addCriterion("region <=", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionLike(String value) {
            addCriterion("region like", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionNotLike(String value) {
            addCriterion("region not like", value, "region");
            return (Criteria) this;
        }

        public Criteria andRegionIn(List<String> values) {
            addCriterion("region in", values, "region");
            return (Criteria) this;
        }

        public Criteria andRegionNotIn(List<String> values) {
            addCriterion("region not in", values, "region");
            return (Criteria) this;
        }

        public Criteria andRegionBetween(String value1, String value2) {
            addCriterion("region between", value1, value2, "region");
            return (Criteria) this;
        }

        public Criteria andRegionNotBetween(String value1, String value2) {
            addCriterion("region not between", value1, value2, "region");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dIsNull() {
            addCriterion("yx_flag_3d is null");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dIsNotNull() {
            addCriterion("yx_flag_3d is not null");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dEqualTo(String value) {
            addCriterion("yx_flag_3d =", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dNotEqualTo(String value) {
            addCriterion("yx_flag_3d <>", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dGreaterThan(String value) {
            addCriterion("yx_flag_3d >", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dGreaterThanOrEqualTo(String value) {
            addCriterion("yx_flag_3d >=", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dLessThan(String value) {
            addCriterion("yx_flag_3d <", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dLessThanOrEqualTo(String value) {
            addCriterion("yx_flag_3d <=", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dLike(String value) {
            addCriterion("yx_flag_3d like", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dNotLike(String value) {
            addCriterion("yx_flag_3d not like", value, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dIn(List<String> values) {
            addCriterion("yx_flag_3d in", values, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dNotIn(List<String> values) {
            addCriterion("yx_flag_3d not in", values, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dBetween(String value1, String value2) {
            addCriterion("yx_flag_3d between", value1, value2, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag3dNotBetween(String value1, String value2) {
            addCriterion("yx_flag_3d not between", value1, value2, "yxFlag3d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dIsNull() {
            addCriterion("yx_flag_7d is null");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dIsNotNull() {
            addCriterion("yx_flag_7d is not null");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dEqualTo(String value) {
            addCriterion("yx_flag_7d =", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dNotEqualTo(String value) {
            addCriterion("yx_flag_7d <>", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dGreaterThan(String value) {
            addCriterion("yx_flag_7d >", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dGreaterThanOrEqualTo(String value) {
            addCriterion("yx_flag_7d >=", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dLessThan(String value) {
            addCriterion("yx_flag_7d <", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dLessThanOrEqualTo(String value) {
            addCriterion("yx_flag_7d <=", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dLike(String value) {
            addCriterion("yx_flag_7d like", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dNotLike(String value) {
            addCriterion("yx_flag_7d not like", value, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dIn(List<String> values) {
            addCriterion("yx_flag_7d in", values, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dNotIn(List<String> values) {
            addCriterion("yx_flag_7d not in", values, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dBetween(String value1, String value2) {
            addCriterion("yx_flag_7d between", value1, value2, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag7dNotBetween(String value1, String value2) {
            addCriterion("yx_flag_7d not between", value1, value2, "yxFlag7d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dIsNull() {
            addCriterion("yx_flag_15d is null");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dIsNotNull() {
            addCriterion("yx_flag_15d is not null");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dEqualTo(String value) {
            addCriterion("yx_flag_15d =", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dNotEqualTo(String value) {
            addCriterion("yx_flag_15d <>", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dGreaterThan(String value) {
            addCriterion("yx_flag_15d >", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dGreaterThanOrEqualTo(String value) {
            addCriterion("yx_flag_15d >=", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dLessThan(String value) {
            addCriterion("yx_flag_15d <", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dLessThanOrEqualTo(String value) {
            addCriterion("yx_flag_15d <=", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dLike(String value) {
            addCriterion("yx_flag_15d like", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dNotLike(String value) {
            addCriterion("yx_flag_15d not like", value, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dIn(List<String> values) {
            addCriterion("yx_flag_15d in", values, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dNotIn(List<String> values) {
            addCriterion("yx_flag_15d not in", values, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dBetween(String value1, String value2) {
            addCriterion("yx_flag_15d between", value1, value2, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag15dNotBetween(String value1, String value2) {
            addCriterion("yx_flag_15d not between", value1, value2, "yxFlag15d");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mIsNull() {
            addCriterion("yx_flag_1m is null");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mIsNotNull() {
            addCriterion("yx_flag_1m is not null");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mEqualTo(String value) {
            addCriterion("yx_flag_1m =", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mNotEqualTo(String value) {
            addCriterion("yx_flag_1m <>", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mGreaterThan(String value) {
            addCriterion("yx_flag_1m >", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mGreaterThanOrEqualTo(String value) {
            addCriterion("yx_flag_1m >=", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mLessThan(String value) {
            addCriterion("yx_flag_1m <", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mLessThanOrEqualTo(String value) {
            addCriterion("yx_flag_1m <=", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mLike(String value) {
            addCriterion("yx_flag_1m like", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mNotLike(String value) {
            addCriterion("yx_flag_1m not like", value, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mIn(List<String> values) {
            addCriterion("yx_flag_1m in", values, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mNotIn(List<String> values) {
            addCriterion("yx_flag_1m not in", values, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mBetween(String value1, String value2) {
            addCriterion("yx_flag_1m between", value1, value2, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andYxFlag1mNotBetween(String value1, String value2) {
            addCriterion("yx_flag_1m not between", value1, value2, "yxFlag1m");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseIsNull() {
            addCriterion("person_flag_house is null");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseIsNotNull() {
            addCriterion("person_flag_house is not null");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseEqualTo(String value) {
            addCriterion("person_flag_house =", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseNotEqualTo(String value) {
            addCriterion("person_flag_house <>", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseGreaterThan(String value) {
            addCriterion("person_flag_house >", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseGreaterThanOrEqualTo(String value) {
            addCriterion("person_flag_house >=", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseLessThan(String value) {
            addCriterion("person_flag_house <", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseLessThanOrEqualTo(String value) {
            addCriterion("person_flag_house <=", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseLike(String value) {
            addCriterion("person_flag_house like", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseNotLike(String value) {
            addCriterion("person_flag_house not like", value, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseIn(List<String> values) {
            addCriterion("person_flag_house in", values, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseNotIn(List<String> values) {
            addCriterion("person_flag_house not in", values, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseBetween(String value1, String value2) {
            addCriterion("person_flag_house between", value1, value2, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagHouseNotBetween(String value1, String value2) {
            addCriterion("person_flag_house not between", value1, value2, "personFlagHouse");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarIsNull() {
            addCriterion("person_flag_car is null");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarIsNotNull() {
            addCriterion("person_flag_car is not null");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarEqualTo(String value) {
            addCriterion("person_flag_car =", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarNotEqualTo(String value) {
            addCriterion("person_flag_car <>", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarGreaterThan(String value) {
            addCriterion("person_flag_car >", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarGreaterThanOrEqualTo(String value) {
            addCriterion("person_flag_car >=", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarLessThan(String value) {
            addCriterion("person_flag_car <", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarLessThanOrEqualTo(String value) {
            addCriterion("person_flag_car <=", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarLike(String value) {
            addCriterion("person_flag_car like", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarNotLike(String value) {
            addCriterion("person_flag_car not like", value, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarIn(List<String> values) {
            addCriterion("person_flag_car in", values, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarNotIn(List<String> values) {
            addCriterion("person_flag_car not in", values, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarBetween(String value1, String value2) {
            addCriterion("person_flag_car between", value1, value2, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagCarNotBetween(String value1, String value2) {
            addCriterion("person_flag_car not between", value1, value2, "personFlagCar");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurIsNull() {
            addCriterion("person_flag_insur is null");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurIsNotNull() {
            addCriterion("person_flag_insur is not null");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurEqualTo(String value) {
            addCriterion("person_flag_insur =", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurNotEqualTo(String value) {
            addCriterion("person_flag_insur <>", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurGreaterThan(String value) {
            addCriterion("person_flag_insur >", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurGreaterThanOrEqualTo(String value) {
            addCriterion("person_flag_insur >=", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurLessThan(String value) {
            addCriterion("person_flag_insur <", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurLessThanOrEqualTo(String value) {
            addCriterion("person_flag_insur <=", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurLike(String value) {
            addCriterion("person_flag_insur like", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurNotLike(String value) {
            addCriterion("person_flag_insur not like", value, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurIn(List<String> values) {
            addCriterion("person_flag_insur in", values, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurNotIn(List<String> values) {
            addCriterion("person_flag_insur not in", values, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurBetween(String value1, String value2) {
            addCriterion("person_flag_insur between", value1, value2, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andPersonFlagInsurNotBetween(String value1, String value2) {
            addCriterion("person_flag_insur not between", value1, value2, "personFlagInsur");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwIsNull() {
            addCriterion("white_list_gw is null");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwIsNotNull() {
            addCriterion("white_list_gw is not null");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwEqualTo(String value) {
            addCriterion("white_list_gw =", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwNotEqualTo(String value) {
            addCriterion("white_list_gw <>", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwGreaterThan(String value) {
            addCriterion("white_list_gw >", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwGreaterThanOrEqualTo(String value) {
            addCriterion("white_list_gw >=", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwLessThan(String value) {
            addCriterion("white_list_gw <", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwLessThanOrEqualTo(String value) {
            addCriterion("white_list_gw <=", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwLike(String value) {
            addCriterion("white_list_gw like", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwNotLike(String value) {
            addCriterion("white_list_gw not like", value, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwIn(List<String> values) {
            addCriterion("white_list_gw in", values, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwNotIn(List<String> values) {
            addCriterion("white_list_gw not in", values, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwBetween(String value1, String value2) {
            addCriterion("white_list_gw between", value1, value2, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListGwNotBetween(String value1, String value2) {
            addCriterion("white_list_gw not between", value1, value2, "whiteListGw");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpIsNull() {
            addCriterion("white_list_fp is null");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpIsNotNull() {
            addCriterion("white_list_fp is not null");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpEqualTo(String value) {
            addCriterion("white_list_fp =", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpNotEqualTo(String value) {
            addCriterion("white_list_fp <>", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpGreaterThan(String value) {
            addCriterion("white_list_fp >", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpGreaterThanOrEqualTo(String value) {
            addCriterion("white_list_fp >=", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpLessThan(String value) {
            addCriterion("white_list_fp <", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpLessThanOrEqualTo(String value) {
            addCriterion("white_list_fp <=", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpLike(String value) {
            addCriterion("white_list_fp like", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpNotLike(String value) {
            addCriterion("white_list_fp not like", value, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpIn(List<String> values) {
            addCriterion("white_list_fp in", values, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpNotIn(List<String> values) {
            addCriterion("white_list_fp not in", values, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpBetween(String value1, String value2) {
            addCriterion("white_list_fp between", value1, value2, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListFpNotBetween(String value1, String value2) {
            addCriterion("white_list_fp not between", value1, value2, "whiteListFp");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcIsNull() {
            addCriterion("white_list_yc is null");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcIsNotNull() {
            addCriterion("white_list_yc is not null");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcEqualTo(String value) {
            addCriterion("white_list_yc =", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcNotEqualTo(String value) {
            addCriterion("white_list_yc <>", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcGreaterThan(String value) {
            addCriterion("white_list_yc >", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcGreaterThanOrEqualTo(String value) {
            addCriterion("white_list_yc >=", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcLessThan(String value) {
            addCriterion("white_list_yc <", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcLessThanOrEqualTo(String value) {
            addCriterion("white_list_yc <=", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcLike(String value) {
            addCriterion("white_list_yc like", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcNotLike(String value) {
            addCriterion("white_list_yc not like", value, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcIn(List<String> values) {
            addCriterion("white_list_yc in", values, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcNotIn(List<String> values) {
            addCriterion("white_list_yc not in", values, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcBetween(String value1, String value2) {
            addCriterion("white_list_yc between", value1, value2, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andWhiteListYcNotBetween(String value1, String value2) {
            addCriterion("white_list_yc not between", value1, value2, "whiteListYc");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolIsNull() {
            addCriterion("prioritySymbol is null");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolIsNotNull() {
            addCriterion("prioritySymbol is not null");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolEqualTo(String value) {
            addCriterion("prioritySymbol =", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolNotEqualTo(String value) {
            addCriterion("prioritySymbol <>", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolGreaterThan(String value) {
            addCriterion("prioritySymbol >", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolGreaterThanOrEqualTo(String value) {
            addCriterion("prioritySymbol >=", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolLessThan(String value) {
            addCriterion("prioritySymbol <", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolLessThanOrEqualTo(String value) {
            addCriterion("prioritySymbol <=", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolLike(String value) {
            addCriterion("prioritySymbol like", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolNotLike(String value) {
            addCriterion("prioritySymbol not like", value, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolIn(List<String> values) {
            addCriterion("prioritySymbol in", values, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolNotIn(List<String> values) {
            addCriterion("prioritySymbol not in", values, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolBetween(String value1, String value2) {
            addCriterion("prioritySymbol between", value1, value2, "prioritysymbol");
            return (Criteria) this;
        }

        public Criteria andPrioritysymbolNotBetween(String value1, String value2) {
            addCriterion("prioritySymbol not between", value1, value2, "prioritysymbol");
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