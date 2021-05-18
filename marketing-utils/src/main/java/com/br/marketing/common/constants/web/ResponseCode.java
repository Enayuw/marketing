package com.br.marketing.common.constants.web;


import com.br.marketing.common.utils.StringUtils;

/**
 * 返回值编号与信息枚举
 *
 * @author Wang Weiwei <email>weiwei02@vip.qq.com / weiwei.wang@100credit.com</email>
 * @version 1.0
 * @sine 2017/12/31
 */
public enum ResponseCode {
    //成功
    SUCC("00", "成功"),
    ERR_NULL("100003", "必选key值缺失或不合法"),
    ERR_PARAM("100006", "请求参数格式错误"),
    MISS_RESULT("100002", "匹配结果为空"),
    MISS_JSON("1000016", "捕获请求json异常，无法解析的错误"),
    ERR_ID_CARD("800001", "身份证号码错误，请重试"),
    ERR_PHONE("800002", "手机号码错误，请重试"),
    ERR_NAME("800003", "姓名错误，请重试"),
    ERR_CUS_NUM("800031", "客户编号错误，请重试"),
    ERR_DATE_FORMAT("800004", "日期格式错误，请重试"),
    ERR_RELATION_FLAG("800005", "关联拨打标记错误，请确认"),
    ERR_TUOMIN_FLAG("800006", "数据脱敏展示标记错误，请重试"),
    ERR_OVER_DUE_FLAG("800007", "逾期标记错误，请重试"),
    ERR_LINK_NAME("800008", "联系人姓名错误，请重试"),
    ERR_LINK_PHONE("800009", "联系人手机号错误，请重试"),
    ERR_PERMISSION("800010", "无接口权限，请联系客服"),
    ERROR_PERMISSION_RELATION("800011", "无号码关联核查权限,请联系客服"),
    ERR_PERMISSION_PHONE("800012", "该apicode无查询该流水权限，请联系客服"),
    ERR_LINK_SIZE("800013", "最多只能有3个联系人，请重试"),
    ERR_LINK_PHONE_SIZE("800014", "最多只能有3个联系人电话，请重试"),
    ERR_PERMISSION_NUMBER("800015", "可用条数不足，请确认，若需要请联系客服"),
    TASK_RUN("800016", "任务正在进行中，请稍后再查询"),
    NO_TASK("800017", "该流水不存在，请校验后再查询"),
    ERR_LINK_PHONE_FORMAT("800018", "联系人格式错误，请重试"),
    TASK_TO_MANY("800019", "当前任务过多，请稍后再查询"),
    PARSE_ID_CARD_ERR("800020", "身份证号码异常，请重试"),
    PARSE_PHONE_ERR("800121", "手机号码异常，请重试"),
    ERR_PERMISSION_PRODUCT("800122","贷中策略无数据产品权限，请确认"),
    ERR_PERMISSION_PRODUCT_DAYNUM("800123","产品日访问条数已超限，请联系客服"),

    ACCOUNT_OVERDUE("800022", "使用到期，请联系客服"),
    ACCOUNT_ERR("800030", "账号错误，请联系客服"),

    ERR_SYSTEM("999998", "接口异常，请联系客服"),
    ERR_PHONE_BUG("999999", "电话虫拨打异常，请联系客服"),
    //批量处理返回码
    //SUC_BATCH_PUT("800020","批量上传成功"),
    ERR_BATCH_COUNT("800221", "批量上传的数量超过1000"),
    ERR_BATCH_IS_NULL("800222", "批量上传的内容为空"),
    ERR_BATCH_ID("800129", "批量上传的编号为空或无编号"),
    ERR_BATCH_PUT("800023", "批量上传全部失败"),
    ERR_BATCH_PUT_PART("800024", "批量上传部分失败"),
    //SUC_BATCH_QUERY("800024","批量查询成功"),
    ERR_BATCH_QUERY("800025", "批量查询失败"),
    NO_RECORDS("800027", "流水号不存在或任务未开始执行"),
    ERROR_DELETE_TIME("800028", "数据删除时间小于当前时间"),
    ERR_DTB_BATCH_COUNT("800321", "数据策略只支持单条"),
    ERROR_FILE_TEMPLATE("800130", "文件模版不合法，请按照系统标准模版检查"),

