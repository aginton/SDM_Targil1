package Inventory;

import Orders.Cart;
import Orders.CartItem;
import Orders.Order;
import Store.Store;

import java.util.*;
import java.util.stream.Collectors;

public class Inventory {

    //TODO: Check if this is number of units sold or number of orders containing item.
    //for example, if in one order there are 5 toiler paper quantities, do we add 5 or 1?
    private List<InventoryItem> listInventoryItems;
    private HashMap<InventoryItem, Float> mapItemsToTotalSold;
    private HashMap<InventoryItem, Float> mapItemsToAvePrice;
    private HashMap<InventoryItem, Set<Store>> mapItemsToStoresWithItem;


    public Inventory() {
        this.listInventoryItems = new ArrayList<InventoryItem>();
        this.mapItemsToTotalSold = new HashMap<InventoryItem, Float>();
        this.mapItemsToAvePrice = new HashMap<InventoryItem, Float>();
        this.mapItemsToStoresWithItem = new HashMap<InventoryItem, Set<Store>>();
    }


    public void addNewItemToInventory(InventoryItem item) {
        //System.out.println("Just entered addNewItemToInventory() for item " + item.getInventoryItemId());
        //System.out.println("Result of setInventoryItems.contains(item): " + listInventoryItems.contains(item));

        listInventoryItems.add(item);
        //System.out.println("Added item " + item.getInventoryItemId() + "");
        mapItemsToTotalSold.put(item, 0f);
        mapItemsToAvePrice.put(item, 0f);
        mapItemsToStoresWithItem.put(item, new HashSet<Store>());
    }


    public InventoryItem getInventoryItemById(int id) {
        for (InventoryItem item : listInventoryItems) {
            if (item.getInventoryItemId() == id)
                return item;
        }
        return null;
    }


    public HashMap<InventoryItem, Float> getMapItemsToTotalSold() {
        return mapItemsToTotalSold;
    }

    public HashMap<InventoryItem, Float> getMapItemsToAvePrice() {
        return mapItemsToAvePrice;
    }

    public HashMap<InventoryItem, Set<Store>> getMapItemsToStoresWithItem() {
        return mapItemsToStoresWithItem;
    }

    public void updateSalesMap(Order order) {
        Cart cart = order.getCartForThisOrder();
        cart.getCart().forEach((k, v) -> updateSalesMap(v));
    }

    private void updateSalesMap(CartItem cartItem) {

        InventoryItem item = getInventoryItemById(cartItem.getInventoryItemId());
        float oldAmount = mapItemsToTotalSold.get(item);
        mapItemsToTotalSold.put(item, oldAmount + cartItem.getItemAmount());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Inventory inventory = (Inventory) o;
        return Objects.equals(mapItemsToTotalSold, inventory.mapItemsToTotalSold) &&
                Objects.equals(listInventoryItems, inventory.listInventoryItems);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapItemsToTotalSold, listInventoryItems);
    }

    public List<InventoryItem> getListInventoryItems() {
        if (listInventoryItems == null)
            listInventoryItems = new ArrayList<InventoryItem>();
        return listInventoryItems;
    }


    //TODO: change to streams
    public void updateAvePrice() {

        for(InventoryItem item : listInventoryItems) {
            Set<Store> setOStores = mapItemsToStoresWithItem.get(item);
            float sum = 0;
            for (Store store : setOStores) {
                sum += store.getMapItemToPrices().get(item.getInventoryItemId());
            }
            int size = setOStores.size();
            mapItemsToAvePrice.put(item, sum / size);
        }

    }

    public void updateStoresCarryingItems(List<Store> stores) {

        for(InventoryItem item : listInventoryItems) {
            Set<Store> setOfStores = mapItemsToStoresWithItem.get(item);
           for(Store store : stores) {
               if (!setOfStores.contains(item) && store.getInventoryItems().contains(item))
                   setOfStores.add(store);
           }
           mapItemsToStoresWithItem.put(item, setOfStores);
        }
    }


    public List<Integer> getListOfInventoryItemIds(){
        return listInventoryItems.stream().map(item-> item.getInventoryItemId()).collect(Collectors.toList());
    }

    public List<InventoryItem> getListOfItemsNotSoldByStore(Store store){
        return listInventoryItems.stream().filter( item-> !mapItemsToStoresWithItem.get(item).contains(store)).collect(Collectors.toList());
    }

}
