import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CompleteTransferStatus extends JFrame {

    // ── colour palette  
    private static final Color BG_COLOR       = new Color(0xB2EBF2);  
    private static final Color PANEL_BG       = new Color(0xB2EBF2);

    private static int si;

    public CompleteTransferStatus(int row) {

        CompleteTransferStatus.si = row;
        setTitle("Complete Transfer Status");
        setSize(800, 550);
        setResizable(false);
       setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       
        //setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        // addWindowListener(new java.awt.event.WindowAdapter() {
        //     @Override
        //     public void windowClosing(java.awt.event.WindowEvent e) {
        //       
        //         new App().setVisible(true);
        //      
        //         dispose();
        //     }
        // });

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

        ImageIcon icon1 = new ImageIcon(getClass().getResource("/assets/icon/done_icon.png"));
        Image img1 = icon1.getImage();
        Image scaledImg1 = img1.getScaledInstance(80, 80, Image.SCALE_SMOOTH);

        JLabel iconLabel = new JLabel(new ImageIcon(scaledImg1));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel);

        JLabel statusLabel = new JLabel("Transfer Completed Successfully!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel);

        //Read Data From database
        List<jsonAttribute> all = DatabaseManager.readTransfers();

        for (jsonAttribute tr : all) {

            if(tr.si.equals(String.valueOf(CompleteTransferStatus.si))) {

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

                add(panel, BorderLayout.NORTH);
                add(infoPanel, BorderLayout.CENTER);

            }
        }


        setVisible(true);
    }

    // Utility to shorten long paths for display
    private String shorten(String text, int maxLength) {
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + ".....";
    }

    
}
