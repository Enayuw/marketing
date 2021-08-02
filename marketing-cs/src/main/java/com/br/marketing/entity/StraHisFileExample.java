package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StraHisFileExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<BaseCriteria> oredCriteria;

    public StraHisFileExample() {
        oredCriteria = new ArrayList<BaseCriteria>();
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

    public List<BaseCriteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(BaseCriteria criteria) {
        oredCriteria.add(criteria);
    }

    public BaseCriteria or() {
        BaseCriteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public BaseCriteria createCriteria() {
        BaseCriteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected BaseCriteria createCriteriaInternal() {
        BaseCriteria criteria = new BaseCriteria();
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

        public BaseCriteria andIdIsNull() {
            addCriterion("id is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIsNull() {
            addCriterion("api_code is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIsNotNull() {
            addCriterion("api_code is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeEqualTo(String value) {
            addCriterion("api_code =", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotEqualTo(String value) {
            addCriterion("api_code <>", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeGreaterThan(String value) {
            addCriterion("api_code >", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeGreaterThanOrEqualTo(String value) {
            addCriterion("api_code >=", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLessThan(String value) {
            addCriterion("api_code <", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLessThanOrEqualTo(String value) {
            addCriterion("api_code <=", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeLike(String value) {
            addCriterion("api_code like", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotLike(String value) {
            addCriterion("api_code not like", value, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeIn(List<String> values) {
            addCriterion("api_code in", values, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotIn(List<String> values) {
            addCriterion("api_code not in", values, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeBetween(String value1, String value2) {
            addCriterion("api_code between", value1, value2, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andApiCodeNotBetween(String value1, String value2) {
            addCriterion("api_code not between", value1, value2, "apiCode");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberIsNull() {
            addCriterion("batch_number is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberIsNotNull() {
            addCriterion("batch_number is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberEqualTo(String value) {
            addCriterion("batch_number =", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotEqualTo(String value) {
            addCriterion("batch_number <>", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberGreaterThan(String value) {
            addCriterion("batch_number >", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberGreaterThanOrEqualTo(String value) {
            addCriterion("batch_number >=", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberLessThan(String value) {
            addCriterion("batch_number <", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberLessThanOrEqualTo(String value) {
            addCriterion("batch_number <=", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberLike(String value) {
            addCriterion("batch_number like", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotLike(String value) {
            addCriterion("batch_number not like", value, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberIn(List<String> values) {
            addCriterion("batch_number in", values, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotIn(List<String> values) {
            addCriterion("batch_number not in", values, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberBetween(String value1, String value2) {
            addCriterion("batch_number between", value1, value2, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andBatchNumberNotBetween(String value1, String value2) {
            addCriterion("batch_number not between", value1, value2, "batchNumber");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathIsNull() {
            addCriterion("file_path is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathIsNotNull() {
            addCriterion("file_path is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathEqualTo(String value) {
            addCriterion("file_path =", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathNotEqualTo(String value) {
            addCriterion("file_path <>", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathGreaterThan(String value) {
            addCriterion("file_path >", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathGreaterThanOrEqualTo(String value) {
            addCriterion("file_path >=", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathLessThan(String value) {
            addCriterion("file_path <", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathLessThanOrEqualTo(String value) {
            addCriterion("file_path <=", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathLike(String value) {
            addCriterion("file_path like", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathNotLike(String value) {
            addCriterion("file_path not like", value, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathIn(List<String> values) {
            addCriterion("file_path in", values, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathNotIn(List<String> values) {
            addCriterion("file_path not in", values, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathBetween(String value1, String value2) {
            addCriterion("file_path between", value1, value2, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFilePathNotBetween(String value1, String value2) {
            addCriterion("file_path not between", value1, value2, "filePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIsNull() {
            addCriterion("create_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIsNotNull() {
            addCriterion("create_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeEqualTo(Date value) {
            addCriterion("create_time =", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotEqualTo(Date value) {
            addCriterion("create_time <>", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeGreaterThan(Date value) {
            addCriterion("create_time >", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("create_time >=", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeLessThan(Date value) {
            addCriterion("create_time <", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeLessThanOrEqualTo(Date value) {
            addCriterion("create_time <=", value, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeIn(List<Date> values) {
            addCriterion("create_time in", values, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotIn(List<Date> values) {
            addCriterion("create_time not in", values, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeBetween(Date value1, Date value2) {
            addCriterion("create_time between", value1, value2, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andCreateTimeNotBetween(Date value1, Date value2) {
            addCriterion("create_time not between", value1, value2, "createTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIsNull() {
            addCriterion("update_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIsNotNull() {
            addCriterion("update_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeEqualTo(Date value) {
            addCriterion("update_time =", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotEqualTo(Date value) {
            addCriterion("update_time <>", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeGreaterThan(Date value) {
            addCriterion("update_time >", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeGreaterThanOrEqualTo(Date value) {
            addCriterion("update_time >=", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeLessThan(Date value) {
            addCriterion("update_time <", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeLessThanOrEqualTo(Date value) {
            addCriterion("update_time <=", value, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeIn(List<Date> values) {
            addCriterion("update_time in", values, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotIn(List<Date> values) {
            addCriterion("update_time not in", values, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeBetween(Date value1, Date value2) {
            addCriterion("update_time between", value1, value2, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUpdateTimeNotBetween(Date value1, Date value2) {
            addCriterion("update_time not between", value1, value2, "updateTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIsNull() {
            addCriterion("status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusEqualTo(Integer value) {
            addCriterion("status =", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotEqualTo(Integer value) {
            addCriterion("status <>", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusGreaterThan(Integer value) {
            addCriterion("status >", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("status >=", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusLessThan(Integer value) {
            addCriterion("status <", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusLessThanOrEqualTo(Integer value) {
            addCriterion("status <=", value, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusIn(List<Integer> values) {
            addCriterion("status in", values, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotIn(List<Integer> values) {
            addCriterion("status not in", values, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusBetween(Integer value1, Integer value2) {
            addCriterion("status between", value1, value2, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("status not between", value1, value2, "status");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeIsNull() {
            addCriterion("type is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeIsNotNull() {
            addCriterion("type is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeEqualTo(Integer value) {
            addCriterion("type =", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeNotEqualTo(Integer value) {
            addCriterion("type <>", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeGreaterThan(Integer value) {
            addCriterion("type >", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("type >=", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeLessThan(Integer value) {
            addCriterion("type <", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeLessThanOrEqualTo(Integer value) {
            addCriterion("type <=", value, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeIn(List<Integer> values) {
            addCriterion("type in", values, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeNotIn(List<Integer> values) {
            addCriterion("type not in", values, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeBetween(Integer value1, Integer value2) {
            addCriterion("type between", value1, value2, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("type not between", value1, value2, "type");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameIsNull() {
            addCriterion("zipFile_name is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameIsNotNull() {
            addCriterion("zipFile_name is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameEqualTo(String value) {
            addCriterion("zipFile_name =", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameNotEqualTo(String value) {
            addCriterion("zipFile_name <>", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameGreaterThan(String value) {
            addCriterion("zipFile_name >", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameGreaterThanOrEqualTo(String value) {
            addCriterion("zipFile_name >=", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameLessThan(String value) {
            addCriterion("zipFile_name <", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameLessThanOrEqualTo(String value) {
            addCriterion("zipFile_name <=", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameLike(String value) {
            addCriterion("zipFile_name like", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameNotLike(String value) {
            addCriterion("zipFile_name not like", value, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameIn(List<String> values) {
            addCriterion("zipFile_name in", values, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameNotIn(List<String> values) {
            addCriterion("zipFile_name not in", values, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameBetween(String value1, String value2) {
            addCriterion("zipFile_name between", value1, value2, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipfileNameNotBetween(String value1, String value2) {
            addCriterion("zipFile_name not between", value1, value2, "zipfileName");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileIsNull() {
            addCriterion("error_file is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileIsNotNull() {
            addCriterion("error_file is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileEqualTo(String value) {
            addCriterion("error_file =", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileNotEqualTo(String value) {
            addCriterion("error_file <>", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileGreaterThan(String value) {
            addCriterion("error_file >", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileGreaterThanOrEqualTo(String value) {
            addCriterion("error_file >=", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileLessThan(String value) {
            addCriterion("error_file <", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileLessThanOrEqualTo(String value) {
            addCriterion("error_file <=", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileLike(String value) {
            addCriterion("error_file like", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileNotLike(String value) {
            addCriterion("error_file not like", value, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileIn(List<String> values) {
            addCriterion("error_file in", values, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileNotIn(List<String> values) {
            addCriterion("error_file not in", values, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileBetween(String value1, String value2) {
            addCriterion("error_file between", value1, value2, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andErrorFileNotBetween(String value1, String value2) {
            addCriterion("error_file not between", value1, value2, "errorFile");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumIsNull() {
            addCriterion("expected_num is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumIsNotNull() {
            addCriterion("expected_num is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumEqualTo(Integer value) {
            addCriterion("expected_num =", value, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumNotEqualTo(Integer value) {
            addCriterion("expected_num <>", value, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumGreaterThan(Integer value) {
            addCriterion("expected_num >", value, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("expected_num >=", value, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumLessThan(Integer value) {
            addCriterion("expected_num <", value, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumLessThanOrEqualTo(Integer value) {
            addCriterion("expected_num <=", value, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumIn(List<Integer> values) {
            addCriterion("expected_num in", values, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumNotIn(List<Integer> values) {
            addCriterion("expected_num not in", values, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumBetween(Integer value1, Integer value2) {
            addCriterion("expected_num between", value1, value2, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andExpectedNumNotBetween(Integer value1, Integer value2) {
            addCriterion("expected_num not between", value1, value2, "expectedNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeIsNull() {
            addCriterion("upload_time is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeIsNotNull() {
            addCriterion("upload_time is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeEqualTo(String value) {
            addCriterion("upload_time =", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeNotEqualTo(String value) {
            addCriterion("upload_time <>", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeGreaterThan(String value) {
            addCriterion("upload_time >", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeGreaterThanOrEqualTo(String value) {
            addCriterion("upload_time >=", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeLessThan(String value) {
            addCriterion("upload_time <", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeLessThanOrEqualTo(String value) {
            addCriterion("upload_time <=", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeLike(String value) {
            addCriterion("upload_time like", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeNotLike(String value) {
            addCriterion("upload_time not like", value, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeIn(List<String> values) {
            addCriterion("upload_time in", values, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeNotIn(List<String> values) {
            addCriterion("upload_time not in", values, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeBetween(String value1, String value2) {
            addCriterion("upload_time between", value1, value2, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andUploadTimeNotBetween(String value1, String value2) {
            addCriterion("upload_time not between", value1, value2, "uploadTime");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeIsNull() {
            addCriterion("file_size is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeIsNotNull() {
            addCriterion("file_size is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeEqualTo(String value) {
            addCriterion("file_size =", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeNotEqualTo(String value) {
            addCriterion("file_size <>", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeGreaterThan(String value) {
            addCriterion("file_size >", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeGreaterThanOrEqualTo(String value) {
            addCriterion("file_size >=", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeLessThan(String value) {
            addCriterion("file_size <", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeLessThanOrEqualTo(String value) {
            addCriterion("file_size <=", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeLike(String value) {
            addCriterion("file_size like", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeNotLike(String value) {
            addCriterion("file_size not like", value, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeIn(List<String> values) {
            addCriterion("file_size in", values, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeNotIn(List<String> values) {
            addCriterion("file_size not in", values, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeBetween(String value1, String value2) {
            addCriterion("file_size between", value1, value2, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileSizeNotBetween(String value1, String value2) {
            addCriterion("file_size not between", value1, value2, "fileSize");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumIsNull() {
            addCriterion("actual_num is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumIsNotNull() {
            addCriterion("actual_num is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumEqualTo(Integer value) {
            addCriterion("actual_num =", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumNotEqualTo(Integer value) {
            addCriterion("actual_num <>", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumGreaterThan(Integer value) {
            addCriterion("actual_num >", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("actual_num >=", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumLessThan(Integer value) {
            addCriterion("actual_num <", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumLessThanOrEqualTo(Integer value) {
            addCriterion("actual_num <=", value, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumIn(List<Integer> values) {
            addCriterion("actual_num in", values, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumNotIn(List<Integer> values) {
            addCriterion("actual_num not in", values, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumBetween(Integer value1, Integer value2) {
            addCriterion("actual_num between", value1, value2, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andActualNumNotBetween(Integer value1, Integer value2) {
            addCriterion("actual_num not between", value1, value2, "actualNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumIsNull() {
            addCriterion("file_num is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumIsNotNull() {
            addCriterion("file_num is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumEqualTo(Integer value) {
            addCriterion("file_num =", value, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumNotEqualTo(Integer value) {
            addCriterion("file_num <>", value, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumGreaterThan(Integer value) {
            addCriterion("file_num >", value, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumGreaterThanOrEqualTo(Integer value) {
            addCriterion("file_num >=", value, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumLessThan(Integer value) {
            addCriterion("file_num <", value, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumLessThanOrEqualTo(Integer value) {
            addCriterion("file_num <=", value, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumIn(List<Integer> values) {
            addCriterion("file_num in", values, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumNotIn(List<Integer> values) {
            addCriterion("file_num not in", values, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumBetween(Integer value1, Integer value2) {
            addCriterion("file_num between", value1, value2, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andFileNumNotBetween(Integer value1, Integer value2) {
            addCriterion("file_num not between", value1, value2, "fileNum");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusIsNull() {
            addCriterion("sign_file_status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusIsNotNull() {
            addCriterion("sign_file_status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusEqualTo(Integer value) {
            addCriterion("sign_file_status =", value, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusNotEqualTo(Integer value) {
            addCriterion("sign_file_status <>", value, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusGreaterThan(Integer value) {
            addCriterion("sign_file_status >", value, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("sign_file_status >=", value, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusLessThan(Integer value) {
            addCriterion("sign_file_status <", value, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusLessThanOrEqualTo(Integer value) {
            addCriterion("sign_file_status <=", value, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusIn(List<Integer> values) {
            addCriterion("sign_file_status in", values, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusNotIn(List<Integer> values) {
            addCriterion("sign_file_status not in", values, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusBetween(Integer value1, Integer value2) {
            addCriterion("sign_file_status between", value1, value2, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andSignFileStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("sign_file_status not between", value1, value2, "signFileStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusIsNull() {
            addCriterion("zip_status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusIsNotNull() {
            addCriterion("zip_status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusEqualTo(Integer value) {
            addCriterion("zip_status =", value, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusNotEqualTo(Integer value) {
            addCriterion("zip_status <>", value, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusGreaterThan(Integer value) {
            addCriterion("zip_status >", value, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("zip_status >=", value, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusLessThan(Integer value) {
            addCriterion("zip_status <", value, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusLessThanOrEqualTo(Integer value) {
            addCriterion("zip_status <=", value, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusIn(List<Integer> values) {
            addCriterion("zip_status in", values, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusNotIn(List<Integer> values) {
            addCriterion("zip_status not in", values, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusBetween(Integer value1, Integer value2) {
            addCriterion("zip_status between", value1, value2, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andZipStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("zip_status not between", value1, value2, "zipStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5IsNull() {
            addCriterion("md5 is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5IsNotNull() {
            addCriterion("md5 is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5EqualTo(String value) {
            addCriterion("md5 =", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5NotEqualTo(String value) {
            addCriterion("md5 <>", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5GreaterThan(String value) {
            addCriterion("md5 >", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5GreaterThanOrEqualTo(String value) {
            addCriterion("md5 >=", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5LessThan(String value) {
            addCriterion("md5 <", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5LessThanOrEqualTo(String value) {
            addCriterion("md5 <=", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5Like(String value) {
            addCriterion("md5 like", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5NotLike(String value) {
            addCriterion("md5 not like", value, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5In(List<String> values) {
            addCriterion("md5 in", values, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5NotIn(List<String> values) {
            addCriterion("md5 not in", values, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5Between(String value1, String value2) {
            addCriterion("md5 between", value1, value2, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andMd5NotBetween(String value1, String value2) {
            addCriterion("md5 not between", value1, value2, "md5");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusIsNull() {
            addCriterion("score_status is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusIsNotNull() {
            addCriterion("score_status is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusEqualTo(Integer value) {
            addCriterion("score_status =", value, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusNotEqualTo(Integer value) {
            addCriterion("score_status <>", value, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusGreaterThan(Integer value) {
            addCriterion("score_status >", value, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusGreaterThanOrEqualTo(Integer value) {
            addCriterion("score_status >=", value, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusLessThan(Integer value) {
            addCriterion("score_status <", value, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusLessThanOrEqualTo(Integer value) {
            addCriterion("score_status <=", value, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusIn(List<Integer> values) {
            addCriterion("score_status in", values, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusNotIn(List<Integer> values) {
            addCriterion("score_status not in", values, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusBetween(Integer value1, Integer value2) {
            addCriterion("score_status between", value1, value2, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andScoreStatusNotBetween(Integer value1, Integer value2) {
            addCriterion("score_status not between", value1, value2, "scoreStatus");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathIsNull() {
            addCriterion("statistic_file_path is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathIsNotNull() {
            addCriterion("statistic_file_path is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathEqualTo(String value) {
            addCriterion("statistic_file_path =", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathNotEqualTo(String value) {
            addCriterion("statistic_file_path <>", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathGreaterThan(String value) {
            addCriterion("statistic_file_path >", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathGreaterThanOrEqualTo(String value) {
            addCriterion("statistic_file_path >=", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathLessThan(String value) {
            addCriterion("statistic_file_path <", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathLessThanOrEqualTo(String value) {
            addCriterion("statistic_file_path <=", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathLike(String value) {
            addCriterion("statistic_file_path like", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathNotLike(String value) {
            addCriterion("statistic_file_path not like", value, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathIn(List<String> values) {
            addCriterion("statistic_file_path in", values, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathNotIn(List<String> values) {
            addCriterion("statistic_file_path not in", values, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathBetween(String value1, String value2) {
            addCriterion("statistic_file_path between", value1, value2, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andStatisticFilePathNotBetween(String value1, String value2) {
            addCriterion("statistic_file_path not between", value1, value2, "statisticFilePath");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleIsNull() {
            addCriterion("show_title is null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleIsNotNull() {
            addCriterion("show_title is not null");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleEqualTo(String value) {
            addCriterion("show_title =", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleNotEqualTo(String value) {
            addCriterion("show_title <>", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleGreaterThan(String value) {
            addCriterion("show_title >", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleGreaterThanOrEqualTo(String value) {
            addCriterion("show_title >=", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleLessThan(String value) {
            addCriterion("show_title <", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleLessThanOrEqualTo(String value) {
            addCriterion("show_title <=", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleLike(String value) {
            addCriterion("show_title like", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleNotLike(String value) {
            addCriterion("show_title not like", value, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleIn(List<String> values) {
            addCriterion("show_title in", values, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleNotIn(List<String> values) {
            addCriterion("show_title not in", values, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleBetween(String value1, String value2) {
            addCriterion("show_title between", value1, value2, "showTitle");
            return (BaseCriteria) this;
        }

        public BaseCriteria andShowTitleNotBetween(String value1, String value2) {
            addCriterion("show_title not between", value1, value2, "showTitle");
            return (BaseCriteria) this;
        }
    }

    public static class BaseCriteria extends GeneratedCriteria {

        protected BaseCriteria() {
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