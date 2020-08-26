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


    public float getItemAmount() {
        return itemAmount;
    }
    public void setItemAmount(float amount) {
        this.itemAmount = amount;
    }

    public int getPrice() {
        return price;
    }
    public void setPrice(int price) {
        this.price = price;
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

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), itemAmount, price);
    }
}
