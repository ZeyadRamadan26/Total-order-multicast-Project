import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Delayer implements Runnable {
    private int port;
    private String serviceName;
    private Transaction transaction;

    public Delayer(int port, String serviceName, Transaction transaction) {
        this.port = port;
        this.serviceName = serviceName;
        this.transaction = transaction;
    }

    public void run() {
        try {
            Thread.sleep(5000L);
            Registry reg = LocateRegistry.getRegistry(this.port);
            NodeI e = (NodeI)reg.lookup(this.serviceName);
            e.performTransaction(this.transaction);
        } catch (NotBoundException | InterruptedException | RemoteException var3) {
            var3.printStackTrace();
        }

    }
}