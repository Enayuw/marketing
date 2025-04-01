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

    PPD_LOD_DATA_COLLECTION(12, "拍拍贷老客转化数据收集"),

    NIWODAI_DATA_COLLECTION(20, "你我贷规则所需数据收集"),

    GOME_DATA_COLLECTION(21, "国美规则所需数据收集"),

    ZHONGYUAN_DATA_COLLECTION(22, "中原规则所需数据收集"),

    ZHONGBANG_DATA_COLLECTION(23, "众邦规则所需数据收集"),

    ZHONGYOU_DATA_COLLECTION(24, "中邮推送客服规则所需数据收集"),

    WEIEDAI_DATA_COLLECTION(26, "微e贷规则所需数据收集"),


    QIFU360_DATA_COLLECTION(27, "360金融规则所需数据收集"),
    ZHONGAN_TRANSFER_FILTER_COLLECTION(28, "众安转化数据过滤规则所需数据收集"),
    YILIAN_TRANSFER_FILTER_COLLECTION(29, "亿联转化数据过滤规则所需数据收集"),
    SHUHE_CUSHOUDENG_RULE_DATA_COLLECTION(30, "数禾促首登规则所需数据收集"),
    YISHI_TRANSFER_FILTER_COLLECTION(31, "医时转化规则所需数据收集"),
    WUBA_TRANSFER_FILTER_COLLECTION(32, "58新客转化规则所需数据收集"),
    SAMOYE_TRANSFER_FILTER_COLLECTION(33, "萨摩耶转化规则所需数据收集"),
    RSXK_DATA_COLLECTION(34, "榕树新客规则所需数据收集"),
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
