package Shop;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import interfaces.IShop;
import interfaces.IStatusListener;
import model.Client;
import model.ItemType;
import model.Order;
import model.Status;
import model.SubmittedOrder;
import policy.CustomPolicy;

public class ShopApp extends UnicastRemoteObject implements IShop {

    private final List<ItemType> itemList = new ArrayList<>();
    private final List<SubmittedOrder> orderList = new ArrayList<>();
    private final Map<Integer,IStatusListener> statusListeners = new HashMap<>();
    private final Map<Integer,Client> clients = new HashMap<>();

    private final DefaultTableModel clientModel;
    private final DefaultTableModel orderModel;
    private Integer i = 1;

    public ShopApp() throws RemoteException {
        // Tworzenie interfejsu graficznego
        JFrame frame = new JFrame("ShopApp");
        frame.setSize(1080, 450);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
        frame.setLocation(x, y);

        createItems();

        clientModel = new DefaultTableModel(new Object[]{"Client ID", "Name"}, 0);
        JTable clientsTable = new JTable(clientModel);
        JScrollPane clientsScrollPane = new JScrollPane(clientsTable);
        clientsScrollPane.setBounds(10, 10, 250, 400);
        frame.add(clientsScrollPane);

        orderModel = new DefaultTableModel(new Object[]{"Order ID", "Client ID", "Status"}, 0);
        JTable ordersTable = new JTable(orderModel);
        JScrollPane ordersScrollPane = new JScrollPane(ordersTable);
        ordersScrollPane.setBounds(310, 10, 400, 400);
        frame.add(ordersScrollPane);

        DefaultTableModel itemModel = new DefaultTableModel(new Object[]{"Product Name", "Price", "Category"}, 0);
        for (ItemType item : itemList) {
            Object[] o = new Object[3];
            o[0] = item.getName();
            o[1] = item.getPrice();
            o[2] = item.getCategory();
            itemModel.addRow(o);
        }
        JTable itemsTable = new JTable(itemModel);
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        itemsScrollPane.setBounds(750, 10, 300, 400);
        frame.add(itemsScrollPane);
        frame.setVisible(true);
    }

    private void createItems() {
        ItemType TShirt = new ItemType();
        TShirt.setName("TShirt");
        TShirt.setCategory(1);
        TShirt.setPrice(80);
        ItemType Cup = new ItemType();
        Cup.setName("Cup");
        Cup.setCategory(2);
        Cup.setPrice(40);
        ItemType Band = new ItemType();
        Band.setName("Band");
        Band.setCategory(3);
        Band.setPrice(10);
        ItemType Stripe = new ItemType();
        Stripe.setName("Stripe");
        Stripe.setCategory(3);
        Stripe.setPrice(15);
        ItemType Leash = new ItemType();
        Leash.setName("Leash");
        Leash.setCategory(3);
        Leash.setPrice(9);
        itemList.add(TShirt);
        itemList.add(Cup);
        itemList.add(Band);
        itemList.add(Stripe);
        itemList.add(Leash);
    }

    public static void main(String[] args) {
        Policy.setPolicy(new CustomPolicy());

        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
            System.out.println("Security manager has been set");
        }

        try {
            ShopApp shop = new ShopApp();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("IShop", shop);
            System.out.println("Shop App ready");
        } catch (Exception e) {
            System.err.println("ShopApp exception: " + e);
            e.printStackTrace();
        }
    }

    @Override
    public int register(Client c) throws RemoteException {
        clients.put(i,c);
        Object[] o = new Object[2];
        o[0] = i;
        o[1] = c.getName();
        clientModel.addRow(o);
        return i++;
    }

    @Override
    public List<ItemType> getItemList() throws RemoteException {
        return itemList;
    }

    @Override
    public int placeOrder(Order o) throws RemoteException {
        SubmittedOrder so = new SubmittedOrder(o);
        orderList.add(so);
        Object[] ord = new Object[3];
        ord[0] = so.getId();
        ord[1] = o.getClientID();
        ord[2] = so.getStatus();
        orderModel.addRow(ord);
        return so.getId();
    }

    @Override
    public List<SubmittedOrder> getSubmittedOrders() throws RemoteException {
        return orderList;
    }

    @Override
    public Status getStatus(int orderId) throws RemoteException {
        SubmittedOrder so = orderList.get(orderId-1);
        return so.getStatus();
    }

    @Override
    public boolean subscribe(IStatusListener listener, int clientId) throws RemoteException {
        statusListeners.put(clientId,listener);
        for (IStatusListener status: statusListeners.values()) {
            System.out.println(status);
        }
        return true;
    }

    @Override
    public boolean unsubscribe(int clientId) throws RemoteException {
        statusListeners.remove(clientId);
        return true;
    }

    @Override
    public boolean setStatus(int orderId, Status newStatus) throws RemoteException {
        SubmittedOrder so = orderList.get(orderId-1);
        so.setStatus(newStatus);
        orderModel.setRowCount(0);
        for (SubmittedOrder order : orderList) {
            Object[] rowData = {order.getId(), order.getOrder().getClientID(), order.getStatus()};
            orderModel.addRow(rowData);
        }
        for (IStatusListener listener : statusListeners.values()) {
            listener.statusChanged(orderId, newStatus);
        }
        return true;
    }
}
