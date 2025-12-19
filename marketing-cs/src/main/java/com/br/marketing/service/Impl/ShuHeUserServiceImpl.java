package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.adapter.transfer.TransferSyncAdapter;
import com.br.marketing.adapter.transfer.adaptee.CaseShuheUserAdaptee;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.constants.rocketmq.MarketingTransferSmallConstants;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.BaseUserType;
import com.br.marketing.entity.*;
import com.br.marketing.enums.CustomerQueueEnum;
import com.br.marketing.enums.clean.DataSourceTypeEnum;
import com.br.marketing.handle.SnowflakeRedisGeneratorHandle;
import com.br.marketing.mapper.CaseShuheUploadDataMapper;
import com.br.marketing.mapper.CaseShuheUserMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ShuHeAESencUtil;
import com.google.api.client.util.Lists;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class ShuHeUserServiceImpl {

    @Resource
    CaseShuheUploadDataMapper caseShuheUploadDataMapper;

    @Resource
    private MarketingUserMapper marketingUserMapper;

    @Autowired
    IMarketingSyncUserService iMarketingSyncUserService;

    @Resource
    MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private AlarmApiClient alarmClient;

    @Resource
    CaseShuheUserMapper caseShuheUserMapper;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    PushRuleService pushRuleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Resource
    private SnowflakeRedisGeneratorHandle snowflakeRedisGeneratorHandle;

    @Resource
    private TrackingService trackingService;

    @Transactional(rollbackFor = Exception.class)
    public Long saveShUploadData(CaseShuheUploadData shuheUploadData, JSONObject uploadDataDTO, JSONArray listInfo) {
        // todo 模拟异常上线后要删除
        pushRuleService.mockDbOrRedisError(1, shuheUploadData.getApiCode());
        caseShuheUploadDataMapper.insertSelective(shuheUploadData);
        return saveSyncInfo(adapterMarketingPreUserDTO(uploadDataDTO, listInfo, shuheUploadData), shuheUploadData);

    }

    /**
     * 2023-12-21 17:10 数禾上传数据适配上传数据
     */
    private MarketingPreUserDTO adapterMarketingPreUserDTO(JSONObject uploadDataDTO, JSONArray listInfo, CaseShuheUploadData shuheUploadData) {
        try {
            JSONObject taskCode = JSONObject.parseObject(uploadDataDTO.getString("taskCode"));
            MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
            userDTO.setTaskId(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE).concat("_").concat(shuheUploadData.getApiCode()));
            userDTO.setRequestId(shuheUploadData.getRequestId());
            userDTO.setLast("0");
            userDTO.setTotal("0");
            List<MarketingPreUserDetailDTO> list = new ArrayList<>();
            MarketingPreUserDetailDTO dto;
            String type = shuheUploadData.getUserType();
            String groupType = null;
            boolean isGroupType = taskCode != null && (groupType = taskCode.getString("groupType")) != null;
            JSONObject varData;
            Map<String, Object> reserveField1;
            int size = listInfo.size();
            for (int i = 0; i < size; i++) {
                JSONObject info = listInfo.getJSONObject(i);
                reserveField1 = new HashMap<>(32);
                dto = new MarketingPreUserDetailDTO();
                if (isGroupType) {
                    reserveField1.put("groupTypeNew", groupType);
                }
                String mobile = info.getString("mobile");
                try {
                    dto.setCell(org.apache.commons.lang3.StringUtils.isNotBlank(mobile) ? ShuHeAESencUtil.decrypt(mobile) : mobile);
                } catch (Exception e) {
                    dto.setCell(mobile);
                    log.error(e.getMessage(), e);
                }
                dto.setGroupType(type);
                dto.setCustNum(info.getString("orderId"));
                varData = info.getJSONObject("varData");
                varDataHandle(varData, dto, reserveField1);
                reserveField1.putAll(info);
                reserveField1.putAll(uploadDataDTO);
                reserveField1.remove("listInfo");
                reserveField1.remove("mobile");
                reserveField1.remove("varData");
                reserveField1.remove("orderId");
                dto.setReserveField1(
                        JSON.toJSONString(reserveField1, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty));
                try {
                    dto.setFingerprint(snowflakeRedisGeneratorHandle.nextId());
                } catch (Exception e) {
                    log.error("上传数据ID生成失败,{}", e.getMessage(), e);
                }
                list.add(dto);
            }
            userDTO.setDataItems(list);
            return userDTO;
        } catch (Exception e) {
            String smg = String.format("数禾上传数据封装对象报错：%s", e.getMessage());
            log.error(smg, e);
            CaseShuheUploadData record = new CaseShuheUploadData();
            record.setId(shuheUploadData.getId());
            record.setStatus(1);
            record.setSaveInfoStatus(1);
            record.setUpdateTime(new Date());
            record.setErrorInfo(smg);
            caseShuheUploadDataMapper.updateByPrimaryKeySelective(record);
        }
        return null;
    }

    /**
     * 2023-12-25 22:27 处理业务字段
     *
     * @param varData       客户业务字段
     * @param dto           百融业务字段
     * @param reserveField1 百融扩展字段
     */
    private void varDataHandle(JSONObject varData, MarketingPreUserDetailDTO dto, Map<String, Object> reserveField1) {
        if (!CollectionUtils.isEmpty(varData)) {
            String keyId = "identificationNo";
            String keyName = "name";
            String keyCusName = "cus_name";
            String keySex = "sex";
            String keyIdNew = "idt_no";
            if (varData.containsKey(keyIdNew)) {
                dto.setId(varData.getString(keyIdNew));
                varData.remove(keyIdNew);
            } else if (varData.containsKey(keyId)) {
                dto.setId(varData.getString(keyId));
                varData.remove(keyId);
            }
            if (varData.containsKey(keyCusName)) {
                dto.setName(varData.getString(keyCusName));
                varData.remove(keyCusName);
            } else if (varData.containsKey(keyName)) {
                dto.setName(varData.getString(keyName));
                varData.remove(keyName);
            }
            if (varData.containsKey(keySex)) {
                String sex = varData.getString(keySex);
                if ("男".equals(sex)) {
                    reserveField1.put("gender", "1");
                } else {
                    reserveField1.put("gender", "女".equals(sex) ? "2" : sex);
                }
                varData.remove(keySex);
            }
            reserveField1.putAll(varData);
        }
    }

    private Long saveSyncInfo(MarketingPreUserDTO userDTO, CaseShuheUploadData shuheUploadData) {
        if (ObjectUtils.isEmpty(userDTO)) {
            return null;
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
        syncInfo.setCreateTime(shuheUploadData.getCreateTime());
        syncInfo.setUpdateTime(shuheUploadData.getCreateTime());
        syncInfo.setActualNum(userDTO.getDataItems().size());
        syncInfo.setJsonData(JSON.toJSONString(userDTO, SerializerFeature.WriteNullStringAsEmpty, SerializerFeature.WriteNullListAsEmpty));
        //数禾-赋值datasourceType
        syncInfo.setDataSourceType(DataSourceTypeEnum.GENERAL_INTERFACE.getCode());
        marketingUserMapper.insertMarketingPreUserByText(syncInfo);
        caseShuheUploadDataMapper.updateByPrimaryKeySelective(record);
        return syncInfo.getId();
    }

    public void saveShTransferData(String apiCode, String jsonData, String requestId, Date createTime) {
        String msg = "";
        ShuheTransferJsonDTO jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
        }.getType());
        String userType = jsonDTO.getBizType();
        // todo 模拟异常上线后要删除
        pushRuleService.mockDbOrRedisError(1, apiCode);
        CaseShuheUser caseShuheUser;
        // 2、判断场景类型
        if (StringUtils.isEmpty(userType)) {
            /*
             * 对bizType字段做兜底，对应营销userType,
             * 当bizType未传时，需要主动去上传接口中查找，
             * 如果未查到需要返回给客户提示信息，并将数据落库到本地
             */
            userType = iMarketingSyncUserService.getUserTypeLatestByCustNum(apiCode, jsonDTO.getOrderId());
            if (userType == null) {
                userType = jsonDTO.getBizType();
            }
        }
        if (marketingCommonConfig.getShuheDxApiCodes().contains(apiCode)) {
            boolean empty = StringUtils.isEmpty(userType);
            caseShuheUser = assembleShuheDxUser(jsonDTO, apiCode, jsonData);
            caseShuheUser.setUserType(userType);
            if (empty) {
                msg = "不存在的业务类型电销转化数据，不会触发后续业务流程!";
                this.sendAlarmMgs("数禾电销全场景数据定制化清洗入库", msg.concat("\napiCode“").concat(apiCode).concat("”\n案件编号“").concat(jsonDTO.getOrderId())
                        .concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^"), alarmClient);
            }
        } else {
            final BaseUserType baseUserType = UserTypeStrategyFactory.getUserTypeStrategy(userType);
            caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(baseUserType, jsonDTO, apiCode, jsonData);
            if (!baseUserType.getApiCodes().contains(apiCode)) {
                log.warn("场景(".concat(baseUserType.getApiCodes().toString()).concat(")与对应apiCode不匹配\n").concat(userType).concat("\napiCode“")
                        .concat(apiCode).concat("”\n案件编号“").concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^"));
            }
        }
        // 3、查询db获取相应TaskId
        String taskId = iMarketingSyncUserService.getTaskIdLatestByCustNum(apiCode, jsonDTO.getOrderId(), userType);
        if (taskId == null) {
            taskId = "";
        }
        // 4、客户转化数据适配标准转化数据
        MarketingTransferSyncUser transferSyncUser =
                new TransferSyncAdapter((CaseShuheUserAdaptee) caseShuheUser).transferSyncUserRequest(taskId, jsonDTO);
        caseShuheUser.setReserveField2(requestId);
        transferSyncUser.setRequestId(requestId);
        // 5、数据落前置库
        if (createTime != null) {
            caseShuheUser.setCreateTime(createTime);
        }
        caseShuheUserMapper.insertSelective(caseShuheUser);
        saveShuheTransferInfo(apiCode, caseShuheUser, transferSyncUser);

        //region 埋点
        try {
            JSONObject condition = new JSONObject();
            condition.put("request_id", transferSyncUser.getRequestId());
            trackingService.trackBusinessLog(DataFlowDirection.IN
                    , apiCode
                    , "数禾定制转化接口上传数据"
                    , "b_case_shuhe_user"
                    , JSON.toJSONString(condition)
                    , 1L
                    , TrackingContext.generateBatchId());
        } catch (Exception ex) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.TRACKING_POINT_SERVICEERROR.getCode()
                            , ex.getMessage()
                            , "埋点异常")
                    , ex);
        }
        //endregion
    }

    private void saveShuheTransferInfo(String apiCode, CaseShuheUser caseShuheUser, MarketingTransferSyncUser transferSyncUser) {
        // 6、转化信息入转化标准库
        try {
            List<TransferDataItemDTO> dataItems = Lists.newArrayList();
            TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
            BeanUtils.copyProperties(transferSyncUser, transferDataItemDTO);
            try {
                transferDataItemDTO.setFingerprint(snowflakeRedisGeneratorHandle.nextId());
            } catch (Exception e) {
                log.error("转化数据ID生成失败,{}", e.getMessage(), e);
            }
            dataItems.add(transferDataItemDTO);
            TransferDataDTO transferDataDTO = new TransferDataDTO();
            transferDataDTO.setDataItems(dataItems);
            transferDataDTO.setRequestId(transferSyncUser.getRequestId());
            MarketingTransferInfo transferInfo = new MarketingTransferInfo();
            transferInfo.setApiCode(apiCode);
            transferInfo.setRequestId(transferSyncUser.getRequestId());
            transferInfo.setCreateTime(new Date());
            transferInfo.setJsonData(JSONObject.toJSONString(transferDataDTO));
            transferInfo.setActualNum(1);
            marketingTransferInfoMapper.insertSelective(transferInfo);
            String id = String.valueOf(transferInfo.getId());
            if (rocketMqSwitch.rocketMQSwitchFlag(apiCode, MarketingTransferSmallConstants.TAG_MARKETING_TRANSFER_RECEIVE_SMALL)) {
                pushRuleService.sendToRocketMqByConfig(apiCode, MarketingTransferSmallConstants.TOPIC
                        , MarketingTransferSmallConstants.TAG_MARKETING_TRANSFER_RECEIVE_SMALL, id, CustomerQueueEnum.ORG_TRANSFER);
            } else {
                pushRuleService.sendToMqByConfig(apiCode, MQConstants.ROUTING_KEY_MARKETING_TRANSFER_RECEIVE_SMALL, id,
                        CustomerQueueEnum.ORG_TRANSFER);
            }
        } catch (Exception e) {
            caseShuheUser.setStatus(2);
            caseShuheUserMapper.updateByPrimaryKey(caseShuheUser);
            String msg = "数禾转化数据入标准转化失败!";
            this.sendAlarmMgs("数禾转化数据入标准转化",
                    msg.concat("\napiCode“").concat(apiCode).concat("”\n案件编号“").concat(caseShuheUser.getCustNum()).concat("”\n").concat("请及时跟进^_^"),
                    alarmClient);
        }
    }

    private CaseShuheUser assembleShuheDxUser(ShuheTransferJsonDTO jsonDTO, String apiCode, String jsonData) {
        CaseShuheUser caseUser = new CaseShuheUserAdaptee();
        caseUser.setApiCode(apiCode);
        final Map<String, String> dataItem = jsonDTO.getDataItem();
        caseUser.setIsTurn(dataItem.getOrDefault("is_turn", ""));
        caseUser.setIsBlack(dataItem.getOrDefault("is_black", ""));
        caseUser.setCustNum(jsonDTO.getOrderId());
        caseUser.setCreateTime(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        caseUser.setUploadDate(LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        caseUser.setBiztype(jsonDTO.getBizType());
        caseUser.setUserType(jsonDTO.getBizType());
        caseUser.setMobile(jsonDTO.getMobile());
        caseUser.setCell(BrCipherMaker.getInstance().encode(jsonDTO.getMobile()));
        caseUser.setJsonData(jsonData);
        String defaultValue = "";
        caseUser.setClcUsrLstAppStaTim(dataItem.getOrDefault("clc_usr_lst_app_sta_tim", defaultValue));
        caseUser.setClcUsrIsoPhoTim(dataItem.getOrDefault("clc_usr_iso_pho_tim", defaultValue));
        caseUser.setClcUsrIsoIdtTim(dataItem.getOrDefault("clc_usr_iso_idt_tim", defaultValue));
        caseUser.setClcUsrIsoCrdTim(dataItem.getOrDefault("clc_usr_iso_crd_tim", defaultValue));
        caseUser.setClcUsrIsoInfTim(dataItem.getOrDefault("clc_usr_iso_inf_tim", defaultValue));
        caseUser.setClcUsrIsoAtoTim(dataItem.getOrDefault("clc_usr_iso_ato_tim", defaultValue));
        caseUser.setClcUsrAdtTimRcnLon(dataItem.getOrDefault("clc_usr_adt_tim_rcn_lon", defaultValue));
        caseUser.setClcUsrFstLogTimAll(dataItem.getOrDefault("clc_usr_fst_log_tim_all", defaultValue));
        caseUser.setClcUsrAdtLmtItr(dataItem.getOrDefault("clc_usr_adt_lmt_itr", defaultValue));
        caseUser.setClcUsrFrtFqOrdTim(dataItem.getOrDefault("clc_usr_frt_fq_ord_tim", defaultValue));
        caseUser.setClcUsrFstLndTimCshBtHl(dataItem.getOrDefault("clc_usr_fst_lnd_tim_csh_bt_hl", defaultValue));
        caseUser.setClcUsrMaxDxRrtEnd(dataItem.getOrDefault("clc_usr_max_dx_rrt_end", defaultValue));
        caseUser.setUsrForbidCallEndTim(dataItem.getOrDefault("usr_forbid_call_end_tim", defaultValue));
        return caseUser;
    }

    private void updateCaseShuhe(CaseShuheUser caseShuheUser) {
        CaseShuheUser csu = new CaseShuheUser();
        csu.setId(caseShuheUser.getId());
        csu.setSaveStatus(caseShuheUser.getSaveStatus());
        csu.setErrorInfo(caseShuheUser.getErrorInfo());
        csu.setUpdateTime(new Date());
        caseShuheUserMapper.updateByPrimaryKeySelective(csu);
    }

    void sendAlarmMgs(String title, String error, AlarmApiClient alarmClient) {
        try {
            alarmClient.sendAlarm(error, title, AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode());
        } catch (Exception ignored) {

        }
    }
}
