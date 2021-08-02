package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingSyncInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public MarketingSyncInfoExample() {
        oredCriteria = new ArrayList<BaseCriteria>();
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

    public List<BaseCriteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(BaseCriteria criteria) {
        oredCriteria.add(criteria);
    }

    public BaseCriteria or() {
        BaseCriteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public BaseCriteria createCriteria() {
        BaseCriteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected BaseCriteria createCriteriaInternal() {
        BaseCriteria criteria = new BaseCriteria();
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

        public BaseCriteria andIdIsNull() {
            addCriterion("id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIsNull() {
            addCriterion("api_code is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIsNotNull() {
            addCriterion("api_code is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeEqualTo(String value) {
            addCriterion("api_code =", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotEqualTo(String value) {
            addCriterion("api_code <>", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeGreaterThan(String value) {
            addCriterion("api_code >", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("api_code >=", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLessThan(String value) {
            addCriterion("api_code <", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLessThanOrEqualTo(String value) {
            addCriterion("api_code <=", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLike(String value) {
            addCriterion("api_code like", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotLike(String value) {
            addCriterion("api_code not like", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIn(List<String> values) {
            addCriterion("api_code in", values, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotIn(List<String> values) {
            addCriterion("api_code not in", values, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeBetween(String value1, String value2) {
            addCriterion("api_code between", value1, value2, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotBetween(String value1, String value2) {
            addCriterion("api_code not between", value1, value2, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchIsNull() {
            addCriterion("cus_batch is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchIsNotNull() {
            addCriterion("cus_batch is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchEqualTo(String value) {
            addCriterion("cus_batch =", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNotEqualTo(String value) {
            addCriterion("cus_batch <>", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchGreaterThan(String value) {
            addCriterion("cus_batch >", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchGreaterThanOrEqualTo(String value) {
            addCriterion("cus_batch >=", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchLessThan(String value) {
            addCriterion("cus_batch <", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchLessThanOrEqualTo(String value) {
            addCriterion("cus_batch <=", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchLike(String value) {
            addCriterion("cus_batch like", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNotLike(String value) {
            addCriterion("cus_batch not like", value, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchIn(List<String> values) {
            addCriterion("cus_batch in", values, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNotIn(List<String> values) {
            addCriterion("cus_batch not in", values, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchBetween(String value1, String value2) {
            addCriterion("cus_batch between", value1, value2, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNotBetween(String value1, String value2) {
            addCriterion("cus_batch not between", value1, value2, "cusBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchIsNull() {
            addCriterion("request_batch is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchIsNotNull() {
            addCriterion("request_batch is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchEqualTo(String value) {
            addCriterion("request_batch =", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchNotEqualTo(String value) {
            addCriterion("request_batch <>", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchGreaterThan(String value) {
            addCriterion("request_batch >", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchGreaterThanOrEqualTo(String value) {
            addCriterion("request_batch >=", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchLessThan(String value) {
            addCriterion("request_batch <", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchLessThanOrEqualTo(String value) {
            addCriterion("request_batch <=", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchLike(String value) {
            addCriterion("request_batch like", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchNotLike(String value) {
            addCriterion("request_batch not like", value, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchIn(List<String> values) {
            addCriterion("request_batch in", values, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchNotIn(List<String> values) {
            addCriterion("request_batch not in", values, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchBetween(String value1, String value2) {
            addCriterion("request_batch between", value1, value2, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestBatchNotBetween(String value1, String value2) {
            addCriterion("request_batch not between", value1, value2, "requestBatch");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIsNull() {
            addCriterion("status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusEqualTo(Integer value) {
            addCriterion("status =", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotEqualTo(Integer value) {
            addCriterion("status <>", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusGreaterThan(Integer value) {
            addCriterion("status >", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("status >=", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusLessThan(Integer value) {
            addCriterion("status <", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("status <=", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIn(List<Integer> values) {
            addCriterion("status in", values, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotIn(List<Integer> values) {
            addCriterion("status not in", values, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("status between", value1, value2, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("status not between", value1, value2, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdIsNull() {
            addCriterion("error_id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdIsNotNull() {
            addCriterion("error_id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdEqualTo(Long value) {
            addCriterion("error_id =", value, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdNotEqualTo(Long value) {
            addCriterion("error_id <>", value, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdGreaterThan(Long value) {
            addCriterion("error_id >", value, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdGreaterThanOrEqualTo(Long value) {
            addCriterion("error_id >=", value, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdLessThan(Long value) {
            addCriterion("error_id <", value, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdLessThanOrEqualTo(Long value) {
            addCriterion("error_id <=", value, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdIn(List<Long> values) {
            addCriterion("error_id in", values, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdNotIn(List<Long> values) {
            addCriterion("error_id not in", values, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdBetween(Long value1, Long value2) {
            addCriterion("error_id between", value1, value2, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorIdNotBetween(Long value1, Long value2) {
            addCriterion("error_id not between", value1, value2, "errorId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumIsNull() {
            addCriterion("actual_num is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumIsNotNull() {
            addCriterion("actual_num is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumEqualTo(Integer value) {
            addCriterion("actual_num =", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumNotEqualTo(Integer value) {
            addCriterion("actual_num <>", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumGreaterThan(Integer value) {
            addCriterion("actual_num >", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_num >=", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumLessThan(Integer value) {
            addCriterion("actual_num <", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumLessThanOrEqualTo(Integer value) {
            addCriterion("actual_num <=", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumIn(List<Integer> values) {
            addCriterion("actual_num in", values, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumNotIn(List<Integer> values) {
            addCriterion("actual_num not in", values, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumBetween(Integer value1, Integer value2) {
            addCriterion("actual_num between", value1, value2, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_num not between", value1, value2, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadIsNull() {
            addCriterion("is_upload is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadIsNotNull() {
            addCriterion("is_upload is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadEqualTo(Integer value) {
            addCriterion("is_upload =", value, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadNotEqualTo(Integer value) {
            addCriterion("is_upload <>", value, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadGreaterThan(Integer value) {
            addCriterion("is_upload >", value, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_upload >=", value, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadLessThan(Integer value) {
            addCriterion("is_upload <", value, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadLessThanOrEqualTo(Integer value) {
            addCriterion("is_upload <=", value, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadIn(List<Integer> values) {
            addCriterion("is_upload in", values, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadNotIn(List<Integer> values) {
            addCriterion("is_upload not in", values, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadBetween(Integer value1, Integer value2) {
            addCriterion("is_upload between", value1, value2, "isUpload");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsUploadNotBetween(Integer value1, Integer value2) {
            addCriterion("is_upload not between", value1, value2, "isUpload");
            return (BaseCriteria) this;
        }
    }

    public static class BaseCriteria extends GeneratedCriteria {

        protected BaseCriteria() {
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