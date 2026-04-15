import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

public class ResumeFileTransfer extends ProgressBar {

    private static final int CHUNK_SIZE = 8 * 1024 * 1024;

    public String sourceFileName, sourcePath, destFolder,fileExtention;
    public String transferId="000";

    // Constructor to initialize and start the transfer
    public ResumeFileTransfer(String sourceFileName, String sourcePath, String destFolder, String transferID) {
        super();

        this.sourceFileName = sourceFileName;
        this.sourcePath = sourcePath;
        this.destFolder = destFolder;
        this.transferId = transferID;
        this.fileExtention = getFileExtension(sourceFileName);

        System.out.println("Transfer ID: " + transferId);

        fileNameLabel.setText("<html><b>File name:</b> " + shorten(sourceFileName, 60) + "</html>");
        fileNameLabel.setToolTipText(sourceFileName);

        sourceLabel.setText("<html><b>Source:</b> " + shorten(sourcePath, 60) + "</html>");
        sourceLabel.setToolTipText(sourcePath);

        destinationLabel.setText("<html><b>Destination:</b> " + shorten(destFolder, 60) + "</html>");
        destinationLabel.setToolTipText(destFolder);

        File sourceFile = new File(sourcePath);
        File destFile = new File(destFolder);
        File logFile = new File(destFile.getAbsolutePath() + ".transfer.log");

        
        new Thread(() -> {
            try {
                copyFileWithResume(sourceFile, destFile, logFile);
            } catch (IOException e) {
                System.out.println("Error during transfer:");
                e.printStackTrace();
            }
        }).start();


    }


    // Utility to extract file extension
    private String getFileExtension(String sourceFileName2) {
        int lastDotIndex = sourceFileName2.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return sourceFileName2.substring(lastDotIndex + 1);
        }
        return "";
    }


    // Core method to copy file with resume support
    private void copyFileWithResume(File source, File dest, File logFile) throws IOException {

        if (!source.exists()) {
            System.out.println("Source file not found: " + source.getAbsolutePath());
            return;
        }

        long fileSize = source.length();
        long resumeFromLog = loadProgress(logFile);
        long actualDestSize = dest.exists() ? dest.length() : 0;
        long resumePosition = Math.min(resumeFromLog, actualDestSize);

        int fileSizeMB = (int) (fileSize / (1024 * 1024));
        fileSizeLabel.setText(("<html><b>File size:</b> " + fileSizeMB + " MB</html>"));
        System.out.println("File size: " + fileSize / (1024 * 1024) + " MB");

        System.out.println("Resuming from byte: " + resumePosition);
        resumeLabel.setText(("<html><b>Resuming from byte:</b> " + resumePosition + "</html>"));

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

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue((int) percent);
                        percentLabel.setText("<html><b>Progress:</b> " + String.format("%.2f%%", percent) + "</html>");
                    });

         
                    System.out.printf("Progress: %.2f%%%n", percent);

                } catch (IOException e) {

                    System.out.println("\nUSB disconnected or write error!");
                    System.out.println("Progress saved at byte: " + totalCopied);

                    // update transfer status to Interrupted in history

                    if (transferId.equals("000")) {
                        // Insert transfer record into history
                        int serialNo = 0;
                        List<jsonAttribute> all = DatabaseManager.readTransfers();
                        if (all == null) {
                            all = new ArrayList<>();
                        }
                        for (jsonAttribute tr : all) {
                            try {
                                if (tr.si != null && !tr.si.isEmpty()) {
                                    int current = Integer.parseInt(tr.si);
                                    serialNo = Math.max(serialNo, current);
                                }
                            } catch (Exception a) {
                            }
                        }
                        jsonAttribute t = new jsonAttribute();
                        t.si = String.valueOf(serialNo + 1);
                        t.transferId = "T" + String.format("%04d", serialNo + 1);
                        t.fileName = sourceFileName != null ? sourceFileName : "";
                        t.sourcePath = sourcePath != null ? sourcePath : "";
                        t.destinationPath = destFolder != null ? destFolder : "";
                        t.fileExtension = fileExtention != null ? fileExtention : "";
                        t.transferStatus = "Interrupted";
                        DatabaseManager.addTransfer(t);

                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            new App().setVisible(true);
                        });

                    } else {

                        DatabaseManager.updateTransfer(transferId, "Interrupted");
                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            new App().setVisible(true);
                        });

                    }

                    return;
                }
            }

            

        
            if (transferId.equals("000")) {

            // Insert New transfer completed record into history
            int serialNo = 0;
            List<jsonAttribute> all = DatabaseManager.readTransfers();

            if (all == null) {
                all = new ArrayList<>();
            }

            for (jsonAttribute tr : all) {
                try {
                    if (tr.si != null && !tr.si.isEmpty()) {
                        int current = Integer.parseInt(tr.si);
                        serialNo = Math.max(serialNo, current);
                    }
                } catch (Exception e) {
                }
            }

            jsonAttribute t = new jsonAttribute();

            t.si = String.valueOf(serialNo + 1);
            t.transferId = "T20230601" + String.format("%04d", serialNo + 1);
            t.fileName = sourceFileName != null ? sourceFileName : "";
            t.sourcePath = sourcePath != null ? sourcePath : "";
            t.destinationPath = destFolder != null ? destFolder : "";
            t.fileExtension = fileExtention != null ? fileExtention : "";
            t.transferStatus = "Completed";

            DatabaseManager.addTransfer(t);

            SwingUtilities.invokeLater(() -> {
                dispose();
                new App().setVisible(true);
            });

            System.out.println("Transfer complete!");
            clearProgress(logFile);
                
            }
            
            else
                {

                // Update existing transfer record to completed
                DatabaseManager.updateTransfer(transferId, "Completed");

                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new App().setVisible(true);
                });

                System.out.println("Transfer complete!");
                clearProgress(logFile);
            }


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


    // Utility to shorten long paths for display
    private String shorten(String text, int maxLength) {
    if (text.length() <= maxLength) return text;
    return text.substring(0, maxLength) + "...";
}


}
