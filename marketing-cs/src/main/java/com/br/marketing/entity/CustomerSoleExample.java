package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerSoleExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CustomerSoleExample() {
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

        public Criteria andCustomerIdIsNull() {
            addCriterion("customer_id is null");
            return (Criteria) this;
        }

        public Criteria andCustomerIdIsNotNull() {
            addCriterion("customer_id is not null");
            return (Criteria) this;
        }

        public Criteria andCustomerIdEqualTo(Long value) {
            addCriterion("customer_id =", value, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdNotEqualTo(Long value) {
            addCriterion("customer_id <>", value, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdGreaterThan(Long value) {
            addCriterion("customer_id >", value, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdGreaterThanOrEqualTo(Long value) {
            addCriterion("customer_id >=", value, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdLessThan(Long value) {
            addCriterion("customer_id <", value, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdLessThanOrEqualTo(Long value) {
            addCriterion("customer_id <=", value, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdIn(List<Long> values) {
            addCriterion("customer_id in", values, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdNotIn(List<Long> values) {
            addCriterion("customer_id not in", values, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdBetween(Long value1, Long value2) {
            addCriterion("customer_id between", value1, value2, "customerId");
            return (Criteria) this;
        }

        public Criteria andCustomerIdNotBetween(Long value1, Long value2) {
            addCriterion("customer_id not between", value1, value2, "customerId");
            return (Criteria) this;
        }

        public Criteria andSoleIdIsNull() {
            addCriterion("sole_id is null");
            return (Criteria) this;
        }

        public Criteria andSoleIdIsNotNull() {
            addCriterion("sole_id is not null");
            return (Criteria) this;
        }

        public Criteria andSoleIdEqualTo(Long value) {
            addCriterion("sole_id =", value, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdNotEqualTo(Long value) {
            addCriterion("sole_id <>", value, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdGreaterThan(Long value) {
            addCriterion("sole_id >", value, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sole_id >=", value, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdLessThan(Long value) {
            addCriterion("sole_id <", value, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdLessThanOrEqualTo(Long value) {
            addCriterion("sole_id <=", value, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdIn(List<Long> values) {
            addCriterion("sole_id in", values, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdNotIn(List<Long> values) {
            addCriterion("sole_id not in", values, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdBetween(Long value1, Long value2) {
            addCriterion("sole_id between", value1, value2, "soleId");
            return (Criteria) this;
        }

        public Criteria andSoleIdNotBetween(Long value1, Long value2) {
            addCriterion("sole_id not between", value1, value2, "soleId");
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

        public Criteria andConditionInfoIsNull() {
            addCriterion("condition_info is null");
            return (Criteria) this;
        }

        public Criteria andConditionInfoIsNotNull() {
            addCriterion("condition_info is not null");
            return (Criteria) this;
        }

        public Criteria andConditionInfoEqualTo(String value) {
            addCriterion("condition_info =", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoNotEqualTo(String value) {
            addCriterion("condition_info <>", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoGreaterThan(String value) {
            addCriterion("condition_info >", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoGreaterThanOrEqualTo(String value) {
            addCriterion("condition_info >=", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoLessThan(String value) {
            addCriterion("condition_info <", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoLessThanOrEqualTo(String value) {
            addCriterion("condition_info <=", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoLike(String value) {
            addCriterion("condition_info like", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoNotLike(String value) {
            addCriterion("condition_info not like", value, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoIn(List<String> values) {
            addCriterion("condition_info in", values, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoNotIn(List<String> values) {
            addCriterion("condition_info not in", values, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoBetween(String value1, String value2) {
            addCriterion("condition_info between", value1, value2, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andConditionInfoNotBetween(String value1, String value2) {
            addCriterion("condition_info not between", value1, value2, "conditionInfo");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountIsNull() {
            addCriterion("user_type_count is null");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountIsNotNull() {
            addCriterion("user_type_count is not null");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountEqualTo(Integer value) {
            addCriterion("user_type_count =", value, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountNotEqualTo(Integer value) {
            addCriterion("user_type_count <>", value, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountGreaterThan(Integer value) {
            addCriterion("user_type_count >", value, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("user_type_count >=", value, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountLessThan(Integer value) {
            addCriterion("user_type_count <", value, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountLessThanOrEqualTo(Integer value) {
            addCriterion("user_type_count <=", value, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountIn(List<Integer> values) {
            addCriterion("user_type_count in", values, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountNotIn(List<Integer> values) {
            addCriterion("user_type_count not in", values, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountBetween(Integer value1, Integer value2) {
            addCriterion("user_type_count between", value1, value2, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andUserTypeCountNotBetween(Integer value1, Integer value2) {
            addCriterion("user_type_count not between", value1, value2, "userTypeCount");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeIsNull() {
            addCriterion("all_user_type is null");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeIsNotNull() {
            addCriterion("all_user_type is not null");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeEqualTo(Integer value) {
            addCriterion("all_user_type =", value, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeNotEqualTo(Integer value) {
            addCriterion("all_user_type <>", value, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeGreaterThan(Integer value) {
            addCriterion("all_user_type >", value, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("all_user_type >=", value, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeLessThan(Integer value) {
            addCriterion("all_user_type <", value, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeLessThanOrEqualTo(Integer value) {
            addCriterion("all_user_type <=", value, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeIn(List<Integer> values) {
            addCriterion("all_user_type in", values, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeNotIn(List<Integer> values) {
            addCriterion("all_user_type not in", values, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeBetween(Integer value1, Integer value2) {
            addCriterion("all_user_type between", value1, value2, "allUserType");
            return (Criteria) this;
        }

        public Criteria andAllUserTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("all_user_type not between", value1, value2, "allUserType");
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