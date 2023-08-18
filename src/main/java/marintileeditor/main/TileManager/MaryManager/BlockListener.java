package marintileeditor.main.TileManager.MaryManager;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import library.Hexagon;
import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.HallTable.HallTable;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// Обработан
public class BlockListener implements Listener {

    // Тех. часть
    public static Logger log = Bukkit.getLogger();

    // Базы данных
    private static Database buildingsDB = Database.BUILDINGS_DATABASE;
    private static Database databaseDB = Database.STAT_DATABASE;

    // Все названия всех HallTables
    public static List<String> namesOfHT;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSignChange(SignChangeEvent e) {
        // Переменные
        String[] lines = e.getLines();
        World world = e.getBlock().getWorld();
        Player p = e.getPlayer();

        // Проверки
        if (world.getEnvironment() != World.Environment.NORMAL) {
            // Если мы не в обычном мире, просто возвращаем функцию
            p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете создать постройку в необычном мире");
            return;
        }
        if (lines[1] == null) return;
        if (lines[2] == null) return;

        // Переменные
        String nameOfCity = lines[2];
        Block sign = e.getBlock();
        String playerStatus = "";
        if (databaseDB.getString("marinbay.player." + p.getName().toLowerCase() + ".status") != null) {
            playerStatus = databaseDB.getString("marinbay.player." + p.getName().toLowerCase() + ".status");
        }
        // Переменная, говорящая ставил ли игрок город
        String placeCity = databaseDB.getString("marinbay.player." + p.getName().toLowerCase() + ".placeCity");
        boolean doesPlayerPlaceCity = false;
        if (placeCity != null) {
            doesPlayerPlaceCity = !placeCity.equals("NO");
        }

        // Этот массив для того, чтобы нельзя было создать город, который уже есть
        namesOfHT = Arrays.asList(buildingsDB.getStringList("halltables"));

        // Создаём разные холтейбл для города и для государства
        if (lines[1].contains("Город")) {
            if (!databaseDB.isSet("marinbay.player." + p.getName().toLowerCase() + ".state")) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете создать город, так как не состоите в государстве");
                return;
            }
            if (playerStatus.equals("general")) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Государь не может создавать города, для этого ему нужны верные слуги в лице Губернаторов и Приближённых");
                return;
            }
            if (!playerStatus.equals("closer") && !playerStatus.equals("guber")) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете создать город, так как вы гражданин");
                return;
            }
            // Проверяем ставил ли игрок город
            if (doesPlayerPlaceCity) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "У вас уже есть свой город, чтобы поставить новый, удалите предыдущий");
                return;
            }
            if (nameOfCity.length() < 3) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Название города не может быть короче 3-ёх символов");
                return;
            }
            if (!MrTransliterator.isValid(nameOfCity)) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Название города не подходит для создания стола, измените имя и попробуйте снова");
                return;
            }
            if (!ProtectedRegion.isValidId(MrTransliterator.rusToEng(nameOfCity.toLowerCase().replaceAll(" ", "")))) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Название города не подходит для создания стола, измените имя и попробуйте снова");
                return;
            }
            // Этот массив для того, чтобы нельзя было создать город, который уже есть
            if (namesOfHT.contains(nameOfCity)) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Город с таким названием уже существует");
                return;
            }
            Hexagon hex = Main.getInstance().grid.getHexagon(sign.getLocation());
            String stateID = databaseDB.getString("marinbay.player." + p.getName().toLowerCase() + ".state");
            if (!MarinBayUtil.isHexagonOwnerThisState(hex, stateID)) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Этот тайл не принадлежит вашему государству, поэтому вы не можете создать в нём город");
                return;
            }
            if (MarinBayUtil.isNeighborsHexagonHasBuildingCS(hex, stateID)) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы не можете создать город в этом тайле, так как рядом уже есть ваш город или государство");
                return;
            }

            // Если все проверки пройдены:
            if (HallTable.create(sign, nameOfCity, "city")) {
                HallTable newHT = HallTable.getHTByBlock(sign);
                String nameOfP = p.getName();

                // Время создания
                buildingsDB.write("halltables." + nameOfCity + ".timeOfCreate", String.valueOf(System.currentTimeMillis()));

                buildingsDB.write("halltables." + nameOfCity + ".creator", nameOfP);
                buildingsDB.write("halltables." + nameOfCity + ".direction", String.valueOf(newHT.getBody().getDirection(sign)));
                databaseDB.addOneElement("marinbay.state." + stateID + ".buildings.halltables", newHT.getName());
                p.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Стол ратуши создан!");
            }
        } else if (lines[1].contains("Государство")) {
            String nameOfState = lines[2];

            if (databaseDB.isSet("marinbay.player." + p.getName().toLowerCase() + ".state")) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы уже состоите в государстве");
                return;
            }
            if (nameOfState.length() < 3) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Название государства не может быть короче 3-ёх символов");
                return;
            }
            if (!MrTransliterator.isValid(nameOfState)) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Название государства не подходит для создания стола, измените имя и попробуйте снова");
                return;
            }
            if (!ProtectedRegion.isValidId(MrTransliterator.rusToEng(nameOfCity.toLowerCase().replaceAll(" ", "")))) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Название государства не подходит для создания стола, измените имя и попробуйте снова");
                return;
            }
            if (namesOfHT.contains(nameOfState)) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Государство или город с таким названием уже существует");
                return;
            }

            // Если все проверки пройдены:
            if (HallTable.create(sign, nameOfState, "state")) {
                HallTable newHT = HallTable.getHTByBlock(sign);
                String nameOfP = p.getName();

                // Время создания
                buildingsDB.write("halltables." + newHT.getName() + ".timeOfCreate", String.valueOf(System.currentTimeMillis()));

                buildingsDB.write("halltables." + nameOfState + ".creator", nameOfP);
                buildingsDB.write("halltables." + nameOfState + ".direction", String.valueOf(newHT.getBody().getDirection(sign)));
                p.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Стол государства создан!");
            }
        } else if (lines[1].contains("Казарма")) {
            if (!databaseDB.isSet("marinbay.player." + p.getName().toLowerCase() + ".state")) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете создать казарму, так как не состоите в государстве");
                return;
            }
            Hexagon hex = Main.getInstance().grid.getHexagon(sign.getLocation());
            String stateID = databaseDB.getString("marinbay.player." + p.getName().toLowerCase() + ".state");
            if (!MarinBayUtil.isHexagonOwnerThisState(hex, stateID)) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Этот тайл не принадлежит вашему государству, поэтому вы не можете создать в нём казарму");
                return;
            }
            if (MarinBayUtil.hasHexagonBuilding(hex.toString())) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "В этом тайле уже есть постройка");
                return;
            }

            // Если все проверки пройдены:
            if (Barrack.create(sign)) {
                Barrack newBarrack = Barrack.getBarrackByBlock(sign);
                String nameOfP = p.getName();

                // Записываем город и государство которому принадлежит
                String ownerState = databaseDB.getString("marinbay.hexagons." + hex + ".owner");
                buildingsDB.write("barracks." + newBarrack.getName() + ".ownerState", ownerState);
                if (databaseDB.isSet("marinbay.hexagons." + hex + ".trueOwner")) {
                    String ownerCity = databaseDB.getString("marinbay.hexagons." + hex + ".trueOwner");
                    buildingsDB.write("barracks." + newBarrack.getName() + ".ownerCity", ownerCity);
                    // Записываю этот барак этому городу
                    databaseDB.addOneElement("marinbay.city." + ownerCity + ".buildings.barracks", newBarrack.getName());
                }

                // Время создания
                buildingsDB.write("barracks." + newBarrack.getName() + ".timeOfCreate", String.valueOf(System.currentTimeMillis()));

                buildingsDB.write("barracks." + newBarrack.getName() + ".creator", nameOfP);
                buildingsDB.write("barracks." + newBarrack.getName() + ".direction", String.valueOf(newBarrack.getBody().getDirection(sign)));
                databaseDB.addOneElement("marinbay.state." + stateID + ".buildings.barracks", newBarrack.getName());
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Стол казармы создан!");
                DynmapDrawer.redrawDynmap();
            }
        }
    }

    // Если уровень холтейбла - 0, то вещи, которые ты уже
    // Успел положить в него, выпадут
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreak(BlockBreakEvent e) {
        if (e.getBlock() == null) return;
        if (e.getBlock().getType() == null) return;

        Block block = e.getBlock();
        Material blocktype = block.getType();

        if (MarinBayUtil.isLog(blocktype) ||
                MarinBayUtil.isBarrel(blocktype) ||
                MarinBayUtil.isLectern(blocktype) ||
                MarinBayUtil.isSmoothHalfStone(blocktype) ||
                MarinBayUtil.isSmoothStone(blocktype) ||
                MarinBayUtil.isSign(blocktype)
        ) {
            HallTable ht = HallTable.getHTByBlock(block);
            Barrack barrack = null;
            if (ht == null) {
                barrack = Barrack.getBarrackByBlock(block);
            }

            if (ht != null) {
                if (ht.getScore() == 0) {
                    ht.remove(block, e.getPlayer());
                }
            }

            if (barrack != null) {
                if (barrack.getScore() == 0) {
                    barrack.remove(block, e.getPlayer(), barrack);
                }
            }
        }
    }
}
