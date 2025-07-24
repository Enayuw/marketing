package com.br.marketing.rule.zhongan;

import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.zhongan.input.ZaSmsRosterLockingDataDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.SmsCallBackBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 众安短信明细入库规则
 */
@Service
@Slf4j
public class ZhongAnSmsCallBackImpl implements AssembleData<ZaSmsRosterLockingDataDTO> {

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public ZaSmsRosterLockingDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        SmsCallBackBO bo = (SmsCallBackBO) transmitFact;
//        log.warn("众安拨打明细符合落库规则，id={}", bo.getId());
        //上传表获取手机号，转为md5加密
        MarketingSyncUser syncUser = marketingSyncInfoMapper.getNewestByCusnumAndStatus(bo.getApiCode(), bo.getCaseNum());
        ZaSmsRosterLockingDataDTO data = new ZaSmsRosterLockingDataDTO();
        data.setApiCode(bo.getApiCode());
        data.setCaseNum(bo.getCaseNum());
        data.setMobileMd5(syncUser != null ? syncUser.getCellMd5() : "");
        data.setBizDate(bo.getCreateDate());
        data.setUserType(bo.getUserType());
        data.setSmsSendStatus(bo.getSmsSendStatus());
        return data;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        //1.剔除黑名单（callStatus=12）数据
        //2.到上传表根据caseNum匹配最新手机号
        //3.手机号在 众安明细锁定表 当日去重
        if (!(transmitFact instanceof SmsCallBackBO)) {
            return false;
        }

        SmsCallBackBO bo = (SmsCallBackBO) transmitFact;
        Set<String> custNums = new HashSet<>();
        custNums.add(bo.getCaseNum());

        Map<String, SyncUserValidityPeriodsBO> keyToSyncUserBO = transferDataValidityPeriodService
                .getValidityPeriodsByCustNumAndUserType(custNums, bo.getUserType(), bo.getApiCode(), new Date());

        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = keyToSyncUserBO.get(bo.getCaseNum());
        if (syncUserValidityPeriodsBO == null) {
            log.warn("众安短信明细回调, 未匹配到上传数据, caseNum: {}, userType: {}", bo.getCaseNum(), bo.getUserType());
            return false;
        }
        List<MarketingSyncUser> syncUsers = syncUserValidityPeriodsBO.getSyncUsers();
        if (syncUsers == null || syncUsers.isEmpty()) {
            log.warn("众安短信明细回调, 未匹配到上传数据, caseNum: {}, userType: {}", bo.getCaseNum(), bo.getUserType());
            return false;
        }
        return true;
    }

    @Override
    public String label() {
        return "ZhongAn_SmsCallBackData_Insert";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ZHONGAN_SMS_LOCK_DATA_INSERT.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }

}
