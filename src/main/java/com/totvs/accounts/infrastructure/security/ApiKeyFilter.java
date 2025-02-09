package com.totvs.accounts.infrastructure.security;

import java.io.IOException;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ApiKeyFilter extends OncePerRequestFilter {

	private static final String API_KEY = "123456789";

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String apiKey = request.getHeader("Authorization");
		if (apiKey == null || !apiKey.equals("ApiKey " + API_KEY)) {
			response.setContentType("application/json");
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().write("{\"error\": \"API Key inválida ou ausente!\"}");
			response.getWriter().flush();
			response.getWriter().close();
			return;
		}
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("apikey_user",
				null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_USER")));
		SecurityContextHolder.getContext().setAuthentication(authentication);

		chain.doFilter(request, response);
	}
}
