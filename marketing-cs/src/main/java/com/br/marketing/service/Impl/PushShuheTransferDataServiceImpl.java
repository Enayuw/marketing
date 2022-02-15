package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.adapter.TransferSyncAdapter;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.CaseShuheUserWithBLOBs;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheTransferDataService;
import com.br.marketing.service.ITransferSyncUserService;
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
            // TODO: 2022/2/11 处理 转化数据为 转化-客服？ 电销？ 黑名单？ 不做处理？ 明文电话需要加密保存到数据库
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
//            String taskId = iMarketingSyncUserService.getTaskIdLatestByCustNum(apiCode, caseShuheUser.getCustNum());
            MarketingTransferSyncUser transferSyncUser = new TransferSyncAdapter(caseShuheUser).transferSyncUserRequest();
            // D20220209数禾转化接口V3.0-客服
            if ("Y".equals(caseShuheUser.getIsBlack())) {
                // 黑名单
                // is_black 字段内容放入 reserveField1字段中
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
            // 转化信息入库
            int i = iTransferSyncUserService.insertSelective(transferSyncUser);
            // D20220209数禾申完转电销
            if ("促申完".equals(caseShuheUser.getUserType())) {
                boolean boolAppStaTim;
                boolean boolIsoAtoTim;
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
                if (StringUtils.isEmpty(caseShuheUser.getClcUsrIsoAtoTim())) {
                    boolIsoAtoTim = Boolean.FALSE;
                } else {
                    LocalDateTime isoAtoTim = LocalDateTime.parse(caseShuheUser.getClcUsrIsoAtoTim(), dateTimeFormatter);
                    LocalDate isoAtoDate = isoAtoTim.toLocalDate();
                    Date appletTime = iMarketingSyncUserService.getAppletTimeByCustNumAndUserType(apiCode
                            , caseShuheUser.getCustNum(), caseShuheUser.getUserType());
                    if (appletTime == null) {
                        boolIsoAtoTim = Boolean.FALSE;
                    } else {
                        LocalDate appletDate = appletTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                        boolIsoAtoTim = isoAtoDate.isBefore(appletDate) || isoAtoDate.isEqual(appletDate);
                    }
                }
                // 校验有效期
                if (boolAppStaTim & boolIsoAtoTim) {

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
