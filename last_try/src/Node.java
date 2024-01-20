import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class Node extends UnicastRemoteObject implements NodeI {
    private static final long serialVersionUID = 1L;

    final static int n = ipAddr.length;


    int lClock, myPort;
    String myIp, myService, mypath;
    PriorityQueue<Transaction> transactions;
    HashMap<String, Integer> transactionsAcks;
    Scanner scan;

    protected Node(int idx) throws RemoteException {
        myIp = ipAddr[idx];
        myPort = ports[idx];
        myService = services[idx];
        mypath = paths[idx];
        lClock = 0;
        transactions = new PriorityQueue();
        transactionsAcks = new HashMap();
        scan = new Scanner(System.in);
    }

    public void multicastTransaction(Transaction t) throws RemoteException, NotBoundException {
        boolean delay = true;
        for (int i = 0; i < n; i++) {
            if (delay && t.sender.equals("A")) {
                if (i == 2) {
                    Delayer delayer = new Delayer(ports[i], services[i],t);
                    new Thread(delayer).start();
                    continue;
                }
            }
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeI e = (NodeI) reg.lookup(services[i]);
            e.performTransaction(t);
        }
        System.out.println("End Multicast Transaction:");
        displayTransactions();
        displayAcks();
    }

    @Override
    public void performTransaction(Transaction t) throws RemoteException, NotBoundException {
        transactions.add(t);
        if (!t.sender.equalsIgnoreCase(myService)) { // If receiver
            lClock = Math.max(lClock, t.clock) + 1;
        }
        System.out.println("Perform Transaction:");
        displayTransactions();
        displayAcks();
        multicastAck(t);
    }

    private void multicastAck(Transaction t) throws RemoteException, NotBoundException {
        boolean delay = true;
        for (int i = 0; i < n; i++) {
            if (delay && myService.equals("A")) { // Delay Sending Ack from A to C
                if (i == 2) {
                    System.out.println("|||||||||||||||||||||||||||||||||||||||||");
                    AckDelayer ackDelayer = new AckDelayer(ports[i], services[i], t);
                    new Thread(ackDelayer).start();
                    continue;
                }
            }
            Registry reg = LocateRegistry.getRegistry(ports[i]);
            NodeI e = (NodeI) reg.lookup(services[i]);
            e.ack(t);
        }
    }

    @Override
    public void ack(Transaction t) throws RemoteException {
        if (transactionsAcks.containsKey(t.tId)) {
            transactionsAcks.put(t.tId, transactionsAcks.get(t.tId) + 1);
        } else transactionsAcks.put(t.tId, 1);
    }

//    @Override
//    public Boolean uploadFile(String fileName, byte[] content, int logicalClock) throws RemoteException {
//        System.out.println("Uploading File...");
//        File localFile = new File(paths + fs.getName());
//
//        if(!localFile.exists()) {
//            localFile.getParentFile().mkdir();
//        }
//        try {
//            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));
//            out.write(fs.getData(), 0, fs.getData().length);
//            out.flush();
//            out.close();
//            return true;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//
//
//
//    @Override
//    public void deleteFile(String fileName, int logicalClock) throws RemoteException {
//
//    }
//
//    @Override
//    public List<String> searchFiles(String keyword, int logicalClock) throws RemoteException {
//        return null;
//    }


//    public FileSerializable downloadFile(String fileName, int logicalClock, String paths) throws RemoteException {
//        System.out.println("Downloading File...");
//        try {
//            File f = new File(paths + fileName);
//            FileSerializable fs = new FileSerializable();
//
//            int fileSize = (int) f.length();
//            byte[] buffer = new byte[fileSize];
//            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
//            in.read(buffer, 0, buffer.length);
//            in.close();
//            fs.setData(buffer);
//            fs.setName(fileName);
//            fs.setLastModifiedDate(new Date(f.lastModified()));
//
//            return fs;
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    public FileSerializable downloadFile(String fileName) throws RemoteException {
        System.out.println("Downloading File...");
        try {
            File f = new File(mypath + fileName);
            FileSerializable fs = new FileSerializable();

            int fileSize = (int) f.length();
            byte[] buffer = new byte[fileSize];
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
            in.read(buffer, 0, buffer.length);
            in.close();

            fs.setData(buffer);
            fs.setName(fileName);
            fs.setLastModifiedDate(new Date(f.lastModified()));

            return fs;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    public void download(String fileName) throws RemoteException, NotBoundException {
        Random random = new Random();
        int randomIndex = random.nextInt(ports.length);
        Registry reg = LocateRegistry.getRegistry(ports[randomIndex]);
        NodeI e = (NodeI) reg.lookup(services[randomIndex]);
        FileSerializable fs = e.downloadFile(fileName); //RMI
        File localFile = new File(mypath + fileName);

        if(!localFile.exists()) {
            System.out.println("can't be find");
         //   localFile.getParentFile().mkdir();
        }
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));
            out.write(fs.getData(), 0, fs.getData().length);
            out.flush();
            out.close();
            System.out.println("File Downloaded");
        } catch (FileNotFoundException u) {
            u.printStackTrace();
        } catch (IOException u) {
            u.printStackTrace();
        }
    }



    public boolean upload(FileSerializable Filepath) throws RemoteException {

        System.out.println("Uploading File...");
        File localFile = new File(mypath + Filepath.getName());

        if(!localFile.exists()) {
            localFile.getParentFile().mkdir();
        }
        try {
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));
            out.write(Filepath.getData(), 0, Filepath.getData().length);
            out.flush();
            out.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean res = upload(Filepath);

        if(res)System.out.println("File uploaded successfully.");
        else System.out.println("An error occurred. Try again later");
        return res;
    }

    @Override
    public boolean searchFiles(String fileName) throws RemoteException {
        System.out.println("Searching for File...");
        File f = new File(mypath + fileName);
        if(f.exists())return true;
        return false;
    }
    public void search( String fileName) throws RemoteException {
        boolean res = searchFiles(fileName);
        if(res)System.out.println("File found");
        else System.out.println("No matches found");
    }

    @Override
    public boolean deleteFile(String fileName) throws RemoteException {
        System.out.println("Deleting File...");
        File f = new File(mypath + fileName);
        return f.delete();
    }
    public void delete(  String fileName) throws RemoteException {
        boolean res = deleteFile(fileName);
        if(res)System.out.println("File deleted successfully.");
        else System.out.println("An error occurred. Try again later");
    }


