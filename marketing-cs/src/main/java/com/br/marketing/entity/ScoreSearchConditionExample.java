package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ScoreSearchConditionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ScoreSearchConditionExample() {
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

        public Criteria andConditionNumberIsNull() {
            addCriterion("`condition_number` is null");
            return (Criteria) this;
        }

        public Criteria andConditionNumberIsNotNull() {
            addCriterion("`condition_number` is not null");
            return (Criteria) this;
        }

        public Criteria andConditionNumberEqualTo(String value) {
            addCriterion("`condition_number` =", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberNotEqualTo(String value) {
            addCriterion("`condition_number` <>", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberGreaterThan(String value) {
            addCriterion("`condition_number` >", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberGreaterThanOrEqualTo(String value) {
            addCriterion("`condition_number` >=", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberLessThan(String value) {
            addCriterion("`condition_number` <", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberLessThanOrEqualTo(String value) {
            addCriterion("`condition_number` <=", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberLike(String value) {
            addCriterion("`condition_number` like", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberNotLike(String value) {
            addCriterion("`condition_number` not like", value, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberIn(List<String> values) {
            addCriterion("`condition_number` in", values, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberNotIn(List<String> values) {
            addCriterion("`condition_number` not in", values, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberBetween(String value1, String value2) {
            addCriterion("`condition_number` between", value1, value2, "conditionNumber");
            return (Criteria) this;
        }

        public Criteria andConditionNumberNotBetween(String value1, String value2) {
            addCriterion("`condition_number` not between", value1, value2, "conditionNumber");
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

        public Criteria andConditionTypeIsNull() {
            addCriterion("condition_type is null");
            return (Criteria) this;
        }

        public Criteria andConditionTypeIsNotNull() {
            addCriterion("condition_type is not null");
            return (Criteria) this;
        }

        public Criteria andConditionTypeEqualTo(Integer value) {
            addCriterion("condition_type =", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotEqualTo(Integer value) {
            addCriterion("condition_type <>", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeGreaterThan(Integer value) {
            addCriterion("condition_type >", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("condition_type >=", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeLessThan(Integer value) {
            addCriterion("condition_type <", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeLessThanOrEqualTo(Integer value) {
            addCriterion("condition_type <=", value, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeIn(List<Integer> values) {
            addCriterion("condition_type in", values, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotIn(List<Integer> values) {
            addCriterion("condition_type not in", values, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeBetween(Integer value1, Integer value2) {
            addCriterion("condition_type between", value1, value2, "conditionType");
            return (Criteria) this;
        }

        public Criteria andConditionTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("condition_type not between", value1, value2, "conditionType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeIsNull() {
            addCriterion("source_type is null");
            return (Criteria) this;
        }

        public Criteria andSourceTypeIsNotNull() {
            addCriterion("source_type is not null");
            return (Criteria) this;
        }

        public Criteria andSourceTypeEqualTo(Integer value) {
            addCriterion("source_type =", value, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeNotEqualTo(Integer value) {
            addCriterion("source_type <>", value, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeGreaterThan(Integer value) {
            addCriterion("source_type >", value, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("source_type >=", value, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeLessThan(Integer value) {
            addCriterion("source_type <", value, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeLessThanOrEqualTo(Integer value) {
            addCriterion("source_type <=", value, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeIn(List<Integer> values) {
            addCriterion("source_type in", values, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeNotIn(List<Integer> values) {
            addCriterion("source_type not in", values, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeBetween(Integer value1, Integer value2) {
            addCriterion("source_type between", value1, value2, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("source_type not between", value1, value2, "sourceType");
            return (Criteria) this;
        }

        public Criteria andSourceConditionIsNull() {
            addCriterion("source_condition is null");
            return (Criteria) this;
        }

        public Criteria andSourceConditionIsNotNull() {
            addCriterion("source_condition is not null");
            return (Criteria) this;
        }

        public Criteria andSourceConditionEqualTo(String value) {
            addCriterion("source_condition =", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionNotEqualTo(String value) {
            addCriterion("source_condition <>", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionGreaterThan(String value) {
            addCriterion("source_condition >", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionGreaterThanOrEqualTo(String value) {
            addCriterion("source_condition >=", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionLessThan(String value) {
            addCriterion("source_condition <", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionLessThanOrEqualTo(String value) {
            addCriterion("source_condition <=", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionLike(String value) {
            addCriterion("source_condition like", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionNotLike(String value) {
            addCriterion("source_condition not like", value, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionIn(List<String> values) {
            addCriterion("source_condition in", values, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionNotIn(List<String> values) {
            addCriterion("source_condition not in", values, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionBetween(String value1, String value2) {
            addCriterion("source_condition between", value1, value2, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andSourceConditionNotBetween(String value1, String value2) {
            addCriterion("source_condition not between", value1, value2, "sourceCondition");
            return (Criteria) this;
        }

        public Criteria andContentIsNull() {
            addCriterion("content is null");
            return (Criteria) this;
        }

        public Criteria andContentIsNotNull() {
            addCriterion("content is not null");
            return (Criteria) this;
        }

        public Criteria andContentEqualTo(String value) {
            addCriterion("content =", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotEqualTo(String value) {
            addCriterion("content <>", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThan(String value) {
            addCriterion("content >", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentGreaterThanOrEqualTo(String value) {
            addCriterion("content >=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThan(String value) {
            addCriterion("content <", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLessThanOrEqualTo(String value) {
            addCriterion("content <=", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentLike(String value) {
            addCriterion("content like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotLike(String value) {
            addCriterion("content not like", value, "content");
            return (Criteria) this;
        }

        public Criteria andContentIn(List<String> values) {
            addCriterion("content in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotIn(List<String> values) {
            addCriterion("content not in", values, "content");
            return (Criteria) this;
        }

        public Criteria andContentBetween(String value1, String value2) {
            addCriterion("content between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andContentNotBetween(String value1, String value2) {
            addCriterion("content not between", value1, value2, "content");
            return (Criteria) this;
        }

        public Criteria andContentShowIsNull() {
            addCriterion("content_show is null");
            return (Criteria) this;
        }

        public Criteria andContentShowIsNotNull() {
            addCriterion("content_show is not null");
            return (Criteria) this;
        }

        public Criteria andContentShowEqualTo(String value) {
            addCriterion("content_show =", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowNotEqualTo(String value) {
            addCriterion("content_show <>", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowGreaterThan(String value) {
            addCriterion("content_show >", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowGreaterThanOrEqualTo(String value) {
            addCriterion("content_show >=", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowLessThan(String value) {
            addCriterion("content_show <", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowLessThanOrEqualTo(String value) {
            addCriterion("content_show <=", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowLike(String value) {
            addCriterion("content_show like", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowNotLike(String value) {
            addCriterion("content_show not like", value, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowIn(List<String> values) {
            addCriterion("content_show in", values, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowNotIn(List<String> values) {
            addCriterion("content_show not in", values, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowBetween(String value1, String value2) {
            addCriterion("content_show between", value1, value2, "contentShow");
            return (Criteria) this;
        }

        public Criteria andContentShowNotBetween(String value1, String value2) {
            addCriterion("content_show not between", value1, value2, "contentShow");
            return (Criteria) this;
        }

        public Criteria andScoreContentIsNull() {
            addCriterion("score_content is null");
            return (Criteria) this;
        }

        public Criteria andScoreContentIsNotNull() {
            addCriterion("score_content is not null");
            return (Criteria) this;
        }

        public Criteria andScoreContentEqualTo(String value) {
            addCriterion("score_content =", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentNotEqualTo(String value) {
            addCriterion("score_content <>", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentGreaterThan(String value) {
            addCriterion("score_content >", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentGreaterThanOrEqualTo(String value) {
            addCriterion("score_content >=", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentLessThan(String value) {
            addCriterion("score_content <", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentLessThanOrEqualTo(String value) {
            addCriterion("score_content <=", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentLike(String value) {
            addCriterion("score_content like", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentNotLike(String value) {
            addCriterion("score_content not like", value, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentIn(List<String> values) {
            addCriterion("score_content in", values, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentNotIn(List<String> values) {
            addCriterion("score_content not in", values, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentBetween(String value1, String value2) {
            addCriterion("score_content between", value1, value2, "scoreContent");
            return (Criteria) this;
        }

        public Criteria andScoreContentNotBetween(String value1, String value2) {
            addCriterion("score_content not between", value1, value2, "scoreContent");
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

        public Criteria andTagContentIsNull() {
            addCriterion("tag_content is null");
            return (Criteria) this;
        }

        public Criteria andTagContentIsNotNull() {
            addCriterion("tag_content is not null");
            return (Criteria) this;
        }

        public Criteria andTagContentEqualTo(String value) {
            addCriterion("tag_content =", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentNotEqualTo(String value) {
            addCriterion("tag_content <>", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentGreaterThan(String value) {
            addCriterion("tag_content >", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentGreaterThanOrEqualTo(String value) {
            addCriterion("tag_content >=", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentLessThan(String value) {
            addCriterion("tag_content <", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentLessThanOrEqualTo(String value) {
            addCriterion("tag_content <=", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentLike(String value) {
            addCriterion("tag_content like", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentNotLike(String value) {
            addCriterion("tag_content not like", value, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentIn(List<String> values) {
            addCriterion("tag_content in", values, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentNotIn(List<String> values) {
            addCriterion("tag_content not in", values, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentBetween(String value1, String value2) {
            addCriterion("tag_content between", value1, value2, "tagContent");
            return (Criteria) this;
        }

        public Criteria andTagContentNotBetween(String value1, String value2) {
            addCriterion("tag_content not between", value1, value2, "tagContent");
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