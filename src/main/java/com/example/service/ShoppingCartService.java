package com.example.service;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import com.example.domain.LoginUser;
import com.example.domain.Order;
import com.example.domain.OrderItem;
import com.example.domain.OrderTopping;
import com.example.form.OrderItemForm;
import com.example.repository.OrderItemRepository;
import com.example.repository.OrderRepository;
import com.example.repository.OrderToppingRepository;

@Service
public class ShoppingCartService {

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private OrderToppingRepository orderToppingRepository;

	@Autowired
	private OrderItemRepository orderItemRepository;

	/**
	 * ユーザーIdとステータスで注文情報を表示.
	 * 
	 * @param userId ユーザーID
	 * @param status ステータス
	 * @return 注文情報
	 */
	public Order showOrderByUserIdAndStatus(Integer userId, Integer status) {
		return orderRepository.findByUserIdAndStatus(userId, status);
	}

	/**
	 * ショッピングカートに商品を追加する.
	 * 
	 * @param form      フォーム
	 * @param loginUser ログイン中のユーザー
	 */
	public void addShoppingCart(OrderItemForm form, Integer userId, @AuthenticationPrincipal LoginUser loginUser) {
		OrderItem orderItem = new OrderItem();
		BeanUtils.copyProperties(form, orderItem);
		Order preOrder = orderRepository.findByUserIdAndStatus(userId, 0);

		if (preOrder != null) {
			orderItem.setOrderId(preOrder.getId());
		} else {
			Order order = new Order();
			order.setStatus(0);
			order.setTotalPrice(0);
			order.setUserId(userId);
			order = orderRepository.save(order);
			orderItem.setOrderId(order.getId());
		}
		OrderItem preOrderItem = orderItemRepository.findByItemIdAndOrderId(orderItem.getSize(), orderItem.getItemId(),
				orderItem.getOrderId());
		if (preOrderItem != null) {
			preOrderItem.setQuantity(preOrderItem.getQuantity() + 1);
			orderItemRepository.save(preOrderItem);
		} else {
			orderItem = orderItemRepository.save(orderItem);
		}
		Integer orderItemId = orderItem.getId();
		if (form.getOrderToppingList() != null) {
			form.getOrderToppingList().forEach(i -> {
				OrderTopping orderTopping = new OrderTopping();
				orderTopping.setToppingId(i);
				if (preOrderItem == null) {
					orderTopping.setOrderItemId(orderItemId);
				} else {
					orderTopping.setOrderItemId(preOrderItem.getId());
				}
				orderToppingRepository.save(orderTopping);
			});
		}
	}

	/**
	 * ログイン前に保持していたオーダー情報を現在のユーザーに変更.
	 * 
	 * @param loginUser ログイン中のユーザー
	 */
	public void changeOrder(Integer userId, @AuthenticationPrincipal LoginUser loginUser) {

		Order preOrder = orderRepository.findByUserIdAndStatus(userId, 0);
		if (preOrder != null) {
			Order newOrder = orderRepository.findByUserIdAndStatus(loginUser.getUser().getId(), 0);
			if (newOrder != null) {
				preOrder.getOrderItemList().forEach(orderItem -> {
					orderItem.setOrderId(newOrder.getId());
					orderItemRepository.save(orderItem);
				});
			} else {
				Order order = new Order();
				order.setStatus(0);
				order.setTotalPrice(0);
				order.setUserId(userId);
				order = orderRepository.save(order);
				Integer orderId = order.getId();
				preOrder.getOrderItemList().forEach(orderItem -> {
					orderItem.setOrderId(orderId);
					orderItemRepository.save(orderItem);
				});

			}
			orderRepository.deleteById(preOrder.getId());
		}
	}

	/**
	 * カートの中身を削除する.
	 * 
	 * @param id orderItemId
	 */
	public void deleteCart(Integer id) {
		orderToppingRepository.deleteByOrderItemId(id);
		orderItemRepository.deleteOrderItem(id);
	}

}
