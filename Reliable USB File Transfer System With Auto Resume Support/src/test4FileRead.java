import java.io.File;
import java.util.*;

public class test4FileRead {
    public static void main(String[] args) {

        File file = new File("connected_drives.txt");
                String text = "";

        
        try (Scanner scanner = new Scanner(file)) 
        {
            text="";

            while (scanner.hasNextLine())
           {
                
                text = scanner.nextLine().toString();
                System.out.println("drives: " + text);
            }

        } 
        catch (Exception e) 
        {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }
        
    }
}
