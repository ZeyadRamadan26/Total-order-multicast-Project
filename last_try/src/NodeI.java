
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface NodeI extends Remote{
    String[] ipAddr = {"127.0.0.1", "127.0.0.1", "127.0.0.1", "127.0.0.1", "127.0.0.1"};
    String[] services = {"A", "B", "C","D","E"};
    Integer[] ports = {2000, 3000, 4000, 6000, 7000};
    String[] paths = {"C:\\Users\\ZeyadRamadan\\Desktop\\Zeyadramadan20102876\\mix\\last_try\\local\\", "C:\\Users\\ZeyadRamadan\\Desktop\\Zeyadramadan20102876\\mix\\last_try\\local1\\", "C:\\Users\\ZeyadRamadan\\Desktop\\Zeyadramadan20102876\\mix\\last_try\\local2\\","C:\\Users\\ZeyadRamadan\\Desktop\\Zeyadramadan20102876\\mix\\last_try\\local3\\","C:\\Users\\ZeyadRamadan\\Desktop\\Zeyadramadan20102876\\mix\\last_try\\local4\\"};
    void performTransaction(Transaction t) throws RemoteException, NotBoundException;
    void ack(Transaction t) throws RemoteException;
    FileSerializable downloadFile(String fileName) throws RemoteException;
    boolean upload(FileSerializable f) throws RemoteException;
    boolean searchFiles(String fileName) throws RemoteException;
    boolean deleteFile(String fileName) throws RemoteException;
}
