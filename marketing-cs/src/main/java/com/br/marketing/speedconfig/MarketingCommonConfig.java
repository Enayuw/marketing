package com.br.marketing.speedconfig;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.speed.client.common.annotations.SpeedFile;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@SpeedFile(filename = "marketingcommon.properties", topic = "marketing")
@Data
public class MarketingCommonConfig {

    /**
     * 推送客服
     */
    private List<String> apiCodeOfpushCustomer;

    /**
     * 推送哈啰
     */
    private List<String> apiCodeOfpushHaluoByTransfer;

    /**
     * 是否是宜信客服转化接口
     */
    private HashMap<String,Boolean> customerTransferIsYx;

    /**
     * 海尔apicode
     */
    private List<String> haierApiCode;

    /**
     * 玖富apicode
     */
    private List<String> jfApiCode;

    /**
     * 记录taskid时间的apicode
     */
    private List<String> apiCodeOfRecordTaskTime;

    /**
     * 去重线程数
     */
    private Integer soleNum;

    /**
     * 萨摩耶场景
     */
    private HashSet<String> groupTypeSaMoye;

    /**
     * 数禾apicode
     */
    private List<String> shuheApiCode;

    /**
     * 客户使用规则映射
     */
    private HashMap<String,String> customerRuleMapping;

    /**
     * 配置走通用流程apiCode
     */
    private List<String> universalProcessApiCode;

    /**
     * 众安推送黑名单定时任务执行时间
     */
    private String zhongAnPushBlackDataExecuteTime;

    /**
     * 众安推送黑名单定时任务开关
     * true 打开，false 关闭执行
     */
    private Boolean zhongAnPushBlackDataSwitch;
    /**
     * 众安推送黑名单数据有效期
     */
    private String zhongAnPushBlackDataPeriod;
    /**
     * 众安推送黑名单线程数设置{userType:threadNum}
     */
    private Map<String,String> zhongAnPushBlackThreadNum;

    /**
     * 数禾转化数据提取分场景, T 代表当前天到月底； T+/-day 代表当前天到day-1天，共day天
     */
    private Map<String, String> shuHeTransferExtractDayMap;

    /**
     * 数禾转化数据提取apiCode集合
     */
    private HashMap<String, List<String>> shuHeTransferExtractApiCodes;

    /**
     * 数禾转化数据提取任务开始时间
     */
    private String shuHeTransferExtractJobStartTime;

    /**
     * 数禾转化数据提取任务是否使用准全量转化数据
     */
    private Boolean shuHeTransferExtractIfUseQuasiTotalQuantity;

    /**
     * 数禾有效期, T 代表当前天到月底； T+/-day 代表当前天到day-1天，共day天
     */
    private Map<String, String> shuHePeriodOfValidityDayMap;

    /**
     * 消息队列过期时间
     */
    private String messageQueueExpireTime;
    /**
     * 宜信实时转化数据提取apiCode集合
     */
    private List<String> yinXinTransferRealTimeApiCodes;

    /**
     * 宜信实时转化数据执行时间
     */
    private String yinXinTransferRealTimeExecuteTime;

    /**
     * 宜信非实时转化数据执行时间
     */
    private String yinXinTransferNoRealTimeExecuteTime;

    /**
     * 宜信实时转化数据real-pass执行时间
     */
    private String yinXinTransferRealPassExecuteTime;

    /**
     * 是否开启宜信非实时数据提取
     */
    private Boolean isOpenYinXinTransferNoRealTimeExtract;

    /**
     * 宜信非实时typelist
     */
    private List<String> yixinNoRealTimeType;

    /**
     * 宜信非实时推客服typelist
     */
    private List<String> yixinNoRealTimePushRobotAIType;

    /**
     * 电销文件定制化处理
     */
    private HashMap<String, List<String>> dxFileCustomize;

    /**
     * 宜信apicode
     */
    private List<String> yiXinApiCode;

    /**
     * 壹钱包加密公钥
     */
    private String yiQianBaoPubKey;
    /**
     * ppd客服类型
     */
    private HashMap<String, List<String>> ppdCustomerType;

