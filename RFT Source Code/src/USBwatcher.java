import java.io.File;
import java.util.*;
import java.nio.file.*;
import javax.swing.filechooser.FileSystemView;

public class USBwatcher implements Runnable {

    private static final File   LOG_FILE       = new File("connected_drives.txt");
    private Set<String>         previousDrives = new HashSet<>();

    @Override
    public void run() {

        previousDrives = getCurrentDrives();
        System.out.println("USB Watcher started...");

        while (true) {

            Set<String> currentDrives = getCurrentDrives();

            // Detect NEW devices
            for (String drive : currentDrives) {
                if (!previousDrives.contains(drive)) {
                    System.out.println("USB Inserted: " + drive);
                    onDeviceInserted(drive);
                }
            }

            // Detect REMOVED devices
            for (String drive : previousDrives) {
                if (!currentDrives.contains(drive)) {
                    System.out.println("USB Removed: " + drive);
                    onDeviceRemoved(drive);
                }
            }

            previousDrives = currentDrives;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    // GET ALL DRIVES AND THEIR INFO
    private Set<String> getCurrentDrives() {

        Set<String> drives = new HashSet<>();
        FileSystemView fsv  = FileSystemView.getFileSystemView();
        File[]         roots = File.listRoots();

        for (File root : roots) {

            if (fsv.isDrive(root) && !fsv.isFloppyDrive(root)) {

                String type  = fsv.getSystemTypeDescription(root);
                String label = fsv.getSystemDisplayName(root);

                if (type == null || type.isEmpty()) continue;

                if (label == null || label.isEmpty()) {
                    label = root.getAbsolutePath();
                }

                drives.add(root.getAbsolutePath() + " - " + type + " (" + label + ")");
            }
        }

        return drives;
    }

    // Write drive detail into the single txt file (create file if not exist)
    private void onDeviceInserted(String driveInfo) {

        if (driveInfo == null || driveInfo.trim().isEmpty()) return;

        System.out.println("Scanning new device... " + driveInfo);

        try {
            if (!LOG_FILE.exists()) {
                LOG_FILE.createNewFile();
            }

            // Read all current lines from the file
            List<String> lines = LOG_FILE.length() > 0
                    ? Files.readAllLines(LOG_FILE.toPath())
                    : new ArrayList<>();

            if (!lines.contains(driveInfo)) {
                lines.add(driveInfo);
                Files.write(LOG_FILE.toPath(), lines);
                System.out.println("Drive info written: " + driveInfo);
                new DeviceConnectedMsg();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Remove that drive's line from the single txt file
    private void onDeviceRemoved(String driveInfo) {

        if (driveInfo == null || driveInfo.trim().isEmpty()) return;
        if (!LOG_FILE.exists()) return;

        try {
            // Read all lines, remove the matching drive line, write back
            List<String> lines = Files.readAllLines(LOG_FILE.toPath());

            if (lines.remove(driveInfo)) {
                Files.write(LOG_FILE.toPath(), lines);
                System.out.println("Drive info removed: " + driveInfo);
                new DeviceDisconnectedMsg();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}