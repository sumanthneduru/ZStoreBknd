package com.zstore.app.usercontroller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zstore.app.entities.User;
import com.zstore.app.userservicecontract.CartServiceContract;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api/cart")
public class CartController {

	private CartServiceContract cartService;

	public CartController(CartServiceContract cartService) {
		super();
		this.cartService = cartService;
	}

	@PostMapping("/add")
	@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
	public ResponseEntity<Void> addToCart(@RequestBody Map<String, Object> request, HttpServletRequest servletRequest) {

//		String username = (String) request.get("username");
		int productId = (int) request.get("productId");

		// Handle quantity: Default to 1 if not provided
		int quantity = request.containsKey("quantity") ? (int) request.get("quantity") : 1;

		User user = (User) servletRequest.getAttribute("authenticatedUser");
		// Fetch the user using username (by hitting to db)
//		User user = userRepository.findByUsername(username)
//				.orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

		// Add the product to the cart
		cartService.addToCart(user.getUserId(), productId, quantity);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@GetMapping("/items")
	public ResponseEntity<Map<String, Object>> getCartItems(HttpServletRequest request) {

		// Assuming you store authenticated user in request attribute (common in
		// JWT/filter setups)
		User user = (User) request.getAttribute("authenticatedUser");

		Map<String, Object> cartItems = cartService.getCartItems(user.getUserId());

		return ResponseEntity.ok(cartItems);
	}

	@PutMapping("/update")
	public ResponseEntity<Void> updateCartItemQuantity(@RequestBody Map<String, Object> request,
			HttpServletRequest servletRequest) {
		int productId = (int) request.get("productId");
		int quantity = (int) request.get("quantity");

		// Fetch the user using username from filter it has authenticated user
		User user = (User) servletRequest.getAttribute("authenticatedUser");

		// Update the cart item quantity
		cartService.updateCartItemQuantity(user.getUserId(), productId, quantity);

		return ResponseEntity.status(HttpStatus.OK).build();
	}

	@DeleteMapping("/delete")
	public ResponseEntity<Void> deleteCartItem(@RequestBody Map<String, Object> request,
			HttpServletRequest servletRequest) {
		String username = (String) request.get("username");
		int productId = (int) request.get("productId");

		// Fetch the user using username
		User user = (User) servletRequest.getAttribute("authenticatedUser");

		// Delete the cart item
		cartService.deleteCartItem(user.getUserId(), productId);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@DeleteMapping("/delete/{productId}")
	public ResponseEntity<Void> deleteCartItem(@PathVariable int productId, HttpServletRequest servletRequest) {

		User user = (User) servletRequest.getAttribute("authenticatedUser");
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		cartService.deleteCartItem(user.getUserId(), productId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/items/count")
	public ResponseEntity<Integer> countCart(HttpServletRequest httpServletRequest, @RequestParam String username) {
		int cartCount = cartService.countCartItems((User) httpServletRequest.getAttribute("authenticatedUser"));
		return ResponseEntity.ok(cartCount);
	}

}