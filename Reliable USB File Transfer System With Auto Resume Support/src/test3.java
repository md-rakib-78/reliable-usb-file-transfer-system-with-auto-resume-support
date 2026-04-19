import java.io.File;
import java.util.*;
import java.nio.file.*;

public class test3 {

    private static final File LOG_FILE = new File("connected_drives.txt");

    public static List<String> getDriveLetters() {

        List<String> letters = new ArrayList<>();

        if (!LOG_FILE.exists() || LOG_FILE.length() == 0) return letters;

        try {
            List<String> lines = Files.readAllLines(LOG_FILE.toPath());

            for (String line : lines) {
                // Each line format: "E:\ - USB Drive (SanDisk)"
                // Drive letter is always the first character
                if (line != null && line.length() > 0) {
                    String letter = String.valueOf(line.charAt(0));
                    if (letter.matches("[A-Za-z]")) {
                        letters.add(letter.toUpperCase());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return letters;
    }

    // Returns true if the given drive letter exists in connected_drives.txt
    public static boolean isDriveConnected(String letter) {

        if (letter == null || letter.trim().isEmpty()) return false;

        return getDriveLetters().contains(letter.toUpperCase());
    }

    public static void main(String[] args) {

        // Get all drive letters
        List<String> driveLetters = getDriveLetters();

        if (driveLetters.isEmpty()) {
            System.out.println("No drives found in file.");
        } else {
            System.out.println("Connected drive letters: " + driveLetters);
            // Output example: Connected drive letters: [E, F, H]

            for (String letter : driveLetters) {
                System.out.println("Drive: " + letter);
            }
        }

        // Search for a specific drive letter
        String search = "H";

        if (isDriveConnected(search)) {
            System.out.println("Drive " + search + " is connected.");
        } else {
            System.out.println("Drive " + search + " is NOT connected.");
        }
    }
}