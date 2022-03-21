package com.br.marketing.origin;

import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;

import java.util.Date;

/**
 * 数禾上下文
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/18 10:39
 */
public class ShuHeProcessHandlerContext extends AbstractProcessHandlerContext {
    /**
     * 场景策略
     */
    private IUserType iUserType;
    /**
     * 上传数据创建时间
     */
    private Date creatTime;

    /**
     * 数禾原始数据-结构
     */
    private CaseShuheUser caseShuheUser;

    /**
     * 批次号
     */
    private String taskId;

    /**
     * 是否继续判断规则
     * true 继续
     */
    private boolean continueJudgeRule;


    public ShuHeProcessHandlerContext(ProcessHandlerContext context) {
        super(context.getApiCode(), context.getTransferInfoId(), context.getCustomerMap(), context.getMqFact());
    }

    public IUserType getiUserType() {
        return iUserType;
    }

    public void setiUserType(IUserType iUserType) {
        this.iUserType = iUserType;
    }

    public Date getCreatTime() {
        return creatTime;
    }

    public void setCreatTime(Date creatTime) {
        this.creatTime = creatTime;
    }

    public CaseShuheUser getCaseShuheUser() {
        return caseShuheUser;
    }

    public void setCaseShuheUser(CaseShuheUser caseShuheUser) {
        this.caseShuheUser = caseShuheUser;
    }

    public boolean isContinueJudgeRule() {
        return continueJudgeRule;
    }

    public void setContinueJudgeRule(boolean continueJudgeRule) {
        this.continueJudgeRule = continueJudgeRule;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    @Override
    public String toString() {
        return "ShuHeProcessHandlerContext{" +
                "iUserType=" + iUserType +
                ", creatTime=" + creatTime +
                ", caseShuheUser=" + caseShuheUser +
                ", taskId='" + taskId + '\'' +
                ", continueJudgeRule=" + continueJudgeRule +
                '}';
    }
}
