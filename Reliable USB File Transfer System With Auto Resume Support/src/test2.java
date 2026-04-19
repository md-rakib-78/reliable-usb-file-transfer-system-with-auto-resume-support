import javax.swing.*;
import java.awt.datatransfer.*;
import java.io.File;
import java.util.List;

public class test2 {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Drag & Drop Demo");
        JTextArea area = new JTextArea("Drop files or folders here...");
        area.setEditable(false);

        // Enable drag & drop
        area.setTransferHandler(new TransferHandler() {

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                try {
                    Transferable t = support.getTransferable();

                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

                    area.append("\n\nDropped items:\n");

                    for (File file : files) {
                        area.append(file.getAbsolutePath() + "\n");

                        if (file.isDirectory()) {
                            area.append(" → Folder detected\n");
                            
                        } else {
                            area.append(" → File detected\n");
                        }
                    }

                    return true;

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
        });

        frame.add(new JScrollPane(area));
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}