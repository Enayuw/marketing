package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.adapter.transfer.TransferSyncAdapter;
import com.br.marketing.adapter.transfer.adaptee.CaseShuheUserAdaptee;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ShuHeAESencUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
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
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private AlarmApiClient alarmClient;

    private final String title = "数禾转化数据定制化清洗入库";

    @Resource
    CaseShuheUserMapper caseShuheUserMapper;

    @Autowired
    TableCreateServiceImpl tableCreateService;

    @Autowired
    PushRuleService pushRuleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    @Transactional(rollbackFor = Exception.class)
    public Long saveShUploadData(CaseShuheUploadData shuheUploadData, JSONObject uploadDataDTO, JSONArray listInfo) {
        //todo 模拟异常上线后要删除
        pushRuleService.mockDbOrRedisError(1, shuheUploadData.getApiCode());
        caseShuheUploadDataMapper.insertSelective(shuheUploadData);
        return saveSyncInfo(adapterMarketingPreUserDTO(uploadDataDTO, listInfo, shuheUploadData), shuheUploadData);
    }

    /**
     * 2023-12-21 17:10
     * 数禾上传数据适配上传数据
     */
    private MarketingPreUserDTO adapterMarketingPreUserDTO(JSONObject uploadDataDTO, JSONArray listInfo
            , CaseShuheUploadData shuheUploadData) {
        try {
            JSONObject taskCode = JSONObject.parseObject(uploadDataDTO.getString("taskCode"));
            MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
            userDTO.setTaskId(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    .concat("_").concat(shuheUploadData.getApiCode()));
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
                if (taskCode != null && taskCode.getString("groupType") != null) {
                    reserveField1.put("groupTypeNew", taskCode.getString("groupType"));
                }
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
                varDataHandle(varData, dto, reserveField1);
                reserveField1.putAll(info);
                reserveField1.putAll(uploadDataDTO);
                reserveField1.remove("listInfo");
                reserveField1.remove("mobile");
                reserveField1.remove("varData");
                reserveField1.remove("orderId");
                dto.setReserveField1(JSON.toJSONString(reserveField1, SerializerFeature.WriteNullStringAsEmpty
                        , SerializerFeature.WriteNullListAsEmpty));
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
     * 2023-12-25 22:27
     * 处理业务字段
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
        syncInfo.setJsonData(JSON.toJSONString(userDTO, SerializerFeature.WriteNullStringAsEmpty
                , SerializerFeature.WriteNullListAsEmpty));
        marketingUserMapper.insertMarketingPreUserByText(syncInfo);
        caseShuheUploadDataMapper.updateByPrimaryKeySelective(record);
        return syncInfo.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Map saveShTransferData(String apiCode, String jsonData,String requestId, ResponseShuheDTO responseShuheDTO,Date createTime){
        HashMap<String, Object> res = new HashMap<>();
        String msg = "";
        ShuheTransferJsonDTO jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
        }.getType());
        String userType = jsonDTO.getBizType();
        //todo 模拟异常上线后要删除
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
            res.put("userTypeUknow", empty);
            caseShuheUser = assembleShuheDxUser(jsonDTO, apiCode, jsonData);
            caseShuheUser.setUserType(userType);
            if (empty) {
                msg = "不存在的业务类型电销转化数据，不会触发后续业务流程!";
                this.sendAlarmMgs("数禾电销全场景数据定制化清洗入库", msg.concat("\napiCode“").concat(apiCode)
                        .concat("”\n案件编号“").concat(jsonDTO.getOrderId()).concat("”\n")
                        .concat("请及时跟进或与数禾客户及时沟通^_^"), alarmClient);
            }
        } else {
            final IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(userType);
            caseShuheUser = CaseShuheUserFactory.newInstance().getCaseShuheUser(iUserType
                    , jsonDTO, apiCode, jsonData);
            boolean sendToQueueBool = iUserType instanceof UnknownUserType;
            res.put("userTypeUknow", sendToQueueBool);
            if (sendToQueueBool) {
                caseShuheUser.setStatus(1);
                msg = "未知的业务类型\"" + userType + "\"!";
                responseShuheDTO.failed("抱歉,".concat(msg));
                caseShuheUser.setErrorInfo("#1" + responseShuheDTO.getDesc());
                this.sendAlarmMgs(title, msg.concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                                .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^")
                        , alarmClient);
            } else if (!iUserType.getApiCodes().contains(apiCode)) {
                log.warn("场景(".concat(iUserType.getApiCodes().toString()).concat(")与对应apiCode不匹配\n")
                        .concat(userType).concat("\napiCode“").concat(apiCode).concat("”\n案件编号“")
                        .concat(jsonDTO.getOrderId()).concat("”\n").concat("请及时跟进或与数禾客户及时沟通^_^"));
            }
        }
        // 3、查询db获取相应TaskId
        String taskId = iMarketingSyncUserService.getTaskIdLatestByCustNum(apiCode, jsonDTO.getOrderId(), userType);
        if (taskId == null) {
            taskId = "";
        }
        // 4、客户转化数据适配标准转化数据
        MarketingTransferSyncUser transferSyncUser = new TransferSyncAdapter(
                (CaseShuheUserAdaptee) caseShuheUser).transferSyncUserRequest(taskId, jsonDTO);
        caseShuheUser.setReserveField2(requestId);
        transferSyncUser.setRequestId(requestId);
        // 5、数据落前置库
        if (createTime != null) {
            caseShuheUser.setCreateTime(createTime);
        }
        caseShuheUserMapper.insertSelective(caseShuheUser);
        // 6、转化信息入转化标准库
        Long id = saveTransferNew(apiCode, caseShuheUser, transferSyncUser, createTime);
        res.put("transferInfoId", id);
        res.put("userType", transferSyncUser.getUserType());
        res.put("id", transferSyncUser.getId());
        res.put("cid", transferSyncUser.getCid());
        res.put("createTime", transferSyncUser.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return res;
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

    private Long saveTransferNew(String apiCode, CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser, Date createTime) {
        MarketingTransferInfo transferInfo = new MarketingTransferInfo();
        transferSyncUser.setCid(tableCreateService.getCId(transferSyncUser.getApiCode()));
        transferSyncUser.settCid(tableCreateService.getTcId(transferSyncUser.getApiCode()));
        LocalDateTime localDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (createTime != null) {
            transferSyncUser.setInsertTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createTime));
            transferSyncUser.setRequestData(new SimpleDateFormat("yyyy-MM-dd").format(createTime));
        }else{
            transferSyncUser.setInsertTime(localDateTime.format(ymdhms));
            transferSyncUser.setRequestData(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        transferSyncUser.setRequestTime(transferSyncUser.getInsertTime());
        transferInfo.setApiCode(apiCode);
        transferInfo.setRequestId(transferSyncUser.getRequestId());
        transferInfo.setCreateTime(new Date());
        transferInfo.setJsonData(JSONObject.toJSONString(transferSyncUser));
        transferInfo.setActualNum(1);
        marketingTransferInfoMapper.insertSelective(transferInfo);
        marketingTransferSyncUserMapper.insertSelective(transferSyncUser);
        return transferInfo.getId();
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
