package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QifuSaveReachDeleteRecordApiPushLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QifuSaveReachDeleteRecordApiPushLogExample() {
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

        public Criteria andRespFlagIsNull() {
            addCriterion("resp_flag is null");
            return (Criteria) this;
        }

        public Criteria andRespFlagIsNotNull() {
            addCriterion("resp_flag is not null");
            return (Criteria) this;
        }

        public Criteria andRespFlagEqualTo(String value) {
            addCriterion("resp_flag =", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagNotEqualTo(String value) {
            addCriterion("resp_flag <>", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagGreaterThan(String value) {
            addCriterion("resp_flag >", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagGreaterThanOrEqualTo(String value) {
            addCriterion("resp_flag >=", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagLessThan(String value) {
            addCriterion("resp_flag <", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagLessThanOrEqualTo(String value) {
            addCriterion("resp_flag <=", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagLike(String value) {
            addCriterion("resp_flag like", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagNotLike(String value) {
            addCriterion("resp_flag not like", value, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagIn(List<String> values) {
            addCriterion("resp_flag in", values, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagNotIn(List<String> values) {
            addCriterion("resp_flag not in", values, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagBetween(String value1, String value2) {
            addCriterion("resp_flag between", value1, value2, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespFlagNotBetween(String value1, String value2) {
            addCriterion("resp_flag not between", value1, value2, "respFlag");
            return (Criteria) this;
        }

        public Criteria andRespCodeIsNull() {
            addCriterion("resp_code is null");
            return (Criteria) this;
        }

        public Criteria andRespCodeIsNotNull() {
            addCriterion("resp_code is not null");
            return (Criteria) this;
        }

        public Criteria andRespCodeEqualTo(String value) {
            addCriterion("resp_code =", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeNotEqualTo(String value) {
            addCriterion("resp_code <>", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeGreaterThan(String value) {
            addCriterion("resp_code >", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeGreaterThanOrEqualTo(String value) {
            addCriterion("resp_code >=", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeLessThan(String value) {
            addCriterion("resp_code <", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeLessThanOrEqualTo(String value) {
            addCriterion("resp_code <=", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeLike(String value) {
            addCriterion("resp_code like", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeNotLike(String value) {
            addCriterion("resp_code not like", value, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeIn(List<String> values) {
            addCriterion("resp_code in", values, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeNotIn(List<String> values) {
            addCriterion("resp_code not in", values, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeBetween(String value1, String value2) {
            addCriterion("resp_code between", value1, value2, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespCodeNotBetween(String value1, String value2) {
            addCriterion("resp_code not between", value1, value2, "respCode");
            return (Criteria) this;
        }

        public Criteria andRespMsgIsNull() {
            addCriterion("resp_msg is null");
            return (Criteria) this;
        }

        public Criteria andRespMsgIsNotNull() {
            addCriterion("resp_msg is not null");
            return (Criteria) this;
        }

        public Criteria andRespMsgEqualTo(String value) {
            addCriterion("resp_msg =", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgNotEqualTo(String value) {
            addCriterion("resp_msg <>", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgGreaterThan(String value) {
            addCriterion("resp_msg >", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgGreaterThanOrEqualTo(String value) {
            addCriterion("resp_msg >=", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgLessThan(String value) {
            addCriterion("resp_msg <", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgLessThanOrEqualTo(String value) {
            addCriterion("resp_msg <=", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgLike(String value) {
            addCriterion("resp_msg like", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgNotLike(String value) {
            addCriterion("resp_msg not like", value, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgIn(List<String> values) {
            addCriterion("resp_msg in", values, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgNotIn(List<String> values) {
            addCriterion("resp_msg not in", values, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgBetween(String value1, String value2) {
            addCriterion("resp_msg between", value1, value2, "respMsg");
            return (Criteria) this;
        }

        public Criteria andRespMsgNotBetween(String value1, String value2) {
            addCriterion("resp_msg not between", value1, value2, "respMsg");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedIsNull() {
            addCriterion("qifu_is_succeed is null");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedIsNotNull() {
            addCriterion("qifu_is_succeed is not null");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedEqualTo(String value) {
            addCriterion("qifu_is_succeed =", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedNotEqualTo(String value) {
            addCriterion("qifu_is_succeed <>", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedGreaterThan(String value) {
            addCriterion("qifu_is_succeed >", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedGreaterThanOrEqualTo(String value) {
            addCriterion("qifu_is_succeed >=", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedLessThan(String value) {
            addCriterion("qifu_is_succeed <", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedLessThanOrEqualTo(String value) {
            addCriterion("qifu_is_succeed <=", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedLike(String value) {
            addCriterion("qifu_is_succeed like", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedNotLike(String value) {
            addCriterion("qifu_is_succeed not like", value, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedIn(List<String> values) {
            addCriterion("qifu_is_succeed in", values, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedNotIn(List<String> values) {
            addCriterion("qifu_is_succeed not in", values, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedBetween(String value1, String value2) {
            addCriterion("qifu_is_succeed between", value1, value2, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuIsSucceedNotBetween(String value1, String value2) {
            addCriterion("qifu_is_succeed not between", value1, value2, "qifuIsSucceed");
            return (Criteria) this;
        }

        public Criteria andQifuMessageIsNull() {
            addCriterion("qifu_message is null");
            return (Criteria) this;
        }

        public Criteria andQifuMessageIsNotNull() {
            addCriterion("qifu_message is not null");
            return (Criteria) this;
        }

        public Criteria andQifuMessageEqualTo(String value) {
            addCriterion("qifu_message =", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageNotEqualTo(String value) {
            addCriterion("qifu_message <>", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageGreaterThan(String value) {
            addCriterion("qifu_message >", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageGreaterThanOrEqualTo(String value) {
            addCriterion("qifu_message >=", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageLessThan(String value) {
            addCriterion("qifu_message <", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageLessThanOrEqualTo(String value) {
            addCriterion("qifu_message <=", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageLike(String value) {
            addCriterion("qifu_message like", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageNotLike(String value) {
            addCriterion("qifu_message not like", value, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageIn(List<String> values) {
            addCriterion("qifu_message in", values, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageNotIn(List<String> values) {
            addCriterion("qifu_message not in", values, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageBetween(String value1, String value2) {
            addCriterion("qifu_message between", value1, value2, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andQifuMessageNotBetween(String value1, String value2) {
            addCriterion("qifu_message not between", value1, value2, "qifuMessage");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateIsNull() {
            addCriterion("sync_applet_date is null");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateIsNotNull() {
            addCriterion("sync_applet_date is not null");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateEqualTo(String value) {
            addCriterion("sync_applet_date =", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateNotEqualTo(String value) {
            addCriterion("sync_applet_date <>", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateGreaterThan(String value) {
            addCriterion("sync_applet_date >", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateGreaterThanOrEqualTo(String value) {
            addCriterion("sync_applet_date >=", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateLessThan(String value) {
            addCriterion("sync_applet_date <", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateLessThanOrEqualTo(String value) {
            addCriterion("sync_applet_date <=", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateLike(String value) {
            addCriterion("sync_applet_date like", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateNotLike(String value) {
            addCriterion("sync_applet_date not like", value, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateIn(List<String> values) {
            addCriterion("sync_applet_date in", values, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateNotIn(List<String> values) {
            addCriterion("sync_applet_date not in", values, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateBetween(String value1, String value2) {
            addCriterion("sync_applet_date between", value1, value2, "syncAppletDate");
            return (Criteria) this;
        }

        public Criteria andSyncAppletDateNotBetween(String value1, String value2) {
            addCriterion("sync_applet_date not between", value1, value2, "syncAppletDate");
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

        public Criteria andPushDateIsNull() {
            addCriterion("push_date is null");
            return (Criteria) this;
        }

        public Criteria andPushDateIsNotNull() {
            addCriterion("push_date is not null");
            return (Criteria) this;
        }

        public Criteria andPushDateEqualTo(String value) {
            addCriterion("push_date =", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateNotEqualTo(String value) {
            addCriterion("push_date <>", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateGreaterThan(String value) {
            addCriterion("push_date >", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateGreaterThanOrEqualTo(String value) {
            addCriterion("push_date >=", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateLessThan(String value) {
            addCriterion("push_date <", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateLessThanOrEqualTo(String value) {
            addCriterion("push_date <=", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateLike(String value) {
            addCriterion("push_date like", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateNotLike(String value) {
            addCriterion("push_date not like", value, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateIn(List<String> values) {
            addCriterion("push_date in", values, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateNotIn(List<String> values) {
            addCriterion("push_date not in", values, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateBetween(String value1, String value2) {
            addCriterion("push_date between", value1, value2, "pushDate");
            return (Criteria) this;
        }

        public Criteria andPushDateNotBetween(String value1, String value2) {
            addCriterion("push_date not between", value1, value2, "pushDate");
            return (Criteria) this;
        }

        public Criteria andErrorMsgIsNull() {
            addCriterion("error_msg is null");
            return (Criteria) this;
        }

        public Criteria andErrorMsgIsNotNull() {
            addCriterion("error_msg is not null");
            return (Criteria) this;
        }

        public Criteria andErrorMsgEqualTo(String value) {
            addCriterion("error_msg =", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgNotEqualTo(String value) {
            addCriterion("error_msg <>", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgGreaterThan(String value) {
            addCriterion("error_msg >", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgGreaterThanOrEqualTo(String value) {
            addCriterion("error_msg >=", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgLessThan(String value) {
            addCriterion("error_msg <", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgLessThanOrEqualTo(String value) {
            addCriterion("error_msg <=", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgLike(String value) {
            addCriterion("error_msg like", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgNotLike(String value) {
            addCriterion("error_msg not like", value, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgIn(List<String> values) {
            addCriterion("error_msg in", values, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgNotIn(List<String> values) {
            addCriterion("error_msg not in", values, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgBetween(String value1, String value2) {
            addCriterion("error_msg between", value1, value2, "errorMsg");
            return (Criteria) this;
        }

        public Criteria andErrorMsgNotBetween(String value1, String value2) {
            addCriterion("error_msg not between", value1, value2, "errorMsg");
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

        public Criteria andRequestNoIsNull() {
            addCriterion("request_no is null");
            return (Criteria) this;
        }

        public Criteria andRequestNoIsNotNull() {
            addCriterion("request_no is not null");
            return (Criteria) this;
        }

        public Criteria andRequestNoEqualTo(String value) {
            addCriterion("request_no =", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoNotEqualTo(String value) {
            addCriterion("request_no <>", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoGreaterThan(String value) {
            addCriterion("request_no >", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoGreaterThanOrEqualTo(String value) {
            addCriterion("request_no >=", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoLessThan(String value) {
            addCriterion("request_no <", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoLessThanOrEqualTo(String value) {
            addCriterion("request_no <=", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoLike(String value) {
            addCriterion("request_no like", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoNotLike(String value) {
            addCriterion("request_no not like", value, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoIn(List<String> values) {
            addCriterion("request_no in", values, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoNotIn(List<String> values) {
            addCriterion("request_no not in", values, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoBetween(String value1, String value2) {
            addCriterion("request_no between", value1, value2, "requestNo");
            return (Criteria) this;
        }

        public Criteria andRequestNoNotBetween(String value1, String value2) {
            addCriterion("request_no not between", value1, value2, "requestNo");
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