package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportStatisticTransferExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ReportStatisticTransferExample() {
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

        public Criteria andReportIdIsNull() {
            addCriterion("report_id is null");
            return (Criteria) this;
        }

        public Criteria andReportIdIsNotNull() {
            addCriterion("report_id is not null");
            return (Criteria) this;
        }

        public Criteria andReportIdEqualTo(String value) {
            addCriterion("report_id =", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotEqualTo(String value) {
            addCriterion("report_id <>", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdGreaterThan(String value) {
            addCriterion("report_id >", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("report_id >=", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLessThan(String value) {
            addCriterion("report_id <", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLessThanOrEqualTo(String value) {
            addCriterion("report_id <=", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLike(String value) {
            addCriterion("report_id like", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotLike(String value) {
            addCriterion("report_id not like", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdIn(List<String> values) {
            addCriterion("report_id in", values, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotIn(List<String> values) {
            addCriterion("report_id not in", values, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdBetween(String value1, String value2) {
            addCriterion("report_id between", value1, value2, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotBetween(String value1, String value2) {
            addCriterion("report_id not between", value1, value2, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdIsNull() {
            addCriterion("report_task_id is null");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdIsNotNull() {
            addCriterion("report_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdEqualTo(String value) {
            addCriterion("report_task_id =", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotEqualTo(String value) {
            addCriterion("report_task_id <>", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdGreaterThan(String value) {
            addCriterion("report_task_id >", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("report_task_id >=", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdLessThan(String value) {
            addCriterion("report_task_id <", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdLessThanOrEqualTo(String value) {
            addCriterion("report_task_id <=", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdLike(String value) {
            addCriterion("report_task_id like", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotLike(String value) {
            addCriterion("report_task_id not like", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdIn(List<String> values) {
            addCriterion("report_task_id in", values, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotIn(List<String> values) {
            addCriterion("report_task_id not in", values, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdBetween(String value1, String value2) {
            addCriterion("report_task_id between", value1, value2, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotBetween(String value1, String value2) {
            addCriterion("report_task_id not between", value1, value2, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTypeIsNull() {
            addCriterion("report_type is null");
            return (Criteria) this;
        }

        public Criteria andReportTypeIsNotNull() {
            addCriterion("report_type is not null");
            return (Criteria) this;
        }

        public Criteria andReportTypeEqualTo(String value) {
            addCriterion("report_type =", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeNotEqualTo(String value) {
            addCriterion("report_type <>", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeGreaterThan(String value) {
            addCriterion("report_type >", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeGreaterThanOrEqualTo(String value) {
            addCriterion("report_type >=", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeLessThan(String value) {
            addCriterion("report_type <", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeLessThanOrEqualTo(String value) {
            addCriterion("report_type <=", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeLike(String value) {
            addCriterion("report_type like", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeNotLike(String value) {
            addCriterion("report_type not like", value, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeIn(List<String> values) {
            addCriterion("report_type in", values, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeNotIn(List<String> values) {
            addCriterion("report_type not in", values, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeBetween(String value1, String value2) {
            addCriterion("report_type between", value1, value2, "reportType");
            return (Criteria) this;
        }

        public Criteria andReportTypeNotBetween(String value1, String value2) {
            addCriterion("report_type not between", value1, value2, "reportType");
            return (Criteria) this;
        }

        public Criteria andScoreFieldIsNull() {
            addCriterion("score_field is null");
            return (Criteria) this;
        }

        public Criteria andScoreFieldIsNotNull() {
            addCriterion("score_field is not null");
            return (Criteria) this;
        }

        public Criteria andScoreFieldEqualTo(String value) {
            addCriterion("score_field =", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldNotEqualTo(String value) {
            addCriterion("score_field <>", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldGreaterThan(String value) {
            addCriterion("score_field >", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldGreaterThanOrEqualTo(String value) {
            addCriterion("score_field >=", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldLessThan(String value) {
            addCriterion("score_field <", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldLessThanOrEqualTo(String value) {
            addCriterion("score_field <=", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldLike(String value) {
            addCriterion("score_field like", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldNotLike(String value) {
            addCriterion("score_field not like", value, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldIn(List<String> values) {
            addCriterion("score_field in", values, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldNotIn(List<String> values) {
            addCriterion("score_field not in", values, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldBetween(String value1, String value2) {
            addCriterion("score_field between", value1, value2, "scoreField");
            return (Criteria) this;
        }

        public Criteria andScoreFieldNotBetween(String value1, String value2) {
            addCriterion("score_field not between", value1, value2, "scoreField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldIsNull() {
            addCriterion("dimension_field is null");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldIsNotNull() {
            addCriterion("dimension_field is not null");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldEqualTo(String value) {
            addCriterion("dimension_field =", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldNotEqualTo(String value) {
            addCriterion("dimension_field <>", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldGreaterThan(String value) {
            addCriterion("dimension_field >", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldGreaterThanOrEqualTo(String value) {
            addCriterion("dimension_field >=", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldLessThan(String value) {
            addCriterion("dimension_field <", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldLessThanOrEqualTo(String value) {
            addCriterion("dimension_field <=", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldLike(String value) {
            addCriterion("dimension_field like", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldNotLike(String value) {
            addCriterion("dimension_field not like", value, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldIn(List<String> values) {
            addCriterion("dimension_field in", values, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldNotIn(List<String> values) {
            addCriterion("dimension_field not in", values, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldBetween(String value1, String value2) {
            addCriterion("dimension_field between", value1, value2, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionFieldNotBetween(String value1, String value2) {
            addCriterion("dimension_field not between", value1, value2, "dimensionField");
            return (Criteria) this;
        }

        public Criteria andDimensionValueIsNull() {
            addCriterion("dimension_value is null");
            return (Criteria) this;
        }

        public Criteria andDimensionValueIsNotNull() {
            addCriterion("dimension_value is not null");
            return (Criteria) this;
        }

        public Criteria andDimensionValueEqualTo(String value) {
            addCriterion("dimension_value =", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueNotEqualTo(String value) {
            addCriterion("dimension_value <>", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueGreaterThan(String value) {
            addCriterion("dimension_value >", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueGreaterThanOrEqualTo(String value) {
            addCriterion("dimension_value >=", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueLessThan(String value) {
            addCriterion("dimension_value <", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueLessThanOrEqualTo(String value) {
            addCriterion("dimension_value <=", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueLike(String value) {
            addCriterion("dimension_value like", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueNotLike(String value) {
            addCriterion("dimension_value not like", value, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueIn(List<String> values) {
            addCriterion("dimension_value in", values, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueNotIn(List<String> values) {
            addCriterion("dimension_value not in", values, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueBetween(String value1, String value2) {
            addCriterion("dimension_value between", value1, value2, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andDimensionValueNotBetween(String value1, String value2) {
            addCriterion("dimension_value not between", value1, value2, "dimensionValue");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldIsNull() {
            addCriterion("multi_head_field is null");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldIsNotNull() {
            addCriterion("multi_head_field is not null");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldEqualTo(String value) {
            addCriterion("multi_head_field =", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldNotEqualTo(String value) {
            addCriterion("multi_head_field <>", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldGreaterThan(String value) {
            addCriterion("multi_head_field >", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldGreaterThanOrEqualTo(String value) {
            addCriterion("multi_head_field >=", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldLessThan(String value) {
            addCriterion("multi_head_field <", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldLessThanOrEqualTo(String value) {
            addCriterion("multi_head_field <=", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldLike(String value) {
            addCriterion("multi_head_field like", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldNotLike(String value) {
            addCriterion("multi_head_field not like", value, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldIn(List<String> values) {
            addCriterion("multi_head_field in", values, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldNotIn(List<String> values) {
            addCriterion("multi_head_field not in", values, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldBetween(String value1, String value2) {
            addCriterion("multi_head_field between", value1, value2, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andMultiHeadFieldNotBetween(String value1, String value2) {
            addCriterion("multi_head_field not between", value1, value2, "multiHeadField");
            return (Criteria) this;
        }

        public Criteria andReportStatusIsNull() {
            addCriterion("report_status is null");
            return (Criteria) this;
        }

        public Criteria andReportStatusIsNotNull() {
            addCriterion("report_status is not null");
            return (Criteria) this;
        }

        public Criteria andReportStatusEqualTo(String value) {
            addCriterion("report_status =", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusNotEqualTo(String value) {
            addCriterion("report_status <>", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusGreaterThan(String value) {
            addCriterion("report_status >", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusGreaterThanOrEqualTo(String value) {
            addCriterion("report_status >=", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusLessThan(String value) {
            addCriterion("report_status <", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusLessThanOrEqualTo(String value) {
            addCriterion("report_status <=", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusLike(String value) {
            addCriterion("report_status like", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusNotLike(String value) {
            addCriterion("report_status not like", value, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusIn(List<String> values) {
            addCriterion("report_status in", values, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusNotIn(List<String> values) {
            addCriterion("report_status not in", values, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusBetween(String value1, String value2) {
            addCriterion("report_status between", value1, value2, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportStatusNotBetween(String value1, String value2) {
            addCriterion("report_status not between", value1, value2, "reportStatus");
            return (Criteria) this;
        }

        public Criteria andReportDateIsNull() {
            addCriterion("report_date is null");
            return (Criteria) this;
        }

        public Criteria andReportDateIsNotNull() {
            addCriterion("report_date is not null");
            return (Criteria) this;
        }

        public Criteria andReportDateEqualTo(String value) {
            addCriterion("report_date =", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateNotEqualTo(String value) {
            addCriterion("report_date <>", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateGreaterThan(String value) {
            addCriterion("report_date >", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateGreaterThanOrEqualTo(String value) {
            addCriterion("report_date >=", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateLessThan(String value) {
            addCriterion("report_date <", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateLessThanOrEqualTo(String value) {
            addCriterion("report_date <=", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateLike(String value) {
            addCriterion("report_date like", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateNotLike(String value) {
            addCriterion("report_date not like", value, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateIn(List<String> values) {
            addCriterion("report_date in", values, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateNotIn(List<String> values) {
            addCriterion("report_date not in", values, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateBetween(String value1, String value2) {
            addCriterion("report_date between", value1, value2, "reportDate");
            return (Criteria) this;
        }

        public Criteria andReportDateNotBetween(String value1, String value2) {
            addCriterion("report_date not between", value1, value2, "reportDate");
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