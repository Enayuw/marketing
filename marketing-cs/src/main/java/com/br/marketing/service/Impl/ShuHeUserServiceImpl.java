package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.adapter.transfer.TransferSyncAdapter;
import com.br.marketing.adapter.transfer.adaptee.CaseShuheUserAdaptee;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.shuhe.ResponseShuheDTO;
import com.br.marketing.dto.shuhe.ShuheTransferJsonDTO;
import com.br.marketing.dto.shuhe.factory.CaseShuheUserFactory;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.dto.shuhe.strategy.UnknownUserType;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.util.ShuHeAESencUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.security.SecureRandom;
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

    DateTimeFormatter ymdhms = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");



    @Transactional(rollbackFor = Exception.class)
    public Long saveShUploadData(CaseShuheUploadData shuheUploadData, JSONObject uploadDataDTO, JSONArray listInfo) {
        caseShuheUploadDataMapper.insertSelective(shuheUploadData);
        //todo 测试pulsar 上线删除
        if("1".equals(uploadDataDTO.getString("test"))){
            throw new RuntimeException("数据库异常");
        }
        //todo 模拟异常上线后要删除
        pushRuleService.mockDbOrRedisError(1,shuheUploadData.getApiCode());
        return saveSyncInfo(adapterMarketingPreUserDTO(uploadDataDTO, listInfo, shuheUploadData), shuheUploadData);
    }

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
            log.error(String.format("数禾上传数据封装对象报错：%s",e.getMessage()), e);
        }
        return null;
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
        syncInfo.setCreateTime(new Date());
        syncInfo.setActualNum(userDTO.getDataItems().size());
        syncInfo.setJsonData(JSON.toJSONString(userDTO, SerializerFeature.WriteNullStringAsEmpty
                , SerializerFeature.WriteNullListAsEmpty));
        marketingUserMapper.insertMarketingPreUserByText(syncInfo);
        caseShuheUploadDataMapper.updateByPrimaryKeySelective(record);
        return syncInfo.getId();
    }

    public Map saveShTransferData(String apiCode, String jsonData,String requestId, ResponseShuheDTO responseShuheDTO,Date createTime){
        HashMap<String, Object> res = new HashMap<>();
        String msg="";
        ShuheTransferJsonDTO jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<ShuheTransferJsonDTO>() {
        }.getType());
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
        res.put("userTypeUknow",sendToQueueBool);
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
        caseShuheUser.setReserveField2(requestId);
        transferSyncUser.setRequestId(requestId);
        // 5、数据落前置库
        if(createTime!=null){
            caseShuheUser.setCreateTime(createTime);
        }
        caseShuheUserMapper.insertSelective(caseShuheUser);
        //todo 测试pulsar 上线删除
        JSONObject testJb = JSONObject.parseObject(jsonData);
        if("1".equals(testJb.getString("test"))){
            throw new RuntimeException("模拟DB异常");
        }

        // 6、转化信息入转化标准库
        Long id = saveTransferNew(apiCode, caseShuheUser, transferSyncUser, !sendToQueueBool,createTime);
        res.put("transferInfoId",id);
        return res;
    }

    private Long saveTransferNew(String apiCode, CaseShuheUser caseShuheUser
            , MarketingTransferSyncUser transferSyncUser, boolean sendToQueueBool, Date createTime) {
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
            alarmClient.sendAlarm(error, title, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
        } catch (Exception ignored) {

        }
    }
}
