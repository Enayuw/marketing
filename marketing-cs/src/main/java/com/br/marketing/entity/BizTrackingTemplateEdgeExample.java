package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BizTrackingTemplateEdgeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public BizTrackingTemplateEdgeExample() {
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

        public Criteria andTemplateIdIsNull() {
            addCriterion("template_id is null");
            return (Criteria) this;
        }

        public Criteria andTemplateIdIsNotNull() {
            addCriterion("template_id is not null");
            return (Criteria) this;
        }

        public Criteria andTemplateIdEqualTo(String value) {
            addCriterion("template_id =", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdNotEqualTo(String value) {
            addCriterion("template_id <>", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdGreaterThan(String value) {
            addCriterion("template_id >", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdGreaterThanOrEqualTo(String value) {
            addCriterion("template_id >=", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdLessThan(String value) {
            addCriterion("template_id <", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdLessThanOrEqualTo(String value) {
            addCriterion("template_id <=", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdLike(String value) {
            addCriterion("template_id like", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdNotLike(String value) {
            addCriterion("template_id not like", value, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdIn(List<String> values) {
            addCriterion("template_id in", values, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdNotIn(List<String> values) {
            addCriterion("template_id not in", values, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdBetween(String value1, String value2) {
            addCriterion("template_id between", value1, value2, "templateId");
            return (Criteria) this;
        }

        public Criteria andTemplateIdNotBetween(String value1, String value2) {
            addCriterion("template_id not between", value1, value2, "templateId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdIsNull() {
            addCriterion("from_node_id is null");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdIsNotNull() {
            addCriterion("from_node_id is not null");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdEqualTo(Long value) {
            addCriterion("from_node_id =", value, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdNotEqualTo(Long value) {
            addCriterion("from_node_id <>", value, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdGreaterThan(Long value) {
            addCriterion("from_node_id >", value, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdGreaterThanOrEqualTo(Long value) {
            addCriterion("from_node_id >=", value, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdLessThan(Long value) {
            addCriterion("from_node_id <", value, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdLessThanOrEqualTo(Long value) {
            addCriterion("from_node_id <=", value, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdIn(List<Long> values) {
            addCriterion("from_node_id in", values, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdNotIn(List<Long> values) {
            addCriterion("from_node_id not in", values, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdBetween(Long value1, Long value2) {
            addCriterion("from_node_id between", value1, value2, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andFromNodeIdNotBetween(Long value1, Long value2) {
            addCriterion("from_node_id not between", value1, value2, "fromNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdIsNull() {
            addCriterion("to_node_id is null");
            return (Criteria) this;
        }

        public Criteria andToNodeIdIsNotNull() {
            addCriterion("to_node_id is not null");
            return (Criteria) this;
        }

        public Criteria andToNodeIdEqualTo(Long value) {
            addCriterion("to_node_id =", value, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdNotEqualTo(Long value) {
            addCriterion("to_node_id <>", value, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdGreaterThan(Long value) {
            addCriterion("to_node_id >", value, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdGreaterThanOrEqualTo(Long value) {
            addCriterion("to_node_id >=", value, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdLessThan(Long value) {
            addCriterion("to_node_id <", value, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdLessThanOrEqualTo(Long value) {
            addCriterion("to_node_id <=", value, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdIn(List<Long> values) {
            addCriterion("to_node_id in", values, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdNotIn(List<Long> values) {
            addCriterion("to_node_id not in", values, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdBetween(Long value1, Long value2) {
            addCriterion("to_node_id between", value1, value2, "toNodeId");
            return (Criteria) this;
        }

        public Criteria andToNodeIdNotBetween(Long value1, Long value2) {
            addCriterion("to_node_id not between", value1, value2, "toNodeId");
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

        public Criteria andDescriptionIsNull() {
            addCriterion("description is null");
            return (Criteria) this;
        }

        public Criteria andDescriptionIsNotNull() {
            addCriterion("description is not null");
            return (Criteria) this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            addCriterion("description =", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotEqualTo(String value) {
            addCriterion("description <>", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThan(String value) {
            addCriterion("description >", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("description >=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThan(String value) {
            addCriterion("description <", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThanOrEqualTo(String value) {
            addCriterion("description <=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLike(String value) {
            addCriterion("description like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotLike(String value) {
            addCriterion("description not like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionIn(List<String> values) {
            addCriterion("description in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotIn(List<String> values) {
            addCriterion("description not in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionBetween(String value1, String value2) {
            addCriterion("description between", value1, value2, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotBetween(String value1, String value2) {
            addCriterion("description not between", value1, value2, "description");
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