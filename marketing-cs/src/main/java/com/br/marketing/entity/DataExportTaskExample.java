package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataExportTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DataExportTaskExample() {
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

        public Criteria andTaskNameIsNull() {
            addCriterion("task_name is null");
            return (Criteria) this;
        }

        public Criteria andTaskNameIsNotNull() {
            addCriterion("task_name is not null");
            return (Criteria) this;
        }

        public Criteria andTaskNameEqualTo(String value) {
            addCriterion("task_name =", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotEqualTo(String value) {
            addCriterion("task_name <>", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThan(String value) {
            addCriterion("task_name >", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameGreaterThanOrEqualTo(String value) {
            addCriterion("task_name >=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThan(String value) {
            addCriterion("task_name <", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLessThanOrEqualTo(String value) {
            addCriterion("task_name <=", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameLike(String value) {
            addCriterion("task_name like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotLike(String value) {
            addCriterion("task_name not like", value, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameIn(List<String> values) {
            addCriterion("task_name in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotIn(List<String> values) {
            addCriterion("task_name not in", values, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameBetween(String value1, String value2) {
            addCriterion("task_name between", value1, value2, "taskName");
            return (Criteria) this;
        }

        public Criteria andTaskNameNotBetween(String value1, String value2) {
            addCriterion("task_name not between", value1, value2, "taskName");
            return (Criteria) this;
        }

        public Criteria andDataSourceIsNull() {
            addCriterion("data_source is null");
            return (Criteria) this;
        }

        public Criteria andDataSourceIsNotNull() {
            addCriterion("data_source is not null");
            return (Criteria) this;
        }

        public Criteria andDataSourceEqualTo(String value) {
            addCriterion("data_source =", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotEqualTo(String value) {
            addCriterion("data_source <>", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceGreaterThan(String value) {
            addCriterion("data_source >", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceGreaterThanOrEqualTo(String value) {
            addCriterion("data_source >=", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceLessThan(String value) {
            addCriterion("data_source <", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceLessThanOrEqualTo(String value) {
            addCriterion("data_source <=", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceLike(String value) {
            addCriterion("data_source like", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotLike(String value) {
            addCriterion("data_source not like", value, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceIn(List<String> values) {
            addCriterion("data_source in", values, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotIn(List<String> values) {
            addCriterion("data_source not in", values, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceBetween(String value1, String value2) {
            addCriterion("data_source between", value1, value2, "dataSource");
            return (Criteria) this;
        }

        public Criteria andDataSourceNotBetween(String value1, String value2) {
            addCriterion("data_source not between", value1, value2, "dataSource");
            return (Criteria) this;
        }

        public Criteria andExportHeadersIsNull() {
            addCriterion("export_headers is null");
            return (Criteria) this;
        }

        public Criteria andExportHeadersIsNotNull() {
            addCriterion("export_headers is not null");
            return (Criteria) this;
        }

        public Criteria andExportHeadersEqualTo(String value) {
            addCriterion("export_headers =", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersNotEqualTo(String value) {
            addCriterion("export_headers <>", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersGreaterThan(String value) {
            addCriterion("export_headers >", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersGreaterThanOrEqualTo(String value) {
            addCriterion("export_headers >=", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersLessThan(String value) {
            addCriterion("export_headers <", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersLessThanOrEqualTo(String value) {
            addCriterion("export_headers <=", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersLike(String value) {
            addCriterion("export_headers like", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersNotLike(String value) {
            addCriterion("export_headers not like", value, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersIn(List<String> values) {
            addCriterion("export_headers in", values, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersNotIn(List<String> values) {
            addCriterion("export_headers not in", values, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersBetween(String value1, String value2) {
            addCriterion("export_headers between", value1, value2, "exportHeaders");
            return (Criteria) this;
        }

        public Criteria andExportHeadersNotBetween(String value1, String value2) {
            addCriterion("export_headers not between", value1, value2, "exportHeaders");
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

        public Criteria andQueryConditionIsNull() {
            addCriterion("query_condition is null");
            return (Criteria) this;
        }

        public Criteria andQueryConditionIsNotNull() {
            addCriterion("query_condition is not null");
            return (Criteria) this;
        }

        public Criteria andQueryConditionEqualTo(String value) {
            addCriterion("query_condition =", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionNotEqualTo(String value) {
            addCriterion("query_condition <>", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionGreaterThan(String value) {
            addCriterion("query_condition >", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionGreaterThanOrEqualTo(String value) {
            addCriterion("query_condition >=", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionLessThan(String value) {
            addCriterion("query_condition <", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionLessThanOrEqualTo(String value) {
            addCriterion("query_condition <=", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionLike(String value) {
            addCriterion("query_condition like", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionNotLike(String value) {
            addCriterion("query_condition not like", value, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionIn(List<String> values) {
            addCriterion("query_condition in", values, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionNotIn(List<String> values) {
            addCriterion("query_condition not in", values, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionBetween(String value1, String value2) {
            addCriterion("query_condition between", value1, value2, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andQueryConditionNotBetween(String value1, String value2) {
            addCriterion("query_condition not between", value1, value2, "queryCondition");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsIsNull() {
            addCriterion("estimated_rows is null");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsIsNotNull() {
            addCriterion("estimated_rows is not null");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsEqualTo(Long value) {
            addCriterion("estimated_rows =", value, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsNotEqualTo(Long value) {
            addCriterion("estimated_rows <>", value, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsGreaterThan(Long value) {
            addCriterion("estimated_rows >", value, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsGreaterThanOrEqualTo(Long value) {
            addCriterion("estimated_rows >=", value, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsLessThan(Long value) {
            addCriterion("estimated_rows <", value, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsLessThanOrEqualTo(Long value) {
            addCriterion("estimated_rows <=", value, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsIn(List<Long> values) {
            addCriterion("estimated_rows in", values, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsNotIn(List<Long> values) {
            addCriterion("estimated_rows not in", values, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsBetween(Long value1, Long value2) {
            addCriterion("estimated_rows between", value1, value2, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andEstimatedRowsNotBetween(Long value1, Long value2) {
            addCriterion("estimated_rows not between", value1, value2, "estimatedRows");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateIsNull() {
            addCriterion("file_name_template is null");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateIsNotNull() {
            addCriterion("file_name_template is not null");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateEqualTo(String value) {
            addCriterion("file_name_template =", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateNotEqualTo(String value) {
            addCriterion("file_name_template <>", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateGreaterThan(String value) {
            addCriterion("file_name_template >", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateGreaterThanOrEqualTo(String value) {
            addCriterion("file_name_template >=", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateLessThan(String value) {
            addCriterion("file_name_template <", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateLessThanOrEqualTo(String value) {
            addCriterion("file_name_template <=", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateLike(String value) {
            addCriterion("file_name_template like", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateNotLike(String value) {
            addCriterion("file_name_template not like", value, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateIn(List<String> values) {
            addCriterion("file_name_template in", values, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateNotIn(List<String> values) {
            addCriterion("file_name_template not in", values, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateBetween(String value1, String value2) {
            addCriterion("file_name_template between", value1, value2, "fileNameTemplate");
            return (Criteria) this;
        }

        public Criteria andFileNameTemplateNotBetween(String value1, String value2) {
            addCriterion("file_name_template not between", value1, value2, "fileNameTemplate");
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

        public Criteria andCreateByIsNull() {
            addCriterion("create_by is null");
            return (Criteria) this;
        }

        public Criteria andCreateByIsNotNull() {
            addCriterion("create_by is not null");
            return (Criteria) this;
        }

        public Criteria andCreateByEqualTo(String value) {
            addCriterion("create_by =", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotEqualTo(String value) {
            addCriterion("create_by <>", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByGreaterThan(String value) {
            addCriterion("create_by >", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByGreaterThanOrEqualTo(String value) {
            addCriterion("create_by >=", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLessThan(String value) {
            addCriterion("create_by <", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLessThanOrEqualTo(String value) {
            addCriterion("create_by <=", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByLike(String value) {
            addCriterion("create_by like", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotLike(String value) {
            addCriterion("create_by not like", value, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByIn(List<String> values) {
            addCriterion("create_by in", values, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotIn(List<String> values) {
            addCriterion("create_by not in", values, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByBetween(String value1, String value2) {
            addCriterion("create_by between", value1, value2, "createBy");
            return (Criteria) this;
        }

        public Criteria andCreateByNotBetween(String value1, String value2) {
            addCriterion("create_by not between", value1, value2, "createBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByIsNull() {
            addCriterion("update_by is null");
            return (Criteria) this;
        }

        public Criteria andUpdateByIsNotNull() {
            addCriterion("update_by is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateByEqualTo(String value) {
            addCriterion("update_by =", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByNotEqualTo(String value) {
            addCriterion("update_by <>", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByGreaterThan(String value) {
            addCriterion("update_by >", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByGreaterThanOrEqualTo(String value) {
            addCriterion("update_by >=", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByLessThan(String value) {
            addCriterion("update_by <", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByLessThanOrEqualTo(String value) {
            addCriterion("update_by <=", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByLike(String value) {
            addCriterion("update_by like", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByNotLike(String value) {
            addCriterion("update_by not like", value, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByIn(List<String> values) {
            addCriterion("update_by in", values, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByNotIn(List<String> values) {
            addCriterion("update_by not in", values, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByBetween(String value1, String value2) {
            addCriterion("update_by between", value1, value2, "updateBy");
            return (Criteria) this;
        }

        public Criteria andUpdateByNotBetween(String value1, String value2) {
            addCriterion("update_by not between", value1, value2, "updateBy");
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