package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingCleanPersistTaskExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingCleanPersistTaskExample() {
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

        public Criteria andCleanDataFileRecordIdIsNull() {
            addCriterion("clean_data_file_record_id is null");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdIsNotNull() {
            addCriterion("clean_data_file_record_id is not null");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdEqualTo(Long value) {
            addCriterion("clean_data_file_record_id =", value, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdNotEqualTo(Long value) {
            addCriterion("clean_data_file_record_id <>", value, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdGreaterThan(Long value) {
            addCriterion("clean_data_file_record_id >", value, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdGreaterThanOrEqualTo(Long value) {
            addCriterion("clean_data_file_record_id >=", value, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdLessThan(Long value) {
            addCriterion("clean_data_file_record_id <", value, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdLessThanOrEqualTo(Long value) {
            addCriterion("clean_data_file_record_id <=", value, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdIn(List<Long> values) {
            addCriterion("clean_data_file_record_id in", values, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdNotIn(List<Long> values) {
            addCriterion("clean_data_file_record_id not in", values, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdBetween(Long value1, Long value2) {
            addCriterion("clean_data_file_record_id between", value1, value2, "cleanDataFileRecordId");
            return (Criteria) this;
        }

        public Criteria andCleanDataFileRecordIdNotBetween(Long value1, Long value2) {
            addCriterion("clean_data_file_record_id not between", value1, value2, "cleanDataFileRecordId");
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

        public Criteria andSftpFileSeparatorIsNull() {
            addCriterion("sftp_file_separator is null");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorIsNotNull() {
            addCriterion("sftp_file_separator is not null");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorEqualTo(String value) {
            addCriterion("sftp_file_separator =", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorNotEqualTo(String value) {
            addCriterion("sftp_file_separator <>", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorGreaterThan(String value) {
            addCriterion("sftp_file_separator >", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorGreaterThanOrEqualTo(String value) {
            addCriterion("sftp_file_separator >=", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorLessThan(String value) {
            addCriterion("sftp_file_separator <", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorLessThanOrEqualTo(String value) {
            addCriterion("sftp_file_separator <=", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorLike(String value) {
            addCriterion("sftp_file_separator like", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorNotLike(String value) {
            addCriterion("sftp_file_separator not like", value, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorIn(List<String> values) {
            addCriterion("sftp_file_separator in", values, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorNotIn(List<String> values) {
            addCriterion("sftp_file_separator not in", values, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorBetween(String value1, String value2) {
            addCriterion("sftp_file_separator between", value1, value2, "sftpFileSeparator");
            return (Criteria) this;
        }

        public Criteria andSftpFileSeparatorNotBetween(String value1, String value2) {
            addCriterion("sftp_file_separator not between", value1, value2, "sftpFileSeparator");
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

        public Criteria andHeaderMappingIdIsNull() {
            addCriterion("header_mapping_id is null");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdIsNotNull() {
            addCriterion("header_mapping_id is not null");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdEqualTo(Long value) {
            addCriterion("header_mapping_id =", value, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdNotEqualTo(Long value) {
            addCriterion("header_mapping_id <>", value, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdGreaterThan(Long value) {
            addCriterion("header_mapping_id >", value, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdGreaterThanOrEqualTo(Long value) {
            addCriterion("header_mapping_id >=", value, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdLessThan(Long value) {
            addCriterion("header_mapping_id <", value, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdLessThanOrEqualTo(Long value) {
            addCriterion("header_mapping_id <=", value, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdIn(List<Long> values) {
            addCriterion("header_mapping_id in", values, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdNotIn(List<Long> values) {
            addCriterion("header_mapping_id not in", values, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdBetween(Long value1, Long value2) {
            addCriterion("header_mapping_id between", value1, value2, "headerMappingId");
            return (Criteria) this;
        }

        public Criteria andHeaderMappingIdNotBetween(Long value1, Long value2) {
            addCriterion("header_mapping_id not between", value1, value2, "headerMappingId");
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

        public Criteria andTotalRowCountIsNull() {
            addCriterion("total_row_count is null");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountIsNotNull() {
            addCriterion("total_row_count is not null");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountEqualTo(Integer value) {
            addCriterion("total_row_count =", value, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountNotEqualTo(Integer value) {
            addCriterion("total_row_count <>", value, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountGreaterThan(Integer value) {
            addCriterion("total_row_count >", value, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountGreaterThanOrEqualTo(Integer value) {
            addCriterion("total_row_count >=", value, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountLessThan(Integer value) {
            addCriterion("total_row_count <", value, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountLessThanOrEqualTo(Integer value) {
            addCriterion("total_row_count <=", value, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountIn(List<Integer> values) {
            addCriterion("total_row_count in", values, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountNotIn(List<Integer> values) {
            addCriterion("total_row_count not in", values, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountBetween(Integer value1, Integer value2) {
            addCriterion("total_row_count between", value1, value2, "totalRowCount");
            return (Criteria) this;
        }

        public Criteria andTotalRowCountNotBetween(Integer value1, Integer value2) {
            addCriterion("total_row_count not between", value1, value2, "totalRowCount");
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
