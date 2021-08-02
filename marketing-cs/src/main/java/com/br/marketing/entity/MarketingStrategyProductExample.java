package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingStrategyProductExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public MarketingStrategyProductExample() {
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

        public BaseCriteria andFileIdIsNull() {
            addCriterion("file_id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdIsNotNull() {
            addCriterion("file_id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdEqualTo(Long value) {
            addCriterion("file_id =", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdNotEqualTo(Long value) {
            addCriterion("file_id <>", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdGreaterThan(Long value) {
            addCriterion("file_id >", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdGreaterThanOrEqualTo(Long value) {
            addCriterion("file_id >=", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdLessThan(Long value) {
            addCriterion("file_id <", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdLessThanOrEqualTo(Long value) {
            addCriterion("file_id <=", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdIn(List<Long> values) {
            addCriterion("file_id in", values, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdNotIn(List<Long> values) {
            addCriterion("file_id not in", values, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdBetween(Long value1, Long value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdNotBetween(Long value1, Long value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
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

        public BaseCriteria andCusBatchNumberIsNull() {
            addCriterion("cus_batch_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberIsNotNull() {
            addCriterion("cus_batch_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberEqualTo(String value) {
            addCriterion("cus_batch_number =", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberNotEqualTo(String value) {
            addCriterion("cus_batch_number <>", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberGreaterThan(String value) {
            addCriterion("cus_batch_number >", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("cus_batch_number >=", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberLessThan(String value) {
            addCriterion("cus_batch_number <", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("cus_batch_number <=", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberLike(String value) {
            addCriterion("cus_batch_number like", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberNotLike(String value) {
            addCriterion("cus_batch_number not like", value, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberIn(List<String> values) {
            addCriterion("cus_batch_number in", values, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberNotIn(List<String> values) {
            addCriterion("cus_batch_number not in", values, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberBetween(String value1, String value2) {
            addCriterion("cus_batch_number between", value1, value2, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCusBatchNumberNotBetween(String value1, String value2) {
            addCriterion("cus_batch_number not between", value1, value2, "cusBatchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberIsNull() {
            addCriterion("batch_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberIsNotNull() {
            addCriterion("batch_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberEqualTo(String value) {
            addCriterion("batch_number =", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotEqualTo(String value) {
            addCriterion("batch_number <>", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberGreaterThan(String value) {
            addCriterion("batch_number >", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("batch_number >=", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberLessThan(String value) {
            addCriterion("batch_number <", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("batch_number <=", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberLike(String value) {
            addCriterion("batch_number like", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotLike(String value) {
            addCriterion("batch_number not like", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberIn(List<String> values) {
            addCriterion("batch_number in", values, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotIn(List<String> values) {
            addCriterion("batch_number not in", values, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberBetween(String value1, String value2) {
            addCriterion("batch_number between", value1, value2, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotBetween(String value1, String value2) {
            addCriterion("batch_number not between", value1, value2, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdIsNull() {
            addCriterion("strategy_id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdIsNotNull() {
            addCriterion("strategy_id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdEqualTo(String value) {
            addCriterion("strategy_id =", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdNotEqualTo(String value) {
            addCriterion("strategy_id <>", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdGreaterThan(String value) {
            addCriterion("strategy_id >", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_id >=", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdLessThan(String value) {
            addCriterion("strategy_id <", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdLessThanOrEqualTo(String value) {
            addCriterion("strategy_id <=", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdLike(String value) {
            addCriterion("strategy_id like", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdNotLike(String value) {
            addCriterion("strategy_id not like", value, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdIn(List<String> values) {
            addCriterion("strategy_id in", values, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdNotIn(List<String> values) {
            addCriterion("strategy_id not in", values, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdBetween(String value1, String value2) {
            addCriterion("strategy_id between", value1, value2, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyIdNotBetween(String value1, String value2) {
            addCriterion("strategy_id not between", value1, value2, "strategyId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameIsNull() {
            addCriterion("product_name is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameIsNotNull() {
            addCriterion("product_name is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameEqualTo(String value) {
            addCriterion("product_name =", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameNotEqualTo(String value) {
            addCriterion("product_name <>", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameGreaterThan(String value) {
            addCriterion("product_name >", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameGreaterThanOrEqualTo(String value) {
            addCriterion("product_name >=", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameLessThan(String value) {
            addCriterion("product_name <", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameLessThanOrEqualTo(String value) {
            addCriterion("product_name <=", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameLike(String value) {
            addCriterion("product_name like", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameNotLike(String value) {
            addCriterion("product_name not like", value, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameIn(List<String> values) {
            addCriterion("product_name in", values, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameNotIn(List<String> values) {
            addCriterion("product_name not in", values, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameBetween(String value1, String value2) {
            addCriterion("product_name between", value1, value2, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductNameNotBetween(String value1, String value2) {
            addCriterion("product_name not between", value1, value2, "productName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionIsNull() {
            addCriterion("product_version is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionIsNotNull() {
            addCriterion("product_version is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionEqualTo(String value) {
            addCriterion("product_version =", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionNotEqualTo(String value) {
            addCriterion("product_version <>", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionGreaterThan(String value) {
            addCriterion("product_version >", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionGreaterThanOrEqualTo(String value) {
            addCriterion("product_version >=", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionLessThan(String value) {
            addCriterion("product_version <", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionLessThanOrEqualTo(String value) {
            addCriterion("product_version <=", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionLike(String value) {
            addCriterion("product_version like", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionNotLike(String value) {
            addCriterion("product_version not like", value, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionIn(List<String> values) {
            addCriterion("product_version in", values, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionNotIn(List<String> values) {
            addCriterion("product_version not in", values, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionBetween(String value1, String value2) {
            addCriterion("product_version between", value1, value2, "productVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andProductVersionNotBetween(String value1, String value2) {
            addCriterion("product_version not between", value1, value2, "productVersion");
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