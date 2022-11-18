package com.br.marketing.monkey.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.monkey.service.ZhongAnPushBlackDataService;
import com.br.marketing.monkeydata.handle.zhongan.ZhongAnPushBlackDataHandle;
import com.br.marketing.monkeydata.entity.commonobj.MarketingSyncCondition;
import com.br.marketing.service.Impl.YiXinTransferServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * @author zhen.li1
 * @date 2022/11/16
 * @desc:众安推送黑名单至客服定时任务处理
 */
@Service
@Slf4j
public class ZhongAnPushBlackDataServiceImpl implements ZhongAnPushBlackDataService {

    final static String EXECUTE_TIME = " 00:00:00";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferActionFrontMapper transferActionFrontMapper;

    @Resource
    YiXinTransferServiceImpl yiXinTransferService;
    @Resource
    private ZhongAnPushBlackDataHandle zhongAnPushBlackDataHandle;

    @Override
    public Result actionPushBlackData(String apiCode) {
        if (StringUtils.isEmpty(apiCode)) {
            apiCode = "3710048";
        }
        Date now = new Date();
        //可配置
        String execute = EXECUTE_TIME;
        if (StringUtils.isNotBlank(marketingCommonConfig.getZhongAnPushBlackDataExecuteTime())) {
            execute = " " + marketingCommonConfig.getZhongAnPushBlackDataExecuteTime();
        }
        Date executeTime = DateHelper.getDatePlusHourMinuteSecond(now, execute);
        String recordDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (!now.before(executeTime)) {
            //查询推送记录
            List<TransferActionFront> actionFrontList = getActionFront(apiCode, 1);
            if (actionFrontList.size() > 0) {
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该任务今日已经推送");
            }
            Long frontId = yiXinTransferService.saveFrontData(apiCode, recordDate, 1);
            int periodDay = 30;
            //默认30天
            if (StringUtils.isNotEmpty(marketingCommonConfig.getZhongAnPushBlackDataPeriod())) {
                periodDay = Integer.valueOf(marketingCommonConfig.getZhongAnPushBlackDataPeriod());
            }
            String appletDateStart = LocalDateTime.now().minusDays(periodDay).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            MarketingSyncCondition marketingSyncCondition = new MarketingSyncCondition();
            marketingSyncCondition.setApiCode(apiCode);
            marketingSyncCondition.setAppletDateStart(appletDateStart);
            marketingSyncCondition.setAppletDateEnd(recordDate);
            zhongAnPushBlackDataHandle.action(marketingSyncCondition);
            yiXinTransferService.updateFrontDataStatus(frontId, 2);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate("众安推送黑名单至客服任务完成");
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue());

    }

    private List<TransferActionFront> getActionFront(String apiCode, int actionType) {
        TransferActionFrontExample example = new TransferActionFrontExample();
        TransferActionFrontExample.Criteria criteria = example.createCriteria();
        criteria.andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);
        return transferActionFrontMapper.selectByExample(example);
    }
}
