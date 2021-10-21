package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskStatusDistributeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TaskStatusDistributeExample() {
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

        public Criteria andFileIdIsNull() {
            addCriterion("file_id is null");
            return (Criteria) this;
        }

        public Criteria andFileIdIsNotNull() {
            addCriterion("file_id is not null");
            return (Criteria) this;
        }

        public Criteria andFileIdEqualTo(Long value) {
            addCriterion("file_id =", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotEqualTo(Long value) {
            addCriterion("file_id <>", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThan(Long value) {
            addCriterion("file_id >", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThanOrEqualTo(Long value) {
            addCriterion("file_id >=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThan(Long value) {
            addCriterion("file_id <", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThanOrEqualTo(Long value) {
            addCriterion("file_id <=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdIn(List<Long> values) {
            addCriterion("file_id in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotIn(List<Long> values) {
            addCriterion("file_id not in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdBetween(Long value1, Long value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotBetween(Long value1, Long value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
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

        public Criteria andStartIdIsNull() {
            addCriterion("start_id is null");
            return (Criteria) this;
        }

        public Criteria andStartIdIsNotNull() {
            addCriterion("start_id is not null");
            return (Criteria) this;
        }

        public Criteria andStartIdEqualTo(Long value) {
            addCriterion("start_id =", value, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdNotEqualTo(Long value) {
            addCriterion("start_id <>", value, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdGreaterThan(Long value) {
            addCriterion("start_id >", value, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdGreaterThanOrEqualTo(Long value) {
            addCriterion("start_id >=", value, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdLessThan(Long value) {
            addCriterion("start_id <", value, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdLessThanOrEqualTo(Long value) {
            addCriterion("start_id <=", value, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdIn(List<Long> values) {
            addCriterion("start_id in", values, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdNotIn(List<Long> values) {
            addCriterion("start_id not in", values, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdBetween(Long value1, Long value2) {
            addCriterion("start_id between", value1, value2, "startId");
            return (Criteria) this;
        }

        public Criteria andStartIdNotBetween(Long value1, Long value2) {
            addCriterion("start_id not between", value1, value2, "startId");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexIsNull() {
            addCriterion("distribute_index is null");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexIsNotNull() {
            addCriterion("distribute_index is not null");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexEqualTo(Integer value) {
            addCriterion("distribute_index =", value, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexNotEqualTo(Integer value) {
            addCriterion("distribute_index <>", value, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexGreaterThan(Integer value) {
            addCriterion("distribute_index >", value, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexGreaterThanOrEqualTo(Integer value) {
            addCriterion("distribute_index >=", value, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexLessThan(Integer value) {
            addCriterion("distribute_index <", value, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexLessThanOrEqualTo(Integer value) {
            addCriterion("distribute_index <=", value, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexIn(List<Integer> values) {
            addCriterion("distribute_index in", values, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexNotIn(List<Integer> values) {
            addCriterion("distribute_index not in", values, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexBetween(Integer value1, Integer value2) {
            addCriterion("distribute_index between", value1, value2, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andDistributeIndexNotBetween(Integer value1, Integer value2) {
            addCriterion("distribute_index not between", value1, value2, "distributeIndex");
            return (Criteria) this;
        }

        public Criteria andEndIdIsNull() {
            addCriterion("end_id is null");
            return (Criteria) this;
        }

        public Criteria andEndIdIsNotNull() {
            addCriterion("end_id is not null");
            return (Criteria) this;
        }

        public Criteria andEndIdEqualTo(Long value) {
            addCriterion("end_id =", value, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdNotEqualTo(Long value) {
            addCriterion("end_id <>", value, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdGreaterThan(Long value) {
            addCriterion("end_id >", value, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdGreaterThanOrEqualTo(Long value) {
            addCriterion("end_id >=", value, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdLessThan(Long value) {
            addCriterion("end_id <", value, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdLessThanOrEqualTo(Long value) {
            addCriterion("end_id <=", value, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdIn(List<Long> values) {
            addCriterion("end_id in", values, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdNotIn(List<Long> values) {
            addCriterion("end_id not in", values, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdBetween(Long value1, Long value2) {
            addCriterion("end_id between", value1, value2, "endId");
            return (Criteria) this;
        }

        public Criteria andEndIdNotBetween(Long value1, Long value2) {
            addCriterion("end_id not between", value1, value2, "endId");
            return (Criteria) this;
        }

        public Criteria andPreNumIsNull() {
            addCriterion("pre_num is null");
            return (Criteria) this;
        }

        public Criteria andPreNumIsNotNull() {
            addCriterion("pre_num is not null");
            return (Criteria) this;
        }

        public Criteria andPreNumEqualTo(Long value) {
            addCriterion("pre_num =", value, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumNotEqualTo(Long value) {
            addCriterion("pre_num <>", value, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumGreaterThan(Long value) {
            addCriterion("pre_num >", value, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumGreaterThanOrEqualTo(Long value) {
            addCriterion("pre_num >=", value, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumLessThan(Long value) {
            addCriterion("pre_num <", value, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumLessThanOrEqualTo(Long value) {
            addCriterion("pre_num <=", value, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumIn(List<Long> values) {
            addCriterion("pre_num in", values, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumNotIn(List<Long> values) {
            addCriterion("pre_num not in", values, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumBetween(Long value1, Long value2) {
            addCriterion("pre_num between", value1, value2, "preNum");
            return (Criteria) this;
        }

        public Criteria andPreNumNotBetween(Long value1, Long value2) {
            addCriterion("pre_num not between", value1, value2, "preNum");
            return (Criteria) this;
        }

        public Criteria andActualNumIsNull() {
            addCriterion("actual_num is null");
            return (Criteria) this;
        }

        public Criteria andActualNumIsNotNull() {
            addCriterion("actual_num is not null");
            return (Criteria) this;
        }

        public Criteria andActualNumEqualTo(Long value) {
            addCriterion("actual_num =", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumNotEqualTo(Long value) {
            addCriterion("actual_num <>", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumGreaterThan(Long value) {
            addCriterion("actual_num >", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumGreaterThanOrEqualTo(Long value) {
            addCriterion("actual_num >=", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumLessThan(Long value) {
            addCriterion("actual_num <", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumLessThanOrEqualTo(Long value) {
            addCriterion("actual_num <=", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumIn(List<Long> values) {
            addCriterion("actual_num in", values, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumNotIn(List<Long> values) {
            addCriterion("actual_num not in", values, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumBetween(Long value1, Long value2) {
            addCriterion("actual_num between", value1, value2, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumNotBetween(Long value1, Long value2) {
            addCriterion("actual_num not between", value1, value2, "actualNum");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("status not between", value1, value2, "status");
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