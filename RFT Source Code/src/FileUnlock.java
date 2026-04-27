import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.util.Arrays;

public class FileUnlock extends JFrame {

    private JTextField pathField;
    private JPasswordField pwField;
    private JLabel statusLabel;
    private JButton unlockBtn;

    private static final Color BG_COLOR = new Color(0xB2EBF2);
        private static final Color PROGRESS_FILL  = new Color(0x76E000); 
    private static final Color BTN_INACTIVE = new Color(100, 110, 115);

    public FileUnlock() {
        super("File Unlock");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // IMPORTANT
        setSize(600, 550);
        setMinimumSize(new Dimension(540, 500));

        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        setIconImage(scaledImg);

        setLocationRelativeTo(null);

        getContentPane().setBackground(BG_COLOR);
        buildUI();
    }

    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildHeader(), BorderLayout.NORTH);
        getContentPane().add(buildCenter(), BorderLayout.CENTER);
        getContentPane().add(buildInfo(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Unlock Your Locked File");
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setForeground(BTN_INACTIVE);

        JLabel sub = new JLabel("Decrypt your AES-256 protected file");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(160, 160, 210));

        JPanel box = new JPanel(new GridLayout(2, 1, 0, 3));
        box.setOpaque(false);
        box.add(title);
        box.add(sub);

        p.add(box, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildCenter() {
        JPanel main = new JPanel(new BorderLayout(0, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(new EmptyBorder(16, 20, 10, 20));

        // File select
        JPanel pathPanel = new JPanel(new BorderLayout(8, 0));
        pathPanel.setBackground(Color.WHITE);
        pathPanel.setBorder(sectionBorder("Select Locked File"));

        pathField = new JTextField();
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pathField.setPreferredSize(new Dimension(0, 34));

        JButton browseBtn = actionButton("Browse…", new Color(100, 200, 210));
        browseBtn.addActionListener(e -> browse());

        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(browseBtn, BorderLayout.EAST);

        // Password
        JPanel pwPanel = new JPanel(new GridLayout(1, 2, 10, 8));
        pwPanel.setBackground(Color.WHITE);
        pwPanel.setBorder(sectionBorder("Enter Password"));

        pwField = new JPasswordField();
        styleField(pwField);

        pwPanel.add(lbl("Password:"));
        pwPanel.add(pwField);

        // Status
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Button
        unlockBtn = actionButton("UNLOCK NOW",PROGRESS_FILL);
        unlockBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        unlockBtn.setPreferredSize(new Dimension(80, 40));
        unlockBtn.addActionListener(e -> performUnlock());

        // Layout
        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setBackground(Color.WHITE);

        top.add(Box.createVerticalStrut(10));
        addFull(top, pathPanel);
        top.add(Box.createVerticalStrut(10));
        addFull(top, pwPanel);
        top.add(Box.createVerticalStrut(12));
        addFull(top, statusLabel);
        top.add(Box.createVerticalStrut(6));
        addFull(top, unlockBtn);
        top.add(Box.createVerticalStrut(10));

        main.add(top, BorderLayout.NORTH);
        return main;
    }

    private JPanel buildInfo() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(235, 245, 255));
        p.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(100, 160, 220)),
                new EmptyBorder(8, 14, 8, 14)));

        JLabel info = new JLabel("<html><b>Info:</b> Select a <b>.locked</b> file to unlock.</html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(info);

        return p;
    }

    // ───────────────── ACTION ─────────────────
    private void browse() {
        FileDialog fd = new FileDialog(this, "Select Locked File", FileDialog.LOAD);
        fd.setDirectory(desktopDir().getAbsolutePath());
        fd.setFile("*.locked"); // filter (works mainly on Windows)

        fd.setVisible(true);

        if (fd.getFile() != null) {
            File file = new File(fd.getDirectory(), fd.getFile());

            if (!file.getName().endsWith(".locked")) {
                status("Select a .locked file only!", Color.RED);
                return;
            }

            pathField.setText(file.getAbsolutePath());
        }
    }

    private void performUnlock() {
        String path = pathField.getText().trim();
        char[] pw = pwField.getPassword();

        if (path.isEmpty()) {
            status("Select a file.", Color.RED);
            return;
        }

        if (pw.length == 0) {
            status("Enter password.", Color.RED);
            return;
        }

        File file = new File(path);

        if (!file.exists() || !file.getName().endsWith(FileLock.LOCK_EXT)) {
            status("Invalid .locked file.", Color.RED);
            return;
        }

        unlockBtn.setEnabled(false);
        status("Working…", new Color(40, 100, 200));

        char[] pwCopy = Arrays.copyOf(pw, pw.length);

        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() {
                try {
                    return unlockFileInternal(file, pwCopy);
                } catch (Exception e) {
                    return false;
                }
            }

            protected void done() {
                Arrays.fill(pwCopy, '\0');
                unlockBtn.setEnabled(true);

                try {
                    if (get()) {
                        status("File unlocked successfully!", new Color(30, 130, 30));
                        pathField.setText("");
                        pwField.setText("");

                    } else {
                        status("Wrong password!", Color.RED);
                    }
                } catch (Exception e) {
                    status("Error occurred.", Color.RED);
                }
            }
        }.execute();
    }

    private boolean unlockFileInternal(File lockedFile, char[] pw) throws Exception {
        String originalName = lockedFile.getName().replace(FileLock.LOCK_EXT, "");
        File output = new File(lockedFile.getParent(), originalName);

        boolean ok = decryptFile(lockedFile, output, pw);

        if (ok)
            lockedFile.delete();
        else
            output.delete();

        return ok;
    }


    private static boolean decryptFile(File input, File output, char[] pw) throws Exception {
        try (FileInputStream fis = new FileInputStream(input)) {

            byte[] magic = new byte[FileLock.MAGIC.length];
            if (fis.read(magic) != magic.length || !Arrays.equals(magic, FileLock.MAGIC))
                throw new IllegalArgumentException("Invalid file format");

            byte[] salt = new byte[FileLock.SALT_SIZE];
            byte[] iv = new byte[FileLock.IV_SIZE];

            fis.read(salt);
            fis.read(iv);

            SecretKey key = FileLock.deriveKey(pw, salt);
            Cipher cipher = Cipher.getInstance(FileLock.TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

            try (FileOutputStream fos = new FileOutputStream(output)) {
                byte[] buffer = new byte[8192];
                int n;

                while ((n = fis.read(buffer)) != -1) {
                    byte[] dec = cipher.update(buffer, 0, n);
                    if (dec != null)
                        fos.write(dec);
                }

                byte[] fin = cipher.doFinal();
                if (fin != null)
                    fos.write(fin);
            }

            return true;

        } catch (BadPaddingException | IllegalBlockSizeException e) {
            return false;
        }
    }

  
    private void status(String msg, Color c) {
        statusLabel.setText(msg);
        statusLabel.setForeground(c);
    }

    private static JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private static void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 32));
    }

    private static JButton actionButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return b;
    }

    private static Border sectionBorder(String title) {
        TitledBorder tb = BorderFactory.createTitledBorder(
                new LineBorder(new Color(200, 200, 220), 1, true), title);
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 11));
        tb.setTitleColor(new Color(80, 80, 130));
        return new CompoundBorder(tb, new EmptyBorder(4, 8, 6, 8));
    }

    private static void addFull(JPanel p, JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(c);
    }

    private static File desktopDir() {
        return new File(System.getProperty("user.home"), "Desktop");
    }

    // ADD THIS METHOD — called from App.main() when launched via right-click
    public void setFilePath(String path) {
        SwingUtilities.invokeLater(() -> pathField.setText(path));
    }

}