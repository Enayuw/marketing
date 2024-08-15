package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XieChengCollidingDataPackageExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public XieChengCollidingDataPackageExample() {
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

        public Criteria andPackageNameIsNull() {
            addCriterion("package_name is null");
            return (Criteria) this;
        }

        public Criteria andPackageNameIsNotNull() {
            addCriterion("package_name is not null");
            return (Criteria) this;
        }

        public Criteria andPackageNameEqualTo(String value) {
            addCriterion("package_name =", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameNotEqualTo(String value) {
            addCriterion("package_name <>", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameGreaterThan(String value) {
            addCriterion("package_name >", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameGreaterThanOrEqualTo(String value) {
            addCriterion("package_name >=", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameLessThan(String value) {
            addCriterion("package_name <", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameLessThanOrEqualTo(String value) {
            addCriterion("package_name <=", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameLike(String value) {
            addCriterion("package_name like", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameNotLike(String value) {
            addCriterion("package_name not like", value, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameIn(List<String> values) {
            addCriterion("package_name in", values, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameNotIn(List<String> values) {
            addCriterion("package_name not in", values, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameBetween(String value1, String value2) {
            addCriterion("package_name between", value1, value2, "packageName");
            return (Criteria) this;
        }

        public Criteria andPackageNameNotBetween(String value1, String value2) {
            addCriterion("package_name not between", value1, value2, "packageName");
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

        public Criteria andRoundIsNull() {
            addCriterion("round is null");
            return (Criteria) this;
        }

        public Criteria andRoundIsNotNull() {
            addCriterion("round is not null");
            return (Criteria) this;
        }

        public Criteria andRoundEqualTo(Integer value) {
            addCriterion("round =", value, "round");
            return (Criteria) this;
        }

        public Criteria andRoundNotEqualTo(Integer value) {
            addCriterion("round <>", value, "round");
            return (Criteria) this;
        }

        public Criteria andRoundGreaterThan(Integer value) {
            addCriterion("round >", value, "round");
            return (Criteria) this;
        }

        public Criteria andRoundGreaterThanOrEqualTo(Integer value) {
            addCriterion("round >=", value, "round");
            return (Criteria) this;
        }

        public Criteria andRoundLessThan(Integer value) {
            addCriterion("round <", value, "round");
            return (Criteria) this;
        }

        public Criteria andRoundLessThanOrEqualTo(Integer value) {
            addCriterion("round <=", value, "round");
            return (Criteria) this;
        }

        public Criteria andRoundIn(List<Integer> values) {
            addCriterion("round in", values, "round");
            return (Criteria) this;
        }

        public Criteria andRoundNotIn(List<Integer> values) {
            addCriterion("round not in", values, "round");
            return (Criteria) this;
        }

        public Criteria andRoundBetween(Integer value1, Integer value2) {
            addCriterion("round between", value1, value2, "round");
            return (Criteria) this;
        }

        public Criteria andRoundNotBetween(Integer value1, Integer value2) {
            addCriterion("round not between", value1, value2, "round");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeIsNull() {
            addCriterion("colliding_time is null");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeIsNotNull() {
            addCriterion("colliding_time is not null");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeEqualTo(Date value) {
            addCriterion("colliding_time =", value, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeNotEqualTo(Date value) {
            addCriterion("colliding_time <>", value, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeGreaterThan(Date value) {
            addCriterion("colliding_time >", value, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("colliding_time >=", value, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeLessThan(Date value) {
            addCriterion("colliding_time <", value, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeLessThanOrEqualTo(Date value) {
            addCriterion("colliding_time <=", value, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeIn(List<Date> values) {
            addCriterion("colliding_time in", values, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeNotIn(List<Date> values) {
            addCriterion("colliding_time not in", values, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeBetween(Date value1, Date value2) {
            addCriterion("colliding_time between", value1, value2, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andCollidingTimeNotBetween(Date value1, Date value2) {
            addCriterion("colliding_time not between", value1, value2, "collidingTime");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIsNull() {
            addCriterion("is_delete is null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIsNotNull() {
            addCriterion("is_delete is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteEqualTo(Integer value) {
            addCriterion("is_delete =", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotEqualTo(Integer value) {
            addCriterion("is_delete <>", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThan(Integer value) {
            addCriterion("is_delete >", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_delete >=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThan(Integer value) {
            addCriterion("is_delete <", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThanOrEqualTo(Integer value) {
            addCriterion("is_delete <=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIn(List<Integer> values) {
            addCriterion("is_delete in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotIn(List<Integer> values) {
            addCriterion("is_delete not in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteBetween(Integer value1, Integer value2) {
            addCriterion("is_delete between", value1, value2, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotBetween(Integer value1, Integer value2) {
            addCriterion("is_delete not between", value1, value2, "isDelete");
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

        public Criteria andActualNumberIsNull() {
            addCriterion("actual_number is null");
            return (Criteria) this;
        }

        public Criteria andActualNumberIsNotNull() {
            addCriterion("actual_number is not null");
            return (Criteria) this;
        }

        public Criteria andActualNumberEqualTo(Integer value) {
            addCriterion("actual_number =", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotEqualTo(Integer value) {
            addCriterion("actual_number <>", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberGreaterThan(Integer value) {
            addCriterion("actual_number >", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_number >=", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberLessThan(Integer value) {
            addCriterion("actual_number <", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberLessThanOrEqualTo(Integer value) {
            addCriterion("actual_number <=", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberIn(List<Integer> values) {
            addCriterion("actual_number in", values, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotIn(List<Integer> values) {
            addCriterion("actual_number not in", values, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberBetween(Integer value1, Integer value2) {
            addCriterion("actual_number between", value1, value2, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_number not between", value1, value2, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberIsNull() {
            addCriterion("discreet_number is null");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberIsNotNull() {
            addCriterion("discreet_number is not null");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberEqualTo(Integer value) {
            addCriterion("discreet_number =", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberNotEqualTo(Integer value) {
            addCriterion("discreet_number <>", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberGreaterThan(Integer value) {
            addCriterion("discreet_number >", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("discreet_number >=", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberLessThan(Integer value) {
            addCriterion("discreet_number <", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberLessThanOrEqualTo(Integer value) {
            addCriterion("discreet_number <=", value, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberIn(List<Integer> values) {
            addCriterion("discreet_number in", values, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberNotIn(List<Integer> values) {
            addCriterion("discreet_number not in", values, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberBetween(Integer value1, Integer value2) {
            addCriterion("discreet_number between", value1, value2, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andDiscreetNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("discreet_number not between", value1, value2, "discreetNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdIsNull() {
            addCriterion("colliding_data_task_id is null");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdIsNotNull() {
            addCriterion("colliding_data_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdEqualTo(Long value) {
            addCriterion("colliding_data_task_id =", value, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdNotEqualTo(Long value) {
            addCriterion("colliding_data_task_id <>", value, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdGreaterThan(Long value) {
            addCriterion("colliding_data_task_id >", value, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdGreaterThanOrEqualTo(Long value) {
            addCriterion("colliding_data_task_id >=", value, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdLessThan(Long value) {
            addCriterion("colliding_data_task_id <", value, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdLessThanOrEqualTo(Long value) {
            addCriterion("colliding_data_task_id <=", value, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdIn(List<Long> values) {
            addCriterion("colliding_data_task_id in", values, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdNotIn(List<Long> values) {
            addCriterion("colliding_data_task_id not in", values, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdBetween(Long value1, Long value2) {
            addCriterion("colliding_data_task_id between", value1, value2, "collidingDataTaskId");
            return (Criteria) this;
        }

        public Criteria andCollidingDataTaskIdNotBetween(Long value1, Long value2) {
            addCriterion("colliding_data_task_id not between", value1, value2, "collidingDataTaskId");
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