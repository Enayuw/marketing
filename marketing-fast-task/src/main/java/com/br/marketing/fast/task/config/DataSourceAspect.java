//package com.br.marketing.fast.task.config;
//
//import org.aspectj.lang.annotation.After;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.annotation.Before;
//import org.aspectj.lang.annotation.Pointcut;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//@Component
//@Aspect
//public class DataSourceAspect {
//    Logger logger = LoggerFactory.getLogger(DataSourceAspect.class);
//    @Autowired
//    private IOptContext optContext;
//    static  final String AOS_RBAC_WRITE="aos_account_write";
//    static final String AOS_RBAC_READ="aos_account_read";
////    static  final String AOS_LOG_WRITE="aos_log_write";
////    static final String AOS_LOG_READ="aos_log_read";
//
//    /**
//     * 切换数据源slave
//     */
//    @Before("accountreadPointcut()")
//    public void accountSlaveInterceptor() {
//
//        if(optContext.getOptBizModule().getTimeSensitiveLevel()>90)
//        {
//            logger.debug("高时效性业务，Read使用主库");
//            logger.debug("切换到数据源{}..............................", "master");
//            DbContextHolder.setDbType(AOS_RBAC_WRITE);
//        }else {
//            logger.debug("切换到数据源{}..............................", "slave");
//            DbContextHolder.setDbType(AOS_RBAC_READ);
//        }
//
//    }
//
//    /**
//     * 切换数据源master
//     */
//    @Before("accountwritePointcut()")
//    public void accountMasterInterceptor() {
//        logger.debug("切换到数据源{}.......................", "master");
//        DbContextHolder.setDbType(AOS_RBAC_WRITE);
//    }
//
////    /**
////     * 切换数据源slave
////     */
////    @Before("logreadPointcut()")
////    public void logSlaveInterceptor() {
////        logger.debug("切换到数据源log{}..............................", "slave");
////        DbContextHolder.setDbType(AOS_LOG_READ);
////    }
////
////    /**
////     * 切换数据源master
////     */
////    @Before("logwritePointcut()")
////    public void logMasterInterceptor() {
////        logger.debug("切换到数据源log{}.......................", "master");
////        DbContextHolder.setDbType(AOS_LOG_WRITE);
////    }
//
//    @After("releasePointcut()")
//    public void afterInterceptor(){
//        logger.debug("释放数据源{}.......................", "");
//        DbContextHolder.clearDbType();
//    }
//
//    @Pointcut(value ="execution(* com.fang.aos.borgservice.dao.*.select*(..)) || " +
//            "execution(* com.fang.aos.borgservice.dao.*.get*(..)) ||"+
//            "execution(* com.fang.aos.borgservice.dao.*.count*(..))")
//    private void accountreadPointcut() {
//    }
//
//    @Pointcut(value = "execution(* com.fang.aos.borgservice.dao.*.insert*(..)) || " +
//            "execution(* com.fang.aos.borgservice.dao.*.update*(..)) || " +
//            "execution(* com.fang.aos.borgservice.dao.*.delete*(..))")
//    private void accountwritePointcut() {
//    }
//
//
////    @Pointcut(value =
////            "execution(* com.fang.aos.borgservice.dao.log.*.select*(..)) || " +
////            "execution(* com.fang.aos.borgservice.dao.log.*.count*(..))")
////    private void logreadPointcut() {
////    }
////
////    @Pointcut(value =
////            "execution(* com.fang.aos.borgservice.dao.log.*.insert*(..)) || " +
////            "execution(* com.fang.aos.borgservice.dao.log.*.update*(..)) || " +
////            "execution(* com.fang.aos.borgservice.dao.log.*.delete*(..))")
////    private void logwritePointcut() {
////    }
//
//    @Pointcut(value = "execution(* com.fang.aos.borgservice.dao.*.insert*(..)) || " +
//            "execution(* com.fang.aos.borgservice.dao.*.update*(..)) || " +
//            "execution(* com.fang.aos.borgservice.dao.*.delete*(..)) ||"+
//            "execution(* com.fang.aos.borgservice.dao.*.select*(..)) || " +
//            "execution(* com.fang.aos.borgservice.dao.*.count*(..)) ||"+
//            "execution(* com.fang.aos.borgservice.dao.*.get*(..))"
////            "execution(* com.fang.aos.borgservice.dao.log.*.insert*(..)) || " +
////            "execution(* com.fang.aos.borgservice.dao.log.*.update*(..)) || " +
////            "execution(* com.fang.aos.borgservice.dao.log.*.delete*(..)) ||"+
////            "execution(* com.fang.aos.borgservice.dao.log.*.select*(..)) || " +
////            "execution(* com.fang.aos.borgservice.dao.log.*.count*(..))"
//    )
//    private void releasePointcut() {
//    }
//}
