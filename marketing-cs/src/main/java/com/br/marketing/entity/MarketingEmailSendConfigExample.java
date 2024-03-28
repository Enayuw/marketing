package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingEmailSendConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingEmailSendConfigExample() {
        oredCriteria = new ArrayList<>();
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
            criteria = new ArrayList<>();
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

        public Criteria andSubjectIsNull() {
            addCriterion("subject is null");
            return (Criteria) this;
        }

        public Criteria andSubjectIsNotNull() {
            addCriterion("subject is not null");
            return (Criteria) this;
        }

        public Criteria andSubjectEqualTo(Integer value) {
            addCriterion("subject =", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectNotEqualTo(Integer value) {
            addCriterion("subject <>", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectGreaterThan(Integer value) {
            addCriterion("subject >", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectGreaterThanOrEqualTo(Integer value) {
            addCriterion("subject >=", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectLessThan(Integer value) {
            addCriterion("subject <", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectLessThanOrEqualTo(Integer value) {
            addCriterion("subject <=", value, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectIn(List<Integer> values) {
            addCriterion("subject in", values, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectNotIn(List<Integer> values) {
            addCriterion("subject not in", values, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectBetween(Integer value1, Integer value2) {
            addCriterion("subject between", value1, value2, "subject");
            return (Criteria) this;
        }

        public Criteria andSubjectNotBetween(Integer value1, Integer value2) {
            addCriterion("subject not between", value1, value2, "subject");
            return (Criteria) this;
        }

        public Criteria andReceiverUserIsNull() {
            addCriterion("receiver_user is null");
            return (Criteria) this;
        }

        public Criteria andReceiverUserIsNotNull() {
            addCriterion("receiver_user is not null");
            return (Criteria) this;
        }

        public Criteria andReceiverUserEqualTo(String value) {
            addCriterion("receiver_user =", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserNotEqualTo(String value) {
            addCriterion("receiver_user <>", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserGreaterThan(String value) {
            addCriterion("receiver_user >", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserGreaterThanOrEqualTo(String value) {
            addCriterion("receiver_user >=", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserLessThan(String value) {
            addCriterion("receiver_user <", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserLessThanOrEqualTo(String value) {
            addCriterion("receiver_user <=", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserLike(String value) {
            addCriterion("receiver_user like", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserNotLike(String value) {
            addCriterion("receiver_user not like", value, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserIn(List<String> values) {
            addCriterion("receiver_user in", values, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserNotIn(List<String> values) {
            addCriterion("receiver_user not in", values, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserBetween(String value1, String value2) {
            addCriterion("receiver_user between", value1, value2, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andReceiverUserNotBetween(String value1, String value2) {
            addCriterion("receiver_user not between", value1, value2, "receiverUser");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentIsNull() {
            addCriterion("is_attachment is null");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentIsNotNull() {
            addCriterion("is_attachment is not null");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentEqualTo(Integer value) {
            addCriterion("is_attachment =", value, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentNotEqualTo(Integer value) {
            addCriterion("is_attachment <>", value, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentGreaterThan(Integer value) {
            addCriterion("is_attachment >", value, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_attachment >=", value, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentLessThan(Integer value) {
            addCriterion("is_attachment <", value, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentLessThanOrEqualTo(Integer value) {
            addCriterion("is_attachment <=", value, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentIn(List<Integer> values) {
            addCriterion("is_attachment in", values, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentNotIn(List<Integer> values) {
            addCriterion("is_attachment not in", values, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentBetween(Integer value1, Integer value2) {
            addCriterion("is_attachment between", value1, value2, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andIsAttachmentNotBetween(Integer value1, Integer value2) {
            addCriterion("is_attachment not between", value1, value2, "isAttachment");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameIsNull() {
            addCriterion("attachment_file_name is null");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameIsNotNull() {
            addCriterion("attachment_file_name is not null");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameEqualTo(String value) {
            addCriterion("attachment_file_name =", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameNotEqualTo(String value) {
            addCriterion("attachment_file_name <>", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameGreaterThan(String value) {
            addCriterion("attachment_file_name >", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameGreaterThanOrEqualTo(String value) {
            addCriterion("attachment_file_name >=", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameLessThan(String value) {
            addCriterion("attachment_file_name <", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameLessThanOrEqualTo(String value) {
            addCriterion("attachment_file_name <=", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameLike(String value) {
            addCriterion("attachment_file_name like", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameNotLike(String value) {
            addCriterion("attachment_file_name not like", value, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameIn(List<String> values) {
            addCriterion("attachment_file_name in", values, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameNotIn(List<String> values) {
            addCriterion("attachment_file_name not in", values, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameBetween(String value1, String value2) {
            addCriterion("attachment_file_name between", value1, value2, "attachmentFileName");
            return (Criteria) this;
        }

        public Criteria andAttachmentFileNameNotBetween(String value1, String value2) {
            addCriterion("attachment_file_name not between", value1, value2, "attachmentFileName");
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

    /**
     */
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