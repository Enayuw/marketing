package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerInfoPushBatchExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public CustomerInfoPushBatchExample() {
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

        public BaseCriteria andMIdIsNull() {
            addCriterion("m_id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdIsNotNull() {
            addCriterion("m_id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdEqualTo(Long value) {
            addCriterion("m_id =", value, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdNotEqualTo(Long value) {
            addCriterion("m_id <>", value, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdGreaterThan(Long value) {
            addCriterion("m_id >", value, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdGreaterThanOrEqualTo(Long value) {
            addCriterion("m_id >=", value, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdLessThan(Long value) {
            addCriterion("m_id <", value, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdLessThanOrEqualTo(Long value) {
            addCriterion("m_id <=", value, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdIn(List<Long> values) {
            addCriterion("m_id in", values, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdNotIn(List<Long> values) {
            addCriterion("m_id not in", values, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdBetween(Long value1, Long value2) {
            addCriterion("m_id between", value1, value2, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMIdNotBetween(Long value1, Long value2) {
            addCriterion("m_id not between", value1, value2, "mId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeIsNull() {
            addCriterion("m_api_code is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeIsNotNull() {
            addCriterion("m_api_code is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeEqualTo(String value) {
            addCriterion("m_api_code =", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotEqualTo(String value) {
            addCriterion("m_api_code <>", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeGreaterThan(String value) {
            addCriterion("m_api_code >", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("m_api_code >=", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeLessThan(String value) {
            addCriterion("m_api_code <", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeLessThanOrEqualTo(String value) {
            addCriterion("m_api_code <=", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeLike(String value) {
            addCriterion("m_api_code like", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotLike(String value) {
            addCriterion("m_api_code not like", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeIn(List<String> values) {
            addCriterion("m_api_code in", values, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotIn(List<String> values) {
            addCriterion("m_api_code not in", values, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeBetween(String value1, String value2) {
            addCriterion("m_api_code between", value1, value2, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotBetween(String value1, String value2) {
            addCriterion("m_api_code not between", value1, value2, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberIsNull() {
            addCriterion("m_batch_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberIsNotNull() {
            addCriterion("m_batch_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberEqualTo(String value) {
            addCriterion("m_batch_number =", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberNotEqualTo(String value) {
            addCriterion("m_batch_number <>", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberGreaterThan(String value) {
            addCriterion("m_batch_number >", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("m_batch_number >=", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberLessThan(String value) {
            addCriterion("m_batch_number <", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("m_batch_number <=", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberLike(String value) {
            addCriterion("m_batch_number like", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberNotLike(String value) {
            addCriterion("m_batch_number not like", value, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberIn(List<String> values) {
            addCriterion("m_batch_number in", values, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberNotIn(List<String> values) {
            addCriterion("m_batch_number not in", values, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberBetween(String value1, String value2) {
            addCriterion("m_batch_number between", value1, value2, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMBatchNumberNotBetween(String value1, String value2) {
            addCriterion("m_batch_number not between", value1, value2, "mBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberIsNull() {
            addCriterion("m_cus_batch_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberIsNotNull() {
            addCriterion("m_cus_batch_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberEqualTo(String value) {
            addCriterion("m_cus_batch_number =", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberNotEqualTo(String value) {
            addCriterion("m_cus_batch_number <>", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberGreaterThan(String value) {
            addCriterion("m_cus_batch_number >", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number >=", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberLessThan(String value) {
            addCriterion("m_cus_batch_number <", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number <=", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberLike(String value) {
            addCriterion("m_cus_batch_number like", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberNotLike(String value) {
            addCriterion("m_cus_batch_number not like", value, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberIn(List<String> values) {
            addCriterion("m_cus_batch_number in", values, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberNotIn(List<String> values) {
            addCriterion("m_cus_batch_number not in", values, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number between", value1, value2, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberNotBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number not between", value1, value2, "mCusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdIsNull() {
            addCriterion("m_file_id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdIsNotNull() {
            addCriterion("m_file_id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdEqualTo(Long value) {
            addCriterion("m_file_id =", value, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdNotEqualTo(Long value) {
            addCriterion("m_file_id <>", value, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdGreaterThan(Long value) {
            addCriterion("m_file_id >", value, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdGreaterThanOrEqualTo(Long value) {
            addCriterion("m_file_id >=", value, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdLessThan(Long value) {
            addCriterion("m_file_id <", value, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdLessThanOrEqualTo(Long value) {
            addCriterion("m_file_id <=", value, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdIn(List<Long> values) {
            addCriterion("m_file_id in", values, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdNotIn(List<Long> values) {
            addCriterion("m_file_id not in", values, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdBetween(Long value1, Long value2) {
            addCriterion("m_file_id between", value1, value2, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMFileIdNotBetween(Long value1, Long value2) {
            addCriterion("m_file_id not between", value1, value2, "mFileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelIsNull() {
            addCriterion("is_del is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelIsNotNull() {
            addCriterion("is_del is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelEqualTo(Integer value) {
            addCriterion("is_del =", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelNotEqualTo(Integer value) {
            addCriterion("is_del <>", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelGreaterThan(Integer value) {
            addCriterion("is_del >", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_del >=", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelLessThan(Integer value) {
            addCriterion("is_del <", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelLessThanOrEqualTo(Integer value) {
            addCriterion("is_del <=", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelIn(List<Integer> values) {
            addCriterion("is_del in", values, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelNotIn(List<Integer> values) {
            addCriterion("is_del not in", values, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelBetween(Integer value1, Integer value2) {
            addCriterion("is_del between", value1, value2, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelNotBetween(Integer value1, Integer value2) {
            addCriterion("is_del not between", value1, value2, "isDel");
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