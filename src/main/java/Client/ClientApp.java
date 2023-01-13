package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.Policy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import interfaces.IShop;
import interfaces.IStatusListener;
import model.*;
import policy.CustomPolicy;

public class ClientApp extends UnicastRemoteObject {

    private IShop shop;
//    @Serial
    private static final long serialVersionUID = 1L;
    private int id;
    private List<ItemType> itemTypes = new ArrayList<>();
    private final Map<Integer, InsideOrder> orders = new HashMap<>();
    private DefaultTableModel orderModel;
    private DefaultTableModel itemsModel;
    private final JPanel panel = new JPanel();

    public ClientApp() throws RemoteException {

        Policy.setPolicy(new CustomPolicy());

        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
            System.out.println("Security manager has been set");
        }

        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            shop = (IShop) registry.lookup("IShop");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        register();
        JFrame frame = new JFrame("Client App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        frame.setSize(1000, 500);
        run();
        frame.setVisible(true);
    }

    public static void main(String[] args) throws RemoteException {
        new ClientApp();
    }

    public void run() {
        JButton placeOrderButton = new JButton("Place Order");
        placeOrderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placeOrder();
            }
        });
        panel.add(placeOrderButton);

        JButton checkOrderStatusButton = new JButton("Check Order Status");
        checkOrderStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int orderId = Integer.parseInt(JOptionPane.showInputDialog("Enter order ID:"));
                checkOrderStatus(orderId);
            }
        });
        panel.add(checkOrderStatusButton);

        JButton subscribeButton = new JButton("Subscribe");
        JButton unsubscribeButton = new JButton("Unsubscribe");
        subscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                subscribe();
                subscribeButton.setEnabled(false);
                unsubscribeButton.setEnabled(true);
            }
        });
        panel.add(subscribeButton);

        unsubscribeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unsubscribe();
                subscribeButton.setEnabled(true);
                unsubscribeButton.setEnabled(false);
            }
        });
        panel.add(unsubscribeButton);
        unsubscribeButton.setEnabled(false);



        JPanel tablesPanel = new JPanel();
        panel.add(tablesPanel);
        orderModel = new DefaultTableModel(new Object[]{"Order ID", "Cost", "Status"}, 0);
        JTable ordersTable = new JTable(orderModel);
        JScrollPane ordersScrollPane = new JScrollPane(ordersTable);
        tablesPanel.add(ordersScrollPane);

        itemsModel = new DefaultTableModel(new Object[]{"Item ID", "Name", "Price", "Category"}, 0);
        JTable itemsTable = new JTable(itemsModel);
        JScrollPane itemsScrollPane = new JScrollPane(itemsTable);
        tablesPanel.add(itemsScrollPane);
        viewItemList();
    }

    private void register() {
        String name = JOptionPane.showInputDialog(null,"Enter your name:","Registration",JOptionPane.QUESTION_MESSAGE);
        if ((name != null) && (name.length() > 0)) {
            Client client = new Client();
            client.setName(name);
            try {
                id = shop.register(client);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        else{
            System.exit(0);
        }
    }

    private void viewItemList() {
        try {
            itemTypes = shop.getItemList();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        for (ItemType item : itemTypes) {
            Object[] o = new Object[4];
            o[0] = itemTypes.indexOf(item);
            o[1] = item.getName();
            o[2] = item.getPrice();
            o[3] = item.getCategory();
            itemsModel.addRow(o);
        }
    }

    private void placeOrder() {
        int reply = 0;
        Order o = new Order(id);
        while (reply == 0) {
            int itemId = Integer.parseInt(JOptionPane.showInputDialog("Enter item ID:"));
            int quantity = Integer.parseInt(JOptionPane.showInputDialog("Enter quantity:"));
            String advert = JOptionPane.showInputDialog("Enter advert:");
            OrderLine orderLine = new OrderLine(itemTypes.get(itemId), quantity, advert);
            o.addOrderLine(orderLine);
            reply = JOptionPane.showConfirmDialog(null, "Do you want to add something to your order?", "Order", JOptionPane.YES_NO_OPTION);
        }
        try {
            int orderId = shop.placeOrder(o);
            orders.put(orderId,new InsideOrder(o,Status.NEW,orderId));
            orderModel.addRow(new Object[]{orderId,o.getCost(),orders.get(orderId).getStatus()});
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void checkOrderStatus(int orderId) {
        try {
            Status status = shop.getStatus(orderId);
            orders.get(orderId).setStatus(status);
            orderModel.setRowCount(0);
            for (InsideOrder order : orders.values()) {
                Object[] rowData = {order.getOrderId(), order.getOrder().getCost(), order.getStatus() };
                orderModel.addRow(rowData);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void subscribe() {
        try {
            IStatusListener listener = new StatusListener(orders, orderModel);
            shop.subscribe(listener,id);
            for (InsideOrder order : orders.values()) {
                checkOrderStatus(order.getOrderId());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribe() {
        try {
            shop.unsubscribe(id);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}

