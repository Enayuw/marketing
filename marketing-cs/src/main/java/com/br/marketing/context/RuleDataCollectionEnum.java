package com.br.marketing.context;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description :
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/2 14:11
 */
public enum RuleDataCollectionEnum {
    HAI_ER_RULE_DATA_COLLECTION(1, "海尔规则所需数据收集"),
    SHU_HE_RULE_DATA_COLLECTION(2, "数禾规则所需数据收集"),
    XIAO_YING_RULE_DATA_COLLECTION(3, "小赢规则所需数据收集"),
    DEFAULT_DATA_COLLECTION(-1, "通用规则收集"),
    YI_XIN_DATA_COLLECTION(4, "宜信推电销所需数据收集"),
    HALUO_DASS_COLLECTION(5, "哈罗推电销数据收集"),
    PPD_DATA_COLLECTION(6, "拍拍贷推电销数据收集"),
    TONG_CHENG_DATA_COLLECTION(7, "同程金融规则所需数据收集"),
    RS_DATA_COLLECTION(8, "榕树规则所需数据收集"),
    ORANGE_DATA_COLLECTION(9, "桔子规则所需数据收集"),

    ELEPHANT_DATA_COLLECTION(10, "小象规则所需数据收集"),

    TONG_CHENG_DATA_COLLECTION_V2(11, "同程金融规则所需数据收集第二版"),

    NIWODAI_DATA_COLLECTION(20, "你我贷规则所需数据收集"),

    GOME_DATA_COLLECTION(21, "国美规则所需数据收集"),

    ZHONGYUAN_DATA_COLLECTION(22, "中原规则所需数据收集"),

    ZHONGBANG_DATA_COLLECTION(23, "众邦规则所需数据收集"),

    ZHONGYOU_DATA_COLLECTION(24, "中邮推送客服规则所需数据收集"),
    XIECHENG_DATA_COLLECTION(25, "携程接口自动化过滤规则所需数据收集"),

    ;


    RuleDataCollectionEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    private final Integer code;
    private final String name;

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }
}
