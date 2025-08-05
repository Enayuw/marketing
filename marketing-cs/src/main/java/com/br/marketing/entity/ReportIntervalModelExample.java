package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportIntervalModelExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ReportIntervalModelExample() {
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

        public Criteria andConfigIdIsNull() {
            addCriterion("config_id is null");
            return (Criteria) this;
        }

        public Criteria andConfigIdIsNotNull() {
            addCriterion("config_id is not null");
            return (Criteria) this;
        }

        public Criteria andConfigIdEqualTo(Long value) {
            addCriterion("config_id =", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdNotEqualTo(Long value) {
            addCriterion("config_id <>", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdGreaterThan(Long value) {
            addCriterion("config_id >", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdGreaterThanOrEqualTo(Long value) {
            addCriterion("config_id >=", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdLessThan(Long value) {
            addCriterion("config_id <", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdLessThanOrEqualTo(Long value) {
            addCriterion("config_id <=", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdIn(List<Long> values) {
            addCriterion("config_id in", values, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdNotIn(List<Long> values) {
            addCriterion("config_id not in", values, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdBetween(Long value1, Long value2) {
            addCriterion("config_id between", value1, value2, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdNotBetween(Long value1, Long value2) {
            addCriterion("config_id not between", value1, value2, "configId");
            return (Criteria) this;
        }

        public Criteria andAxisTypeIsNull() {
            addCriterion("axis_type is null");
            return (Criteria) this;
        }

        public Criteria andAxisTypeIsNotNull() {
            addCriterion("axis_type is not null");
            return (Criteria) this;
        }

        public Criteria andAxisTypeEqualTo(String value) {
            addCriterion("axis_type =", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeNotEqualTo(String value) {
            addCriterion("axis_type <>", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeGreaterThan(String value) {
            addCriterion("axis_type >", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeGreaterThanOrEqualTo(String value) {
            addCriterion("axis_type >=", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeLessThan(String value) {
            addCriterion("axis_type <", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeLessThanOrEqualTo(String value) {
            addCriterion("axis_type <=", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeLike(String value) {
            addCriterion("axis_type like", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeNotLike(String value) {
            addCriterion("axis_type not like", value, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeIn(List<String> values) {
            addCriterion("axis_type in", values, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeNotIn(List<String> values) {
            addCriterion("axis_type not in", values, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeBetween(String value1, String value2) {
            addCriterion("axis_type between", value1, value2, "axisType");
            return (Criteria) this;
        }

        public Criteria andAxisTypeNotBetween(String value1, String value2) {
            addCriterion("axis_type not between", value1, value2, "axisType");
            return (Criteria) this;
        }

        public Criteria andXModelNameIsNull() {
            addCriterion("x_model_name is null");
            return (Criteria) this;
        }

        public Criteria andXModelNameIsNotNull() {
            addCriterion("x_model_name is not null");
            return (Criteria) this;
        }

        public Criteria andXModelNameEqualTo(String value) {
            addCriterion("x_model_name =", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameNotEqualTo(String value) {
            addCriterion("x_model_name <>", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameGreaterThan(String value) {
            addCriterion("x_model_name >", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameGreaterThanOrEqualTo(String value) {
            addCriterion("x_model_name >=", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameLessThan(String value) {
            addCriterion("x_model_name <", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameLessThanOrEqualTo(String value) {
            addCriterion("x_model_name <=", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameLike(String value) {
            addCriterion("x_model_name like", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameNotLike(String value) {
            addCriterion("x_model_name not like", value, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameIn(List<String> values) {
            addCriterion("x_model_name in", values, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameNotIn(List<String> values) {
            addCriterion("x_model_name not in", values, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameBetween(String value1, String value2) {
            addCriterion("x_model_name between", value1, value2, "xModelName");
            return (Criteria) this;
        }

        public Criteria andXModelNameNotBetween(String value1, String value2) {
            addCriterion("x_model_name not between", value1, value2, "xModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameIsNull() {
            addCriterion("y_model_name is null");
            return (Criteria) this;
        }

        public Criteria andYModelNameIsNotNull() {
            addCriterion("y_model_name is not null");
            return (Criteria) this;
        }

        public Criteria andYModelNameEqualTo(String value) {
            addCriterion("y_model_name =", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameNotEqualTo(String value) {
            addCriterion("y_model_name <>", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameGreaterThan(String value) {
            addCriterion("y_model_name >", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameGreaterThanOrEqualTo(String value) {
            addCriterion("y_model_name >=", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameLessThan(String value) {
            addCriterion("y_model_name <", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameLessThanOrEqualTo(String value) {
            addCriterion("y_model_name <=", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameLike(String value) {
            addCriterion("y_model_name like", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameNotLike(String value) {
            addCriterion("y_model_name not like", value, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameIn(List<String> values) {
            addCriterion("y_model_name in", values, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameNotIn(List<String> values) {
            addCriterion("y_model_name not in", values, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameBetween(String value1, String value2) {
            addCriterion("y_model_name between", value1, value2, "yModelName");
            return (Criteria) this;
        }

        public Criteria andYModelNameNotBetween(String value1, String value2) {
            addCriterion("y_model_name not between", value1, value2, "yModelName");
            return (Criteria) this;
        }

        public Criteria andXIntervalListIsNull() {
            addCriterion("x_interval_list is null");
            return (Criteria) this;
        }

        public Criteria andXIntervalListIsNotNull() {
            addCriterion("x_interval_list is not null");
            return (Criteria) this;
        }

        public Criteria andXIntervalListEqualTo(String value) {
            addCriterion("x_interval_list =", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListNotEqualTo(String value) {
            addCriterion("x_interval_list <>", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListGreaterThan(String value) {
            addCriterion("x_interval_list >", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListGreaterThanOrEqualTo(String value) {
            addCriterion("x_interval_list >=", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListLessThan(String value) {
            addCriterion("x_interval_list <", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListLessThanOrEqualTo(String value) {
            addCriterion("x_interval_list <=", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListLike(String value) {
            addCriterion("x_interval_list like", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListNotLike(String value) {
            addCriterion("x_interval_list not like", value, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListIn(List<String> values) {
            addCriterion("x_interval_list in", values, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListNotIn(List<String> values) {
            addCriterion("x_interval_list not in", values, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListBetween(String value1, String value2) {
            addCriterion("x_interval_list between", value1, value2, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andXIntervalListNotBetween(String value1, String value2) {
            addCriterion("x_interval_list not between", value1, value2, "xIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListIsNull() {
            addCriterion("y_interval_list is null");
            return (Criteria) this;
        }

        public Criteria andYIntervalListIsNotNull() {
            addCriterion("y_interval_list is not null");
            return (Criteria) this;
        }

        public Criteria andYIntervalListEqualTo(String value) {
            addCriterion("y_interval_list =", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListNotEqualTo(String value) {
            addCriterion("y_interval_list <>", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListGreaterThan(String value) {
            addCriterion("y_interval_list >", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListGreaterThanOrEqualTo(String value) {
            addCriterion("y_interval_list >=", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListLessThan(String value) {
            addCriterion("y_interval_list <", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListLessThanOrEqualTo(String value) {
            addCriterion("y_interval_list <=", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListLike(String value) {
            addCriterion("y_interval_list like", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListNotLike(String value) {
            addCriterion("y_interval_list not like", value, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListIn(List<String> values) {
            addCriterion("y_interval_list in", values, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListNotIn(List<String> values) {
            addCriterion("y_interval_list not in", values, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListBetween(String value1, String value2) {
            addCriterion("y_interval_list between", value1, value2, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andYIntervalListNotBetween(String value1, String value2) {
            addCriterion("y_interval_list not between", value1, value2, "yIntervalList");
            return (Criteria) this;
        }

        public Criteria andOrderIsNull() {
            addCriterion("`order` is null");
            return (Criteria) this;
        }

        public Criteria andOrderIsNotNull() {
            addCriterion("`order` is not null");
            return (Criteria) this;
        }

        public Criteria andOrderEqualTo(String value) {
            addCriterion("`order` =", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotEqualTo(String value) {
            addCriterion("`order` <>", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderGreaterThan(String value) {
            addCriterion("`order` >", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderGreaterThanOrEqualTo(String value) {
            addCriterion("`order` >=", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLessThan(String value) {
            addCriterion("`order` <", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLessThanOrEqualTo(String value) {
            addCriterion("`order` <=", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLike(String value) {
            addCriterion("`order` like", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotLike(String value) {
            addCriterion("`order` not like", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderIn(List<String> values) {
            addCriterion("`order` in", values, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotIn(List<String> values) {
            addCriterion("`order` not in", values, "order");
            return (Criteria) this;
        }

        public Criteria andOrderBetween(String value1, String value2) {
            addCriterion("`order` between", value1, value2, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotBetween(String value1, String value2) {
            addCriterion("`order` not between", value1, value2, "order");
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