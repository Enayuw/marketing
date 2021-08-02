package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupStrategyConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public GroupStrategyConfigExample() {
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

        public BaseCriteria andGroupTypeIsNull() {
            addCriterion("group_type is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeIsNotNull() {
            addCriterion("group_type is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeEqualTo(String value) {
            addCriterion("group_type =", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeNotEqualTo(String value) {
            addCriterion("group_type <>", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeGreaterThan(String value) {
            addCriterion("group_type >", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeGreaterThanOrEqualTo(String value) {
            addCriterion("group_type >=", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeLessThan(String value) {
            addCriterion("group_type <", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeLessThanOrEqualTo(String value) {
            addCriterion("group_type <=", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeLike(String value) {
            addCriterion("group_type like", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeNotLike(String value) {
            addCriterion("group_type not like", value, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeIn(List<String> values) {
            addCriterion("group_type in", values, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeNotIn(List<String> values) {
            addCriterion("group_type not in", values, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeBetween(String value1, String value2) {
            addCriterion("group_type between", value1, value2, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeNotBetween(String value1, String value2) {
            addCriterion("group_type not between", value1, value2, "groupType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortIsNull() {
            addCriterion("group_type_short is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortIsNotNull() {
            addCriterion("group_type_short is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortEqualTo(String value) {
            addCriterion("group_type_short =", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortNotEqualTo(String value) {
            addCriterion("group_type_short <>", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortGreaterThan(String value) {
            addCriterion("group_type_short >", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortGreaterThanOrEqualTo(String value) {
            addCriterion("group_type_short >=", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortLessThan(String value) {
            addCriterion("group_type_short <", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortLessThanOrEqualTo(String value) {
            addCriterion("group_type_short <=", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortLike(String value) {
            addCriterion("group_type_short like", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortNotLike(String value) {
            addCriterion("group_type_short not like", value, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortIn(List<String> values) {
            addCriterion("group_type_short in", values, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortNotIn(List<String> values) {
            addCriterion("group_type_short not in", values, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortBetween(String value1, String value2) {
            addCriterion("group_type_short between", value1, value2, "groupTypeShort");
            return (BaseCriteria) this;
        }

        public BaseCriteria andGroupTypeShortNotBetween(String value1, String value2) {
            addCriterion("group_type_short not between", value1, value2, "groupTypeShort");
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

        public BaseCriteria andBaseInfoIsNull() {
            addCriterion("base_info is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoIsNotNull() {
            addCriterion("base_info is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoEqualTo(String value) {
            addCriterion("base_info =", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoNotEqualTo(String value) {
            addCriterion("base_info <>", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoGreaterThan(String value) {
            addCriterion("base_info >", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoGreaterThanOrEqualTo(String value) {
            addCriterion("base_info >=", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoLessThan(String value) {
            addCriterion("base_info <", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoLessThanOrEqualTo(String value) {
            addCriterion("base_info <=", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoLike(String value) {
            addCriterion("base_info like", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoNotLike(String value) {
            addCriterion("base_info not like", value, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoIn(List<String> values) {
            addCriterion("base_info in", values, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoNotIn(List<String> values) {
            addCriterion("base_info not in", values, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoBetween(String value1, String value2) {
            addCriterion("base_info between", value1, value2, "baseInfo");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBaseInfoNotBetween(String value1, String value2) {
            addCriterion("base_info not between", value1, value2, "baseInfo");
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

        public BaseCriteria andExecTypeIsNull() {
            addCriterion("exec_type is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeIsNotNull() {
            addCriterion("exec_type is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeEqualTo(Integer value) {
            addCriterion("exec_type =", value, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeNotEqualTo(Integer value) {
            addCriterion("exec_type <>", value, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeGreaterThan(Integer value) {
            addCriterion("exec_type >", value, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("exec_type >=", value, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeLessThan(Integer value) {
            addCriterion("exec_type <", value, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeLessThanOrEqualTo(Integer value) {
            addCriterion("exec_type <=", value, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeIn(List<Integer> values) {
            addCriterion("exec_type in", values, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeNotIn(List<Integer> values) {
            addCriterion("exec_type not in", values, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeBetween(Integer value1, Integer value2) {
            addCriterion("exec_type between", value1, value2, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExecTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("exec_type not between", value1, value2, "execType");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayIsNull() {
            addCriterion("cycle_day is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayIsNotNull() {
            addCriterion("cycle_day is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayEqualTo(Integer value) {
            addCriterion("cycle_day =", value, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayNotEqualTo(Integer value) {
            addCriterion("cycle_day <>", value, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayGreaterThan(Integer value) {
            addCriterion("cycle_day >", value, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayGreaterThanOrEqualTo(Integer value) {
            addCriterion("cycle_day >=", value, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayLessThan(Integer value) {
            addCriterion("cycle_day <", value, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayLessThanOrEqualTo(Integer value) {
            addCriterion("cycle_day <=", value, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayIn(List<Integer> values) {
            addCriterion("cycle_day in", values, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayNotIn(List<Integer> values) {
            addCriterion("cycle_day not in", values, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayBetween(Integer value1, Integer value2) {
            addCriterion("cycle_day between", value1, value2, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleDayNotBetween(Integer value1, Integer value2) {
            addCriterion("cycle_day not between", value1, value2, "cycleDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayIsNull() {
            addCriterion("cycle_end_day is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayIsNotNull() {
            addCriterion("cycle_end_day is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayEqualTo(String value) {
            addCriterion("cycle_end_day =", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayNotEqualTo(String value) {
            addCriterion("cycle_end_day <>", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayGreaterThan(String value) {
            addCriterion("cycle_end_day >", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayGreaterThanOrEqualTo(String value) {
            addCriterion("cycle_end_day >=", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayLessThan(String value) {
            addCriterion("cycle_end_day <", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayLessThanOrEqualTo(String value) {
            addCriterion("cycle_end_day <=", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayLike(String value) {
            addCriterion("cycle_end_day like", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayNotLike(String value) {
            addCriterion("cycle_end_day not like", value, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayIn(List<String> values) {
            addCriterion("cycle_end_day in", values, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayNotIn(List<String> values) {
            addCriterion("cycle_end_day not in", values, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayBetween(String value1, String value2) {
            addCriterion("cycle_end_day between", value1, value2, "cycleEndDay");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCycleEndDayNotBetween(String value1, String value2) {
            addCriterion("cycle_end_day not between", value1, value2, "cycleEndDay");
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