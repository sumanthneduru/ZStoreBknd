package com.zstore.app.adminservice;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.zstore.app.adminservicecontract.AdminUserServiceContract;
import com.zstore.app.entities.Role;
import com.zstore.app.entities.User;
import com.zstore.app.userrepository.JWTTokenRepository;
import com.zstore.app.userrepository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class AdminUserService implements AdminUserServiceContract {

	private final UserRepository userRepository;
    private final JWTTokenRepository jwtTokenRepository;

    public AdminUserService(UserRepository userRepository, JWTTokenRepository jwtTokenRepository) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
    }
	
	@Override
	@Transactional
	public User modifyUser(Integer userId, String username, String email, String role) {
		// Check if the user exists
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        User existingUser = userOptional.get();
        // Update user fields
        if (username != null && !username.isEmpty()) {
            existingUser.setUsername(username);
        }
        if (email != null && !email.isEmpty()) {
            existingUser.setEmail(email);
        }
        if (role != null && !role.isEmpty()) {
            try {
                existingUser.setRole(Role.valueOf(role));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + role);
            }
        }

        // Delete associated JWT tokens
        jwtTokenRepository.deleteByUserId(userId);

        // Save updated user
        return userRepository.save(existingUser);
	}

	@Override
	public User getUserById(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

}
