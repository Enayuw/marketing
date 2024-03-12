package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValidityPeriodResendRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ValidityPeriodResendRecordExample() {
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

        public Criteria andValidityPeriodIdIsNull() {
            addCriterion("validity_period_id is null");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdIsNotNull() {
            addCriterion("validity_period_id is not null");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdEqualTo(Long value) {
            addCriterion("validity_period_id =", value, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdNotEqualTo(Long value) {
            addCriterion("validity_period_id <>", value, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdGreaterThan(Long value) {
            addCriterion("validity_period_id >", value, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdGreaterThanOrEqualTo(Long value) {
            addCriterion("validity_period_id >=", value, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdLessThan(Long value) {
            addCriterion("validity_period_id <", value, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdLessThanOrEqualTo(Long value) {
            addCriterion("validity_period_id <=", value, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdIn(List<Long> values) {
            addCriterion("validity_period_id in", values, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdNotIn(List<Long> values) {
            addCriterion("validity_period_id not in", values, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdBetween(Long value1, Long value2) {
            addCriterion("validity_period_id between", value1, value2, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andValidityPeriodIdNotBetween(Long value1, Long value2) {
            addCriterion("validity_period_id not between", value1, value2, "validityPeriodId");
            return (Criteria) this;
        }

        public Criteria andResendTypeIsNull() {
            addCriterion("resend_type is null");
            return (Criteria) this;
        }

        public Criteria andResendTypeIsNotNull() {
            addCriterion("resend_type is not null");
            return (Criteria) this;
        }

        public Criteria andResendTypeEqualTo(Integer value) {
            addCriterion("resend_type =", value, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeNotEqualTo(Integer value) {
            addCriterion("resend_type <>", value, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeGreaterThan(Integer value) {
            addCriterion("resend_type >", value, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("resend_type >=", value, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeLessThan(Integer value) {
            addCriterion("resend_type <", value, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeLessThanOrEqualTo(Integer value) {
            addCriterion("resend_type <=", value, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeIn(List<Integer> values) {
            addCriterion("resend_type in", values, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeNotIn(List<Integer> values) {
            addCriterion("resend_type not in", values, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeBetween(Integer value1, Integer value2) {
            addCriterion("resend_type between", value1, value2, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("resend_type not between", value1, value2, "resendType");
            return (Criteria) this;
        }

        public Criteria andResendDataIsNull() {
            addCriterion("resend_data is null");
            return (Criteria) this;
        }

        public Criteria andResendDataIsNotNull() {
            addCriterion("resend_data is not null");
            return (Criteria) this;
        }

        public Criteria andResendDataEqualTo(String value) {
            addCriterion("resend_data =", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataNotEqualTo(String value) {
            addCriterion("resend_data <>", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataGreaterThan(String value) {
            addCriterion("resend_data >", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataGreaterThanOrEqualTo(String value) {
            addCriterion("resend_data >=", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataLessThan(String value) {
            addCriterion("resend_data <", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataLessThanOrEqualTo(String value) {
            addCriterion("resend_data <=", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataLike(String value) {
            addCriterion("resend_data like", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataNotLike(String value) {
            addCriterion("resend_data not like", value, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataIn(List<String> values) {
            addCriterion("resend_data in", values, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataNotIn(List<String> values) {
            addCriterion("resend_data not in", values, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataBetween(String value1, String value2) {
            addCriterion("resend_data between", value1, value2, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendDataNotBetween(String value1, String value2) {
            addCriterion("resend_data not between", value1, value2, "resendData");
            return (Criteria) this;
        }

        public Criteria andResendStatusIsNull() {
            addCriterion("resend_status is null");
            return (Criteria) this;
        }

        public Criteria andResendStatusIsNotNull() {
            addCriterion("resend_status is not null");
            return (Criteria) this;
        }

        public Criteria andResendStatusEqualTo(Integer value) {
            addCriterion("resend_status =", value, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusNotEqualTo(Integer value) {
            addCriterion("resend_status <>", value, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusGreaterThan(Integer value) {
            addCriterion("resend_status >", value, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("resend_status >=", value, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusLessThan(Integer value) {
            addCriterion("resend_status <", value, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusLessThanOrEqualTo(Integer value) {
            addCriterion("resend_status <=", value, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusIn(List<Integer> values) {
            addCriterion("resend_status in", values, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusNotIn(List<Integer> values) {
            addCriterion("resend_status not in", values, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusBetween(Integer value1, Integer value2) {
            addCriterion("resend_status between", value1, value2, "resendStatus");
            return (Criteria) this;
        }

        public Criteria andResendStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("resend_status not between", value1, value2, "resendStatus");
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