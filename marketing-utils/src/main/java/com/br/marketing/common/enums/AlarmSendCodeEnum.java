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
    // 接口字段新增检查,立即推送，一般
    EXCEPTION_NEW_FIELD_CHECK("62011", "接口字段新增检查"),
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

    //TC 同城易融代项目
    EXCEPTION_TC("63001","同城易融业务报错code"),

    //数据治理平台调用marketing-inner-api邮件发送接口使用
    DATA_GOVERNANCE_PLATFORM_SEND_EMAIL("70000", "数据治理平台邮件发送"),

    INITDATA_MUST_ERROR("70001","代运营明细数据缺少必填参数"),

    TRANSFER_MUST_ERROR("70002","转化明细数据缺少必填参数"),

    //推送Daas异常,阶梯推送,
    PUSHING_DAASERROR("71000", "推送Daas异常"),
    //推送客服异常,阶梯推送
    PUSHING_CUSTOMERERROR("72000", "推送客服异常"),
    //推送决策异常,阶梯推送
    PUSHING_DECISIONERROR("73000", "推送决策异常"),
    //推送Api异常,阶梯推送
    PUSHING_APIERROR("74000", "推送Api异常"),
    //调用有效期方法异常,立即推送
    VALIDITY_INTERFACEERROR("75000", "调用有效期方法异常"),
    //RocketMQ消费异常
    ROCKETMQ_CONSUMER_ERROR("76000", "RocketMQ消费异常"),

    DB_ERROR("80001", "数据库异常"),

    MARKETING_ERROR("90000", "服务开关未关闭"),

    FILE_DOWNLOAD_SYNC_ERROR("77000", "文件下载同步异常"),


    //中台业务未知错误,立即推送
    YINGXIAO_SERVICEERROR("6000000", "中台业务未知错误"),

    //58业务错误,立即推送
    EXCEPTION_WBXK_ALARM("6001001", "58业务告警码"),
    //58接口错误,阶梯推送
    WBXK_INTERFACEERROR("6001002", "58接口调用失败"),



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
    //360业务错误,立即推送
    QIFUAI_SERVICEERROR("6004003", "360ai业务告警码"),
    //360接口错误,阶梯推送
    QIFUAI_INTERFACEERROR("6004004", "360ai接口调用失败"),
    //360促动支业务错误,立即推送
    QIFUCUDONGZHI_SERVICEERROR("6004005", "360促动支业务告警码"),
    //360催收定制上传数据错误,立即推送
    QIFUCUDONGZHIREPORT_SERVICEERROR("6004006", "360促动支报表业务告警码"),
    //360促动支报表业务错误,立即推送
    SANLIULINGCOLLECTION_SERVICEERROR("6004007", "360催收定制上传数据告警码"),
    //携程业务错误,立即推送
    XIECHENG_SERVICEERROR("6005001", "携程业务告警码"),
    //携程接口错误,阶梯推送
    XIECHENG_INTERFACEERROR("6005002", "携程接口调用失败"),

    //众安业务异常,立即推送
    ZHONGAN_SERVICEERROR("6006001", "众安业务异常"),
    //众安接口调用失败,阶梯推送
    ZHONGAN_INTERFACEERROR("6006002", "众安接口调用失败"),
    //众安上报业务发生错误,立即推送
    ZHONGAN_REPORTEERROR("6006003", "众安上报业务发生错误"),

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

    //哈啰接口调用失败,阶梯推送
    HALUO_CALLBACK_DATA_INTERFACEERROR("6012003", "哈啰3710217营销回传失败"),

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
    //同程推送文件到SFTP失败
    TONGCHENG_PUSHFILETOSFTP("6017003", "同程文件推送SFTP失败"),


    //你我贷业务异常,立即推送
    NIWODAI_SERVICEERROR("6018001", "你我贷业务异常"),
    //你我贷接口调用失败,阶梯推送
    NIWODAI_INTERFACEERROR("6018002", "你我贷接口调用失败"),

    //榕树业务异常,立即推送
    RONGSHU_SERVICEERROR("6019001", "榕树业务异常"),
    //榕树接口调用失败,阶梯推送
    RONGSHU_INTERFACEERROR("6019002", "榕树接口调用失败"),
    //榕树业务处理阶梯告警
    RONGSHU_PROCESS_WARNING("6019003", "榕树业务处理问题阶梯告警"),

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
    //国美黑名单自动化过滤未查询到手机号,阶梯推送
    GUOMEI_PHONENOTFUND("6026003", "国美黑名单过滤未查询到手机号"),

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

    //BI报表业务异常,立即推送
    BIREPORT_SERVICEERROR("6037001", "BI报表业务异常"),

    //微聚业务异常,立即推送
    WEIJU_SERVICEERROR("6038001", "微聚业务异常"),

    //顺丰业务异常,立即推送
    SHUNFENG_SERVICEERROR("6039001", "顺丰业务异常"),

    ES_RETRY_DATAERROR("6040001", "ES补推数据异常"),

    HX_CAR_CLUE_INTERFACE("6041001", "海星车线索接口异常"),

    //萨摩耶定制业务异常,立即推送
    SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR("6041001", "萨摩耶定制上传业务异常"),
    SAMOYE_CUSTOMIZE_TRANSFER_SERVICEERROR("6041002", "萨摩耶定制转化业务异常"),
    //萨摩耶黑名单自动化过滤未查询到手机号,阶梯推送
    SAMOYE_TRANSFER_PHONE_NOT_FIND("6041003", "萨摩耶黑名单自动化过滤未查询到手机号"),
    PUSH_TO_SFTP("6042003", "跑分文件推送SFTP异常"),
    //中台标签业务告警码
    TAG_SERVICEERROR("6043001", "中台标签业务处理异常"),
    //中台标签业务告警码
    DATACLEANING_SERVICEERROR("6044001", "规则数据清洗业务处理异常"),
    //获取近一个月有数据的日期接口调用失败
    LASTMONTHDATDDATES_SERVICEERROR("6044002","获取近一个月有数据的日期接口调用失败"),
    DATACLEANING_TRIALPROCESSERROR("6044003","规则试跑业务处理异常"),
    //萨摩耶黑名单传输业务异常,立即推送
    SMY_SERVICEERROR("6050001", "萨摩耶黑名单传输业务异常"),
    //萨摩耶接口调用失败,阶梯推送
    SMY_INTERFACEERROR("6050002", "萨摩耶接口调用失败"),

    //恒昌业务异常,立即推送
    HENGCHANG_SERVICEERROR("6051001", "恒昌业务异常"),

    //车线索业务异常,立即推送
    CARCLUE_SERVICEERROR("6052001", "车线索业务异常"),

    //中台清洗规则执行错误,立即推送
    DATACLEA_SERVICEERROR("6053001", "中台清洗规则执行错误"),

    //pp停车业务告警码
    PP_MARKING_SERVICEERROR("6060001", "pp停车业务异常"),

    //泰康业务告警码
    TAIKANG_MARKING_SERVICEERROR("6061001", "泰康业务异常"),

    //泰康-钉钉回传业务告警码
    TAIKANG_DINGDING_SERVICEERROR("6061001", "泰康线索钉钉数据回传异常"),

    //榕树新客接口调用失败
    RSXK_INTERFACE("6071001", "榕树新客接口异常"),
    //榕树新客业务异常
    RSXK_SERVICEERROR("6071002", "榕树新客业务异常"),
    //BI业务异常
    BI_SERVICEERROR("6072001", "BI业务异常"),
    //云客业务异常
    YUNKE_SERVICEERROR("6080001", "云客业务异常"),

    MOCK_SERVICEERROR("6091000", "Mock系统异常"),

    UMENG_SERVICEERROR("6090001", "友盟智能时机业务异常"),

    SANLIULING_SERVICEERROR("6090002", "360-pp流量业务异常"),

    DIDI_V5_SERVICEERROR("6091001", "滴滴V5业务异常"),

    IBMP_LINE_SERVICEERROR("7000001", "IBMP-获取线路信息异常"),

    TONGCHENG_CPA_SERVICEERROR("7001001", "同程CPA业务异常"),

    TRACKING_POINT_SERVICEERROR("7000002", "埋点异常"),

    SYNC_REPORT_EXPORT_SERVICEERROR("8001001", "上传记录导出异常"),

    //查询mock-Api异常,立即推送
    MOCK_APIERROR("8001002", "查询mock-Api异常"),

    POLLING_GROUP_EXCEPTION("8001003", "轮询开发组异常"),

    MARKETING_AVIATORSCRIPT_LINESMS_ERROR("8001004", "短信/线路钉钉文档配置入库异常"),

    MARKETING_AVIATORSCRIPT_LINE_CHANGE_ERROR("8001005", "线路三方字段同步异常"),
    //随意记
    SUIYIJI_SERVICE_ERROR("8001008", "随意记调用异常"),

    MARKETING_AVIATORSCRIPT_SMS_CHANGE_ERROR("8001007", "短信三方字段同步异常"),


    ZHONGYUAN_XIAOJIN_SERVICEERROR("8001006", "中原消金业务异常"),

    CALLBACK_LARGE_MODEL("7100001", "大模型回调业务异常"),

    XYF_SERVICEERROR("7011001", "信用飞业务异常"),

    WUBA_AI_SERVICEERROR("7012001", "58AI接口异常"),

    NINGBO_BANK_SERVICEERROR("7013001", "宁波银行接口异常"),

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
