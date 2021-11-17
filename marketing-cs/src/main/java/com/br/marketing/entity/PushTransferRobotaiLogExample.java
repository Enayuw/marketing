package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PushTransferRobotaiLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PushTransferRobotaiLogExample() {
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

        public Criteria andTransferInfoIdIsNull() {
            addCriterion("transfer_info_id is null");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdIsNotNull() {
            addCriterion("transfer_info_id is not null");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdEqualTo(Long value) {
            addCriterion("transfer_info_id =", value, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdNotEqualTo(Long value) {
            addCriterion("transfer_info_id <>", value, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdGreaterThan(Long value) {
            addCriterion("transfer_info_id >", value, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdGreaterThanOrEqualTo(Long value) {
            addCriterion("transfer_info_id >=", value, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdLessThan(Long value) {
            addCriterion("transfer_info_id <", value, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdLessThanOrEqualTo(Long value) {
            addCriterion("transfer_info_id <=", value, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdIn(List<Long> values) {
            addCriterion("transfer_info_id in", values, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdNotIn(List<Long> values) {
            addCriterion("transfer_info_id not in", values, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdBetween(Long value1, Long value2) {
            addCriterion("transfer_info_id between", value1, value2, "transferInfoId");
            return (Criteria) this;
        }

        public Criteria andTransferInfoIdNotBetween(Long value1, Long value2) {
            addCriterion("transfer_info_id not between", value1, value2, "transferInfoId");
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

        public Criteria andResponseBodyIsNull() {
            addCriterion("response_body is null");
            return (Criteria) this;
        }

        public Criteria andResponseBodyIsNotNull() {
            addCriterion("response_body is not null");
            return (Criteria) this;
        }

        public Criteria andResponseBodyEqualTo(String value) {
            addCriterion("response_body =", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyNotEqualTo(String value) {
            addCriterion("response_body <>", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyGreaterThan(String value) {
            addCriterion("response_body >", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyGreaterThanOrEqualTo(String value) {
            addCriterion("response_body >=", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyLessThan(String value) {
            addCriterion("response_body <", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyLessThanOrEqualTo(String value) {
            addCriterion("response_body <=", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyLike(String value) {
            addCriterion("response_body like", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyNotLike(String value) {
            addCriterion("response_body not like", value, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyIn(List<String> values) {
            addCriterion("response_body in", values, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyNotIn(List<String> values) {
            addCriterion("response_body not in", values, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyBetween(String value1, String value2) {
            addCriterion("response_body between", value1, value2, "responseBody");
            return (Criteria) this;
        }

        public Criteria andResponseBodyNotBetween(String value1, String value2) {
            addCriterion("response_body not between", value1, value2, "responseBody");
            return (Criteria) this;
        }

        public Criteria andServiceCodeIsNull() {
            addCriterion("service_code is null");
            return (Criteria) this;
        }

        public Criteria andServiceCodeIsNotNull() {
            addCriterion("service_code is not null");
            return (Criteria) this;
        }

        public Criteria andServiceCodeEqualTo(String value) {
            addCriterion("service_code =", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeNotEqualTo(String value) {
            addCriterion("service_code <>", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeGreaterThan(String value) {
            addCriterion("service_code >", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeGreaterThanOrEqualTo(String value) {
            addCriterion("service_code >=", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeLessThan(String value) {
            addCriterion("service_code <", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeLessThanOrEqualTo(String value) {
            addCriterion("service_code <=", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeLike(String value) {
            addCriterion("service_code like", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeNotLike(String value) {
            addCriterion("service_code not like", value, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeIn(List<String> values) {
            addCriterion("service_code in", values, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeNotIn(List<String> values) {
            addCriterion("service_code not in", values, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeBetween(String value1, String value2) {
            addCriterion("service_code between", value1, value2, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andServiceCodeNotBetween(String value1, String value2) {
            addCriterion("service_code not between", value1, value2, "serviceCode");
            return (Criteria) this;
        }

        public Criteria andMessageIsNull() {
            addCriterion("message is null");
            return (Criteria) this;
        }

        public Criteria andMessageIsNotNull() {
            addCriterion("message is not null");
            return (Criteria) this;
        }

        public Criteria andMessageEqualTo(String value) {
            addCriterion("message =", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotEqualTo(String value) {
            addCriterion("message <>", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageGreaterThan(String value) {
            addCriterion("message >", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageGreaterThanOrEqualTo(String value) {
            addCriterion("message >=", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLessThan(String value) {
            addCriterion("message <", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLessThanOrEqualTo(String value) {
            addCriterion("message <=", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLike(String value) {
            addCriterion("message like", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotLike(String value) {
            addCriterion("message not like", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageIn(List<String> values) {
            addCriterion("message in", values, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotIn(List<String> values) {
            addCriterion("message not in", values, "message");
            return (Criteria) this;
        }

        public Criteria andMessageBetween(String value1, String value2) {
            addCriterion("message between", value1, value2, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotBetween(String value1, String value2) {
            addCriterion("message not between", value1, value2, "message");
            return (Criteria) this;
        }

        public Criteria andRowSizeIsNull() {
            addCriterion("row_size is null");
            return (Criteria) this;
        }

        public Criteria andRowSizeIsNotNull() {
            addCriterion("row_size is not null");
            return (Criteria) this;
        }

        public Criteria andRowSizeEqualTo(Integer value) {
            addCriterion("row_size =", value, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeNotEqualTo(Integer value) {
            addCriterion("row_size <>", value, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeGreaterThan(Integer value) {
            addCriterion("row_size >", value, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeGreaterThanOrEqualTo(Integer value) {
            addCriterion("row_size >=", value, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeLessThan(Integer value) {
            addCriterion("row_size <", value, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeLessThanOrEqualTo(Integer value) {
            addCriterion("row_size <=", value, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeIn(List<Integer> values) {
            addCriterion("row_size in", values, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeNotIn(List<Integer> values) {
            addCriterion("row_size not in", values, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeBetween(Integer value1, Integer value2) {
            addCriterion("row_size between", value1, value2, "rowSize");
            return (Criteria) this;
        }

        public Criteria andRowSizeNotBetween(Integer value1, Integer value2) {
            addCriterion("row_size not between", value1, value2, "rowSize");
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

        public Criteria andCompensateTimesIsNull() {
            addCriterion("compensate_times is null");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesIsNotNull() {
            addCriterion("compensate_times is not null");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesEqualTo(Integer value) {
            addCriterion("compensate_times =", value, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesNotEqualTo(Integer value) {
            addCriterion("compensate_times <>", value, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesGreaterThan(Integer value) {
            addCriterion("compensate_times >", value, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("compensate_times >=", value, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesLessThan(Integer value) {
            addCriterion("compensate_times <", value, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesLessThanOrEqualTo(Integer value) {
            addCriterion("compensate_times <=", value, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesIn(List<Integer> values) {
            addCriterion("compensate_times in", values, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesNotIn(List<Integer> values) {
            addCriterion("compensate_times not in", values, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesBetween(Integer value1, Integer value2) {
            addCriterion("compensate_times between", value1, value2, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andCompensateTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("compensate_times not between", value1, value2, "compensateTimes");
            return (Criteria) this;
        }

        public Criteria andPushStatusIsNull() {
            addCriterion("push_status is null");
            return (Criteria) this;
        }

        public Criteria andPushStatusIsNotNull() {
            addCriterion("push_status is not null");
            return (Criteria) this;
        }

        public Criteria andPushStatusEqualTo(Integer value) {
            addCriterion("push_status =", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotEqualTo(Integer value) {
            addCriterion("push_status <>", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThan(Integer value) {
            addCriterion("push_status >", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_status >=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThan(Integer value) {
            addCriterion("push_status <", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThanOrEqualTo(Integer value) {
            addCriterion("push_status <=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusIn(List<Integer> values) {
            addCriterion("push_status in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotIn(List<Integer> values) {
            addCriterion("push_status not in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusBetween(Integer value1, Integer value2) {
            addCriterion("push_status between", value1, value2, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("push_status not between", value1, value2, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andRequestBodyIsNull() {
            addCriterion("request_body is null");
            return (Criteria) this;
        }

        public Criteria andRequestBodyIsNotNull() {
            addCriterion("request_body is not null");
            return (Criteria) this;
        }

        public Criteria andRequestBodyEqualTo(String value) {
            addCriterion("request_body =", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyNotEqualTo(String value) {
            addCriterion("request_body <>", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyGreaterThan(String value) {
            addCriterion("request_body >", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyGreaterThanOrEqualTo(String value) {
            addCriterion("request_body >=", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyLessThan(String value) {
            addCriterion("request_body <", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyLessThanOrEqualTo(String value) {
            addCriterion("request_body <=", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyLike(String value) {
            addCriterion("request_body like", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyNotLike(String value) {
            addCriterion("request_body not like", value, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyIn(List<String> values) {
            addCriterion("request_body in", values, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyNotIn(List<String> values) {
            addCriterion("request_body not in", values, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyBetween(String value1, String value2) {
            addCriterion("request_body between", value1, value2, "requestBody");
            return (Criteria) this;
        }

        public Criteria andRequestBodyNotBetween(String value1, String value2) {
            addCriterion("request_body not between", value1, value2, "requestBody");
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