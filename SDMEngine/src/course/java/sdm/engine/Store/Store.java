package course.java.sdm.engine.Store;

import course.java.sdm.engine.Inventory.InventoryItem;
import course.java.sdm.engine.Orders.Cart;
import course.java.sdm.engine.Orders.Order;
import course.java.sdm.engine.jaxb.schema.generated.SDMSell;
import course.java.sdm.engine.jaxb.schema.generated.SDMStore;

import java.util.*;

public class Store {

    private int storeId;
    private String storeName;
    private int deliveryPpk;

    public void setTotalDeliveryIncome(float totalDeliveryIncome) {
        this.totalDeliveryIncome = totalDeliveryIncome;
    }

    private float totalDeliveryIncome;
    private HashMap<Integer, Float> mapItemsToAmountSold;
    private HashMap<Integer, Integer> mapItemToPrices;
    private List<InventoryItem> inventoryItems;
    private List<Integer> storeLocation;
    private List<Order> orders;

    public Store(SDMStore store) {
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.storeLocation = new ArrayList();
        storeLocation.add(store.getLocation().getX());
        storeLocation.add(store.getLocation().getY());
        this.totalDeliveryIncome = 0f;
        this.deliveryPpk = store.getDeliveryPpk();
        this.inventoryItems = new ArrayList<>();
        this.mapItemsToAmountSold = new HashMap<>();
        this.mapItemToPrices = new HashMap<>();

        for (SDMSell sell : store.getSDMPrices().getSDMSell()) {
            mapItemToPrices.put(sell.getItemId(), sell.getPrice());
            mapItemsToAmountSold.put(sell.getItemId(), (float) 0);
        }

        this.orders = new ArrayList<>();
        //System.out.println("Successfully created store " + storeId + "!");
    }

    public List<InventoryItem> getInventoryItems() {
        return inventoryItems;
    }

    public HashMap<Integer, Float> getMapItemsToAmountSold() {
        if (mapItemsToAmountSold == null)
            mapItemsToAmountSold = new HashMap<>();
        return mapItemsToAmountSold;
    }

    public HashMap<Integer, Integer> getMapItemToPrices() {
        if (mapItemToPrices == null)
            mapItemToPrices = new HashMap<>();
        return mapItemToPrices;
    }

    public float getTotalDeliveryIncome() {return totalDeliveryIncome; }

    public void setStoreInventory(List<InventoryItem> i_inventoryItems) {
        if (this.inventoryItems != null)
            this.inventoryItems = new ArrayList<>();

        this.inventoryItems = i_inventoryItems;
    }


    public int getStoreId() {
        return storeId;
    }

    public String getStoreName() {
        return storeName;
    }

    public List<Integer> getStoreLocation() {
        return storeLocation;
    }


    public List<Order> getOrders() {
        if (orders == null)
            orders = new ArrayList<Order>();

        return orders;
    }

    public int getDeliveryPpk() {
        return deliveryPpk;
    }

    public void addOrder(Order order) {
        //System.out.println("\nEntered course.java.sdm.engine.Store.addOrder()");
        //System.out.println("about to call orders.add(order)");
        orders.add(order);
        setTotalDeliveryIncome(order.getDeliveryCost()+this.getTotalDeliveryIncome());
        updateStoreInventory(order.getCartForThisOrder());
    }

    //assuming storing amountSold as float
    private void updateStoreInventory(Cart cart) {
        cart.getCart().forEach((k, v) -> {
            float amountInCart = v.getItemAmount();
            float oldAmountSold = mapItemsToAmountSold.get(k);
            mapItemsToAmountSold.put(k, amountInCart + oldAmountSold);
        });
    }

    public InventoryItem getInventoryItemById(int priceID) {
        for (InventoryItem item : inventoryItems) {
            if (item.getInventoryItemId() == priceID)
                return item;
        }
        System.out.printf("Error: No such itemId exists in %s's inventory!", getStoreName());
        return null;
    }

    public void addItemToStoreInventory(InventoryItem item, int price){
        if (inventoryItems.contains(item)){
            return;
        }
        inventoryItems.add(item);
        Collections.sort(inventoryItems);
        mapItemsToAmountSold.put(item.getInventoryItemId(), 0f);
        mapItemToPrices.put(item.getInventoryItemId(), price);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;

        //TODO: Make sure that this equals implementation is okay (i.e., that we can define equals based only on immutable fields (id, name, userlocation, ppk))
        return storeId == store.storeId &&
                deliveryPpk == store.deliveryPpk &&
                storeName.equals(store.storeName) &&
                Objects.equals(storeLocation, store.storeLocation);

        /*Original version:*/
//        return storeId == store.storeId &&
//                deliveryPpk == store.deliveryPpk &&
//                storeName.equals(store.storeName) &&
//                Objects.equals(storeLocation, store.storeLocation) &&
//                Objects.equals(inventoryItems, store.inventoryItems) &&
//                Objects.equals(mapItemsToAmountSold, store.mapItemsToAmountSold) &&
//                Objects.equals(mapItemToPrices, store.mapItemToPrices) &&
//                Objects.equals(orders, store.orders);
    }

    @Override
    public int hashCode() {

        return Objects.hash(storeId, storeName, storeLocation, deliveryPpk);
        //Original:
//        return Objects.hash(storeId, storeName, storeLocation, deliveryPpk, inventoryItems);
    }

    public float getDeliveryCost(List<Integer> userLocation) {
        return deliveryPpk* getDistance(userLocation, getStoreLocation());
    }

    public static float getDistance(List<Integer> userLocation, List<Integer> storeLocation) {
        if (userLocation.size() != 2 || storeLocation.size() != 2){
            System.out.println("Error: Input lists must each contain 2 points!");
            return -1;
        }
        int xDelta = userLocation.get(0) -storeLocation.get(0);
        int yDelta = userLocation.get(1) -storeLocation.get(1);
        return (float) Math.sqrt((xDelta*xDelta)+(yDelta*yDelta));
    }
}
