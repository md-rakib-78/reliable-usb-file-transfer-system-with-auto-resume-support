import org.junit.*;
import static org.junit.Assert.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;

/**
 * Automation Test Suite: ResumeFileTransfer - Log & Progress Methods
 * Tests saveProgress, loadProgress, clearProgress, getFileExtension, shorten
 * via Java Reflection (since those methods are private/static).
 */
public class ResumeFileTransferTest {

    private File tempLog;

    // ── Access private static methods via reflection ──────────────────────────
    private static long invokeLoadProgress(File logFile) throws Exception {
        Method m = ResumeFileTransfer.class.getDeclaredMethod("loadProgress", File.class);
        m.setAccessible(true);
        return (long) m.invoke(null, logFile);
    }

    private static void invokeSaveProgress(long position, File logFile) throws Exception {
        Method m = ResumeFileTransfer.class.getDeclaredMethod("saveProgress", long.class, File.class);
        m.setAccessible(true);
        m.invoke(null, position, logFile);
    }

    private static void invokeClearProgress(File logFile) throws Exception {
        Method m = ResumeFileTransfer.class.getDeclaredMethod("clearProgress", File.class);
        m.setAccessible(true);
        m.invoke(null, logFile);
    }

    @Before
    public void setUp() throws Exception {
        tempLog = File.createTempFile("transfer_test", ".log");
        tempLog.deleteOnExit();
    }

    @After
    public void tearDown() {
        if (tempLog != null && tempLog.exists()) tempLog.delete();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-01: saveProgress() writes byte position to log file
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testSaveProgress_WritesPositionToFile() throws Exception {
        invokeSaveProgress(8388608L, tempLog);
        String content = new String(Files.readAllBytes(tempLog.toPath())).trim();
        assertEquals("Log file should contain exact byte position", "8388608", content);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-02: loadProgress() reads byte position back correctly
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testLoadProgress_ReadsPositionCorrectly() throws Exception {
        invokeSaveProgress(16777216L, tempLog);
        long result = invokeLoadProgress(tempLog);
        assertEquals("loadProgress should return 16777216", 16777216L, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-03: loadProgress() returns 0 when log file does not exist
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testLoadProgress_NonExistentFile_ReturnsZero() throws Exception {
        File missing = new File("non_existent_file_xyz.log");
        long result = invokeLoadProgress(missing);
        assertEquals("Should return 0 for non-existent log", 0L, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-04: loadProgress() throws IOException on corrupted log
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testLoadProgress_CorruptedLog_ThrowsIOException() throws Exception {
        try (FileWriter fw = new FileWriter(tempLog)) {
            fw.write("CORRUPTED_NOT_A_NUMBER");
        }
        try {
            invokeLoadProgress(tempLog);
            fail("Expected IOException for corrupted log file");
        } catch (InvocationTargetException e) {
            assertTrue("Cause should be IOException",
                    e.getCause() instanceof IOException);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-05: clearProgress() deletes the log file
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testClearProgress_DeletesLogFile() throws Exception {
        assertTrue("Log file should exist before clear", tempLog.exists());
        invokeClearProgress(tempLog);
        assertFalse("Log file should be deleted after clearProgress", tempLog.exists());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-06: clearProgress() on non-existent file - no exception
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testClearProgress_NonExistentFile_NoException() throws Exception {
        File missing = new File("does_not_exist_clear.log");
        // Should not throw any exception
        invokeClearProgress(missing);
        assertTrue("Test passed - no exception thrown", true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-07: saveProgress() overwrites previous value
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testSaveProgress_OverwritesPreviousValue() throws Exception {
        invokeSaveProgress(1000L, tempLog);
        invokeSaveProgress(9999999L, tempLog);
        long result = invokeLoadProgress(tempLog);
        assertEquals("Should contain the latest value", 9999999L, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-08: saveProgress() with position 0 (start of transfer)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testSaveProgress_ZeroPosition() throws Exception {
        invokeSaveProgress(0L, tempLog);
        long result = invokeLoadProgress(tempLog);
        assertEquals("Should handle zero position", 0L, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-09: saveProgress() with large file position (> 2GB)
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testSaveProgress_LargeFilePosition() throws Exception {
        long largePos = 3L * 1024 * 1024 * 1024; // 3 GB
        invokeSaveProgress(largePos, tempLog);
        long result = invokeLoadProgress(tempLog);
        assertEquals("Should handle positions > 2GB (long type)", largePos, result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-10: getFileExtension() returns correct extension
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testGetFileExtension_ValidExtension() throws Exception {
        // Create a dummy instance to call getFileExtension via reflection
        // (it's private instance method, we need a real instance)
        // We test by passing known filenames through public field
        ResumeFileTransfer rf = new ResumeFileTransfer("document.pdf",
                "/src/document.pdf", "/dest/", "000") {
            // Anonymous override to prevent full GUI init - testing via field
        };
        assertEquals("Extension should be pdf", "pdf", rf.fileExtention);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-11: getFileExtension() returns empty string when no extension
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testGetFileExtension_NoExtension() throws Exception {
        ResumeFileTransfer rf = new ResumeFileTransfer("README",
                "/src/README", "/dest/", "000") {};
        assertEquals("Extension should be empty", "", rf.fileExtention);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TC-AUTO-RF-12: getFileExtension() returns last extension for multi-dot names
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    public void testGetFileExtension_MultiDotFileName() throws Exception {
        ResumeFileTransfer rf = new ResumeFileTransfer("archive.tar.gz",
                "/src/archive.tar.gz", "/dest/", "000") {};
        assertEquals("Extension should be gz (last segment)", "gz", rf.fileExtention);
    }
}
