package Inventory;

import Store.Store;
import jaxb.schema.generated.SDMItem;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class InventoryItem implements Comparable<InventoryItem> {
    protected int inventoryItemId;
    protected String itemName;
    protected String purchaseCategory;
    protected int amountSold;
    protected int avePrice;

    public void setAmountSold(int amountSold) {
        this.amountSold = amountSold;
    }

    protected Set<Store> storesCarryingItem;

    public int getAvePrice() {
        return avePrice;
    }

    public InventoryItem(SDMItem item){
        this.inventoryItemId = item.getId();
        this.itemName = item.getName();
        this.purchaseCategory = item.getPurchaseCategory();
        this.amountSold = 0;
        this.avePrice = 0;
        this.storesCarryingItem = new HashSet<Store>();
        System.out.printf("Created InventoryItem %d!", inventoryItemId);
    }

    public void updateAvePrice(){
        int sum = 0;
        for (Store store: storesCarryingItem){
            sum += store.getMapItemToPrices().get(inventoryItemId);
        }

        //this.avePrice = (sum / storesCarryingItem.size());
        this.avePrice = sum;
    }

    public void setAvePrice(int avePrice) {
        this.avePrice = avePrice;
    }

    public void addCarryingStore(Store store){
        storesCarryingItem.add(store);
    }

    public int getInventoryItemId() {
        return inventoryItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public String getPurchaseCategory() {
        return purchaseCategory;
    }

    public Set<Store> getStoresCarryingItem() {
        return storesCarryingItem;
    }

    public int getAmountSold() {
        return amountSold;
    }

    //https://www.codexpedia.com/java/java-set-and-hashset-with-custom-class/
    // Currently using default equals hashCode
    @Override
    public boolean equals(Object o) {
        System.out.println("In equals");
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        if (o instanceof InventoryItem){
            InventoryItem oitem = (InventoryItem) o;
            return inventoryItemId == oitem.getInventoryItemId() && itemName.equals(oitem.itemName) && purchaseCategory.equals(oitem.purchaseCategory);
        } else{
            return false;
        }
    }



    //https://stackoverflow.com/questions/17355690/contains-and-remove-methods-for-instances-user-defined-classes-in-a-list

    //https://stackoverflow.com/questions/5110376/hashset-contains-problem-with-custom-objects   <-- "never let hash value depend on mutable field"
    @Override
    public int hashCode() {
        return Objects.hash(inventoryItemId, itemName, purchaseCategory);
    }

    @Override
    public int compareTo(InventoryItem o) {
        return this.getInventoryItemId() - o.getInventoryItemId();
        //return 0;
    }
}
