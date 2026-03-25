package com.br.marketing.datarelayservice.processor;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrRevokeRecord;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.mapper.MarketingTcyrRevokeRecordMapper;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Override
    protected String fetchApiCode() {
        return apiCode();
    }

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
        String scene = tcyrSyncRecordMapper.selectLatestSceneByBatchNo(apiCode, batchNo);
        MarketingTcyrRevokeRecord record = new MarketingTcyrRevokeRecord();
        record.setApiCode(apiCode);
        record.setRequestNo(tcRequestDTO.getRequestNo());
        record.setBatchNo(batchNo);
        record.setData(appendSceneAndPushFlag(tcRequestDTO.getData(), scene));
        record.setStatus(0);
        record.setCreateTime(new Date());
        record.setUpdateTime(new Date());
        try {
            tcyrRevokeRecordMapper.insertSelective(record);
            return record.getId();
        } catch (DuplicateKeyException e) {
            //告警
            record.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            record.setStatus(2);
            tcyrRevokeRecordMapper.insertSelective(record);
            return null;
        }
    }

    private String appendSceneAndPushFlag(String rawData, String scene) {
        JSONObject jsonObject = safeParse(rawData);
        if (jsonObject == null) {
            jsonObject = new JSONObject();
        }
        jsonObject.put("scene", scene);
        jsonObject.put("isPushOutBound", scene != null ? "0" : "1");
        return jsonObject.toJSONString();
    }

    private JSONObject safeParse(String rawData) {
        if (StringUtils.isBlank(rawData)) {
            return new JSONObject();
        }
        try {
            return JSONObject.parseObject(rawData);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
