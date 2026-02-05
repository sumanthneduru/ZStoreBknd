 package com.zstore.app.userservice;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.zstore.app.entities.JWTToken;
import com.zstore.app.entities.User;
import com.zstore.app.userrepository.JWTTokenRepository;
import com.zstore.app.userrepository.UserRepository;
import com.zstore.app.userservicecontract.AuthServiceContract;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class AuthService implements AuthServiceContract {
	
	private final Key SIGNING_KEY;
	
	private final JWTTokenRepository tokenRepository;
	private final UserRepository userRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	public AuthService(JWTTokenRepository tokenRepository, UserRepository userRepository, @Value("${jwt.secret}") String jwtSecret) {
		super();
		this.tokenRepository = tokenRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = new BCryptPasswordEncoder();
		
		if(jwtSecret.getBytes(StandardCharsets.UTF_8).length < 64) {
			throw new IllegalArgumentException("JWTSecret in app.prop must be > 64 bytes long for HS512");
		}
		
		this.SIGNING_KEY = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public User authenticate(String username, String password) {
		// TODO Auto-generated method stub
		User user = userRepository.findByUsername(username).orElseThrow(()->new RuntimeException("Invalid username")); 
		if(!passwordEncoder.matches(password, user.getPassword())) {
			throw new RuntimeException("Invalid Password");
		}
		return user;
	}

	@Override
	public String generateToken(User user) {
		String token;
		LocalDateTime now = LocalDateTime.now();
		JWTToken existingToken = tokenRepository.findByUserId(user.getUserId());
		if(existingToken!= null && now.isBefore(existingToken.getExpiresAt())) {
			token = existingToken.getToken();
		} else {
			token = generateNewToken(user);
			if(existingToken != null) {
				tokenRepository.delete(existingToken);
			}
			saveToken(user, token);
		}
		return token;
	}

	@Override
	public String generateNewToken(User user) {
		return Jwts.builder()
				.setSubject(user.getUsername())
				.claim("role", user.getRole().name())
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 3600000))
				.signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
				.compact();
	}

	@Override
	public void saveToken(User user, String token) {
		JWTToken jwtToken = new JWTToken(user, token, LocalDateTime.now().plusHours(1));
		tokenRepository.save(jwtToken);
	}
	@Override
	public boolean validateToken(String token) {
	    try {
	        System.err.println("VALIDATING TOKEN...");

	        // Parse and validate the JWT signature & expiry
	        Jwts.parserBuilder()
	                .setSigningKey(SIGNING_KEY)
	                .build()
	                .parseClaimsJws(token);

	        // Check if token exists in DB and is not expired
	        Optional<JWTToken> jwtToken =
	                tokenRepository.findByToken(token);

	        if (jwtToken.isPresent()) {
	            System.err.println("Token Expiry: " +
	                    jwtToken.get().getExpiresAt());
	            System.err.println("Current Time: " +
	                    LocalDateTime.now());

	            return jwtToken.get()
	                    .getExpiresAt()
	                    .isAfter(LocalDateTime.now());
	        }

	        return false;

	    } catch (Exception e) {
	        System.err.println("Token validation failed: " + e.getMessage());
	        return false;
	    }
	}

	@Override
	public String extractUsername(String token) {
	    return Jwts.parserBuilder()
	            .setSigningKey(SIGNING_KEY)
	            .build()
	            .parseClaimsJws(token)
	            .getBody()
	            .getSubject();
	}

	@Override
	public void logout(User user) {
        int userId = user.getUserId();

        // Fetch JWT token stored for the user
        JWTToken token = tokenRepository.findByUserId(userId);

        // Delete token if exists (invalidate session)
        if (token != null) {
        	tokenRepository.deleteByUserId(userId);
        }
    }

}
