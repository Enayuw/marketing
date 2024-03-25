package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QifuStrategyReportDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QifuStrategyReportDataExample() {
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

        public Criteria andUpdateDateIsNull() {
            addCriterion("update_date is null");
            return (Criteria) this;
        }

        public Criteria andUpdateDateIsNotNull() {
            addCriterion("update_date is not null");
            return (Criteria) this;
        }

        public Criteria andUpdateDateEqualTo(String value) {
            addCriterion("update_date =", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateNotEqualTo(String value) {
            addCriterion("update_date <>", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateGreaterThan(String value) {
            addCriterion("update_date >", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateGreaterThanOrEqualTo(String value) {
            addCriterion("update_date >=", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateLessThan(String value) {
            addCriterion("update_date <", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateLessThanOrEqualTo(String value) {
            addCriterion("update_date <=", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateLike(String value) {
            addCriterion("update_date like", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateNotLike(String value) {
            addCriterion("update_date not like", value, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateIn(List<String> values) {
            addCriterion("update_date in", values, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateNotIn(List<String> values) {
            addCriterion("update_date not in", values, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateBetween(String value1, String value2) {
            addCriterion("update_date between", value1, value2, "updateDate");
            return (Criteria) this;
        }

        public Criteria andUpdateDateNotBetween(String value1, String value2) {
            addCriterion("update_date not between", value1, value2, "updateDate");
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

        public Criteria andSupplierIsNull() {
            addCriterion("supplier is null");
            return (Criteria) this;
        }

        public Criteria andSupplierIsNotNull() {
            addCriterion("supplier is not null");
            return (Criteria) this;
        }

        public Criteria andSupplierEqualTo(String value) {
            addCriterion("supplier =", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierNotEqualTo(String value) {
            addCriterion("supplier <>", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierGreaterThan(String value) {
            addCriterion("supplier >", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierGreaterThanOrEqualTo(String value) {
            addCriterion("supplier >=", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierLessThan(String value) {
            addCriterion("supplier <", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierLessThanOrEqualTo(String value) {
            addCriterion("supplier <=", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierLike(String value) {
            addCriterion("supplier like", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierNotLike(String value) {
            addCriterion("supplier not like", value, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierIn(List<String> values) {
            addCriterion("supplier in", values, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierNotIn(List<String> values) {
            addCriterion("supplier not in", values, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierBetween(String value1, String value2) {
            addCriterion("supplier between", value1, value2, "supplier");
            return (Criteria) this;
        }

        public Criteria andSupplierNotBetween(String value1, String value2) {
            addCriterion("supplier not between", value1, value2, "supplier");
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

        public Criteria andUserCountEqualTo(String value) {
            addCriterion("user_count =", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountNotEqualTo(String value) {
            addCriterion("user_count <>", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountGreaterThan(String value) {
            addCriterion("user_count >", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountGreaterThanOrEqualTo(String value) {
            addCriterion("user_count >=", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountLessThan(String value) {
            addCriterion("user_count <", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountLessThanOrEqualTo(String value) {
            addCriterion("user_count <=", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountLike(String value) {
            addCriterion("user_count like", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountNotLike(String value) {
            addCriterion("user_count not like", value, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountIn(List<String> values) {
            addCriterion("user_count in", values, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountNotIn(List<String> values) {
            addCriterion("user_count not in", values, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountBetween(String value1, String value2) {
            addCriterion("user_count between", value1, value2, "userCount");
            return (Criteria) this;
        }

        public Criteria andUserCountNotBetween(String value1, String value2) {
            addCriterion("user_count not between", value1, value2, "userCount");
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

        public Criteria andApplySubmitUserCountEqualTo(String value) {
            addCriterion("apply_submit_user_count =", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountNotEqualTo(String value) {
            addCriterion("apply_submit_user_count <>", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountGreaterThan(String value) {
            addCriterion("apply_submit_user_count >", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountGreaterThanOrEqualTo(String value) {
            addCriterion("apply_submit_user_count >=", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountLessThan(String value) {
            addCriterion("apply_submit_user_count <", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountLessThanOrEqualTo(String value) {
            addCriterion("apply_submit_user_count <=", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountLike(String value) {
            addCriterion("apply_submit_user_count like", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountNotLike(String value) {
            addCriterion("apply_submit_user_count not like", value, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountIn(List<String> values) {
            addCriterion("apply_submit_user_count in", values, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountNotIn(List<String> values) {
            addCriterion("apply_submit_user_count not in", values, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountBetween(String value1, String value2) {
            addCriterion("apply_submit_user_count between", value1, value2, "applySubmitUserCount");
            return (Criteria) this;
        }

        public Criteria andApplySubmitUserCountNotBetween(String value1, String value2) {
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

        public Criteria andCreditSuccessUserCountEqualTo(String value) {
            addCriterion("credit_success_user_count =", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountNotEqualTo(String value) {
            addCriterion("credit_success_user_count <>", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountGreaterThan(String value) {
            addCriterion("credit_success_user_count >", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountGreaterThanOrEqualTo(String value) {
            addCriterion("credit_success_user_count >=", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountLessThan(String value) {
            addCriterion("credit_success_user_count <", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountLessThanOrEqualTo(String value) {
            addCriterion("credit_success_user_count <=", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountLike(String value) {
            addCriterion("credit_success_user_count like", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountNotLike(String value) {
            addCriterion("credit_success_user_count not like", value, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountIn(List<String> values) {
            addCriterion("credit_success_user_count in", values, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountNotIn(List<String> values) {
            addCriterion("credit_success_user_count not in", values, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountBetween(String value1, String value2) {
            addCriterion("credit_success_user_count between", value1, value2, "creditSuccessUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditSuccessUserCountNotBetween(String value1, String value2) {
            addCriterion("credit_success_user_count not between", value1, value2, "creditSuccessUserCount");
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

        public Criteria andDeltaApplySubmitCountEqualTo(String value) {
            addCriterion("delta_apply_submit_count =", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountNotEqualTo(String value) {
            addCriterion("delta_apply_submit_count <>", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountGreaterThan(String value) {
            addCriterion("delta_apply_submit_count >", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountGreaterThanOrEqualTo(String value) {
            addCriterion("delta_apply_submit_count >=", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountLessThan(String value) {
            addCriterion("delta_apply_submit_count <", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountLessThanOrEqualTo(String value) {
            addCriterion("delta_apply_submit_count <=", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountLike(String value) {
            addCriterion("delta_apply_submit_count like", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountNotLike(String value) {
            addCriterion("delta_apply_submit_count not like", value, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountIn(List<String> values) {
            addCriterion("delta_apply_submit_count in", values, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountNotIn(List<String> values) {
            addCriterion("delta_apply_submit_count not in", values, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountBetween(String value1, String value2) {
            addCriterion("delta_apply_submit_count between", value1, value2, "deltaApplySubmitCount");
            return (Criteria) this;
        }

        public Criteria andDeltaApplySubmitCountNotBetween(String value1, String value2) {
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

        public Criteria andDeltaCreditSuccessCountEqualTo(String value) {
            addCriterion("delta_credit_success_count =", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountNotEqualTo(String value) {
            addCriterion("delta_credit_success_count <>", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountGreaterThan(String value) {
            addCriterion("delta_credit_success_count >", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountGreaterThanOrEqualTo(String value) {
            addCriterion("delta_credit_success_count >=", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountLessThan(String value) {
            addCriterion("delta_credit_success_count <", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountLessThanOrEqualTo(String value) {
            addCriterion("delta_credit_success_count <=", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountLike(String value) {
            addCriterion("delta_credit_success_count like", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountNotLike(String value) {
            addCriterion("delta_credit_success_count not like", value, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountIn(List<String> values) {
            addCriterion("delta_credit_success_count in", values, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountNotIn(List<String> values) {
            addCriterion("delta_credit_success_count not in", values, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountBetween(String value1, String value2) {
            addCriterion("delta_credit_success_count between", value1, value2, "deltaCreditSuccessCount");
            return (Criteria) this;
        }

        public Criteria andDeltaCreditSuccessCountNotBetween(String value1, String value2) {
            addCriterion("delta_credit_success_count not between", value1, value2, "deltaCreditSuccessCount");
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