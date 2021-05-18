package com.br.marketing.api.bean;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.br.marketing.common.constants.strategy.StrategyTaskStatus;
import com.br.marketing.common.constants.web.ResponseCode;
import lombok.Data;

import java.util.Date;
import java.util.List;

/** 策略实例的实体对象，该对象主要用于与mysql数据库进行交互
 *
 * 该对象同时关联三张数据库表，分别如下
 * stra_his_batch 策略历史批量单条对应表
 * stra_his_list 策略历史列表
 * stra_his_detail 策略历史详情表
 * @author Wang Weiwei
 * @since 2018/3/20
 */
@Data
public class StrategyHistoryEntity {
    private Integer id;

    private Integer ownerId;

    private List<Integer> createUser;

    private String swiftNumber;

    /**
     * 批量任务编号
     * */
    private String batchSwift;
    /**
     * 单条任务编号
     * */
    private String singleSwift;

    /**商户编号*/
    private String apiCode;

    /**策略编号*/
    private String strategyId;

    /**策略版本*/
    private String version;

    /**返回码*/
    private String code = ResponseCode.TASK_RUN.getCode();

    /**策略任务状态
     * @see StrategyTaskStatus
     * */
    private String status = StrategyTaskStatus.COMPUTING.getCode();

    /**请求创建时间*/
    private Date createTime;
    /**请求完成时间*/
    private Date doneTime;
    /**数据删除时间*/
    private Date deleteTime = DateUtil.offset(new Date(), DateField.YEAR,5);
          //  DateUtil.offsetMonth(new Date(), 2);

    /**请求类型
     * */
    private String requestType;

    /**身份证号*/
    private String idCard;

    /**姓名*/
    private String name;

    /**手机号*/
    private String cell;

    /**客户编号*/
    private String cusNum;

    /**策略业务建议*/
    private String advice;

    /**规则风险风级*/
    private String ruleRisk;

    /**贷前重审风险风级*/
    private String retryRisk;

    /**行为评分风险风级*/
    private String behaviorRisk;

    /**最终风险分级*/
    private String riskLevel;

    /**请求参数*/
    private String requestParam;

    /**响应参数*/
    private String responseJson;

    /**画像返回结果*/
    private String hxResponseJson;

    /**三相之力返回结果**/
    private String sxResponseJson;

    /**规则引擎返回结果*/
    private String ruleEngineJson;

    /**贷前策略重审返回结果*/
    private String strategyRetryJson;

    /**行为评分返回结果*/
    private String behaviorScoreJson;
    /**审批通过日*/
    private Date passDate;
    /**贷款到期日*/
    private Date loanMaturityDate;
    /**贷前审批结果*/
    private String approveResult;

    private Integer deleted;

    /**查询开始日期*/
    private String queryBeginDate;
    /**查询结束日期*/
    private String queryEndDate;
    /**审批通过开始日期*/
    private String passBeginDate;
    /**审批通过结束日期*/
    private String passEndDate;

    private String searchStatus;

    private Integer start;

    private Integer limit;

    private Integer startCusNum;

    private Integer endCusNum;

}
