package com.br.marketing.entity.wuba;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WuBaAiConversionDataExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public WuBaAiConversionDataExample() {
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

        public Criteria andDataIdIsNull() {
            addCriterion("data_id is null");
            return (Criteria) this;
        }

        public Criteria andDataIdIsNotNull() {
            addCriterion("data_id is not null");
            return (Criteria) this;
        }

        public Criteria andDataIdEqualTo(String value) {
            addCriterion("data_id =", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdNotEqualTo(String value) {
            addCriterion("data_id <>", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdGreaterThan(String value) {
            addCriterion("data_id >", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdGreaterThanOrEqualTo(String value) {
            addCriterion("data_id >=", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdLessThan(String value) {
            addCriterion("data_id <", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdLessThanOrEqualTo(String value) {
            addCriterion("data_id <=", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdLike(String value) {
            addCriterion("data_id like", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdNotLike(String value) {
            addCriterion("data_id not like", value, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdIn(List<String> values) {
            addCriterion("data_id in", values, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdNotIn(List<String> values) {
            addCriterion("data_id not in", values, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdBetween(String value1, String value2) {
            addCriterion("data_id between", value1, value2, "dataId");
            return (Criteria) this;
        }

        public Criteria andDataIdNotBetween(String value1, String value2) {
            addCriterion("data_id not between", value1, value2, "dataId");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeIsNull() {
            addCriterion("dw_event_time is null");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeIsNotNull() {
            addCriterion("dw_event_time is not null");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeEqualTo(String value) {
            addCriterion("dw_event_time =", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeNotEqualTo(String value) {
            addCriterion("dw_event_time <>", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeGreaterThan(String value) {
            addCriterion("dw_event_time >", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeGreaterThanOrEqualTo(String value) {
            addCriterion("dw_event_time >=", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeLessThan(String value) {
            addCriterion("dw_event_time <", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeLessThanOrEqualTo(String value) {
            addCriterion("dw_event_time <=", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeLike(String value) {
            addCriterion("dw_event_time like", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeNotLike(String value) {
            addCriterion("dw_event_time not like", value, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeIn(List<String> values) {
            addCriterion("dw_event_time in", values, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeNotIn(List<String> values) {
            addCriterion("dw_event_time not in", values, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeBetween(String value1, String value2) {
            addCriterion("dw_event_time between", value1, value2, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andDwEventTimeNotBetween(String value1, String value2) {
            addCriterion("dw_event_time not between", value1, value2, "dwEventTime");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptIsNull() {
            addCriterion("mobile_encrypt is null");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptIsNotNull() {
            addCriterion("mobile_encrypt is not null");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptEqualTo(String value) {
            addCriterion("mobile_encrypt =", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptNotEqualTo(String value) {
            addCriterion("mobile_encrypt <>", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptGreaterThan(String value) {
            addCriterion("mobile_encrypt >", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptGreaterThanOrEqualTo(String value) {
            addCriterion("mobile_encrypt >=", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptLessThan(String value) {
            addCriterion("mobile_encrypt <", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptLessThanOrEqualTo(String value) {
            addCriterion("mobile_encrypt <=", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptLike(String value) {
            addCriterion("mobile_encrypt like", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptNotLike(String value) {
            addCriterion("mobile_encrypt not like", value, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptIn(List<String> values) {
            addCriterion("mobile_encrypt in", values, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptNotIn(List<String> values) {
            addCriterion("mobile_encrypt not in", values, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptBetween(String value1, String value2) {
            addCriterion("mobile_encrypt between", value1, value2, "mobileEncrypt");
            return (Criteria) this;
        }

        public Criteria andMobileEncryptNotBetween(String value1, String value2) {
            addCriterion("mobile_encrypt not between", value1, value2, "mobileEncrypt");
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

        public Criteria andDebtApplyTimeIsNull() {
            addCriterion("debt_apply_time is null");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeIsNotNull() {
            addCriterion("debt_apply_time is not null");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeEqualTo(String value) {
            addCriterion("debt_apply_time =", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeNotEqualTo(String value) {
            addCriterion("debt_apply_time <>", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeGreaterThan(String value) {
            addCriterion("debt_apply_time >", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeGreaterThanOrEqualTo(String value) {
            addCriterion("debt_apply_time >=", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeLessThan(String value) {
            addCriterion("debt_apply_time <", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeLessThanOrEqualTo(String value) {
            addCriterion("debt_apply_time <=", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeLike(String value) {
            addCriterion("debt_apply_time like", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeNotLike(String value) {
            addCriterion("debt_apply_time not like", value, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeIn(List<String> values) {
            addCriterion("debt_apply_time in", values, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeNotIn(List<String> values) {
            addCriterion("debt_apply_time not in", values, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeBetween(String value1, String value2) {
            addCriterion("debt_apply_time between", value1, value2, "debtApplyTime");
            return (Criteria) this;
        }

        public Criteria andDebtApplyTimeNotBetween(String value1, String value2) {
            addCriterion("debt_apply_time not between", value1, value2, "debtApplyTime");
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

        public Criteria andExpireDateIsNull() {
            addCriterion("expire_date is null");
            return (Criteria) this;
        }

        public Criteria andExpireDateIsNotNull() {
            addCriterion("expire_date is not null");
            return (Criteria) this;
        }

        public Criteria andExpireDateEqualTo(String value) {
            addCriterion("expire_date =", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateNotEqualTo(String value) {
            addCriterion("expire_date <>", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateGreaterThan(String value) {
            addCriterion("expire_date >", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateGreaterThanOrEqualTo(String value) {
            addCriterion("expire_date >=", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateLessThan(String value) {
            addCriterion("expire_date <", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateLessThanOrEqualTo(String value) {
            addCriterion("expire_date <=", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateLike(String value) {
            addCriterion("expire_date like", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateNotLike(String value) {
            addCriterion("expire_date not like", value, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateIn(List<String> values) {
            addCriterion("expire_date in", values, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateNotIn(List<String> values) {
            addCriterion("expire_date not in", values, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateBetween(String value1, String value2) {
            addCriterion("expire_date between", value1, value2, "expireDate");
            return (Criteria) this;
        }

        public Criteria andExpireDateNotBetween(String value1, String value2) {
            addCriterion("expire_date not between", value1, value2, "expireDate");
            return (Criteria) this;
        }

        public Criteria andInversionStatusIsNull() {
            addCriterion("inversion_status is null");
            return (Criteria) this;
        }

        public Criteria andInversionStatusIsNotNull() {
            addCriterion("inversion_status is not null");
            return (Criteria) this;
        }

        public Criteria andInversionStatusEqualTo(String value) {
            addCriterion("inversion_status =", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusNotEqualTo(String value) {
            addCriterion("inversion_status <>", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusGreaterThan(String value) {
            addCriterion("inversion_status >", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusGreaterThanOrEqualTo(String value) {
            addCriterion("inversion_status >=", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusLessThan(String value) {
            addCriterion("inversion_status <", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusLessThanOrEqualTo(String value) {
            addCriterion("inversion_status <=", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusLike(String value) {
            addCriterion("inversion_status like", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusNotLike(String value) {
            addCriterion("inversion_status not like", value, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusIn(List<String> values) {
            addCriterion("inversion_status in", values, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusNotIn(List<String> values) {
            addCriterion("inversion_status not in", values, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusBetween(String value1, String value2) {
            addCriterion("inversion_status between", value1, value2, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andInversionStatusNotBetween(String value1, String value2) {
            addCriterion("inversion_status not between", value1, value2, "inversionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusIsNull() {
            addCriterion("push_decision_status is null");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusIsNotNull() {
            addCriterion("push_decision_status is not null");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusEqualTo(String value) {
            addCriterion("push_decision_status =", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusNotEqualTo(String value) {
            addCriterion("push_decision_status <>", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusGreaterThan(String value) {
            addCriterion("push_decision_status >", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusGreaterThanOrEqualTo(String value) {
            addCriterion("push_decision_status >=", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusLessThan(String value) {
            addCriterion("push_decision_status <", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusLessThanOrEqualTo(String value) {
            addCriterion("push_decision_status <=", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusLike(String value) {
            addCriterion("push_decision_status like", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusNotLike(String value) {
            addCriterion("push_decision_status not like", value, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusIn(List<String> values) {
            addCriterion("push_decision_status in", values, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusNotIn(List<String> values) {
            addCriterion("push_decision_status not in", values, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusBetween(String value1, String value2) {
            addCriterion("push_decision_status between", value1, value2, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andPushDecisionStatusNotBetween(String value1, String value2) {
            addCriterion("push_decision_status not between", value1, value2, "pushDecisionStatus");
            return (Criteria) this;
        }

        public Criteria andReserveFieldIsNull() {
            addCriterion("reserve_field is null");
            return (Criteria) this;
        }

        public Criteria andReserveFieldIsNotNull() {
            addCriterion("reserve_field is not null");
            return (Criteria) this;
        }

        public Criteria andReserveFieldEqualTo(String value) {
            addCriterion("reserve_field =", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldNotEqualTo(String value) {
            addCriterion("reserve_field <>", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldGreaterThan(String value) {
            addCriterion("reserve_field >", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldGreaterThanOrEqualTo(String value) {
            addCriterion("reserve_field >=", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldLessThan(String value) {
            addCriterion("reserve_field <", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldLessThanOrEqualTo(String value) {
            addCriterion("reserve_field <=", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldLike(String value) {
            addCriterion("reserve_field like", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldNotLike(String value) {
            addCriterion("reserve_field not like", value, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldIn(List<String> values) {
            addCriterion("reserve_field in", values, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldNotIn(List<String> values) {
            addCriterion("reserve_field not in", values, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldBetween(String value1, String value2) {
            addCriterion("reserve_field between", value1, value2, "reserveField");
            return (Criteria) this;
        }

        public Criteria andReserveFieldNotBetween(String value1, String value2) {
            addCriterion("reserve_field not between", value1, value2, "reserveField");
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