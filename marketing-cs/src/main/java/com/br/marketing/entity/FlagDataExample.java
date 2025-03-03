package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class FlagDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public FlagDataExample() {
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

        public Criteria andCustNumIsNull() {
            addCriterion("cust_num is null");
            return (Criteria) this;
        }

        public Criteria andCustNumIsNotNull() {
            addCriterion("cust_num is not null");
            return (Criteria) this;
        }

        public Criteria andCustNumEqualTo(String value) {
            addCriterion("cust_num =", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotEqualTo(String value) {
            addCriterion("cust_num <>", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumGreaterThan(String value) {
            addCriterion("cust_num >", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumGreaterThanOrEqualTo(String value) {
            addCriterion("cust_num >=", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLessThan(String value) {
            addCriterion("cust_num <", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLessThanOrEqualTo(String value) {
            addCriterion("cust_num <=", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumLike(String value) {
            addCriterion("cust_num like", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotLike(String value) {
            addCriterion("cust_num not like", value, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumIn(List<String> values) {
            addCriterion("cust_num in", values, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotIn(List<String> values) {
            addCriterion("cust_num not in", values, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumBetween(String value1, String value2) {
            addCriterion("cust_num between", value1, value2, "custNum");
            return (Criteria) this;
        }

        public Criteria andCustNumNotBetween(String value1, String value2) {
            addCriterion("cust_num not between", value1, value2, "custNum");
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

        public Criteria andAppletDateIsNull() {
            addCriterion("applet_date is null");
            return (Criteria) this;
        }

        public Criteria andAppletDateIsNotNull() {
            addCriterion("applet_date is not null");
            return (Criteria) this;
        }

        public Criteria andAppletDateEqualTo(String value) {
            addCriterion("applet_date =", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotEqualTo(String value) {
            addCriterion("applet_date <>", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThan(String value) {
            addCriterion("applet_date >", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateGreaterThanOrEqualTo(String value) {
            addCriterion("applet_date >=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThan(String value) {
            addCriterion("applet_date <", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLessThanOrEqualTo(String value) {
            addCriterion("applet_date <=", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateLike(String value) {
            addCriterion("applet_date like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotLike(String value) {
            addCriterion("applet_date not like", value, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateIn(List<String> values) {
            addCriterion("applet_date in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotIn(List<String> values) {
            addCriterion("applet_date not in", values, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateBetween(String value1, String value2) {
            addCriterion("applet_date between", value1, value2, "appletDate");
            return (Criteria) this;
        }

        public Criteria andAppletDateNotBetween(String value1, String value2) {
            addCriterion("applet_date not between", value1, value2, "appletDate");
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

        public Criteria andCellMd5IsNull() {
            addCriterion("cell_md5 is null");
            return (Criteria) this;
        }

        public Criteria andCellMd5IsNotNull() {
            addCriterion("cell_md5 is not null");
            return (Criteria) this;
        }

        public Criteria andCellMd5EqualTo(String value) {
            addCriterion("cell_md5 =", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotEqualTo(String value) {
            addCriterion("cell_md5 <>", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5GreaterThan(String value) {
            addCriterion("cell_md5 >", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5GreaterThanOrEqualTo(String value) {
            addCriterion("cell_md5 >=", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5LessThan(String value) {
            addCriterion("cell_md5 <", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5LessThanOrEqualTo(String value) {
            addCriterion("cell_md5 <=", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5Like(String value) {
            addCriterion("cell_md5 like", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotLike(String value) {
            addCriterion("cell_md5 not like", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5In(List<String> values) {
            addCriterion("cell_md5 in", values, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotIn(List<String> values) {
            addCriterion("cell_md5 not in", values, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5Between(String value1, String value2) {
            addCriterion("cell_md5 between", value1, value2, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotBetween(String value1, String value2) {
            addCriterion("cell_md5 not between", value1, value2, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellSha256IsNull() {
            addCriterion("cell_sha256 is null");
            return (Criteria) this;
        }

        public Criteria andCellSha256IsNotNull() {
            addCriterion("cell_sha256 is not null");
            return (Criteria) this;
        }

        public Criteria andCellSha256EqualTo(String value) {
            addCriterion("cell_sha256 =", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotEqualTo(String value) {
            addCriterion("cell_sha256 <>", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256GreaterThan(String value) {
            addCriterion("cell_sha256 >", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256GreaterThanOrEqualTo(String value) {
            addCriterion("cell_sha256 >=", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256LessThan(String value) {
            addCriterion("cell_sha256 <", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256LessThanOrEqualTo(String value) {
            addCriterion("cell_sha256 <=", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256Like(String value) {
            addCriterion("cell_sha256 like", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotLike(String value) {
            addCriterion("cell_sha256 not like", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256In(List<String> values) {
            addCriterion("cell_sha256 in", values, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotIn(List<String> values) {
            addCriterion("cell_sha256 not in", values, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256Between(String value1, String value2) {
            addCriterion("cell_sha256 between", value1, value2, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotBetween(String value1, String value2) {
            addCriterion("cell_sha256 not between", value1, value2, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellLogIsNull() {
            addCriterion("cell_log is null");
            return (Criteria) this;
        }

        public Criteria andCellLogIsNotNull() {
            addCriterion("cell_log is not null");
            return (Criteria) this;
        }

        public Criteria andCellLogEqualTo(String value) {
            addCriterion("cell_log =", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogNotEqualTo(String value) {
            addCriterion("cell_log <>", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogGreaterThan(String value) {
            addCriterion("cell_log >", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogGreaterThanOrEqualTo(String value) {
            addCriterion("cell_log >=", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogLessThan(String value) {
            addCriterion("cell_log <", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogLessThanOrEqualTo(String value) {
            addCriterion("cell_log <=", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogLike(String value) {
            addCriterion("cell_log like", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogNotLike(String value) {
            addCriterion("cell_log not like", value, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogIn(List<String> values) {
            addCriterion("cell_log in", values, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogNotIn(List<String> values) {
            addCriterion("cell_log not in", values, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogBetween(String value1, String value2) {
            addCriterion("cell_log between", value1, value2, "cellLog");
            return (Criteria) this;
        }

        public Criteria andCellLogNotBetween(String value1, String value2) {
            addCriterion("cell_log not between", value1, value2, "cellLog");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationIsNull() {
            addCriterion("flag_cell_decode_computation is null");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationIsNotNull() {
            addCriterion("flag_cell_decode_computation is not null");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationEqualTo(Integer value) {
            addCriterion("flag_cell_decode_computation =", value, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationNotEqualTo(Integer value) {
            addCriterion("flag_cell_decode_computation <>", value, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationGreaterThan(Integer value) {
            addCriterion("flag_cell_decode_computation >", value, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_cell_decode_computation >=", value, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationLessThan(Integer value) {
            addCriterion("flag_cell_decode_computation <", value, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationLessThanOrEqualTo(Integer value) {
            addCriterion("flag_cell_decode_computation <=", value, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationIn(List<Integer> values) {
            addCriterion("flag_cell_decode_computation in", values, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationNotIn(List<Integer> values) {
            addCriterion("flag_cell_decode_computation not in", values, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationBetween(Integer value1, Integer value2) {
            addCriterion("flag_cell_decode_computation between", value1, value2, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCellDecodeComputationNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_cell_decode_computation not between", value1, value2, "flagCellDecodeComputation");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistIsNull() {
            addCriterion("dt_whitelist is null");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistIsNotNull() {
            addCriterion("dt_whitelist is not null");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistEqualTo(Date value) {
            addCriterionForJDBCDate("dt_whitelist =", value, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistNotEqualTo(Date value) {
            addCriterionForJDBCDate("dt_whitelist <>", value, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistGreaterThan(Date value) {
            addCriterionForJDBCDate("dt_whitelist >", value, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("dt_whitelist >=", value, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistLessThan(Date value) {
            addCriterionForJDBCDate("dt_whitelist <", value, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("dt_whitelist <=", value, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistIn(List<Date> values) {
            addCriterionForJDBCDate("dt_whitelist in", values, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistNotIn(List<Date> values) {
            addCriterionForJDBCDate("dt_whitelist not in", values, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("dt_whitelist between", value1, value2, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andDtWhitelistNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("dt_whitelist not between", value1, value2, "dtWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustIsNull() {
            addCriterion("flag_new_cust is null");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustIsNotNull() {
            addCriterion("flag_new_cust is not null");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustEqualTo(Integer value) {
            addCriterion("flag_new_cust =", value, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustNotEqualTo(Integer value) {
            addCriterion("flag_new_cust <>", value, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustGreaterThan(Integer value) {
            addCriterion("flag_new_cust >", value, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_new_cust >=", value, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustLessThan(Integer value) {
            addCriterion("flag_new_cust <", value, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustLessThanOrEqualTo(Integer value) {
            addCriterion("flag_new_cust <=", value, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustIn(List<Integer> values) {
            addCriterion("flag_new_cust in", values, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustNotIn(List<Integer> values) {
            addCriterion("flag_new_cust not in", values, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustBetween(Integer value1, Integer value2) {
            addCriterion("flag_new_cust between", value1, value2, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_new_cust not between", value1, value2, "flagNewCust");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationIsNull() {
            addCriterion("flag_new_cust_computation is null");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationIsNotNull() {
            addCriterion("flag_new_cust_computation is not null");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationEqualTo(Integer value) {
            addCriterion("flag_new_cust_computation =", value, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationNotEqualTo(Integer value) {
            addCriterion("flag_new_cust_computation <>", value, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationGreaterThan(Integer value) {
            addCriterion("flag_new_cust_computation >", value, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_new_cust_computation >=", value, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationLessThan(Integer value) {
            addCriterion("flag_new_cust_computation <", value, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationLessThanOrEqualTo(Integer value) {
            addCriterion("flag_new_cust_computation <=", value, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationIn(List<Integer> values) {
            addCriterion("flag_new_cust_computation in", values, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationNotIn(List<Integer> values) {
            addCriterion("flag_new_cust_computation not in", values, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationBetween(Integer value1, Integer value2) {
            addCriterion("flag_new_cust_computation between", value1, value2, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagNewCustComputationNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_new_cust_computation not between", value1, value2, "flagNewCustComputation");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupIsNull() {
            addCriterion("flag_riskgroup is null");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupIsNotNull() {
            addCriterion("flag_riskgroup is not null");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupEqualTo(String value) {
            addCriterion("flag_riskgroup =", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupNotEqualTo(String value) {
            addCriterion("flag_riskgroup <>", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupGreaterThan(String value) {
            addCriterion("flag_riskgroup >", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupGreaterThanOrEqualTo(String value) {
            addCriterion("flag_riskgroup >=", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupLessThan(String value) {
            addCriterion("flag_riskgroup <", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupLessThanOrEqualTo(String value) {
            addCriterion("flag_riskgroup <=", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupLike(String value) {
            addCriterion("flag_riskgroup like", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupNotLike(String value) {
            addCriterion("flag_riskgroup not like", value, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupIn(List<String> values) {
            addCriterion("flag_riskgroup in", values, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupNotIn(List<String> values) {
            addCriterion("flag_riskgroup not in", values, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupBetween(String value1, String value2) {
            addCriterion("flag_riskgroup between", value1, value2, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagRiskgroupNotBetween(String value1, String value2) {
            addCriterion("flag_riskgroup not between", value1, value2, "flagRiskgroup");
            return (Criteria) this;
        }

        public Criteria andFlagInterestIsNull() {
            addCriterion("flag_interest is null");
            return (Criteria) this;
        }

        public Criteria andFlagInterestIsNotNull() {
            addCriterion("flag_interest is not null");
            return (Criteria) this;
        }

        public Criteria andFlagInterestEqualTo(Integer value) {
            addCriterion("flag_interest =", value, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestNotEqualTo(Integer value) {
            addCriterion("flag_interest <>", value, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestGreaterThan(Integer value) {
            addCriterion("flag_interest >", value, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_interest >=", value, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestLessThan(Integer value) {
            addCriterion("flag_interest <", value, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestLessThanOrEqualTo(Integer value) {
            addCriterion("flag_interest <=", value, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestIn(List<Integer> values) {
            addCriterion("flag_interest in", values, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestNotIn(List<Integer> values) {
            addCriterion("flag_interest not in", values, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestBetween(Integer value1, Integer value2) {
            addCriterion("flag_interest between", value1, value2, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagInterestNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_interest not between", value1, value2, "flagInterest");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationIsNull() {
            addCriterion("flag_customer_base_computation is null");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationIsNotNull() {
            addCriterion("flag_customer_base_computation is not null");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationEqualTo(Integer value) {
            addCriterion("flag_customer_base_computation =", value, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationNotEqualTo(Integer value) {
            addCriterion("flag_customer_base_computation <>", value, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationGreaterThan(Integer value) {
            addCriterion("flag_customer_base_computation >", value, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_customer_base_computation >=", value, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationLessThan(Integer value) {
            addCriterion("flag_customer_base_computation <", value, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationLessThanOrEqualTo(Integer value) {
            addCriterion("flag_customer_base_computation <=", value, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationIn(List<Integer> values) {
            addCriterion("flag_customer_base_computation in", values, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationNotIn(List<Integer> values) {
            addCriterion("flag_customer_base_computation not in", values, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationBetween(Integer value1, Integer value2) {
            addCriterion("flag_customer_base_computation between", value1, value2, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagCustomerBaseComputationNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_customer_base_computation not between", value1, value2, "flagCustomerBaseComputation");
            return (Criteria) this;
        }

        public Criteria andFlagAgeIsNull() {
            addCriterion("flag_age is null");
            return (Criteria) this;
        }

        public Criteria andFlagAgeIsNotNull() {
            addCriterion("flag_age is not null");
            return (Criteria) this;
        }

        public Criteria andFlagAgeEqualTo(Integer value) {
            addCriterion("flag_age =", value, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeNotEqualTo(Integer value) {
            addCriterion("flag_age <>", value, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeGreaterThan(Integer value) {
            addCriterion("flag_age >", value, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_age >=", value, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeLessThan(Integer value) {
            addCriterion("flag_age <", value, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeLessThanOrEqualTo(Integer value) {
            addCriterion("flag_age <=", value, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeIn(List<Integer> values) {
            addCriterion("flag_age in", values, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeNotIn(List<Integer> values) {
            addCriterion("flag_age not in", values, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeBetween(Integer value1, Integer value2) {
            addCriterion("flag_age between", value1, value2, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagAgeNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_age not between", value1, value2, "flagAge");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceIsNull() {
            addCriterion("flag_province is null");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceIsNotNull() {
            addCriterion("flag_province is not null");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceEqualTo(Integer value) {
            addCriterion("flag_province =", value, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceNotEqualTo(Integer value) {
            addCriterion("flag_province <>", value, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceGreaterThan(Integer value) {
            addCriterion("flag_province >", value, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_province >=", value, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceLessThan(Integer value) {
            addCriterion("flag_province <", value, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceLessThanOrEqualTo(Integer value) {
            addCriterion("flag_province <=", value, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceIn(List<Integer> values) {
            addCriterion("flag_province in", values, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceNotIn(List<Integer> values) {
            addCriterion("flag_province not in", values, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceBetween(Integer value1, Integer value2) {
            addCriterion("flag_province between", value1, value2, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagProvinceNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_province not between", value1, value2, "flagProvince");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallIsNull() {
            addCriterion("flag_special_small is null");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallIsNotNull() {
            addCriterion("flag_special_small is not null");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallEqualTo(Integer value) {
            addCriterion("flag_special_small =", value, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallNotEqualTo(Integer value) {
            addCriterion("flag_special_small <>", value, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallGreaterThan(Integer value) {
            addCriterion("flag_special_small >", value, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_special_small >=", value, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallLessThan(Integer value) {
            addCriterion("flag_special_small <", value, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallLessThanOrEqualTo(Integer value) {
            addCriterion("flag_special_small <=", value, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallIn(List<Integer> values) {
            addCriterion("flag_special_small in", values, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallNotIn(List<Integer> values) {
            addCriterion("flag_special_small not in", values, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallBetween(Integer value1, Integer value2) {
            addCriterion("flag_special_small between", value1, value2, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialSmallNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_special_small not between", value1, value2, "flagSpecialSmall");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleIsNull() {
            addCriterion("flag_specialrisklevel_rule is null");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleIsNotNull() {
            addCriterion("flag_specialrisklevel_rule is not null");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleEqualTo(Integer value) {
            addCriterion("flag_specialrisklevel_rule =", value, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleNotEqualTo(Integer value) {
            addCriterion("flag_specialrisklevel_rule <>", value, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleGreaterThan(Integer value) {
            addCriterion("flag_specialrisklevel_rule >", value, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_specialrisklevel_rule >=", value, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleLessThan(Integer value) {
            addCriterion("flag_specialrisklevel_rule <", value, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleLessThanOrEqualTo(Integer value) {
            addCriterion("flag_specialrisklevel_rule <=", value, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleIn(List<Integer> values) {
            addCriterion("flag_specialrisklevel_rule in", values, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleNotIn(List<Integer> values) {
            addCriterion("flag_specialrisklevel_rule not in", values, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleBetween(Integer value1, Integer value2) {
            addCriterion("flag_specialrisklevel_rule between", value1, value2, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagSpecialrisklevelRuleNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_specialrisklevel_rule not between", value1, value2, "flagSpecialrisklevelRule");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsIsNull() {
            addCriterion("flag_indexcs is null");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsIsNotNull() {
            addCriterion("flag_indexcs is not null");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsEqualTo(Integer value) {
            addCriterion("flag_indexcs =", value, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsNotEqualTo(Integer value) {
            addCriterion("flag_indexcs <>", value, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsGreaterThan(Integer value) {
            addCriterion("flag_indexcs >", value, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_indexcs >=", value, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsLessThan(Integer value) {
            addCriterion("flag_indexcs <", value, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsLessThanOrEqualTo(Integer value) {
            addCriterion("flag_indexcs <=", value, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsIn(List<Integer> values) {
            addCriterion("flag_indexcs in", values, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsNotIn(List<Integer> values) {
            addCriterion("flag_indexcs not in", values, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsBetween(Integer value1, Integer value2) {
            addCriterion("flag_indexcs between", value1, value2, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagIndexcsNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_indexcs not between", value1, value2, "flagIndexcs");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanIsNull() {
            addCriterion("flag_applyloan is null");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanIsNotNull() {
            addCriterion("flag_applyloan is not null");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanEqualTo(Integer value) {
            addCriterion("flag_applyloan =", value, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanNotEqualTo(Integer value) {
            addCriterion("flag_applyloan <>", value, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanGreaterThan(Integer value) {
            addCriterion("flag_applyloan >", value, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_applyloan >=", value, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanLessThan(Integer value) {
            addCriterion("flag_applyloan <", value, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanLessThanOrEqualTo(Integer value) {
            addCriterion("flag_applyloan <=", value, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanIn(List<Integer> values) {
            addCriterion("flag_applyloan in", values, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanNotIn(List<Integer> values) {
            addCriterion("flag_applyloan not in", values, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanBetween(Integer value1, Integer value2) {
            addCriterion("flag_applyloan between", value1, value2, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagApplyloanNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_applyloan not between", value1, value2, "flagApplyloan");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseIsNull() {
            addCriterion("flag_scoreysbase is null");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseIsNotNull() {
            addCriterion("flag_scoreysbase is not null");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseEqualTo(Integer value) {
            addCriterion("flag_scoreysbase =", value, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseNotEqualTo(Integer value) {
            addCriterion("flag_scoreysbase <>", value, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseGreaterThan(Integer value) {
            addCriterion("flag_scoreysbase >", value, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_scoreysbase >=", value, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseLessThan(Integer value) {
            addCriterion("flag_scoreysbase <", value, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseLessThanOrEqualTo(Integer value) {
            addCriterion("flag_scoreysbase <=", value, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseIn(List<Integer> values) {
            addCriterion("flag_scoreysbase in", values, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseNotIn(List<Integer> values) {
            addCriterion("flag_scoreysbase not in", values, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseBetween(Integer value1, Integer value2) {
            addCriterion("flag_scoreysbase between", value1, value2, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScoreysbaseNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_scoreysbase not between", value1, value2, "flagScoreysbase");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebIsNull() {
            addCriterion("flag_scorefxsbbaseb is null");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebIsNotNull() {
            addCriterion("flag_scorefxsbbaseb is not null");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebEqualTo(Integer value) {
            addCriterion("flag_scorefxsbbaseb =", value, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebNotEqualTo(Integer value) {
            addCriterion("flag_scorefxsbbaseb <>", value, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebGreaterThan(Integer value) {
            addCriterion("flag_scorefxsbbaseb >", value, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_scorefxsbbaseb >=", value, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebLessThan(Integer value) {
            addCriterion("flag_scorefxsbbaseb <", value, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebLessThanOrEqualTo(Integer value) {
            addCriterion("flag_scorefxsbbaseb <=", value, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebIn(List<Integer> values) {
            addCriterion("flag_scorefxsbbaseb in", values, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebNotIn(List<Integer> values) {
            addCriterion("flag_scorefxsbbaseb not in", values, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorefxsbbaseb between", value1, value2, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorefxsbbasebNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorefxsbbaseb not between", value1, value2, "flagScorefxsbbaseb");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinIsNull() {
            addCriterion("flag_scorescashonregisternologin is null");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinIsNotNull() {
            addCriterion("flag_scorescashonregisternologin is not null");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinEqualTo(Integer value) {
            addCriterion("flag_scorescashonregisternologin =", value, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinNotEqualTo(Integer value) {
            addCriterion("flag_scorescashonregisternologin <>", value, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinGreaterThan(Integer value) {
            addCriterion("flag_scorescashonregisternologin >", value, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_scorescashonregisternologin >=", value, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinLessThan(Integer value) {
            addCriterion("flag_scorescashonregisternologin <", value, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinLessThanOrEqualTo(Integer value) {
            addCriterion("flag_scorescashonregisternologin <=", value, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinIn(List<Integer> values) {
            addCriterion("flag_scorescashonregisternologin in", values, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinNotIn(List<Integer> values) {
            addCriterion("flag_scorescashonregisternologin not in", values, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorescashonregisternologin between", value1, value2, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonregisternologinNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorescashonregisternologin not between", value1, value2, "flagScorescashonregisternologin");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyIsNull() {
            addCriterion("flag_scorescashonyxxy is null");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyIsNotNull() {
            addCriterion("flag_scorescashonyxxy is not null");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyEqualTo(Integer value) {
            addCriterion("flag_scorescashonyxxy =", value, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyNotEqualTo(Integer value) {
            addCriterion("flag_scorescashonyxxy <>", value, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyGreaterThan(Integer value) {
            addCriterion("flag_scorescashonyxxy >", value, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_scorescashonyxxy >=", value, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyLessThan(Integer value) {
            addCriterion("flag_scorescashonyxxy <", value, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyLessThanOrEqualTo(Integer value) {
            addCriterion("flag_scorescashonyxxy <=", value, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyIn(List<Integer> values) {
            addCriterion("flag_scorescashonyxxy in", values, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyNotIn(List<Integer> values) {
            addCriterion("flag_scorescashonyxxy not in", values, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorescashonyxxy between", value1, value2, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorescashonyxxyNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorescashonyxxy not between", value1, value2, "flagScorescashonyxxy");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymIsNull() {
            addCriterion("flag_scorencashonzawswyyym is null");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymIsNotNull() {
            addCriterion("flag_scorencashonzawswyyym is not null");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymEqualTo(Integer value) {
            addCriterion("flag_scorencashonzawswyyym =", value, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymNotEqualTo(Integer value) {
            addCriterion("flag_scorencashonzawswyyym <>", value, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymGreaterThan(Integer value) {
            addCriterion("flag_scorencashonzawswyyym >", value, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_scorencashonzawswyyym >=", value, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymLessThan(Integer value) {
            addCriterion("flag_scorencashonzawswyyym <", value, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymLessThanOrEqualTo(Integer value) {
            addCriterion("flag_scorencashonzawswyyym <=", value, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymIn(List<Integer> values) {
            addCriterion("flag_scorencashonzawswyyym in", values, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymNotIn(List<Integer> values) {
            addCriterion("flag_scorencashonzawswyyym not in", values, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorencashonzawswyyym between", value1, value2, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagScorencashonzawswyyymNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_scorencashonzawswyyym not between", value1, value2, "flagScorencashonzawswyyym");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationIsNull() {
            addCriterion("flag_high_risk_computation is null");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationIsNotNull() {
            addCriterion("flag_high_risk_computation is not null");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationEqualTo(Integer value) {
            addCriterion("flag_high_risk_computation =", value, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationNotEqualTo(Integer value) {
            addCriterion("flag_high_risk_computation <>", value, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationGreaterThan(Integer value) {
            addCriterion("flag_high_risk_computation >", value, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_high_risk_computation >=", value, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationLessThan(Integer value) {
            addCriterion("flag_high_risk_computation <", value, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationLessThanOrEqualTo(Integer value) {
            addCriterion("flag_high_risk_computation <=", value, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationIn(List<Integer> values) {
            addCriterion("flag_high_risk_computation in", values, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationNotIn(List<Integer> values) {
            addCriterion("flag_high_risk_computation not in", values, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationBetween(Integer value1, Integer value2) {
            addCriterion("flag_high_risk_computation between", value1, value2, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagHighRiskComputationNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_high_risk_computation not between", value1, value2, "flagHighRiskComputation");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistIsNull() {
            addCriterion("flag_intellaudio_blacklist is null");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistIsNotNull() {
            addCriterion("flag_intellaudio_blacklist is not null");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistEqualTo(Integer value) {
            addCriterion("flag_intellaudio_blacklist =", value, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistNotEqualTo(Integer value) {
            addCriterion("flag_intellaudio_blacklist <>", value, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistGreaterThan(Integer value) {
            addCriterion("flag_intellaudio_blacklist >", value, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_intellaudio_blacklist >=", value, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistLessThan(Integer value) {
            addCriterion("flag_intellaudio_blacklist <", value, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistLessThanOrEqualTo(Integer value) {
            addCriterion("flag_intellaudio_blacklist <=", value, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistIn(List<Integer> values) {
            addCriterion("flag_intellaudio_blacklist in", values, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistNotIn(List<Integer> values) {
            addCriterion("flag_intellaudio_blacklist not in", values, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistBetween(Integer value1, Integer value2) {
            addCriterion("flag_intellaudio_blacklist between", value1, value2, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagIntellaudioBlacklistNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_intellaudio_blacklist not between", value1, value2, "flagIntellaudioBlacklist");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessIsNull() {
            addCriterion("flag_without_willingness is null");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessIsNotNull() {
            addCriterion("flag_without_willingness is not null");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessEqualTo(Integer value) {
            addCriterion("flag_without_willingness =", value, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessNotEqualTo(Integer value) {
            addCriterion("flag_without_willingness <>", value, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessGreaterThan(Integer value) {
            addCriterion("flag_without_willingness >", value, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_without_willingness >=", value, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessLessThan(Integer value) {
            addCriterion("flag_without_willingness <", value, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessLessThanOrEqualTo(Integer value) {
            addCriterion("flag_without_willingness <=", value, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessIn(List<Integer> values) {
            addCriterion("flag_without_willingness in", values, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessNotIn(List<Integer> values) {
            addCriterion("flag_without_willingness not in", values, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessBetween(Integer value1, Integer value2) {
            addCriterion("flag_without_willingness between", value1, value2, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagWithoutWillingnessNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_without_willingness not between", value1, value2, "flagWithoutWillingness");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationIsNull() {
            addCriterion("flag_blacklist_computation is null");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationIsNotNull() {
            addCriterion("flag_blacklist_computation is not null");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationEqualTo(Integer value) {
            addCriterion("flag_blacklist_computation =", value, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationNotEqualTo(Integer value) {
            addCriterion("flag_blacklist_computation <>", value, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationGreaterThan(Integer value) {
            addCriterion("flag_blacklist_computation >", value, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_blacklist_computation >=", value, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationLessThan(Integer value) {
            addCriterion("flag_blacklist_computation <", value, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationLessThanOrEqualTo(Integer value) {
            addCriterion("flag_blacklist_computation <=", value, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationIn(List<Integer> values) {
            addCriterion("flag_blacklist_computation in", values, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationNotIn(List<Integer> values) {
            addCriterion("flag_blacklist_computation not in", values, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationBetween(Integer value1, Integer value2) {
            addCriterion("flag_blacklist_computation between", value1, value2, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagBlacklistComputationNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_blacklist_computation not between", value1, value2, "flagBlacklistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistIsNull() {
            addCriterion("flag_score_whitelist is null");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistIsNotNull() {
            addCriterion("flag_score_whitelist is not null");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistEqualTo(Integer value) {
            addCriterion("flag_score_whitelist =", value, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistNotEqualTo(Integer value) {
            addCriterion("flag_score_whitelist <>", value, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistGreaterThan(Integer value) {
            addCriterion("flag_score_whitelist >", value, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_score_whitelist >=", value, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistLessThan(Integer value) {
            addCriterion("flag_score_whitelist <", value, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistLessThanOrEqualTo(Integer value) {
            addCriterion("flag_score_whitelist <=", value, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistIn(List<Integer> values) {
            addCriterion("flag_score_whitelist in", values, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistNotIn(List<Integer> values) {
            addCriterion("flag_score_whitelist not in", values, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistBetween(Integer value1, Integer value2) {
            addCriterion("flag_score_whitelist between", value1, value2, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagScoreWhitelistNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_score_whitelist not between", value1, value2, "flagScoreWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistIsNull() {
            addCriterion("flag_whitelist is null");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistIsNotNull() {
            addCriterion("flag_whitelist is not null");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistEqualTo(Integer value) {
            addCriterion("flag_whitelist =", value, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistNotEqualTo(Integer value) {
            addCriterion("flag_whitelist <>", value, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistGreaterThan(Integer value) {
            addCriterion("flag_whitelist >", value, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_whitelist >=", value, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistLessThan(Integer value) {
            addCriterion("flag_whitelist <", value, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistLessThanOrEqualTo(Integer value) {
            addCriterion("flag_whitelist <=", value, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistIn(List<Integer> values) {
            addCriterion("flag_whitelist in", values, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistNotIn(List<Integer> values) {
            addCriterion("flag_whitelist not in", values, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistBetween(Integer value1, Integer value2) {
            addCriterion("flag_whitelist between", value1, value2, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_whitelist not between", value1, value2, "flagWhitelist");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationIsNull() {
            addCriterion("flag_whitelist_computation is null");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationIsNotNull() {
            addCriterion("flag_whitelist_computation is not null");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationEqualTo(Integer value) {
            addCriterion("flag_whitelist_computation =", value, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationNotEqualTo(Integer value) {
            addCriterion("flag_whitelist_computation <>", value, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationGreaterThan(Integer value) {
            addCriterion("flag_whitelist_computation >", value, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationGreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_whitelist_computation >=", value, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationLessThan(Integer value) {
            addCriterion("flag_whitelist_computation <", value, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationLessThanOrEqualTo(Integer value) {
            addCriterion("flag_whitelist_computation <=", value, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationIn(List<Integer> values) {
            addCriterion("flag_whitelist_computation in", values, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationNotIn(List<Integer> values) {
            addCriterion("flag_whitelist_computation not in", values, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationBetween(Integer value1, Integer value2) {
            addCriterion("flag_whitelist_computation between", value1, value2, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andFlagWhitelistComputationNotBetween(Integer value1, Integer value2) {
            addCriterion("flag_whitelist_computation not between", value1, value2, "flagWhitelistComputation");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusIsNull() {
            addCriterion("es_sync_status is null");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusIsNotNull() {
            addCriterion("es_sync_status is not null");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusEqualTo(Integer value) {
            addCriterion("es_sync_status =", value, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusNotEqualTo(Integer value) {
            addCriterion("es_sync_status <>", value, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusGreaterThan(Integer value) {
            addCriterion("es_sync_status >", value, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("es_sync_status >=", value, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusLessThan(Integer value) {
            addCriterion("es_sync_status <", value, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusLessThanOrEqualTo(Integer value) {
            addCriterion("es_sync_status <=", value, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusIn(List<Integer> values) {
            addCriterion("es_sync_status in", values, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusNotIn(List<Integer> values) {
            addCriterion("es_sync_status not in", values, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusBetween(Integer value1, Integer value2) {
            addCriterion("es_sync_status between", value1, value2, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andEsSyncStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("es_sync_status not between", value1, value2, "esSyncStatus");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1IsNull() {
            addCriterion("flag_extend1 is null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1IsNotNull() {
            addCriterion("flag_extend1 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1EqualTo(Integer value) {
            addCriterion("flag_extend1 =", value, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1NotEqualTo(Integer value) {
            addCriterion("flag_extend1 <>", value, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1GreaterThan(Integer value) {
            addCriterion("flag_extend1 >", value, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_extend1 >=", value, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1LessThan(Integer value) {
            addCriterion("flag_extend1 <", value, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1LessThanOrEqualTo(Integer value) {
            addCriterion("flag_extend1 <=", value, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1In(List<Integer> values) {
            addCriterion("flag_extend1 in", values, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1NotIn(List<Integer> values) {
            addCriterion("flag_extend1 not in", values, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1Between(Integer value1, Integer value2) {
            addCriterion("flag_extend1 between", value1, value2, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend1NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_extend1 not between", value1, value2, "flagExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1IsNull() {
            addCriterion("flag_computation_extend1 is null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1IsNotNull() {
            addCriterion("flag_computation_extend1 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1EqualTo(Integer value) {
            addCriterion("flag_computation_extend1 =", value, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1NotEqualTo(Integer value) {
            addCriterion("flag_computation_extend1 <>", value, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1GreaterThan(Integer value) {
            addCriterion("flag_computation_extend1 >", value, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend1 >=", value, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1LessThan(Integer value) {
            addCriterion("flag_computation_extend1 <", value, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1LessThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend1 <=", value, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1In(List<Integer> values) {
            addCriterion("flag_computation_extend1 in", values, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1NotIn(List<Integer> values) {
            addCriterion("flag_computation_extend1 not in", values, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1Between(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend1 between", value1, value2, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend1NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend1 not between", value1, value2, "flagComputationExtend1");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2IsNull() {
            addCriterion("flag_extend2 is null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2IsNotNull() {
            addCriterion("flag_extend2 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2EqualTo(Integer value) {
            addCriterion("flag_extend2 =", value, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2NotEqualTo(Integer value) {
            addCriterion("flag_extend2 <>", value, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2GreaterThan(Integer value) {
            addCriterion("flag_extend2 >", value, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_extend2 >=", value, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2LessThan(Integer value) {
            addCriterion("flag_extend2 <", value, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2LessThanOrEqualTo(Integer value) {
            addCriterion("flag_extend2 <=", value, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2In(List<Integer> values) {
            addCriterion("flag_extend2 in", values, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2NotIn(List<Integer> values) {
            addCriterion("flag_extend2 not in", values, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2Between(Integer value1, Integer value2) {
            addCriterion("flag_extend2 between", value1, value2, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend2NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_extend2 not between", value1, value2, "flagExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2IsNull() {
            addCriterion("flag_computation_extend2 is null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2IsNotNull() {
            addCriterion("flag_computation_extend2 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2EqualTo(Integer value) {
            addCriterion("flag_computation_extend2 =", value, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2NotEqualTo(Integer value) {
            addCriterion("flag_computation_extend2 <>", value, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2GreaterThan(Integer value) {
            addCriterion("flag_computation_extend2 >", value, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend2 >=", value, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2LessThan(Integer value) {
            addCriterion("flag_computation_extend2 <", value, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2LessThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend2 <=", value, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2In(List<Integer> values) {
            addCriterion("flag_computation_extend2 in", values, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2NotIn(List<Integer> values) {
            addCriterion("flag_computation_extend2 not in", values, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2Between(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend2 between", value1, value2, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend2NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend2 not between", value1, value2, "flagComputationExtend2");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3IsNull() {
            addCriterion("flag_extend3 is null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3IsNotNull() {
            addCriterion("flag_extend3 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3EqualTo(Integer value) {
            addCriterion("flag_extend3 =", value, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3NotEqualTo(Integer value) {
            addCriterion("flag_extend3 <>", value, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3GreaterThan(Integer value) {
            addCriterion("flag_extend3 >", value, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_extend3 >=", value, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3LessThan(Integer value) {
            addCriterion("flag_extend3 <", value, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3LessThanOrEqualTo(Integer value) {
            addCriterion("flag_extend3 <=", value, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3In(List<Integer> values) {
            addCriterion("flag_extend3 in", values, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3NotIn(List<Integer> values) {
            addCriterion("flag_extend3 not in", values, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3Between(Integer value1, Integer value2) {
            addCriterion("flag_extend3 between", value1, value2, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend3NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_extend3 not between", value1, value2, "flagExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3IsNull() {
            addCriterion("flag_computation_extend3 is null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3IsNotNull() {
            addCriterion("flag_computation_extend3 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3EqualTo(Integer value) {
            addCriterion("flag_computation_extend3 =", value, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3NotEqualTo(Integer value) {
            addCriterion("flag_computation_extend3 <>", value, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3GreaterThan(Integer value) {
            addCriterion("flag_computation_extend3 >", value, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend3 >=", value, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3LessThan(Integer value) {
            addCriterion("flag_computation_extend3 <", value, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3LessThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend3 <=", value, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3In(List<Integer> values) {
            addCriterion("flag_computation_extend3 in", values, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3NotIn(List<Integer> values) {
            addCriterion("flag_computation_extend3 not in", values, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3Between(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend3 between", value1, value2, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend3NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend3 not between", value1, value2, "flagComputationExtend3");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4IsNull() {
            addCriterion("flag_extend4 is null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4IsNotNull() {
            addCriterion("flag_extend4 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4EqualTo(Integer value) {
            addCriterion("flag_extend4 =", value, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4NotEqualTo(Integer value) {
            addCriterion("flag_extend4 <>", value, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4GreaterThan(Integer value) {
            addCriterion("flag_extend4 >", value, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_extend4 >=", value, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4LessThan(Integer value) {
            addCriterion("flag_extend4 <", value, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4LessThanOrEqualTo(Integer value) {
            addCriterion("flag_extend4 <=", value, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4In(List<Integer> values) {
            addCriterion("flag_extend4 in", values, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4NotIn(List<Integer> values) {
            addCriterion("flag_extend4 not in", values, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4Between(Integer value1, Integer value2) {
            addCriterion("flag_extend4 between", value1, value2, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend4NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_extend4 not between", value1, value2, "flagExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4IsNull() {
            addCriterion("flag_computation_extend4 is null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4IsNotNull() {
            addCriterion("flag_computation_extend4 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4EqualTo(Integer value) {
            addCriterion("flag_computation_extend4 =", value, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4NotEqualTo(Integer value) {
            addCriterion("flag_computation_extend4 <>", value, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4GreaterThan(Integer value) {
            addCriterion("flag_computation_extend4 >", value, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend4 >=", value, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4LessThan(Integer value) {
            addCriterion("flag_computation_extend4 <", value, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4LessThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend4 <=", value, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4In(List<Integer> values) {
            addCriterion("flag_computation_extend4 in", values, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4NotIn(List<Integer> values) {
            addCriterion("flag_computation_extend4 not in", values, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4Between(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend4 between", value1, value2, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend4NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend4 not between", value1, value2, "flagComputationExtend4");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5IsNull() {
            addCriterion("flag_extend5 is null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5IsNotNull() {
            addCriterion("flag_extend5 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5EqualTo(Integer value) {
            addCriterion("flag_extend5 =", value, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5NotEqualTo(Integer value) {
            addCriterion("flag_extend5 <>", value, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5GreaterThan(Integer value) {
            addCriterion("flag_extend5 >", value, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_extend5 >=", value, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5LessThan(Integer value) {
            addCriterion("flag_extend5 <", value, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5LessThanOrEqualTo(Integer value) {
            addCriterion("flag_extend5 <=", value, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5In(List<Integer> values) {
            addCriterion("flag_extend5 in", values, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5NotIn(List<Integer> values) {
            addCriterion("flag_extend5 not in", values, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5Between(Integer value1, Integer value2) {
            addCriterion("flag_extend5 between", value1, value2, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend5NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_extend5 not between", value1, value2, "flagExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5IsNull() {
            addCriterion("flag_computation_extend5 is null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5IsNotNull() {
            addCriterion("flag_computation_extend5 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5EqualTo(Integer value) {
            addCriterion("flag_computation_extend5 =", value, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5NotEqualTo(Integer value) {
            addCriterion("flag_computation_extend5 <>", value, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5GreaterThan(Integer value) {
            addCriterion("flag_computation_extend5 >", value, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend5 >=", value, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5LessThan(Integer value) {
            addCriterion("flag_computation_extend5 <", value, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5LessThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend5 <=", value, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5In(List<Integer> values) {
            addCriterion("flag_computation_extend5 in", values, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5NotIn(List<Integer> values) {
            addCriterion("flag_computation_extend5 not in", values, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5Between(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend5 between", value1, value2, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend5NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend5 not between", value1, value2, "flagComputationExtend5");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6IsNull() {
            addCriterion("flag_extend6 is null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6IsNotNull() {
            addCriterion("flag_extend6 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6EqualTo(Integer value) {
            addCriterion("flag_extend6 =", value, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6NotEqualTo(Integer value) {
            addCriterion("flag_extend6 <>", value, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6GreaterThan(Integer value) {
            addCriterion("flag_extend6 >", value, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_extend6 >=", value, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6LessThan(Integer value) {
            addCriterion("flag_extend6 <", value, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6LessThanOrEqualTo(Integer value) {
            addCriterion("flag_extend6 <=", value, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6In(List<Integer> values) {
            addCriterion("flag_extend6 in", values, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6NotIn(List<Integer> values) {
            addCriterion("flag_extend6 not in", values, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6Between(Integer value1, Integer value2) {
            addCriterion("flag_extend6 between", value1, value2, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagExtend6NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_extend6 not between", value1, value2, "flagExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6IsNull() {
            addCriterion("flag_computation_extend6 is null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6IsNotNull() {
            addCriterion("flag_computation_extend6 is not null");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6EqualTo(Integer value) {
            addCriterion("flag_computation_extend6 =", value, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6NotEqualTo(Integer value) {
            addCriterion("flag_computation_extend6 <>", value, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6GreaterThan(Integer value) {
            addCriterion("flag_computation_extend6 >", value, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6GreaterThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend6 >=", value, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6LessThan(Integer value) {
            addCriterion("flag_computation_extend6 <", value, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6LessThanOrEqualTo(Integer value) {
            addCriterion("flag_computation_extend6 <=", value, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6In(List<Integer> values) {
            addCriterion("flag_computation_extend6 in", values, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6NotIn(List<Integer> values) {
            addCriterion("flag_computation_extend6 not in", values, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6Between(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend6 between", value1, value2, "flagComputationExtend6");
            return (Criteria) this;
        }

        public Criteria andFlagComputationExtend6NotBetween(Integer value1, Integer value2) {
            addCriterion("flag_computation_extend6 not between", value1, value2, "flagComputationExtend6");
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

        public Criteria andDataFlagMessageIsNull() {
            addCriterion("data_flag_message is null");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageIsNotNull() {
            addCriterion("data_flag_message is not null");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageEqualTo(String value) {
            addCriterion("data_flag_message =", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageNotEqualTo(String value) {
            addCriterion("data_flag_message <>", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageGreaterThan(String value) {
            addCriterion("data_flag_message >", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageGreaterThanOrEqualTo(String value) {
            addCriterion("data_flag_message >=", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageLessThan(String value) {
            addCriterion("data_flag_message <", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageLessThanOrEqualTo(String value) {
            addCriterion("data_flag_message <=", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageLike(String value) {
            addCriterion("data_flag_message like", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageNotLike(String value) {
            addCriterion("data_flag_message not like", value, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageIn(List<String> values) {
            addCriterion("data_flag_message in", values, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageNotIn(List<String> values) {
            addCriterion("data_flag_message not in", values, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageBetween(String value1, String value2) {
            addCriterion("data_flag_message between", value1, value2, "dataFlagMessage");
            return (Criteria) this;
        }

        public Criteria andDataFlagMessageNotBetween(String value1, String value2) {
            addCriterion("data_flag_message not between", value1, value2, "dataFlagMessage");
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

        public Criteria andCreateDateIsNull() {
            addCriterion("create_date is null");
            return (Criteria) this;
        }

        public Criteria andCreateDateIsNotNull() {
            addCriterion("create_date is not null");
            return (Criteria) this;
        }

        public Criteria andCreateDateEqualTo(Integer value) {
            addCriterion("create_date =", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotEqualTo(Integer value) {
            addCriterion("create_date <>", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThan(Integer value) {
            addCriterion("create_date >", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateGreaterThanOrEqualTo(Integer value) {
            addCriterion("create_date >=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThan(Integer value) {
            addCriterion("create_date <", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateLessThanOrEqualTo(Integer value) {
            addCriterion("create_date <=", value, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateIn(List<Integer> values) {
            addCriterion("create_date in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotIn(List<Integer> values) {
            addCriterion("create_date not in", values, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateBetween(Integer value1, Integer value2) {
            addCriterion("create_date between", value1, value2, "createDate");
            return (Criteria) this;
        }

        public Criteria andCreateDateNotBetween(Integer value1, Integer value2) {
            addCriterion("create_date not between", value1, value2, "createDate");
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