package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.adapter.transfer.TransferSyncAdapter;
import com.br.marketing.adapter.transfer.adaptee.CaseShuheUserAdaptee;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.exception.CommonException;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.shuhe.Response2ShuheDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CaseShuheUploadDataMapper;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.IPushShuheDataService;
import com.br.marketing.service.ITransferSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ShuHeAESencUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 数禾转化实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:25
 */
@Service
@Slf4j
public class PushShuheDataServiceImpl implements IPushShuheDataService {

    @Resource
    private CaseShuheUserMapper caseShuheUserMapper;
    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private ITransferSyncUserService iTransferSyncUserService;
    @Resource
    private RedisChgService redisChgService;
    @Resource
    private RabbitMqProducter producter;
    @Resource
    private TableCreateServiceImpl tableCreateService;
    @Resource
    private CaseShuheUploadDataMapper caseShuheUploadDataMapper;
    @Resource
    private MarketingUserMapper marketingUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    private static final ThreadPoolExecutor BR_EXECUTORS = BrExecutors.getThreadPool(1, 2);
    private final String title = "数禾转化数据定制化清洗入库";
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter yyMMddHH = DateTimeFormatter.ofPattern("yyMMdd");
    private static final Set<String> FIELD_SET = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Autowired
    ShuHeUserServiceImpl shuHeUserService;

    static {
        // D20220824数禾定制版上传接口改造一期 初始化字段 2022-9-1 16:49:53
        FIELD_SET.addAll(Arrays.asList(
                "listInfo"
                , "templateCode"
                , "extraInfo"
                , "templateName"
                , "outboundFrequency"
                , "operatingCycle"
                , "mobile"
                , "orderId"
                , "bizId"
                , "varData"
                , "bizType"
                , "name"
                , "identificationNo"
                , "clc_usr_adt_tim_rcn_lon"
                , "clc_usr_adt_lmt_fst_all"
                , "clc_usr_adt_lmt_lv0"
                , "clc_usr_hvy_max_3_avl_lmt"
                , "clc_usr_lst_app_sta_tim"
                , "clc_usr_lst_non_dcp_trs_tim"
                , "clc_usr_new_adt_rat_btr"
                , "clc_usr_new_adt_rat_csh"
                , "clc_usr_new_adt_rat_hgl"
                , "off_usr_last_adjlmt_add_lmt"
                , "off_usr_lsh_out_day_flg"
                , "off_usr_lst_adj_lmt_tim_micro_all"
                , "off_usr_lst_ord_tim_all"
        ));
    }


