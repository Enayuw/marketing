package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LoanSyncConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public LoanSyncConfigExample() {
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

        public Criteria andTypeIsNull() {
            addCriterion("type is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(Integer value) {
            addCriterion("type =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(Integer value) {
            addCriterion("type <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(Integer value) {
            addCriterion("type >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("type >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(Integer value) {
            addCriterion("type <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(Integer value) {
            addCriterion("type <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<Integer> values) {
            addCriterion("type in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<Integer> values) {
            addCriterion("type not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(Integer value1, Integer value2) {
            addCriterion("type between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("type not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andDataTypeIsNull() {
            addCriterion("data_type is null");
            return (Criteria) this;
        }

        public Criteria andDataTypeIsNotNull() {
            addCriterion("data_type is not null");
            return (Criteria) this;
        }

        public Criteria andDataTypeEqualTo(Integer value) {
            addCriterion("data_type =", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotEqualTo(Integer value) {
            addCriterion("data_type <>", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeGreaterThan(Integer value) {
            addCriterion("data_type >", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("data_type >=", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeLessThan(Integer value) {
            addCriterion("data_type <", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeLessThanOrEqualTo(Integer value) {
            addCriterion("data_type <=", value, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeIn(List<Integer> values) {
            addCriterion("data_type in", values, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotIn(List<Integer> values) {
            addCriterion("data_type not in", values, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeBetween(Integer value1, Integer value2) {
            addCriterion("data_type between", value1, value2, "dataType");
            return (Criteria) this;
        }

        public Criteria andDataTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("data_type not between", value1, value2, "dataType");
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

        public Criteria andTargetPathIsNull() {
            addCriterion("target_path is null");
            return (Criteria) this;
        }

        public Criteria andTargetPathIsNotNull() {
            addCriterion("target_path is not null");
            return (Criteria) this;
        }

        public Criteria andTargetPathEqualTo(String value) {
            addCriterion("target_path =", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathNotEqualTo(String value) {
            addCriterion("target_path <>", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathGreaterThan(String value) {
            addCriterion("target_path >", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathGreaterThanOrEqualTo(String value) {
            addCriterion("target_path >=", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathLessThan(String value) {
            addCriterion("target_path <", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathLessThanOrEqualTo(String value) {
            addCriterion("target_path <=", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathLike(String value) {
            addCriterion("target_path like", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathNotLike(String value) {
            addCriterion("target_path not like", value, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathIn(List<String> values) {
            addCriterion("target_path in", values, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathNotIn(List<String> values) {
            addCriterion("target_path not in", values, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathBetween(String value1, String value2) {
            addCriterion("target_path between", value1, value2, "targetPath");
            return (Criteria) this;
        }

        public Criteria andTargetPathNotBetween(String value1, String value2) {
            addCriterion("target_path not between", value1, value2, "targetPath");
            return (Criteria) this;
        }

        public Criteria andSuffixIsNull() {
            addCriterion("suffix is null");
            return (Criteria) this;
        }

        public Criteria andSuffixIsNotNull() {
            addCriterion("suffix is not null");
            return (Criteria) this;
        }

        public Criteria andSuffixEqualTo(String value) {
            addCriterion("suffix =", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixNotEqualTo(String value) {
            addCriterion("suffix <>", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixGreaterThan(String value) {
            addCriterion("suffix >", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixGreaterThanOrEqualTo(String value) {
            addCriterion("suffix >=", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixLessThan(String value) {
            addCriterion("suffix <", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixLessThanOrEqualTo(String value) {
            addCriterion("suffix <=", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixLike(String value) {
            addCriterion("suffix like", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixNotLike(String value) {
            addCriterion("suffix not like", value, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixIn(List<String> values) {
            addCriterion("suffix in", values, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixNotIn(List<String> values) {
            addCriterion("suffix not in", values, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixBetween(String value1, String value2) {
            addCriterion("suffix between", value1, value2, "suffix");
            return (Criteria) this;
        }

        public Criteria andSuffixNotBetween(String value1, String value2) {
            addCriterion("suffix not between", value1, value2, "suffix");
            return (Criteria) this;
        }

        public Criteria andCheckFinishIsNull() {
            addCriterion("check_finish is null");
            return (Criteria) this;
        }

        public Criteria andCheckFinishIsNotNull() {
            addCriterion("check_finish is not null");
            return (Criteria) this;
        }

        public Criteria andCheckFinishEqualTo(Integer value) {
            addCriterion("check_finish =", value, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishNotEqualTo(Integer value) {
            addCriterion("check_finish <>", value, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishGreaterThan(Integer value) {
            addCriterion("check_finish >", value, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishGreaterThanOrEqualTo(Integer value) {
            addCriterion("check_finish >=", value, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishLessThan(Integer value) {
            addCriterion("check_finish <", value, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishLessThanOrEqualTo(Integer value) {
            addCriterion("check_finish <=", value, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishIn(List<Integer> values) {
            addCriterion("check_finish in", values, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishNotIn(List<Integer> values) {
            addCriterion("check_finish not in", values, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishBetween(Integer value1, Integer value2) {
            addCriterion("check_finish between", value1, value2, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckFinishNotBetween(Integer value1, Integer value2) {
            addCriterion("check_finish not between", value1, value2, "checkFinish");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessIsNull() {
            addCriterion("check_success is null");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessIsNotNull() {
            addCriterion("check_success is not null");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessEqualTo(Integer value) {
            addCriterion("check_success =", value, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessNotEqualTo(Integer value) {
            addCriterion("check_success <>", value, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessGreaterThan(Integer value) {
            addCriterion("check_success >", value, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessGreaterThanOrEqualTo(Integer value) {
            addCriterion("check_success >=", value, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessLessThan(Integer value) {
            addCriterion("check_success <", value, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessLessThanOrEqualTo(Integer value) {
            addCriterion("check_success <=", value, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessIn(List<Integer> values) {
            addCriterion("check_success in", values, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessNotIn(List<Integer> values) {
            addCriterion("check_success not in", values, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessBetween(Integer value1, Integer value2) {
            addCriterion("check_success between", value1, value2, "checkSuccess");
            return (Criteria) this;
        }

        public Criteria andCheckSuccessNotBetween(Integer value1, Integer value2) {
            addCriterion("check_success not between", value1, value2, "checkSuccess");
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

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNull() {
            addCriterion("remark is null");
            return (Criteria) this;
        }

        public Criteria andRemarkIsNotNull() {
            addCriterion("remark is not null");
            return (Criteria) this;
        }

        public Criteria andRemarkEqualTo(String value) {
            addCriterion("remark =", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotEqualTo(String value) {
            addCriterion("remark <>", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThan(String value) {
            addCriterion("remark >", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkGreaterThanOrEqualTo(String value) {
            addCriterion("remark >=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThan(String value) {
            addCriterion("remark <", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLessThanOrEqualTo(String value) {
            addCriterion("remark <=", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkLike(String value) {
            addCriterion("remark like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotLike(String value) {
            addCriterion("remark not like", value, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkIn(List<String> values) {
            addCriterion("remark in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotIn(List<String> values) {
            addCriterion("remark not in", values, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkBetween(String value1, String value2) {
            addCriterion("remark between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andRemarkNotBetween(String value1, String value2) {
            addCriterion("remark not between", value1, value2, "remark");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostIsNull() {
            addCriterion("src_sftp_host is null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostIsNotNull() {
            addCriterion("src_sftp_host is not null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostEqualTo(String value) {
            addCriterion("src_sftp_host =", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostNotEqualTo(String value) {
            addCriterion("src_sftp_host <>", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostGreaterThan(String value) {
            addCriterion("src_sftp_host >", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostGreaterThanOrEqualTo(String value) {
            addCriterion("src_sftp_host >=", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostLessThan(String value) {
            addCriterion("src_sftp_host <", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostLessThanOrEqualTo(String value) {
            addCriterion("src_sftp_host <=", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostLike(String value) {
            addCriterion("src_sftp_host like", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostNotLike(String value) {
            addCriterion("src_sftp_host not like", value, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostIn(List<String> values) {
            addCriterion("src_sftp_host in", values, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostNotIn(List<String> values) {
            addCriterion("src_sftp_host not in", values, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostBetween(String value1, String value2) {
            addCriterion("src_sftp_host between", value1, value2, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpHostNotBetween(String value1, String value2) {
            addCriterion("src_sftp_host not between", value1, value2, "srcSftpHost");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortIsNull() {
            addCriterion("src_sftp_port is null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortIsNotNull() {
            addCriterion("src_sftp_port is not null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortEqualTo(Integer value) {
            addCriterion("src_sftp_port =", value, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortNotEqualTo(Integer value) {
            addCriterion("src_sftp_port <>", value, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortGreaterThan(Integer value) {
            addCriterion("src_sftp_port >", value, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("src_sftp_port >=", value, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortLessThan(Integer value) {
            addCriterion("src_sftp_port <", value, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortLessThanOrEqualTo(Integer value) {
            addCriterion("src_sftp_port <=", value, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortIn(List<Integer> values) {
            addCriterion("src_sftp_port in", values, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortNotIn(List<Integer> values) {
            addCriterion("src_sftp_port not in", values, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortBetween(Integer value1, Integer value2) {
            addCriterion("src_sftp_port between", value1, value2, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPortNotBetween(Integer value1, Integer value2) {
            addCriterion("src_sftp_port not between", value1, value2, "srcSftpPort");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserIsNull() {
            addCriterion("src_sftp_user is null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserIsNotNull() {
            addCriterion("src_sftp_user is not null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserEqualTo(String value) {
            addCriterion("src_sftp_user =", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserNotEqualTo(String value) {
            addCriterion("src_sftp_user <>", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserGreaterThan(String value) {
            addCriterion("src_sftp_user >", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserGreaterThanOrEqualTo(String value) {
            addCriterion("src_sftp_user >=", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserLessThan(String value) {
            addCriterion("src_sftp_user <", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserLessThanOrEqualTo(String value) {
            addCriterion("src_sftp_user <=", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserLike(String value) {
            addCriterion("src_sftp_user like", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserNotLike(String value) {
            addCriterion("src_sftp_user not like", value, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserIn(List<String> values) {
            addCriterion("src_sftp_user in", values, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserNotIn(List<String> values) {
            addCriterion("src_sftp_user not in", values, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserBetween(String value1, String value2) {
            addCriterion("src_sftp_user between", value1, value2, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpUserNotBetween(String value1, String value2) {
            addCriterion("src_sftp_user not between", value1, value2, "srcSftpUser");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdIsNull() {
            addCriterion("src_sftp_pwd is null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdIsNotNull() {
            addCriterion("src_sftp_pwd is not null");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdEqualTo(String value) {
            addCriterion("src_sftp_pwd =", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdNotEqualTo(String value) {
            addCriterion("src_sftp_pwd <>", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdGreaterThan(String value) {
            addCriterion("src_sftp_pwd >", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdGreaterThanOrEqualTo(String value) {
            addCriterion("src_sftp_pwd >=", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdLessThan(String value) {
            addCriterion("src_sftp_pwd <", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdLessThanOrEqualTo(String value) {
            addCriterion("src_sftp_pwd <=", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdLike(String value) {
            addCriterion("src_sftp_pwd like", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdNotLike(String value) {
            addCriterion("src_sftp_pwd not like", value, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdIn(List<String> values) {
            addCriterion("src_sftp_pwd in", values, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdNotIn(List<String> values) {
            addCriterion("src_sftp_pwd not in", values, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdBetween(String value1, String value2) {
            addCriterion("src_sftp_pwd between", value1, value2, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andSrcSftpPwdNotBetween(String value1, String value2) {
            addCriterion("src_sftp_pwd not between", value1, value2, "srcSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostIsNull() {
            addCriterion("target_sftp_host is null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostIsNotNull() {
            addCriterion("target_sftp_host is not null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostEqualTo(String value) {
            addCriterion("target_sftp_host =", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostNotEqualTo(String value) {
            addCriterion("target_sftp_host <>", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostGreaterThan(String value) {
            addCriterion("target_sftp_host >", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostGreaterThanOrEqualTo(String value) {
            addCriterion("target_sftp_host >=", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostLessThan(String value) {
            addCriterion("target_sftp_host <", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostLessThanOrEqualTo(String value) {
            addCriterion("target_sftp_host <=", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostLike(String value) {
            addCriterion("target_sftp_host like", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostNotLike(String value) {
            addCriterion("target_sftp_host not like", value, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostIn(List<String> values) {
            addCriterion("target_sftp_host in", values, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostNotIn(List<String> values) {
            addCriterion("target_sftp_host not in", values, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostBetween(String value1, String value2) {
            addCriterion("target_sftp_host between", value1, value2, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpHostNotBetween(String value1, String value2) {
            addCriterion("target_sftp_host not between", value1, value2, "targetSftpHost");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortIsNull() {
            addCriterion("target_sftp_port is null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortIsNotNull() {
            addCriterion("target_sftp_port is not null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortEqualTo(Integer value) {
            addCriterion("target_sftp_port =", value, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortNotEqualTo(Integer value) {
            addCriterion("target_sftp_port <>", value, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortGreaterThan(Integer value) {
            addCriterion("target_sftp_port >", value, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("target_sftp_port >=", value, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortLessThan(Integer value) {
            addCriterion("target_sftp_port <", value, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortLessThanOrEqualTo(Integer value) {
            addCriterion("target_sftp_port <=", value, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortIn(List<Integer> values) {
            addCriterion("target_sftp_port in", values, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortNotIn(List<Integer> values) {
            addCriterion("target_sftp_port not in", values, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortBetween(Integer value1, Integer value2) {
            addCriterion("target_sftp_port between", value1, value2, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPortNotBetween(Integer value1, Integer value2) {
            addCriterion("target_sftp_port not between", value1, value2, "targetSftpPort");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserIsNull() {
            addCriterion("target_sftp_user is null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserIsNotNull() {
            addCriterion("target_sftp_user is not null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserEqualTo(String value) {
            addCriterion("target_sftp_user =", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserNotEqualTo(String value) {
            addCriterion("target_sftp_user <>", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserGreaterThan(String value) {
            addCriterion("target_sftp_user >", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserGreaterThanOrEqualTo(String value) {
            addCriterion("target_sftp_user >=", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserLessThan(String value) {
            addCriterion("target_sftp_user <", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserLessThanOrEqualTo(String value) {
            addCriterion("target_sftp_user <=", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserLike(String value) {
            addCriterion("target_sftp_user like", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserNotLike(String value) {
            addCriterion("target_sftp_user not like", value, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserIn(List<String> values) {
            addCriterion("target_sftp_user in", values, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserNotIn(List<String> values) {
            addCriterion("target_sftp_user not in", values, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserBetween(String value1, String value2) {
            addCriterion("target_sftp_user between", value1, value2, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpUserNotBetween(String value1, String value2) {
            addCriterion("target_sftp_user not between", value1, value2, "targetSftpUser");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdIsNull() {
            addCriterion("target_sftp_pwd is null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdIsNotNull() {
            addCriterion("target_sftp_pwd is not null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdEqualTo(String value) {
            addCriterion("target_sftp_pwd =", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdNotEqualTo(String value) {
            addCriterion("target_sftp_pwd <>", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdGreaterThan(String value) {
            addCriterion("target_sftp_pwd >", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdGreaterThanOrEqualTo(String value) {
            addCriterion("target_sftp_pwd >=", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdLessThan(String value) {
            addCriterion("target_sftp_pwd <", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdLessThanOrEqualTo(String value) {
            addCriterion("target_sftp_pwd <=", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdLike(String value) {
            addCriterion("target_sftp_pwd like", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdNotLike(String value) {
            addCriterion("target_sftp_pwd not like", value, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdIn(List<String> values) {
            addCriterion("target_sftp_pwd in", values, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdNotIn(List<String> values) {
            addCriterion("target_sftp_pwd not in", values, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdBetween(String value1, String value2) {
            addCriterion("target_sftp_pwd between", value1, value2, "targetSftpPwd");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPwdNotBetween(String value1, String value2) {
            addCriterion("target_sftp_pwd not between", value1, value2, "targetSftpPwd");
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

        public Criteria andSrcTypeIsNull() {
            addCriterion("src_type is null");
            return (Criteria) this;
        }

        public Criteria andSrcTypeIsNotNull() {
            addCriterion("src_type is not null");
            return (Criteria) this;
        }

        public Criteria andSrcTypeEqualTo(String value) {
            addCriterion("src_type =", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeNotEqualTo(String value) {
            addCriterion("src_type <>", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeGreaterThan(String value) {
            addCriterion("src_type >", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeGreaterThanOrEqualTo(String value) {
            addCriterion("src_type >=", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeLessThan(String value) {
            addCriterion("src_type <", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeLessThanOrEqualTo(String value) {
            addCriterion("src_type <=", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeLike(String value) {
            addCriterion("src_type like", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeNotLike(String value) {
            addCriterion("src_type not like", value, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeIn(List<String> values) {
            addCriterion("src_type in", values, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeNotIn(List<String> values) {
            addCriterion("src_type not in", values, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeBetween(String value1, String value2) {
            addCriterion("src_type between", value1, value2, "srcType");
            return (Criteria) this;
        }

        public Criteria andSrcTypeNotBetween(String value1, String value2) {
            addCriterion("src_type not between", value1, value2, "srcType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIsNull() {
            addCriterion("target_type is null");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIsNotNull() {
            addCriterion("target_type is not null");
            return (Criteria) this;
        }

        public Criteria andTargetTypeEqualTo(String value) {
            addCriterion("target_type =", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotEqualTo(String value) {
            addCriterion("target_type <>", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThan(String value) {
            addCriterion("target_type >", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeGreaterThanOrEqualTo(String value) {
            addCriterion("target_type >=", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThan(String value) {
            addCriterion("target_type <", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeLessThanOrEqualTo(String value) {
            addCriterion("target_type <=", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeLike(String value) {
            addCriterion("target_type like", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotLike(String value) {
            addCriterion("target_type not like", value, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeIn(List<String> values) {
            addCriterion("target_type in", values, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotIn(List<String> values) {
            addCriterion("target_type not in", values, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeBetween(String value1, String value2) {
            addCriterion("target_type between", value1, value2, "targetType");
            return (Criteria) this;
        }

        public Criteria andTargetTypeNotBetween(String value1, String value2) {
            addCriterion("target_type not between", value1, value2, "targetType");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeIsNull() {
            addCriterion("exclusion_time is null");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeIsNotNull() {
            addCriterion("exclusion_time is not null");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeEqualTo(String value) {
            addCriterion("exclusion_time =", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeNotEqualTo(String value) {
            addCriterion("exclusion_time <>", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeGreaterThan(String value) {
            addCriterion("exclusion_time >", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeGreaterThanOrEqualTo(String value) {
            addCriterion("exclusion_time >=", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeLessThan(String value) {
            addCriterion("exclusion_time <", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeLessThanOrEqualTo(String value) {
            addCriterion("exclusion_time <=", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeLike(String value) {
            addCriterion("exclusion_time like", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeNotLike(String value) {
            addCriterion("exclusion_time not like", value, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeIn(List<String> values) {
            addCriterion("exclusion_time in", values, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeNotIn(List<String> values) {
            addCriterion("exclusion_time not in", values, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeBetween(String value1, String value2) {
            addCriterion("exclusion_time between", value1, value2, "exclusionTime");
            return (Criteria) this;
        }

        public Criteria andExclusionTimeNotBetween(String value1, String value2) {
            addCriterion("exclusion_time not between", value1, value2, "exclusionTime");
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