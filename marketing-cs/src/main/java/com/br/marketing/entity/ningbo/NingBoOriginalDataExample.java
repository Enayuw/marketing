package com.br.marketing.entity.ningbo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class NingBoOriginalDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public NingBoOriginalDataExample() {
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

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
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

        public Criteria andTaskDateIsNull() {
            addCriterion("task_date is null");
            return (Criteria) this;
        }

        public Criteria andTaskDateIsNotNull() {
            addCriterion("task_date is not null");
            return (Criteria) this;
        }

        public Criteria andTaskDateEqualTo(Date value) {
            addCriterionForJDBCDate("task_date =", value, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("task_date <>", value, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateGreaterThan(Date value) {
            addCriterionForJDBCDate("task_date >", value, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("task_date >=", value, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateLessThan(Date value) {
            addCriterionForJDBCDate("task_date <", value, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("task_date <=", value, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateIn(List<Date> values) {
            addCriterionForJDBCDate("task_date in", values, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("task_date not in", values, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("task_date between", value1, value2, "taskDate");
            return (Criteria) this;
        }

        public Criteria andTaskDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("task_date not between", value1, value2, "taskDate");
            return (Criteria) this;
        }

        public Criteria andBankNameIsNull() {
            addCriterion("bank_name is null");
            return (Criteria) this;
        }

        public Criteria andBankNameIsNotNull() {
            addCriterion("bank_name is not null");
            return (Criteria) this;
        }

        public Criteria andBankNameEqualTo(String value) {
            addCriterion("bank_name =", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameNotEqualTo(String value) {
            addCriterion("bank_name <>", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameGreaterThan(String value) {
            addCriterion("bank_name >", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameGreaterThanOrEqualTo(String value) {
            addCriterion("bank_name >=", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameLessThan(String value) {
            addCriterion("bank_name <", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameLessThanOrEqualTo(String value) {
            addCriterion("bank_name <=", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameLike(String value) {
            addCriterion("bank_name like", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameNotLike(String value) {
            addCriterion("bank_name not like", value, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameIn(List<String> values) {
            addCriterion("bank_name in", values, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameNotIn(List<String> values) {
            addCriterion("bank_name not in", values, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameBetween(String value1, String value2) {
            addCriterion("bank_name between", value1, value2, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankNameNotBetween(String value1, String value2) {
            addCriterion("bank_name not between", value1, value2, "bankName");
            return (Criteria) this;
        }

        public Criteria andBankName2IsNull() {
            addCriterion("bank_name2 is null");
            return (Criteria) this;
        }

        public Criteria andBankName2IsNotNull() {
            addCriterion("bank_name2 is not null");
            return (Criteria) this;
        }

        public Criteria andBankName2EqualTo(String value) {
            addCriterion("bank_name2 =", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2NotEqualTo(String value) {
            addCriterion("bank_name2 <>", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2GreaterThan(String value) {
            addCriterion("bank_name2 >", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2GreaterThanOrEqualTo(String value) {
            addCriterion("bank_name2 >=", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2LessThan(String value) {
            addCriterion("bank_name2 <", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2LessThanOrEqualTo(String value) {
            addCriterion("bank_name2 <=", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2Like(String value) {
            addCriterion("bank_name2 like", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2NotLike(String value) {
            addCriterion("bank_name2 not like", value, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2In(List<String> values) {
            addCriterion("bank_name2 in", values, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2NotIn(List<String> values) {
            addCriterion("bank_name2 not in", values, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2Between(String value1, String value2) {
            addCriterion("bank_name2 between", value1, value2, "bankName2");
            return (Criteria) this;
        }

        public Criteria andBankName2NotBetween(String value1, String value2) {
            addCriterion("bank_name2 not between", value1, value2, "bankName2");
            return (Criteria) this;
        }

        public Criteria andFirstNameIsNull() {
            addCriterion("first_name is null");
            return (Criteria) this;
        }

        public Criteria andFirstNameIsNotNull() {
            addCriterion("first_name is not null");
            return (Criteria) this;
        }

        public Criteria andFirstNameEqualTo(String value) {
            addCriterion("first_name =", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameNotEqualTo(String value) {
            addCriterion("first_name <>", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameGreaterThan(String value) {
            addCriterion("first_name >", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameGreaterThanOrEqualTo(String value) {
            addCriterion("first_name >=", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameLessThan(String value) {
            addCriterion("first_name <", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameLessThanOrEqualTo(String value) {
            addCriterion("first_name <=", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameLike(String value) {
            addCriterion("first_name like", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameNotLike(String value) {
            addCriterion("first_name not like", value, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameIn(List<String> values) {
            addCriterion("first_name in", values, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameNotIn(List<String> values) {
            addCriterion("first_name not in", values, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameBetween(String value1, String value2) {
            addCriterion("first_name between", value1, value2, "firstName");
            return (Criteria) this;
        }

        public Criteria andFirstNameNotBetween(String value1, String value2) {
            addCriterion("first_name not between", value1, value2, "firstName");
            return (Criteria) this;
        }

        public Criteria andGenderIsNull() {
            addCriterion("gender is null");
            return (Criteria) this;
        }

        public Criteria andGenderIsNotNull() {
            addCriterion("gender is not null");
            return (Criteria) this;
        }

        public Criteria andGenderEqualTo(String value) {
            addCriterion("gender =", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotEqualTo(String value) {
            addCriterion("gender <>", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderGreaterThan(String value) {
            addCriterion("gender >", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderGreaterThanOrEqualTo(String value) {
            addCriterion("gender >=", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderLessThan(String value) {
            addCriterion("gender <", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderLessThanOrEqualTo(String value) {
            addCriterion("gender <=", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderLike(String value) {
            addCriterion("gender like", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotLike(String value) {
            addCriterion("gender not like", value, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderIn(List<String> values) {
            addCriterion("gender in", values, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotIn(List<String> values) {
            addCriterion("gender not in", values, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderBetween(String value1, String value2) {
            addCriterion("gender between", value1, value2, "gender");
            return (Criteria) this;
        }

        public Criteria andGenderNotBetween(String value1, String value2) {
            addCriterion("gender not between", value1, value2, "gender");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtIsNull() {
            addCriterion("cur_arear_amt is null");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtIsNotNull() {
            addCriterion("cur_arear_amt is not null");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtEqualTo(BigDecimal value) {
            addCriterion("cur_arear_amt =", value, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtNotEqualTo(BigDecimal value) {
            addCriterion("cur_arear_amt <>", value, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtGreaterThan(BigDecimal value) {
            addCriterion("cur_arear_amt >", value, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("cur_arear_amt >=", value, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtLessThan(BigDecimal value) {
            addCriterion("cur_arear_amt <", value, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtLessThanOrEqualTo(BigDecimal value) {
            addCriterion("cur_arear_amt <=", value, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtIn(List<BigDecimal> values) {
            addCriterion("cur_arear_amt in", values, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtNotIn(List<BigDecimal> values) {
            addCriterion("cur_arear_amt not in", values, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cur_arear_amt between", value1, value2, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andCurArearAmtNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("cur_arear_amt not between", value1, value2, "curArearAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtIsNull() {
            addCriterion("billing_no_repay_amt is null");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtIsNotNull() {
            addCriterion("billing_no_repay_amt is not null");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtEqualTo(BigDecimal value) {
            addCriterion("billing_no_repay_amt =", value, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtNotEqualTo(BigDecimal value) {
            addCriterion("billing_no_repay_amt <>", value, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtGreaterThan(BigDecimal value) {
            addCriterion("billing_no_repay_amt >", value, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("billing_no_repay_amt >=", value, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtLessThan(BigDecimal value) {
            addCriterion("billing_no_repay_amt <", value, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtLessThanOrEqualTo(BigDecimal value) {
            addCriterion("billing_no_repay_amt <=", value, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtIn(List<BigDecimal> values) {
            addCriterion("billing_no_repay_amt in", values, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtNotIn(List<BigDecimal> values) {
            addCriterion("billing_no_repay_amt not in", values, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("billing_no_repay_amt between", value1, value2, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andBillingNoRepayAmtNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("billing_no_repay_amt not between", value1, value2, "billingNoRepayAmt");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayIsNull() {
            addCriterion("stm_mindue_no_repay is null");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayIsNotNull() {
            addCriterion("stm_mindue_no_repay is not null");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayEqualTo(BigDecimal value) {
            addCriterion("stm_mindue_no_repay =", value, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayNotEqualTo(BigDecimal value) {
            addCriterion("stm_mindue_no_repay <>", value, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayGreaterThan(BigDecimal value) {
            addCriterion("stm_mindue_no_repay >", value, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("stm_mindue_no_repay >=", value, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayLessThan(BigDecimal value) {
            addCriterion("stm_mindue_no_repay <", value, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayLessThanOrEqualTo(BigDecimal value) {
            addCriterion("stm_mindue_no_repay <=", value, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayIn(List<BigDecimal> values) {
            addCriterion("stm_mindue_no_repay in", values, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayNotIn(List<BigDecimal> values) {
            addCriterion("stm_mindue_no_repay not in", values, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("stm_mindue_no_repay between", value1, value2, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andStmMindueNoRepayNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("stm_mindue_no_repay not between", value1, value2, "stmMindueNoRepay");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtIsNull() {
            addCriterion("over_due_amt is null");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtIsNotNull() {
            addCriterion("over_due_amt is not null");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtEqualTo(BigDecimal value) {
            addCriterion("over_due_amt =", value, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtNotEqualTo(BigDecimal value) {
            addCriterion("over_due_amt <>", value, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtGreaterThan(BigDecimal value) {
            addCriterion("over_due_amt >", value, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("over_due_amt >=", value, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtLessThan(BigDecimal value) {
            addCriterion("over_due_amt <", value, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtLessThanOrEqualTo(BigDecimal value) {
            addCriterion("over_due_amt <=", value, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtIn(List<BigDecimal> values) {
            addCriterion("over_due_amt in", values, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtNotIn(List<BigDecimal> values) {
            addCriterion("over_due_amt not in", values, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("over_due_amt between", value1, value2, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andOverDueAmtNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("over_due_amt not between", value1, value2, "overDueAmt");
            return (Criteria) this;
        }

        public Criteria andMoPhoneIsNull() {
            addCriterion("mo_phone is null");
            return (Criteria) this;
        }

        public Criteria andMoPhoneIsNotNull() {
            addCriterion("mo_phone is not null");
            return (Criteria) this;
        }

        public Criteria andMoPhoneEqualTo(String value) {
            addCriterion("mo_phone =", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneNotEqualTo(String value) {
            addCriterion("mo_phone <>", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneGreaterThan(String value) {
            addCriterion("mo_phone >", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneGreaterThanOrEqualTo(String value) {
            addCriterion("mo_phone >=", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneLessThan(String value) {
            addCriterion("mo_phone <", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneLessThanOrEqualTo(String value) {
            addCriterion("mo_phone <=", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneLike(String value) {
            addCriterion("mo_phone like", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneNotLike(String value) {
            addCriterion("mo_phone not like", value, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneIn(List<String> values) {
            addCriterion("mo_phone in", values, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneNotIn(List<String> values) {
            addCriterion("mo_phone not in", values, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneBetween(String value1, String value2) {
            addCriterion("mo_phone between", value1, value2, "moPhone");
            return (Criteria) this;
        }

        public Criteria andMoPhoneNotBetween(String value1, String value2) {
            addCriterion("mo_phone not between", value1, value2, "moPhone");
            return (Criteria) this;
        }

        public Criteria andCardNbrIsNull() {
            addCriterion("card_nbr is null");
            return (Criteria) this;
        }

        public Criteria andCardNbrIsNotNull() {
            addCriterion("card_nbr is not null");
            return (Criteria) this;
        }

        public Criteria andCardNbrEqualTo(String value) {
            addCriterion("card_nbr =", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrNotEqualTo(String value) {
            addCriterion("card_nbr <>", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrGreaterThan(String value) {
            addCriterion("card_nbr >", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrGreaterThanOrEqualTo(String value) {
            addCriterion("card_nbr >=", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrLessThan(String value) {
            addCriterion("card_nbr <", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrLessThanOrEqualTo(String value) {
            addCriterion("card_nbr <=", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrLike(String value) {
            addCriterion("card_nbr like", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrNotLike(String value) {
            addCriterion("card_nbr not like", value, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrIn(List<String> values) {
            addCriterion("card_nbr in", values, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrNotIn(List<String> values) {
            addCriterion("card_nbr not in", values, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrBetween(String value1, String value2) {
            addCriterion("card_nbr between", value1, value2, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andCardNbrNotBetween(String value1, String value2) {
            addCriterion("card_nbr not between", value1, value2, "cardNbr");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateIsNull() {
            addCriterion("pmt_due_date is null");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateIsNotNull() {
            addCriterion("pmt_due_date is not null");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateEqualTo(String value) {
            addCriterion("pmt_due_date =", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateNotEqualTo(String value) {
            addCriterion("pmt_due_date <>", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateGreaterThan(String value) {
            addCriterion("pmt_due_date >", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateGreaterThanOrEqualTo(String value) {
            addCriterion("pmt_due_date >=", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateLessThan(String value) {
            addCriterion("pmt_due_date <", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateLessThanOrEqualTo(String value) {
            addCriterion("pmt_due_date <=", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateLike(String value) {
            addCriterion("pmt_due_date like", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateNotLike(String value) {
            addCriterion("pmt_due_date not like", value, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateIn(List<String> values) {
            addCriterion("pmt_due_date in", values, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateNotIn(List<String> values) {
            addCriterion("pmt_due_date not in", values, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateBetween(String value1, String value2) {
            addCriterion("pmt_due_date between", value1, value2, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andPmtDueDateNotBetween(String value1, String value2) {
            addCriterion("pmt_due_date not between", value1, value2, "pmtDueDate");
            return (Criteria) this;
        }

        public Criteria andCycleNbrIsNull() {
            addCriterion("cycle_nbr is null");
            return (Criteria) this;
        }

        public Criteria andCycleNbrIsNotNull() {
            addCriterion("cycle_nbr is not null");
            return (Criteria) this;
        }

        public Criteria andCycleNbrEqualTo(String value) {
            addCriterion("cycle_nbr =", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrNotEqualTo(String value) {
            addCriterion("cycle_nbr <>", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrGreaterThan(String value) {
            addCriterion("cycle_nbr >", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrGreaterThanOrEqualTo(String value) {
            addCriterion("cycle_nbr >=", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrLessThan(String value) {
            addCriterion("cycle_nbr <", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrLessThanOrEqualTo(String value) {
            addCriterion("cycle_nbr <=", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrLike(String value) {
            addCriterion("cycle_nbr like", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrNotLike(String value) {
            addCriterion("cycle_nbr not like", value, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrIn(List<String> values) {
            addCriterion("cycle_nbr in", values, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrNotIn(List<String> values) {
            addCriterion("cycle_nbr not in", values, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrBetween(String value1, String value2) {
            addCriterion("cycle_nbr between", value1, value2, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andCycleNbrNotBetween(String value1, String value2) {
            addCriterion("cycle_nbr not between", value1, value2, "cycleNbr");
            return (Criteria) this;
        }

        public Criteria andMthsOdueIsNull() {
            addCriterion("mths_odue is null");
            return (Criteria) this;
        }

        public Criteria andMthsOdueIsNotNull() {
            addCriterion("mths_odue is not null");
            return (Criteria) this;
        }

        public Criteria andMthsOdueEqualTo(Integer value) {
            addCriterion("mths_odue =", value, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueNotEqualTo(Integer value) {
            addCriterion("mths_odue <>", value, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueGreaterThan(Integer value) {
            addCriterion("mths_odue >", value, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueGreaterThanOrEqualTo(Integer value) {
            addCriterion("mths_odue >=", value, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueLessThan(Integer value) {
            addCriterion("mths_odue <", value, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueLessThanOrEqualTo(Integer value) {
            addCriterion("mths_odue <=", value, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueIn(List<Integer> values) {
            addCriterion("mths_odue in", values, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueNotIn(List<Integer> values) {
            addCriterion("mths_odue not in", values, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueBetween(Integer value1, Integer value2) {
            addCriterion("mths_odue between", value1, value2, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andMthsOdueNotBetween(Integer value1, Integer value2) {
            addCriterion("mths_odue not between", value1, value2, "mthsOdue");
            return (Criteria) this;
        }

        public Criteria andOverDueDayIsNull() {
            addCriterion("over_due_day is null");
            return (Criteria) this;
        }

        public Criteria andOverDueDayIsNotNull() {
            addCriterion("over_due_day is not null");
            return (Criteria) this;
        }

        public Criteria andOverDueDayEqualTo(Integer value) {
            addCriterion("over_due_day =", value, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayNotEqualTo(Integer value) {
            addCriterion("over_due_day <>", value, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayGreaterThan(Integer value) {
            addCriterion("over_due_day >", value, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayGreaterThanOrEqualTo(Integer value) {
            addCriterion("over_due_day >=", value, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayLessThan(Integer value) {
            addCriterion("over_due_day <", value, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayLessThanOrEqualTo(Integer value) {
            addCriterion("over_due_day <=", value, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayIn(List<Integer> values) {
            addCriterion("over_due_day in", values, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayNotIn(List<Integer> values) {
            addCriterion("over_due_day not in", values, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayBetween(Integer value1, Integer value2) {
            addCriterion("over_due_day between", value1, value2, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andOverDueDayNotBetween(Integer value1, Integer value2) {
            addCriterion("over_due_day not between", value1, value2, "overDueDay");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysIsNull() {
            addCriterion("ai_called_days is null");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysIsNotNull() {
            addCriterion("ai_called_days is not null");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysEqualTo(Integer value) {
            addCriterion("ai_called_days =", value, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysNotEqualTo(Integer value) {
            addCriterion("ai_called_days <>", value, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysGreaterThan(Integer value) {
            addCriterion("ai_called_days >", value, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysGreaterThanOrEqualTo(Integer value) {
            addCriterion("ai_called_days >=", value, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysLessThan(Integer value) {
            addCriterion("ai_called_days <", value, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysLessThanOrEqualTo(Integer value) {
            addCriterion("ai_called_days <=", value, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysIn(List<Integer> values) {
            addCriterion("ai_called_days in", values, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysNotIn(List<Integer> values) {
            addCriterion("ai_called_days not in", values, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysBetween(Integer value1, Integer value2) {
            addCriterion("ai_called_days between", value1, value2, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCalledDaysNotBetween(Integer value1, Integer value2) {
            addCriterion("ai_called_days not between", value1, value2, "aiCalledDays");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesIsNull() {
            addCriterion("ai_call_conn_times is null");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesIsNotNull() {
            addCriterion("ai_call_conn_times is not null");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesEqualTo(Integer value) {
            addCriterion("ai_call_conn_times =", value, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesNotEqualTo(Integer value) {
            addCriterion("ai_call_conn_times <>", value, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesGreaterThan(Integer value) {
            addCriterion("ai_call_conn_times >", value, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesGreaterThanOrEqualTo(Integer value) {
            addCriterion("ai_call_conn_times >=", value, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesLessThan(Integer value) {
            addCriterion("ai_call_conn_times <", value, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesLessThanOrEqualTo(Integer value) {
            addCriterion("ai_call_conn_times <=", value, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesIn(List<Integer> values) {
            addCriterion("ai_call_conn_times in", values, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesNotIn(List<Integer> values) {
            addCriterion("ai_call_conn_times not in", values, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesBetween(Integer value1, Integer value2) {
            addCriterion("ai_call_conn_times between", value1, value2, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andAiCallConnTimesNotBetween(Integer value1, Integer value2) {
            addCriterion("ai_call_conn_times not between", value1, value2, "aiCallConnTimes");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelIsNull() {
            addCriterion("user_risk_level is null");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelIsNotNull() {
            addCriterion("user_risk_level is not null");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelEqualTo(String value) {
            addCriterion("user_risk_level =", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelNotEqualTo(String value) {
            addCriterion("user_risk_level <>", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelGreaterThan(String value) {
            addCriterion("user_risk_level >", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelGreaterThanOrEqualTo(String value) {
            addCriterion("user_risk_level >=", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelLessThan(String value) {
            addCriterion("user_risk_level <", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelLessThanOrEqualTo(String value) {
            addCriterion("user_risk_level <=", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelLike(String value) {
            addCriterion("user_risk_level like", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelNotLike(String value) {
            addCriterion("user_risk_level not like", value, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelIn(List<String> values) {
            addCriterion("user_risk_level in", values, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelNotIn(List<String> values) {
            addCriterion("user_risk_level not in", values, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelBetween(String value1, String value2) {
            addCriterion("user_risk_level between", value1, value2, "userRiskLevel");
            return (Criteria) this;
        }

        public Criteria andUserRiskLevelNotBetween(String value1, String value2) {
            addCriterion("user_risk_level not between", value1, value2, "userRiskLevel");
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

        public Criteria andStrategyCodeIsNull() {
            addCriterion("strategy_code is null");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeIsNotNull() {
            addCriterion("strategy_code is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeEqualTo(String value) {
            addCriterion("strategy_code =", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeNotEqualTo(String value) {
            addCriterion("strategy_code <>", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeGreaterThan(String value) {
            addCriterion("strategy_code >", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_code >=", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeLessThan(String value) {
            addCriterion("strategy_code <", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeLessThanOrEqualTo(String value) {
            addCriterion("strategy_code <=", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeLike(String value) {
            addCriterion("strategy_code like", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeNotLike(String value) {
            addCriterion("strategy_code not like", value, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeIn(List<String> values) {
            addCriterion("strategy_code in", values, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeNotIn(List<String> values) {
            addCriterion("strategy_code not in", values, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeBetween(String value1, String value2) {
            addCriterion("strategy_code between", value1, value2, "strategyCode");
            return (Criteria) this;
        }

        public Criteria andStrategyCodeNotBetween(String value1, String value2) {
            addCriterion("strategy_code not between", value1, value2, "strategyCode");
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