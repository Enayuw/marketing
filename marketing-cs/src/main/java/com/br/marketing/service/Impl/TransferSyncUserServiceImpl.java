package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CaseShuheUploadDataMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.ITransferSyncUserService;
import com.br.marketing.util.ShuHeAESencUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 客户转化数据记录业务接口 实现类
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/15 10:00
 */
@Service
@Slf4j
public class TransferSyncUserServiceImpl implements ITransferSyncUserService {

    @Resource
    private RabbitMqProducter producter;

    @Resource
    CaseShuheUploadDataMapper caseShuheUploadDataMapper;

    @Resource
    private MarketingUserMapper marketingUserMapper;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Override
    public int insertSelective(MarketingTransferSyncUser marketingTransferSyncUser) {
        return marketingTransferSyncUserMapper.insertSelective(marketingTransferSyncUser);
    }

    @Override
    public int updateByPrimaryKeySelective(MarketingTransferSyncUser marketingTransferSyncUser) {
        return marketingTransferSyncUserMapper.updateByPrimaryKeySelective(marketingTransferSyncUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveShUploadData(CaseShuheUploadData shuheUploadData, JSONObject uploadDataDTO,JSONArray listInfo) {
        caseShuheUploadDataMapper.insertSelective(shuheUploadData);
        saveSyncInfo(adapterMarketingPreUserDTO(uploadDataDTO, listInfo, shuheUploadData), shuheUploadData);
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
            log.error(e.getMessage(), e);
        }
        return null;
    }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertInfoAndSync(MarketingTransferSyncUser marketingTransferSyncUser
            , MarketingTransferInfo transferInfo, CaseShuheUser caseShuheUser) throws Exception {
        int rowInfo = marketingTransferInfoMapper.insertSelective(transferInfo);
        if (rowInfo < 1) {
            caseShuheUser.setSaveStatus(2);
            throw new Exception("#2保存到'b_marketing_transfer_info'失败");
        }
        int rowSync = marketingTransferSyncUserMapper.insertSelective(marketingTransferSyncUser);
        if (rowSync < 1) {
            caseShuheUser.setSaveStatus(3);
            throw new Exception("#3保存到'b_marketing_transfer_sync_" + marketingTransferSyncUser.gettCid() + "'失败");
        }
    }
}
