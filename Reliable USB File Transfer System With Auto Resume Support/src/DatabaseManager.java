import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

public class DatabaseManager {
    

    private static final String FILE_PATH = "history.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // 🔹 Read all
    public static List<jsonAttribute> readTransfers() {
    try {
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        Reader reader = new FileReader(file);

        List<jsonAttribute> list = gson.fromJson(reader,
                new com.google.gson.reflect.TypeToken<List<jsonAttribute>>() {}.getType());

        reader.close();

        return list != null ? list : new ArrayList<>();

    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>(); // ✅ never null
    }
}

    // 🔹 Write all
    public static void writeTransfers(List<jsonAttribute> list) {
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(list, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 🔹 Add (Append)
   public static void addTransfer(jsonAttribute t) {
    try {
        List<jsonAttribute> list = readTransfers();

        // ✅ FIX: handle null
        if (list == null) {
            list = new ArrayList<>();
        }

        list.add(t);

        Writer writer = new FileWriter(FILE_PATH);
        gson.toJson(list, writer);
        writer.flush();
        writer.close();

    } catch (Exception e) {
        e.printStackTrace();
    }
}


    // 🔹 Update by transferId
    public static void updateTransfer(String id, String newStatus) {
        List<jsonAttribute> list = readTransfers();

        for (jsonAttribute t : list) {
            if (t.transferId.equals(id)) {
                t.transferStatus = newStatus;
                break;
            }
        }

        writeTransfers(list);
    }

    // 🔹 Delete by transferId
    public static void deleteTransfer(String id) {
        List<jsonAttribute> list = readTransfers();
        list.removeIf(t -> t.transferId.equals(id));
        writeTransfers(list);
    }
}