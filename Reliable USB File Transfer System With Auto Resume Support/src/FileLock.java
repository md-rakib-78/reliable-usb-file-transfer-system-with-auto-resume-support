import javax.crypto.*;
import javax.crypto.spec.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.security.*;
import java.util.Arrays;


public class FileLock extends JFrame {

    static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    static final String KEY_ALGORITHM  = "AES";
    static final String KEY_FACTORY    = "PBKDF2WithHmacSHA256";
    static final int    ITERATIONS     = 65536;
    static final int    KEY_LENGTH     = 256;
    static final int    SALT_SIZE      = 16;
    static final int    IV_SIZE        = 16;
    static final String LOCK_EXT       = ".locked";
    static final byte[] MAGIC          = {'F', 'L', 'C', 'K'};


    private JTextField     pathField;
    private JPasswordField pwField, confirmField;
    private JLabel         statusLabel;
    private JButton        lockBtn;

    private static final Color BG_COLOR     = new Color(0xB2EBF2);
    private static final Color BTN_INACTIVE = new Color(100, 110, 115);
    private static final Color CANCEL_COLOR   = new Color(0xEF5350);


    public FileLock() {
        super("File Lock");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 600);
        setMinimumSize(new Dimension(540, 540));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG_COLOR);

        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        setIconImage(scaledImg);

        buildUI();
    }


    private void buildUI() {
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(buildHeader(),  BorderLayout.NORTH);
        getContentPane().add(buildCenter(),  BorderLayout.CENTER);
        getContentPane().add(buildWarning(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG_COLOR);
        p.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Keep Safe Your Important File");
        title.setFont(new Font("Segoe UI", Font.BOLD, 21));
        title.setForeground(BTN_INACTIVE);

        JLabel sub = new JLabel("Encrypt your files with AES-256");
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

        JPanel pathPanel = new JPanel(new BorderLayout(8, 0));
        pathPanel.setBackground(Color.WHITE);
        pathPanel.setBorder(sectionBorder("Select File to Lock"));
        pathField = new JTextField();
        pathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        pathField.setPreferredSize(new Dimension(0, 34));
        JButton browseBtn = actionButton("Browse…", new Color(100, 200, 210));
        browseBtn.addActionListener(e -> browse());
        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(browseBtn, BorderLayout.EAST);

        JPanel pwPanel = new JPanel(new GridLayout(2, 2, 10, 8));
        pwPanel.setBackground(Color.WHITE);
        pwPanel.setBorder(sectionBorder("Set Password"));
        pwField      = new JPasswordField();
        confirmField = new JPasswordField();
        styleField(pwField);
        styleField(confirmField);
        pwPanel.add(lbl("Password:"));
        pwPanel.add(pwField);
        pwPanel.add(lbl("Confirm:"));
        pwPanel.add(confirmField);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));

        lockBtn = actionButton("LOCK NOW", CANCEL_COLOR);
        lockBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lockBtn.setPreferredSize(new Dimension(80, 40));
        lockBtn.addActionListener(e -> performLock());

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
        addFull(top, lockBtn);
        top.add(Box.createVerticalStrut(10));

        main.add(top, BorderLayout.NORTH);
        return main;
    }


    private JPanel buildWarning() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(255, 250, 225));
        p.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(220, 175, 50)),
                new EmptyBorder(8, 14, 8, 14)));
        JLabel w = new JLabel("<html><b>Warning:</b> Remember your password! "
                + "Encrypted files <u>cannot</u> be recovered without the correct password.</html>");
        w.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(w);
        return p;
    }


