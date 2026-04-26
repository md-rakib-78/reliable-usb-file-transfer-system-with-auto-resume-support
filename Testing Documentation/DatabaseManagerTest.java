import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;
import java.nio.file.*;

/**
 * Automation Test Suite: DatabaseManager
 * Tests all CRUD operations on history.json using JUnit 4
 */
public class DatabaseManagerTest {

    private static final String TEST_JSON = "history.json";

    // ── Before each test: clean up any existing history.json ──────────────────
    @Before
    public void setUp() throws Exception {
        File f = new File(TEST_JSON);
        if (f.exists()) f.delete();
    }

    // ── After each test: clean up ─────────────────────────────────────────────
    @After
    public void tearDown() throws Exception {
        File f = new File(TEST_JSON);
        if (f.exists()) f.delete();
    }

    // ── Helper: build a sample transfer record ────────────────────────────────
    private jsonAttribute makeRecord(String si, String id, String status) {
        jsonAttribute t = new jsonAttribute();
        t.si              = si;
        t.transferId      = id;
        t.fileName        = "testfile.txt";
        t.sourcePath      = "C:/source/testfile.txt";
        t.destinationPath = "D:/dest/testfile.txt";
        t.fileExtension   = "txt";
        t.fileSize        = "100";
        t.transferStatus  = status;
        t.transferPercentage = "100.00";
        t.interruptStatus = "0";
        return t;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-01: readTransfers() returns empty list when no file exists
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testReadTransfers_NoFile_ReturnsEmptyList() {
        List<jsonAttribute> result = DatabaseManager.readTransfers();
        assertNotNull("Result should not be null", result);
        assertEquals("Should return empty list when no file", 0, result.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-02: addTransfer() creates history.json and stores record
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testAddTransfer_CreatesFileAndStoresRecord() {
        jsonAttribute t = makeRecord("1", "T0001", "Completed");
        DatabaseManager.addTransfer(t);

        File f = new File(TEST_JSON);
        assertTrue("history.json should be created", f.exists());

        List<jsonAttribute> list = DatabaseManager.readTransfers();
        assertEquals("Should have 1 record", 1, list.size());
        assertEquals("TransferId should match", "T0001", list.get(0).transferId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-03: addTransfer() appends without overwriting existing records
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testAddTransfer_AppendsMultipleRecords() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Completed"));
        DatabaseManager.addTransfer(makeRecord("2", "T0002", "Interrupted"));
        DatabaseManager.addTransfer(makeRecord("3", "T0003", "Completed"));

        List<jsonAttribute> list = DatabaseManager.readTransfers();
        assertEquals("Should have 3 records", 3, list.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-04: readTransfers() parses all fields correctly
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testReadTransfers_ParsesAllFieldsCorrectly() {
        jsonAttribute t = makeRecord("1", "T0001", "Completed");
        t.fileName        = "video.mp4";
        t.fileExtension   = "mp4";
        t.fileSize        = "512";
        t.interruptStatus = "2";
        t.transferPercentage = "75.50";
        DatabaseManager.addTransfer(t);

        jsonAttribute result = DatabaseManager.readTransfers().get(0);
        assertEquals("video.mp4",  result.fileName);
        assertEquals("mp4",        result.fileExtension);
        assertEquals("512",        result.fileSize);
        assertEquals("2",          result.interruptStatus);
        assertEquals("75.50",      result.transferPercentage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-05: updateTransfer() updates status and interrupt count
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testUpdateTransfer_UpdatesStatusAndInterruptCount() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Interrupted"));
        DatabaseManager.updateTransfer("T0001", "Completed", "3", "100.00");

        jsonAttribute updated = DatabaseManager.readTransfers().get(0);
        assertEquals("Status should be Completed",  "Completed",  updated.transferStatus);
        assertEquals("InterruptStatus should be 3", "3",          updated.interruptStatus);
        assertEquals("Percentage should be 100.00", "100.00",     updated.transferPercentage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-06: updateTransfer() does not affect other records
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testUpdateTransfer_DoesNotAffectOtherRecords() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Interrupted"));
        DatabaseManager.addTransfer(makeRecord("2", "T0002", "Interrupted"));
        DatabaseManager.updateTransfer("T0001", "Completed", "1", "100.00");

        List<jsonAttribute> list = DatabaseManager.readTransfers();
        assertEquals("T0002 should remain Interrupted", "Interrupted",
                list.stream().filter(x -> x.transferId.equals("T0002"))
                        .findFirst().get().transferStatus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-07: deleteTransfer() removes correct record
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testDeleteTransfer_RemovesCorrectRecord() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Completed"));
        DatabaseManager.addTransfer(makeRecord("2", "T0002", "Completed"));
        DatabaseManager.deleteTransfer("T0001");

        List<jsonAttribute> list = DatabaseManager.readTransfers();
        assertEquals("Should have 1 record left", 1, list.size());
        assertEquals("Remaining record should be T0002", "T0002", list.get(0).transferId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-08: deleteTransfer() on non-existent ID changes nothing
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testDeleteTransfer_NonExistentId_NoChange() {
        DatabaseManager.addTransfer(makeRecord("1", "T0001", "Completed"));
        DatabaseManager.deleteTransfer("T9999");

        List<jsonAttribute> list = DatabaseManager.readTransfers();
        assertEquals("Record count should be unchanged", 1, list.size());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-09: writeTransfers() then readTransfers() is consistent
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testWriteThenRead_IsConsistent() {
        List<jsonAttribute> original = new ArrayList<>();
        original.add(makeRecord("1", "T0001", "Completed"));
        original.add(makeRecord("2", "T0002", "Interrupted"));
        DatabaseManager.writeTransfers(original);

        List<jsonAttribute> read = DatabaseManager.readTransfers();
        assertEquals("Count should match", 2, read.size());
        assertEquals("First ID should match",  "T0001", read.get(0).transferId);
        assertEquals("Second ID should match", "T0002", read.get(1).transferId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-DB-10: readTransfers() handles malformed JSON gracefully
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testReadTransfers_MalformedJson_ReturnsEmptyList() throws Exception {
        // Write garbage into history.json
        try (FileWriter fw = new FileWriter(TEST_JSON)) {
            fw.write("{ THIS IS NOT VALID JSON !!! }}}");
        }
        List<jsonAttribute> result = DatabaseManager.readTransfers();
        assertNotNull("Should not throw, should return empty list", result);
    }
}
