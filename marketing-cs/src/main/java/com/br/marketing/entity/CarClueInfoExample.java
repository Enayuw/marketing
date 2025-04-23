package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarClueInfoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CarClueInfoExample() {
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

        public Criteria andCidIsNull() {
            addCriterion("cid is null");
            return (Criteria) this;
        }

        public Criteria andCidIsNotNull() {
            addCriterion("cid is not null");
            return (Criteria) this;
        }

        public Criteria andCidEqualTo(String value) {
            addCriterion("cid =", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotEqualTo(String value) {
            addCriterion("cid <>", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidGreaterThan(String value) {
            addCriterion("cid >", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidGreaterThanOrEqualTo(String value) {
            addCriterion("cid >=", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLessThan(String value) {
            addCriterion("cid <", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLessThanOrEqualTo(String value) {
            addCriterion("cid <=", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLike(String value) {
            addCriterion("cid like", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotLike(String value) {
            addCriterion("cid not like", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidIn(List<String> values) {
            addCriterion("cid in", values, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotIn(List<String> values) {
            addCriterion("cid not in", values, "cid");
            return (Criteria) this;
        }

        public Criteria andCidBetween(String value1, String value2) {
            addCriterion("cid between", value1, value2, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotBetween(String value1, String value2) {
            addCriterion("cid not between", value1, value2, "cid");
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

        public Criteria andCustNumIsNull() {
            addCriterion("cust_num is null");
            return (Criteria) this;
        }

        public Criteria andCustNumIsNotNull() {
            addCriterion("cust_num is not null");
            return (Criteria) this;
        }

        public Criteria andCustNumEqualTo(String value) {
            addCriterion("cust_num =", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotEqualTo(String value) {
            addCriterion("cust_num <>", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumGreaterThan(String value) {
            addCriterion("cust_num >", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumGreaterThanOrEqualTo(String value) {
            addCriterion("cust_num >=", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLessThan(String value) {
            addCriterion("cust_num <", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLessThanOrEqualTo(String value) {
            addCriterion("cust_num <=", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLike(String value) {
            addCriterion("cust_num like", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotLike(String value) {
            addCriterion("cust_num not like", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumIn(List<String> values) {
            addCriterion("cust_num in", values, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotIn(List<String> values) {
            addCriterion("cust_num not in", values, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumBetween(String value1, String value2) {
            addCriterion("cust_num between", value1, value2, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotBetween(String value1, String value2) {
            addCriterion("cust_num not between", value1, value2, "custNum");
            return (Criteria) this;
        }

        public Criteria andCellIsNull() {
            addCriterion("cell is null");
            return (Criteria) this;
        }

        public Criteria andCellIsNotNull() {
            addCriterion("cell is not null");
            return (Criteria) this;
        }

        public Criteria andCellEqualTo(String value) {
            addCriterion("cell =", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotEqualTo(String value) {
            addCriterion("cell <>", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellGreaterThan(String value) {
            addCriterion("cell >", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellGreaterThanOrEqualTo(String value) {
            addCriterion("cell >=", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLessThan(String value) {
            addCriterion("cell <", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLessThanOrEqualTo(String value) {
            addCriterion("cell <=", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLike(String value) {
            addCriterion("cell like", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotLike(String value) {
            addCriterion("cell not like", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellIn(List<String> values) {
            addCriterion("cell in", values, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotIn(List<String> values) {
            addCriterion("cell not in", values, "cell");
            return (Criteria) this;
        }

        public Criteria andCellBetween(String value1, String value2) {
            addCriterion("cell between", value1, value2, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotBetween(String value1, String value2) {
            addCriterion("cell not between", value1, value2, "cell");
            return (Criteria) this;
        }

        public Criteria andIntentionIsNull() {
            addCriterion("intention is null");
            return (Criteria) this;
        }

        public Criteria andIntentionIsNotNull() {
            addCriterion("intention is not null");
            return (Criteria) this;
        }

        public Criteria andIntentionEqualTo(String value) {
            addCriterion("intention =", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionNotEqualTo(String value) {
            addCriterion("intention <>", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionGreaterThan(String value) {
            addCriterion("intention >", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionGreaterThanOrEqualTo(String value) {
            addCriterion("intention >=", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionLessThan(String value) {
            addCriterion("intention <", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionLessThanOrEqualTo(String value) {
            addCriterion("intention <=", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionLike(String value) {
            addCriterion("intention like", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionNotLike(String value) {
            addCriterion("intention not like", value, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionIn(List<String> values) {
            addCriterion("intention in", values, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionNotIn(List<String> values) {
            addCriterion("intention not in", values, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionBetween(String value1, String value2) {
            addCriterion("intention between", value1, value2, "intention");
            return (Criteria) this;
        }

        public Criteria andIntentionNotBetween(String value1, String value2) {
            addCriterion("intention not between", value1, value2, "intention");
            return (Criteria) this;
        }

        public Criteria andBrandIsNull() {
            addCriterion("brand is null");
            return (Criteria) this;
        }

        public Criteria andBrandIsNotNull() {
            addCriterion("brand is not null");
            return (Criteria) this;
        }

        public Criteria andBrandEqualTo(String value) {
            addCriterion("brand =", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandNotEqualTo(String value) {
            addCriterion("brand <>", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandGreaterThan(String value) {
            addCriterion("brand >", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandGreaterThanOrEqualTo(String value) {
            addCriterion("brand >=", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandLessThan(String value) {
            addCriterion("brand <", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandLessThanOrEqualTo(String value) {
            addCriterion("brand <=", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandLike(String value) {
            addCriterion("brand like", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandNotLike(String value) {
            addCriterion("brand not like", value, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandIn(List<String> values) {
            addCriterion("brand in", values, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandNotIn(List<String> values) {
            addCriterion("brand not in", values, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandBetween(String value1, String value2) {
            addCriterion("brand between", value1, value2, "brand");
            return (Criteria) this;
        }

        public Criteria andBrandNotBetween(String value1, String value2) {
            addCriterion("brand not between", value1, value2, "brand");
            return (Criteria) this;
        }

        public Criteria andMemberIsNull() {
            addCriterion("`member` is null");
            return (Criteria) this;
        }

        public Criteria andMemberIsNotNull() {
            addCriterion("`member` is not null");
            return (Criteria) this;
        }

        public Criteria andMemberEqualTo(String value) {
            addCriterion("`member` =", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberNotEqualTo(String value) {
            addCriterion("`member` <>", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberGreaterThan(String value) {
            addCriterion("`member` >", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberGreaterThanOrEqualTo(String value) {
            addCriterion("`member` >=", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberLessThan(String value) {
            addCriterion("`member` <", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberLessThanOrEqualTo(String value) {
            addCriterion("`member` <=", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberLike(String value) {
            addCriterion("`member` like", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberNotLike(String value) {
            addCriterion("`member` not like", value, "member");
            return (Criteria) this;
        }

        public Criteria andMemberIn(List<String> values) {
            addCriterion("`member` in", values, "member");
            return (Criteria) this;
        }

        public Criteria andMemberNotIn(List<String> values) {
            addCriterion("`member` not in", values, "member");
            return (Criteria) this;
        }

        public Criteria andMemberBetween(String value1, String value2) {
            addCriterion("`member` between", value1, value2, "member");
            return (Criteria) this;
        }

        public Criteria andMemberNotBetween(String value1, String value2) {
            addCriterion("`member` not between", value1, value2, "member");
            return (Criteria) this;
        }

        public Criteria andSeriesIsNull() {
            addCriterion("series is null");
            return (Criteria) this;
        }

        public Criteria andSeriesIsNotNull() {
            addCriterion("series is not null");
            return (Criteria) this;
        }

        public Criteria andSeriesEqualTo(String value) {
            addCriterion("series =", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotEqualTo(String value) {
            addCriterion("series <>", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesGreaterThan(String value) {
            addCriterion("series >", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesGreaterThanOrEqualTo(String value) {
            addCriterion("series >=", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesLessThan(String value) {
            addCriterion("series <", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesLessThanOrEqualTo(String value) {
            addCriterion("series <=", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesLike(String value) {
            addCriterion("series like", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotLike(String value) {
            addCriterion("series not like", value, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesIn(List<String> values) {
            addCriterion("series in", values, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotIn(List<String> values) {
            addCriterion("series not in", values, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesBetween(String value1, String value2) {
            addCriterion("series between", value1, value2, "series");
            return (Criteria) this;
        }

        public Criteria andSeriesNotBetween(String value1, String value2) {
            addCriterion("series not between", value1, value2, "series");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdIsNull() {
            addCriterion("clue_match_brand_id is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdIsNotNull() {
            addCriterion("clue_match_brand_id is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdEqualTo(String value) {
            addCriterion("clue_match_brand_id =", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdNotEqualTo(String value) {
            addCriterion("clue_match_brand_id <>", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdGreaterThan(String value) {
            addCriterion("clue_match_brand_id >", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_brand_id >=", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdLessThan(String value) {
            addCriterion("clue_match_brand_id <", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdLessThanOrEqualTo(String value) {
            addCriterion("clue_match_brand_id <=", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdLike(String value) {
            addCriterion("clue_match_brand_id like", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdNotLike(String value) {
            addCriterion("clue_match_brand_id not like", value, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdIn(List<String> values) {
            addCriterion("clue_match_brand_id in", values, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdNotIn(List<String> values) {
            addCriterion("clue_match_brand_id not in", values, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdBetween(String value1, String value2) {
            addCriterion("clue_match_brand_id between", value1, value2, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIdNotBetween(String value1, String value2) {
            addCriterion("clue_match_brand_id not between", value1, value2, "clueMatchBrandId");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIsNull() {
            addCriterion("clue_match_brand is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIsNotNull() {
            addCriterion("clue_match_brand is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandEqualTo(String value) {
            addCriterion("clue_match_brand =", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandNotEqualTo(String value) {
            addCriterion("clue_match_brand <>", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandGreaterThan(String value) {
            addCriterion("clue_match_brand >", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_brand >=", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandLessThan(String value) {
            addCriterion("clue_match_brand <", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandLessThanOrEqualTo(String value) {
            addCriterion("clue_match_brand <=", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandLike(String value) {
            addCriterion("clue_match_brand like", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandNotLike(String value) {
            addCriterion("clue_match_brand not like", value, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandIn(List<String> values) {
            addCriterion("clue_match_brand in", values, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandNotIn(List<String> values) {
            addCriterion("clue_match_brand not in", values, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandBetween(String value1, String value2) {
            addCriterion("clue_match_brand between", value1, value2, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchBrandNotBetween(String value1, String value2) {
            addCriterion("clue_match_brand not between", value1, value2, "clueMatchBrand");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdIsNull() {
            addCriterion("clue_match_series_id is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdIsNotNull() {
            addCriterion("clue_match_series_id is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdEqualTo(String value) {
            addCriterion("clue_match_series_id =", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdNotEqualTo(String value) {
            addCriterion("clue_match_series_id <>", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdGreaterThan(String value) {
            addCriterion("clue_match_series_id >", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_series_id >=", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdLessThan(String value) {
            addCriterion("clue_match_series_id <", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdLessThanOrEqualTo(String value) {
            addCriterion("clue_match_series_id <=", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdLike(String value) {
            addCriterion("clue_match_series_id like", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdNotLike(String value) {
            addCriterion("clue_match_series_id not like", value, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdIn(List<String> values) {
            addCriterion("clue_match_series_id in", values, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdNotIn(List<String> values) {
            addCriterion("clue_match_series_id not in", values, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdBetween(String value1, String value2) {
            addCriterion("clue_match_series_id between", value1, value2, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIdNotBetween(String value1, String value2) {
            addCriterion("clue_match_series_id not between", value1, value2, "clueMatchSeriesId");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIsNull() {
            addCriterion("clue_match_series is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIsNotNull() {
            addCriterion("clue_match_series is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesEqualTo(String value) {
            addCriterion("clue_match_series =", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesNotEqualTo(String value) {
            addCriterion("clue_match_series <>", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesGreaterThan(String value) {
            addCriterion("clue_match_series >", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_series >=", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesLessThan(String value) {
            addCriterion("clue_match_series <", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesLessThanOrEqualTo(String value) {
            addCriterion("clue_match_series <=", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesLike(String value) {
            addCriterion("clue_match_series like", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesNotLike(String value) {
            addCriterion("clue_match_series not like", value, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesIn(List<String> values) {
            addCriterion("clue_match_series in", values, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesNotIn(List<String> values) {
            addCriterion("clue_match_series not in", values, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesBetween(String value1, String value2) {
            addCriterion("clue_match_series between", value1, value2, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andClueMatchSeriesNotBetween(String value1, String value2) {
            addCriterion("clue_match_series not between", value1, value2, "clueMatchSeries");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeIsNull() {
            addCriterion("match_brand_series_type is null");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeIsNotNull() {
            addCriterion("match_brand_series_type is not null");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeEqualTo(Integer value) {
            addCriterion("match_brand_series_type =", value, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeNotEqualTo(Integer value) {
            addCriterion("match_brand_series_type <>", value, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeGreaterThan(Integer value) {
            addCriterion("match_brand_series_type >", value, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("match_brand_series_type >=", value, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeLessThan(Integer value) {
            addCriterion("match_brand_series_type <", value, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeLessThanOrEqualTo(Integer value) {
            addCriterion("match_brand_series_type <=", value, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeIn(List<Integer> values) {
            addCriterion("match_brand_series_type in", values, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeNotIn(List<Integer> values) {
            addCriterion("match_brand_series_type not in", values, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeBetween(Integer value1, Integer value2) {
            addCriterion("match_brand_series_type between", value1, value2, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andMatchBrandSeriesTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("match_brand_series_type not between", value1, value2, "matchBrandSeriesType");
            return (Criteria) this;
        }

        public Criteria andProvinceIsNull() {
            addCriterion("province is null");
            return (Criteria) this;
        }

        public Criteria andProvinceIsNotNull() {
            addCriterion("province is not null");
            return (Criteria) this;
        }

        public Criteria andProvinceEqualTo(String value) {
            addCriterion("province =", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotEqualTo(String value) {
            addCriterion("province <>", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceGreaterThan(String value) {
            addCriterion("province >", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceGreaterThanOrEqualTo(String value) {
            addCriterion("province >=", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceLessThan(String value) {
            addCriterion("province <", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceLessThanOrEqualTo(String value) {
            addCriterion("province <=", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceLike(String value) {
            addCriterion("province like", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotLike(String value) {
            addCriterion("province not like", value, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceIn(List<String> values) {
            addCriterion("province in", values, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotIn(List<String> values) {
            addCriterion("province not in", values, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceBetween(String value1, String value2) {
            addCriterion("province between", value1, value2, "province");
            return (Criteria) this;
        }

        public Criteria andProvinceNotBetween(String value1, String value2) {
            addCriterion("province not between", value1, value2, "province");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdIsNull() {
            addCriterion("clue_match_province_id is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdIsNotNull() {
            addCriterion("clue_match_province_id is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdEqualTo(String value) {
            addCriterion("clue_match_province_id =", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdNotEqualTo(String value) {
            addCriterion("clue_match_province_id <>", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdGreaterThan(String value) {
            addCriterion("clue_match_province_id >", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_province_id >=", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdLessThan(String value) {
            addCriterion("clue_match_province_id <", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdLessThanOrEqualTo(String value) {
            addCriterion("clue_match_province_id <=", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdLike(String value) {
            addCriterion("clue_match_province_id like", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdNotLike(String value) {
            addCriterion("clue_match_province_id not like", value, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdIn(List<String> values) {
            addCriterion("clue_match_province_id in", values, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdNotIn(List<String> values) {
            addCriterion("clue_match_province_id not in", values, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdBetween(String value1, String value2) {
            addCriterion("clue_match_province_id between", value1, value2, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIdNotBetween(String value1, String value2) {
            addCriterion("clue_match_province_id not between", value1, value2, "clueMatchProvinceId");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIsNull() {
            addCriterion("clue_match_province is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIsNotNull() {
            addCriterion("clue_match_province is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceEqualTo(String value) {
            addCriterion("clue_match_province =", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceNotEqualTo(String value) {
            addCriterion("clue_match_province <>", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceGreaterThan(String value) {
            addCriterion("clue_match_province >", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_province >=", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceLessThan(String value) {
            addCriterion("clue_match_province <", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceLessThanOrEqualTo(String value) {
            addCriterion("clue_match_province <=", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceLike(String value) {
            addCriterion("clue_match_province like", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceNotLike(String value) {
            addCriterion("clue_match_province not like", value, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceIn(List<String> values) {
            addCriterion("clue_match_province in", values, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceNotIn(List<String> values) {
            addCriterion("clue_match_province not in", values, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceBetween(String value1, String value2) {
            addCriterion("clue_match_province between", value1, value2, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andClueMatchProvinceNotBetween(String value1, String value2) {
            addCriterion("clue_match_province not between", value1, value2, "clueMatchProvince");
            return (Criteria) this;
        }

        public Criteria andCityIsNull() {
            addCriterion("city is null");
            return (Criteria) this;
        }

        public Criteria andCityIsNotNull() {
            addCriterion("city is not null");
            return (Criteria) this;
        }

        public Criteria andCityEqualTo(String value) {
            addCriterion("city =", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotEqualTo(String value) {
            addCriterion("city <>", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityGreaterThan(String value) {
            addCriterion("city >", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityGreaterThanOrEqualTo(String value) {
            addCriterion("city >=", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLessThan(String value) {
            addCriterion("city <", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLessThanOrEqualTo(String value) {
            addCriterion("city <=", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityLike(String value) {
            addCriterion("city like", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotLike(String value) {
            addCriterion("city not like", value, "city");
            return (Criteria) this;
        }

        public Criteria andCityIn(List<String> values) {
            addCriterion("city in", values, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotIn(List<String> values) {
            addCriterion("city not in", values, "city");
            return (Criteria) this;
        }

        public Criteria andCityBetween(String value1, String value2) {
            addCriterion("city between", value1, value2, "city");
            return (Criteria) this;
        }

        public Criteria andCityNotBetween(String value1, String value2) {
            addCriterion("city not between", value1, value2, "city");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdIsNull() {
            addCriterion("clue_match_city_id is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdIsNotNull() {
            addCriterion("clue_match_city_id is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdEqualTo(String value) {
            addCriterion("clue_match_city_id =", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdNotEqualTo(String value) {
            addCriterion("clue_match_city_id <>", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdGreaterThan(String value) {
            addCriterion("clue_match_city_id >", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_city_id >=", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdLessThan(String value) {
            addCriterion("clue_match_city_id <", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdLessThanOrEqualTo(String value) {
            addCriterion("clue_match_city_id <=", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdLike(String value) {
            addCriterion("clue_match_city_id like", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdNotLike(String value) {
            addCriterion("clue_match_city_id not like", value, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdIn(List<String> values) {
            addCriterion("clue_match_city_id in", values, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdNotIn(List<String> values) {
            addCriterion("clue_match_city_id not in", values, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdBetween(String value1, String value2) {
            addCriterion("clue_match_city_id between", value1, value2, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIdNotBetween(String value1, String value2) {
            addCriterion("clue_match_city_id not between", value1, value2, "clueMatchCityId");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIsNull() {
            addCriterion("clue_match_city is null");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIsNotNull() {
            addCriterion("clue_match_city is not null");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityEqualTo(String value) {
            addCriterion("clue_match_city =", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityNotEqualTo(String value) {
            addCriterion("clue_match_city <>", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityGreaterThan(String value) {
            addCriterion("clue_match_city >", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityGreaterThanOrEqualTo(String value) {
            addCriterion("clue_match_city >=", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityLessThan(String value) {
            addCriterion("clue_match_city <", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityLessThanOrEqualTo(String value) {
            addCriterion("clue_match_city <=", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityLike(String value) {
            addCriterion("clue_match_city like", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityNotLike(String value) {
            addCriterion("clue_match_city not like", value, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityIn(List<String> values) {
            addCriterion("clue_match_city in", values, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityNotIn(List<String> values) {
            addCriterion("clue_match_city not in", values, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityBetween(String value1, String value2) {
            addCriterion("clue_match_city between", value1, value2, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andClueMatchCityNotBetween(String value1, String value2) {
            addCriterion("clue_match_city not between", value1, value2, "clueMatchCity");
            return (Criteria) this;
        }

        public Criteria andRecordingPathIsNull() {
            addCriterion("recording_path is null");
            return (Criteria) this;
        }

        public Criteria andRecordingPathIsNotNull() {
            addCriterion("recording_path is not null");
            return (Criteria) this;
        }

        public Criteria andRecordingPathEqualTo(String value) {
            addCriterion("recording_path =", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotEqualTo(String value) {
            addCriterion("recording_path <>", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathGreaterThan(String value) {
            addCriterion("recording_path >", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathGreaterThanOrEqualTo(String value) {
            addCriterion("recording_path >=", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathLessThan(String value) {
            addCriterion("recording_path <", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathLessThanOrEqualTo(String value) {
            addCriterion("recording_path <=", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathLike(String value) {
            addCriterion("recording_path like", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotLike(String value) {
            addCriterion("recording_path not like", value, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathIn(List<String> values) {
            addCriterion("recording_path in", values, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotIn(List<String> values) {
            addCriterion("recording_path not in", values, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathBetween(String value1, String value2) {
            addCriterion("recording_path between", value1, value2, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andRecordingPathNotBetween(String value1, String value2) {
            addCriterion("recording_path not between", value1, value2, "recordingPath");
            return (Criteria) this;
        }

        public Criteria andCallDialogIsNull() {
            addCriterion("call_dialog is null");
            return (Criteria) this;
        }

        public Criteria andCallDialogIsNotNull() {
            addCriterion("call_dialog is not null");
            return (Criteria) this;
        }

        public Criteria andCallDialogEqualTo(String value) {
            addCriterion("call_dialog =", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotEqualTo(String value) {
            addCriterion("call_dialog <>", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogGreaterThan(String value) {
            addCriterion("call_dialog >", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogGreaterThanOrEqualTo(String value) {
            addCriterion("call_dialog >=", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogLessThan(String value) {
            addCriterion("call_dialog <", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogLessThanOrEqualTo(String value) {
            addCriterion("call_dialog <=", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogLike(String value) {
            addCriterion("call_dialog like", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotLike(String value) {
            addCriterion("call_dialog not like", value, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogIn(List<String> values) {
            addCriterion("call_dialog in", values, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotIn(List<String> values) {
            addCriterion("call_dialog not in", values, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogBetween(String value1, String value2) {
            addCriterion("call_dialog between", value1, value2, "callDialog");
            return (Criteria) this;
        }

        public Criteria andCallDialogNotBetween(String value1, String value2) {
            addCriterion("call_dialog not between", value1, value2, "callDialog");
            return (Criteria) this;
        }

        public Criteria andClueIdIsNull() {
            addCriterion("clue_id is null");
            return (Criteria) this;
        }

        public Criteria andClueIdIsNotNull() {
            addCriterion("clue_id is not null");
            return (Criteria) this;
        }

        public Criteria andClueIdEqualTo(String value) {
            addCriterion("clue_id =", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdNotEqualTo(String value) {
            addCriterion("clue_id <>", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdGreaterThan(String value) {
            addCriterion("clue_id >", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdGreaterThanOrEqualTo(String value) {
            addCriterion("clue_id >=", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdLessThan(String value) {
            addCriterion("clue_id <", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdLessThanOrEqualTo(String value) {
            addCriterion("clue_id <=", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdLike(String value) {
            addCriterion("clue_id like", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdNotLike(String value) {
            addCriterion("clue_id not like", value, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdIn(List<String> values) {
            addCriterion("clue_id in", values, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdNotIn(List<String> values) {
            addCriterion("clue_id not in", values, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdBetween(String value1, String value2) {
            addCriterion("clue_id between", value1, value2, "clueId");
            return (Criteria) this;
        }

        public Criteria andClueIdNotBetween(String value1, String value2) {
            addCriterion("clue_id not between", value1, value2, "clueId");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelIsNull() {
            addCriterion("clue_push_channel is null");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelIsNotNull() {
            addCriterion("clue_push_channel is not null");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelEqualTo(String value) {
            addCriterion("clue_push_channel =", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelNotEqualTo(String value) {
            addCriterion("clue_push_channel <>", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelGreaterThan(String value) {
            addCriterion("clue_push_channel >", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelGreaterThanOrEqualTo(String value) {
            addCriterion("clue_push_channel >=", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelLessThan(String value) {
            addCriterion("clue_push_channel <", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelLessThanOrEqualTo(String value) {
            addCriterion("clue_push_channel <=", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelLike(String value) {
            addCriterion("clue_push_channel like", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelNotLike(String value) {
            addCriterion("clue_push_channel not like", value, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelIn(List<String> values) {
            addCriterion("clue_push_channel in", values, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelNotIn(List<String> values) {
            addCriterion("clue_push_channel not in", values, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelBetween(String value1, String value2) {
            addCriterion("clue_push_channel between", value1, value2, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andCluePushChannelNotBetween(String value1, String value2) {
            addCriterion("clue_push_channel not between", value1, value2, "cluePushChannel");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusIsNull() {
            addCriterion("clue_data_status is null");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusIsNotNull() {
            addCriterion("clue_data_status is not null");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusEqualTo(Integer value) {
            addCriterion("clue_data_status =", value, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusNotEqualTo(Integer value) {
            addCriterion("clue_data_status <>", value, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusGreaterThan(Integer value) {
            addCriterion("clue_data_status >", value, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("clue_data_status >=", value, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusLessThan(Integer value) {
            addCriterion("clue_data_status <", value, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusLessThanOrEqualTo(Integer value) {
            addCriterion("clue_data_status <=", value, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusIn(List<Integer> values) {
            addCriterion("clue_data_status in", values, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusNotIn(List<Integer> values) {
            addCriterion("clue_data_status not in", values, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusBetween(Integer value1, Integer value2) {
            addCriterion("clue_data_status between", value1, value2, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueDataStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("clue_data_status not between", value1, value2, "clueDataStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusIsNull() {
            addCriterion("clue_complete_status is null");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusIsNotNull() {
            addCriterion("clue_complete_status is not null");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusEqualTo(Integer value) {
            addCriterion("clue_complete_status =", value, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusNotEqualTo(Integer value) {
            addCriterion("clue_complete_status <>", value, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusGreaterThan(Integer value) {
            addCriterion("clue_complete_status >", value, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("clue_complete_status >=", value, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusLessThan(Integer value) {
            addCriterion("clue_complete_status <", value, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusLessThanOrEqualTo(Integer value) {
            addCriterion("clue_complete_status <=", value, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusIn(List<Integer> values) {
            addCriterion("clue_complete_status in", values, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusNotIn(List<Integer> values) {
            addCriterion("clue_complete_status not in", values, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusBetween(Integer value1, Integer value2) {
            addCriterion("clue_complete_status between", value1, value2, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andClueCompleteStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("clue_complete_status not between", value1, value2, "clueCompleteStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusIsNull() {
            addCriterion("clue_push_status is null");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusIsNotNull() {
            addCriterion("clue_push_status is not null");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusEqualTo(Integer value) {
            addCriterion("clue_push_status =", value, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusNotEqualTo(Integer value) {
            addCriterion("clue_push_status <>", value, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusGreaterThan(Integer value) {
            addCriterion("clue_push_status >", value, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("clue_push_status >=", value, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusLessThan(Integer value) {
            addCriterion("clue_push_status <", value, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusLessThanOrEqualTo(Integer value) {
            addCriterion("clue_push_status <=", value, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusIn(List<Integer> values) {
            addCriterion("clue_push_status in", values, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusNotIn(List<Integer> values) {
            addCriterion("clue_push_status not in", values, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusBetween(Integer value1, Integer value2) {
            addCriterion("clue_push_status between", value1, value2, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("clue_push_status not between", value1, value2, "cluePushStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusIsNull() {
            addCriterion("clue_callback_status is null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusIsNotNull() {
            addCriterion("clue_callback_status is not null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusEqualTo(Integer value) {
            addCriterion("clue_callback_status =", value, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusNotEqualTo(Integer value) {
            addCriterion("clue_callback_status <>", value, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusGreaterThan(Integer value) {
            addCriterion("clue_callback_status >", value, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("clue_callback_status >=", value, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusLessThan(Integer value) {
            addCriterion("clue_callback_status <", value, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusLessThanOrEqualTo(Integer value) {
            addCriterion("clue_callback_status <=", value, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusIn(List<Integer> values) {
            addCriterion("clue_callback_status in", values, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusNotIn(List<Integer> values) {
            addCriterion("clue_callback_status not in", values, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusBetween(Integer value1, Integer value2) {
            addCriterion("clue_callback_status between", value1, value2, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andClueCallbackStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("clue_callback_status not between", value1, value2, "clueCallbackStatus");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonIsNull() {
            addCriterion("clue_push_error_reason is null");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonIsNotNull() {
            addCriterion("clue_push_error_reason is not null");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonEqualTo(String value) {
            addCriterion("clue_push_error_reason =", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonNotEqualTo(String value) {
            addCriterion("clue_push_error_reason <>", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonGreaterThan(String value) {
            addCriterion("clue_push_error_reason >", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonGreaterThanOrEqualTo(String value) {
            addCriterion("clue_push_error_reason >=", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonLessThan(String value) {
            addCriterion("clue_push_error_reason <", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonLessThanOrEqualTo(String value) {
            addCriterion("clue_push_error_reason <=", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonLike(String value) {
            addCriterion("clue_push_error_reason like", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonNotLike(String value) {
            addCriterion("clue_push_error_reason not like", value, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonIn(List<String> values) {
            addCriterion("clue_push_error_reason in", values, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonNotIn(List<String> values) {
            addCriterion("clue_push_error_reason not in", values, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonBetween(String value1, String value2) {
            addCriterion("clue_push_error_reason between", value1, value2, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andCluePushErrorReasonNotBetween(String value1, String value2) {
            addCriterion("clue_push_error_reason not between", value1, value2, "cluePushErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonIsNull() {
            addCriterion("clue_error_reason is null");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonIsNotNull() {
            addCriterion("clue_error_reason is not null");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonEqualTo(String value) {
            addCriterion("clue_error_reason =", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonNotEqualTo(String value) {
            addCriterion("clue_error_reason <>", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonGreaterThan(String value) {
            addCriterion("clue_error_reason >", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonGreaterThanOrEqualTo(String value) {
            addCriterion("clue_error_reason >=", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonLessThan(String value) {
            addCriterion("clue_error_reason <", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonLessThanOrEqualTo(String value) {
            addCriterion("clue_error_reason <=", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonLike(String value) {
            addCriterion("clue_error_reason like", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonNotLike(String value) {
            addCriterion("clue_error_reason not like", value, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonIn(List<String> values) {
            addCriterion("clue_error_reason in", values, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonNotIn(List<String> values) {
            addCriterion("clue_error_reason not in", values, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonBetween(String value1, String value2) {
            addCriterion("clue_error_reason between", value1, value2, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueErrorReasonNotBetween(String value1, String value2) {
            addCriterion("clue_error_reason not between", value1, value2, "clueErrorReason");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultIsNull() {
            addCriterion("clue_callback_result is null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultIsNotNull() {
            addCriterion("clue_callback_result is not null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultEqualTo(String value) {
            addCriterion("clue_callback_result =", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultNotEqualTo(String value) {
            addCriterion("clue_callback_result <>", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultGreaterThan(String value) {
            addCriterion("clue_callback_result >", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultGreaterThanOrEqualTo(String value) {
            addCriterion("clue_callback_result >=", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultLessThan(String value) {
            addCriterion("clue_callback_result <", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultLessThanOrEqualTo(String value) {
            addCriterion("clue_callback_result <=", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultLike(String value) {
            addCriterion("clue_callback_result like", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultNotLike(String value) {
            addCriterion("clue_callback_result not like", value, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultIn(List<String> values) {
            addCriterion("clue_callback_result in", values, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultNotIn(List<String> values) {
            addCriterion("clue_callback_result not in", values, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultBetween(String value1, String value2) {
            addCriterion("clue_callback_result between", value1, value2, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackResultNotBetween(String value1, String value2) {
            addCriterion("clue_callback_result not between", value1, value2, "clueCallbackResult");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateIsNull() {
            addCriterion("clue_callback_push_state is null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateIsNotNull() {
            addCriterion("clue_callback_push_state is not null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateEqualTo(Integer value) {
            addCriterion("clue_callback_push_state =", value, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateNotEqualTo(Integer value) {
            addCriterion("clue_callback_push_state <>", value, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateGreaterThan(Integer value) {
            addCriterion("clue_callback_push_state >", value, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateGreaterThanOrEqualTo(Integer value) {
            addCriterion("clue_callback_push_state >=", value, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateLessThan(Integer value) {
            addCriterion("clue_callback_push_state <", value, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateLessThanOrEqualTo(Integer value) {
            addCriterion("clue_callback_push_state <=", value, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateIn(List<Integer> values) {
            addCriterion("clue_callback_push_state in", values, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateNotIn(List<Integer> values) {
            addCriterion("clue_callback_push_state not in", values, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateBetween(Integer value1, Integer value2) {
            addCriterion("clue_callback_push_state between", value1, value2, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackPushStateNotBetween(Integer value1, Integer value2) {
            addCriterion("clue_callback_push_state not between", value1, value2, "clueCallbackPushState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateIsNull() {
            addCriterion("clue_callback_final_state is null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateIsNotNull() {
            addCriterion("clue_callback_final_state is not null");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateEqualTo(Integer value) {
            addCriterion("clue_callback_final_state =", value, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateNotEqualTo(Integer value) {
            addCriterion("clue_callback_final_state <>", value, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateGreaterThan(Integer value) {
            addCriterion("clue_callback_final_state >", value, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateGreaterThanOrEqualTo(Integer value) {
            addCriterion("clue_callback_final_state >=", value, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateLessThan(Integer value) {
            addCriterion("clue_callback_final_state <", value, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateLessThanOrEqualTo(Integer value) {
            addCriterion("clue_callback_final_state <=", value, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateIn(List<Integer> values) {
            addCriterion("clue_callback_final_state in", values, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateNotIn(List<Integer> values) {
            addCriterion("clue_callback_final_state not in", values, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateBetween(Integer value1, Integer value2) {
            addCriterion("clue_callback_final_state between", value1, value2, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andClueCallbackFinalStateNotBetween(Integer value1, Integer value2) {
            addCriterion("clue_callback_final_state not between", value1, value2, "clueCallbackFinalState");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIsNull() {
            addCriterion("resource_type is null");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIsNotNull() {
            addCriterion("resource_type is not null");
            return (Criteria) this;
        }

        public Criteria andResourceTypeEqualTo(String value) {
            addCriterion("resource_type =", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotEqualTo(String value) {
            addCriterion("resource_type <>", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeGreaterThan(String value) {
            addCriterion("resource_type >", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeGreaterThanOrEqualTo(String value) {
            addCriterion("resource_type >=", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeLessThan(String value) {
            addCriterion("resource_type <", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeLessThanOrEqualTo(String value) {
            addCriterion("resource_type <=", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeLike(String value) {
            addCriterion("resource_type like", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotLike(String value) {
            addCriterion("resource_type not like", value, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeIn(List<String> values) {
            addCriterion("resource_type in", values, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotIn(List<String> values) {
            addCriterion("resource_type not in", values, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeBetween(String value1, String value2) {
            addCriterion("resource_type between", value1, value2, "resourceType");
            return (Criteria) this;
        }

        public Criteria andResourceTypeNotBetween(String value1, String value2) {
            addCriterion("resource_type not between", value1, value2, "resourceType");
            return (Criteria) this;
        }

        public Criteria andExtendInfoIsNull() {
            addCriterion("extend_info is null");
            return (Criteria) this;
        }

        public Criteria andExtendInfoIsNotNull() {
            addCriterion("extend_info is not null");
            return (Criteria) this;
        }

        public Criteria andExtendInfoEqualTo(String value) {
            addCriterion("extend_info =", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotEqualTo(String value) {
            addCriterion("extend_info <>", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoGreaterThan(String value) {
            addCriterion("extend_info >", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoGreaterThanOrEqualTo(String value) {
            addCriterion("extend_info >=", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoLessThan(String value) {
            addCriterion("extend_info <", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoLessThanOrEqualTo(String value) {
            addCriterion("extend_info <=", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoLike(String value) {
            addCriterion("extend_info like", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotLike(String value) {
            addCriterion("extend_info not like", value, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoIn(List<String> values) {
            addCriterion("extend_info in", values, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotIn(List<String> values) {
            addCriterion("extend_info not in", values, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoBetween(String value1, String value2) {
            addCriterion("extend_info between", value1, value2, "extendInfo");
            return (Criteria) this;
        }

        public Criteria andExtendInfoNotBetween(String value1, String value2) {
            addCriterion("extend_info not between", value1, value2, "extendInfo");
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

        public Criteria andCleanTimeIsNull() {
            addCriterion("clean_time is null");
            return (Criteria) this;
        }

        public Criteria andCleanTimeIsNotNull() {
            addCriterion("clean_time is not null");
            return (Criteria) this;
        }

        public Criteria andCleanTimeEqualTo(Date value) {
            addCriterion("clean_time =", value, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeNotEqualTo(Date value) {
            addCriterion("clean_time <>", value, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeGreaterThan(Date value) {
            addCriterion("clean_time >", value, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("clean_time >=", value, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeLessThan(Date value) {
            addCriterion("clean_time <", value, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeLessThanOrEqualTo(Date value) {
            addCriterion("clean_time <=", value, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeIn(List<Date> values) {
            addCriterion("clean_time in", values, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeNotIn(List<Date> values) {
            addCriterion("clean_time not in", values, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeBetween(Date value1, Date value2) {
            addCriterion("clean_time between", value1, value2, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andCleanTimeNotBetween(Date value1, Date value2) {
            addCriterion("clean_time not between", value1, value2, "cleanTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeIsNull() {
            addCriterion("push_time is null");
            return (Criteria) this;
        }

        public Criteria andPushTimeIsNotNull() {
            addCriterion("push_time is not null");
            return (Criteria) this;
        }

        public Criteria andPushTimeEqualTo(Date value) {
            addCriterion("push_time =", value, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeNotEqualTo(Date value) {
            addCriterion("push_time <>", value, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeGreaterThan(Date value) {
            addCriterion("push_time >", value, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("push_time >=", value, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeLessThan(Date value) {
            addCriterion("push_time <", value, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeLessThanOrEqualTo(Date value) {
            addCriterion("push_time <=", value, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeIn(List<Date> values) {
            addCriterion("push_time in", values, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeNotIn(List<Date> values) {
            addCriterion("push_time not in", values, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeBetween(Date value1, Date value2) {
            addCriterion("push_time between", value1, value2, "pushTime");
            return (Criteria) this;
        }

        public Criteria andPushTimeNotBetween(Date value1, Date value2) {
            addCriterion("push_time not between", value1, value2, "pushTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeIsNull() {
            addCriterion("call_back_time is null");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeIsNotNull() {
            addCriterion("call_back_time is not null");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeEqualTo(Date value) {
            addCriterion("call_back_time =", value, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeNotEqualTo(Date value) {
            addCriterion("call_back_time <>", value, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeGreaterThan(Date value) {
            addCriterion("call_back_time >", value, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("call_back_time >=", value, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeLessThan(Date value) {
            addCriterion("call_back_time <", value, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeLessThanOrEqualTo(Date value) {
            addCriterion("call_back_time <=", value, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeIn(List<Date> values) {
            addCriterion("call_back_time in", values, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeNotIn(List<Date> values) {
            addCriterion("call_back_time not in", values, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeBetween(Date value1, Date value2) {
            addCriterion("call_back_time between", value1, value2, "callBackTime");
            return (Criteria) this;
        }

        public Criteria andCallBackTimeNotBetween(Date value1, Date value2) {
            addCriterion("call_back_time not between", value1, value2, "callBackTime");
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

        public Criteria andCallIdIsNull() {
            addCriterion("call_id is null");
            return (Criteria) this;
        }

        public Criteria andCallIdIsNotNull() {
            addCriterion("call_id is not null");
            return (Criteria) this;
        }

        public Criteria andCallIdEqualTo(String value) {
            addCriterion("call_id =", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdNotEqualTo(String value) {
            addCriterion("call_id <>", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdGreaterThan(String value) {
            addCriterion("call_id >", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdGreaterThanOrEqualTo(String value) {
            addCriterion("call_id >=", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdLessThan(String value) {
            addCriterion("call_id <", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdLessThanOrEqualTo(String value) {
            addCriterion("call_id <=", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdLike(String value) {
            addCriterion("call_id like", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdNotLike(String value) {
            addCriterion("call_id not like", value, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdIn(List<String> values) {
            addCriterion("call_id in", values, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdNotIn(List<String> values) {
            addCriterion("call_id not in", values, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdBetween(String value1, String value2) {
            addCriterion("call_id between", value1, value2, "callId");
            return (Criteria) this;
        }

        public Criteria andCallIdNotBetween(String value1, String value2) {
            addCriterion("call_id not between", value1, value2, "callId");
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