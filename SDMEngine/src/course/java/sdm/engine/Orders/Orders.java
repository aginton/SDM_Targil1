package course.java.sdm.engine.Orders;

import java.util.ArrayList;
import java.util.List;

public class Orders {

    private List<Order> orders;

    public List<Order> getOrders(){
        if (orders == null){
            orders = new ArrayList<Order>();
        }
        return this.orders;
    }

    public void addOrder(Order order){
        if (orders == null)
            orders = new ArrayList<Order>();

        orders.add(order);
    }

}
