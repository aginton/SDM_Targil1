package course.java.sdm.engine.Orders;

import course.java.sdm.engine.Inventory.ePurchaseCategory;
import course.java.sdm.engine.Store.Store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Cart {

    private HashMap<Integer,CartItem> cart;
    private float cartTotalPrice = 0;

    public Cart() {
        this.cart = new HashMap<Integer,CartItem>();
        cartTotalPrice = 0f;
    }

    public int getNumberOfTypesOfItemsInCart()
    {
        return cart.size();
    }

    public int getNumItemsInCart() {
        int numberOfItems = 0;
        for(CartItem item : cart.values()) {
            if (item.getPurchaseCategory() == ePurchaseCategory.QUANTITY) {
                numberOfItems += Math.round(item.getItemAmount());
            }
            if (item.getPurchaseCategory() == ePurchaseCategory.WEIGHT) {
                numberOfItems++;
            }
        }

        return numberOfItems;
    }

    public HashMap<Integer,CartItem> getCart() {
        return cart;
    }

    public boolean isEmpty()
    {
        return cart.isEmpty();
    }

    public float getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void add(CartItem cartItem) {
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

    public Set<Store> getStoresBoughtFrom() {
        Set<Store> storesBoughtFrom = new HashSet<>();
        cart.forEach((k,v) -> storesBoughtFrom.add(v.getStoreBoughtFrom()));
        return storesBoughtFrom;
    }
}
