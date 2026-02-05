package com.zstore.app.userservicecontract;

import com.zstore.app.entities.User;
import com.zstore.app.entities.UserDAO;

public interface UserServiceContract {
	UserDAO registerUser(User user);
}
