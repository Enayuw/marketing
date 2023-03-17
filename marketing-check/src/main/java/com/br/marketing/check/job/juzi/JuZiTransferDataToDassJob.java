package com.br.marketing.check.job.juzi;

import com.br.marketing.check.service.JuZiPeriodPredicateService;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.mapper.DataDistributeDetailLogMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.PhoneSaleExtendInfoMapper;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @Autowired
    private List<JuZiPeriodPredicateService> juZiPeriodPredicateServiceList;

    @Autowired
    private PhoneSaleExtendInfoMapper phoneSaleExtendInfoMapper;



    @Autowired
    private DataDistributeDetailLogMapper dataDistributeDetailLogMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        executePeriodData("a",juZiPeriodPredicateServiceList);


        // 情况A
        boolean D_Continue = Boolean.TRUE;
        Long aminId = null;
        while (D_Continue) {
            // 查询转化数据表
            List<MarketingTransferSyncUser> juZiDRuleDataList = marketingTransferSyncUserMapper.getJuZiDRuleData(TCID, aminId);
            // 都没有推过才会继续执行，推过的数据要剔除掉。
            if (juZiDRuleDataList.size() <= 0) {
                D_Continue = Boolean.FALSE;
                continue;
            }
            aminId = juZiDRuleDataList.get(juZiDRuleDataList.size() - 1).getId() + 1;
            List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists = juZiDRuleDataList.stream().map(jz -> transferDataValidityPeriodService.getNewValidityPeriodTransferData(jz)).collect(Collectors.toList()).stream().filter(Objects::nonNull).collect(Collectors.toList());
            if(marketingTransferSyncUserCellLists.size()>0){
                // 查询电销推送日志表
                Set<String> toDassLogInfoSet = phoneSaleExtendInfoMapper.getToDassLogInfoList(marketingTransferSyncUserCellLists.get(0).getApiCode(), marketingTransferSyncUserCellLists.stream().map(MarketingTransferSyncUserCell::getCustNum).collect(Collectors.toSet()));
                // 查询决策推送日志表
                Set<String> distributionToDassLogInfoSet =dataDistributeDetailLogMapper.getToDataDistributeInfoList(marketingTransferSyncUserCellLists.get(0).getApiCode(), marketingTransferSyncUserCellLists.stream().map(MarketingTransferSyncUserCell::getCustNum).collect(Collectors.toSet()));
                // 合并2个集合
                Set<String> resultSet = new HashSet<>();
                Stream.of(toDassLogInfoSet, distributionToDassLogInfoSet).forEach(resultSet::addAll);
                // 判断集合和是否包含待推送数据。
                List<MarketingTransferSyncUserCell> toDassDataList = new ArrayList<>();
                for (MarketingTransferSyncUserCell marketingTransferSyncUserCellList : marketingTransferSyncUserCellLists) {
                    if (!resultSet.contains(marketingTransferSyncUserCellList.getCustNum())) {
                        toDassDataList.add(marketingTransferSyncUserCellList);
                    }
                }
                // 推送决策
                if(toDassDataList.size()>0){

                }

            }
            System.out.println(marketingTransferSyncUserCellLists);
        }


        //
    //    // 情况B
    //    boolean B_Continue = Boolean.TRUE;
    //    Long bminId = null;
    //    while (B_Continue) {
    //        List<MarketingTransferSyncUser> juZiARuleDataList = marketingTransferSyncUserMapper.getJuZiARuleData(TCID, bminId);
    //        if (juZiARuleDataList.size() <= 0) {
    //            B_Continue = Boolean.FALSE;
    //            continue;
    //        }
    //        bminId = juZiARuleDataList.get(juZiARuleDataList.size() - 1).getId() + 1;
    //        List<MarketingTransferSyncUserCell> MarketingTransferSyncUserCellLists = juZiARuleDataList.stream().map(jz -> transferDataValidityPeriodService.getNewValidityPeriodTransferData(jz)).collect(Collectors.toList()).stream().filter((marketingSyncUser) -> marketingSyncUser != null).collect(Collectors.toList());
    //        juZiCheckToDassService.checkTimeDataToDx("b",MarketingTransferSyncUserCellLists);
    //        System.out.println(MarketingTransferSyncUserCellLists);
    //    }
    //
    //    // 情况C
    //    boolean C_Continue = Boolean.TRUE;
    //    Long cminId = null;
    //    while (C_Continue) {
    //        List<MarketingTransferSyncUser> juZiARuleDataList = marketingTransferSyncUserMapper.getJuZiARuleData(TCID, cminId);
    //        if (juZiARuleDataList.size() <= 0) {
    //            C_Continue = Boolean.FALSE;
    //            continue;
    //        }
    //        cminId = juZiARuleDataList.get(juZiARuleDataList.size() - 1).getId() + 1;
    //        List<MarketingTransferSyncUserCell> MarketingTransferSyncUserCellLists = juZiARuleDataList.stream().map(jz -> transferDataValidityPeriodService.getNewValidityPeriodTransferData(jz)).collect(Collectors.toList()).stream().filter((marketingSyncUser) -> marketingSyncUser != null).collect(Collectors.toList());
    //        juZiCheckToDassService.checkTimeDataToDx("c",MarketingTransferSyncUserCellLists);
    //        System.out.println(MarketingTransferSyncUserCellLists);
    //    }
    }

    private void executePeriodData(String status ,  List<JuZiPeriodPredicateService> juZiPeriodPredicateServiceList) {
        // 情况A
        boolean A_Continue = Boolean.TRUE;
        Long aminId = null;
        while (A_Continue) {
            // 查询转化数据表
            List<MarketingTransferSyncUser> juZiARuleDataList = marketingTransferSyncUserMapper.getJuZiARuleData(TCID, aminId);
            // 都没有推过才会继续执行，推过的数据要剔除掉。
            if (juZiARuleDataList.size() <= 0) {
                A_Continue = Boolean.FALSE;
                continue;
            }
            aminId = juZiARuleDataList.get(juZiARuleDataList.size() - 1).getId() + 1;
            List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists = juZiARuleDataList.stream().map(jz -> transferDataValidityPeriodService.getNewValidityPeriodTransferData(jz)).collect(Collectors.toList()).stream().filter(Objects::nonNull).collect(Collectors.toList());
            if(marketingTransferSyncUserCellLists.size()>0){
                // 查询电销推送日志表
                Set<String> toDassLogInfoSet = phoneSaleExtendInfoMapper.getToDassLogInfoList(marketingTransferSyncUserCellLists.get(0).getApiCode(), marketingTransferSyncUserCellLists.stream().map(MarketingTransferSyncUserCell::getCustNum).collect(Collectors.toSet()));
                // 查询决策推送日志表
                Set<String> distributionToDassLogInfoSet =dataDistributeDetailLogMapper.getToDataDistributeInfoList(marketingTransferSyncUserCellLists.get(0).getApiCode(), marketingTransferSyncUserCellLists.stream().map(MarketingTransferSyncUserCell::getCustNum).collect(Collectors.toSet()));
                // 合并2个集合
                Set<String> resultSet = new HashSet<>();
                Stream.of(toDassLogInfoSet, distributionToDassLogInfoSet).forEach(resultSet::addAll);
                // 判断集合和是否包含待推送数据。
                List<MarketingTransferSyncUserCell> toDassDataList = new ArrayList<>();
                for (MarketingTransferSyncUserCell marketingTransferSyncUserCellList : marketingTransferSyncUserCellLists) {
                    if (!resultSet.contains(marketingTransferSyncUserCellList.getCustNum())) {
                        toDassDataList.add(marketingTransferSyncUserCellList);
                    }
                }
                // 推送daas
                if(toDassDataList.size()>0){
                    juZiPeriodPredicateServiceList.forEach(juZiPeriodPredicateService -> juZiPeriodPredicateService.transferDataPeriod(status,toDassDataList));
                }

            }
            System.out.println(marketingTransferSyncUserCellLists);
        }
    }

}
