package com.zstore.app.userservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.zstore.app.entities.CartItems;
import com.zstore.app.entities.Product;
import com.zstore.app.entities.ProductImage;
import com.zstore.app.entities.User;
import com.zstore.app.userrepository.CartRepository;
import com.zstore.app.userrepository.ProductImageRepository;
import com.zstore.app.userrepository.ProductRepository;
import com.zstore.app.userrepository.UserRepository;
import com.zstore.app.userservicecontract.CartServiceContract;

@Service
public class CartService implements CartServiceContract {

	UserRepository userRepository;
	ProductRepository productRepository;
	CartRepository cartRepository;
	ProductImageRepository productImageRepository;

	public CartService(UserRepository userRepository, ProductRepository productRepository, CartRepository cartRepository, ProductImageRepository productImageRepository) {
		super();
		this.userRepository = userRepository;
		this.productRepository = productRepository;
		this.cartRepository = cartRepository;
		this.productImageRepository = productImageRepository;
	}

	@Override
	public void addToCart(int userId, int productId, int quantity) {

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

		// Fetch cart item for this userId and productId
		Optional<CartItems> existingItem = cartRepository.findByUserAndProduct(userId, productId);

		if (existingItem.isPresent()) {
			CartItems cartItem = existingItem.get();
			cartItem.setQuantity(cartItem.getQuantity() + quantity);
			cartRepository.save(cartItem);
		} else {
			CartItems newItem = new CartItems(user, product, quantity);
			cartRepository.save(newItem);
		}
	}

	@Override
	public Map<String, Object> getCartItems(int userId) {
		// Fetch the cart items for the user with product details
		List<CartItems> cartItems = cartRepository.findCartItemsWithProductDetails(userId);

		// Create response map
		Map<String, Object> response = new HashMap<>();
		User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

		response.put("username", user.getUsername());
		response.put("role", user.getRole().toString());

		// List to hold product details
		List<Map<String, Object>> products = new ArrayList<>();
		double overallTotalPrice = 0;

		for (CartItems cartItem : cartItems) {
			Map<String, Object> productDetails = new HashMap<>();

			// Get product
			Product product = cartItem.getProduct();

			// Get product images
			List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(product.getProductId());

			String imageUrl = (productImages != null && !productImages.isEmpty()) ? productImages.get(0).getImageUrl() : "default-image-url";

			// Populate product details
			productDetails.put("product_id", product.getProductId());
			productDetails.put("image_url", imageUrl);
			productDetails.put("name", product.getName());
			productDetails.put("description", product.getDescription());
			productDetails.put("price_per_unit", product.getPrice());
			productDetails.put("quantity", cartItem.getQuantity());
			productDetails.put("total_price", cartItem.getQuantity() * product.getPrice().doubleValue());

			// Add to products list
			products.add(productDetails);

			// Update overall total
			overallTotalPrice += cartItem.getQuantity() * product.getPrice().doubleValue();
		}

		// Prepare final cart response
		Map<String, Object> cart = new HashMap<>();
		cart.put("products", products);
		cart.put("overall_total_price", overallTotalPrice);

		response.put("cart", cart);

		return response;
	}

	@Override
	public void updateCartItemQuantity(int userId, int productId, int quantity) {
	    User user = userRepository.findById(userId)
	        .orElseThrow(() -> new IllegalArgumentException("User not found"));

	    Product product = productRepository.findById(productId)
	        .orElseThrow(() -> new IllegalArgumentException("Product not found"));

	    // Fetch cart item for this userId and productId
	    Optional<CartItems> existingItem = cartRepository
	        .findByUserAndProduct(userId, productId);

	    if (existingItem.isPresent()) {
	        CartItems cartItem = existingItem.get();
	        if (quantity == 0) {
	            deleteCartItem(userId, productId);
	        } else {
	            cartItem.setQuantity(quantity);
	            cartRepository.save(cartItem);
	        }
	    }
	}
	
	@Override
	public void deleteCartItem(int userId, int productId) {
	    User user = userRepository.findById(userId)
	        .orElseThrow(() -> new IllegalArgumentException("User not found"));

	    Product product = productRepository.findById(productId)
	        .orElseThrow(() -> new IllegalArgumentException("Product not found"));

	    cartRepository.deleteCartItem(userId, productId);
	}

	@Override
	public int countCartItems(User user) {
		return cartRepository.countTotalItems(user.getUserId());
	}
	
	

}
