package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ShuHeCuFuJieDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ShuHeCuFuJieDataExample() {
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

        public Criteria andNameSha256IsNull() {
            addCriterion("name_sha256 is null");
            return (Criteria) this;
        }

        public Criteria andNameSha256IsNotNull() {
            addCriterion("name_sha256 is not null");
            return (Criteria) this;
        }

        public Criteria andNameSha256EqualTo(String value) {
            addCriterion("name_sha256 =", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256NotEqualTo(String value) {
            addCriterion("name_sha256 <>", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256GreaterThan(String value) {
            addCriterion("name_sha256 >", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256GreaterThanOrEqualTo(String value) {
            addCriterion("name_sha256 >=", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256LessThan(String value) {
            addCriterion("name_sha256 <", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256LessThanOrEqualTo(String value) {
            addCriterion("name_sha256 <=", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256Like(String value) {
            addCriterion("name_sha256 like", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256NotLike(String value) {
            addCriterion("name_sha256 not like", value, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256In(List<String> values) {
            addCriterion("name_sha256 in", values, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256NotIn(List<String> values) {
            addCriterion("name_sha256 not in", values, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256Between(String value1, String value2) {
            addCriterion("name_sha256 between", value1, value2, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andNameSha256NotBetween(String value1, String value2) {
            addCriterion("name_sha256 not between", value1, value2, "nameSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256IsNull() {
            addCriterion("mobile_sha256 is null");
            return (Criteria) this;
        }

        public Criteria andMobileSha256IsNotNull() {
            addCriterion("mobile_sha256 is not null");
            return (Criteria) this;
        }

        public Criteria andMobileSha256EqualTo(String value) {
            addCriterion("mobile_sha256 =", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256NotEqualTo(String value) {
            addCriterion("mobile_sha256 <>", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256GreaterThan(String value) {
            addCriterion("mobile_sha256 >", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256GreaterThanOrEqualTo(String value) {
            addCriterion("mobile_sha256 >=", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256LessThan(String value) {
            addCriterion("mobile_sha256 <", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256LessThanOrEqualTo(String value) {
            addCriterion("mobile_sha256 <=", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256Like(String value) {
            addCriterion("mobile_sha256 like", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256NotLike(String value) {
            addCriterion("mobile_sha256 not like", value, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256In(List<String> values) {
            addCriterion("mobile_sha256 in", values, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256NotIn(List<String> values) {
            addCriterion("mobile_sha256 not in", values, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256Between(String value1, String value2) {
            addCriterion("mobile_sha256 between", value1, value2, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andMobileSha256NotBetween(String value1, String value2) {
            addCriterion("mobile_sha256 not between", value1, value2, "mobileSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256IsNull() {
            addCriterion("identification_no_sha256 is null");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256IsNotNull() {
            addCriterion("identification_no_sha256 is not null");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256EqualTo(String value) {
            addCriterion("identification_no_sha256 =", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256NotEqualTo(String value) {
            addCriterion("identification_no_sha256 <>", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256GreaterThan(String value) {
            addCriterion("identification_no_sha256 >", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256GreaterThanOrEqualTo(String value) {
            addCriterion("identification_no_sha256 >=", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256LessThan(String value) {
            addCriterion("identification_no_sha256 <", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256LessThanOrEqualTo(String value) {
            addCriterion("identification_no_sha256 <=", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256Like(String value) {
            addCriterion("identification_no_sha256 like", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256NotLike(String value) {
            addCriterion("identification_no_sha256 not like", value, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256In(List<String> values) {
            addCriterion("identification_no_sha256 in", values, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256NotIn(List<String> values) {
            addCriterion("identification_no_sha256 not in", values, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256Between(String value1, String value2) {
            addCriterion("identification_no_sha256 between", value1, value2, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andIdentificationNoSha256NotBetween(String value1, String value2) {
            addCriterion("identification_no_sha256 not between", value1, value2, "identificationNoSha256");
            return (Criteria) this;
        }

        public Criteria andAdtLmtIsNull() {
            addCriterion("adt_lmt is null");
            return (Criteria) this;
        }

        public Criteria andAdtLmtIsNotNull() {
            addCriterion("adt_lmt is not null");
            return (Criteria) this;
        }

        public Criteria andAdtLmtEqualTo(String value) {
            addCriterion("adt_lmt =", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtNotEqualTo(String value) {
            addCriterion("adt_lmt <>", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtGreaterThan(String value) {
            addCriterion("adt_lmt >", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtGreaterThanOrEqualTo(String value) {
            addCriterion("adt_lmt >=", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtLessThan(String value) {
            addCriterion("adt_lmt <", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtLessThanOrEqualTo(String value) {
            addCriterion("adt_lmt <=", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtLike(String value) {
            addCriterion("adt_lmt like", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtNotLike(String value) {
            addCriterion("adt_lmt not like", value, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtIn(List<String> values) {
            addCriterion("adt_lmt in", values, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtNotIn(List<String> values) {
            addCriterion("adt_lmt not in", values, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtBetween(String value1, String value2) {
            addCriterion("adt_lmt between", value1, value2, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAdtLmtNotBetween(String value1, String value2) {
            addCriterion("adt_lmt not between", value1, value2, "adtLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtIsNull() {
            addCriterion("avl_lmt is null");
            return (Criteria) this;
        }

        public Criteria andAvlLmtIsNotNull() {
            addCriterion("avl_lmt is not null");
            return (Criteria) this;
        }

        public Criteria andAvlLmtEqualTo(String value) {
            addCriterion("avl_lmt =", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtNotEqualTo(String value) {
            addCriterion("avl_lmt <>", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtGreaterThan(String value) {
            addCriterion("avl_lmt >", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtGreaterThanOrEqualTo(String value) {
            addCriterion("avl_lmt >=", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtLessThan(String value) {
            addCriterion("avl_lmt <", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtLessThanOrEqualTo(String value) {
            addCriterion("avl_lmt <=", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtLike(String value) {
            addCriterion("avl_lmt like", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtNotLike(String value) {
            addCriterion("avl_lmt not like", value, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtIn(List<String> values) {
            addCriterion("avl_lmt in", values, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtNotIn(List<String> values) {
            addCriterion("avl_lmt not in", values, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtBetween(String value1, String value2) {
            addCriterion("avl_lmt between", value1, value2, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andAvlLmtNotBetween(String value1, String value2) {
            addCriterion("avl_lmt not between", value1, value2, "avlLmt");
            return (Criteria) this;
        }

        public Criteria andOrderStartIsNull() {
            addCriterion("order_start is null");
            return (Criteria) this;
        }

        public Criteria andOrderStartIsNotNull() {
            addCriterion("order_start is not null");
            return (Criteria) this;
        }

        public Criteria andOrderStartEqualTo(String value) {
            addCriterion("order_start =", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartNotEqualTo(String value) {
            addCriterion("order_start <>", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartGreaterThan(String value) {
            addCriterion("order_start >", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartGreaterThanOrEqualTo(String value) {
            addCriterion("order_start >=", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartLessThan(String value) {
            addCriterion("order_start <", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartLessThanOrEqualTo(String value) {
            addCriterion("order_start <=", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartLike(String value) {
            addCriterion("order_start like", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartNotLike(String value) {
            addCriterion("order_start not like", value, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartIn(List<String> values) {
            addCriterion("order_start in", values, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartNotIn(List<String> values) {
            addCriterion("order_start not in", values, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartBetween(String value1, String value2) {
            addCriterion("order_start between", value1, value2, "orderStart");
            return (Criteria) this;
        }

        public Criteria andOrderStartNotBetween(String value1, String value2) {
            addCriterion("order_start not between", value1, value2, "orderStart");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntIsNull() {
            addCriterion("risk_pass_cnt is null");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntIsNotNull() {
            addCriterion("risk_pass_cnt is not null");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntEqualTo(String value) {
            addCriterion("risk_pass_cnt =", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntNotEqualTo(String value) {
            addCriterion("risk_pass_cnt <>", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntGreaterThan(String value) {
            addCriterion("risk_pass_cnt >", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntGreaterThanOrEqualTo(String value) {
            addCriterion("risk_pass_cnt >=", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntLessThan(String value) {
            addCriterion("risk_pass_cnt <", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntLessThanOrEqualTo(String value) {
            addCriterion("risk_pass_cnt <=", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntLike(String value) {
            addCriterion("risk_pass_cnt like", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntNotLike(String value) {
            addCriterion("risk_pass_cnt not like", value, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntIn(List<String> values) {
            addCriterion("risk_pass_cnt in", values, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntNotIn(List<String> values) {
            addCriterion("risk_pass_cnt not in", values, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntBetween(String value1, String value2) {
            addCriterion("risk_pass_cnt between", value1, value2, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andRiskPassCntNotBetween(String value1, String value2) {
            addCriterion("risk_pass_cnt not between", value1, value2, "riskPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntIsNull() {
            addCriterion("fund_pass_cnt is null");
            return (Criteria) this;
        }

        public Criteria andFundPassCntIsNotNull() {
            addCriterion("fund_pass_cnt is not null");
            return (Criteria) this;
        }

        public Criteria andFundPassCntEqualTo(String value) {
            addCriterion("fund_pass_cnt =", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntNotEqualTo(String value) {
            addCriterion("fund_pass_cnt <>", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntGreaterThan(String value) {
            addCriterion("fund_pass_cnt >", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntGreaterThanOrEqualTo(String value) {
            addCriterion("fund_pass_cnt >=", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntLessThan(String value) {
            addCriterion("fund_pass_cnt <", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntLessThanOrEqualTo(String value) {
            addCriterion("fund_pass_cnt <=", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntLike(String value) {
            addCriterion("fund_pass_cnt like", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntNotLike(String value) {
            addCriterion("fund_pass_cnt not like", value, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntIn(List<String> values) {
            addCriterion("fund_pass_cnt in", values, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntNotIn(List<String> values) {
            addCriterion("fund_pass_cnt not in", values, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntBetween(String value1, String value2) {
            addCriterion("fund_pass_cnt between", value1, value2, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andFundPassCntNotBetween(String value1, String value2) {
            addCriterion("fund_pass_cnt not between", value1, value2, "fundPassCnt");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountIsNull() {
            addCriterion("loan_principal_amount is null");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountIsNotNull() {
            addCriterion("loan_principal_amount is not null");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountEqualTo(String value) {
            addCriterion("loan_principal_amount =", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountNotEqualTo(String value) {
            addCriterion("loan_principal_amount <>", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountGreaterThan(String value) {
            addCriterion("loan_principal_amount >", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountGreaterThanOrEqualTo(String value) {
            addCriterion("loan_principal_amount >=", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountLessThan(String value) {
            addCriterion("loan_principal_amount <", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountLessThanOrEqualTo(String value) {
            addCriterion("loan_principal_amount <=", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountLike(String value) {
            addCriterion("loan_principal_amount like", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountNotLike(String value) {
            addCriterion("loan_principal_amount not like", value, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountIn(List<String> values) {
            addCriterion("loan_principal_amount in", values, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountNotIn(List<String> values) {
            addCriterion("loan_principal_amount not in", values, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountBetween(String value1, String value2) {
            addCriterion("loan_principal_amount between", value1, value2, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andLoanPrincipalAmountNotBetween(String value1, String value2) {
            addCriterion("loan_principal_amount not between", value1, value2, "loanPrincipalAmount");
            return (Criteria) this;
        }

        public Criteria andDsIsNull() {
            addCriterion("ds is null");
            return (Criteria) this;
        }

        public Criteria andDsIsNotNull() {
            addCriterion("ds is not null");
            return (Criteria) this;
        }

        public Criteria andDsEqualTo(String value) {
            addCriterion("ds =", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsNotEqualTo(String value) {
            addCriterion("ds <>", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsGreaterThan(String value) {
            addCriterion("ds >", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsGreaterThanOrEqualTo(String value) {
            addCriterion("ds >=", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsLessThan(String value) {
            addCriterion("ds <", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsLessThanOrEqualTo(String value) {
            addCriterion("ds <=", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsLike(String value) {
            addCriterion("ds like", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsNotLike(String value) {
            addCriterion("ds not like", value, "ds");
            return (Criteria) this;
        }

        public Criteria andDsIn(List<String> values) {
            addCriterion("ds in", values, "ds");
            return (Criteria) this;
        }

        public Criteria andDsNotIn(List<String> values) {
            addCriterion("ds not in", values, "ds");
            return (Criteria) this;
        }

        public Criteria andDsBetween(String value1, String value2) {
            addCriterion("ds between", value1, value2, "ds");
            return (Criteria) this;
        }

        public Criteria andDsNotBetween(String value1, String value2) {
            addCriterion("ds not between", value1, value2, "ds");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateIsNull() {
            addCriterion("is_non_operate is null");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateIsNotNull() {
            addCriterion("is_non_operate is not null");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateEqualTo(String value) {
            addCriterion("is_non_operate =", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateNotEqualTo(String value) {
            addCriterion("is_non_operate <>", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateGreaterThan(String value) {
            addCriterion("is_non_operate >", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateGreaterThanOrEqualTo(String value) {
            addCriterion("is_non_operate >=", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateLessThan(String value) {
            addCriterion("is_non_operate <", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateLessThanOrEqualTo(String value) {
            addCriterion("is_non_operate <=", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateLike(String value) {
            addCriterion("is_non_operate like", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateNotLike(String value) {
            addCriterion("is_non_operate not like", value, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateIn(List<String> values) {
            addCriterion("is_non_operate in", values, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateNotIn(List<String> values) {
            addCriterion("is_non_operate not in", values, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateBetween(String value1, String value2) {
            addCriterion("is_non_operate between", value1, value2, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andIsNonOperateNotBetween(String value1, String value2) {
            addCriterion("is_non_operate not between", value1, value2, "isNonOperate");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6IsNull() {
            addCriterion("asset_level_6 is null");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6IsNotNull() {
            addCriterion("asset_level_6 is not null");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6EqualTo(String value) {
            addCriterion("asset_level_6 =", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6NotEqualTo(String value) {
            addCriterion("asset_level_6 <>", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6GreaterThan(String value) {
            addCriterion("asset_level_6 >", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6GreaterThanOrEqualTo(String value) {
            addCriterion("asset_level_6 >=", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6LessThan(String value) {
            addCriterion("asset_level_6 <", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6LessThanOrEqualTo(String value) {
            addCriterion("asset_level_6 <=", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6Like(String value) {
            addCriterion("asset_level_6 like", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6NotLike(String value) {
            addCriterion("asset_level_6 not like", value, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6In(List<String> values) {
            addCriterion("asset_level_6 in", values, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6NotIn(List<String> values) {
            addCriterion("asset_level_6 not in", values, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6Between(String value1, String value2) {
            addCriterion("asset_level_6 between", value1, value2, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andAssetLevel6NotBetween(String value1, String value2) {
            addCriterion("asset_level_6 not between", value1, value2, "assetLevel6");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimIsNull() {
            addCriterion("lst_non_dcp_trs_tim is null");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimIsNotNull() {
            addCriterion("lst_non_dcp_trs_tim is not null");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimEqualTo(String value) {
            addCriterion("lst_non_dcp_trs_tim =", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimNotEqualTo(String value) {
            addCriterion("lst_non_dcp_trs_tim <>", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimGreaterThan(String value) {
            addCriterion("lst_non_dcp_trs_tim >", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimGreaterThanOrEqualTo(String value) {
            addCriterion("lst_non_dcp_trs_tim >=", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimLessThan(String value) {
            addCriterion("lst_non_dcp_trs_tim <", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimLessThanOrEqualTo(String value) {
            addCriterion("lst_non_dcp_trs_tim <=", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimLike(String value) {
            addCriterion("lst_non_dcp_trs_tim like", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimNotLike(String value) {
            addCriterion("lst_non_dcp_trs_tim not like", value, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimIn(List<String> values) {
            addCriterion("lst_non_dcp_trs_tim in", values, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimNotIn(List<String> values) {
            addCriterion("lst_non_dcp_trs_tim not in", values, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimBetween(String value1, String value2) {
            addCriterion("lst_non_dcp_trs_tim between", value1, value2, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstNonDcpTrsTimNotBetween(String value1, String value2) {
            addCriterion("lst_non_dcp_trs_tim not between", value1, value2, "lstNonDcpTrsTim");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlIsNull() {
            addCriterion("lst_ord_tim_all_blbtchhl is null");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlIsNotNull() {
            addCriterion("lst_ord_tim_all_blbtchhl is not null");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlEqualTo(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl =", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlNotEqualTo(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl <>", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlGreaterThan(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl >", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlGreaterThanOrEqualTo(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl >=", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlLessThan(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl <", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlLessThanOrEqualTo(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl <=", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlLike(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl like", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlNotLike(String value) {
            addCriterion("lst_ord_tim_all_blbtchhl not like", value, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlIn(List<String> values) {
            addCriterion("lst_ord_tim_all_blbtchhl in", values, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlNotIn(List<String> values) {
            addCriterion("lst_ord_tim_all_blbtchhl not in", values, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlBetween(String value1, String value2) {
            addCriterion("lst_ord_tim_all_blbtchhl between", value1, value2, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstOrdTimAllBlbtchhlNotBetween(String value1, String value2) {
            addCriterion("lst_ord_tim_all_blbtchhl not between", value1, value2, "lstOrdTimAllBlbtchhl");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyIsNull() {
            addCriterion("lst_adt_apy_tim_hvy is null");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyIsNotNull() {
            addCriterion("lst_adt_apy_tim_hvy is not null");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyEqualTo(String value) {
            addCriterion("lst_adt_apy_tim_hvy =", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyNotEqualTo(String value) {
            addCriterion("lst_adt_apy_tim_hvy <>", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyGreaterThan(String value) {
            addCriterion("lst_adt_apy_tim_hvy >", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyGreaterThanOrEqualTo(String value) {
            addCriterion("lst_adt_apy_tim_hvy >=", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyLessThan(String value) {
            addCriterion("lst_adt_apy_tim_hvy <", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyLessThanOrEqualTo(String value) {
            addCriterion("lst_adt_apy_tim_hvy <=", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyLike(String value) {
            addCriterion("lst_adt_apy_tim_hvy like", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyNotLike(String value) {
            addCriterion("lst_adt_apy_tim_hvy not like", value, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyIn(List<String> values) {
            addCriterion("lst_adt_apy_tim_hvy in", values, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyNotIn(List<String> values) {
            addCriterion("lst_adt_apy_tim_hvy not in", values, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyBetween(String value1, String value2) {
            addCriterion("lst_adt_apy_tim_hvy between", value1, value2, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAdtApyTimHvyNotBetween(String value1, String value2) {
            addCriterion("lst_adt_apy_tim_hvy not between", value1, value2, "lstAdtApyTimHvy");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimIsNull() {
            addCriterion("lst_app_sta_tim is null");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimIsNotNull() {
            addCriterion("lst_app_sta_tim is not null");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimEqualTo(String value) {
            addCriterion("lst_app_sta_tim =", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimNotEqualTo(String value) {
            addCriterion("lst_app_sta_tim <>", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimGreaterThan(String value) {
            addCriterion("lst_app_sta_tim >", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimGreaterThanOrEqualTo(String value) {
            addCriterion("lst_app_sta_tim >=", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimLessThan(String value) {
            addCriterion("lst_app_sta_tim <", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimLessThanOrEqualTo(String value) {
            addCriterion("lst_app_sta_tim <=", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimLike(String value) {
            addCriterion("lst_app_sta_tim like", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimNotLike(String value) {
            addCriterion("lst_app_sta_tim not like", value, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimIn(List<String> values) {
            addCriterion("lst_app_sta_tim in", values, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimNotIn(List<String> values) {
            addCriterion("lst_app_sta_tim not in", values, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimBetween(String value1, String value2) {
            addCriterion("lst_app_sta_tim between", value1, value2, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstAppStaTimNotBetween(String value1, String value2) {
            addCriterion("lst_app_sta_tim not between", value1, value2, "lstAppStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimIsNull() {
            addCriterion("lst_mp_sta_tim is null");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimIsNotNull() {
            addCriterion("lst_mp_sta_tim is not null");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimEqualTo(String value) {
            addCriterion("lst_mp_sta_tim =", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimNotEqualTo(String value) {
            addCriterion("lst_mp_sta_tim <>", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimGreaterThan(String value) {
            addCriterion("lst_mp_sta_tim >", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimGreaterThanOrEqualTo(String value) {
            addCriterion("lst_mp_sta_tim >=", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimLessThan(String value) {
            addCriterion("lst_mp_sta_tim <", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimLessThanOrEqualTo(String value) {
            addCriterion("lst_mp_sta_tim <=", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimLike(String value) {
            addCriterion("lst_mp_sta_tim like", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimNotLike(String value) {
            addCriterion("lst_mp_sta_tim not like", value, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimIn(List<String> values) {
            addCriterion("lst_mp_sta_tim in", values, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimNotIn(List<String> values) {
            addCriterion("lst_mp_sta_tim not in", values, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimBetween(String value1, String value2) {
            addCriterion("lst_mp_sta_tim between", value1, value2, "lstMpStaTim");
            return (Criteria) this;
        }

        public Criteria andLstMpStaTimNotBetween(String value1, String value2) {
            addCriterion("lst_mp_sta_tim not between", value1, value2, "lstMpStaTim");
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

        public Criteria andCreateDateIsNull() {
            addCriterion("create_date is null");
            return (Criteria) this;
        }

        public Criteria andCreateDateIsNotNull() {
            addCriterion("create_date is not null");
            return (Criteria) this;
        }

        public Criteria andCreateDateEqualTo(Date value) {
            addCriterionForJDBCDate("create_date =", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("create_date <>", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThan(Date value) {
            addCriterionForJDBCDate("create_date >", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("create_date >=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThan(Date value) {
            addCriterionForJDBCDate("create_date <", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("create_date <=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateIn(List<Date> values) {
            addCriterionForJDBCDate("create_date in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("create_date not in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("create_date between", value1, value2, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("create_date not between", value1, value2, "createDate");
            return (Criteria) this;
        }

        public Criteria andLocalIdIsNull() {
            addCriterion("local_id is null");
            return (Criteria) this;
        }

        public Criteria andLocalIdIsNotNull() {
            addCriterion("local_id is not null");
            return (Criteria) this;
        }

        public Criteria andLocalIdEqualTo(Long value) {
            addCriterion("local_id =", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotEqualTo(Long value) {
            addCriterion("local_id <>", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThan(Long value) {
            addCriterion("local_id >", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdGreaterThanOrEqualTo(Long value) {
            addCriterion("local_id >=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThan(Long value) {
            addCriterion("local_id <", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdLessThanOrEqualTo(Long value) {
            addCriterion("local_id <=", value, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdIn(List<Long> values) {
            addCriterion("local_id in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotIn(List<Long> values) {
            addCriterion("local_id not in", values, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdBetween(Long value1, Long value2) {
            addCriterion("local_id between", value1, value2, "localId");
            return (Criteria) this;
        }

        public Criteria andLocalIdNotBetween(Long value1, Long value2) {
            addCriterion("local_id not between", value1, value2, "localId");
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

        public Criteria andApiCodeEqualTo(Integer value) {
            addCriterion("api_code =", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeNotEqualTo(Integer value) {
            addCriterion("api_code <>", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeGreaterThan(Integer value) {
            addCriterion("api_code >", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeGreaterThanOrEqualTo(Integer value) {
            addCriterion("api_code >=", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeLessThan(Integer value) {
            addCriterion("api_code <", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeLessThanOrEqualTo(Integer value) {
            addCriterion("api_code <=", value, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeIn(List<Integer> values) {
            addCriterion("api_code in", values, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeNotIn(List<Integer> values) {
            addCriterion("api_code not in", values, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeBetween(Integer value1, Integer value2) {
            addCriterion("api_code between", value1, value2, "apiCode");
            return (Criteria) this;
        }

        public Criteria andApiCodeNotBetween(Integer value1, Integer value2) {
            addCriterion("api_code not between", value1, value2, "apiCode");
            return (Criteria) this;
        }

        public Criteria andDataMessageIsNull() {
            addCriterion("data_message is null");
            return (Criteria) this;
        }

        public Criteria andDataMessageIsNotNull() {
            addCriterion("data_message is not null");
            return (Criteria) this;
        }

        public Criteria andDataMessageEqualTo(String value) {
            addCriterion("data_message =", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotEqualTo(String value) {
            addCriterion("data_message <>", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageGreaterThan(String value) {
            addCriterion("data_message >", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageGreaterThanOrEqualTo(String value) {
            addCriterion("data_message >=", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageLessThan(String value) {
            addCriterion("data_message <", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageLessThanOrEqualTo(String value) {
            addCriterion("data_message <=", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageLike(String value) {
            addCriterion("data_message like", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotLike(String value) {
            addCriterion("data_message not like", value, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageIn(List<String> values) {
            addCriterion("data_message in", values, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotIn(List<String> values) {
            addCriterion("data_message not in", values, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageBetween(String value1, String value2) {
            addCriterion("data_message between", value1, value2, "dataMessage");
            return (Criteria) this;
        }

        public Criteria andDataMessageNotBetween(String value1, String value2) {
            addCriterion("data_message not between", value1, value2, "dataMessage");
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