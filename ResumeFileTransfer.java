import java.io.*;

public class ResumeFileTransfer {

    private static final int CHUNK_SIZE = 8 * 1024 * 1024; 

    public static void main(String[] args) {

        FileChooserUtil.chooseFile();
        String sourcePath = FileChooserUtil.selectedPath;
        String sourceFileName = FileChooserUtil.selectedFileName;

        if (sourcePath == null) {
            System.out.println("No file selected.");
            return;
        }
        System.out.println("Selected file path: " + sourcePath);
        System.out.println("Selected file name: " + sourceFileName);

        FileChooserUtil.chooseFolderAndGetPath();
        String destFolder = FileChooserUtil.SelectedDestinationFolder;

        if (destFolder == null) {
            System.out.println("No folder selected.");
            return;
        }
        System.out.println("Selected destination folder: " + destFolder);

        File sourceFile = new File(sourcePath);
        File destFile = new File(destFolder);

        File logFile = new File(destFile.getAbsolutePath() + ".transfer.log");

        try {
            copyFileWithResume(sourceFile, destFile, logFile);
        } catch (IOException e) {
            System.out.println("Error during transfer:");
            e.printStackTrace();
        }
    }


    private static void copyFileWithResume(File source, File dest, File logFile) throws IOException {

        if (!source.exists()) {
            System.out.println("Source file not found: " + source.getAbsolutePath());
            return;
        }

        long fileSize = source.length();
        long resumeFromLog = loadProgress(logFile);
        long actualDestSize = dest.exists() ? dest.length() : 0;
        long resumePosition = Math.min(resumeFromLog, actualDestSize);

        System.out.println("File size: " + fileSize / (1024 * 1024) + " MB");
        System.out.println("Resuming from byte: " + resumePosition);

        try (RandomAccessFile src = new RandomAccessFile(source, "r");
             RandomAccessFile dst = new RandomAccessFile(dest, "rw")) {

            src.seek(resumePosition);
            dst.seek(resumePosition);

            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            long totalCopied = resumePosition;

            while ((bytesRead = src.read(buffer)) != -1) {
                try {
                    dst.write(buffer, 0, bytesRead);
                    dst.getFD().sync(); 

                    totalCopied += bytesRead;
                    saveProgress(totalCopied, logFile);

                    double percent = (totalCopied * 100.0) / fileSize;
                    System.out.printf("Progress: %.2f%%%n", percent);

                } catch (IOException e) {
                    System.out.println("\nUSB disconnected or write error!");
                    System.out.println("Progress saved at byte: " + totalCopied);
                    return; 
                }
            }

            System.out.println("Transfer complete!");
            clearProgress(logFile); 
        }
    }


    private static void saveProgress(long position, File logFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile))) {
            writer.write(Long.toString(position));
        }
    }


    private static long loadProgress(File logFile) throws IOException {
        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line = reader.readLine();
                return Long.parseLong(line.trim());
            } catch (Exception e) {
                System.out.println("Warning: log file corrupted, starting from 0");
                return 0;
            }
        }
        return 0;
    }


    private static void clearProgress(File logFile) {
        if (logFile.exists()) {
            logFile.delete();
        }
    }
}
