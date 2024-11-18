package com.br.marketing.client.guomei.base;

import java.io.Serializable;
import java.util.Collection;

/**
 * 国美回调请求参数
 *
 * @author Hua Qiang
 * @date 2024-08-20 15:17
 */
public abstract class AbstractGmCallBackRequestBase<T extends Collection<AbstractUserListBase>> implements Serializable {
    private static final long serialVersionUID = 132649769325247994L;

    /**
     * 2024-08-20 15:31
     * 流水号(每次请求唯一)
     * <p>
     * 必填
     */
    private String requestId;

    /**
     * 2024-08-20 15:31
     * 机构编码（固定值，对接时提供）
     * <p>
     * 必填
     */
    private String institutionCode;

    /**
     * 2024-08-20 15:31
     * 数据集合
     * <p>
     * 必填
     */
    private T userList;

    public AbstractGmCallBackRequestBase() {
    }

    public AbstractGmCallBackRequestBase(String requestId, String institutionCode, T userList) {
        this.requestId = requestId;
        this.institutionCode = institutionCode;
        this.userList = userList;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public T getUserList() {
        return userList;
    }

    public void setUserList(T userList) {
        this.userList = userList;
    }

    @Override
    public String toString() {
        return "AbstractGmCallBackRequestBase{" +
                "requestId='" + requestId + '\'' +
                ", institutionCode='" + institutionCode + '\'' +
                ", userList=" + userList +
                '}';
    }
}
