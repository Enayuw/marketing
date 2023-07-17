package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;

import java.util.List;

/**
 * 宜信基础剔除规则接口
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:29
 */
public interface YiXinProcessGetBaseExcludeRuleDataService {
    /**
     * 基础数据里的数据 判断是否在 T 日转化的数据中 transformType !=1 并且 type!=13 ，如果满足 不推
     * @param marketingTransferSyncUsers  转化数据集合
     */
    void excludeRuleFirst(List<MarketingTransferSyncUser>  marketingTransferSyncUsers);

    /**
     * 基础数据里的数据 判断每条数据在 全量转化的数据中 caseEffective=0 如果满足 不推
     * @param marketingTransferSyncUsers 转化数据集合
     */
    void excludeRuleSecond(List<MarketingTransferSyncUser> marketingTransferSyncUsers);

    /**
     * 基础数据里的数据 判断 3 天内是否推 daas 人工，包括 api 和 sftp 推送的情况，如果满足 不推
     * @param marketingTransferSyncUsers 转化数据集合
     */
    void excludeRuleThird(List<MarketingTransferSyncUser> marketingTransferSyncUsers);

    /**
     * 7 天内 当前 custNum a 情况 是否推送过 如果满足 不推
     * @param marketingTransferSyncUser 转化数据集合
     */
    void excludeRuleFourth(List<MarketingTransferSyncUser> marketingTransferSyncUser);
    /**
     * custNum 在 30 天内有 type != 12
     * @param marketingTransferSyncUsers 转化数据集合
     */
    void excludeRuleFifth(List<MarketingTransferSyncUser> marketingTransferSyncUsers);

    /**
     * 基础数据里的数据 判断每条数据在 T 日是否命中 3710012 黑名单的数据 如果满足 不推
     * @param marketingTransferSyncUsers 转化数据集合
     */
    void excludeRuleSixth(List<MarketingTransferSyncUser> marketingTransferSyncUsers);


}
