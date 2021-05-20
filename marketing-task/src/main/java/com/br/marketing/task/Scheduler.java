package com.br.marketing.task;


import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;

@ImportResource(locations = {"classpath:scheduler.xml"})
@SpringBootApplication(scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.br.marketing"})
@MapperScan("com.br.marketing.mapper")

@Slf4j
public class Scheduler {
    public static ConfigurableApplicationContext ac;
    /**
     * @param args incr 增量、all 全量、once 一次
     */
    public static void main(String[] args) {
        Long start=System.currentTimeMillis();
        log.warn("Scheduler开始启动！");
        ac= new SpringApplicationBuilder().sources(Scheduler.class).web(false).run(args);
        Long end =System.currentTimeMillis();
        log.warn("Scheduler启动结束，耗时{}",end-start);
//        String arg = args[0];
//        if(MonitorTypeEnum.PPD.getType().equals(arg)){
//            LoanWarningPpdServiceImpl loanWarningPpdServiceImpl=Scheduler.ac.getBean(LoanWarningPpdServiceImpl.class);
//            loanWarningPpdServiceImpl.process(args[1]);
//        }else if(MonitorTypeEnum.CHG.getType().equals(arg)){
//            LoanWarningChgServiceImpl loanWarningChgServiceImpl=Scheduler.ac.getBean(LoanWarningChgServiceImpl.class);
//            loanWarningChgServiceImpl.process(args[1]);
//        }else if(MonitorTypeEnum.HNNX.getType().equals(arg)){
//            LoanWarningHnnxServiceImpl loanWarningHNNXServiceImpl=Scheduler.ac.getBean(LoanWarningHnnxServiceImpl.class);
//            loanWarningHNNXServiceImpl.process(args[1]);
//        }else  if(MonitorTypeEnum.MARKETING.getType().equals(arg)){
//            LoanWarningMarketingServiceImpl loanWarningMarketingServiceImpl=Scheduler.ac.getBean(LoanWarningMarketingServiceImpl.class);
//            loanWarningMarketingServiceImpl.process(args[1]);
//        }else{
//            String params="";
//            if(args.length>1){
//                params=args[0]+","+args[1];
//            }
//            LoanWarningService loanWarningService= Scheduler.ac.getBean(LoanWarningServiceImpl.class);
//            loanWarningService.process(params);
//        }
    }

}