    /**
     * 促复借可用额度
     */
    private Double clcUsrAvlLmtLv0;

    /**
     * 哈啰回调数据落库配置线程数
     */
    private Integer haloSaveDataThreadNum;

    /**
     * 数禾推送电销情况;eg:{"促复借":["a","b","c"],"促首借":["a","b"]}
     */
    private HashMap<String, List<String>> shuHePushDXStatusMap;

    /**
     * # 数禾用户可用额度区间;大于:&gt;、小于:&lt;、等于:&eq;、不等于:&nq;、大于等于:&ge;、小于等于:&le;;eg:["&ge;","1","&le;","100"]
     */
    private List<String> shuHeUserAvailableQuotaRange;

    /**
     * 2022/5/17 15:05
     * 拨打记录数据配置apicode推送mq
     */
    private List<String> callRecordDataPushMqApiCodes;

    /**
     * 玖富转化数据提取apiCode集合
     */
    private List<String> JiuFuTransferApiCodes;

    /**
     * 玖富转化数据提取执行时间
     */
    private String JiuFuTransferExecuteTime;

    /**
     * 小赢转化数据推送智能客服apiCode集合
     */
    private List<String> xiaoYingTransferPushRobotApiCodes;

    /**
     * 小赢转化数据提取apiCode集合
     */
    private List<String> xiaoYingTransferExtractApiCodes;

    /**
     * 小赢登录断点转化数据提取任务开始时间
     */
    private String xiaoYingDengLuDuanDianTransferExtractJobTime;

    /**
     * 小赢促提转化数据提取任务开始时间
     */
    private String xiaoYingCuTiTransferExtractJobTime;

    /**
     * 小赢全量转化数据提取任务开始时间
     */
    private String xiaoYingFullLoadTransferExtractJobTime;

    /**
     * {"dassBlack":30,"customerBlack":30}
     */
    private HashMap<String,Integer> shuhePushBlackDay;

    /**
     *  哈罗转化数据规则
     *  {"status":"a,b,d","Ddate":"4","ABCdate":"5","taskIddate":"35","dTimes":"7"}
     */
    private HashMap<String,String> haluoTransferRule;

    /**
     * key:业务sql名称
     * value:0-shardingjdbc;1:tiflash;2:tikv
     */
    private HashMap<String,Integer> sqlType;

    /**
     * 跑分资源数量
     */
    private Integer taskResourceMaxNum;

    /**
     * 拍拍贷新客实时转化数据提取apicode集合
     */
    private List<String> PPDTransferFileApiCodes;

    /**
     * 拍拍贷新客实时转化数据提取执行时间
     */
    private String PPDTransferFileExecuteTime;

    /**
     * check服务job的上线开关
     */
    private Boolean checkJobOnlineSwitch;

    /**
     * 离线跑批入es文件 线程数
     */
    private Integer OffLineInserEsThreadNum;
    /**
     * 同程金融转化有效期, T 代表当前天到月底； T+/-day 代表当前天到day-1天，共day天
     */
    private String tongChengPeriodOfValidityDay;

    /**
     * #海尔有效期，T代表当天,T+day代表day天到当天
     */
    private String haierPeriodOfValidityDay;
    /**
     * 原始数据流转至规则的apicode集合
     */
    private List<String> initDataPushRule;

    /**
     * 原始上传数据推送决策 手机号加密类型{"7410437":1}
     * 1-md5;2-sha256;
     */
    private HashMap<String, Integer> pushCellEncPolicy;

    /**
     * 2022/9/22 17:52
     * 自由定义场景与上传日期
     * key userType；value dateSet
     */
    private Map<String, Set<String>> freeUserTypeAndDateMap;

    /**
     * 同程转化数据提取apicode集合
     */
    private List<String> tongChengTransferFileApiCodes;

    /**
     * 同程转化数据提取执行时间
     */
    private String tongChengTransferExecuteTime;