private void browse() {
    FileDialog fd = new FileDialog(this, "Select File", FileDialog.LOAD);

    ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
    Image img = icon.getImage();
    Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
    setIconImage(scaledImg);

    fd.setDirectory(desktopDir().getAbsolutePath());

    fd.setVisible(true);

    String file = fd.getFile();
    String dir  = fd.getDirectory();

    if (file != null && dir != null) {
        File selected = new File(dir, file);
        pathField.setText(selected.getAbsolutePath());
    }
}
    

    private void performLock() {
        String path    = pathField.getText().trim();
        char[] pw      = pwField.getPassword();
        char[] confirm = confirmField.getPassword();

        if (path.isEmpty())               { status("Select a file.", Color.RED); return; }
        if (pw.length < 4)               { status("Password must be at least 4 characters.", Color.RED); return; }

       
        String password = new String(pw);

        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c))
                hasUpper = true;
            if (Character.isLowerCase(c))
                hasLower = true;
            if (Character.isDigit(c)) 
                hasDigit = true;
            
        }

        if (!hasDigit) {
            status("Password must contain at least one digit (0-9)!", Color.RED);
            return;
        }

        if (!hasUpper) {
            status("Password must contain at least one uppercase letter (A-Z)!", Color.RED);
            return;
        }
        if (!hasLower) {
            status("Password must contain at least one lowercase letter (a-z)!", Color.RED);
            return;
        }
        if (!password.matches(".*[^a-zA-Z0-9].*")) {
            status("Password must contain at least one special character (!@#$%&?)!", Color.RED);
            return;
        }

        if (!Arrays.equals(pw, confirm)) { status("Passwords do not match!", Color.RED); return; }

        File target = new File(path);
        if (!target.exists() || !target.isFile()) { status("File does not exist.", Color.RED); return; }

        lockBtn.setEnabled(false);
        status("Working…", new Color(40, 100, 200));

        char[] pwCopy = Arrays.copyOf(pw, pw.length);
        new SwingWorker<Boolean, Void>() {
            String err;

            protected Boolean doInBackground() {
                try {
                    return lockFileInternal(target, pwCopy);
                } catch (Exception ex) { err = ex.getMessage(); return false; }
            }

            protected void done() {
                Arrays.fill(pwCopy, '\0');
                lockBtn.setEnabled(true);
                if (err != null) {
                    status("X" + err, Color.RED);
                } else {
                    try {
                        if (get()) {
                            status("File locked successfully!", new Color(30, 130, 30));
                            pathField.setText("");
                            pwField.setText("");
                            confirmField.setText("");

                            try {
                                Thread.sleep(3000);
                                dispose();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception ignored) {}
                }
            }
        }.execute();
    }

    void status(String msg, Color c) {
        statusLabel.setText(msg);
        statusLabel.setForeground(c);
    }



// Lock logic here
    private boolean lockFileInternal(File file, char[] pw) throws Exception {
        if (file.getName().endsWith(LOCK_EXT))
            throw new IllegalStateException("Already locked: " + file.getName());
        File out = new File(file.getParent(), file.getName() + LOCK_EXT);
        encryptFile(file, out, pw);
        file.delete();
        return true;
    }



  // Crypto uses
    static void encryptFile(File input, File output, char[] pw) throws Exception {
        byte[]    salt   = randomBytes(SALT_SIZE);
        byte[]    iv     = randomBytes(IV_SIZE);
        SecretKey key    = deriveKey(pw, salt);
        Cipher    cipher = Cipher.getInstance(TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        try (FileInputStream  fis = new FileInputStream(input);
             FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(MAGIC);
            fos.write(salt);
            fos.write(iv);
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) {
                byte[] enc = cipher.update(buf, 0, n);
                if (enc != null) fos.write(enc);
            }
            byte[] fin = cipher.doFinal();
            if (fin != null) fos.write(fin);
        }
    }

    static SecretKey deriveKey(char[] pw, byte[] salt) throws Exception {
        SecretKeyFactory f    = SecretKeyFactory.getInstance(KEY_FACTORY);
        PBEKeySpec       spec = new PBEKeySpec(pw, salt, ITERATIONS, KEY_LENGTH);
        byte[]           kb   = f.generateSecret(spec).getEncoded();
        spec.clearPassword();
        return new SecretKeySpec(kb, KEY_ALGORITHM);
    }

    static byte[] randomBytes(int n) {
        byte[] b = new byte[n];
        new SecureRandom().nextBytes(b);
        return b;
    }



// Swing Helpers
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
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFocusPainted(false); b.setBorderPainted(false); b.setOpaque(true);
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
        c.setAlignmentX(LEFT_ALIGNMENT);
        p.add(c);
    }

    private static File desktopDir() {
        return new File(System.getProperty("user.home"), "Desktop");
    }

}