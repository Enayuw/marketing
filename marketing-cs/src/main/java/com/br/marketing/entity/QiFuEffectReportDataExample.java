package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QiFuEffectReportDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QiFuEffectReportDataExample() {
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

        public Criteria andBelongMonthIsNull() {
            addCriterion("belong_month is null");
            return (Criteria) this;
        }

        public Criteria andBelongMonthIsNotNull() {
            addCriterion("belong_month is not null");
            return (Criteria) this;
        }

        public Criteria andBelongMonthEqualTo(String value) {
            addCriterion("belong_month =", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthNotEqualTo(String value) {
            addCriterion("belong_month <>", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthGreaterThan(String value) {
            addCriterion("belong_month >", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthGreaterThanOrEqualTo(String value) {
            addCriterion("belong_month >=", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthLessThan(String value) {
            addCriterion("belong_month <", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthLessThanOrEqualTo(String value) {
            addCriterion("belong_month <=", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthLike(String value) {
            addCriterion("belong_month like", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthNotLike(String value) {
            addCriterion("belong_month not like", value, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthIn(List<String> values) {
            addCriterion("belong_month in", values, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthNotIn(List<String> values) {
            addCriterion("belong_month not in", values, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthBetween(String value1, String value2) {
            addCriterion("belong_month between", value1, value2, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andBelongMonthNotBetween(String value1, String value2) {
            addCriterion("belong_month not between", value1, value2, "belongMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthIsNull() {
            addCriterion("strategy_month is null");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthIsNotNull() {
            addCriterion("strategy_month is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthEqualTo(String value) {
            addCriterion("strategy_month =", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthNotEqualTo(String value) {
            addCriterion("strategy_month <>", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthGreaterThan(String value) {
            addCriterion("strategy_month >", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_month >=", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthLessThan(String value) {
            addCriterion("strategy_month <", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthLessThanOrEqualTo(String value) {
            addCriterion("strategy_month <=", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthLike(String value) {
            addCriterion("strategy_month like", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthNotLike(String value) {
            addCriterion("strategy_month not like", value, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthIn(List<String> values) {
            addCriterion("strategy_month in", values, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthNotIn(List<String> values) {
            addCriterion("strategy_month not in", values, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthBetween(String value1, String value2) {
            addCriterion("strategy_month between", value1, value2, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andStrategyMonthNotBetween(String value1, String value2) {
            addCriterion("strategy_month not between", value1, value2, "strategyMonth");
            return (Criteria) this;
        }

        public Criteria andUpdDateIsNull() {
            addCriterion("upd_date is null");
            return (Criteria) this;
        }

        public Criteria andUpdDateIsNotNull() {
            addCriterion("upd_date is not null");
            return (Criteria) this;
        }

        public Criteria andUpdDateEqualTo(String value) {
            addCriterion("upd_date =", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateNotEqualTo(String value) {
            addCriterion("upd_date <>", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateGreaterThan(String value) {
            addCriterion("upd_date >", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateGreaterThanOrEqualTo(String value) {
            addCriterion("upd_date >=", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateLessThan(String value) {
            addCriterion("upd_date <", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateLessThanOrEqualTo(String value) {
            addCriterion("upd_date <=", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateLike(String value) {
            addCriterion("upd_date like", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateNotLike(String value) {
            addCriterion("upd_date not like", value, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateIn(List<String> values) {
            addCriterion("upd_date in", values, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateNotIn(List<String> values) {
            addCriterion("upd_date not in", values, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateBetween(String value1, String value2) {
            addCriterion("upd_date between", value1, value2, "updDate");
            return (Criteria) this;
        }

        public Criteria andUpdDateNotBetween(String value1, String value2) {
            addCriterion("upd_date not between", value1, value2, "updDate");
            return (Criteria) this;
        }

        public Criteria andCanvasNameIsNull() {
            addCriterion("canvas_name is null");
            return (Criteria) this;
        }

        public Criteria andCanvasNameIsNotNull() {
            addCriterion("canvas_name is not null");
            return (Criteria) this;
        }

        public Criteria andCanvasNameEqualTo(String value) {
            addCriterion("canvas_name =", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameNotEqualTo(String value) {
            addCriterion("canvas_name <>", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameGreaterThan(String value) {
            addCriterion("canvas_name >", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameGreaterThanOrEqualTo(String value) {
            addCriterion("canvas_name >=", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameLessThan(String value) {
            addCriterion("canvas_name <", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameLessThanOrEqualTo(String value) {
            addCriterion("canvas_name <=", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameLike(String value) {
            addCriterion("canvas_name like", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameNotLike(String value) {
            addCriterion("canvas_name not like", value, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameIn(List<String> values) {
            addCriterion("canvas_name in", values, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameNotIn(List<String> values) {
            addCriterion("canvas_name not in", values, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameBetween(String value1, String value2) {
            addCriterion("canvas_name between", value1, value2, "canvasName");
            return (Criteria) this;
        }

        public Criteria andCanvasNameNotBetween(String value1, String value2) {
            addCriterion("canvas_name not between", value1, value2, "canvasName");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorIsNull() {
            addCriterion("agent_operator is null");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorIsNotNull() {
            addCriterion("agent_operator is not null");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorEqualTo(String value) {
            addCriterion("agent_operator =", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorNotEqualTo(String value) {
            addCriterion("agent_operator <>", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorGreaterThan(String value) {
            addCriterion("agent_operator >", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorGreaterThanOrEqualTo(String value) {
            addCriterion("agent_operator >=", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorLessThan(String value) {
            addCriterion("agent_operator <", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorLessThanOrEqualTo(String value) {
            addCriterion("agent_operator <=", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorLike(String value) {
            addCriterion("agent_operator like", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorNotLike(String value) {
            addCriterion("agent_operator not like", value, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorIn(List<String> values) {
            addCriterion("agent_operator in", values, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorNotIn(List<String> values) {
            addCriterion("agent_operator not in", values, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorBetween(String value1, String value2) {
            addCriterion("agent_operator between", value1, value2, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andAgentOperatorNotBetween(String value1, String value2) {
            addCriterion("agent_operator not between", value1, value2, "agentOperator");
            return (Criteria) this;
        }

        public Criteria andGroupNameIsNull() {
            addCriterion("group_name is null");
            return (Criteria) this;
        }

        public Criteria andGroupNameIsNotNull() {
            addCriterion("group_name is not null");
            return (Criteria) this;
        }

        public Criteria andGroupNameEqualTo(String value) {
            addCriterion("group_name =", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotEqualTo(String value) {
            addCriterion("group_name <>", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameGreaterThan(String value) {
            addCriterion("group_name >", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameGreaterThanOrEqualTo(String value) {
            addCriterion("group_name >=", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameLessThan(String value) {
            addCriterion("group_name <", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameLessThanOrEqualTo(String value) {
            addCriterion("group_name <=", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameLike(String value) {
            addCriterion("group_name like", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotLike(String value) {
            addCriterion("group_name not like", value, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameIn(List<String> values) {
            addCriterion("group_name in", values, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotIn(List<String> values) {
            addCriterion("group_name not in", values, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameBetween(String value1, String value2) {
            addCriterion("group_name between", value1, value2, "groupName");
            return (Criteria) this;
        }

        public Criteria andGroupNameNotBetween(String value1, String value2) {
            addCriterion("group_name not between", value1, value2, "groupName");
            return (Criteria) this;
        }

        public Criteria andUserCountIsNull() {
            addCriterion("user_count is null");
            return (Criteria) this;
        }

        public Criteria andUserCountIsNotNull() {
            addCriterion("user_count is not null");
            return (Criteria) this;
        }

        public Criteria andUserCountEqualTo(Integer value) {
            addCriterion("user_count =", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountNotEqualTo(Integer value) {
            addCriterion("user_count <>", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountGreaterThan(Integer value) {
            addCriterion("user_count >", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("user_count >=", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountLessThan(Integer value) {
            addCriterion("user_count <", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("user_count <=", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountIn(List<Integer> values) {
            addCriterion("user_count in", values, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountNotIn(List<Integer> values) {
            addCriterion("user_count not in", values, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountBetween(Integer value1, Integer value2) {
            addCriterion("user_count between", value1, value2, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("user_count not between", value1, value2, "userCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountIsNull() {
            addCriterion("login_user_count is null");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountIsNotNull() {
            addCriterion("login_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountEqualTo(Integer value) {
            addCriterion("login_user_count =", value, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountNotEqualTo(Integer value) {
            addCriterion("login_user_count <>", value, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountGreaterThan(Integer value) {
            addCriterion("login_user_count >", value, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("login_user_count >=", value, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountLessThan(Integer value) {
            addCriterion("login_user_count <", value, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("login_user_count <=", value, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountIn(List<Integer> values) {
            addCriterion("login_user_count in", values, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountNotIn(List<Integer> values) {
            addCriterion("login_user_count not in", values, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountBetween(Integer value1, Integer value2) {
            addCriterion("login_user_count between", value1, value2, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("login_user_count not between", value1, value2, "loginUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountIsNull() {
            addCriterion("apply_submit_user_count is null");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountIsNotNull() {
            addCriterion("apply_submit_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountEqualTo(Integer value) {
            addCriterion("apply_submit_user_count =", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountNotEqualTo(Integer value) {
            addCriterion("apply_submit_user_count <>", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountGreaterThan(Integer value) {
            addCriterion("apply_submit_user_count >", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("apply_submit_user_count >=", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountLessThan(Integer value) {
            addCriterion("apply_submit_user_count <", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("apply_submit_user_count <=", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountIn(List<Integer> values) {
            addCriterion("apply_submit_user_count in", values, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountNotIn(List<Integer> values) {
            addCriterion("apply_submit_user_count not in", values, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountBetween(Integer value1, Integer value2) {
            addCriterion("apply_submit_user_count between", value1, value2, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("apply_submit_user_count not between", value1, value2, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountIsNull() {
            addCriterion("credit_success_user_count is null");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountIsNotNull() {
            addCriterion("credit_success_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountEqualTo(Integer value) {
            addCriterion("credit_success_user_count =", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountNotEqualTo(Integer value) {
            addCriterion("credit_success_user_count <>", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountGreaterThan(Integer value) {
            addCriterion("credit_success_user_count >", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("credit_success_user_count >=", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountLessThan(Integer value) {
            addCriterion("credit_success_user_count <", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("credit_success_user_count <=", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountIn(List<Integer> values) {
            addCriterion("credit_success_user_count in", values, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountNotIn(List<Integer> values) {
            addCriterion("credit_success_user_count not in", values, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountBetween(Integer value1, Integer value2) {
            addCriterion("credit_success_user_count between", value1, value2, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("credit_success_user_count not between", value1, value2, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andLoginRateIsNull() {
            addCriterion("login_rate is null");
            return (Criteria) this;
        }

        public Criteria andLoginRateIsNotNull() {
            addCriterion("login_rate is not null");
            return (Criteria) this;
        }

        public Criteria andLoginRateEqualTo(String value) {
            addCriterion("login_rate =", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateNotEqualTo(String value) {
            addCriterion("login_rate <>", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateGreaterThan(String value) {
            addCriterion("login_rate >", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateGreaterThanOrEqualTo(String value) {
            addCriterion("login_rate >=", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateLessThan(String value) {
            addCriterion("login_rate <", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateLessThanOrEqualTo(String value) {
            addCriterion("login_rate <=", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateLike(String value) {
            addCriterion("login_rate like", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateNotLike(String value) {
            addCriterion("login_rate not like", value, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateIn(List<String> values) {
            addCriterion("login_rate in", values, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateNotIn(List<String> values) {
            addCriterion("login_rate not in", values, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateBetween(String value1, String value2) {
            addCriterion("login_rate between", value1, value2, "loginRate");
            return (Criteria) this;
        }

        public Criteria andLoginRateNotBetween(String value1, String value2) {
            addCriterion("login_rate not between", value1, value2, "loginRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateIsNull() {
            addCriterion("apply_submit_rate is null");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateIsNotNull() {
            addCriterion("apply_submit_rate is not null");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateEqualTo(String value) {
            addCriterion("apply_submit_rate =", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateNotEqualTo(String value) {
            addCriterion("apply_submit_rate <>", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateGreaterThan(String value) {
            addCriterion("apply_submit_rate >", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateGreaterThanOrEqualTo(String value) {
            addCriterion("apply_submit_rate >=", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateLessThan(String value) {
            addCriterion("apply_submit_rate <", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateLessThanOrEqualTo(String value) {
            addCriterion("apply_submit_rate <=", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateLike(String value) {
            addCriterion("apply_submit_rate like", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateNotLike(String value) {
            addCriterion("apply_submit_rate not like", value, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateIn(List<String> values) {
            addCriterion("apply_submit_rate in", values, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateNotIn(List<String> values) {
            addCriterion("apply_submit_rate not in", values, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateBetween(String value1, String value2) {
            addCriterion("apply_submit_rate between", value1, value2, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andApplySubmitRateNotBetween(String value1, String value2) {
            addCriterion("apply_submit_rate not between", value1, value2, "applySubmitRate");
            return (Criteria) this;
        }

        public Criteria andPassRateIsNull() {
            addCriterion("pass_rate is null");
            return (Criteria) this;
        }

        public Criteria andPassRateIsNotNull() {
            addCriterion("pass_rate is not null");
            return (Criteria) this;
        }

        public Criteria andPassRateEqualTo(String value) {
            addCriterion("pass_rate =", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotEqualTo(String value) {
            addCriterion("pass_rate <>", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThan(String value) {
            addCriterion("pass_rate >", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateGreaterThanOrEqualTo(String value) {
            addCriterion("pass_rate >=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThan(String value) {
            addCriterion("pass_rate <", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLessThanOrEqualTo(String value) {
            addCriterion("pass_rate <=", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateLike(String value) {
            addCriterion("pass_rate like", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotLike(String value) {
            addCriterion("pass_rate not like", value, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateIn(List<String> values) {
            addCriterion("pass_rate in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotIn(List<String> values) {
            addCriterion("pass_rate not in", values, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateBetween(String value1, String value2) {
            addCriterion("pass_rate between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andPassRateNotBetween(String value1, String value2) {
            addCriterion("pass_rate not between", value1, value2, "passRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateIsNull() {
            addCriterion("credit_success_rate is null");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateIsNotNull() {
            addCriterion("credit_success_rate is not null");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateEqualTo(String value) {
            addCriterion("credit_success_rate =", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateNotEqualTo(String value) {
            addCriterion("credit_success_rate <>", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateGreaterThan(String value) {
            addCriterion("credit_success_rate >", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateGreaterThanOrEqualTo(String value) {
            addCriterion("credit_success_rate >=", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateLessThan(String value) {
            addCriterion("credit_success_rate <", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateLessThanOrEqualTo(String value) {
            addCriterion("credit_success_rate <=", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateLike(String value) {
            addCriterion("credit_success_rate like", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateNotLike(String value) {
            addCriterion("credit_success_rate not like", value, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateIn(List<String> values) {
            addCriterion("credit_success_rate in", values, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateNotIn(List<String> values) {
            addCriterion("credit_success_rate not in", values, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateBetween(String value1, String value2) {
            addCriterion("credit_success_rate between", value1, value2, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessRateNotBetween(String value1, String value2) {
            addCriterion("credit_success_rate not between", value1, value2, "creditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateIsNull() {
            addCriterion("delta_apply_submit_rate is null");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateIsNotNull() {
            addCriterion("delta_apply_submit_rate is not null");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateEqualTo(String value) {
            addCriterion("delta_apply_submit_rate =", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateNotEqualTo(String value) {
            addCriterion("delta_apply_submit_rate <>", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateGreaterThan(String value) {
            addCriterion("delta_apply_submit_rate >", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateGreaterThanOrEqualTo(String value) {
            addCriterion("delta_apply_submit_rate >=", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateLessThan(String value) {
            addCriterion("delta_apply_submit_rate <", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateLessThanOrEqualTo(String value) {
            addCriterion("delta_apply_submit_rate <=", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateLike(String value) {
            addCriterion("delta_apply_submit_rate like", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateNotLike(String value) {
            addCriterion("delta_apply_submit_rate not like", value, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateIn(List<String> values) {
            addCriterion("delta_apply_submit_rate in", values, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateNotIn(List<String> values) {
            addCriterion("delta_apply_submit_rate not in", values, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateBetween(String value1, String value2) {
            addCriterion("delta_apply_submit_rate between", value1, value2, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitRateNotBetween(String value1, String value2) {
            addCriterion("delta_apply_submit_rate not between", value1, value2, "deltaApplySubmitRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateIsNull() {
            addCriterion("delta_credit_success_rate is null");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateIsNotNull() {
            addCriterion("delta_credit_success_rate is not null");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateEqualTo(String value) {
            addCriterion("delta_credit_success_rate =", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateNotEqualTo(String value) {
            addCriterion("delta_credit_success_rate <>", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateGreaterThan(String value) {
            addCriterion("delta_credit_success_rate >", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateGreaterThanOrEqualTo(String value) {
            addCriterion("delta_credit_success_rate >=", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateLessThan(String value) {
            addCriterion("delta_credit_success_rate <", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateLessThanOrEqualTo(String value) {
            addCriterion("delta_credit_success_rate <=", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateLike(String value) {
            addCriterion("delta_credit_success_rate like", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateNotLike(String value) {
            addCriterion("delta_credit_success_rate not like", value, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateIn(List<String> values) {
            addCriterion("delta_credit_success_rate in", values, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateNotIn(List<String> values) {
            addCriterion("delta_credit_success_rate not in", values, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateBetween(String value1, String value2) {
            addCriterion("delta_credit_success_rate between", value1, value2, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessRateNotBetween(String value1, String value2) {
            addCriterion("delta_credit_success_rate not between", value1, value2, "deltaCreditSuccessRate");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountIsNull() {
            addCriterion("delta_apply_submit_count is null");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountIsNotNull() {
            addCriterion("delta_apply_submit_count is not null");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountEqualTo(Integer value) {
            addCriterion("delta_apply_submit_count =", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountNotEqualTo(Integer value) {
            addCriterion("delta_apply_submit_count <>", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountGreaterThan(Integer value) {
            addCriterion("delta_apply_submit_count >", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("delta_apply_submit_count >=", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountLessThan(Integer value) {
            addCriterion("delta_apply_submit_count <", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountLessThanOrEqualTo(Integer value) {
            addCriterion("delta_apply_submit_count <=", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountIn(List<Integer> values) {
            addCriterion("delta_apply_submit_count in", values, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountNotIn(List<Integer> values) {
            addCriterion("delta_apply_submit_count not in", values, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountBetween(Integer value1, Integer value2) {
            addCriterion("delta_apply_submit_count between", value1, value2, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountNotBetween(Integer value1, Integer value2) {
            addCriterion("delta_apply_submit_count not between", value1, value2, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountIsNull() {
            addCriterion("delta_credit_success_count is null");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountIsNotNull() {
            addCriterion("delta_credit_success_count is not null");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountEqualTo(Integer value) {
            addCriterion("delta_credit_success_count =", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountNotEqualTo(Integer value) {
            addCriterion("delta_credit_success_count <>", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountGreaterThan(Integer value) {
            addCriterion("delta_credit_success_count >", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("delta_credit_success_count >=", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountLessThan(Integer value) {
            addCriterion("delta_credit_success_count <", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountLessThanOrEqualTo(Integer value) {
            addCriterion("delta_credit_success_count <=", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountIn(List<Integer> values) {
            addCriterion("delta_credit_success_count in", values, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountNotIn(List<Integer> values) {
            addCriterion("delta_credit_success_count not in", values, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountBetween(Integer value1, Integer value2) {
            addCriterion("delta_credit_success_count between", value1, value2, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountNotBetween(Integer value1, Integer value2) {
            addCriterion("delta_credit_success_count not between", value1, value2, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountIsNull() {
            addCriterion("attr_apply_user_count is null");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountIsNotNull() {
            addCriterion("attr_apply_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountEqualTo(Integer value) {
            addCriterion("attr_apply_user_count =", value, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountNotEqualTo(Integer value) {
            addCriterion("attr_apply_user_count <>", value, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountGreaterThan(Integer value) {
            addCriterion("attr_apply_user_count >", value, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("attr_apply_user_count >=", value, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountLessThan(Integer value) {
            addCriterion("attr_apply_user_count <", value, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("attr_apply_user_count <=", value, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountIn(List<Integer> values) {
            addCriterion("attr_apply_user_count in", values, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountNotIn(List<Integer> values) {
            addCriterion("attr_apply_user_count not in", values, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountBetween(Integer value1, Integer value2) {
            addCriterion("attr_apply_user_count between", value1, value2, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrApplyUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("attr_apply_user_count not between", value1, value2, "attrApplyUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountIsNull() {
            addCriterion("attr_credit_user_count is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountIsNotNull() {
            addCriterion("attr_credit_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountEqualTo(Integer value) {
            addCriterion("attr_credit_user_count =", value, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountNotEqualTo(Integer value) {
            addCriterion("attr_credit_user_count <>", value, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountGreaterThan(Integer value) {
            addCriterion("attr_credit_user_count >", value, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count >=", value, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountLessThan(Integer value) {
            addCriterion("attr_credit_user_count <", value, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count <=", value, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountIn(List<Integer> values) {
            addCriterion("attr_credit_user_count in", values, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountNotIn(List<Integer> values) {
            addCriterion("attr_credit_user_count not in", values, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count between", value1, value2, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count not between", value1, value2, "attrCreditUserCount");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountAIsNull() {
            addCriterion("attr_credit_user_count_a is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountAIsNotNull() {
            addCriterion("attr_credit_user_count_a is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountAEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_a =", value, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountANotEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_a <>", value, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountAGreaterThan(Integer value) {
            addCriterion("attr_credit_user_count_a >", value, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountAGreaterThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_a >=", value, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountALessThan(Integer value) {
            addCriterion("attr_credit_user_count_a <", value, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountALessThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_a <=", value, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountAIn(List<Integer> values) {
            addCriterion("attr_credit_user_count_a in", values, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountANotIn(List<Integer> values) {
            addCriterion("attr_credit_user_count_a not in", values, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountABetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count_a between", value1, value2, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountANotBetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count_a not between", value1, value2, "attrCreditUserCountA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBIsNull() {
            addCriterion("attr_credit_user_count_b is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBIsNotNull() {
            addCriterion("attr_credit_user_count_b is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_b =", value, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBNotEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_b <>", value, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBGreaterThan(Integer value) {
            addCriterion("attr_credit_user_count_b >", value, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBGreaterThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_b >=", value, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBLessThan(Integer value) {
            addCriterion("attr_credit_user_count_b <", value, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBLessThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_b <=", value, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBIn(List<Integer> values) {
            addCriterion("attr_credit_user_count_b in", values, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBNotIn(List<Integer> values) {
            addCriterion("attr_credit_user_count_b not in", values, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBBetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count_b between", value1, value2, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountBNotBetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count_b not between", value1, value2, "attrCreditUserCountB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCIsNull() {
            addCriterion("attr_credit_user_count_c is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCIsNotNull() {
            addCriterion("attr_credit_user_count_c is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_c =", value, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCNotEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_c <>", value, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCGreaterThan(Integer value) {
            addCriterion("attr_credit_user_count_c >", value, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCGreaterThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_c >=", value, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCLessThan(Integer value) {
            addCriterion("attr_credit_user_count_c <", value, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCLessThanOrEqualTo(Integer value) {
            addCriterion("attr_credit_user_count_c <=", value, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCIn(List<Integer> values) {
            addCriterion("attr_credit_user_count_c in", values, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCNotIn(List<Integer> values) {
            addCriterion("attr_credit_user_count_c not in", values, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCBetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count_c between", value1, value2, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditUserCountCNotBetween(Integer value1, Integer value2) {
            addCriterion("attr_credit_user_count_c not between", value1, value2, "attrCreditUserCountC");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioIsNull() {
            addCriterion("attr_apply_ratio is null");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioIsNotNull() {
            addCriterion("attr_apply_ratio is not null");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioEqualTo(String value) {
            addCriterion("attr_apply_ratio =", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioNotEqualTo(String value) {
            addCriterion("attr_apply_ratio <>", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioGreaterThan(String value) {
            addCriterion("attr_apply_ratio >", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioGreaterThanOrEqualTo(String value) {
            addCriterion("attr_apply_ratio >=", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioLessThan(String value) {
            addCriterion("attr_apply_ratio <", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioLessThanOrEqualTo(String value) {
            addCriterion("attr_apply_ratio <=", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioLike(String value) {
            addCriterion("attr_apply_ratio like", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioNotLike(String value) {
            addCriterion("attr_apply_ratio not like", value, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioIn(List<String> values) {
            addCriterion("attr_apply_ratio in", values, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioNotIn(List<String> values) {
            addCriterion("attr_apply_ratio not in", values, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioBetween(String value1, String value2) {
            addCriterion("attr_apply_ratio between", value1, value2, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRatioNotBetween(String value1, String value2) {
            addCriterion("attr_apply_ratio not between", value1, value2, "attrApplyRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioIsNull() {
            addCriterion("attr_credit_ratio is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioIsNotNull() {
            addCriterion("attr_credit_ratio is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioEqualTo(String value) {
            addCriterion("attr_credit_ratio =", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioNotEqualTo(String value) {
            addCriterion("attr_credit_ratio <>", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioGreaterThan(String value) {
            addCriterion("attr_credit_ratio >", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioGreaterThanOrEqualTo(String value) {
            addCriterion("attr_credit_ratio >=", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioLessThan(String value) {
            addCriterion("attr_credit_ratio <", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioLessThanOrEqualTo(String value) {
            addCriterion("attr_credit_ratio <=", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioLike(String value) {
            addCriterion("attr_credit_ratio like", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioNotLike(String value) {
            addCriterion("attr_credit_ratio not like", value, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioIn(List<String> values) {
            addCriterion("attr_credit_ratio in", values, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioNotIn(List<String> values) {
            addCriterion("attr_credit_ratio not in", values, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioBetween(String value1, String value2) {
            addCriterion("attr_credit_ratio between", value1, value2, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRatioNotBetween(String value1, String value2) {
            addCriterion("attr_credit_ratio not between", value1, value2, "attrCreditRatio");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateIsNull() {
            addCriterion("attr_apply_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateIsNotNull() {
            addCriterion("attr_apply_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateEqualTo(String value) {
            addCriterion("attr_apply_rate =", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateNotEqualTo(String value) {
            addCriterion("attr_apply_rate <>", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateGreaterThan(String value) {
            addCriterion("attr_apply_rate >", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_apply_rate >=", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateLessThan(String value) {
            addCriterion("attr_apply_rate <", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateLessThanOrEqualTo(String value) {
            addCriterion("attr_apply_rate <=", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateLike(String value) {
            addCriterion("attr_apply_rate like", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateNotLike(String value) {
            addCriterion("attr_apply_rate not like", value, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateIn(List<String> values) {
            addCriterion("attr_apply_rate in", values, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateNotIn(List<String> values) {
            addCriterion("attr_apply_rate not in", values, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateBetween(String value1, String value2) {
            addCriterion("attr_apply_rate between", value1, value2, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrApplyRateNotBetween(String value1, String value2) {
            addCriterion("attr_apply_rate not between", value1, value2, "attrApplyRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateIsNull() {
            addCriterion("attr_credit_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateIsNotNull() {
            addCriterion("attr_credit_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateEqualTo(String value) {
            addCriterion("attr_credit_rate =", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateNotEqualTo(String value) {
            addCriterion("attr_credit_rate <>", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateGreaterThan(String value) {
            addCriterion("attr_credit_rate >", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_credit_rate >=", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateLessThan(String value) {
            addCriterion("attr_credit_rate <", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateLessThanOrEqualTo(String value) {
            addCriterion("attr_credit_rate <=", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateLike(String value) {
            addCriterion("attr_credit_rate like", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateNotLike(String value) {
            addCriterion("attr_credit_rate not like", value, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateIn(List<String> values) {
            addCriterion("attr_credit_rate in", values, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateNotIn(List<String> values) {
            addCriterion("attr_credit_rate not in", values, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateBetween(String value1, String value2) {
            addCriterion("attr_credit_rate between", value1, value2, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditRateNotBetween(String value1, String value2) {
            addCriterion("attr_credit_rate not between", value1, value2, "attrCreditRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioAIsNull() {
            addCriterion("attr_credit_count_ratio_a is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioAIsNotNull() {
            addCriterion("attr_credit_count_ratio_a is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioAEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_a =", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioANotEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_a <>", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioAGreaterThan(String value) {
            addCriterion("attr_credit_count_ratio_a >", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioAGreaterThanOrEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_a >=", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioALessThan(String value) {
            addCriterion("attr_credit_count_ratio_a <", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioALessThanOrEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_a <=", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioALike(String value) {
            addCriterion("attr_credit_count_ratio_a like", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioANotLike(String value) {
            addCriterion("attr_credit_count_ratio_a not like", value, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioAIn(List<String> values) {
            addCriterion("attr_credit_count_ratio_a in", values, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioANotIn(List<String> values) {
            addCriterion("attr_credit_count_ratio_a not in", values, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioABetween(String value1, String value2) {
            addCriterion("attr_credit_count_ratio_a between", value1, value2, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioANotBetween(String value1, String value2) {
            addCriterion("attr_credit_count_ratio_a not between", value1, value2, "attrCreditCountRatioA");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBIsNull() {
            addCriterion("attr_credit_count_ratio_b is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBIsNotNull() {
            addCriterion("attr_credit_count_ratio_b is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_b =", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBNotEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_b <>", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBGreaterThan(String value) {
            addCriterion("attr_credit_count_ratio_b >", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBGreaterThanOrEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_b >=", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBLessThan(String value) {
            addCriterion("attr_credit_count_ratio_b <", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBLessThanOrEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_b <=", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBLike(String value) {
            addCriterion("attr_credit_count_ratio_b like", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBNotLike(String value) {
            addCriterion("attr_credit_count_ratio_b not like", value, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBIn(List<String> values) {
            addCriterion("attr_credit_count_ratio_b in", values, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBNotIn(List<String> values) {
            addCriterion("attr_credit_count_ratio_b not in", values, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBBetween(String value1, String value2) {
            addCriterion("attr_credit_count_ratio_b between", value1, value2, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioBNotBetween(String value1, String value2) {
            addCriterion("attr_credit_count_ratio_b not between", value1, value2, "attrCreditCountRatioB");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCIsNull() {
            addCriterion("attr_credit_count_ratio_c is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCIsNotNull() {
            addCriterion("attr_credit_count_ratio_c is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_c =", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCNotEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_c <>", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCGreaterThan(String value) {
            addCriterion("attr_credit_count_ratio_c >", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCGreaterThanOrEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_c >=", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCLessThan(String value) {
            addCriterion("attr_credit_count_ratio_c <", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCLessThanOrEqualTo(String value) {
            addCriterion("attr_credit_count_ratio_c <=", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCLike(String value) {
            addCriterion("attr_credit_count_ratio_c like", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCNotLike(String value) {
            addCriterion("attr_credit_count_ratio_c not like", value, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCIn(List<String> values) {
            addCriterion("attr_credit_count_ratio_c in", values, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCNotIn(List<String> values) {
            addCriterion("attr_credit_count_ratio_c not in", values, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCBetween(String value1, String value2) {
            addCriterion("attr_credit_count_ratio_c between", value1, value2, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrCreditCountRatioCNotBetween(String value1, String value2) {
            addCriterion("attr_credit_count_ratio_c not between", value1, value2, "attrCreditCountRatioC");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitIsNull() {
            addCriterion("attr_avg_credit_limit is null");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitIsNotNull() {
            addCriterion("attr_avg_credit_limit is not null");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitEqualTo(String value) {
            addCriterion("attr_avg_credit_limit =", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitNotEqualTo(String value) {
            addCriterion("attr_avg_credit_limit <>", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitGreaterThan(String value) {
            addCriterion("attr_avg_credit_limit >", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitGreaterThanOrEqualTo(String value) {
            addCriterion("attr_avg_credit_limit >=", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitLessThan(String value) {
            addCriterion("attr_avg_credit_limit <", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitLessThanOrEqualTo(String value) {
            addCriterion("attr_avg_credit_limit <=", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitLike(String value) {
            addCriterion("attr_avg_credit_limit like", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitNotLike(String value) {
            addCriterion("attr_avg_credit_limit not like", value, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitIn(List<String> values) {
            addCriterion("attr_avg_credit_limit in", values, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitNotIn(List<String> values) {
            addCriterion("attr_avg_credit_limit not in", values, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitBetween(String value1, String value2) {
            addCriterion("attr_avg_credit_limit between", value1, value2, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrAvgCreditLimitNotBetween(String value1, String value2) {
            addCriterion("attr_avg_credit_limit not between", value1, value2, "attrAvgCreditLimit");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateIsNull() {
            addCriterion("attr_user_per_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateIsNotNull() {
            addCriterion("attr_user_per_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateEqualTo(String value) {
            addCriterion("attr_user_per_rate =", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateNotEqualTo(String value) {
            addCriterion("attr_user_per_rate <>", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateGreaterThan(String value) {
            addCriterion("attr_user_per_rate >", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_user_per_rate >=", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateLessThan(String value) {
            addCriterion("attr_user_per_rate <", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateLessThanOrEqualTo(String value) {
            addCriterion("attr_user_per_rate <=", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateLike(String value) {
            addCriterion("attr_user_per_rate like", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateNotLike(String value) {
            addCriterion("attr_user_per_rate not like", value, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateIn(List<String> values) {
            addCriterion("attr_user_per_rate in", values, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateNotIn(List<String> values) {
            addCriterion("attr_user_per_rate not in", values, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateBetween(String value1, String value2) {
            addCriterion("attr_user_per_rate between", value1, value2, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerRateNotBetween(String value1, String value2) {
            addCriterion("attr_user_per_rate not between", value1, value2, "attrUserPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateIsNull() {
            addCriterion("attr_user_act_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateIsNotNull() {
            addCriterion("attr_user_act_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateEqualTo(String value) {
            addCriterion("attr_user_act_rate =", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateNotEqualTo(String value) {
            addCriterion("attr_user_act_rate <>", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateGreaterThan(String value) {
            addCriterion("attr_user_act_rate >", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_user_act_rate >=", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateLessThan(String value) {
            addCriterion("attr_user_act_rate <", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateLessThanOrEqualTo(String value) {
            addCriterion("attr_user_act_rate <=", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateLike(String value) {
            addCriterion("attr_user_act_rate like", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateNotLike(String value) {
            addCriterion("attr_user_act_rate not like", value, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateIn(List<String> values) {
            addCriterion("attr_user_act_rate in", values, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateNotIn(List<String> values) {
            addCriterion("attr_user_act_rate not in", values, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateBetween(String value1, String value2) {
            addCriterion("attr_user_act_rate between", value1, value2, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserActRateNotBetween(String value1, String value2) {
            addCriterion("attr_user_act_rate not between", value1, value2, "attrUserActRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateIsNull() {
            addCriterion("attr_user_per_aprl_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateIsNotNull() {
            addCriterion("attr_user_per_aprl_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateEqualTo(String value) {
            addCriterion("attr_user_per_aprl_rate =", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateNotEqualTo(String value) {
            addCriterion("attr_user_per_aprl_rate <>", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateGreaterThan(String value) {
            addCriterion("attr_user_per_aprl_rate >", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_user_per_aprl_rate >=", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateLessThan(String value) {
            addCriterion("attr_user_per_aprl_rate <", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateLessThanOrEqualTo(String value) {
            addCriterion("attr_user_per_aprl_rate <=", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateLike(String value) {
            addCriterion("attr_user_per_aprl_rate like", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateNotLike(String value) {
            addCriterion("attr_user_per_aprl_rate not like", value, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateIn(List<String> values) {
            addCriterion("attr_user_per_aprl_rate in", values, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateNotIn(List<String> values) {
            addCriterion("attr_user_per_aprl_rate not in", values, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateBetween(String value1, String value2) {
            addCriterion("attr_user_per_aprl_rate between", value1, value2, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrUserPerAprlRateNotBetween(String value1, String value2) {
            addCriterion("attr_user_per_aprl_rate not between", value1, value2, "attrUserPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateIsNull() {
            addCriterion("attr_amt_per_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateIsNotNull() {
            addCriterion("attr_amt_per_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateEqualTo(String value) {
            addCriterion("attr_amt_per_rate =", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateNotEqualTo(String value) {
            addCriterion("attr_amt_per_rate <>", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateGreaterThan(String value) {
            addCriterion("attr_amt_per_rate >", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_amt_per_rate >=", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateLessThan(String value) {
            addCriterion("attr_amt_per_rate <", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateLessThanOrEqualTo(String value) {
            addCriterion("attr_amt_per_rate <=", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateLike(String value) {
            addCriterion("attr_amt_per_rate like", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateNotLike(String value) {
            addCriterion("attr_amt_per_rate not like", value, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateIn(List<String> values) {
            addCriterion("attr_amt_per_rate in", values, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateNotIn(List<String> values) {
            addCriterion("attr_amt_per_rate not in", values, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateBetween(String value1, String value2) {
            addCriterion("attr_amt_per_rate between", value1, value2, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerRateNotBetween(String value1, String value2) {
            addCriterion("attr_amt_per_rate not between", value1, value2, "attrAmtPerRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateIsNull() {
            addCriterion("attr_amt_act_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateIsNotNull() {
            addCriterion("attr_amt_act_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateEqualTo(String value) {
            addCriterion("attr_amt_act_rate =", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateNotEqualTo(String value) {
            addCriterion("attr_amt_act_rate <>", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateGreaterThan(String value) {
            addCriterion("attr_amt_act_rate >", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_amt_act_rate >=", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateLessThan(String value) {
            addCriterion("attr_amt_act_rate <", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateLessThanOrEqualTo(String value) {
            addCriterion("attr_amt_act_rate <=", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateLike(String value) {
            addCriterion("attr_amt_act_rate like", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateNotLike(String value) {
            addCriterion("attr_amt_act_rate not like", value, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateIn(List<String> values) {
            addCriterion("attr_amt_act_rate in", values, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateNotIn(List<String> values) {
            addCriterion("attr_amt_act_rate not in", values, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateBetween(String value1, String value2) {
            addCriterion("attr_amt_act_rate between", value1, value2, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtActRateNotBetween(String value1, String value2) {
            addCriterion("attr_amt_act_rate not between", value1, value2, "attrAmtActRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateIsNull() {
            addCriterion("attr_amt_per_aprl_rate is null");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateIsNotNull() {
            addCriterion("attr_amt_per_aprl_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateEqualTo(String value) {
            addCriterion("attr_amt_per_aprl_rate =", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateNotEqualTo(String value) {
            addCriterion("attr_amt_per_aprl_rate <>", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateGreaterThan(String value) {
            addCriterion("attr_amt_per_aprl_rate >", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateGreaterThanOrEqualTo(String value) {
            addCriterion("attr_amt_per_aprl_rate >=", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateLessThan(String value) {
            addCriterion("attr_amt_per_aprl_rate <", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateLessThanOrEqualTo(String value) {
            addCriterion("attr_amt_per_aprl_rate <=", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateLike(String value) {
            addCriterion("attr_amt_per_aprl_rate like", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateNotLike(String value) {
            addCriterion("attr_amt_per_aprl_rate not like", value, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateIn(List<String> values) {
            addCriterion("attr_amt_per_aprl_rate in", values, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateNotIn(List<String> values) {
            addCriterion("attr_amt_per_aprl_rate not in", values, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateBetween(String value1, String value2) {
            addCriterion("attr_amt_per_aprl_rate between", value1, value2, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrAmtPerAprlRateNotBetween(String value1, String value2) {
            addCriterion("attr_amt_per_aprl_rate not between", value1, value2, "attrAmtPerAprlRate");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtIsNull() {
            addCriterion("attr_credit_avg_act_amt is null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtIsNotNull() {
            addCriterion("attr_credit_avg_act_amt is not null");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtEqualTo(String value) {
            addCriterion("attr_credit_avg_act_amt =", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtNotEqualTo(String value) {
            addCriterion("attr_credit_avg_act_amt <>", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtGreaterThan(String value) {
            addCriterion("attr_credit_avg_act_amt >", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtGreaterThanOrEqualTo(String value) {
            addCriterion("attr_credit_avg_act_amt >=", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtLessThan(String value) {
            addCriterion("attr_credit_avg_act_amt <", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtLessThanOrEqualTo(String value) {
            addCriterion("attr_credit_avg_act_amt <=", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtLike(String value) {
            addCriterion("attr_credit_avg_act_amt like", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtNotLike(String value) {
            addCriterion("attr_credit_avg_act_amt not like", value, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtIn(List<String> values) {
            addCriterion("attr_credit_avg_act_amt in", values, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtNotIn(List<String> values) {
            addCriterion("attr_credit_avg_act_amt not in", values, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtBetween(String value1, String value2) {
            addCriterion("attr_credit_avg_act_amt between", value1, value2, "attrCreditAvgActAmt");
            return (Criteria) this;
        }

        public Criteria andAttrCreditAvgActAmtNotBetween(String value1, String value2) {
            addCriterion("attr_credit_avg_act_amt not between", value1, value2, "attrCreditAvgActAmt");
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