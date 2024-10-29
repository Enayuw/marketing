package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportStatisticsScoreExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ReportStatisticsScoreExample() {
        oredCriteria = new ArrayList<>();
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
            criteria = new ArrayList<>();
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

        public Criteria andReportIdEqualTo(Long value) {
            addCriterion("report_id =", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotEqualTo(Long value) {
            addCriterion("report_id <>", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdGreaterThan(Long value) {
            addCriterion("report_id >", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdGreaterThanOrEqualTo(Long value) {
            addCriterion("report_id >=", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLessThan(Long value) {
            addCriterion("report_id <", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdLessThanOrEqualTo(Long value) {
            addCriterion("report_id <=", value, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdIn(List<Long> values) {
            addCriterion("report_id in", values, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotIn(List<Long> values) {
            addCriterion("report_id not in", values, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdBetween(Long value1, Long value2) {
            addCriterion("report_id between", value1, value2, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportIdNotBetween(Long value1, Long value2) {
            addCriterion("report_id not between", value1, value2, "reportId");
            return (Criteria) this;
        }

        public Criteria andReportRuleIsNull() {
            addCriterion("report_rule is null");
            return (Criteria) this;
        }

        public Criteria andReportRuleIsNotNull() {
            addCriterion("report_rule is not null");
            return (Criteria) this;
        }

        public Criteria andReportRuleEqualTo(String value) {
            addCriterion("report_rule =", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleNotEqualTo(String value) {
            addCriterion("report_rule <>", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleGreaterThan(String value) {
            addCriterion("report_rule >", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleGreaterThanOrEqualTo(String value) {
            addCriterion("report_rule >=", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleLessThan(String value) {
            addCriterion("report_rule <", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleLessThanOrEqualTo(String value) {
            addCriterion("report_rule <=", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleLike(String value) {
            addCriterion("report_rule like", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleNotLike(String value) {
            addCriterion("report_rule not like", value, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleIn(List<String> values) {
            addCriterion("report_rule in", values, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleNotIn(List<String> values) {
            addCriterion("report_rule not in", values, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleBetween(String value1, String value2) {
            addCriterion("report_rule between", value1, value2, "reportRule");
            return (Criteria) this;
        }

        public Criteria andReportRuleNotBetween(String value1, String value2) {
            addCriterion("report_rule not between", value1, value2, "reportRule");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListIsNull() {
            addCriterion("batch_number_list is null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListIsNotNull() {
            addCriterion("batch_number_list is not null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListEqualTo(String value) {
            addCriterion("batch_number_list =", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListNotEqualTo(String value) {
            addCriterion("batch_number_list <>", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListGreaterThan(String value) {
            addCriterion("batch_number_list >", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListGreaterThanOrEqualTo(String value) {
            addCriterion("batch_number_list >=", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListLessThan(String value) {
            addCriterion("batch_number_list <", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListLessThanOrEqualTo(String value) {
            addCriterion("batch_number_list <=", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListLike(String value) {
            addCriterion("batch_number_list like", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListNotLike(String value) {
            addCriterion("batch_number_list not like", value, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListIn(List<String> values) {
            addCriterion("batch_number_list in", values, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListNotIn(List<String> values) {
            addCriterion("batch_number_list not in", values, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListBetween(String value1, String value2) {
            addCriterion("batch_number_list between", value1, value2, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andBatchNumberListNotBetween(String value1, String value2) {
            addCriterion("batch_number_list not between", value1, value2, "batchNumberList");
            return (Criteria) this;
        }

        public Criteria andFieldXIsNull() {
            addCriterion("field_x is null");
            return (Criteria) this;
        }

        public Criteria andFieldXIsNotNull() {
            addCriterion("field_x is not null");
            return (Criteria) this;
        }

        public Criteria andFieldXEqualTo(String value) {
            addCriterion("field_x =", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXNotEqualTo(String value) {
            addCriterion("field_x <>", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXGreaterThan(String value) {
            addCriterion("field_x >", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXGreaterThanOrEqualTo(String value) {
            addCriterion("field_x >=", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXLessThan(String value) {
            addCriterion("field_x <", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXLessThanOrEqualTo(String value) {
            addCriterion("field_x <=", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXLike(String value) {
            addCriterion("field_x like", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXNotLike(String value) {
            addCriterion("field_x not like", value, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXIn(List<String> values) {
            addCriterion("field_x in", values, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXNotIn(List<String> values) {
            addCriterion("field_x not in", values, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXBetween(String value1, String value2) {
            addCriterion("field_x between", value1, value2, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldXNotBetween(String value1, String value2) {
            addCriterion("field_x not between", value1, value2, "fieldX");
            return (Criteria) this;
        }

        public Criteria andFieldYIsNull() {
            addCriterion("field_y is null");
            return (Criteria) this;
        }

        public Criteria andFieldYIsNotNull() {
            addCriterion("field_y is not null");
            return (Criteria) this;
        }

        public Criteria andFieldYEqualTo(String value) {
            addCriterion("field_y =", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYNotEqualTo(String value) {
            addCriterion("field_y <>", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYGreaterThan(String value) {
            addCriterion("field_y >", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYGreaterThanOrEqualTo(String value) {
            addCriterion("field_y >=", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYLessThan(String value) {
            addCriterion("field_y <", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYLessThanOrEqualTo(String value) {
            addCriterion("field_y <=", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYLike(String value) {
            addCriterion("field_y like", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYNotLike(String value) {
            addCriterion("field_y not like", value, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYIn(List<String> values) {
            addCriterion("field_y in", values, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYNotIn(List<String> values) {
            addCriterion("field_y not in", values, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYBetween(String value1, String value2) {
            addCriterion("field_y between", value1, value2, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldYNotBetween(String value1, String value2) {
            addCriterion("field_y not between", value1, value2, "fieldY");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeIsNull() {
            addCriterion("field_x_range is null");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeIsNotNull() {
            addCriterion("field_x_range is not null");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeEqualTo(String value) {
            addCriterion("field_x_range =", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeNotEqualTo(String value) {
            addCriterion("field_x_range <>", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeGreaterThan(String value) {
            addCriterion("field_x_range >", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeGreaterThanOrEqualTo(String value) {
            addCriterion("field_x_range >=", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeLessThan(String value) {
            addCriterion("field_x_range <", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeLessThanOrEqualTo(String value) {
            addCriterion("field_x_range <=", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeLike(String value) {
            addCriterion("field_x_range like", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeNotLike(String value) {
            addCriterion("field_x_range not like", value, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeIn(List<String> values) {
            addCriterion("field_x_range in", values, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeNotIn(List<String> values) {
            addCriterion("field_x_range not in", values, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeBetween(String value1, String value2) {
            addCriterion("field_x_range between", value1, value2, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldXRangeNotBetween(String value1, String value2) {
            addCriterion("field_x_range not between", value1, value2, "fieldXRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeIsNull() {
            addCriterion("field_y_range is null");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeIsNotNull() {
            addCriterion("field_y_range is not null");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeEqualTo(String value) {
            addCriterion("field_y_range =", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeNotEqualTo(String value) {
            addCriterion("field_y_range <>", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeGreaterThan(String value) {
            addCriterion("field_y_range >", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeGreaterThanOrEqualTo(String value) {
            addCriterion("field_y_range >=", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeLessThan(String value) {
            addCriterion("field_y_range <", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeLessThanOrEqualTo(String value) {
            addCriterion("field_y_range <=", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeLike(String value) {
            addCriterion("field_y_range like", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeNotLike(String value) {
            addCriterion("field_y_range not like", value, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeIn(List<String> values) {
            addCriterion("field_y_range in", values, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeNotIn(List<String> values) {
            addCriterion("field_y_range not in", values, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeBetween(String value1, String value2) {
            addCriterion("field_y_range between", value1, value2, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andFieldYRangeNotBetween(String value1, String value2) {
            addCriterion("field_y_range not between", value1, value2, "fieldYRange");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeIsNull() {
            addCriterion("report_score_type is null");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeIsNotNull() {
            addCriterion("report_score_type is not null");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeEqualTo(Integer value) {
            addCriterion("report_score_type =", value, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeNotEqualTo(Integer value) {
            addCriterion("report_score_type <>", value, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeGreaterThan(Integer value) {
            addCriterion("report_score_type >", value, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("report_score_type >=", value, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeLessThan(Integer value) {
            addCriterion("report_score_type <", value, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeLessThanOrEqualTo(Integer value) {
            addCriterion("report_score_type <=", value, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeIn(List<Integer> values) {
            addCriterion("report_score_type in", values, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeNotIn(List<Integer> values) {
            addCriterion("report_score_type not in", values, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeBetween(Integer value1, Integer value2) {
            addCriterion("report_score_type between", value1, value2, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andReportScoreTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("report_score_type not between", value1, value2, "reportScoreType");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescIsNull() {
            addCriterion("statistics_desc is null");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescIsNotNull() {
            addCriterion("statistics_desc is not null");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescEqualTo(String value) {
            addCriterion("statistics_desc =", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescNotEqualTo(String value) {
            addCriterion("statistics_desc <>", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescGreaterThan(String value) {
            addCriterion("statistics_desc >", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescGreaterThanOrEqualTo(String value) {
            addCriterion("statistics_desc >=", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescLessThan(String value) {
            addCriterion("statistics_desc <", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescLessThanOrEqualTo(String value) {
            addCriterion("statistics_desc <=", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescLike(String value) {
            addCriterion("statistics_desc like", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescNotLike(String value) {
            addCriterion("statistics_desc not like", value, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescIn(List<String> values) {
            addCriterion("statistics_desc in", values, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescNotIn(List<String> values) {
            addCriterion("statistics_desc not in", values, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescBetween(String value1, String value2) {
            addCriterion("statistics_desc between", value1, value2, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsDescNotBetween(String value1, String value2) {
            addCriterion("statistics_desc not between", value1, value2, "statisticsDesc");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderIsNull() {
            addCriterion("statistics_order is null");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderIsNotNull() {
            addCriterion("statistics_order is not null");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderEqualTo(Integer value) {
            addCriterion("statistics_order =", value, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderNotEqualTo(Integer value) {
            addCriterion("statistics_order <>", value, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderGreaterThan(Integer value) {
            addCriterion("statistics_order >", value, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderGreaterThanOrEqualTo(Integer value) {
            addCriterion("statistics_order >=", value, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderLessThan(Integer value) {
            addCriterion("statistics_order <", value, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderLessThanOrEqualTo(Integer value) {
            addCriterion("statistics_order <=", value, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderIn(List<Integer> values) {
            addCriterion("statistics_order in", values, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderNotIn(List<Integer> values) {
            addCriterion("statistics_order not in", values, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderBetween(Integer value1, Integer value2) {
            addCriterion("statistics_order between", value1, value2, "statisticsOrder");
            return (Criteria) this;
        }

        public Criteria andStatisticsOrderNotBetween(Integer value1, Integer value2) {
            addCriterion("statistics_order not between", value1, value2, "statisticsOrder");
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

    /**
     */
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