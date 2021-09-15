package com.br.marketing.fast.task;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(exclude = {MultipartAutoConfiguration.class}, scanBasePackages = {"com.br.marketing"})
@EnableAspectJAutoProxy
@EnableDiscoveryClient
@MapperScan("com.br.marketing.mapper")
@EnableBatchProcessing(modular = true)
@Slf4j
public class FastTaskApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(FastTaskApplication.class, args);

        JobRegistry jobRegistry = run.getBean(JobRegistry.class);
        Job job = null;
        try {
            job = jobRegistry.getJob("scoreFastJob");
        } catch (NoSuchJobException e) {
            e.printStackTrace();
        }
        JobLauncher jobLauncher = run.getBean(JobLauncher.class);
        JobExecution jobExecution = null;
        try {
            jobExecution = jobLauncher.run(job,null);
        } catch (JobExecutionAlreadyRunningException e) {
            e.printStackTrace();
        } catch (JobRestartException e) {
            e.printStackTrace();
        } catch (JobInstanceAlreadyCompleteException e) {
            e.printStackTrace();
        } catch (JobParametersInvalidException e) {
            e.printStackTrace();
        }
        if (!jobExecution.getExitStatus().equals(ExitStatus.COMPLETED)) {
//            throw new RuntimeException(format("%s Job execution failed.", jobName));
        }

    }
}
