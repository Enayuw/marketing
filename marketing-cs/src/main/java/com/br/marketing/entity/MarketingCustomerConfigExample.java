package com.br.marketing.entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MarketingCustomerConfigExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public MarketingCustomerConfigExample() {
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

        public Criteria andThreeKEncryptTypeIsNull() {
            addCriterion("three_k_encrypt_type is null");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeIsNotNull() {
            addCriterion("three_k_encrypt_type is not null");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeEqualTo(Integer value) {
            addCriterion("three_k_encrypt_type =", value, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeNotEqualTo(Integer value) {
            addCriterion("three_k_encrypt_type <>", value, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeGreaterThan(Integer value) {
            addCriterion("three_k_encrypt_type >", value, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("three_k_encrypt_type >=", value, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeLessThan(Integer value) {
            addCriterion("three_k_encrypt_type <", value, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeLessThanOrEqualTo(Integer value) {
            addCriterion("three_k_encrypt_type <=", value, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeIn(List<Integer> values) {
            addCriterion("three_k_encrypt_type in", values, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeNotIn(List<Integer> values) {
            addCriterion("three_k_encrypt_type not in", values, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeBetween(Integer value1, Integer value2) {
            addCriterion("three_k_encrypt_type between", value1, value2, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andThreeKEncryptTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("three_k_encrypt_type not between", value1, value2, "threeKEncryptType");
            return (Criteria) this;
        }

        public Criteria andCipherModeIsNull() {
            addCriterion("cipher_mode is null");
            return (Criteria) this;
        }

        public Criteria andCipherModeIsNotNull() {
            addCriterion("cipher_mode is not null");
            return (Criteria) this;
        }

        public Criteria andCipherModeEqualTo(String value) {
            addCriterion("cipher_mode =", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeNotEqualTo(String value) {
            addCriterion("cipher_mode <>", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeGreaterThan(String value) {
            addCriterion("cipher_mode >", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeGreaterThanOrEqualTo(String value) {
            addCriterion("cipher_mode >=", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeLessThan(String value) {
            addCriterion("cipher_mode <", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeLessThanOrEqualTo(String value) {
            addCriterion("cipher_mode <=", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeLike(String value) {
            addCriterion("cipher_mode like", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeNotLike(String value) {
            addCriterion("cipher_mode not like", value, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeIn(List<String> values) {
            addCriterion("cipher_mode in", values, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeNotIn(List<String> values) {
            addCriterion("cipher_mode not in", values, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeBetween(String value1, String value2) {
            addCriterion("cipher_mode between", value1, value2, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andCipherModeNotBetween(String value1, String value2) {
            addCriterion("cipher_mode not between", value1, value2, "cipherMode");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeIsNull() {
            addCriterion("padding_scheme is null");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeIsNotNull() {
            addCriterion("padding_scheme is not null");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeEqualTo(String value) {
            addCriterion("padding_scheme =", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeNotEqualTo(String value) {
            addCriterion("padding_scheme <>", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeGreaterThan(String value) {
            addCriterion("padding_scheme >", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeGreaterThanOrEqualTo(String value) {
            addCriterion("padding_scheme >=", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeLessThan(String value) {
            addCriterion("padding_scheme <", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeLessThanOrEqualTo(String value) {
            addCriterion("padding_scheme <=", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeLike(String value) {
            addCriterion("padding_scheme like", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeNotLike(String value) {
            addCriterion("padding_scheme not like", value, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeIn(List<String> values) {
            addCriterion("padding_scheme in", values, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeNotIn(List<String> values) {
            addCriterion("padding_scheme not in", values, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeBetween(String value1, String value2) {
            addCriterion("padding_scheme between", value1, value2, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andPaddingSchemeNotBetween(String value1, String value2) {
            addCriterion("padding_scheme not between", value1, value2, "paddingScheme");
            return (Criteria) this;
        }

        public Criteria andCharsetIsNull() {
            addCriterion("charset is null");
            return (Criteria) this;
        }

        public Criteria andCharsetIsNotNull() {
            addCriterion("charset is not null");
            return (Criteria) this;
        }

        public Criteria andCharsetEqualTo(String value) {
            addCriterion("charset =", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetNotEqualTo(String value) {
            addCriterion("charset <>", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetGreaterThan(String value) {
            addCriterion("charset >", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetGreaterThanOrEqualTo(String value) {
            addCriterion("charset >=", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetLessThan(String value) {
            addCriterion("charset <", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetLessThanOrEqualTo(String value) {
            addCriterion("charset <=", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetLike(String value) {
            addCriterion("charset like", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetNotLike(String value) {
            addCriterion("charset not like", value, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetIn(List<String> values) {
            addCriterion("charset in", values, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetNotIn(List<String> values) {
            addCriterion("charset not in", values, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetBetween(String value1, String value2) {
            addCriterion("charset between", value1, value2, "charset");
            return (Criteria) this;
        }

        public Criteria andCharsetNotBetween(String value1, String value2) {
            addCriterion("charset not between", value1, value2, "charset");
            return (Criteria) this;
        }

        public Criteria andIvIsNull() {
            addCriterion("iv is null");
            return (Criteria) this;
        }

        public Criteria andIvIsNotNull() {
            addCriterion("iv is not null");
            return (Criteria) this;
        }

        public Criteria andIvEqualTo(String value) {
            addCriterion("iv =", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvNotEqualTo(String value) {
            addCriterion("iv <>", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvGreaterThan(String value) {
            addCriterion("iv >", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvGreaterThanOrEqualTo(String value) {
            addCriterion("iv >=", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvLessThan(String value) {
            addCriterion("iv <", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvLessThanOrEqualTo(String value) {
            addCriterion("iv <=", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvLike(String value) {
            addCriterion("iv like", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvNotLike(String value) {
            addCriterion("iv not like", value, "iv");
            return (Criteria) this;
        }

        public Criteria andIvIn(List<String> values) {
            addCriterion("iv in", values, "iv");
            return (Criteria) this;
        }

        public Criteria andIvNotIn(List<String> values) {
            addCriterion("iv not in", values, "iv");
            return (Criteria) this;
        }

        public Criteria andIvBetween(String value1, String value2) {
            addCriterion("iv between", value1, value2, "iv");
            return (Criteria) this;
        }

        public Criteria andIvNotBetween(String value1, String value2) {
            addCriterion("iv not between", value1, value2, "iv");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysIsNull() {
            addCriterion("dynamic_keys is null");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysIsNotNull() {
            addCriterion("dynamic_keys is not null");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysEqualTo(String value) {
            addCriterion("dynamic_keys =", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysNotEqualTo(String value) {
            addCriterion("dynamic_keys <>", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysGreaterThan(String value) {
            addCriterion("dynamic_keys >", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysGreaterThanOrEqualTo(String value) {
            addCriterion("dynamic_keys >=", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysLessThan(String value) {
            addCriterion("dynamic_keys <", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysLessThanOrEqualTo(String value) {
            addCriterion("dynamic_keys <=", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysLike(String value) {
            addCriterion("dynamic_keys like", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysNotLike(String value) {
            addCriterion("dynamic_keys not like", value, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysIn(List<String> values) {
            addCriterion("dynamic_keys in", values, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysNotIn(List<String> values) {
            addCriterion("dynamic_keys not in", values, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysBetween(String value1, String value2) {
            addCriterion("dynamic_keys between", value1, value2, "dynamicKeys");
            return (Criteria) this;
        }

        public Criteria andDynamicKeysNotBetween(String value1, String value2) {
            addCriterion("dynamic_keys not between", value1, value2, "dynamicKeys");
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

        public Criteria andCheckTypeIsNull() {
            addCriterion("check_type is null");
            return (Criteria) this;
        }

        public Criteria andCheckTypeIsNotNull() {
            addCriterion("check_type is not null");
            return (Criteria) this;
        }

        public Criteria andCheckTypeEqualTo(Integer value) {
            addCriterion("check_type =", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeNotEqualTo(Integer value) {
            addCriterion("check_type <>", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeGreaterThan(Integer value) {
            addCriterion("check_type >", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeGreaterThanOrEqualTo(Integer value) {
            addCriterion("check_type >=", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeLessThan(Integer value) {
            addCriterion("check_type <", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeLessThanOrEqualTo(Integer value) {
            addCriterion("check_type <=", value, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeIn(List<Integer> values) {
            addCriterion("check_type in", values, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeNotIn(List<Integer> values) {
            addCriterion("check_type not in", values, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeBetween(Integer value1, Integer value2) {
            addCriterion("check_type between", value1, value2, "checkType");
            return (Criteria) this;
        }

        public Criteria andCheckTypeNotBetween(Integer value1, Integer value2) {
            addCriterion("check_type not between", value1, value2, "checkType");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorIsNull() {
            addCriterion("score_separator is null");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorIsNotNull() {
            addCriterion("score_separator is not null");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorEqualTo(String value) {
            addCriterion("score_separator =", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorNotEqualTo(String value) {
            addCriterion("score_separator <>", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorGreaterThan(String value) {
            addCriterion("score_separator >", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorGreaterThanOrEqualTo(String value) {
            addCriterion("score_separator >=", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorLessThan(String value) {
            addCriterion("score_separator <", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorLessThanOrEqualTo(String value) {
            addCriterion("score_separator <=", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorLike(String value) {
            addCriterion("score_separator like", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorNotLike(String value) {
            addCriterion("score_separator not like", value, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorIn(List<String> values) {
            addCriterion("score_separator in", values, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorNotIn(List<String> values) {
            addCriterion("score_separator not in", values, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorBetween(String value1, String value2) {
            addCriterion("score_separator between", value1, value2, "scoreSeparator");
            return (Criteria) this;
        }

        public Criteria andScoreSeparatorNotBetween(String value1, String value2) {
            addCriterion("score_separator not between", value1, value2, "scoreSeparator");
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