package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScoreStatisticsDetailExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ScoreStatisticsDetailExample() {
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

        public Criteria andStatisticsIdIsNull() {
            addCriterion("statistics_id is null");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdIsNotNull() {
            addCriterion("statistics_id is not null");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdEqualTo(Long value) {
            addCriterion("statistics_id =", value, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdNotEqualTo(Long value) {
            addCriterion("statistics_id <>", value, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdGreaterThan(Long value) {
            addCriterion("statistics_id >", value, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdGreaterThanOrEqualTo(Long value) {
            addCriterion("statistics_id >=", value, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdLessThan(Long value) {
            addCriterion("statistics_id <", value, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdLessThanOrEqualTo(Long value) {
            addCriterion("statistics_id <=", value, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdIn(List<Long> values) {
            addCriterion("statistics_id in", values, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdNotIn(List<Long> values) {
            addCriterion("statistics_id not in", values, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdBetween(Long value1, Long value2) {
            addCriterion("statistics_id between", value1, value2, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andStatisticsIdNotBetween(Long value1, Long value2) {
            addCriterion("statistics_id not between", value1, value2, "statisticsId");
            return (Criteria) this;
        }

        public Criteria andFieldXValueIsNull() {
            addCriterion("field_x_value is null");
            return (Criteria) this;
        }

        public Criteria andFieldXValueIsNotNull() {
            addCriterion("field_x_value is not null");
            return (Criteria) this;
        }

        public Criteria andFieldXValueEqualTo(String value) {
            addCriterion("field_x_value =", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueNotEqualTo(String value) {
            addCriterion("field_x_value <>", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueGreaterThan(String value) {
            addCriterion("field_x_value >", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueGreaterThanOrEqualTo(String value) {
            addCriterion("field_x_value >=", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueLessThan(String value) {
            addCriterion("field_x_value <", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueLessThanOrEqualTo(String value) {
            addCriterion("field_x_value <=", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueLike(String value) {
            addCriterion("field_x_value like", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueNotLike(String value) {
            addCriterion("field_x_value not like", value, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueIn(List<String> values) {
            addCriterion("field_x_value in", values, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueNotIn(List<String> values) {
            addCriterion("field_x_value not in", values, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueBetween(String value1, String value2) {
            addCriterion("field_x_value between", value1, value2, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldXValueNotBetween(String value1, String value2) {
            addCriterion("field_x_value not between", value1, value2, "fieldXValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueIsNull() {
            addCriterion("field_y_value is null");
            return (Criteria) this;
        }

        public Criteria andFieldYValueIsNotNull() {
            addCriterion("field_y_value is not null");
            return (Criteria) this;
        }

        public Criteria andFieldYValueEqualTo(String value) {
            addCriterion("field_y_value =", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueNotEqualTo(String value) {
            addCriterion("field_y_value <>", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueGreaterThan(String value) {
            addCriterion("field_y_value >", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueGreaterThanOrEqualTo(String value) {
            addCriterion("field_y_value >=", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueLessThan(String value) {
            addCriterion("field_y_value <", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueLessThanOrEqualTo(String value) {
            addCriterion("field_y_value <=", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueLike(String value) {
            addCriterion("field_y_value like", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueNotLike(String value) {
            addCriterion("field_y_value not like", value, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueIn(List<String> values) {
            addCriterion("field_y_value in", values, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueNotIn(List<String> values) {
            addCriterion("field_y_value not in", values, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueBetween(String value1, String value2) {
            addCriterion("field_y_value between", value1, value2, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldYValueNotBetween(String value1, String value2) {
            addCriterion("field_y_value not between", value1, value2, "fieldYValue");
            return (Criteria) this;
        }

        public Criteria andFieldNumIsNull() {
            addCriterion("field_num is null");
            return (Criteria) this;
        }

        public Criteria andFieldNumIsNotNull() {
            addCriterion("field_num is not null");
            return (Criteria) this;
        }

        public Criteria andFieldNumEqualTo(Integer value) {
            addCriterion("field_num =", value, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumNotEqualTo(Integer value) {
            addCriterion("field_num <>", value, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumGreaterThan(Integer value) {
            addCriterion("field_num >", value, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("field_num >=", value, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumLessThan(Integer value) {
            addCriterion("field_num <", value, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumLessThanOrEqualTo(Integer value) {
            addCriterion("field_num <=", value, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumIn(List<Integer> values) {
            addCriterion("field_num in", values, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumNotIn(List<Integer> values) {
            addCriterion("field_num not in", values, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumBetween(Integer value1, Integer value2) {
            addCriterion("field_num between", value1, value2, "fieldNum");
            return (Criteria) this;
        }

        public Criteria andFieldNumNotBetween(Integer value1, Integer value2) {
            addCriterion("field_num not between", value1, value2, "fieldNum");
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