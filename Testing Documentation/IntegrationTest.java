import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

/**
 * Automation Integration Test Suite
 * Tests interactions between DatabaseManager, jsonAttribute, and the file system
 * These tests simulate full transfer lifecycle scenarios end-to-end (without GUI).
 */
public class IntegrationTest {

    private static final String TEST_JSON = "history.json";

    @Before
    public void setUp() {
        File f = new File(TEST_JSON);
        if (f.exists()) f.delete();
    }

    @After
    public void tearDown() {
        File f = new File(TEST_JSON);
        if (f.exists()) f.delete();
    }

    private jsonAttribute makeRecord(String si, String id, String status, String interrupt, String pct) {
        jsonAttribute t = new jsonAttribute();
        t.si                 = si;
        t.transferId         = id;
        t.fileName           = "file_" + si + ".dat";
        t.sourcePath         = "C:/src/file_" + si + ".dat";
        t.destinationPath    = "E:/dest/file_" + si + ".dat";
        t.fileExtension      = "dat";
        t.fileSize           = "200";
        t.transferStatus     = status;
        t.transferPercentage = pct;
        t.interruptStatus    = interrupt;
        return t;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-01: Full new transfer lifecycle - Completed
    // Simulates: new transfer starts (id=000) → completes → DB record added
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testFullTransferLifecycle_Completed() {
        // Simulate what ResumeFileTransfer does on completion (transferId="000")
        int serialNo = 0;
        List<jsonAttribute> all = DatabaseManager.readTransfers();
        for (jsonAttribute tr : all) {
            try { serialNo = Math.max(serialNo, Integer.parseInt(tr.si)); } catch (Exception e) {}
        }

        jsonAttribute t = makeRecord(
                String.valueOf(serialNo + 1),
                "T" + String.format("%04d", serialNo + 1),
                "Completed", "0", "100.00");
        DatabaseManager.addTransfer(t);

        List<jsonAttribute> result = DatabaseManager.readTransfers();
        assertEquals("Should have 1 record", 1, result.size());
        assertEquals("Status should be Completed", "Completed", result.get(0).transferStatus);
        assertEquals("TransferId should be T0001", "T0001", result.get(0).transferId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-02: Transfer interrupted mid-way → DB records Interrupted
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testTransferInterrupted_RecordSaved() {
        jsonAttribute t = makeRecord("1", "T0001", "Interrupted", "1", "45.67");
        DatabaseManager.addTransfer(t);

        jsonAttribute result = DatabaseManager.readTransfers().get(0);
        assertEquals("Interrupted", result.transferStatus);
        assertEquals("1",           result.interruptStatus);
        assertEquals("45.67",       result.transferPercentage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-03: Transfer interrupted multiple times → interrupt count increments
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testMultipleInterrupts_CountIncrements() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Interrupted", "1", "30.00"));
        // Simulate 2nd interrupt: interruptStatus = max(current) + 1 = 2
        DatabaseManager.updateTransfer("T0001", "Interrupted", "2", "55.00");
        // Simulate 3rd interrupt
        DatabaseManager.updateTransfer("T0001", "Interrupted", "3", "75.00");

        jsonAttribute result = DatabaseManager.readTransfers().get(0);
        assertEquals("Interrupt count should be 3", "3", result.interruptStatus);
        assertEquals("Percentage should reflect last interrupt", "75.00", result.transferPercentage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-04: Resume interrupted transfer → status updates to Completed
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testResumeInterrupted_UpdatesToCompleted() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Interrupted", "2", "60.00"));
        // Simulate resume + completion
        DatabaseManager.updateTransfer("T0001", "Completed", "2", "100.00");

        jsonAttribute result = DatabaseManager.readTransfers().get(0);
        assertEquals("Status should be Completed",  "Completed", result.transferStatus);
        assertEquals("Interrupt count preserved",   "2",         result.interruptStatus);
        assertEquals("Percentage should be 100.00", "100.00",    result.transferPercentage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-05: Serial number auto-increments across multiple new transfers
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testSerialNumberAutoIncrement() {
        for (int i = 0; i < 5; i++) {
            List<jsonAttribute> all = DatabaseManager.readTransfers();
            int serialNo = 0;
            for (jsonAttribute tr : all) {
                try { serialNo = Math.max(serialNo, Integer.parseInt(tr.si)); } catch (Exception e) {}
            }
            jsonAttribute t = makeRecord(
                    String.valueOf(serialNo + 1),
                    "T" + String.format("%04d", serialNo + 1),
                    "Completed", "0", "100.00");
            DatabaseManager.addTransfer(t);
        }

        List<jsonAttribute> all = DatabaseManager.readTransfers();
        assertEquals("Should have 5 records", 5, all.size());
        assertEquals("Last SI should be 5", "5", all.get(4).si);
        assertEquals("Last transferId should be T0005", "T0005", all.get(4).transferId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-06: Log file save and load round-trip (progress persistence)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testLogFileSaveLoadRoundTrip() throws Exception {
        File logFile = File.createTempFile("transfer_int_test", ".log");
        logFile.deleteOnExit();

        java.lang.reflect.Method save = ResumeFileTransfer.class
                .getDeclaredMethod("saveProgress", long.class, File.class);
        save.setAccessible(true);

        java.lang.reflect.Method load = ResumeFileTransfer.class
                .getDeclaredMethod("loadProgress", File.class);
        load.setAccessible(true);

        // Simulate multiple chunk writes
        long[] positions = {8388608L, 16777216L, 25165824L, 33554432L};
        for (long pos : positions) {
            save.invoke(null, pos, logFile);
        }

        long finalPos = (long) load.invoke(null, logFile);
        assertEquals("Final log position should be last saved value", 33554432L, finalPos);
        logFile.delete();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-07: Delete all records leaves empty list
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testDeleteAllRecords_LeavesEmptyList() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Completed", "0", "100.00"));
        DatabaseManager.addTransfer(makeRecord("2", "T0002", "Completed", "0", "100.00"));
        DatabaseManager.addTransfer(makeRecord("3", "T0003", "Interrupted", "1", "50.00"));

        DatabaseManager.deleteTransfer("T0001");
        DatabaseManager.deleteTransfer("T0002");
        DatabaseManager.deleteTransfer("T0003");

        List<jsonAttribute> list = DatabaseManager.readTransfers();
        assertEquals("All records deleted, list should be empty", 0, list.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-INT-08: Mixed statuses are stored and retrieved independently
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testMixedStatuses_StoredAndRetrievedCorrectly() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Completed",   "0", "100.00"));
        DatabaseManager.addTransfer(makeRecord("2", "T0002", "Interrupted", "3", "72.50"));
        DatabaseManager.addTransfer(makeRecord("3", "T0003", "Interrupted", "1", "10.00"));

        List<jsonAttribute> list = DatabaseManager.readTransfers();
        assertEquals(3, list.size());

        long completedCount   = list.stream().filter(t -> "Completed".equals(t.transferStatus)).count();
        long interruptedCount = list.stream().filter(t -> "Interrupted".equals(t.transferStatus)).count();

        assertEquals("1 completed record",    1, completedCount);
        assertEquals("2 interrupted records", 2, interruptedCount);
    }
}
