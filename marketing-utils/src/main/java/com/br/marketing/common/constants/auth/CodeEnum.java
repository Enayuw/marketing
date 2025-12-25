package com.br.marketing.common.constants.auth;

public enum CodeEnum {
    SUCC("000000", "成功"),
    FAILURE("100001", "失败"),
    ERROR("100002", "内部错误"),
    PARAM_ERROR("100003", "参数错误"),
    CHECK_CODE_ERROR("100005", "验证码错误"),
    NAME_PWD_ERROR("100006", "用户名或密码错误"),
    NOT_FOUND_ACCOUNT("100007", "账号不存在"),
    USER_INVALID_SESSION_ERROR("100013", "session过期"),
    USER_NOTRESOURCES_ERROR("100014", "访问权限受制，禁止访问！"),
    ALREADY_USED("100015", "正在使用中，无法删除"),
    SERVICE_ERROR("100000", "服务器开小差了，请稍后再试！"),
    REPEAT("103004", "帐号已存在！！"),
    NAME_REPEAT("103005", "名称已存在！！"),
    PERMISSION_DENIED("103006", "权限不足..."),
    ILLEGAL_PARAM("103007", "参数不合法!"),
    MIN_PARAM_ERROR("100033", "必选key值缺失"),
    DELETE_ZERO_RULETYPE("100015", "删除规则集为0"),
    PASSWD_ERROR("100004", "密码错误"),
    CONFIRM_PASSWORD_ERROR("100006", "确认密码错误"),
    codeT_FOUND_ACCOUNT("100007", "账号不存在"),
    IS_INVALID_ACCOUNT("100008", "该用户已锁定，请联系管理员解锁"),
    IS_FIRST_LOGIN("100009", "首次登录"),
    LOGIN_PASSWD_ERR_MORE_THEN_COUNT("100010", "登录密码错误输入超过五次"),
    INVALID_IP_ADDRESS("100011", "IP登录受限"),
    ERROR_FILE("100015", "文件格式错误"),
    ERROR_FILE_UPLOAD_SYSTEM("100016", "上传失败，文件系统异常，请联系客服"),
    OCCUPIED("103000", "该apiCode创建过管理员"),
    NOT_PRODUCT("103001", "未选择开通的业务产品！"),
    UCCOMPAY_ERROR("103002", "插入公司信息失败！"),
    UCCOMPAY_EXIST("103003", "该公司已存在！"),
    CREATE_NOT_APICODE("103005", "未获取公司对应的ApiCode"),
    CREATE_NOT_USERNAME("103006", "未获取到公司名称"),
    NOT_USER_DATA_BY_APICODE("103007", "未获取到该apiCode用户信息"),
    USER_TYPE_ERROE("103008", "商户类型非管理员"),
    NOT_RULES("103009", "没有规则集"),
    NOT_TYPE_VERSION("103017", "没有规则集版本"),
    NOT_IDSTR_EMPTY("103010", "Id不能为空"),
    RESPONSE_CODE_EMPTY("103011", "无数据"),
    RESPONSE_CODE_COMPANY_USECOUNT_IS_OVER("103012", "公司审批次数超出"),
    RESPONSE_CODE_APPROVEING_NOT_OTHER("103013", "正在审批中不能进行其他操作"),
    FIELDS("103015", "规则变量长度大于20"),
    VALIDATE_RULE("103016", "没有参数或权重超出正常范围(100=>权重>0)或style为空"),
    TIMEOUT("103014", "匹配结果为空，未生成报告"),
    EXAMINE_USECOUNT_IS_OVER("100010", "您已超出当日访问限制次数!"),
    COMPASS_IN_LOANWARN_SUCC("1000018", "转入贷中成功"),
    CHANGETOLOAN_REPEAT("1000019", "贷前转贷中，该条数据已转入过贷中。"),
    HAVE_TASK("1000020", "当前有任务正在进行中，请稍等！"),
    COMPASS_IN_LOANWARN_ERROR("1000099", "转入贷中失败"),
    COBWEB_OVERLIMITS("2000001", "超出次数限制"),
    COBWEB_COMPANY_NOT_EXITS("2000002", "没有开通贷后管理"),
    COBWEB_NORIGHTS("2000003", "没有开通商品权限"),
    COBWEB_REQUEST_WRONG("2000004", "请求不合法"),
    COBWEB_OVERL_TIMES("2000005", "业务已经过期"),
    RULES_ERROR("3000000", "规则集依赖数据产品重复"),
    NAME_EXIST("3000001", "策略名称已存在"),
    IP_ADDRESS_ERROR("3000002", "IP地址获取失败！"),
    IP_CHECK_FAIL("3000003", "IP白名单校验未通过！"),
    API_SUCC("00", "成功"),
    API_OTHER_ERROR("100001", "其他错误"),
    API_DECISION_ERROR("200007", "决策结果为空"),
    API_PARAM_ERROR("100003", "必选key值缺失或不合法"),
    API_OVER_LIMIT("100010", "条数不足"),
    API_USER_ERROR("100011", "账户停用，请联系客服"),
    API_STRATEGY_ERROR("100013", "apicode未配置此策略"),
    API_NOT_PRODUCT("100014", "产品权限不足"),
    API_ALL_SUCC("200008", "全部策略成功"),
    API_NOT_ALL_SUCC("200009", "部分策略成功"),
    API_ALL_FAIL("200010", "全部策略失败"),
    API_FLAT_ERROR("200006", "数据打平错误"),
    TASK_PRIORITY_EXIST("100401", "当前当前排序数字已存在"),

    NOT_FOUND_CLEAN_RULE_CONFIG("100004","未配置清洗规则"),
    NOT_FOUND_DATA_ITEMS_CONFIG("100005","未配置dataItems字段");


    private final String code;
    private final String message;

    private CodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }
}
