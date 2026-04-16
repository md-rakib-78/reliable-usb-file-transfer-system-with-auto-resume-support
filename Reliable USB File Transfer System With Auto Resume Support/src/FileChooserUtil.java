import javax.swing.*;
import java.awt.*;
import java.io.File;



public class FileChooserUtil {

    public static String selectedPath = null;
    public static String selectedFileName = null;
    public static String SelectedDestinationFolder=null;

    public static void chooseFile() {

        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file");

        JFrame frame = new JFrame();
        frame.setUndecorated(true); 
        frame.setLocationRelativeTo(null); 

        Image icon = new ImageIcon(FileChooserUtil.class.getResource("/assets/img/logo_icon.png")).getImage();

        frame.setIconImage(icon);

        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        int result = fileChooser.showOpenDialog(frame);

        frame.dispose();

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            selectedPath = file.getAbsolutePath().replace("\\", "/");
            selectedFileName = file.getName();
        }
    }




    public static void chooseFolderAndGetPath() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JFileChooser folderChooser = new JFileChooser();
        folderChooser.setDialogTitle("Select a Folder");
        folderChooser.setCurrentDirectory(
                new File(System.getProperty("user.home")));

        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderChooser.setAcceptAllFileFilterUsed(false);

        JFrame frame = new JFrame();
        frame.setUndecorated(true);

        Image icon = new ImageIcon(
                FileChooserUtil.class.getResource("/assets/img/logo_icon.png")).getImage();
        frame.setIconImage(icon);

        int result = folderChooser.showOpenDialog(frame); 

        frame.dispose(); 

        if (result == JFileChooser.APPROVE_OPTION) {
            String convert = folderChooser.getSelectedFile().getAbsolutePath();
            String desConverted = convert.replace("\\", "/");
            SelectedDestinationFolder = desConverted.concat("/" + selectedFileName);
        }
    }

}
