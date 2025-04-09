package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryUserRealMessageExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QueryUserRealMessageExample() {
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

        public Criteria andRespDataIsNull() {
            addCriterion("resp_data is null");
            return (Criteria) this;
        }

        public Criteria andRespDataIsNotNull() {
            addCriterion("resp_data is not null");
            return (Criteria) this;
        }

        public Criteria andRespDataEqualTo(String value) {
            addCriterion("resp_data =", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataNotEqualTo(String value) {
            addCriterion("resp_data <>", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataGreaterThan(String value) {
            addCriterion("resp_data >", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataGreaterThanOrEqualTo(String value) {
            addCriterion("resp_data >=", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataLessThan(String value) {
            addCriterion("resp_data <", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataLessThanOrEqualTo(String value) {
            addCriterion("resp_data <=", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataLike(String value) {
            addCriterion("resp_data like", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataNotLike(String value) {
            addCriterion("resp_data not like", value, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataIn(List<String> values) {
            addCriterion("resp_data in", values, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataNotIn(List<String> values) {
            addCriterion("resp_data not in", values, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataBetween(String value1, String value2) {
            addCriterion("resp_data between", value1, value2, "respData");
            return (Criteria) this;
        }

        public Criteria andRespDataNotBetween(String value1, String value2) {
            addCriterion("resp_data not between", value1, value2, "respData");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoIsNull() {
            addCriterion("unique_req_no is null");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoIsNotNull() {
            addCriterion("unique_req_no is not null");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoEqualTo(String value) {
            addCriterion("unique_req_no =", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoNotEqualTo(String value) {
            addCriterion("unique_req_no <>", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoGreaterThan(String value) {
            addCriterion("unique_req_no >", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoGreaterThanOrEqualTo(String value) {
            addCriterion("unique_req_no >=", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoLessThan(String value) {
            addCriterion("unique_req_no <", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoLessThanOrEqualTo(String value) {
            addCriterion("unique_req_no <=", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoLike(String value) {
            addCriterion("unique_req_no like", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoNotLike(String value) {
            addCriterion("unique_req_no not like", value, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoIn(List<String> values) {
            addCriterion("unique_req_no in", values, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoNotIn(List<String> values) {
            addCriterion("unique_req_no not in", values, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoBetween(String value1, String value2) {
            addCriterion("unique_req_no between", value1, value2, "uniqueReqNo");
            return (Criteria) this;
        }

        public Criteria andUniqueReqNoNotBetween(String value1, String value2) {
            addCriterion("unique_req_no not between", value1, value2, "uniqueReqNo");
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

        public Criteria andMobileMd5IsNull() {
            addCriterion("mobile_md5 is null");
            return (Criteria) this;
        }

        public Criteria andMobileMd5IsNotNull() {
            addCriterion("mobile_md5 is not null");
            return (Criteria) this;
        }

        public Criteria andMobileMd5EqualTo(String value) {
            addCriterion("mobile_md5 =", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5NotEqualTo(String value) {
            addCriterion("mobile_md5 <>", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5GreaterThan(String value) {
            addCriterion("mobile_md5 >", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5GreaterThanOrEqualTo(String value) {
            addCriterion("mobile_md5 >=", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5LessThan(String value) {
            addCriterion("mobile_md5 <", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5LessThanOrEqualTo(String value) {
            addCriterion("mobile_md5 <=", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5Like(String value) {
            addCriterion("mobile_md5 like", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5NotLike(String value) {
            addCriterion("mobile_md5 not like", value, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5In(List<String> values) {
            addCriterion("mobile_md5 in", values, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5NotIn(List<String> values) {
            addCriterion("mobile_md5 not in", values, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5Between(String value1, String value2) {
            addCriterion("mobile_md5 between", value1, value2, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andMobileMd5NotBetween(String value1, String value2) {
            addCriterion("mobile_md5 not between", value1, value2, "mobileMd5");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignIsNull() {
            addCriterion("stop_marketing_sign is null");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignIsNotNull() {
            addCriterion("stop_marketing_sign is not null");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignEqualTo(String value) {
            addCriterion("stop_marketing_sign =", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignNotEqualTo(String value) {
            addCriterion("stop_marketing_sign <>", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignGreaterThan(String value) {
            addCriterion("stop_marketing_sign >", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignGreaterThanOrEqualTo(String value) {
            addCriterion("stop_marketing_sign >=", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignLessThan(String value) {
            addCriterion("stop_marketing_sign <", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignLessThanOrEqualTo(String value) {
            addCriterion("stop_marketing_sign <=", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignLike(String value) {
            addCriterion("stop_marketing_sign like", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignNotLike(String value) {
            addCriterion("stop_marketing_sign not like", value, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignIn(List<String> values) {
            addCriterion("stop_marketing_sign in", values, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignNotIn(List<String> values) {
            addCriterion("stop_marketing_sign not in", values, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignBetween(String value1, String value2) {
            addCriterion("stop_marketing_sign between", value1, value2, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andStopMarketingSignNotBetween(String value1, String value2) {
            addCriterion("stop_marketing_sign not between", value1, value2, "stopMarketingSign");
            return (Criteria) this;
        }

        public Criteria andUserMessageIsNull() {
            addCriterion("user_message is null");
            return (Criteria) this;
        }

        public Criteria andUserMessageIsNotNull() {
            addCriterion("user_message is not null");
            return (Criteria) this;
        }

        public Criteria andUserMessageEqualTo(String value) {
            addCriterion("user_message =", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageNotEqualTo(String value) {
            addCriterion("user_message <>", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageGreaterThan(String value) {
            addCriterion("user_message >", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageGreaterThanOrEqualTo(String value) {
            addCriterion("user_message >=", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageLessThan(String value) {
            addCriterion("user_message <", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageLessThanOrEqualTo(String value) {
            addCriterion("user_message <=", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageLike(String value) {
            addCriterion("user_message like", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageNotLike(String value) {
            addCriterion("user_message not like", value, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageIn(List<String> values) {
            addCriterion("user_message in", values, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageNotIn(List<String> values) {
            addCriterion("user_message not in", values, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageBetween(String value1, String value2) {
            addCriterion("user_message between", value1, value2, "userMessage");
            return (Criteria) this;
        }

        public Criteria andUserMessageNotBetween(String value1, String value2) {
            addCriterion("user_message not between", value1, value2, "userMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageIsNull() {
            addCriterion("risk_message is null");
            return (Criteria) this;
        }

        public Criteria andRiskMessageIsNotNull() {
            addCriterion("risk_message is not null");
            return (Criteria) this;
        }

        public Criteria andRiskMessageEqualTo(String value) {
            addCriterion("risk_message =", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageNotEqualTo(String value) {
            addCriterion("risk_message <>", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageGreaterThan(String value) {
            addCriterion("risk_message >", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageGreaterThanOrEqualTo(String value) {
            addCriterion("risk_message >=", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageLessThan(String value) {
            addCriterion("risk_message <", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageLessThanOrEqualTo(String value) {
            addCriterion("risk_message <=", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageLike(String value) {
            addCriterion("risk_message like", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageNotLike(String value) {
            addCriterion("risk_message not like", value, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageIn(List<String> values) {
            addCriterion("risk_message in", values, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageNotIn(List<String> values) {
            addCriterion("risk_message not in", values, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageBetween(String value1, String value2) {
            addCriterion("risk_message between", value1, value2, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andRiskMessageNotBetween(String value1, String value2) {
            addCriterion("risk_message not between", value1, value2, "riskMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageIsNull() {
            addCriterion("trade_message is null");
            return (Criteria) this;
        }

        public Criteria andTradeMessageIsNotNull() {
            addCriterion("trade_message is not null");
            return (Criteria) this;
        }

        public Criteria andTradeMessageEqualTo(String value) {
            addCriterion("trade_message =", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageNotEqualTo(String value) {
            addCriterion("trade_message <>", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageGreaterThan(String value) {
            addCriterion("trade_message >", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageGreaterThanOrEqualTo(String value) {
            addCriterion("trade_message >=", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageLessThan(String value) {
            addCriterion("trade_message <", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageLessThanOrEqualTo(String value) {
            addCriterion("trade_message <=", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageLike(String value) {
            addCriterion("trade_message like", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageNotLike(String value) {
            addCriterion("trade_message not like", value, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageIn(List<String> values) {
            addCriterion("trade_message in", values, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageNotIn(List<String> values) {
            addCriterion("trade_message not in", values, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageBetween(String value1, String value2) {
            addCriterion("trade_message between", value1, value2, "tradeMessage");
            return (Criteria) this;
        }

        public Criteria andTradeMessageNotBetween(String value1, String value2) {
            addCriterion("trade_message not between", value1, value2, "tradeMessage");
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

        public Criteria andCreateDateIsNull() {
            addCriterion("create_date is null");
            return (Criteria) this;
        }

        public Criteria andCreateDateIsNotNull() {
            addCriterion("create_date is not null");
            return (Criteria) this;
        }

        public Criteria andCreateDateEqualTo(String value) {
            addCriterion("create_date =", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotEqualTo(String value) {
            addCriterion("create_date <>", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThan(String value) {
            addCriterion("create_date >", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThanOrEqualTo(String value) {
            addCriterion("create_date >=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThan(String value) {
            addCriterion("create_date <", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThanOrEqualTo(String value) {
            addCriterion("create_date <=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLike(String value) {
            addCriterion("create_date like", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotLike(String value) {
            addCriterion("create_date not like", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateIn(List<String> values) {
            addCriterion("create_date in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotIn(List<String> values) {
            addCriterion("create_date not in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateBetween(String value1, String value2) {
            addCriterion("create_date between", value1, value2, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotBetween(String value1, String value2) {
            addCriterion("create_date not between", value1, value2, "createDate");
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

        public Criteria andUploadUpdateStatusIsNull() {
            addCriterion("upload_update_status is null");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusIsNotNull() {
            addCriterion("upload_update_status is not null");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusEqualTo(Integer value) {
            addCriterion("upload_update_status =", value, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusNotEqualTo(Integer value) {
            addCriterion("upload_update_status <>", value, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusGreaterThan(Integer value) {
            addCriterion("upload_update_status >", value, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("upload_update_status >=", value, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusLessThan(Integer value) {
            addCriterion("upload_update_status <", value, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusLessThanOrEqualTo(Integer value) {
            addCriterion("upload_update_status <=", value, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusIn(List<Integer> values) {
            addCriterion("upload_update_status in", values, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusNotIn(List<Integer> values) {
            addCriterion("upload_update_status not in", values, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusBetween(Integer value1, Integer value2) {
            addCriterion("upload_update_status between", value1, value2, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andUploadUpdateStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("upload_update_status not between", value1, value2, "uploadUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusIsNull() {
            addCriterion("es_update_status is null");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusIsNotNull() {
            addCriterion("es_update_status is not null");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusEqualTo(Integer value) {
            addCriterion("es_update_status =", value, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusNotEqualTo(Integer value) {
            addCriterion("es_update_status <>", value, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusGreaterThan(Integer value) {
            addCriterion("es_update_status >", value, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("es_update_status >=", value, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusLessThan(Integer value) {
            addCriterion("es_update_status <", value, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusLessThanOrEqualTo(Integer value) {
            addCriterion("es_update_status <=", value, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusIn(List<Integer> values) {
            addCriterion("es_update_status in", values, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusNotIn(List<Integer> values) {
            addCriterion("es_update_status not in", values, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusBetween(Integer value1, Integer value2) {
            addCriterion("es_update_status between", value1, value2, "esUpdateStatus");
            return (Criteria) this;
        }

        public Criteria andEsUpdateStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("es_update_status not between", value1, value2, "esUpdateStatus");
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