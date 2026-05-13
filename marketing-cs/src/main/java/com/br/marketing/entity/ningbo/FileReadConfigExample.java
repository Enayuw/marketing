package com.br.marketing.entity.ningbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileReadConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FileReadConfigExample() {
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

        public Criteria andConfigNameIsNull() {
            addCriterion("config_name is null");
            return (Criteria) this;
        }

        public Criteria andConfigNameIsNotNull() {
            addCriterion("config_name is not null");
            return (Criteria) this;
        }

        public Criteria andConfigNameEqualTo(String value) {
            addCriterion("config_name =", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameNotEqualTo(String value) {
            addCriterion("config_name <>", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameGreaterThan(String value) {
            addCriterion("config_name >", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameGreaterThanOrEqualTo(String value) {
            addCriterion("config_name >=", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameLessThan(String value) {
            addCriterion("config_name <", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameLessThanOrEqualTo(String value) {
            addCriterion("config_name <=", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameLike(String value) {
            addCriterion("config_name like", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameNotLike(String value) {
            addCriterion("config_name not like", value, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameIn(List<String> values) {
            addCriterion("config_name in", values, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameNotIn(List<String> values) {
            addCriterion("config_name not in", values, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameBetween(String value1, String value2) {
            addCriterion("config_name between", value1, value2, "configName");
            return (Criteria) this;
        }

        public Criteria andConfigNameNotBetween(String value1, String value2) {
            addCriterion("config_name not between", value1, value2, "configName");
            return (Criteria) this;
        }

        public Criteria andFieldMappingIsNull() {
            addCriterion("field_mapping is null");
            return (Criteria) this;
        }

        public Criteria andFieldMappingIsNotNull() {
            addCriterion("field_mapping is not null");
            return (Criteria) this;
        }

        public Criteria andFieldMappingEqualTo(String value) {
            addCriterion("field_mapping =", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingNotEqualTo(String value) {
            addCriterion("field_mapping <>", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingGreaterThan(String value) {
            addCriterion("field_mapping >", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingGreaterThanOrEqualTo(String value) {
            addCriterion("field_mapping >=", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingLessThan(String value) {
            addCriterion("field_mapping <", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingLessThanOrEqualTo(String value) {
            addCriterion("field_mapping <=", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingLike(String value) {
            addCriterion("field_mapping like", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingNotLike(String value) {
            addCriterion("field_mapping not like", value, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingIn(List<String> values) {
            addCriterion("field_mapping in", values, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingNotIn(List<String> values) {
            addCriterion("field_mapping not in", values, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingBetween(String value1, String value2) {
            addCriterion("field_mapping between", value1, value2, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFieldMappingNotBetween(String value1, String value2) {
            addCriterion("field_mapping not between", value1, value2, "fieldMapping");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorIsNull() {
            addCriterion("file_separator is null");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorIsNotNull() {
            addCriterion("file_separator is not null");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorEqualTo(String value) {
            addCriterion("file_separator =", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorNotEqualTo(String value) {
            addCriterion("file_separator <>", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorGreaterThan(String value) {
            addCriterion("file_separator >", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorGreaterThanOrEqualTo(String value) {
            addCriterion("file_separator >=", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorLessThan(String value) {
            addCriterion("file_separator <", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorLessThanOrEqualTo(String value) {
            addCriterion("file_separator <=", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorLike(String value) {
            addCriterion("file_separator like", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorNotLike(String value) {
            addCriterion("file_separator not like", value, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorIn(List<String> values) {
            addCriterion("file_separator in", values, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorNotIn(List<String> values) {
            addCriterion("file_separator not in", values, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorBetween(String value1, String value2) {
            addCriterion("file_separator between", value1, value2, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileSeparatorNotBetween(String value1, String value2) {
            addCriterion("file_separator not between", value1, value2, "fileSeparator");
            return (Criteria) this;
        }

        public Criteria andFileCharsetIsNull() {
            addCriterion("file_charset is null");
            return (Criteria) this;
        }

        public Criteria andFileCharsetIsNotNull() {
            addCriterion("file_charset is not null");
            return (Criteria) this;
        }

        public Criteria andFileCharsetEqualTo(String value) {
            addCriterion("file_charset =", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetNotEqualTo(String value) {
            addCriterion("file_charset <>", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetGreaterThan(String value) {
            addCriterion("file_charset >", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetGreaterThanOrEqualTo(String value) {
            addCriterion("file_charset >=", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetLessThan(String value) {
            addCriterion("file_charset <", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetLessThanOrEqualTo(String value) {
            addCriterion("file_charset <=", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetLike(String value) {
            addCriterion("file_charset like", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetNotLike(String value) {
            addCriterion("file_charset not like", value, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetIn(List<String> values) {
            addCriterion("file_charset in", values, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetNotIn(List<String> values) {
            addCriterion("file_charset not in", values, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetBetween(String value1, String value2) {
            addCriterion("file_charset between", value1, value2, "fileCharset");
            return (Criteria) this;
        }

        public Criteria andFileCharsetNotBetween(String value1, String value2) {
            addCriterion("file_charset not between", value1, value2, "fileCharset");
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

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("`status` not between", value1, value2, "status");
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