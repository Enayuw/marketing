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
    public static final String marketingTikiv = "marketing-tikiv";
    public static final String marketingTiFlash = "marketing-tiflash";
    public static final String MARKETING_DORIS = "marketing-doris";
    public static final String MARKETING_BI = "marketing-bi";
    public static final String MARKETING_LOG = "marketing-log";
    public static final String MARKETING_DATA_MAP = "marketing-datamap";


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

    /**
     * 切换Marketing Log数据源
     */
    @Before("logOfMarketing()")
    public void logOfMarketingInterceptor() {
        if(logger.isInfoEnabled()){
            logger.info("切换到数据源{}.......................", "Log");
        }
        DbContextHolder.setDbType(MARKETING_LOG);
    }


    /**
     * 切换Doris BI数据源
     */
    @Before("bIOfMarketing()")
    public void bIOfMarketingInterceptor() {
        if(logger.isInfoEnabled()){
            logger.info("切换到数据源{}.......................", "BI");
        }
        DbContextHolder.setDbType(MARKETING_BI);
    }

    /**
     * 切换Doris dataMap数据源
     */
    @Before("dataMapOfMarketing()")
    public void dataMapOfMarketingInterceptor() {
        if(logger.isInfoEnabled()){
            logger.info("切换到数据源{}.......................", "dataMap");
        }
        DbContextHolder.setDbType(MARKETING_DATA_MAP);
    }

    @After("tiKvOfMarketing()||tiflashOfMarketing()||dorisOfMarketing()||bIOfMarketing()||logOfMarketing()||dataMapOfMarketing()")
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

    @Pointcut(value = "@annotation(com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfBIMarketing)||execution(* com.br.marketing" +
            ".mapper.*.*bI_(..))")
    public void bIOfMarketing() {
    }

    @Pointcut(value = "@annotation(com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfLogMarketing)||execution(* com.br.marketing.mapper.*.*log_(..))" +
            "||execution(* com.br.marketing.mapper.datasource.log.*.*(..))")
    public void logOfMarketing() {

    }

    @Pointcut(value = "@annotation(com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfDataMapMarketing)||execution(* com.br.marketing" +
            ".mapper.*.*DM_(..))")
    public void dataMapOfMarketing() {
    }

}
