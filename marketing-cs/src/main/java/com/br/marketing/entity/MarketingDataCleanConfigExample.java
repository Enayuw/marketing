package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingDataCleanConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingDataCleanConfigExample() {
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

        public Criteria andRuleIdIsNull() {
            addCriterion("rule_id is null");
            return (Criteria) this;
        }

        public Criteria andRuleIdIsNotNull() {
            addCriterion("rule_id is not null");
            return (Criteria) this;
        }

        public Criteria andRuleIdEqualTo(String value) {
            addCriterion("rule_id =", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdNotEqualTo(String value) {
            addCriterion("rule_id <>", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdGreaterThan(String value) {
            addCriterion("rule_id >", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdGreaterThanOrEqualTo(String value) {
            addCriterion("rule_id >=", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdLessThan(String value) {
            addCriterion("rule_id <", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdLessThanOrEqualTo(String value) {
            addCriterion("rule_id <=", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdLike(String value) {
            addCriterion("rule_id like", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdNotLike(String value) {
            addCriterion("rule_id not like", value, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdIn(List<String> values) {
            addCriterion("rule_id in", values, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdNotIn(List<String> values) {
            addCriterion("rule_id not in", values, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdBetween(String value1, String value2) {
            addCriterion("rule_id between", value1, value2, "ruleId");
            return (Criteria) this;
        }

        public Criteria andRuleIdNotBetween(String value1, String value2) {
            addCriterion("rule_id not between", value1, value2, "ruleId");
            return (Criteria) this;
        }

        public Criteria andMappingModeIsNull() {
            addCriterion("mapping_mode is null");
            return (Criteria) this;
        }

        public Criteria andMappingModeIsNotNull() {
            addCriterion("mapping_mode is not null");
            return (Criteria) this;
        }

        public Criteria andMappingModeEqualTo(Integer value) {
            addCriterion("mapping_mode =", value, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeNotEqualTo(Integer value) {
            addCriterion("mapping_mode <>", value, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeGreaterThan(Integer value) {
            addCriterion("mapping_mode >", value, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeGreaterThanOrEqualTo(Integer value) {
            addCriterion("mapping_mode >=", value, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeLessThan(Integer value) {
            addCriterion("mapping_mode <", value, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeLessThanOrEqualTo(Integer value) {
            addCriterion("mapping_mode <=", value, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeIn(List<Integer> values) {
            addCriterion("mapping_mode in", values, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeNotIn(List<Integer> values) {
            addCriterion("mapping_mode not in", values, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeBetween(Integer value1, Integer value2) {
            addCriterion("mapping_mode between", value1, value2, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andMappingModeNotBetween(Integer value1, Integer value2) {
            addCriterion("mapping_mode not between", value1, value2, "mappingMode");
            return (Criteria) this;
        }

        public Criteria andOriginTypeIsNull() {
            addCriterion("origin_type is null");
            return (Criteria) this;
        }

        public Criteria andOriginTypeIsNotNull() {
            addCriterion("origin_type is not null");
            return (Criteria) this;
        }

        public Criteria andOriginTypeEqualTo(Integer value) {
            addCriterion("origin_type =", value, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeNotEqualTo(Integer value) {
            addCriterion("origin_type <>", value, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeGreaterThan(Integer value) {
            addCriterion("origin_type >", value, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("origin_type >=", value, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeLessThan(Integer value) {
            addCriterion("origin_type <", value, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeLessThanOrEqualTo(Integer value) {
            addCriterion("origin_type <=", value, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeIn(List<Integer> values) {
            addCriterion("origin_type in", values, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeNotIn(List<Integer> values) {
            addCriterion("origin_type not in", values, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeBetween(Integer value1, Integer value2) {
            addCriterion("origin_type between", value1, value2, "originType");
            return (Criteria) this;
        }

        public Criteria andOriginTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("origin_type not between", value1, value2, "originType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIsNull() {
            addCriterion("target_type is null");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIsNotNull() {
            addCriterion("target_type is not null");
            return (Criteria) this;
        }

        public Criteria andTargetTypeEqualTo(Integer value) {
            addCriterion("target_type =", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotEqualTo(Integer value) {
            addCriterion("target_type <>", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThan(Integer value) {
            addCriterion("target_type >", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("target_type >=", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThan(Integer value) {
            addCriterion("target_type <", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThanOrEqualTo(Integer value) {
            addCriterion("target_type <=", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIn(List<Integer> values) {
            addCriterion("target_type in", values, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotIn(List<Integer> values) {
            addCriterion("target_type not in", values, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeBetween(Integer value1, Integer value2) {
            addCriterion("target_type between", value1, value2, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("target_type not between", value1, value2, "targetType");
            return (Criteria) this;
        }

        public Criteria andOriginNameIsNull() {
            addCriterion("origin_name is null");
            return (Criteria) this;
        }

        public Criteria andOriginNameIsNotNull() {
            addCriterion("origin_name is not null");
            return (Criteria) this;
        }

        public Criteria andOriginNameEqualTo(String value) {
            addCriterion("origin_name =", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameNotEqualTo(String value) {
            addCriterion("origin_name <>", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameGreaterThan(String value) {
            addCriterion("origin_name >", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameGreaterThanOrEqualTo(String value) {
            addCriterion("origin_name >=", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameLessThan(String value) {
            addCriterion("origin_name <", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameLessThanOrEqualTo(String value) {
            addCriterion("origin_name <=", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameLike(String value) {
            addCriterion("origin_name like", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameNotLike(String value) {
            addCriterion("origin_name not like", value, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameIn(List<String> values) {
            addCriterion("origin_name in", values, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameNotIn(List<String> values) {
            addCriterion("origin_name not in", values, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameBetween(String value1, String value2) {
            addCriterion("origin_name between", value1, value2, "originName");
            return (Criteria) this;
        }

        public Criteria andOriginNameNotBetween(String value1, String value2) {
            addCriterion("origin_name not between", value1, value2, "originName");
            return (Criteria) this;
        }

        public Criteria andTargetNameIsNull() {
            addCriterion("target_name is null");
            return (Criteria) this;
        }

        public Criteria andTargetNameIsNotNull() {
            addCriterion("target_name is not null");
            return (Criteria) this;
        }

        public Criteria andTargetNameEqualTo(String value) {
            addCriterion("target_name =", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameNotEqualTo(String value) {
            addCriterion("target_name <>", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameGreaterThan(String value) {
            addCriterion("target_name >", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameGreaterThanOrEqualTo(String value) {
            addCriterion("target_name >=", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameLessThan(String value) {
            addCriterion("target_name <", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameLessThanOrEqualTo(String value) {
            addCriterion("target_name <=", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameLike(String value) {
            addCriterion("target_name like", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameNotLike(String value) {
            addCriterion("target_name not like", value, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameIn(List<String> values) {
            addCriterion("target_name in", values, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameNotIn(List<String> values) {
            addCriterion("target_name not in", values, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameBetween(String value1, String value2) {
            addCriterion("target_name between", value1, value2, "targetName");
            return (Criteria) this;
        }

        public Criteria andTargetNameNotBetween(String value1, String value2) {
            addCriterion("target_name not between", value1, value2, "targetName");
            return (Criteria) this;
        }

        public Criteria andConversionIsNull() {
            addCriterion("`conversion` is null");
            return (Criteria) this;
        }

        public Criteria andConversionIsNotNull() {
            addCriterion("`conversion` is not null");
            return (Criteria) this;
        }

        public Criteria andConversionEqualTo(String value) {
            addCriterion("`conversion` =", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionNotEqualTo(String value) {
            addCriterion("`conversion` <>", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionGreaterThan(String value) {
            addCriterion("`conversion` >", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionGreaterThanOrEqualTo(String value) {
            addCriterion("`conversion` >=", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionLessThan(String value) {
            addCriterion("`conversion` <", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionLessThanOrEqualTo(String value) {
            addCriterion("`conversion` <=", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionLike(String value) {
            addCriterion("`conversion` like", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionNotLike(String value) {
            addCriterion("`conversion` not like", value, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionIn(List<String> values) {
            addCriterion("`conversion` in", values, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionNotIn(List<String> values) {
            addCriterion("`conversion` not in", values, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionBetween(String value1, String value2) {
            addCriterion("`conversion` between", value1, value2, "conversion");
            return (Criteria) this;
        }

        public Criteria andConversionNotBetween(String value1, String value2) {
            addCriterion("`conversion` not between", value1, value2, "conversion");
            return (Criteria) this;
        }

        public Criteria andDefaultValueIsNull() {
            addCriterion("default_value is null");
            return (Criteria) this;
        }

        public Criteria andDefaultValueIsNotNull() {
            addCriterion("default_value is not null");
            return (Criteria) this;
        }

        public Criteria andDefaultValueEqualTo(String value) {
            addCriterion("default_value =", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueNotEqualTo(String value) {
            addCriterion("default_value <>", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueGreaterThan(String value) {
            addCriterion("default_value >", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueGreaterThanOrEqualTo(String value) {
            addCriterion("default_value >=", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueLessThan(String value) {
            addCriterion("default_value <", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueLessThanOrEqualTo(String value) {
            addCriterion("default_value <=", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueLike(String value) {
            addCriterion("default_value like", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueNotLike(String value) {
            addCriterion("default_value not like", value, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueIn(List<String> values) {
            addCriterion("default_value in", values, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueNotIn(List<String> values) {
            addCriterion("default_value not in", values, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueBetween(String value1, String value2) {
            addCriterion("default_value between", value1, value2, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDefaultValueNotBetween(String value1, String value2) {
            addCriterion("default_value not between", value1, value2, "defaultValue");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternIsNull() {
            addCriterion("date_transform_pattern is null");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternIsNotNull() {
            addCriterion("date_transform_pattern is not null");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternEqualTo(String value) {
            addCriterion("date_transform_pattern =", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternNotEqualTo(String value) {
            addCriterion("date_transform_pattern <>", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternGreaterThan(String value) {
            addCriterion("date_transform_pattern >", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternGreaterThanOrEqualTo(String value) {
            addCriterion("date_transform_pattern >=", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternLessThan(String value) {
            addCriterion("date_transform_pattern <", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternLessThanOrEqualTo(String value) {
            addCriterion("date_transform_pattern <=", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternLike(String value) {
            addCriterion("date_transform_pattern like", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternNotLike(String value) {
            addCriterion("date_transform_pattern not like", value, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternIn(List<String> values) {
            addCriterion("date_transform_pattern in", values, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternNotIn(List<String> values) {
            addCriterion("date_transform_pattern not in", values, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternBetween(String value1, String value2) {
            addCriterion("date_transform_pattern between", value1, value2, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDateTransformPatternNotBetween(String value1, String value2) {
            addCriterion("date_transform_pattern not between", value1, value2, "dateTransformPattern");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeIsNull() {
            addCriterion("decimal_reserve_type is null");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeIsNotNull() {
            addCriterion("decimal_reserve_type is not null");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeEqualTo(String value) {
            addCriterion("decimal_reserve_type =", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeNotEqualTo(String value) {
            addCriterion("decimal_reserve_type <>", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeGreaterThan(String value) {
            addCriterion("decimal_reserve_type >", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeGreaterThanOrEqualTo(String value) {
            addCriterion("decimal_reserve_type >=", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeLessThan(String value) {
            addCriterion("decimal_reserve_type <", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeLessThanOrEqualTo(String value) {
            addCriterion("decimal_reserve_type <=", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeLike(String value) {
            addCriterion("decimal_reserve_type like", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeNotLike(String value) {
            addCriterion("decimal_reserve_type not like", value, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeIn(List<String> values) {
            addCriterion("decimal_reserve_type in", values, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeNotIn(List<String> values) {
            addCriterion("decimal_reserve_type not in", values, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeBetween(String value1, String value2) {
            addCriterion("decimal_reserve_type between", value1, value2, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReserveTypeNotBetween(String value1, String value2) {
            addCriterion("decimal_reserve_type not between", value1, value2, "decimalReserveType");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionIsNull() {
            addCriterion("decimal_reserve_precision is null");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionIsNotNull() {
            addCriterion("decimal_reserve_precision is not null");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionEqualTo(Integer value) {
            addCriterion("decimal_reserve_precision =", value, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionNotEqualTo(Integer value) {
            addCriterion("decimal_reserve_precision <>", value, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionGreaterThan(Integer value) {
            addCriterion("decimal_reserve_precision >", value, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionGreaterThanOrEqualTo(Integer value) {
            addCriterion("decimal_reserve_precision >=", value, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionLessThan(Integer value) {
            addCriterion("decimal_reserve_precision <", value, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionLessThanOrEqualTo(Integer value) {
            addCriterion("decimal_reserve_precision <=", value, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionIn(List<Integer> values) {
            addCriterion("decimal_reserve_precision in", values, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionNotIn(List<Integer> values) {
            addCriterion("decimal_reserve_precision not in", values, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionBetween(Integer value1, Integer value2) {
            addCriterion("decimal_reserve_precision between", value1, value2, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalReservePrecisionNotBetween(Integer value1, Integer value2) {
            addCriterion("decimal_reserve_precision not between", value1, value2, "decimalReservePrecision");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioIsNull() {
            addCriterion("decimal_unit_ratio is null");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioIsNotNull() {
            addCriterion("decimal_unit_ratio is not null");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioEqualTo(String value) {
            addCriterion("decimal_unit_ratio =", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioNotEqualTo(String value) {
            addCriterion("decimal_unit_ratio <>", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioGreaterThan(String value) {
            addCriterion("decimal_unit_ratio >", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioGreaterThanOrEqualTo(String value) {
            addCriterion("decimal_unit_ratio >=", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioLessThan(String value) {
            addCriterion("decimal_unit_ratio <", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioLessThanOrEqualTo(String value) {
            addCriterion("decimal_unit_ratio <=", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioLike(String value) {
            addCriterion("decimal_unit_ratio like", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioNotLike(String value) {
            addCriterion("decimal_unit_ratio not like", value, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioIn(List<String> values) {
            addCriterion("decimal_unit_ratio in", values, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioNotIn(List<String> values) {
            addCriterion("decimal_unit_ratio not in", values, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioBetween(String value1, String value2) {
            addCriterion("decimal_unit_ratio between", value1, value2, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andDecimalUnitRatioNotBetween(String value1, String value2) {
            addCriterion("decimal_unit_ratio not between", value1, value2, "decimalUnitRatio");
            return (Criteria) this;
        }

        public Criteria andMappingConditionIsNull() {
            addCriterion("mapping_condition is null");
            return (Criteria) this;
        }

        public Criteria andMappingConditionIsNotNull() {
            addCriterion("mapping_condition is not null");
            return (Criteria) this;
        }

        public Criteria andMappingConditionEqualTo(String value) {
            addCriterion("mapping_condition =", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionNotEqualTo(String value) {
            addCriterion("mapping_condition <>", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionGreaterThan(String value) {
            addCriterion("mapping_condition >", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionGreaterThanOrEqualTo(String value) {
            addCriterion("mapping_condition >=", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionLessThan(String value) {
            addCriterion("mapping_condition <", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionLessThanOrEqualTo(String value) {
            addCriterion("mapping_condition <=", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionLike(String value) {
            addCriterion("mapping_condition like", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionNotLike(String value) {
            addCriterion("mapping_condition not like", value, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionIn(List<String> values) {
            addCriterion("mapping_condition in", values, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionNotIn(List<String> values) {
            addCriterion("mapping_condition not in", values, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionBetween(String value1, String value2) {
            addCriterion("mapping_condition between", value1, value2, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingConditionNotBetween(String value1, String value2) {
            addCriterion("mapping_condition not between", value1, value2, "mappingCondition");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueIsNull() {
            addCriterion("mapping_out_value is null");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueIsNotNull() {
            addCriterion("mapping_out_value is not null");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueEqualTo(String value) {
            addCriterion("mapping_out_value =", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueNotEqualTo(String value) {
            addCriterion("mapping_out_value <>", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueGreaterThan(String value) {
            addCriterion("mapping_out_value >", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueGreaterThanOrEqualTo(String value) {
            addCriterion("mapping_out_value >=", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueLessThan(String value) {
            addCriterion("mapping_out_value <", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueLessThanOrEqualTo(String value) {
            addCriterion("mapping_out_value <=", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueLike(String value) {
            addCriterion("mapping_out_value like", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueNotLike(String value) {
            addCriterion("mapping_out_value not like", value, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueIn(List<String> values) {
            addCriterion("mapping_out_value in", values, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueNotIn(List<String> values) {
            addCriterion("mapping_out_value not in", values, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueBetween(String value1, String value2) {
            addCriterion("mapping_out_value between", value1, value2, "mappingOutValue");
            return (Criteria) this;
        }

        public Criteria andMappingOutValueNotBetween(String value1, String value2) {
            addCriterion("mapping_out_value not between", value1, value2, "mappingOutValue");
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