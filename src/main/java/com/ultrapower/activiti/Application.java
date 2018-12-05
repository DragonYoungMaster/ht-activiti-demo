package com.ultrapower.activiti;

import org.activiti.spring.boot.SecurityAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class
		, scanBasePackages = {"com.ultrapower.activiti"})
@EnableAutoConfiguration(exclude={
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
		, org.activiti.spring.boot.SecurityAutoConfiguration.class})
@ServletComponentScan
public class Application extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
