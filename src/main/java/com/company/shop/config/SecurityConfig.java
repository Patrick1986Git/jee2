package com.company.shop.config;

import com.company.shop.security.SecurityConstants;
import com.company.shop.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final UserDetailsServiceImpl userDetailsService;

	public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable()) // JWT-ready
				.userDetailsService(userDetailsService)
				.authorizeHttpRequests(auth -> auth.requestMatchers(SecurityConstants.PUBLIC_ENDPOINTS).permitAll()
						.requestMatchers("/admin/**").hasRole("ADMIN").anyRequest().authenticated())
				.formLogin(form -> form.disable()) // REST/JWT
				.httpBasic(basic -> basic.disable()); // REST/JWT

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