    /**
     * 携程数据推送线程数
     */
    private Integer xiechengDataSendThread;


    /**
     * 携程数据mq消费线程数
     */
    private Integer xiechengMqThread;

    /**
     * nfs路径
     */
    private String nfsPath;

    /**
     * 桔子转化数据入库特殊处理apicode集合
     */
    private List<String> juZiTransferInsertApiCodes;
    /**
     * 桔子实时转化定时任务执行时间
     */
    private String juZiRealTimeTransferExecuteTime;

    /**
     * 桔子实时转化锁定期配置
     */
    private Map<String, String> juZiRealTimeLockConfig;

    /**
     * 桔子周期性推送dass,查询实时推送数据日期;eg:{"a":[2],"b":[2],"c":[2,6,13,27],"d":[6]}
     */
    private Map<String, List<Integer>> orangeTransferCyclicalPushDassDay;
    /**
     * 哈啰转化数据提取apicode集合
     */
    private List<String> haLuoTransferFileApiCodes;
    /**
     * 萨摩耶转化数据提取apicode集合
     */
    private List<String> saMoYeTransferFileApiCodes;

    /**
     * 接口日志记录判断标识key为接口名称，第一个为db记录判断，第二为file记录判断{"zanPushDetail":[false,true],"zanZk":[false,true]}
     */
    private HashMap<String, List<Boolean>> apiLogMark;

    /**
     * 众安名单锁定推送数据线有效期, T 代表当前天到月底； T+/-day 代表当前天到day-1天，共day天
     */
    private Map<String, String> zhongAnPeriodOfValidityDay;

    /**
     * 众安名单锁定推送数据线程池配置,eg：[25,50],25为核心线程数，50为最大线程数
     */
    @Deprecated
    private List<Integer> zhongAnPushTreadPoolSize;

    /**
     * 众安名单锁定推送数据线程池配置,eg：{"CG":[50,100],"MG":[50,100],"other":[1,20]},50为业务线程数据，100为推送线程数
     */
    private HashMap<String, List<Integer>> zhongAnPushTreadPoolSizeMap;

    /**
     * 众安推送锁定名单推送时间
     */
    private String zhongAnRosterLockingTime;

    /**
     * 跑分分组分位值
     */
    private Integer quantileValue;

    /**
     * 推送决策优化跑分记录时间节点
     */
    private String scoreFileYhTime;

    /**
     *规则筛选从es获取的最大线程数
     */
    private Integer scoreByEsThreadNum;

    /**
     * 规则筛选调用决策接口的线程数
     */
    private Integer scoreToJcThreadNum;

    /**
     * 携程推送短信退订接口线程数设置
     */
    private String xieChengSmsQuitThreadNum;
    /**
     * 携程转化数据入库特殊处理apicode集合
     */
    private List<String> xieChengTransferInsertApiCodes;

    /**
     * 中邮转化数据提取apiCode集合
     */
    private List<String> ZhongYouTransferApiCodes;

    /**
     * 中邮转化数据提取执行时间
     */
    private String ZhongYouTransferExecuteTime;

    /**
     * 众安异业撞库数据提取apiCode集合
     */
    private List<String> ZhongAnTransferApiCodes;

    /**
     * 玖众安异业撞库数据提取执行时间
     */
    private String ZhongAnTransferExecuteTime;


    /**
     * 携程转化数据提取apiCode集合
     */
    private List<String> XieChengTransferApiCodes;

    /**
     * 携程新场景转化数据提取apiCode集合
     */
    private List<String> XieChengNewTransferApiCodes;

    /**
     * 携程转化数据提取执行时间,携程撞库提取时间
     */
    private List<String> XieChengTransferExecuteTime;

    /**
     * 携程新场景转化数据提取执行时间,携程撞库提取时间
     */
    private String XieChengNewTransferExecuteTime;

    /**
     * 携程新场景转化数据有效期
     */
    private int XieChengNewTransferValidityDay;

    /**
     * 拍拍贷有效期34；目前仅老客使用
     */
    private String ppdValidityDay;

