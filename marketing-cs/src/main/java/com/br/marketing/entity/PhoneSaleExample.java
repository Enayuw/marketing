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

        public Criteria andBatchIdIsNull() {
            addCriterion("batch_id is null");
            return (Criteria) this;
        }

        public Criteria andBatchIdIsNotNull() {
            addCriterion("batch_id is not null");
            return (Criteria) this;
        }

        public Criteria andBatchIdEqualTo(String value) {
            addCriterion("batch_id =", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdNotEqualTo(String value) {
            addCriterion("batch_id <>", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdGreaterThan(String value) {
            addCriterion("batch_id >", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdGreaterThanOrEqualTo(String value) {
            addCriterion("batch_id >=", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdLessThan(String value) {
            addCriterion("batch_id <", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdLessThanOrEqualTo(String value) {
            addCriterion("batch_id <=", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdLike(String value) {
            addCriterion("batch_id like", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdNotLike(String value) {
            addCriterion("batch_id not like", value, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdIn(List<String> values) {
            addCriterion("batch_id in", values, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdNotIn(List<String> values) {
            addCriterion("batch_id not in", values, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdBetween(String value1, String value2) {
            addCriterion("batch_id between", value1, value2, "batchId");
            return (Criteria) this;
        }

        public Criteria andBatchIdNotBetween(String value1, String value2) {
            addCriterion("batch_id not between", value1, value2, "batchId");
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

        public Criteria andCidAesIsNull() {
            addCriterion("cid_aes is null");
            return (Criteria) this;
        }

        public Criteria andCidAesIsNotNull() {
            addCriterion("cid_aes is not null");
            return (Criteria) this;
        }

        public Criteria andCidAesEqualTo(String value) {
            addCriterion("cid_aes =", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesNotEqualTo(String value) {
            addCriterion("cid_aes <>", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesGreaterThan(String value) {
            addCriterion("cid_aes >", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesGreaterThanOrEqualTo(String value) {
            addCriterion("cid_aes >=", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesLessThan(String value) {
            addCriterion("cid_aes <", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesLessThanOrEqualTo(String value) {
            addCriterion("cid_aes <=", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesLike(String value) {
            addCriterion("cid_aes like", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesNotLike(String value) {
            addCriterion("cid_aes not like", value, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesIn(List<String> values) {
            addCriterion("cid_aes in", values, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesNotIn(List<String> values) {
            addCriterion("cid_aes not in", values, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesBetween(String value1, String value2) {
            addCriterion("cid_aes between", value1, value2, "cidAes");
            return (Criteria) this;
        }

        public Criteria andCidAesNotBetween(String value1, String value2) {
            addCriterion("cid_aes not between", value1, value2, "cidAes");
            return (Criteria) this;
        }

        public Criteria andSexIsNull() {
            addCriterion("sex is null");
            return (Criteria) this;
        }

        public Criteria andSexIsNotNull() {
            addCriterion("sex is not null");
            return (Criteria) this;
        }

        public Criteria andSexEqualTo(String value) {
            addCriterion("sex =", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexNotEqualTo(String value) {
            addCriterion("sex <>", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexGreaterThan(String value) {
            addCriterion("sex >", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexGreaterThanOrEqualTo(String value) {
            addCriterion("sex >=", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexLessThan(String value) {
            addCriterion("sex <", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexLessThanOrEqualTo(String value) {
            addCriterion("sex <=", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexLike(String value) {
            addCriterion("sex like", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexNotLike(String value) {
            addCriterion("sex not like", value, "sex");
            return (Criteria) this;
        }

        public Criteria andSexIn(List<String> values) {
            addCriterion("sex in", values, "sex");
            return (Criteria) this;
        }

        public Criteria andSexNotIn(List<String> values) {
            addCriterion("sex not in", values, "sex");
            return (Criteria) this;
        }

        public Criteria andSexBetween(String value1, String value2) {
            addCriterion("sex between", value1, value2, "sex");
            return (Criteria) this;
        }

        public Criteria andSexNotBetween(String value1, String value2) {
            addCriterion("sex not between", value1, value2, "sex");
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

        public Criteria andRiskScoreIsNull() {
            addCriterion("risk_score is null");
            return (Criteria) this;
        }

        public Criteria andRiskScoreIsNotNull() {
            addCriterion("risk_score is not null");
            return (Criteria) this;
        }

        public Criteria andRiskScoreEqualTo(String value) {
            addCriterion("risk_score =", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreNotEqualTo(String value) {
            addCriterion("risk_score <>", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreGreaterThan(String value) {
            addCriterion("risk_score >", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreGreaterThanOrEqualTo(String value) {
            addCriterion("risk_score >=", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreLessThan(String value) {
            addCriterion("risk_score <", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreLessThanOrEqualTo(String value) {
            addCriterion("risk_score <=", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreLike(String value) {
            addCriterion("risk_score like", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreNotLike(String value) {
            addCriterion("risk_score not like", value, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreIn(List<String> values) {
            addCriterion("risk_score in", values, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreNotIn(List<String> values) {
            addCriterion("risk_score not in", values, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreBetween(String value1, String value2) {
            addCriterion("risk_score between", value1, value2, "riskScore");
            return (Criteria) this;
        }

        public Criteria andRiskScoreNotBetween(String value1, String value2) {
            addCriterion("risk_score not between", value1, value2, "riskScore");
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

        public Criteria andDayIsNull() {
            addCriterion("day is null");
            return (Criteria) this;
        }

        public Criteria andDayIsNotNull() {
            addCriterion("day is not null");
            return (Criteria) this;
        }

        public Criteria andDayEqualTo(String value) {
            addCriterion("day =", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayNotEqualTo(String value) {
            addCriterion("day <>", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayGreaterThan(String value) {
            addCriterion("day >", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayGreaterThanOrEqualTo(String value) {
            addCriterion("day >=", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayLessThan(String value) {
            addCriterion("day <", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayLessThanOrEqualTo(String value) {
            addCriterion("day <=", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayLike(String value) {
            addCriterion("day like", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayNotLike(String value) {
            addCriterion("day not like", value, "day");
            return (Criteria) this;
        }

        public Criteria andDayIn(List<String> values) {
            addCriterion("day in", values, "day");
            return (Criteria) this;
        }

        public Criteria andDayNotIn(List<String> values) {
            addCriterion("day not in", values, "day");
            return (Criteria) this;
        }

        public Criteria andDayBetween(String value1, String value2) {
            addCriterion("day between", value1, value2, "day");
            return (Criteria) this;
        }

        public Criteria andDayNotBetween(String value1, String value2) {
            addCriterion("day not between", value1, value2, "day");
            return (Criteria) this;
        }

        public Criteria andExtraSetIsNull() {
            addCriterion("extra_set is null");
            return (Criteria) this;
        }

        public Criteria andExtraSetIsNotNull() {
            addCriterion("extra_set is not null");
            return (Criteria) this;
        }

        public Criteria andExtraSetEqualTo(String value) {
            addCriterion("extra_set =", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetNotEqualTo(String value) {
            addCriterion("extra_set <>", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetGreaterThan(String value) {
            addCriterion("extra_set >", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetGreaterThanOrEqualTo(String value) {
            addCriterion("extra_set >=", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetLessThan(String value) {
            addCriterion("extra_set <", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetLessThanOrEqualTo(String value) {
            addCriterion("extra_set <=", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetLike(String value) {
            addCriterion("extra_set like", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetNotLike(String value) {
            addCriterion("extra_set not like", value, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetIn(List<String> values) {
            addCriterion("extra_set in", values, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetNotIn(List<String> values) {
            addCriterion("extra_set not in", values, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetBetween(String value1, String value2) {
            addCriterion("extra_set between", value1, value2, "extraSet");
            return (Criteria) this;
        }

        public Criteria andExtraSetNotBetween(String value1, String value2) {
            addCriterion("extra_set not between", value1, value2, "extraSet");
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