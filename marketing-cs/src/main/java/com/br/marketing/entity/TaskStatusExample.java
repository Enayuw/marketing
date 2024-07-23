package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.List;

public class TaskStatusExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TaskStatusExample() {
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

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
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

        public Criteria andBatchNumberIsNull() {
            addCriterion("batch_number is null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberIsNotNull() {
            addCriterion("batch_number is not null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberEqualTo(String value) {
            addCriterion("batch_number =", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotEqualTo(String value) {
            addCriterion("batch_number <>", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberGreaterThan(String value) {
            addCriterion("batch_number >", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("batch_number >=", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLessThan(String value) {
            addCriterion("batch_number <", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("batch_number <=", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLike(String value) {
            addCriterion("batch_number like", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotLike(String value) {
            addCriterion("batch_number not like", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberIn(List<String> values) {
            addCriterion("batch_number in", values, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotIn(List<String> values) {
            addCriterion("batch_number not in", values, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberBetween(String value1, String value2) {
            addCriterion("batch_number between", value1, value2, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotBetween(String value1, String value2) {
            addCriterion("batch_number not between", value1, value2, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andAllStatusIsNull() {
            addCriterion("all_status is null");
            return (Criteria) this;
        }

        public Criteria andAllStatusIsNotNull() {
            addCriterion("all_status is not null");
            return (Criteria) this;
        }

        public Criteria andAllStatusEqualTo(Integer value) {
            addCriterion("all_status =", value, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusNotEqualTo(Integer value) {
            addCriterion("all_status <>", value, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusGreaterThan(Integer value) {
            addCriterion("all_status >", value, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("all_status >=", value, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusLessThan(Integer value) {
            addCriterion("all_status <", value, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusLessThanOrEqualTo(Integer value) {
            addCriterion("all_status <=", value, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusIn(List<Integer> values) {
            addCriterion("all_status in", values, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusNotIn(List<Integer> values) {
            addCriterion("all_status not in", values, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusBetween(Integer value1, Integer value2) {
            addCriterion("all_status between", value1, value2, "allStatus");
            return (Criteria) this;
        }

        public Criteria andAllStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("all_status not between", value1, value2, "allStatus");
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

        public Criteria andCreateTimeEqualTo(String value) {
            addCriterion("create_time =", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotEqualTo(String value) {
            addCriterion("create_time <>", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThan(String value) {
            addCriterion("create_time >", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeGreaterThanOrEqualTo(String value) {
            addCriterion("create_time >=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThan(String value) {
            addCriterion("create_time <", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLessThanOrEqualTo(String value) {
            addCriterion("create_time <=", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeLike(String value) {
            addCriterion("create_time like", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotLike(String value) {
            addCriterion("create_time not like", value, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeIn(List<String> values) {
            addCriterion("create_time in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotIn(List<String> values) {
            addCriterion("create_time not in", values, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeBetween(String value1, String value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (Criteria) this;
        }

        public Criteria andCreateTimeNotBetween(String value1, String value2) {
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

        public Criteria andUpdateTimeEqualTo(String value) {
            addCriterion("update_time =", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotEqualTo(String value) {
            addCriterion("update_time <>", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThan(String value) {
            addCriterion("update_time >", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeGreaterThanOrEqualTo(String value) {
            addCriterion("update_time >=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThan(String value) {
            addCriterion("update_time <", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLessThanOrEqualTo(String value) {
            addCriterion("update_time <=", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeLike(String value) {
            addCriterion("update_time like", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotLike(String value) {
            addCriterion("update_time not like", value, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeIn(List<String> values) {
            addCriterion("update_time in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotIn(List<String> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeBetween(String value1, String value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andUpdateTimeNotBetween(String value1, String value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (Criteria) this;
        }

        public Criteria andIncrDateIsNull() {
            addCriterion("incr_date is null");
            return (Criteria) this;
        }

        public Criteria andIncrDateIsNotNull() {
            addCriterion("incr_date is not null");
            return (Criteria) this;
        }

        public Criteria andIncrDateEqualTo(String value) {
            addCriterion("incr_date =", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateNotEqualTo(String value) {
            addCriterion("incr_date <>", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateGreaterThan(String value) {
            addCriterion("incr_date >", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateGreaterThanOrEqualTo(String value) {
            addCriterion("incr_date >=", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateLessThan(String value) {
            addCriterion("incr_date <", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateLessThanOrEqualTo(String value) {
            addCriterion("incr_date <=", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateLike(String value) {
            addCriterion("incr_date like", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateNotLike(String value) {
            addCriterion("incr_date not like", value, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateIn(List<String> values) {
            addCriterion("incr_date in", values, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateNotIn(List<String> values) {
            addCriterion("incr_date not in", values, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateBetween(String value1, String value2) {
            addCriterion("incr_date between", value1, value2, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrDateNotBetween(String value1, String value2) {
            addCriterion("incr_date not between", value1, value2, "incrDate");
            return (Criteria) this;
        }

        public Criteria andIncrStatusIsNull() {
            addCriterion("incr_status is null");
            return (Criteria) this;
        }

        public Criteria andIncrStatusIsNotNull() {
            addCriterion("incr_status is not null");
            return (Criteria) this;
        }

        public Criteria andIncrStatusEqualTo(Integer value) {
            addCriterion("incr_status =", value, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusNotEqualTo(Integer value) {
            addCriterion("incr_status <>", value, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusGreaterThan(Integer value) {
            addCriterion("incr_status >", value, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("incr_status >=", value, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusLessThan(Integer value) {
            addCriterion("incr_status <", value, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusLessThanOrEqualTo(Integer value) {
            addCriterion("incr_status <=", value, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusIn(List<Integer> values) {
            addCriterion("incr_status in", values, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusNotIn(List<Integer> values) {
            addCriterion("incr_status not in", values, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusBetween(Integer value1, Integer value2) {
            addCriterion("incr_status between", value1, value2, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andIncrStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("incr_status not between", value1, value2, "incrStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusIsNull() {
            addCriterion("once_status is null");
            return (Criteria) this;
        }

        public Criteria andOnceStatusIsNotNull() {
            addCriterion("once_status is not null");
            return (Criteria) this;
        }

        public Criteria andOnceStatusEqualTo(Integer value) {
            addCriterion("once_status =", value, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusNotEqualTo(Integer value) {
            addCriterion("once_status <>", value, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusGreaterThan(Integer value) {
            addCriterion("once_status >", value, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("once_status >=", value, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusLessThan(Integer value) {
            addCriterion("once_status <", value, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusLessThanOrEqualTo(Integer value) {
            addCriterion("once_status <=", value, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusIn(List<Integer> values) {
            addCriterion("once_status in", values, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusNotIn(List<Integer> values) {
            addCriterion("once_status not in", values, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusBetween(Integer value1, Integer value2) {
            addCriterion("once_status between", value1, value2, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andOnceStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("once_status not between", value1, value2, "onceStatus");
            return (Criteria) this;
        }

        public Criteria andPauseTypeIsNull() {
            addCriterion("pause_type is null");
            return (Criteria) this;
        }

        public Criteria andPauseTypeIsNotNull() {
            addCriterion("pause_type is not null");
            return (Criteria) this;
        }

        public Criteria andPauseTypeEqualTo(Integer value) {
            addCriterion("pause_type =", value, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeNotEqualTo(Integer value) {
            addCriterion("pause_type <>", value, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeGreaterThan(Integer value) {
            addCriterion("pause_type >", value, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("pause_type >=", value, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeLessThan(Integer value) {
            addCriterion("pause_type <", value, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeLessThanOrEqualTo(Integer value) {
            addCriterion("pause_type <=", value, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeIn(List<Integer> values) {
            addCriterion("pause_type in", values, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeNotIn(List<Integer> values) {
            addCriterion("pause_type not in", values, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeBetween(Integer value1, Integer value2) {
            addCriterion("pause_type between", value1, value2, "pauseType");
            return (Criteria) this;
        }

        public Criteria andPauseTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("pause_type not between", value1, value2, "pauseType");
            return (Criteria) this;
        }

        public Criteria andFileIdIsNull() {
            addCriterion("file_id is null");
            return (Criteria) this;
        }

        public Criteria andFileIdIsNotNull() {
            addCriterion("file_id is not null");
            return (Criteria) this;
        }

        public Criteria andFileIdEqualTo(Integer value) {
            addCriterion("file_id =", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotEqualTo(Integer value) {
            addCriterion("file_id <>", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThan(Integer value) {
            addCriterion("file_id >", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("file_id >=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThan(Integer value) {
            addCriterion("file_id <", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThanOrEqualTo(Integer value) {
            addCriterion("file_id <=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdIn(List<Integer> values) {
            addCriterion("file_id in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotIn(List<Integer> values) {
            addCriterion("file_id not in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdBetween(Integer value1, Integer value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotBetween(Integer value1, Integer value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
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