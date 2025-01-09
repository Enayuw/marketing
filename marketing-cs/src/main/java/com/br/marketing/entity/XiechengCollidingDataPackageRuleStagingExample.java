package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XiechengCollidingDataPackageRuleStagingExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public XiechengCollidingDataPackageRuleStagingExample() {
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

        public Criteria andCollidingBackNumberIsNull() {
            addCriterion("colliding_back_number is null");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberIsNotNull() {
            addCriterion("colliding_back_number is not null");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberEqualTo(Integer value) {
            addCriterion("colliding_back_number =", value, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberNotEqualTo(Integer value) {
            addCriterion("colliding_back_number <>", value, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberGreaterThan(Integer value) {
            addCriterion("colliding_back_number >", value, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("colliding_back_number >=", value, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberLessThan(Integer value) {
            addCriterion("colliding_back_number <", value, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberLessThanOrEqualTo(Integer value) {
            addCriterion("colliding_back_number <=", value, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberIn(List<Integer> values) {
            addCriterion("colliding_back_number in", values, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberNotIn(List<Integer> values) {
            addCriterion("colliding_back_number not in", values, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberBetween(Integer value1, Integer value2) {
            addCriterion("colliding_back_number between", value1, value2, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingBackNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("colliding_back_number not between", value1, value2, "collidingBackNumber");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeIsNull() {
            addCriterion("colliding_start_time is null");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeIsNotNull() {
            addCriterion("colliding_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeEqualTo(Date value) {
            addCriterion("colliding_start_time =", value, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeNotEqualTo(Date value) {
            addCriterion("colliding_start_time <>", value, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeGreaterThan(Date value) {
            addCriterion("colliding_start_time >", value, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("colliding_start_time >=", value, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeLessThan(Date value) {
            addCriterion("colliding_start_time <", value, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("colliding_start_time <=", value, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeIn(List<Date> values) {
            addCriterion("colliding_start_time in", values, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeNotIn(List<Date> values) {
            addCriterion("colliding_start_time not in", values, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeBetween(Date value1, Date value2) {
            addCriterion("colliding_start_time between", value1, value2, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("colliding_start_time not between", value1, value2, "collidingStartTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeIsNull() {
            addCriterion("colliding_end_time is null");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeIsNotNull() {
            addCriterion("colliding_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeEqualTo(Date value) {
            addCriterion("colliding_end_time =", value, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeNotEqualTo(Date value) {
            addCriterion("colliding_end_time <>", value, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeGreaterThan(Date value) {
            addCriterion("colliding_end_time >", value, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("colliding_end_time >=", value, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeLessThan(Date value) {
            addCriterion("colliding_end_time <", value, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("colliding_end_time <=", value, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeIn(List<Date> values) {
            addCriterion("colliding_end_time in", values, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeNotIn(List<Date> values) {
            addCriterion("colliding_end_time not in", values, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeBetween(Date value1, Date value2) {
            addCriterion("colliding_end_time between", value1, value2, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andCollidingEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("colliding_end_time not between", value1, value2, "collidingEndTime");
            return (Criteria) this;
        }

        public Criteria andStartTimesIsNull() {
            addCriterion("start_times is null");
            return (Criteria) this;
        }

        public Criteria andStartTimesIsNotNull() {
            addCriterion("start_times is not null");
            return (Criteria) this;
        }

        public Criteria andStartTimesEqualTo(String value) {
            addCriterion("start_times =", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesNotEqualTo(String value) {
            addCriterion("start_times <>", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesGreaterThan(String value) {
            addCriterion("start_times >", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesGreaterThanOrEqualTo(String value) {
            addCriterion("start_times >=", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesLessThan(String value) {
            addCriterion("start_times <", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesLessThanOrEqualTo(String value) {
            addCriterion("start_times <=", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesLike(String value) {
            addCriterion("start_times like", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesNotLike(String value) {
            addCriterion("start_times not like", value, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesIn(List<String> values) {
            addCriterion("start_times in", values, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesNotIn(List<String> values) {
            addCriterion("start_times not in", values, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesBetween(String value1, String value2) {
            addCriterion("start_times between", value1, value2, "startTimes");
            return (Criteria) this;
        }

        public Criteria andStartTimesNotBetween(String value1, String value2) {
            addCriterion("start_times not between", value1, value2, "startTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesIsNull() {
            addCriterion("colliding_times is null");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesIsNotNull() {
            addCriterion("colliding_times is not null");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesEqualTo(Integer value) {
            addCriterion("colliding_times =", value, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesNotEqualTo(Integer value) {
            addCriterion("colliding_times <>", value, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesGreaterThan(Integer value) {
            addCriterion("colliding_times >", value, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("colliding_times >=", value, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesLessThan(Integer value) {
            addCriterion("colliding_times <", value, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesLessThanOrEqualTo(Integer value) {
            addCriterion("colliding_times <=", value, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesIn(List<Integer> values) {
            addCriterion("colliding_times in", values, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesNotIn(List<Integer> values) {
            addCriterion("colliding_times not in", values, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesBetween(Integer value1, Integer value2) {
            addCriterion("colliding_times between", value1, value2, "collidingTimes");
            return (Criteria) this;
        }

        public Criteria andCollidingTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("colliding_times not between", value1, value2, "collidingTimes");
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