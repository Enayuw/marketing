package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingTaskAutoBuildConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingTaskAutoBuildConfigExample() {
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

        public Criteria andScoreRuleIdIsNull() {
            addCriterion("score_rule_id is null");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdIsNotNull() {
            addCriterion("score_rule_id is not null");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdEqualTo(Integer value) {
            addCriterion("score_rule_id =", value, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdNotEqualTo(Integer value) {
            addCriterion("score_rule_id <>", value, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdGreaterThan(Integer value) {
            addCriterion("score_rule_id >", value, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("score_rule_id >=", value, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdLessThan(Integer value) {
            addCriterion("score_rule_id <", value, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdLessThanOrEqualTo(Integer value) {
            addCriterion("score_rule_id <=", value, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdIn(List<Integer> values) {
            addCriterion("score_rule_id in", values, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdNotIn(List<Integer> values) {
            addCriterion("score_rule_id not in", values, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdBetween(Integer value1, Integer value2) {
            addCriterion("score_rule_id between", value1, value2, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andScoreRuleIdNotBetween(Integer value1, Integer value2) {
            addCriterion("score_rule_id not between", value1, value2, "scoreRuleId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdIsNull() {
            addCriterion("sync_report_id is null");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdIsNotNull() {
            addCriterion("sync_report_id is not null");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdEqualTo(String value) {
            addCriterion("sync_report_id =", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdNotEqualTo(String value) {
            addCriterion("sync_report_id <>", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdGreaterThan(String value) {
            addCriterion("sync_report_id >", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdGreaterThanOrEqualTo(String value) {
            addCriterion("sync_report_id >=", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdLessThan(String value) {
            addCriterion("sync_report_id <", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdLessThanOrEqualTo(String value) {
            addCriterion("sync_report_id <=", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdLike(String value) {
            addCriterion("sync_report_id like", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdNotLike(String value) {
            addCriterion("sync_report_id not like", value, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdIn(List<String> values) {
            addCriterion("sync_report_id in", values, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdNotIn(List<String> values) {
            addCriterion("sync_report_id not in", values, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdBetween(String value1, String value2) {
            addCriterion("sync_report_id between", value1, value2, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andSyncReportIdNotBetween(String value1, String value2) {
            addCriterion("sync_report_id not between", value1, value2, "syncReportId");
            return (Criteria) this;
        }

        public Criteria andDataConditionIsNull() {
            addCriterion("data_condition is null");
            return (Criteria) this;
        }

        public Criteria andDataConditionIsNotNull() {
            addCriterion("data_condition is not null");
            return (Criteria) this;
        }

        public Criteria andDataConditionEqualTo(String value) {
            addCriterion("data_condition =", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotEqualTo(String value) {
            addCriterion("data_condition <>", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionGreaterThan(String value) {
            addCriterion("data_condition >", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionGreaterThanOrEqualTo(String value) {
            addCriterion("data_condition >=", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionLessThan(String value) {
            addCriterion("data_condition <", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionLessThanOrEqualTo(String value) {
            addCriterion("data_condition <=", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionLike(String value) {
            addCriterion("data_condition like", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotLike(String value) {
            addCriterion("data_condition not like", value, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionIn(List<String> values) {
            addCriterion("data_condition in", values, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotIn(List<String> values) {
            addCriterion("data_condition not in", values, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionBetween(String value1, String value2) {
            addCriterion("data_condition between", value1, value2, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andDataConditionNotBetween(String value1, String value2) {
            addCriterion("data_condition not between", value1, value2, "dataCondition");
            return (Criteria) this;
        }

        public Criteria andStartDateIsNull() {
            addCriterion("start_date is null");
            return (Criteria) this;
        }

        public Criteria andStartDateIsNotNull() {
            addCriterion("start_date is not null");
            return (Criteria) this;
        }

        public Criteria andStartDateEqualTo(String value) {
            addCriterion("start_date =", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateNotEqualTo(String value) {
            addCriterion("start_date <>", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateGreaterThan(String value) {
            addCriterion("start_date >", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateGreaterThanOrEqualTo(String value) {
            addCriterion("start_date >=", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateLessThan(String value) {
            addCriterion("start_date <", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateLessThanOrEqualTo(String value) {
            addCriterion("start_date <=", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateLike(String value) {
            addCriterion("start_date like", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateNotLike(String value) {
            addCriterion("start_date not like", value, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateIn(List<String> values) {
            addCriterion("start_date in", values, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateNotIn(List<String> values) {
            addCriterion("start_date not in", values, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateBetween(String value1, String value2) {
            addCriterion("start_date between", value1, value2, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartDateNotBetween(String value1, String value2) {
            addCriterion("start_date not between", value1, value2, "startDate");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNull() {
            addCriterion("start_time is null");
            return (Criteria) this;
        }

        public Criteria andStartTimeIsNotNull() {
            addCriterion("start_time is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimeEqualTo(String value) {
            addCriterion("start_time =", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotEqualTo(String value) {
            addCriterion("start_time <>", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThan(String value) {
            addCriterion("start_time >", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeGreaterThanOrEqualTo(String value) {
            addCriterion("start_time >=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThan(String value) {
            addCriterion("start_time <", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLessThanOrEqualTo(String value) {
            addCriterion("start_time <=", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeLike(String value) {
            addCriterion("start_time like", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotLike(String value) {
            addCriterion("start_time not like", value, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeIn(List<String> values) {
            addCriterion("start_time in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotIn(List<String> values) {
            addCriterion("start_time not in", values, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeBetween(String value1, String value2) {
            addCriterion("start_time between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andStartTimeNotBetween(String value1, String value2) {
            addCriterion("start_time not between", value1, value2, "startTime");
            return (Criteria) this;
        }

        public Criteria andCloseDateIsNull() {
            addCriterion("close_date is null");
            return (Criteria) this;
        }

        public Criteria andCloseDateIsNotNull() {
            addCriterion("close_date is not null");
            return (Criteria) this;
        }

        public Criteria andCloseDateEqualTo(String value) {
            addCriterion("close_date =", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateNotEqualTo(String value) {
            addCriterion("close_date <>", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateGreaterThan(String value) {
            addCriterion("close_date >", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateGreaterThanOrEqualTo(String value) {
            addCriterion("close_date >=", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateLessThan(String value) {
            addCriterion("close_date <", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateLessThanOrEqualTo(String value) {
            addCriterion("close_date <=", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateLike(String value) {
            addCriterion("close_date like", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateNotLike(String value) {
            addCriterion("close_date not like", value, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateIn(List<String> values) {
            addCriterion("close_date in", values, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateNotIn(List<String> values) {
            addCriterion("close_date not in", values, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateBetween(String value1, String value2) {
            addCriterion("close_date between", value1, value2, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCloseDateNotBetween(String value1, String value2) {
            addCriterion("close_date not between", value1, value2, "closeDate");
            return (Criteria) this;
        }

        public Criteria andCycleDayIsNull() {
            addCriterion("cycle_day is null");
            return (Criteria) this;
        }

        public Criteria andCycleDayIsNotNull() {
            addCriterion("cycle_day is not null");
            return (Criteria) this;
        }

        public Criteria andCycleDayEqualTo(Integer value) {
            addCriterion("cycle_day =", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayNotEqualTo(Integer value) {
            addCriterion("cycle_day <>", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayGreaterThan(Integer value) {
            addCriterion("cycle_day >", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayGreaterThanOrEqualTo(Integer value) {
            addCriterion("cycle_day >=", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayLessThan(Integer value) {
            addCriterion("cycle_day <", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayLessThanOrEqualTo(Integer value) {
            addCriterion("cycle_day <=", value, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayIn(List<Integer> values) {
            addCriterion("cycle_day in", values, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayNotIn(List<Integer> values) {
            addCriterion("cycle_day not in", values, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayBetween(Integer value1, Integer value2) {
            addCriterion("cycle_day between", value1, value2, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andCycleDayNotBetween(Integer value1, Integer value2) {
            addCriterion("cycle_day not between", value1, value2, "cycleDay");
            return (Criteria) this;
        }

        public Criteria andExtendIsNull() {
            addCriterion("extend is null");
            return (Criteria) this;
        }

        public Criteria andExtendIsNotNull() {
            addCriterion("extend is not null");
            return (Criteria) this;
        }

        public Criteria andExtendEqualTo(String value) {
            addCriterion("extend =", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotEqualTo(String value) {
            addCriterion("extend <>", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThan(String value) {
            addCriterion("extend >", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThanOrEqualTo(String value) {
            addCriterion("extend >=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThan(String value) {
            addCriterion("extend <", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThanOrEqualTo(String value) {
            addCriterion("extend <=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLike(String value) {
            addCriterion("extend like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotLike(String value) {
            addCriterion("extend not like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendIn(List<String> values) {
            addCriterion("extend in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotIn(List<String> values) {
            addCriterion("extend not in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendBetween(String value1, String value2) {
            addCriterion("extend between", value1, value2, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotBetween(String value1, String value2) {
            addCriterion("extend not between", value1, value2, "extend");
            return (Criteria) this;
        }

        public Criteria andLabelNameIsNull() {
            addCriterion("label_name is null");
            return (Criteria) this;
        }

        public Criteria andLabelNameIsNotNull() {
            addCriterion("label_name is not null");
            return (Criteria) this;
        }

        public Criteria andLabelNameEqualTo(String value) {
            addCriterion("label_name =", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotEqualTo(String value) {
            addCriterion("label_name <>", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameGreaterThan(String value) {
            addCriterion("label_name >", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameGreaterThanOrEqualTo(String value) {
            addCriterion("label_name >=", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameLessThan(String value) {
            addCriterion("label_name <", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameLessThanOrEqualTo(String value) {
            addCriterion("label_name <=", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameLike(String value) {
            addCriterion("label_name like", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotLike(String value) {
            addCriterion("label_name not like", value, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameIn(List<String> values) {
            addCriterion("label_name in", values, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotIn(List<String> values) {
            addCriterion("label_name not in", values, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameBetween(String value1, String value2) {
            addCriterion("label_name between", value1, value2, "labelName");
            return (Criteria) this;
        }

        public Criteria andLabelNameNotBetween(String value1, String value2) {
            addCriterion("label_name not between", value1, value2, "labelName");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNull() {
            addCriterion("is_deleted is null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNotNull() {
            addCriterion("is_deleted is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedEqualTo(Integer value) {
            addCriterion("is_deleted =", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotEqualTo(Integer value) {
            addCriterion("is_deleted <>", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThan(Integer value) {
            addCriterion("is_deleted >", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_deleted >=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThan(Integer value) {
            addCriterion("is_deleted <", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThanOrEqualTo(Integer value) {
            addCriterion("is_deleted <=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIn(List<Integer> values) {
            addCriterion("is_deleted in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotIn(List<Integer> values) {
            addCriterion("is_deleted not in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted between", value1, value2, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted not between", value1, value2, "isDeleted");
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