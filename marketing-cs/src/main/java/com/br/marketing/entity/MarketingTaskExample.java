package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public MarketingTaskExample() {
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

        public BaseCriteria andIdIsNull() {
            addCriterion("id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotBetween(Integer value1, Integer value2) {
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

        public BaseCriteria andFileNameIsNull() {
            addCriterion("file_name is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameIsNotNull() {
            addCriterion("file_name is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameEqualTo(String value) {
            addCriterion("file_name =", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameNotEqualTo(String value) {
            addCriterion("file_name <>", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameGreaterThan(String value) {
            addCriterion("file_name >", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameGreaterThanOrEqualTo(String value) {
            addCriterion("file_name >=", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameLessThan(String value) {
            addCriterion("file_name <", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameLessThanOrEqualTo(String value) {
            addCriterion("file_name <=", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameLike(String value) {
            addCriterion("file_name like", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameNotLike(String value) {
            addCriterion("file_name not like", value, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameIn(List<String> values) {
            addCriterion("file_name in", values, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameNotIn(List<String> values) {
            addCriterion("file_name not in", values, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameBetween(String value1, String value2) {
            addCriterion("file_name between", value1, value2, "fileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNameNotBetween(String value1, String value2) {
            addCriterion("file_name not between", value1, value2, "fileName");
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

        public BaseCriteria andFileIdEqualTo(Integer value) {
            addCriterion("file_id =", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdNotEqualTo(Integer value) {
            addCriterion("file_id <>", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdGreaterThan(Integer value) {
            addCriterion("file_id >", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("file_id >=", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdLessThan(Integer value) {
            addCriterion("file_id <", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdLessThanOrEqualTo(Integer value) {
            addCriterion("file_id <=", value, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdIn(List<Integer> values) {
            addCriterion("file_id in", values, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdNotIn(List<Integer> values) {
            addCriterion("file_id not in", values, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdBetween(Integer value1, Integer value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileIdNotBetween(Integer value1, Integer value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeIsNull() {
            addCriterion("request_code is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeIsNotNull() {
            addCriterion("request_code is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeEqualTo(String value) {
            addCriterion("request_code =", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeNotEqualTo(String value) {
            addCriterion("request_code <>", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeGreaterThan(String value) {
            addCriterion("request_code >", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeGreaterThanOrEqualTo(String value) {
            addCriterion("request_code >=", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeLessThan(String value) {
            addCriterion("request_code <", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeLessThanOrEqualTo(String value) {
            addCriterion("request_code <=", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeLike(String value) {
            addCriterion("request_code like", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeNotLike(String value) {
            addCriterion("request_code not like", value, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeIn(List<String> values) {
            addCriterion("request_code in", values, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeNotIn(List<String> values) {
            addCriterion("request_code not in", values, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeBetween(String value1, String value2) {
            addCriterion("request_code between", value1, value2, "requestCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andRequestCodeNotBetween(String value1, String value2) {
            addCriterion("request_code not between", value1, value2, "requestCode");
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

        public BaseCriteria andFrequencyIsNull() {
            addCriterion("frequency is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyIsNotNull() {
            addCriterion("frequency is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyEqualTo(String value) {
            addCriterion("frequency =", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyNotEqualTo(String value) {
            addCriterion("frequency <>", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyGreaterThan(String value) {
            addCriterion("frequency >", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyGreaterThanOrEqualTo(String value) {
            addCriterion("frequency >=", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyLessThan(String value) {
            addCriterion("frequency <", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyLessThanOrEqualTo(String value) {
            addCriterion("frequency <=", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyLike(String value) {
            addCriterion("frequency like", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyNotLike(String value) {
            addCriterion("frequency not like", value, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyIn(List<String> values) {
            addCriterion("frequency in", values, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyNotIn(List<String> values) {
            addCriterion("frequency not in", values, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyBetween(String value1, String value2) {
            addCriterion("frequency between", value1, value2, "frequency");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFrequencyNotBetween(String value1, String value2) {
            addCriterion("frequency not between", value1, value2, "frequency");
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

        public BaseCriteria andMonitorStatusIsNull() {
            addCriterion("monitor_status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusIsNotNull() {
            addCriterion("monitor_status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusEqualTo(Integer value) {
            addCriterion("monitor_status =", value, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusNotEqualTo(Integer value) {
            addCriterion("monitor_status <>", value, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusGreaterThan(Integer value) {
            addCriterion("monitor_status >", value, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("monitor_status >=", value, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusLessThan(Integer value) {
            addCriterion("monitor_status <", value, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusLessThanOrEqualTo(Integer value) {
            addCriterion("monitor_status <=", value, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusIn(List<Integer> values) {
            addCriterion("monitor_status in", values, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusNotIn(List<Integer> values) {
            addCriterion("monitor_status not in", values, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusBetween(Integer value1, Integer value2) {
            addCriterion("monitor_status between", value1, value2, "monitorStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("monitor_status not between", value1, value2, "monitorStatus");
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

        public BaseCriteria andTaskNumberIsNull() {
            addCriterion("task_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberIsNotNull() {
            addCriterion("task_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberEqualTo(Integer value) {
            addCriterion("task_number =", value, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberNotEqualTo(Integer value) {
            addCriterion("task_number <>", value, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberGreaterThan(Integer value) {
            addCriterion("task_number >", value, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("task_number >=", value, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberLessThan(Integer value) {
            addCriterion("task_number <", value, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberLessThanOrEqualTo(Integer value) {
            addCriterion("task_number <=", value, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberIn(List<Integer> values) {
            addCriterion("task_number in", values, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberNotIn(List<Integer> values) {
            addCriterion("task_number not in", values, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberBetween(Integer value1, Integer value2) {
            addCriterion("task_number between", value1, value2, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTaskNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("task_number not between", value1, value2, "taskNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberIsNull() {
            addCriterion("actual_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberIsNotNull() {
            addCriterion("actual_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberEqualTo(Integer value) {
            addCriterion("actual_number =", value, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberNotEqualTo(Integer value) {
            addCriterion("actual_number <>", value, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberGreaterThan(Integer value) {
            addCriterion("actual_number >", value, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_number >=", value, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberLessThan(Integer value) {
            addCriterion("actual_number <", value, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberLessThanOrEqualTo(Integer value) {
            addCriterion("actual_number <=", value, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberIn(List<Integer> values) {
            addCriterion("actual_number in", values, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberNotIn(List<Integer> values) {
            addCriterion("actual_number not in", values, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberBetween(Integer value1, Integer value2) {
            addCriterion("actual_number between", value1, value2, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_number not between", value1, value2, "actualNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameIsNull() {
            addCriterion("strategy_name is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameIsNotNull() {
            addCriterion("strategy_name is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameEqualTo(String value) {
            addCriterion("strategy_name =", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameNotEqualTo(String value) {
            addCriterion("strategy_name <>", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameGreaterThan(String value) {
            addCriterion("strategy_name >", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_name >=", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameLessThan(String value) {
            addCriterion("strategy_name <", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameLessThanOrEqualTo(String value) {
            addCriterion("strategy_name <=", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameLike(String value) {
            addCriterion("strategy_name like", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameNotLike(String value) {
            addCriterion("strategy_name not like", value, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameIn(List<String> values) {
            addCriterion("strategy_name in", values, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameNotIn(List<String> values) {
            addCriterion("strategy_name not in", values, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameBetween(String value1, String value2) {
            addCriterion("strategy_name between", value1, value2, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStrategyNameNotBetween(String value1, String value2) {
            addCriterion("strategy_name not between", value1, value2, "strategyName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateIsNull() {
            addCriterion("close_date is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateIsNotNull() {
            addCriterion("close_date is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateEqualTo(String value) {
            addCriterion("close_date =", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateNotEqualTo(String value) {
            addCriterion("close_date <>", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateGreaterThan(String value) {
            addCriterion("close_date >", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateGreaterThanOrEqualTo(String value) {
            addCriterion("close_date >=", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateLessThan(String value) {
            addCriterion("close_date <", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateLessThanOrEqualTo(String value) {
            addCriterion("close_date <=", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateLike(String value) {
            addCriterion("close_date like", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateNotLike(String value) {
            addCriterion("close_date not like", value, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateIn(List<String> values) {
            addCriterion("close_date in", values, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateNotIn(List<String> values) {
            addCriterion("close_date not in", values, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateBetween(String value1, String value2) {
            addCriterion("close_date between", value1, value2, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCloseDateNotBetween(String value1, String value2) {
            addCriterion("close_date not between", value1, value2, "closeDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateIsNull() {
            addCriterion("start_date is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateIsNotNull() {
            addCriterion("start_date is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateEqualTo(String value) {
            addCriterion("start_date =", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateNotEqualTo(String value) {
            addCriterion("start_date <>", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateGreaterThan(String value) {
            addCriterion("start_date >", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateGreaterThanOrEqualTo(String value) {
            addCriterion("start_date >=", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateLessThan(String value) {
            addCriterion("start_date <", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateLessThanOrEqualTo(String value) {
            addCriterion("start_date <=", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateLike(String value) {
            addCriterion("start_date like", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateNotLike(String value) {
            addCriterion("start_date not like", value, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateIn(List<String> values) {
            addCriterion("start_date in", values, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateNotIn(List<String> values) {
            addCriterion("start_date not in", values, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateBetween(String value1, String value2) {
            addCriterion("start_date between", value1, value2, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStartDateNotBetween(String value1, String value2) {
            addCriterion("start_date not between", value1, value2, "startDate");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageIsNull() {
            addCriterion("error_message is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageIsNotNull() {
            addCriterion("error_message is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageEqualTo(String value) {
            addCriterion("error_message =", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageNotEqualTo(String value) {
            addCriterion("error_message <>", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageGreaterThan(String value) {
            addCriterion("error_message >", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageGreaterThanOrEqualTo(String value) {
            addCriterion("error_message >=", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageLessThan(String value) {
            addCriterion("error_message <", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageLessThanOrEqualTo(String value) {
            addCriterion("error_message <=", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageLike(String value) {
            addCriterion("error_message like", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageNotLike(String value) {
            addCriterion("error_message not like", value, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageIn(List<String> values) {
            addCriterion("error_message in", values, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageNotIn(List<String> values) {
            addCriterion("error_message not in", values, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageBetween(String value1, String value2) {
            addCriterion("error_message between", value1, value2, "errorMessage");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorMessageNotBetween(String value1, String value2) {
            addCriterion("error_message not between", value1, value2, "errorMessage");
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

        public BaseCriteria andMonitorTypeIsNull() {
            addCriterion("monitor_type is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeIsNotNull() {
            addCriterion("monitor_type is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeEqualTo(Integer value) {
            addCriterion("monitor_type =", value, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeNotEqualTo(Integer value) {
            addCriterion("monitor_type <>", value, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeGreaterThan(Integer value) {
            addCriterion("monitor_type >", value, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("monitor_type >=", value, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeLessThan(Integer value) {
            addCriterion("monitor_type <", value, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeLessThanOrEqualTo(Integer value) {
            addCriterion("monitor_type <=", value, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeIn(List<Integer> values) {
            addCriterion("monitor_type in", values, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeNotIn(List<Integer> values) {
            addCriterion("monitor_type not in", values, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeBetween(Integer value1, Integer value2) {
            addCriterion("monitor_type between", value1, value2, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("monitor_type not between", value1, value2, "monitorType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementIsNull() {
            addCriterion("increment is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementIsNotNull() {
            addCriterion("increment is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementEqualTo(Integer value) {
            addCriterion("increment =", value, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementNotEqualTo(Integer value) {
            addCriterion("increment <>", value, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementGreaterThan(Integer value) {
            addCriterion("increment >", value, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementGreaterThanOrEqualTo(Integer value) {
            addCriterion("increment >=", value, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementLessThan(Integer value) {
            addCriterion("increment <", value, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementLessThanOrEqualTo(Integer value) {
            addCriterion("increment <=", value, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementIn(List<Integer> values) {
            addCriterion("increment in", values, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementNotIn(List<Integer> values) {
            addCriterion("increment not in", values, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementBetween(Integer value1, Integer value2) {
            addCriterion("increment between", value1, value2, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIncrementNotBetween(Integer value1, Integer value2) {
            addCriterion("increment not between", value1, value2, "increment");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelIsNull() {
            addCriterion("monitor_model is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelIsNotNull() {
            addCriterion("monitor_model is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelEqualTo(Integer value) {
            addCriterion("monitor_model =", value, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelNotEqualTo(Integer value) {
            addCriterion("monitor_model <>", value, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelGreaterThan(Integer value) {
            addCriterion("monitor_model >", value, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelGreaterThanOrEqualTo(Integer value) {
            addCriterion("monitor_model >=", value, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelLessThan(Integer value) {
            addCriterion("monitor_model <", value, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelLessThanOrEqualTo(Integer value) {
            addCriterion("monitor_model <=", value, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelIn(List<Integer> values) {
            addCriterion("monitor_model in", values, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelNotIn(List<Integer> values) {
            addCriterion("monitor_model not in", values, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelBetween(Integer value1, Integer value2) {
            addCriterion("monitor_model between", value1, value2, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMonitorModelNotBetween(Integer value1, Integer value2) {
            addCriterion("monitor_model not between", value1, value2, "monitorModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckIsNull() {
            addCriterion("is_check is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckIsNotNull() {
            addCriterion("is_check is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckEqualTo(Integer value) {
            addCriterion("is_check =", value, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckNotEqualTo(Integer value) {
            addCriterion("is_check <>", value, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckGreaterThan(Integer value) {
            addCriterion("is_check >", value, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_check >=", value, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckLessThan(Integer value) {
            addCriterion("is_check <", value, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckLessThanOrEqualTo(Integer value) {
            addCriterion("is_check <=", value, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckIn(List<Integer> values) {
            addCriterion("is_check in", values, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckNotIn(List<Integer> values) {
            addCriterion("is_check not in", values, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckBetween(Integer value1, Integer value2) {
            addCriterion("is_check between", value1, value2, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsCheckNotBetween(Integer value1, Integer value2) {
            addCriterion("is_check not between", value1, value2, "isCheck");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairIsNull() {
            addCriterion("is_repair is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairIsNotNull() {
            addCriterion("is_repair is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairEqualTo(Integer value) {
            addCriterion("is_repair =", value, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairNotEqualTo(Integer value) {
            addCriterion("is_repair <>", value, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairGreaterThan(Integer value) {
            addCriterion("is_repair >", value, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_repair >=", value, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairLessThan(Integer value) {
            addCriterion("is_repair <", value, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairLessThanOrEqualTo(Integer value) {
            addCriterion("is_repair <=", value, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairIn(List<Integer> values) {
            addCriterion("is_repair in", values, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairNotIn(List<Integer> values) {
            addCriterion("is_repair not in", values, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairBetween(Integer value1, Integer value2) {
            addCriterion("is_repair between", value1, value2, "isRepair");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsRepairNotBetween(Integer value1, Integer value2) {
            addCriterion("is_repair not between", value1, value2, "isRepair");
            return (BaseCriteria) this;
        }
    }

    public static class BaseCriteria extends AbstractGeneratedCriteria {

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