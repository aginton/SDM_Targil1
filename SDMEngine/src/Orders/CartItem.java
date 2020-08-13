package Orders;

import Inventory.InventoryItem;

import java.util.Objects;

public class CartItem {
    protected int itemId;
    protected String itemName;
    protected String purchaseCategory;
    protected float amount;
    protected int price;

    public CartItem(InventoryItem item, float amount, int price){
        this.itemId = item.getInventoryItemId();
        this.itemName = item.getItemName();
        this.purchaseCategory = item.getPurchaseCategory();
        this.amount = amount;
        this.price = price;
    }

    public int getItemId() {
        return itemId;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getItemName() {
        return itemName;
    }

    public String getPurchaseCategory() {
        return purchaseCategory;
    }

    public float getAmount() {
        return amount;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return itemId == cartItem.itemId &&
                Float.compare(cartItem.amount, amount) == 0 &&
                price == cartItem.price &&
                Objects.equals(itemName, cartItem.itemName) &&
                Objects.equals(purchaseCategory, cartItem.purchaseCategory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, itemName, purchaseCategory, amount, price);
    }
}
