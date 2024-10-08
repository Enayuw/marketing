package com.br.marketing.speedconfig;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.JobPushDecisionParameterBO;
import com.br.marketing.enums.CustomerPushDecisionActionEnum;
import com.br.marketing.enums.DingDingAlarmFunctionEnum;
import com.br.speed.client.common.annotations.SpeedFile;
import lombok.Data;
import org.springframework.context.annotation.Configuration;
import shaded.com.google.common.collect.Lists;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
     * 原始上传消费端入库线程数
     */
    private Integer soleNum;

    /**
     * 原始转化消费端入库线程数
     */
    private Integer soleNumTrans;

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
     * eg：{"促首登":"T","促申完":"T-15","促首借":"T+31","促复借":"T+0"}
     *
     * @deprecated 弃用，已使用新有效期配置
     */
    @Deprecated
    private Map<String, String> shuHeTransferExtractDayMap;

    /**
     * 数禾转化数据提取apiCode集合
     * eg:{"3710004":["促申完","促首登"],"3710023":["促首借"],"3710043":["促复借"],"3710051":["重申"],"3710071":["促首登"]}
     */
    private HashMap<String, List<String>> shuHeTransferExtractApiCodes;

    /**
     * 数禾转化数据提取任务开始时间
     */
    private String shuHeTransferExtractJobStartTime;

    /**
     * 数禾转化数据提取任务是否使用准全量转化数据
     * @deprecated 弃用，已使用新版有效期范围
     */
    @Deprecated
    private Boolean shuHeTransferExtractIfUseQuasiTotalQuantity;

    /**
     * 数禾有效期, T 代表当前天到月底； T+/-day 代表当前天到day-1天，共day天
     * eg：{"促首登":"T","促申完":"T-15","促首借":"T+31","促复借":"T+0"}
     * @deprecated 弃用，已使用最新版本有效期配置
     */
    @Deprecated
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
     * 宜信转化数据提取V4.0 apiCode集合
     * (list第一个值放文件名称前缀，防止新job覆盖小程序的，文件对比没问题后改成与小程序同名文件，并下掉小程序代码)
     * yinXinTransferV4ApiCodes=["yixinzhuanhua_all","7411787"]
     */
    private List<String> yinXinTransferV4ApiCodes;

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
     * check服务job的上线开关
     */
    private Boolean dataBridgeJobOnlineSwitch;

    /**
     * 服务上线job开关-Monkey
     */
    private Boolean dataMonkeyJobOnlineSwitch;

    /**
     * ruleScheduler服务job的上线开关
     */
    private Boolean ruleSchedulerJobOnlineSwitch;

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
     * 同程转化数据提取apicode集合
     */
    private List<String> NewTongChengTransferFileApiCodes;

    /**
     * 同程转化数据提取执行时间
     */
    private String NewTongChengTransferExecuteTime;
    /**
     * 同程集团转化数据提取apiCode集合
     */
    private List<String> TongChengGroupTransferFileApiCodes;
    /**
     * 同程转化数据提取执行时间
     */
    private String TongChengGroupTransferExecuteTime;
    /**
     * 宜信转化数据提取V4.0
     */
    private String YiXinV4TransferExecuteTime;

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
     * 携程短信撞库线程数重试
     */
    private Integer xieChengSmsCollidingRetryThread;

    /**
     * 携程短信撞库报警量级
     */
    private Integer xieChengSmsCollidingRetryWarnCount;

    /**
     * 携程短信撞库报警量级
     */
    private List<String> xieChengSmsCollidingRetryWarnAllTime;

    /**
     * 携程短信撞库挡板及异常 [true,true]
     */
    private List<Boolean> xieChengSmsCollidingRetrySwitch;

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
     * 榕树自动化规则调用决策，根据情况配置对应的决策参数 strategyCode
     * 说明：
     * {
     *   "apiCode":{
     *     "finalState":"策略编号"
     *   }
     * }
     * 样例：
     * {
     * 	"7492800": {
     * 		"2": "CASTR0142044",
     * 		"3": "CASTR0142043",
     * 		"4": "CASTR0142042",
     * 		"5": "CASTR0142041",
     * 		"6": "CASTR0142040",
     * 		"7": "CASTR0142039",
     * 		"8": "CASTR0142039"
     *  },
     * 	"4004643": {
     * 		"2": "CASTR0000864",
     * 		"3": "CASTR0000865"
     *  }
     * }
     */
    private HashMap<String, JSONObject> rongShuPushPolicyStrategyCode;

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
     * {
     * "7492800":{"c":"CASTR0000846","d":"CASTR0000847","1":"CASTR0000846"},
     * "4004643":{"c":"CASTR0000864","d":"CASTR0000865"}
     * }
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
     * 众安明细推送Mock挡板  1: 开启, 0: 关闭
     * {"pushSwitch":"1","retCode":"1"}
     */
    private String zhongAnPushMock;

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
//
//    /**
//     * 滴滴有效期天数
//     */
//    private Long didiValidDays;

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
     * 宜信转化过滤推送百应执行时间
     */
    private String yiXinTransferPushBaiYingExecuteTime;

    /**
     * 宜信转化过滤推送百应执行时间
     */
    private Map<String, Integer> yiXinTransferPushBaiYingPageConfig;

    /**
     * 宜信转化过滤推送百应线程
     */
    private Map<String, Integer> yiXinTransferPushBaiYingThreadPool;

    /**
     * 宜信转化过滤推送百应推送配置
     */
    private Map<String, Object> yiXinTransferPushBaiYingPush;

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
     * 2024-07-05 16:04
     * 生成默认有效期定制配置的apiCode集合
     */
    private Set<String> customizeConfigValidDefaultApiCodes;

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
     * 2023-09-27 19:12
     * 奇富保存触达记录删除,删除的天
     */
    private int qiFuSaveReachDeleteRecordDay = 43;

    /**
     * 2023-09-27 19:12
     * 奇富钉钉告警机器人token
     */
    private String qiFuDingDingAccessToken = "7f32618dafd2d2126f5564aaf57a35867c8775baf78777140990c16d56edc457";

    /**
     * 2023-09-27 19:12
     * 奇富钉钉告警机器人密钥
     */
    private String qiFuDingDingSecret = "SEC4d2d8a91842ad25136e92213a852ebe5cf1c22ddaf49dcfd352d5a9323eb1ca8";

    /**
     * 2023-09-27 19:12
     * 奇富接口公钥
     */
    private String qiFuApiPublicKey;

    /**
     * 2023-09-27 19:12
     * 奇富接口AppId
     */
    private String qiFuApiAppId;


    /**
     * 奇富360 推送客服过滤apiCode 配置
     * {"QiFu_TransferData_To_CustomerFilter":"3710053","QiFu_TransferData_To_CustomerFilter_Brother":"3710105"}
     */
    private Map<String, String> qiFuApiCodeToCustomerMap;

    /**
     * 奇富360推送决策 apiCode
     * qiFuToJueCeApiCodes={"3710053":"3710105","7491631":"7491630"}
     */
    private HashMap<String, String> qiFuToJueCeApiCodes;

    /**
     * 奇富断点自动化数据推决策线程数
     */
    private Integer qiFuBreakPointDataToJueCeThreadNum;

    /**
     * 2024-06-27
     * 奇富删除触达记录删除区间
     */
    private Integer qiFuDeleteReachRecordDay;

    /**
     * 页数动态调整配置
     * dynamicPageSize={"yxToDx":20000,"yxToCustomer":20000,"yhGet":20000}
     */
    private HashMap<String, Integer> dynamicPageSize;

    /**
     * 2023-10-28 10:35
     * 客户定制化接口自定义配置apiCode
     * eg:{"T_GUME":["3710076", "7492805"]}
     */
    private Map<String, List<String>> customerHandlerEnumConfigMap;


    /**
     * 转化数据执行通用规则重推流程线程数
     */
    private Integer universalTransferProcessResendThreadNum;

    /**
     * 推送决策系统的选择
     */
    private Integer pushJcSelect;

    /**
     * 众邦财富转化数据提取apiCode集合
     */
    private List<String> ZhongBangTransferApiCodes;

    /**
     * 众邦财富转化数据提取执行时间
     */
    private String ZhongBangTransferExecuteTime;

    /**
     * 众邦财富定制标签线程数
     */
    private Integer zhongBangCaifuLabelThreadNum;

    /**
     * 数据处理通用流程单任务线程数
     */
    private Integer dataProcessAnTaskThreadNum;


    /**
     * 众邦财富拉取文件
     * 格式：{apiCode:{文件名称:表头}}
     * 文件名称带扩展名时直接使用该名称,文件名最后一个字符为“_”时系统自动默认拼接日期
     * eg:{"3710027":[{"original_caifu_":"custNum,id,cell"},{"original_daikuan_":"custNum,id","transform_":"custNum,ifLogin1"}]}
     */
    private Map<String, List<Map<String, String>>> zhongBangPullFileDataConfigMap;

    /**
     * 2023-11-22 13:20
     * 众邦财富拉取文件，拉取（T+/-N）天的文件
     */
    private int zhongBangPullFileDataDay = -1;

    /**
     * 众邦财富定制标签测试
     */
    private Boolean zhongBangCaifuLabelTest;

    /**
     * 众邦录音明细回调测试
     * {"open":"true","code":"500"}
     *      code success:1
     *      code fail:0
     *      code 流控:500
     */
    private String zhongBangRecodFileReTest;

    /**
     * 众邦财富上传录音文件明细配置,{"b_zhongbang_voice_file_detail":{"fileType":"zhongbang_voice","uploadPoolSize":5,"getFilePoolSize":5}}
     */
    private Map<String, JSONObject> zhongBangVoiceFileConfig = new HashMap<>();


    /**
     * 2023-12-05 10:35
     * 众邦文件下载配置信息
     */
    private Map<String, String> zhongBangDownloadFileInfoMap;
    private String zhongBangDownloadFileSlotKey;

    /**
     * 数据转化提取任务锁失效时间
     */
    private Long transferFileTaskJobLockExpireTime;

    /**
     * 同程不运营名单推送客户接口挡板开关 true:开启挡板。false:关闭挡板
     * tongChengUndoMock={"switch":false,"httpcode":"200","code":"1001"}
     */
    private HashMap<String, Object> tongChengUndoMock;

    /**
     * 推送百应黑名单接口挡板开关 true:开启挡板。false:关闭挡板
     * baiYingUndoMock={"switch":true,"httpcode":"200","code":"000000"}
     * switch：
     *      true:开启挡板
     *      false:关闭挡板
     * httpcode：
     *      200:请求成功
     * code：
     *      000000:调用百应黑名单接口成功
     */
    private HashMap<String, Object> baiYingUndoMock;

    /**
     * 推送之家创建线索接口挡板开关 true:开启挡板。false:关闭挡板
     * zhiJiaUndoMock={"switch":true,"httpcode":"200","code":"0"}
     * switch：
     *      true:开启挡板
     *      false:关闭挡板
     * httpcode：
     *      200:请求成功
     * code：
     *      0:调用之家创建线索接口成功
     */
    private HashMap<String, Object> zhiJiaUndoMock;

    /**
     *
     */
    private HashMap<String, Object> qiFuDeleteReachRecordMock;

    /**
     * 同程不运营名单推送客户接口线程数
     */
    private Integer tongChengUndoThreadNum;

    /**
     * 同程不运营名单推送客户接口apiCode集合
     */
    private List<String> tongChengUndoApiCodes;
    /**
     * 携程推送短信退订接口配置信息
     * eg:{"3710090":{"appid":"bairong002","signKey":"95cc01ec07387a44","aesKey":"f3df6f62f0527bf0","aesIv":"3b2dac323465b024"}}
     */
    private Map<String, Map<String, String>> xieChengSmsQuitConfig;

    /**
     * 携程推送短信退订apiCode
     */
    private List<String> xieChengSmsQuitApiCodes;


    /**
     * 跑分结果推送重试次数
     */
    private Integer scorePushRetryNum;

    /**
     * 跑分入库线程数量
     */
    private Integer scoreDbAndRedisThreadNum;

    /**
     * 跑分更新顺序线程数量
     */
    private Integer scoreUpdateSortThreadNum;

    /**
     * 数禾电销apiCode
     */
    private List<String> shuheDxApiCodes;





    /**
     * 有效期变更apiCode
     */
    private List<String> validityPeriodApiCodeList;

    /**
     * 有效期变更接口开关
     */
    private Boolean changeValidityPeriodIndex;


    /**
     * 跑分回调获取taskId分页
     */
    private Integer scoreTaskPageSizeByPushCustomer;

    /**
     * 跑分回调获取数据分页
     */
    private Integer scoreDataPageSizeByPushCustomer;

    /**
     * 数禾上传数据推决策，策略编号设置
     */
    private Map<String, String> shuheToJueCeStrategy;


    /**
     * 海尔撞库线程池数量配置
     */
    private Integer haierCollidingDataThreadNum;
    /**
     * 海尔撞库单次查询数量配置
     */
    private Integer haierCollidingDataPageSize;

    /**
     * 海尔撞库配置
     */
    private Map<String, String> haierCollidingDataConfig;

    /**
     * 海尔接口公钥
     */
    private String haierApiPublicKey;

    /**
     * 海尔撞库接口mock配置
     */
    private HashMap<String, Object> haierCollidingDataMock;

    /**
     * 海尔撞库上传清洗apiCode配置
     */
    private List<String> haierCollidingDataSyncApiCode;

    /**
     * 海尔转化数据提取apiCode集合
     */
    private List<String> NewHaierTransferApiCodes;

    /**
     * 海尔转化数据提取执行时间
     */
    private String NewHaierTransferExecuteTime;

    /**
     * 众邦转化数据提取apiCode集合
     */
    private List<String> ZhongBangApiCodes;

    /**
     * 众邦转化数据提取执行时间
     */
    private String ZhongBangExecuteTime;


    /**
     * 模拟跑分回调异常 1-获取数据异常；2-更新排序异常；3-推送数据异常
     * {"1":true,"2":true}
     */
    private HashMap<String,Boolean> mockCallBackError;
    /**
     * 奇富360转化数据提取apiCode集合
     */
    private List<String> QiFuTransferApiCodes;

    /**
     * 奇富360转化数据提取执行时间
     */
    private String QiFuTransferExecuteTime;

    /**
     * 同程待运营名单推送客户接口挡板开关 true:开启挡板。false:关闭挡板
     * tongChengAgentMock={"switch":false,"httpcode":"200","code":"1001"}
     */
    private HashMap<String, Object> tongChengAgentMock;


    /**
     * 同程集团运营名单推送客户接口apiCode集合
     */
    private List<String> TongChengGroupOperationApiCodes;

    /**
     * 同程集团运营名单推送客户接口线程数
     */
    private Integer tongChengGroupOperationThreadNum;

    /**
     * 同程集团运营名单推送客户接口单批次捞数量
     */
    private Integer tongChengGroupOperationNum;
    /**
     * 2024/1/24 15:52
     * 数禾数据场景与apiCode映射信息
     * key userType；value apiCodeSet
     * eg：{"促复借":["3710051","7410785"]}
     */
    private Map<String, List<String>> shuHeUserTypeAndApiCodeMappingMap = new HashMap<>();

    /**
    * 携程撞库异常量级钉钉通知accessToken
     */
    private String xieChengGroupAccessToken;

    /**
     * 携程撞库异常量级钉钉通知Secret
     */
    private String xieChengGroupSecret;

    /**
     * 得物撞库开关
     * true 开启撞库  false  暂停撞库
     */
    private Boolean deWuCollidingSwitch;

    /**
     * 得物撞库线程池数
     */
    private Integer deWuCollidingThread;

    /**
     * 得物撞库数据上传线程池数
     */
    private Integer deWuCollidingDataUploadSyncThread;

    /**
     * 得物撞库apiCode
     */
    private String deWuCollidingAiCode;

    /**
     * 得物停止撞库量级
     */
    private Integer deWuCollidingStopCount;

    /**
     * 2024-03-01 15:12
     * 业务名称_函数名 参考{@link DingDingAlarmFunctionEnum}
     * 钉钉告警机器人WebHook信息token与secret(密钥);
     * startTime：允许告警的开始时间，endTime：允许告警的结束时间，闭区间，格式: hh:mm:dd;
     * at：需要@的人
     * {"业务名称_函数名":{"token":"token","secret":"secret","startTime":"startTime","endTime":"endTime","at":["cell"]}}
     */
    private Map<String, JSONObject> dingDingWebHookInfo = new ConcurrentHashMap<>();

    /**
     * apiCode自动生成场景与统计控制
     * ["3","4"]
     */
    private Set<String> userTypeAndSumRealtimeApiCodeStartsWith = new HashSet<>(Arrays.asList("3", "4"));


    /**
     * 得物撞库limit 数量降级的量级
     */
    private Integer deWuCollidingStopThresholdCount;

    /**
     *  得物撞库url地址
     */

    private String deWuCollidingUrl;
    /**
     * 得物撞库appId
     */
    private String deWuAppId;
    /**
     * 得物撞库一次性从基表中获取数据量
     */
    private int deWuCollidingLimit = 10000;
    /**
     * 得物mock数据开关["开关","httpcode","code","status"],
     * 样例：
     *   deWuCollidingMockSwitch=["true","200","200","1"] 开启挡板，并且得到网络响应200,数据中code=200,status=1的样例数据
     * 详解：
     *   开关:
     *     "true":开启挡板,使用测试数据
     *     "false"关闭挡板,使用真实调用客户的返回结果
     *   httpcode:
     *     "200":返回httpcode=200的mock数据
     *     "500":返回httpcode=500的mock数据
     *     "1001":返回httpcode=1001的mock数据
     *   code:
     *     "200":返回code=200的mock数据
     *     "401":返回code=401的签名认证失败的mock数据
     *   status:
     *     "0":status=0的mock数据
     *     "1":status=1的mock数据
     */
    private List<String> deWuCollidingMockSwitch;

    /**
     * 2024-03-13 22:06
     * 数禾非黑名单判断生效apicode集合
     */
    private Set<String> shuHeNonBlackListApiCodeSet = new HashSet<>(
            Arrays.asList("3710071", "3710051", "3710023", "3710128", "3710117", "3710123", "7410785"));

    /**
     * 2024-03-22 16:11
     * 上传和转化实时统计开关，false 关闭实时统计，true 开启实时统计
     */
    private Boolean uploadAndTransferDataRealtimeStatisSwitch = false;

    /**
     * 携程强制开启撞库开关
     * true 打开，false 关闭
     */
    private Boolean xieChengForceOpenSwitch;

    /**
     * 携程非周期撞库线程池数
     */
    private Integer xiechengRobCollidingThread;

    /**
     * 携程撞库分钟阈值
     */
    private Integer xiechengPerMinuteThreshold;


    /**
     * 携程撞库分页大小
     */
    private Integer xiechengCollidingPageSize;


    /**
     * 携程记录撞库日志线程数
     */
    private Integer xiechengSaveCollidingLogThread;

    /**
     * 携程数据清洗线程数
     */
    private Integer xieChengCleanThreadCount;
    /**
     * 携程数据清洗limit 量级
     */
    private Integer xieChengCleanLimitCount;

    /**
     * 清洗暂停开关 true 开启清洗  false 关闭 清洗
     */
    private Boolean xieChengCleanSwitch;

    /**
     * 携程定制化配置ApiCode
     */
    private String xieChengDingZhiApiCode;

    /**
     * 携程转化数据提取apiCode集合
     */
    private List<String> XieChengTwoTransferApiCodes;


    /**
     * 携程转化数据提取执行时间,携程撞库提取时间
     */
    private List<String> XieChengTwoTransferExecuteTime;

    /**
     * 携程短信撞库数据提取时间范围,索引未知0为开始时间，1为结束时间，key小于0为T-n，等于0为T，大于0为T+1
     * eg:[{-1:"06:00"},{0:"07:00"}]
     */
    private List<Map<Integer, String>> xieChengCallBackResultTimeRange = new ArrayList<>();

    /**
     * 数禾促复借转化数据提取数据提取apicode集合
     */
    private List<String> ShuHeCuFuJieTransferFileApiCodes;

    /**
     * 数禾促复借转化数据提取执行时间
     */
    private String ShuHeCuFuJieTransferFileExecuteTime;

    /**
     * 2024-04-18 10:53
     * 数禾适配新有效期apiCode与场景信息eg:{apiCode:[场景]}
     * @deprecated 已废弃
     */
    @Deprecated
    private Map<String, JSONArray> shuHeNewPeriodOfValidityMap = new HashMap<>();
    /**
     * 修复cell的apiCode前缀集合
     */
    private List<String> updateCellApiCodePrefix;

    /**
     * 无解密清洗配置
     */
    private String noDesCleanConfig;


    /**
     * 携程跑分数据同步、TRUE数据剔除、非TRUE数据清洗apicode集合
     */
    private List<String> xieChengCollidingDataProcessApiCodes;

    /**
     * 携程跑分数据同步 查询T-n~T日跑分记录表
     * XieChengRuleScoreToDbLastDays=3（T-3~T）
     */
    private Integer XieChengRuleScoreToDbLastDays;

    /**
     * 携程撞库数据清洗线程数
     */
    private Integer xieChengCollidingDataProcessThread;

    /**
     * 携程撞库跑分数据同步线程数
     */
    private Integer xieChengCollidingRuleScoreToDBThread;

    /**
     * 携程撞库数据推送决策线程数
     */
    private Integer xieChengCollidingDataPushPolicyThread;

    /**
     * 携程撞库跑分数据同步文件字段映射
     * xieChengCollidingRuleScoreFieldMap={"userType":"user_type"}
     */
    private Map<String, String> xieChengCollidingRuleScoreFieldMap;

    /**
     * 携程定制化页面周期TRUE列表apiCode配置
     */
    private String xieChengCustomizeTrueApiCode;

    /**
     * 携程撞库停止跑分数据同步，跑分编号配置集合
     * ["7410950_20240507000000_6334","7410950_20240507000000_6682"]
     */
    private List<String> xieChengCollidingRuleScoreStopBatchNums;

    /**
     * 携程定制化页面列表排序字段配置
     */
    private Map<String, JSONObject> xieChengCustomizeOrderByClauseConfig;
    /**
     * 推送决策超时后，查询结果要延时的 分钟 数
     */
    private Long queryCustomerPushTimeOutDelay;

    /**
     * 宜信推决策(原转人工数据)的 apiCode
     */
    private String yiXinToPolicyApiCode;

    /**
     * 金美鑫黑名单文件校验相关配置
     */
    private String jinMeiXinBlackListFileCheckConfig;

    /**
     * 榕树转化数据提取apiCode集合
     */
    private List<String> rongShuTransferApiCodes;

    /**
     * 榕树数据提取时间
     */
    private String rongShuFileExecTime;
    /**
     * 间隔几分钟后调用决策配置
     * apiCode：客户编号
     * source：1通用转化流程,2客服拨打数据,3原始数据上传流程,4人工拨打流程,5转化数据集合流程,6初始数据集合流程
     * breakFlag：是否继续走实时流程（true：不走实时流程，false：走实时流程）
     * intervalTime：间隔时间（默认单位：分钟）
     * 例：{"7410086":{"apiCode":"7410086","source":"1","breakFlag":"true","intervalTime":"1"},
     *      "3010086":{"apiCode":"3010086","source":"3","breakFlag":"false","intervalTime":"2"}}
     */
    private Map<String, JSONObject> periodPushConfig;


    /**
     * 数据清洗试跑apiCode
     */
    private String datacleanTestRunApiCode;

    /**
     *  * marketing-task跑分服务节点数量
     */
    private Integer numberOfScoreTaskNodes;

    /**
     * 携程CPA撞库过滤转化数据convType配置
     */
    private List<String> xieChengCpaExcludeConvTypeConfig = Lists.newArrayList("107","105");

    /**
     * 奇富360获取用户信息接口线程数
     */
    private Integer qiFuQryUserMessageThreadNum;

    /**
     * 奇富360用户信息接口分页参数
     */
    private Integer qiFuQryUserMessageSize;

    /**
     * 奇富360转化数据提取apiCode集合
     */
    private List<String> CuDongZhiTransferExtractApiCodes;

    /**
     * 奇富360转化数据提取执行时间
     */
    private String CuDongZhiTransferFileExecuteTime;
    /**
     * 奇富360数据清洗调用转化接口线程数
     */
    private Integer dataCleanQiFu360CoreAndMaxNum;
    /**
     * #奇富360获取用户信息接口挡板 true:开启挡板。false:关闭挡板
     */
    private HashMap<String, Object> qifuQryUserMessageMock;

    /**
     * 数禾推送客服黑名单促首登场景apicode映射配置
     * eg:{"3710149":["促首登"],"7410727":["促首登"]}
     */
    private HashMap<String, List<String>> shuHeCuShouDengBlackListApiCodeMapping;

    /**
     * 数禾推送客服转化促首登场景apicode映射配置
     * eg:{"3710149":["促首登"],"7410727":["促首登"]}
     */
    private HashMap<String, List<String>> shuHeCuShouDengTransferApiCodeMapping;

    /**
     *  携程撞库推决策分页查询数量
     */
    private Integer xiechengZkToPlicyPageSize;

    /**
     *  携程短信撞库接口参数配置
     */
    private JSONObject xieChengSmsCollidingConfig;

    /**
     *  奇富360推决策策略编号配置
     */
    private JSONObject qiFuToPolicyStrategyCodeConfig;

    /**
     * 奇富360上传推决策有效期检测延迟时长配置，单位：秒
     */
    private Long qiFuSyncToPolicyValidityCheckDelayTime;

    /**
     * 之家创建线索程池数
     */
    private Integer zhiJiaCollidingThread;

    /**
     * 之家落库apiCode
     */
    private String zhiJiaApiCode;

    /**
     * 之家用户信息分页参数
     */
    private Integer zhiJiaQryUserMessageSize;
    /**
     * 奇富360数据提取参数配置
     */
    private HashMap<String, JSONObject> qiFuExtDataConfig;

    /**
     * 奇富360全量数据提取VT切换参数配置
     */
    private Boolean qiFuExtDataVTConfig;

    /**
     * 奇富360全量数据提取参数配置
     */
    private HashMap<String, JSONObject> qiFuFullExtDataConfig;

    /**
     * 奇富360全量数据提取线程数
     */
    private Integer qiFuFullExtDataSoleNum;

    /**
     * 苏商推送通话明细apiCode
     */
    private List<String> suShangApiCodes;

    /**
     * 苏商查询数据 limit 量级
     */
    private Integer suShangSearchPageSize;

    /**
     * 苏商推送数据线程数
     */
    private Integer suShangPushThreadNum;

    /**
     * 苏商自动化回传apiCode集合
     */
    private List<String> SuShangTransferExtractApiCodes;

    /**
     * 苏商自动化回传执行时间
     */
    private String SuShangTransferFileExecuteTime;


    /**
     * 58同程撞库提交撞库apiCode集合
     */
    private List<String> wubaCollidingApiCodes;

    /**
     * 58撞库数据落库线程数（同步、提交撞库、查询撞库结果）
     */
    private Integer wubaCollidingDataSyncThreadNum;

    /**
     * 58同步撞库数据分页条数
     */
    private Integer wuBaCollidingDataSyncPageSize;

    /**
     * 58撞库高价值文件名集合
     */
    private List<String> wubaCollidingHighValueFiles;

    /**
     * 58撞库每日最大量级限制
     */
    private Integer wubaCollidingDataMaxCountLimit;

    /**
     * 58提交周期撞库开关(true:开启，false关闭)
     * {"T":true,"S":true}
     */
    private HashMap<String, Boolean> wuBaCollidingDataSwitch;

    /**
     * 58提交撞库周期天数配置
     * {"T":8,"S":15}
     */
    private HashMap<String, Integer> wuBaCollidingCycleDayConfig;

    /**
     * 58提交撞库数据分页条数
     */
    private Integer wuBaCollidingDataSubmitPageSize;

    /**
     * 58查询撞库结果分页条数
     */
    private Integer wuBaCollidingQueryResultPageSize;

    /**
     * 58查询撞库结果等待时长（分钟）
     */
    private Integer wuBaCollidingQueryResultWaitMinutes;

    /**
     * 58新客-营销名单上报-修改上报数据-开关，1-开，0-关
     */
    private String wuBaSubmitConversionChangeDataSwitch;

    /**
     * 58新客-营销名单上报-修改上报数据-条件参数
     */
    private List<Map<String, String>> wuBaSubmitConversionChangeDataParams;

    /**
     * 58新客-营销名单上报-修改上报数据-线程参数
     */
    private Map<String, String> wuBaSubmitConversionChangeDataThreadConfig;

    /**
     * 58新客-营销名单上报-开关，1-开，0-关
     */
    private String wuBaSubmitConversionSwitch;

    /**
     * 58新客-营销名单上报-条件参数
     */
    private List<Map<String, String>> wuBaSubmitConversionParams;

    /**
     * 58新客-营销名单上报-分页条数
     */
    private Integer wuBaSubmitConversionPageSize;

    /**
     * 58新客-修改营销名单上报批次-开关，1-开，0-关
     */
    private String wuBaChangeQueryBatchSwitch;

    /**
     * 58新客-修改营销名单上报批次-pushTime查询区间
     */
    private JSONObject wuBaChangeQueryBatchPushTimeInterval;

    /**
     * 58新客-营销转化结果查询-开关，1-开，0-关
     */
    private String wuBaQueryConversionSwitch;

    /**
     * 58新客-营销转化结果查询-apiCode集合
     */
    private List<String> wuBaQueryConversionApiCodes;

    /**
     * 58新客-营销转化结果查询-pushTime查询区间
     */
    private JSONObject wuBaQueryConversionPushTimeInterval;

    /**
     * 58新客-营销转化结果查询-调用线程
     */
    private List<Integer> wuBaQueryConversionThreadPool;

    /**
     * 58新客-营销转化结果查询-批量DB线程
     */
    private Integer wuBaQueryConversionBatDBThreadPool;

    /**
     * 58新客-营销转化结果查询-批量DB分页条数
     */
    private Integer wuBaQueryConversionBatDBPartitionSize;

    /**
     * 58新客-营销转化结果查询-通用字段配置
     */
    private Set<String> wuBaQueryConversionKnowFields;

    /**
     * 58新客-营销名单上报-数据提取-执行时间
     */
    private String wuBaSubmitDataToFileExecuteTime;

    /**
     * 58新客-营销名单上报-数据提取-ApiCodes
     */
    private List<String> wuBaSubmitDataTransferApiCodes;

    /**
     * 58新客-营销名单上报-数据提取-StartDate, 例：20240814
     */
    private List<String> wuBaSubmitDataToFileStartDate;

    /**
     * 通用清洗线程池
     */
    private Integer autoCleanDataThreadNum;

    /**
     * 通用Mock配置
     */
    private JSONObject commonMockConfig;

    /**
     *
     * 58新课数据提取参数配置
     */
    private HashMap<String, JSONObject> wbxkExtDataConfig;

    /**
     * 58撞库接口参数配置
     */
    private JSONObject wuBaCollidingUrlConfig;

    /**
     * 医时转化数据提取apiCode集合
     */
    private List<String> YiShiTransferApiCodes;

    /**
         * 医时转化数据提取执行时间
     */
    private String YiShiTransferFileExecuteTime;


    /**
     * 2023-10-28 10:35
     * 客户定制化上传接口自定义配置apiCode
     * eg:{"U_GUME":["3710076", "7492805"]}
     */
    private Map<String, List<String>> customerUploadHandlerEnumConfigMap;

    /**
     * 携程跑分数据同步任务保持驼峰字段配置
     */
    private List<String> xieChengScoreToDbKeepCamelColumns;

    /**
     * 携程清洗任务分页条数
     */
    private Map<String, Integer> xieChengCollidingDataProcessPageSize;

    /**
     * 推决策报警apiCode集合
     */
    private List<String> pushAlarmApiCode;
    /**
     *  * 2024-08-13 15:42
     * 榕树上传数据清洗线程池配置,eg：[25,50],25为核心线程数，50为最大线程数
     */
    private List<Integer> rongShuCleanUploadTreadPoolSize = new ArrayList<>(Arrays.asList(2, 5));

    /**
     * 奇富360数据提取custNum对应上传字段
     */
    private HashMap<String, List<String>> qiFuExtDataCustNumMapConfig;

    /**
     * 原始上传数据清洗映射配置 key:apiCode value:映射的原值字段
     */
    private HashMap<String, String> dataCleanMappingMap;

    /**
     * 原始上传数据清洗映射新值字段
     */
    private List<String> dataCleanValue;

    /**
     * bi报表步长配置
     */
    private Map<String,List<String>> biReportStepConfig;

    /**
     * 2024-08-20 15:42
     * 跑分模型分布 配置数据展示规则时需要的产品前缀
     */
    private Set<String> reportScorePrefixSet;

    /**
     * 跑分报表分值区间配置
     */
    private Map<String, Integer> scoreReportRangeConfig;

    /**
     * 58钉钉告警token
     */
    private String wuBaDingDingAccessToken;

    /**
     * 58钉钉告警密钥
     */
    private String wuBaDingDingSecret;

    /**
     * 数禾推送百可录AES加密key配置
     */
    private JSONObject shuHeToBioclooAesKeyConfig;

    /**
     * 携程Bi报表展示数量
     */
    private Map<String, Integer> xiechengBiReportShowNumMap;

    /**
     * 携程Bi单日撞库报表dataPacket排序
     */
    private Map<String, Integer> xiechengBiReportDistrubuteDayDataPacketOrderMap;

    /**
     * 加解密秘钥配置 格式 {"7492770":{"aesKey":"7H6j39W6W6b90077","aesIv":"sd0S94rU1009P43B"}}
     */
    private JSONObject cryptoConfig;

    /**
     * Bi报表分组维度配置
     */
    private HashMap<String, JSONObject> biReportGroupConfig;

    /**
     * Bi报表分组维度字典配置
     */
    private HashMap<String, JSONObject> biReportGroupDictConfig;

    /**
     * 跑分模型分布 配置数据展示规则时需要的产品（无多头）前缀
     */
    private Set<String> reportScoreOnlyPrefixSet;

    /**
     * 跑分模型分布 配置数据展示规则时需要的多头前缀
     */
    private Set<String> reportmultPointPrefixSet;

    /**
     * bi报表场景名称前缀配置
     */
    private HashMap<String, String> biReportScenePrefixConfig;

    /**
     * 数禾推送百可录场景及apiCode路由配置
     */
    private JSONObject shuHeToBioclooUserTypeAndApiCodeMapping;

}

