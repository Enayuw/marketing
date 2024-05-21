package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RequestOperationLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<CriteriaAbstract> oredCriteria;

    public RequestOperationLogExample() {
        oredCriteria = new ArrayList<CriteriaAbstract>();
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

    public List<CriteriaAbstract> getOredCriteria() {
        return oredCriteria;
    }

    public void or(CriteriaAbstract criteria) {
        oredCriteria.add(criteria);
    }

    public CriteriaAbstract or() {
        CriteriaAbstract criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public CriteriaAbstract createCriteria() {
        CriteriaAbstract criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected CriteriaAbstract createCriteriaInternal() {
        CriteriaAbstract criteria = new CriteriaAbstract();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class AbstractGeneratedCriteria {
        protected List<Criterion> criteria;

        protected AbstractGeneratedCriteria() {
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

        public CriteriaAbstract andIdIsNull() {
            addCriterion("id is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdIsNotNull() {
            addCriterion("id is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorIsNull() {
            addCriterion("`operator` is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorIsNotNull() {
            addCriterion("`operator` is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorEqualTo(String value) {
            addCriterion("`operator` =", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorNotEqualTo(String value) {
            addCriterion("`operator` <>", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorGreaterThan(String value) {
            addCriterion("`operator` >", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorGreaterThanOrEqualTo(String value) {
            addCriterion("`operator` >=", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorLessThan(String value) {
            addCriterion("`operator` <", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorLessThanOrEqualTo(String value) {
            addCriterion("`operator` <=", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorLike(String value) {
            addCriterion("`operator` like", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorNotLike(String value) {
            addCriterion("`operator` not like", value, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorIn(List<String> values) {
            addCriterion("`operator` in", values, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorNotIn(List<String> values) {
            addCriterion("`operator` not in", values, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorBetween(String value1, String value2) {
            addCriterion("`operator` between", value1, value2, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOperatorNotBetween(String value1, String value2) {
            addCriterion("`operator` not between", value1, value2, "operator");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoIsNull() {
            addCriterion("biz_no is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoIsNotNull() {
            addCriterion("biz_no is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoEqualTo(String value) {
            addCriterion("biz_no =", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoNotEqualTo(String value) {
            addCriterion("biz_no <>", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoGreaterThan(String value) {
            addCriterion("biz_no >", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoGreaterThanOrEqualTo(String value) {
            addCriterion("biz_no >=", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoLessThan(String value) {
            addCriterion("biz_no <", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoLessThanOrEqualTo(String value) {
            addCriterion("biz_no <=", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoLike(String value) {
            addCriterion("biz_no like", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoNotLike(String value) {
            addCriterion("biz_no not like", value, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoIn(List<String> values) {
            addCriterion("biz_no in", values, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoNotIn(List<String> values) {
            addCriterion("biz_no not in", values, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoBetween(String value1, String value2) {
            addCriterion("biz_no between", value1, value2, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andBizNoNotBetween(String value1, String value2) {
            addCriterion("biz_no not between", value1, value2, "bizNo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamIsNull() {
            addCriterion("request_param is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamIsNotNull() {
            addCriterion("request_param is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamEqualTo(String value) {
            addCriterion("request_param =", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamNotEqualTo(String value) {
            addCriterion("request_param <>", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamGreaterThan(String value) {
            addCriterion("request_param >", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamGreaterThanOrEqualTo(String value) {
            addCriterion("request_param >=", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamLessThan(String value) {
            addCriterion("request_param <", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamLessThanOrEqualTo(String value) {
            addCriterion("request_param <=", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamLike(String value) {
            addCriterion("request_param like", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamNotLike(String value) {
            addCriterion("request_param not like", value, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamIn(List<String> values) {
            addCriterion("request_param in", values, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamNotIn(List<String> values) {
            addCriterion("request_param not in", values, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamBetween(String value1, String value2) {
            addCriterion("request_param between", value1, value2, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andRequestParamNotBetween(String value1, String value2) {
            addCriterion("request_param not between", value1, value2, "requestParam");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultIsNull() {
            addCriterion("`result` is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultIsNotNull() {
            addCriterion("`result` is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultEqualTo(String value) {
            addCriterion("`result` =", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultNotEqualTo(String value) {
            addCriterion("`result` <>", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultGreaterThan(String value) {
            addCriterion("`result` >", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultGreaterThanOrEqualTo(String value) {
            addCriterion("`result` >=", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultLessThan(String value) {
            addCriterion("`result` <", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultLessThanOrEqualTo(String value) {
            addCriterion("`result` <=", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultLike(String value) {
            addCriterion("`result` like", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultNotLike(String value) {
            addCriterion("`result` not like", value, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultIn(List<String> values) {
            addCriterion("`result` in", values, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultNotIn(List<String> values) {
            addCriterion("`result` not in", values, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultBetween(String value1, String value2) {
            addCriterion("`result` between", value1, value2, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andResultNotBetween(String value1, String value2) {
            addCriterion("`result` not between", value1, value2, "result");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlIsNull() {
            addCriterion("url is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlIsNotNull() {
            addCriterion("url is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlEqualTo(String value) {
            addCriterion("url =", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlNotEqualTo(String value) {
            addCriterion("url <>", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlGreaterThan(String value) {
            addCriterion("url >", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlGreaterThanOrEqualTo(String value) {
            addCriterion("url >=", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlLessThan(String value) {
            addCriterion("url <", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlLessThanOrEqualTo(String value) {
            addCriterion("url <=", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlLike(String value) {
            addCriterion("url like", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlNotLike(String value) {
            addCriterion("url not like", value, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlIn(List<String> values) {
            addCriterion("url in", values, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlNotIn(List<String> values) {
            addCriterion("url not in", values, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlBetween(String value1, String value2) {
            addCriterion("url between", value1, value2, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUrlNotBetween(String value1, String value2) {
            addCriterion("url not between", value1, value2, "url");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoIsNull() {
            addCriterion("extend_info is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoIsNotNull() {
            addCriterion("extend_info is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoEqualTo(String value) {
            addCriterion("extend_info =", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoNotEqualTo(String value) {
            addCriterion("extend_info <>", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoGreaterThan(String value) {
            addCriterion("extend_info >", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoGreaterThanOrEqualTo(String value) {
            addCriterion("extend_info >=", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoLessThan(String value) {
            addCriterion("extend_info <", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoLessThanOrEqualTo(String value) {
            addCriterion("extend_info <=", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoLike(String value) {
            addCriterion("extend_info like", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoNotLike(String value) {
            addCriterion("extend_info not like", value, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoIn(List<String> values) {
            addCriterion("extend_info in", values, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoNotIn(List<String> values) {
            addCriterion("extend_info not in", values, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoBetween(String value1, String value2) {
            addCriterion("extend_info between", value1, value2, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andExtendInfoNotBetween(String value1, String value2) {
            addCriterion("extend_info not between", value1, value2, "extendInfo");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueIsNull() {
            addCriterion("original_value is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueIsNotNull() {
            addCriterion("original_value is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueEqualTo(String value) {
            addCriterion("original_value =", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueNotEqualTo(String value) {
            addCriterion("original_value <>", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueGreaterThan(String value) {
            addCriterion("original_value >", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueGreaterThanOrEqualTo(String value) {
            addCriterion("original_value >=", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueLessThan(String value) {
            addCriterion("original_value <", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueLessThanOrEqualTo(String value) {
            addCriterion("original_value <=", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueLike(String value) {
            addCriterion("original_value like", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueNotLike(String value) {
            addCriterion("original_value not like", value, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueIn(List<String> values) {
            addCriterion("original_value in", values, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueNotIn(List<String> values) {
            addCriterion("original_value not in", values, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueBetween(String value1, String value2) {
            addCriterion("original_value between", value1, value2, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andOriginalValueNotBetween(String value1, String value2) {
            addCriterion("original_value not between", value1, value2, "originalValue");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteIsNull() {
            addCriterion("is_delete is null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteIsNotNull() {
            addCriterion("is_delete is not null");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteEqualTo(Integer value) {
            addCriterion("is_delete =", value, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteNotEqualTo(Integer value) {
            addCriterion("is_delete <>", value, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteGreaterThan(Integer value) {
            addCriterion("is_delete >", value, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_delete >=", value, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteLessThan(Integer value) {
            addCriterion("is_delete <", value, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteLessThanOrEqualTo(Integer value) {
            addCriterion("is_delete <=", value, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteIn(List<Integer> values) {
            addCriterion("is_delete in", values, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteNotIn(List<Integer> values) {
            addCriterion("is_delete not in", values, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteBetween(Integer value1, Integer value2) {
            addCriterion("is_delete between", value1, value2, "isDelete");
            return (CriteriaAbstract) this;
        }

        public CriteriaAbstract andIsDeleteNotBetween(Integer value1, Integer value2) {
            addCriterion("is_delete not between", value1, value2, "isDelete");
            return (CriteriaAbstract) this;
        }
    }

    public static class CriteriaAbstract extends AbstractGeneratedCriteria {

        protected CriteriaAbstract() {
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