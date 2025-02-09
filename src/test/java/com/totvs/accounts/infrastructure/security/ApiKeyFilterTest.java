package com.totvs.accounts.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public class ApiKeyFilterTest {

	private ApiKeyFilter apiKeyFilter;
	private MockHttpServletRequest request;
	private MockHttpServletResponse response;
	private FilterChain filterChain;

	@BeforeEach
	public void setUp() {
		apiKeyFilter = new ApiKeyFilter();
		request = new MockHttpServletRequest();
		response = new MockHttpServletResponse();
		filterChain = mock(FilterChain.class);
		SecurityContextHolder.clearContext();
	}

	@AfterEach
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void testDoFilterInternal_ValidApiKey() throws ServletException, IOException {
		request.addHeader("Authorization", "ApiKey 123456789");
		apiKeyFilter.doFilter(request, response, filterChain);
		verify(filterChain, times(1)).doFilter(request, response);
		UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) SecurityContextHolder
				.getContext().getAuthentication();
		assertNotNull(auth);
		assertEquals("apikey_user", auth.getPrincipal());
		assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_API_USER")));
	}

	@Test
	public void testDoFilterInternal_MissingApiKey() throws ServletException, IOException {
		apiKeyFilter.doFilter(request, response, filterChain);
		verify(filterChain, never()).doFilter(any(), any());
		assertEquals(401, response.getStatus());
		assertEquals("application/json", response.getContentType());
		String content = response.getContentAsString();
		assertTrue(content.contains("API Key inválida ou ausente!"));
	}

	@Test
	public void testDoFilterInternal_InvalidApiKey() throws ServletException, IOException {
		request.addHeader("Authorization", "ApiKey wrongkey");
		apiKeyFilter.doFilter(request, response, filterChain);
		verify(filterChain, never()).doFilter(any(), any());
		assertEquals(401, response.getStatus());
		assertEquals("application/json", response.getContentType());
		String content = response.getContentAsString();
		assertTrue(content.contains("API Key inválida ou ausente!"));
	}
}
