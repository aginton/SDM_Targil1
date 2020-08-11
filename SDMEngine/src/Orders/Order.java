package Orders;


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
    protected SDMStore store;
    protected HashMap<Integer, HashMap<String, Object>> cart;


    public int getOrderId(){return orderId;}
    public SDMStore getStore(){return store;}
//    public String getStoreName(){return storeName;}
//    public int getStoreId() {return storeID;}

//    public void setStoreID(int storeID){this.storeID = storeID;}

    public float getCartTotal() {return cartTotal;}
    public float getDeliveryCost(){return deliveryCost;}

    public Date getOrderDate(){return orderDate;}



    public HashMap<Integer, HashMap<String, Object>> getCart(){
        if (cart == null){
            cart = new HashMap<>();
        }
        return cart;
    }

    //TODO: Make a static method that calculates the delivery cost and returns value

    public Order(SDMStore store, List<Integer> userLocation, Date orderDate, float cartTotal, float deliveryDistance, HashMap<Integer, HashMap<String, Object>> cart){
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

        System.out.println("Successfully created order " + orderId);
    }

}
