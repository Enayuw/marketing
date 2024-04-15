package com.br.marketing.entity.dataProcess;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataProcessingConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DataProcessingConfigExample() {
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

        public Criteria andFileHeaderIsNull() {
            addCriterion("file_header is null");
            return (Criteria) this;
        }

        public Criteria andFileHeaderIsNotNull() {
            addCriterion("file_header is not null");
            return (Criteria) this;
        }

        public Criteria andFileHeaderEqualTo(String value) {
            addCriterion("file_header =", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotEqualTo(String value) {
            addCriterion("file_header <>", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderGreaterThan(String value) {
            addCriterion("file_header >", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderGreaterThanOrEqualTo(String value) {
            addCriterion("file_header >=", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderLessThan(String value) {
            addCriterion("file_header <", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderLessThanOrEqualTo(String value) {
            addCriterion("file_header <=", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderLike(String value) {
            addCriterion("file_header like", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotLike(String value) {
            addCriterion("file_header not like", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderIn(List<String> values) {
            addCriterion("file_header in", values, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotIn(List<String> values) {
            addCriterion("file_header not in", values, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderBetween(String value1, String value2) {
            addCriterion("file_header between", value1, value2, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotBetween(String value1, String value2) {
            addCriterion("file_header not between", value1, value2, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andDataSplitIsNull() {
            addCriterion("data_split is null");
            return (Criteria) this;
        }

        public Criteria andDataSplitIsNotNull() {
            addCriterion("data_split is not null");
            return (Criteria) this;
        }

        public Criteria andDataSplitEqualTo(String value) {
            addCriterion("data_split =", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitNotEqualTo(String value) {
            addCriterion("data_split <>", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitGreaterThan(String value) {
            addCriterion("data_split >", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitGreaterThanOrEqualTo(String value) {
            addCriterion("data_split >=", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitLessThan(String value) {
            addCriterion("data_split <", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitLessThanOrEqualTo(String value) {
            addCriterion("data_split <=", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitLike(String value) {
            addCriterion("data_split like", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitNotLike(String value) {
            addCriterion("data_split not like", value, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitIn(List<String> values) {
            addCriterion("data_split in", values, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitNotIn(List<String> values) {
            addCriterion("data_split not in", values, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitBetween(String value1, String value2) {
            addCriterion("data_split between", value1, value2, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andDataSplitNotBetween(String value1, String value2) {
            addCriterion("data_split not between", value1, value2, "dataSplit");
            return (Criteria) this;
        }

        public Criteria andProxyNameIsNull() {
            addCriterion("proxy_name is null");
            return (Criteria) this;
        }

        public Criteria andProxyNameIsNotNull() {
            addCriterion("proxy_name is not null");
            return (Criteria) this;
        }

        public Criteria andProxyNameEqualTo(String value) {
            addCriterion("proxy_name =", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameNotEqualTo(String value) {
            addCriterion("proxy_name <>", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameGreaterThan(String value) {
            addCriterion("proxy_name >", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameGreaterThanOrEqualTo(String value) {
            addCriterion("proxy_name >=", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameLessThan(String value) {
            addCriterion("proxy_name <", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameLessThanOrEqualTo(String value) {
            addCriterion("proxy_name <=", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameLike(String value) {
            addCriterion("proxy_name like", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameNotLike(String value) {
            addCriterion("proxy_name not like", value, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameIn(List<String> values) {
            addCriterion("proxy_name in", values, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameNotIn(List<String> values) {
            addCriterion("proxy_name not in", values, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameBetween(String value1, String value2) {
            addCriterion("proxy_name between", value1, value2, "proxyName");
            return (Criteria) this;
        }

        public Criteria andProxyNameNotBetween(String value1, String value2) {
            addCriterion("proxy_name not between", value1, value2, "proxyName");
            return (Criteria) this;
        }

        public Criteria andUrlIsNull() {
            addCriterion("url is null");
            return (Criteria) this;
        }

        public Criteria andUrlIsNotNull() {
            addCriterion("url is not null");
            return (Criteria) this;
        }

        public Criteria andUrlEqualTo(String value) {
            addCriterion("url =", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotEqualTo(String value) {
            addCriterion("url <>", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlGreaterThan(String value) {
            addCriterion("url >", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlGreaterThanOrEqualTo(String value) {
            addCriterion("url >=", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLessThan(String value) {
            addCriterion("url <", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLessThanOrEqualTo(String value) {
            addCriterion("url <=", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlLike(String value) {
            addCriterion("url like", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotLike(String value) {
            addCriterion("url not like", value, "url");
            return (Criteria) this;
        }

        public Criteria andUrlIn(List<String> values) {
            addCriterion("url in", values, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotIn(List<String> values) {
            addCriterion("url not in", values, "url");
            return (Criteria) this;
        }

        public Criteria andUrlBetween(String value1, String value2) {
            addCriterion("url between", value1, value2, "url");
            return (Criteria) this;
        }

        public Criteria andUrlNotBetween(String value1, String value2) {
            addCriterion("url not between", value1, value2, "url");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelIsNull() {
            addCriterion("priority_level is null");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelIsNotNull() {
            addCriterion("priority_level is not null");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelEqualTo(Byte value) {
            addCriterion("priority_level =", value, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelNotEqualTo(Byte value) {
            addCriterion("priority_level <>", value, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelGreaterThan(Byte value) {
            addCriterion("priority_level >", value, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelGreaterThanOrEqualTo(Byte value) {
            addCriterion("priority_level >=", value, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelLessThan(Byte value) {
            addCriterion("priority_level <", value, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelLessThanOrEqualTo(Byte value) {
            addCriterion("priority_level <=", value, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelIn(List<Byte> values) {
            addCriterion("priority_level in", values, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelNotIn(List<Byte> values) {
            addCriterion("priority_level not in", values, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelBetween(Byte value1, Byte value2) {
            addCriterion("priority_level between", value1, value2, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andPriorityLevelNotBetween(Byte value1, Byte value2) {
            addCriterion("priority_level not between", value1, value2, "priorityLevel");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNull() {
            addCriterion("is_deleted is null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNotNull() {
            addCriterion("is_deleted is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedEqualTo(Integer value) {
            addCriterion("is_deleted =", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotEqualTo(Integer value) {
            addCriterion("is_deleted <>", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThan(Integer value) {
            addCriterion("is_deleted >", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_deleted >=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThan(Integer value) {
            addCriterion("is_deleted <", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThanOrEqualTo(Integer value) {
            addCriterion("is_deleted <=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIn(List<Integer> values) {
            addCriterion("is_deleted in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotIn(List<Integer> values) {
            addCriterion("is_deleted not in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted between", value1, value2, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted not between", value1, value2, "isDeleted");
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