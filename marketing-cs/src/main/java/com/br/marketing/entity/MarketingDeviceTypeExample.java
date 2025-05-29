package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingDeviceTypeExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingDeviceTypeExample() {
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

        public Criteria andCellIsNull() {
            addCriterion("cell is null");
            return (Criteria) this;
        }

        public Criteria andCellIsNotNull() {
            addCriterion("cell is not null");
            return (Criteria) this;
        }

        public Criteria andCellEqualTo(String value) {
            addCriterion("cell =", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotEqualTo(String value) {
            addCriterion("cell <>", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellGreaterThan(String value) {
            addCriterion("cell >", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellGreaterThanOrEqualTo(String value) {
            addCriterion("cell >=", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLessThan(String value) {
            addCriterion("cell <", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLessThanOrEqualTo(String value) {
            addCriterion("cell <=", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellLike(String value) {
            addCriterion("cell like", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotLike(String value) {
            addCriterion("cell not like", value, "cell");
            return (Criteria) this;
        }

        public Criteria andCellIn(List<String> values) {
            addCriterion("cell in", values, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotIn(List<String> values) {
            addCriterion("cell not in", values, "cell");
            return (Criteria) this;
        }

        public Criteria andCellBetween(String value1, String value2) {
            addCriterion("cell between", value1, value2, "cell");
            return (Criteria) this;
        }

        public Criteria andCellNotBetween(String value1, String value2) {
            addCriterion("cell not between", value1, value2, "cell");
            return (Criteria) this;
        }

        public Criteria andCellMd5IsNull() {
            addCriterion("cell_md5 is null");
            return (Criteria) this;
        }

        public Criteria andCellMd5IsNotNull() {
            addCriterion("cell_md5 is not null");
            return (Criteria) this;
        }

        public Criteria andCellMd5EqualTo(String value) {
            addCriterion("cell_md5 =", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotEqualTo(String value) {
            addCriterion("cell_md5 <>", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5GreaterThan(String value) {
            addCriterion("cell_md5 >", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5GreaterThanOrEqualTo(String value) {
            addCriterion("cell_md5 >=", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5LessThan(String value) {
            addCriterion("cell_md5 <", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5LessThanOrEqualTo(String value) {
            addCriterion("cell_md5 <=", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5Like(String value) {
            addCriterion("cell_md5 like", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotLike(String value) {
            addCriterion("cell_md5 not like", value, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5In(List<String> values) {
            addCriterion("cell_md5 in", values, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotIn(List<String> values) {
            addCriterion("cell_md5 not in", values, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5Between(String value1, String value2) {
            addCriterion("cell_md5 between", value1, value2, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellMd5NotBetween(String value1, String value2) {
            addCriterion("cell_md5 not between", value1, value2, "cellMd5");
            return (Criteria) this;
        }

        public Criteria andCellSha256IsNull() {
            addCriterion("cell_sha256 is null");
            return (Criteria) this;
        }

        public Criteria andCellSha256IsNotNull() {
            addCriterion("cell_sha256 is not null");
            return (Criteria) this;
        }

        public Criteria andCellSha256EqualTo(String value) {
            addCriterion("cell_sha256 =", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotEqualTo(String value) {
            addCriterion("cell_sha256 <>", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256GreaterThan(String value) {
            addCriterion("cell_sha256 >", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256GreaterThanOrEqualTo(String value) {
            addCriterion("cell_sha256 >=", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256LessThan(String value) {
            addCriterion("cell_sha256 <", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256LessThanOrEqualTo(String value) {
            addCriterion("cell_sha256 <=", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256Like(String value) {
            addCriterion("cell_sha256 like", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotLike(String value) {
            addCriterion("cell_sha256 not like", value, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256In(List<String> values) {
            addCriterion("cell_sha256 in", values, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotIn(List<String> values) {
            addCriterion("cell_sha256 not in", values, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256Between(String value1, String value2) {
            addCriterion("cell_sha256 between", value1, value2, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha256NotBetween(String value1, String value2) {
            addCriterion("cell_sha256 not between", value1, value2, "cellSha256");
            return (Criteria) this;
        }

        public Criteria andCellSha1IsNull() {
            addCriterion("cell_sha1 is null");
            return (Criteria) this;
        }

        public Criteria andCellSha1IsNotNull() {
            addCriterion("cell_sha1 is not null");
            return (Criteria) this;
        }

        public Criteria andCellSha1EqualTo(String value) {
            addCriterion("cell_sha1 =", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1NotEqualTo(String value) {
            addCriterion("cell_sha1 <>", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1GreaterThan(String value) {
            addCriterion("cell_sha1 >", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1GreaterThanOrEqualTo(String value) {
            addCriterion("cell_sha1 >=", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1LessThan(String value) {
            addCriterion("cell_sha1 <", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1LessThanOrEqualTo(String value) {
            addCriterion("cell_sha1 <=", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1Like(String value) {
            addCriterion("cell_sha1 like", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1NotLike(String value) {
            addCriterion("cell_sha1 not like", value, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1In(List<String> values) {
            addCriterion("cell_sha1 in", values, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1NotIn(List<String> values) {
            addCriterion("cell_sha1 not in", values, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1Between(String value1, String value2) {
            addCriterion("cell_sha1 between", value1, value2, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andCellSha1NotBetween(String value1, String value2) {
            addCriterion("cell_sha1 not between", value1, value2, "cellSha1");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeIsNull() {
            addCriterion("device_type is null");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeIsNotNull() {
            addCriterion("device_type is not null");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeEqualTo(Integer value) {
            addCriterion("device_type =", value, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeNotEqualTo(Integer value) {
            addCriterion("device_type <>", value, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeGreaterThan(Integer value) {
            addCriterion("device_type >", value, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("device_type >=", value, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeLessThan(Integer value) {
            addCriterion("device_type <", value, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeLessThanOrEqualTo(Integer value) {
            addCriterion("device_type <=", value, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeIn(List<Integer> values) {
            addCriterion("device_type in", values, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeNotIn(List<Integer> values) {
            addCriterion("device_type not in", values, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeBetween(Integer value1, Integer value2) {
            addCriterion("device_type between", value1, value2, "deviceType");
            return (Criteria) this;
        }

        public Criteria andDeviceTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("device_type not between", value1, value2, "deviceType");
            return (Criteria) this;
        }

        public Criteria andFailMsgIsNull() {
            addCriterion("fail_msg is null");
            return (Criteria) this;
        }

        public Criteria andFailMsgIsNotNull() {
            addCriterion("fail_msg is not null");
            return (Criteria) this;
        }

        public Criteria andFailMsgEqualTo(String value) {
            addCriterion("fail_msg =", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotEqualTo(String value) {
            addCriterion("fail_msg <>", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgGreaterThan(String value) {
            addCriterion("fail_msg >", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgGreaterThanOrEqualTo(String value) {
            addCriterion("fail_msg >=", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgLessThan(String value) {
            addCriterion("fail_msg <", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgLessThanOrEqualTo(String value) {
            addCriterion("fail_msg <=", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgLike(String value) {
            addCriterion("fail_msg like", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotLike(String value) {
            addCriterion("fail_msg not like", value, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgIn(List<String> values) {
            addCriterion("fail_msg in", values, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotIn(List<String> values) {
            addCriterion("fail_msg not in", values, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgBetween(String value1, String value2) {
            addCriterion("fail_msg between", value1, value2, "failMsg");
            return (Criteria) this;
        }

        public Criteria andFailMsgNotBetween(String value1, String value2) {
            addCriterion("fail_msg not between", value1, value2, "failMsg");
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