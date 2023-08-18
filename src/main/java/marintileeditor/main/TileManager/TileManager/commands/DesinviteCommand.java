package marintileeditor.main.TileManager.TileManager.commands;

import marintileeditor.main.TileManager.TileManager.Database.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

// Обработано
public class DesinviteCommand implements CommandExecutor {

    // Базы данных
    Database databaseDB = Database.STAT_DATABASE;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 567 это специальный код, чтобы никто случайно не ввёл эту команду
        if (!args[0].equals("567")) {
            sender.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не Don_Yagon!");
            return true;
        }
        if (args.length < 2) {
            return true;
        }

        // Убираем invitesFromCities
        String playerName = args[1];
        databaseDB.write("marinbay.player." + playerName.toLowerCase() + ".invitesFromCities", null);

        // Отменяем приглашение для игрока
        Player player = Bukkit.getPlayer(playerName);
        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Приглашение отменено");

        return true;
    }
}
