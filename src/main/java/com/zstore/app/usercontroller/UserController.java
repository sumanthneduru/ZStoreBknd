package com.zstore.app.usercontroller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zstore.app.entities.User;
import com.zstore.app.entities.UserDAO;
import com.zstore.app.userservice.UserService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/users")
public class UserController {
	private UserService service;

	public UserController(UserService service) {
		super();
		this.service = service;
	}
	
	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) { 
		try {
			UserDAO registeredUser = service.registerUser(user);
			return ResponseEntity.ok(Map.of("message", "User Registered Successfully", "user", registeredUser));
		} catch(Exception e) {
			return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
		}
	}
	
}
