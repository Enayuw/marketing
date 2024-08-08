package com.br.marketing.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 营销中台报警发送码 枚举类
 *
 * @author juanjuan.song@brgroup.com
 * @dateTime 2022/10/28 17:30
 */
@Getter
@AllArgsConstructor
public enum AlarmSendCodeEnum {

    //主动发送——成功通知(开发,测试,产品,运营),立即推送
    SUCCESS_UPLOAD("50000", "uploadSuccess"),
    //主动发送——成功通知(开发,测试),立即推送
    SUCCESS_INTERNAL("51000", "InternalSuccessNotice"),
    //未知错误,立即推送
    ERROR_UNKNOWN("60000", "sysError"),
    //主动发送——业务异常,立即推送
    EXCEPTION_URGENT("61000", "dataExceptionUrgent"),
    //主动发送——业务异常,阶梯推送
    EXCEPTION_COMMON("62000", "dataExceptionCommonly"),
    //萨摩耶转化数据报警，手机号缺失,阶梯推送
    EXCEPTION_SAMOYE("62001", "samoyeCommonly"),
    //画像返回98,阶梯推送
    EXCEPTION_HUAX("62002", "huaxiangCommonly"),
    //滴滴联合建模,阶梯推送
    EXCEPTION_DIDI("62003", "didiCommonly"),
    //滴滴联合建模,阶梯推送
    EXCEPTION_SPEEDCOMMONCONFIG("62004", "marketingCommonConfigAlarm"),
    // 有效期配置异常,阶梯推送，一般
    EXCEPTION_VALIDITY_PERIOD("62005", "有效期规则提示"),
    //pulsar消费requestId冲突
    REQUESTID_CONFLICT("62006", "requestIdConflict"),
    // 接口字段新增检查,阶梯推送，一般
    EXCEPTION_NEW_FIELD_CHECK("62006", "接口字段新增检查"),
    // 一般通知,阶梯推送，一般
    EXCEPTION_USUAL_NOTICE("62007", "通知"),
    //众安通话明细回调
    EXCEPTION_ZHONGAN_CALL_RECORD("62008", "众安通话明细回调"),
    //携程业务报错，立即推送
    XIECHENG_RECORD("62009", "携程业务报错"),
    //业务未知错误,立即推送，63000
    SERVICEERROR_UNKNOWN("63000", "业务实现未知错误"),
    //三方接口错误,告警周期和告警次数，64000
    INTERFACE_ERROR("64000", "三方接口错误"),


    //宜信非实时推客服告警,立即推送,
    EXCEPTION_YIXIN_PUSH_CUSTOMER("62010", "宜信非实时推客服"),
    EXCEPTION_WUBA("62058", "58业务报错code"),
    //360业务错误,立即推送
    EXCEPTION_QIFU_ALARM("62360", "360业务告警码"),


    //数据治理平台调用marketing-inner-api邮件发送接口使用
    DATA_GOVERNANCE_PLATFORM_SEND_EMAIL("70000", "数据治理平台邮件发送"),
    //推送Daas异常,阶梯推送,
    PUSHING_DAASERROR("71000", "推送Daas异常"),
    //推送客服异常,阶梯推送
    PUSHING_CUSTOMERERROR("72000", "推送客服异常"),
    //推送决策异常,阶梯推送
    PUSHING_DECISIONERROR("72000", "推送决策异常"),
    //推送Api异常,阶梯推送
    PUSHING_APIERROR("74000", "推送Api异常"),
    //调用有效期方法异常,立即推送
    VALIDITY_INTERFACEERROR("75000", "调用有效期方法异常"),


    //中台业务未知错误,立即推送
    YINGXIAO_SERVICEERROR("6000000", "中台业务未知错误"),

    //58业务错误,立即推送
    EXCEPTION_WBXK_ALARM("6001001", "58业务告警码"),
    //58接口错误,阶梯推送
    WBXK_INTERFACEERROR("6001002", "58接口调用失败"),


    INITDATA_MUST_ERROR("70001","代运营明细数据缺少必填参数"),

    TRANSFER_MUST_ERROR("70002","转化明细数据缺少必填参数"),

    //之家业务错误,立即推送
    ZHIJIA_SERVICEERROR("6002001", "之家车线索业务异常"),
    //之家接口错误,阶梯推送
    ZHIJIA_INTERFACEERROR("6002002", "之家接口调用失败"),

