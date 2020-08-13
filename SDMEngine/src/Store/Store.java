package Store;

import Inventory.InventoryItem;
import Orders.Cart;
import Orders.CartItem;
import Orders.Order;
import jaxb.schema.generated.SDMSell;
import jaxb.schema.generated.SDMStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Store {

    protected int storeId;
    protected String storeName;
    protected int deliveryPpk;
    //TODO: implement totalItemsSold or get rid of it altogether
    protected int totalItemsSold;
    protected HashMap<Integer, Float> mapItemsToAmountSold;
    protected HashMap<Integer, Integer> mapItemToPrices;
    protected List<InventoryItem> inventoryItems;
    protected List<Integer> storeLocation;
    protected List<Order> orders;

    public Store(SDMStore store, List<Integer> storeLocation, int ppk) {
        this.storeId = store.getId();
        this.storeName = store.getName();
        this.storeLocation = storeLocation;
        this.totalItemsSold = 0;
        this.deliveryPpk = ppk;
        this.inventoryItems = new ArrayList<>();
        this.mapItemsToAmountSold = new HashMap<>();
        this.mapItemToPrices = new HashMap<>();

        for (SDMSell sell : store.getSDMPrices().getSDMSell()) {
            mapItemToPrices.put(sell.getItemId(), sell.getPrice());
        }

        for (SDMSell sell : store.getSDMPrices().getSDMSell()) {
            mapItemsToAmountSold.put(sell.getItemId(), (float) 0);
        }

        this.orders = new ArrayList<>();
        System.out.println("Successfully created store " + storeId + "!");
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


    public int getTotalItemsSold() {
        return totalItemsSold;
    }


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
        System.out.println("\nEntered Store.addOrder()");
        System.out.println("about to call orders.add(order)");
        orders.add(order);
        updateStoreInventory(order.getCartForThisOrder());
    }

    //TODO: Check if amountSold for weight is also only counted as 1 for each order, or if record using floats
    //assuming storing amountSold as float
    private void updateStoreInventory(Cart cart) {
        cart.getCart().forEach((k, v) -> {
            float amountInCart = v.getAmount();
            float oldAmountSold = mapItemsToAmountSold.get(k);
            mapItemsToAmountSold.put(k, amountInCart + oldAmountSold);
        });
    }

    public InventoryItem getInventoryItemById(int priceID) {
        for (InventoryItem item : inventoryItems) {
            if (item.getInventoryItemId() == priceID)
                return item;
        }
        System.out.println("Error: No such itemId exists in store inventory!");
        return null;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return storeId == store.storeId &&
                deliveryPpk == store.deliveryPpk &&
                totalItemsSold == store.totalItemsSold &&
                storeName.equals(store.storeName) &&
                Objects.equals(storeLocation, store.storeLocation) &&
                Objects.equals(inventoryItems, store.inventoryItems) &&
                Objects.equals(mapItemsToAmountSold, store.mapItemsToAmountSold) &&
                Objects.equals(mapItemToPrices, store.mapItemToPrices) &&
                Objects.equals(orders, store.orders);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, storeName, storeLocation, deliveryPpk, inventoryItems);
    }
}
