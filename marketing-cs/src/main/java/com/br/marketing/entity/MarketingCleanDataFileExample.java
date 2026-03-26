package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingCleanDataFileExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingCleanDataFileExample() {
        oredCriteria = new ArrayList<>();
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
            criteria = new ArrayList<>();
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

        public Criteria andDbOperateTypeIsNull() {
            addCriterion("db_operate_type is null");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeIsNotNull() {
            addCriterion("db_operate_type is not null");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeEqualTo(Integer value) {
            addCriterion("db_operate_type =", value, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeNotEqualTo(Integer value) {
            addCriterion("db_operate_type <>", value, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeGreaterThan(Integer value) {
            addCriterion("db_operate_type >", value, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("db_operate_type >=", value, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeLessThan(Integer value) {
            addCriterion("db_operate_type <", value, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeLessThanOrEqualTo(Integer value) {
            addCriterion("db_operate_type <=", value, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeIn(List<Integer> values) {
            addCriterion("db_operate_type in", values, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeNotIn(List<Integer> values) {
            addCriterion("db_operate_type not in", values, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeBetween(Integer value1, Integer value2) {
            addCriterion("db_operate_type between", value1, value2, "dbOperateType");
            return (Criteria) this;
        }

        public Criteria andDbOperateTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("db_operate_type not between", value1, value2, "dbOperateType");
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

        public Criteria andZipNameIsNull() {
            addCriterion("zip_name is null");
            return (Criteria) this;
        }

        public Criteria andZipNameIsNotNull() {
            addCriterion("zip_name is not null");
            return (Criteria) this;
        }

        public Criteria andZipNameEqualTo(String value) {
            addCriterion("zip_name =", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameNotEqualTo(String value) {
            addCriterion("zip_name <>", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameGreaterThan(String value) {
            addCriterion("zip_name >", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameGreaterThanOrEqualTo(String value) {
            addCriterion("zip_name >=", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameLessThan(String value) {
            addCriterion("zip_name <", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameLessThanOrEqualTo(String value) {
            addCriterion("zip_name <=", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameLike(String value) {
            addCriterion("zip_name like", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameNotLike(String value) {
            addCriterion("zip_name not like", value, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameIn(List<String> values) {
            addCriterion("zip_name in", values, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameNotIn(List<String> values) {
            addCriterion("zip_name not in", values, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameBetween(String value1, String value2) {
            addCriterion("zip_name between", value1, value2, "zipName");
            return (Criteria) this;
        }

        public Criteria andZipNameNotBetween(String value1, String value2) {
            addCriterion("zip_name not between", value1, value2, "zipName");
            return (Criteria) this;
        }

        public Criteria andFileHeaderIsNull() {
            addCriterion("file_header is null");
            return (Criteria) this;
        }

        public Criteria andFileHeaderIsNotNull() {
            addCriterion("file_header is not null");
            return (Criteria) this;
        }

        public Criteria andFileHeaderEqualTo(String value) {
            addCriterion("file_header =", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotEqualTo(String value) {
            addCriterion("file_header <>", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderGreaterThan(String value) {
            addCriterion("file_header >", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderGreaterThanOrEqualTo(String value) {
            addCriterion("file_header >=", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderLessThan(String value) {
            addCriterion("file_header <", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderLessThanOrEqualTo(String value) {
            addCriterion("file_header <=", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderLike(String value) {
            addCriterion("file_header like", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotLike(String value) {
            addCriterion("file_header not like", value, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderIn(List<String> values) {
            addCriterion("file_header in", values, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotIn(List<String> values) {
            addCriterion("file_header not in", values, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderBetween(String value1, String value2) {
            addCriterion("file_header between", value1, value2, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andFileHeaderNotBetween(String value1, String value2) {
            addCriterion("file_header not between", value1, value2, "fileHeader");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersIsNull() {
            addCriterion("virtual_headers is null");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersIsNotNull() {
            addCriterion("virtual_headers is not null");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersEqualTo(String value) {
            addCriterion("virtual_headers =", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersNotEqualTo(String value) {
            addCriterion("virtual_headers <>", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersGreaterThan(String value) {
            addCriterion("virtual_headers >", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersGreaterThanOrEqualTo(String value) {
            addCriterion("virtual_headers >=", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersLessThan(String value) {
            addCriterion("virtual_headers <", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersLessThanOrEqualTo(String value) {
            addCriterion("virtual_headers <=", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersLike(String value) {
            addCriterion("virtual_headers like", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersNotLike(String value) {
            addCriterion("virtual_headers not like", value, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersIn(List<String> values) {
            addCriterion("virtual_headers in", values, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersNotIn(List<String> values) {
            addCriterion("virtual_headers not in", values, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersBetween(String value1, String value2) {
            addCriterion("virtual_headers between", value1, value2, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andVirtualHeadersNotBetween(String value1, String value2) {
            addCriterion("virtual_headers not between", value1, value2, "virtualHeaders");
            return (Criteria) this;
        }

        public Criteria andFileDataIsNull() {
            addCriterion("file_data is null");
            return (Criteria) this;
        }

        public Criteria andFileDataIsNotNull() {
            addCriterion("file_data is not null");
            return (Criteria) this;
        }

        public Criteria andFileDataEqualTo(String value) {
            addCriterion("file_data =", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataNotEqualTo(String value) {
            addCriterion("file_data <>", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataGreaterThan(String value) {
            addCriterion("file_data >", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataGreaterThanOrEqualTo(String value) {
            addCriterion("file_data >=", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataLessThan(String value) {
            addCriterion("file_data <", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataLessThanOrEqualTo(String value) {
            addCriterion("file_data <=", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataLike(String value) {
            addCriterion("file_data like", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataNotLike(String value) {
            addCriterion("file_data not like", value, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataIn(List<String> values) {
            addCriterion("file_data in", values, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataNotIn(List<String> values) {
            addCriterion("file_data not in", values, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataBetween(String value1, String value2) {
            addCriterion("file_data between", value1, value2, "fileData");
            return (Criteria) this;
        }

        public Criteria andFileDataNotBetween(String value1, String value2) {
            addCriterion("file_data not between", value1, value2, "fileData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataIsNull() {
            addCriterion("test_run_data is null");
            return (Criteria) this;
        }

        public Criteria andTestRunDataIsNotNull() {
            addCriterion("test_run_data is not null");
            return (Criteria) this;
        }

        public Criteria andTestRunDataEqualTo(String value) {
            addCriterion("test_run_data =", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataNotEqualTo(String value) {
            addCriterion("test_run_data <>", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataGreaterThan(String value) {
            addCriterion("test_run_data >", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataGreaterThanOrEqualTo(String value) {
            addCriterion("test_run_data >=", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataLessThan(String value) {
            addCriterion("test_run_data <", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataLessThanOrEqualTo(String value) {
            addCriterion("test_run_data <=", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataLike(String value) {
            addCriterion("test_run_data like", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataNotLike(String value) {
            addCriterion("test_run_data not like", value, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataIn(List<String> values) {
            addCriterion("test_run_data in", values, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataNotIn(List<String> values) {
            addCriterion("test_run_data not in", values, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataBetween(String value1, String value2) {
            addCriterion("test_run_data between", value1, value2, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTestRunDataNotBetween(String value1, String value2) {
            addCriterion("test_run_data not between", value1, value2, "testRunData");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathIsNull() {
            addCriterion("target_sftp_path is null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathIsNotNull() {
            addCriterion("target_sftp_path is not null");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathEqualTo(String value) {
            addCriterion("target_sftp_path =", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathNotEqualTo(String value) {
            addCriterion("target_sftp_path <>", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathGreaterThan(String value) {
            addCriterion("target_sftp_path >", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathGreaterThanOrEqualTo(String value) {
            addCriterion("target_sftp_path >=", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathLessThan(String value) {
            addCriterion("target_sftp_path <", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathLessThanOrEqualTo(String value) {
            addCriterion("target_sftp_path <=", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathLike(String value) {
            addCriterion("target_sftp_path like", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathNotLike(String value) {
            addCriterion("target_sftp_path not like", value, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathIn(List<String> values) {
            addCriterion("target_sftp_path in", values, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathNotIn(List<String> values) {
            addCriterion("target_sftp_path not in", values, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathBetween(String value1, String value2) {
            addCriterion("target_sftp_path between", value1, value2, "targetSftpPath");
            return (Criteria) this;
        }

        public Criteria andTargetSftpPathNotBetween(String value1, String value2) {
            addCriterion("target_sftp_path not between", value1, value2, "targetSftpPath");
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

        public Criteria andStatusEqualTo(Integer value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Integer value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Integer value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Integer value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Integer> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Integer> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("`status` not between", value1, value2, "status");
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

        public Criteria andSyncConfigIdIsNull() {
            addCriterion("sync_config_id is null");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdIsNotNull() {
            addCriterion("sync_config_id is not null");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdEqualTo(Long value) {
            addCriterion("sync_config_id =", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdNotEqualTo(Long value) {
            addCriterion("sync_config_id <>", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdGreaterThan(Long value) {
            addCriterion("sync_config_id >", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdGreaterThanOrEqualTo(Long value) {
            addCriterion("sync_config_id >=", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdLessThan(Long value) {
            addCriterion("sync_config_id <", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdLessThanOrEqualTo(Long value) {
            addCriterion("sync_config_id <=", value, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdIn(List<Long> values) {
            addCriterion("sync_config_id in", values, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdNotIn(List<Long> values) {
            addCriterion("sync_config_id not in", values, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdBetween(Long value1, Long value2) {
            addCriterion("sync_config_id between", value1, value2, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andSyncConfigIdNotBetween(Long value1, Long value2) {
            addCriterion("sync_config_id not between", value1, value2, "syncConfigId");
            return (Criteria) this;
        }

        public Criteria andMd5ValueIsNull() {
            addCriterion("md5_value is null");
            return (Criteria) this;
        }

        public Criteria andMd5ValueIsNotNull() {
            addCriterion("md5_value is not null");
            return (Criteria) this;
        }

        public Criteria andMd5ValueEqualTo(String value) {
            addCriterion("md5_value =", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueNotEqualTo(String value) {
            addCriterion("md5_value <>", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueGreaterThan(String value) {
            addCriterion("md5_value >", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueGreaterThanOrEqualTo(String value) {
            addCriterion("md5_value >=", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueLessThan(String value) {
            addCriterion("md5_value <", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueLessThanOrEqualTo(String value) {
            addCriterion("md5_value <=", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueLike(String value) {
            addCriterion("md5_value like", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueNotLike(String value) {
            addCriterion("md5_value not like", value, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueIn(List<String> values) {
            addCriterion("md5_value in", values, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueNotIn(List<String> values) {
            addCriterion("md5_value not in", values, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueBetween(String value1, String value2) {
            addCriterion("md5_value between", value1, value2, "md5Value");
            return (Criteria) this;
        }

        public Criteria andMd5ValueNotBetween(String value1, String value2) {
            addCriterion("md5_value not between", value1, value2, "md5Value");
            return (Criteria) this;
        }

        public Criteria andProcessStatusIsNull() {
            addCriterion("process_status is null");
            return (Criteria) this;
        }

        public Criteria andProcessStatusIsNotNull() {
            addCriterion("process_status is not null");
            return (Criteria) this;
        }

        public Criteria andProcessStatusEqualTo(Integer value) {
            addCriterion("process_status =", value, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusNotEqualTo(Integer value) {
            addCriterion("process_status <>", value, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusGreaterThan(Integer value) {
            addCriterion("process_status >", value, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("process_status >=", value, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusLessThan(Integer value) {
            addCriterion("process_status <", value, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusLessThanOrEqualTo(Integer value) {
            addCriterion("process_status <=", value, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusIn(List<Integer> values) {
            addCriterion("process_status in", values, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusNotIn(List<Integer> values) {
            addCriterion("process_status not in", values, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusBetween(Integer value1, Integer value2) {
            addCriterion("process_status between", value1, value2, "processStatus");
            return (Criteria) this;
        }

        public Criteria andProcessStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("process_status not between", value1, value2, "processStatus");
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

        public Criteria andReceiveDateIsNull() {
            addCriterion("receive_date is null");
            return (Criteria) this;
        }

        public Criteria andReceiveDateIsNotNull() {
            addCriterion("receive_date is not null");
            return (Criteria) this;
        }

        public Criteria andReceiveDateEqualTo(String value) {
            addCriterion("receive_date =", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotEqualTo(String value) {
            addCriterion("receive_date <>", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateGreaterThan(String value) {
            addCriterion("receive_date >", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateGreaterThanOrEqualTo(String value) {
            addCriterion("receive_date >=", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLessThan(String value) {
            addCriterion("receive_date <", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLessThanOrEqualTo(String value) {
            addCriterion("receive_date <=", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateLike(String value) {
            addCriterion("receive_date like", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotLike(String value) {
            addCriterion("receive_date not like", value, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateIn(List<String> values) {
            addCriterion("receive_date in", values, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotIn(List<String> values) {
            addCriterion("receive_date not in", values, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateBetween(String value1, String value2) {
            addCriterion("receive_date between", value1, value2, "receiveDate");
            return (Criteria) this;
        }

        public Criteria andReceiveDateNotBetween(String value1, String value2) {
            addCriterion("receive_date not between", value1, value2, "receiveDate");
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