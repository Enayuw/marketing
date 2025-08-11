package com.br.marketing.datarelayservice.processor;

import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrCpaSyncRecord;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.mapper.MarketingTcyrCpaSyncRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcCpaDataPushProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrCpaSyncRecordMapper tcyrCpaSyncRecordMapper;

    @Override
    protected String fetchApiCode() {
        return apiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrCpaSyncRecord record = new MarketingTcyrCpaSyncRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrCpaSyncRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrCpaSyncRecord record = new MarketingTcyrCpaSyncRecord();
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setData(tcRequestDTO.getData());
        record.setStatus(0);
        record.setDownStatus(0);
        record.setIsDel(1);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try {
            tcyrCpaSyncRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警 todo
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(2);
            tcyrCpaSyncRecordMapper.insertSelective(record);
            return null;
        }
    }


}
