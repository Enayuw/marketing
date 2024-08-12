package com.br.marketing.entity.clean.rongshu;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RongshuPaofenFileUpdateSyncCleanLogExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public RongshuPaofenFileUpdateSyncCleanLogExample() {
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

        public Criteria andMarketingCleanDataFileIdIsNull() {
            addCriterion("marketing_clean_data_file_id is null");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdIsNotNull() {
            addCriterion("marketing_clean_data_file_id is not null");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdEqualTo(Long value) {
            addCriterion("marketing_clean_data_file_id =", value, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdNotEqualTo(Long value) {
            addCriterion("marketing_clean_data_file_id <>", value, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdGreaterThan(Long value) {
            addCriterion("marketing_clean_data_file_id >", value, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdGreaterThanOrEqualTo(Long value) {
            addCriterion("marketing_clean_data_file_id >=", value, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdLessThan(Long value) {
            addCriterion("marketing_clean_data_file_id <", value, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdLessThanOrEqualTo(Long value) {
            addCriterion("marketing_clean_data_file_id <=", value, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdIn(List<Long> values) {
            addCriterion("marketing_clean_data_file_id in", values, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdNotIn(List<Long> values) {
            addCriterion("marketing_clean_data_file_id not in", values, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdBetween(Long value1, Long value2) {
            addCriterion("marketing_clean_data_file_id between", value1, value2, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andMarketingCleanDataFileIdNotBetween(Long value1, Long value2) {
            addCriterion("marketing_clean_data_file_id not between", value1, value2, "marketingCleanDataFileId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdIsNull() {
            addCriterion("sync_apicode_id is null");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdIsNotNull() {
            addCriterion("sync_apicode_id is not null");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdEqualTo(Long value) {
            addCriterion("sync_apicode_id =", value, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdNotEqualTo(Long value) {
            addCriterion("sync_apicode_id <>", value, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdGreaterThan(Long value) {
            addCriterion("sync_apicode_id >", value, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sync_apicode_id >=", value, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdLessThan(Long value) {
            addCriterion("sync_apicode_id <", value, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdLessThanOrEqualTo(Long value) {
            addCriterion("sync_apicode_id <=", value, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdIn(List<Long> values) {
            addCriterion("sync_apicode_id in", values, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdNotIn(List<Long> values) {
            addCriterion("sync_apicode_id not in", values, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdBetween(Long value1, Long value2) {
            addCriterion("sync_apicode_id between", value1, value2, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andSyncApicodeIdNotBetween(Long value1, Long value2) {
            addCriterion("sync_apicode_id not between", value1, value2, "syncApicodeId");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonIsNull() {
            addCriterion("history_data_json is null");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonIsNotNull() {
            addCriterion("history_data_json is not null");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonEqualTo(String value) {
            addCriterion("history_data_json =", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonNotEqualTo(String value) {
            addCriterion("history_data_json <>", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonGreaterThan(String value) {
            addCriterion("history_data_json >", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonGreaterThanOrEqualTo(String value) {
            addCriterion("history_data_json >=", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonLessThan(String value) {
            addCriterion("history_data_json <", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonLessThanOrEqualTo(String value) {
            addCriterion("history_data_json <=", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonLike(String value) {
            addCriterion("history_data_json like", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonNotLike(String value) {
            addCriterion("history_data_json not like", value, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonIn(List<String> values) {
            addCriterion("history_data_json in", values, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonNotIn(List<String> values) {
            addCriterion("history_data_json not in", values, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonBetween(String value1, String value2) {
            addCriterion("history_data_json between", value1, value2, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andHistoryDataJsonNotBetween(String value1, String value2) {
            addCriterion("history_data_json not between", value1, value2, "historyDataJson");
            return (Criteria) this;
        }

        public Criteria andUidIsNull() {
            addCriterion("`uid` is null");
            return (Criteria) this;
        }

        public Criteria andUidIsNotNull() {
            addCriterion("`uid` is not null");
            return (Criteria) this;
        }

        public Criteria andUidEqualTo(String value) {
            addCriterion("`uid` =", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotEqualTo(String value) {
            addCriterion("`uid` <>", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThan(String value) {
            addCriterion("`uid` >", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidGreaterThanOrEqualTo(String value) {
            addCriterion("`uid` >=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThan(String value) {
            addCriterion("`uid` <", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLessThanOrEqualTo(String value) {
            addCriterion("`uid` <=", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidLike(String value) {
            addCriterion("`uid` like", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotLike(String value) {
            addCriterion("`uid` not like", value, "uid");
            return (Criteria) this;
        }

        public Criteria andUidIn(List<String> values) {
            addCriterion("`uid` in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotIn(List<String> values) {
            addCriterion("`uid` not in", values, "uid");
            return (Criteria) this;
        }

        public Criteria andUidBetween(String value1, String value2) {
            addCriterion("`uid` between", value1, value2, "uid");
            return (Criteria) this;
        }

        public Criteria andUidNotBetween(String value1, String value2) {
            addCriterion("`uid` not between", value1, value2, "uid");
            return (Criteria) this;
        }

        public Criteria andIsSuccessIsNull() {
            addCriterion("is_success is null");
            return (Criteria) this;
        }

        public Criteria andIsSuccessIsNotNull() {
            addCriterion("is_success is not null");
            return (Criteria) this;
        }

        public Criteria andIsSuccessEqualTo(Integer value) {
            addCriterion("is_success =", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessNotEqualTo(Integer value) {
            addCriterion("is_success <>", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessGreaterThan(Integer value) {
            addCriterion("is_success >", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessGreaterThanOrEqualTo(Integer value) {
            addCriterion("is_success >=", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessLessThan(Integer value) {
            addCriterion("is_success <", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessLessThanOrEqualTo(Integer value) {
            addCriterion("is_success <=", value, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessIn(List<Integer> values) {
            addCriterion("is_success in", values, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessNotIn(List<Integer> values) {
            addCriterion("is_success not in", values, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessBetween(Integer value1, Integer value2) {
            addCriterion("is_success between", value1, value2, "isSuccess");
            return (Criteria) this;
        }

        public Criteria andIsSuccessNotBetween(Integer value1, Integer value2) {
            addCriterion("is_success not between", value1, value2, "isSuccess");
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

        public Criteria andNewDataJsonIsNull() {
            addCriterion("new_data_json is null");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonIsNotNull() {
            addCriterion("new_data_json is not null");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonEqualTo(String value) {
            addCriterion("new_data_json =", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonNotEqualTo(String value) {
            addCriterion("new_data_json <>", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonGreaterThan(String value) {
            addCriterion("new_data_json >", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonGreaterThanOrEqualTo(String value) {
            addCriterion("new_data_json >=", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonLessThan(String value) {
            addCriterion("new_data_json <", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonLessThanOrEqualTo(String value) {
            addCriterion("new_data_json <=", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonLike(String value) {
            addCriterion("new_data_json like", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonNotLike(String value) {
            addCriterion("new_data_json not like", value, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonIn(List<String> values) {
            addCriterion("new_data_json in", values, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonNotIn(List<String> values) {
            addCriterion("new_data_json not in", values, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonBetween(String value1, String value2) {
            addCriterion("new_data_json between", value1, value2, "newDataJson");
            return (Criteria) this;
        }

        public Criteria andNewDataJsonNotBetween(String value1, String value2) {
            addCriterion("new_data_json not between", value1, value2, "newDataJson");
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