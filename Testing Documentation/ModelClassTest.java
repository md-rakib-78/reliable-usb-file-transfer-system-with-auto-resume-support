import org.junit.*;
import static org.junit.Assert.*;

/**
 * Automation Test Suite: Data Model Classes
 * Tests jsonAttribute and transferAttribute POJO behavior
 */
public class ModelClassTest {

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-01: jsonAttribute fields are null by default
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testJsonAttribute_DefaultFieldsAreNull() {
        jsonAttribute t = new jsonAttribute();
        assertNull("si should be null",               t.si);
        assertNull("transferId should be null",       t.transferId);
        assertNull("fileName should be null",         t.fileName);
        assertNull("sourcePath should be null",       t.sourcePath);
        assertNull("destinationPath should be null",  t.destinationPath);
        assertNull("fileExtension should be null",    t.fileExtension);
        assertNull("transferStatus should be null",   t.transferStatus);
        assertNull("transferPercentage should be null", t.transferPercentage);
        assertNull("fileSize should be null",         t.fileSize);
        assertNull("interruptStatus should be null",  t.interruptStatus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-02: jsonAttribute fields can be set and retrieved correctly
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testJsonAttribute_SetAndGetAllFields() {
        jsonAttribute t = new jsonAttribute();
        t.si                 = "1";
        t.transferId         = "T0001";
        t.fileName           = "video.mp4";
        t.sourcePath         = "C:/videos/video.mp4";
        t.destinationPath    = "E:/backup/video.mp4";
        t.fileExtension      = "mp4";
        t.transferStatus     = "Completed";
        t.transferPercentage = "100.00";
        t.fileSize           = "1024";
        t.interruptStatus    = "2";

        assertEquals("1",                    t.si);
        assertEquals("T0001",                t.transferId);
        assertEquals("video.mp4",            t.fileName);
        assertEquals("C:/videos/video.mp4",  t.sourcePath);
        assertEquals("E:/backup/video.mp4",  t.destinationPath);
        assertEquals("mp4",                  t.fileExtension);
        assertEquals("Completed",            t.transferStatus);
        assertEquals("100.00",               t.transferPercentage);
        assertEquals("1024",                 t.fileSize);
        assertEquals("2",                    t.interruptStatus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-03: jsonAttribute allows "Interrupted" status value
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testJsonAttribute_InterruptedStatus() {
        jsonAttribute t = new jsonAttribute();
        t.transferStatus = "Interrupted";
        assertEquals("Interrupted", t.transferStatus);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-04: jsonAttribute interruptStatus stores numeric string
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testJsonAttribute_InterruptStatusNumericString() {
        jsonAttribute t = new jsonAttribute();
        t.interruptStatus = "5";
        int count = Integer.parseInt(t.interruptStatus);
        assertEquals("interruptStatus parsed as int should be 5", 5, count);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-05: jsonAttribute transferPercentage stores decimal string
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testJsonAttribute_PercentageDecimalString() {
        jsonAttribute t = new jsonAttribute();
        t.transferPercentage = "67.34";
        double pct = Double.parseDouble(t.transferPercentage);
        assertEquals("Percentage should be 67.34", 67.34, pct, 0.001);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-06: transferAttribute default fields are null
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testTransferAttribute_DefaultFieldsAreNull() {
        transferAttribute ta = new transferAttribute();
        assertNull("sourcePath should be null",     ta.sourcePath);
        assertNull("sourceFileName should be null", ta.sourceFileName);
        assertNull("destFolder should be null",     ta.destFolder);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-07: transferAttribute fields can be set and retrieved
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testTransferAttribute_SetAndGetFields() {
        transferAttribute ta = new transferAttribute();
        ta.sourcePath     = "C:/docs/report.pdf";
        ta.sourceFileName = "report.pdf";
        ta.destFolder     = "E:/backup/";

        assertEquals("C:/docs/report.pdf", ta.sourcePath);
        assertEquals("report.pdf",         ta.sourceFileName);
        assertEquals("E:/backup/",         ta.destFolder);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-M-08: jsonAttribute si field used for serial number ordering
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testJsonAttribute_SiFieldNumericOrdering() {
        jsonAttribute t1 = new jsonAttribute(); t1.si = "1";
        jsonAttribute t2 = new jsonAttribute(); t2.si = "2";
        jsonAttribute t3 = new jsonAttribute(); t3.si = "10";

        assertTrue("si=10 > si=2 as integer",
                Integer.parseInt(t3.si) > Integer.parseInt(t2.si));
        assertTrue("si=2 > si=1 as integer",
                Integer.parseInt(t2.si) > Integer.parseInt(t1.si));
    }
}
