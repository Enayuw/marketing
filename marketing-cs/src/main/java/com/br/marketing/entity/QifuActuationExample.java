package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QifuActuationExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public QifuActuationExample() {
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

        public Criteria andIssueMonthIsNull() {
            addCriterion("issue_month is null");
            return (Criteria) this;
        }

        public Criteria andIssueMonthIsNotNull() {
            addCriterion("issue_month is not null");
            return (Criteria) this;
        }

        public Criteria andIssueMonthEqualTo(String value) {
            addCriterion("issue_month =", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthNotEqualTo(String value) {
            addCriterion("issue_month <>", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthGreaterThan(String value) {
            addCriterion("issue_month >", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthGreaterThanOrEqualTo(String value) {
            addCriterion("issue_month >=", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthLessThan(String value) {
            addCriterion("issue_month <", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthLessThanOrEqualTo(String value) {
            addCriterion("issue_month <=", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthLike(String value) {
            addCriterion("issue_month like", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthNotLike(String value) {
            addCriterion("issue_month not like", value, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthIn(List<String> values) {
            addCriterion("issue_month in", values, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthNotIn(List<String> values) {
            addCriterion("issue_month not in", values, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthBetween(String value1, String value2) {
            addCriterion("issue_month between", value1, value2, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueMonthNotBetween(String value1, String value2) {
            addCriterion("issue_month not between", value1, value2, "issueMonth");
            return (Criteria) this;
        }

        public Criteria andIssueDateIsNull() {
            addCriterion("issue_date is null");
            return (Criteria) this;
        }

        public Criteria andIssueDateIsNotNull() {
            addCriterion("issue_date is not null");
            return (Criteria) this;
        }

        public Criteria andIssueDateEqualTo(String value) {
            addCriterion("issue_date =", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateNotEqualTo(String value) {
            addCriterion("issue_date <>", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateGreaterThan(String value) {
            addCriterion("issue_date >", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateGreaterThanOrEqualTo(String value) {
            addCriterion("issue_date >=", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateLessThan(String value) {
            addCriterion("issue_date <", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateLessThanOrEqualTo(String value) {
            addCriterion("issue_date <=", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateLike(String value) {
            addCriterion("issue_date like", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateNotLike(String value) {
            addCriterion("issue_date not like", value, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateIn(List<String> values) {
            addCriterion("issue_date in", values, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateNotIn(List<String> values) {
            addCriterion("issue_date not in", values, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateBetween(String value1, String value2) {
            addCriterion("issue_date between", value1, value2, "issueDate");
            return (Criteria) this;
        }

        public Criteria andIssueDateNotBetween(String value1, String value2) {
            addCriterion("issue_date not between", value1, value2, "issueDate");
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

        public Criteria andGroupTypeNameIsNull() {
            addCriterion("group_type_name is null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameIsNotNull() {
            addCriterion("group_type_name is not null");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameEqualTo(String value) {
            addCriterion("group_type_name =", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameNotEqualTo(String value) {
            addCriterion("group_type_name <>", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameGreaterThan(String value) {
            addCriterion("group_type_name >", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameGreaterThanOrEqualTo(String value) {
            addCriterion("group_type_name >=", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameLessThan(String value) {
            addCriterion("group_type_name <", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameLessThanOrEqualTo(String value) {
            addCriterion("group_type_name <=", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameLike(String value) {
            addCriterion("group_type_name like", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameNotLike(String value) {
            addCriterion("group_type_name not like", value, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameIn(List<String> values) {
            addCriterion("group_type_name in", values, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameNotIn(List<String> values) {
            addCriterion("group_type_name not in", values, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameBetween(String value1, String value2) {
            addCriterion("group_type_name between", value1, value2, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andGroupTypeNameNotBetween(String value1, String value2) {
            addCriterion("group_type_name not between", value1, value2, "groupTypeName");
            return (Criteria) this;
        }

        public Criteria andValidDateIsNull() {
            addCriterion("valid_date is null");
            return (Criteria) this;
        }

        public Criteria andValidDateIsNotNull() {
            addCriterion("valid_date is not null");
            return (Criteria) this;
        }

        public Criteria andValidDateEqualTo(String value) {
            addCriterion("valid_date =", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateNotEqualTo(String value) {
            addCriterion("valid_date <>", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateGreaterThan(String value) {
            addCriterion("valid_date >", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateGreaterThanOrEqualTo(String value) {
            addCriterion("valid_date >=", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateLessThan(String value) {
            addCriterion("valid_date <", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateLessThanOrEqualTo(String value) {
            addCriterion("valid_date <=", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateLike(String value) {
            addCriterion("valid_date like", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateNotLike(String value) {
            addCriterion("valid_date not like", value, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateIn(List<String> values) {
            addCriterion("valid_date in", values, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateNotIn(List<String> values) {
            addCriterion("valid_date not in", values, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateBetween(String value1, String value2) {
            addCriterion("valid_date between", value1, value2, "validDate");
            return (Criteria) this;
        }

        public Criteria andValidDateNotBetween(String value1, String value2) {
            addCriterion("valid_date not between", value1, value2, "validDate");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountIsNull() {
            addCriterion("credit_user_count is null");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountIsNotNull() {
            addCriterion("credit_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountEqualTo(Integer value) {
            addCriterion("credit_user_count =", value, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountNotEqualTo(Integer value) {
            addCriterion("credit_user_count <>", value, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountGreaterThan(Integer value) {
            addCriterion("credit_user_count >", value, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("credit_user_count >=", value, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountLessThan(Integer value) {
            addCriterion("credit_user_count <", value, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("credit_user_count <=", value, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountIn(List<Integer> values) {
            addCriterion("credit_user_count in", values, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountNotIn(List<Integer> values) {
            addCriterion("credit_user_count not in", values, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountBetween(Integer value1, Integer value2) {
            addCriterion("credit_user_count between", value1, value2, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andCreditUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("credit_user_count not between", value1, value2, "creditUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountIsNull() {
            addCriterion("app_login_user_count is null");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountIsNotNull() {
            addCriterion("app_login_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountEqualTo(Integer value) {
            addCriterion("app_login_user_count =", value, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountNotEqualTo(Integer value) {
            addCriterion("app_login_user_count <>", value, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountGreaterThan(Integer value) {
            addCriterion("app_login_user_count >", value, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("app_login_user_count >=", value, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountLessThan(Integer value) {
            addCriterion("app_login_user_count <", value, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("app_login_user_count <=", value, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountIn(List<Integer> values) {
            addCriterion("app_login_user_count in", values, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountNotIn(List<Integer> values) {
            addCriterion("app_login_user_count not in", values, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountBetween(Integer value1, Integer value2) {
            addCriterion("app_login_user_count between", value1, value2, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("app_login_user_count not between", value1, value2, "appLoginUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountIsNull() {
            addCriterion("start_user_count is null");
            return (Criteria) this;
        }

        public Criteria andStartUserCountIsNotNull() {
            addCriterion("start_user_count is not null");
            return (Criteria) this;
        }

        public Criteria andStartUserCountEqualTo(Integer value) {
            addCriterion("start_user_count =", value, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountNotEqualTo(Integer value) {
            addCriterion("start_user_count <>", value, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountGreaterThan(Integer value) {
            addCriterion("start_user_count >", value, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("start_user_count >=", value, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountLessThan(Integer value) {
            addCriterion("start_user_count <", value, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountLessThanOrEqualTo(Integer value) {
            addCriterion("start_user_count <=", value, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountIn(List<Integer> values) {
            addCriterion("start_user_count in", values, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountNotIn(List<Integer> values) {
            addCriterion("start_user_count not in", values, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountBetween(Integer value1, Integer value2) {
            addCriterion("start_user_count between", value1, value2, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andStartUserCountNotBetween(Integer value1, Integer value2) {
            addCriterion("start_user_count not between", value1, value2, "startUserCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountIsNull() {
            addCriterion("user_loan_count is null");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountIsNotNull() {
            addCriterion("user_loan_count is not null");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountEqualTo(Integer value) {
            addCriterion("user_loan_count =", value, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountNotEqualTo(Integer value) {
            addCriterion("user_loan_count <>", value, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountGreaterThan(Integer value) {
            addCriterion("user_loan_count >", value, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("user_loan_count >=", value, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountLessThan(Integer value) {
            addCriterion("user_loan_count <", value, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountLessThanOrEqualTo(Integer value) {
            addCriterion("user_loan_count <=", value, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountIn(List<Integer> values) {
            addCriterion("user_loan_count in", values, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountNotIn(List<Integer> values) {
            addCriterion("user_loan_count not in", values, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountBetween(Integer value1, Integer value2) {
            addCriterion("user_loan_count between", value1, value2, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andUserLoanCountNotBetween(Integer value1, Integer value2) {
            addCriterion("user_loan_count not between", value1, value2, "userLoanCount");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateIsNull() {
            addCriterion("app_login_rate is null");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateIsNotNull() {
            addCriterion("app_login_rate is not null");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateEqualTo(String value) {
            addCriterion("app_login_rate =", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateNotEqualTo(String value) {
            addCriterion("app_login_rate <>", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateGreaterThan(String value) {
            addCriterion("app_login_rate >", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateGreaterThanOrEqualTo(String value) {
            addCriterion("app_login_rate >=", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateLessThan(String value) {
            addCriterion("app_login_rate <", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateLessThanOrEqualTo(String value) {
            addCriterion("app_login_rate <=", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateLike(String value) {
            addCriterion("app_login_rate like", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateNotLike(String value) {
            addCriterion("app_login_rate not like", value, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateIn(List<String> values) {
            addCriterion("app_login_rate in", values, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateNotIn(List<String> values) {
            addCriterion("app_login_rate not in", values, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateBetween(String value1, String value2) {
            addCriterion("app_login_rate between", value1, value2, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andAppLoginRateNotBetween(String value1, String value2) {
            addCriterion("app_login_rate not between", value1, value2, "appLoginRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateIsNull() {
            addCriterion("user_start_rate is null");
            return (Criteria) this;
        }

        public Criteria andUserStartRateIsNotNull() {
            addCriterion("user_start_rate is not null");
            return (Criteria) this;
        }

        public Criteria andUserStartRateEqualTo(String value) {
            addCriterion("user_start_rate =", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateNotEqualTo(String value) {
            addCriterion("user_start_rate <>", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateGreaterThan(String value) {
            addCriterion("user_start_rate >", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateGreaterThanOrEqualTo(String value) {
            addCriterion("user_start_rate >=", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateLessThan(String value) {
            addCriterion("user_start_rate <", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateLessThanOrEqualTo(String value) {
            addCriterion("user_start_rate <=", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateLike(String value) {
            addCriterion("user_start_rate like", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateNotLike(String value) {
            addCriterion("user_start_rate not like", value, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateIn(List<String> values) {
            addCriterion("user_start_rate in", values, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateNotIn(List<String> values) {
            addCriterion("user_start_rate not in", values, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateBetween(String value1, String value2) {
            addCriterion("user_start_rate between", value1, value2, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserStartRateNotBetween(String value1, String value2) {
            addCriterion("user_start_rate not between", value1, value2, "userStartRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateIsNull() {
            addCriterion("user_loan_rate is null");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateIsNotNull() {
            addCriterion("user_loan_rate is not null");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateEqualTo(String value) {
            addCriterion("user_loan_rate =", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateNotEqualTo(String value) {
            addCriterion("user_loan_rate <>", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateGreaterThan(String value) {
            addCriterion("user_loan_rate >", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateGreaterThanOrEqualTo(String value) {
            addCriterion("user_loan_rate >=", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateLessThan(String value) {
            addCriterion("user_loan_rate <", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateLessThanOrEqualTo(String value) {
            addCriterion("user_loan_rate <=", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateLike(String value) {
            addCriterion("user_loan_rate like", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateNotLike(String value) {
            addCriterion("user_loan_rate not like", value, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateIn(List<String> values) {
            addCriterion("user_loan_rate in", values, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateNotIn(List<String> values) {
            addCriterion("user_loan_rate not in", values, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateBetween(String value1, String value2) {
            addCriterion("user_loan_rate between", value1, value2, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andUserLoanRateNotBetween(String value1, String value2) {
            addCriterion("user_loan_rate not between", value1, value2, "userLoanRate");
            return (Criteria) this;
        }

        public Criteria andCreateDateIsNull() {
            addCriterion("create_date is null");
            return (Criteria) this;
        }

        public Criteria andCreateDateIsNotNull() {
            addCriterion("create_date is not null");
            return (Criteria) this;
        }

        public Criteria andCreateDateEqualTo(String value) {
            addCriterion("create_date =", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotEqualTo(String value) {
            addCriterion("create_date <>", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThan(String value) {
            addCriterion("create_date >", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThanOrEqualTo(String value) {
            addCriterion("create_date >=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThan(String value) {
            addCriterion("create_date <", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThanOrEqualTo(String value) {
            addCriterion("create_date <=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLike(String value) {
            addCriterion("create_date like", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotLike(String value) {
            addCriterion("create_date not like", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateIn(List<String> values) {
            addCriterion("create_date in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotIn(List<String> values) {
            addCriterion("create_date not in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateBetween(String value1, String value2) {
            addCriterion("create_date between", value1, value2, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotBetween(String value1, String value2) {
            addCriterion("create_date not between", value1, value2, "createDate");
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