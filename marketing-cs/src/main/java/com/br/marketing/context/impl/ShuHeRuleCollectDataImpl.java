package com.br.marketing.context.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.context.AbstractRuleCollectDataService;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.RuleNecessaryData;
import com.br.marketing.dto.shuhe.factory.UserTypeStrategyFactory;
import com.br.marketing.dto.shuhe.strategy.IUserType;
import com.br.marketing.entity.CaseShuheUser;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
public class ShuHeRuleCollectDataImpl implements AbstractRuleCollectDataService {

    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;
    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public void ruleNecessaryData(List transmitFacts, ProcessHandlerContext context) {
        if (!transmitFacts.isEmpty() && transmitFacts.get(0) instanceof MarketingTransferSyncUser) {
            ShuHeRuleNecessaryData shuHeRuleNecessaryData = new ShuHeRuleNecessaryData();
            context.setRuleNecessaryData(shuHeRuleNecessaryData);
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFacts.get(0);
            // 获取上传表信息
            Set<String> set = new HashSet<>(Collections.singletonList(transfer.getCustNum()));
            List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(context.getApiCode(), set);
            Map<String, MarketingSyncUser> collect = preUserByTask.stream().collect(
                    Collectors.toMap(MarketingSyncUser::getCustNum, Function.identity(), (v1, v2) ->
                            v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2));
            shuHeRuleNecessaryData.setCustomerMap(collect);

            // 生成后续使用数据上下文
            Date creatTime = iMarketingSyncUserService.getCreatTimeByCustNumAndUserType(transfer.getApiCode()
                    , transfer.getCustNum(), transfer.getUserType());
            shuHeRuleNecessaryData.setCreatTime(creatTime);
            IUserType iUserType = UserTypeStrategyFactory.getUserTypeStrategy(transfer.getUserType());
            shuHeRuleNecessaryData.setIUserType(iUserType);
            shuHeRuleNecessaryData.setContinueJudgeRule(true);
            CaseShuheUser caseShuheUser = new CaseShuheUser();
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotEmpty(reserveField1)) {
                JSONObject object = JSONObject.parseObject(reserveField1);
                caseShuheUser.setIsTurn(object.getString("is_turn"));
                caseShuheUser.setIsBlack(object.getString("is_black"));
                caseShuheUser.setClcUsrLstAppStaTim(object.getString("clc_usr_lst_app_sta_tim"));
                caseShuheUser.setClcUsrIsoPhoTim(object.getString("clc_usr_iso_pho_tim"));
                caseShuheUser.setClcUsrIsoIdtTim(object.getString("clc_usr_iso_idt_tim"));
                caseShuheUser.setClcUsrIsoCrdTim(object.getString("clc_usr_iso_crd_tim"));
                caseShuheUser.setClcUsrIsoInfTim(object.getString("clc_usr_iso_inf_tim"));
                caseShuheUser.setClcUsrFrtFqOrdTim(object.getString("applyLoanTime"));
                caseShuheUser.setCell(BrCipherMaker.getInstance().decode(object.getString("cell")));
                shuHeRuleNecessaryData.setTaskId(object.getString("taskId"));
            }
            caseShuheUser.setClcUsrFstLogTimAll(transfer.getLoginTime());
            caseShuheUser.setClcUsrIsoAtoTim(transfer.getApplyTime());
            caseShuheUser.setClcUsrAdtTimRcnLon(transfer.getAuditTime());
            caseShuheUser.setClcUsrAdtLmtItr(transfer.getAuditAmount());
            caseShuheUser.setClcUsrFstLndTimCshBtHl(transfer.getLentTime());
            caseShuheUser.setUserType(transfer.getUserType());
            caseShuheUser.setCustNum(transfer.getCustNum());
            shuHeRuleNecessaryData.setCaseShuheUser(caseShuheUser);
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

        private Map<String, MarketingSyncUser> customerMap;

        private MarketingTransferSyncUser transfer;

        public void setTransfer(MarketingTransferSyncUser transfer) {
            this.transfer = transfer;
            setCaseShuheUserValue();
        }

        private void setCaseShuheUserValue() {
            if (this.transfer == null) {
                return;
            }
            String reserveField1 = this.transfer.getReserveField1();
            if (StringUtils.isNotEmpty(reserveField1)) {
                JSONObject object = JSONObject.parseObject(reserveField1);
                this.caseShuheUser.setIsTurn(object.getString("is_turn"));
                this.caseShuheUser.setIsBlack(object.getString("is_black"));
                this.caseShuheUser.setClcUsrLstAppStaTim(object.getString("clc_usr_lst_app_sta_tim"));
                this.caseShuheUser.setClcUsrIsoPhoTim(object.getString("clc_usr_iso_pho_tim"));
                this.caseShuheUser.setClcUsrIsoIdtTim(object.getString("clc_usr_iso_idt_tim"));
                this.caseShuheUser.setClcUsrIsoCrdTim(object.getString("clc_usr_iso_crd_tim"));
                this.caseShuheUser.setClcUsrIsoInfTim(object.getString("clc_usr_iso_inf_tim"));
                this.caseShuheUser.setClcUsrFrtFqOrdTim(object.getString("applyLoanTime"));
                this.caseShuheUser.setCell(BrCipherMaker.getInstance().decode(object.getString("cell")));
                this.taskId = object.getString("taskId");
            }
            this.caseShuheUser.setClcUsrFstLogTimAll(this.transfer.getLoginTime());
            this.caseShuheUser.setClcUsrIsoAtoTim(this.transfer.getApplyTime());
            this.caseShuheUser.setClcUsrAdtTimRcnLon(this.transfer.getAuditTime());
            this.caseShuheUser.setClcUsrAdtLmtItr(this.transfer.getAuditAmount());
            this.caseShuheUser.setClcUsrFstLndTimCshBtHl(this.transfer.getLentTime());
            this.caseShuheUser.setUserType(this.transfer.getUserType());
            this.caseShuheUser.setApiCode(this.transfer.getApiCode());
            this.caseShuheUser.setCustNum(this.transfer.getCustNum());
        }
    }
}
