package com.br.marketing.check.job.juzi;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/3/14 10:57
 */

@Component
@Slf4j
public class JuZiTransferDataToDassJob extends AbstractSimpleElasticJob {
    /*
    * 推送逻辑：
    a 情况：查询转化数据表 b_marketing_transfer_sync_3874 根据条件 custNum & ifApply=1 & applyResult为空或null & auditAmount>=1000 & applyDt为T-1日， create_time 判断 匹配上传数据里的有效期内最新一条推送dass。

    b 情况：根据apiCode, user_type=a ,audit_amount-lent_amount >= 1000的custNum 时间为 T-2 和 T-6 且 ， 查询电销推送记录表  2 和 6  包含T，查询出当天需要推送的2天前和6天前的数据。找出当前数据的上传表里有效期内最新的一条数据。

    c 情况：查询转化数据表 b_marketing_transfer_sync_3874 根据条件 create_time为当天，ifApply=1 & applyResult为空或null &auditAmount≥1000 & loginTime为T-1日 & audit_amount-lent_amount >= 1000的custNum 。找出当前数据上传表里有效期内最新的一条数据。

    d 情况：查询转化数据表 b_marketing_transfer_sync_3874 根据条件 create_time为当天 ifLogin=1 & ifApply为空或null & applyResult为空或null & ifLent为空或null
 不需要判断有效期
 * */

    private final  static  String TCID = "7780";

    @Autowired
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Autowired
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        // 情况A
        boolean isContiue = Boolean.TRUE;
        Long minId = null;
        while (isContiue) {
            List<MarketingTransferSyncUser> juZiARuleDataList = marketingTransferSyncUserMapper.getJuZiARuleData(TCID, minId);
            if (juZiARuleDataList.size() <= 0) {
                isContiue = Boolean.FALSE;
                continue;
            }
            minId = juZiARuleDataList.get(juZiARuleDataList.size() - 1).getId() + 1;
            List<MarketingSyncUser> collect = juZiARuleDataList.stream().map(jz -> transferDataValidityPeriodService.getNewValidityPeriodData(jz)).collect(Collectors.toList()).stream().filter((marketingSyncUser) -> marketingSyncUser != null).collect(Collectors.toList());
            System.out.println(collect);
        }
    }
}
