package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileDbConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FileDbConfigExample() {
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

        public Criteria andSftpConfigIdIsNull() {
            addCriterion("sftp_config_id is null");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdIsNotNull() {
            addCriterion("sftp_config_id is not null");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdEqualTo(Long value) {
            addCriterion("sftp_config_id =", value, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdNotEqualTo(Long value) {
            addCriterion("sftp_config_id <>", value, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdGreaterThan(Long value) {
            addCriterion("sftp_config_id >", value, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sftp_config_id >=", value, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdLessThan(Long value) {
            addCriterion("sftp_config_id <", value, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdLessThanOrEqualTo(Long value) {
            addCriterion("sftp_config_id <=", value, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdIn(List<Long> values) {
            addCriterion("sftp_config_id in", values, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdNotIn(List<Long> values) {
            addCriterion("sftp_config_id not in", values, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdBetween(Long value1, Long value2) {
            addCriterion("sftp_config_id between", value1, value2, "sftpConfigId");
            return (Criteria) this;
        }

        public Criteria andSftpConfigIdNotBetween(Long value1, Long value2) {
            addCriterion("sftp_config_id not between", value1, value2, "sftpConfigId");
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

        public Criteria andInnerPathIsNull() {
            addCriterion("inner_path is null");
            return (Criteria) this;
        }

        public Criteria andInnerPathIsNotNull() {
            addCriterion("inner_path is not null");
            return (Criteria) this;
        }

        public Criteria andInnerPathEqualTo(String value) {
            addCriterion("inner_path =", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathNotEqualTo(String value) {
            addCriterion("inner_path <>", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathGreaterThan(String value) {
            addCriterion("inner_path >", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathGreaterThanOrEqualTo(String value) {
            addCriterion("inner_path >=", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathLessThan(String value) {
            addCriterion("inner_path <", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathLessThanOrEqualTo(String value) {
            addCriterion("inner_path <=", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathLike(String value) {
            addCriterion("inner_path like", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathNotLike(String value) {
            addCriterion("inner_path not like", value, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathIn(List<String> values) {
            addCriterion("inner_path in", values, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathNotIn(List<String> values) {
            addCriterion("inner_path not in", values, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathBetween(String value1, String value2) {
            addCriterion("inner_path between", value1, value2, "innerPath");
            return (Criteria) this;
        }

        public Criteria andInnerPathNotBetween(String value1, String value2) {
            addCriterion("inner_path not between", value1, value2, "innerPath");
            return (Criteria) this;
        }

        public Criteria andDbNameIsNull() {
            addCriterion("db_name is null");
            return (Criteria) this;
        }

        public Criteria andDbNameIsNotNull() {
            addCriterion("db_name is not null");
            return (Criteria) this;
        }

        public Criteria andDbNameEqualTo(String value) {
            addCriterion("db_name =", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameNotEqualTo(String value) {
            addCriterion("db_name <>", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameGreaterThan(String value) {
            addCriterion("db_name >", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameGreaterThanOrEqualTo(String value) {
            addCriterion("db_name >=", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameLessThan(String value) {
            addCriterion("db_name <", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameLessThanOrEqualTo(String value) {
            addCriterion("db_name <=", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameLike(String value) {
            addCriterion("db_name like", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameNotLike(String value) {
            addCriterion("db_name not like", value, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameIn(List<String> values) {
            addCriterion("db_name in", values, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameNotIn(List<String> values) {
            addCriterion("db_name not in", values, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameBetween(String value1, String value2) {
            addCriterion("db_name between", value1, value2, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbNameNotBetween(String value1, String value2) {
            addCriterion("db_name not between", value1, value2, "dbName");
            return (Criteria) this;
        }

        public Criteria andDbFieldsIsNull() {
            addCriterion("db_fields is null");
            return (Criteria) this;
        }

        public Criteria andDbFieldsIsNotNull() {
            addCriterion("db_fields is not null");
            return (Criteria) this;
        }

        public Criteria andDbFieldsEqualTo(String value) {
            addCriterion("db_fields =", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsNotEqualTo(String value) {
            addCriterion("db_fields <>", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsGreaterThan(String value) {
            addCriterion("db_fields >", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsGreaterThanOrEqualTo(String value) {
            addCriterion("db_fields >=", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsLessThan(String value) {
            addCriterion("db_fields <", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsLessThanOrEqualTo(String value) {
            addCriterion("db_fields <=", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsLike(String value) {
            addCriterion("db_fields like", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsNotLike(String value) {
            addCriterion("db_fields not like", value, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsIn(List<String> values) {
            addCriterion("db_fields in", values, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsNotIn(List<String> values) {
            addCriterion("db_fields not in", values, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsBetween(String value1, String value2) {
            addCriterion("db_fields between", value1, value2, "dbFields");
            return (Criteria) this;
        }

        public Criteria andDbFieldsNotBetween(String value1, String value2) {
            addCriterion("db_fields not between", value1, value2, "dbFields");
            return (Criteria) this;
        }

        public Criteria andRouteKeyIsNull() {
            addCriterion("route_key is null");
            return (Criteria) this;
        }

        public Criteria andRouteKeyIsNotNull() {
            addCriterion("route_key is not null");
            return (Criteria) this;
        }

        public Criteria andRouteKeyEqualTo(String value) {
            addCriterion("route_key =", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyNotEqualTo(String value) {
            addCriterion("route_key <>", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyGreaterThan(String value) {
            addCriterion("route_key >", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyGreaterThanOrEqualTo(String value) {
            addCriterion("route_key >=", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyLessThan(String value) {
            addCriterion("route_key <", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyLessThanOrEqualTo(String value) {
            addCriterion("route_key <=", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyLike(String value) {
            addCriterion("route_key like", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyNotLike(String value) {
            addCriterion("route_key not like", value, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyIn(List<String> values) {
            addCriterion("route_key in", values, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyNotIn(List<String> values) {
            addCriterion("route_key not in", values, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyBetween(String value1, String value2) {
            addCriterion("route_key between", value1, value2, "routeKey");
            return (Criteria) this;
        }

        public Criteria andRouteKeyNotBetween(String value1, String value2) {
            addCriterion("route_key not between", value1, value2, "routeKey");
            return (Criteria) this;
        }

        public Criteria andFileTypeIsNull() {
            addCriterion("file_type is null");
            return (Criteria) this;
        }

        public Criteria andFileTypeIsNotNull() {
            addCriterion("file_type is not null");
            return (Criteria) this;
        }

        public Criteria andFileTypeEqualTo(String value) {
            addCriterion("file_type =", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotEqualTo(String value) {
            addCriterion("file_type <>", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeGreaterThan(String value) {
            addCriterion("file_type >", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeGreaterThanOrEqualTo(String value) {
            addCriterion("file_type >=", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeLessThan(String value) {
            addCriterion("file_type <", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeLessThanOrEqualTo(String value) {
            addCriterion("file_type <=", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeLike(String value) {
            addCriterion("file_type like", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotLike(String value) {
            addCriterion("file_type not like", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeIn(List<String> values) {
            addCriterion("file_type in", values, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotIn(List<String> values) {
            addCriterion("file_type not in", values, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeBetween(String value1, String value2) {
            addCriterion("file_type between", value1, value2, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotBetween(String value1, String value2) {
            addCriterion("file_type not between", value1, value2, "fileType");
            return (Criteria) this;
        }

        public Criteria andDelIsNull() {
            addCriterion("del is null");
            return (Criteria) this;
        }

        public Criteria andDelIsNotNull() {
            addCriterion("del is not null");
            return (Criteria) this;
        }

        public Criteria andDelEqualTo(Integer value) {
            addCriterion("del =", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelNotEqualTo(Integer value) {
            addCriterion("del <>", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelGreaterThan(Integer value) {
            addCriterion("del >", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelGreaterThanOrEqualTo(Integer value) {
            addCriterion("del >=", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelLessThan(Integer value) {
            addCriterion("del <", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelLessThanOrEqualTo(Integer value) {
            addCriterion("del <=", value, "del");
            return (Criteria) this;
        }

        public Criteria andDelIn(List<Integer> values) {
            addCriterion("del in", values, "del");
            return (Criteria) this;
        }

        public Criteria andDelNotIn(List<Integer> values) {
            addCriterion("del not in", values, "del");
            return (Criteria) this;
        }

        public Criteria andDelBetween(Integer value1, Integer value2) {
            addCriterion("del between", value1, value2, "del");
            return (Criteria) this;
        }

        public Criteria andDelNotBetween(Integer value1, Integer value2) {
            addCriterion("del not between", value1, value2, "del");
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