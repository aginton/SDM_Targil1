package course.java.sdm.engine.Orders;

import course.java.sdm.engine.Store.Store;
import course.java.sdm.engine.Inventory.InventoryItem;
import java.util.Objects;

public class CartItem extends InventoryItem {
    
    private float itemAmount;
    private int price;
    Store storeBoughtFrom;

    public CartItem(InventoryItem item, float amount, int price, Store storeBoughtFrom){
        super(item);
        this.itemAmount = amount;
        this.price = price;
        this.storeBoughtFrom = storeBoughtFrom;
    }

//    public int getItemId() {
//        return itemId;
//    }

    public void setItemAmount(float amount) {
        this.itemAmount = amount;
    }

    public void setPrice(int price) {
        this.price = price;
    }

//    public String getItemName() {
//        return itemName;
//    }

//    public ePurchaseCategory getPurchaseCategory() {
//        return purchaseCategory;
//    }

    public float getItemAmount() {
        return itemAmount;
    }

    public int getPrice() {
        return price;
    }

    public Store getStoreBoughtFrom() {
        return storeBoughtFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CartItem cartItem = (CartItem) o;
        return Float.compare(cartItem.itemAmount, itemAmount) == 0 &&
                price == cartItem.price;
    }

    //    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        CartItem cartItem = (CartItem) o;
//        return itemId == cartItem.itemId &&
//                Float.compare(cartItem.amountInCart, amountInCart) == 0 &&
//                price == cartItem.price &&
//                Objects.equals(itemName, cartItem.itemName) &&
//                Objects.equals(purchaseCategory, cartItem.purchaseCategory);
//    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemAmount, price);
    }


//    @Override
//    public int hashCode() {
//        return Objects.hash(itemId, itemName, purchaseCategory, amountInCart, price);
//    }
}
