package com.br.marketing.datarelayservice.processor;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrCpaFailRecord;
import com.br.marketing.enums.TcCpaDownStatusEnum;
import com.br.marketing.enums.TcCpaIsDelEnum;
import com.br.marketing.enums.TcCpaRecordStatusEnum;
import com.br.marketing.mapper.MarketingTcyrCpaFailRecordMapper;
import groovy.util.logging.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Date;

@Service
@Slf4j
public class TcCpaFailDataPushProcessor extends AbstractTcCustomizeProcessor{

    @Resource
    private MarketingTcyrCpaFailRecordMapper tcyrCpaFailRecordMapper;

    @Override
    protected String fetchApiCode() {
        return cpaApiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrCpaFailRecord record = new MarketingTcyrCpaFailRecord();
        record.setId(recordId);
        record.setStatus(status);
        record.setMsg(msg);
        tcyrCpaFailRecordMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrCpaFailRecord record = new MarketingTcyrCpaFailRecord();
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setData(tcRequestDTO.getData());
        record.setStatus(TcCpaRecordStatusEnum.ACCESS_IN.getValue());
        record.setDownStatus(TcCpaDownStatusEnum.DEAL_NO.getValue());
        record.setIsDel(TcCpaIsDelEnum.DEL_NO.getValue());
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try {
            tcyrCpaFailRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警 todo
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(TcCpaRecordStatusEnum.ACCESS_FAIL.getValue());
            tcyrCpaFailRecordMapper.insertSelective(record);
            return null;
        }
    }


}
