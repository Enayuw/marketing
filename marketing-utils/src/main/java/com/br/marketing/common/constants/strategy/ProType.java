package com.br.marketing.common.constants.strategy;

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
 *
 * @Description : 产品分类
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2018/5/5 16:37
 */
public enum  ProType {
    //规则集
    RULE_TYPE("rulew","规则集"),
    //评分
    BEHAVIOR_SCORE("scoreml","评分"),
    //贷前重审
    STRATEGY_RETRY("reviewStr","贷前重审"),
    //基础数据
    BASE_DATA("baseData","基础数据"),
    //号码状态核查
    PHONE_CHECK("phoneCheck","号码状态核查");
    private final String code;
    private final String message;

    ProType(String code, String message){
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
