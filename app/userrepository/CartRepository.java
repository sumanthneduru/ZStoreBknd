package com.zstore.app.userrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.zstore.app.entities.CartItems;

import jakarta.transaction.Transactional;

@Repository
public interface CartRepository extends JpaRepository<CartItems, Integer> {
	@Query("SELECT c FROM CartItems c WHERE c.user.userId = :userId AND c.product.productId = :productId")
	Optional<CartItems> findByUserAndProduct(@Param("userId") int userId, @Param("productId") int productId);

	@Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItems c WHERE c.user.userId = :userId")
	int countTotalItems(@Param("userId") int userId);

	@Query("SELECT c FROM CartItems c JOIN FETCH c.product p LEFT JOIN FETCH ProductImage pi ON p.productId = pi.product.productId WHERE c.user.userId = :userId")
	java.util.List<CartItems> findCartItemsWithProductDetails(int userId);
	
	@Query("UPDATE CartItems c SET c.quantity = :quantity where c.id = :cartId")
	void updateCartItemQuantity(int cartId, int quantity);
	
	@Modifying			//
	@Transactional		// these both will perform delete even for things are secure
	@Query("DELETE FROM CartItems c WHERE c.user.userId = :userId AND c.product.productId = :productId")
	void deleteCartItem(@Param("userId") int userId, @Param("productId") int productId);

	@Modifying			//
	@Transactional		// these both will perform delete even for things are secure
	@Query("DELETE FROM CartItems c WHERE c.user.userId = :userId")
	void deleteAllCartItemsByUserId(int userId);

}
