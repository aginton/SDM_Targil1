package course.java.sdm.engine.Orders;

public class OrderId {

    int id;
    int subId;

    public OrderId(int id, int subId) {
        this.id = id;
        this.subId = subId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSubId() {
        return subId;
    }

    public void setSubId(int subId) {
        this.subId = subId;
    }

    @Override
    public String toString() {
        return id + (subId == -1 ? "" : "-" + subId) ;
    }
}
