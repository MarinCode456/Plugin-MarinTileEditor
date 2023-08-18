package marintileeditor.main.TileManager.MaryManager;

import library.Hexagon;
import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.HallTable.HallTable;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;

import java.util.ArrayList;
import java.util.logging.Logger;

// Обработано
public class MarinBayUtil {

    // Тех. часть
    private static Logger log = Bukkit.getLogger();
    private static World world = Bukkit.getWorld("world");

    // Базы данных
    private static Database databaseDB = Database.STAT_DATABASE;
    private static Database buildingsDB = Database.BUILDINGS_DATABASE;

    // Проверки блоков
    public static boolean isSign(Material type) {
        return type.name().endsWith("SIGN");
    }
    public static boolean isLog(Material type) {
        return type.name().endsWith("LOG");
    }
    public static boolean isSmoothStone(Material type) {
        return type == Material.SMOOTH_STONE;
    }
    public static boolean isSmoothHalfStone(Material type) {
        return type == Material.SMOOTH_STONE_SLAB;
    }
    public static boolean isBarrel(Material type) {
        return type == Material.BARREL;
    }
    public static boolean isLectern(Material type) {
        return type == Material.LECTERN;
    }
    public static boolean isHalfBlockRightInvert(Block half) {
        BlockData data = half.getBlockData();
        return data instanceof org.bukkit.block.data.type.Slab && ((Slab) data).getType() == Slab.Type.TOP;
    }
    public static boolean isGrindStone(Material type) {
        return type == Material.GRINDSTONE;
    }
    public static boolean isFletchingTable(Material type) {
        return type == Material.FLETCHING_TABLE;
    }

    // Получаем все существующие столы ратуш
    public static ArrayList<HallTable> getAllHallTables() {
        String[] stringHallTablesNames = buildingsDB.getStringList("halltables");
        ArrayList<Block> signs = new ArrayList<>();
        Location signLoc;

        // Получаем по порядку все блоки табличек всех HallTable'ов
        for (String htName : stringHallTablesNames) {
            signLoc = Main.getInstance().getLocation(buildingsDB.getString("halltables." + htName + ".signLocation"));
            signs.add(world.getBlockAt(signLoc));
        }

        // Получаем все HallTables
        ArrayList<HallTable> hallTables = new ArrayList<>();
        for (int i = 0; i < stringHallTablesNames.length; i++) {
            HallTable newHT = new HallTable(signs.get(i), stringHallTablesNames[i], false);
            hallTables.add(i, newHT);
        }

        return hallTables;
    }

    // Получаем все существующие столы казарм
    public static ArrayList<Barrack> getAllBarracks() {
        String[] stringBarracksNames = buildingsDB.getStringList("barracks");
        ArrayList<Block> signs = new ArrayList<>();
        Location signLoc;

        // Получаем по порядку все блоки табличек всех HallTable'ов
        for (String barrackName : stringBarracksNames) {
            signLoc = Main.getInstance().getLocation(buildingsDB.getString("barracks." + barrackName + ".signLocation"));
            signs.add(world.getBlockAt(signLoc));
        }

        // Получаем все Barracks
        ArrayList<Barrack> barracks = new ArrayList<>();
        for (int i = 0; i < stringBarracksNames.length; i++) {
            Barrack newBarrack = new Barrack(stringBarracksNames[i], signs.get(i));
            barracks.add(i, newBarrack);
        }

        return barracks;
    }

    // Проверяет есть ли у соседних гексагонов в радиусе 1
    // Постройка city или state ИМЕННО ТОГО ГОСУДАРСТВА, В КОТОРОМ ТЫ
    // Если есть - возвращает true, если нет - false
    public static boolean isNeighborsHexagonHasBuildingCS(Hexagon hexagon, String stateID) {
        Hexagon[] neighbors = hexagon.getNeighbors(1);
        for (Hexagon hex : neighbors) {
            String owner = databaseDB.getString("marinbay.hexagons." + hex.toString() + ".owner");
            String building = databaseDB.getString("marinbay.hexagons." + hex.toString() + ".building");
            if (building == null || owner == null) continue;
            if (building.equals("state") || building.equals("city")) {
                if (owner.equals(stateID)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Проверяет владелец ли данное государство данному гексагону
    public static boolean isHexagonOwnerThisState(Hexagon hexagon, String stateID) {
        String owner = databaseDB.getString("marinbay.hexagons." + hexagon.toString() + ".owner");
        if (owner == null) return false;
        return databaseDB.getString("marinbay.hexagons." + hexagon.toString() + ".owner").equals(stateID);
    }

    // Проверяет есть ли у указанного гексагона постройка building
    public static boolean hasHexagonBuilding(String hex) {
        return databaseDB.isSet("marinbay.hexagons." + hex + ".building");
    }
}
