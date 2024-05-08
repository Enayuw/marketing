package com.br.marketing.config.datasourceconfig;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(-1)
@ConditionalOnProperty(prefix = "datasource.database",name = "defaultSource",havingValue = "shardingmarketing",matchIfMissing = false)
public class DataSourceAspect {
    Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);
    public static final String marketingTikiv = "marketingTikiv";
    public static final String marketingTiFlash = "marketingTiFlash";
    public static final String MARKETING_DORIS = "marketingDoris";

    /**
     * 切换tikv数据源
     */
    @Before("tiKvOfMarketing()")
    public void tiKvOfMarketingInterceptor() {
        if(logger.isInfoEnabled()){
            logger.info("切换到数据源{}.......................", "tikv");
        }
        DbContextHolder.setDbType(marketingTikiv);
    }

    /**
     * 切换tiflash数据源
     */
    @Before("tiflashOfMarketing()")
    public void tiflashOfMarketingInterceptor() {
        if(logger.isInfoEnabled()){
            logger.info("切换到数据源{}.......................", "tiflash");
        }
        DbContextHolder.setDbType(marketingTiFlash);
    }

    /**
     * 切换Doris数据源
     */
    @Before("dorisOfMarketing()")
    public void dorisOfMarketingInterceptor() {
        if(logger.isInfoEnabled()){
            logger.info("切换到数据源{}.......................", "Doris");
        }
        DbContextHolder.setDbType(MARKETING_DORIS);
    }

    @After("tiKvOfMarketing()||tiflashOfMarketing()||dorisOfMarketing()")
    public void afterInterceptor() {
        if(logger.isInfoEnabled()){
            logger.info("释放数据源{}.......................", DbContextHolder.getDbType());
        }
        DbContextHolder.clearDbType();
    }

    @Pointcut(value = "@annotation(com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfTikvMarketing)||execution(* com.br.marketing.mapper.*.*tikv_(..))")
    public void tiKvOfMarketing() {
    }

    @Pointcut(value = "@annotation(com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfTiFlashMarketing)||execution(* com.br.marketing.mapper.*.*tiflash_(..))")
    public void tiflashOfMarketing() {
    }

    @Pointcut(value = "@annotation(com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfDorisMarketing)||execution(* com.br.marketing" +
            ".mapper.*.*doris_(..))")
    public void dorisOfMarketing() {
    }
}
