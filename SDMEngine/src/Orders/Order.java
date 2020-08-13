package Orders;


import Store.Store;
import jaxb.schema.generated.SDMStore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Order {
    private static int numOfOrders = 1;
    protected int orderId;
    protected List<Integer> userLocation;
    protected Date orderDate;
    protected Store store;
    protected Cart cart;
    protected float cartTotal;
    protected float deliveryCost;

    public Order(Store store, List<Integer> userLocation, Date orderDate,float deliveryDistance, Cart c){
        this.orderId = numOfOrders++;
        this.store = store;
        this.userLocation = userLocation;
        this.orderDate = orderDate;
        this.cartTotal = cartTotal;
        this.deliveryCost = deliveryDistance;
        this.cart = c;
    }

    public int getOrderId(){return orderId;}
    public Store getStore(){return store;}
//    public String getStoreName(){return storeName;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return orderId == order.orderId &&
                Float.compare(order.cartTotal, cartTotal) == 0 &&
                Float.compare(order.deliveryCost, deliveryCost) == 0 &&
                Objects.equals(userLocation, order.userLocation) &&
                Objects.equals(orderDate, order.orderDate) &&
                Objects.equals(store, order.store) &&
                Objects.equals(cart, order.cart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, userLocation, orderDate, cartTotal, deliveryCost, store, cart);
    }

    public float getCartTotal() {return cartTotal;}
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
