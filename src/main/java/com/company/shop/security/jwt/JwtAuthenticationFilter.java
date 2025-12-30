package com.company.shop.security.jwt;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import com.company.shop.security.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.GenericFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class JwtAuthenticationFilter extends GenericFilter {

	private final JwtTokenProvider tokenProvider;
	private final UserDetailsServiceImpl userDetailsService;

	public JwtAuthenticationFilter(JwtTokenProvider tokenProvider, UserDetailsServiceImpl userDetailsService) {
		this.tokenProvider = tokenProvider;
		this.userDetailsService = userDetailsService;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest http = (HttpServletRequest) request;
		String header = http.getHeader("Authorization");

		if (header != null && header.startsWith("Bearer ")) {
			String token = header.substring(7);

			if (tokenProvider.validate(token)) {
				String username = tokenProvider.getUsername(token);
				var userDetails = userDetailsService.loadUserByUsername(username);

				var auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

				auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(http));
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
		}

		chain.doFilter(request, response);
	}
}
