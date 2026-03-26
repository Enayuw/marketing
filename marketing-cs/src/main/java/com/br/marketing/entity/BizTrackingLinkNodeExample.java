package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BizTrackingLinkNodeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public BizTrackingLinkNodeExample() {
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

        public Criteria andLinkIdIsNull() {
            addCriterion("link_id is null");
            return (Criteria) this;
        }

        public Criteria andLinkIdIsNotNull() {
            addCriterion("link_id is not null");
            return (Criteria) this;
        }

        public Criteria andLinkIdEqualTo(Long value) {
            addCriterion("link_id =", value, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdNotEqualTo(Long value) {
            addCriterion("link_id <>", value, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdGreaterThan(Long value) {
            addCriterion("link_id >", value, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdGreaterThanOrEqualTo(Long value) {
            addCriterion("link_id >=", value, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdLessThan(Long value) {
            addCriterion("link_id <", value, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdLessThanOrEqualTo(Long value) {
            addCriterion("link_id <=", value, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdIn(List<Long> values) {
            addCriterion("link_id in", values, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdNotIn(List<Long> values) {
            addCriterion("link_id not in", values, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdBetween(Long value1, Long value2) {
            addCriterion("link_id between", value1, value2, "linkId");
            return (Criteria) this;
        }

        public Criteria andLinkIdNotBetween(Long value1, Long value2) {
            addCriterion("link_id not between", value1, value2, "linkId");
            return (Criteria) this;
        }

        public Criteria andNodeIdIsNull() {
            addCriterion("node_id is null");
            return (Criteria) this;
        }

        public Criteria andNodeIdIsNotNull() {
            addCriterion("node_id is not null");
            return (Criteria) this;
        }

        public Criteria andNodeIdEqualTo(Long value) {
            addCriterion("node_id =", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotEqualTo(Long value) {
            addCriterion("node_id <>", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdGreaterThan(Long value) {
            addCriterion("node_id >", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdGreaterThanOrEqualTo(Long value) {
            addCriterion("node_id >=", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLessThan(Long value) {
            addCriterion("node_id <", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdLessThanOrEqualTo(Long value) {
            addCriterion("node_id <=", value, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdIn(List<Long> values) {
            addCriterion("node_id in", values, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotIn(List<Long> values) {
            addCriterion("node_id not in", values, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdBetween(Long value1, Long value2) {
            addCriterion("node_id between", value1, value2, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeIdNotBetween(Long value1, Long value2) {
            addCriterion("node_id not between", value1, value2, "nodeId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdIsNull() {
            addCriterion("node_dict_id is null");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdIsNotNull() {
            addCriterion("node_dict_id is not null");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdEqualTo(Long value) {
            addCriterion("node_dict_id =", value, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdNotEqualTo(Long value) {
            addCriterion("node_dict_id <>", value, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdGreaterThan(Long value) {
            addCriterion("node_dict_id >", value, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdGreaterThanOrEqualTo(Long value) {
            addCriterion("node_dict_id >=", value, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdLessThan(Long value) {
            addCriterion("node_dict_id <", value, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdLessThanOrEqualTo(Long value) {
            addCriterion("node_dict_id <=", value, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdIn(List<Long> values) {
            addCriterion("node_dict_id in", values, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdNotIn(List<Long> values) {
            addCriterion("node_dict_id not in", values, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdBetween(Long value1, Long value2) {
            addCriterion("node_dict_id between", value1, value2, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeDictIdNotBetween(Long value1, Long value2) {
            addCriterion("node_dict_id not between", value1, value2, "nodeDictId");
            return (Criteria) this;
        }

        public Criteria andNodeAliasIsNull() {
            addCriterion("node_alias is null");
            return (Criteria) this;
        }

        public Criteria andNodeAliasIsNotNull() {
            addCriterion("node_alias is not null");
            return (Criteria) this;
        }

        public Criteria andNodeAliasEqualTo(String value) {
            addCriterion("node_alias =", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasNotEqualTo(String value) {
            addCriterion("node_alias <>", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasGreaterThan(String value) {
            addCriterion("node_alias >", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasGreaterThanOrEqualTo(String value) {
            addCriterion("node_alias >=", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasLessThan(String value) {
            addCriterion("node_alias <", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasLessThanOrEqualTo(String value) {
            addCriterion("node_alias <=", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasLike(String value) {
            addCriterion("node_alias like", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasNotLike(String value) {
            addCriterion("node_alias not like", value, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasIn(List<String> values) {
            addCriterion("node_alias in", values, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasNotIn(List<String> values) {
            addCriterion("node_alias not in", values, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasBetween(String value1, String value2) {
            addCriterion("node_alias between", value1, value2, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andNodeAliasNotBetween(String value1, String value2) {
            addCriterion("node_alias not between", value1, value2, "nodeAlias");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeIsNull() {
            addCriterion("edge_type is null");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeIsNotNull() {
            addCriterion("edge_type is not null");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeEqualTo(String value) {
            addCriterion("edge_type =", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeNotEqualTo(String value) {
            addCriterion("edge_type <>", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeGreaterThan(String value) {
            addCriterion("edge_type >", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeGreaterThanOrEqualTo(String value) {
            addCriterion("edge_type >=", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeLessThan(String value) {
            addCriterion("edge_type <", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeLessThanOrEqualTo(String value) {
            addCriterion("edge_type <=", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeLike(String value) {
            addCriterion("edge_type like", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeNotLike(String value) {
            addCriterion("edge_type not like", value, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeIn(List<String> values) {
            addCriterion("edge_type in", values, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeNotIn(List<String> values) {
            addCriterion("edge_type not in", values, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeBetween(String value1, String value2) {
            addCriterion("edge_type between", value1, value2, "edgeType");
            return (Criteria) this;
        }

        public Criteria andEdgeTypeNotBetween(String value1, String value2) {
            addCriterion("edge_type not between", value1, value2, "edgeType");
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

        public Criteria andCreatedTimeIsNull() {
            addCriterion("created_time is null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIsNotNull() {
            addCriterion("created_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeEqualTo(Date value) {
            addCriterion("created_time =", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotEqualTo(Date value) {
            addCriterion("created_time <>", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThan(Date value) {
            addCriterion("created_time >", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("created_time >=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThan(Date value) {
            addCriterion("created_time <", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThanOrEqualTo(Date value) {
            addCriterion("created_time <=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIn(List<Date> values) {
            addCriterion("created_time in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotIn(List<Date> values) {
            addCriterion("created_time not in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeBetween(Date value1, Date value2) {
            addCriterion("created_time between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotBetween(Date value1, Date value2) {
            addCriterion("created_time not between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeIsNull() {
            addCriterion("updated_time is null");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeIsNotNull() {
            addCriterion("updated_time is not null");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeEqualTo(Date value) {
            addCriterion("updated_time =", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeNotEqualTo(Date value) {
            addCriterion("updated_time <>", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeGreaterThan(Date value) {
            addCriterion("updated_time >", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("updated_time >=", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeLessThan(Date value) {
            addCriterion("updated_time <", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeLessThanOrEqualTo(Date value) {
            addCriterion("updated_time <=", value, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeIn(List<Date> values) {
            addCriterion("updated_time in", values, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeNotIn(List<Date> values) {
            addCriterion("updated_time not in", values, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeBetween(Date value1, Date value2) {
            addCriterion("updated_time between", value1, value2, "updatedTime");
            return (Criteria) this;
        }

        public Criteria andUpdatedTimeNotBetween(Date value1, Date value2) {
            addCriterion("updated_time not between", value1, value2, "updatedTime");
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