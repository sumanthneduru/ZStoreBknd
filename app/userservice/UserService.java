package com.zstore.app.userservice;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.zstore.app.entities.User;
import com.zstore.app.entities.UserDAO;
import com.zstore.app.userrepository.UserRepository;
import com.zstore.app.userservicecontract.UserServiceContract;

@Service
public class UserService implements UserServiceContract {
	
	private UserRepository repository;
	private BCryptPasswordEncoder passwordEncoder;
	
	public UserService(UserRepository repository) {
		super();
		this.repository = repository;
		this.passwordEncoder = new BCryptPasswordEncoder();
	}


	@Override
	public UserDAO registerUser(User user) {
		if(repository.findByEmail(user.getEmail()).isPresent()) {
			throw new RuntimeException("Email is already registered");
		}
		if(repository.findByUsername(user.getUsername()).isPresent()) {
			throw new RuntimeException("Username is already taken");
		}
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		UserDAO dao = new UserDAO(user.getRole(), user.getUsername(), user.getEmail());
		repository.save(user);
		return dao;
	}

}
