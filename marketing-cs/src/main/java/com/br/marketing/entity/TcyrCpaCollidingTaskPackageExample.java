package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TcyrCpaCollidingTaskPackageExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TcyrCpaCollidingTaskPackageExample() {
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

        public Criteria andCollidingTaskIdIsNull() {
            addCriterion("colliding_task_id is null");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdIsNotNull() {
            addCriterion("colliding_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdEqualTo(Long value) {
            addCriterion("colliding_task_id =", value, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdNotEqualTo(Long value) {
            addCriterion("colliding_task_id <>", value, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdGreaterThan(Long value) {
            addCriterion("colliding_task_id >", value, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdGreaterThanOrEqualTo(Long value) {
            addCriterion("colliding_task_id >=", value, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdLessThan(Long value) {
            addCriterion("colliding_task_id <", value, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdLessThanOrEqualTo(Long value) {
            addCriterion("colliding_task_id <=", value, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdIn(List<Long> values) {
            addCriterion("colliding_task_id in", values, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdNotIn(List<Long> values) {
            addCriterion("colliding_task_id not in", values, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdBetween(Long value1, Long value2) {
            addCriterion("colliding_task_id between", value1, value2, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingTaskIdNotBetween(Long value1, Long value2) {
            addCriterion("colliding_task_id not between", value1, value2, "collidingTaskId");
            return (Criteria) this;
        }

        public Criteria andPackageTypeIsNull() {
            addCriterion("package_type is null");
            return (Criteria) this;
        }

        public Criteria andPackageTypeIsNotNull() {
            addCriterion("package_type is not null");
            return (Criteria) this;
        }

        public Criteria andPackageTypeEqualTo(Integer value) {
            addCriterion("package_type =", value, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeNotEqualTo(Integer value) {
            addCriterion("package_type <>", value, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeGreaterThan(Integer value) {
            addCriterion("package_type >", value, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("package_type >=", value, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeLessThan(Integer value) {
            addCriterion("package_type <", value, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeLessThanOrEqualTo(Integer value) {
            addCriterion("package_type <=", value, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeIn(List<Integer> values) {
            addCriterion("package_type in", values, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeNotIn(List<Integer> values) {
            addCriterion("package_type not in", values, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeBetween(Integer value1, Integer value2) {
            addCriterion("package_type between", value1, value2, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("package_type not between", value1, value2, "packageType");
            return (Criteria) this;
        }

        public Criteria andPackageIdIsNull() {
            addCriterion("package_id is null");
            return (Criteria) this;
        }

        public Criteria andPackageIdIsNotNull() {
            addCriterion("package_id is not null");
            return (Criteria) this;
        }

        public Criteria andPackageIdEqualTo(Long value) {
            addCriterion("package_id =", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdNotEqualTo(Long value) {
            addCriterion("package_id <>", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdGreaterThan(Long value) {
            addCriterion("package_id >", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdGreaterThanOrEqualTo(Long value) {
            addCriterion("package_id >=", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdLessThan(Long value) {
            addCriterion("package_id <", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdLessThanOrEqualTo(Long value) {
            addCriterion("package_id <=", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdIn(List<Long> values) {
            addCriterion("package_id in", values, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdNotIn(List<Long> values) {
            addCriterion("package_id not in", values, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdBetween(Long value1, Long value2) {
            addCriterion("package_id between", value1, value2, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdNotBetween(Long value1, Long value2) {
            addCriterion("package_id not between", value1, value2, "packageId");
            return (Criteria) this;
        }

        public Criteria andPriorityIsNull() {
            addCriterion("priority is null");
            return (Criteria) this;
        }

        public Criteria andPriorityIsNotNull() {
            addCriterion("priority is not null");
            return (Criteria) this;
        }

        public Criteria andPriorityEqualTo(Integer value) {
            addCriterion("priority =", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotEqualTo(Integer value) {
            addCriterion("priority <>", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThan(Integer value) {
            addCriterion("priority >", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityGreaterThanOrEqualTo(Integer value) {
            addCriterion("priority >=", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityLessThan(Integer value) {
            addCriterion("priority <", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityLessThanOrEqualTo(Integer value) {
            addCriterion("priority <=", value, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityIn(List<Integer> values) {
            addCriterion("priority in", values, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotIn(List<Integer> values) {
            addCriterion("priority not in", values, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityBetween(Integer value1, Integer value2) {
            addCriterion("priority between", value1, value2, "priority");
            return (Criteria) this;
        }

        public Criteria andPriorityNotBetween(Integer value1, Integer value2) {
            addCriterion("priority not between", value1, value2, "priority");
            return (Criteria) this;
        }

        public Criteria andFailMsgIsNull() {
            addCriterion("fail_msg is null");
            return (Criteria) this;
        }

        public Criteria andFailMsgIsNotNull() {
            addCriterion("fail_msg is not null");
            return (Criteria) this;
        }

        public Criteria andFailMsgEqualTo(String value) {
            addCriterion("fail_msg =", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotEqualTo(String value) {
            addCriterion("fail_msg <>", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgGreaterThan(String value) {
            addCriterion("fail_msg >", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgGreaterThanOrEqualTo(String value) {
            addCriterion("fail_msg >=", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgLessThan(String value) {
            addCriterion("fail_msg <", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgLessThanOrEqualTo(String value) {
            addCriterion("fail_msg <=", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgLike(String value) {
            addCriterion("fail_msg like", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotLike(String value) {
            addCriterion("fail_msg not like", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgIn(List<String> values) {
            addCriterion("fail_msg in", values, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotIn(List<String> values) {
            addCriterion("fail_msg not in", values, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgBetween(String value1, String value2) {
            addCriterion("fail_msg between", value1, value2, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotBetween(String value1, String value2) {
            addCriterion("fail_msg not between", value1, value2, "failMsg");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoIsNull() {
            addCriterion("supply_rule_info is null");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoIsNotNull() {
            addCriterion("supply_rule_info is not null");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoEqualTo(String value) {
            addCriterion("supply_rule_info =", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoNotEqualTo(String value) {
            addCriterion("supply_rule_info <>", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoGreaterThan(String value) {
            addCriterion("supply_rule_info >", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoGreaterThanOrEqualTo(String value) {
            addCriterion("supply_rule_info >=", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoLessThan(String value) {
            addCriterion("supply_rule_info <", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoLessThanOrEqualTo(String value) {
            addCriterion("supply_rule_info <=", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoLike(String value) {
            addCriterion("supply_rule_info like", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoNotLike(String value) {
            addCriterion("supply_rule_info not like", value, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoIn(List<String> values) {
            addCriterion("supply_rule_info in", values, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoNotIn(List<String> values) {
            addCriterion("supply_rule_info not in", values, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoBetween(String value1, String value2) {
            addCriterion("supply_rule_info between", value1, value2, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andSupplyRuleInfoNotBetween(String value1, String value2) {
            addCriterion("supply_rule_info not between", value1, value2, "supplyRuleInfo");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlIsNull() {
            addCriterion("execute_sql is null");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlIsNotNull() {
            addCriterion("execute_sql is not null");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlEqualTo(String value) {
            addCriterion("execute_sql =", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlNotEqualTo(String value) {
            addCriterion("execute_sql <>", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlGreaterThan(String value) {
            addCriterion("execute_sql >", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlGreaterThanOrEqualTo(String value) {
            addCriterion("execute_sql >=", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlLessThan(String value) {
            addCriterion("execute_sql <", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlLessThanOrEqualTo(String value) {
            addCriterion("execute_sql <=", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlLike(String value) {
            addCriterion("execute_sql like", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlNotLike(String value) {
            addCriterion("execute_sql not like", value, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlIn(List<String> values) {
            addCriterion("execute_sql in", values, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlNotIn(List<String> values) {
            addCriterion("execute_sql not in", values, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlBetween(String value1, String value2) {
            addCriterion("execute_sql between", value1, value2, "executeSql");
            return (Criteria) this;
        }

        public Criteria andExecuteSqlNotBetween(String value1, String value2) {
            addCriterion("execute_sql not between", value1, value2, "executeSql");
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

        public Criteria andMagnitudeIsNull() {
            addCriterion("magnitude is null");
            return (Criteria) this;
        }

        public Criteria andMagnitudeIsNotNull() {
            addCriterion("magnitude is not null");
            return (Criteria) this;
        }

        public Criteria andMagnitudeEqualTo(Integer value) {
            addCriterion("magnitude =", value, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeNotEqualTo(Integer value) {
            addCriterion("magnitude <>", value, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeGreaterThan(Integer value) {
            addCriterion("magnitude >", value, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeGreaterThanOrEqualTo(Integer value) {
            addCriterion("magnitude >=", value, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeLessThan(Integer value) {
            addCriterion("magnitude <", value, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeLessThanOrEqualTo(Integer value) {
            addCriterion("magnitude <=", value, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeIn(List<Integer> values) {
            addCriterion("magnitude in", values, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeNotIn(List<Integer> values) {
            addCriterion("magnitude not in", values, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeBetween(Integer value1, Integer value2) {
            addCriterion("magnitude between", value1, value2, "magnitude");
            return (Criteria) this;
        }

        public Criteria andMagnitudeNotBetween(Integer value1, Integer value2) {
            addCriterion("magnitude not between", value1, value2, "magnitude");
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