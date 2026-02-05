package com.zstore.app.userservice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.zstore.app.entities.CartItems;
import com.zstore.app.entities.Order;
import com.zstore.app.entities.OrderItem;
import com.zstore.app.entities.OrderStatus;
import com.zstore.app.userrepository.CartRepository;
import com.zstore.app.userrepository.OrderItemRepository;
import com.zstore.app.userrepository.OrderRepository;
import com.zstore.app.userservicecontract.PaymentServiceContract;

import jakarta.transaction.Transactional;

@Service
public class PaymentService implements PaymentServiceContract {

	@Value("${razorpay.key.id}")
	private String razorpayKeyId;

	@Value("${razorpay.key.secret}")
	private String razorpayKeySecret;

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final CartRepository cartRepository;

	public PaymentService(OrderRepository orderRepository, OrderItemRepository orderItemRepository, CartRepository cartRepository) {
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.cartRepository = cartRepository;
	}

	@Override
	@Transactional
	public String createOrder(int userId, BigDecimal totalAmount, List<OrderItem> cartItems) throws RazorpayException {

		RazorpayClient client = new RazorpayClient(razorpayKeyId, razorpayKeySecret);

		JSONObject request = new JSONObject();
		request.put("amount", totalAmount.multiply(BigDecimal.valueOf(100)).intValue());
		request.put("currency", "INR");
		request.put("receipt", "txn_" + System.currentTimeMillis());

		com.razorpay.Order razorpayOrder = client.orders.create(request);

		Order order = new Order();
		order.setOrderId(razorpayOrder.get("id"));
		order.setUserId(userId);
		order.setTotalAmount(totalAmount);
		order.setStatus(OrderStatus.PENDING);
		order.setCreatedAt(LocalDateTime.now());

		orderRepository.save(order);

		return razorpayOrder.get("id");
	}

	@Override
	@Transactional
	public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature,
			int userId) {

		try {
			JSONObject attributes = new JSONObject();
			attributes.put("razorpay_order_id", razorpayOrderId);
			attributes.put("razorpay_payment_id", razorpayPaymentId);
			attributes.put("razorpay_signature", razorpaySignature);

			boolean isValid = Utils.verifyPaymentSignature(attributes, razorpayKeySecret);

			if (isValid) {
				Order order = orderRepository.findById(razorpayOrderId)
						.orElseThrow(() -> new RuntimeException("Order not found"));

				order.setStatus(OrderStatus.SUCCESS);
				order.setUpdatedAt(LocalDateTime.now());
				orderRepository.save(order);

				List<CartItems> cartItems = cartRepository.findCartItemsWithProductDetails(userId);

				for (CartItems cartItem : cartItems) {
					OrderItem item = new OrderItem();
					item.setOrder(order);
					item.setProductId(cartItem.getProduct().getProductId());
					item.setQuantity(cartItem.getQuantity());
					item.setPricePerUnit(cartItem.getProduct().getPrice());
					item.setTotalPrice(
							cartItem.getProduct().getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
					orderItemRepository.save(item);
				}

				cartRepository.deleteAllCartItemsByUserId(userId);
				return true;
			}
			return false;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}