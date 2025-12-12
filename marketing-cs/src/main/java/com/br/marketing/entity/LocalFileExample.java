package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LocalFileExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LocalFileExample() {
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

        public Criteria andFileTypeIsNull() {
            addCriterion("file_type is null");
            return (Criteria) this;
        }

        public Criteria andFileTypeIsNotNull() {
            addCriterion("file_type is not null");
            return (Criteria) this;
        }

        public Criteria andFileTypeEqualTo(String value) {
            addCriterion("file_type =", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotEqualTo(String value) {
            addCriterion("file_type <>", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeGreaterThan(String value) {
            addCriterion("file_type >", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeGreaterThanOrEqualTo(String value) {
            addCriterion("file_type >=", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeLessThan(String value) {
            addCriterion("file_type <", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeLessThanOrEqualTo(String value) {
            addCriterion("file_type <=", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeLike(String value) {
            addCriterion("file_type like", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotLike(String value) {
            addCriterion("file_type not like", value, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeIn(List<String> values) {
            addCriterion("file_type in", values, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotIn(List<String> values) {
            addCriterion("file_type not in", values, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeBetween(String value1, String value2) {
            addCriterion("file_type between", value1, value2, "fileType");
            return (Criteria) this;
        }

        public Criteria andFileTypeNotBetween(String value1, String value2) {
            addCriterion("file_type not between", value1, value2, "fileType");
            return (Criteria) this;
        }

        public Criteria andSrcPathIsNull() {
            addCriterion("src_path is null");
            return (Criteria) this;
        }

        public Criteria andSrcPathIsNotNull() {
            addCriterion("src_path is not null");
            return (Criteria) this;
        }

        public Criteria andSrcPathEqualTo(String value) {
            addCriterion("src_path =", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathNotEqualTo(String value) {
            addCriterion("src_path <>", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathGreaterThan(String value) {
            addCriterion("src_path >", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathGreaterThanOrEqualTo(String value) {
            addCriterion("src_path >=", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathLessThan(String value) {
            addCriterion("src_path <", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathLessThanOrEqualTo(String value) {
            addCriterion("src_path <=", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathLike(String value) {
            addCriterion("src_path like", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathNotLike(String value) {
            addCriterion("src_path not like", value, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathIn(List<String> values) {
            addCriterion("src_path in", values, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathNotIn(List<String> values) {
            addCriterion("src_path not in", values, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathBetween(String value1, String value2) {
            addCriterion("src_path between", value1, value2, "srcPath");
            return (Criteria) this;
        }

        public Criteria andSrcPathNotBetween(String value1, String value2) {
            addCriterion("src_path not between", value1, value2, "srcPath");
            return (Criteria) this;
        }

        public Criteria andFileNameIsNull() {
            addCriterion("file_name is null");
            return (Criteria) this;
        }

        public Criteria andFileNameIsNotNull() {
            addCriterion("file_name is not null");
            return (Criteria) this;
        }

        public Criteria andFileNameEqualTo(String value) {
            addCriterion("file_name =", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotEqualTo(String value) {
            addCriterion("file_name <>", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameGreaterThan(String value) {
            addCriterion("file_name >", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameGreaterThanOrEqualTo(String value) {
            addCriterion("file_name >=", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLessThan(String value) {
            addCriterion("file_name <", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLessThanOrEqualTo(String value) {
            addCriterion("file_name <=", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameLike(String value) {
            addCriterion("file_name like", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotLike(String value) {
            addCriterion("file_name not like", value, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameIn(List<String> values) {
            addCriterion("file_name in", values, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotIn(List<String> values) {
            addCriterion("file_name not in", values, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameBetween(String value1, String value2) {
            addCriterion("file_name between", value1, value2, "fileName");
            return (Criteria) this;
        }

        public Criteria andFileNameNotBetween(String value1, String value2) {
            addCriterion("file_name not between", value1, value2, "fileName");
            return (Criteria) this;
        }

        public Criteria andLocalPathIsNull() {
            addCriterion("local_path is null");
            return (Criteria) this;
        }

        public Criteria andLocalPathIsNotNull() {
            addCriterion("local_path is not null");
            return (Criteria) this;
        }

        public Criteria andLocalPathEqualTo(String value) {
            addCriterion("local_path =", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathNotEqualTo(String value) {
            addCriterion("local_path <>", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathGreaterThan(String value) {
            addCriterion("local_path >", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathGreaterThanOrEqualTo(String value) {
            addCriterion("local_path >=", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathLessThan(String value) {
            addCriterion("local_path <", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathLessThanOrEqualTo(String value) {
            addCriterion("local_path <=", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathLike(String value) {
            addCriterion("local_path like", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathNotLike(String value) {
            addCriterion("local_path not like", value, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathIn(List<String> values) {
            addCriterion("local_path in", values, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathNotIn(List<String> values) {
            addCriterion("local_path not in", values, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathBetween(String value1, String value2) {
            addCriterion("local_path between", value1, value2, "localPath");
            return (Criteria) this;
        }

        public Criteria andLocalPathNotBetween(String value1, String value2) {
            addCriterion("local_path not between", value1, value2, "localPath");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("`status` is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("`status` is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(String value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(String value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(String value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(String value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(String value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(String value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(String value) {
            addCriterion("`status` like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(String value) {
            addCriterion("`status` not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<String> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<String> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(String value1, String value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(String value1, String value2) {
            addCriterion("`status` not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andCompleteIsNull() {
            addCriterion("complete is null");
            return (Criteria) this;
        }

        public Criteria andCompleteIsNotNull() {
            addCriterion("complete is not null");
            return (Criteria) this;
        }

        public Criteria andCompleteEqualTo(String value) {
            addCriterion("complete =", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteNotEqualTo(String value) {
            addCriterion("complete <>", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteGreaterThan(String value) {
            addCriterion("complete >", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteGreaterThanOrEqualTo(String value) {
            addCriterion("complete >=", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteLessThan(String value) {
            addCriterion("complete <", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteLessThanOrEqualTo(String value) {
            addCriterion("complete <=", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteLike(String value) {
            addCriterion("complete like", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteNotLike(String value) {
            addCriterion("complete not like", value, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteIn(List<String> values) {
            addCriterion("complete in", values, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteNotIn(List<String> values) {
            addCriterion("complete not in", values, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteBetween(String value1, String value2) {
            addCriterion("complete between", value1, value2, "complete");
            return (Criteria) this;
        }

        public Criteria andCompleteNotBetween(String value1, String value2) {
            addCriterion("complete not between", value1, value2, "complete");
            return (Criteria) this;
        }

        public Criteria andActualNumberIsNull() {
            addCriterion("actual_number is null");
            return (Criteria) this;
        }

        public Criteria andActualNumberIsNotNull() {
            addCriterion("actual_number is not null");
            return (Criteria) this;
        }

        public Criteria andActualNumberEqualTo(Integer value) {
            addCriterion("actual_number =", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotEqualTo(Integer value) {
            addCriterion("actual_number <>", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberGreaterThan(Integer value) {
            addCriterion("actual_number >", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_number >=", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberLessThan(Integer value) {
            addCriterion("actual_number <", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberLessThanOrEqualTo(Integer value) {
            addCriterion("actual_number <=", value, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberIn(List<Integer> values) {
            addCriterion("actual_number in", values, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotIn(List<Integer> values) {
            addCriterion("actual_number not in", values, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberBetween(Integer value1, Integer value2) {
            addCriterion("actual_number between", value1, value2, "actualNumber");
            return (Criteria) this;
        }

        public Criteria andActualNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_number not between", value1, value2, "actualNumber");
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

        public Criteria andErrorActualNumberIsNull() {
            addCriterion("error_actual_number is null");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberIsNotNull() {
            addCriterion("error_actual_number is not null");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberEqualTo(Integer value) {
            addCriterion("error_actual_number =", value, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberNotEqualTo(Integer value) {
            addCriterion("error_actual_number <>", value, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberGreaterThan(Integer value) {
            addCriterion("error_actual_number >", value, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("error_actual_number >=", value, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberLessThan(Integer value) {
            addCriterion("error_actual_number <", value, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberLessThanOrEqualTo(Integer value) {
            addCriterion("error_actual_number <=", value, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberIn(List<Integer> values) {
            addCriterion("error_actual_number in", values, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberNotIn(List<Integer> values) {
            addCriterion("error_actual_number not in", values, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberBetween(Integer value1, Integer value2) {
            addCriterion("error_actual_number between", value1, value2, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andErrorActualNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("error_actual_number not between", value1, value2, "errorActualNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberIsNull() {
            addCriterion("push_number is null");
            return (Criteria) this;
        }

        public Criteria andPushNumberIsNotNull() {
            addCriterion("push_number is not null");
            return (Criteria) this;
        }

        public Criteria andPushNumberEqualTo(Integer value) {
            addCriterion("push_number =", value, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberNotEqualTo(Integer value) {
            addCriterion("push_number <>", value, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberGreaterThan(Integer value) {
            addCriterion("push_number >", value, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_number >=", value, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberLessThan(Integer value) {
            addCriterion("push_number <", value, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberLessThanOrEqualTo(Integer value) {
            addCriterion("push_number <=", value, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberIn(List<Integer> values) {
            addCriterion("push_number in", values, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberNotIn(List<Integer> values) {
            addCriterion("push_number not in", values, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberBetween(Integer value1, Integer value2) {
            addCriterion("push_number between", value1, value2, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushNumberNotBetween(Integer value1, Integer value2) {
            addCriterion("push_number not between", value1, value2, "pushNumber");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeIsNull() {
            addCriterion("push_start_time is null");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeIsNotNull() {
            addCriterion("push_start_time is not null");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeEqualTo(Date value) {
            addCriterion("push_start_time =", value, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeNotEqualTo(Date value) {
            addCriterion("push_start_time <>", value, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeGreaterThan(Date value) {
            addCriterion("push_start_time >", value, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("push_start_time >=", value, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeLessThan(Date value) {
            addCriterion("push_start_time <", value, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeLessThanOrEqualTo(Date value) {
            addCriterion("push_start_time <=", value, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeIn(List<Date> values) {
            addCriterion("push_start_time in", values, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeNotIn(List<Date> values) {
            addCriterion("push_start_time not in", values, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeBetween(Date value1, Date value2) {
            addCriterion("push_start_time between", value1, value2, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushStartTimeNotBetween(Date value1, Date value2) {
            addCriterion("push_start_time not between", value1, value2, "pushStartTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeIsNull() {
            addCriterion("push_end_time is null");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeIsNotNull() {
            addCriterion("push_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeEqualTo(Date value) {
            addCriterion("push_end_time =", value, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeNotEqualTo(Date value) {
            addCriterion("push_end_time <>", value, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeGreaterThan(Date value) {
            addCriterion("push_end_time >", value, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("push_end_time >=", value, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeLessThan(Date value) {
            addCriterion("push_end_time <", value, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("push_end_time <=", value, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeIn(List<Date> values) {
            addCriterion("push_end_time in", values, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeNotIn(List<Date> values) {
            addCriterion("push_end_time not in", values, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeBetween(Date value1, Date value2) {
            addCriterion("push_end_time between", value1, value2, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("push_end_time not between", value1, value2, "pushEndTime");
            return (Criteria) this;
        }

        public Criteria andPushStatusIsNull() {
            addCriterion("push_status is null");
            return (Criteria) this;
        }

        public Criteria andPushStatusIsNotNull() {
            addCriterion("push_status is not null");
            return (Criteria) this;
        }

        public Criteria andPushStatusEqualTo(String value) {
            addCriterion("push_status =", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotEqualTo(String value) {
            addCriterion("push_status <>", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThan(String value) {
            addCriterion("push_status >", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThanOrEqualTo(String value) {
            addCriterion("push_status >=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThan(String value) {
            addCriterion("push_status <", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThanOrEqualTo(String value) {
            addCriterion("push_status <=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLike(String value) {
            addCriterion("push_status like", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotLike(String value) {
            addCriterion("push_status not like", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusIn(List<String> values) {
            addCriterion("push_status in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotIn(List<String> values) {
            addCriterion("push_status not in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusBetween(String value1, String value2) {
            addCriterion("push_status between", value1, value2, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotBetween(String value1, String value2) {
            addCriterion("push_status not between", value1, value2, "pushStatus");
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

        public Criteria andRetryCountIsNull() {
            addCriterion("retry_count is null");
            return (Criteria) this;
        }

        public Criteria andRetryCountIsNotNull() {
            addCriterion("retry_count is not null");
            return (Criteria) this;
        }

        public Criteria andRetryCountEqualTo(Integer value) {
            addCriterion("retry_count =", value, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountNotEqualTo(Integer value) {
            addCriterion("retry_count <>", value, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountGreaterThan(Integer value) {
            addCriterion("retry_count >", value, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("retry_count >=", value, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountLessThan(Integer value) {
            addCriterion("retry_count <", value, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountLessThanOrEqualTo(Integer value) {
            addCriterion("retry_count <=", value, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountIn(List<Integer> values) {
            addCriterion("retry_count in", values, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountNotIn(List<Integer> values) {
            addCriterion("retry_count not in", values, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountBetween(Integer value1, Integer value2) {
            addCriterion("retry_count between", value1, value2, "retryCount");
            return (Criteria) this;
        }

        public Criteria andRetryCountNotBetween(Integer value1, Integer value2) {
            addCriterion("retry_count not between", value1, value2, "retryCount");
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