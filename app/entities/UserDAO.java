package com.zstore.app.entities;

public class UserDAO {
	Role role;
	String username;
	String email;
	public UserDAO() {
	}
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public UserDAO(Role role, String username, String email) {
		super();
		this.role = role;
		this.username = username;
		this.email = email;
	}
}
