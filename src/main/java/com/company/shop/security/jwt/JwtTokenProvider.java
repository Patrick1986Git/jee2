package com.company.shop.security.jwt;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

		// Pobieramy role i łączymy je w jeden ciąg znaków (np. "ROLE_USER,ROLE_ADMIN")
		String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		Map<String, Object> claims = new HashMap<>();
		claims.put("roles", authorities);

		return Jwts.builder().setClaims(claims) // Ustawienie własnych claimów
				.setSubject(authentication.getName()).setIssuedAt(now).setExpiration(expiry)
				.signWith(key, SignatureAlgorithm.HS256).compact();
	}

	public String getUsername(String token) {
		return parseClaims(token).getBody().getSubject();
	}

	/**
	 * Pobiera role zapisane w tokenie. Przyda się w filtrze do odbudowania obiektu
	 * Authentication.
	 */
	public String getRoles(String token) {
		return parseClaims(token).getBody().get("roles", String.class);
	}

	public boolean validate(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			// W Enterprise warto tutaj zalogować konkretny powód (np. expired vs invalid signature)
			return false;
		}
	}

	private Jws<Claims> parseClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
	}
}