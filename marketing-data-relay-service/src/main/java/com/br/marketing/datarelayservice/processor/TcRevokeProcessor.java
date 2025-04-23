package com.br.marketing.datarelayservice.processor;

import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrRevokeRecord;
import com.br.marketing.mapper.MarketingTcyrRevokeRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcRevokeProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrRevokeRecordMapper tcyrRevokeRecordMapper;

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrRevokeRecord record = new MarketingTcyrRevokeRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrRevokeRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrRevokeRecord record = new MarketingTcyrRevokeRecord();
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setData(tcRequestDTO.getData());
        record.setCreateTime(new Date());
        try {
            tcyrRevokeRecordMapper.insert(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            tcyrRevokeRecordMapper.insert(record);
            return null;
        }
    }
}
