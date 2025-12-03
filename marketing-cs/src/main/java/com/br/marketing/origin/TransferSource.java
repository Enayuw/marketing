package com.br.marketing.origin;



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
 * @Description : 转化数据来源 枚举
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/12 15:50
 */
public enum TransferSource {

    UNIVERSAL_TRANSFER_PROCESS(1, "通用转化流程"),
    CUSTOMER_CALL_RECORD(2, "客服拨打数据"),
    ORIGIN_DATA_UPLOAD_PROCESS(3, "原始数据上传流程"),
    ARTIFICIAL_DIAL_PROCESS(4, "人工拨打流程"),
    TRANSFER_DATA_SET_PROCESS(5, "转化数据集合流程"),
    INIT_DATA_SET_PROCESS(6, "初始数据集合流程"),
    CUSTOMER_SMS_CALLBACK(7, "客服短信数据"),
    CUSTOMER_SMS_CALLBACK_AT_ONCE(8, "短信发送即回调数据"),
    ;


    TransferSource(Integer code, String name) {
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
