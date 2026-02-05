package com.zstore.app.userservicecontract;

import java.util.Map;

import com.zstore.app.entities.User;

public interface OrderServiceContract {
	public Map<String, Object> getOrdersForUser(User user);
}
