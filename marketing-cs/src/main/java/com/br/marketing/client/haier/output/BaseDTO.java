package com.br.marketing.client.haier.output;


/**
 * 请求基础属性
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/3 15:49
 */
public abstract class BaseDTO {
    //对接方标识
    protected String apiCode;
    // 推送数据
    protected String formData;
    //校验值
    protected String checkData;

    public BaseDTO() {
    }

    public BaseDTO(String apiCode) {
        this.apiCode = apiCode;
    }

    public BaseDTO(String apiCode, String formData, String checkData) {
        this.apiCode = apiCode;
        this.formData = formData;
        this.checkData = checkData;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public String getCheckData() {
        return checkData;
    }

    public void setCheckData(String checkData) {
        this.checkData = checkData;
    }


}
