package com.br.marketing.context.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.ShuheBlackPhoneRecordMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description :
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/22 13:51
 */
@Service
@Slf4j
public class ShuHeRuleCollectDataImpl extends CommonMethodHandlerService {

    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;
    @Resource
    private ShuheBlackPhoneRecordMapper shuheBlackPhoneRecordMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            ShuHeRuleNecessaryData shuHeRuleNecessaryData = new ShuHeRuleNecessaryData();
            context.setRuleNecessaryData(shuHeRuleNecessaryData);
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFacts.get(0);
            // 获取上传表信息
            Set<String> set = new HashSet<>(Collections.singletonList(transfer.getCustNum()));
            Map<String, MarketingSyncUser> collect = customerMarketingSyncUser(set, context.getApiCode());
            shuHeRuleNecessaryData.setCustomerMap(collect);
            // 生成后续使用数据上下文
            Date creatTime = iMarketingSyncUserService.getCreatTimeByCustNumAndUserType(transfer.getApiCode()
                    , transfer.getCustNum(), transfer.getUserType());
            shuHeRuleNecessaryData.setCreatTime(creatTime);
            IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(transfer.getUserType());
            shuHeRuleNecessaryData.setIUserType(iUserType);
            shuHeRuleNecessaryData.setContinueJudgeRule(true);
            caseShuheUserAdapter(transfer, shuHeRuleNecessaryData);
            MarketingSyncUser marketingSyncUserByCell = marketingSyncInfoMapper.getNewestPreUserByCell(context.getApiCode(),
                    BrCipherMaker.getInstance().encode(shuHeRuleNecessaryData.getCaseShuheUser().getCell()));
            shuHeRuleNecessaryData.setMarketingSyncUserByCell(marketingSyncUserByCell);
            String apiCode = context.getApiCode();
            if (marketingCommonConfig.getShuHeNonBlackListApiCodeSet().contains(apiCode)) {
                int count = shuheBlackPhoneRecordMapper.countTmpNonBlackListByCell(
                        shuHeRuleNecessaryData.getCaseShuheUser().getMobile());
                shuHeRuleNecessaryData.setNonBlackListCount(count);
            }

        }
    }

    @Override
    public RuleDataCollectionEnum label() {
        return RuleDataCollectionEnum.SHU_HE_RULE_DATA_COLLECTION;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class ShuHeRuleNecessaryData extends RuleNecessaryData {
        /**
         * 场景策略
         */
        private IUserType iUserType;
        /**
         * 上传数据创建时间
         */
        private Date creatTime;

        /**
         * 数禾原始数据-结构
         */
        private CaseShuheUser caseShuheUser;

        /**
         * 批次号
         */
        private String taskId;

        /**
         * 是否继续判断规则
         * true 继续
         */
        private boolean continueJudgeRule;

        /**
         * 2024-03-13 21:18
         * 非黑名单量级
         */
        private int nonBlackListCount;

        private Map<String, MarketingSyncUser> customerMap;
        /**
         * 根据手机号cell获取最新一条上传数据
         */
        private MarketingSyncUser marketingSyncUserByCell;

        private MarketingTransferSyncUser transfer;

        /**
         * 转化所需要的有效期数据
         */
        private Map<String, SyncUserValidityPeriodsBO> userValidityPeriodsBOMap;

        public void setTransfer(MarketingTransferSyncUser transfer) {
            this.transfer = transfer;
            caseShuheUserAdapter(transfer, this);
        }
    }

    private static String replace000(String dateStr) {
        String replacement = "";
        if (StringUtils.isEmpty(dateStr)) {
            return replacement;
        }
        String target = ":000";
        return dateStr.replace(target, replacement);
    }

    /**
     * 解析json
     */
    private static void caseShuheUserAdapter(MarketingTransferSyncUser transfer, ShuHeRuleNecessaryData data) {
        if (transfer == null) {
            return;
        }
        CaseShuheUser user = new CaseShuheUser();
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.isNotEmpty(reserveField1)) {
            JSONObject object = JSONObject.parseObject(reserveField1);
            user.setIsTurn(object.getString("is_turn"));
            user.setIsBlack(object.getString("is_black"));
            user.setClcUsrLstAppStaTim(object.getString("clc_usr_lst_app_sta_tim"));
            user.setClcUsrIsoPhoTim(object.getString("clc_usr_iso_pho_tim"));
            user.setClcUsrIsoIdtTim(object.getString("clc_usr_iso_idt_tim"));
            user.setClcUsrIsoCrdTim(object.getString("clc_usr_iso_crd_tim"));
            user.setClcUsrIsoInfTim(object.getString("clc_usr_iso_inf_tim"));
            user.setClcUsrFrtFqOrdTim(object.getString("applyLoanTime"));
            user.setClcUsrMaxDxRrtEnd(object.getString("clc_usr_max_dx_rrt_end"));
            user.setUsrForbidCallEndTim(object.getString("usr_forbid_call_end_tim"));
            user.setCell(BrCipherMaker.getInstance().decode(object.getString("cell")));
            user.setMobile(object.getString("cell"));
            user.setJsonObject(object);
            data.setTaskId(object.getString("taskId"));
        }
        user.setClcUsrFstLogTimAll(replace000(transfer.getLoginTime()));
        user.setClcUsrIsoAtoTim(replace000(transfer.getApplyTime()));
        user.setClcUsrAdtTimRcnLon(replace000(transfer.getAuditTime()));
        user.setClcUsrAdtLmtItr(replace000(transfer.getAuditAmount()));
        user.setClcUsrFstLndTimCshBtHl(replace000(transfer.getLentTime()));
        user.setUserType(transfer.getUserType());
        user.setApiCode(transfer.getApiCode());
        user.setCustNum(transfer.getCustNum());
        user.setReserveField1(reserveField1);
        data.setCaseShuheUser(user);
    }
}
