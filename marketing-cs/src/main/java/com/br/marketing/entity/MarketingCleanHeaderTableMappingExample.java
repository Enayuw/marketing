package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingCleanHeaderTableMappingExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingCleanHeaderTableMappingExample() {
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

        public Criteria andSyncConfigIdIsNull() {
            addCriterion("sync_config_id is null");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdIsNotNull() {
            addCriterion("sync_config_id is not null");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdEqualTo(Long value) {
            addCriterion("sync_config_id =", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdNotEqualTo(Long value) {
            addCriterion("sync_config_id <>", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdGreaterThan(Long value) {
            addCriterion("sync_config_id >", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sync_config_id >=", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdLessThan(Long value) {
            addCriterion("sync_config_id <", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdLessThanOrEqualTo(Long value) {
            addCriterion("sync_config_id <=", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdIn(List<Long> values) {
            addCriterion("sync_config_id in", values, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdNotIn(List<Long> values) {
            addCriterion("sync_config_id not in", values, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdBetween(Long value1, Long value2) {
            addCriterion("sync_config_id between", value1, value2, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdNotBetween(Long value1, Long value2) {
            addCriterion("sync_config_id not between", value1, value2, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaIsNull() {
            addCriterion("header_schema is null");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaIsNotNull() {
            addCriterion("header_schema is not null");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaEqualTo(String value) {
            addCriterion("header_schema =", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaNotEqualTo(String value) {
            addCriterion("header_schema <>", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaGreaterThan(String value) {
            addCriterion("header_schema >", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaGreaterThanOrEqualTo(String value) {
            addCriterion("header_schema >=", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaLessThan(String value) {
            addCriterion("header_schema <", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaLessThanOrEqualTo(String value) {
            addCriterion("header_schema <=", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaLike(String value) {
            addCriterion("header_schema like", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaNotLike(String value) {
            addCriterion("header_schema not like", value, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaIn(List<String> values) {
            addCriterion("header_schema in", values, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaNotIn(List<String> values) {
            addCriterion("header_schema not in", values, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaBetween(String value1, String value2) {
            addCriterion("header_schema between", value1, value2, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSchemaNotBetween(String value1, String value2) {
            addCriterion("header_schema not between", value1, value2, "headerSchema");
            return (Criteria) this;
        }

        public Criteria andHeaderSignIsNull() {
            addCriterion("header_sign is null");
            return (Criteria) this;
        }

        public Criteria andHeaderSignIsNotNull() {
            addCriterion("header_sign is not null");
            return (Criteria) this;
        }

        public Criteria andHeaderSignEqualTo(String value) {
            addCriterion("header_sign =", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignNotEqualTo(String value) {
            addCriterion("header_sign <>", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignGreaterThan(String value) {
            addCriterion("header_sign >", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignGreaterThanOrEqualTo(String value) {
            addCriterion("header_sign >=", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignLessThan(String value) {
            addCriterion("header_sign <", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignLessThanOrEqualTo(String value) {
            addCriterion("header_sign <=", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignLike(String value) {
            addCriterion("header_sign like", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignNotLike(String value) {
            addCriterion("header_sign not like", value, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignIn(List<String> values) {
            addCriterion("header_sign in", values, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignNotIn(List<String> values) {
            addCriterion("header_sign not in", values, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignBetween(String value1, String value2) {
            addCriterion("header_sign between", value1, value2, "headerSign");
            return (Criteria) this;
        }

        public Criteria andHeaderSignNotBetween(String value1, String value2) {
            addCriterion("header_sign not between", value1, value2, "headerSign");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnIsNull() {
            addCriterion("column_schema_en is null");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnIsNotNull() {
            addCriterion("column_schema_en is not null");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnEqualTo(String value) {
            addCriterion("column_schema_en =", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnNotEqualTo(String value) {
            addCriterion("column_schema_en <>", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnGreaterThan(String value) {
            addCriterion("column_schema_en >", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnGreaterThanOrEqualTo(String value) {
            addCriterion("column_schema_en >=", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnLessThan(String value) {
            addCriterion("column_schema_en <", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnLessThanOrEqualTo(String value) {
            addCriterion("column_schema_en <=", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnLike(String value) {
            addCriterion("column_schema_en like", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnNotLike(String value) {
            addCriterion("column_schema_en not like", value, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnIn(List<String> values) {
            addCriterion("column_schema_en in", values, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnNotIn(List<String> values) {
            addCriterion("column_schema_en not in", values, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnBetween(String value1, String value2) {
            addCriterion("column_schema_en between", value1, value2, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andColumnSchemaEnNotBetween(String value1, String value2) {
            addCriterion("column_schema_en not between", value1, value2, "columnSchemaEn");
            return (Criteria) this;
        }

        public Criteria andTableNameIsNull() {
            addCriterion("`table_name` is null");
            return (Criteria) this;
        }

        public Criteria andTableNameIsNotNull() {
            addCriterion("`table_name` is not null");
            return (Criteria) this;
        }

        public Criteria andTableNameEqualTo(String value) {
            addCriterion("`table_name` =", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameNotEqualTo(String value) {
            addCriterion("`table_name` <>", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameGreaterThan(String value) {
            addCriterion("`table_name` >", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameGreaterThanOrEqualTo(String value) {
            addCriterion("`table_name` >=", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameLessThan(String value) {
            addCriterion("`table_name` <", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameLessThanOrEqualTo(String value) {
            addCriterion("`table_name` <=", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameLike(String value) {
            addCriterion("`table_name` like", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameNotLike(String value) {
            addCriterion("`table_name` not like", value, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameIn(List<String> values) {
            addCriterion("`table_name` in", values, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameNotIn(List<String> values) {
            addCriterion("`table_name` not in", values, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameBetween(String value1, String value2) {
            addCriterion("`table_name` between", value1, value2, "tableName");
            return (Criteria) this;
        }

        public Criteria andTableNameNotBetween(String value1, String value2) {
            addCriterion("`table_name` not between", value1, value2, "tableName");
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
