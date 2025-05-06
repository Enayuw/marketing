package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarClueManageConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CarClueManageConfigExample() {
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

        public Criteria andPullDateIsNull() {
            addCriterion("pull_date is null");
            return (Criteria) this;
        }

        public Criteria andPullDateIsNotNull() {
            addCriterion("pull_date is not null");
            return (Criteria) this;
        }

        public Criteria andPullDateEqualTo(String value) {
            addCriterion("pull_date =", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateNotEqualTo(String value) {
            addCriterion("pull_date <>", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateGreaterThan(String value) {
            addCriterion("pull_date >", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateGreaterThanOrEqualTo(String value) {
            addCriterion("pull_date >=", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateLessThan(String value) {
            addCriterion("pull_date <", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateLessThanOrEqualTo(String value) {
            addCriterion("pull_date <=", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateLike(String value) {
            addCriterion("pull_date like", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateNotLike(String value) {
            addCriterion("pull_date not like", value, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateIn(List<String> values) {
            addCriterion("pull_date in", values, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateNotIn(List<String> values) {
            addCriterion("pull_date not in", values, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateBetween(String value1, String value2) {
            addCriterion("pull_date between", value1, value2, "pullDate");
            return (Criteria) this;
        }

        public Criteria andPullDateNotBetween(String value1, String value2) {
            addCriterion("pull_date not between", value1, value2, "pullDate");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigIsNull() {
            addCriterion("intention_config is null");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigIsNotNull() {
            addCriterion("intention_config is not null");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigEqualTo(String value) {
            addCriterion("intention_config =", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigNotEqualTo(String value) {
            addCriterion("intention_config <>", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigGreaterThan(String value) {
            addCriterion("intention_config >", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigGreaterThanOrEqualTo(String value) {
            addCriterion("intention_config >=", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigLessThan(String value) {
            addCriterion("intention_config <", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigLessThanOrEqualTo(String value) {
            addCriterion("intention_config <=", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigLike(String value) {
            addCriterion("intention_config like", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigNotLike(String value) {
            addCriterion("intention_config not like", value, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigIn(List<String> values) {
            addCriterion("intention_config in", values, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigNotIn(List<String> values) {
            addCriterion("intention_config not in", values, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigBetween(String value1, String value2) {
            addCriterion("intention_config between", value1, value2, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andIntentionConfigNotBetween(String value1, String value2) {
            addCriterion("intention_config not between", value1, value2, "intentionConfig");
            return (Criteria) this;
        }

        public Criteria andCleanTypeIsNull() {
            addCriterion("clean_type is null");
            return (Criteria) this;
        }

        public Criteria andCleanTypeIsNotNull() {
            addCriterion("clean_type is not null");
            return (Criteria) this;
        }

        public Criteria andCleanTypeEqualTo(Integer value) {
            addCriterion("clean_type =", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeNotEqualTo(Integer value) {
            addCriterion("clean_type <>", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeGreaterThan(Integer value) {
            addCriterion("clean_type >", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("clean_type >=", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeLessThan(Integer value) {
            addCriterion("clean_type <", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeLessThanOrEqualTo(Integer value) {
            addCriterion("clean_type <=", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeIn(List<Integer> values) {
            addCriterion("clean_type in", values, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeNotIn(List<Integer> values) {
            addCriterion("clean_type not in", values, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeBetween(Integer value1, Integer value2) {
            addCriterion("clean_type between", value1, value2, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("clean_type not between", value1, value2, "cleanType");
            return (Criteria) this;
        }

        public Criteria andPullTypeIsNull() {
            addCriterion("pull_type is null");
            return (Criteria) this;
        }

        public Criteria andPullTypeIsNotNull() {
            addCriterion("pull_type is not null");
            return (Criteria) this;
        }

        public Criteria andPullTypeEqualTo(Integer value) {
            addCriterion("pull_type =", value, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeNotEqualTo(Integer value) {
            addCriterion("pull_type <>", value, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeGreaterThan(Integer value) {
            addCriterion("pull_type >", value, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("pull_type >=", value, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeLessThan(Integer value) {
            addCriterion("pull_type <", value, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeLessThanOrEqualTo(Integer value) {
            addCriterion("pull_type <=", value, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeIn(List<Integer> values) {
            addCriterion("pull_type in", values, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeNotIn(List<Integer> values) {
            addCriterion("pull_type not in", values, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeBetween(Integer value1, Integer value2) {
            addCriterion("pull_type between", value1, value2, "pullType");
            return (Criteria) this;
        }

        public Criteria andPullTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("pull_type not between", value1, value2, "pullType");
            return (Criteria) this;
        }

        public Criteria andOptUserIdIsNull() {
            addCriterion("opt_user_id is null");
            return (Criteria) this;
        }

        public Criteria andOptUserIdIsNotNull() {
            addCriterion("opt_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andOptUserIdEqualTo(Long value) {
            addCriterion("opt_user_id =", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotEqualTo(Long value) {
            addCriterion("opt_user_id <>", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdGreaterThan(Long value) {
            addCriterion("opt_user_id >", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("opt_user_id >=", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdLessThan(Long value) {
            addCriterion("opt_user_id <", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdLessThanOrEqualTo(Long value) {
            addCriterion("opt_user_id <=", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdIn(List<Long> values) {
            addCriterion("opt_user_id in", values, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotIn(List<Long> values) {
            addCriterion("opt_user_id not in", values, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdBetween(Long value1, Long value2) {
            addCriterion("opt_user_id between", value1, value2, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotBetween(Long value1, Long value2) {
            addCriterion("opt_user_id not between", value1, value2, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserNameIsNull() {
            addCriterion("opt_user_name is null");
            return (Criteria) this;
        }

        public Criteria andOptUserNameIsNotNull() {
            addCriterion("opt_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andOptUserNameEqualTo(String value) {
            addCriterion("opt_user_name =", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotEqualTo(String value) {
            addCriterion("opt_user_name <>", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameGreaterThan(String value) {
            addCriterion("opt_user_name >", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("opt_user_name >=", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameLessThan(String value) {
            addCriterion("opt_user_name <", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameLessThanOrEqualTo(String value) {
            addCriterion("opt_user_name <=", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameLike(String value) {
            addCriterion("opt_user_name like", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotLike(String value) {
            addCriterion("opt_user_name not like", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameIn(List<String> values) {
            addCriterion("opt_user_name in", values, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotIn(List<String> values) {
            addCriterion("opt_user_name not in", values, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameBetween(String value1, String value2) {
            addCriterion("opt_user_name between", value1, value2, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotBetween(String value1, String value2) {
            addCriterion("opt_user_name not between", value1, value2, "optUserName");
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