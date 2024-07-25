package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WubaSubmitConversionDataTransferCleanExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public WubaSubmitConversionDataTransferCleanExample() {
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

        public Criteria andBatchNoIsNull() {
            addCriterion("batch_no is null");
            return (Criteria) this;
        }

        public Criteria andBatchNoIsNotNull() {
            addCriterion("batch_no is not null");
            return (Criteria) this;
        }

        public Criteria andBatchNoEqualTo(String value) {
            addCriterion("batch_no =", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotEqualTo(String value) {
            addCriterion("batch_no <>", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoGreaterThan(String value) {
            addCriterion("batch_no >", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoGreaterThanOrEqualTo(String value) {
            addCriterion("batch_no >=", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoLessThan(String value) {
            addCriterion("batch_no <", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoLessThanOrEqualTo(String value) {
            addCriterion("batch_no <=", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoLike(String value) {
            addCriterion("batch_no like", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotLike(String value) {
            addCriterion("batch_no not like", value, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoIn(List<String> values) {
            addCriterion("batch_no in", values, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotIn(List<String> values) {
            addCriterion("batch_no not in", values, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoBetween(String value1, String value2) {
            addCriterion("batch_no between", value1, value2, "batchNo");
            return (Criteria) this;
        }

        public Criteria andBatchNoNotBetween(String value1, String value2) {
            addCriterion("batch_no not between", value1, value2, "batchNo");
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

        public Criteria andCleanStatusIsNull() {
            addCriterion("clean_status is null");
            return (Criteria) this;
        }

        public Criteria andCleanStatusIsNotNull() {
            addCriterion("clean_status is not null");
            return (Criteria) this;
        }

        public Criteria andCleanStatusEqualTo(Integer value) {
            addCriterion("clean_status =", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusNotEqualTo(Integer value) {
            addCriterion("clean_status <>", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusGreaterThan(Integer value) {
            addCriterion("clean_status >", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("clean_status >=", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusLessThan(Integer value) {
            addCriterion("clean_status <", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusLessThanOrEqualTo(Integer value) {
            addCriterion("clean_status <=", value, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusIn(List<Integer> values) {
            addCriterion("clean_status in", values, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusNotIn(List<Integer> values) {
            addCriterion("clean_status not in", values, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusBetween(Integer value1, Integer value2) {
            addCriterion("clean_status between", value1, value2, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andCleanStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("clean_status not between", value1, value2, "cleanStatus");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeIsNull() {
            addCriterion("last_login_time is null");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeIsNotNull() {
            addCriterion("last_login_time is not null");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeEqualTo(String value) {
            addCriterion("last_login_time =", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeNotEqualTo(String value) {
            addCriterion("last_login_time <>", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeGreaterThan(String value) {
            addCriterion("last_login_time >", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeGreaterThanOrEqualTo(String value) {
            addCriterion("last_login_time >=", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeLessThan(String value) {
            addCriterion("last_login_time <", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeLessThanOrEqualTo(String value) {
            addCriterion("last_login_time <=", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeLike(String value) {
            addCriterion("last_login_time like", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeNotLike(String value) {
            addCriterion("last_login_time not like", value, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeIn(List<String> values) {
            addCriterion("last_login_time in", values, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeNotIn(List<String> values) {
            addCriterion("last_login_time not in", values, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeBetween(String value1, String value2) {
            addCriterion("last_login_time between", value1, value2, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andLastLoginTimeNotBetween(String value1, String value2) {
            addCriterion("last_login_time not between", value1, value2, "lastLoginTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeIsNull() {
            addCriterion("finance_apply_time is null");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeIsNotNull() {
            addCriterion("finance_apply_time is not null");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeEqualTo(String value) {
            addCriterion("finance_apply_time =", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeNotEqualTo(String value) {
            addCriterion("finance_apply_time <>", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeGreaterThan(String value) {
            addCriterion("finance_apply_time >", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeGreaterThanOrEqualTo(String value) {
            addCriterion("finance_apply_time >=", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeLessThan(String value) {
            addCriterion("finance_apply_time <", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeLessThanOrEqualTo(String value) {
            addCriterion("finance_apply_time <=", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeLike(String value) {
            addCriterion("finance_apply_time like", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeNotLike(String value) {
            addCriterion("finance_apply_time not like", value, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeIn(List<String> values) {
            addCriterion("finance_apply_time in", values, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeNotIn(List<String> values) {
            addCriterion("finance_apply_time not in", values, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeBetween(String value1, String value2) {
            addCriterion("finance_apply_time between", value1, value2, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceApplyTimeNotBetween(String value1, String value2) {
            addCriterion("finance_apply_time not between", value1, value2, "financeApplyTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusIsNull() {
            addCriterion("finance_credit_status is null");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusIsNotNull() {
            addCriterion("finance_credit_status is not null");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusEqualTo(String value) {
            addCriterion("finance_credit_status =", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusNotEqualTo(String value) {
            addCriterion("finance_credit_status <>", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusGreaterThan(String value) {
            addCriterion("finance_credit_status >", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusGreaterThanOrEqualTo(String value) {
            addCriterion("finance_credit_status >=", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusLessThan(String value) {
            addCriterion("finance_credit_status <", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusLessThanOrEqualTo(String value) {
            addCriterion("finance_credit_status <=", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusLike(String value) {
            addCriterion("finance_credit_status like", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusNotLike(String value) {
            addCriterion("finance_credit_status not like", value, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusIn(List<String> values) {
            addCriterion("finance_credit_status in", values, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusNotIn(List<String> values) {
            addCriterion("finance_credit_status not in", values, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusBetween(String value1, String value2) {
            addCriterion("finance_credit_status between", value1, value2, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditStatusNotBetween(String value1, String value2) {
            addCriterion("finance_credit_status not between", value1, value2, "financeCreditStatus");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeIsNull() {
            addCriterion("finance_credit_finish_time is null");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeIsNotNull() {
            addCriterion("finance_credit_finish_time is not null");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeEqualTo(String value) {
            addCriterion("finance_credit_finish_time =", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeNotEqualTo(String value) {
            addCriterion("finance_credit_finish_time <>", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeGreaterThan(String value) {
            addCriterion("finance_credit_finish_time >", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeGreaterThanOrEqualTo(String value) {
            addCriterion("finance_credit_finish_time >=", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeLessThan(String value) {
            addCriterion("finance_credit_finish_time <", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeLessThanOrEqualTo(String value) {
            addCriterion("finance_credit_finish_time <=", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeLike(String value) {
            addCriterion("finance_credit_finish_time like", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeNotLike(String value) {
            addCriterion("finance_credit_finish_time not like", value, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeIn(List<String> values) {
            addCriterion("finance_credit_finish_time in", values, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeNotIn(List<String> values) {
            addCriterion("finance_credit_finish_time not in", values, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeBetween(String value1, String value2) {
            addCriterion("finance_credit_finish_time between", value1, value2, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andFinanceCreditFinishTimeNotBetween(String value1, String value2) {
            addCriterion("finance_credit_finish_time not between", value1, value2, "financeCreditFinishTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeIsNull() {
            addCriterion("debt_time is null");
            return (Criteria) this;
        }

        public Criteria andDebtTimeIsNotNull() {
            addCriterion("debt_time is not null");
            return (Criteria) this;
        }

        public Criteria andDebtTimeEqualTo(String value) {
            addCriterion("debt_time =", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeNotEqualTo(String value) {
            addCriterion("debt_time <>", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeGreaterThan(String value) {
            addCriterion("debt_time >", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeGreaterThanOrEqualTo(String value) {
            addCriterion("debt_time >=", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeLessThan(String value) {
            addCriterion("debt_time <", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeLessThanOrEqualTo(String value) {
            addCriterion("debt_time <=", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeLike(String value) {
            addCriterion("debt_time like", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeNotLike(String value) {
            addCriterion("debt_time not like", value, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeIn(List<String> values) {
            addCriterion("debt_time in", values, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeNotIn(List<String> values) {
            addCriterion("debt_time not in", values, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeBetween(String value1, String value2) {
            addCriterion("debt_time between", value1, value2, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtTimeNotBetween(String value1, String value2) {
            addCriterion("debt_time not between", value1, value2, "debtTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeIsNull() {
            addCriterion("debt_pass_time is null");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeIsNotNull() {
            addCriterion("debt_pass_time is not null");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeEqualTo(String value) {
            addCriterion("debt_pass_time =", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeNotEqualTo(String value) {
            addCriterion("debt_pass_time <>", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeGreaterThan(String value) {
            addCriterion("debt_pass_time >", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeGreaterThanOrEqualTo(String value) {
            addCriterion("debt_pass_time >=", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeLessThan(String value) {
            addCriterion("debt_pass_time <", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeLessThanOrEqualTo(String value) {
            addCriterion("debt_pass_time <=", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeLike(String value) {
            addCriterion("debt_pass_time like", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeNotLike(String value) {
            addCriterion("debt_pass_time not like", value, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeIn(List<String> values) {
            addCriterion("debt_pass_time in", values, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeNotIn(List<String> values) {
            addCriterion("debt_pass_time not in", values, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeBetween(String value1, String value2) {
            addCriterion("debt_pass_time between", value1, value2, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andDebtPassTimeNotBetween(String value1, String value2) {
            addCriterion("debt_pass_time not between", value1, value2, "debtPassTime");
            return (Criteria) this;
        }

        public Criteria andLoanAmtIsNull() {
            addCriterion("loan_amt is null");
            return (Criteria) this;
        }

        public Criteria andLoanAmtIsNotNull() {
            addCriterion("loan_amt is not null");
            return (Criteria) this;
        }

        public Criteria andLoanAmtEqualTo(String value) {
            addCriterion("loan_amt =", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtNotEqualTo(String value) {
            addCriterion("loan_amt <>", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtGreaterThan(String value) {
            addCriterion("loan_amt >", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtGreaterThanOrEqualTo(String value) {
            addCriterion("loan_amt >=", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtLessThan(String value) {
            addCriterion("loan_amt <", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtLessThanOrEqualTo(String value) {
            addCriterion("loan_amt <=", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtLike(String value) {
            addCriterion("loan_amt like", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtNotLike(String value) {
            addCriterion("loan_amt not like", value, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtIn(List<String> values) {
            addCriterion("loan_amt in", values, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtNotIn(List<String> values) {
            addCriterion("loan_amt not in", values, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtBetween(String value1, String value2) {
            addCriterion("loan_amt between", value1, value2, "loanAmt");
            return (Criteria) this;
        }

        public Criteria andLoanAmtNotBetween(String value1, String value2) {
            addCriterion("loan_amt not between", value1, value2, "loanAmt");
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

        public Criteria andIsDeletedIsNull() {
            addCriterion("is_deleted is null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIsNotNull() {
            addCriterion("is_deleted is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeletedEqualTo(Integer value) {
            addCriterion("is_deleted =", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotEqualTo(Integer value) {
            addCriterion("is_deleted <>", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThan(Integer value) {
            addCriterion("is_deleted >", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_deleted >=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThan(Integer value) {
            addCriterion("is_deleted <", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedLessThanOrEqualTo(Integer value) {
            addCriterion("is_deleted <=", value, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedIn(List<Integer> values) {
            addCriterion("is_deleted in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotIn(List<Integer> values) {
            addCriterion("is_deleted not in", values, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted between", value1, value2, "isDeleted");
            return (Criteria) this;
        }

        public Criteria andIsDeletedNotBetween(Integer value1, Integer value2) {
            addCriterion("is_deleted not between", value1, value2, "isDeleted");
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

        public Criteria andTaskIdIsNull() {
            addCriterion("task_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNotNull() {
            addCriterion("task_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdEqualTo(Long value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(Long value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(Long value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(Long value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(Long value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(Long value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<Long> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<Long> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(Long value1, Long value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(Long value1, Long value2) {
            addCriterion("task_id not between", value1, value2, "taskId");
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