import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ProgressBar extends JFrame implements ActionListener {


    // ── colour palette  
    private static final Color BG_COLOR       = new Color(0xB2EBF2);  
    private static final Color PANEL_BG       = new Color(0xB2EBF2);
    private static final Color PROGRESS_FILL  = new Color(0x76E000);  
    private static final Color PROGRESS_TRACK = new Color(0xE8E8E8);
    private static final Color PAUSE_COLOR    = new Color(0x757575);
    private static final Color CANCEL_COLOR   = new Color(0xEF5350);


    public JLabel fileNameLabel, sourceLabel, destinationLabel;
    public JLabel fileSizeLabel, transferredLabel, timeLabel, resumeLabel, percentLabel;
    public JProgressBar progressBar;
    public JButton pauseBtn, cancelBtn;



    public ProgressBar() {

        setTitle("Transfer Progress View");
        setSize(600, 400);
        //setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);

        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        setIconImage(scaledImg);

        setLayout(new BorderLayout());


        // Center Panel
        JPanel panel = new JPanel();
        panel.setBackground(PANEL_BG);
        panel.setLayout(new GridLayout(9, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        fileNameLabel = new JLabel("File name: ");
        fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        sourceLabel = new JLabel("Source: ");
        sourceLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        destinationLabel = new JLabel("Destination: ");
        destinationLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        fileSizeLabel = new JLabel("File size: ");
        fileSizeLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        resumeLabel = new JLabel("Resuming from byte: ");
        resumeLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        percentLabel = new JLabel("Progress: ");
        percentLabel.setFont(new Font("Arial", Font.PLAIN, 13));

        panel.add(fileNameLabel);
        panel.add(sourceLabel);
        panel.add(destinationLabel);
        panel.add(fileSizeLabel);
        panel.add(resumeLabel);
        panel.add(percentLabel);

        add(panel, BorderLayout.NORTH);

        // Progress Bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setFont(new Font("Arial", Font.BOLD, 14));
        progressBar.setBorderPainted(false);
        progressBar.setBackground(PROGRESS_TRACK);
        progressBar.setForeground(PROGRESS_FILL);
        progressBar.setPreferredSize(new Dimension(550, 30));

        JPanel progressPanel = new JPanel();
        progressPanel.setBackground(PANEL_BG);
        progressPanel.add(progressBar);
        add(progressPanel, BorderLayout.CENTER);

        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 40));
        buttonPanel.setBackground(PANEL_BG);

        pauseBtn = new JButton("PAUSE");
        pauseBtn.setFont(new Font("Arial", Font.BOLD, 14));
        pauseBtn.setBackground(PAUSE_COLOR);
        pauseBtn.setBorderPainted(false);
        pauseBtn.setFocusPainted(false);
        pauseBtn.setForeground(Color.WHITE);
        pauseBtn.addActionListener(this);
        
        cancelBtn = new JButton("CANCEL");
        cancelBtn.setFont(new Font("Arial", Font.BOLD, 14));
        cancelBtn.setBackground(CANCEL_COLOR);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setBorderPainted(false);
        cancelBtn.setForeground(Color.WHITE);
        cancelBtn.addActionListener(this);  

        buttonPanel.add(pauseBtn);
        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.PAGE_END);


        setVisible(true);
    }



    public void actionPerformed(ActionEvent e) {
        
        if(e.getSource() == pauseBtn) {
            System.out.println("PAUSE button clicked");
        } else if(e.getSource() == cancelBtn) {
            System.out.println("CANCEL button clicked");
        }

    }



    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProgressBar::new);   
    }


}