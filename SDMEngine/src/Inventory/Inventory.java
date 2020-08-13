package Inventory;

import Orders.CartItem;

import java.util.*;

public class Inventory {

    //TODO: Check if this is number of units sold or number of orders containing item.
    //for example, if in one order there are 5 toiler paper quantities, do we add 5 or 1?
    protected HashMap<InventoryItem, Integer> mapItemsToTotalSold;


    //  protected HashMap<InventoryItem, Integer> mapHowManyOrdersContainItem;
    protected List<InventoryItem> listInventoryItems;

    public Inventory() {
        this.listInventoryItems = new ArrayList<InventoryItem>();
        this.mapItemsToTotalSold = new HashMap<InventoryItem, Integer>();
    }


//    public HashMap<InventoryItem, Integer> getMapItemsToTotalSold() {
//        return mapItemsToTotalSold;
//    }

    public void addNewItemToInventory(InventoryItem item) {
        System.out.println("Just entered addNewItemToInventory() for item " + item.getInventoryItemId());
        System.out.println("Result of setInventoryItems.contains(item): " + listInventoryItems.contains(item));
        ;

        if (!listInventoryItems.contains(item)) {
            System.out.println("Entered not if clause");
            listInventoryItems.add(item);
            mapItemsToTotalSold.put(item, 0);
            System.out.println("Added item " + item.inventoryItemId + "");
            return;
        }
        System.out.println("Error: this item already is in inventory!");
    }


    public InventoryItem getInventoryItemById(int id) {
        for (InventoryItem item : listInventoryItems) {
            if (item.getInventoryItemId() == id)
                return item;
        }
        System.out.println("Error: No such id found in inventory!");
        return null;
    }

    public void updateSalesMap(HashMap<Integer, CartItem> cart) {
        cart.forEach((k, v) -> updateSalesMap(v));
    }

    private void updateSalesMap(CartItem v) {
        //int cartItemId = v.getItemId();
        InventoryItem item = getInventoryItemById(v.getItemId());
        if (item == null) {
            System.out.printf("Inventory does not contain item %d, %s!", v.getItemId(), v.getItemName());
            return;
        } else {
            //int oldAmount = mapItemsToTotalSold.get(item);
            int oldAmount = item.amountSold;

            if (v.getPurchaseCategory().equalsIgnoreCase("weight")) {
                //mapItemsToTotalSold.put(item, oldAmount+1);
                item.setAmountSold(oldAmount + 1);

            } else if (v.getPurchaseCategory().equalsIgnoreCase("quantity")) {
                int amountToAdd = Math.round(v.getAmount());
                //mapItemsToTotalSold.put(item, oldAmount+amountToAdd);
                item.setAmountSold(oldAmount + amountToAdd);
            }
        }
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
}
