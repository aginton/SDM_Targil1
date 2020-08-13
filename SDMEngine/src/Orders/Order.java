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
//    protected int storeID;
//    protected String storeName;
    protected List<Integer> userLocation;
    protected Date orderDate;
    protected float cartTotal;
    protected float deliveryCost;
    protected Store store;
    //protected int numItemsInCart;
    //protected HashMap<Integer, HashMap<String, Object>> cart;
    protected Cart cart;

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
//    public int getStoreId() {return storeID;}

//    public void setStoreID(int storeID){this.storeID = storeID;}

    //public int getNumItemsInCart(){return numItemsInCart;}
    public float getCartTotal() {return cartTotal;}
    public float getDeliveryCost(){return deliveryCost;}

    public Date getOrderDate(){return orderDate;}

    public static int getNumOfOrders() {
        return numOfOrders;
    }

    public List<Integer> getUserLocation() {
        return userLocation;
    }

    public Cart getCartForThisOrder() {
        return cart;
    }


    //TODO: Make a static method that calculates the delivery cost and returns value
//    public Order(Store store, List<Integer> userLocation, Date orderDate, float cartTotal, float deliveryDistance, HashMap<Integer, HashMap<String, Object>> cart, int numItemsInCart){
//        System.out.println("Inside Order constructor");
//        this.orderId = numOfOrders;
//        numOfOrders++;
//
//        //this.storeID = storeId;
//        this.store = store;
//        this.userLocation = userLocation;
//        this.orderDate = orderDate;
//        this.cartTotal = cartTotal;
//        this.deliveryCost = deliveryDistance;
//        this.cart = cart;
//        this.numItemsInCart = numItemsInCart;
//
//        System.out.println("Successfully created order " + orderId);
//    }

}
