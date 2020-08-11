package Orders;


import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {
    private static int numOfOrders = 1;
    protected int orderId;
    protected int storeID;
    protected List<Integer> userLocation;
    protected Date orderDate;
    protected float cartTotal;
    protected float deliveryDistance;


    protected HashMap<Integer, HashMap<String, Object>> cart;

    protected String dateAndTime;


    public int getOrderId(){return orderId;}

    public int getStoreId() {return storeID;}
    public void setStoreID(int storeID){this.storeID = storeID;}

    public float cartTotal() {return cartTotal;}


    public float getDeliveryDistance(){return deliveryDistance;}

    public HashMap<Integer, HashMap<String, Object>> getCart(){
        if (cart == null){
            cart = new HashMap<>();
        }
        return cart;
    }

    //TODO: Make a static method that calculates the delivery cost and returns value

    public Order(int storeId, List<Integer> userLocation, Date orderDate, float cartTotal, float deliveryDistance, HashMap<Integer, HashMap<String, Object>> cart){
        System.out.println("Inside Order constructor");
        this.orderId = numOfOrders;
        numOfOrders++;

        this.storeID = storeId;
        this.userLocation = userLocation;
        this.orderDate = orderDate;
        this.cartTotal = cartTotal;
        this.deliveryDistance = deliveryDistance;
        this.cart = cart;

        System.out.println("Successfully created order " + orderId);
    }

}
