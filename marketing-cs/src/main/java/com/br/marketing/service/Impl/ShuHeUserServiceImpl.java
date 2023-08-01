package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.MQConstants;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.CaseShuheUploadData;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.mapper.CaseShuheUploadDataMapper;
import com.br.marketing.mapper.MarketingUserMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
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

@Service
@Slf4j
public class ShuHeUserServiceImpl {

    @Resource
    CaseShuheUploadDataMapper caseShuheUploadDataMapper;

    @Resource
    private MarketingUserMapper marketingUserMapper;

    @Transactional(rollbackFor = Exception.class)
    public Long saveShUploadData(CaseShuheUploadData shuheUploadData, JSONObject uploadDataDTO, JSONArray listInfo) {
        caseShuheUploadDataMapper.insertSelective(shuheUploadData);
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
            log.error(e.getMessage(), e);
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
        try {
            syncInfo.setJsonData(JSON.toJSONString(userDTO, SerializerFeature.WriteNullStringAsEmpty
                    , SerializerFeature.WriteNullListAsEmpty));
        } catch (Exception e) {
            record.setSaveInfoStatus(1);
            log.error(e.getMessage(), e);
        }
        int i = marketingUserMapper.insertMarketingPreUserByText(syncInfo);
        caseShuheUploadDataMapper.updateByPrimaryKeySelective(record);
        return syncInfo.getId();
    }
}
