package marintileeditor.main.TileManager.MaryManager;

import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.HallTable.HallTable;
import marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Player.MarinPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.logging.Logger;

// Обработано
public class PlayerListener implements Listener {

    // Тех. часть
    public Logger log = Bukkit.getLogger();

    // Базы данных
    private Database databaseDB = Database.STAT_DATABASE;
    private Database generalsDB = Database.GENERALS_DATABASE;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerInteract(PlayerInteractEvent e) {

        // Переменные
        Block clickedBlock = e.getClickedBlock();

        // Проверки
        // Работает только с основной рукой
        if (e.getHand() == null) return;
        if (!e.getHand().equals(EquipmentSlot.HAND)) return;
        if (clickedBlock == null) return;
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        // Переменные
        Player player = e.getPlayer();
        MarinPlayer mPlayer = new MarinPlayer(player);
        Material type = clickedBlock.getType();
        HallTable ht = null;
        Barrack barre = null;

        // Получаем стол ратуши, если это нужный нам тип блока
        // Если это не то, значит вернёт ht = null
        if (MarinBayUtil.isLog(type) ||
        MarinBayUtil.isBarrel(type) ||
        MarinBayUtil.isLectern(type) ||
        MarinBayUtil.isSmoothHalfStone(type) ||
        MarinBayUtil.isSmoothStone(type)
        ) {
            ht = HallTable.getHTByBlock(clickedBlock);
            if (ht == null) {
                barre = Barrack.getBarrackByBlock(clickedBlock);
            }
        }

        if (ht != null) {
            String creatorName = ht.getCreator();
            if (creatorName.equals(player.getName())) {
                e.setCancelled(true);
                ht.open(player);
            } else {
                // Если это не гражданин этого государства, то пишем ему об этом
                if (!mPlayer.isCitizenThatState(ht.getStateID())) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не гражданин этого государства и у вас нет доступа к его столу");
                    return;
                }
                // Если это не создатель холтейбла, но все же государь иди приближенный, то ему
                // Всё равно можно открыть инвентарь
                if (mPlayer.isGeneral() || mPlayer.isCloser()) {
                    e.setCancelled(true);
                    ht.open(player);
                    return;
                }

                e.setCancelled(true);
                ht.openForCitizen(player);
                ht.playOpeningSound();
                return;
            }
        }

        if (barre != null) {
            String creatorName = barre.getCreator();
            if (creatorName.equals(player.getName())) {
                int scoreBarre = barre.getScore();
                if (scoreBarre == 3) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Эта казарма максимального уровня");
                    return;
                }

                e.setCancelled(true);
                barre.open(player);
            } else {
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Доступ к казармам имеет только её создатель и глава государства");
                return;
            }
        }
    }

    // Записываем время выхода генералов, чтобы если что удалить их госво, если они не заходят 7 дней
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void playerQuit(PlayerQuitEvent e) {
        MarinPlayer player = new MarinPlayer(e.getPlayer());
        // Проверяем генерал ли он
        if (player.isGeneral()) {
            generalsDB.write("generals." + player.getId() + ".time", String.valueOf(System.currentTimeMillis()));
            generalsDB.write("generals." + player.getId() + ".state", player.getStateID());
        }
        // Проверяем приближённый ли он
        if (player.isCloser()) {
            generalsDB.write("closers." + player.getId() + ".time", String.valueOf(System.currentTimeMillis()));
            generalsDB.write("closers." + player.getId() + ".state", player.getStateID());
        }
    }

    // При заходе в игру всем генералам, у которых удалились страны будет писаться сообщение об этом
    // Но лишь один раз. До сообщения в файле generals.yml - NO_MORE_GENERAL
    // После - просто удаляем игрока из датабазы
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void checkNotGeneralJoin(PlayerJoinEvent e) {
        MarinPlayer player = new MarinPlayer(e.getPlayer());
        // Проверяем генерал ли он
        if (generalsDB.isSet("generals." + player.getId())) {
            if (generalsDB.getString("generals." + player.getId() + ".time").equals("NOT_GENERAL_MORE")) {
                e.getPlayer().sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Пока вы отсутствовали вашу страну разграбили разбойники, ваше государство удалено");
                generalsDB.remove("generals." + player.getId());
            }
        }
        // Проверяем клозер ли он
        if (generalsDB.isSet("closers." + player.getId())) {
            if (generalsDB.getString("closers." + player.getId() + ".time").equals("NOT_CLOSER_MORE")) {
                e.getPlayer().sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Пока вы отсутствовали вашу страну разграбили разбойники, ваше государство удалено");
                generalsDB.remove("closers." + player.getId());
            }
        }
        // Выводим сообщение о приглашении в государство, если такое есть
        if (databaseDB.isSet("marinbay.player." + player.getId() + ".invitesFromCities")) {
            String stateID = databaseDB.getString("marinbay.player." + player.getId() + ".invitesFromCities");
            String generalNameLower = databaseDB.getString("marinbay.state." + stateID + ".general");
            String generalName = databaseDB.getString("marinbay.player." + generalNameLower + ".name");
            InventoryHelper.sendInviteMessage(generalName, stateID, e.getPlayer());
        }
        // Выводим сообщение о том, что вас кикнули, если такое произошло
        if (databaseDB.isSet("marinbay.player." + player.getId() + ".townKicked")) {
            String stateName = databaseDB.getString("marinbay.player." + player.getId() + ".townKicked");
            player.getPlayer().sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы исключены из государства " + stateName);
            databaseDB.remove("marinbay.player." + player.getId() + ".townKicked");
        }
    }
}
