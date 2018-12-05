package com.activiti.ultrapower;

import org.activiti.spring.SpringAsyncExecutor;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.activiti.spring.boot.AbstractProcessEngineAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;

import javax.sql.DataSource;

/**
 * author: teng.he
 * time: 15:08 2018/12/4
 * desc:
 */
@Configuration
@PropertySource({"classpath:application.properties"})
public class ActivitiConfig extends AbstractProcessEngineAutoConfiguration {
  @Bean
  @Primary
  @ConfigurationProperties(prefix = "spring.datasource")
  public DataSource activitiDataSource() {
    return DataSourceBuilder.create().build();
  }

  @Bean
  public SpringProcessEngineConfiguration springProcessEngineConfiguration(
      PlatformTransactionManager transactionManager,
      SpringAsyncExecutor springAsyncExecutor) throws IOException {

    return baseSpringProcessEngineConfiguration(
        activitiDataSource(),
        transactionManager,
        springAsyncExecutor);
  }
}
