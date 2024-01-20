import java.io.*;
import java.util.Date;

public class Transaction implements Comparable<Transaction>, Serializable{
    private static final long serialVersionUID = 1L;

    public static final int Download = 1;
    public static final int Upload = 2;
    public static final int Delete = 3;
    public static final int Search = 4;
    public String fileName;

    String tId;
    String sender;
    FileSerializable Filepath;
    int pId, clock, operation;


    public Transaction(String tId, int pId, String sender, int operation, int clock) {
        this.tId = tId;
        this.pId = pId;
        this.sender = sender;
        this.operation = operation;
        this.clock = clock;
    }
    public Transaction(String tId, int pId, String sender, int operation, int clock ,String Filepath) throws FileNotFoundException {
        this.tId = tId;
        this.pId = pId;
        this.sender = sender;
        this.operation = operation;
        this.clock = clock;

        File f = new File("C:\\Users\\ZeyadRamadan\\Desktop\\mix\\last_try\\local2\\" + "zeyad.txt");
        FileSerializable fs = new FileSerializable();
        int fileSize = (int) f.length();
        byte[] buffer = new byte[fileSize];
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
        try {
            in.read(buffer, 0, buffer.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            in.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fs.setData(buffer);
        fs.setName(Filepath);
        fs.setLastModifiedDate(new Date(f.lastModified()));
        this.Filepath = fs;
    }


    @Override
    public String toString() {
        if(operation == Transaction.Download) {
            return sender + ": " + "Download " + Download + ", Logical Clock = " + clock;
        }
        else if (operation == Transaction.Upload){
            return sender + ": " + "Upload " + Upload + ", Logical Clock = " + clock;
        }
        else if (operation == Transaction.Search){
            return sender + ": " + "Search " + Search + ", Logical Clock = " + clock;
        }
        else if (operation == Transaction.Delete){
            return sender + ": " + "Delete " + Delete + ", Logical Clock = " + clock;
        }
        return null;
    }

    // Total Order using logical clocks
    @Override
    public int compareTo(Transaction o) {
        // Tie Breaker
        if(this.clock == o.clock)
            return this.pId - o.pId;
        return this.clock - o.clock;
    }

}
