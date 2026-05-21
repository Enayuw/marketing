package com.br.marketing.datarelayservice.processor;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.entity.MarketingTcyrTransferRecord;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.mapper.MarketingTcyrTransferRecordMapper;
import org.apache.commons.lang3.StringUtils;
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

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Override
    protected String fetchApiCode() {
        return apiCode();
    }

    @Override
    protected void updateRecord(Long recordId, Integer status, String msg) {
        MarketingTcyrTransferRecord row = new MarketingTcyrTransferRecord();
        row.setId(recordId);
        row.setStatus(status);
        row.setMsg(msg);
        tcyrTransferRecordMapper.updateByPrimaryKeySelective(row);
    }

    @Override
    protected Long recordSave(TcRequestDTO tcRequestDTO, String batchNo, String apiCode, String brPrivateKey) {
        MarketingTcyrSyncRecord syncRecord = tcyrSyncRecordMapper.selectLatestByBatchNo(batchNo);
        if (syncRecord == null || StringUtils.isBlank(syncRecord.getApiCode())) {
            throw new IllegalStateException(
                    "转化通知：batchNo 在 b_marketing_tcyr_sync_record 中不存在或未分配 apiCode，batchNo=" + batchNo);
        }
        String resolvedApiCode = syncRecord.getApiCode();
        String scene = syncRecord.getScene();
        MarketingTcyrTransferRecord row = new MarketingTcyrTransferRecord();
        row.setApiCode(resolvedApiCode);
        row.setRequestNo(tcRequestDTO.getRequestNo());
        row.setBatchNo(batchNo);
        row.setData(appendSceneAndPushFlag(tcRequestDTO.getData(), scene));
        row.setStatus(0);
        row.setCreateTime(new Date());
        row.setUpdateTime(new Date());
        row.setIsClean(0);
        row.setIsDel(1);
        try {
            tcyrTransferRecordMapper.insertSelective(row);
            return row.getId();
        } catch (DuplicateKeyException e) {
            //告警
            row.setRequestNo(tcRequestDTO.getRequestNo() + "_" + System.currentTimeMillis());
            row.setStatus(2);
            tcyrTransferRecordMapper.insertSelective(row);
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
