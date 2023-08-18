package marintileeditor.main.TileManager.TileManager.ID;

import com.fasterxml.uuid.Generators;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.logging.Logger;

// Обработано
public class ID {

    // Тех. часть
    static Logger log = Bukkit.getLogger();

    // Получаем ID города по его имени
    public static String getCityID(String name) {
        Database buildingsDB = Database.BUILDINGS_DATABASE;
        if (!buildingsDB.isSet("halltables." + name + ".id")) {
            return null;
        }
        return buildingsDB.getString("halltables." + name + ".id");
    }

    // Получаем ID холтейбла по его имени
    public static String getHallTableID(String name) {
        Database buildingsDB = Database.BUILDINGS_DATABASE;
        if (!buildingsDB.isSet("halltables." + name + ".id")) {
            String stringID = Generators.timeBasedGenerator().generate().toString();
            buildingsDB.write("halltables." + name + ".id", stringID);
        }
        return buildingsDB.getString("halltables." + name + ".id");
    }

    // Получаем ID казармы по её имени
    public static String getBarrackID(String name) {
        Database buildingsDB = Database.BUILDINGS_DATABASE;
        if (!buildingsDB.isSet("barracks." + name + ".id")) {
            String stringID = Generators.timeBasedGenerator().generate().toString();
            buildingsDB.write("barracks." + name + ".id", stringID);
        }
        return buildingsDB.getString("barracks." + name + ".id");
    }
}
