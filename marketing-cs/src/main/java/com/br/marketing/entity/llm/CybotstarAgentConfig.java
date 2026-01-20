package com.br.marketing.entity.llm;

import java.util.Date;

public class CybotstarAgentConfig {
    /**
     * 
     */
    private Long id;

    /**
     * 智能体编码（对应枚举）
     */
    private String agentCode;

    /**
     * 智能体名称
     */
    private String agentName;

    /**
     * cybertron-robot-key
     */
    private String robotKey;

    /**
     * cybertron-robot-token
     */
    private String robotToken;

    /**
     * 用户名
     */
    private String username;

    /**
     * 删除标志；1-正常；9-删除；
     */
    private Integer isDel;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAgentCode() {
        return agentCode;
    }

    public void setAgentCode(String agentCode) {
        this.agentCode = agentCode == null ? null : agentCode.trim();
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName == null ? null : agentName.trim();
    }

    public String getRobotKey() {
        return robotKey;
    }

    public void setRobotKey(String robotKey) {
        this.robotKey = robotKey == null ? null : robotKey.trim();
    }

    public String getRobotToken() {
        return robotToken;
    }

    public void setRobotToken(String robotToken) {
        this.robotToken = robotToken == null ? null : robotToken.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public Integer getIsDel() {
        return isDel;
    }

    public void setIsDel(Integer isDel) {
        this.isDel = isDel;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}