    //医时业务错误,立即推送
    YISHI_SERVICEERROR("6003001", "医时业务异常"),
    //医时接口错误,阶梯推送
    YISHI_INTERFACEERROR("6003002", "医时接口调用失败"),

    //360业务错误,立即推送
    QIFU_SERVICEERROR("6004001", "360业务告警码"),
    //360接口错误,阶梯推送
    QIFU_INTERFACEERROR("6004002", "360接口调用失败"),

    //携程业务错误,立即推送
    XIECHENG_SERVICEERROR("6005001", "携程业务告警码"),
    //携程接口错误,阶梯推送
    XIECHENG_INTERFACEERROR("6005002", "携程接口调用失败"),

    //众安业务异常,立即推送
    ZHONGAN_SERVICEERROR("6006001", "众安业务异常"),
    //众安接口调用失败,阶梯推送
    ZHONGAN_INTERFACEERROR("6006002", "众安接口调用失败"),

    //数禾业务异常,立即推送
    SHUHE_SERVICEERROR("6007001", "数禾业务异常"),
    //数禾接口调用失败,阶梯推送
    SHUHE_INTERFACEERROR("6007002", "数禾接口调用失败"),

    //宜信业务异常,立即推送
    YIXIN_SERVICEERROR("6008001", "宜信业务异常"),
    //宜信接口调用失败,阶梯推送
    YIXIN_INTERFACEERROR("6008002", "宜信接口调用失败"),

    //洋钱罐业务异常,立即推送
    YANGQIANGUAN_SERVICEERROR("6009001", "洋钱罐业务异常"),
    //洋钱罐接口调用失败,阶梯推送
    YANGQIANGUAN_INTERFACEERROR("6009002", "洋钱罐接口调用失败"),

    //拍拍贷业务异常,立即推送
    PAIPAIDAI_SERVICEERROR("6010001", "拍拍贷业务异常"),
    //拍拍贷接口调用失败,阶梯推送
    PAIPAIDAI_INTERFACEERROR("6010002", "拍拍贷接口调用失败"),

    //海尔业务异常,立即推送
    HAIER_SERVICEERROR("6011001", "海尔业务异常"),
    //海尔接口调用失败,阶梯推送
    HAIER_INTERFACEERROR("6011002", "海尔接口调用失败"),

    //哈啰业务异常,立即推送
    HALUO_SERVICEERROR("6012001", "哈啰业务异常"),
    //哈啰接口调用失败,阶梯推送
    HALUO_INTERFACEERROR("6012002", "哈啰接口调用失败"),

    //桔子业务异常,立即推送
    JUZI_SERVICEERROR("6013001", "桔子业务异常"),
    //桔子接口调用失败,阶梯推送
    JUZI_INTERFACEERROR("6013002", "桔子接口调用失败"),

    //玖富业务异常,立即推送
    JIUFU_SERVICEERROR("6014001", "玖富业务异常"),
    //玖富接口调用失败,阶梯推送
    JIUFU_INTERFACEERROR("6014002", "玖富接口调用失败"),

    //小微业务异常,立即推送
    XAIOWEI_SERVICEERROR("6015001", "小微业务异常"),
    //小微接口调用失败,阶梯推送
    XAIOWEI_INTERFACEERROR("6015002", "小微接口调用失败"),

    //小赢业务异常,立即推送
    XIAOYING_SERVICEERROR("6016001", "小赢业务异常"),
    //小赢接口调用失败,阶梯推送
    XIAOYING_INTERFACEERROR("6016002", "小赢接口调用失败"),

    //同程业务异常,立即推送
    TONGCHENG_SERVICEERROR("6017001", "同程业务异常"),
    //同程接口调用失败,阶梯推送
    TONGCHENG_INTERFACEERROR("6017002", "同程接口调用失败"),

    //你我贷业务异常,立即推送
    NIWODAI_SERVICEERROR("6018001", "你我贷业务异常"),
    //你我贷接口调用失败,阶梯推送
    NIWODAI_INTERFACEERROR("6018002", "你我贷接口调用失败"),

    //榕树业务异常,立即推送
    RONGSHU_SERVICEERROR("6019001", "榕树业务异常"),
    //榕树接口调用失败,阶梯推送
    RONGSHU_INTERFACEERROR("6019002", "榕树接口调用失败"),

