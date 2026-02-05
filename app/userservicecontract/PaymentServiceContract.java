package com.zstore.app.userservicecontract;

import java.math.BigDecimal;
import java.util.List;

import com.razorpay.RazorpayException;
import com.zstore.app.entities.OrderItem;

import jakarta.transaction.Transactional;

public interface PaymentServiceContract {
	@Transactional
	public String createOrder(int userId, BigDecimal totalAmount, List<OrderItem> cartItems) throws RazorpayException;
	public boolean verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature,int userId);
}
