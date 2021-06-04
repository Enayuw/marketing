package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingDirtyUserExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingDirtyUserExample() {
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

    protected abstract static class AbstractGeneratedCriteria {
        protected List<Criterion> criteria;

        protected AbstractGeneratedCriteria() {
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

        public Criteria andIdEqualTo(Integer value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Integer value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Integer value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Integer value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Integer value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Integer value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Integer> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Integer> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Integer value1, Integer value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Integer value1, Integer value2) {
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

        public Criteria andBatchNumberIsNull() {
            addCriterion("batch_number is null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberIsNotNull() {
            addCriterion("batch_number is not null");
            return (Criteria) this;
        }

        public Criteria andBatchNumberEqualTo(String value) {
            addCriterion("batch_number =", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotEqualTo(String value) {
            addCriterion("batch_number <>", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberGreaterThan(String value) {
            addCriterion("batch_number >", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("batch_number >=", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLessThan(String value) {
            addCriterion("batch_number <", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("batch_number <=", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberLike(String value) {
            addCriterion("batch_number like", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotLike(String value) {
            addCriterion("batch_number not like", value, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberIn(List<String> values) {
            addCriterion("batch_number in", values, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotIn(List<String> values) {
            addCriterion("batch_number not in", values, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberBetween(String value1, String value2) {
            addCriterion("batch_number between", value1, value2, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andBatchNumberNotBetween(String value1, String value2) {
            addCriterion("batch_number not between", value1, value2, "batchNumber");
            return (Criteria) this;
        }

        public Criteria andCusNumIsNull() {
            addCriterion("cus_num is null");
            return (Criteria) this;
        }

        public Criteria andCusNumIsNotNull() {
            addCriterion("cus_num is not null");
            return (Criteria) this;
        }

        public Criteria andCusNumEqualTo(String value) {
            addCriterion("cus_num =", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumNotEqualTo(String value) {
            addCriterion("cus_num <>", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumGreaterThan(String value) {
            addCriterion("cus_num >", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumGreaterThanOrEqualTo(String value) {
            addCriterion("cus_num >=", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumLessThan(String value) {
            addCriterion("cus_num <", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumLessThanOrEqualTo(String value) {
            addCriterion("cus_num <=", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumLike(String value) {
            addCriterion("cus_num like", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumNotLike(String value) {
            addCriterion("cus_num not like", value, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumIn(List<String> values) {
            addCriterion("cus_num in", values, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumNotIn(List<String> values) {
            addCriterion("cus_num not in", values, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumBetween(String value1, String value2) {
            addCriterion("cus_num between", value1, value2, "cusNum");
            return (Criteria) this;
        }

        public Criteria andCusNumNotBetween(String value1, String value2) {
            addCriterion("cus_num not between", value1, value2, "cusNum");
            return (Criteria) this;
        }

        public Criteria andIdCardIsNull() {
            addCriterion("id_card is null");
            return (Criteria) this;
        }

        public Criteria andIdCardIsNotNull() {
            addCriterion("id_card is not null");
            return (Criteria) this;
        }

        public Criteria andIdCardEqualTo(String value) {
            addCriterion("id_card =", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotEqualTo(String value) {
            addCriterion("id_card <>", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardGreaterThan(String value) {
            addCriterion("id_card >", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardGreaterThanOrEqualTo(String value) {
            addCriterion("id_card >=", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardLessThan(String value) {
            addCriterion("id_card <", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardLessThanOrEqualTo(String value) {
            addCriterion("id_card <=", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardLike(String value) {
            addCriterion("id_card like", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotLike(String value) {
            addCriterion("id_card not like", value, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardIn(List<String> values) {
            addCriterion("id_card in", values, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotIn(List<String> values) {
            addCriterion("id_card not in", values, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardBetween(String value1, String value2) {
            addCriterion("id_card between", value1, value2, "idCard");
            return (Criteria) this;
        }

        public Criteria andIdCardNotBetween(String value1, String value2) {
            addCriterion("id_card not between", value1, value2, "idCard");
            return (Criteria) this;
        }

        public Criteria andNameIsNull() {
            addCriterion("name is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("name is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("name =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("name <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("name >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("name >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("name <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("name <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("name like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("name not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("name in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("name not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("name between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("name not between", value1, value2, "name");
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

        public Criteria andLinkmanCellIsNull() {
            addCriterion("linkman_cell is null");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellIsNotNull() {
            addCriterion("linkman_cell is not null");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellEqualTo(String value) {
            addCriterion("linkman_cell =", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellNotEqualTo(String value) {
            addCriterion("linkman_cell <>", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellGreaterThan(String value) {
            addCriterion("linkman_cell >", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellGreaterThanOrEqualTo(String value) {
            addCriterion("linkman_cell >=", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellLessThan(String value) {
            addCriterion("linkman_cell <", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellLessThanOrEqualTo(String value) {
            addCriterion("linkman_cell <=", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellLike(String value) {
            addCriterion("linkman_cell like", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellNotLike(String value) {
            addCriterion("linkman_cell not like", value, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellIn(List<String> values) {
            addCriterion("linkman_cell in", values, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellNotIn(List<String> values) {
            addCriterion("linkman_cell not in", values, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellBetween(String value1, String value2) {
            addCriterion("linkman_cell between", value1, value2, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andLinkmanCellNotBetween(String value1, String value2) {
            addCriterion("linkman_cell not between", value1, value2, "linkmanCell");
            return (Criteria) this;
        }

        public Criteria andHomeAddrIsNull() {
            addCriterion("home_addr is null");
            return (Criteria) this;
        }

        public Criteria andHomeAddrIsNotNull() {
            addCriterion("home_addr is not null");
            return (Criteria) this;
        }

        public Criteria andHomeAddrEqualTo(String value) {
            addCriterion("home_addr =", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrNotEqualTo(String value) {
            addCriterion("home_addr <>", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrGreaterThan(String value) {
            addCriterion("home_addr >", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrGreaterThanOrEqualTo(String value) {
            addCriterion("home_addr >=", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrLessThan(String value) {
            addCriterion("home_addr <", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrLessThanOrEqualTo(String value) {
            addCriterion("home_addr <=", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrLike(String value) {
            addCriterion("home_addr like", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrNotLike(String value) {
            addCriterion("home_addr not like", value, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrIn(List<String> values) {
            addCriterion("home_addr in", values, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrNotIn(List<String> values) {
            addCriterion("home_addr not in", values, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrBetween(String value1, String value2) {
            addCriterion("home_addr between", value1, value2, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andHomeAddrNotBetween(String value1, String value2) {
            addCriterion("home_addr not between", value1, value2, "homeAddr");
            return (Criteria) this;
        }

        public Criteria andTelHomeIsNull() {
            addCriterion("tel_home is null");
            return (Criteria) this;
        }

        public Criteria andTelHomeIsNotNull() {
            addCriterion("tel_home is not null");
            return (Criteria) this;
        }

        public Criteria andTelHomeEqualTo(String value) {
            addCriterion("tel_home =", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeNotEqualTo(String value) {
            addCriterion("tel_home <>", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeGreaterThan(String value) {
            addCriterion("tel_home >", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeGreaterThanOrEqualTo(String value) {
            addCriterion("tel_home >=", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeLessThan(String value) {
            addCriterion("tel_home <", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeLessThanOrEqualTo(String value) {
            addCriterion("tel_home <=", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeLike(String value) {
            addCriterion("tel_home like", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeNotLike(String value) {
            addCriterion("tel_home not like", value, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeIn(List<String> values) {
            addCriterion("tel_home in", values, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeNotIn(List<String> values) {
            addCriterion("tel_home not in", values, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeBetween(String value1, String value2) {
            addCriterion("tel_home between", value1, value2, "telHome");
            return (Criteria) this;
        }

        public Criteria andTelHomeNotBetween(String value1, String value2) {
            addCriterion("tel_home not between", value1, value2, "telHome");
            return (Criteria) this;
        }

        public Criteria andMailIsNull() {
            addCriterion("mail is null");
            return (Criteria) this;
        }

        public Criteria andMailIsNotNull() {
            addCriterion("mail is not null");
            return (Criteria) this;
        }

        public Criteria andMailEqualTo(String value) {
            addCriterion("mail =", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailNotEqualTo(String value) {
            addCriterion("mail <>", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailGreaterThan(String value) {
            addCriterion("mail >", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailGreaterThanOrEqualTo(String value) {
            addCriterion("mail >=", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailLessThan(String value) {
            addCriterion("mail <", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailLessThanOrEqualTo(String value) {
            addCriterion("mail <=", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailLike(String value) {
            addCriterion("mail like", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailNotLike(String value) {
            addCriterion("mail not like", value, "mail");
            return (Criteria) this;
        }

        public Criteria andMailIn(List<String> values) {
            addCriterion("mail in", values, "mail");
            return (Criteria) this;
        }

        public Criteria andMailNotIn(List<String> values) {
            addCriterion("mail not in", values, "mail");
            return (Criteria) this;
        }

        public Criteria andMailBetween(String value1, String value2) {
            addCriterion("mail between", value1, value2, "mail");
            return (Criteria) this;
        }

        public Criteria andMailNotBetween(String value1, String value2) {
            addCriterion("mail not between", value1, value2, "mail");
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

        public Criteria andTimeRangeIsNull() {
            addCriterion("time_range is null");
            return (Criteria) this;
        }

        public Criteria andTimeRangeIsNotNull() {
            addCriterion("time_range is not null");
            return (Criteria) this;
        }

        public Criteria andTimeRangeEqualTo(String value) {
            addCriterion("time_range =", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeNotEqualTo(String value) {
            addCriterion("time_range <>", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeGreaterThan(String value) {
            addCriterion("time_range >", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeGreaterThanOrEqualTo(String value) {
            addCriterion("time_range >=", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeLessThan(String value) {
            addCriterion("time_range <", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeLessThanOrEqualTo(String value) {
            addCriterion("time_range <=", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeLike(String value) {
            addCriterion("time_range like", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeNotLike(String value) {
            addCriterion("time_range not like", value, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeIn(List<String> values) {
            addCriterion("time_range in", values, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeNotIn(List<String> values) {
            addCriterion("time_range not in", values, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeBetween(String value1, String value2) {
            addCriterion("time_range between", value1, value2, "timeRange");
            return (Criteria) this;
        }

        public Criteria andTimeRangeNotBetween(String value1, String value2) {
            addCriterion("time_range not between", value1, value2, "timeRange");
            return (Criteria) this;
        }

        public Criteria andPassDateIsNull() {
            addCriterion("pass_date is null");
            return (Criteria) this;
        }

        public Criteria andPassDateIsNotNull() {
            addCriterion("pass_date is not null");
            return (Criteria) this;
        }

        public Criteria andPassDateEqualTo(String value) {
            addCriterion("pass_date =", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateNotEqualTo(String value) {
            addCriterion("pass_date <>", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateGreaterThan(String value) {
            addCriterion("pass_date >", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateGreaterThanOrEqualTo(String value) {
            addCriterion("pass_date >=", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateLessThan(String value) {
            addCriterion("pass_date <", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateLessThanOrEqualTo(String value) {
            addCriterion("pass_date <=", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateLike(String value) {
            addCriterion("pass_date like", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateNotLike(String value) {
            addCriterion("pass_date not like", value, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateIn(List<String> values) {
            addCriterion("pass_date in", values, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateNotIn(List<String> values) {
            addCriterion("pass_date not in", values, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateBetween(String value1, String value2) {
            addCriterion("pass_date between", value1, value2, "passDate");
            return (Criteria) this;
        }

        public Criteria andPassDateNotBetween(String value1, String value2) {
            addCriterion("pass_date not between", value1, value2, "passDate");
            return (Criteria) this;
        }

        public Criteria andApprovalResultIsNull() {
            addCriterion("approval_result is null");
            return (Criteria) this;
        }

        public Criteria andApprovalResultIsNotNull() {
            addCriterion("approval_result is not null");
            return (Criteria) this;
        }

        public Criteria andApprovalResultEqualTo(String value) {
            addCriterion("approval_result =", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultNotEqualTo(String value) {
            addCriterion("approval_result <>", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultGreaterThan(String value) {
            addCriterion("approval_result >", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultGreaterThanOrEqualTo(String value) {
            addCriterion("approval_result >=", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultLessThan(String value) {
            addCriterion("approval_result <", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultLessThanOrEqualTo(String value) {
            addCriterion("approval_result <=", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultLike(String value) {
            addCriterion("approval_result like", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultNotLike(String value) {
            addCriterion("approval_result not like", value, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultIn(List<String> values) {
            addCriterion("approval_result in", values, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultNotIn(List<String> values) {
            addCriterion("approval_result not in", values, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultBetween(String value1, String value2) {
            addCriterion("approval_result between", value1, value2, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andApprovalResultNotBetween(String value1, String value2) {
            addCriterion("approval_result not between", value1, value2, "approvalResult");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateIsNull() {
            addCriterion("loanMaturity_date is null");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateIsNotNull() {
            addCriterion("loanMaturity_date is not null");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateEqualTo(String value) {
            addCriterion("loanMaturity_date =", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateNotEqualTo(String value) {
            addCriterion("loanMaturity_date <>", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateGreaterThan(String value) {
            addCriterion("loanMaturity_date >", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateGreaterThanOrEqualTo(String value) {
            addCriterion("loanMaturity_date >=", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateLessThan(String value) {
            addCriterion("loanMaturity_date <", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateLessThanOrEqualTo(String value) {
            addCriterion("loanMaturity_date <=", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateLike(String value) {
            addCriterion("loanMaturity_date like", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateNotLike(String value) {
            addCriterion("loanMaturity_date not like", value, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateIn(List<String> values) {
            addCriterion("loanMaturity_date in", values, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateNotIn(List<String> values) {
            addCriterion("loanMaturity_date not in", values, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateBetween(String value1, String value2) {
            addCriterion("loanMaturity_date between", value1, value2, "loanmaturityDate");
            return (Criteria) this;
        }

        public Criteria andLoanmaturityDateNotBetween(String value1, String value2) {
            addCriterion("loanMaturity_date not between", value1, value2, "loanmaturityDate");
            return (Criteria) this;
        }
    }

    public static class Criteria extends AbstractGeneratedCriteria {

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