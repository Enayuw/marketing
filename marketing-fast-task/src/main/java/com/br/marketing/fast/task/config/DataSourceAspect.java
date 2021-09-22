package com.br.marketing.fast.task.config;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class DataSourceAspect {
    Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);
    static  final String marketing_write="marketing";
    static final String marketing_read="marketing_read";

    /**
     * 切换数据源slave
     */
    @Before("accountreadPointcut()")
    public void accountSlaveInterceptor() {

//        if(optContext.getOptBizModule().getTimeSensitiveLevel()>90)
//        {
//            logger.debug("高时效性业务，Read使用主库");
//            logger.debug("切换到数据源{}..............................", "master");
//            DbContextHolder.setDbType(marketing_write);
//        }else {
            logger.debug("切换到数据源{}..............................", "slave");
            DbContextHolder.setDbType(marketing_read);
//        }

    }

    /**
     * 切换数据源master
     */
    @Before("accountwritePointcut()")
    public void accountMasterInterceptor() {
        logger.debug("切换到数据源{}.......................", "master");
        DbContextHolder.setDbType(marketing_write);
    }

    @After("releasePointcut()")
    public void afterInterceptor(){
        logger.debug("释放数据源{}.......................", "");
        DbContextHolder.clearDbType();
    }

    @Pointcut(value ="execution(* com.br.marketing.mapper.*.select*(..)) || " +
            "execution(* com.br.marketing.mapper.*.get*(..)) ||"+
            "execution(* com.br.marketing.mapper.*.count*(..))")
    private void accountreadPointcut() {
    }

    @Pointcut(value = "execution(* com.br.marketing.mapper.*.insert*(..)) || " +
            "execution(* com.br.marketing.mapper.*.update*(..)) || " +
            "execution(* com.br.marketing.mapper.*.delete*(..))")
    private void accountwritePointcut() {
    }
    
    @Pointcut(value = "execution(* com.br.marketing.mapper.*.insert*(..)) || " +
            "execution(* com.br.marketing.mapper.*.update*(..)) || " +
            "execution(* com.br.marketing.mapper.*.delete*(..)) ||"+
            "execution(* com.br.marketing.mapper.*.select*(..)) || " +
            "execution(* com.br.marketing.mapper.*.count*(..)) ||"+
            "execution(* com.br.marketing.mapper.*.get*(..))"
    )
    private void releasePointcut() {
    }
}
