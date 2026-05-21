package com.br.marketing.service.tc.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.enums.TcyrAssignStatusEnum;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcyrSyncRecordApiCodeFillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Objects;

@Service
@Slf4j
public class TcyrSyncRecordApiCodeFillServiceImpl implements TcyrSyncRecordApiCodeFillService {

    @Resource
    private MarketingTcyrSyncRecordMapper marketingTcyrSyncRecordMapper;

    @Override
    public Result<Void> fillApiCode(String batchNo, String apiCode) {
        if (StringUtils.isBlank(batchNo) || StringUtils.isBlank(apiCode)) {
            return new Result<Void>().setCode(ResultCode.PARAM_ERROR.getValue()).setMessage("batchNo、apiCode不能为空");
        }
        String bn = batchNo.trim();
        String code = apiCode.trim();

        MarketingTcyrSyncRecord row = marketingTcyrSyncRecordMapper.selectLatestByBatchNo(bn);
        if (row == null) {
            return new Result<Void>().setCode(ResultCode.FAIL.getValue()).setMessage("批次不存在或已删除");
        }
        if (!Objects.equals(1, row.getIsDel())) {
            return new Result<Void>().setCode(ResultCode.FAIL.getValue()).setMessage("记录不可用");
        }
        if (!Objects.equals(TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue(), row.getStatus())) {
            return new Result<Void>().setCode(ResultCode.FAIL.getValue()).setMessage("仅接入成功记录可补齐apiCode");
        }
        String existing = row.getApiCode();
        if (StringUtils.isNotBlank(existing)) {
            if (code.equals(existing.trim())) {
                if (!TcyrAssignStatusEnum.isFilled(row.getAssignStatus())) {
                    MarketingTcyrSyncRecord align = new MarketingTcyrSyncRecord();
                    align.setId(row.getId());
                    align.setAssignStatus(TcyrAssignStatusEnum.FILLED.getValue());
                    align.setUpdateTime(new Date());
                    marketingTcyrSyncRecordMapper.updateByPrimaryKeySelective(align);
                }
                return new Result<Void>().success().setMessage("已补齐，幂等跳过");
            }
            return new Result<Void>().setCode(ResultCode.FAIL.getValue())
                    .setMessage("apiCode已存在且与入参不一致，拒绝覆盖");
        }

        MarketingTcyrSyncRecord patch = new MarketingTcyrSyncRecord();
        patch.setId(row.getId());
        patch.setApiCode(code);
        patch.setAssignStatus(TcyrAssignStatusEnum.FILLED.getValue());
        patch.setUpdateTime(new Date());
        int n = marketingTcyrSyncRecordMapper.updateByPrimaryKeySelective(patch);
        if (n <= 0) {
            return new Result<Void>().setCode(ResultCode.FAIL.getValue()).setMessage("更新失败");
        }
        log.info("TcyrSyncRecord apiCode filled, id={}, batchNo={}, apiCode={}", row.getId(), bn, code);
        return new Result<Void>().success();
    }
}
