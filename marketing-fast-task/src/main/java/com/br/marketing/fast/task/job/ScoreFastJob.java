package com.br.marketing.fast.task.job;



import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInterruptedException;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

public class ScoreFastJob {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;


    public Step scoreFirst(){
        return new Step() {
            @Override
            public String getName() {
                return null;
            }

            @Override
            public boolean isAllowStartIfComplete() {
                return false;
            }

            @Override
            public int getStartLimit() {
                return 0;
            }

            @Override
            public void execute(StepExecution stepExecution) throws JobInterruptedException {
                System.out.println("开始步骤");
            }
        };
    }


    @Bean
    public Job scoreFastJob() {
        return jobBuilderFactory.get("scoreFastJob")
                .start(scoreFirst())
                .build();
    }
}
