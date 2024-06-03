package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PeriodPushStatisticsLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PeriodPushStatisticsLogExample() {
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

        public Criteria andPplIdIsNull() {
            addCriterion("ppl_id is null");
            return (Criteria) this;
        }

        public Criteria andPplIdIsNotNull() {
            addCriterion("ppl_id is not null");
            return (Criteria) this;
        }

        public Criteria andPplIdEqualTo(String value) {
            addCriterion("ppl_id =", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdNotEqualTo(String value) {
            addCriterion("ppl_id <>", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdGreaterThan(String value) {
            addCriterion("ppl_id >", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdGreaterThanOrEqualTo(String value) {
            addCriterion("ppl_id >=", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdLessThan(String value) {
            addCriterion("ppl_id <", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdLessThanOrEqualTo(String value) {
            addCriterion("ppl_id <=", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdLike(String value) {
            addCriterion("ppl_id like", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdNotLike(String value) {
            addCriterion("ppl_id not like", value, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdIn(List<String> values) {
            addCriterion("ppl_id in", values, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdNotIn(List<String> values) {
            addCriterion("ppl_id not in", values, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdBetween(String value1, String value2) {
            addCriterion("ppl_id between", value1, value2, "pplId");
            return (Criteria) this;
        }

        public Criteria andPplIdNotBetween(String value1, String value2) {
            addCriterion("ppl_id not between", value1, value2, "pplId");
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

        public Criteria andResultContentIsNull() {
            addCriterion("result_content is null");
            return (Criteria) this;
        }

        public Criteria andResultContentIsNotNull() {
            addCriterion("result_content is not null");
            return (Criteria) this;
        }

        public Criteria andResultContentEqualTo(String value) {
            addCriterion("result_content =", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentNotEqualTo(String value) {
            addCriterion("result_content <>", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentGreaterThan(String value) {
            addCriterion("result_content >", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentGreaterThanOrEqualTo(String value) {
            addCriterion("result_content >=", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentLessThan(String value) {
            addCriterion("result_content <", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentLessThanOrEqualTo(String value) {
            addCriterion("result_content <=", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentLike(String value) {
            addCriterion("result_content like", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentNotLike(String value) {
            addCriterion("result_content not like", value, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentIn(List<String> values) {
            addCriterion("result_content in", values, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentNotIn(List<String> values) {
            addCriterion("result_content not in", values, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentBetween(String value1, String value2) {
            addCriterion("result_content between", value1, value2, "resultContent");
            return (Criteria) this;
        }

        public Criteria andResultContentNotBetween(String value1, String value2) {
            addCriterion("result_content not between", value1, value2, "resultContent");
            return (Criteria) this;
        }

        public Criteria andFailNumIsNull() {
            addCriterion("fail_num is null");
            return (Criteria) this;
        }

        public Criteria andFailNumIsNotNull() {
            addCriterion("fail_num is not null");
            return (Criteria) this;
        }

        public Criteria andFailNumEqualTo(Integer value) {
            addCriterion("fail_num =", value, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumNotEqualTo(Integer value) {
            addCriterion("fail_num <>", value, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumGreaterThan(Integer value) {
            addCriterion("fail_num >", value, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("fail_num >=", value, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumLessThan(Integer value) {
            addCriterion("fail_num <", value, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumLessThanOrEqualTo(Integer value) {
            addCriterion("fail_num <=", value, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumIn(List<Integer> values) {
            addCriterion("fail_num in", values, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumNotIn(List<Integer> values) {
            addCriterion("fail_num not in", values, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumBetween(Integer value1, Integer value2) {
            addCriterion("fail_num between", value1, value2, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailNumNotBetween(Integer value1, Integer value2) {
            addCriterion("fail_num not between", value1, value2, "failNum");
            return (Criteria) this;
        }

        public Criteria andFailIdsIsNull() {
            addCriterion("fail_ids is null");
            return (Criteria) this;
        }

        public Criteria andFailIdsIsNotNull() {
            addCriterion("fail_ids is not null");
            return (Criteria) this;
        }

        public Criteria andFailIdsEqualTo(String value) {
            addCriterion("fail_ids =", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsNotEqualTo(String value) {
            addCriterion("fail_ids <>", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsGreaterThan(String value) {
            addCriterion("fail_ids >", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsGreaterThanOrEqualTo(String value) {
            addCriterion("fail_ids >=", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsLessThan(String value) {
            addCriterion("fail_ids <", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsLessThanOrEqualTo(String value) {
            addCriterion("fail_ids <=", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsLike(String value) {
            addCriterion("fail_ids like", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsNotLike(String value) {
            addCriterion("fail_ids not like", value, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsIn(List<String> values) {
            addCriterion("fail_ids in", values, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsNotIn(List<String> values) {
            addCriterion("fail_ids not in", values, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsBetween(String value1, String value2) {
            addCriterion("fail_ids between", value1, value2, "failIds");
            return (Criteria) this;
        }

        public Criteria andFailIdsNotBetween(String value1, String value2) {
            addCriterion("fail_ids not between", value1, value2, "failIds");
            return (Criteria) this;
        }

        public Criteria andPushNumIsNull() {
            addCriterion("push_num is null");
            return (Criteria) this;
        }

        public Criteria andPushNumIsNotNull() {
            addCriterion("push_num is not null");
            return (Criteria) this;
        }

        public Criteria andPushNumEqualTo(Integer value) {
            addCriterion("push_num =", value, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumNotEqualTo(Integer value) {
            addCriterion("push_num <>", value, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumGreaterThan(Integer value) {
            addCriterion("push_num >", value, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_num >=", value, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumLessThan(Integer value) {
            addCriterion("push_num <", value, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumLessThanOrEqualTo(Integer value) {
            addCriterion("push_num <=", value, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumIn(List<Integer> values) {
            addCriterion("push_num in", values, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumNotIn(List<Integer> values) {
            addCriterion("push_num not in", values, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumBetween(Integer value1, Integer value2) {
            addCriterion("push_num between", value1, value2, "pushNum");
            return (Criteria) this;
        }

        public Criteria andPushNumNotBetween(Integer value1, Integer value2) {
            addCriterion("push_num not between", value1, value2, "pushNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumIsNull() {
            addCriterion("total_num is null");
            return (Criteria) this;
        }

        public Criteria andTotalNumIsNotNull() {
            addCriterion("total_num is not null");
            return (Criteria) this;
        }

        public Criteria andTotalNumEqualTo(Integer value) {
            addCriterion("total_num =", value, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumNotEqualTo(Integer value) {
            addCriterion("total_num <>", value, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumGreaterThan(Integer value) {
            addCriterion("total_num >", value, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("total_num >=", value, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumLessThan(Integer value) {
            addCriterion("total_num <", value, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumLessThanOrEqualTo(Integer value) {
            addCriterion("total_num <=", value, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumIn(List<Integer> values) {
            addCriterion("total_num in", values, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumNotIn(List<Integer> values) {
            addCriterion("total_num not in", values, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumBetween(Integer value1, Integer value2) {
            addCriterion("total_num between", value1, value2, "totalNum");
            return (Criteria) this;
        }

        public Criteria andTotalNumNotBetween(Integer value1, Integer value2) {
            addCriterion("total_num not between", value1, value2, "totalNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumIsNull() {
            addCriterion("meet_conditions_num is null");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumIsNotNull() {
            addCriterion("meet_conditions_num is not null");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumEqualTo(Integer value) {
            addCriterion("meet_conditions_num =", value, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumNotEqualTo(Integer value) {
            addCriterion("meet_conditions_num <>", value, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumGreaterThan(Integer value) {
            addCriterion("meet_conditions_num >", value, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("meet_conditions_num >=", value, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumLessThan(Integer value) {
            addCriterion("meet_conditions_num <", value, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumLessThanOrEqualTo(Integer value) {
            addCriterion("meet_conditions_num <=", value, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumIn(List<Integer> values) {
            addCriterion("meet_conditions_num in", values, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumNotIn(List<Integer> values) {
            addCriterion("meet_conditions_num not in", values, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumBetween(Integer value1, Integer value2) {
            addCriterion("meet_conditions_num between", value1, value2, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andMeetConditionsNumNotBetween(Integer value1, Integer value2) {
            addCriterion("meet_conditions_num not between", value1, value2, "meetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumIsNull() {
            addCriterion("fail_meet_conditions_num is null");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumIsNotNull() {
            addCriterion("fail_meet_conditions_num is not null");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumEqualTo(Integer value) {
            addCriterion("fail_meet_conditions_num =", value, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumNotEqualTo(Integer value) {
            addCriterion("fail_meet_conditions_num <>", value, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumGreaterThan(Integer value) {
            addCriterion("fail_meet_conditions_num >", value, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("fail_meet_conditions_num >=", value, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumLessThan(Integer value) {
            addCriterion("fail_meet_conditions_num <", value, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumLessThanOrEqualTo(Integer value) {
            addCriterion("fail_meet_conditions_num <=", value, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumIn(List<Integer> values) {
            addCriterion("fail_meet_conditions_num in", values, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumNotIn(List<Integer> values) {
            addCriterion("fail_meet_conditions_num not in", values, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumBetween(Integer value1, Integer value2) {
            addCriterion("fail_meet_conditions_num between", value1, value2, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetConditionsNumNotBetween(Integer value1, Integer value2) {
            addCriterion("fail_meet_conditions_num not between", value1, value2, "failMeetConditionsNum");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsIsNull() {
            addCriterion("fail_meet_ids is null");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsIsNotNull() {
            addCriterion("fail_meet_ids is not null");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsEqualTo(String value) {
            addCriterion("fail_meet_ids =", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsNotEqualTo(String value) {
            addCriterion("fail_meet_ids <>", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsGreaterThan(String value) {
            addCriterion("fail_meet_ids >", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsGreaterThanOrEqualTo(String value) {
            addCriterion("fail_meet_ids >=", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsLessThan(String value) {
            addCriterion("fail_meet_ids <", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsLessThanOrEqualTo(String value) {
            addCriterion("fail_meet_ids <=", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsLike(String value) {
            addCriterion("fail_meet_ids like", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsNotLike(String value) {
            addCriterion("fail_meet_ids not like", value, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsIn(List<String> values) {
            addCriterion("fail_meet_ids in", values, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsNotIn(List<String> values) {
            addCriterion("fail_meet_ids not in", values, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsBetween(String value1, String value2) {
            addCriterion("fail_meet_ids between", value1, value2, "failMeetIds");
            return (Criteria) this;
        }

        public Criteria andFailMeetIdsNotBetween(String value1, String value2) {
            addCriterion("fail_meet_ids not between", value1, value2, "failMeetIds");
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