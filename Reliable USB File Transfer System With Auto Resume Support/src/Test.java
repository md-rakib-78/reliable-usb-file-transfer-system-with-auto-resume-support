import java.util.List;

public class Test {

    public static void main(String[] args) {
        
        jsonAttribute t = new jsonAttribute();
        t.si = "02";
        t.transferId = "T1001";
        t.fileName = "file.zip";
        t.sourcePath = "D:\\data";
        t.destinationPath = "E:\\backup";
        t.fileExtension = "zip";
        t.transferStatus = "Pending";

        DatabaseManager.addTransfer(t);

        // Read
        List<jsonAttribute> all = DatabaseManager.readTransfers();
        for (jsonAttribute tr : all) {
            System.out.println(tr.fileName);
        }

        // Update
        DatabaseManager.updateTransfer("T1001", "Completed");

        // Delete
        DatabaseManager.deleteTransfer("T1001");



    }
}