//    public void download(  String fileName) throws RemoteException {
//        FileSerializable fs = downloadFile(fileName); //RMI
//        File localFile = new File(paths[idx] + fileName);
//
//        if(!localFile.exists()) {
//            localFile.getParentFile().mkdir();
//        }
//        try {
//            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));
//            out.write(fs.getData(), 0, fs.getData().length);
//            out.flush();
//            out.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    @Override
//    public boolean uploadFile(FileSerializable fs) throws RemoteException {
//        System.out.println("Uploading File...");
//        File localFile = new File(Path + fs.getName());
//
//        if(!localFile.exists()) {
//            localFile.getParentFile().mkdir();
//        }
//        try {
//            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(localFile));
//            out.write(fs.getData(), 0, fs.getData().length);
//            out.flush();
//            out.close();
//            return true;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }
//
//    @Override
//    public boolean searchFiles(String fileName) throws RemoteException {
//        System.out.println("Searching for File...");
//        File f = new File(FileServer.PATH + fileName);
//        if(f.exists())return true;
//        return false;
//    }
//
//    @Override
//    public boolean deleteFile(String fileName) throws RemoteException {
//        System.out.println("Deleting File...");
//        File f = new File(FileServer.PATH + fileName);
//        return f.delete();
//    }
    public void fetchNewTransaction() throws RemoteException, NotBoundException {
        if(transactions.size() > 0 && transactionsAcks.containsKey(transactions.peek().tId)
                && transactionsAcks.get(transactions.peek().tId) == n){

            System.out.println("Fetch New Transaction:");
            displayTransactions();
            displayAcks();

            Transaction t = transactions.poll();
            transactionsAcks.remove(t.tId);

            System.out.println("After Fetch New Transaction:");
            displayTransactions();
            displayAcks();

            System.out.println("Performing Transaction: " + t);
            if(t.operation == Transaction.Download) {
                download(t.fileName);
            }
            else if(t.operation == Transaction.Upload) {
                 upload(t.Filepath);
            }
            else if(t.operation == Transaction.Search) {
                search(t.fileName);
            }
            else if(t.operation == Transaction.Delete) {
                delete(t.fileName);

            }

        }
    }

    private void displayTransactions() {
        System.out.println("---------------Transactions--------------");
        for(Transaction t: transactions) {
            if(t.operation == Transaction.Download) {
                System.out.println("(" + "Download" + ")" + t.sender + ", " + t.clock + ", " + t.tId + ")");
            }
            else if (t.operation == Transaction.Upload){
                System.out.println("(" + "Upload" + ")" + t.sender + ", " + t.clock + ", " + t.tId + ")");
            }
            else if (t.operation == Transaction.Search){
                System.out.println("(" + "Search" + ")" + t.sender + ", " + t.clock + ", " + t.tId + ")");
            }
            else if (t.operation == Transaction.Delete){
                System.out.println("(" + "Delete" + ")" + t.sender + ", " + t.clock + ", " + t.tId + ")");
            }
        }
        System.out.println("=========================================");
    }

    private void displayAcks() {
        System.out.println("---------------Acks--------------");
        Set<String> keys = transactionsAcks.keySet();
        for(String k: keys) {
            System.out.println("(" + k + ", " + transactionsAcks.get(k) + ")");
        }
        System.out.println("=========================================");
    }

}