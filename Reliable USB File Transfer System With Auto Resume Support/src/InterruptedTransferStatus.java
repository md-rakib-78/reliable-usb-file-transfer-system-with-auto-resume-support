import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;

public class InterruptedTransferStatus extends JFrame {

    private static final File LOG_FILE = new File("connected_drives.txt");

    private static final Color BG_COLOR       = new Color(0xB2EBF2);  
    private static final Color PANEL_BG       = new Color(0xB2EBF2);
    private static final Color PAUSE_COLOR    = new Color(0x757575);
    private static final Color CANCEL_COLOR   = new Color(0xEF5350);

    private static int si;
    private static String sourceFileName;
    private static String sourcePath;
    private static String destFolder;
    private static String transferId;

    public InterruptedTransferStatus(int row,String sourceFileName, String sourcePath, String destFolder, String transferID) {

        InterruptedTransferStatus.si = row;
        InterruptedTransferStatus.sourceFileName = sourceFileName;
        InterruptedTransferStatus.sourcePath = sourcePath;
        InterruptedTransferStatus.destFolder = destFolder;
        InterruptedTransferStatus.transferId = transferID;

        setTitle("Interrupted Transfer Status");
        setSize(800, 550);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        setIconImage(scaledImg);

        setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);
        panel.setLayout(new GridLayout(2, 1, 5, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 20, 10, 20));

        ImageIcon icon1 = new ImageIcon(getClass().getResource("/assets/icon/error_icon.png"));
        Image img1 = icon1.getImage();
        Image scaledImg1 = img1.getScaledInstance(80, 80, Image.SCALE_SMOOTH);

