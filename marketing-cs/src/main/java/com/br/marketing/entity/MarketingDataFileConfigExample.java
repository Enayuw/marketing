package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingDataFileConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingDataFileConfigExample() {
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

        public Criteria andFileIdIsNull() {
            addCriterion("file_id is null");
            return (Criteria) this;
        }

        public Criteria andFileIdIsNotNull() {
            addCriterion("file_id is not null");
            return (Criteria) this;
        }

        public Criteria andFileIdEqualTo(Integer value) {
            addCriterion("file_id =", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotEqualTo(Integer value) {
            addCriterion("file_id <>", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThan(Integer value) {
            addCriterion("file_id >", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("file_id >=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThan(Integer value) {
            addCriterion("file_id <", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdLessThanOrEqualTo(Integer value) {
            addCriterion("file_id <=", value, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdIn(List<Integer> values) {
            addCriterion("file_id in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotIn(List<Integer> values) {
            addCriterion("file_id not in", values, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdBetween(Integer value1, Integer value2) {
            addCriterion("file_id between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andFileIdNotBetween(Integer value1, Integer value2) {
            addCriterion("file_id not between", value1, value2, "fileId");
            return (Criteria) this;
        }

        public Criteria andCleanTypeIsNull() {
            addCriterion("clean_type is null");
            return (Criteria) this;
        }

        public Criteria andCleanTypeIsNotNull() {
            addCriterion("clean_type is not null");
            return (Criteria) this;
        }

        public Criteria andCleanTypeEqualTo(Integer value) {
            addCriterion("clean_type =", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeNotEqualTo(Integer value) {
            addCriterion("clean_type <>", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeGreaterThan(Integer value) {
            addCriterion("clean_type >", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("clean_type >=", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeLessThan(Integer value) {
            addCriterion("clean_type <", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeLessThanOrEqualTo(Integer value) {
            addCriterion("clean_type <=", value, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeIn(List<Integer> values) {
            addCriterion("clean_type in", values, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeNotIn(List<Integer> values) {
            addCriterion("clean_type not in", values, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeBetween(Integer value1, Integer value2) {
            addCriterion("clean_type between", value1, value2, "cleanType");
            return (Criteria) this;
        }

        public Criteria andCleanTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("clean_type not between", value1, value2, "cleanType");
            return (Criteria) this;
        }

        public Criteria andRuleNameIsNull() {
            addCriterion("rule_name is null");
            return (Criteria) this;
        }

        public Criteria andRuleNameIsNotNull() {
            addCriterion("rule_name is not null");
            return (Criteria) this;
        }

        public Criteria andRuleNameEqualTo(String value) {
            addCriterion("rule_name =", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotEqualTo(String value) {
            addCriterion("rule_name <>", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameGreaterThan(String value) {
            addCriterion("rule_name >", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameGreaterThanOrEqualTo(String value) {
            addCriterion("rule_name >=", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLessThan(String value) {
            addCriterion("rule_name <", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLessThanOrEqualTo(String value) {
            addCriterion("rule_name <=", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameLike(String value) {
            addCriterion("rule_name like", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotLike(String value) {
            addCriterion("rule_name not like", value, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameIn(List<String> values) {
            addCriterion("rule_name in", values, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotIn(List<String> values) {
            addCriterion("rule_name not in", values, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameBetween(String value1, String value2) {
            addCriterion("rule_name between", value1, value2, "ruleName");
            return (Criteria) this;
        }

        public Criteria andRuleNameNotBetween(String value1, String value2) {
            addCriterion("rule_name not between", value1, value2, "ruleName");
            return (Criteria) this;
        }

        public Criteria andFieldConfigIsNull() {
            addCriterion("field_config is null");
            return (Criteria) this;
        }

        public Criteria andFieldConfigIsNotNull() {
            addCriterion("field_config is not null");
            return (Criteria) this;
        }

        public Criteria andFieldConfigEqualTo(String value) {
            addCriterion("field_config =", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigNotEqualTo(String value) {
            addCriterion("field_config <>", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigGreaterThan(String value) {
            addCriterion("field_config >", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigGreaterThanOrEqualTo(String value) {
            addCriterion("field_config >=", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigLessThan(String value) {
            addCriterion("field_config <", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigLessThanOrEqualTo(String value) {
            addCriterion("field_config <=", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigLike(String value) {
            addCriterion("field_config like", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigNotLike(String value) {
            addCriterion("field_config not like", value, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigIn(List<String> values) {
            addCriterion("field_config in", values, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigNotIn(List<String> values) {
            addCriterion("field_config not in", values, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigBetween(String value1, String value2) {
            addCriterion("field_config between", value1, value2, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigNotBetween(String value1, String value2) {
            addCriterion("field_config not between", value1, value2, "fieldConfig");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowIsNull() {
            addCriterion("field_config_show is null");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowIsNotNull() {
            addCriterion("field_config_show is not null");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowEqualTo(String value) {
            addCriterion("field_config_show =", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowNotEqualTo(String value) {
            addCriterion("field_config_show <>", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowGreaterThan(String value) {
            addCriterion("field_config_show >", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowGreaterThanOrEqualTo(String value) {
            addCriterion("field_config_show >=", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowLessThan(String value) {
            addCriterion("field_config_show <", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowLessThanOrEqualTo(String value) {
            addCriterion("field_config_show <=", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowLike(String value) {
            addCriterion("field_config_show like", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowNotLike(String value) {
            addCriterion("field_config_show not like", value, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowIn(List<String> values) {
            addCriterion("field_config_show in", values, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowNotIn(List<String> values) {
            addCriterion("field_config_show not in", values, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowBetween(String value1, String value2) {
            addCriterion("field_config_show between", value1, value2, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andFieldConfigShowNotBetween(String value1, String value2) {
            addCriterion("field_config_show not between", value1, value2, "fieldConfigShow");
            return (Criteria) this;
        }

        public Criteria andServiceNameIsNull() {
            addCriterion("service_name is null");
            return (Criteria) this;
        }

        public Criteria andServiceNameIsNotNull() {
            addCriterion("service_name is not null");
            return (Criteria) this;
        }

        public Criteria andServiceNameEqualTo(String value) {
            addCriterion("service_name =", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameNotEqualTo(String value) {
            addCriterion("service_name <>", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameGreaterThan(String value) {
            addCriterion("service_name >", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameGreaterThanOrEqualTo(String value) {
            addCriterion("service_name >=", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameLessThan(String value) {
            addCriterion("service_name <", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameLessThanOrEqualTo(String value) {
            addCriterion("service_name <=", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameLike(String value) {
            addCriterion("service_name like", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameNotLike(String value) {
            addCriterion("service_name not like", value, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameIn(List<String> values) {
            addCriterion("service_name in", values, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameNotIn(List<String> values) {
            addCriterion("service_name not in", values, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameBetween(String value1, String value2) {
            addCriterion("service_name between", value1, value2, "serviceName");
            return (Criteria) this;
        }

        public Criteria andServiceNameNotBetween(String value1, String value2) {
            addCriterion("service_name not between", value1, value2, "serviceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameIsNull() {
            addCriterion("transfer_service_name is null");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameIsNotNull() {
            addCriterion("transfer_service_name is not null");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameEqualTo(String value) {
            addCriterion("transfer_service_name =", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameNotEqualTo(String value) {
            addCriterion("transfer_service_name <>", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameGreaterThan(String value) {
            addCriterion("transfer_service_name >", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameGreaterThanOrEqualTo(String value) {
            addCriterion("transfer_service_name >=", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameLessThan(String value) {
            addCriterion("transfer_service_name <", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameLessThanOrEqualTo(String value) {
            addCriterion("transfer_service_name <=", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameLike(String value) {
            addCriterion("transfer_service_name like", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameNotLike(String value) {
            addCriterion("transfer_service_name not like", value, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameIn(List<String> values) {
            addCriterion("transfer_service_name in", values, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameNotIn(List<String> values) {
            addCriterion("transfer_service_name not in", values, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameBetween(String value1, String value2) {
            addCriterion("transfer_service_name between", value1, value2, "transferServiceName");
            return (Criteria) this;
        }

        public Criteria andTransferServiceNameNotBetween(String value1, String value2) {
            addCriterion("transfer_service_name not between", value1, value2, "transferServiceName");
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

        public Criteria andIsChecklistNameIsNull() {
            addCriterion("is_checklist_name is null");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameIsNotNull() {
            addCriterion("is_checklist_name is not null");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameEqualTo(Byte value) {
            addCriterion("is_checklist_name =", value, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameNotEqualTo(Byte value) {
            addCriterion("is_checklist_name <>", value, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameGreaterThan(Byte value) {
            addCriterion("is_checklist_name >", value, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameGreaterThanOrEqualTo(Byte value) {
            addCriterion("is_checklist_name >=", value, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameLessThan(Byte value) {
            addCriterion("is_checklist_name <", value, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameLessThanOrEqualTo(Byte value) {
            addCriterion("is_checklist_name <=", value, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameIn(List<Byte> values) {
            addCriterion("is_checklist_name in", values, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameNotIn(List<Byte> values) {
            addCriterion("is_checklist_name not in", values, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameBetween(Byte value1, Byte value2) {
            addCriterion("is_checklist_name between", value1, value2, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andIsChecklistNameNotBetween(Byte value1, Byte value2) {
            addCriterion("is_checklist_name not between", value1, value2, "isChecklistName");
            return (Criteria) this;
        }

        public Criteria andValidationRulesIsNull() {
            addCriterion("validation_rules is null");
            return (Criteria) this;
        }

        public Criteria andValidationRulesIsNotNull() {
            addCriterion("validation_rules is not null");
            return (Criteria) this;
        }

        public Criteria andValidationRulesEqualTo(String value) {
            addCriterion("validation_rules =", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesNotEqualTo(String value) {
            addCriterion("validation_rules <>", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesGreaterThan(String value) {
            addCriterion("validation_rules >", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesGreaterThanOrEqualTo(String value) {
            addCriterion("validation_rules >=", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesLessThan(String value) {
            addCriterion("validation_rules <", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesLessThanOrEqualTo(String value) {
            addCriterion("validation_rules <=", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesLike(String value) {
            addCriterion("validation_rules like", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesNotLike(String value) {
            addCriterion("validation_rules not like", value, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesIn(List<String> values) {
            addCriterion("validation_rules in", values, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesNotIn(List<String> values) {
            addCriterion("validation_rules not in", values, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesBetween(String value1, String value2) {
            addCriterion("validation_rules between", value1, value2, "validationRules");
            return (Criteria) this;
        }

        public Criteria andValidationRulesNotBetween(String value1, String value2) {
            addCriterion("validation_rules not between", value1, value2, "validationRules");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameIsNull() {
            addCriterion("auto_table_name is null");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameIsNotNull() {
            addCriterion("auto_table_name is not null");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameEqualTo(String value) {
            addCriterion("auto_table_name =", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameNotEqualTo(String value) {
            addCriterion("auto_table_name <>", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameGreaterThan(String value) {
            addCriterion("auto_table_name >", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameGreaterThanOrEqualTo(String value) {
            addCriterion("auto_table_name >=", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameLessThan(String value) {
            addCriterion("auto_table_name <", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameLessThanOrEqualTo(String value) {
            addCriterion("auto_table_name <=", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameLike(String value) {
            addCriterion("auto_table_name like", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameNotLike(String value) {
            addCriterion("auto_table_name not like", value, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameIn(List<String> values) {
            addCriterion("auto_table_name in", values, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameNotIn(List<String> values) {
            addCriterion("auto_table_name not in", values, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameBetween(String value1, String value2) {
            addCriterion("auto_table_name between", value1, value2, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoTableNameNotBetween(String value1, String value2) {
            addCriterion("auto_table_name not between", value1, value2, "autoTableName");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnIsNull() {
            addCriterion("auto_duplicate_column is null");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnIsNotNull() {
            addCriterion("auto_duplicate_column is not null");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnEqualTo(String value) {
            addCriterion("auto_duplicate_column =", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnNotEqualTo(String value) {
            addCriterion("auto_duplicate_column <>", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnGreaterThan(String value) {
            addCriterion("auto_duplicate_column >", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnGreaterThanOrEqualTo(String value) {
            addCriterion("auto_duplicate_column >=", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnLessThan(String value) {
            addCriterion("auto_duplicate_column <", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnLessThanOrEqualTo(String value) {
            addCriterion("auto_duplicate_column <=", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnLike(String value) {
            addCriterion("auto_duplicate_column like", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnNotLike(String value) {
            addCriterion("auto_duplicate_column not like", value, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnIn(List<String> values) {
            addCriterion("auto_duplicate_column in", values, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnNotIn(List<String> values) {
            addCriterion("auto_duplicate_column not in", values, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnBetween(String value1, String value2) {
            addCriterion("auto_duplicate_column between", value1, value2, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoDuplicateColumnNotBetween(String value1, String value2) {
            addCriterion("auto_duplicate_column not between", value1, value2, "autoDuplicateColumn");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlIsNull() {
            addCriterion("auto_search_data_sql is null");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlIsNotNull() {
            addCriterion("auto_search_data_sql is not null");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlEqualTo(String value) {
            addCriterion("auto_search_data_sql =", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlNotEqualTo(String value) {
            addCriterion("auto_search_data_sql <>", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlGreaterThan(String value) {
            addCriterion("auto_search_data_sql >", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlGreaterThanOrEqualTo(String value) {
            addCriterion("auto_search_data_sql >=", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlLessThan(String value) {
            addCriterion("auto_search_data_sql <", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlLessThanOrEqualTo(String value) {
            addCriterion("auto_search_data_sql <=", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlLike(String value) {
            addCriterion("auto_search_data_sql like", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlNotLike(String value) {
            addCriterion("auto_search_data_sql not like", value, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlIn(List<String> values) {
            addCriterion("auto_search_data_sql in", values, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlNotIn(List<String> values) {
            addCriterion("auto_search_data_sql not in", values, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlBetween(String value1, String value2) {
            addCriterion("auto_search_data_sql between", value1, value2, "autoSearchDataSql");
            return (Criteria) this;
        }

        public Criteria andAutoSearchDataSqlNotBetween(String value1, String value2) {
            addCriterion("auto_search_data_sql not between", value1, value2, "autoSearchDataSql");
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