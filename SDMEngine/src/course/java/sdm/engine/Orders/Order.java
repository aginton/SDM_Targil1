package course.java.sdm.engine.Orders;


import course.java.sdm.engine.Store.Store;

import java.util.*;

public class Order {
    private static int numOfOrders = 0;

    public static void setNumOfOrders(int numOfOrders) {
        Order.numOfOrders = numOfOrders;
    }

    private OrderId orderId;
    private List<Integer> userLocation;
    private Date orderDate;
    private Cart cart;
    private float deliveryCost;
    Set<Store> storesBoughtFrom;
    eOrderType orderType;

    public Order(List<Integer> userLocation,
                 Date orderDate,
                 float deliveryCost,
                 Cart cart, Set<Store> storesBoughtFrom,
                 eOrderType orderType) {

        if (orderType == eOrderType.STATIC_ORDER || orderType == eOrderType.DYNAMIC_ORDER) {
            numOfOrders++;
            this.orderId = new OrderId(numOfOrders, -1);
        }

        if (orderType == eOrderType.SPLITTED_DYNAMIC_ORDER) {
            //id in format: <dynamic-order-id.store-id>
            Iterator<Store> iterator = storesBoughtFrom.iterator();
            int storeId = iterator.next().getStoreId();
            this.orderId = new OrderId(numOfOrders, storeId);
        }

        this.userLocation = userLocation;
        this.orderDate = orderDate;
        this.deliveryCost = deliveryCost;
        this.cart = cart;
        this.storesBoughtFrom = storesBoughtFrom;
        this.orderType = orderType;

    }

    public List<Integer> getUserLocation() {
        return userLocation;
    }

    public Set<Store> getStoresBoughtFrom() {
        return storesBoughtFrom;
    }

    public OrderId getOrderId(){return orderId;}

    public int getNumberOfStoresInvolved() {
        return storesBoughtFrom.size();
    }

    public eOrderType getOrderType() {
        return orderType;
    }

    public float getCartTotal() {return cart.getCartTotalPrice();}

    public float getDeliveryCost(){return deliveryCost;}
    public Date getOrderDate(){return orderDate;}

    public Cart getCartForThisOrder() {
        return cart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId &&
                Float.compare(order.deliveryCost, deliveryCost) == 0 &&
                Objects.equals(userLocation, order.userLocation) &&
                Objects.equals(orderDate, order.orderDate) &&
                Objects.equals(cart, order.cart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, userLocation, orderDate, deliveryCost, cart);
    }
}
