package Orders;


import Store.Store;
import jaxb.schema.generated.SDMStore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
    protected int numItemsInCart;
    protected HashMap<Integer, HashMap<String, Object>> cart;
    protected Cart orderCart;


    public int getOrderId(){return orderId;}
    public Store getStore(){return store;}
//    public String getStoreName(){return storeName;}
//    public int getStoreId() {return storeID;}

//    public void setStoreID(int storeID){this.storeID = storeID;}

    public int getNumItemsInCart(){return numItemsInCart;}
    public float getCartTotal() {return cartTotal;}
    public float getDeliveryCost(){return deliveryCost;}

    public Date getOrderDate(){return orderDate;}



    public HashMap<Integer, HashMap<String, Object>> getCart(){
        if (cart == null){
            cart = new HashMap<>();
        }
        return cart;
    }

    public Order(Store store, List<Integer> userLocation, Date orderDate,float deliveryDistance, Cart c){
        this.store = store;
        this.userLocation = userLocation;
        this.orderDate = orderDate;
        this.cartTotal = cartTotal;
        this.deliveryCost = deliveryDistance;
    }


    //TODO: Make a static method that calculates the delivery cost and returns value
    public Order(Store store, List<Integer> userLocation, Date orderDate, float cartTotal, float deliveryDistance, HashMap<Integer, HashMap<String, Object>> cart, int numItemsInCart){
        System.out.println("Inside Order constructor");
        this.orderId = numOfOrders;
        numOfOrders++;

        //this.storeID = storeId;
        this.store = store;
        this.userLocation = userLocation;
        this.orderDate = orderDate;
        this.cartTotal = cartTotal;
        this.deliveryCost = deliveryDistance;
        this.cart = cart;
        this.numItemsInCart = numItemsInCart;

        System.out.println("Successfully created order " + orderId);
    }

}
