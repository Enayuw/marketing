package com.br.marketing.datarelayservice.processor;

import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrTransferRecord;
import com.br.marketing.mapper.MarketingTcyrTransferRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcTransformNotifyProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrTransferRecordMapper tcyrTransferRecordMapper;

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrTransferRecord record = new MarketingTcyrTransferRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrTransferRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrTransferRecord marketingTcyrTransferRecord = new MarketingTcyrTransferRecord();
        marketingTcyrTransferRecord.setApiCode(apiCode);
        marketingTcyrTransferRecord.setRequestNo(tcRequestDTO.getRequestNo());
        marketingTcyrTransferRecord.setBatchNo(batchNo);
        marketingTcyrTransferRecord.setData(tcRequestDTO.getData());
        marketingTcyrTransferRecord.setCreateTime(new Date());
        try {
            tcyrTransferRecordMapper.insert(marketingTcyrTransferRecord);
            return marketingTcyrTransferRecord.getId();
        } catch (DuplicateKeyException e) {
            //告警
            marketingTcyrTransferRecord.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            tcyrTransferRecordMapper.insert(marketingTcyrTransferRecord);
            return null;
        }
    }
}
