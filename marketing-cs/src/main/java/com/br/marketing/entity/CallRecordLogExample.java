package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CallRecordLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CallRecordLogExample() {
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

        public Criteria andRecordIdIsNull() {
            addCriterion("record_id is null");
            return (Criteria) this;
        }

        public Criteria andRecordIdIsNotNull() {
            addCriterion("record_id is not null");
            return (Criteria) this;
        }

        public Criteria andRecordIdEqualTo(Long value) {
            addCriterion("record_id =", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdNotEqualTo(Long value) {
            addCriterion("record_id <>", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdGreaterThan(Long value) {
            addCriterion("record_id >", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdGreaterThanOrEqualTo(Long value) {
            addCriterion("record_id >=", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdLessThan(Long value) {
            addCriterion("record_id <", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdLessThanOrEqualTo(Long value) {
            addCriterion("record_id <=", value, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdIn(List<Long> values) {
            addCriterion("record_id in", values, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdNotIn(List<Long> values) {
            addCriterion("record_id not in", values, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdBetween(Long value1, Long value2) {
            addCriterion("record_id between", value1, value2, "recordId");
            return (Criteria) this;
        }

        public Criteria andRecordIdNotBetween(Long value1, Long value2) {
            addCriterion("record_id not between", value1, value2, "recordId");
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

        public Criteria andCaseNumIsNull() {
            addCriterion("case_num is null");
            return (Criteria) this;
        }

        public Criteria andCaseNumIsNotNull() {
            addCriterion("case_num is not null");
            return (Criteria) this;
        }

        public Criteria andCaseNumEqualTo(String value) {
            addCriterion("case_num =", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotEqualTo(String value) {
            addCriterion("case_num <>", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumGreaterThan(String value) {
            addCriterion("case_num >", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumGreaterThanOrEqualTo(String value) {
            addCriterion("case_num >=", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumLessThan(String value) {
            addCriterion("case_num <", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumLessThanOrEqualTo(String value) {
            addCriterion("case_num <=", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumLike(String value) {
            addCriterion("case_num like", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotLike(String value) {
            addCriterion("case_num not like", value, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumIn(List<String> values) {
            addCriterion("case_num in", values, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotIn(List<String> values) {
            addCriterion("case_num not in", values, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumBetween(String value1, String value2) {
            addCriterion("case_num between", value1, value2, "caseNum");
            return (Criteria) this;
        }

        public Criteria andCaseNumNotBetween(String value1, String value2) {
            addCriterion("case_num not between", value1, value2, "caseNum");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeIsNull() {
            addCriterion("intention_grade is null");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeIsNotNull() {
            addCriterion("intention_grade is not null");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeEqualTo(String value) {
            addCriterion("intention_grade =", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotEqualTo(String value) {
            addCriterion("intention_grade <>", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeGreaterThan(String value) {
            addCriterion("intention_grade >", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeGreaterThanOrEqualTo(String value) {
            addCriterion("intention_grade >=", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeLessThan(String value) {
            addCriterion("intention_grade <", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeLessThanOrEqualTo(String value) {
            addCriterion("intention_grade <=", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeLike(String value) {
            addCriterion("intention_grade like", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotLike(String value) {
            addCriterion("intention_grade not like", value, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeIn(List<String> values) {
            addCriterion("intention_grade in", values, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotIn(List<String> values) {
            addCriterion("intention_grade not in", values, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeBetween(String value1, String value2) {
            addCriterion("intention_grade between", value1, value2, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andIntentionGradeNotBetween(String value1, String value2) {
            addCriterion("intention_grade not between", value1, value2, "intentionGrade");
            return (Criteria) this;
        }

        public Criteria andInboundStatusIsNull() {
            addCriterion("inbound_status is null");
            return (Criteria) this;
        }

        public Criteria andInboundStatusIsNotNull() {
            addCriterion("inbound_status is not null");
            return (Criteria) this;
        }

        public Criteria andInboundStatusEqualTo(Integer value) {
            addCriterion("inbound_status =", value, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusNotEqualTo(Integer value) {
            addCriterion("inbound_status <>", value, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusGreaterThan(Integer value) {
            addCriterion("inbound_status >", value, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("inbound_status >=", value, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusLessThan(Integer value) {
            addCriterion("inbound_status <", value, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusLessThanOrEqualTo(Integer value) {
            addCriterion("inbound_status <=", value, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusIn(List<Integer> values) {
            addCriterion("inbound_status in", values, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusNotIn(List<Integer> values) {
            addCriterion("inbound_status not in", values, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusBetween(Integer value1, Integer value2) {
            addCriterion("inbound_status between", value1, value2, "inboundStatus");
            return (Criteria) this;
        }

        public Criteria andInboundStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("inbound_status not between", value1, value2, "inboundStatus");
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

        public Criteria andErrorMessageIsNull() {
            addCriterion("error_message is null");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIsNotNull() {
            addCriterion("error_message is not null");
            return (Criteria) this;
        }

        public Criteria andErrorMessageEqualTo(String value) {
            addCriterion("error_message =", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotEqualTo(String value) {
            addCriterion("error_message <>", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageGreaterThan(String value) {
            addCriterion("error_message >", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageGreaterThanOrEqualTo(String value) {
            addCriterion("error_message >=", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLessThan(String value) {
            addCriterion("error_message <", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLessThanOrEqualTo(String value) {
            addCriterion("error_message <=", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageLike(String value) {
            addCriterion("error_message like", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotLike(String value) {
            addCriterion("error_message not like", value, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageIn(List<String> values) {
            addCriterion("error_message in", values, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotIn(List<String> values) {
            addCriterion("error_message not in", values, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageBetween(String value1, String value2) {
            addCriterion("error_message between", value1, value2, "errorMessage");
            return (Criteria) this;
        }

        public Criteria andErrorMessageNotBetween(String value1, String value2) {
            addCriterion("error_message not between", value1, value2, "errorMessage");
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