    /**
     * 携程短信撞库轮询时间间隔
     */
    private Integer xieChengSmsCollidingDays;

    /**
     * 携程短信撞库线程数
     */
    private Integer xieChengSmsCollidingThread;

    /**
     * 携程短信撞库线程数Version2
     */
    private Integer xieChengSmsCollidingThreadVt;

    /**
     * 携程短信撞库线插入线程数量
     */
    private Integer xieChengSmsCollidingThreadLogSaveVt;
    /**
     * 携程短信撞库更新结果线程数
     */
    private Integer xieChengSmsCollidingThreadLogUpdateVt;
    /**
     * 携程短信撞库查询单次数据量级
     */
    private Integer xieChengSmsCollidingDataVtPageSize;

    /**
     * 通用文件入库
     */
    private Integer threadNumSftpToDbByCommon;

    /**
     * 入库条数
     */
    private Integer dataNumSftpToDbByCommon;

    /**
     * 拍拍贷老客推电销去重时间天数
     */
    private Integer ppdOldPhoneValidityDay;

    /**
     * 拍拍贷老客转人工数据提取apicode集合
     */
    private List<String> PPDOldTransferFileApiCodes;
    /**
     * 拍拍贷老客转人工数据提取执行时间
     */
    private String PPDOldTransferFileExecuteTime;
    /**
     * 拍拍贷有效期34；目前仅老客使用
     */
    private String ppdOldValidityDayStr;

    /**
     * 榕树转化数据生效截止时间
     */
    private String rsTransferDataToCustomerExpireDate;

    /**
     * 榕树为提取金额
     */
    private Integer rsUnlentAmount;

    /**
     * 榕树有效期
     */
    private String rsValidityDay;

    /**
     * 榕树周期性推送时间集合
     */
    private List<Integer> rongShuCyclePushDays;

    /**
     * 榕树推送人工Ibu接口开关，true为可推送，false不能推送
     */
    private Boolean rongShuPushDaasSwitch;

    /**
     * 榕树推送人工Ibu通用接口开关，true为推送通用接口，false推送老接口
     */
    private Boolean rongShuPushNewIbuSwitch;


    /**
     * 携程拨打明细推送暂停开关 true：暂停开关打开，false 暂停开关关闭
     */
    private Boolean xieChengCallingRecordSwitch;
    /**
     * 携程拨打明细补推sleep 时间
     */
    private Integer xieChengCallingRecordSleep;

    /**
     * 携程短信撞库开始时间
     */
    private String xieChengSmsCollidingStartTime;

    /**
     * 携程定时任务推决策有效期配置:配置为数字
     */
    private Integer xieChengPushPolicyValidityDay;

    /**
     * 榕树推送决策策略集
     */
    private HashMap<String, JSONObject> rsStrategyCodes;

    /**
     * 携程定时任务推决策apicode配置:[sourceapicode,targetapicode]
     */
    private List<String> xieChengPushPolicyApiCode;
    /**
     * 桔子转化数据提取
     */
    private List<String> orangeTransferFileApiCodes;


    /**
     * 分期乐自动化周期转决策周期，day为周期天数，格式{apiCode:{day:策略编号}}
     * eg:{"3710027":{0:"CASTR0000361",7:"CASTR0000362",14:"CASTR0000363"},"7410027":{0:"CASTR0000361",7:"CASTR0000362",14:"CASTR0000363"}}
     */
    private Map<String, Map<Integer, String>> fenqilePeriodPushDecisionPeriod;


    /**
     * 桔子转化数据推电销决策策略编号设置
     */
    private Map<String, String> originStrategyMap;
    /**
     * 桔子转化数据推daas 决策 apiCode
     */
    private List<String> originToDassApiCodes;

    /**
     * 你我贷推决策apicode配置
     */
    private String niWoDaiPushPolicyTargetApiCode;

    /**
     * 转化数据推送决策api_code对应关系 {(sourceapicode,targetapicode)}
     */
    private HashMap<String, String> apiCodeMatch;

