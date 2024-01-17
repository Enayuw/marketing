package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScorePushCustomerConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ScorePushCustomerConfigExample() {
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

        public Criteria andScoreRuleShortNameIsNull() {
            addCriterion("score_rule_short_name is null");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameIsNotNull() {
            addCriterion("score_rule_short_name is not null");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameEqualTo(String value) {
            addCriterion("score_rule_short_name =", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameNotEqualTo(String value) {
            addCriterion("score_rule_short_name <>", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameGreaterThan(String value) {
            addCriterion("score_rule_short_name >", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameGreaterThanOrEqualTo(String value) {
            addCriterion("score_rule_short_name >=", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameLessThan(String value) {
            addCriterion("score_rule_short_name <", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameLessThanOrEqualTo(String value) {
            addCriterion("score_rule_short_name <=", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameLike(String value) {
            addCriterion("score_rule_short_name like", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameNotLike(String value) {
            addCriterion("score_rule_short_name not like", value, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameIn(List<String> values) {
            addCriterion("score_rule_short_name in", values, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameNotIn(List<String> values) {
            addCriterion("score_rule_short_name not in", values, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameBetween(String value1, String value2) {
            addCriterion("score_rule_short_name between", value1, value2, "scoreRuleShortName");
            return (Criteria) this;
        }

        public Criteria andScoreRuleShortNameNotBetween(String value1, String value2) {
            addCriterion("score_rule_short_name not between", value1, value2, "scoreRuleShortName");
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

        public Criteria andScoreSort1MappingIsNull() {
            addCriterion("score_sort1_mapping is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingIsNotNull() {
            addCriterion("score_sort1_mapping is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingEqualTo(String value) {
            addCriterion("score_sort1_mapping =", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingNotEqualTo(String value) {
            addCriterion("score_sort1_mapping <>", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingGreaterThan(String value) {
            addCriterion("score_sort1_mapping >", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingGreaterThanOrEqualTo(String value) {
            addCriterion("score_sort1_mapping >=", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingLessThan(String value) {
            addCriterion("score_sort1_mapping <", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingLessThanOrEqualTo(String value) {
            addCriterion("score_sort1_mapping <=", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingLike(String value) {
            addCriterion("score_sort1_mapping like", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingNotLike(String value) {
            addCriterion("score_sort1_mapping not like", value, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingIn(List<String> values) {
            addCriterion("score_sort1_mapping in", values, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingNotIn(List<String> values) {
            addCriterion("score_sort1_mapping not in", values, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingBetween(String value1, String value2) {
            addCriterion("score_sort1_mapping between", value1, value2, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort1MappingNotBetween(String value1, String value2) {
            addCriterion("score_sort1_mapping not between", value1, value2, "scoreSort1Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingIsNull() {
            addCriterion("score_sort2_mapping is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingIsNotNull() {
            addCriterion("score_sort2_mapping is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingEqualTo(String value) {
            addCriterion("score_sort2_mapping =", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingNotEqualTo(String value) {
            addCriterion("score_sort2_mapping <>", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingGreaterThan(String value) {
            addCriterion("score_sort2_mapping >", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingGreaterThanOrEqualTo(String value) {
            addCriterion("score_sort2_mapping >=", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingLessThan(String value) {
            addCriterion("score_sort2_mapping <", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingLessThanOrEqualTo(String value) {
            addCriterion("score_sort2_mapping <=", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingLike(String value) {
            addCriterion("score_sort2_mapping like", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingNotLike(String value) {
            addCriterion("score_sort2_mapping not like", value, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingIn(List<String> values) {
            addCriterion("score_sort2_mapping in", values, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingNotIn(List<String> values) {
            addCriterion("score_sort2_mapping not in", values, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingBetween(String value1, String value2) {
            addCriterion("score_sort2_mapping between", value1, value2, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort2MappingNotBetween(String value1, String value2) {
            addCriterion("score_sort2_mapping not between", value1, value2, "scoreSort2Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingIsNull() {
            addCriterion("score_sort3_mapping is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingIsNotNull() {
            addCriterion("score_sort3_mapping is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingEqualTo(String value) {
            addCriterion("score_sort3_mapping =", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingNotEqualTo(String value) {
            addCriterion("score_sort3_mapping <>", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingGreaterThan(String value) {
            addCriterion("score_sort3_mapping >", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingGreaterThanOrEqualTo(String value) {
            addCriterion("score_sort3_mapping >=", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingLessThan(String value) {
            addCriterion("score_sort3_mapping <", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingLessThanOrEqualTo(String value) {
            addCriterion("score_sort3_mapping <=", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingLike(String value) {
            addCriterion("score_sort3_mapping like", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingNotLike(String value) {
            addCriterion("score_sort3_mapping not like", value, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingIn(List<String> values) {
            addCriterion("score_sort3_mapping in", values, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingNotIn(List<String> values) {
            addCriterion("score_sort3_mapping not in", values, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingBetween(String value1, String value2) {
            addCriterion("score_sort3_mapping between", value1, value2, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort3MappingNotBetween(String value1, String value2) {
            addCriterion("score_sort3_mapping not between", value1, value2, "scoreSort3Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingIsNull() {
            addCriterion("score_sort4_mapping is null");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingIsNotNull() {
            addCriterion("score_sort4_mapping is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingEqualTo(String value) {
            addCriterion("score_sort4_mapping =", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingNotEqualTo(String value) {
            addCriterion("score_sort4_mapping <>", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingGreaterThan(String value) {
            addCriterion("score_sort4_mapping >", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingGreaterThanOrEqualTo(String value) {
            addCriterion("score_sort4_mapping >=", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingLessThan(String value) {
            addCriterion("score_sort4_mapping <", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingLessThanOrEqualTo(String value) {
            addCriterion("score_sort4_mapping <=", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingLike(String value) {
            addCriterion("score_sort4_mapping like", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingNotLike(String value) {
            addCriterion("score_sort4_mapping not like", value, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingIn(List<String> values) {
            addCriterion("score_sort4_mapping in", values, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingNotIn(List<String> values) {
            addCriterion("score_sort4_mapping not in", values, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingBetween(String value1, String value2) {
            addCriterion("score_sort4_mapping between", value1, value2, "scoreSort4Mapping");
            return (Criteria) this;
        }

        public Criteria andScoreSort4MappingNotBetween(String value1, String value2) {
            addCriterion("score_sort4_mapping not between", value1, value2, "scoreSort4Mapping");
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

        public Criteria andPushTypeIsNull() {
            addCriterion("push_type is null");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNotNull() {
            addCriterion("push_type is not null");
            return (Criteria) this;
        }

        public Criteria andPushTypeEqualTo(Integer value) {
            addCriterion("push_type =", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotEqualTo(Integer value) {
            addCriterion("push_type <>", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThan(Integer value) {
            addCriterion("push_type >", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_type >=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThan(Integer value) {
            addCriterion("push_type <", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThanOrEqualTo(Integer value) {
            addCriterion("push_type <=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeIn(List<Integer> values) {
            addCriterion("push_type in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotIn(List<Integer> values) {
            addCriterion("push_type not in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeBetween(Integer value1, Integer value2) {
            addCriterion("push_type between", value1, value2, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("push_type not between", value1, value2, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushMethodIsNull() {
            addCriterion("push_method is null");
            return (Criteria) this;
        }

        public Criteria andPushMethodIsNotNull() {
            addCriterion("push_method is not null");
            return (Criteria) this;
        }

        public Criteria andPushMethodEqualTo(String value) {
            addCriterion("push_method =", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodNotEqualTo(String value) {
            addCriterion("push_method <>", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodGreaterThan(String value) {
            addCriterion("push_method >", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodGreaterThanOrEqualTo(String value) {
            addCriterion("push_method >=", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodLessThan(String value) {
            addCriterion("push_method <", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodLessThanOrEqualTo(String value) {
            addCriterion("push_method <=", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodLike(String value) {
            addCriterion("push_method like", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodNotLike(String value) {
            addCriterion("push_method not like", value, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodIn(List<String> values) {
            addCriterion("push_method in", values, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodNotIn(List<String> values) {
            addCriterion("push_method not in", values, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodBetween(String value1, String value2) {
            addCriterion("push_method between", value1, value2, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andPushMethodNotBetween(String value1, String value2) {
            addCriterion("push_method not between", value1, value2, "pushMethod");
            return (Criteria) this;
        }

        public Criteria andResourceConfigIsNull() {
            addCriterion("resource_config is null");
            return (Criteria) this;
        }

        public Criteria andResourceConfigIsNotNull() {
            addCriterion("resource_config is not null");
            return (Criteria) this;
        }

        public Criteria andResourceConfigEqualTo(String value) {
            addCriterion("resource_config =", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigNotEqualTo(String value) {
            addCriterion("resource_config <>", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigGreaterThan(String value) {
            addCriterion("resource_config >", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigGreaterThanOrEqualTo(String value) {
            addCriterion("resource_config >=", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigLessThan(String value) {
            addCriterion("resource_config <", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigLessThanOrEqualTo(String value) {
            addCriterion("resource_config <=", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigLike(String value) {
            addCriterion("resource_config like", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigNotLike(String value) {
            addCriterion("resource_config not like", value, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigIn(List<String> values) {
            addCriterion("resource_config in", values, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigNotIn(List<String> values) {
            addCriterion("resource_config not in", values, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigBetween(String value1, String value2) {
            addCriterion("resource_config between", value1, value2, "resourceConfig");
            return (Criteria) this;
        }

        public Criteria andResourceConfigNotBetween(String value1, String value2) {
            addCriterion("resource_config not between", value1, value2, "resourceConfig");
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