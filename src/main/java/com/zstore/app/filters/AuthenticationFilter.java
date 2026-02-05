package com.zstore.app.filters;

import com.zstore.app.entities.Role;
import com.zstore.app.entities.User;
import com.zstore.app.userrepository.UserRepository;
import com.zstore.app.userservice.AuthService;
import com.zstore.app.userservicecontract.AuthServiceContract;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@WebFilter(urlPatterns = { "/api/*", "/admin/*" })
@Component
public class AuthenticationFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

	private final AuthServiceContract authService;
	private final UserRepository userRepository;

	private static final String ALLOWED_ORIGIN = "http://localhost:5173";

	private static final String[] UNAUTHENTICATED_PATHS = { "/api/users/register", "/api/auth/login" };

	public AuthenticationFilter(AuthServiceContract authService, UserRepository userRepository) {
		System.out.println("Authentication Filter Started");
		this.authService = authService;
		this.userRepository = userRepository;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;

		try {
			executeFilterLogic(httpRequest, httpResponse, chain);
		} catch (Exception e) {
			logger.error("Unexpected error in AuthenticationFilter", e);
			sendErrorResponse(httpResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal server error");
		}
	}

	private void executeFilterLogic(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		String requestURI = request.getRequestURI();
		logger.info("Request URI: {}", requestURI);

		// Allow unauthenticated paths
		if (Arrays.asList(UNAUTHENTICATED_PATHS).contains(requestURI)) {
			chain.doFilter(request, response);
			return;
		}

		// Handle preflight (OPTIONS) requests
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			setCORSHeaders(response);
		    chain.doFilter(request, response);
			return;
		}

		// Extract token from cookies
		String token = getAuthTokenFromCookies(request);
		if (token == null || !authService.validateToken(token)) {
			sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: Invalid or missing token");
			return;
		}

		// Extract username
		String username = authService.extractUsername(token);
		Optional<User> userOptional = userRepository.findByUsername(username);

		if (userOptional.isEmpty()) {
			sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: User not found");
			return;
		}

		User authenticatedUser = userOptional.get();
		Role role = authenticatedUser.getRole();

		logger.info("Authenticated User: {}, Role: {}", authenticatedUser.getUsername(), role);

		// Role-based access control
		if (requestURI.startsWith("/admin") && role != Role.ADMIN) {
			sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Admin access required");
			return;
		}

		if (requestURI.startsWith("/api") && role != Role.CUSTOMER && role != Role.ADMIN) {
			sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden: Customer access required");
			return;
		}

		// Attach authenticated user to request
		request.setAttribute("authenticatedUser", authenticatedUser);

		chain.doFilter(request, response);
	}

	private void setCORSHeaders(HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", ALLOWED_ORIGIN);
		response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
		response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setStatus(HttpServletResponse.SC_OK);
	}

	private void sendErrorResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
		response.setStatus(statusCode);
		response.getWriter().write(message);
	}

	private String getAuthTokenFromCookies(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies != null) {
			return Arrays.stream(cookies).filter(c -> "authToken".equals(c.getName())).map(Cookie::getValue).findFirst()
					.orElse(null);
		}
		return null;
	}
}
