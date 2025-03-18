package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarClueRelationalMappingExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CarClueRelationalMappingExample() {
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

        public Criteria andBrandIdIsNull() {
            addCriterion("brand_id is null");
            return (Criteria) this;
        }

        public Criteria andBrandIdIsNotNull() {
            addCriterion("brand_id is not null");
            return (Criteria) this;
        }

        public Criteria andBrandIdEqualTo(Integer value) {
            addCriterion("brand_id =", value, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdNotEqualTo(Integer value) {
            addCriterion("brand_id <>", value, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdGreaterThan(Integer value) {
            addCriterion("brand_id >", value, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("brand_id >=", value, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdLessThan(Integer value) {
            addCriterion("brand_id <", value, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdLessThanOrEqualTo(Integer value) {
            addCriterion("brand_id <=", value, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdIn(List<Integer> values) {
            addCriterion("brand_id in", values, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdNotIn(List<Integer> values) {
            addCriterion("brand_id not in", values, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdBetween(Integer value1, Integer value2) {
            addCriterion("brand_id between", value1, value2, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandIdNotBetween(Integer value1, Integer value2) {
            addCriterion("brand_id not between", value1, value2, "brandId");
            return (Criteria) this;
        }

        public Criteria andBrandNameIsNull() {
            addCriterion("brand_name is null");
            return (Criteria) this;
        }

        public Criteria andBrandNameIsNotNull() {
            addCriterion("brand_name is not null");
            return (Criteria) this;
        }

        public Criteria andBrandNameEqualTo(String value) {
            addCriterion("brand_name =", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameNotEqualTo(String value) {
            addCriterion("brand_name <>", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameGreaterThan(String value) {
            addCriterion("brand_name >", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameGreaterThanOrEqualTo(String value) {
            addCriterion("brand_name >=", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameLessThan(String value) {
            addCriterion("brand_name <", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameLessThanOrEqualTo(String value) {
            addCriterion("brand_name <=", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameLike(String value) {
            addCriterion("brand_name like", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameNotLike(String value) {
            addCriterion("brand_name not like", value, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameIn(List<String> values) {
            addCriterion("brand_name in", values, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameNotIn(List<String> values) {
            addCriterion("brand_name not in", values, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameBetween(String value1, String value2) {
            addCriterion("brand_name between", value1, value2, "brandName");
            return (Criteria) this;
        }

        public Criteria andBrandNameNotBetween(String value1, String value2) {
            addCriterion("brand_name not between", value1, value2, "brandName");
            return (Criteria) this;
        }

        public Criteria andSeriesIdIsNull() {
            addCriterion("series_id is null");
            return (Criteria) this;
        }

        public Criteria andSeriesIdIsNotNull() {
            addCriterion("series_id is not null");
            return (Criteria) this;
        }

        public Criteria andSeriesIdEqualTo(Integer value) {
            addCriterion("series_id =", value, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdNotEqualTo(Integer value) {
            addCriterion("series_id <>", value, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdGreaterThan(Integer value) {
            addCriterion("series_id >", value, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("series_id >=", value, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdLessThan(Integer value) {
            addCriterion("series_id <", value, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdLessThanOrEqualTo(Integer value) {
            addCriterion("series_id <=", value, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdIn(List<Integer> values) {
            addCriterion("series_id in", values, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdNotIn(List<Integer> values) {
            addCriterion("series_id not in", values, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdBetween(Integer value1, Integer value2) {
            addCriterion("series_id between", value1, value2, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesIdNotBetween(Integer value1, Integer value2) {
            addCriterion("series_id not between", value1, value2, "seriesId");
            return (Criteria) this;
        }

        public Criteria andSeriesNameIsNull() {
            addCriterion("series_name is null");
            return (Criteria) this;
        }

        public Criteria andSeriesNameIsNotNull() {
            addCriterion("series_name is not null");
            return (Criteria) this;
        }

        public Criteria andSeriesNameEqualTo(String value) {
            addCriterion("series_name =", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameNotEqualTo(String value) {
            addCriterion("series_name <>", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameGreaterThan(String value) {
            addCriterion("series_name >", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameGreaterThanOrEqualTo(String value) {
            addCriterion("series_name >=", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameLessThan(String value) {
            addCriterion("series_name <", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameLessThanOrEqualTo(String value) {
            addCriterion("series_name <=", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameLike(String value) {
            addCriterion("series_name like", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameNotLike(String value) {
            addCriterion("series_name not like", value, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameIn(List<String> values) {
            addCriterion("series_name in", values, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameNotIn(List<String> values) {
            addCriterion("series_name not in", values, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameBetween(String value1, String value2) {
            addCriterion("series_name between", value1, value2, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSeriesNameNotBetween(String value1, String value2) {
            addCriterion("series_name not between", value1, value2, "seriesName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameIsNull() {
            addCriterion("satisfy_province_name is null");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameIsNotNull() {
            addCriterion("satisfy_province_name is not null");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameEqualTo(String value) {
            addCriterion("satisfy_province_name =", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameNotEqualTo(String value) {
            addCriterion("satisfy_province_name <>", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameGreaterThan(String value) {
            addCriterion("satisfy_province_name >", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameGreaterThanOrEqualTo(String value) {
            addCriterion("satisfy_province_name >=", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameLessThan(String value) {
            addCriterion("satisfy_province_name <", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameLessThanOrEqualTo(String value) {
            addCriterion("satisfy_province_name <=", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameLike(String value) {
            addCriterion("satisfy_province_name like", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameNotLike(String value) {
            addCriterion("satisfy_province_name not like", value, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameIn(List<String> values) {
            addCriterion("satisfy_province_name in", values, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameNotIn(List<String> values) {
            addCriterion("satisfy_province_name not in", values, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameBetween(String value1, String value2) {
            addCriterion("satisfy_province_name between", value1, value2, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyProvinceNameNotBetween(String value1, String value2) {
            addCriterion("satisfy_province_name not between", value1, value2, "satisfyProvinceName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameIsNull() {
            addCriterion("satisfy_city_name is null");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameIsNotNull() {
            addCriterion("satisfy_city_name is not null");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameEqualTo(String value) {
            addCriterion("satisfy_city_name =", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameNotEqualTo(String value) {
            addCriterion("satisfy_city_name <>", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameGreaterThan(String value) {
            addCriterion("satisfy_city_name >", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameGreaterThanOrEqualTo(String value) {
            addCriterion("satisfy_city_name >=", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameLessThan(String value) {
            addCriterion("satisfy_city_name <", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameLessThanOrEqualTo(String value) {
            addCriterion("satisfy_city_name <=", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameLike(String value) {
            addCriterion("satisfy_city_name like", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameNotLike(String value) {
            addCriterion("satisfy_city_name not like", value, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameIn(List<String> values) {
            addCriterion("satisfy_city_name in", values, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameNotIn(List<String> values) {
            addCriterion("satisfy_city_name not in", values, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameBetween(String value1, String value2) {
            addCriterion("satisfy_city_name between", value1, value2, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andSatisfyCityNameNotBetween(String value1, String value2) {
            addCriterion("satisfy_city_name not between", value1, value2, "satisfyCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameIsNull() {
            addCriterion("exclude_province_name is null");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameIsNotNull() {
            addCriterion("exclude_province_name is not null");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameEqualTo(String value) {
            addCriterion("exclude_province_name =", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameNotEqualTo(String value) {
            addCriterion("exclude_province_name <>", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameGreaterThan(String value) {
            addCriterion("exclude_province_name >", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameGreaterThanOrEqualTo(String value) {
            addCriterion("exclude_province_name >=", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameLessThan(String value) {
            addCriterion("exclude_province_name <", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameLessThanOrEqualTo(String value) {
            addCriterion("exclude_province_name <=", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameLike(String value) {
            addCriterion("exclude_province_name like", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameNotLike(String value) {
            addCriterion("exclude_province_name not like", value, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameIn(List<String> values) {
            addCriterion("exclude_province_name in", values, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameNotIn(List<String> values) {
            addCriterion("exclude_province_name not in", values, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameBetween(String value1, String value2) {
            addCriterion("exclude_province_name between", value1, value2, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeProvinceNameNotBetween(String value1, String value2) {
            addCriterion("exclude_province_name not between", value1, value2, "excludeProvinceName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameIsNull() {
            addCriterion("exclude_city_name is null");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameIsNotNull() {
            addCriterion("exclude_city_name is not null");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameEqualTo(String value) {
            addCriterion("exclude_city_name =", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameNotEqualTo(String value) {
            addCriterion("exclude_city_name <>", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameGreaterThan(String value) {
            addCriterion("exclude_city_name >", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameGreaterThanOrEqualTo(String value) {
            addCriterion("exclude_city_name >=", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameLessThan(String value) {
            addCriterion("exclude_city_name <", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameLessThanOrEqualTo(String value) {
            addCriterion("exclude_city_name <=", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameLike(String value) {
            addCriterion("exclude_city_name like", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameNotLike(String value) {
            addCriterion("exclude_city_name not like", value, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameIn(List<String> values) {
            addCriterion("exclude_city_name in", values, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameNotIn(List<String> values) {
            addCriterion("exclude_city_name not in", values, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameBetween(String value1, String value2) {
            addCriterion("exclude_city_name between", value1, value2, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andExcludeCityNameNotBetween(String value1, String value2) {
            addCriterion("exclude_city_name not between", value1, value2, "excludeCityName");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeIsNull() {
            addCriterion("province_type is null");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeIsNotNull() {
            addCriterion("province_type is not null");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeEqualTo(Integer value) {
            addCriterion("province_type =", value, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeNotEqualTo(Integer value) {
            addCriterion("province_type <>", value, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeGreaterThan(Integer value) {
            addCriterion("province_type >", value, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("province_type >=", value, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeLessThan(Integer value) {
            addCriterion("province_type <", value, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeLessThanOrEqualTo(Integer value) {
            addCriterion("province_type <=", value, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeIn(List<Integer> values) {
            addCriterion("province_type in", values, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeNotIn(List<Integer> values) {
            addCriterion("province_type not in", values, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeBetween(Integer value1, Integer value2) {
            addCriterion("province_type between", value1, value2, "provinceType");
            return (Criteria) this;
        }

        public Criteria andProvinceTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("province_type not between", value1, value2, "provinceType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeIsNull() {
            addCriterion("matching_type is null");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeIsNotNull() {
            addCriterion("matching_type is not null");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeEqualTo(Integer value) {
            addCriterion("matching_type =", value, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeNotEqualTo(Integer value) {
            addCriterion("matching_type <>", value, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeGreaterThan(Integer value) {
            addCriterion("matching_type >", value, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("matching_type >=", value, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeLessThan(Integer value) {
            addCriterion("matching_type <", value, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeLessThanOrEqualTo(Integer value) {
            addCriterion("matching_type <=", value, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeIn(List<Integer> values) {
            addCriterion("matching_type in", values, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeNotIn(List<Integer> values) {
            addCriterion("matching_type not in", values, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeBetween(Integer value1, Integer value2) {
            addCriterion("matching_type between", value1, value2, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("matching_type not between", value1, value2, "matchingType");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseIsNull() {
            addCriterion("matching_cause is null");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseIsNotNull() {
            addCriterion("matching_cause is not null");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseEqualTo(String value) {
            addCriterion("matching_cause =", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseNotEqualTo(String value) {
            addCriterion("matching_cause <>", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseGreaterThan(String value) {
            addCriterion("matching_cause >", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseGreaterThanOrEqualTo(String value) {
            addCriterion("matching_cause >=", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseLessThan(String value) {
            addCriterion("matching_cause <", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseLessThanOrEqualTo(String value) {
            addCriterion("matching_cause <=", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseLike(String value) {
            addCriterion("matching_cause like", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseNotLike(String value) {
            addCriterion("matching_cause not like", value, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseIn(List<String> values) {
            addCriterion("matching_cause in", values, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseNotIn(List<String> values) {
            addCriterion("matching_cause not in", values, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseBetween(String value1, String value2) {
            addCriterion("matching_cause between", value1, value2, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andMatchingCauseNotBetween(String value1, String value2) {
            addCriterion("matching_cause not between", value1, value2, "matchingCause");
            return (Criteria) this;
        }

        public Criteria andAppletDateIsNull() {
            addCriterion("applet_date is null");
            return (Criteria) this;
        }

        public Criteria andAppletDateIsNotNull() {
            addCriterion("applet_date is not null");
            return (Criteria) this;
        }

        public Criteria andAppletDateEqualTo(String value) {
            addCriterion("applet_date =", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotEqualTo(String value) {
            addCriterion("applet_date <>", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThan(String value) {
            addCriterion("applet_date >", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThanOrEqualTo(String value) {
            addCriterion("applet_date >=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThan(String value) {
            addCriterion("applet_date <", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThanOrEqualTo(String value) {
            addCriterion("applet_date <=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLike(String value) {
            addCriterion("applet_date like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotLike(String value) {
            addCriterion("applet_date not like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateIn(List<String> values) {
            addCriterion("applet_date in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotIn(List<String> values) {
            addCriterion("applet_date not in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateBetween(String value1, String value2) {
            addCriterion("applet_date between", value1, value2, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotBetween(String value1, String value2) {
            addCriterion("applet_date not between", value1, value2, "appletDate");
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

        public Criteria andDailyLimitedIsNull() {
            addCriterion("daily_limited is null");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedIsNotNull() {
            addCriterion("daily_limited is not null");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedEqualTo(Integer value) {
            addCriterion("daily_limited =", value, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedNotEqualTo(Integer value) {
            addCriterion("daily_limited <>", value, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedGreaterThan(Integer value) {
            addCriterion("daily_limited >", value, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedGreaterThanOrEqualTo(Integer value) {
            addCriterion("daily_limited >=", value, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedLessThan(Integer value) {
            addCriterion("daily_limited <", value, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedLessThanOrEqualTo(Integer value) {
            addCriterion("daily_limited <=", value, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedIn(List<Integer> values) {
            addCriterion("daily_limited in", values, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedNotIn(List<Integer> values) {
            addCriterion("daily_limited not in", values, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedBetween(Integer value1, Integer value2) {
            addCriterion("daily_limited between", value1, value2, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andDailyLimitedNotBetween(Integer value1, Integer value2) {
            addCriterion("daily_limited not between", value1, value2, "dailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedIsNull() {
            addCriterion("match_daily_limited is null");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedIsNotNull() {
            addCriterion("match_daily_limited is not null");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedEqualTo(Integer value) {
            addCriterion("match_daily_limited =", value, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedNotEqualTo(Integer value) {
            addCriterion("match_daily_limited <>", value, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedGreaterThan(Integer value) {
            addCriterion("match_daily_limited >", value, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedGreaterThanOrEqualTo(Integer value) {
            addCriterion("match_daily_limited >=", value, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedLessThan(Integer value) {
            addCriterion("match_daily_limited <", value, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedLessThanOrEqualTo(Integer value) {
            addCriterion("match_daily_limited <=", value, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedIn(List<Integer> values) {
            addCriterion("match_daily_limited in", values, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedNotIn(List<Integer> values) {
            addCriterion("match_daily_limited not in", values, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedBetween(Integer value1, Integer value2) {
            addCriterion("match_daily_limited between", value1, value2, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andMatchDailyLimitedNotBetween(Integer value1, Integer value2) {
            addCriterion("match_daily_limited not between", value1, value2, "matchDailyLimited");
            return (Criteria) this;
        }

        public Criteria andDemandIdIsNull() {
            addCriterion("demand_id is null");
            return (Criteria) this;
        }

        public Criteria andDemandIdIsNotNull() {
            addCriterion("demand_id is not null");
            return (Criteria) this;
        }

        public Criteria andDemandIdEqualTo(String value) {
            addCriterion("demand_id =", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdNotEqualTo(String value) {
            addCriterion("demand_id <>", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdGreaterThan(String value) {
            addCriterion("demand_id >", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdGreaterThanOrEqualTo(String value) {
            addCriterion("demand_id >=", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdLessThan(String value) {
            addCriterion("demand_id <", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdLessThanOrEqualTo(String value) {
            addCriterion("demand_id <=", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdLike(String value) {
            addCriterion("demand_id like", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdNotLike(String value) {
            addCriterion("demand_id not like", value, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdIn(List<String> values) {
            addCriterion("demand_id in", values, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdNotIn(List<String> values) {
            addCriterion("demand_id not in", values, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdBetween(String value1, String value2) {
            addCriterion("demand_id between", value1, value2, "demandId");
            return (Criteria) this;
        }

        public Criteria andDemandIdNotBetween(String value1, String value2) {
            addCriterion("demand_id not between", value1, value2, "demandId");
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