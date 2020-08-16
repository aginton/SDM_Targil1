package Orders;

import Inventory.InventoryItem;
import Inventory.ePurchaseCategory;

import java.util.Objects;

public class CartItem extends InventoryItem {
    
    private float amountInCart;
    private int price;

    public CartItem(InventoryItem item, float amount, int price){
        super(item);
        //this.itemId = item.getInventoryItemId();
        //this.itemName = item.getItemName();
        //this.purchaseCategory = item.getPurchaseCategory();
        this.amountInCart = amount;
        this.price = price;
    }

//    public int getItemId() {
//        return itemId;
//    }

    public void setAmountInCart(float amount) {
        this.amountInCart = amount;
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

    public float getAmountInCart() {
        return amountInCart;
    }

    public int getPrice() {
        return price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CartItem cartItem = (CartItem) o;
        return Float.compare(cartItem.amountInCart, amountInCart) == 0 &&
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
        return Objects.hash(super.hashCode(), amountInCart, price);
    }


//    @Override
//    public int hashCode() {
//        return Objects.hash(itemId, itemName, purchaseCategory, amountInCart, price);
//    }
}
