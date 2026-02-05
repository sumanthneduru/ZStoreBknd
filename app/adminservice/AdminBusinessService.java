package com.zstore.app.adminservice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.zstore.app.adminservicecontract.AdminBusinessServiceContract;
import com.zstore.app.entities.Order;
import com.zstore.app.entities.OrderItem;
import com.zstore.app.entities.OrderStatus;
import com.zstore.app.userrepository.OrderItemRepository;
import com.zstore.app.userrepository.OrderRepository;
import com.zstore.app.userrepository.ProductRepository;

@Service
public class AdminBusinessService implements AdminBusinessServiceContract {

	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final ProductRepository productRepository;

	public AdminBusinessService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
			ProductRepository productRepository) {
		super();
		this.orderRepository = orderRepository;
		this.orderItemRepository = orderItemRepository;
		this.productRepository = productRepository;
	}

	@Override
	public Map<String, Object> calculateMonthlyBusiness(int month, int year) {
		List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByMonthAndYear(month, year);
		return calculateBusinessMetrics(successfulOrders);
	}

	@Override
	public Map<String, Object> calculateDailyBusiness(LocalDate date) {
		List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByDate(date);
		return calculateBusinessMetrics(successfulOrders);
	}

	@Override
	public Map<String, Object> calculateYearlyBusiness(int year) {
		List<Order> successfulOrders = orderRepository.findSuccessfulOrdersByYear(year);
		return calculateBusinessMetrics(successfulOrders);
	}

	@Override
	public Map<String, Object> calculateOverallBusiness() {
		List<Order> successfulOrders = orderRepository.findAllByStatusForOverallBusiness();

		BigDecimal totalBusiness = orderRepository.calculateOverallBusiness();

		Map<String, Object> response = calculateBusinessMetrics(successfulOrders);

		response.put("totalBusiness", totalBusiness.doubleValue());
		return response;
	}

	private Map<String, Object> calculateBusinessMetrics(List<Order> orders) {

		double totalRevenue = 0.0;
		Map<String, Integer> categorySales = new HashMap<>();

		for (Order order : orders) {
			totalRevenue += order.getTotalAmount().doubleValue();
			List<OrderItem> items = orderItemRepository.findByOrderId(order.getOrderId());
			for (OrderItem item : items) {
				String categoryName = productRepository.findCategoryNameByProductId(item.getProductId());
				categorySales.put(categoryName, categorySales.getOrDefault(categoryName, 0) + item.getQuantity());
			}
		}

		Map<String, Object> metrics = new HashMap<>();
		metrics.put("totalRevenue", totalRevenue);
		metrics.put("categorySales", categorySales);

		return metrics;
	}

}
