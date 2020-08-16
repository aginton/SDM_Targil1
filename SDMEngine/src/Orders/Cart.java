package Orders;

import Inventory.ePurchaseCategory;

import java.util.HashMap;
import java.util.Set;

public class Cart {

    private HashMap<Integer,CartItem> cart;
    private float cartTotalPrice;

    public HashMap<Integer,CartItem> getCart() {
        return cart;
    }

    public Cart() {
        this.cart = new HashMap<Integer,CartItem>();
        cartTotalPrice = 0f;
    }

    public float getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void add(CartItem item) {
//        if (item.getPurchaseCategory() == ePurchaseCategory.QUANTITY)
//            this.numberOfItemsInCart += item.getAmount();
//
//        else if (item.getPurchaseCategory() == ePurchaseCategory.WEIGHT)
//            this.numberOfItemsInCart++;
//
//        this.cartTotalPrice += item.getPrice() * item.getAmount();
//        int k = item.getItemId();

        int id = item.getInventoryItemId();
        if (cart.containsKey(id)) {
            CartItem existingItem = cart.get(id);
            float amountInCart = existingItem.getAmountInCart();
            existingItem.setAmountInCart(amountInCart + item.getAmountInCart());
            return;
        }
        cart.put(id, item);
    }
}