    //亿联业务异常,立即推送
    YILIAN_SERVICEERROR("6020001", "亿联业务异常"),
    //亿联接口调用失败,阶梯推送
    YILIAN_INTERFACEERROR("6020002", "亿联接口调用失败"),

    //中邮业务异常,立即推送
    ZHONGYOU_SERVICEERROR("6021001", "中邮业务异常"),
    //中邮接口调用失败,阶梯推送
    ZHONGYOU_INTERFACEERROR("6021002", "中邮接口调用失败"),

    //永辉业务异常,立即推送
    YONGHUI_SERVICEERROR("6022001", "永辉业务异常"),
    //永辉接口调用失败,阶梯推送
    YONGHUI_INTERFACEERROR("6022002", "永辉接口调用失败"),

    //宜人贷业务异常,立即推送
    YIRENDAI_SERVICEERROR("6023001", "宜人贷业务异常"),
    //宜人贷接口调用失败,阶梯推送
    YIRENDAI_INTERFACEERROR("6023002", "宜人贷接口调用失败"),

    //微众业务异常,立即推送
    WEIZHONG_SERVICEERROR("6025001", "微众业务异常"),
    //微众接口调用失败,阶梯推送
    WEIZHONG_INTERFACEERROR("6025002", "微众接口调用失败"),

    //国美业务异常,立即推送
    GUOMEI_SERVICEERROR("6026001", "国美业务异常"),
    //国美接口调用失败,阶梯推送
    GUOMEI_INTERFACEERROR("6026002", "国美接口调用失败"),

    //中原业务异常,立即推送
    ZHONGYUAN_SERVICEERROR("6027001", "中原业务异常"),
    //中原接口调用失败,阶梯推送
    ZHONGYUAN_INTERFACEERROR("6027002", "中原接口调用失败"),

    //滴滴业务异常,立即推送
    DIDI_SERVICEERROR("6028001", "滴滴业务异常"),
    //滴滴接口调用失败,阶梯推送
    DIDI_INTERFACEERROR("6028002", "滴滴接口调用失败"),

    //保险业务异常,立即推送
    BAOXIAN_SERVICEERROR("6029001", "保险业务异常"),
    //保险接口调用失败,阶梯推送
    BAOXIAN_INTERFACEERROR("6029002", "保险接口调用失败"),

    //时光业务异常,立即推送
    SHIGUANG_SERVICEERROR("6030001", "时光业务异常"),
    //时光接口调用失败,阶梯推送
    SHIGUANG_INTERFACEERROR("6030002", "时光接口调用失败"),

    //金美信业务异常,立即推送
    JINMEIXIN_SERVICEERROR("6031001", "金美信业务异常"),
    //金美信接口调用失败,阶梯推送
    JINMEIXIN_INTERFACEERROR("6031002", "金美信接口调用失败"),

    //苏宁业务异常,立即推送
    SUNING_SERVICEERROR("6032001", "苏宁业务异常"),
    //苏宁接口调用失败,阶梯推送
    SUNING_INTERFACEERROR("6032002", "苏宁接口调用失败"),

    //招联业务异常,立即推送
    ZHAOLIAN_SERVICEERROR("6033001", "招联业务异常"),
    //招联接口调用失败,阶梯推送
    ZHAOLIAN_INTERFACEERROR("6033002", "招联接口调用失败"),

    //喜马拉雅业务异常,立即推送
    XIMALAYA_SERVICEERROR("6034001", "喜马拉雅业务异常"),
    //喜马拉雅接口调用失败,阶梯推送
    XIMALAYA_INTERFACEERROR("6034002", "喜马拉雅接口调用失败"),

    //小贷业务异常,立即推送
    XIAODAI_SERVICEERROR("6035001", "小贷业务异常"),
    //小贷接口调用失败,阶梯推送
    XIAODAI_INTERFACEERROR("6035002", "小贷接口调用失败"),

    //度小满业务异常,立即推送
    DUXIAOMAN_SERVICEERROR("6036001", "度小满业务异常"),
    //度小满接口调用失败,阶梯推送
    DUXIAOMAN_INTERFACEERROR("6036002", "度小满接口调用失败"),

    ;

    /**
     * 2022/10/28 17:30 发送码
     */
    private final String code;

    /**
     * 2022/10/28 17:30 消息
     */
    private final String message;

}
