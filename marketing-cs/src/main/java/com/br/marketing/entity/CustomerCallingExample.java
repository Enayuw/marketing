package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerCallingExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CustomerCallingExample() {
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

        public Criteria andMessageIsNull() {
            addCriterion("message is null");
            return (Criteria) this;
        }

        public Criteria andMessageIsNotNull() {
            addCriterion("message is not null");
            return (Criteria) this;
        }

        public Criteria andMessageEqualTo(String value) {
            addCriterion("message =", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotEqualTo(String value) {
            addCriterion("message <>", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageGreaterThan(String value) {
            addCriterion("message >", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageGreaterThanOrEqualTo(String value) {
            addCriterion("message >=", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLessThan(String value) {
            addCriterion("message <", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLessThanOrEqualTo(String value) {
            addCriterion("message <=", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageLike(String value) {
            addCriterion("message like", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotLike(String value) {
            addCriterion("message not like", value, "message");
            return (Criteria) this;
        }

        public Criteria andMessageIn(List<String> values) {
            addCriterion("message in", values, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotIn(List<String> values) {
            addCriterion("message not in", values, "message");
            return (Criteria) this;
        }

        public Criteria andMessageBetween(String value1, String value2) {
            addCriterion("message between", value1, value2, "message");
            return (Criteria) this;
        }

        public Criteria andMessageNotBetween(String value1, String value2) {
            addCriterion("message not between", value1, value2, "message");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailIsNull() {
            addCriterion("columns_detail is null");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailIsNotNull() {
            addCriterion("columns_detail is not null");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailEqualTo(String value) {
            addCriterion("columns_detail =", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailNotEqualTo(String value) {
            addCriterion("columns_detail <>", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailGreaterThan(String value) {
            addCriterion("columns_detail >", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailGreaterThanOrEqualTo(String value) {
            addCriterion("columns_detail >=", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailLessThan(String value) {
            addCriterion("columns_detail <", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailLessThanOrEqualTo(String value) {
            addCriterion("columns_detail <=", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailLike(String value) {
            addCriterion("columns_detail like", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailNotLike(String value) {
            addCriterion("columns_detail not like", value, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailIn(List<String> values) {
            addCriterion("columns_detail in", values, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailNotIn(List<String> values) {
            addCriterion("columns_detail not in", values, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailBetween(String value1, String value2) {
            addCriterion("columns_detail between", value1, value2, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andColumnsDetailNotBetween(String value1, String value2) {
            addCriterion("columns_detail not between", value1, value2, "columnsDetail");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordIsNull() {
            addCriterion("sftp_password is null");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordIsNotNull() {
            addCriterion("sftp_password is not null");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordEqualTo(String value) {
            addCriterion("sftp_password =", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordNotEqualTo(String value) {
            addCriterion("sftp_password <>", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordGreaterThan(String value) {
            addCriterion("sftp_password >", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordGreaterThanOrEqualTo(String value) {
            addCriterion("sftp_password >=", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordLessThan(String value) {
            addCriterion("sftp_password <", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordLessThanOrEqualTo(String value) {
            addCriterion("sftp_password <=", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordLike(String value) {
            addCriterion("sftp_password like", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordNotLike(String value) {
            addCriterion("sftp_password not like", value, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordIn(List<String> values) {
            addCriterion("sftp_password in", values, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordNotIn(List<String> values) {
            addCriterion("sftp_password not in", values, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordBetween(String value1, String value2) {
            addCriterion("sftp_password between", value1, value2, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andSftpPasswordNotBetween(String value1, String value2) {
            addCriterion("sftp_password not between", value1, value2, "sftpPassword");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoIsNull() {
            addCriterion("extend_config_info is null");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoIsNotNull() {
            addCriterion("extend_config_info is not null");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoEqualTo(String value) {
            addCriterion("extend_config_info =", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotEqualTo(String value) {
            addCriterion("extend_config_info <>", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoGreaterThan(String value) {
            addCriterion("extend_config_info >", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoGreaterThanOrEqualTo(String value) {
            addCriterion("extend_config_info >=", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoLessThan(String value) {
            addCriterion("extend_config_info <", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoLessThanOrEqualTo(String value) {
            addCriterion("extend_config_info <=", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoLike(String value) {
            addCriterion("extend_config_info like", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotLike(String value) {
            addCriterion("extend_config_info not like", value, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoIn(List<String> values) {
            addCriterion("extend_config_info in", values, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotIn(List<String> values) {
            addCriterion("extend_config_info not in", values, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoBetween(String value1, String value2) {
            addCriterion("extend_config_info between", value1, value2, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andExtendConfigInfoNotBetween(String value1, String value2) {
            addCriterion("extend_config_info not between", value1, value2, "extendConfigInfo");
            return (Criteria) this;
        }

        public Criteria andConditionsIsNull() {
            addCriterion("conditions is null");
            return (Criteria) this;
        }

        public Criteria andConditionsIsNotNull() {
            addCriterion("conditions is not null");
            return (Criteria) this;
        }

        public Criteria andConditionsEqualTo(String value) {
            addCriterion("conditions =", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsNotEqualTo(String value) {
            addCriterion("conditions <>", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsGreaterThan(String value) {
            addCriterion("conditions >", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsGreaterThanOrEqualTo(String value) {
            addCriterion("conditions >=", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsLessThan(String value) {
            addCriterion("conditions <", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsLessThanOrEqualTo(String value) {
            addCriterion("conditions <=", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsLike(String value) {
            addCriterion("conditions like", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsNotLike(String value) {
            addCriterion("conditions not like", value, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsIn(List<String> values) {
            addCriterion("conditions in", values, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsNotIn(List<String> values) {
            addCriterion("conditions not in", values, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsBetween(String value1, String value2) {
            addCriterion("conditions between", value1, value2, "conditions");
            return (Criteria) this;
        }

        public Criteria andConditionsNotBetween(String value1, String value2) {
            addCriterion("conditions not between", value1, value2, "conditions");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumIsNull() {
            addCriterion("push_thread_num is null");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumIsNotNull() {
            addCriterion("push_thread_num is not null");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumEqualTo(Integer value) {
            addCriterion("push_thread_num =", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumNotEqualTo(Integer value) {
            addCriterion("push_thread_num <>", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumGreaterThan(Integer value) {
            addCriterion("push_thread_num >", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_thread_num >=", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumLessThan(Integer value) {
            addCriterion("push_thread_num <", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumLessThanOrEqualTo(Integer value) {
            addCriterion("push_thread_num <=", value, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumIn(List<Integer> values) {
            addCriterion("push_thread_num in", values, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumNotIn(List<Integer> values) {
            addCriterion("push_thread_num not in", values, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumBetween(Integer value1, Integer value2) {
            addCriterion("push_thread_num between", value1, value2, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushThreadNumNotBetween(Integer value1, Integer value2) {
            addCriterion("push_thread_num not between", value1, value2, "pushThreadNum");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNull() {
            addCriterion("push_type is null");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNotNull() {
            addCriterion("push_type is not null");
            return (Criteria) this;
        }

        public Criteria andPushTypeEqualTo(Integer value) {
            addCriterion("push_type =", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotEqualTo(Integer value) {
            addCriterion("push_type <>", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThan(Integer value) {
            addCriterion("push_type >", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_type >=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThan(Integer value) {
            addCriterion("push_type <", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThanOrEqualTo(Integer value) {
            addCriterion("push_type <=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeIn(List<Integer> values) {
            addCriterion("push_type in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotIn(List<Integer> values) {
            addCriterion("push_type not in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeBetween(Integer value1, Integer value2) {
            addCriterion("push_type between", value1, value2, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("push_type not between", value1, value2, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushUrlIsNull() {
            addCriterion("push_url is null");
            return (Criteria) this;
        }

        public Criteria andPushUrlIsNotNull() {
            addCriterion("push_url is not null");
            return (Criteria) this;
        }

        public Criteria andPushUrlEqualTo(String value) {
            addCriterion("push_url =", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotEqualTo(String value) {
            addCriterion("push_url <>", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlGreaterThan(String value) {
            addCriterion("push_url >", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlGreaterThanOrEqualTo(String value) {
            addCriterion("push_url >=", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlLessThan(String value) {
            addCriterion("push_url <", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlLessThanOrEqualTo(String value) {
            addCriterion("push_url <=", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlLike(String value) {
            addCriterion("push_url like", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotLike(String value) {
            addCriterion("push_url not like", value, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlIn(List<String> values) {
            addCriterion("push_url in", values, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotIn(List<String> values) {
            addCriterion("push_url not in", values, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlBetween(String value1, String value2) {
            addCriterion("push_url between", value1, value2, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andPushUrlNotBetween(String value1, String value2) {
            addCriterion("push_url not between", value1, value2, "pushUrl");
            return (Criteria) this;
        }

        public Criteria andSftpPathIsNull() {
            addCriterion("sftp_path is null");
            return (Criteria) this;
        }

        public Criteria andSftpPathIsNotNull() {
            addCriterion("sftp_path is not null");
            return (Criteria) this;
        }

        public Criteria andSftpPathEqualTo(String value) {
            addCriterion("sftp_path =", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathNotEqualTo(String value) {
            addCriterion("sftp_path <>", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathGreaterThan(String value) {
            addCriterion("sftp_path >", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathGreaterThanOrEqualTo(String value) {
            addCriterion("sftp_path >=", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathLessThan(String value) {
            addCriterion("sftp_path <", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathLessThanOrEqualTo(String value) {
            addCriterion("sftp_path <=", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathLike(String value) {
            addCriterion("sftp_path like", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathNotLike(String value) {
            addCriterion("sftp_path not like", value, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathIn(List<String> values) {
            addCriterion("sftp_path in", values, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathNotIn(List<String> values) {
            addCriterion("sftp_path not in", values, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathBetween(String value1, String value2) {
            addCriterion("sftp_path between", value1, value2, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpPathNotBetween(String value1, String value2) {
            addCriterion("sftp_path not between", value1, value2, "sftpPath");
            return (Criteria) this;
        }

        public Criteria andSftpNameIsNull() {
            addCriterion("sftp_name is null");
            return (Criteria) this;
        }

        public Criteria andSftpNameIsNotNull() {
            addCriterion("sftp_name is not null");
            return (Criteria) this;
        }

        public Criteria andSftpNameEqualTo(String value) {
            addCriterion("sftp_name =", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameNotEqualTo(String value) {
            addCriterion("sftp_name <>", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameGreaterThan(String value) {
            addCriterion("sftp_name >", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameGreaterThanOrEqualTo(String value) {
            addCriterion("sftp_name >=", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameLessThan(String value) {
            addCriterion("sftp_name <", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameLessThanOrEqualTo(String value) {
            addCriterion("sftp_name <=", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameLike(String value) {
            addCriterion("sftp_name like", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameNotLike(String value) {
            addCriterion("sftp_name not like", value, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameIn(List<String> values) {
            addCriterion("sftp_name in", values, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameNotIn(List<String> values) {
            addCriterion("sftp_name not in", values, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameBetween(String value1, String value2) {
            addCriterion("sftp_name between", value1, value2, "sftpName");
            return (Criteria) this;
        }

        public Criteria andSftpNameNotBetween(String value1, String value2) {
            addCriterion("sftp_name not between", value1, value2, "sftpName");
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

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
            addCriterion("status not between", value1, value2, "status");
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