    @Override
    public ResponseCustomDTO saveShuheTransferData(String apiCode, String jsonData) {
        ResponseShuheDTO responseShuheDTO = new ResponseShuheDTO();
        responseShuheDTO.success();
        ShuheTransferJsonDTO jsonDTO = null;
        String requestId = "";
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
            }.getType());
            // 1、校验参数合法性
            String msg = nonNullCheck(jsonDTO);
            if (!"".equals(msg)) {
                responseShuheDTO.failed("抱歉,缺失必填参数！缺失参数为：".concat(msg));
                msg = "缺失必填参数:".concat(msg).concat("\napiCode“" + apiCode).concat("”\nuserType“"
                        + jsonDTO.getBizType()).concat("”\n案件编号“" + jsonDTO.getOrderId())
                        .concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^");
                this.sendAlarmMgsUrgent(title, msg, alarmClient);
                return responseShuheDTO;
            }
            String userType = jsonDTO.getBizType();
            // 2、判断场景类型
            if (StringUtils.isEmpty(userType)) {
                /*
                 * 对bizType字段做兜底，对应营销userType,
                 * 当bizType未传时，需要主动去上传接口中查找，
                 * 如果未查到需要返回给客户提示信息，并将数据落库到本地
                 */
                userType = iMarketingSyncUserService.getUserTypeLatestByCustNum(apiCode, jsonDTO.getOrderId());
            }
            final IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(userType);
            CaseShuheUser caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(iUserType
                    , jsonDTO, apiCode, jsonData);
            boolean sendToQueueBool = iUserType instanceof UnknownUserType;
            if (sendToQueueBool) {
                caseShuheUser.setStatus(1);
                msg = "未知的业务类型\"" + userType + "\"!";
                responseShuheDTO.failed("抱歉,".concat(msg));
                caseShuheUser.setErrorInfo("#1" + responseShuheDTO.getDesc());
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                                .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^")
                                ,alarmClient);
            } else if (!iUserType.getApiCodes().contains(apiCode)) {
                log.warn("场景(".concat(iUserType.getApiCodes().toString()).concat(")与对应apiCode不匹配\n")
                        .concat(userType).concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                        .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^"));
            }
            // 3、查询db获取相应TaskId
            String taskId = iMarketingSyncUserService.getTaskIdLatestByCustNum(apiCode, jsonDTO.getOrderId(), userType);
            if (taskId == null) {
                taskId = "";
            }
            // 4、客户转化数据适配标准转化数据
            MarketingTransferSyncUser transferSyncUser = new TransferSyncAdapter(
                    (CaseShuheUserAdaptee) caseShuheUser).transferSyncUserRequest(taskId, jsonDTO);
            SecureRandom random = new SecureRandom();
            requestId = Md5Utils.cell32(caseShuheUser.getJsonData()
                    .concat("@" + System.currentTimeMillis()).concat("#" + random.nextInt(10000)));
            caseShuheUser.setReserveField2(requestId);
            transferSyncUser.setRequestId(requestId);
            // 5、数据落前置库
            try {
                int row = caseShuheUserMapper.insertSelective(caseShuheUser);
                if (row < 1) {
                    throw new Exception("'b_case_shuhe_user'入库失败!影响的记录数：" + row);
                }
            } catch (Exception e) {
                msg = "数禾推送数据前置表保存失败！".concat("\napiCode“").concat(apiCode).concat("”\nuserType“")
                        .concat(userType).concat("”\n案件编号“").concat(jsonDTO.getOrderId()).concat("”\n")
                        .concat("”\nrequestId“").concat(requestId).concat("”\n");
                this.sendAlarmMgsUrgent(title, msg, alarmClient);
                log.error(msg.concat("”\njsondata:").concat(jsonData).concat("\n").concat("" + e.getMessage()), e);
                faultTolerantInsert(jsonDTO, jsonData, apiCode, msg);
                responseShuheDTO.failed("抱歉，保存失败");
            }
            // 6、转化信息入转化标准库
            saveTransferNew(apiCode, caseShuheUser, transferSyncUser, !sendToQueueBool);
            if (caseShuheUser.getSaveStatus() != null) {
                updateCaseShuhe(caseShuheUser);
            }
            return responseShuheDTO;
        } catch (Exception e) {
            log.error(String.format("shuhe_error apicode:%s;jsondata:%s;requestId:%s"
                    , apiCode, jsonData, requestId));
            log.error(e.getMessage(), e);
            exceptionSave(jsonDTO, jsonData, apiCode, e);
            return responseShuheDTO.failed();
        }
    }

    private String nonNullCheck(ShuheTransferJsonDTO jsonDTO) {
        String msg = "";
        if (StringUtils.isEmpty(jsonDTO.getOrderId())) {
            msg += "orderId,释义：批量上传案件编号；";
        }
        if (StringUtils.isEmpty(jsonDTO.getMobile())) {
            msg += "mobile,释义：手机号；";
        }
        if (jsonDTO.getDataItem() == null || jsonDTO.getDataItem().size() < 1) {
            msg += "dataItem,释义：扩展字段；";
        }
        return msg;
    }

    /**
     * 异常保存，只保存必要字段
     */
    private void exceptionSave(ShuheTransferJsonDTO jsonDTO, String jsonData
            , String apiCode, Exception e) {
        CaseShuheUser user = new CaseShuheUser();
        user.setJsonData(jsonData);
        user.setApiCode(apiCode);
        user.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        user.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        user.setErrorInfo("#5" + e.getMessage());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setStatus(2);
        if (e instanceof SQLException) {
            user.setSaveStatus(1);
        }
        if (jsonDTO != null) {
            user.setMobile(jsonDTO.getMobile());
            user.setBiztype(jsonDTO.getBizType());
            user.setCustNum(jsonDTO.getOrderId());
        }
        try {
            caseShuheUserMapper.insertSelective(user);
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
            this.sendAlarmMgs(title, ("apiCode“").concat(apiCode)
                    .concat("”\n").concat(exception.toString()),alarmClient);
        }
        this.sendAlarmMgs(title, ("apiCode“").concat(apiCode)
                .concat("”\n").concat(e.toString()),alarmClient);
    }

    /**
     * 保存失败后容错保存，只保存必要字段
     */
    private void faultTolerantInsert(ShuheTransferJsonDTO jsonDTO, String jsonData
            , String apiCode, String msg) {
        try {
            CaseShuheUser caseShuheUser = new CaseShuheUser();
            caseShuheUser.setSaveStatus(1);
            caseShuheUser.setJsonData(jsonData);
            caseShuheUser.setCustNum(jsonDTO.getOrderId());
            caseShuheUser.setApiCode(apiCode);
            caseShuheUser.setMobile(jsonDTO.getMobile());
            caseShuheUser.setBiztype(jsonDTO.getBizType());
            caseShuheUser.setErrorInfo("#4" + caseShuheUser.getErrorInfo() + (";").concat(msg));
            caseShuheUser.setIsTransfer(0);
            caseShuheUser.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
            caseShuheUser.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            caseShuheUserMapper.insertSelective(caseShuheUser);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void setCid(MarketingTransferSyncUser transferSyncUser) {
        String key = "marketing:api:shuhe:transfer:cid:".concat(transferSyncUser.getApiCode());
        String cId = "";
        try {
            cId = redisChgService.get(key);
            if (StringUtils.isEmpty(cId)) {
                cId = tableCreateService.getCId(transferSyncUser.getApiCode());
                if (StringUtils.isEmpty(cId)) {
                    return;
                }
                // 缓存七天
                redisChgService.setex(key, cId, 7 * 86400);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        transferSyncUser.setCid(cId);
        transferSyncUser.settCid(cId.replaceFirst("-", ""));
    }


    private void updateCaseShuhe(CaseShuheUser caseShuheUser) {
        BR_EXECUTORS.execute(() -> {
            CaseShuheUser csu = new CaseShuheUser();
            csu.setId(caseShuheUser.getId());
            csu.setSaveStatus(caseShuheUser.getSaveStatus());
            csu.setErrorInfo(caseShuheUser.getErrorInfo());
            csu.setUpdateTime(new Date());
            caseShuheUserMapper.updateByPrimaryKeySelective(csu);
        });
    }

    /**
     * 去转化新方法 适应框架
     */
    private void saveTransferNew(String apiCode, CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser, boolean sendToQueueBool) {
        MarketingTransferInfo transferInfo = new MarketingTransferInfo();
        this.setCid(transferSyncUser);
        if (StringUtils.isEmpty(transferSyncUser.gettCid())) {
            this.sendAlarmMgs(title, "数禾客户信息未维护...".concat("\napiCode“").concat(caseShuheUser.getApiCode())
                    .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                    .concat("”\n案件编号“").concat(caseShuheUser.getCustNum())
                    .concat("”\n").concat("尽快处理^_^"),alarmClient);
            return;
        }
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        transferSyncUser.setInsertTime(localDateTime.format(dateTimeFormatter));
        transferSyncUser.setRequestData(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        transferSyncUser.setRequestTime(transferSyncUser.getInsertTime());
        transferInfo.setApiCode(apiCode);
        transferInfo.setRequestId(transferSyncUser.getRequestId());
        transferInfo.setCreateTime(new Date());
        transferInfo.setJsonData(JSONObject.toJSONString(transferSyncUser));
        transferInfo.setActualNum(1);
        try {
            iTransferSyncUserService.insertInfoAndSync(transferSyncUser, transferInfo, caseShuheUser);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (caseShuheUser.getSaveStatus() == null) {
                caseShuheUser.setSaveStatus(2);
            }
            caseShuheUser.setErrorInfo(StringUtils.isEmpty(caseShuheUser.getErrorInfo()) ? e.getMessage()
                    : caseShuheUser.getErrorInfo() + "\n" + e.getMessage());
            alarmMgs(caseShuheUser);
            return;
        }
        try {
            List<String> universalProcessApiCode = marketingCommonConfig.getUniversalProcessApiCode();
            if (sendToQueueBool && universalProcessApiCode.contains(apiCode)) {
                final MqFact mqFact = new MqFact();
                mqFact.setSourceId(transferInfo.getId());
                mqFact.setSource(TransferSource.UNIVERSAL_TRANSFER_PROCESS.getCode());
                producter.sendToUniversalTransferQueue(mqFact);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // 4-发送mq失败
            caseShuheUser.setSaveStatus(4);
            String m = "发送mq失败,失败原因：" + e.getMessage();
            caseShuheUser.setErrorInfo(StringUtils.isEmpty(caseShuheUser.getErrorInfo())
                    ? m : (caseShuheUser.getErrorInfo() + "\n" + m));
        }
    }


    private void alarmMgs(CaseShuheUser caseShuheUser) {
        this.sendAlarmMgsUrgent(title, ("apiCode“").concat(caseShuheUser.getApiCode())
                        .concat("”\nuserType“").concat(caseShuheUser.getUserType())
                        .concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n")
                        .concat(caseShuheUser.getErrorInfo())
                        ,alarmClient);
    }

    @Override
    public ResponseCustomDTO saveUploadData(String apiCode, String jsonData) {
        String requestId = buildRequestId(apiCode);
        CaseShuheUploadData shuheUploadData = new CaseShuheUploadData();
        shuheUploadData.setJsonData(jsonData);
        shuheUploadData.setUploadDate(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        shuheUploadData.setCreateTime(new Date());
        shuheUploadData.setUpdateTime(shuheUploadData.getCreateTime());
        shuheUploadData.setApiCode(apiCode);
        shuheUploadData.setRequestId(requestId);
        Response2ShuheDTO response2ShuheDTO = new Response2ShuheDTO();
        response2ShuheDTO.setMsgId(shuheUploadData.getRequestId());
        if (org.apache.commons.lang3.StringUtils.isBlank(jsonData)) {
            response2ShuheDTO.failed(",内容不可为空");
            exceptionSave(shuheUploadData, response2ShuheDTO, null);
            return response2ShuheDTO;
        }
        final JSONObject uploadDataDTO;
        try {
            uploadDataDTO = JSONObject.parseObject(jsonData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            response2ShuheDTO.failed(",JSON结构解析失败");
            exceptionSave(shuheUploadData, response2ShuheDTO, e);
            return response2ShuheDTO;
        }
        final JSONArray listInfo = uploadDataDTO.getJSONArray("listInfo");
        if (requiredCheck(listInfo, response2ShuheDTO)) {
            exceptionSave(shuheUploadData, response2ShuheDTO, null);
            return response2ShuheDTO;
        }
        if (uploadDataDTO.containsKey("extraInfo")) {
            String userType = uploadDataDTO.getString("extraInfo");
            shuheUploadData.setUserType(StringUtils.isEmpty(userType) ? "" : userType);
        }
        Long infoId = null;
        try {
            infoId = shuHeUserService.saveShUploadData(shuheUploadData, uploadDataDTO, listInfo);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            ProductPulsarProducer producer = null;
            try {
                producer = ProductPulsarClientManager.newProducer(PulsarTopic.upLoadShTopic);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("apiCode",apiCode);
                jsonObject.put("requestId",requestId);
                jsonObject.put("jsonData",jsonData);
                byte[] message = jsonObject.toJSONString().getBytes();
                producer.send(message);
            } catch (PulsarClientException e) {
                response2ShuheDTO.failed(",内部错误");
                return response2ShuheDTO;
            }
        }
        if(infoId!=null){
            producter.send(MQConstants.ROUTING_KEY_MARKETING_PRE_USER_SHUHERECEIVE, infoId.toString());
        }
        BR_EXECUTORS.execute(() -> checkField(uploadDataDTO, listInfo));
        return response2ShuheDTO.success();
    }


    @Override
    public Result<Boolean> consumerShUpload(String msg) {

        return null;
    }

    /**
     * 2022/9/1 11:46
     * 适配标准上传接口结构
     */
    private MarketingPreUserDTO adapterMarketingPreUserDTO(JSONObject uploadDataDTO, JSONArray listInfo
            , CaseShuheUploadData shuheUploadData) {
        try {
            MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
            userDTO.setTaskId(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
            userDTO.setRequestId(shuheUploadData.getRequestId());
            userDTO.setLast("0");
            userDTO.setTotal("0");
            List<MarketingPreUserDetailDTO> list = new ArrayList<>();
            MarketingPreUserDetailDTO dto;
            String type = shuheUploadData.getUserType();
            JSONObject varData;
            Map<String, Object> reserveField1;
            int size = listInfo.size();
            for (int i = 0; i < size; i++) {
                JSONObject info = listInfo.getJSONObject(i);
                reserveField1 = new HashMap<>(32);
                dto = new MarketingPreUserDetailDTO();
                String mobile = info.getString("mobile");
                try {
                    dto.setCell(org.apache.commons.lang3.StringUtils.isNotBlank(mobile)
                            ? ShuHeAESencUtil.decrypt(mobile) : mobile);
                } catch (Exception e) {
                    dto.setCell(mobile);
                    log.error(e.getMessage(), e);
                }
                dto.setGroupType(type);
                dto.setCustNum(info.getString("orderId"));
                varData = info.getJSONObject("varData");
                if (!CollectionUtils.isEmpty(varData)) {
                    String keyId = "identificationNo";
                    String keyName = "name";
                    if (varData.containsKey(keyId)) {
                        dto.setId(varData.getString(keyId));
                        varData.remove(keyId);
                    }
                    if (varData.containsKey(keyName)) {
                        dto.setName(varData.getString(keyName));
                        varData.remove(keyName);
                    }
                    reserveField1.putAll(varData);
                }
                reserveField1.putAll(info);
                reserveField1.putAll(uploadDataDTO);
                reserveField1.remove("listInfo");
                reserveField1.remove("mobile");
                reserveField1.remove("varData");
                reserveField1.remove("orderId");
                reserveField1.remove("extraInfo");
                dto.setReserveField1(JSON.toJSONString(reserveField1, SerializerFeature.WriteNullStringAsEmpty
                        , SerializerFeature.WriteNullListAsEmpty));
                list.add(dto);
            }
            userDTO.setDataItems(list);
            return userDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 2022/9/1 11:45
     * 保存到info表
     */
    private void saveSyncInfo(MarketingPreUserDTO userDTO, CaseShuheUploadData shuheUploadData) {
        if (ObjectUtils.isEmpty(userDTO)) {
            return;
        }
        MarketingSyncInfo syncInfo = new MarketingSyncInfo();
        CaseShuheUploadData record = new CaseShuheUploadData();
        record.setId(shuheUploadData.getId());
        record.setRequestId(shuheUploadData.getRequestId());
        syncInfo.setApiCode(shuheUploadData.getApiCode());
        syncInfo.setCusBatch(userDTO.getTaskId());
        syncInfo.setRequestBatch(userDTO.getRequestId());
        syncInfo.setLast((byte) 0);
        syncInfo.setTotal(0L);
        syncInfo.setCreateTime(new Date());
        syncInfo.setActualNum(userDTO.getDataItems().size());
        try {
            syncInfo.setJsonData(JSON.toJSONString(userDTO, SerializerFeature.WriteNullStringAsEmpty
                    , SerializerFeature.WriteNullListAsEmpty));
            int i = marketingUserMapper.insertMarketingPreUserByText(syncInfo);
            if (i == 1 && syncInfo.getId() != null) {
                try {
                    producter.send(MQConstants.ROUTING_KEY_MARKETING_PRE_USER_SHUHERECEIVE, syncInfo.getId().toString());
                } catch (Exception e) {
                    record.setSaveInfoStatus(2);
                    log.error(e.getMessage(), e);
                }
            } else {
                record.setSaveInfoStatus(1);
            }
        } catch (Exception e) {
            record.setSaveInfoStatus(1);
            log.error(e.getMessage(), e);
        } finally {
            try {
                int u = caseShuheUploadDataMapper.updateByPrimaryKeySelective(record);
                if (u != 1) {
                    String mgs = "数禾上传数据前置表更新信息失败";
                    BusinessException exception = new BusinessException(mgs);
                    exception.setExceptionMessage(mgs);
                    throw exception;
                }
            } catch (Exception e) {
                log.error(e.getMessage()
                        + "\n前置表Id:" + shuheUploadData.getId()
                        + "\nrequestId:" + shuheUploadData.getRequestId()
                        + "\napiCode:" + shuheUploadData.getApiCode()
                        + "\njsonData:" + shuheUploadData.getJsonData(), e);
            }
        }
    }

    /**
     * 2022/9/1 9:06
     * 异常保存
     */
    private void exceptionSave(CaseShuheUploadData shuheUploadData, Response2ShuheDTO response2ShuheDTO, Exception e) {
        shuheUploadData.setErrorInfo(response2ShuheDTO.getDesc() + (e == null ? "" : "\n" + e.getMessage()));
        shuheUploadData.setStatus(1);
        shuheUploadData.setSaveInfoStatus(1);
        try {
            int i = caseShuheUploadDataMapper.insertSelective(shuheUploadData);
            if (i == 1) {
                response2ShuheDTO.setMsgId(serialNumberAddId(shuheUploadData));
            } else {
                String mgs = "数禾上传数据异常数据入库失败";
                BusinessException exception = new BusinessException(mgs);
                exception.setExceptionMessage(mgs);
                throw exception;
            }
        } catch (Exception exception) {
            log.error(exception.getMessage()
                    + "\nrequestId:" + shuheUploadData.getRequestId()
                    + "\napiCode:" + shuheUploadData.getApiCode()
                    + "\njsonData:" + shuheUploadData.getJsonData(), exception);
        }
    }

    /**
     * 2022/9/1 10:45
     * 必填检查
     */
    private boolean requiredCheck(JSONArray listInfo, Response2ShuheDTO response2ShuheDTO) {
        if (CollectionUtils.isEmpty(listInfo)) {
            try {
                response2ShuheDTO.failed(",名单列表内容为空");
                BusinessException exception = new BusinessException(response2ShuheDTO.getDesc());
                exception.setExceptionMessage(response2ShuheDTO.getDesc());
                throw exception;
            } catch (BusinessException e) {
                log.error(e.getMessage(), e);
                return true;
            }
        }
        return false;
    }

    /**
     * 2022/9/1 10:45
     * 新增字段检查
     */
    private void checkField(JSONObject uploadDataDTO, JSONArray listInfo) {
        try {
            Set<String> keySet = new HashSet<>(uploadDataDTO.keySet());
            StringBuilder fieldStr = new StringBuilder();
            int size = listInfo.size();
            for (int i = 0; i < size; i++) {
                JSONObject info = listInfo.getJSONObject(i);
                keySet.addAll(info.keySet());
                JSONObject varData = info.getJSONObject("varData");
                if (varData != null) {
                    keySet.addAll(varData.keySet());
                }
            }
            String separator = "、";
            for (String key : keySet) {
                if (FIELD_SET.add(key)) {
                    Long aLong = redisChgService.saddMember(RedisKeyConstant.shuHeUploadDataFieldKey, key);
                    if (aLong == 1) {
                        fieldStr.append(fieldStr.length() > 0 ? separator : "\n").append(key);
                    }
                }
            }
            if (fieldStr.length() > 0) {
                alarmClient.sendAlarm("本次请求发现新增字段："
                                .concat(fieldStr.toString())
                                .concat("\n请及时与客户沟通确认^_^"), "数禾上传数据接口字段新增检查",
                                AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
                Long rSum = redisChgService.scard(RedisKeyConstant.shuHeUploadDataFieldKey);
                if (rSum == null || rSum < FIELD_SET.size()) {
                    redisChgService.sadd(RedisKeyConstant.shuHeUploadDataFieldKey, new ArrayList<>(FIELD_SET));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    /**
     * 2022/8/30 16:24
     * 流水号生成规则：
     * 1.年取倒数2位+月（两位）+日（两位）+apicode取倒数4位+纳秒倒数3~8位（共6位）+3位随机数
     * 2.流水线长：2+2+2+4+6+3
     * eg:
     * 2208300004377960345
     */
    private String getSerialNumber(String apiCode) {
        SecureRandom random = new SecureRandom();
        int length = apiCode.length();
        return LocalDateTime.now().format(yyMMddHH)
                + apiCode.substring(length - 4, length)
                + (String.format(("%06d"), (System.nanoTime() / 100 % 1000000)))
                + (random.nextInt(900) + 100);
    }

    /**
     * 数禾上传接口创建requestID
     * @param apiCode
     * 日期（6，年后2+月2+日2）_apiCode（7）_毫秒（13）_随机数（6）
     * @return
     */
    private String buildRequestId(String apiCode) {
        SecureRandom random = new SecureRandom();
        return LocalDateTime.now().format(yyMMddHH)
                .concat("_"+apiCode)
                .concat("_"+System.currentTimeMillis())
                .concat("_"+random.nextInt(999999));
    }


    /**
     * 2022/8/30 18:04
     * 业务流水流水号生成规则：
     * 1.流水号+数据库id
     * 2.业务流水号总长：19+id长度，如使用bigint类型，则：19+19=38
     * eg:
     * 2208300004611020223123456 id为123456
     */
    private String serialNumberAddId(CaseShuheUploadData data) {
        return data.getRequestId() + (data.getId() > 0 ? data.getId() : "");
    }
}
