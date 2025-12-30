package com.company.shop.security.jwt;

import java.security.Key;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtTokenProvider {

	private final JwtProperties properties;
	private final Key key;

	public JwtTokenProvider(JwtProperties properties) {
		this.properties = properties;
		this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes());
	}

	public String generateToken(Authentication authentication) {

		Date now = new Date();
		Date expiry = new Date(now.getTime() + properties.getExpiration());

		return Jwts.builder().setSubject(authentication.getName()).setIssuedAt(now).setExpiration(expiry)
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String getUsername(String token) {
		return parseClaims(token).getBody().getSubject();
	}

	public boolean validate(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			return false;
		}
	}

	private Jws<Claims> parseClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
	}
}