    /**
     * 你我贷有效期
     */
    private String youMeDValidityDayStr;

    /**
     * 你我贷apiCode
     */
    private  List<String> youMeDApiCodes;

    /**
     * 你我贷转化数据提取相关配置 线程数量，是否继续（1-继续，0-退出）{"threadNum":"10","isContinue":"1"}
     */
    private Map<String,String> youMeDDataPull;

    /**
     * 你我贷数据提取时间
     */
    private String youMeDFileExecTime;

    /**
     * 海尔数据提取apiCode
     */
    private  List<String> haierApiCodes;


    /**
     * 海尔数据提取时间
     */
    private String haierTransferFileExecTime;
    /**
     * 国美apiCode
     */
    private  List<String> gomeApiCodes;
    /**
     * 国美数据提取时间
     */
    private String gomeFileExecTime;


    /**
     * 转化数据字段 apiCode配置{"3710058":"xiecheng"}
     */
    private Map<String, String> transferProcessFieldApiCode;

    /**
     * 2023-04-12 11:34
     * 自动化转决策任务配置
     * key {@link CustomerPushDecisionActionEnum}; value List{@link JobPushDecisionParameterBO}
     */
    private LinkedHashMap<String, JSONArray> jobPushDecisionParameterMap = new LinkedHashMap<>();

    /**
     * 众安转化数据提取时间
     */
    private String zhongAnFileExecTime;

    /**
     * 众安撞库要推送的userType
     */
    private List<String> zhongAnZkUserType;


    /**
     * 众安撞库userType对应的channelCode配置
     */
    private Map<String, String> ZhongAnZkUserTypeChannelCode;


    /**
     * 分期乐推送电销配置 场景：手机号推送去重天数 {"usertType":30}
     */
    private HashMap<String, Long> fenqiHappyPushDassConfig;

    /**
     * 对客作业配置 {"test":{"isThread":true,"threadNum":10,"isPause":false}}
     */
    private HashMap<String,JSONObject> customerJobConfig;


    /**
     * 滴滴推送通话明细线程数
     */
    private Integer didiCallRecordThread;

    /**
     * 滴滴推送通话明细执行时间
     */
    private String didiCallRecordExecTime;
    /**
     * 滴滴推送明细开关
     */
    private boolean didiCallRecordSwitch;

    /**
     * 滴滴联合建模执行时间
     */
    private String didiModelingExecTime;

    /**
     * 滴滴联合建模线程数
     */
    private String didiModelingThreadNum;

    /**
     * 滴滴剔除数据配置
     */
    private List<String> resverfiled1Data;

    /**
     * 众安明细推送配置{"userType":{"isPush":"1/0","channelCode":"****"}}
     */
    private HashMap<String, JSONObject> zhongAnDetailPush;


    /**
     * 永辉化数据提取apiCode
     */
    private List<String> yonghuiTransferExtractApiCodes;
    /**
     * 永辉转化数据提取时间
     */
    private String yonghuiTransferExtractTime;

    /**
     * 滴滴联合建模任务开关
     * true 打开，false 关闭执行
     */
    private Boolean didiModelingDataSwitch;

    /**
     * 滴滴数据提取apiCode集合
     */
    private List<String> didiApiCodes;

    /**
     * 滴滴联合建模数据提取时间
     */
    private String didiModeingFileExecTime;

    /**
     * 滴滴接口挡板开关
     * true 打开挡板，false 关闭挡板
     * {"pushSmsTrafficAccess":true,"pushReachSuccess":true,"pushJMASS":true}
     */
    private Map<String, Boolean> didiMockSwitch;

    /**
     * 滴滴准入重试次数
     */
    private Integer didiAllowRetryNum;

    /**
     * 滴滴有效期天数
     */
    private Long didiValidDays;

    /**
     * 推送客服黑名单apiCode(一对多分发)
     * customerBlackListApiCodes={"3710058":["3710058","3710078"],"7410950":["7410950","7410951"]}
     */
    private HashMap<String, List<String>> customerBlackListApiCodes;

