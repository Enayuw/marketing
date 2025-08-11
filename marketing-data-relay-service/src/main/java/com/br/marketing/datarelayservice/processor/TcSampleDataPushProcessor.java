package com.br.marketing.datarelayservice.processor;

import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrSampleRecord;
import com.br.marketing.mapper.MarketingTcyrSampleRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcSampleDataPushProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrSampleRecordMapper tcyrSampleRecordMapper;

    @Override
    protected String fetchApiCode() {
        return apiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrSampleRecord record = new MarketingTcyrSampleRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrSampleRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrSampleRecord record = new MarketingTcyrSampleRecord();
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
            tcyrSampleRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(2);
            tcyrSampleRecordMapper.insertSelective(record);
            return null;
        }
    }
}
