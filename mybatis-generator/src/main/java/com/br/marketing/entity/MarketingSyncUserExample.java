package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingSyncUserExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingSyncUserExample() {
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

        public Criteria andCusBatchIsNull() {
            addCriterion("cus_batch is null");
            return (Criteria) this;
        }

        public Criteria andCusBatchIsNotNull() {
            addCriterion("cus_batch is not null");
            return (Criteria) this;
        }

        public Criteria andCusBatchEqualTo(String value) {
            addCriterion("cus_batch =", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotEqualTo(String value) {
            addCriterion("cus_batch <>", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchGreaterThan(String value) {
            addCriterion("cus_batch >", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchGreaterThanOrEqualTo(String value) {
            addCriterion("cus_batch >=", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchLessThan(String value) {
            addCriterion("cus_batch <", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchLessThanOrEqualTo(String value) {
            addCriterion("cus_batch <=", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchLike(String value) {
            addCriterion("cus_batch like", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotLike(String value) {
            addCriterion("cus_batch not like", value, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchIn(List<String> values) {
            addCriterion("cus_batch in", values, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotIn(List<String> values) {
            addCriterion("cus_batch not in", values, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchBetween(String value1, String value2) {
            addCriterion("cus_batch between", value1, value2, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andCusBatchNotBetween(String value1, String value2) {
            addCriterion("cus_batch not between", value1, value2, "cusBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchIsNull() {
            addCriterion("request_batch is null");
            return (Criteria) this;
        }

        public Criteria andRequestBatchIsNotNull() {
            addCriterion("request_batch is not null");
            return (Criteria) this;
        }

        public Criteria andRequestBatchEqualTo(String value) {
            addCriterion("request_batch =", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchNotEqualTo(String value) {
            addCriterion("request_batch <>", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchGreaterThan(String value) {
            addCriterion("request_batch >", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchGreaterThanOrEqualTo(String value) {
            addCriterion("request_batch >=", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchLessThan(String value) {
            addCriterion("request_batch <", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchLessThanOrEqualTo(String value) {
            addCriterion("request_batch <=", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchLike(String value) {
            addCriterion("request_batch like", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchNotLike(String value) {
            addCriterion("request_batch not like", value, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchIn(List<String> values) {
            addCriterion("request_batch in", values, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchNotIn(List<String> values) {
            addCriterion("request_batch not in", values, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchBetween(String value1, String value2) {
            addCriterion("request_batch between", value1, value2, "requestBatch");
            return (Criteria) this;
        }

        public Criteria andRequestBatchNotBetween(String value1, String value2) {
            addCriterion("request_batch not between", value1, value2, "requestBatch");
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

        public Criteria andIdCardIsNull() {
            addCriterion("id_card is null");
            return (Criteria) this;
        }

        public Criteria andIdCardIsNotNull() {
            addCriterion("id_card is not null");
            return (Criteria) this;
        }

        public Criteria andIdCardEqualTo(String value) {
            addCriterion("id_card =", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotEqualTo(String value) {
            addCriterion("id_card <>", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardGreaterThan(String value) {
            addCriterion("id_card >", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardGreaterThanOrEqualTo(String value) {
            addCriterion("id_card >=", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardLessThan(String value) {
            addCriterion("id_card <", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardLessThanOrEqualTo(String value) {
            addCriterion("id_card <=", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardLike(String value) {
            addCriterion("id_card like", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotLike(String value) {
            addCriterion("id_card not like", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardIn(List<String> values) {
            addCriterion("id_card in", values, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotIn(List<String> values) {
            addCriterion("id_card not in", values, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardBetween(String value1, String value2) {
            addCriterion("id_card between", value1, value2, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotBetween(String value1, String value2) {
            addCriterion("id_card not between", value1, value2, "idCard");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
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

        public Criteria andCellMd5IsNull() {
            addCriterion("cell_md5 is null");
            return (Criteria) this;
        }

        public Criteria andCellMd5IsNotNull() {
            addCriterion("cell_md5 is not null");
            return (Criteria) this;
        }

        public Criteria andCellMd5EqualTo(String value) {
            addCriterion("cell_md5 =", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotEqualTo(String value) {
            addCriterion("cell_md5 <>", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5GreaterThan(String value) {
            addCriterion("cell_md5 >", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5GreaterThanOrEqualTo(String value) {
            addCriterion("cell_md5 >=", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5LessThan(String value) {
            addCriterion("cell_md5 <", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5LessThanOrEqualTo(String value) {
            addCriterion("cell_md5 <=", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5Like(String value) {
            addCriterion("cell_md5 like", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotLike(String value) {
            addCriterion("cell_md5 not like", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5In(List<String> values) {
            addCriterion("cell_md5 in", values, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotIn(List<String> values) {
            addCriterion("cell_md5 not in", values, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5Between(String value1, String value2) {
            addCriterion("cell_md5 between", value1, value2, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotBetween(String value1, String value2) {
            addCriterion("cell_md5 not between", value1, value2, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellSha256IsNull() {
            addCriterion("cell_sha256 is null");
            return (Criteria) this;
        }

        public Criteria andCellSha256IsNotNull() {
            addCriterion("cell_sha256 is not null");
            return (Criteria) this;
        }

        public Criteria andCellSha256EqualTo(String value) {
            addCriterion("cell_sha256 =", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotEqualTo(String value) {
            addCriterion("cell_sha256 <>", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256GreaterThan(String value) {
            addCriterion("cell_sha256 >", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256GreaterThanOrEqualTo(String value) {
            addCriterion("cell_sha256 >=", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256LessThan(String value) {
            addCriterion("cell_sha256 <", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256LessThanOrEqualTo(String value) {
            addCriterion("cell_sha256 <=", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256Like(String value) {
            addCriterion("cell_sha256 like", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotLike(String value) {
            addCriterion("cell_sha256 not like", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256In(List<String> values) {
            addCriterion("cell_sha256 in", values, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotIn(List<String> values) {
            addCriterion("cell_sha256 not in", values, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256Between(String value1, String value2) {
            addCriterion("cell_sha256 between", value1, value2, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotBetween(String value1, String value2) {
            addCriterion("cell_sha256 not between", value1, value2, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIsNull() {
            addCriterion("group_type is null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIsNotNull() {
            addCriterion("group_type is not null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeEqualTo(String value) {
            addCriterion("group_type =", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotEqualTo(String value) {
            addCriterion("group_type <>", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThan(String value) {
            addCriterion("group_type >", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeGreaterThanOrEqualTo(String value) {
            addCriterion("group_type >=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThan(String value) {
            addCriterion("group_type <", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLessThanOrEqualTo(String value) {
            addCriterion("group_type <=", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeLike(String value) {
            addCriterion("group_type like", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotLike(String value) {
            addCriterion("group_type not like", value, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeIn(List<String> values) {
            addCriterion("group_type in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotIn(List<String> values) {
            addCriterion("group_type not in", values, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeBetween(String value1, String value2) {
            addCriterion("group_type between", value1, value2, "groupType");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNotBetween(String value1, String value2) {
            addCriterion("group_type not between", value1, value2, "groupType");
            return (Criteria) this;
        }

        public Criteria andUserTypeIsNull() {
            addCriterion("user_type is null");
            return (Criteria) this;
        }

        public Criteria andUserTypeIsNotNull() {
            addCriterion("user_type is not null");
            return (Criteria) this;
        }

        public Criteria andUserTypeEqualTo(String value) {
            addCriterion("user_type =", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotEqualTo(String value) {
            addCriterion("user_type <>", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThan(String value) {
            addCriterion("user_type >", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeGreaterThanOrEqualTo(String value) {
            addCriterion("user_type >=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThan(String value) {
            addCriterion("user_type <", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLessThanOrEqualTo(String value) {
            addCriterion("user_type <=", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeLike(String value) {
            addCriterion("user_type like", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotLike(String value) {
            addCriterion("user_type not like", value, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeIn(List<String> values) {
            addCriterion("user_type in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotIn(List<String> values) {
            addCriterion("user_type not in", values, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeBetween(String value1, String value2) {
            addCriterion("user_type between", value1, value2, "userType");
            return (Criteria) this;
        }

        public Criteria andUserTypeNotBetween(String value1, String value2) {
            addCriterion("user_type not between", value1, value2, "userType");
            return (Criteria) this;
        }

        public Criteria andRegisterDateIsNull() {
            addCriterion("register_date is null");
            return (Criteria) this;
        }

        public Criteria andRegisterDateIsNotNull() {
            addCriterion("register_date is not null");
            return (Criteria) this;
        }

        public Criteria andRegisterDateEqualTo(String value) {
            addCriterion("register_date =", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateNotEqualTo(String value) {
            addCriterion("register_date <>", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateGreaterThan(String value) {
            addCriterion("register_date >", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateGreaterThanOrEqualTo(String value) {
            addCriterion("register_date >=", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateLessThan(String value) {
            addCriterion("register_date <", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateLessThanOrEqualTo(String value) {
            addCriterion("register_date <=", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateLike(String value) {
            addCriterion("register_date like", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateNotLike(String value) {
            addCriterion("register_date not like", value, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateIn(List<String> values) {
            addCriterion("register_date in", values, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateNotIn(List<String> values) {
            addCriterion("register_date not in", values, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateBetween(String value1, String value2) {
            addCriterion("register_date between", value1, value2, "registerDate");
            return (Criteria) this;
        }

        public Criteria andRegisterDateNotBetween(String value1, String value2) {
            addCriterion("register_date not between", value1, value2, "registerDate");
            return (Criteria) this;
        }

        public Criteria andReserveField1IsNull() {
            addCriterion("reserve_field1 is null");
            return (Criteria) this;
        }

        public Criteria andReserveField1IsNotNull() {
            addCriterion("reserve_field1 is not null");
            return (Criteria) this;
        }

        public Criteria andReserveField1EqualTo(String value) {
            addCriterion("reserve_field1 =", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotEqualTo(String value) {
            addCriterion("reserve_field1 <>", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1GreaterThan(String value) {
            addCriterion("reserve_field1 >", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1GreaterThanOrEqualTo(String value) {
            addCriterion("reserve_field1 >=", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1LessThan(String value) {
            addCriterion("reserve_field1 <", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1LessThanOrEqualTo(String value) {
            addCriterion("reserve_field1 <=", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1Like(String value) {
            addCriterion("reserve_field1 like", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotLike(String value) {
            addCriterion("reserve_field1 not like", value, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1In(List<String> values) {
            addCriterion("reserve_field1 in", values, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotIn(List<String> values) {
            addCriterion("reserve_field1 not in", values, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1Between(String value1, String value2) {
            addCriterion("reserve_field1 between", value1, value2, "reserveField1");
            return (Criteria) this;
        }

        public Criteria andReserveField1NotBetween(String value1, String value2) {
            addCriterion("reserve_field1 not between", value1, value2, "reserveField1");
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

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andFailTypeIsNull() {
            addCriterion("fail_type is null");
            return (Criteria) this;
        }

        public Criteria andFailTypeIsNotNull() {
            addCriterion("fail_type is not null");
            return (Criteria) this;
        }

        public Criteria andFailTypeEqualTo(String value) {
            addCriterion("fail_type =", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeNotEqualTo(String value) {
            addCriterion("fail_type <>", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeGreaterThan(String value) {
            addCriterion("fail_type >", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeGreaterThanOrEqualTo(String value) {
            addCriterion("fail_type >=", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeLessThan(String value) {
            addCriterion("fail_type <", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeLessThanOrEqualTo(String value) {
            addCriterion("fail_type <=", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeLike(String value) {
            addCriterion("fail_type like", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeNotLike(String value) {
            addCriterion("fail_type not like", value, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeIn(List<String> values) {
            addCriterion("fail_type in", values, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeNotIn(List<String> values) {
            addCriterion("fail_type not in", values, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeBetween(String value1, String value2) {
            addCriterion("fail_type between", value1, value2, "failType");
            return (Criteria) this;
        }

        public Criteria andFailTypeNotBetween(String value1, String value2) {
            addCriterion("fail_type not between", value1, value2, "failType");
            return (Criteria) this;
        }

        public Criteria andAppletTimeIsNull() {
            addCriterion("applet_time is null");
            return (Criteria) this;
        }

        public Criteria andAppletTimeIsNotNull() {
            addCriterion("applet_time is not null");
            return (Criteria) this;
        }

        public Criteria andAppletTimeEqualTo(Date value) {
            addCriterion("applet_time =", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeNotEqualTo(Date value) {
            addCriterion("applet_time <>", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeGreaterThan(Date value) {
            addCriterion("applet_time >", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("applet_time >=", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeLessThan(Date value) {
            addCriterion("applet_time <", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeLessThanOrEqualTo(Date value) {
            addCriterion("applet_time <=", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeIn(List<Date> values) {
            addCriterion("applet_time in", values, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeNotIn(List<Date> values) {
            addCriterion("applet_time not in", values, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeBetween(Date value1, Date value2) {
            addCriterion("applet_time between", value1, value2, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeNotBetween(Date value1, Date value2) {
            addCriterion("applet_time not between", value1, value2, "appletTime");
            return (Criteria) this;
        }

        public Criteria andIsTaskIsNull() {
            addCriterion("is_task is null");
            return (Criteria) this;
        }

        public Criteria andIsTaskIsNotNull() {
            addCriterion("is_task is not null");
            return (Criteria) this;
        }

        public Criteria andIsTaskEqualTo(Integer value) {
            addCriterion("is_task =", value, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskNotEqualTo(Integer value) {
            addCriterion("is_task <>", value, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskGreaterThan(Integer value) {
            addCriterion("is_task >", value, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_task >=", value, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskLessThan(Integer value) {
            addCriterion("is_task <", value, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskLessThanOrEqualTo(Integer value) {
            addCriterion("is_task <=", value, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskIn(List<Integer> values) {
            addCriterion("is_task in", values, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskNotIn(List<Integer> values) {
            addCriterion("is_task not in", values, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskBetween(Integer value1, Integer value2) {
            addCriterion("is_task between", value1, value2, "isTask");
            return (Criteria) this;
        }

        public Criteria andIsTaskNotBetween(Integer value1, Integer value2) {
            addCriterion("is_task not between", value1, value2, "isTask");
            return (Criteria) this;
        }

        public Criteria andTaskTimeIsNull() {
            addCriterion("task_time is null");
            return (Criteria) this;
        }

        public Criteria andTaskTimeIsNotNull() {
            addCriterion("task_time is not null");
            return (Criteria) this;
        }

        public Criteria andTaskTimeEqualTo(Date value) {
            addCriterion("task_time =", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotEqualTo(Date value) {
            addCriterion("task_time <>", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeGreaterThan(Date value) {
            addCriterion("task_time >", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("task_time >=", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeLessThan(Date value) {
            addCriterion("task_time <", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeLessThanOrEqualTo(Date value) {
            addCriterion("task_time <=", value, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeIn(List<Date> values) {
            addCriterion("task_time in", values, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotIn(List<Date> values) {
            addCriterion("task_time not in", values, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeBetween(Date value1, Date value2) {
            addCriterion("task_time between", value1, value2, "taskTime");
            return (Criteria) this;
        }

        public Criteria andTaskTimeNotBetween(Date value1, Date value2) {
            addCriterion("task_time not between", value1, value2, "taskTime");
            return (Criteria) this;
        }

        public Criteria andIsRepeatIsNull() {
            addCriterion("is_repeat is null");
            return (Criteria) this;
        }

        public Criteria andIsRepeatIsNotNull() {
            addCriterion("is_repeat is not null");
            return (Criteria) this;
        }

        public Criteria andIsRepeatEqualTo(Integer value) {
            addCriterion("is_repeat =", value, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatNotEqualTo(Integer value) {
            addCriterion("is_repeat <>", value, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatGreaterThan(Integer value) {
            addCriterion("is_repeat >", value, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_repeat >=", value, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatLessThan(Integer value) {
            addCriterion("is_repeat <", value, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatLessThanOrEqualTo(Integer value) {
            addCriterion("is_repeat <=", value, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatIn(List<Integer> values) {
            addCriterion("is_repeat in", values, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatNotIn(List<Integer> values) {
            addCriterion("is_repeat not in", values, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatBetween(Integer value1, Integer value2) {
            addCriterion("is_repeat between", value1, value2, "isRepeat");
            return (Criteria) this;
        }

        public Criteria andIsRepeatNotBetween(Integer value1, Integer value2) {
            addCriterion("is_repeat not between", value1, value2, "isRepeat");
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