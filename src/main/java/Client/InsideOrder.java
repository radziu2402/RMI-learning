package Client;

import model.Order;
import model.Status;

import java.io.Serializable;

class InsideOrder implements Serializable {
    private final Order order;
    private Status status;
    private int orderId;

    public InsideOrder(Order order, Status status, int orderId) {
        this.order = order;
        this.status = status;
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public Order getOrder() {
        return order;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}