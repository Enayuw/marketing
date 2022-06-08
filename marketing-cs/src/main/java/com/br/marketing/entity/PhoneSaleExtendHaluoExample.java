package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PhoneSaleExtendHaluoExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public PhoneSaleExtendHaluoExample() {
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

        public Criteria andPIdIsNull() {
            addCriterion("p_id is null");
            return (Criteria) this;
        }

        public Criteria andPIdIsNotNull() {
            addCriterion("p_id is not null");
            return (Criteria) this;
        }

        public Criteria andPIdEqualTo(Long value) {
            addCriterion("p_id =", value, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdNotEqualTo(Long value) {
            addCriterion("p_id <>", value, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdGreaterThan(Long value) {
            addCriterion("p_id >", value, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdGreaterThanOrEqualTo(Long value) {
            addCriterion("p_id >=", value, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdLessThan(Long value) {
            addCriterion("p_id <", value, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdLessThanOrEqualTo(Long value) {
            addCriterion("p_id <=", value, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdIn(List<Long> values) {
            addCriterion("p_id in", values, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdNotIn(List<Long> values) {
            addCriterion("p_id not in", values, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdBetween(Long value1, Long value2) {
            addCriterion("p_id between", value1, value2, "pId");
            return (Criteria) this;
        }

        public Criteria andPIdNotBetween(Long value1, Long value2) {
            addCriterion("p_id not between", value1, value2, "pId");
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

        public Criteria andTaskIdIsNull() {
            addCriterion("task_id is null");
            return (Criteria) this;
        }

        public Criteria andTaskIdIsNotNull() {
            addCriterion("task_id is not null");
            return (Criteria) this;
        }

        public Criteria andTaskIdEqualTo(String value) {
            addCriterion("task_id =", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotEqualTo(String value) {
            addCriterion("task_id <>", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThan(String value) {
            addCriterion("task_id >", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdGreaterThanOrEqualTo(String value) {
            addCriterion("task_id >=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThan(String value) {
            addCriterion("task_id <", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLessThanOrEqualTo(String value) {
            addCriterion("task_id <=", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdLike(String value) {
            addCriterion("task_id like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotLike(String value) {
            addCriterion("task_id not like", value, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdIn(List<String> values) {
            addCriterion("task_id in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotIn(List<String> values) {
            addCriterion("task_id not in", values, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdBetween(String value1, String value2) {
            addCriterion("task_id between", value1, value2, "taskId");
            return (Criteria) this;
        }

        public Criteria andTaskIdNotBetween(String value1, String value2) {
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

        public Criteria andAppletTimeIsNull() {
            addCriterion("applet_time is null");
            return (Criteria) this;
        }

        public Criteria andAppletTimeIsNotNull() {
            addCriterion("applet_time is not null");
            return (Criteria) this;
        }

        public Criteria andAppletTimeEqualTo(String value) {
            addCriterion("applet_time =", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeNotEqualTo(String value) {
            addCriterion("applet_time <>", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeGreaterThan(String value) {
            addCriterion("applet_time >", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeGreaterThanOrEqualTo(String value) {
            addCriterion("applet_time >=", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeLessThan(String value) {
            addCriterion("applet_time <", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeLessThanOrEqualTo(String value) {
            addCriterion("applet_time <=", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeLike(String value) {
            addCriterion("applet_time like", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeNotLike(String value) {
            addCriterion("applet_time not like", value, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeIn(List<String> values) {
            addCriterion("applet_time in", values, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeNotIn(List<String> values) {
            addCriterion("applet_time not in", values, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeBetween(String value1, String value2) {
            addCriterion("applet_time between", value1, value2, "appletTime");
            return (Criteria) this;
        }

        public Criteria andAppletTimeNotBetween(String value1, String value2) {
            addCriterion("applet_time not between", value1, value2, "appletTime");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andPStatusIsNull() {
            addCriterion("p_status is null");
            return (Criteria) this;
        }

        public Criteria andPStatusIsNotNull() {
            addCriterion("p_status is not null");
            return (Criteria) this;
        }

        public Criteria andPStatusEqualTo(Integer value) {
            addCriterion("p_status =", value, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusNotEqualTo(Integer value) {
            addCriterion("p_status <>", value, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusGreaterThan(Integer value) {
            addCriterion("p_status >", value, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("p_status >=", value, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusLessThan(Integer value) {
            addCriterion("p_status <", value, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusLessThanOrEqualTo(Integer value) {
            addCriterion("p_status <=", value, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusIn(List<Integer> values) {
            addCriterion("p_status in", values, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusNotIn(List<Integer> values) {
            addCriterion("p_status not in", values, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusBetween(Integer value1, Integer value2) {
            addCriterion("p_status between", value1, value2, "pStatus");
            return (Criteria) this;
        }

        public Criteria andPStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("p_status not between", value1, value2, "pStatus");
            return (Criteria) this;
        }

        public Criteria andSourceIdIsNull() {
            addCriterion("source_id is null");
            return (Criteria) this;
        }

        public Criteria andSourceIdIsNotNull() {
            addCriterion("source_id is not null");
            return (Criteria) this;
        }

        public Criteria andSourceIdEqualTo(Long value) {
            addCriterion("source_id =", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdNotEqualTo(Long value) {
            addCriterion("source_id <>", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdGreaterThan(Long value) {
            addCriterion("source_id >", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdGreaterThanOrEqualTo(Long value) {
            addCriterion("source_id >=", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdLessThan(Long value) {
            addCriterion("source_id <", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdLessThanOrEqualTo(Long value) {
            addCriterion("source_id <=", value, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdIn(List<Long> values) {
            addCriterion("source_id in", values, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdNotIn(List<Long> values) {
            addCriterion("source_id not in", values, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdBetween(Long value1, Long value2) {
            addCriterion("source_id between", value1, value2, "sourceId");
            return (Criteria) this;
        }

        public Criteria andSourceIdNotBetween(Long value1, Long value2) {
            addCriterion("source_id not between", value1, value2, "sourceId");
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