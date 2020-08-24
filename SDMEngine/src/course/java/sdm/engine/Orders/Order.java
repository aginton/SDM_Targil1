package course.java.sdm.engine.Orders;


import course.java.sdm.engine.Store.Store;

import java.util.*;

public class Order {
    private static int numOfOrders = 1;

    public static void setNumOfOrders(int numOfOrders) {
        Order.numOfOrders = numOfOrders;
    }

    private int orderId;
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

        this.orderId = numOfOrders++;
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

    public int getOrderId(){return orderId;}
//    public String getStoreName(){return storeName;}

    public int getNumberOfStoresInvolved() {
        return storesBoughtFrom.size();
    }

    public eOrderType getOrderType() {
        return orderType;
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

    public float getCartTotal() {return cart.getCartTotalPrice();}
    public float getDeliveryCost(){return deliveryCost;}

    public Date getOrderDate(){return orderDate;}

//    public List<Integer> getUserLocation() {
//        return userLocation;
//    }

    public Cart getCartForThisOrder() {
        return cart;
    }
    //TODO: Make a static method that calculates the delivery cost and returns value


}
