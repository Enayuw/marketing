package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReportFieldMappingExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ReportFieldMappingExample() {
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

        public Criteria andReportTaskIdIsNull() {
            addCriterion("report_task_id is null");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdIsNotNull() {
            addCriterion("report_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdEqualTo(String value) {
            addCriterion("report_task_id =", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotEqualTo(String value) {
            addCriterion("report_task_id <>", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdGreaterThan(String value) {
            addCriterion("report_task_id >", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("report_task_id >=", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdLessThan(String value) {
            addCriterion("report_task_id <", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdLessThanOrEqualTo(String value) {
            addCriterion("report_task_id <=", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdLike(String value) {
            addCriterion("report_task_id like", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotLike(String value) {
            addCriterion("report_task_id not like", value, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdIn(List<String> values) {
            addCriterion("report_task_id in", values, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotIn(List<String> values) {
            addCriterion("report_task_id not in", values, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdBetween(String value1, String value2) {
            addCriterion("report_task_id between", value1, value2, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andReportTaskIdNotBetween(String value1, String value2) {
            addCriterion("report_task_id not between", value1, value2, "reportTaskId");
            return (Criteria) this;
        }

        public Criteria andUserTypeIsNull() {
            addCriterion("user_type is null");
            return (Criteria) this;
        }

        public Criteria andUserTypeIsNotNull() {
            addCriterion("user_type is not null");
            return (Criteria) this;
        }

        public Criteria andUserTypeEqualTo(String value) {
            addCriterion("user_type =", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotEqualTo(String value) {
            addCriterion("user_type <>", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThan(String value) {
            addCriterion("user_type >", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThanOrEqualTo(String value) {
            addCriterion("user_type >=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThan(String value) {
            addCriterion("user_type <", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThanOrEqualTo(String value) {
            addCriterion("user_type <=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLike(String value) {
            addCriterion("user_type like", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotLike(String value) {
            addCriterion("user_type not like", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeIn(List<String> values) {
            addCriterion("user_type in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotIn(List<String> values) {
            addCriterion("user_type not in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeBetween(String value1, String value2) {
            addCriterion("user_type between", value1, value2, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotBetween(String value1, String value2) {
            addCriterion("user_type not between", value1, value2, "userType");
            return (Criteria) this;
        }

        public Criteria andItemShowIsNull() {
            addCriterion("item_show is null");
            return (Criteria) this;
        }

        public Criteria andItemShowIsNotNull() {
            addCriterion("item_show is not null");
            return (Criteria) this;
        }

        public Criteria andItemShowEqualTo(String value) {
            addCriterion("item_show =", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowNotEqualTo(String value) {
            addCriterion("item_show <>", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowGreaterThan(String value) {
            addCriterion("item_show >", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowGreaterThanOrEqualTo(String value) {
            addCriterion("item_show >=", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowLessThan(String value) {
            addCriterion("item_show <", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowLessThanOrEqualTo(String value) {
            addCriterion("item_show <=", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowLike(String value) {
            addCriterion("item_show like", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowNotLike(String value) {
            addCriterion("item_show not like", value, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowIn(List<String> values) {
            addCriterion("item_show in", values, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowNotIn(List<String> values) {
            addCriterion("item_show not in", values, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowBetween(String value1, String value2) {
            addCriterion("item_show between", value1, value2, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemShowNotBetween(String value1, String value2) {
            addCriterion("item_show not between", value1, value2, "itemShow");
            return (Criteria) this;
        }

        public Criteria andItemNameIsNull() {
            addCriterion("item_name is null");
            return (Criteria) this;
        }

        public Criteria andItemNameIsNotNull() {
            addCriterion("item_name is not null");
            return (Criteria) this;
        }

        public Criteria andItemNameEqualTo(String value) {
            addCriterion("item_name =", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameNotEqualTo(String value) {
            addCriterion("item_name <>", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameGreaterThan(String value) {
            addCriterion("item_name >", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameGreaterThanOrEqualTo(String value) {
            addCriterion("item_name >=", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameLessThan(String value) {
            addCriterion("item_name <", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameLessThanOrEqualTo(String value) {
            addCriterion("item_name <=", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameLike(String value) {
            addCriterion("item_name like", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameNotLike(String value) {
            addCriterion("item_name not like", value, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameIn(List<String> values) {
            addCriterion("item_name in", values, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameNotIn(List<String> values) {
            addCriterion("item_name not in", values, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameBetween(String value1, String value2) {
            addCriterion("item_name between", value1, value2, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemNameNotBetween(String value1, String value2) {
            addCriterion("item_name not between", value1, value2, "itemName");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeIsNull() {
            addCriterion("item_format_type is null");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeIsNotNull() {
            addCriterion("item_format_type is not null");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeEqualTo(String value) {
            addCriterion("item_format_type =", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeNotEqualTo(String value) {
            addCriterion("item_format_type <>", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeGreaterThan(String value) {
            addCriterion("item_format_type >", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeGreaterThanOrEqualTo(String value) {
            addCriterion("item_format_type >=", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeLessThan(String value) {
            addCriterion("item_format_type <", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeLessThanOrEqualTo(String value) {
            addCriterion("item_format_type <=", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeLike(String value) {
            addCriterion("item_format_type like", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeNotLike(String value) {
            addCriterion("item_format_type not like", value, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeIn(List<String> values) {
            addCriterion("item_format_type in", values, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeNotIn(List<String> values) {
            addCriterion("item_format_type not in", values, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeBetween(String value1, String value2) {
            addCriterion("item_format_type between", value1, value2, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemFormatTypeNotBetween(String value1, String value2) {
            addCriterion("item_format_type not between", value1, value2, "itemFormatType");
            return (Criteria) this;
        }

        public Criteria andItemOrderIsNull() {
            addCriterion("item_order is null");
            return (Criteria) this;
        }

        public Criteria andItemOrderIsNotNull() {
            addCriterion("item_order is not null");
            return (Criteria) this;
        }

        public Criteria andItemOrderEqualTo(Integer value) {
            addCriterion("item_order =", value, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderNotEqualTo(Integer value) {
            addCriterion("item_order <>", value, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderGreaterThan(Integer value) {
            addCriterion("item_order >", value, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderGreaterThanOrEqualTo(Integer value) {
            addCriterion("item_order >=", value, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderLessThan(Integer value) {
            addCriterion("item_order <", value, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderLessThanOrEqualTo(Integer value) {
            addCriterion("item_order <=", value, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderIn(List<Integer> values) {
            addCriterion("item_order in", values, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderNotIn(List<Integer> values) {
            addCriterion("item_order not in", values, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderBetween(Integer value1, Integer value2) {
            addCriterion("item_order between", value1, value2, "itemOrder");
            return (Criteria) this;
        }

        public Criteria andItemOrderNotBetween(Integer value1, Integer value2) {
            addCriterion("item_order not between", value1, value2, "itemOrder");
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