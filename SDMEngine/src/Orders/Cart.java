package Orders;

import java.util.HashMap;

public class Cart {

    private HashMap<Integer,CartItem> cart;
    private float cartTotalPrice;

    public Cart() {
        this.cart = new HashMap<Integer,CartItem>();
        cartTotalPrice = 0f;
    }

    public HashMap<Integer,CartItem> getCart() {
        return cart;
    }

    public float getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void add(CartItem cartItem) {
//        if (item.getPurchaseCategory() == ePurchaseCategory.QUANTITY)
//            this.numberOfItemsInCart += item.getAmount();
//
//        else if (item.getPurchaseCategory() == ePurchaseCategory.WEIGHT)
//            this.numberOfItemsInCart++;
//
//        this.cartTotalPrice += item.getPrice() * item.getAmount();
//        int k = item.getItemId();

        int id = cartItem.getInventoryItemId();
        cartTotalPrice += cartItem.getPrice()*cartItem.getItemAmount();
        if (cart.containsKey(id)) {
            CartItem existingItem = cart.get(id);
            float amountInCart = existingItem.getItemAmount();
            existingItem.setItemAmount(amountInCart + cartItem.getItemAmount());
            return;
        }
        cart.put(id, cartItem);

    }
}
