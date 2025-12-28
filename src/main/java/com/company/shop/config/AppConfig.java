package com.company.shop.config;

import com.company.shop.common.model.AuditAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AppConfig {

	@Bean
	public AuditAwareImpl auditorProvider() {
		return new AuditAwareImpl();
	}
}
