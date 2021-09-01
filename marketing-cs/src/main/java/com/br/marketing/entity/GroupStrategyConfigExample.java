package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupStrategyConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public GroupStrategyConfigExample() {
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

        public Criteria andGroupTypeIsNull() {
            addCriterion("group_type is null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIsNotNull() {
            addCriterion("group_type is not null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeEqualTo(String value) {
            addCriterion("group_type =", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotEqualTo(String value) {
            addCriterion("group_type <>", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThan(String value) {
            addCriterion("group_type >", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThanOrEqualTo(String value) {
            addCriterion("group_type >=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThan(String value) {
            addCriterion("group_type <", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThanOrEqualTo(String value) {
            addCriterion("group_type <=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLike(String value) {
            addCriterion("group_type like", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotLike(String value) {
            addCriterion("group_type not like", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIn(List<String> values) {
            addCriterion("group_type in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotIn(List<String> values) {
            addCriterion("group_type not in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeBetween(String value1, String value2) {
            addCriterion("group_type between", value1, value2, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotBetween(String value1, String value2) {
            addCriterion("group_type not between", value1, value2, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortIsNull() {
            addCriterion("group_type_short is null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortIsNotNull() {
            addCriterion("group_type_short is not null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortEqualTo(String value) {
            addCriterion("group_type_short =", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortNotEqualTo(String value) {
            addCriterion("group_type_short <>", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortGreaterThan(String value) {
            addCriterion("group_type_short >", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortGreaterThanOrEqualTo(String value) {
            addCriterion("group_type_short >=", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortLessThan(String value) {
            addCriterion("group_type_short <", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortLessThanOrEqualTo(String value) {
            addCriterion("group_type_short <=", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortLike(String value) {
            addCriterion("group_type_short like", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortNotLike(String value) {
            addCriterion("group_type_short not like", value, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortIn(List<String> values) {
            addCriterion("group_type_short in", values, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortNotIn(List<String> values) {
            addCriterion("group_type_short not in", values, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortBetween(String value1, String value2) {
            addCriterion("group_type_short between", value1, value2, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andGroupTypeShortNotBetween(String value1, String value2) {
            addCriterion("group_type_short not between", value1, value2, "groupTypeShort");
            return (Criteria) this;
        }

        public Criteria andStrategyIdIsNull() {
            addCriterion("strategy_id is null");
            return (Criteria) this;
        }

        public Criteria andStrategyIdIsNotNull() {
            addCriterion("strategy_id is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyIdEqualTo(String value) {
            addCriterion("strategy_id =", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotEqualTo(String value) {
            addCriterion("strategy_id <>", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdGreaterThan(String value) {
            addCriterion("strategy_id >", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_id >=", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdLessThan(String value) {
            addCriterion("strategy_id <", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdLessThanOrEqualTo(String value) {
            addCriterion("strategy_id <=", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdLike(String value) {
            addCriterion("strategy_id like", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotLike(String value) {
            addCriterion("strategy_id not like", value, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdIn(List<String> values) {
            addCriterion("strategy_id in", values, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotIn(List<String> values) {
            addCriterion("strategy_id not in", values, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdBetween(String value1, String value2) {
            addCriterion("strategy_id between", value1, value2, "strategyId");
            return (Criteria) this;
        }

        public Criteria andStrategyIdNotBetween(String value1, String value2) {
            addCriterion("strategy_id not between", value1, value2, "strategyId");
            return (Criteria) this;
        }

        public Criteria andBaseInfoIsNull() {
            addCriterion("base_info is null");
            return (Criteria) this;
        }

        public Criteria andBaseInfoIsNotNull() {
            addCriterion("base_info is not null");
            return (Criteria) this;
        }

        public Criteria andBaseInfoEqualTo(String value) {
            addCriterion("base_info =", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotEqualTo(String value) {
            addCriterion("base_info <>", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoGreaterThan(String value) {
            addCriterion("base_info >", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoGreaterThanOrEqualTo(String value) {
            addCriterion("base_info >=", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoLessThan(String value) {
            addCriterion("base_info <", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoLessThanOrEqualTo(String value) {
            addCriterion("base_info <=", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoLike(String value) {
            addCriterion("base_info like", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotLike(String value) {
            addCriterion("base_info not like", value, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoIn(List<String> values) {
            addCriterion("base_info in", values, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotIn(List<String> values) {
            addCriterion("base_info not in", values, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoBetween(String value1, String value2) {
            addCriterion("base_info between", value1, value2, "baseInfo");
            return (Criteria) this;
        }

        public Criteria andBaseInfoNotBetween(String value1, String value2) {
            addCriterion("base_info not between", value1, value2, "baseInfo");
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

        public Criteria andExecTypeIsNull() {
            addCriterion("exec_type is null");
            return (Criteria) this;
        }

        public Criteria andExecTypeIsNotNull() {
            addCriterion("exec_type is not null");
            return (Criteria) this;
        }

        public Criteria andExecTypeEqualTo(Integer value) {
            addCriterion("exec_type =", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeNotEqualTo(Integer value) {
            addCriterion("exec_type <>", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeGreaterThan(Integer value) {
            addCriterion("exec_type >", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("exec_type >=", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeLessThan(Integer value) {
            addCriterion("exec_type <", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeLessThanOrEqualTo(Integer value) {
            addCriterion("exec_type <=", value, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeIn(List<Integer> values) {
            addCriterion("exec_type in", values, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeNotIn(List<Integer> values) {
            addCriterion("exec_type not in", values, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeBetween(Integer value1, Integer value2) {
            addCriterion("exec_type between", value1, value2, "execType");
            return (Criteria) this;
        }

        public Criteria andExecTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("exec_type not between", value1, value2, "execType");
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

        public Criteria andCycleEndDayIsNull() {
            addCriterion("cycle_end_day is null");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayIsNotNull() {
            addCriterion("cycle_end_day is not null");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayEqualTo(String value) {
            addCriterion("cycle_end_day =", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotEqualTo(String value) {
            addCriterion("cycle_end_day <>", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayGreaterThan(String value) {
            addCriterion("cycle_end_day >", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayGreaterThanOrEqualTo(String value) {
            addCriterion("cycle_end_day >=", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayLessThan(String value) {
            addCriterion("cycle_end_day <", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayLessThanOrEqualTo(String value) {
            addCriterion("cycle_end_day <=", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayLike(String value) {
            addCriterion("cycle_end_day like", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotLike(String value) {
            addCriterion("cycle_end_day not like", value, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayIn(List<String> values) {
            addCriterion("cycle_end_day in", values, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotIn(List<String> values) {
            addCriterion("cycle_end_day not in", values, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayBetween(String value1, String value2) {
            addCriterion("cycle_end_day between", value1, value2, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andCycleEndDayNotBetween(String value1, String value2) {
            addCriterion("cycle_end_day not between", value1, value2, "cycleEndDay");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNull() {
            addCriterion("push_type is null");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNotNull() {
            addCriterion("push_type is not null");
            return (Criteria) this;
        }

        public Criteria andPushTypeEqualTo(Integer value) {
            addCriterion("push_type =", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotEqualTo(Integer value) {
            addCriterion("push_type <>", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThan(Integer value) {
            addCriterion("push_type >", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_type >=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThan(Integer value) {
            addCriterion("push_type <", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThanOrEqualTo(Integer value) {
            addCriterion("push_type <=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeIn(List<Integer> values) {
            addCriterion("push_type in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotIn(List<Integer> values) {
            addCriterion("push_type not in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeBetween(Integer value1, Integer value2) {
            addCriterion("push_type between", value1, value2, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("push_type not between", value1, value2, "pushType");
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