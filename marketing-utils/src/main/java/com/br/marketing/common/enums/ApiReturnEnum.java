package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 智能决策-接口返回信息
 *
 * @author juanjuan.song
 * @dateTime 2022/1/19 16:00
 */
@Getter
@AllArgsConstructor
public enum ApiReturnEnum {

    // 00 访问成功
    SUCCESS("00", "成功"),
    ERROR_1("900001", "程序错误!"),
    ERROR_2("900002", "公司不存在!"),
    ERROR_3("900003", "公司被禁用!"),
    ERROR_4("900004", "任务不存在!"),
    ERROR_6("900006", "请求参数错误!"),
    ERROR_7("900007", "测试条数受限!"),
    ERROR_9("900009", "超出最大上传数量!"),
    ERROR_10("900010", "此任务状态不支持导入!"),
    ERROR_11("900011", "流程失效!"),
    ERROR_12("900012", "案件不存在!"),
    ERROR_13("900013", "数据正在导入!"),
    ERROR_15("900015", "数据导入全部失败!"),
    ERROR_16("900016", "案件导入失败（部分成功部分失败）!"),
    ERROR_18("900018", "案件状态不支持结束!"),
    ERROR_19("900019", "暂无可用流程!"),
    ERROR_20("900020", "系统支持中!"),
    ERROR_21("900021", "操作失败!"),
    ERROR_22("900022", "流程未授权!"),
    ERROR_23("900023", "公司下未配置短信模板!"),
    ERROR_24("900024", "获取流程变量失败!"),
    ERROR_25("900025", "拨打结果不存在!"),
    ERROR_26("900026", "批次不存在!"),
    ERROR_27("900027", "主叫号码未授权!"),
    ERROR_28("900028", "未配置资源方!"),
    ERROR_29("900029", "重呼状态不支持!"),
    ERROR_30("900030", "录音文件格式错误!"),
    ERROR_31("900031", "请求重复，accessNumber重复!"),
    ERROR_32("900032", "未开启客户标签重呼规则配置!"),
    ERROR_33("900033", "未指定不重乎客户标签范围!"),
    ;

    /**
     * 状态码
     */
    private String code;

    /**
     * 信息
     */
    private String message;

    public static String getByCode(String code){
        for (ApiReturnEnum each: ApiReturnEnum.values()) {
            if (code.equals(each.getCode())) {
                return each.getMessage();
            }
        }
        return "";
    }
}
