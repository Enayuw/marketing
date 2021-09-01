package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerInfoPushBatchExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CustomerInfoPushBatchExample() {
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

        public Criteria andMIdIsNull() {
            addCriterion("m_id is null");
            return (Criteria) this;
        }

        public Criteria andMIdIsNotNull() {
            addCriterion("m_id is not null");
            return (Criteria) this;
        }

        public Criteria andMIdEqualTo(Long value) {
            addCriterion("m_id =", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdNotEqualTo(Long value) {
            addCriterion("m_id <>", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdGreaterThan(Long value) {
            addCriterion("m_id >", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdGreaterThanOrEqualTo(Long value) {
            addCriterion("m_id >=", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdLessThan(Long value) {
            addCriterion("m_id <", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdLessThanOrEqualTo(Long value) {
            addCriterion("m_id <=", value, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdIn(List<Long> values) {
            addCriterion("m_id in", values, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdNotIn(List<Long> values) {
            addCriterion("m_id not in", values, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdBetween(Long value1, Long value2) {
            addCriterion("m_id between", value1, value2, "mId");
            return (Criteria) this;
        }

        public Criteria andMIdNotBetween(Long value1, Long value2) {
            addCriterion("m_id not between", value1, value2, "mId");
            return (Criteria) this;
        }

        public Criteria andMApiCodeIsNull() {
            addCriterion("m_api_code is null");
            return (Criteria) this;
        }

        public Criteria andMApiCodeIsNotNull() {
            addCriterion("m_api_code is not null");
            return (Criteria) this;
        }

        public Criteria andMApiCodeEqualTo(String value) {
            addCriterion("m_api_code =", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotEqualTo(String value) {
            addCriterion("m_api_code <>", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeGreaterThan(String value) {
            addCriterion("m_api_code >", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("m_api_code >=", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeLessThan(String value) {
            addCriterion("m_api_code <", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeLessThanOrEqualTo(String value) {
            addCriterion("m_api_code <=", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeLike(String value) {
            addCriterion("m_api_code like", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotLike(String value) {
            addCriterion("m_api_code not like", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeIn(List<String> values) {
            addCriterion("m_api_code in", values, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotIn(List<String> values) {
            addCriterion("m_api_code not in", values, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeBetween(String value1, String value2) {
            addCriterion("m_api_code between", value1, value2, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotBetween(String value1, String value2) {
            addCriterion("m_api_code not between", value1, value2, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberIsNull() {
            addCriterion("m_batch_number is null");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberIsNotNull() {
            addCriterion("m_batch_number is not null");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberEqualTo(String value) {
            addCriterion("m_batch_number =", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberNotEqualTo(String value) {
            addCriterion("m_batch_number <>", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberGreaterThan(String value) {
            addCriterion("m_batch_number >", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("m_batch_number >=", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberLessThan(String value) {
            addCriterion("m_batch_number <", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("m_batch_number <=", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberLike(String value) {
            addCriterion("m_batch_number like", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberNotLike(String value) {
            addCriterion("m_batch_number not like", value, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberIn(List<String> values) {
            addCriterion("m_batch_number in", values, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberNotIn(List<String> values) {
            addCriterion("m_batch_number not in", values, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberBetween(String value1, String value2) {
            addCriterion("m_batch_number between", value1, value2, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMBatchNumberNotBetween(String value1, String value2) {
            addCriterion("m_batch_number not between", value1, value2, "mBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberIsNull() {
            addCriterion("m_cus_batch_number is null");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberIsNotNull() {
            addCriterion("m_cus_batch_number is not null");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberEqualTo(String value) {
            addCriterion("m_cus_batch_number =", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberNotEqualTo(String value) {
            addCriterion("m_cus_batch_number <>", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberGreaterThan(String value) {
            addCriterion("m_cus_batch_number >", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number >=", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberLessThan(String value) {
            addCriterion("m_cus_batch_number <", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number <=", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberLike(String value) {
            addCriterion("m_cus_batch_number like", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberNotLike(String value) {
            addCriterion("m_cus_batch_number not like", value, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberIn(List<String> values) {
            addCriterion("m_cus_batch_number in", values, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberNotIn(List<String> values) {
            addCriterion("m_cus_batch_number not in", values, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number between", value1, value2, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberNotBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number not between", value1, value2, "mCusBatchNumber");
            return (Criteria) this;
        }

        public Criteria andMFileIdIsNull() {
            addCriterion("m_file_id is null");
            return (Criteria) this;
        }

        public Criteria andMFileIdIsNotNull() {
            addCriterion("m_file_id is not null");
            return (Criteria) this;
        }

        public Criteria andMFileIdEqualTo(Long value) {
            addCriterion("m_file_id =", value, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdNotEqualTo(Long value) {
            addCriterion("m_file_id <>", value, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdGreaterThan(Long value) {
            addCriterion("m_file_id >", value, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdGreaterThanOrEqualTo(Long value) {
            addCriterion("m_file_id >=", value, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdLessThan(Long value) {
            addCriterion("m_file_id <", value, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdLessThanOrEqualTo(Long value) {
            addCriterion("m_file_id <=", value, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdIn(List<Long> values) {
            addCriterion("m_file_id in", values, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdNotIn(List<Long> values) {
            addCriterion("m_file_id not in", values, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdBetween(Long value1, Long value2) {
            addCriterion("m_file_id between", value1, value2, "mFileId");
            return (Criteria) this;
        }

        public Criteria andMFileIdNotBetween(Long value1, Long value2) {
            addCriterion("m_file_id not between", value1, value2, "mFileId");
            return (Criteria) this;
        }

        public Criteria andIsDelIsNull() {
            addCriterion("is_del is null");
            return (Criteria) this;
        }

        public Criteria andIsDelIsNotNull() {
            addCriterion("is_del is not null");
            return (Criteria) this;
        }

        public Criteria andIsDelEqualTo(Integer value) {
            addCriterion("is_del =", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotEqualTo(Integer value) {
            addCriterion("is_del <>", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelGreaterThan(Integer value) {
            addCriterion("is_del >", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_del >=", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelLessThan(Integer value) {
            addCriterion("is_del <", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelLessThanOrEqualTo(Integer value) {
            addCriterion("is_del <=", value, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelIn(List<Integer> values) {
            addCriterion("is_del in", values, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotIn(List<Integer> values) {
            addCriterion("is_del not in", values, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelBetween(Integer value1, Integer value2) {
            addCriterion("is_del between", value1, value2, "isDel");
            return (Criteria) this;
        }

        public Criteria andIsDelNotBetween(Integer value1, Integer value2) {
            addCriterion("is_del not between", value1, value2, "isDel");
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

    public static class Criteria extends AbstractGeneratedCriteria {

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