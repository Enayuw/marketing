package com.br.marketing.service.strategy.pushpreview;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.PushCustomerDTO;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.rulecenter.impl.push.UploadRePushPolicyStrategy;
import com.br.marketing.vo.xiecheng.PushViewVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 上传任务推送预览策略
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class UploadTaskPushPreviewStrategy implements IPushPreviewStrategy {

    @Resource
    private MarketingSyncReportMapper syncReportMapper;

    @Resource
    private UploadRePushPolicyStrategy uploadRePushPolicyStrategy;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public Result<PushViewVO> execute(PushCustomerDTO dto) {
        int total = 0;
        PushViewVO pushViewVO = new PushViewVO();

        String uploadReportId = dto.getUploadReportId();
        if (StringUtils.isEmpty(uploadReportId)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("入参缺少上传记录id");
        }

        String[] split = uploadReportId.split(",");
        // 1. 转换为Long类型的ID列表
        List<Long> ids = Arrays.stream(split)
                .map(String::trim)
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 2. 批量查询所有的MarketingSyncReport
        List<MarketingSyncReport> syncReports = syncReportMapper.selectByIds(ids);
        if (syncReports.isEmpty()) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到上传任务，任务ids：" + ids);
        }

        // 3. 解析页面规则条件
        String filterCondition = uploadRePushPolicyStrategy.getUploadDataCondition(
                dto.getmRuleCondition(), dto.getApiCode());
        log.warn("解析页面规则条件 sql={}", filterCondition);

        // 4. 循环查询每个条件的数据量级并累加
        String repushTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        for (MarketingSyncReport report : syncReports) {
            if (report != null) {
                String apiCode = report.getApiCode();
                String appletDate = report.getAppletDate();
                String userType = report.getUserType();

                // 判断appletDate是否为当天，只有当天才需要时间条件
                if (today.equals(appletDate)) {
                    repushTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                }
                // 非当天数据：createTime 和 updateTime 保持为 null，只使用 appletDate 条件

                // 单次查询该条件的数据量级
                Integer count = marketingSyncUserMapper.countByCondition(
                        apiCode, appletDate, userType, repushTime, filterCondition);
                total += (count != null ? count : 0);
            }
        }

        if (total <= 0) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("无符合的数据");
        }

        pushViewVO.setTotal(total);
        pushViewVO.setRepushTime(repushTime);
        return new Result<PushViewVO>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushViewVO);
    }

    @Override
    public PushPreviewStrategyEnum getStrategyType() {
        return PushPreviewStrategyEnum.UPLOAD_TASK;
    }
}
