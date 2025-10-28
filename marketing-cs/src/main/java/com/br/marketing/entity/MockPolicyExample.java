package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MockPolicyExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MockPolicyExample() {
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

        public Criteria andMockNameIsNull() {
            addCriterion("mock_name is null");
            return (Criteria) this;
        }

        public Criteria andMockNameIsNotNull() {
            addCriterion("mock_name is not null");
            return (Criteria) this;
        }

        public Criteria andMockNameEqualTo(String value) {
            addCriterion("mock_name =", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameNotEqualTo(String value) {
            addCriterion("mock_name <>", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameGreaterThan(String value) {
            addCriterion("mock_name >", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameGreaterThanOrEqualTo(String value) {
            addCriterion("mock_name >=", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameLessThan(String value) {
            addCriterion("mock_name <", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameLessThanOrEqualTo(String value) {
            addCriterion("mock_name <=", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameLike(String value) {
            addCriterion("mock_name like", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameNotLike(String value) {
            addCriterion("mock_name not like", value, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameIn(List<String> values) {
            addCriterion("mock_name in", values, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameNotIn(List<String> values) {
            addCriterion("mock_name not in", values, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameBetween(String value1, String value2) {
            addCriterion("mock_name between", value1, value2, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockNameNotBetween(String value1, String value2) {
            addCriterion("mock_name not between", value1, value2, "mockName");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeIsNull() {
            addCriterion("mock_policy_type is null");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeIsNotNull() {
            addCriterion("mock_policy_type is not null");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeEqualTo(Integer value) {
            addCriterion("mock_policy_type =", value, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeNotEqualTo(Integer value) {
            addCriterion("mock_policy_type <>", value, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeGreaterThan(Integer value) {
            addCriterion("mock_policy_type >", value, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("mock_policy_type >=", value, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeLessThan(Integer value) {
            addCriterion("mock_policy_type <", value, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeLessThanOrEqualTo(Integer value) {
            addCriterion("mock_policy_type <=", value, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeIn(List<Integer> values) {
            addCriterion("mock_policy_type in", values, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeNotIn(List<Integer> values) {
            addCriterion("mock_policy_type not in", values, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeBetween(Integer value1, Integer value2) {
            addCriterion("mock_policy_type between", value1, value2, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andMockPolicyTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("mock_policy_type not between", value1, value2, "mockPolicyType");
            return (Criteria) this;
        }

        public Criteria andEnabledIsNull() {
            addCriterion("enabled is null");
            return (Criteria) this;
        }

        public Criteria andEnabledIsNotNull() {
            addCriterion("enabled is not null");
            return (Criteria) this;
        }

        public Criteria andEnabledEqualTo(Integer value) {
            addCriterion("enabled =", value, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledNotEqualTo(Integer value) {
            addCriterion("enabled <>", value, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledGreaterThan(Integer value) {
            addCriterion("enabled >", value, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledGreaterThanOrEqualTo(Integer value) {
            addCriterion("enabled >=", value, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledLessThan(Integer value) {
            addCriterion("enabled <", value, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledLessThanOrEqualTo(Integer value) {
            addCriterion("enabled <=", value, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledIn(List<Integer> values) {
            addCriterion("enabled in", values, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledNotIn(List<Integer> values) {
            addCriterion("enabled not in", values, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledBetween(Integer value1, Integer value2) {
            addCriterion("enabled between", value1, value2, "enabled");
            return (Criteria) this;
        }

        public Criteria andEnabledNotBetween(Integer value1, Integer value2) {
            addCriterion("enabled not between", value1, value2, "enabled");
            return (Criteria) this;
        }

        public Criteria andVersionIsNull() {
            addCriterion("version is null");
            return (Criteria) this;
        }

        public Criteria andVersionIsNotNull() {
            addCriterion("version is not null");
            return (Criteria) this;
        }

        public Criteria andVersionEqualTo(String value) {
            addCriterion("version =", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotEqualTo(String value) {
            addCriterion("version <>", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThan(String value) {
            addCriterion("version >", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionGreaterThanOrEqualTo(String value) {
            addCriterion("version >=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThan(String value) {
            addCriterion("version <", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLessThanOrEqualTo(String value) {
            addCriterion("version <=", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionLike(String value) {
            addCriterion("version like", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotLike(String value) {
            addCriterion("version not like", value, "version");
            return (Criteria) this;
        }

        public Criteria andVersionIn(List<String> values) {
            addCriterion("version in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotIn(List<String> values) {
            addCriterion("version not in", values, "version");
            return (Criteria) this;
        }

        public Criteria andVersionBetween(String value1, String value2) {
            addCriterion("version between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andVersionNotBetween(String value1, String value2) {
            addCriterion("version not between", value1, value2, "version");
            return (Criteria) this;
        }

        public Criteria andDescriptionIsNull() {
            addCriterion("description is null");
            return (Criteria) this;
        }

        public Criteria andDescriptionIsNotNull() {
            addCriterion("description is not null");
            return (Criteria) this;
        }

        public Criteria andDescriptionEqualTo(String value) {
            addCriterion("description =", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotEqualTo(String value) {
            addCriterion("description <>", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThan(String value) {
            addCriterion("description >", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionGreaterThanOrEqualTo(String value) {
            addCriterion("description >=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThan(String value) {
            addCriterion("description <", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLessThanOrEqualTo(String value) {
            addCriterion("description <=", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionLike(String value) {
            addCriterion("description like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotLike(String value) {
            addCriterion("description not like", value, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionIn(List<String> values) {
            addCriterion("description in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotIn(List<String> values) {
            addCriterion("description not in", values, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionBetween(String value1, String value2) {
            addCriterion("description between", value1, value2, "description");
            return (Criteria) this;
        }

        public Criteria andDescriptionNotBetween(String value1, String value2) {
            addCriterion("description not between", value1, value2, "description");
            return (Criteria) this;
        }

        public Criteria andOptUserIdIsNull() {
            addCriterion("opt_user_id is null");
            return (Criteria) this;
        }

        public Criteria andOptUserIdIsNotNull() {
            addCriterion("opt_user_id is not null");
            return (Criteria) this;
        }

        public Criteria andOptUserIdEqualTo(Long value) {
            addCriterion("opt_user_id =", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotEqualTo(Long value) {
            addCriterion("opt_user_id <>", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdGreaterThan(Long value) {
            addCriterion("opt_user_id >", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdGreaterThanOrEqualTo(Long value) {
            addCriterion("opt_user_id >=", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdLessThan(Long value) {
            addCriterion("opt_user_id <", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdLessThanOrEqualTo(Long value) {
            addCriterion("opt_user_id <=", value, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdIn(List<Long> values) {
            addCriterion("opt_user_id in", values, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotIn(List<Long> values) {
            addCriterion("opt_user_id not in", values, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdBetween(Long value1, Long value2) {
            addCriterion("opt_user_id between", value1, value2, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserIdNotBetween(Long value1, Long value2) {
            addCriterion("opt_user_id not between", value1, value2, "optUserId");
            return (Criteria) this;
        }

        public Criteria andOptUserNameIsNull() {
            addCriterion("opt_user_name is null");
            return (Criteria) this;
        }

        public Criteria andOptUserNameIsNotNull() {
            addCriterion("opt_user_name is not null");
            return (Criteria) this;
        }

        public Criteria andOptUserNameEqualTo(String value) {
            addCriterion("opt_user_name =", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotEqualTo(String value) {
            addCriterion("opt_user_name <>", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameGreaterThan(String value) {
            addCriterion("opt_user_name >", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameGreaterThanOrEqualTo(String value) {
            addCriterion("opt_user_name >=", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameLessThan(String value) {
            addCriterion("opt_user_name <", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameLessThanOrEqualTo(String value) {
            addCriterion("opt_user_name <=", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameLike(String value) {
            addCriterion("opt_user_name like", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotLike(String value) {
            addCriterion("opt_user_name not like", value, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameIn(List<String> values) {
            addCriterion("opt_user_name in", values, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotIn(List<String> values) {
            addCriterion("opt_user_name not in", values, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameBetween(String value1, String value2) {
            addCriterion("opt_user_name between", value1, value2, "optUserName");
            return (Criteria) this;
        }

        public Criteria andOptUserNameNotBetween(String value1, String value2) {
            addCriterion("opt_user_name not between", value1, value2, "optUserName");
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