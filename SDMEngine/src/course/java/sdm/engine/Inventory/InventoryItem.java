package course.java.sdm.engine.Inventory;

import course.java.sdm.engine.jaxb.schema.generated.SDMItem;

import java.util.Objects;

public class InventoryItem implements Comparable<InventoryItem> {

    private int inventoryItemId;
    private String itemName;
    private final ePurchaseCategory purchaseCategory;

    public InventoryItem(SDMItem item) {

        this.inventoryItemId = item.getId();
        this.itemName = item.getName();
        this.purchaseCategory = ePurchaseCategory.valueOf(item.getPurchaseCategory().toUpperCase());
        //System.out.printf("Created InventoryItem %d!", inventoryItemId);
    }

    public InventoryItem(InventoryItem item){
        this.inventoryItemId = item.getInventoryItemId();
        this.purchaseCategory = item.getPurchaseCategory();
        this.itemName = item.getItemName();
    }

    //Getters
    public int getInventoryItemId() {
        return inventoryItemId;
    }

    public String getItemName() {
        return itemName;
    }

    public ePurchaseCategory getPurchaseCategory() {
        return purchaseCategory;
    }

//    public void updateAvePrice(){
//        float sum = 0f;
//        for (course.java.sdm.engine.Store store: storesCarryingItem){
//            sum += store.getMapItemToPrices().get(inventoryItemId);
//        }
//
//        this.avePrice = (sum / storesCarryingItem.size());
//    }
//
//    public void addCarryingStore(course.java.sdm.engine.Store store){
//        storesCarryingItem.add(store);
//    }

    //https://www.codexpedia.com/java/java-set-and-hashset-with-custom-class/
    // Currently using default equals hashCode
    @Override
    public boolean equals(Object o) {
       // System.out.println("In equals");
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

    //TODO: See how to sort list of InventoryItems based on itemIds
    //https://stackoverflow.com/questions/13491450/java-sorting-user-defined-objects

}
