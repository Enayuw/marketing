package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingBuildInTemplateJsonParseExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingBuildInTemplateJsonParseExample() {
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

        public Criteria andSystemTypeIsNull() {
            addCriterion("system_type is null");
            return (Criteria) this;
        }

        public Criteria andSystemTypeIsNotNull() {
            addCriterion("system_type is not null");
            return (Criteria) this;
        }

        public Criteria andSystemTypeEqualTo(Integer value) {
            addCriterion("system_type =", value, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeNotEqualTo(Integer value) {
            addCriterion("system_type <>", value, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeGreaterThan(Integer value) {
            addCriterion("system_type >", value, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("system_type >=", value, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeLessThan(Integer value) {
            addCriterion("system_type <", value, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeLessThanOrEqualTo(Integer value) {
            addCriterion("system_type <=", value, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeIn(List<Integer> values) {
            addCriterion("system_type in", values, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeNotIn(List<Integer> values) {
            addCriterion("system_type not in", values, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeBetween(Integer value1, Integer value2) {
            addCriterion("system_type between", value1, value2, "systemType");
            return (Criteria) this;
        }

        public Criteria andSystemTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("system_type not between", value1, value2, "systemType");
            return (Criteria) this;
        }

        public Criteria andDataTypeIsNull() {
            addCriterion("data_type is null");
            return (Criteria) this;
        }

        public Criteria andDataTypeIsNotNull() {
            addCriterion("data_type is not null");
            return (Criteria) this;
        }

        public Criteria andDataTypeEqualTo(Integer value) {
            addCriterion("data_type =", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotEqualTo(Integer value) {
            addCriterion("data_type <>", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeGreaterThan(Integer value) {
            addCriterion("data_type >", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("data_type >=", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeLessThan(Integer value) {
            addCriterion("data_type <", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeLessThanOrEqualTo(Integer value) {
            addCriterion("data_type <=", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeIn(List<Integer> values) {
            addCriterion("data_type in", values, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotIn(List<Integer> values) {
            addCriterion("data_type not in", values, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeBetween(Integer value1, Integer value2) {
            addCriterion("data_type between", value1, value2, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("data_type not between", value1, value2, "dataType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeIsNull() {
            addCriterion("accept_type is null");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeIsNotNull() {
            addCriterion("accept_type is not null");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeEqualTo(Integer value) {
            addCriterion("accept_type =", value, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeNotEqualTo(Integer value) {
            addCriterion("accept_type <>", value, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeGreaterThan(Integer value) {
            addCriterion("accept_type >", value, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("accept_type >=", value, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeLessThan(Integer value) {
            addCriterion("accept_type <", value, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeLessThanOrEqualTo(Integer value) {
            addCriterion("accept_type <=", value, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeIn(List<Integer> values) {
            addCriterion("accept_type in", values, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeNotIn(List<Integer> values) {
            addCriterion("accept_type not in", values, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeBetween(Integer value1, Integer value2) {
            addCriterion("accept_type between", value1, value2, "acceptType");
            return (Criteria) this;
        }

        public Criteria andAcceptTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("accept_type not between", value1, value2, "acceptType");
            return (Criteria) this;
        }

        public Criteria andNodeNameIsNull() {
            addCriterion("node_name is null");
            return (Criteria) this;
        }

        public Criteria andNodeNameIsNotNull() {
            addCriterion("node_name is not null");
            return (Criteria) this;
        }

        public Criteria andNodeNameEqualTo(String value) {
            addCriterion("node_name =", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameNotEqualTo(String value) {
            addCriterion("node_name <>", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameGreaterThan(String value) {
            addCriterion("node_name >", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameGreaterThanOrEqualTo(String value) {
            addCriterion("node_name >=", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameLessThan(String value) {
            addCriterion("node_name <", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameLessThanOrEqualTo(String value) {
            addCriterion("node_name <=", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameLike(String value) {
            addCriterion("node_name like", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameNotLike(String value) {
            addCriterion("node_name not like", value, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameIn(List<String> values) {
            addCriterion("node_name in", values, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameNotIn(List<String> values) {
            addCriterion("node_name not in", values, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameBetween(String value1, String value2) {
            addCriterion("node_name between", value1, value2, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeNameNotBetween(String value1, String value2) {
            addCriterion("node_name not between", value1, value2, "nodeName");
            return (Criteria) this;
        }

        public Criteria andNodeValueIsNull() {
            addCriterion("node_value is null");
            return (Criteria) this;
        }

        public Criteria andNodeValueIsNotNull() {
            addCriterion("node_value is not null");
            return (Criteria) this;
        }

        public Criteria andNodeValueEqualTo(String value) {
            addCriterion("node_value =", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueNotEqualTo(String value) {
            addCriterion("node_value <>", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueGreaterThan(String value) {
            addCriterion("node_value >", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueGreaterThanOrEqualTo(String value) {
            addCriterion("node_value >=", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueLessThan(String value) {
            addCriterion("node_value <", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueLessThanOrEqualTo(String value) {
            addCriterion("node_value <=", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueLike(String value) {
            addCriterion("node_value like", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueNotLike(String value) {
            addCriterion("node_value not like", value, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueIn(List<String> values) {
            addCriterion("node_value in", values, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueNotIn(List<String> values) {
            addCriterion("node_value not in", values, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueBetween(String value1, String value2) {
            addCriterion("node_value between", value1, value2, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andNodeValueNotBetween(String value1, String value2) {
            addCriterion("node_value not between", value1, value2, "nodeValue");
            return (Criteria) this;
        }

        public Criteria andParentPathIsNull() {
            addCriterion("parent_path is null");
            return (Criteria) this;
        }

        public Criteria andParentPathIsNotNull() {
            addCriterion("parent_path is not null");
            return (Criteria) this;
        }

        public Criteria andParentPathEqualTo(String value) {
            addCriterion("parent_path =", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathNotEqualTo(String value) {
            addCriterion("parent_path <>", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathGreaterThan(String value) {
            addCriterion("parent_path >", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathGreaterThanOrEqualTo(String value) {
            addCriterion("parent_path >=", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathLessThan(String value) {
            addCriterion("parent_path <", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathLessThanOrEqualTo(String value) {
            addCriterion("parent_path <=", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathLike(String value) {
            addCriterion("parent_path like", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathNotLike(String value) {
            addCriterion("parent_path not like", value, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathIn(List<String> values) {
            addCriterion("parent_path in", values, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathNotIn(List<String> values) {
            addCriterion("parent_path not in", values, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathBetween(String value1, String value2) {
            addCriterion("parent_path between", value1, value2, "parentPath");
            return (Criteria) this;
        }

        public Criteria andParentPathNotBetween(String value1, String value2) {
            addCriterion("parent_path not between", value1, value2, "parentPath");
            return (Criteria) this;
        }

        public Criteria andNodeTypeIsNull() {
            addCriterion("node_type is null");
            return (Criteria) this;
        }

        public Criteria andNodeTypeIsNotNull() {
            addCriterion("node_type is not null");
            return (Criteria) this;
        }

        public Criteria andNodeTypeEqualTo(String value) {
            addCriterion("node_type =", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeNotEqualTo(String value) {
            addCriterion("node_type <>", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeGreaterThan(String value) {
            addCriterion("node_type >", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeGreaterThanOrEqualTo(String value) {
            addCriterion("node_type >=", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeLessThan(String value) {
            addCriterion("node_type <", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeLessThanOrEqualTo(String value) {
            addCriterion("node_type <=", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeLike(String value) {
            addCriterion("node_type like", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeNotLike(String value) {
            addCriterion("node_type not like", value, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeIn(List<String> values) {
            addCriterion("node_type in", values, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeNotIn(List<String> values) {
            addCriterion("node_type not in", values, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeBetween(String value1, String value2) {
            addCriterion("node_type between", value1, value2, "nodeType");
            return (Criteria) this;
        }

        public Criteria andNodeTypeNotBetween(String value1, String value2) {
            addCriterion("node_type not between", value1, value2, "nodeType");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemIsNull() {
            addCriterion("is_array_item is null");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemIsNotNull() {
            addCriterion("is_array_item is not null");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemEqualTo(Boolean value) {
            addCriterion("is_array_item =", value, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemNotEqualTo(Boolean value) {
            addCriterion("is_array_item <>", value, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemGreaterThan(Boolean value) {
            addCriterion("is_array_item >", value, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemGreaterThanOrEqualTo(Boolean value) {
            addCriterion("is_array_item >=", value, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemLessThan(Boolean value) {
            addCriterion("is_array_item <", value, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemLessThanOrEqualTo(Boolean value) {
            addCriterion("is_array_item <=", value, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemIn(List<Boolean> values) {
            addCriterion("is_array_item in", values, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemNotIn(List<Boolean> values) {
            addCriterion("is_array_item not in", values, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemBetween(Boolean value1, Boolean value2) {
            addCriterion("is_array_item between", value1, value2, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andIsArrayItemNotBetween(Boolean value1, Boolean value2) {
            addCriterion("is_array_item not between", value1, value2, "isArrayItem");
            return (Criteria) this;
        }

        public Criteria andLevelIsNull() {
            addCriterion("`level` is null");
            return (Criteria) this;
        }

        public Criteria andLevelIsNotNull() {
            addCriterion("`level` is not null");
            return (Criteria) this;
        }

        public Criteria andLevelEqualTo(Integer value) {
            addCriterion("`level` =", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotEqualTo(Integer value) {
            addCriterion("`level` <>", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThan(Integer value) {
            addCriterion("`level` >", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelGreaterThanOrEqualTo(Integer value) {
            addCriterion("`level` >=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThan(Integer value) {
            addCriterion("`level` <", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelLessThanOrEqualTo(Integer value) {
            addCriterion("`level` <=", value, "level");
            return (Criteria) this;
        }

        public Criteria andLevelIn(List<Integer> values) {
            addCriterion("`level` in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotIn(List<Integer> values) {
            addCriterion("`level` not in", values, "level");
            return (Criteria) this;
        }

        public Criteria andLevelBetween(Integer value1, Integer value2) {
            addCriterion("`level` between", value1, value2, "level");
            return (Criteria) this;
        }

        public Criteria andLevelNotBetween(Integer value1, Integer value2) {
            addCriterion("`level` not between", value1, value2, "level");
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