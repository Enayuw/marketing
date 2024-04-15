package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XieChengStatisticsReportExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public XieChengStatisticsReportExample() {
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

        public Criteria andReportTimeIsNull() {
            addCriterion("report_time is null");
            return (Criteria) this;
        }

        public Criteria andReportTimeIsNotNull() {
            addCriterion("report_time is not null");
            return (Criteria) this;
        }

        public Criteria andReportTimeEqualTo(String value) {
            addCriterion("report_time =", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeNotEqualTo(String value) {
            addCriterion("report_time <>", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeGreaterThan(String value) {
            addCriterion("report_time >", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeGreaterThanOrEqualTo(String value) {
            addCriterion("report_time >=", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeLessThan(String value) {
            addCriterion("report_time <", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeLessThanOrEqualTo(String value) {
            addCriterion("report_time <=", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeLike(String value) {
            addCriterion("report_time like", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeNotLike(String value) {
            addCriterion("report_time not like", value, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeIn(List<String> values) {
            addCriterion("report_time in", values, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeNotIn(List<String> values) {
            addCriterion("report_time not in", values, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeBetween(String value1, String value2) {
            addCriterion("report_time between", value1, value2, "reportTime");
            return (Criteria) this;
        }

        public Criteria andReportTimeNotBetween(String value1, String value2) {
            addCriterion("report_time not between", value1, value2, "reportTime");
            return (Criteria) this;
        }

        public Criteria andUploadCountIsNull() {
            addCriterion("upload_count is null");
            return (Criteria) this;
        }

        public Criteria andUploadCountIsNotNull() {
            addCriterion("upload_count is not null");
            return (Criteria) this;
        }

        public Criteria andUploadCountEqualTo(String value) {
            addCriterion("upload_count =", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountNotEqualTo(String value) {
            addCriterion("upload_count <>", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountGreaterThan(String value) {
            addCriterion("upload_count >", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountGreaterThanOrEqualTo(String value) {
            addCriterion("upload_count >=", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountLessThan(String value) {
            addCriterion("upload_count <", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountLessThanOrEqualTo(String value) {
            addCriterion("upload_count <=", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountLike(String value) {
            addCriterion("upload_count like", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountNotLike(String value) {
            addCriterion("upload_count not like", value, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountIn(List<String> values) {
            addCriterion("upload_count in", values, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountNotIn(List<String> values) {
            addCriterion("upload_count not in", values, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountBetween(String value1, String value2) {
            addCriterion("upload_count between", value1, value2, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andUploadCountNotBetween(String value1, String value2) {
            addCriterion("upload_count not between", value1, value2, "uploadCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountIsNull() {
            addCriterion("reported_home_page_count is null");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountIsNotNull() {
            addCriterion("reported_home_page_count is not null");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountEqualTo(String value) {
            addCriterion("reported_home_page_count =", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountNotEqualTo(String value) {
            addCriterion("reported_home_page_count <>", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountGreaterThan(String value) {
            addCriterion("reported_home_page_count >", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountGreaterThanOrEqualTo(String value) {
            addCriterion("reported_home_page_count >=", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountLessThan(String value) {
            addCriterion("reported_home_page_count <", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountLessThanOrEqualTo(String value) {
            addCriterion("reported_home_page_count <=", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountLike(String value) {
            addCriterion("reported_home_page_count like", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountNotLike(String value) {
            addCriterion("reported_home_page_count not like", value, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountIn(List<String> values) {
            addCriterion("reported_home_page_count in", values, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountNotIn(List<String> values) {
            addCriterion("reported_home_page_count not in", values, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountBetween(String value1, String value2) {
            addCriterion("reported_home_page_count between", value1, value2, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedHomePageCountNotBetween(String value1, String value2) {
            addCriterion("reported_home_page_count not between", value1, value2, "reportedHomePageCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountIsNull() {
            addCriterion("reported_initiate_count is null");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountIsNotNull() {
            addCriterion("reported_initiate_count is not null");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountEqualTo(String value) {
            addCriterion("reported_initiate_count =", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountNotEqualTo(String value) {
            addCriterion("reported_initiate_count <>", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountGreaterThan(String value) {
            addCriterion("reported_initiate_count >", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountGreaterThanOrEqualTo(String value) {
            addCriterion("reported_initiate_count >=", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountLessThan(String value) {
            addCriterion("reported_initiate_count <", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountLessThanOrEqualTo(String value) {
            addCriterion("reported_initiate_count <=", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountLike(String value) {
            addCriterion("reported_initiate_count like", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountNotLike(String value) {
            addCriterion("reported_initiate_count not like", value, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountIn(List<String> values) {
            addCriterion("reported_initiate_count in", values, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountNotIn(List<String> values) {
            addCriterion("reported_initiate_count not in", values, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountBetween(String value1, String value2) {
            addCriterion("reported_initiate_count between", value1, value2, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedInitiateCountNotBetween(String value1, String value2) {
            addCriterion("reported_initiate_count not between", value1, value2, "reportedInitiateCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountIsNull() {
            addCriterion("reported_success_count is null");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountIsNotNull() {
            addCriterion("reported_success_count is not null");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountEqualTo(String value) {
            addCriterion("reported_success_count =", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountNotEqualTo(String value) {
            addCriterion("reported_success_count <>", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountGreaterThan(String value) {
            addCriterion("reported_success_count >", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountGreaterThanOrEqualTo(String value) {
            addCriterion("reported_success_count >=", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountLessThan(String value) {
            addCriterion("reported_success_count <", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountLessThanOrEqualTo(String value) {
            addCriterion("reported_success_count <=", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountLike(String value) {
            addCriterion("reported_success_count like", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountNotLike(String value) {
            addCriterion("reported_success_count not like", value, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountIn(List<String> values) {
            addCriterion("reported_success_count in", values, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountNotIn(List<String> values) {
            addCriterion("reported_success_count not in", values, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountBetween(String value1, String value2) {
            addCriterion("reported_success_count between", value1, value2, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedSuccessCountNotBetween(String value1, String value2) {
            addCriterion("reported_success_count not between", value1, value2, "reportedSuccessCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountIsNull() {
            addCriterion("reported_credit_count is null");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountIsNotNull() {
            addCriterion("reported_credit_count is not null");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountEqualTo(String value) {
            addCriterion("reported_credit_count =", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountNotEqualTo(String value) {
            addCriterion("reported_credit_count <>", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountGreaterThan(String value) {
            addCriterion("reported_credit_count >", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountGreaterThanOrEqualTo(String value) {
            addCriterion("reported_credit_count >=", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountLessThan(String value) {
            addCriterion("reported_credit_count <", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountLessThanOrEqualTo(String value) {
            addCriterion("reported_credit_count <=", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountLike(String value) {
            addCriterion("reported_credit_count like", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountNotLike(String value) {
            addCriterion("reported_credit_count not like", value, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountIn(List<String> values) {
            addCriterion("reported_credit_count in", values, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountNotIn(List<String> values) {
            addCriterion("reported_credit_count not in", values, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountBetween(String value1, String value2) {
            addCriterion("reported_credit_count between", value1, value2, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedCreditCountNotBetween(String value1, String value2) {
            addCriterion("reported_credit_count not between", value1, value2, "reportedCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountIsNull() {
            addCriterion("reported_drawings_count is null");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountIsNotNull() {
            addCriterion("reported_drawings_count is not null");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountEqualTo(String value) {
            addCriterion("reported_drawings_count =", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountNotEqualTo(String value) {
            addCriterion("reported_drawings_count <>", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountGreaterThan(String value) {
            addCriterion("reported_drawings_count >", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountGreaterThanOrEqualTo(String value) {
            addCriterion("reported_drawings_count >=", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountLessThan(String value) {
            addCriterion("reported_drawings_count <", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountLessThanOrEqualTo(String value) {
            addCriterion("reported_drawings_count <=", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountLike(String value) {
            addCriterion("reported_drawings_count like", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountNotLike(String value) {
            addCriterion("reported_drawings_count not like", value, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountIn(List<String> values) {
            addCriterion("reported_drawings_count in", values, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountNotIn(List<String> values) {
            addCriterion("reported_drawings_count not in", values, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountBetween(String value1, String value2) {
            addCriterion("reported_drawings_count between", value1, value2, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedDrawingsCountNotBetween(String value1, String value2) {
            addCriterion("reported_drawings_count not between", value1, value2, "reportedDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountIsNull() {
            addCriterion("reported_million_credit_count is null");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountIsNotNull() {
            addCriterion("reported_million_credit_count is not null");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountEqualTo(String value) {
            addCriterion("reported_million_credit_count =", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountNotEqualTo(String value) {
            addCriterion("reported_million_credit_count <>", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountGreaterThan(String value) {
            addCriterion("reported_million_credit_count >", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountGreaterThanOrEqualTo(String value) {
            addCriterion("reported_million_credit_count >=", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountLessThan(String value) {
            addCriterion("reported_million_credit_count <", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountLessThanOrEqualTo(String value) {
            addCriterion("reported_million_credit_count <=", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountLike(String value) {
            addCriterion("reported_million_credit_count like", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountNotLike(String value) {
            addCriterion("reported_million_credit_count not like", value, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountIn(List<String> values) {
            addCriterion("reported_million_credit_count in", values, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountNotIn(List<String> values) {
            addCriterion("reported_million_credit_count not in", values, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountBetween(String value1, String value2) {
            addCriterion("reported_million_credit_count between", value1, value2, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andReportedMillionCreditCountNotBetween(String value1, String value2) {
            addCriterion("reported_million_credit_count not between", value1, value2, "reportedMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountIsNull() {
            addCriterion("outbound_count is null");
            return (Criteria) this;
        }

        public Criteria andOutboundCountIsNotNull() {
            addCriterion("outbound_count is not null");
            return (Criteria) this;
        }

        public Criteria andOutboundCountEqualTo(String value) {
            addCriterion("outbound_count =", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountNotEqualTo(String value) {
            addCriterion("outbound_count <>", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountGreaterThan(String value) {
            addCriterion("outbound_count >", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountGreaterThanOrEqualTo(String value) {
            addCriterion("outbound_count >=", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountLessThan(String value) {
            addCriterion("outbound_count <", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountLessThanOrEqualTo(String value) {
            addCriterion("outbound_count <=", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountLike(String value) {
            addCriterion("outbound_count like", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountNotLike(String value) {
            addCriterion("outbound_count not like", value, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountIn(List<String> values) {
            addCriterion("outbound_count in", values, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountNotIn(List<String> values) {
            addCriterion("outbound_count not in", values, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountBetween(String value1, String value2) {
            addCriterion("outbound_count between", value1, value2, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCountNotBetween(String value1, String value2) {
            addCriterion("outbound_count not between", value1, value2, "outboundCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountIsNull() {
            addCriterion("outbound_home_page_count is null");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountIsNotNull() {
            addCriterion("outbound_home_page_count is not null");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountEqualTo(String value) {
            addCriterion("outbound_home_page_count =", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountNotEqualTo(String value) {
            addCriterion("outbound_home_page_count <>", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountGreaterThan(String value) {
            addCriterion("outbound_home_page_count >", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountGreaterThanOrEqualTo(String value) {
            addCriterion("outbound_home_page_count >=", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountLessThan(String value) {
            addCriterion("outbound_home_page_count <", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountLessThanOrEqualTo(String value) {
            addCriterion("outbound_home_page_count <=", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountLike(String value) {
            addCriterion("outbound_home_page_count like", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountNotLike(String value) {
            addCriterion("outbound_home_page_count not like", value, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountIn(List<String> values) {
            addCriterion("outbound_home_page_count in", values, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountNotIn(List<String> values) {
            addCriterion("outbound_home_page_count not in", values, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountBetween(String value1, String value2) {
            addCriterion("outbound_home_page_count between", value1, value2, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundHomePageCountNotBetween(String value1, String value2) {
            addCriterion("outbound_home_page_count not between", value1, value2, "outboundHomePageCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountIsNull() {
            addCriterion("outbound_initiate_count is null");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountIsNotNull() {
            addCriterion("outbound_initiate_count is not null");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountEqualTo(String value) {
            addCriterion("outbound_initiate_count =", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountNotEqualTo(String value) {
            addCriterion("outbound_initiate_count <>", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountGreaterThan(String value) {
            addCriterion("outbound_initiate_count >", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountGreaterThanOrEqualTo(String value) {
            addCriterion("outbound_initiate_count >=", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountLessThan(String value) {
            addCriterion("outbound_initiate_count <", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountLessThanOrEqualTo(String value) {
            addCriterion("outbound_initiate_count <=", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountLike(String value) {
            addCriterion("outbound_initiate_count like", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountNotLike(String value) {
            addCriterion("outbound_initiate_count not like", value, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountIn(List<String> values) {
            addCriterion("outbound_initiate_count in", values, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountNotIn(List<String> values) {
            addCriterion("outbound_initiate_count not in", values, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountBetween(String value1, String value2) {
            addCriterion("outbound_initiate_count between", value1, value2, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundInitiateCountNotBetween(String value1, String value2) {
            addCriterion("outbound_initiate_count not between", value1, value2, "outboundInitiateCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountIsNull() {
            addCriterion("outbound_success_count is null");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountIsNotNull() {
            addCriterion("outbound_success_count is not null");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountEqualTo(String value) {
            addCriterion("outbound_success_count =", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountNotEqualTo(String value) {
            addCriterion("outbound_success_count <>", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountGreaterThan(String value) {
            addCriterion("outbound_success_count >", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountGreaterThanOrEqualTo(String value) {
            addCriterion("outbound_success_count >=", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountLessThan(String value) {
            addCriterion("outbound_success_count <", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountLessThanOrEqualTo(String value) {
            addCriterion("outbound_success_count <=", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountLike(String value) {
            addCriterion("outbound_success_count like", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountNotLike(String value) {
            addCriterion("outbound_success_count not like", value, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountIn(List<String> values) {
            addCriterion("outbound_success_count in", values, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountNotIn(List<String> values) {
            addCriterion("outbound_success_count not in", values, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountBetween(String value1, String value2) {
            addCriterion("outbound_success_count between", value1, value2, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundSuccessCountNotBetween(String value1, String value2) {
            addCriterion("outbound_success_count not between", value1, value2, "outboundSuccessCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountIsNull() {
            addCriterion("outbound_credit_count is null");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountIsNotNull() {
            addCriterion("outbound_credit_count is not null");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountEqualTo(String value) {
            addCriterion("outbound_credit_count =", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountNotEqualTo(String value) {
            addCriterion("outbound_credit_count <>", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountGreaterThan(String value) {
            addCriterion("outbound_credit_count >", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountGreaterThanOrEqualTo(String value) {
            addCriterion("outbound_credit_count >=", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountLessThan(String value) {
            addCriterion("outbound_credit_count <", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountLessThanOrEqualTo(String value) {
            addCriterion("outbound_credit_count <=", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountLike(String value) {
            addCriterion("outbound_credit_count like", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountNotLike(String value) {
            addCriterion("outbound_credit_count not like", value, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountIn(List<String> values) {
            addCriterion("outbound_credit_count in", values, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountNotIn(List<String> values) {
            addCriterion("outbound_credit_count not in", values, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountBetween(String value1, String value2) {
            addCriterion("outbound_credit_count between", value1, value2, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundCreditCountNotBetween(String value1, String value2) {
            addCriterion("outbound_credit_count not between", value1, value2, "outboundCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountIsNull() {
            addCriterion("outbound_drawings_count is null");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountIsNotNull() {
            addCriterion("outbound_drawings_count is not null");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountEqualTo(String value) {
            addCriterion("outbound_drawings_count =", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountNotEqualTo(String value) {
            addCriterion("outbound_drawings_count <>", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountGreaterThan(String value) {
            addCriterion("outbound_drawings_count >", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountGreaterThanOrEqualTo(String value) {
            addCriterion("outbound_drawings_count >=", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountLessThan(String value) {
            addCriterion("outbound_drawings_count <", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountLessThanOrEqualTo(String value) {
            addCriterion("outbound_drawings_count <=", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountLike(String value) {
            addCriterion("outbound_drawings_count like", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountNotLike(String value) {
            addCriterion("outbound_drawings_count not like", value, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountIn(List<String> values) {
            addCriterion("outbound_drawings_count in", values, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountNotIn(List<String> values) {
            addCriterion("outbound_drawings_count not in", values, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountBetween(String value1, String value2) {
            addCriterion("outbound_drawings_count between", value1, value2, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundDrawingsCountNotBetween(String value1, String value2) {
            addCriterion("outbound_drawings_count not between", value1, value2, "outboundDrawingsCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountIsNull() {
            addCriterion("outbound_million_credit_count is null");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountIsNotNull() {
            addCriterion("outbound_million_credit_count is not null");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountEqualTo(String value) {
            addCriterion("outbound_million_credit_count =", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountNotEqualTo(String value) {
            addCriterion("outbound_million_credit_count <>", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountGreaterThan(String value) {
            addCriterion("outbound_million_credit_count >", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountGreaterThanOrEqualTo(String value) {
            addCriterion("outbound_million_credit_count >=", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountLessThan(String value) {
            addCriterion("outbound_million_credit_count <", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountLessThanOrEqualTo(String value) {
            addCriterion("outbound_million_credit_count <=", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountLike(String value) {
            addCriterion("outbound_million_credit_count like", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountNotLike(String value) {
            addCriterion("outbound_million_credit_count not like", value, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountIn(List<String> values) {
            addCriterion("outbound_million_credit_count in", values, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountNotIn(List<String> values) {
            addCriterion("outbound_million_credit_count not in", values, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountBetween(String value1, String value2) {
            addCriterion("outbound_million_credit_count between", value1, value2, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andOutboundMillionCreditCountNotBetween(String value1, String value2) {
            addCriterion("outbound_million_credit_count not between", value1, value2, "outboundMillionCreditCount");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("remark is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("remark is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("remark =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("remark <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("remark >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("remark >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("remark <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("remark <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("remark like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("remark not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("remark in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("remark not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("remark between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("remark not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIsNull() {
            addCriterion("is_delete is null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIsNotNull() {
            addCriterion("is_delete is not null");
            return (Criteria) this;
        }

        public Criteria andIsDeleteEqualTo(Integer value) {
            addCriterion("is_delete =", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotEqualTo(Integer value) {
            addCriterion("is_delete <>", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThan(Integer value) {
            addCriterion("is_delete >", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_delete >=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThan(Integer value) {
            addCriterion("is_delete <", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteLessThanOrEqualTo(Integer value) {
            addCriterion("is_delete <=", value, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteIn(List<Integer> values) {
            addCriterion("is_delete in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotIn(List<Integer> values) {
            addCriterion("is_delete not in", values, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteBetween(Integer value1, Integer value2) {
            addCriterion("is_delete between", value1, value2, "isDelete");
            return (Criteria) this;
        }

        public Criteria andIsDeleteNotBetween(Integer value1, Integer value2) {
            addCriterion("is_delete not between", value1, value2, "isDelete");
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