package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.PropertyPreFilters;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.adapter.TransferSyncAdapter;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dos.PeriodOfValidityDO;
import com.br.marketing.dto.PushShDXDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.PhoneBlackMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheTransferDataService;
import com.br.marketing.service.ITransferSyncUserService;
import com.br.marketing.service.PushDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 数禾转化实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:25
 */
@Service
@Slf4j
public class PushShuheTransferDataServiceImpl implements IPushShuheTransferDataService {

    @Resource
    private CaseShuheUserMapper caseShuheUserMapper;
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private ITransferSyncUserService iTransferSyncUserService;
    @Resource
    private PushDataService pushDataService;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;
    @Resource
    private LocalFileMapper localFileMapper;
    @Resource
    private PhoneBlackMapper phoneBlackMapper;
    @Resource
    private RabbitMqProducter producter;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(20, 80);

    @Override
    public ResponseShuheDTO insertShuheTransferData(String apiCode, String jsonData) {
        String msg = "";
        String title = "数禾转化数据定制化清洗入库";
        ResponseShuheDTO responseShuheDTO = new ResponseShuheDTO();
        try {
            final ShuheTransferJsonDTO jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
            }.getType());
            if (StringUtils.isEmpty(jsonDTO.getOrderId())) {
                msg += "orderId,释义：批量上传案件编号；";
            }
            if (StringUtils.isEmpty(jsonDTO.getMobile())) {
                msg += "mobile,释义：手机号；";
            }
            if (jsonDTO.getDataItem() == null || jsonDTO.getDataItem().size() < 1) {
                msg += "dataItem,释义：扩展字段；";
            }
            if (!"".equals(msg)) {
                responseShuheDTO.failed("抱歉,缺失必填参数！缺失参数为：".concat(msg));
                log.info("shuhe-1:{}", responseShuheDTO.getDesc());
                this.sendAlarmMgs(title, "缺失必填参数".concat(msg).concat("案件编号“").concat(jsonDTO.getOrderId())
                        .concat("”").concat("请及时跟进或与数禾客户及时沟通^_^"), appName, secretKey, alarmClient);
                return responseShuheDTO;
            }
            String userType = jsonDTO.getBizType();
            if (StringUtils.isEmpty(userType)) {
                /*
                 * 对bizType字段做兜底，对应营销userType,
                 * 当bizType未传时，需要主动去上传接口中查找，
                 * 如果未查到需要返回给客户提示信息，并将数据落库到本地
                 */
                userType = iMarketingSyncUserService.getUserTypeLatestByCustNum(apiCode, jsonDTO.getOrderId());
            }
            IUserType userTypeStrategy = UserTypeStrategyFactory.getUserTypeStrategy(userType);
            CaseShuheUserWithBLOBs caseShuheUser;
            if (userTypeStrategy instanceof UnknownUserType) {
                msg = "未知的业务类型\"" + userType + "\"!";
                caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(userTypeStrategy, jsonDTO, apiCode, jsonData);
                responseShuheDTO.failed("抱歉,".concat(msg));
                caseShuheUser.setErrorInfo(responseShuheDTO.getDesc());
                log.info("shuhe-2:{}", responseShuheDTO.getDesc());
                this.sendAlarmMgs(title, msg.concat("案件编号“").concat(jsonDTO.getOrderId()).concat("”")
                        .concat("请及时跟进或与数禾客户及时沟通^_^"), appName, secretKey, alarmClient);
            } else {
                caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(userTypeStrategy, jsonDTO, apiCode, jsonData);
                responseShuheDTO.success();
            }
            Integer row = null;
            Exception exception = null;
//            CaseShuheUserWithBLOBs finalCaseShuheUser = caseShuheUser;
//            final Future<Integer> futureInsert = BR_EXECUTORS.submit(() -> caseShuheUserMapper.insertSelective(finalCaseShuheUser));
//            try {
//                row = futureInsert.get(5, TimeUnit.SECONDS);
//            } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                log.error(e.getMessage(), e);
//                exception = e;
//            }
//            String taskId = iMarketingSyncUserService.getTaskIdLatestByCustNum(apiCode, caseShuheUser.getCustNum());
            MarketingTransferSyncUser transferSyncUser = new TransferSyncAdapter(caseShuheUser).transferSyncUserRequest();
            // D20220209数禾转化接口V3.0-客服
            if ("Y".equals(caseShuheUser.getIsBlack())) {
                // 黑名单逻辑
                // is_black 字段内容放入transferSyncUser表 reserveField1字段中
                try {
                    LocalFile localFile = new LocalFile();
                    localFile.setApiCode(apiCode);
                    localFile.setCreateTime(new Date());
                    localFile.setFileType("");
                    localFile.setFileName("数禾-转化");
                    localFileMapper.insertSelective(localFile);
                    PhoneBlack phoneBlack = new PhoneBlack();
                    phoneBlack.setLocalId(localFile.getId());
                    phoneBlack.setName("");
                    phoneBlack.setPhone(caseShuheUser.getCell());
                    phoneBlack.setExpiredate(transferSyncUser.getReserveField1());
                    phoneBlack.setCreateTime(new Date());
                    phoneBlack.setUpdateTime(new Date());
                    phoneBlackMapper.insertSelective(phoneBlack);
                    if (localFile.getId() != null && localFile.getId() > 0) {
                        producter.send(MQConstants.ROUTING_KEY_MARKETING_PUSH_BLACK, localFile.getId().toString());
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    this.sendAlarmMgs(title, "保存到黑名单失败" + e.getMessage(), appName, secretKey, alarmClient);

                }
            } else if ("Y".equals(caseShuheUser.getIsTurn()) || (
                    StringUtils.isEmpty(caseShuheUser.getIsBlack())
                            && StringUtils.isEmpty(caseShuheUser.getIsTurn())
                            && StringUtils.isEmpty(caseShuheUser.getUserType())
                            && StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim())
                            && StringUtils.isEmpty(caseShuheUser.getClcUsrFstLogTimAll())
                            && StringUtils.isEmpty(caseShuheUser.getClcUsrFrtFqOrdTim()))) {
                transferSyncUser.setIfTransform("2");
            } else if (
                    ("促申完".equals(caseShuheUser.getUserType()) && !StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim())) ||
                            ("促首登".equals(caseShuheUser.getUserType()) && !StringUtils.isEmpty(caseShuheUser.getClcUsrFstLogTimAll())) ||
                            ("促首借".equals(caseShuheUser.getUserType()) && !StringUtils.isEmpty(caseShuheUser.getClcUsrFrtFqOrdTim()))) {
                // 转化
                transferSyncUser.setIfTransform("1");
            }
            // D20220209数禾转化数据定制化清洗入库
            row = caseShuheUserMapper.insertSelective(caseShuheUser);
            if (row == null || row < 1) {
                msg = "数禾推送数据保存失败！";
                this.sendAlarmMgs(title, msg.concat("案件编号“").concat(jsonDTO.getOrderId()).concat("”")
                        .concat("尽快处理^_^"), appName, secretKey, alarmClient);
                log.error(msg);
                responseShuheDTO.failed("抱歉，".concat(msg));
                caseShuheUser = new CaseShuheUserWithBLOBs();
                caseShuheUser.setJsonData(jsonData);
                caseShuheUser.setCustNum(jsonDTO.getOrderId());
                caseShuheUser.setApiCode(apiCode);
                caseShuheUser.setMobile(jsonDTO.getMobile());
                caseShuheUser.setBiztype(jsonDTO.getBizType());
                if (exception != null) {
                    caseShuheUser.setErrorInfo(msg.concat("##:").concat(exception.toString()));
                } else {
                    caseShuheUser.setErrorInfo(msg);
                }
                caseShuheUserMapper.insertSelective(caseShuheUser);
            }

            // 转化信息入库
            try {
                transferSyncUser.setRequestId(Md5Utils.cell32(jsonData));
                int row_sync = iTransferSyncUserService.insertSelective(transferSyncUser);
                if (row_sync > 0) {
                    MarketingTransferInfo transferInfo = new MarketingTransferInfo();
                    transferInfo.setApiCode(apiCode);
                    transferInfo.setRequestId(transferInfo.getRequestId());
                    transferInfo.setCreateTime(new Date());
                    transferInfo.setJsonData(jsonData);
                    transferInfo.setActualNum(1);
                    int row_info = marketingTransferInfoMapper.insertSelective(transferInfo);
                    if (row_info > 0) {
                        producter.send(MQConstants.ROUTING_KEY_MARKETING_TRANSFER_PUSH_CUSTOMER, transferInfo.getId().toString());
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                this.sendAlarmMgs(title, "保存到转化信息失败" + e.getMessage(), appName, secretKey, alarmClient);
            }
            // D20220209数禾申完转电销
            if ("促申完".equals(caseShuheUser.getUserType())) {
                boolean boolAppStaTim;
                final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                /* 数禾申完转电销 情况a
                 * clc_usr_lst_app_sta_tim日期值为当天&clc_usr_iso_ato_tim日期不大于原始数据上传时间&userType=促申完
                 * &cusNun&有效期内
                 */
                if (StringUtils.isEmpty(caseShuheUser.getClcUsrLstAppStaTim())) {
                    boolAppStaTim = Boolean.FALSE;
                } else {
                    LocalDateTime appStaTim = LocalDateTime.parse(caseShuheUser.getClcUsrLstAppStaTim(), dateTimeFormatter);
                    LocalDate localDate = LocalDate.now();
                    LocalDate appStaDate = appStaTim.toLocalDate();
                    boolAppStaTim = localDate.isEqual(appStaDate);
                }
                if (boolAppStaTim) {
                    boolean boolIsoAtoTim;
                    if (StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim())) {
                        boolIsoAtoTim = Boolean.FALSE;
                    } else {
                        LocalDateTime isoAtoTim = LocalDateTime.parse(caseShuheUser.getClcUsrIsoAtoTim(), dateTimeFormatter);
                        Date appletTime = iMarketingSyncUserService.getAppletTimeByCustNumAndUserType(apiCode
                                , caseShuheUser.getCustNum(), caseShuheUser.getUserType());
                        if (appletTime == null) {
                            boolIsoAtoTim = Boolean.FALSE;
                        } else {
                            LocalDateTime appletDate = appletTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                            boolIsoAtoTim = isoAtoTim.isBefore(appletDate);
                        }
                    }
                    if (boolIsoAtoTim) {
                        // 校验有效期
                        String yyyyMMdd = "yyyy-MM-dd";
                        Boolean periodOfValidity = iMarketingSyncUserService.isPeriodOfValidity(apiCode
                                , caseShuheUser.getCustNum(), PeriodOfValidityDO.closInterval15Day(yyyyMMdd));
                        if (periodOfValidity) {
                            String key = "marketing:api:shuhe:transfer:cid:".concat(apiCode);
                            String cId;
                            try {
                                cId = redisChgService.get(key);
                                if (StringUtils.isEmpty(cId)) {
                                    cId = tableCreateService.getTcId(apiCode);
                                    // 缓存七天
                                    redisChgService.setex(key, cId, 7 * 86400);
                                }
                            } catch (Exception e) {
                                cId = tableCreateService.getTcId(apiCode);
                                log.error(e.getMessage(), e);
                            }
                            LocalFile localFile = new LocalFile();
                            PhoneSale phoneSale = new PhoneSale();
                            PhoneSaleExtendShuhe phoneSaleExtendShuhe = new PhoneSaleExtendShuhe();
                            localFile.setCid(cId);
                            localFile.setApiCode(apiCode);
                            localFile.setFileName("数禾-转化");
                            phoneSale.setUid(transferSyncUser.getCustNum());
                            //明文
                            phoneSale.setPhone(caseShuheUser.getMobile());
                            phoneSale.setName("");
                            phoneSale.setOrgname("shuheshenwan");
                            phoneSale.setSource("16");
                            phoneSale.setUserType("2");
                            phoneSale.setLoginTime("");
                            PropertyPreFilters filters = new PropertyPreFilters();
                            PropertyPreFilters.MySimplePropertyPreFilter includefilter = filters.addFilter();
                            includefilter.addIncludes("clc_usr_iso_pho_tim"
                                    , "clc_usr_iso_idt_tim"
                                    , "clc_usr_iso_crd_tim"
                                    , "clc_usr_iso_inf_tim"
                            );
                            String field1 = JSONObject.toJSONString(JSONObject.parseObject(jsonData), includefilter, SerializerFeature.WriteMapNullValue);
                            phoneSale.setExtend(field1);
                            phoneSaleExtendShuhe.setCustNum(transferSyncUser.getCustNum());
                            LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
                            LocalDate localDate = localDateTime.toLocalDate();
                            phoneSaleExtendShuhe.setAppletDate(localDate.format(DateTimeFormatter.ofPattern(yyyyMMdd)));
                            phoneSaleExtendShuhe.setAppletTime(localDateTime.format(dateTimeFormatter));
                            PushShDXDTO pushShDXDTO = new PushShDXDTO()
                                    .setLocalFile(localFile)
                                    .setPhoneSale(phoneSale)
                                    .setPhoneSaleExtendShuhe(phoneSaleExtendShuhe);
                            try {
                                Result<Boolean> booleanResult = pushDataService.pushShDX(pushShDXDTO);
                                if (booleanResult.getData()) {
                                    log.info("推送电销成功");
                                } else {
                                    msg = String.format("数禾(custNum=%s)推送电销（人工）失败！失败信息：%s", transferSyncUser.getCustNum(), booleanResult.getData());
                                    log.error(msg);
                                    this.sendAlarmMgs(title, msg, appName, secretKey, alarmClient);
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                this.sendAlarmMgs(title, "保存到电销失败" + e.getMessage(), appName, secretKey, alarmClient);

                            }
                        }
                    }
                }
            }
            return responseShuheDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            CaseShuheUserWithBLOBs User = new CaseShuheUserWithBLOBs();
            User.setJsonData(jsonData);
            User.setApiCode(apiCode);
            User.setErrorInfo(e.toString());
            try {
                caseShuheUserMapper.insertSelective(User);
            } catch (Exception exception) {
                log.error(exception.getMessage(), exception);
                this.sendAlarmMgs(title, exception.getMessage(), appName, secretKey, alarmClient);
            }
            this.sendAlarmMgs(title, e.getMessage(), appName, secretKey, alarmClient);
            return responseShuheDTO.failed();
        }
    }
}
