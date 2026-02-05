package com.zstore.app.userservicecontract;

import java.util.Map;

import com.zstore.app.entities.User;

public interface CartServiceContract {
	public void addToCart(int userId, int productId, int quantity);
	public Map<String, Object> getCartItems(int userId);
	public void updateCartItemQuantity(int userId, int productId, int quantity);
	public void deleteCartItem(int userId, int productId);
	public int countCartItems(User user);
}
