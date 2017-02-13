package com.crossover.trial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan({ "com.crossover.trial.weather.builder", "com.crossover.trial.weather.service",
		"com.crossover.trial.weather.restservice,com.crossover.trial.weather" })
@PropertySource("classpath:application.properties")
public class AppConfig {

	@Bean
	public PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}