        JLabel iconLabel = new JLabel(new ImageIcon(scaledImg1));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel);

        JLabel statusLabel = new JLabel("Transfer Interrupted !");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel);

        List<jsonAttribute> all = DatabaseManager.readTransfers();

        for (jsonAttribute tr : all) {

            if(tr.si.equals(String.valueOf(InterruptedTransferStatus.si))) {

                JLabel fileNameLabel = new JLabel("<html><b>File name: </b>" + shorten(tr.fileName, 70) + "</html>");
                fileNameLabel.setToolTipText(tr.fileName);
                fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                JLabel sourceLabel = new JLabel("<html><b>Source: </b>" + shorten(tr.sourcePath, 70) + "</html>");
                sourceLabel.setToolTipText(tr.sourcePath);
                sourceLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                JLabel destinationLabel = new JLabel("<html><b>Destination: </b>" + shorten(tr.destinationPath, 70) + "</html>");
                destinationLabel.setToolTipText(tr.destinationPath);
                destinationLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                JLabel fileSizeLabel = new JLabel("<html><b>File size: </b> " + tr.fileSize + "MB</html>");
                fileSizeLabel.setToolTipText(tr.fileSize);
                fileSizeLabel.setFont(new Font("Arial", Font.PLAIN, 14));

                JLabel transferPercentage = new JLabel("<html><b>Transfer Percentage: </b>" + tr.transferPercentage + "%</html>");
                transferPercentage.setToolTipText(tr.transferPercentage + "%");
                transferPercentage.setFont(new Font("Arial", Font.PLAIN, 14));

                JLabel interruptStatus = new JLabel("<html><b>Interrupt Count: </b>" + tr.interruptStatus + "</html>");
                interruptStatus.setToolTipText(tr.interruptStatus);
                interruptStatus.setFont(new Font("Arial", Font.PLAIN, 14));

                JPanel infoPanel = new JPanel();
                infoPanel.setBackground(PANEL_BG);
                infoPanel.setLayout(new GridLayout(10, 1, 5, 0));
                infoPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));

                infoPanel.add(fileNameLabel);
                infoPanel.add(sourceLabel);
                infoPanel.add(destinationLabel);
                infoPanel.add(fileSizeLabel);
                infoPanel.add(transferPercentage);
                infoPanel.add(interruptStatus);

                JPanel bottomPanel = new JPanel();
                bottomPanel.setBackground(PANEL_BG);
                bottomPanel.setLayout(new GridLayout(1, 2, 30, 50));
                bottomPanel.setBorder(BorderFactory.createEmptyBorder(20, 200, 50, 200));

                JButton resumeButton = new JButton("Resume Transfer");
                resumeButton.setFont(new Font("Arial", Font.BOLD, 14));
                resumeButton.setBackground(PAUSE_COLOR);
                resumeButton.setForeground(Color.WHITE);
                resumeButton.setFocusPainted(false);
                resumeButton.setToolTipText("Click to resume the interrupted transfer");
                resumeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                resumeButton.setOpaque(true);
                resumeButton.setContentAreaFilled(true);
                resumeButton.setBorderPainted(false); 

                resumeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {


                        List<jsonAttribute> all = DatabaseManager.readTransfers();
                        for (jsonAttribute tr : all) {
                            
                            if(tr.transferId.equals(InterruptedTransferStatus.transferId)) {
                                InterruptedTransferStatus.sourceFileName = tr.fileName;
                                InterruptedTransferStatus.sourcePath = tr.sourcePath;
                                InterruptedTransferStatus.destFolder = tr.destinationPath;
                                InterruptedTransferStatus.transferId = tr.transferId;
                                break;
                            }

                        }

                        List<String> driveLetters = getDriveLetters();

                        char first = InterruptedTransferStatus.destFolder.charAt(0);
                        String search = String.valueOf(first);

                        if (isDriveConnected(search)) {
                            System.out.println("Drive " + search + " is connected.");
                            dispose();
                            new ResumeFileTransfer(InterruptedTransferStatus.sourceFileName,
                                    InterruptedTransferStatus.sourcePath, InterruptedTransferStatus.destFolder,
                                    InterruptedTransferStatus.transferId);
                        } else {
                            
                            dispose();
                            SwingUtilities.invokeLater(() -> {
                                new RequestForReconnectDevice().setVisible(true);
                            });

                        }
                    }
                });
                bottomPanel.add(resumeButton);


                JButton cancelButton = new JButton("Cancel Transfer");
                cancelButton.setFont(new Font("Arial", Font.BOLD, 14));
                cancelButton.setBackground(CANCEL_COLOR);
                cancelButton.setForeground(Color.WHITE);
                cancelButton.setFocusPainted(false);
                cancelButton.setToolTipText("Click to cancel the interrupted transfer and return to main menu");
                cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
                cancelButton.setOpaque(true); // FIX
                cancelButton.setContentAreaFilled(true); // FIX
                cancelButton.setBorderPainted(false); // FIX

                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                        new App().setVisible(true);
                    }
                });
                bottomPanel.add(cancelButton);

                add(panel, BorderLayout.NORTH);
                add(infoPanel, BorderLayout.CENTER);
                add(bottomPanel, BorderLayout.SOUTH);
            }
        }

        setVisible(true);
    }




    private String shorten(String text, int maxLength) {
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + ".....";
    }

    public static List<String> getDriveLetters() {

        List<String> letters = new ArrayList<>();

        if (!LOG_FILE.exists() || LOG_FILE.length() == 0)
            return letters;

        try {
            List<String> lines = Files.readAllLines(LOG_FILE.toPath());

            for (String line : lines) {

                if (line != null && line.length() > 0) {
                    String letter = String.valueOf(line.charAt(0));
                    if (letter.matches("[A-Za-z]")) {
                        letters.add(letter.toUpperCase());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return letters;
    }

    // Returns true if the given drive letter exists in connected_drives.txt
    public static boolean isDriveConnected(String letter) {

        if (letter == null || letter.trim().isEmpty())
            return false;

        return getDriveLetters().contains(letter.toUpperCase());
    }



}