    // 策略贷中专属状态码
    ERROR_PASS_DATE_TOO_BIGER("800100", "审批通过日大于当前时间"),
    ERROR_MATURITY_DATE_TOO_BIGER("800101", "审批通过日大于或等于贷款到期日"),
    ERROR_MATURITY_DATE_BIGER_NOW("800131", "贷款到期日小于当前时间"),
    ERROR_APPROVE_RESULT("800111", "贷前审批结果错误"),
    STRATEGY_STOPED("800102", "贷中策略已被禁用"),
    ERROR_STRATEGR_ID("800103", "贷中策略编号错误"),
    MISS_STRATEGR_ID("800104", "贷中策略编号不存在"),
    MISS_DTB_STRATEGR_ID("800106", "数据策略编号不存在"),
    DTB_STRATEGR_ERROR("800107", "数据策略配置错误"),
    NO_SWIFT_NUMBER_PERMISSION("800105", "无该流水查询权限"),
    ERROR_PERMISSION_STATUS("800051", "策略贷中状态为不可用"),
    ERROR_PERMISSION_ENDDATE("800052", "超过策略贷中截止时间"),
    ERROR_START_RULE("800053", "开启策略贷中规则集异常"),
    ERR_TYPE("800054","修改内容错误"),
    ERROR_USER_DATE_TOO_BIGER("800055", "观察日大于当前时间"),
    ERROR_MATURITY_DATE_TOO_BIGER_USER("800056", "观察日大于或等于贷款到期日"),
    ERROR_PASS_DATE_TOO_BIGER_USER("800057", "观察日大于或等于审批通过日"),
    ERROR_USER_TIME_NOTTODAY("800058", "当日版观察日必须在当天时间范围内"),

    //自动监控状态码
    ERROR_MONITOR_FREQUENCY("800060", "监控周期错误"),
    ERROR_CELL_STATUS_CHECK("800061", "号码状态监控标记错误"),
    ERROR_MONITOR_DUE_TIME("800062", "监控截止日期错误"),
    ERROR_MONITOR_PERSON("800063", "被监控人信息错误"),
    ERROR_BATCH_NUMBER("800064", "批次号错误"),
    ERROR_BATCH_REPEAT_PERSON("800065", "被监控人重复"),
    ERROR_MONITOR_TIME("800066", "监控日期错误"),
    ERROR_MONITOR_MODIFY_TYPE("800067", "监控修改类型错误"),
    ERROR_LOAN_APPROVE_RESULT("800068", "贷前审批结果错误"),
    ERROR_PHONE_PERMISSION("800069", "无电话号码核查权限"),
    ERROR_BATCH_NUMBER_STATUS("800070", "批次号状态错误"),
    ERROR_MONITOR_PERSON_STATUS("800071", "被监控人状态错误"),
    ERROR_MONITOR_DELAY_DAYS("800072", "延长监控天数有误"),
    MONITOR_PERSON_NOT_EXISTS("800073", "批次号或客户编号对应监控人不存在"),
    MONITOR_SCHEDULE_ERROR("800074", "定期监控异常"),
    ERR_BATCH_UPDATE("800075", "批量更新全部失败"),
    ERR_BATCH_UPDATE_PART("800076", "批量更新部分失败"),
    ERR_BATCH_DELETE("800077", "批量更新全部失败"),
    ERR_BATCH_DELETE_PART("800078", "批量更新部分失败"),
    REPEAT_MONITOR_TASK_PERSON("800079", "监控任务中被监控人已存在"),
    ERROR_MONITOR_UPLOAD_COUNT("800080", "定期监控上传条数超限"),
    ERROR_MONITOR_UPLOAD_EMPTY("800081", "定期监控上传内容为空"),
    ERROR_MONITOR_PASS_DATE("800082","审批通过日为空或格式非法"),
    ERROR_MONITOR_UPLOAD_TEMPLATE("800083","请严格按照模板上传数据"),
    ERR_PERMISSION_DAY_ACCESS("800201","本日访问次数超限,请联系客服"),
//     号码核查web端错误码
//    ERROR_PERMISSION_RELATION("800050", "无号码关联核查权限"),

    //预警条件错误码
    ERR_ALARM_COND_DISABLE("800400","禁用预警条件失败，仍有监控任务使用中"),
    ERR_ALARM_COND_REPEAT("800401","条件名称已存在，请重新输入"),
    SX_EXCEPTION("800059", "三相之力结果异常");



    private final String code;
    private final String message;

    private ResponseCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static ResponseCode codeOf(String code) {
        if (StringUtils.isNotEmpty(code)) {
            for (ResponseCode responseCode : ResponseCode.values()) {
                if (code.equals(responseCode.getCode())) {
                    return responseCode;
                }
            }
        }
        return null;
    }
}
