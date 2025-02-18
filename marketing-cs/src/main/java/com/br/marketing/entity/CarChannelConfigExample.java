package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CarChannelConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public CarChannelConfigExample() {
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

        public Criteria andCidIsNull() {
            addCriterion("cid is null");
            return (Criteria) this;
        }

        public Criteria andCidIsNotNull() {
            addCriterion("cid is not null");
            return (Criteria) this;
        }

        public Criteria andCidEqualTo(String value) {
            addCriterion("cid =", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotEqualTo(String value) {
            addCriterion("cid <>", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidGreaterThan(String value) {
            addCriterion("cid >", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidGreaterThanOrEqualTo(String value) {
            addCriterion("cid >=", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLessThan(String value) {
            addCriterion("cid <", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLessThanOrEqualTo(String value) {
            addCriterion("cid <=", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidLike(String value) {
            addCriterion("cid like", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotLike(String value) {
            addCriterion("cid not like", value, "cid");
            return (Criteria) this;
        }

        public Criteria andCidIn(List<String> values) {
            addCriterion("cid in", values, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotIn(List<String> values) {
            addCriterion("cid not in", values, "cid");
            return (Criteria) this;
        }

        public Criteria andCidBetween(String value1, String value2) {
            addCriterion("cid between", value1, value2, "cid");
            return (Criteria) this;
        }

        public Criteria andCidNotBetween(String value1, String value2) {
            addCriterion("cid not between", value1, value2, "cid");
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

        public Criteria andNameIsNull() {
            addCriterion("`name` is null");
            return (Criteria) this;
        }

        public Criteria andNameIsNotNull() {
            addCriterion("`name` is not null");
            return (Criteria) this;
        }

        public Criteria andNameEqualTo(String value) {
            addCriterion("`name` =", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotEqualTo(String value) {
            addCriterion("`name` <>", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThan(String value) {
            addCriterion("`name` >", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameGreaterThanOrEqualTo(String value) {
            addCriterion("`name` >=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThan(String value) {
            addCriterion("`name` <", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLessThanOrEqualTo(String value) {
            addCriterion("`name` <=", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameLike(String value) {
            addCriterion("`name` like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotLike(String value) {
            addCriterion("`name` not like", value, "name");
            return (Criteria) this;
        }

        public Criteria andNameIn(List<String> values) {
            addCriterion("`name` in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotIn(List<String> values) {
            addCriterion("`name` not in", values, "name");
            return (Criteria) this;
        }

        public Criteria andNameBetween(String value1, String value2) {
            addCriterion("`name` between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andNameNotBetween(String value1, String value2) {
            addCriterion("`name` not between", value1, value2, "name");
            return (Criteria) this;
        }

        public Criteria andOrderIsNull() {
            addCriterion("`order` is null");
            return (Criteria) this;
        }

        public Criteria andOrderIsNotNull() {
            addCriterion("`order` is not null");
            return (Criteria) this;
        }

        public Criteria andOrderEqualTo(String value) {
            addCriterion("`order` =", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotEqualTo(String value) {
            addCriterion("`order` <>", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderGreaterThan(String value) {
            addCriterion("`order` >", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderGreaterThanOrEqualTo(String value) {
            addCriterion("`order` >=", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLessThan(String value) {
            addCriterion("`order` <", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLessThanOrEqualTo(String value) {
            addCriterion("`order` <=", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderLike(String value) {
            addCriterion("`order` like", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotLike(String value) {
            addCriterion("`order` not like", value, "order");
            return (Criteria) this;
        }

        public Criteria andOrderIn(List<String> values) {
            addCriterion("`order` in", values, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotIn(List<String> values) {
            addCriterion("`order` not in", values, "order");
            return (Criteria) this;
        }

        public Criteria andOrderBetween(String value1, String value2) {
            addCriterion("`order` between", value1, value2, "order");
            return (Criteria) this;
        }

        public Criteria andOrderNotBetween(String value1, String value2) {
            addCriterion("`order` not between", value1, value2, "order");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoIsNull() {
            addCriterion("strategy_config_info is null");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoIsNotNull() {
            addCriterion("strategy_config_info is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoEqualTo(String value) {
            addCriterion("strategy_config_info =", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoNotEqualTo(String value) {
            addCriterion("strategy_config_info <>", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoGreaterThan(String value) {
            addCriterion("strategy_config_info >", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_config_info >=", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoLessThan(String value) {
            addCriterion("strategy_config_info <", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoLessThanOrEqualTo(String value) {
            addCriterion("strategy_config_info <=", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoLike(String value) {
            addCriterion("strategy_config_info like", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoNotLike(String value) {
            addCriterion("strategy_config_info not like", value, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoIn(List<String> values) {
            addCriterion("strategy_config_info in", values, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoNotIn(List<String> values) {
            addCriterion("strategy_config_info not in", values, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoBetween(String value1, String value2) {
            addCriterion("strategy_config_info between", value1, value2, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyConfigInfoNotBetween(String value1, String value2) {
            addCriterion("strategy_config_info not between", value1, value2, "strategyConfigInfo");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerIsNull() {
            addCriterion("strategy_fitler is null");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerIsNotNull() {
            addCriterion("strategy_fitler is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerEqualTo(String value) {
            addCriterion("strategy_fitler =", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerNotEqualTo(String value) {
            addCriterion("strategy_fitler <>", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerGreaterThan(String value) {
            addCriterion("strategy_fitler >", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_fitler >=", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerLessThan(String value) {
            addCriterion("strategy_fitler <", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerLessThanOrEqualTo(String value) {
            addCriterion("strategy_fitler <=", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerLike(String value) {
            addCriterion("strategy_fitler like", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerNotLike(String value) {
            addCriterion("strategy_fitler not like", value, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerIn(List<String> values) {
            addCriterion("strategy_fitler in", values, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerNotIn(List<String> values) {
            addCriterion("strategy_fitler not in", values, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerBetween(String value1, String value2) {
            addCriterion("strategy_fitler between", value1, value2, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyFitlerNotBetween(String value1, String value2) {
            addCriterion("strategy_fitler not between", value1, value2, "strategyFitler");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchIsNull() {
            addCriterion("strategy_match is null");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchIsNotNull() {
            addCriterion("strategy_match is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchEqualTo(String value) {
            addCriterion("strategy_match =", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchNotEqualTo(String value) {
            addCriterion("strategy_match <>", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchGreaterThan(String value) {
            addCriterion("strategy_match >", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_match >=", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchLessThan(String value) {
            addCriterion("strategy_match <", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchLessThanOrEqualTo(String value) {
            addCriterion("strategy_match <=", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchLike(String value) {
            addCriterion("strategy_match like", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchNotLike(String value) {
            addCriterion("strategy_match not like", value, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchIn(List<String> values) {
            addCriterion("strategy_match in", values, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchNotIn(List<String> values) {
            addCriterion("strategy_match not in", values, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchBetween(String value1, String value2) {
            addCriterion("strategy_match between", value1, value2, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyMatchNotBetween(String value1, String value2) {
            addCriterion("strategy_match not between", value1, value2, "strategyMatch");
            return (Criteria) this;
        }

        public Criteria andStrategyPushIsNull() {
            addCriterion("strategy_push is null");
            return (Criteria) this;
        }

        public Criteria andStrategyPushIsNotNull() {
            addCriterion("strategy_push is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyPushEqualTo(String value) {
            addCriterion("strategy_push =", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushNotEqualTo(String value) {
            addCriterion("strategy_push <>", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushGreaterThan(String value) {
            addCriterion("strategy_push >", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_push >=", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushLessThan(String value) {
            addCriterion("strategy_push <", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushLessThanOrEqualTo(String value) {
            addCriterion("strategy_push <=", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushLike(String value) {
            addCriterion("strategy_push like", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushNotLike(String value) {
            addCriterion("strategy_push not like", value, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushIn(List<String> values) {
            addCriterion("strategy_push in", values, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushNotIn(List<String> values) {
            addCriterion("strategy_push not in", values, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushBetween(String value1, String value2) {
            addCriterion("strategy_push between", value1, value2, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyPushNotBetween(String value1, String value2) {
            addCriterion("strategy_push not between", value1, value2, "strategyPush");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackIsNull() {
            addCriterion("strategy_callback is null");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackIsNotNull() {
            addCriterion("strategy_callback is not null");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackEqualTo(String value) {
            addCriterion("strategy_callback =", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackNotEqualTo(String value) {
            addCriterion("strategy_callback <>", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackGreaterThan(String value) {
            addCriterion("strategy_callback >", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackGreaterThanOrEqualTo(String value) {
            addCriterion("strategy_callback >=", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackLessThan(String value) {
            addCriterion("strategy_callback <", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackLessThanOrEqualTo(String value) {
            addCriterion("strategy_callback <=", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackLike(String value) {
            addCriterion("strategy_callback like", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackNotLike(String value) {
            addCriterion("strategy_callback not like", value, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackIn(List<String> values) {
            addCriterion("strategy_callback in", values, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackNotIn(List<String> values) {
            addCriterion("strategy_callback not in", values, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackBetween(String value1, String value2) {
            addCriterion("strategy_callback between", value1, value2, "strategyCallback");
            return (Criteria) this;
        }

        public Criteria andStrategyCallbackNotBetween(String value1, String value2) {
            addCriterion("strategy_callback not between", value1, value2, "strategyCallback");
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