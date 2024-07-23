package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StraHisFileExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public StraHisFileExample() {
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

        public Criteria andFilePathIsNull() {
            addCriterion("file_path is null");
            return (Criteria) this;
        }

        public Criteria andFilePathIsNotNull() {
            addCriterion("file_path is not null");
            return (Criteria) this;
        }

        public Criteria andFilePathEqualTo(String value) {
            addCriterion("file_path =", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotEqualTo(String value) {
            addCriterion("file_path <>", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathGreaterThan(String value) {
            addCriterion("file_path >", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathGreaterThanOrEqualTo(String value) {
            addCriterion("file_path >=", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathLessThan(String value) {
            addCriterion("file_path <", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathLessThanOrEqualTo(String value) {
            addCriterion("file_path <=", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathLike(String value) {
            addCriterion("file_path like", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotLike(String value) {
            addCriterion("file_path not like", value, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathIn(List<String> values) {
            addCriterion("file_path in", values, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotIn(List<String> values) {
            addCriterion("file_path not in", values, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathBetween(String value1, String value2) {
            addCriterion("file_path between", value1, value2, "filePath");
            return (Criteria) this;
        }

        public Criteria andFilePathNotBetween(String value1, String value2) {
            addCriterion("file_path not between", value1, value2, "filePath");
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

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(Integer value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(Integer value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(Integer value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(Integer value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(Integer value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<Integer> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<Integer> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(Integer value1, Integer value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("`type` not between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andZipfileNameIsNull() {
            addCriterion("zipFile_name is null");
            return (Criteria) this;
        }

        public Criteria andZipfileNameIsNotNull() {
            addCriterion("zipFile_name is not null");
            return (Criteria) this;
        }

        public Criteria andZipfileNameEqualTo(String value) {
            addCriterion("zipFile_name =", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameNotEqualTo(String value) {
            addCriterion("zipFile_name <>", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameGreaterThan(String value) {
            addCriterion("zipFile_name >", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameGreaterThanOrEqualTo(String value) {
            addCriterion("zipFile_name >=", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameLessThan(String value) {
            addCriterion("zipFile_name <", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameLessThanOrEqualTo(String value) {
            addCriterion("zipFile_name <=", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameLike(String value) {
            addCriterion("zipFile_name like", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameNotLike(String value) {
            addCriterion("zipFile_name not like", value, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameIn(List<String> values) {
            addCriterion("zipFile_name in", values, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameNotIn(List<String> values) {
            addCriterion("zipFile_name not in", values, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameBetween(String value1, String value2) {
            addCriterion("zipFile_name between", value1, value2, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andZipfileNameNotBetween(String value1, String value2) {
            addCriterion("zipFile_name not between", value1, value2, "zipfileName");
            return (Criteria) this;
        }

        public Criteria andErrorFileIsNull() {
            addCriterion("error_file is null");
            return (Criteria) this;
        }

        public Criteria andErrorFileIsNotNull() {
            addCriterion("error_file is not null");
            return (Criteria) this;
        }

        public Criteria andErrorFileEqualTo(String value) {
            addCriterion("error_file =", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileNotEqualTo(String value) {
            addCriterion("error_file <>", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileGreaterThan(String value) {
            addCriterion("error_file >", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileGreaterThanOrEqualTo(String value) {
            addCriterion("error_file >=", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileLessThan(String value) {
            addCriterion("error_file <", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileLessThanOrEqualTo(String value) {
            addCriterion("error_file <=", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileLike(String value) {
            addCriterion("error_file like", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileNotLike(String value) {
            addCriterion("error_file not like", value, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileIn(List<String> values) {
            addCriterion("error_file in", values, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileNotIn(List<String> values) {
            addCriterion("error_file not in", values, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileBetween(String value1, String value2) {
            addCriterion("error_file between", value1, value2, "errorFile");
            return (Criteria) this;
        }

        public Criteria andErrorFileNotBetween(String value1, String value2) {
            addCriterion("error_file not between", value1, value2, "errorFile");
            return (Criteria) this;
        }

        public Criteria andExpectedNumIsNull() {
            addCriterion("expected_num is null");
            return (Criteria) this;
        }

        public Criteria andExpectedNumIsNotNull() {
            addCriterion("expected_num is not null");
            return (Criteria) this;
        }

        public Criteria andExpectedNumEqualTo(Integer value) {
            addCriterion("expected_num =", value, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumNotEqualTo(Integer value) {
            addCriterion("expected_num <>", value, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumGreaterThan(Integer value) {
            addCriterion("expected_num >", value, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("expected_num >=", value, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumLessThan(Integer value) {
            addCriterion("expected_num <", value, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumLessThanOrEqualTo(Integer value) {
            addCriterion("expected_num <=", value, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumIn(List<Integer> values) {
            addCriterion("expected_num in", values, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumNotIn(List<Integer> values) {
            addCriterion("expected_num not in", values, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumBetween(Integer value1, Integer value2) {
            addCriterion("expected_num between", value1, value2, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andExpectedNumNotBetween(Integer value1, Integer value2) {
            addCriterion("expected_num not between", value1, value2, "expectedNum");
            return (Criteria) this;
        }

        public Criteria andUploadTimeIsNull() {
            addCriterion("upload_time is null");
            return (Criteria) this;
        }

        public Criteria andUploadTimeIsNotNull() {
            addCriterion("upload_time is not null");
            return (Criteria) this;
        }

        public Criteria andUploadTimeEqualTo(String value) {
            addCriterion("upload_time =", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotEqualTo(String value) {
            addCriterion("upload_time <>", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeGreaterThan(String value) {
            addCriterion("upload_time >", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeGreaterThanOrEqualTo(String value) {
            addCriterion("upload_time >=", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeLessThan(String value) {
            addCriterion("upload_time <", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeLessThanOrEqualTo(String value) {
            addCriterion("upload_time <=", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeLike(String value) {
            addCriterion("upload_time like", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotLike(String value) {
            addCriterion("upload_time not like", value, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeIn(List<String> values) {
            addCriterion("upload_time in", values, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotIn(List<String> values) {
            addCriterion("upload_time not in", values, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeBetween(String value1, String value2) {
            addCriterion("upload_time between", value1, value2, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andUploadTimeNotBetween(String value1, String value2) {
            addCriterion("upload_time not between", value1, value2, "uploadTime");
            return (Criteria) this;
        }

        public Criteria andFileSizeIsNull() {
            addCriterion("file_size is null");
            return (Criteria) this;
        }

        public Criteria andFileSizeIsNotNull() {
            addCriterion("file_size is not null");
            return (Criteria) this;
        }

        public Criteria andFileSizeEqualTo(String value) {
            addCriterion("file_size =", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeNotEqualTo(String value) {
            addCriterion("file_size <>", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeGreaterThan(String value) {
            addCriterion("file_size >", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeGreaterThanOrEqualTo(String value) {
            addCriterion("file_size >=", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeLessThan(String value) {
            addCriterion("file_size <", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeLessThanOrEqualTo(String value) {
            addCriterion("file_size <=", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeLike(String value) {
            addCriterion("file_size like", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeNotLike(String value) {
            addCriterion("file_size not like", value, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeIn(List<String> values) {
            addCriterion("file_size in", values, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeNotIn(List<String> values) {
            addCriterion("file_size not in", values, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeBetween(String value1, String value2) {
            addCriterion("file_size between", value1, value2, "fileSize");
            return (Criteria) this;
        }

        public Criteria andFileSizeNotBetween(String value1, String value2) {
            addCriterion("file_size not between", value1, value2, "fileSize");
            return (Criteria) this;
        }

        public Criteria andActualNumIsNull() {
            addCriterion("actual_num is null");
            return (Criteria) this;
        }

        public Criteria andActualNumIsNotNull() {
            addCriterion("actual_num is not null");
            return (Criteria) this;
        }

        public Criteria andActualNumEqualTo(Integer value) {
            addCriterion("actual_num =", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumNotEqualTo(Integer value) {
            addCriterion("actual_num <>", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumGreaterThan(Integer value) {
            addCriterion("actual_num >", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_num >=", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumLessThan(Integer value) {
            addCriterion("actual_num <", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumLessThanOrEqualTo(Integer value) {
            addCriterion("actual_num <=", value, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumIn(List<Integer> values) {
            addCriterion("actual_num in", values, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumNotIn(List<Integer> values) {
            addCriterion("actual_num not in", values, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumBetween(Integer value1, Integer value2) {
            addCriterion("actual_num between", value1, value2, "actualNum");
            return (Criteria) this;
        }

        public Criteria andActualNumNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_num not between", value1, value2, "actualNum");
            return (Criteria) this;
        }

        public Criteria andFileNumIsNull() {
            addCriterion("file_num is null");
            return (Criteria) this;
        }

        public Criteria andFileNumIsNotNull() {
            addCriterion("file_num is not null");
            return (Criteria) this;
        }

        public Criteria andFileNumEqualTo(Integer value) {
            addCriterion("file_num =", value, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumNotEqualTo(Integer value) {
            addCriterion("file_num <>", value, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumGreaterThan(Integer value) {
            addCriterion("file_num >", value, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("file_num >=", value, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumLessThan(Integer value) {
            addCriterion("file_num <", value, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumLessThanOrEqualTo(Integer value) {
            addCriterion("file_num <=", value, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumIn(List<Integer> values) {
            addCriterion("file_num in", values, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumNotIn(List<Integer> values) {
            addCriterion("file_num not in", values, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumBetween(Integer value1, Integer value2) {
            addCriterion("file_num between", value1, value2, "fileNum");
            return (Criteria) this;
        }

        public Criteria andFileNumNotBetween(Integer value1, Integer value2) {
            addCriterion("file_num not between", value1, value2, "fileNum");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusIsNull() {
            addCriterion("sign_file_status is null");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusIsNotNull() {
            addCriterion("sign_file_status is not null");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusEqualTo(Integer value) {
            addCriterion("sign_file_status =", value, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusNotEqualTo(Integer value) {
            addCriterion("sign_file_status <>", value, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusGreaterThan(Integer value) {
            addCriterion("sign_file_status >", value, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("sign_file_status >=", value, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusLessThan(Integer value) {
            addCriterion("sign_file_status <", value, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusLessThanOrEqualTo(Integer value) {
            addCriterion("sign_file_status <=", value, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusIn(List<Integer> values) {
            addCriterion("sign_file_status in", values, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusNotIn(List<Integer> values) {
            addCriterion("sign_file_status not in", values, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusBetween(Integer value1, Integer value2) {
            addCriterion("sign_file_status between", value1, value2, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andSignFileStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("sign_file_status not between", value1, value2, "signFileStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusIsNull() {
            addCriterion("zip_status is null");
            return (Criteria) this;
        }

        public Criteria andZipStatusIsNotNull() {
            addCriterion("zip_status is not null");
            return (Criteria) this;
        }

        public Criteria andZipStatusEqualTo(Integer value) {
            addCriterion("zip_status =", value, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusNotEqualTo(Integer value) {
            addCriterion("zip_status <>", value, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusGreaterThan(Integer value) {
            addCriterion("zip_status >", value, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("zip_status >=", value, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusLessThan(Integer value) {
            addCriterion("zip_status <", value, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusLessThanOrEqualTo(Integer value) {
            addCriterion("zip_status <=", value, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusIn(List<Integer> values) {
            addCriterion("zip_status in", values, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusNotIn(List<Integer> values) {
            addCriterion("zip_status not in", values, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusBetween(Integer value1, Integer value2) {
            addCriterion("zip_status between", value1, value2, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andZipStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("zip_status not between", value1, value2, "zipStatus");
            return (Criteria) this;
        }

        public Criteria andMd5IsNull() {
            addCriterion("md5 is null");
            return (Criteria) this;
        }

        public Criteria andMd5IsNotNull() {
            addCriterion("md5 is not null");
            return (Criteria) this;
        }

        public Criteria andMd5EqualTo(String value) {
            addCriterion("md5 =", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5NotEqualTo(String value) {
            addCriterion("md5 <>", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5GreaterThan(String value) {
            addCriterion("md5 >", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5GreaterThanOrEqualTo(String value) {
            addCriterion("md5 >=", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5LessThan(String value) {
            addCriterion("md5 <", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5LessThanOrEqualTo(String value) {
            addCriterion("md5 <=", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5Like(String value) {
            addCriterion("md5 like", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5NotLike(String value) {
            addCriterion("md5 not like", value, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5In(List<String> values) {
            addCriterion("md5 in", values, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5NotIn(List<String> values) {
            addCriterion("md5 not in", values, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5Between(String value1, String value2) {
            addCriterion("md5 between", value1, value2, "md5");
            return (Criteria) this;
        }

        public Criteria andMd5NotBetween(String value1, String value2) {
            addCriterion("md5 not between", value1, value2, "md5");
            return (Criteria) this;
        }

        public Criteria andScoreStatusIsNull() {
            addCriterion("score_status is null");
            return (Criteria) this;
        }

        public Criteria andScoreStatusIsNotNull() {
            addCriterion("score_status is not null");
            return (Criteria) this;
        }

        public Criteria andScoreStatusEqualTo(Integer value) {
            addCriterion("score_status =", value, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusNotEqualTo(Integer value) {
            addCriterion("score_status <>", value, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusGreaterThan(Integer value) {
            addCriterion("score_status >", value, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("score_status >=", value, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusLessThan(Integer value) {
            addCriterion("score_status <", value, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusLessThanOrEqualTo(Integer value) {
            addCriterion("score_status <=", value, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusIn(List<Integer> values) {
            addCriterion("score_status in", values, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusNotIn(List<Integer> values) {
            addCriterion("score_status not in", values, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusBetween(Integer value1, Integer value2) {
            addCriterion("score_status between", value1, value2, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andScoreStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("score_status not between", value1, value2, "scoreStatus");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathIsNull() {
            addCriterion("statistic_file_path is null");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathIsNotNull() {
            addCriterion("statistic_file_path is not null");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathEqualTo(String value) {
            addCriterion("statistic_file_path =", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathNotEqualTo(String value) {
            addCriterion("statistic_file_path <>", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathGreaterThan(String value) {
            addCriterion("statistic_file_path >", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathGreaterThanOrEqualTo(String value) {
            addCriterion("statistic_file_path >=", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathLessThan(String value) {
            addCriterion("statistic_file_path <", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathLessThanOrEqualTo(String value) {
            addCriterion("statistic_file_path <=", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathLike(String value) {
            addCriterion("statistic_file_path like", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathNotLike(String value) {
            addCriterion("statistic_file_path not like", value, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathIn(List<String> values) {
            addCriterion("statistic_file_path in", values, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathNotIn(List<String> values) {
            addCriterion("statistic_file_path not in", values, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathBetween(String value1, String value2) {
            addCriterion("statistic_file_path between", value1, value2, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andStatisticFilePathNotBetween(String value1, String value2) {
            addCriterion("statistic_file_path not between", value1, value2, "statisticFilePath");
            return (Criteria) this;
        }

        public Criteria andShowTitleIsNull() {
            addCriterion("show_title is null");
            return (Criteria) this;
        }

        public Criteria andShowTitleIsNotNull() {
            addCriterion("show_title is not null");
            return (Criteria) this;
        }

        public Criteria andShowTitleEqualTo(String value) {
            addCriterion("show_title =", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleNotEqualTo(String value) {
            addCriterion("show_title <>", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleGreaterThan(String value) {
            addCriterion("show_title >", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleGreaterThanOrEqualTo(String value) {
            addCriterion("show_title >=", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleLessThan(String value) {
            addCriterion("show_title <", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleLessThanOrEqualTo(String value) {
            addCriterion("show_title <=", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleLike(String value) {
            addCriterion("show_title like", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleNotLike(String value) {
            addCriterion("show_title not like", value, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleIn(List<String> values) {
            addCriterion("show_title in", values, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleNotIn(List<String> values) {
            addCriterion("show_title not in", values, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleBetween(String value1, String value2) {
            addCriterion("show_title between", value1, value2, "showTitle");
            return (Criteria) this;
        }

        public Criteria andShowTitleNotBetween(String value1, String value2) {
            addCriterion("show_title not between", value1, value2, "showTitle");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNull() {
            addCriterion("push_type is null");
            return (Criteria) this;
        }

        public Criteria andPushTypeIsNotNull() {
            addCriterion("push_type is not null");
            return (Criteria) this;
        }

        public Criteria andPushTypeEqualTo(Integer value) {
            addCriterion("push_type =", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotEqualTo(Integer value) {
            addCriterion("push_type <>", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThan(Integer value) {
            addCriterion("push_type >", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_type >=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThan(Integer value) {
            addCriterion("push_type <", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeLessThanOrEqualTo(Integer value) {
            addCriterion("push_type <=", value, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeIn(List<Integer> values) {
            addCriterion("push_type in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotIn(List<Integer> values) {
            addCriterion("push_type not in", values, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeBetween(Integer value1, Integer value2) {
            addCriterion("push_type between", value1, value2, "pushType");
            return (Criteria) this;
        }

        public Criteria andPushTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("push_type not between", value1, value2, "pushType");
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

        public Criteria andPushStatusEqualTo(Integer value) {
            addCriterion("push_status =", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotEqualTo(Integer value) {
            addCriterion("push_status <>", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThan(Integer value) {
            addCriterion("push_status >", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("push_status >=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThan(Integer value) {
            addCriterion("push_status <", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusLessThanOrEqualTo(Integer value) {
            addCriterion("push_status <=", value, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusIn(List<Integer> values) {
            addCriterion("push_status in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotIn(List<Integer> values) {
            addCriterion("push_status not in", values, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusBetween(Integer value1, Integer value2) {
            addCriterion("push_status between", value1, value2, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andPushStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("push_status not between", value1, value2, "pushStatus");
            return (Criteria) this;
        }

        public Criteria andIndexNumIsNull() {
            addCriterion("index_num is null");
            return (Criteria) this;
        }

        public Criteria andIndexNumIsNotNull() {
            addCriterion("index_num is not null");
            return (Criteria) this;
        }

        public Criteria andIndexNumEqualTo(Integer value) {
            addCriterion("index_num =", value, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumNotEqualTo(Integer value) {
            addCriterion("index_num <>", value, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumGreaterThan(Integer value) {
            addCriterion("index_num >", value, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("index_num >=", value, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumLessThan(Integer value) {
            addCriterion("index_num <", value, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumLessThanOrEqualTo(Integer value) {
            addCriterion("index_num <=", value, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumIn(List<Integer> values) {
            addCriterion("index_num in", values, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumNotIn(List<Integer> values) {
            addCriterion("index_num not in", values, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumBetween(Integer value1, Integer value2) {
            addCriterion("index_num between", value1, value2, "indexNum");
            return (Criteria) this;
        }

        public Criteria andIndexNumNotBetween(Integer value1, Integer value2) {
            addCriterion("index_num not between", value1, value2, "indexNum");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathIsNull() {
            addCriterion("inner_ftp_path is null");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathIsNotNull() {
            addCriterion("inner_ftp_path is not null");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathEqualTo(String value) {
            addCriterion("inner_ftp_path =", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathNotEqualTo(String value) {
            addCriterion("inner_ftp_path <>", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathGreaterThan(String value) {
            addCriterion("inner_ftp_path >", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathGreaterThanOrEqualTo(String value) {
            addCriterion("inner_ftp_path >=", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathLessThan(String value) {
            addCriterion("inner_ftp_path <", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathLessThanOrEqualTo(String value) {
            addCriterion("inner_ftp_path <=", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathLike(String value) {
            addCriterion("inner_ftp_path like", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathNotLike(String value) {
            addCriterion("inner_ftp_path not like", value, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathIn(List<String> values) {
            addCriterion("inner_ftp_path in", values, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathNotIn(List<String> values) {
            addCriterion("inner_ftp_path not in", values, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathBetween(String value1, String value2) {
            addCriterion("inner_ftp_path between", value1, value2, "innerFtpPath");
            return (Criteria) this;
        }

        public Criteria andInnerFtpPathNotBetween(String value1, String value2) {
            addCriterion("inner_ftp_path not between", value1, value2, "innerFtpPath");
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

        public Criteria andOfflineFilePathIsNull() {
            addCriterion("offline_file_path is null");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathIsNotNull() {
            addCriterion("offline_file_path is not null");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathEqualTo(String value) {
            addCriterion("offline_file_path =", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathNotEqualTo(String value) {
            addCriterion("offline_file_path <>", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathGreaterThan(String value) {
            addCriterion("offline_file_path >", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathGreaterThanOrEqualTo(String value) {
            addCriterion("offline_file_path >=", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathLessThan(String value) {
            addCriterion("offline_file_path <", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathLessThanOrEqualTo(String value) {
            addCriterion("offline_file_path <=", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathLike(String value) {
            addCriterion("offline_file_path like", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathNotLike(String value) {
            addCriterion("offline_file_path not like", value, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathIn(List<String> values) {
            addCriterion("offline_file_path in", values, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathNotIn(List<String> values) {
            addCriterion("offline_file_path not in", values, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathBetween(String value1, String value2) {
            addCriterion("offline_file_path between", value1, value2, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andOfflineFilePathNotBetween(String value1, String value2) {
            addCriterion("offline_file_path not between", value1, value2, "offlineFilePath");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeIsNull() {
            addCriterion("running_end_time is null");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeIsNotNull() {
            addCriterion("running_end_time is not null");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeEqualTo(Date value) {
            addCriterion("running_end_time =", value, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeNotEqualTo(Date value) {
            addCriterion("running_end_time <>", value, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeGreaterThan(Date value) {
            addCriterion("running_end_time >", value, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("running_end_time >=", value, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeLessThan(Date value) {
            addCriterion("running_end_time <", value, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeLessThanOrEqualTo(Date value) {
            addCriterion("running_end_time <=", value, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeIn(List<Date> values) {
            addCriterion("running_end_time in", values, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeNotIn(List<Date> values) {
            addCriterion("running_end_time not in", values, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeBetween(Date value1, Date value2) {
            addCriterion("running_end_time between", value1, value2, "runningEndTime");
            return (Criteria) this;
        }

        public Criteria andRunningEndTimeNotBetween(Date value1, Date value2) {
            addCriterion("running_end_time not between", value1, value2, "runningEndTime");
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