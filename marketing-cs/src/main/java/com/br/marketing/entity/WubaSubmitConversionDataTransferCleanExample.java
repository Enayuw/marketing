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

        public Criteria andLastlogintimeIsNull() {
            addCriterion("lastLoginTime is null");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeIsNotNull() {
            addCriterion("lastLoginTime is not null");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeEqualTo(String value) {
            addCriterion("lastLoginTime =", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeNotEqualTo(String value) {
            addCriterion("lastLoginTime <>", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeGreaterThan(String value) {
            addCriterion("lastLoginTime >", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeGreaterThanOrEqualTo(String value) {
            addCriterion("lastLoginTime >=", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeLessThan(String value) {
            addCriterion("lastLoginTime <", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeLessThanOrEqualTo(String value) {
            addCriterion("lastLoginTime <=", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeLike(String value) {
            addCriterion("lastLoginTime like", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeNotLike(String value) {
            addCriterion("lastLoginTime not like", value, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeIn(List<String> values) {
            addCriterion("lastLoginTime in", values, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeNotIn(List<String> values) {
            addCriterion("lastLoginTime not in", values, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeBetween(String value1, String value2) {
            addCriterion("lastLoginTime between", value1, value2, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andLastlogintimeNotBetween(String value1, String value2) {
            addCriterion("lastLoginTime not between", value1, value2, "lastlogintime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeIsNull() {
            addCriterion("financeApplyTime is null");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeIsNotNull() {
            addCriterion("financeApplyTime is not null");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeEqualTo(String value) {
            addCriterion("financeApplyTime =", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeNotEqualTo(String value) {
            addCriterion("financeApplyTime <>", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeGreaterThan(String value) {
            addCriterion("financeApplyTime >", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeGreaterThanOrEqualTo(String value) {
            addCriterion("financeApplyTime >=", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeLessThan(String value) {
            addCriterion("financeApplyTime <", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeLessThanOrEqualTo(String value) {
            addCriterion("financeApplyTime <=", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeLike(String value) {
            addCriterion("financeApplyTime like", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeNotLike(String value) {
            addCriterion("financeApplyTime not like", value, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeIn(List<String> values) {
            addCriterion("financeApplyTime in", values, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeNotIn(List<String> values) {
            addCriterion("financeApplyTime not in", values, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeBetween(String value1, String value2) {
            addCriterion("financeApplyTime between", value1, value2, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinanceapplytimeNotBetween(String value1, String value2) {
            addCriterion("financeApplyTime not between", value1, value2, "financeapplytime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusIsNull() {
            addCriterion("financeCreditStatus is null");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusIsNotNull() {
            addCriterion("financeCreditStatus is not null");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusEqualTo(String value) {
            addCriterion("financeCreditStatus =", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusNotEqualTo(String value) {
            addCriterion("financeCreditStatus <>", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusGreaterThan(String value) {
            addCriterion("financeCreditStatus >", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusGreaterThanOrEqualTo(String value) {
            addCriterion("financeCreditStatus >=", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusLessThan(String value) {
            addCriterion("financeCreditStatus <", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusLessThanOrEqualTo(String value) {
            addCriterion("financeCreditStatus <=", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusLike(String value) {
            addCriterion("financeCreditStatus like", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusNotLike(String value) {
            addCriterion("financeCreditStatus not like", value, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusIn(List<String> values) {
            addCriterion("financeCreditStatus in", values, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusNotIn(List<String> values) {
            addCriterion("financeCreditStatus not in", values, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusBetween(String value1, String value2) {
            addCriterion("financeCreditStatus between", value1, value2, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditstatusNotBetween(String value1, String value2) {
            addCriterion("financeCreditStatus not between", value1, value2, "financecreditstatus");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeIsNull() {
            addCriterion("financeCreditFinishTime is null");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeIsNotNull() {
            addCriterion("financeCreditFinishTime is not null");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeEqualTo(String value) {
            addCriterion("financeCreditFinishTime =", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeNotEqualTo(String value) {
            addCriterion("financeCreditFinishTime <>", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeGreaterThan(String value) {
            addCriterion("financeCreditFinishTime >", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeGreaterThanOrEqualTo(String value) {
            addCriterion("financeCreditFinishTime >=", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeLessThan(String value) {
            addCriterion("financeCreditFinishTime <", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeLessThanOrEqualTo(String value) {
            addCriterion("financeCreditFinishTime <=", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeLike(String value) {
            addCriterion("financeCreditFinishTime like", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeNotLike(String value) {
            addCriterion("financeCreditFinishTime not like", value, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeIn(List<String> values) {
            addCriterion("financeCreditFinishTime in", values, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeNotIn(List<String> values) {
            addCriterion("financeCreditFinishTime not in", values, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeBetween(String value1, String value2) {
            addCriterion("financeCreditFinishTime between", value1, value2, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andFinancecreditfinishtimeNotBetween(String value1, String value2) {
            addCriterion("financeCreditFinishTime not between", value1, value2, "financecreditfinishtime");
            return (Criteria) this;
        }

        public Criteria andDebttimeIsNull() {
            addCriterion("debtTime is null");
            return (Criteria) this;
        }

        public Criteria andDebttimeIsNotNull() {
            addCriterion("debtTime is not null");
            return (Criteria) this;
        }

        public Criteria andDebttimeEqualTo(String value) {
            addCriterion("debtTime =", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeNotEqualTo(String value) {
            addCriterion("debtTime <>", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeGreaterThan(String value) {
            addCriterion("debtTime >", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeGreaterThanOrEqualTo(String value) {
            addCriterion("debtTime >=", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeLessThan(String value) {
            addCriterion("debtTime <", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeLessThanOrEqualTo(String value) {
            addCriterion("debtTime <=", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeLike(String value) {
            addCriterion("debtTime like", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeNotLike(String value) {
            addCriterion("debtTime not like", value, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeIn(List<String> values) {
            addCriterion("debtTime in", values, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeNotIn(List<String> values) {
            addCriterion("debtTime not in", values, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeBetween(String value1, String value2) {
            addCriterion("debtTime between", value1, value2, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebttimeNotBetween(String value1, String value2) {
            addCriterion("debtTime not between", value1, value2, "debttime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeIsNull() {
            addCriterion("debtPassTime is null");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeIsNotNull() {
            addCriterion("debtPassTime is not null");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeEqualTo(String value) {
            addCriterion("debtPassTime =", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeNotEqualTo(String value) {
            addCriterion("debtPassTime <>", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeGreaterThan(String value) {
            addCriterion("debtPassTime >", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeGreaterThanOrEqualTo(String value) {
            addCriterion("debtPassTime >=", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeLessThan(String value) {
            addCriterion("debtPassTime <", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeLessThanOrEqualTo(String value) {
            addCriterion("debtPassTime <=", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeLike(String value) {
            addCriterion("debtPassTime like", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeNotLike(String value) {
            addCriterion("debtPassTime not like", value, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeIn(List<String> values) {
            addCriterion("debtPassTime in", values, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeNotIn(List<String> values) {
            addCriterion("debtPassTime not in", values, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeBetween(String value1, String value2) {
            addCriterion("debtPassTime between", value1, value2, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andDebtpasstimeNotBetween(String value1, String value2) {
            addCriterion("debtPassTime not between", value1, value2, "debtpasstime");
            return (Criteria) this;
        }

        public Criteria andLoanamtIsNull() {
            addCriterion("loanAmt is null");
            return (Criteria) this;
        }

        public Criteria andLoanamtIsNotNull() {
            addCriterion("loanAmt is not null");
            return (Criteria) this;
        }

        public Criteria andLoanamtEqualTo(String value) {
            addCriterion("loanAmt =", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtNotEqualTo(String value) {
            addCriterion("loanAmt <>", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtGreaterThan(String value) {
            addCriterion("loanAmt >", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtGreaterThanOrEqualTo(String value) {
            addCriterion("loanAmt >=", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtLessThan(String value) {
            addCriterion("loanAmt <", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtLessThanOrEqualTo(String value) {
            addCriterion("loanAmt <=", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtLike(String value) {
            addCriterion("loanAmt like", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtNotLike(String value) {
            addCriterion("loanAmt not like", value, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtIn(List<String> values) {
            addCriterion("loanAmt in", values, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtNotIn(List<String> values) {
            addCriterion("loanAmt not in", values, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtBetween(String value1, String value2) {
            addCriterion("loanAmt between", value1, value2, "loanamt");
            return (Criteria) this;
        }

        public Criteria andLoanamtNotBetween(String value1, String value2) {
            addCriterion("loanAmt not between", value1, value2, "loanamt");
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