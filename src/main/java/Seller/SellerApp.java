package Seller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.Policy;
import java.util.List;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import interfaces.IShop;
import model.Status;
import model.SubmittedOrder;
import policy.CustomPolicy;

public class SellerApp extends JPanel implements Runnable{
    private IShop shop;
    private DefaultTableModel ordersModel = new DefaultTableModel();
    public SellerApp() {
        Policy.setPolicy(new CustomPolicy());

        if(System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
            System.out.println("Security manager has been set");
        }


        try {
            Registry registry = LocateRegistry.getRegistry();
            shop = (IShop) registry.lookup("IShop");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Seller App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);
        SellerApp sellerApp = new SellerApp();
        frame.add(sellerApp);
        sellerApp.start();
        frame.setVisible(true);
        sellerApp.run();
    }


    public void start() {
        getOrders();
        ordersModel = new DefaultTableModel(new Object[]{"Order ID", "Client ID", "Status"}, 0);
        JTable ordersTable = new JTable(ordersModel);
        JScrollPane clientsScrollPane = new JScrollPane(ordersTable);
        add(clientsScrollPane);
        JButton setStatusButton = new JButton("Change Status");
        setStatusButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOrderStatus();
            }
        });
        add(setStatusButton);
    }

    private void getOrders() {
        try {
            ordersModel.setRowCount(0);
            List<SubmittedOrder> submittedOrders = shop.getSubmittedOrders();
            for (SubmittedOrder order: submittedOrders) {
                ordersModel.addRow(new Object[]{order.getId(),order.getOrder().getClientID(),order.getStatus()});
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void updateOrderStatus() {
        int orderId = Integer.parseInt(JOptionPane.showInputDialog("Enter order ID:"));
        String[] choices = {"NEW", "PROCESSING", "READY", "DELIVERED"};
        String statusString = (String) JOptionPane.showInputDialog(null, "Choose now...",
                "Status Choice", JOptionPane.QUESTION_MESSAGE, null, choices, choices[1]);
        Status newStatus = Status.valueOf(statusString);
        getOrders();
        try {
            shop.setStatus(orderId, newStatus);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            getOrders();
        }
    }
}
