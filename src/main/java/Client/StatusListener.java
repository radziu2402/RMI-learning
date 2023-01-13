package Client;

import interfaces.IStatusListener;
import model.Status;

import javax.swing.table.DefaultTableModel;
//import java.io.Serial;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;


public class StatusListener extends UnicastRemoteObject implements IStatusListener, Serializable {

    private static final long serialVersionUID = 1L;
    private final Map<Integer, InsideOrder> orders;
    private final DefaultTableModel orderModel;

    public StatusListener(Map<Integer, InsideOrder> orders, DefaultTableModel orderModel) throws RemoteException {
        this.orders = orders;
        this.orderModel = orderModel;
    }

    @Override
    public void statusChanged(int id, Status status) throws RemoteException {
        if(orders.get(id)!=null) {
            orders.get(id).setStatus(status);
            orderModel.setRowCount(0);
            for (InsideOrder order : orders.values()) {
                Object[] rowData = {order.getOrderId(), order.getOrder().getCost(), order.getStatus()};
                orderModel.addRow(rowData);
            }
        }
    }
}
