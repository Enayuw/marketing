package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DxmSftpConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public DxmSftpConfigExample() {
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

        public Criteria andClientSftpHostIsNull() {
            addCriterion("client_sftp_host is null");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostIsNotNull() {
            addCriterion("client_sftp_host is not null");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostEqualTo(String value) {
            addCriterion("client_sftp_host =", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostNotEqualTo(String value) {
            addCriterion("client_sftp_host <>", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostGreaterThan(String value) {
            addCriterion("client_sftp_host >", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostGreaterThanOrEqualTo(String value) {
            addCriterion("client_sftp_host >=", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostLessThan(String value) {
            addCriterion("client_sftp_host <", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostLessThanOrEqualTo(String value) {
            addCriterion("client_sftp_host <=", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostLike(String value) {
            addCriterion("client_sftp_host like", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostNotLike(String value) {
            addCriterion("client_sftp_host not like", value, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostIn(List<String> values) {
            addCriterion("client_sftp_host in", values, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostNotIn(List<String> values) {
            addCriterion("client_sftp_host not in", values, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostBetween(String value1, String value2) {
            addCriterion("client_sftp_host between", value1, value2, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpHostNotBetween(String value1, String value2) {
            addCriterion("client_sftp_host not between", value1, value2, "clientSftpHost");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortIsNull() {
            addCriterion("client_sftp_port is null");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortIsNotNull() {
            addCriterion("client_sftp_port is not null");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortEqualTo(Integer value) {
            addCriterion("client_sftp_port =", value, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortNotEqualTo(Integer value) {
            addCriterion("client_sftp_port <>", value, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortGreaterThan(Integer value) {
            addCriterion("client_sftp_port >", value, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("client_sftp_port >=", value, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortLessThan(Integer value) {
            addCriterion("client_sftp_port <", value, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortLessThanOrEqualTo(Integer value) {
            addCriterion("client_sftp_port <=", value, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortIn(List<Integer> values) {
            addCriterion("client_sftp_port in", values, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortNotIn(List<Integer> values) {
            addCriterion("client_sftp_port not in", values, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortBetween(Integer value1, Integer value2) {
            addCriterion("client_sftp_port between", value1, value2, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpPortNotBetween(Integer value1, Integer value2) {
            addCriterion("client_sftp_port not between", value1, value2, "clientSftpPort");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserIsNull() {
            addCriterion("client_sftp_user is null");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserIsNotNull() {
            addCriterion("client_sftp_user is not null");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserEqualTo(String value) {
            addCriterion("client_sftp_user =", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserNotEqualTo(String value) {
            addCriterion("client_sftp_user <>", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserGreaterThan(String value) {
            addCriterion("client_sftp_user >", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserGreaterThanOrEqualTo(String value) {
            addCriterion("client_sftp_user >=", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserLessThan(String value) {
            addCriterion("client_sftp_user <", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserLessThanOrEqualTo(String value) {
            addCriterion("client_sftp_user <=", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserLike(String value) {
            addCriterion("client_sftp_user like", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserNotLike(String value) {
            addCriterion("client_sftp_user not like", value, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserIn(List<String> values) {
            addCriterion("client_sftp_user in", values, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserNotIn(List<String> values) {
            addCriterion("client_sftp_user not in", values, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserBetween(String value1, String value2) {
            addCriterion("client_sftp_user between", value1, value2, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpUserNotBetween(String value1, String value2) {
            addCriterion("client_sftp_user not between", value1, value2, "clientSftpUser");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdIsNull() {
            addCriterion("client_sftp_pwd is null");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdIsNotNull() {
            addCriterion("client_sftp_pwd is not null");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdEqualTo(String value) {
            addCriterion("client_sftp_pwd =", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdNotEqualTo(String value) {
            addCriterion("client_sftp_pwd <>", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdGreaterThan(String value) {
            addCriterion("client_sftp_pwd >", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdGreaterThanOrEqualTo(String value) {
            addCriterion("client_sftp_pwd >=", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdLessThan(String value) {
            addCriterion("client_sftp_pwd <", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdLessThanOrEqualTo(String value) {
            addCriterion("client_sftp_pwd <=", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdLike(String value) {
            addCriterion("client_sftp_pwd like", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdNotLike(String value) {
            addCriterion("client_sftp_pwd not like", value, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdIn(List<String> values) {
            addCriterion("client_sftp_pwd in", values, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdNotIn(List<String> values) {
            addCriterion("client_sftp_pwd not in", values, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdBetween(String value1, String value2) {
            addCriterion("client_sftp_pwd between", value1, value2, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPwdNotBetween(String value1, String value2) {
            addCriterion("client_sftp_pwd not between", value1, value2, "clientSftpPwd");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathIsNull() {
            addCriterion("client_sftp_path is null");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathIsNotNull() {
            addCriterion("client_sftp_path is not null");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathEqualTo(String value) {
            addCriterion("client_sftp_path =", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathNotEqualTo(String value) {
            addCriterion("client_sftp_path <>", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathGreaterThan(String value) {
            addCriterion("client_sftp_path >", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathGreaterThanOrEqualTo(String value) {
            addCriterion("client_sftp_path >=", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathLessThan(String value) {
            addCriterion("client_sftp_path <", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathLessThanOrEqualTo(String value) {
            addCriterion("client_sftp_path <=", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathLike(String value) {
            addCriterion("client_sftp_path like", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathNotLike(String value) {
            addCriterion("client_sftp_path not like", value, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathIn(List<String> values) {
            addCriterion("client_sftp_path in", values, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathNotIn(List<String> values) {
            addCriterion("client_sftp_path not in", values, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathBetween(String value1, String value2) {
            addCriterion("client_sftp_path between", value1, value2, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andClientSftpPathNotBetween(String value1, String value2) {
            addCriterion("client_sftp_path not between", value1, value2, "clientSftpPath");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyIsNull() {
            addCriterion("rsa_private_key is null");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyIsNotNull() {
            addCriterion("rsa_private_key is not null");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyEqualTo(String value) {
            addCriterion("rsa_private_key =", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyNotEqualTo(String value) {
            addCriterion("rsa_private_key <>", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyGreaterThan(String value) {
            addCriterion("rsa_private_key >", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyGreaterThanOrEqualTo(String value) {
            addCriterion("rsa_private_key >=", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyLessThan(String value) {
            addCriterion("rsa_private_key <", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyLessThanOrEqualTo(String value) {
            addCriterion("rsa_private_key <=", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyLike(String value) {
            addCriterion("rsa_private_key like", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyNotLike(String value) {
            addCriterion("rsa_private_key not like", value, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyIn(List<String> values) {
            addCriterion("rsa_private_key in", values, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyNotIn(List<String> values) {
            addCriterion("rsa_private_key not in", values, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyBetween(String value1, String value2) {
            addCriterion("rsa_private_key between", value1, value2, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPrivateKeyNotBetween(String value1, String value2) {
            addCriterion("rsa_private_key not between", value1, value2, "rsaPrivateKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyIsNull() {
            addCriterion("rsa_public_key is null");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyIsNotNull() {
            addCriterion("rsa_public_key is not null");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyEqualTo(String value) {
            addCriterion("rsa_public_key =", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyNotEqualTo(String value) {
            addCriterion("rsa_public_key <>", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyGreaterThan(String value) {
            addCriterion("rsa_public_key >", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyGreaterThanOrEqualTo(String value) {
            addCriterion("rsa_public_key >=", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyLessThan(String value) {
            addCriterion("rsa_public_key <", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyLessThanOrEqualTo(String value) {
            addCriterion("rsa_public_key <=", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyLike(String value) {
            addCriterion("rsa_public_key like", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyNotLike(String value) {
            addCriterion("rsa_public_key not like", value, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyIn(List<String> values) {
            addCriterion("rsa_public_key in", values, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyNotIn(List<String> values) {
            addCriterion("rsa_public_key not in", values, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyBetween(String value1, String value2) {
            addCriterion("rsa_public_key between", value1, value2, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andRsaPublicKeyNotBetween(String value1, String value2) {
            addCriterion("rsa_public_key not between", value1, value2, "rsaPublicKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyIsNull() {
            addCriterion("aes_key is null");
            return (Criteria) this;
        }

        public Criteria andAesKeyIsNotNull() {
            addCriterion("aes_key is not null");
            return (Criteria) this;
        }

        public Criteria andAesKeyEqualTo(String value) {
            addCriterion("aes_key =", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyNotEqualTo(String value) {
            addCriterion("aes_key <>", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyGreaterThan(String value) {
            addCriterion("aes_key >", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyGreaterThanOrEqualTo(String value) {
            addCriterion("aes_key >=", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyLessThan(String value) {
            addCriterion("aes_key <", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyLessThanOrEqualTo(String value) {
            addCriterion("aes_key <=", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyLike(String value) {
            addCriterion("aes_key like", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyNotLike(String value) {
            addCriterion("aes_key not like", value, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyIn(List<String> values) {
            addCriterion("aes_key in", values, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyNotIn(List<String> values) {
            addCriterion("aes_key not in", values, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyBetween(String value1, String value2) {
            addCriterion("aes_key between", value1, value2, "aesKey");
            return (Criteria) this;
        }

        public Criteria andAesKeyNotBetween(String value1, String value2) {
            addCriterion("aes_key not between", value1, value2, "aesKey");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostIsNull() {
            addCriterion("internal_sftp_host is null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostIsNotNull() {
            addCriterion("internal_sftp_host is not null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostEqualTo(String value) {
            addCriterion("internal_sftp_host =", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostNotEqualTo(String value) {
            addCriterion("internal_sftp_host <>", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostGreaterThan(String value) {
            addCriterion("internal_sftp_host >", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostGreaterThanOrEqualTo(String value) {
            addCriterion("internal_sftp_host >=", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostLessThan(String value) {
            addCriterion("internal_sftp_host <", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostLessThanOrEqualTo(String value) {
            addCriterion("internal_sftp_host <=", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostLike(String value) {
            addCriterion("internal_sftp_host like", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostNotLike(String value) {
            addCriterion("internal_sftp_host not like", value, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostIn(List<String> values) {
            addCriterion("internal_sftp_host in", values, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostNotIn(List<String> values) {
            addCriterion("internal_sftp_host not in", values, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostBetween(String value1, String value2) {
            addCriterion("internal_sftp_host between", value1, value2, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpHostNotBetween(String value1, String value2) {
            addCriterion("internal_sftp_host not between", value1, value2, "internalSftpHost");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortIsNull() {
            addCriterion("internal_sftp_port is null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortIsNotNull() {
            addCriterion("internal_sftp_port is not null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortEqualTo(Integer value) {
            addCriterion("internal_sftp_port =", value, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortNotEqualTo(Integer value) {
            addCriterion("internal_sftp_port <>", value, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortGreaterThan(Integer value) {
            addCriterion("internal_sftp_port >", value, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortGreaterThanOrEqualTo(Integer value) {
            addCriterion("internal_sftp_port >=", value, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortLessThan(Integer value) {
            addCriterion("internal_sftp_port <", value, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortLessThanOrEqualTo(Integer value) {
            addCriterion("internal_sftp_port <=", value, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortIn(List<Integer> values) {
            addCriterion("internal_sftp_port in", values, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortNotIn(List<Integer> values) {
            addCriterion("internal_sftp_port not in", values, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortBetween(Integer value1, Integer value2) {
            addCriterion("internal_sftp_port between", value1, value2, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPortNotBetween(Integer value1, Integer value2) {
            addCriterion("internal_sftp_port not between", value1, value2, "internalSftpPort");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserIsNull() {
            addCriterion("internal_sftp_user is null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserIsNotNull() {
            addCriterion("internal_sftp_user is not null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserEqualTo(String value) {
            addCriterion("internal_sftp_user =", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserNotEqualTo(String value) {
            addCriterion("internal_sftp_user <>", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserGreaterThan(String value) {
            addCriterion("internal_sftp_user >", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserGreaterThanOrEqualTo(String value) {
            addCriterion("internal_sftp_user >=", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserLessThan(String value) {
            addCriterion("internal_sftp_user <", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserLessThanOrEqualTo(String value) {
            addCriterion("internal_sftp_user <=", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserLike(String value) {
            addCriterion("internal_sftp_user like", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserNotLike(String value) {
            addCriterion("internal_sftp_user not like", value, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserIn(List<String> values) {
            addCriterion("internal_sftp_user in", values, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserNotIn(List<String> values) {
            addCriterion("internal_sftp_user not in", values, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserBetween(String value1, String value2) {
            addCriterion("internal_sftp_user between", value1, value2, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpUserNotBetween(String value1, String value2) {
            addCriterion("internal_sftp_user not between", value1, value2, "internalSftpUser");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdIsNull() {
            addCriterion("internal_sftp_pwd is null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdIsNotNull() {
            addCriterion("internal_sftp_pwd is not null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdEqualTo(String value) {
            addCriterion("internal_sftp_pwd =", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdNotEqualTo(String value) {
            addCriterion("internal_sftp_pwd <>", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdGreaterThan(String value) {
            addCriterion("internal_sftp_pwd >", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdGreaterThanOrEqualTo(String value) {
            addCriterion("internal_sftp_pwd >=", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdLessThan(String value) {
            addCriterion("internal_sftp_pwd <", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdLessThanOrEqualTo(String value) {
            addCriterion("internal_sftp_pwd <=", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdLike(String value) {
            addCriterion("internal_sftp_pwd like", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdNotLike(String value) {
            addCriterion("internal_sftp_pwd not like", value, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdIn(List<String> values) {
            addCriterion("internal_sftp_pwd in", values, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdNotIn(List<String> values) {
            addCriterion("internal_sftp_pwd not in", values, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdBetween(String value1, String value2) {
            addCriterion("internal_sftp_pwd between", value1, value2, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPwdNotBetween(String value1, String value2) {
            addCriterion("internal_sftp_pwd not between", value1, value2, "internalSftpPwd");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathIsNull() {
            addCriterion("internal_sftp_path is null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathIsNotNull() {
            addCriterion("internal_sftp_path is not null");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathEqualTo(String value) {
            addCriterion("internal_sftp_path =", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathNotEqualTo(String value) {
            addCriterion("internal_sftp_path <>", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathGreaterThan(String value) {
            addCriterion("internal_sftp_path >", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathGreaterThanOrEqualTo(String value) {
            addCriterion("internal_sftp_path >=", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathLessThan(String value) {
            addCriterion("internal_sftp_path <", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathLessThanOrEqualTo(String value) {
            addCriterion("internal_sftp_path <=", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathLike(String value) {
            addCriterion("internal_sftp_path like", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathNotLike(String value) {
            addCriterion("internal_sftp_path not like", value, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathIn(List<String> values) {
            addCriterion("internal_sftp_path in", values, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathNotIn(List<String> values) {
            addCriterion("internal_sftp_path not in", values, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathBetween(String value1, String value2) {
            addCriterion("internal_sftp_path between", value1, value2, "internalSftpPath");
            return (Criteria) this;
        }

        public Criteria andInternalSftpPathNotBetween(String value1, String value2) {
            addCriterion("internal_sftp_path not between", value1, value2, "internalSftpPath");
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

        public Criteria andStatusEqualTo(Byte value) {
            addCriterion("`status` =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(Byte value) {
            addCriterion("`status` <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(Byte value) {
            addCriterion("`status` >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(Byte value) {
            addCriterion("`status` >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(Byte value) {
            addCriterion("`status` <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(Byte value) {
            addCriterion("`status` <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<Byte> values) {
            addCriterion("`status` in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<Byte> values) {
            addCriterion("`status` not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(Byte value1, Byte value2) {
            addCriterion("`status` between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(Byte value1, Byte value2) {
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

        public Criteria andTypeIsNull() {
            addCriterion("`type` is null");
            return (Criteria) this;
        }

        public Criteria andTypeIsNotNull() {
            addCriterion("`type` is not null");
            return (Criteria) this;
        }

        public Criteria andTypeEqualTo(Byte value) {
            addCriterion("`type` =", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotEqualTo(Byte value) {
            addCriterion("`type` <>", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThan(Byte value) {
            addCriterion("`type` >", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeGreaterThanOrEqualTo(Byte value) {
            addCriterion("`type` >=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThan(Byte value) {
            addCriterion("`type` <", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeLessThanOrEqualTo(Byte value) {
            addCriterion("`type` <=", value, "type");
            return (Criteria) this;
        }

        public Criteria andTypeIn(List<Byte> values) {
            addCriterion("`type` in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotIn(List<Byte> values) {
            addCriterion("`type` not in", values, "type");
            return (Criteria) this;
        }

        public Criteria andTypeBetween(Byte value1, Byte value2) {
            addCriterion("`type` between", value1, value2, "type");
            return (Criteria) this;
        }

        public Criteria andTypeNotBetween(Byte value1, Byte value2) {
            addCriterion("`type` not between", value1, value2, "type");
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