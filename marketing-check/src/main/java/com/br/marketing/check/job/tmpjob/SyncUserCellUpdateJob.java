package com.br.marketing.check.job.tmpjob;

import cn.hutool.core.collection.ListUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.MarketingCustomerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SyncUserCellUpdateJob extends AbstractSimpleElasticJob {


    @Autowired
    MarketingCustomerService marketingCustomerService;

    @Autowired
    IMarketingSyncUserService iMarketingSyncUserService;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        List<String> prefixApiCode = !ObjectUtils.isEmpty(marketingCommonConfig.getUpdateCellApiCodePrefix())
                ? marketingCommonConfig.getUpdateCellApiCodePrefix()
                : Arrays.asList("3", "4");
        List<String> codes = marketingCustomerService.getApiCodeByProd(prefixApiCode);


        for (String code : codes) {
            try {
                if (!iMarketingSyncUserService.existUploadTable(code)) {
                    continue;
                }
            } catch (Exception ex) {
                continue;
            }
            JSONObject noDesCleanConfig = JSONObject.parseObject(marketingCommonConfig.getNoDesCleanConfig());
            Integer threadSize = noDesCleanConfig.getInteger("threadSize");
            ThreadPoolExecutor threadPool = BrExecutors
                    .getThreadPool(threadSize, threadSize);
            Boolean mark = Boolean.TRUE;
            Long minId = iMarketingSyncUserService.noDesUploadOfMinId(code);
            if (minId == null) {
                continue;
            }
            log.warn(String.format("apiCode:【%s】清洗开始", code));
            minId = minId - 1;
            while (mark) {
                List<MarketingSyncUser> marketingSyncUsers = iMarketingSyncUserService.noDesUploadByMinId(code, minId);
                if (marketingSyncUsers.size() <= 0) {
                    mark = Boolean.FALSE;
                    continue;
                }
                JSONObject updateConfig = JSONObject.parseObject(marketingCommonConfig.getNoDesCleanConfig());
                if (updateConfig != null && updateConfig.getBoolean("isPause")) {
                    mark = Boolean.FALSE;
                    continue;
                }
                minId = marketingSyncUsers.get(marketingSyncUsers.size() - 1).getId();
                if (updateConfig != null && !threadSize.equals(updateConfig.getInteger("threadSize"))) {
                    threadSize = updateConfig.getInteger("threadSize");
                    ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, threadSize);
                }
                threadPool.submit(() -> {
                    try {
                        for (MarketingSyncUser marketingSyncUser : marketingSyncUsers) {
                            String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
                            String md5 = Md5Utils.cell32(cell);
                            String sha256 = Sha256Util.getSHA256Encrypt(cell);
                            marketingSyncUser.setCellMd5(md5);
                            marketingSyncUser.setCellSha256(sha256);
                        }

                        List<List<MarketingSyncUser>> partition = ListUtils.partition(marketingSyncUsers, 50);
                        String logSql = "";
                        for (List<MarketingSyncUser> syncUsers : partition) {
                            try {
                                StringBuilder updateSql = new StringBuilder();
                                updateSql.append(String.format("update b_marketing_sync_%s set ", code));
                                StringBuilder md5Sql = new StringBuilder();
                                StringBuilder shaSql = new StringBuilder();
                                StringBuilder whereSql = new StringBuilder();

                                md5Sql.append(" cell_md5=( case id  ");
                                shaSql.append(" cell_sha256=( case id ");
                                whereSql.append(" where id in ( ");
                                for (MarketingSyncUser syncUser : syncUsers) {
                                    md5Sql.append(String.format(" when %d then '%s' "
                                            , syncUser.getId(), syncUser.getCellMd5()));
                                    shaSql.append(String.format(" when %d then '%s' "
                                            , syncUser.getId(), syncUser.getCellSha256()));
                                    whereSql.append(String.format(" %d,", syncUser.getId()));
                                }
                                md5Sql.append(" end )");
                                shaSql.append(" end )");
                                String whereSqlStr = whereSql.toString().substring(0, whereSql.toString().length() - 1).concat(" )");
                                String updateSqlStr = updateSql.append(md5Sql).append(",").append(shaSql).append(whereSqlStr).toString();
                                logSql = updateSqlStr;
                                iMarketingSyncUserService.updateSqlByNoDes(updateSqlStr);
                            } catch (Exception ex) {
                                log.error(ex.getMessage(), ex);
                                log.warn(logSql);
                            }
                        }
                    } catch (Exception ex) {
                        log.error(ex.getMessage(), ex);
                    }
                });
            }
            threadPool.shutdown();
            try {
                while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                    log.warn(String.format("apiCode:【%s】清洗结束", code));
                }
            } catch (InterruptedException ex) {
                log.warn(ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }

        }


    }
}
