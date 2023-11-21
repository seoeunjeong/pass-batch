package com.batch.job.pass.addPasses;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AddPassesJobConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final AddPassesTasklet addPassesTasklet;

    public AddPassesJobConfig(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory, AddPassesTasklet addPassesTasklet) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
        this.addPassesTasklet = addPassesTasklet;
    }

    @Bean
    public Job addPassesJob(){
        return jobBuilderFactory.get("addPassesJob")
                .start(addPassesStep())
                .build();
    }

    @Bean
    public Step addPassesStep() {
        return stepBuilderFactory.get("addPassesStep")
                .tasklet(addPassesTasklet)
                .build();
    }
}
