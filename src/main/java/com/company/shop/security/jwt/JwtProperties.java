package com.company.shop.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

	private final String secret;
	private final long expiration;
	private final long refreshExpiration;

	// W Spring Boot 3.0+ adnotacja @ConstructorBinding jest opcjonalna,
	// jeśli klasa ma tylko jeden konstruktor.
	public JwtProperties(String secret, long expiration, long refreshExpiration) {
		this.secret = secret;
		this.expiration = expiration;
		this.refreshExpiration = refreshExpiration;
	}

	public String getSecret() {
		return secret;
	}

	public long getExpiration() {
		return expiration;
	}

	public long getRefreshExpiration() {
		return refreshExpiration;
	}
}