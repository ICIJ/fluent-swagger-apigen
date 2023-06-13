package org.icij.swagger.petstore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StoreData {
    static List<Order> orders = new ArrayList<Order>();

    static {
        orders.add(createOrder(1, 1, 2, new Date(), "placed"));
        orders.add(createOrder(2, 1, 2, new Date(), "delivered"));
        orders.add(createOrder(3, 2, 2, new Date(), "placed"));
        orders.add(createOrder(4, 2, 2, new Date(), "delivered"));
        orders.add(createOrder(5, 3, 2, new Date(), "placed"));
        orders.add(createOrder(6, 3, 2, new Date(), "placed"));
        orders.add(createOrder(7, 3, 2, new Date(), "placed"));
        orders.add(createOrder(8, 3, 2, new Date(), "placed"));
        orders.add(createOrder(9, 3, 2, new Date(), "placed"));
        orders.add(createOrder(10, 3, 2, new Date(), "placed"));
    }

    public Order findOrderById(long orderId) {
        for (Order order : orders) {
            if (order.getId() == orderId) {
                return order;
            }
        }
        return null;
    }

    public Order placeOrder(Order order) {
        if (orders.size() > 0) {
            for (int i = orders.size() - 1; i >= 0; i--) {
                if (orders.get(i).getId() == order.getId()) {
                    orders.remove(i);
                }
            }
        }
        orders.add(order);
        return order;
    }

    public boolean deleteOrder(long orderId) {
        if (orders.size() > 0) {
            for (int i = orders.size() - 1; i >= 0; i--) {
                if (orders.get(i).getId() == orderId) {
                    orders.remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    private static Order createOrder(long id, long petId, int quantity,
                                     Date shipDate, String status) {
        Order order = new Order();
        order.setId(id);
        order.setPetId(petId);
        order.setQuantity(quantity);
        order.setShipDate(shipDate);
        order.setStatus(status);
        return order;
    }
}
