package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerInfoPushMainExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public CustomerInfoPushMainExample() {
        oredCriteria = new ArrayList<BaseCriteria>();
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

    public List<BaseCriteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(BaseCriteria criteria) {
        oredCriteria.add(criteria);
    }

    public BaseCriteria or() {
        BaseCriteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public BaseCriteria createCriteria() {
        BaseCriteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected BaseCriteria createCriteriaInternal() {
        BaseCriteria criteria = new BaseCriteria();
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

        public BaseCriteria andIdIsNull() {
            addCriterion("id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeIsNull() {
            addCriterion("m_api_code is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeIsNotNull() {
            addCriterion("m_api_code is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeEqualTo(String value) {
            addCriterion("m_api_code =", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotEqualTo(String value) {
            addCriterion("m_api_code <>", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeGreaterThan(String value) {
            addCriterion("m_api_code >", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("m_api_code >=", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeLessThan(String value) {
            addCriterion("m_api_code <", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeLessThanOrEqualTo(String value) {
            addCriterion("m_api_code <=", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeLike(String value) {
            addCriterion("m_api_code like", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotLike(String value) {
            addCriterion("m_api_code not like", value, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeIn(List<String> values) {
            addCriterion("m_api_code in", values, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotIn(List<String> values) {
            addCriterion("m_api_code not in", values, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeBetween(String value1, String value2) {
            addCriterion("m_api_code between", value1, value2, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMApiCodeNotBetween(String value1, String value2) {
            addCriterion("m_api_code not between", value1, value2, "mApiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelIsNull() {
            addCriterion("m_model is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelIsNotNull() {
            addCriterion("m_model is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelEqualTo(String value) {
            addCriterion("m_model =", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelNotEqualTo(String value) {
            addCriterion("m_model <>", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelGreaterThan(String value) {
            addCriterion("m_model >", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelGreaterThanOrEqualTo(String value) {
            addCriterion("m_model >=", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelLessThan(String value) {
            addCriterion("m_model <", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelLessThanOrEqualTo(String value) {
            addCriterion("m_model <=", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelLike(String value) {
            addCriterion("m_model like", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelNotLike(String value) {
            addCriterion("m_model not like", value, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelIn(List<String> values) {
            addCriterion("m_model in", values, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelNotIn(List<String> values) {
            addCriterion("m_model not in", values, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelBetween(String value1, String value2) {
            addCriterion("m_model between", value1, value2, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelNotBetween(String value1, String value2) {
            addCriterion("m_model not between", value1, value2, "mModel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionIsNull() {
            addCriterion("m_model_version is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionIsNotNull() {
            addCriterion("m_model_version is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionEqualTo(String value) {
            addCriterion("m_model_version =", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionNotEqualTo(String value) {
            addCriterion("m_model_version <>", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionGreaterThan(String value) {
            addCriterion("m_model_version >", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionGreaterThanOrEqualTo(String value) {
            addCriterion("m_model_version >=", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionLessThan(String value) {
            addCriterion("m_model_version <", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionLessThanOrEqualTo(String value) {
            addCriterion("m_model_version <=", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionLike(String value) {
            addCriterion("m_model_version like", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionNotLike(String value) {
            addCriterion("m_model_version not like", value, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionIn(List<String> values) {
            addCriterion("m_model_version in", values, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionNotIn(List<String> values) {
            addCriterion("m_model_version not in", values, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionBetween(String value1, String value2) {
            addCriterion("m_model_version between", value1, value2, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMModelVersionNotBetween(String value1, String value2) {
            addCriterion("m_model_version not between", value1, value2, "mModelVersion");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinIsNull() {
            addCriterion("m_num_min is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinIsNotNull() {
            addCriterion("m_num_min is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinEqualTo(Integer value) {
            addCriterion("m_num_min =", value, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinNotEqualTo(Integer value) {
            addCriterion("m_num_min <>", value, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinGreaterThan(Integer value) {
            addCriterion("m_num_min >", value, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_num_min >=", value, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinLessThan(Integer value) {
            addCriterion("m_num_min <", value, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinLessThanOrEqualTo(Integer value) {
            addCriterion("m_num_min <=", value, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinIn(List<Integer> values) {
            addCriterion("m_num_min in", values, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinNotIn(List<Integer> values) {
            addCriterion("m_num_min not in", values, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinBetween(Integer value1, Integer value2) {
            addCriterion("m_num_min between", value1, value2, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMinNotBetween(Integer value1, Integer value2) {
            addCriterion("m_num_min not between", value1, value2, "mNumMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxIsNull() {
            addCriterion("m_num_max is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxIsNotNull() {
            addCriterion("m_num_max is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxEqualTo(Integer value) {
            addCriterion("m_num_max =", value, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxNotEqualTo(Integer value) {
            addCriterion("m_num_max <>", value, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxGreaterThan(Integer value) {
            addCriterion("m_num_max >", value, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_num_max >=", value, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxLessThan(Integer value) {
            addCriterion("m_num_max <", value, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxLessThanOrEqualTo(Integer value) {
            addCriterion("m_num_max <=", value, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxIn(List<Integer> values) {
            addCriterion("m_num_max in", values, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxNotIn(List<Integer> values) {
            addCriterion("m_num_max not in", values, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxBetween(Integer value1, Integer value2) {
            addCriterion("m_num_max between", value1, value2, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMNumMaxNotBetween(Integer value1, Integer value2) {
            addCriterion("m_num_max not between", value1, value2, "mNumMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinIsNull() {
            addCriterion("m_score_min is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinIsNotNull() {
            addCriterion("m_score_min is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinEqualTo(Integer value) {
            addCriterion("m_score_min =", value, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinNotEqualTo(Integer value) {
            addCriterion("m_score_min <>", value, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinGreaterThan(Integer value) {
            addCriterion("m_score_min >", value, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_score_min >=", value, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinLessThan(Integer value) {
            addCriterion("m_score_min <", value, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinLessThanOrEqualTo(Integer value) {
            addCriterion("m_score_min <=", value, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinIn(List<Integer> values) {
            addCriterion("m_score_min in", values, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinNotIn(List<Integer> values) {
            addCriterion("m_score_min not in", values, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinBetween(Integer value1, Integer value2) {
            addCriterion("m_score_min between", value1, value2, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMinNotBetween(Integer value1, Integer value2) {
            addCriterion("m_score_min not between", value1, value2, "mScoreMin");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxIsNull() {
            addCriterion("m_score_max is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxIsNotNull() {
            addCriterion("m_score_max is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxEqualTo(Integer value) {
            addCriterion("m_score_max =", value, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxNotEqualTo(Integer value) {
            addCriterion("m_score_max <>", value, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxGreaterThan(Integer value) {
            addCriterion("m_score_max >", value, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_score_max >=", value, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxLessThan(Integer value) {
            addCriterion("m_score_max <", value, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxLessThanOrEqualTo(Integer value) {
            addCriterion("m_score_max <=", value, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxIn(List<Integer> values) {
            addCriterion("m_score_max in", values, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxNotIn(List<Integer> values) {
            addCriterion("m_score_max not in", values, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxBetween(Integer value1, Integer value2) {
            addCriterion("m_score_max between", value1, value2, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMScoreMaxNotBetween(Integer value1, Integer value2) {
            addCriterion("m_score_max not between", value1, value2, "mScoreMax");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumIsNull() {
            addCriterion("m_plan_num is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumIsNotNull() {
            addCriterion("m_plan_num is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumEqualTo(Integer value) {
            addCriterion("m_plan_num =", value, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumNotEqualTo(Integer value) {
            addCriterion("m_plan_num <>", value, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumGreaterThan(Integer value) {
            addCriterion("m_plan_num >", value, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_plan_num >=", value, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumLessThan(Integer value) {
            addCriterion("m_plan_num <", value, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumLessThanOrEqualTo(Integer value) {
            addCriterion("m_plan_num <=", value, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumIn(List<Integer> values) {
            addCriterion("m_plan_num in", values, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumNotIn(List<Integer> values) {
            addCriterion("m_plan_num not in", values, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumBetween(Integer value1, Integer value2) {
            addCriterion("m_plan_num between", value1, value2, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMPlanNumNotBetween(Integer value1, Integer value2) {
            addCriterion("m_plan_num not between", value1, value2, "mPlanNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumIsNull() {
            addCriterion("m_realy_num is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumIsNotNull() {
            addCriterion("m_realy_num is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumEqualTo(Integer value) {
            addCriterion("m_realy_num =", value, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumNotEqualTo(Integer value) {
            addCriterion("m_realy_num <>", value, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumGreaterThan(Integer value) {
            addCriterion("m_realy_num >", value, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_realy_num >=", value, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumLessThan(Integer value) {
            addCriterion("m_realy_num <", value, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumLessThanOrEqualTo(Integer value) {
            addCriterion("m_realy_num <=", value, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumIn(List<Integer> values) {
            addCriterion("m_realy_num in", values, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumNotIn(List<Integer> values) {
            addCriterion("m_realy_num not in", values, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumBetween(Integer value1, Integer value2) {
            addCriterion("m_realy_num between", value1, value2, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMRealyNumNotBetween(Integer value1, Integer value2) {
            addCriterion("m_realy_num not between", value1, value2, "mRealyNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListIsNull() {
            addCriterion("m_cus_batch_number_list is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListIsNotNull() {
            addCriterion("m_cus_batch_number_list is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListEqualTo(String value) {
            addCriterion("m_cus_batch_number_list =", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListNotEqualTo(String value) {
            addCriterion("m_cus_batch_number_list <>", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListGreaterThan(String value) {
            addCriterion("m_cus_batch_number_list >", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListGreaterThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number_list >=", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListLessThan(String value) {
            addCriterion("m_cus_batch_number_list <", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListLessThanOrEqualTo(String value) {
            addCriterion("m_cus_batch_number_list <=", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListLike(String value) {
            addCriterion("m_cus_batch_number_list like", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListNotLike(String value) {
            addCriterion("m_cus_batch_number_list not like", value, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListIn(List<String> values) {
            addCriterion("m_cus_batch_number_list in", values, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListNotIn(List<String> values) {
            addCriterion("m_cus_batch_number_list not in", values, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number_list between", value1, value2, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMCusBatchNumberListNotBetween(String value1, String value2) {
            addCriterion("m_cus_batch_number_list not between", value1, value2, "mCusBatchNumberList");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusIsNull() {
            addCriterion("m_status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusIsNotNull() {
            addCriterion("m_status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusEqualTo(Integer value) {
            addCriterion("m_status =", value, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusNotEqualTo(Integer value) {
            addCriterion("m_status <>", value, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusGreaterThan(Integer value) {
            addCriterion("m_status >", value, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("m_status >=", value, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusLessThan(Integer value) {
            addCriterion("m_status <", value, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusLessThanOrEqualTo(Integer value) {
            addCriterion("m_status <=", value, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusIn(List<Integer> values) {
            addCriterion("m_status in", values, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusNotIn(List<Integer> values) {
            addCriterion("m_status not in", values, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusBetween(Integer value1, Integer value2) {
            addCriterion("m_status between", value1, value2, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("m_status not between", value1, value2, "mStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelIsNull() {
            addCriterion("is_del is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelIsNotNull() {
            addCriterion("is_del is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelEqualTo(Integer value) {
            addCriterion("is_del =", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelNotEqualTo(Integer value) {
            addCriterion("is_del <>", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelGreaterThan(Integer value) {
            addCriterion("is_del >", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_del >=", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelLessThan(Integer value) {
            addCriterion("is_del <", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelLessThanOrEqualTo(Integer value) {
            addCriterion("is_del <=", value, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelIn(List<Integer> values) {
            addCriterion("is_del in", values, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelNotIn(List<Integer> values) {
            addCriterion("is_del not in", values, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelBetween(Integer value1, Integer value2) {
            addCriterion("is_del between", value1, value2, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIsDelNotBetween(Integer value1, Integer value2) {
            addCriterion("is_del not between", value1, value2, "isDel");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeIsNull() {
            addCriterion("finish_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeIsNotNull() {
            addCriterion("finish_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeEqualTo(Date value) {
            addCriterion("finish_time =", value, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeNotEqualTo(Date value) {
            addCriterion("finish_time <>", value, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeGreaterThan(Date value) {
            addCriterion("finish_time >", value, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("finish_time >=", value, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeLessThan(Date value) {
            addCriterion("finish_time <", value, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeLessThanOrEqualTo(Date value) {
            addCriterion("finish_time <=", value, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeIn(List<Date> values) {
            addCriterion("finish_time in", values, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeNotIn(List<Date> values) {
            addCriterion("finish_time not in", values, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeBetween(Date value1, Date value2) {
            addCriterion("finish_time between", value1, value2, "finishTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFinishTimeNotBetween(Date value1, Date value2) {
            addCriterion("finish_time not between", value1, value2, "finishTime");
            return (BaseCriteria) this;
        }
    }

    public static class BaseCriteria extends GeneratedCriteria {

        protected BaseCriteria() {
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