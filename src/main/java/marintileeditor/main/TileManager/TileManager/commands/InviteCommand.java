package marintileeditor.main.TileManager.TileManager.commands;

import marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper;
import marintileeditor.main.TileManager.MaryManager.MrTransliterator;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Обработано
public class InviteCommand implements CommandExecutor {

    // Базы данных
    Database databaseDB = Database.STAT_DATABASE;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 567 это специальный код, чтобы никто случайно не ввёл эту команду
        if (args.length == 0 || !args[0].equals("567")) {
            sender.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не Don_Yagon!");
            return true;
        }
        if (args.length < 3) {
            return true;
        }
        String playerName = args[1];
        String lowerCasePlayer = playerName.toLowerCase();
        String playerState = databaseDB.getString("marinbay.player." + lowerCasePlayer + ".state");
        if (playerState != null) {
            // Если state не null, значит у игрока уже есть государство, принимать его точно не стоит
            sender.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете принять приглашение, так как уже гражданин государства");
            return true;
        }

        // Основные переменные
        String stateID = args[2];
        String stateName = databaseDB.getString("marinbay.state." + stateID + ".name");
        String regionName = MrTransliterator.rusToEng(stateName.toLowerCase().replace(" ", ""));

        // Вписываем человека в citizens
        // Вписываем state человеку и время его прибытия в государство
        // Добавляем это государство в строку wherebe, пишем статус citizen
        databaseDB.writePlayerToState("marinbay.state." + stateID + ".citisens", playerName);
        databaseDB.addOneElement("marinbay.state." + stateID + ".ranks.citizen", playerName);
        databaseDB.write("marinbay.player." + lowerCasePlayer + ".state", stateID);
        databaseDB.write("marinbay.player." + lowerCasePlayer + ".timeInvite", "" + System.currentTimeMillis());
        databaseDB.writeNewStateWhereBe("marinbay.player." + lowerCasePlayer + ".wherebe", stateName);
        databaseDB.write("marinbay.player." + lowerCasePlayer + ".status", "citizen");
        databaseDB.write("marinbay.player." + playerName.toLowerCase() + ".invitesFromCities", null);

        InventoryHelper.addToRegion(regionName,playerName);

        Player player = Bukkit.getPlayer(playerName);
        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Поздравляем! Теперь вы гражданин государства " + stateName + "!");

        return true;
    }
}
