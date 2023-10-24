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
