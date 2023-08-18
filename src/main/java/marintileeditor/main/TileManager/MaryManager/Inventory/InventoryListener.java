package marintileeditor.main.TileManager.MaryManager.Inventory;

import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.HallTable.HallTable;
import marintileeditor.main.TileManager.MaryManager.MrTransliterator;
import marintileeditor.main.TileManager.TileManager.City.City;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.ID.ID;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import marintileeditor.main.TileManager.TileManager.Player.MarinPlayer;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper.getItemLoreForHallTable;
import static marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper.getLoreForRankRaiseNDowngrade;

// Обработан
public class InventoryListener implements Listener {

    // Основные переменные
    Inventory clickInventory;
    HallTable ht;
    Inventory playerInventory;

    // Тех. часть
    Logger log = Bukkit.getLogger();

    // Базы данных
    Database buildingsDB = Database.BUILDINGS_DATABASE;
    Database databaseDB = Database.STAT_DATABASE;
    Database colorsChangeDB = Database.COLOR_CHANGE_DATABASE;
    Database inviteDB = Database.INVITE_DATABASE;

    // Переменные для получения информации о предмете, на который кликнули
    String nameOf11Item = "";
    String nameOf12Item = "";
    String nameOf25Item = "";
    String displayNameOfCurrentItem;
    String stateID;
    Material materialTypeOfCurrentItem;
    ItemStack currentItem;

    // Списки лоров для исключения, добавления игроков и тд, для инвентарей с головами
    ArrayList<String> lore = new ArrayList<>();
    List<String> loreOf1Item;
    List<String> loreOfHeadKick;
    List<String> loreOfHeadKickAlternative;
    List<String> loreOfHeadInvite;
    List<String> loreOfHeadInviteAlternative;

    List<String> loreRaiseRankCitizen;
    List<String> loreRaiseRankGuber;
    List<String> loreRaiseRankCloser1;
    List<String> loreRaiseRankCloser2;
    List<String> loreDowngradeRankCloser;
    List<String> loreDowngradeRankGuber;

    // Слоты для изменения предметов
    public static int [] slotesCreateButton = {
            26
    };

    // Проверки и кнопки
    ItemStack buttonItem;
    String buttonName;
    String firstItemOKNOT;
    String secondItemOKNOT;
    String thirdItemOKNOT;

    @EventHandler
    public void inventoryClick(InventoryClickEvent e) {
        // Если слоты 11, 12 и 0 пустуют, то это точно не наш инвентарь
        try {
            if (e.getInventory().getItem(11) == null) {
                if (e.getInventory().getItem(0) == null) {
                    if (e.getInventory().getItem(12) == null) {
                        return;
                    }
                }
            }
        } catch (IndexOutOfBoundsException o){
            // А вдруг у нас инвентарь не такой большой, чтобы содержать слот 11?
            return;
        }
        // Если предмет на который кликнули нулл то возвращаем
        if (e.getCurrentItem() == null) return;
        // Не очень удобная, но действенная конструкция
        if (e.getInventory().getItem(0) == null &&
                e.getInventory().getItem(11) == null &&
                e.getInventory().getItem(12) == null) {
            return;
        }
        if (e.getInventory().getItem(0) != null) {
            if (e.getInventory().getItem(0).getType() != Material.HONEYCOMB &&
                    e.getInventory().getItem(0).getType() != Material.WHITE_CONCRETE &&
                    e.getInventory().getItem(0).getType() != Material.PLAYER_HEAD) {
                return;
            }
        }
        if (e.getInventory().getItem(11) != null) {
            if (e.getInventory().getItem(11).getType() != Material.HONEYCOMB &&
                    e.getInventory().getItem(11).getType() != Material.BLUE_CONCRETE &&
                    e.getInventory().getItem(11).getType() != Material.PLAYER_HEAD) {
                return;
            }
        }
        if (e.getInventory().getItem(12) != null) {
            if (e.getInventory().getItem(12).getType() != Material.HONEYCOMB &&
                    e.getInventory().getItem(12).getType() != Material.BROWN_CONCRETE &&
                    e.getInventory().getItem(12).getType() != Material.PLAYER_HEAD) {
                return;
            }
        }
        // Если в 26 слоте у нас кнопка "Создать казарму" или "Вы пока не можете...",
        // То это точно не холтейбл и возвращаем функцию
        if (e.getInventory().getItem(26) != null) {
            String display26 = e.getInventory().getItem(26).getItemMeta().getDisplayName();
            if (display26.equals(ChatColor.AQUA + "Создать казарму") ||
                    display26.equals(ChatColor.AQUA + "Вы пока не можете создать казарму") ||
                    display26.equals(ChatColor.AQUA + "Улучшить казарму") ||
                    display26.equals(ChatColor.AQUA + "Вы пока не можете улучшить казарму")) {
                return;
            }
        }
        if (e.getInventory().getItem(11) != null) {
            String display11 = e.getInventory().getItem(11).getItemMeta().getDisplayName();
            if (display11.equals(ChatColor.AQUA + "Создать с казармой")) {
                return;
            }
        }

        // Получаем холтейбл, по которому кликнули
        Block currentBlock = e.getWhoClicked().getTargetBlockExact(5);
        ht = HallTable.getHTByBlock(currentBlock);
        if (ht == null) {
            // Если ht не находится, то инвентарь просто закрывается
            e.getWhoClicked().closeInventory();
            return;
        }

        // Основные переменные, которые нужны на протяжении всего класса
        clickInventory = e.getInventory();
        String htName = ht.getName();
        String htStatus = ht.getStatus();
        currentItem = e.getCurrentItem();
        displayNameOfCurrentItem = currentItem.getItemMeta().getDisplayName();
        materialTypeOfCurrentItem = currentItem.getType();
        Player player = (Player) e.getWhoClicked();
        playerInventory = player.getInventory();
        // Уровень постройки и какой вариант игрок выбрал при постановке - с ратушей или без
        int score = Main.getInstance().getUpgradeScore("halltables." + htName + ".upgrade.score");
        String buildingYesNo = buildingsDB.getString("halltables." + htName + ".building");

        // Определяем расположение таблички
        String signLocationStr = buildingsDB.getString("halltables." + htName + ".signLocation");
        Location signLocation = Main.getInstance().getLocation(signLocationStr);

        // Получаем айди государства
        String cityID = buildingsDB.getString("halltables." + htName + ".id");
        if (htStatus.equals("state")) {
            stateID = buildingsDB.getString("halltables." + htName + ".id");
        } else {
            stateID = databaseDB.getString("marinbay.city." + cityID + ".owner");
        }

        // Лоры для проверки и разных инвентарей
        loreOfHeadKick = getItemLoreForHallTable("civilianKick");
        loreOfHeadKickAlternative = getItemLoreForHallTable("civilianKicked");
        loreOfHeadInvite = getItemLoreForHallTable("invite");
        loreOfHeadInviteAlternative = getItemLoreForHallTable("invited");
        loreRaiseRankCitizen = InventoryHelper.getAddLinesToLoreRank("raise", "citizen",
                1);
        loreRaiseRankGuber = InventoryHelper.getAddLinesToLoreRank("raise", "guber",
                1);
        loreRaiseRankCloser1 = InventoryHelper.getAddLinesToLoreRank("raise", "closer",
                1);
        loreRaiseRankCloser2 = InventoryHelper.getAddLinesToLoreRank("raise", "closer",
                2);
        loreDowngradeRankCloser = InventoryHelper.getAddLinesToLoreRank("downgrade", "closer",
                1);
        loreDowngradeRankGuber = InventoryHelper.getAddLinesToLoreRank("downgrade", "guber",
                1);

        // Чтобы не было ошибок, проверяем на null
        if (e.getInventory().getItem(0) != null) {
            loreOf1Item = e.getInventory().getItem(0).getItemMeta().getLore();
        } else {
            // Пустышка
            loreOf1Item = new ArrayList<>();
            loreOf1Item.add("");
            loreOf1Item.add("");
            loreOf1Item.add("");
        }

        if (e.getInventory().getItem(11) != null) {
            nameOf11Item = e.getInventory().getItem(11).getItemMeta().getDisplayName();
        } else {
            nameOf11Item = "";
        }

        if (e.getInventory().getItem(25) != null) {
            nameOf25Item = e.getInventory().getItem(25).getItemMeta().getDisplayName();
        } else {
            nameOf25Item = "";
        }

        if (e.getInventory().getItem(12) != null) {
            nameOf12Item = e.getInventory().getItem(12).getItemMeta().getDisplayName();
        } else {
            nameOf12Item = "";
        }

        // Меню для выхода игрока из города
        if (displayNameOfCurrentItem.equals(ChatColor.RED + "Выйти из государства")) {
            String playerName = player.getName();
            MarinPlayer marinPlayer = new MarinPlayer(playerName);
            player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы покинули государство");

            // Если игрок еще и губернатор или приближённый, то проверяем есть ли у него город
            if (marinPlayer.isCloser() || marinPlayer.isGubernator()) {
                String placeCity = marinPlayer.getPlaceCity();
                if (placeCity != null) {
                    if (!placeCity.equals("NO")) {
                        // Если код здесь, значит у игрока есть свой город и нам надо его удалить
                        String nameCity = databaseDB.getString("marinbay.city." + placeCity + ".name");
                        HallTable htToDelete = HallTable.getHTByName(nameCity);
                        InventoryHelper.deleteCity(placeCity, htToDelete,1);
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Ваш город " + nameCity + " удалён");
                    }
                }
            }

            // Если государь в сети, пишем ему, что игрок вышел из госва
            String generalNick = marinPlayer.getMastersNick();
            if (generalNick != null) {
                Player generalPlayer = Bukkit.getPlayer(generalNick);
                if (generalPlayer != null) {
                    generalPlayer.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Игрок " +
                            ChatColor.AQUA + playerName + ChatColor.WHITE + " покинул ваше государство");
                }
            }

            // Удаляем из поля ranks
            marinPlayer.kickFromRanks();
            // Удаляем игрока
            databaseDB.removePlayer("marinbay.state." + stateID + ".citisens", playerName);

            e.setCancelled(true);
            player.closeInventory();
            return;
        }

