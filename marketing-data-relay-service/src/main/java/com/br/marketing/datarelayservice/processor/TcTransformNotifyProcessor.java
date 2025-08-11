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
    protected String fetchApiCode() {
        return apiCode();
    }

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
        MarketingTcyrTransferRecord record = new MarketingTcyrTransferRecord();
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setData(tcRequestDTO.getData());
        record.setStatus(0);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        record.setIsClean(0);
        record.setIsDel(1);
        try {
            tcyrTransferRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(2);
            tcyrTransferRecordMapper.insertSelective(record);
            return null;
        }
    }
}
