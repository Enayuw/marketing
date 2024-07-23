package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class XieChengCollidingDataLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public XieChengCollidingDataLogExample() {
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

        public Criteria andSmsCollidingDataIdIsNull() {
            addCriterion("sms_colliding_data_id is null");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdIsNotNull() {
            addCriterion("sms_colliding_data_id is not null");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdEqualTo(Long value) {
            addCriterion("sms_colliding_data_id =", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdNotEqualTo(Long value) {
            addCriterion("sms_colliding_data_id <>", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdGreaterThan(Long value) {
            addCriterion("sms_colliding_data_id >", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sms_colliding_data_id >=", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdLessThan(Long value) {
            addCriterion("sms_colliding_data_id <", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdLessThanOrEqualTo(Long value) {
            addCriterion("sms_colliding_data_id <=", value, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdIn(List<Long> values) {
            addCriterion("sms_colliding_data_id in", values, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdNotIn(List<Long> values) {
            addCriterion("sms_colliding_data_id not in", values, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdBetween(Long value1, Long value2) {
            addCriterion("sms_colliding_data_id between", value1, value2, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andSmsCollidingDataIdNotBetween(Long value1, Long value2) {
            addCriterion("sms_colliding_data_id not between", value1, value2, "smsCollidingDataId");
            return (Criteria) this;
        }

        public Criteria andPackageIdIsNull() {
            addCriterion("package_id is null");
            return (Criteria) this;
        }

        public Criteria andPackageIdIsNotNull() {
            addCriterion("package_id is not null");
            return (Criteria) this;
        }

        public Criteria andPackageIdEqualTo(Long value) {
            addCriterion("package_id =", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdNotEqualTo(Long value) {
            addCriterion("package_id <>", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdGreaterThan(Long value) {
            addCriterion("package_id >", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdGreaterThanOrEqualTo(Long value) {
            addCriterion("package_id >=", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdLessThan(Long value) {
            addCriterion("package_id <", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdLessThanOrEqualTo(Long value) {
            addCriterion("package_id <=", value, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdIn(List<Long> values) {
            addCriterion("package_id in", values, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdNotIn(List<Long> values) {
            addCriterion("package_id not in", values, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdBetween(Long value1, Long value2) {
            addCriterion("package_id between", value1, value2, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageIdNotBetween(Long value1, Long value2) {
            addCriterion("package_id not between", value1, value2, "packageId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdIsNull() {
            addCriterion("package_rule_id is null");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdIsNotNull() {
            addCriterion("package_rule_id is not null");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdEqualTo(Long value) {
            addCriterion("package_rule_id =", value, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdNotEqualTo(Long value) {
            addCriterion("package_rule_id <>", value, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdGreaterThan(Long value) {
            addCriterion("package_rule_id >", value, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdGreaterThanOrEqualTo(Long value) {
            addCriterion("package_rule_id >=", value, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdLessThan(Long value) {
            addCriterion("package_rule_id <", value, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdLessThanOrEqualTo(Long value) {
            addCriterion("package_rule_id <=", value, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdIn(List<Long> values) {
            addCriterion("package_rule_id in", values, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdNotIn(List<Long> values) {
            addCriterion("package_rule_id not in", values, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdBetween(Long value1, Long value2) {
            addCriterion("package_rule_id between", value1, value2, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andPackageRuleIdNotBetween(Long value1, Long value2) {
            addCriterion("package_rule_id not between", value1, value2, "packageRuleId");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeIsNull() {
            addCriterion("data_source_type is null");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeIsNotNull() {
            addCriterion("data_source_type is not null");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeEqualTo(String value) {
            addCriterion("data_source_type =", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeNotEqualTo(String value) {
            addCriterion("data_source_type <>", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeGreaterThan(String value) {
            addCriterion("data_source_type >", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeGreaterThanOrEqualTo(String value) {
            addCriterion("data_source_type >=", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeLessThan(String value) {
            addCriterion("data_source_type <", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeLessThanOrEqualTo(String value) {
            addCriterion("data_source_type <=", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeLike(String value) {
            addCriterion("data_source_type like", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeNotLike(String value) {
            addCriterion("data_source_type not like", value, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeIn(List<String> values) {
            addCriterion("data_source_type in", values, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeNotIn(List<String> values) {
            addCriterion("data_source_type not in", values, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeBetween(String value1, String value2) {
            addCriterion("data_source_type between", value1, value2, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andDataSourceTypeNotBetween(String value1, String value2) {
            addCriterion("data_source_type not between", value1, value2, "dataSourceType");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListIsNull() {
            addCriterion("cell_sha256_code_list is null");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListIsNotNull() {
            addCriterion("cell_sha256_code_list is not null");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListEqualTo(String value) {
            addCriterion("cell_sha256_code_list =", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListNotEqualTo(String value) {
            addCriterion("cell_sha256_code_list <>", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListGreaterThan(String value) {
            addCriterion("cell_sha256_code_list >", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListGreaterThanOrEqualTo(String value) {
            addCriterion("cell_sha256_code_list >=", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListLessThan(String value) {
            addCriterion("cell_sha256_code_list <", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListLessThanOrEqualTo(String value) {
            addCriterion("cell_sha256_code_list <=", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListLike(String value) {
            addCriterion("cell_sha256_code_list like", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListNotLike(String value) {
            addCriterion("cell_sha256_code_list not like", value, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListIn(List<String> values) {
            addCriterion("cell_sha256_code_list in", values, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListNotIn(List<String> values) {
            addCriterion("cell_sha256_code_list not in", values, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListBetween(String value1, String value2) {
            addCriterion("cell_sha256_code_list between", value1, value2, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andCellSha256CodeListNotBetween(String value1, String value2) {
            addCriterion("cell_sha256_code_list not between", value1, value2, "cellSha256CodeList");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeIsNull() {
            addCriterion("release_time is null");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeIsNotNull() {
            addCriterion("release_time is not null");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeEqualTo(String value) {
            addCriterion("release_time =", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeNotEqualTo(String value) {
            addCriterion("release_time <>", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeGreaterThan(String value) {
            addCriterion("release_time >", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeGreaterThanOrEqualTo(String value) {
            addCriterion("release_time >=", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeLessThan(String value) {
            addCriterion("release_time <", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeLessThanOrEqualTo(String value) {
            addCriterion("release_time <=", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeLike(String value) {
            addCriterion("release_time like", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeNotLike(String value) {
            addCriterion("release_time not like", value, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeIn(List<String> values) {
            addCriterion("release_time in", values, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeNotIn(List<String> values) {
            addCriterion("release_time not in", values, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeBetween(String value1, String value2) {
            addCriterion("release_time between", value1, value2, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseTimeNotBetween(String value1, String value2) {
            addCriterion("release_time not between", value1, value2, "releaseTime");
            return (Criteria) this;
        }

        public Criteria andReleaseDateIsNull() {
            addCriterion("release_date is null");
            return (Criteria) this;
        }

        public Criteria andReleaseDateIsNotNull() {
            addCriterion("release_date is not null");
            return (Criteria) this;
        }

        public Criteria andReleaseDateEqualTo(String value) {
            addCriterion("release_date =", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateNotEqualTo(String value) {
            addCriterion("release_date <>", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateGreaterThan(String value) {
            addCriterion("release_date >", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateGreaterThanOrEqualTo(String value) {
            addCriterion("release_date >=", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateLessThan(String value) {
            addCriterion("release_date <", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateLessThanOrEqualTo(String value) {
            addCriterion("release_date <=", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateLike(String value) {
            addCriterion("release_date like", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateNotLike(String value) {
            addCriterion("release_date not like", value, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateIn(List<String> values) {
            addCriterion("release_date in", values, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateNotIn(List<String> values) {
            addCriterion("release_date not in", values, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateBetween(String value1, String value2) {
            addCriterion("release_date between", value1, value2, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andReleaseDateNotBetween(String value1, String value2) {
            addCriterion("release_date not between", value1, value2, "releaseDate");
            return (Criteria) this;
        }

        public Criteria andOrgChannelIsNull() {
            addCriterion("org_channel is null");
            return (Criteria) this;
        }

        public Criteria andOrgChannelIsNotNull() {
            addCriterion("org_channel is not null");
            return (Criteria) this;
        }

        public Criteria andOrgChannelEqualTo(String value) {
            addCriterion("org_channel =", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotEqualTo(String value) {
            addCriterion("org_channel <>", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelGreaterThan(String value) {
            addCriterion("org_channel >", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelGreaterThanOrEqualTo(String value) {
            addCriterion("org_channel >=", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelLessThan(String value) {
            addCriterion("org_channel <", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelLessThanOrEqualTo(String value) {
            addCriterion("org_channel <=", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelLike(String value) {
            addCriterion("org_channel like", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotLike(String value) {
            addCriterion("org_channel not like", value, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelIn(List<String> values) {
            addCriterion("org_channel in", values, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotIn(List<String> values) {
            addCriterion("org_channel not in", values, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelBetween(String value1, String value2) {
            addCriterion("org_channel between", value1, value2, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andOrgChannelNotBetween(String value1, String value2) {
            addCriterion("org_channel not between", value1, value2, "orgChannel");
            return (Criteria) this;
        }

        public Criteria andMktLevelIsNull() {
            addCriterion("mkt_level is null");
            return (Criteria) this;
        }

        public Criteria andMktLevelIsNotNull() {
            addCriterion("mkt_level is not null");
            return (Criteria) this;
        }

        public Criteria andMktLevelEqualTo(String value) {
            addCriterion("mkt_level =", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotEqualTo(String value) {
            addCriterion("mkt_level <>", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelGreaterThan(String value) {
            addCriterion("mkt_level >", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelGreaterThanOrEqualTo(String value) {
            addCriterion("mkt_level >=", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelLessThan(String value) {
            addCriterion("mkt_level <", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelLessThanOrEqualTo(String value) {
            addCriterion("mkt_level <=", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelLike(String value) {
            addCriterion("mkt_level like", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotLike(String value) {
            addCriterion("mkt_level not like", value, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelIn(List<String> values) {
            addCriterion("mkt_level in", values, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotIn(List<String> values) {
            addCriterion("mkt_level not in", values, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelBetween(String value1, String value2) {
            addCriterion("mkt_level between", value1, value2, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andMktLevelNotBetween(String value1, String value2) {
            addCriterion("mkt_level not between", value1, value2, "mktLevel");
            return (Criteria) this;
        }

        public Criteria andInfoIsNull() {
            addCriterion("info is null");
            return (Criteria) this;
        }

        public Criteria andInfoIsNotNull() {
            addCriterion("info is not null");
            return (Criteria) this;
        }

        public Criteria andInfoEqualTo(String value) {
            addCriterion("info =", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotEqualTo(String value) {
            addCriterion("info <>", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoGreaterThan(String value) {
            addCriterion("info >", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoGreaterThanOrEqualTo(String value) {
            addCriterion("info >=", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoLessThan(String value) {
            addCriterion("info <", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoLessThanOrEqualTo(String value) {
            addCriterion("info <=", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoLike(String value) {
            addCriterion("info like", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotLike(String value) {
            addCriterion("info not like", value, "info");
            return (Criteria) this;
        }

        public Criteria andInfoIn(List<String> values) {
            addCriterion("info in", values, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotIn(List<String> values) {
            addCriterion("info not in", values, "info");
            return (Criteria) this;
        }

        public Criteria andInfoBetween(String value1, String value2) {
            addCriterion("info between", value1, value2, "info");
            return (Criteria) this;
        }

        public Criteria andInfoNotBetween(String value1, String value2) {
            addCriterion("info not between", value1, value2, "info");
            return (Criteria) this;
        }

        public Criteria andResultIsNull() {
            addCriterion("`result` is null");
            return (Criteria) this;
        }

        public Criteria andResultIsNotNull() {
            addCriterion("`result` is not null");
            return (Criteria) this;
        }

        public Criteria andResultEqualTo(Boolean value) {
            addCriterion("`result` =", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotEqualTo(Boolean value) {
            addCriterion("`result` <>", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultGreaterThan(Boolean value) {
            addCriterion("`result` >", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultGreaterThanOrEqualTo(Boolean value) {
            addCriterion("`result` >=", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultLessThan(Boolean value) {
            addCriterion("`result` <", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultLessThanOrEqualTo(Boolean value) {
            addCriterion("`result` <=", value, "result");
            return (Criteria) this;
        }

        public Criteria andResultIn(List<Boolean> values) {
            addCriterion("`result` in", values, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotIn(List<Boolean> values) {
            addCriterion("`result` not in", values, "result");
            return (Criteria) this;
        }

        public Criteria andResultBetween(Boolean value1, Boolean value2) {
            addCriterion("`result` between", value1, value2, "result");
            return (Criteria) this;
        }

        public Criteria andResultNotBetween(Boolean value1, Boolean value2) {
            addCriterion("`result` not between", value1, value2, "result");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListIsNull() {
            addCriterion("market_coupon_list is null");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListIsNotNull() {
            addCriterion("market_coupon_list is not null");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListEqualTo(String value) {
            addCriterion("market_coupon_list =", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListNotEqualTo(String value) {
            addCriterion("market_coupon_list <>", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListGreaterThan(String value) {
            addCriterion("market_coupon_list >", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListGreaterThanOrEqualTo(String value) {
            addCriterion("market_coupon_list >=", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListLessThan(String value) {
            addCriterion("market_coupon_list <", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListLessThanOrEqualTo(String value) {
            addCriterion("market_coupon_list <=", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListLike(String value) {
            addCriterion("market_coupon_list like", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListNotLike(String value) {
            addCriterion("market_coupon_list not like", value, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListIn(List<String> values) {
            addCriterion("market_coupon_list in", values, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListNotIn(List<String> values) {
            addCriterion("market_coupon_list not in", values, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListBetween(String value1, String value2) {
            addCriterion("market_coupon_list between", value1, value2, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andMarketCouponListNotBetween(String value1, String value2) {
            addCriterion("market_coupon_list not between", value1, value2, "marketCouponList");
            return (Criteria) this;
        }

        public Criteria andCouponCodeIsNull() {
            addCriterion("coupon_code is null");
            return (Criteria) this;
        }

        public Criteria andCouponCodeIsNotNull() {
            addCriterion("coupon_code is not null");
            return (Criteria) this;
        }

        public Criteria andCouponCodeEqualTo(String value) {
            addCriterion("coupon_code =", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeNotEqualTo(String value) {
            addCriterion("coupon_code <>", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeGreaterThan(String value) {
            addCriterion("coupon_code >", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeGreaterThanOrEqualTo(String value) {
            addCriterion("coupon_code >=", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeLessThan(String value) {
            addCriterion("coupon_code <", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeLessThanOrEqualTo(String value) {
            addCriterion("coupon_code <=", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeLike(String value) {
            addCriterion("coupon_code like", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeNotLike(String value) {
            addCriterion("coupon_code not like", value, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeIn(List<String> values) {
            addCriterion("coupon_code in", values, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeNotIn(List<String> values) {
            addCriterion("coupon_code not in", values, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeBetween(String value1, String value2) {
            addCriterion("coupon_code between", value1, value2, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponCodeNotBetween(String value1, String value2) {
            addCriterion("coupon_code not between", value1, value2, "couponCode");
            return (Criteria) this;
        }

        public Criteria andCouponDescIsNull() {
            addCriterion("coupon_desc is null");
            return (Criteria) this;
        }

        public Criteria andCouponDescIsNotNull() {
            addCriterion("coupon_desc is not null");
            return (Criteria) this;
        }

        public Criteria andCouponDescEqualTo(String value) {
            addCriterion("coupon_desc =", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescNotEqualTo(String value) {
            addCriterion("coupon_desc <>", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescGreaterThan(String value) {
            addCriterion("coupon_desc >", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescGreaterThanOrEqualTo(String value) {
            addCriterion("coupon_desc >=", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescLessThan(String value) {
            addCriterion("coupon_desc <", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescLessThanOrEqualTo(String value) {
            addCriterion("coupon_desc <=", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescLike(String value) {
            addCriterion("coupon_desc like", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescNotLike(String value) {
            addCriterion("coupon_desc not like", value, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescIn(List<String> values) {
            addCriterion("coupon_desc in", values, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescNotIn(List<String> values) {
            addCriterion("coupon_desc not in", values, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescBetween(String value1, String value2) {
            addCriterion("coupon_desc between", value1, value2, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andCouponDescNotBetween(String value1, String value2) {
            addCriterion("coupon_desc not between", value1, value2, "couponDesc");
            return (Criteria) this;
        }

        public Criteria andHttpCodeIsNull() {
            addCriterion("http_code is null");
            return (Criteria) this;
        }

        public Criteria andHttpCodeIsNotNull() {
            addCriterion("http_code is not null");
            return (Criteria) this;
        }

        public Criteria andHttpCodeEqualTo(Integer value) {
            addCriterion("http_code =", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeNotEqualTo(Integer value) {
            addCriterion("http_code <>", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeGreaterThan(Integer value) {
            addCriterion("http_code >", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeGreaterThanOrEqualTo(Integer value) {
            addCriterion("http_code >=", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeLessThan(Integer value) {
            addCriterion("http_code <", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeLessThanOrEqualTo(Integer value) {
            addCriterion("http_code <=", value, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeIn(List<Integer> values) {
            addCriterion("http_code in", values, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeNotIn(List<Integer> values) {
            addCriterion("http_code not in", values, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeBetween(Integer value1, Integer value2) {
            addCriterion("http_code between", value1, value2, "httpCode");
            return (Criteria) this;
        }

        public Criteria andHttpCodeNotBetween(Integer value1, Integer value2) {
            addCriterion("http_code not between", value1, value2, "httpCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeIsNull() {
            addCriterion("business_code is null");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeIsNotNull() {
            addCriterion("business_code is not null");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeEqualTo(Integer value) {
            addCriterion("business_code =", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeNotEqualTo(Integer value) {
            addCriterion("business_code <>", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeGreaterThan(Integer value) {
            addCriterion("business_code >", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeGreaterThanOrEqualTo(Integer value) {
            addCriterion("business_code >=", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeLessThan(Integer value) {
            addCriterion("business_code <", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeLessThanOrEqualTo(Integer value) {
            addCriterion("business_code <=", value, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeIn(List<Integer> values) {
            addCriterion("business_code in", values, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeNotIn(List<Integer> values) {
            addCriterion("business_code not in", values, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeBetween(Integer value1, Integer value2) {
            addCriterion("business_code between", value1, value2, "businessCode");
            return (Criteria) this;
        }

        public Criteria andBusinessCodeNotBetween(Integer value1, Integer value2) {
            addCriterion("business_code not between", value1, value2, "businessCode");
            return (Criteria) this;
        }

        public Criteria andReturnContentIsNull() {
            addCriterion("return_content is null");
            return (Criteria) this;
        }

        public Criteria andReturnContentIsNotNull() {
            addCriterion("return_content is not null");
            return (Criteria) this;
        }

        public Criteria andReturnContentEqualTo(String value) {
            addCriterion("return_content =", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentNotEqualTo(String value) {
            addCriterion("return_content <>", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentGreaterThan(String value) {
            addCriterion("return_content >", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentGreaterThanOrEqualTo(String value) {
            addCriterion("return_content >=", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentLessThan(String value) {
            addCriterion("return_content <", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentLessThanOrEqualTo(String value) {
            addCriterion("return_content <=", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentLike(String value) {
            addCriterion("return_content like", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentNotLike(String value) {
            addCriterion("return_content not like", value, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentIn(List<String> values) {
            addCriterion("return_content in", values, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentNotIn(List<String> values) {
            addCriterion("return_content not in", values, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentBetween(String value1, String value2) {
            addCriterion("return_content between", value1, value2, "returnContent");
            return (Criteria) this;
        }

        public Criteria andReturnContentNotBetween(String value1, String value2) {
            addCriterion("return_content not between", value1, value2, "returnContent");
            return (Criteria) this;
        }

        public Criteria andExtendIsNull() {
            addCriterion("extend is null");
            return (Criteria) this;
        }

        public Criteria andExtendIsNotNull() {
            addCriterion("extend is not null");
            return (Criteria) this;
        }

        public Criteria andExtendEqualTo(String value) {
            addCriterion("extend =", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotEqualTo(String value) {
            addCriterion("extend <>", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThan(String value) {
            addCriterion("extend >", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendGreaterThanOrEqualTo(String value) {
            addCriterion("extend >=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThan(String value) {
            addCriterion("extend <", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLessThanOrEqualTo(String value) {
            addCriterion("extend <=", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendLike(String value) {
            addCriterion("extend like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotLike(String value) {
            addCriterion("extend not like", value, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendIn(List<String> values) {
            addCriterion("extend in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotIn(List<String> values) {
            addCriterion("extend not in", values, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendBetween(String value1, String value2) {
            addCriterion("extend between", value1, value2, "extend");
            return (Criteria) this;
        }

        public Criteria andExtendNotBetween(String value1, String value2) {
            addCriterion("extend not between", value1, value2, "extend");
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