package com.zstore.app.userservicecontract;

import com.zstore.app.entities.User;

public interface AuthServiceContract {
	public User authenticate(String username, String password);
	public String generateToken(User user);
	public String generateNewToken(User user);
	void saveToken(User user, String token);
	boolean validateToken(String token);
	String extractUsername(String token); 
	public void logout(User user);
}
