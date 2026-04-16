import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DeviceDisconnectedMsg extends JFrame {

    private static final Color BG_COLOR       = new Color(0xB2EBF2);  
    private static final Color PANEL_BG       = new Color(0xB2EBF2);
    private static final Color CANCEL_COLOR   = new Color(0xEF5350);


    public DeviceDisconnectedMsg() {


        setTitle("Disconnected Device");
        setSize(500, 330);
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
        panel.setLayout(new GridLayout(2, 1, 0, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        ImageIcon icon1 = new ImageIcon(getClass().getResource("/assets/icon/disconnect_usb.png"));
        Image img1 = icon1.getImage();
        Image scaledImg1 = img1.getScaledInstance(90, 90, Image.SCALE_SMOOTH);

        JLabel iconLabel = new JLabel(new ImageIcon(scaledImg1));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel);

        JLabel statusLabel = new JLabel("Please reconnect the device and try again !");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel);

    



        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(PANEL_BG);
        bottomPanel.setLayout(new GridLayout(1, 1, 30, 50));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(30, 180, 30, 180));

        JButton cancelButton = new JButton("OK");
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

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DeviceDisconnectedMsg();
        });
    }
}