import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class App extends JFrame {

    // ── Colors (match your teal/mint theme) ──────────────────────────────────
    private static final Color BG_HEADER    = new Color(160, 220, 220); // teal header
    private static final Color BG_MAIN      = new Color(240, 248, 248); // light body
    private static final Color BTN_ACTIVE   = new Color(255, 255, 255); // white active tab
    private static final Color BTN_INACTIVE = new Color(100, 110, 115); // dark inactive tab
    private static final Color STATUS_OK    = new Color(30,  160,  60); // green Complete
    private static final Color STATUS_ERR   = new Color(210,  40,  40); // red Interrupt
    private static final Color CARD_BG      = Color.WHITE;

    transferAttribute transferAttr = new transferAttribute();

    public App() {
        setTitle("Reliable File Transfer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_MAIN);

        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        setIconImage(scaledImg);

        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildSrcDestPanel(), BorderLayout.CENTER); // wrapped together below
        setVisible(true);
    }



    // ─────────────────────────────────────────────────────────────────────────
    //  TOP BAR  (logo + nav tabs + window controls)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_HEADER);
        bar.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        // ── Logo area (left) ───────────────────────────────────────────────
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        logoPanel.setOpaque(false);

        // Logo icon fetch
        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/img/logo_icon.png"));
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(img);
        JLabel logoIcon = new JLabel(resizedIcon);

        JLabel logoText = new JLabel("RELIABLE FILE TRANSFER");
        logoText.setFont(new Font("SansSerif", Font.BOLD, 25));
        logoText.setForeground(new Color(50, 50, 90));

        logoPanel.add(logoIcon);
        logoPanel.add(logoText);

        // ── Nav tabs (center) ────────────────────────────────────────────────
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        navPanel.setOpaque(false);

        String[] tabs = {"FILE TRANSFER", "CONVERT .ZIP", "EXTRACT ZIP", ".LOG FILE"};

        for (int i = 0; i < tabs.length; i++) {
            JButton btn = new JButton(tabs[i]);
            btn.setFont(new Font("SansSerif", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(130, 34));
            if (i == 0) { // active tab
                btn.setBackground(BTN_ACTIVE);
                btn.setForeground(new Color(50, 50, 90));
            } else {
                btn.setBackground(BTN_INACTIVE);
                btn.setForeground(Color.WHITE);
            }
            final int idx = i;
            btn.addActionListener(e -> onTabSelected(idx, tabs[idx]));
            navPanel.add(btn);
        }


        bar.add(logoPanel, BorderLayout.WEST);
        bar.add(navPanel,  BorderLayout.EAST);
        return bar;
    }



    // ─────────────────────────────────────────────────────────────────────────
    //  CENTER  (SRC/DEST hero card + history table)
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildSrcDestPanel() {
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.setBackground(BG_MAIN);
        wrapper.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        wrapper.add(buildHeroCard());
        wrapper.add(Box.createVerticalStrut(20));
        wrapper.add(buildHistoryTable());

        return wrapper;
    }

    // ── Hero card: SRC button, DEST button, USB illustration ─────────────────
    private JPanel buildHeroCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 220), 1),
            BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // Left: SRC + DEST circular buttons
        JPanel btnArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 24, 0));
        btnArea.setOpaque(false);

        btnArea.add(buildFolderCircleBtn("SRC.", new Color(100, 200, 210), "src"));
        btnArea.add(buildFolderCircleBtn("DEST.", new Color(200, 230, 200), "dest"));

        // Right: USB image placeholder
        JLabel usbImg = new JLabel();
        usbImg.setPreferredSize(new Dimension(620, 120));
        usbImg.setHorizontalAlignment(SwingConstants.RIGHT);
        usbImg.setIcon(new ImageIcon(getClass().getResource("/assets/img/wallpaper.jpg")));

        card.add(btnArea, BorderLayout.WEST);
        card.add(usbImg,  BorderLayout.EAST);
        return card;
    }

    
     //Circular folder button used for SRC and DEST.

    private JPanel buildFolderCircleBtn(String label, Color bgColor, String actionId) {
        JPanel circle = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        circle.setPreferredSize(new Dimension(110, 110));
        circle.setOpaque(false);
        circle.setLayout(new GridBagLayout());
        circle.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        // Folder icon label
        JLabel folderIcon = new JLabel();
        folderIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
    
        // Logo icon fetch
        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/icon/folder_icon.png"));
        Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(img);
      

        folderIcon.setIcon(resizedIcon);
        folderIcon.setFont(new Font("Serif", Font.PLAIN, 28));

        JLabel txt = new JLabel(label);
        txt.setAlignmentX(Component.CENTER_ALIGNMENT);
        txt.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 13));
        txt.setForeground(Color.WHITE);

        inner.add(folderIcon);
        inner.add(txt);
        circle.add(inner);

        // Click handler
        circle.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onFolderButtonClicked(actionId); }
            @Override public void mouseEntered(MouseEvent e) { circle.setBackground(bgColor.darker()); }
            @Override public void mouseExited (MouseEvent e) { circle.setBackground(bgColor); }
        });

        return circle;
    }

    // ── History table ─────────────────────────────────────────────────────────
    private JScrollPane buildHistoryTable() {
        String[] cols = {"", "Si.", "File name", "Source Path", "Destination Path", "File Type", "Transfer Status",""};

        //file icon fetch
        ImageIcon icon = new ImageIcon(getClass().getResource("/assets/icon/file_icon.png"));
        Image img = icon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(img);

        //success icon fetch
        ImageIcon sucIcon = new ImageIcon(getClass().getResource("/assets/icon/done_icon.png"));
        Image sucImg = sucIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon resizedSucIcon = new ImageIcon(sucImg);

        //error icon fetch
        ImageIcon errIcon = new ImageIcon(getClass().getResource("/assets/icon/error_icon.png"));
        Image errImg = errIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
        ImageIcon resizedErrIcon = new ImageIcon(errImg);



        List<jsonAttribute> all = DatabaseManager.readTransfers();

        if (all == null) {
            all = new ArrayList<>();
        }

        List<Object[]> dataList = new ArrayList<>();

        for (jsonAttribute tr : all) {

            if (tr.transferStatus.equalsIgnoreCase("Completed")) {

                dataList.add(new Object[] {
                        resizedIcon,
                        safe(shorten(tr.si, 10)),
                        safe(shorten(tr.fileName, 25)),
                        safe(shorten(tr.sourcePath, 30)),
                        safe(shorten(tr.destinationPath, 30)),
                        safe(shorten("." + tr.fileExtension, 10)),
                        safe(shorten(tr.transferStatus, 15)),
                        resizedSucIcon
                });

            } else {

                dataList.add(new Object[] {
                        resizedIcon,
                        safe(shorten(tr.si, 10)),
                        safe(shorten(tr.fileName, 25)),
                        safe(shorten(tr.sourcePath, 30)),
                        safe(shorten(tr.destinationPath, 30)),
                        safe(shorten("." + tr.fileExtension, 10)),
                        safe(shorten(tr.transferStatus, 15)),
                        resizedErrIcon
                });

            }
        }


        Object[][] data = dataList.isEmpty()
                ? new Object[0][cols.length]
                : dataList.toArray(new Object[0][]);

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public Class<?> getColumnClass(int column) {
                return column == 0 || column == 7 ? ImageIcon.class : String.class;
            }
        };



        JTable table = new JTable(model);
        table.setRowHeight(36);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(220, 240, 245));

        // Column widths
        int[] widths = {40, 50, 180, 200, 200, 80, 140, 40};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // ── Transfer Status column: render as a clickable button ─────────────
        table.getColumnModel().getColumn(6).setCellRenderer(new StatusButtonRenderer());
        table.getColumnModel().getColumn(6).setCellEditor(new StatusButtonEditor(new JCheckBox()));

        // Row click (outside the button cell) — optional general row handler
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col != 6 && row >= 0) onRowClicked(row, model);
            }
        });

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 220)),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        JLabel title = new JLabel("  HISTORY");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scroll, BorderLayout.CENTER);

        // Wrap in scroll pane for the outer layout
        JScrollPane outer = new JScrollPane(panel);
        outer.setBorder(BorderFactory.createEmptyBorder());
        return outer;
    }


    private String safe(String value) {
    return value == null ? "" : value;
}

    // ─────────────────────────────────────────────────────────────────────────
    //  Transfer Status — Button Renderer
    //  Renders each status cell as a colored button label
    // ─────────────────────────────────────────────────────────────────────────
    static class StatusButtonRenderer extends JButton implements TableCellRenderer {
        StatusButtonRenderer() {
            setOpaque(true);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new Font("SansSerif", Font.BOLD, 12));
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            String status = value == null ? "" : value.toString();
            setText(status);
            if ("Completed".equalsIgnoreCase(status)) {
                setForeground(new Color(30, 160, 60));
                setBackground(new Color(220, 245, 225));
            } else {
                setForeground(new Color(210, 40, 40));
                setBackground(new Color(250, 220, 220));
            }
            return this;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Transfer Status — Button Editor
    //  Makes the button clickable; fires onStatusButtonClicked()
    // ─────────────────────────────────────────────────────────────────────────
    class StatusButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String  currentStatus;
        private int     currentRow;

        StatusButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFont(new Font("SansSerif", Font.BOLD, 12));
            button.addActionListener(e -> {fireEditingStopped();
            onStatusButtonClicked(currentRow, currentStatus);
            });
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            currentStatus = value == null ? "" : value.toString();
            currentRow    = row;
            button.setText(currentStatus);
            if ("Completed".equalsIgnoreCase(currentStatus)) {
                button.setForeground(new Color(30, 160, 60));
                button.setBackground(new Color(220, 245, 225));
            } else {
                button.setForeground(new Color(210, 40, 40));
                button.setBackground(new Color(250, 220, 220));
            }
            return button;
        }
        @Override public Object getCellEditorValue() { return currentStatus; }
    }



    // ─────────────────────────────────────────────────────────────────────────
    //  ACTION HANDLERS  — wire your real logic here
    // ─────────────────────────────────────────────────────────────────────────

    /** Fired when a nav tab is clicked. */
    private void onTabSelected(int index, String tabName) {
        System.out.println("Tab selected: " + tabName + " (index " + index + ")");
        // TODO: swap the center panel content to match the selected tab
    }

    /** Fired when the SRC or DEST circle button is clicked. */
    private void onFolderButtonClicked(String which) {

    
        
        if (which.toUpperCase().equals("SRC")) {

            FileChooserUtil.chooseFile();
            transferAttr.sourcePath = FileChooserUtil.selectedPath;
            transferAttr.sourceFileName = FileChooserUtil.selectedFileName;

            if (transferAttr.sourcePath == null) {
                System.out.println("No file selected.");
                return;
            }
            
        }

        if(which.toUpperCase().equals("DEST")) {

            FileChooserUtil.chooseFolderAndGetPath();
            transferAttr.destFolder = FileChooserUtil.SelectedDestinationFolder;

            if (transferAttr.destFolder == null) {
                System.out.println("No folder selected.");
                return;
            }

            System.out.println("Selected file path: " + transferAttr.sourcePath);
            System.out.println("Selected file name: " + transferAttr.sourceFileName);

            System.out.println("Selected destination folder: " + transferAttr.destFolder);

            String fileExtension = transferAttr.sourceFileName.contains(".") ?transferAttr.sourceFileName.substring(transferAttr.sourceFileName.lastIndexOf(".") + 1) : "";
            System.out.println("File extension: " + fileExtension);

            // Start the file transfer with resume capability
           new ResumeFileTransfer(transferAttr.sourceFileName,transferAttr.sourcePath, transferAttr.destFolder,"000");
           dispose();
        
           System.out.println("Done");
        }


    }

    /** Fired when a data row is clicked (not the status button). */
    private void onRowClicked(int row, DefaultTableModel model) {
        System.out.println("Row clicked: " + row + " — " + model.getValueAt(row, 2));
        // TODO: show details panel or open file explorer
    }

  

    // Completed/Interrupted status button click handler
    private void onStatusButtonClicked(int row, String status) {
        
        if(status.equalsIgnoreCase("Completed")) {

            System.out.println("Status button clicked in row " + row + ": " + status);
            
            
        } else {

            // If Interrupted, open the ResumeFileTransfer window for that transfer
            List<jsonAttribute> all = DatabaseManager.readTransfers();
            for (jsonAttribute tr : all) {
                if(row == Integer.parseInt(tr.si) - 1 && tr.transferStatus.equalsIgnoreCase("Interrupted")) {
                    

                    System.out.println("Opening ResumeFileTransfer for transfer ID: " + tr.transferId);
                    System.out.println("File: " + tr.fileName);
                    System.out.println("Source: " + tr.sourcePath);
                    System.out.println("Destination: " + tr.destinationPath);

                    new ResumeFileTransfer(tr.fileName, tr.sourcePath, tr.destinationPath,tr.transferId);
                    dispose();
                    System.out.println("Done");
                   
                }

            }

            System.out.println("Status button clicked in row " + row + ": " + status);  
        }
    }



    // Utility to shorten long paths for display
    private String shorten(String text, int maxLength) {
        if (text.length() <= maxLength)
            return text;
        return text.substring(0, maxLength) + "...";
    }



    // ─────────────────────────────────────────────────────────────────────────
    //  MAIN
    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(App::new);
    }
}