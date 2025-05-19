package com.br.marketing.service.tc.impl;

import com.br.marketing.entity.MarketingTcyrTransferRecord;
import com.br.marketing.mapper.MarketingTcyrTransferRecordMapper;
import com.br.marketing.service.tc.TcTransferRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 同城易融-转化上传
 *
 * @author zhiyong.zhang
 * @date 2024/04/21
 */
@Service
@Slf4j
public class TcTransferRecordServiceImpl implements TcTransferRecordService {


    @Resource
    private MarketingTcyrTransferRecordMapper tcyrTransferRecordMapper;


    @Override
    public List<MarketingTcyrTransferRecord> selectTcyrTransforRecordList(String apiCode, Integer status, Long lastSearchId, Integer searchSize) {
        return tcyrTransferRecordMapper.selectTcyrTransforRecordList(apiCode,status,lastSearchId,searchSize);
    }

    @Override
    public Integer updateCleanStatus(List<Long> idList, Integer cleanStatus) {
        return tcyrTransferRecordMapper.updateCleanStatus(idList,cleanStatus);

    }
}