    /**
     * 宜信推决策 查询转化数据的 apiCode
     */
    private String yiXinGetTransferToJueCeApiCode;

    /**
     * 宜信推决策  推决策数据集的 apiCode
     */
    private String yiXinTransferToJueCeApiCode;

    /**
     * 宜信实时转化数据推决策策略编号设置
     */
    private Map<String, String> yiXinToJueCeStrategyMap;

    /**
     * 宜信推决策线程池线程数量
     */
    private Integer yiXinToJueCeTpNum;

    /**
     * 宜信查询基础数据 limit 量级
     */
    private Integer yiXinSearchPageSize;

    /**
     * 滴滴联合建模新接口执行时间
     */
    private String didiModelingNewExecTime;

    /**
     * 滴滴联合建模新接口线程数
     */
    private String didiModelingNewThreadNum;

    /**
     * 滴滴联合建模新接口任务开关
     * true 打开，false 关闭执行
     */
    private Boolean didiModelingNewDataSwitch;

    /**
     * 滴滴联合建模mediaName
     */
    private Map<String, String> didiModelingMediaNameMap;
    /**
     * 转化数据的apiCode、convType、推送的apiCode映射关系
     * pushConvTypeConfig={"3710058":{"106":["3710058","3710078"],"107":["3710058"]},"7412009":{"106":["7412009","7410951"],"107":["7412009"]}}
     */
    private HashMap<String, JSONObject> pushConvTypeConfig;

    /**
     * 携程定时任务推决策情况apiCode配置:{"b":"371058"}
     */
    private Map<String,String> xieChengPushPolicyStatusToApiCode;

    /**
     * 迁移配置 key-功能项；value-具体的值；{"jobToEngineRoom":"1(开启)/0（关闭）"}
     */
    private HashMap<String,String> moveConfig;

    /**
     * job集群指定{“作业名称”:"zwpro/yzpro/zwfz/yzfz/zw/yz/all","default":"zwpro/yzpro/zwfz/yzfz/zw/yz/all"}
     */
    private HashMap<String,String> jobCluster;

    /**
     * 携程广告明细推送条件判断配置{“3710058”:{"condition":"1",soleCellApiCodes:["3710058","3710078"]},“3710078”:{"condition":1,soleCellApiCodes:["3710058","3710078"]},“3710090”:{"condition":2,soleCellApiCodes:["3710090","3710091"]},“3710091”:{"condition":2,soleCellApiCodes:["3710090","3710091"]}}
     */
    private HashMap<String, JSONObject> xieChengCallPushCondition;

    /**
     * 携程新场景短信撞库推送客服转化apiCode配置(一对多分发)
     * xiechengSmsCustomerTransferApiCodes={"3710090":["3710090","3710091"],"7410950":["7410950","7410951"]}
     */
    private HashMap<String, List<String>> xiechengSmsCustomerTransferApiCodes;

    /**
     * 携程短信撞库新场景apicode
     * xieChengSmsApiCode=3710090
     */
    private String xieChengSmsApiCode;

    /**
     * 携程新场景短信撞库,mq推送客服转化线程数
     */
    private Integer xieChengSmsMqPushCustomerThreadNum;

    /**
     * 2023-07-05 16:04
     * 非生成默认有效期配置的apiCode集合
     */
    private Set<String> nonConfigValidDefaultApiCodes;

    /**
     * 推送dass意向登记判断配置{"labelNm":["A","B"]}
     */
    private HashMap<String,List<String>>  gradeOfcallToDass;

    /**
     * 携程vt配置信息{"adVt":{"appId":"bairong002","source":"BaiRong_CPS_C01","iv":"3b2dac323465b024","aesKey":"f3df6f62f0527bf0","singKey":"95cc01ec07387a44"}}
     */
    private HashMap<String,JSONObject> xieChengVtConfig;

    /**
     * 宜信推决策，情况L策略编号设置
     */
    private Map<String, String> yiXinToJueCeStrategyMapOfL;

