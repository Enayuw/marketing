package com.br.marketing.common.constants;

/**
 * 1 对外输出均采用6位编码，但是不能使用1开头的编码，1开头的编码是安全代理使用的。
 *
 * 2 6位编码的含义：
 *
 *    前两位：系统标识位，例如30代表的是智能营销中台
 *
 *    第三位：异常类型，0代表数据本身错误，1代表客户权限错误，2代表内部校验错误
 *
 *    其余三位：根据具体场景扩展
 *
 * 3 正常：00
 *
 *    重入：999999
 *
 *    其它异常：999998
 *
 *
 */
public enum MarketingErrorInfo {
    //成功
    SUCCESS("00", "成功"),

    //参数异常--客户上送数据问题
    JSON_DATA_ERROR("300001", "jsonData非法"),
    TASK_ID_ERROR("300003", "taskid非法"),
    REQUEST_ID_ERROR("300004", "requestId非法"),
    CUST_NUM_ERROR("300005", "custNum非法"),
    GROUP_TYPE_ERROR("300006", "groupType或者userType非法"),
    TIME_FORMAT_ERROR("300007", "时间格式错误"),
    QUANTITY_ERROR("300008", "数据量过大或者为空"),
    LAST_ERROR("300009", "last非法"),
    TOTAL_ERROR("300010", "total非法"),
    PARAM_ISNULL_ERROR("300011", "必填参数缺失"),


    //权限异常
    API_CODE_AUTH_ERROR("301001", "apicode权限异常"),

    //内部逻辑异常--程序内部校验
    DATA_NOT_EXIST_ERROR("302001", "数据不存在"),
    //调用失败
    REQUEST_FAIL_ERROR("302002", "调用内部服务失败"),

    //统一的未知异常
    UNKNOWN_ERROR("999998", "未知异常"),

    //重入异常
    REPEAT_ERROR("999999", "重入");



    private String errorCode;
    private String errorMsg;

    MarketingErrorInfo(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
