package Orders;

import java.util.HashMap;
import java.util.Set;

public class Cart {

    protected HashMap<Integer, CartItem> cart;
    protected float cartTotalPrice;
    protected int numberOfItemsInCart = 0;

    public HashMap<Integer, CartItem> getCart() {
        if (cart == null)
            cart = new HashMap<Integer, CartItem>();

        return cart;
    }

    public float getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void add(CartItem item) {
        if (item.purchaseCategory.equalsIgnoreCase("quantity"))
            this.numberOfItemsInCart += item.amount;

        else if (item.purchaseCategory.equalsIgnoreCase("weight"))
            this.numberOfItemsInCart++;

        this.cartTotalPrice += item.price * item.amount;
        int k = item.getItemId();

        if (cart.containsKey(k)) {
            CartItem i = cart.get(k);
            float amountInCart = i.getAmount();
            i.setAmount(amountInCart + item.getAmount());
            return;
        }
        cart.put(item.getItemId(), item);
    }
}
