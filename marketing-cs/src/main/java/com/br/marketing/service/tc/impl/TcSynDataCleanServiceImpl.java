package com.br.marketing.service.tc.impl;

import com.br.marketing.entity.MarketingTcyrSync;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrSyncMapper;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataCleanService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 同城易融拉取文件入库-Service实现
 *
 * @author zhiyong.zhang
 * @date 2024/04/21
 */
@Service
@Slf4j
public class TcSynDataCleanServiceImpl implements TcSyncDataCleanService {

    @Resource
    private MarketingTcyrSyncMapper tcyrSyncMapper;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;


    @Override
    public List<MarketingTcyrSync> selectTcSyncList(String batchNo, Integer cleanStatus, Long lastSearchId, Integer searchSize) {
        return tcyrSyncMapper.selectTcSyncList(batchNo,cleanStatus,lastSearchId,searchSize);
    }

    @Override
    public Integer updateCleanStatus(List<Long> idList, Integer cleanStatus) {
        return tcyrSyncMapper.updateCleanStatus(idList,cleanStatus);
    }

    @Override
    public List<MarketingTcyrSyncRecord> searchAllTcyrSyncList(String apiCode,Integer status) {
        return tcyrSyncRecordMapper.searchAllTcyrSyncList(apiCode,status);
    }

}
