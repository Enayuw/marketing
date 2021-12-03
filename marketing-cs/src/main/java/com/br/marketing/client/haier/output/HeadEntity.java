package com.br.marketing.client.haier.output;

/**
 * 响应报文请求头
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/12/3 15:54
 */
public class HeadEntity {
    /**
     * 接口返回码
     * 接口1：
     * <p>
     * 返回码	     描述	                处理方式
     * <p>
     * 00000	     成功	                处理成功
     * MSG0411001	系统异常	                请联系对接人员处理
     * MSG70000001	apiCode非法	            请检查apiCode是否正确配置
     * MSG70000002	checkCode非法	        请检查生成checkCode是否有误
     * MSG70000003	apiCode未生效	        请联系对接人员处理
     * MSG70000004	formData无效	        formData为空
     * MSG70000005	formData无法解析	        修改formData或联系对接人员处理
     * MSG70000006	requestId为空	        requestId为空
     * MSG70000007	requestId全局不唯一	    推送全局唯一requestId
     * MSG70000008	type为空	            type为空
     * <p>
     * <p>
     * 接口2：
     * <p>
     * 返回码	     描述	                处理方式
     * <p>
     * 00000	     成功	                处理成功
     * MSG0411001	系统异常	                请联系对接人员处理
     * MSG70000001	apiCode非法	            请检查apiCode是否正确配置
     * MSG70000002	checkCode非法	        请检查生成checkCode是否有误
     * MSG70000003	apiCode未生效	        请联系对接人员处理
     * MSG70000004	formData无效	        formData为空
     * MSG70000005	formData无法解析	        修改formData或联系对接人员处理
     * MSG70000006	requestId为空	        requestId为空
     * MSG70000008	batchNo为空	            batchNo为空
     * MSG70000009	type为空	            type为空
     * MSG70000010	type非法	            type约定枚举类型
     * MSG70000011	dataItems非法	        dataItems为空
     * MSG70000012	dataItems数据量过多	    dataItems数据量大于500
     * MSG70000013	taskId非法	            taskId为空或不存在
     * MSG70000014	custNum非法	            custNum为空或不存在
     * MSG70000015	未知异常	                请联系对接人员处理
     */
    private String regFlag;
    /**
     * 响应描述
     */
    private String regMsg;
    /**
     * 提示信息
     */
    private String showMsg;

    public HeadEntity() {
    }

    public HeadEntity(String regFlag, String regMsg, String showMsg) {
        this.regFlag = regFlag;
        this.regMsg = regMsg;
        this.showMsg = showMsg;
    }

    public String getRegFlag() {
        return regFlag;
    }

    public void setRegFlag(String regFlag) {
        this.regFlag = regFlag;
    }

    public String getRegMsg() {
        return regMsg;
    }

    public void setRegMsg(String regMsg) {
        this.regMsg = regMsg;
    }

    public String getShowMsg() {
        return showMsg;
    }

    public void setShowMsg(String showMsg) {
        this.showMsg = showMsg;
    }

    @Override
    public String toString() {
        return "HeadEntity{" +
                "regFlag='" + regFlag + '\'' +
                ", regMsg='" + regMsg + '\'' +
                ", showMsg='" + showMsg + '\'' +
                '}';
    }
}
