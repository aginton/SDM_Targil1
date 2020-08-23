package Orders;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Orders {

    private List<Order> orders;

    public List<Order> getOrders(){
        if (orders == null){
            orders = new ArrayList<Order>();
        }
        return this.orders;
    }

//    public List<Order> getOrdersForStore(int storeId){
//        return orders.stream().filter(order->order.getStore().getId()==storeId).collect(Collectors.toList());
//    }
//
//    public List<Order> getOrdersContainingItem(int itemId){
//        List<Order> res = orders.stream().filter(order->order.getCart().containsKey(itemId)).collect(Collectors.toList());
//
//        return res;
//    }
//
    public void addOrder(Order order){
        if (orders == null)
            orders = new ArrayList<Order>();

        orders.add(order);
        //System.out.println("Order " + order.getOrderId() + " successfully added to Orders!");
    }

}
