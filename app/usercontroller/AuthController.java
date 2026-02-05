package com.zstore.app.usercontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zstore.app.entities.LoginReqDTO;
import com.zstore.app.entities.User;
import com.zstore.app.userservice.AuthService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // allowCredentials = "true" let cookies
																			// allow to method
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService service;

	public AuthController(AuthService service) {
		this.service = service;
	}

	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginReqDTO dto, HttpServletResponse response) {
		try {
			User user = service.authenticate(dto.getUsername(), dto.getPassword());
			String token = service.generateToken(user);

			response.addHeader("Set-Cookie",
					String.format("authToken=%s;HttpOnly;Path=/;Max-Age=3600;SameSite=none;Secure", token));

			return ResponseEntity.ok(Map.of("message", "Login Successful", "userName", user.getUsername(), "role",
					user.getRole().name()));

		} catch (RuntimeException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("Error", e.getMessage()));
		}
	}

	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {

		try {
			// Get authenticated user from request (set by filter)
			User user = (User) request.getAttribute("authenticatedUser");

			if (user == null) {
				return ResponseEntity.status(401).body(Map.of("message", "User not authenticated"));
			}

			// Logout logic (delete token from DB)
			service.logout(user);

			// Clear auth cookie
			Cookie cookie = new Cookie("authToken", null);
			cookie.setHttpOnly(true);
			cookie.setMaxAge(0);
			cookie.setPath("/");
			response.addCookie(cookie);

			Map<String, String> responseBody = new HashMap<>();
			responseBody.put("message", "Logout successful");

			return ResponseEntity.ok(responseBody);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("message", "Logout failed"));
		}
	}
}