        // (0) С нулевого уровня на первый
        if (score == 0 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 32 буханки хлеба") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            // Если это нужный инвентарь, то поднимать и класть в него ничего не выйдет

            // Если игрок кликнул по буханке с хлебом и у него есть в инвентаре 32 хлеба, то
            // 32 хлеба заберется у него из инвентаря, а в датабазу внесется информация, что
            // Слот заполнен

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 32 буханки хлеба")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.BREAD, 32, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 32 хлеба на постройку Ратуши");
                    // Открываем обновлённый инвентарь заместо старого
                    ht.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 32 буханок хлеба!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 128 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        128, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 128 древесины на постройку Ратуши");
                    ht.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 128 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 192 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 192, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 192 гладкого камня на постройку Ратуши");
                    ht.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 192 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 15 алмазов")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                    Material.DIAMOND, 15, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 15 алмазов на постройку Ратуши");
                    ht.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 15 алмазов!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                       displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                       displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Создать государство") || displayNameOfCurrentItem.equals(ChatColor.AQUA + "Создать город")) {
                if (ht != null) {
                    player.closeInventory();
                    ht.openInvRatushaChoice(player);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = InventoryHelper.getCreateButton(htStatus, "OK");
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = InventoryHelper.getCreateButton(htStatus, "NO");
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (1) С первого уровня на второй
        else if (score == 1 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 64 жареной свинины") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 64 жареной свинины")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.COOKED_PORKCHOP, 64, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 64 жареной свинины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 64 жареной свинины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 128 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 128, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 128 гладкого камня на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 128 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 192 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        192, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 192 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 192 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 20 алмазов")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.DIAMOND, 20, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 20 алмазов на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 20 алмазов!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha2.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (2) Со второго уровня на третий
        else if (score == 2 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 64 жареной свинины") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 64 жареной свинины")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.COOKED_PORKCHOP, 64, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 64 жареной свинины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 64 жареной свинины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 192 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        192, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 192 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 192 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 256 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 256, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 256 гладкого камня на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 256 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 16 золотых блоков")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.GOLD_BLOCK, 16, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 16 золотых блоков на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 16 золотых блоков!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha3.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (3) С третьего уровня на чётвертый
        else if (score == 3 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 16 тыквенных пирогов") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 16 тыквенных пирогов")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.PUMPKIN_PIE, 16, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 16 тыквенных пирогов на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 16 тыквенных пирогов!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 320 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        320, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 320 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 320 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 256 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 256, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 256 гладкого камня на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 256 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 24 золотых блока")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.GOLD_BLOCK, 24, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 24 золотых блока на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 24 золотых блоков!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha4.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (4) С четвёртого уровня на пятый
        else if (score == 4 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 24 тыквенных пирога") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 24 тыквенных пирога")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.PUMPKIN_PIE, 24, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 24 тыквенных пирога на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 24 тыквенных пирогов!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 320 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        320, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 320 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 320 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 320 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 320, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 320 гладкого камня на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 320 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 32 алмаза")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.DIAMOND, 32, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 32 алмаза на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 32 алмазов!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha5.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (5) С пятого уровня на шестой
        else if (score == 5 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.CAKE, 1, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 1 торт на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет торта!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 320 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        320, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 320 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 320 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 384 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 384, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 384 гладкого камня на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 384 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 32 алмаза")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.DIAMOND, 32, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 32 алмаза на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 32 алмазов!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha6.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (6) С шестого уровня на седьмой
        else if (score == 6 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.CAKE, 1, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 1 торт на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет торта!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 384 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        384, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 384 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 384 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 384 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 384, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 384 гладкого камня на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 384 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 48 алмазов")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.DIAMOND, 48, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 48 алмазов на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 48 алмазов!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha7.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (7) С седьмого уровня на восьмой
        else if (score == 7 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.CAKE, 1, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 1 торт на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет торта!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 384 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        384, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 384 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 384 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 448 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.SMOOTH_STONE, 448, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 448 гладкого камня на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 448 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 48 золотых блоков")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.GOLD_BLOCK, 48, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 48 золотых блоков на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 48 золотых блоков!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha8.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (8) С восьмого уровня на девятый
        else if (score == 8 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.CAKE, 1, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 1 торт на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет торта!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 384 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        384, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 384 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 384 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 448 серой шерсти")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.GRAY_WOOL, 448, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 448 серой шерсти на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 448 серой шерсти!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 48 золотых блоков")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.GOLD_BLOCK, 48, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 48 золотых блоков на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 48 золотых блоков!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha9.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (9) С девятого уровня на десятый
        else if (score == 9 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 1 торт")) {
                if (InventoryHelper.ifContainsGetItems("firstItem", htName, playerInventory, player,
                        Material.CAKE, 1, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 1 торт на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет торта!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 512 древесины")) {
                if (InventoryHelper.ifContainsGetWood("secondItem", htName, playerInventory, player,
                        512, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 512 древесины на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 512 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 512 серой шерсти")) {
                if (InventoryHelper.ifContainsGetItems("secondItem", htName, playerInventory, player,
                        Material.GRAY_WOOL, 512, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 512 серой шерсти на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 512 серой шерсти!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Требуется 64 золотых блока")) {
                if (InventoryHelper.ifContainsGetItems("thirdItem", htName, playerInventory, player,
                        Material.GOLD_BLOCK, 64, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 64 золотых блока на улучшение Ратуши");
                    ht.openUpgradeRatushaInventory(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 64 золотых блоков!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GOLD + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить ратушу")) {
                if (ht != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuilding(ht, "ratusha10.schem", true);
                    }

                    // Отдаём новый гексагон и перезагружаем предметы
                    InventoryHelper.getRandomHexagon(signLocation, ht, player);
                    InventoryHelper.toDefaultValuesItems(htName);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // Выбор ставить или не ставить ратушу
        else if (nameOf11Item.equals(ChatColor.AQUA + "Создать с ратушей")) {
            if (materialTypeOfCurrentItem == Material.HONEYCOMB) {
                player.closeInventory();
                boolean isState = htStatus.equals("state");

                // Проверяю, что гексагон, в котором находится табличка, не занят
                // Если такой гексагон уже есть в базе данных, значит он занят и создать государство не выйдет
                if (isState) {
                    if (databaseDB.isSet("marinbay.hexagons." + Main.grid.getHexagon(signLocation).toString())) {
                        player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Этот тайл уже занят");
                        e.setCancelled(true);
                        return;
                    }
                }
                // Проверяем, что в статусе у чела губер или клозер
                String pStat = databaseDB.getString("marinbay.player." + player.getName().toLowerCase() + ".status");
                // Если pStat null, то у чела нет статуса и государства, значит он делает холтейбл страны, а не города
                if (pStat != null) {
                    if (pStat.equals("general") || pStat.equals("citizen")) {
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете создать город, так как являетесь создателем государства, для этого у вас есть Губернаторы и Приближённые");
                        e.setCancelled(true);
                        return;
                    }
                }

                City city;
                if (isState) {
                    city = new City(ID.getCityID(htName), "state");
                } else {
                    city = new City(ID.getCityID(htName), "city");
                }
                if (city.exists()) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Такое государство или город уже существует");
                    e.setCancelled(true);
                    return;
                }

                if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Создать с ратушей")) {
                    // Здесь переносим постройку если выбран вариант с ратушей
                    // И записываем в базу данных, что у нас всё с постройкой
                    InventoryHelper.pasteNewBuilding(ht, "ratusha1.schem", true);
                    buildingsDB.write("halltables." + htName + ".building", "YES");
                } else {
                    buildingsDB.write("halltables." + htName + ".building", "NO");
                }

                MarinPlayer marinPlayer = new MarinPlayer(player.getName());
                // Указываю дефолтный колор, потом его можно будет изменить
                if (isState) {
                    city.createState(marinPlayer, htName, "#40AEED", signLocationStr);
                } else {
                    String stateID = marinPlayer.getStateID();
                    city.create(marinPlayer, htName, signLocationStr, stateID);
                }

                // Создаём регион, при этом обновляем stateID у ht
                // Причём, если это гос-во, то действуем как обычно
                // Но если это город, то мы обновляем регион именно государства
                ht.updStateID();
                if (isState) {
                    InventoryHelper.createRegionH(ht);
                } else {
                    // Получаем ht государства по ht города
                    String stateID = ht.getStateID();
                    String stringSign = databaseDB.getString("marinbay.state." + stateID + ".capital");
                    HallTable state_ht = new HallTable(Main.getInstance().getLocation(stringSign).getBlock(), databaseDB.getString("marinbay.state." + stateID + ".name"), false);

                    // 3-ей строчкой здесь мы создаём регион стола для этого города
                    InventoryHelper.removeRegion(state_ht.getName());
                    InventoryHelper.createRegionH(state_ht);
                    InventoryHelper.createRegion(MrTransliterator.rusToEng(ht.getName().toLowerCase().replace(" ", "")), ht.getCorner(1), ht.getCorner(2));
                }

                // Обнуляем все предметы, чтобы можно было заполнить их снова
                // А также записываем, что мы выбрали вариант с постройкой
                buildingsDB.write("halltables." + htName + ".upgrade.score", "1");
                InventoryHelper.toDefaultValuesItems(htName);

                if (isState) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Государство " + htName + " успешно создано!");
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Город " + htName + " успешно создан!");
                }
            }
            e.setCancelled(true);
            return;
        }

        // Меню государства
        if (nameOf11Item.equals(ChatColor.RED + "Игроки") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Город")) {
            if (displayNameOfCurrentItem.equals(ChatColor.RED + "Игроки")) {
                ht.openInvPlayersType(player);
            }

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Государство")) {
                ht.openInvChangeColorRatusha(player);
            }

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Город")) {
                if (score < 10) {
                    ht.openUpgradeRatushaInventory(player);
                }

                e.setCancelled(true);
                return;
            }

            if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Улучшения")) {
                ht.openInvUpgradesBuildings(player);
            }

            if (displayNameOfCurrentItem.equals(ChatColor.GRAY + "Удалить город")) {
                if (materialTypeOfCurrentItem == Material.HONEYCOMB) {
                    // Это первое нажатие
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы уверены? Повторное нажатие на кнопку удалит этот город и все принадлежащие ему тайлы и строения");
                    lore = InventoryHelper.getItemLoreForHallTable("deleteCityButton");
                    String displayName3 = InventoryHelper.getItemNameForHallTable(39, "BUTTON");
                    ItemStack deleteCityItem = InventoryHelper.getMenuItem(Material.LEATHER, 1, displayName3, lore, 1);
                    e.getInventory().setItem(26, deleteCityItem);
                }
                if (materialTypeOfCurrentItem == Material.LEATHER) {
                    // Это второе нажатие и удаление города
                    if (cityID != null) {
                        player.closeInventory();
                        InventoryHelper.deleteCity(cityID, ht, 1);
                        DynmapDrawer.redrawDynmap();

                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Город удалён");
                    }
                }
            }

            e.setCancelled(true);
            return;
        }
        // Меню "Ратуша" и "Изменить цвет"
        if (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Ратуша")) {
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Ратуша")) {
                if (score < 10) {
                    ht.openUpgradeRatushaInventory(player);
                }
            }
            if (displayNameOfCurrentItem.equals(ChatColor.RED + "Цвет государства")) {
                ht.openInvChangeColor(player);
            }

            e.setCancelled(true);
            return;
        }
        // Меню выбора цвета государства
        if (nameOf11Item.equals(ChatColor.BLUE + "Синий")) {
            // Проверка, что игрок не нажимал на кнопку за последние 3 дня
            if (!InventoryHelper.checkThreeDays(player)) {
                e.setCancelled(true);
                return;
            }

            if (displayNameOfCurrentItem.equals(ChatColor.WHITE + "Белый")) {
                City.changeColor(player, "#FFFAFA");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Оранжевый")) {
                City.changeColor(player, "#FF9900");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Сиреневый")) {
                City.changeColor(player, "#CDA4DE");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_AQUA + "Светло-синий")) {
                City.changeColor(player, "#40AEED");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.YELLOW + "Жёлтый")) {
                City.changeColor(player, "#FFFF33");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Зелёный")) {
                City.changeColor(player, "#19E619");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.LIGHT_PURPLE + "Розовый")) {
                City.changeColor(player, "#FFC0CB");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_GRAY + "Серый")) {
                City.changeColor(player, "#808080");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.GRAY + "Светло-серый")) {
                City.changeColor(player, "#C0C0C0");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Бирюзовый")) {
                City.changeColor(player, "#1CC5BF");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Фиолетовый")) {
                City.changeColor(player, "#9932CC");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.BLUE + "Синий")) {
                City.changeColor(player, "#3333FF");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_RED + "Коричневый")) {
                City.changeColor(player, "#964B00");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_GREEN + "Тёмно-зелёный")) {
                City.changeColor(player, "#228B22");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Красный")) {
                City.changeColor(player, "#FF2400");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_GRAY + "Чёрный")) {
                City.changeColor(player, "#1A1A1A");
                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Отлично! Цвет поставлен");
            }

            // Записываем когда игрок нажимал на кнопку
            colorsChangeDB.write(player.getName(), String.valueOf(System.currentTimeMillis()));

            e.setCancelled(true);
            return;
        }
        // Меню выбора типа игроков
        if (nameOf11Item.equals(ChatColor.AQUA + "Граждане")) {
            if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Граждане")) {
                ht.openInvCivillianPlayer(player);
            }

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Свободные игроки")) {
                ht.openInvFreePlayer(player);
            }
            e.setCancelled(true);
            return;
        }
        // Инвентарь выбора - Повышение или Понижение в должности игрока
        if (nameOf25Item.equals(ChatColor.RED + "Понижение")) {
            if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Повышение")) {
                ht.openInvRankRaise(player);
            }
            if (displayNameOfCurrentItem.equals(ChatColor.RED + "Понижение")) {
                ht.openInvRankDowngrade(player);
            }

            e.setCancelled(true);
            return;
        }
        // Меню выбора действия над гражданином государства
        if (nameOf11Item.equals(ChatColor.GOLD + "Повышение")) {
            if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Повышение")) {
                ht.openInvRank(player);
            }
            if (displayNameOfCurrentItem.equals(ChatColor.RED + "Информация об игроках")) {
                ht.openInvInfoCivilian(player);
            }
            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Исключить из государства")) {
                ht.openInvKickCivilian(player);
            }
            e.setCancelled(true);
            return;
        }
        // Инвентари исключения, информации и повышения рангов игроков
        if (loreOf1Item.equals(loreOfHeadKick) || loreOf1Item.equals(loreOfHeadKickAlternative)) {
            if (loreOf1Item.equals(loreOfHeadKick) && materialTypeOfCurrentItem == Material.PLAYER_HEAD) {
                String playerName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                String stateName = databaseDB.getString("marinbay.state." + stateID + ".name");
                MarinPlayer marinPlayer = new MarinPlayer(playerName);

                // Удаляем этого игрока из должности, которую он занимает в государстве
                marinPlayer.kickFromRanks();
                // Удаляем игрока из строки citizens и убираем его state
                databaseDB.removePlayer("marinbay.state." + stateID + ".citisens", playerName);

                Player player1 = Bukkit.getPlayer(playerName);
                if (player1 != null) {
                    player1.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы исключены из государства " + stateName);
                } else {
                    // Если игрок не в сети, то записываем townKicked, чтобы когда он появится
                    // ему написало что он выгнан
                    databaseDB.write("marinbay.player." + playerName.toLowerCase() + ".townKicked", stateName);
                }

                // Проверяем был ли у человека город
                String placeCity = marinPlayer.getPlaceCity();
                if (placeCity != null) {
                    // Сюда попадаем, если у него есть графа placeCity
                    if (!placeCity.equals("NO")) {
                        // Да, у игрока есть город и его надо удалить
                        String cityName = databaseDB.getString("marinbay.city." + placeCity + ".name");
                        HallTable toDelete = HallTable.getHTByName(cityName);
                        InventoryHelper.deleteCity(placeCity, toDelete, 1);
                        DynmapDrawer.redrawDynmap();
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Город " + cityName + " этого игрока удалён");
                    }
                }

                // Удаляем игрока из региона
                String regionName = MrTransliterator.rusToEng(stateName.toLowerCase().replace(" ", ""));
                InventoryHelper.removeFromRegion(regionName, playerName);

                ItemMeta skullmeta = currentItem.getItemMeta();
                skullmeta.setDisplayName(ChatColor.GRAY + playerName);
                skullmeta.setLore(getItemLoreForHallTable("civilianKicked"));

                currentItem.setItemMeta(skullmeta);
            }
            e.setCancelled(true);
            return;
        }
        // Нажимаем на кнопку далее
        if (nameOf25Item.equals(ChatColor.GRAY + "Далее")) {
            if (materialTypeOfCurrentItem == Material.HONEYCOMB) {
                ht.open2InvKickCivilian(player);
            } else if (materialTypeOfCurrentItem == Material.LEATHER) {
                ht.open2InvInfoCivilian(player);
            } else if (materialTypeOfCurrentItem == Material.SUGAR) {
                ht.open2InvInfoFree(player);
            } else if (materialTypeOfCurrentItem == Material.CHORUS_FRUIT) {
                ht.open2InvInviteFree(player);
            } else if (materialTypeOfCurrentItem == Material.GUNPOWDER) {
                ht.open2InvUpgradesBuildings(player);
            } else if (materialTypeOfCurrentItem == Material.FEATHER) {
                ht.open2InvRankRaise(player);
            } else if (materialTypeOfCurrentItem == Material.BOOK) {
                ht.open2InvRankDowngrade(player);
            }
            e.setCancelled(true);
            return;
        }
        // Информация об игроках
        if (loreOf1Item.get(0).equals("Информация об игроке:")) {
            e.setCancelled(true);
            return;
        }
        // Инвентарь инвайта игрока
        if (loreOf1Item.equals(loreOfHeadInvite) || loreOf1Item.equals(loreOfHeadInviteAlternative)) {
            if (loreOf1Item.equals(loreOfHeadInvite) && materialTypeOfCurrentItem == Material.PLAYER_HEAD) {
                String playerName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                Player player1 = Bukkit.getPlayer(playerName);
                if (player1 == null) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Игрок не в сети");
                    e.setCancelled(true);
                    return;
                }
                // Проверяем есть ли место для этого игрока в государстве
                if (!City.canYouInviteNewCitizens(stateID)) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете приглашать новых игроков, так как в государстве не хватает места");
                    e.setCancelled(true);
                    return;
                }
                // Проверка, что твоё государство не приглашало этого игрока за последние 3 часа
                if (!InventoryHelper.checkIf3HoursPassed(stateID, player1)) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы уже отправляли приглашение этому игроку за последние 3 часа");
                    e.setCancelled(true);
                    return;
                }

                // Добавляем строку инвайт человеку и записываем время инвайта в датабазу
                databaseDB.write("marinbay.player." + playerName.toLowerCase() + ".invitesFromCities", stateID);
                inviteDB.write(playerName + "." + stateID, "" + System.currentTimeMillis());

                String generalLowerName = databaseDB.getString("marinbay.state." + stateID + ".general");
                String generalName = databaseDB.getString("marinbay.player." + generalLowerName + ".name");

                InventoryHelper.sendInviteMessage(generalName, stateID, player1);

                ItemMeta skullmeta = currentItem.getItemMeta();
                skullmeta.setDisplayName(ChatColor.GRAY + playerName);
                skullmeta.setLore(getItemLoreForHallTable("invited"));

                currentItem.setItemMeta(skullmeta);
            }
            e.setCancelled(true);
            return;
        }
        // Инвентарь свободных игроков - пригласить и информация
        if (nameOf11Item.equals(ChatColor.GOLD + "Пригласить в государство")) {
            if (displayNameOfCurrentItem.equals(ChatColor.GOLD + "Пригласить в государство")) {
                ht.openInvInviteFree(player);
            }

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Информация об игроке")) {
                ht.openInvInfoFree(player);
            }
            e.setCancelled(true);
            return;
        }
        // Инвентарь построек-улучшений
        if (loreOf1Item.get(0).equalsIgnoreCase(ChatColor.WHITE + "Информация о ратуше") ||
                loreOf1Item.get(0).equalsIgnoreCase(ChatColor.WHITE + "Информация о казарме")) {
            e.setCancelled(true);
            return;
        }
        // Инвентарь повышения должности игрока
        if (loreOf1Item.size() >= 3) {
            if (loreOf1Item.get(2).equals(loreRaiseRankCitizen.get(2))) {
                List<String> loreOfCurrenItem = currentItem.getItemMeta().getLore();
                String playerName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                String playerLower = playerName.toLowerCase();
                Player newGos = Bukkit.getPlayer(playerName);
                MarinPlayer marinPlayer = new MarinPlayer(player);

                // Повышение до губернатора
                if (loreOfCurrenItem.get(3).equals(loreRaiseRankCitizen.get(3))) {
                    databaseDB.write("marinbay.player." + playerLower + ".status", "guber");
                    databaseDB.removeOneElement("marinbay.state." + stateID + ".ranks.citizen", playerName, ", ");
                    databaseDB.addOneElement("marinbay.state." + stateID + ".ranks.guber", playerName);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Игрок " + playerName + " повышен до должности: " + ChatColor.AQUA + "Губернатор");
                    ht.openInvRankRaise(player);

                    // Если этот игрок в сети, то отправляем ему сообщение, что теперь он
                    if (newGos != null) {
                        newGos.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Поздравляем! " +
                                "Вы повышены до должности " + ChatColor.AQUA + " Губернатор");
                    }
                }
                // Повышение до приближённого
                else if (loreOfCurrenItem.get(3).equals(loreRaiseRankGuber.get(3))) {
                    if (!marinPlayer.isGeneral()) {
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете" +
                                " повышать до Приближённых, так как вы не глава государства");
                        e.setCancelled(true);
                        return;
                    }

                    databaseDB.write("marinbay.player." + playerLower + ".status", "closer");
                    databaseDB.removeOneElement("marinbay.state." + stateID + ".ranks.guber", playerName, ", ");
                    databaseDB.addOneElement("marinbay.state." + stateID + ".ranks.closer", playerName);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Игрок " + playerName + " повышен до должности: " + ChatColor.AQUA + "Приближённый Государя");
                    ht.openInvRankRaise(player);

                    // Если этот игрок в сети, то отправляем ему сообщение, что теперь он
                    if (newGos != null) {
                        newGos.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Поздравляем! " +
                                "Вы повышены до должности " + ChatColor.AQUA + " Приближённый Государя");
                    }
                }
                // Повышение до короля - 1
                else if (loreOfCurrenItem.get(5).equals(loreRaiseRankCloser1.get(5))) {
                    if (!marinPlayer.isGeneral()) {
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете" +
                                " повысить игрок до Государя, так как вы не глава государства");
                        e.setCancelled(true);
                        return;
                    }
                    // Строка, чтобы приближённый не мог повысить сам себя до короля
                    if (playerName.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете" +
                                " повысить самого себя");
                        e.setCancelled(true);
                        return;
                    }

                    // Первое нажатие
                    String display = currentItem.getItemMeta().getDisplayName();

                    SkullMeta skull = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
                    if (Bukkit.getPlayer(playerLower) != null) {
                        skull.setOwningPlayer(Bukkit.getPlayer(playerLower));
                    }
                    skull.setDisplayName(display);
                    skull.setLore(loreRaiseRankCloser2);
                    ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                    item.setItemMeta(skull);

                    clickInventory.setItem(e.getSlot(), item);
                }
                // Повышение до короля - 2
                else if (loreOfCurrenItem.get(5).equals(loreRaiseRankCloser2.get(5))) {
                    // Второе нажатие
                    String oldGeneral = ht.getGeneral();
                    String oldGeneralName = databaseDB.getString("marinbay.player." + oldGeneral + ".name");
                    databaseDB.write("marinbay.player." + oldGeneral + ".status", "guber");
                    databaseDB.write("marinbay.player." + playerLower + ".status", "general");
                    databaseDB.write("marinbay.state." + stateID + ".general", playerLower);
                    databaseDB.removeOneElement("marinbay.state." + stateID + ".ranks.closer", playerName, ", ");
                    // Ставим нового короля в начало строки citisens
                    databaseDB.castlingElement("marinbay.state." + stateID + ".citisens", playerName);
                    // Старому королю даём ранк guber
                    databaseDB.addOneElement("marinbay.state." + stateID + ".ranks.guber", oldGeneralName);

                    ht.setCreator(playerName);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Поздравляем! " +
                            "Теперь игрок " + playerName + " - глава государства!");
                    player.closeInventory();

                    // Если этот игрок в сети, то отправляем ему сообщение, что теперь он Государь
                    if (newGos != null) {
                        String stateName = databaseDB.getString("marinbay.state." + stateID + ".name");
                        newGos.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Поздравляем! " +
                                "Теперь вы глава государства " + stateName);
                    }
                }

                e.setCancelled(true);
                return;
            }
        }
        // Инвентарь понижения должности игрока
        if (loreOf1Item.size() >= 3) {
            if (loreOf1Item.get(2).equals(loreDowngradeRankCloser.get(2))) {
                List<String> loreOfCurrenItem = currentItem.getItemMeta().getLore();
                String playerName = ChatColor.stripColor(currentItem.getItemMeta().getDisplayName());
                String playerLower = playerName.toLowerCase();
                MarinPlayer marinPlayer = new MarinPlayer(playerName);
                Player newGos = Bukkit.getPlayer(playerName);

                // Понижение до губернатора
                if (loreOfCurrenItem.get(3).equals(loreDowngradeRankCloser.get(3))) {
                    if (!marinPlayer.isGeneral()) {
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете" +
                                " понизить другого Приближённого, так как вы не глава государства");
                        e.setCancelled(true);
                        return;
                    }
                    // Строка, чтобы приближённый не мог повысить сам себя до короля
                    if (playerName.equalsIgnoreCase(player.getName())) {
                        player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы не можете" +
                                " понизить самого себя");
                        e.setCancelled(true);
                        return;
                    }

                    databaseDB.write("marinbay.player." + playerLower + ".status", "guber");
                    databaseDB.removeOneElement("marinbay.state." + stateID + ".ranks.closer", playerName, ", ");
                    databaseDB.addOneElement("marinbay.state." + stateID + ".ranks.guber", playerName);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Игрок " + playerName + " понижен до должности: " + ChatColor.AQUA + "Губернатор");
                    ht.openInvRankDowngrade(player);

                    // Если этот игрок в сети, то отправляем ему сообщение, что теперь он
                    if (newGos != null) {
                        newGos.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE +
                                "Вас понизили до должности " + ChatColor.AQUA + "Губернатор");
                    }
                }
                // Понижение до гражданина
                if (loreOfCurrenItem.get(3).equals(loreDowngradeRankGuber.get(3))) {
                    databaseDB.write("marinbay.player." + playerLower + ".status", "citizen");
                    databaseDB.removeOneElement("marinbay.state." + stateID + ".ranks.guber", playerName, ", ");
                    databaseDB.addOneElement("marinbay.state." + stateID + ".ranks.citizen", playerName);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Игрок " + playerName + " понижен до должности: " + ChatColor.AQUA + "Гражданин");
                    ht.openInvRankDowngrade(player);

                    // Если этот игрок в сети, то отправляем ему сообщение, что теперь он
                    if (newGos != null) {
                        newGos.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE +
                                "Вас понизили до должности " + ChatColor.AQUA + "Гражданин");
                    }

                    String placeCity = marinPlayer.getPlaceCity();
                    if (placeCity != null) {
                        if (!placeCity.equals("NO")) {
                            // Если код здесь, значит у игрока есть какой-то город и нам надо
                            // Его удалить
                            String cityName = databaseDB.getString("marinbay.city." + placeCity + ".name");
                            HallTable toDelete = HallTable.getHTByName(cityName);
                            InventoryHelper.deleteCity(placeCity, toDelete, 1);
                            DynmapDrawer.redrawDynmap();
                            player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Город " + cityName + " удалён");
                        }
                    }
                }
                e.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void barrackClick(InventoryClickEvent e) {
        // Если слоты 11, 12 и 0 пустуют, то это точно не наш инвентарь
        try {
            if (e.getInventory().getItem(11) == null) {
                return;
            }
        } catch (IndexOutOfBoundsException o){
            // А вдруг у нас инвентарь не такой большой, чтобы содержать слот 11?
            return;
        }
        // Если предмет на который кликнули нулл то возвращаем
        if (e.getCurrentItem() == null) {
            return;
        }
        // Не очень удобная, но действенная конструкция
        if (e.getInventory().getItem(11) == null) {
            return;
        }
        if (e.getInventory().getItem(11).getType() != Material.HONEYCOMB) {
            return;
        }

        // Если в 26 слоте у нас не кнопка "Создать казарму" или "Вы пока не можете...",
        // То это точно не казарма и возвращаем функцию
        if (e.getInventory().getItem(26) != null) {
            String display26 = e.getInventory().getItem(26).getItemMeta().getDisplayName();
            if (!(display26.equals(ChatColor.AQUA + "Создать казарму") ||
                    display26.equals(ChatColor.AQUA + "Вы пока не можете создать казарму") ||
                    display26.equals(ChatColor.AQUA + "Улучшить казарму") ||
                    display26.equals(ChatColor.AQUA + "Вы пока не можете улучшить казарму"))) {
                return;
            }
        } else {
            String display11 = e.getInventory().getItem(11).getItemMeta().getDisplayName();
            if (!display11.equals(ChatColor.BLUE + "Создать с казармой")) {
                return;
            }
        }

        // Получаем казарму, по которой кликнули
        Block currentBlock = e.getWhoClicked().getTargetBlockExact(5);
        Barrack barrack = Barrack.getBarrackByBlock(currentBlock);
        if (barrack == null) {
            // Если barrack не находится, то инвентарь просто закрывается
            e.getWhoClicked().closeInventory();
            return;
        }

        // Основные переменные, которые нужны на протяжении всего класса
        clickInventory = e.getInventory();
        String barrackName = barrack.getName();
        currentItem = e.getCurrentItem();
        materialTypeOfCurrentItem = currentItem.getType();
        displayNameOfCurrentItem = currentItem.getItemMeta().getDisplayName();
        Player player = (Player) e.getWhoClicked();
        playerInventory = player.getInventory();
        // Уровень постройки и какой вариант игрок выбрал при постановке - с казармой или без
        int score = Main.getInstance().getUpgradeScore("barracks." + barrackName + ".upgrade.score");
        String buildingYesNo = buildingsDB.getString("barracks." + barrackName + ".building");

        // Определяем расположение таблички
        String signLocationStr = buildingsDB.getString("barracks." + barrackName + ".signLocation");
        Location signLocation = Main.getInstance().getLocation(signLocationStr);

        nameOf11Item = e.getInventory().getItem(11).getItemMeta().getDisplayName();

        // (0) С нулевого уровня на первый
        if (score == 0 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 64 стрелы") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            // Если это нужный инвентарь, то поднимать и класть в него ничего не выйдет

            // Если игрок кликнул по буханке с хлебом и у него есть в инвентаре 32 хлеба, то
            // 32 хлеба заберется у него из инвентаря, а в датабазу внесется информация, что
            // Слот заполнен

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 64 стрелы")) {
                if (InventoryHelper.ifContainsGetItemsB("firstItem", barrackName, playerInventory, player,
                        Material.ARROW, 64, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 64 стрелы на постройку Казармы");
                    // Открываем обновлённый инвентарь заместо старого
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 64 стрел!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 128 древесины")) {
                if (InventoryHelper.ifContainsGetWoodB("secondItem", barrackName, playerInventory, player,
                        128, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 128 древесины на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 128 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 128 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItemsB("secondItem", barrackName, playerInventory, player,
                        Material.SMOOTH_STONE, 128, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 128 гладкого камня на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 128 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.YELLOW + "Требуется 32 снопа сена")) {
                if (InventoryHelper.ifContainsGetItemsB("thirdItem", barrackName, playerInventory, player,
                        Material.HAY_BLOCK, 32, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 32 снопа сена на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 32 снопов сена!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.YELLOW + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Создать казарму")) {
                if (barrack != null) {
                    player.closeInventory();
                    barrack.openInvBarrackChoice(player);
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Создать казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.AQUA + "Вы пока не можете создать казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (1) С первого уровня на второй
        if (score == 1 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 128 стрел") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            // Если это нужный инвентарь, то поднимать и класть в него ничего не выйдет

            // Если игрок кликнул по буханке с хлебом и у него есть в инвентаре 32 хлеба, то
            // 32 хлеба заберется у него из инвентаря, а в датабазу внесется информация, что
            // Слот заполнен

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 128 стрел")) {
                if (InventoryHelper.ifContainsGetItemsB("firstItem", barrackName, playerInventory, player,
                        Material.ARROW, 128, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 128 стрел на постройку Казармы");
                    // Открываем обновлённый инвентарь заместо старого
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 128 стрел!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 128 древесины")) {
                if (InventoryHelper.ifContainsGetWoodB("secondItem", barrackName, playerInventory, player,
                        128, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 128 древесины на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 128 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 192 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItemsB("secondItem", barrackName, playerInventory, player,
                        Material.SMOOTH_STONE, 192, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 192 гладкого камня на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 192 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.YELLOW + "Требуется 48 снопов сена")) {
                if (InventoryHelper.ifContainsGetItemsB("thirdItem", barrackName, playerInventory, player,
                        Material.HAY_BLOCK, 48, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 48 снопов сена на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 48 снопов сена!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.YELLOW + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить казарму")) {
                if (barrack != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuildingB(barrack, "kazarma2.schem", true);
                    }

                    // Записываем новый score
                    int new_score = score + 1;
                    buildingsDB.write("barracks." + barrackName + ".upgrade.score", String.valueOf(new_score));

                    // Добавляем жителя
                    barrack.plusOneCitizen();

                    // Перезагружаем предметы
                    InventoryHelper.toDefaultValuesItemsB(barrackName);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Поздравляем с улучшением казармы!");
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Создать казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.AQUA + "Вы пока не можете создать казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // (2) Со второго уровня на третий
        if (score == 2 && (nameOf11Item.equals(ChatColor.DARK_PURPLE + "Требуется 192 стрелы") || nameOf11Item.equals(ChatColor.DARK_PURPLE + "Выполнено"))) {
            // Если это нужный инвентарь, то поднимать и класть в него ничего не выйдет

            // Если игрок кликнул по буханке с хлебом и у него есть в инвентаре 32 хлеба, то
            // 32 хлеба заберется у него из инвентаря, а в датабазу внесется информация, что
            // Слот заполнен

            if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Требуется 192 стрелы")) {
                if (InventoryHelper.ifContainsGetItemsB("firstItem", barrackName, playerInventory, player,
                        Material.ARROW, 192, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.firstItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 192 стрелы на постройку Казармы");
                    // Открываем обновлённый инвентарь заместо старого
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 192 стрел!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.RED + "Требуется 192 древесины")) {
                if (InventoryHelper.ifContainsGetWoodB("secondItem", barrackName, playerInventory, player,
                        192, "NOT0")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.secondItem", "NOT1");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 192 древесины на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 192 древесины!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.GREEN + "Требуется 192 гладкого камня")) {
                if (InventoryHelper.ifContainsGetItemsB("secondItem", barrackName, playerInventory, player,
                        Material.SMOOTH_STONE, 192, "NOT1")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.secondItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 192 гладкого камня на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 192 гладкого камня!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.YELLOW + "Требуется 64 снопа сена")) {
                if (InventoryHelper.ifContainsGetItemsB("thirdItem", barrackName, playerInventory, player,
                        Material.HAY_BLOCK, 64, "NOT")) {
                    // Сюда пишем то, на что должна изменится строка в датабазе
                    buildingsDB.write("barracks." + barrackName + ".upgrade.thirdItem", "OK");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы положили 64 снопа сена на постройку Казармы");
                    barrack.open(player);
                } else {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " У вас в инвентаре нет 64 снопов сена!");
                }
            } else if (displayNameOfCurrentItem.equals(ChatColor.DARK_PURPLE + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.RED + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.GREEN + "Выполнено") ||
                    displayNameOfCurrentItem.equals(ChatColor.YELLOW + "Выполнено")) {
                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вы уже заполнили этот слот");
            } else if (displayNameOfCurrentItem.equals(ChatColor.AQUA + "Улучшить казарму")) {
                if (barrack != null) {
                    player.closeInventory();

                    // Здесь переносим постройку если игрок выбрал вариант с постройкой
                    if (buildingYesNo.equals("YES")) {
                        InventoryHelper.pasteNewBuildingB(barrack, "kazarma3.schem", true);
                    }

                    // Записываем новый score
                    int new_score = score + 1;
                    buildingsDB.write("barracks." + barrackName + ".upgrade.score", String.valueOf(new_score));

                    // Добавляем жителя
                    barrack.plusOneCitizen();

                    // Перезагружаем предметы
                    InventoryHelper.toDefaultValuesItemsB(barrackName);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Поздравляем! Вы достигли максимального уровня казармы!");
                }
            }

            // Устанавливаем название кнопки создания
            // Если все 3 слота положены, то кнопка должна измениться
            firstItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.firstItem");
            secondItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.secondItem");
            thirdItemOKNOT = buildingsDB.getString("barracks." + barrackName + ".upgrade.thirdItem");

            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Создать казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.AQUA + "Вы пока не можете создать казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }
            InventoryHelper.setItemForManySlotes(slotesCreateButton, clickInventory, buttonItem);

            e.setCancelled(true);
            return;
        }

        // Выбор ставить или не ставить казарму
        else if (nameOf11Item.equals(ChatColor.BLUE + "Создать с казармой")) {
            if (materialTypeOfCurrentItem == Material.HONEYCOMB) {
                player.closeInventory();

                // Проверяю, что гексагон, в котором находится табличка, не занят
                // Если такой гексагон уже есть в базе данных, значит он занят и создать государство не выйдет
                if (databaseDB.isSet("marinbay.hexagons." + Main.grid.getHexagon(signLocation).toString() + ".building")) {
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Этот тайл уже занят другой постройкой");
                    e.setCancelled(true);
                    return;
                }

                if (displayNameOfCurrentItem.equals(ChatColor.BLUE + "Создать с казармой")) {
                    // Здесь переносим постройку если выбран вариант с ратушей
                    // И записываем в базу данных, что у нас всё с постройкой
                    InventoryHelper.pasteNewBuildingB(barrack, "kazarma1.schem", true);
                    buildingsDB.write("barracks." + barrackName + ".building", "YES");
                } else {
                    buildingsDB.write("barracks." + barrackName + ".building", "NO");
                }

                // Создаём регион
                InventoryHelper.createRegionB(barrack);

                // Обнуляем все предметы, чтобы можно было заполнить их снова
                // А также записываем, что мы выбрали вариант с постройкой
                buildingsDB.write("barracks." + barrackName + ".upgrade.score", "1");
                InventoryHelper.toDefaultValuesItemsB(barrackName);
                // Записываем информацию о казарме в гексагон
                String hex = Main.grid.getHexagon(signLocation).toString();
                databaseDB.write("marinbay.hexagons." + hex + ".building", "barrack");
                databaseDB.write("marinbay.hexagons." + hex + ".barrackName", barrackName);
                // Добавляем жителя к государству
                barrack.plusOneCitizen();

                player.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Казарма успешно создана!");
            }
            e.setCancelled(true);
            return;
        }
    }
}
