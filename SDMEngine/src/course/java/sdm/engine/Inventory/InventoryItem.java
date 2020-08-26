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


    @Override
    public int hashCode() {
        return Objects.hash(inventoryItemId, itemName, purchaseCategory);
    }

    @Override
    public int compareTo(InventoryItem o) {
        return this.getInventoryItemId() - o.getInventoryItemId();
    }

}
