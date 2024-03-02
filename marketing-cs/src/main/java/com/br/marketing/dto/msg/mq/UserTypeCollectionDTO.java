package com.br.marketing.dto.msg.mq;

import java.io.Serializable;

/**
 * 场景收集
 *
 * @author Guo Zeqiang
 * @dateTime 2024-02-28 11:40
 */
public class UserTypeCollectionDTO implements Serializable {

    private static final long serialVersionUID = 5850349796245796303L;

    /**
     * 2024-02-28 11:24
     * 场景
     */
    private String userType;

    /**
     * 2024-02-28 13:42
     * 批次号
     */
    private String taskId;

    /**
     * 2024-02-28 13:42
     * 数据状态
     */
    private Integer status;


    public UserTypeCollectionDTO() {
    }

    public UserTypeCollectionDTO(String userType, String taskId, Integer status) {
        this.userType = userType;
        this.taskId = taskId;
        this.status = status;
    }

    public UserTypeCollectionDTO(String userType) {
        this.userType = userType;
    }


    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "UserTypeCollectionDTO{" +
                "taskId='" + taskId + '\'' +
                ", status=" + status +
                ", userType='" + userType + '\'' +
                '}';
    }
}
