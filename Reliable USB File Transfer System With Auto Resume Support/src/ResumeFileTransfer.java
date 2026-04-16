import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import java.awt.*;



public class ResumeFileTransfer extends ProgressBar {

    private static final int CHUNK_SIZE = 8 * 1024 * 1024;

    public String sourceFileName, sourcePath, destFolder,fileExtention;
    public String transferId="000";
    public int interruptStatus = 0; 

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

                // please device disconnected during transfer, show message and return to main menu
                dispose();
                SwingUtilities.invokeLater(() -> {
                    new DeviceDisconnectedMsg().setVisible(true);
                });
                
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
   
        long resumeFromLog = 0;

        if (!logFile.exists() && !transferId.equals("000")) {

            //Log file missing 
            int result = showLogMissingDialog();

            if (result == JOptionPane.YES_OPTION) {
                resumeFromLog = 0;
            } else {
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    new App().setVisible(true);
                });
                return;
            }

        } else {

            try {
                resumeFromLog = loadProgress(logFile);

            } catch (IOException e) {

                // Log corrupted
                int result = showCorruptedLogDialog();

                if (result == JOptionPane.YES_OPTION) {
                    resumeFromLog = 0;
                } else {
                    SwingUtilities.invokeLater(() -> {
                        dispose();
                        new App().setVisible(true);
                    });
                    return;
                }
            }
        }


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

            String transferPercentage ="0";

            while ((bytesRead = src.read(buffer)) != -1) {
                try {
                    dst.write(buffer, 0, bytesRead);
                    dst.getFD().sync();

                    totalCopied += bytesRead;
                    saveProgress(totalCopied, logFile);

                    double percent = (totalCopied * 100.0) / fileSize;

                    transferPercentage = String.format("%.2f", percent);

                    SwingUtilities.invokeLater(() -> {
                        progressBar.setValue((int) percent);
                        percentLabel.setText("<html><b>Progress:</b> " + String.format("%.2f%%", percent) + "</html>");
                    });

         
                    System.out.printf("Progress: %.2f%%%n", percent);

                } catch (IOException e) {

                    System.out.println("\nUSB disconnected or write error!");
                    System.out.println("Progress saved at byte: " + totalCopied);

                  

                    // new transfer interrupted, insert record into history----------------------------------------
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
                        t.fileSize = String.valueOf(fileSizeMB) != null ? String.valueOf(fileSizeMB) : "";
                        t.interruptStatus= String.valueOf(interruptStatus+1);
                        t.transferStatus = "Interrupted";
                        t.transferPercentage = transferPercentage != null ? transferPercentage : "0";
                        DatabaseManager.addTransfer(t);

                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            new App().setVisible(true);
                        });

                    }
                    
                    // Existing transfer interrupted, update history ----------------------------------------
                    else 
                        {

                        List<jsonAttribute> all = DatabaseManager.readTransfers();
           
                        for (jsonAttribute tr : all) {
                            
                            if(transferId.equals(tr.transferId)) {
                                try {
                                    interruptStatus = Math.max(interruptStatus, Integer.parseInt(tr.interruptStatus));
                                } catch (Exception a) {
                                }
                            }
                        }

                        System.out.println("Updating existing transfer record to Interrupted with interruptStatus: " + (interruptStatus+1));

                        DatabaseManager.updateTransfer(transferId, "Interrupted", String.valueOf(interruptStatus+1), transferPercentage);
                        SwingUtilities.invokeLater(() -> {
                            dispose();
                            new App().setVisible(true);
                        });

                    }

                    return;
                }
            }






            // New transfer completed successfully, update history-----------------------------------------------
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
            t.fileSize = String.valueOf(fileSizeMB) != null ? String.valueOf(fileSizeMB) : "";
            t.interruptStatus= String.valueOf(interruptStatus);
            t.transferStatus = "Completed";

            DatabaseManager.addTransfer(t);

            SwingUtilities.invokeLater(() -> {
                dispose();
                new App().setVisible(true);
            });

            System.out.println("Transfer complete!");
            clearProgress(logFile);
                
            }
            
            // Existing transfer resumed and completed, update history ----------------------------------------------
            else
                {

                    List<jsonAttribute> all = DatabaseManager.readTransfers();

                    for (jsonAttribute tr : all) {

                        if (transferId.equals(tr.transferId)) {
                            try {
                                interruptStatus = Math.max(interruptStatus, Integer.parseInt(tr.interruptStatus));
                            } catch (Exception a) {
                            }
                        }
                    }


                // Update existing transfer record to completed
                DatabaseManager.updateTransfer(transferId, "Completed", String.valueOf(interruptStatus), transferPercentage);

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

                // If log file is corrupted, delete it and start from 0
                logFile.delete();
                throw new IOException("Corrupted log file");

            }
        }
        return 0;
    }



    private int showLogMissingDialog() {

        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);

        JLabel label = new JLabel(
                "<html><center><b>Log file not found! Transfer will restart.<br>Continue?</b></center></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);

        JOptionPane optionPane = new JOptionPane(label, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION);
        optionPane.setPreferredSize(new Dimension(450, 150));
           label.setFont(new Font("Arial", Font.PLAIN, 14));

        JDialog dialog = optionPane.createDialog(this, "Log Missing");
        dialog.setIconImage(scaledImg);
        dialog.setVisible(true);

        return (int) optionPane.getValue();
    }




    private int showCorruptedLogDialog() {
        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);

        JLabel label = new JLabel("<html><center><b>Log file corrupted! <br>Do you want to restart transfer?</b></center></html>");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 14));

        JOptionPane optionPane = new JOptionPane(label, JOptionPane.PLAIN_MESSAGE, JOptionPane.YES_NO_OPTION);
        optionPane.setPreferredSize(new Dimension(450, 150));

        JDialog dialog = optionPane.createDialog(this, "Confirm");
        dialog.setIconImage(scaledImg);
        dialog.setVisible(true);

        return (int) optionPane.getValue();
    }








    private static void clearProgress(File logFile) {
        if (logFile.exists()) {
            logFile.delete();
        }
    }


    // Utility to shorten long paths for display
    private String shorten(String text, int maxLength) {
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
    }


}