    /**
     * 滴滴接口mediaNm配置{“pushSmsTrafficAccess”:“bairongA”}
     */
    private Map<String, String> didiMediaNm;

    /**
     * 中邮数据文件流读取落库apiCode
     */
    private String zhongyouApiCode;

    /**
     * 中邮清洗数据线程数
     */
    private Integer zhongYouCleanDataThreadNum;

    /**
     * 中邮数据文件流读取落库字段数
     */
    private Integer zhongyouColumnsSize;

    /**
     * 中邮落库数据线程数
     */
    private Integer zhongYouFileDataThreadNum;

    /**
     * 模拟db异常，redis异常，true是开启，false是关闭
     * {"apiCode":true,"redis":true}
     */
    private HashMap<String,Boolean> mockError;

    /**
     * pulsar消费的开关，false关闭开关；true 打开开关
     */
    private Boolean pulsarSwitch;

    /**
     * 2023-08-23 15:15
     * 众邦状态分组与开关，状态优先级与配置顺序对应，最先配置的优先级也最高；
     * eg：{"d":{"groupNo":2,"dxUserType":"2","switch":true},"c":{"groupNo":1,"dxUserType":"1","switch":true},"b":{"groupNo":1,"dxUserType":"1","switch":true},"a":{"groupNo":1,"dxUserType":"1","switch":true}}
     * map key 为状态 value 状态对应的分组及开关，groupNumber为组号；dxUserType为人工电销业务线场景；switch为开关标识，true为开
     */
    private LinkedHashMap<String, JSONObject> zhongbangStatusTypeMap;

    /**
     * 众邦转化数据推人工转化过滤接口 apiCode
     */
    private List<String> zhongBangToDassFilterApiCodes;

    /**
     * 众邦转化数据推人工转化过滤接口线程数
     */
    private Integer zhongBangToDassFilterThreadNum;

    /**
     * 众邦转化数据推人工转化过滤接口 查询T-n~T日命中人工（sftp和api）的数据
     * zhongBangToDassLastDays=2（T-2~T）
     */
    private Integer zhongBangToDassLastDays;

    /**
     * 众邦转化数据推外呼首次非首次开关：true为首次，false非首次
     */
    private Boolean zhongBangToAIFirstSwitch;

    /**
     * 2023-08-27 15:19
     * 众邦转化数据推送daas线程数,默认1
     */
    private int zhongBangTransferPushDaasThreadPoolSize = 1;

    /**
     * 2023-08-27 15:19
     * 众邦手机去重天数，默认7天
     */
    private int zhongbangCellDistributeDay = 7;

    /**
     * 中原转化数据job api
     */
    private Set<String> zhongYuanJobApiCodes;

    /**
     * 中原线程池配置
     */
    private int ZhongYuanTransferPushOutBoundThreadPoolSize;
    /**
     * 中原转化数据、拨打明细推电销开关
     * {"condition_1":true,"condition_2":true,"condition_3":true,"condition_4":true,"condition_5":true,"condition_6":true}
     */
    private Map<String,Boolean> zhongYuanConditionMap;

    /**
     * 中原转化数据推Daas 和 客服 数据线程数
     */
    private Integer zhongYuanTransferDataToDaasAndCustomerFilterThreadNum;
    /**
     * 中原转化数据推Daas 7天内推送一次
     */
    private Integer zhongYuanDaysToSend;
    /**
     * 携程新AppId 撞库
     */
    private String xieChengNewAppId;

    /**
     * 奇富360 推送客服过滤apiCode 配置
     * {"QiFu_TransferData_To_CustomerFilter":"3710053","QiFu_TransferData_To_CustomerFilter_Brother":"3710105"}
     */
    private Map<String,String> qiFuApiCodeToCustomerMap;

    /**
     * 奇富360 apiCode
     */
    private Set<String> qiFuApiCodes;

    /**
     * 奇富断点自动化数据推决策线程数
     */
    private Integer qiFuBreakPointDataToJueCeThreadNum;

}

