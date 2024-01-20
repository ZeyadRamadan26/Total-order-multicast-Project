import java.io.FileNotFoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
//		int pId = Integer.parseInt(args[0]);
        int pId = 4;

        try {
            System.out.println("Node: " + NodeI.services[pId]);

            Node obj = new Node(pId);
            initServer(obj);
            initClient(obj, pId);

        } catch (RemoteException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (NotBoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }

    public static void initServer(Node obj) throws RemoteException{
        Registry reg = LocateRegistry.createRegistry(obj.myPort);
        reg.rebind(obj.myService, obj);
    }

    private static void initClient(Node obj, int pId) throws RemoteException, NotBoundException, FileNotFoundException {

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    obj.fetchNewTransaction();
                } catch (RemoteException ex) {
                    System.out.println(ex.getMessage());
                } catch (NotBoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0, 100);

        while (true){
            // Get message from client console
            System.out.println("File Operations Menu:");
            System.out.println("1. Download File");
            System.out.println("2. Upload File");
            System.out.println("3. Delete File");
            System.out.println("4. Search File");
            System.out.print("Enter your choice: ");
            int operation = obj.scan.nextInt();
            if(operation != 1 && operation != 2 && operation != 3 && operation != 4) {
                System.out.println("is not valid");
                continue;
            }
                Scanner scanner = new Scanner(System.in);
                System.out.print("Enter file name: ");
                String fileName = scanner.next();
                String tId = UUID.randomUUID().toString();
                String sender = obj.myService;

                obj.lClock++;
                Transaction t = null;
                if(operation == 1) {
                   t = new Transaction(tId, pId, sender, operation, obj.lClock);
                    obj.download(fileName);
                }
                else if( operation == 2){
                   t = new Transaction(tId, pId, sender, operation, obj.lClock,"zeyad.txt");
                    obj.multicastTransaction(t);
                }
                else if( operation == 3){
                  t =  new Transaction(tId, pId, sender, operation, obj.lClock);
                    obj.multicastTransaction(t);
                }
                else {
                   t = new Transaction(tId, pId, sender, operation, obj.lClock);
                    obj.search(fileName);
                }
            obj.multicastTransaction(t);
        }

    }

}