package com.br.marketing.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MarketingLineAccountRecordExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingLineAccountRecordExample() {
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

        protected void addCriterionForJDBCDate(String condition, Date value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value.getTime()), property);
        }

        protected void addCriterionForJDBCDate(String condition, List<Date> values, String property) {
            if (values == null || values.size() == 0) {
                throw new RuntimeException("Value list for " + property + " cannot be null or empty");
            }
            List<java.sql.Date> dateList = new ArrayList<java.sql.Date>();
            Iterator<Date> iter = values.iterator();
            while (iter.hasNext()) {
                dateList.add(new java.sql.Date(iter.next().getTime()));
            }
            addCriterion(condition, dateList, property);
        }

        protected void addCriterionForJDBCDate(String condition, Date value1, Date value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            addCriterion(condition, new java.sql.Date(value1.getTime()), new java.sql.Date(value2.getTime()), property);
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

        public Criteria andConfigIdIsNull() {
            addCriterion("config_id is null");
            return (Criteria) this;
        }

        public Criteria andConfigIdIsNotNull() {
            addCriterion("config_id is not null");
            return (Criteria) this;
        }

        public Criteria andConfigIdEqualTo(Long value) {
            addCriterion("config_id =", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdNotEqualTo(Long value) {
            addCriterion("config_id <>", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdGreaterThan(Long value) {
            addCriterion("config_id >", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdGreaterThanOrEqualTo(Long value) {
            addCriterion("config_id >=", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdLessThan(Long value) {
            addCriterion("config_id <", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdLessThanOrEqualTo(Long value) {
            addCriterion("config_id <=", value, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdIn(List<Long> values) {
            addCriterion("config_id in", values, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdNotIn(List<Long> values) {
            addCriterion("config_id not in", values, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdBetween(Long value1, Long value2) {
            addCriterion("config_id between", value1, value2, "configId");
            return (Criteria) this;
        }

        public Criteria andConfigIdNotBetween(Long value1, Long value2) {
            addCriterion("config_id not between", value1, value2, "configId");
            return (Criteria) this;
        }

        public Criteria andLineSupplierIsNull() {
            addCriterion("line_supplier is null");
            return (Criteria) this;
        }

        public Criteria andLineSupplierIsNotNull() {
            addCriterion("line_supplier is not null");
            return (Criteria) this;
        }

        public Criteria andLineSupplierEqualTo(String value) {
            addCriterion("line_supplier =", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierNotEqualTo(String value) {
            addCriterion("line_supplier <>", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierGreaterThan(String value) {
            addCriterion("line_supplier >", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierGreaterThanOrEqualTo(String value) {
            addCriterion("line_supplier >=", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierLessThan(String value) {
            addCriterion("line_supplier <", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierLessThanOrEqualTo(String value) {
            addCriterion("line_supplier <=", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierLike(String value) {
            addCriterion("line_supplier like", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierNotLike(String value) {
            addCriterion("line_supplier not like", value, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierIn(List<String> values) {
            addCriterion("line_supplier in", values, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierNotIn(List<String> values) {
            addCriterion("line_supplier not in", values, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierBetween(String value1, String value2) {
            addCriterion("line_supplier between", value1, value2, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLineSupplierNotBetween(String value1, String value2) {
            addCriterion("line_supplier not between", value1, value2, "lineSupplier");
            return (Criteria) this;
        }

        public Criteria andLinesInfoIsNull() {
            addCriterion("lines_info is null");
            return (Criteria) this;
        }

        public Criteria andLinesInfoIsNotNull() {
            addCriterion("lines_info is not null");
            return (Criteria) this;
        }

        public Criteria andLinesInfoEqualTo(String value) {
            addCriterion("lines_info =", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoNotEqualTo(String value) {
            addCriterion("lines_info <>", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoGreaterThan(String value) {
            addCriterion("lines_info >", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoGreaterThanOrEqualTo(String value) {
            addCriterion("lines_info >=", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoLessThan(String value) {
            addCriterion("lines_info <", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoLessThanOrEqualTo(String value) {
            addCriterion("lines_info <=", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoLike(String value) {
            addCriterion("lines_info like", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoNotLike(String value) {
            addCriterion("lines_info not like", value, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoIn(List<String> values) {
            addCriterion("lines_info in", values, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoNotIn(List<String> values) {
            addCriterion("lines_info not in", values, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoBetween(String value1, String value2) {
            addCriterion("lines_info between", value1, value2, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andLinesInfoNotBetween(String value1, String value2) {
            addCriterion("lines_info not between", value1, value2, "linesInfo");
            return (Criteria) this;
        }

        public Criteria andPriceIsNull() {
            addCriterion("price is null");
            return (Criteria) this;
        }

        public Criteria andPriceIsNotNull() {
            addCriterion("price is not null");
            return (Criteria) this;
        }

        public Criteria andPriceEqualTo(BigDecimal value) {
            addCriterion("price =", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceNotEqualTo(BigDecimal value) {
            addCriterion("price <>", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceGreaterThan(BigDecimal value) {
            addCriterion("price >", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceGreaterThanOrEqualTo(BigDecimal value) {
            addCriterion("price >=", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceLessThan(BigDecimal value) {
            addCriterion("price <", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceLessThanOrEqualTo(BigDecimal value) {
            addCriterion("price <=", value, "price");
            return (Criteria) this;
        }

        public Criteria andPriceIn(List<BigDecimal> values) {
            addCriterion("price in", values, "price");
            return (Criteria) this;
        }

        public Criteria andPriceNotIn(List<BigDecimal> values) {
            addCriterion("price not in", values, "price");
            return (Criteria) this;
        }

        public Criteria andPriceBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("price between", value1, value2, "price");
            return (Criteria) this;
        }

        public Criteria andPriceNotBetween(BigDecimal value1, BigDecimal value2) {
            addCriterion("price not between", value1, value2, "price");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateIsNull() {
            addCriterion("effect_start_date is null");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateIsNotNull() {
            addCriterion("effect_start_date is not null");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateEqualTo(Date value) {
            addCriterionForJDBCDate("effect_start_date =", value, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("effect_start_date <>", value, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateGreaterThan(Date value) {
            addCriterionForJDBCDate("effect_start_date >", value, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("effect_start_date >=", value, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateLessThan(Date value) {
            addCriterionForJDBCDate("effect_start_date <", value, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("effect_start_date <=", value, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateIn(List<Date> values) {
            addCriterionForJDBCDate("effect_start_date in", values, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("effect_start_date not in", values, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("effect_start_date between", value1, value2, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectStartDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("effect_start_date not between", value1, value2, "effectStartDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateIsNull() {
            addCriterion("effect_end_date is null");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateIsNotNull() {
            addCriterion("effect_end_date is not null");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateEqualTo(Date value) {
            addCriterionForJDBCDate("effect_end_date =", value, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateNotEqualTo(Date value) {
            addCriterionForJDBCDate("effect_end_date <>", value, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateGreaterThan(Date value) {
            addCriterionForJDBCDate("effect_end_date >", value, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateGreaterThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("effect_end_date >=", value, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateLessThan(Date value) {
            addCriterionForJDBCDate("effect_end_date <", value, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateLessThanOrEqualTo(Date value) {
            addCriterionForJDBCDate("effect_end_date <=", value, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateIn(List<Date> values) {
            addCriterionForJDBCDate("effect_end_date in", values, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateNotIn(List<Date> values) {
            addCriterionForJDBCDate("effect_end_date not in", values, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("effect_end_date between", value1, value2, "effectEndDate");
            return (Criteria) this;
        }

        public Criteria andEffectEndDateNotBetween(Date value1, Date value2) {
            addCriterionForJDBCDate("effect_end_date not between", value1, value2, "effectEndDate");
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