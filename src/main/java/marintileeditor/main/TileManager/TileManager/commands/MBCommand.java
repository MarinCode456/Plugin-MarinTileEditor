package marintileeditor.main.TileManager.TileManager.commands;

import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.HallTable.HallTable;
import marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

import static marintileeditor.main.TileManager.TileManager.City.City.addOneHexagon;
import static marintileeditor.main.TileManager.TileManager.City.City.removeOneHexagon;

// Обработано
public class MBCommand implements CommandExecutor {

    // Логгер
    private Logger log = Bukkit.getLogger();

    // Базы данных
    Database databaseDB = Database.STAT_DATABASE;
    Database buildinsDB = Database.BUILDINGS_DATABASE;
    Database idDB = Database.ID_LINK_DATABASE;
    Database colorDB = Database.COLOR_CHANGE_DATABASE;
    Database inviteDB = Database.INVITE_DATABASE;
    Database generalsDB = Database.GENERALS_DATABASE;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем, что это дон ягон
        String nameSender = sender.getName();
        Player player = (Player) sender;

        if (!nameSender.equals("Don_Yagon")) {
            sender.sendMessage(ChatColor.RED + "[MarinBay] " + ChatColor.WHITE + "Вы не Don_Yagon!");
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "[MarinBay] " + ChatColor.WHITE + "Ну и где args[], мымра?");
            return true;
        }

        // Команда перезагружает все базы данных и карту динмап
        if (args[0].equals("reload")) {
            databaseDB.reload();
            buildinsDB.reload();
            idDB.reload();
            colorDB.reload();
            inviteDB.reload();
            generalsDB.reload();
            DynmapDrawer.redrawDynmap();

            sender.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Базы данных перезагружены");
        }

        // Проверка в каком гексагоне стоит игрок
        if (args[0].equals("hexagon")) {
            sender.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Ваш гексагон: " + Main.grid.getHexagon(player.getLocation()).toString());
        }

        // Команда отдаёт тайл, в котором нахожусь я тому гос-ву, что я укажу
        if (args[0].equals("give")) {
            if (args.length < 2) {
                return true;
            }

            // Проверка, есть ли такое государство
            // !!! указываем название, как на динмапе "Ягон" например, важно попасть в регистр
            String stateID = buildinsDB.getString("halltables." + args[1] + ".id");
            if ((stateID == null) || !buildinsDB.getString("halltables." + args[1] + ".status").equals("state")) {
                sender.sendMessage(ChatColor.RED + "[MarinBay] " + ChatColor.WHITE + "Государства с названием " + args[1] + " не существует!");
                return true;
            }

            // Проверка, занят ли гексагон, в котором находится игрок
            String hexStr = Main.grid.getHexagon(player).toString();
            if (!databaseDB.isSet("marinbay.hexagons." + hexStr)) {
                sender.sendMessage(ChatColor.RED + "[MarinBay]" + ChatColor.WHITE + " Этот тайл не принадлежит никакому государству");
                return true;
            }

            // Получаем старого владельца гексагона
            String oldOwnerID = databaseDB.getString("marinbay.hexagons." + hexStr + ".owner");

            // Меняем владельца гексагона и убираем постройку если есть
            databaseDB.write("marinbay.hexagons." + hexStr + ".owner", stateID);
            if (databaseDB.isSet("marinbay.hexagons." + hexStr + ".building")) {
                String building = databaseDB.getString("marinbay.hexagons." + hexStr + ".building");

                if (building.equals("city")) {
                    String cityID = databaseDB.getString("marinbay.hexagons." + hexStr + ".cityID");
                    String cityName = databaseDB.getString("marinbay.city." + cityID + ".name");
                    String cityCapital = databaseDB.getString("marinbay.city." + cityID + ".capital");
                    Block capitalBlock = Main.getInstance().getLocation(cityCapital).getBlock();

                    log.info("1 Я нашёл город в этом тайле и начинаю его удаление");
                    log.info("1 Айди города - " + cityID);
                    log.info("1 Название города - " + cityName);
                    log.info("1 Табличка города - " + cityCapital);
                    HallTable cityHT = new HallTable(capitalBlock, cityName, false);
                    InventoryHelper.deleteCity(cityID, cityHT,0);
                } else if (building.equals("barrack")) {
                    String barrackName = databaseDB.getString("marinbay.hexagons." + hexStr + ".barrackName");
                    log.info("2 Я нашёл казарму для удаления - " + barrackName);
                    Barrack.removeFromDBs(barrackName);
                }
            }

            // Удаляем гексагон из старого государства
            // Добавляем гексагон в новое государство
            removeOneHexagon(oldOwnerID, hexStr);
            addOneHexagon(stateID, hexStr);

            // Обновляем регионы обоих государств
            InventoryHelper.updateStateRegion(stateID);
            InventoryHelper.updateStateRegion(oldOwnerID);

            // Обновляем карту динмапа
            DynmapDrawer.redrawDynmap();

            // Пишем, что всё отлично
            sender.sendMessage(ChatColor.BLUE + "[MarinBay]" + ChatColor.WHITE + " Всё прошло успешно)");
            return true;
        }

        return true;
    }
}
