package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.List;

public class TaikangDingDingDataRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public TaikangDingDingDataRecordExample() {
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

        public Criteria andBrowseDateIsNull() {
            addCriterion("browse_date is null");
            return (Criteria) this;
        }

        public Criteria andBrowseDateIsNotNull() {
            addCriterion("browse_date is not null");
            return (Criteria) this;
        }

        public Criteria andBrowseDateEqualTo(String value) {
            addCriterion("browse_date =", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateNotEqualTo(String value) {
            addCriterion("browse_date <>", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateGreaterThan(String value) {
            addCriterion("browse_date >", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateGreaterThanOrEqualTo(String value) {
            addCriterion("browse_date >=", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateLessThan(String value) {
            addCriterion("browse_date <", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateLessThanOrEqualTo(String value) {
            addCriterion("browse_date <=", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateLike(String value) {
            addCriterion("browse_date like", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateNotLike(String value) {
            addCriterion("browse_date not like", value, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateIn(List<String> values) {
            addCriterion("browse_date in", values, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateNotIn(List<String> values) {
            addCriterion("browse_date not in", values, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateBetween(String value1, String value2) {
            addCriterion("browse_date between", value1, value2, "browseDate");
            return (Criteria) this;
        }

        public Criteria andBrowseDateNotBetween(String value1, String value2) {
            addCriterion("browse_date not between", value1, value2, "browseDate");
            return (Criteria) this;
        }

        public Criteria andApplicationNameIsNull() {
            addCriterion("application_name is null");
            return (Criteria) this;
        }

        public Criteria andApplicationNameIsNotNull() {
            addCriterion("application_name is not null");
            return (Criteria) this;
        }

        public Criteria andApplicationNameEqualTo(String value) {
            addCriterion("application_name =", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameNotEqualTo(String value) {
            addCriterion("application_name <>", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameGreaterThan(String value) {
            addCriterion("application_name >", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameGreaterThanOrEqualTo(String value) {
            addCriterion("application_name >=", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameLessThan(String value) {
            addCriterion("application_name <", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameLessThanOrEqualTo(String value) {
            addCriterion("application_name <=", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameLike(String value) {
            addCriterion("application_name like", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameNotLike(String value) {
            addCriterion("application_name not like", value, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameIn(List<String> values) {
            addCriterion("application_name in", values, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameNotIn(List<String> values) {
            addCriterion("application_name not in", values, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameBetween(String value1, String value2) {
            addCriterion("application_name between", value1, value2, "applicationName");
            return (Criteria) this;
        }

        public Criteria andApplicationNameNotBetween(String value1, String value2) {
            addCriterion("application_name not between", value1, value2, "applicationName");
            return (Criteria) this;
        }

        public Criteria andReturnResult1IsNull() {
            addCriterion("return_result1 is null");
            return (Criteria) this;
        }

        public Criteria andReturnResult1IsNotNull() {
            addCriterion("return_result1 is not null");
            return (Criteria) this;
        }

        public Criteria andReturnResult1EqualTo(String value) {
            addCriterion("return_result1 =", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1NotEqualTo(String value) {
            addCriterion("return_result1 <>", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1GreaterThan(String value) {
            addCriterion("return_result1 >", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1GreaterThanOrEqualTo(String value) {
            addCriterion("return_result1 >=", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1LessThan(String value) {
            addCriterion("return_result1 <", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1LessThanOrEqualTo(String value) {
            addCriterion("return_result1 <=", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1Like(String value) {
            addCriterion("return_result1 like", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1NotLike(String value) {
            addCriterion("return_result1 not like", value, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1In(List<String> values) {
            addCriterion("return_result1 in", values, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1NotIn(List<String> values) {
            addCriterion("return_result1 not in", values, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1Between(String value1, String value2) {
            addCriterion("return_result1 between", value1, value2, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andReturnResult1NotBetween(String value1, String value2) {
            addCriterion("return_result1 not between", value1, value2, "returnResult1");
            return (Criteria) this;
        }

        public Criteria andCreatedByIsNull() {
            addCriterion("created_by is null");
            return (Criteria) this;
        }

        public Criteria andCreatedByIsNotNull() {
            addCriterion("created_by is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedByEqualTo(String value) {
            addCriterion("created_by =", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotEqualTo(String value) {
            addCriterion("created_by <>", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByGreaterThan(String value) {
            addCriterion("created_by >", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByGreaterThanOrEqualTo(String value) {
            addCriterion("created_by >=", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLessThan(String value) {
            addCriterion("created_by <", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLessThanOrEqualTo(String value) {
            addCriterion("created_by <=", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByLike(String value) {
            addCriterion("created_by like", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotLike(String value) {
            addCriterion("created_by not like", value, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByIn(List<String> values) {
            addCriterion("created_by in", values, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotIn(List<String> values) {
            addCriterion("created_by not in", values, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByBetween(String value1, String value2) {
            addCriterion("created_by between", value1, value2, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedByNotBetween(String value1, String value2) {
            addCriterion("created_by not between", value1, value2, "createdBy");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIsNull() {
            addCriterion("created_time is null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIsNotNull() {
            addCriterion("created_time is not null");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeEqualTo(String value) {
            addCriterion("created_time =", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotEqualTo(String value) {
            addCriterion("created_time <>", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThan(String value) {
            addCriterion("created_time >", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeGreaterThanOrEqualTo(String value) {
            addCriterion("created_time >=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThan(String value) {
            addCriterion("created_time <", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLessThanOrEqualTo(String value) {
            addCriterion("created_time <=", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeLike(String value) {
            addCriterion("created_time like", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotLike(String value) {
            addCriterion("created_time not like", value, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeIn(List<String> values) {
            addCriterion("created_time in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotIn(List<String> values) {
            addCriterion("created_time not in", values, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeBetween(String value1, String value2) {
            addCriterion("created_time between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andCreatedTimeNotBetween(String value1, String value2) {
            addCriterion("created_time not between", value1, value2, "createdTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByIsNull() {
            addCriterion("last_modified_by is null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByIsNotNull() {
            addCriterion("last_modified_by is not null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByEqualTo(String value) {
            addCriterion("last_modified_by =", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByNotEqualTo(String value) {
            addCriterion("last_modified_by <>", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByGreaterThan(String value) {
            addCriterion("last_modified_by >", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByGreaterThanOrEqualTo(String value) {
            addCriterion("last_modified_by >=", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByLessThan(String value) {
            addCriterion("last_modified_by <", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByLessThanOrEqualTo(String value) {
            addCriterion("last_modified_by <=", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByLike(String value) {
            addCriterion("last_modified_by like", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByNotLike(String value) {
            addCriterion("last_modified_by not like", value, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByIn(List<String> values) {
            addCriterion("last_modified_by in", values, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByNotIn(List<String> values) {
            addCriterion("last_modified_by not in", values, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByBetween(String value1, String value2) {
            addCriterion("last_modified_by between", value1, value2, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedByNotBetween(String value1, String value2) {
            addCriterion("last_modified_by not between", value1, value2, "lastModifiedBy");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeIsNull() {
            addCriterion("last_modified_time is null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeIsNotNull() {
            addCriterion("last_modified_time is not null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeEqualTo(String value) {
            addCriterion("last_modified_time =", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeNotEqualTo(String value) {
            addCriterion("last_modified_time <>", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeGreaterThan(String value) {
            addCriterion("last_modified_time >", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeGreaterThanOrEqualTo(String value) {
            addCriterion("last_modified_time >=", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeLessThan(String value) {
            addCriterion("last_modified_time <", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeLessThanOrEqualTo(String value) {
            addCriterion("last_modified_time <=", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeLike(String value) {
            addCriterion("last_modified_time like", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeNotLike(String value) {
            addCriterion("last_modified_time not like", value, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeIn(List<String> values) {
            addCriterion("last_modified_time in", values, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeNotIn(List<String> values) {
            addCriterion("last_modified_time not in", values, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeBetween(String value1, String value2) {
            addCriterion("last_modified_time between", value1, value2, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedTimeNotBetween(String value1, String value2) {
            addCriterion("last_modified_time not between", value1, value2, "lastModifiedTime");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdIsNull() {
            addCriterion("last_modified_user_id is null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdIsNotNull() {
            addCriterion("last_modified_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdEqualTo(String value) {
            addCriterion("last_modified_user_id =", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdNotEqualTo(String value) {
            addCriterion("last_modified_user_id <>", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdGreaterThan(String value) {
            addCriterion("last_modified_user_id >", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdGreaterThanOrEqualTo(String value) {
            addCriterion("last_modified_user_id >=", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdLessThan(String value) {
            addCriterion("last_modified_user_id <", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdLessThanOrEqualTo(String value) {
            addCriterion("last_modified_user_id <=", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdLike(String value) {
            addCriterion("last_modified_user_id like", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdNotLike(String value) {
            addCriterion("last_modified_user_id not like", value, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdIn(List<String> values) {
            addCriterion("last_modified_user_id in", values, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdNotIn(List<String> values) {
            addCriterion("last_modified_user_id not in", values, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdBetween(String value1, String value2) {
            addCriterion("last_modified_user_id between", value1, value2, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserIdNotBetween(String value1, String value2) {
            addCriterion("last_modified_user_id not between", value1, value2, "lastModifiedUserId");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameIsNull() {
            addCriterion("last_modified_user_name is null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameIsNotNull() {
            addCriterion("last_modified_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameEqualTo(String value) {
            addCriterion("last_modified_user_name =", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameNotEqualTo(String value) {
            addCriterion("last_modified_user_name <>", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameGreaterThan(String value) {
            addCriterion("last_modified_user_name >", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("last_modified_user_name >=", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameLessThan(String value) {
            addCriterion("last_modified_user_name <", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameLessThanOrEqualTo(String value) {
            addCriterion("last_modified_user_name <=", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameLike(String value) {
            addCriterion("last_modified_user_name like", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameNotLike(String value) {
            addCriterion("last_modified_user_name not like", value, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameIn(List<String> values) {
            addCriterion("last_modified_user_name in", values, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameNotIn(List<String> values) {
            addCriterion("last_modified_user_name not in", values, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameBetween(String value1, String value2) {
            addCriterion("last_modified_user_name between", value1, value2, "lastModifiedUserName");
            return (Criteria) this;
        }

        public Criteria andLastModifiedUserNameNotBetween(String value1, String value2) {
            addCriterion("last_modified_user_name not between", value1, value2, "lastModifiedUserName");
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