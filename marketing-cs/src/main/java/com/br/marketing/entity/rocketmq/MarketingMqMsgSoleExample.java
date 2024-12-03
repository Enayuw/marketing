//package com.br.marketing.entity.rocketmq;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.List;
//
//public class MarketingMqMsgSoleExample {
//    protected String orderByClause;
//
//    protected boolean distinct;
//
//    protected List<Criteria> oredCriteria;
//
//    public MarketingMqMsgSoleExample() {
//        oredCriteria = new ArrayList<Criteria>();
//    }
//
//    public void setOrderByClause(String orderByClause) {
//        this.orderByClause = orderByClause;
//    }
//
//    public String getOrderByClause() {
//        return orderByClause;
//    }
//
//    public void setDistinct(boolean distinct) {
//        this.distinct = distinct;
//    }
//
//    public boolean isDistinct() {
//        return distinct;
//    }
//
//    public List<Criteria> getOredCriteria() {
//        return oredCriteria;
//    }
//
//    public void or(Criteria criteria) {
//        oredCriteria.add(criteria);
//    }
//
//    public Criteria or() {
//        Criteria criteria = createCriteriaInternal();
//        oredCriteria.add(criteria);
//        return criteria;
//    }
//
//    public Criteria createCriteria() {
//        Criteria criteria = createCriteriaInternal();
//        if (oredCriteria.size() == 0) {
//            oredCriteria.add(criteria);
//        }
//        return criteria;
//    }
//
//    protected Criteria createCriteriaInternal() {
//        Criteria criteria = new Criteria();
//        return criteria;
//    }
//
//    public void clear() {
//        oredCriteria.clear();
//        orderByClause = null;
//        distinct = false;
//    }
//
//    protected abstract static class GeneratedCriteria {
//        protected List<Criterion> criteria;
//
//        protected GeneratedCriteria() {
//            super();
//            criteria = new ArrayList<Criterion>();
//        }
//
//        public boolean isValid() {
//            return criteria.size() > 0;
//        }
//
//        public List<Criterion> getAllCriteria() {
//            return criteria;
//        }
//
//        public List<Criterion> getCriteria() {
//            return criteria;
//        }
//
//        protected void addCriterion(String condition) {
//            if (condition == null) {
//                throw new RuntimeException("Value for condition cannot be null");
//            }
//            criteria.add(new Criterion(condition));
//        }
//
//        protected void addCriterion(String condition, Object value, String property) {
//            if (value == null) {
//                throw new RuntimeException("Value for " + property + " cannot be null");
//            }
//            criteria.add(new Criterion(condition, value));
//        }
//
//        protected void addCriterion(String condition, Object value1, Object value2, String property) {
//            if (value1 == null || value2 == null) {
//                throw new RuntimeException("Between values for " + property + " cannot be null");
//            }
//            criteria.add(new Criterion(condition, value1, value2));
//        }
//
//        public Criteria andIdIsNull() {
//            addCriterion("id is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdIsNotNull() {
//            addCriterion("id is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdEqualTo(Long value) {
//            addCriterion("id =", value, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdNotEqualTo(Long value) {
//            addCriterion("id <>", value, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdGreaterThan(Long value) {
//            addCriterion("id >", value, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdGreaterThanOrEqualTo(Long value) {
//            addCriterion("id >=", value, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdLessThan(Long value) {
//            addCriterion("id <", value, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdLessThanOrEqualTo(Long value) {
//            addCriterion("id <=", value, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdIn(List<Long> values) {
//            addCriterion("id in", values, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdNotIn(List<Long> values) {
//            addCriterion("id not in", values, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdBetween(Long value1, Long value2) {
//            addCriterion("id between", value1, value2, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andIdNotBetween(Long value1, Long value2) {
//            addCriterion("id not between", value1, value2, "id");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicIsNull() {
//            addCriterion("topic is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicIsNotNull() {
//            addCriterion("topic is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicEqualTo(String value) {
//            addCriterion("topic =", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicNotEqualTo(String value) {
//            addCriterion("topic <>", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicGreaterThan(String value) {
//            addCriterion("topic >", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicGreaterThanOrEqualTo(String value) {
//            addCriterion("topic >=", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicLessThan(String value) {
//            addCriterion("topic <", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicLessThanOrEqualTo(String value) {
//            addCriterion("topic <=", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicLike(String value) {
//            addCriterion("topic like", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicNotLike(String value) {
//            addCriterion("topic not like", value, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicIn(List<String> values) {
//            addCriterion("topic in", values, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicNotIn(List<String> values) {
//            addCriterion("topic not in", values, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicBetween(String value1, String value2) {
//            addCriterion("topic between", value1, value2, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTopicNotBetween(String value1, String value2) {
//            addCriterion("topic not between", value1, value2, "topic");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsIsNull() {
//            addCriterion("tags is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsIsNotNull() {
//            addCriterion("tags is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsEqualTo(String value) {
//            addCriterion("tags =", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsNotEqualTo(String value) {
//            addCriterion("tags <>", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsGreaterThan(String value) {
//            addCriterion("tags >", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsGreaterThanOrEqualTo(String value) {
//            addCriterion("tags >=", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsLessThan(String value) {
//            addCriterion("tags <", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsLessThanOrEqualTo(String value) {
//            addCriterion("tags <=", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsLike(String value) {
//            addCriterion("tags like", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsNotLike(String value) {
//            addCriterion("tags not like", value, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsIn(List<String> values) {
//            addCriterion("tags in", values, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsNotIn(List<String> values) {
//            addCriterion("tags not in", values, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsBetween(String value1, String value2) {
//            addCriterion("tags between", value1, value2, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andTagsNotBetween(String value1, String value2) {
//            addCriterion("tags not between", value1, value2, "tags");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageIsNull() {
//            addCriterion("message is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageIsNotNull() {
//            addCriterion("message is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageEqualTo(String value) {
//            addCriterion("message =", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageNotEqualTo(String value) {
//            addCriterion("message <>", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageGreaterThan(String value) {
//            addCriterion("message >", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageGreaterThanOrEqualTo(String value) {
//            addCriterion("message >=", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageLessThan(String value) {
//            addCriterion("message <", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageLessThanOrEqualTo(String value) {
//            addCriterion("message <=", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageLike(String value) {
//            addCriterion("message like", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageNotLike(String value) {
//            addCriterion("message not like", value, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageIn(List<String> values) {
//            addCriterion("message in", values, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageNotIn(List<String> values) {
//            addCriterion("message not in", values, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageBetween(String value1, String value2) {
//            addCriterion("message between", value1, value2, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMessageNotBetween(String value1, String value2) {
//            addCriterion("message not between", value1, value2, "message");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdIsNull() {
//            addCriterion("msg_id is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdIsNotNull() {
//            addCriterion("msg_id is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdEqualTo(String value) {
//            addCriterion("msg_id =", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdNotEqualTo(String value) {
//            addCriterion("msg_id <>", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdGreaterThan(String value) {
//            addCriterion("msg_id >", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdGreaterThanOrEqualTo(String value) {
//            addCriterion("msg_id >=", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdLessThan(String value) {
//            addCriterion("msg_id <", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdLessThanOrEqualTo(String value) {
//            addCriterion("msg_id <=", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdLike(String value) {
//            addCriterion("msg_id like", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdNotLike(String value) {
//            addCriterion("msg_id not like", value, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdIn(List<String> values) {
//            addCriterion("msg_id in", values, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdNotIn(List<String> values) {
//            addCriterion("msg_id not in", values, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdBetween(String value1, String value2) {
//            addCriterion("msg_id between", value1, value2, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgIdNotBetween(String value1, String value2) {
//            addCriterion("msg_id not between", value1, value2, "msgId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdIsNull() {
//            addCriterion("msg_product_id is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdIsNotNull() {
//            addCriterion("msg_product_id is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdEqualTo(String value) {
//            addCriterion("msg_product_id =", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdNotEqualTo(String value) {
//            addCriterion("msg_product_id <>", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdGreaterThan(String value) {
//            addCriterion("msg_product_id >", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdGreaterThanOrEqualTo(String value) {
//            addCriterion("msg_product_id >=", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdLessThan(String value) {
//            addCriterion("msg_product_id <", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdLessThanOrEqualTo(String value) {
//            addCriterion("msg_product_id <=", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdLike(String value) {
//            addCriterion("msg_product_id like", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdNotLike(String value) {
//            addCriterion("msg_product_id not like", value, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdIn(List<String> values) {
//            addCriterion("msg_product_id in", values, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdNotIn(List<String> values) {
//            addCriterion("msg_product_id not in", values, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdBetween(String value1, String value2) {
//            addCriterion("msg_product_id between", value1, value2, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andMsgProductIdNotBetween(String value1, String value2) {
//            addCriterion("msg_product_id not between", value1, value2, "msgProductId");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeIsNull() {
//            addCriterion("create_time is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeIsNotNull() {
//            addCriterion("create_time is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeEqualTo(Date value) {
//            addCriterion("create_time =", value, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeNotEqualTo(Date value) {
//            addCriterion("create_time <>", value, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeGreaterThan(Date value) {
//            addCriterion("create_time >", value, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeGreaterThanOrEqualTo(Date value) {
//            addCriterion("create_time >=", value, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeLessThan(Date value) {
//            addCriterion("create_time <", value, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeLessThanOrEqualTo(Date value) {
//            addCriterion("create_time <=", value, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeIn(List<Date> values) {
//            addCriterion("create_time in", values, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeNotIn(List<Date> values) {
//            addCriterion("create_time not in", values, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeBetween(Date value1, Date value2) {
//            addCriterion("create_time between", value1, value2, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andCreateTimeNotBetween(Date value1, Date value2) {
//            addCriterion("create_time not between", value1, value2, "createTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeIsNull() {
//            addCriterion("update_time is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeIsNotNull() {
//            addCriterion("update_time is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeEqualTo(Date value) {
//            addCriterion("update_time =", value, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeNotEqualTo(Date value) {
//            addCriterion("update_time <>", value, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeGreaterThan(Date value) {
//            addCriterion("update_time >", value, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
//            addCriterion("update_time >=", value, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeLessThan(Date value) {
//            addCriterion("update_time <", value, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeLessThanOrEqualTo(Date value) {
//            addCriterion("update_time <=", value, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeIn(List<Date> values) {
//            addCriterion("update_time in", values, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeNotIn(List<Date> values) {
//            addCriterion("update_time not in", values, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeBetween(Date value1, Date value2) {
//            addCriterion("update_time between", value1, value2, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andUpdateTimeNotBetween(Date value1, Date value2) {
//            addCriterion("update_time not between", value1, value2, "updateTime");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelIsNull() {
//            addCriterion("is_del is null");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelIsNotNull() {
//            addCriterion("is_del is not null");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelEqualTo(Integer value) {
//            addCriterion("is_del =", value, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelNotEqualTo(Integer value) {
//            addCriterion("is_del <>", value, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelGreaterThan(Integer value) {
//            addCriterion("is_del >", value, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelGreaterThanOrEqualTo(Integer value) {
//            addCriterion("is_del >=", value, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelLessThan(Integer value) {
//            addCriterion("is_del <", value, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelLessThanOrEqualTo(Integer value) {
//            addCriterion("is_del <=", value, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelIn(List<Integer> values) {
//            addCriterion("is_del in", values, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelNotIn(List<Integer> values) {
//            addCriterion("is_del not in", values, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelBetween(Integer value1, Integer value2) {
//            addCriterion("is_del between", value1, value2, "isDel");
//            return (Criteria) this;
//        }
//
//        public Criteria andIsDelNotBetween(Integer value1, Integer value2) {
//            addCriterion("is_del not between", value1, value2, "isDel");
//            return (Criteria) this;
//        }
//    }
//
//    public static class Criteria extends GeneratedCriteria {
//
//        protected Criteria() {
//            super();
//        }
//    }
//
//    public static class Criterion {
//        private String condition;
//
//        private Object value;
//
//        private Object secondValue;
//
//        private boolean noValue;
//
//        private boolean singleValue;
//
//        private boolean betweenValue;
//
//        private boolean listValue;
//
//        private String typeHandler;
//
//        public String getCondition() {
//            return condition;
//        }
//
//        public Object getValue() {
//            return value;
//        }
//
//        public Object getSecondValue() {
//            return secondValue;
//        }
//
//        public boolean isNoValue() {
//            return noValue;
//        }
//
//        public boolean isSingleValue() {
//            return singleValue;
//        }
//
//        public boolean isBetweenValue() {
//            return betweenValue;
//        }
//
//        public boolean isListValue() {
//            return listValue;
//        }
//
//        public String getTypeHandler() {
//            return typeHandler;
//        }
//
//        protected Criterion(String condition) {
//            super();
//            this.condition = condition;
//            this.typeHandler = null;
//            this.noValue = true;
//        }
//
//        protected Criterion(String condition, Object value, String typeHandler) {
//            super();
//            this.condition = condition;
//            this.value = value;
//            this.typeHandler = typeHandler;
//            if (value instanceof List<?>) {
//                this.listValue = true;
//            } else {
//                this.singleValue = true;
//            }
//        }
//
//        protected Criterion(String condition, Object value) {
//            this(condition, value, null);
//        }
//
//        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
//            super();
//            this.condition = condition;
//            this.value = value;
//            this.secondValue = secondValue;
//            this.typeHandler = typeHandler;
//            this.betweenValue = true;
//        }
//
//        protected Criterion(String condition, Object value, Object secondValue) {
//            this(condition, value, secondValue, null);
//        }
//    }
//}