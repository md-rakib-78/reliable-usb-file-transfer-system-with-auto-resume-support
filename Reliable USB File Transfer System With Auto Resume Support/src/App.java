import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class App extends JFrame {

    public App()
    {
        setUndecorated(true);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int sW = screenSize.width;
        int sH = screenSize.height;

        // Top Panel 
        JPanel toPanel = new JPanel();
        toPanel.setBounds(0, 0, sW, sH/4);
        toPanel.setBackground(Color.decode("#baf2fd"));
        add(toPanel);

        //Close Button
        JButton close = new JButton("X");
        close.setBounds(sW-70, 10, 60, 30);
        close.setBackground(Color.BLACK);
        close.setForeground(Color.white);
        close.setFocusPainted(false);
        toPanel.add(close);
        
        getContentPane().setBackground(Color.WHITE);
        setSize(sW,sH);
        setLayout(null);
        setVisible(true);  
    }


    public static void main(String[] args) throws Exception {
        new App();
    }
}
