package com.br.marketing.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerInfoPushMainExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CustomerInfoPushMainExample() {
        oredCriteria = new ArrayList<>();
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
            criteria = new ArrayList<>();
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

        public Criteria andMApiCodeIsNull() {
            addCriterion("m_api_code is null");
            return (Criteria) this;
        }

        public Criteria andMApiCodeIsNotNull() {
            addCriterion("m_api_code is not null");
            return (Criteria) this;
        }

        public Criteria andMApiCodeEqualTo(String value) {
            addCriterion("m_api_code =", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotEqualTo(String value) {
            addCriterion("m_api_code <>", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeGreaterThan(String value) {
            addCriterion("m_api_code >", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("m_api_code >=", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeLessThan(String value) {
            addCriterion("m_api_code <", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeLessThanOrEqualTo(String value) {
            addCriterion("m_api_code <=", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeLike(String value) {
            addCriterion("m_api_code like", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotLike(String value) {
            addCriterion("m_api_code not like", value, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeIn(List<String> values) {
            addCriterion("m_api_code in", values, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotIn(List<String> values) {
            addCriterion("m_api_code not in", values, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeBetween(String value1, String value2) {
            addCriterion("m_api_code between", value1, value2, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMApiCodeNotBetween(String value1, String value2) {
            addCriterion("m_api_code not between", value1, value2, "mApiCode");
            return (Criteria) this;
        }

        public Criteria andMModelIsNull() {
            addCriterion("m_model is null");
            return (Criteria) this;
        }

        public Criteria andMModelIsNotNull() {
            addCriterion("m_model is not null");
            return (Criteria) this;
        }

        public Criteria andMModelEqualTo(String value) {
            addCriterion("m_model =", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelNotEqualTo(String value) {
            addCriterion("m_model <>", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelGreaterThan(String value) {
            addCriterion("m_model >", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelGreaterThanOrEqualTo(String value) {
            addCriterion("m_model >=", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelLessThan(String value) {
            addCriterion("m_model <", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelLessThanOrEqualTo(String value) {
            addCriterion("m_model <=", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelLike(String value) {
            addCriterion("m_model like", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelNotLike(String value) {
            addCriterion("m_model not like", value, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelIn(List<String> values) {
            addCriterion("m_model in", values, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelNotIn(List<String> values) {
            addCriterion("m_model not in", values, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelBetween(String value1, String value2) {
            addCriterion("m_model between", value1, value2, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelNotBetween(String value1, String value2) {
            addCriterion("m_model not between", value1, value2, "mModel");
            return (Criteria) this;
        }

        public Criteria andMModelVersionIsNull() {
            addCriterion("m_model_version is null");
            return (Criteria) this;
        }

        public Criteria andMModelVersionIsNotNull() {
            addCriterion("m_model_version is not null");
            return (Criteria) this;
        }

        public Criteria andMModelVersionEqualTo(String value) {
            addCriterion("m_model_version =", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionNotEqualTo(String value) {
            addCriterion("m_model_version <>", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionGreaterThan(String value) {
            addCriterion("m_model_version >", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionGreaterThanOrEqualTo(String value) {
            addCriterion("m_model_version >=", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionLessThan(String value) {
            addCriterion("m_model_version <", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionLessThanOrEqualTo(String value) {
            addCriterion("m_model_version <=", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionLike(String value) {
            addCriterion("m_model_version like", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionNotLike(String value) {
            addCriterion("m_model_version not like", value, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionIn(List<String> values) {
            addCriterion("m_model_version in", values, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionNotIn(List<String> values) {
            addCriterion("m_model_version not in", values, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionBetween(String value1, String value2) {
            addCriterion("m_model_version between", value1, value2, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMModelVersionNotBetween(String value1, String value2) {
            addCriterion("m_model_version not between", value1, value2, "mModelVersion");
            return (Criteria) this;
        }

        public Criteria andMNumMinIsNull() {
            addCriterion("m_num_min is null");
            return (Criteria) this;
        }

        public Criteria andMNumMinIsNotNull() {
            addCriterion("m_num_min is not null");
            return (Criteria) this;
        }

        public Criteria andMNumMinEqualTo(Integer value) {
            addCriterion("m_num_min =", value, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinNotEqualTo(Integer value) {
            addCriterion("m_num_min <>", value, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinGreaterThan(Integer value) {
            addCriterion("m_num_min >", value, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_num_min >=", value, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinLessThan(Integer value) {
            addCriterion("m_num_min <", value, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinLessThanOrEqualTo(Integer value) {
            addCriterion("m_num_min <=", value, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinIn(List<Integer> values) {
            addCriterion("m_num_min in", values, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinNotIn(List<Integer> values) {
            addCriterion("m_num_min not in", values, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinBetween(Integer value1, Integer value2) {
            addCriterion("m_num_min between", value1, value2, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMinNotBetween(Integer value1, Integer value2) {
            addCriterion("m_num_min not between", value1, value2, "mNumMin");
            return (Criteria) this;
        }

        public Criteria andMNumMaxIsNull() {
            addCriterion("m_num_max is null");
            return (Criteria) this;
        }

        public Criteria andMNumMaxIsNotNull() {
            addCriterion("m_num_max is not null");
            return (Criteria) this;
        }

        public Criteria andMNumMaxEqualTo(Integer value) {
            addCriterion("m_num_max =", value, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxNotEqualTo(Integer value) {
            addCriterion("m_num_max <>", value, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxGreaterThan(Integer value) {
            addCriterion("m_num_max >", value, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_num_max >=", value, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxLessThan(Integer value) {
            addCriterion("m_num_max <", value, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxLessThanOrEqualTo(Integer value) {
            addCriterion("m_num_max <=", value, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxIn(List<Integer> values) {
            addCriterion("m_num_max in", values, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxNotIn(List<Integer> values) {
            addCriterion("m_num_max not in", values, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxBetween(Integer value1, Integer value2) {
            addCriterion("m_num_max between", value1, value2, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMNumMaxNotBetween(Integer value1, Integer value2) {
            addCriterion("m_num_max not between", value1, value2, "mNumMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMinIsNull() {
            addCriterion("m_score_min is null");
            return (Criteria) this;
        }

        public Criteria andMScoreMinIsNotNull() {
            addCriterion("m_score_min is not null");
            return (Criteria) this;
        }

        public Criteria andMScoreMinEqualTo(Integer value) {
            addCriterion("m_score_min =", value, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinNotEqualTo(Integer value) {
            addCriterion("m_score_min <>", value, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinGreaterThan(Integer value) {
            addCriterion("m_score_min >", value, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_score_min >=", value, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinLessThan(Integer value) {
            addCriterion("m_score_min <", value, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinLessThanOrEqualTo(Integer value) {
            addCriterion("m_score_min <=", value, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinIn(List<Integer> values) {
            addCriterion("m_score_min in", values, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinNotIn(List<Integer> values) {
            addCriterion("m_score_min not in", values, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinBetween(Integer value1, Integer value2) {
            addCriterion("m_score_min between", value1, value2, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMinNotBetween(Integer value1, Integer value2) {
            addCriterion("m_score_min not between", value1, value2, "mScoreMin");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxIsNull() {
            addCriterion("m_score_max is null");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxIsNotNull() {
            addCriterion("m_score_max is not null");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxEqualTo(Integer value) {
            addCriterion("m_score_max =", value, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxNotEqualTo(Integer value) {
            addCriterion("m_score_max <>", value, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxGreaterThan(Integer value) {
            addCriterion("m_score_max >", value, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_score_max >=", value, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxLessThan(Integer value) {
            addCriterion("m_score_max <", value, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxLessThanOrEqualTo(Integer value) {
            addCriterion("m_score_max <=", value, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxIn(List<Integer> values) {
            addCriterion("m_score_max in", values, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxNotIn(List<Integer> values) {
            addCriterion("m_score_max not in", values, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxBetween(Integer value1, Integer value2) {
            addCriterion("m_score_max between", value1, value2, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMScoreMaxNotBetween(Integer value1, Integer value2) {
            addCriterion("m_score_max not between", value1, value2, "mScoreMax");
            return (Criteria) this;
        }

        public Criteria andMPlanNumIsNull() {
            addCriterion("m_plan_num is null");
            return (Criteria) this;
        }

        public Criteria andMPlanNumIsNotNull() {
            addCriterion("m_plan_num is not null");
            return (Criteria) this;
        }

        public Criteria andMPlanNumEqualTo(Integer value) {
            addCriterion("m_plan_num =", value, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumNotEqualTo(Integer value) {
            addCriterion("m_plan_num <>", value, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumGreaterThan(Integer value) {
            addCriterion("m_plan_num >", value, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_plan_num >=", value, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumLessThan(Integer value) {
            addCriterion("m_plan_num <", value, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumLessThanOrEqualTo(Integer value) {
            addCriterion("m_plan_num <=", value, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumIn(List<Integer> values) {
            addCriterion("m_plan_num in", values, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumNotIn(List<Integer> values) {
            addCriterion("m_plan_num not in", values, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumBetween(Integer value1, Integer value2) {
            addCriterion("m_plan_num between", value1, value2, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMPlanNumNotBetween(Integer value1, Integer value2) {
            addCriterion("m_plan_num not between", value1, value2, "mPlanNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumIsNull() {
            addCriterion("m_realy_num is null");
            return (Criteria) this;
        }

        public Criteria andMRealyNumIsNotNull() {
            addCriterion("m_realy_num is not null");
            return (Criteria) this;
        }

        public Criteria andMRealyNumEqualTo(Integer value) {
            addCriterion("m_realy_num =", value, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumNotEqualTo(Integer value) {
            addCriterion("m_realy_num <>", value, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumGreaterThan(Integer value) {
            addCriterion("m_realy_num >", value, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_realy_num >=", value, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumLessThan(Integer value) {
            addCriterion("m_realy_num <", value, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumLessThanOrEqualTo(Integer value) {
            addCriterion("m_realy_num <=", value, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumIn(List<Integer> values) {
            addCriterion("m_realy_num in", values, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumNotIn(List<Integer> values) {
            addCriterion("m_realy_num not in", values, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumBetween(Integer value1, Integer value2) {
            addCriterion("m_realy_num between", value1, value2, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMRealyNumNotBetween(Integer value1, Integer value2) {
            addCriterion("m_realy_num not between", value1, value2, "mRealyNum");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListIsNull() {
            addCriterion("m_cus_batch_number_list is null");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListIsNotNull() {
            addCriterion("m_cus_batch_number_list is not null");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListEqualTo(String value) {
            addCriterion("m_cus_batch_number_list =", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListNotEqualTo(String value) {
            addCriterion("m_cus_batch_number_list <>", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListGreaterThan(String value) {
            addCriterion("m_cus_batch_number_list >", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListGreaterThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number_list >=", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListLessThan(String value) {
            addCriterion("m_cus_batch_number_list <", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListLessThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number_list <=", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListLike(String value) {
            addCriterion("m_cus_batch_number_list like", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListNotLike(String value) {
            addCriterion("m_cus_batch_number_list not like", value, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListIn(List<String> values) {
            addCriterion("m_cus_batch_number_list in", values, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListNotIn(List<String> values) {
            addCriterion("m_cus_batch_number_list not in", values, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number_list between", value1, value2, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMCusBatchNumberListNotBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number_list not between", value1, value2, "mCusBatchNumberList");
            return (Criteria) this;
        }

        public Criteria andMStatusIsNull() {
            addCriterion("m_status is null");
            return (Criteria) this;
        }

        public Criteria andMStatusIsNotNull() {
            addCriterion("m_status is not null");
            return (Criteria) this;
        }

        public Criteria andMStatusEqualTo(Integer value) {
            addCriterion("m_status =", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusNotEqualTo(Integer value) {
            addCriterion("m_status <>", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusGreaterThan(Integer value) {
            addCriterion("m_status >", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_status >=", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusLessThan(Integer value) {
            addCriterion("m_status <", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusLessThanOrEqualTo(Integer value) {
            addCriterion("m_status <=", value, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusIn(List<Integer> values) {
            addCriterion("m_status in", values, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusNotIn(List<Integer> values) {
            addCriterion("m_status not in", values, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusBetween(Integer value1, Integer value2) {
            addCriterion("m_status between", value1, value2, "mStatus");
            return (Criteria) this;
        }

        public Criteria andMStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("m_status not between", value1, value2, "mStatus");
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

        public Criteria andFinishTimeIsNull() {
            addCriterion("finish_time is null");
            return (Criteria) this;
        }

        public Criteria andFinishTimeIsNotNull() {
            addCriterion("finish_time is not null");
            return (Criteria) this;
        }

        public Criteria andFinishTimeEqualTo(Date value) {
            addCriterion("finish_time =", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeNotEqualTo(Date value) {
            addCriterion("finish_time <>", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeGreaterThan(Date value) {
            addCriterion("finish_time >", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("finish_time >=", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeLessThan(Date value) {
            addCriterion("finish_time <", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeLessThanOrEqualTo(Date value) {
            addCriterion("finish_time <=", value, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeIn(List<Date> values) {
            addCriterion("finish_time in", values, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeNotIn(List<Date> values) {
            addCriterion("finish_time not in", values, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeBetween(Date value1, Date value2) {
            addCriterion("finish_time between", value1, value2, "finishTime");
            return (Criteria) this;
        }

        public Criteria andFinishTimeNotBetween(Date value1, Date value2) {
            addCriterion("finish_time not between", value1, value2, "finishTime");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionIsNull() {
            addCriterion("m_rule_condition is null");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionIsNotNull() {
            addCriterion("m_rule_condition is not null");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionEqualTo(String value) {
            addCriterion("m_rule_condition =", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionNotEqualTo(String value) {
            addCriterion("m_rule_condition <>", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionGreaterThan(String value) {
            addCriterion("m_rule_condition >", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionGreaterThanOrEqualTo(String value) {
            addCriterion("m_rule_condition >=", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionLessThan(String value) {
            addCriterion("m_rule_condition <", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionLessThanOrEqualTo(String value) {
            addCriterion("m_rule_condition <=", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionLike(String value) {
            addCriterion("m_rule_condition like", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionNotLike(String value) {
            addCriterion("m_rule_condition not like", value, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionIn(List<String> values) {
            addCriterion("m_rule_condition in", values, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionNotIn(List<String> values) {
            addCriterion("m_rule_condition not in", values, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionBetween(String value1, String value2) {
            addCriterion("m_rule_condition between", value1, value2, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionNotBetween(String value1, String value2) {
            addCriterion("m_rule_condition not between", value1, value2, "mRuleCondition");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowIsNull() {
            addCriterion("m_rule_condition_show is null");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowIsNotNull() {
            addCriterion("m_rule_condition_show is not null");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowEqualTo(String value) {
            addCriterion("m_rule_condition_show =", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowNotEqualTo(String value) {
            addCriterion("m_rule_condition_show <>", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowGreaterThan(String value) {
            addCriterion("m_rule_condition_show >", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowGreaterThanOrEqualTo(String value) {
            addCriterion("m_rule_condition_show >=", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowLessThan(String value) {
            addCriterion("m_rule_condition_show <", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowLessThanOrEqualTo(String value) {
            addCriterion("m_rule_condition_show <=", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowLike(String value) {
            addCriterion("m_rule_condition_show like", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowNotLike(String value) {
            addCriterion("m_rule_condition_show not like", value, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowIn(List<String> values) {
            addCriterion("m_rule_condition_show in", values, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowNotIn(List<String> values) {
            addCriterion("m_rule_condition_show not in", values, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowBetween(String value1, String value2) {
            addCriterion("m_rule_condition_show between", value1, value2, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMRuleConditionShowNotBetween(String value1, String value2) {
            addCriterion("m_rule_condition_show not between", value1, value2, "mRuleConditionShow");
            return (Criteria) this;
        }

        public Criteria andMPercentageIsNull() {
            addCriterion("m_percentage is null");
            return (Criteria) this;
        }

        public Criteria andMPercentageIsNotNull() {
            addCriterion("m_percentage is not null");
            return (Criteria) this;
        }

        public Criteria andMPercentageEqualTo(BigDecimal value) {
            addCriterion("m_percentage =", value, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageNotEqualTo(BigDecimal value) {
            addCriterion("m_percentage <>", value, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageGreaterThan(BigDecimal value) {
            addCriterion("m_percentage >", value, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("m_percentage >=", value, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageLessThan(BigDecimal value) {
            addCriterion("m_percentage <", value, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageLessThanOrEqualTo(BigDecimal value) {
            addCriterion("m_percentage <=", value, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageIn(List<BigDecimal> values) {
            addCriterion("m_percentage in", values, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageNotIn(List<BigDecimal> values) {
            addCriterion("m_percentage not in", values, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("m_percentage between", value1, value2, "mPercentage");
            return (Criteria) this;
        }

        public Criteria andMPercentageNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("m_percentage not between", value1, value2, "mPercentage");
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

        public Criteria andOptUserIdEqualTo(String value) {
            addCriterion("opt_user_id =", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotEqualTo(String value) {
            addCriterion("opt_user_id <>", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdGreaterThan(String value) {
            addCriterion("opt_user_id >", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("opt_user_id >=", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdLessThan(String value) {
            addCriterion("opt_user_id <", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdLessThanOrEqualTo(String value) {
            addCriterion("opt_user_id <=", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdLike(String value) {
            addCriterion("opt_user_id like", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotLike(String value) {
            addCriterion("opt_user_id not like", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdIn(List<String> values) {
            addCriterion("opt_user_id in", values, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotIn(List<String> values) {
            addCriterion("opt_user_id not in", values, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdBetween(String value1, String value2) {
            addCriterion("opt_user_id between", value1, value2, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotBetween(String value1, String value2) {
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

        public Criteria andFilterTypeIsNull() {
            addCriterion("filter_type is null");
            return (Criteria) this;
        }

        public Criteria andFilterTypeIsNotNull() {
            addCriterion("filter_type is not null");
            return (Criteria) this;
        }

        public Criteria andFilterTypeEqualTo(Integer value) {
            addCriterion("filter_type =", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeNotEqualTo(Integer value) {
            addCriterion("filter_type <>", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeGreaterThan(Integer value) {
            addCriterion("filter_type >", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("filter_type >=", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeLessThan(Integer value) {
            addCriterion("filter_type <", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeLessThanOrEqualTo(Integer value) {
            addCriterion("filter_type <=", value, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeIn(List<Integer> values) {
            addCriterion("filter_type in", values, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeNotIn(List<Integer> values) {
            addCriterion("filter_type not in", values, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeBetween(Integer value1, Integer value2) {
            addCriterion("filter_type between", value1, value2, "filterType");
            return (Criteria) this;
        }

        public Criteria andFilterTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("filter_type not between", value1, value2, "filterType");
            return (Criteria) this;
        }

        public Criteria andExtendIsNull() {
            addCriterion("extend is null");
            return (Criteria) this;
        }

        public Criteria andExtendIsNotNull() {
            addCriterion("extend is not null");
            return (Criteria) this;
        }

        public Criteria andExtendEqualTo(String value) {
            addCriterion("extend =", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotEqualTo(String value) {
            addCriterion("extend <>", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThan(String value) {
            addCriterion("extend >", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThanOrEqualTo(String value) {
            addCriterion("extend >=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThan(String value) {
            addCriterion("extend <", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThanOrEqualTo(String value) {
            addCriterion("extend <=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLike(String value) {
            addCriterion("extend like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotLike(String value) {
            addCriterion("extend not like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendIn(List<String> values) {
            addCriterion("extend in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotIn(List<String> values) {
            addCriterion("extend not in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendBetween(String value1, String value2) {
            addCriterion("extend between", value1, value2, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotBetween(String value1, String value2) {
            addCriterion("extend not between", value1, value2, "extend");
            return (Criteria) this;
        }
    }

    /**
     */
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