package marintileeditor.main.TileManager.MaryManager.Inventory;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import library.Hexagon;
import library.HexagonComponents.HexagonSide;
import library.Point;
import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.HallTable.HallTable;
import marintileeditor.main.TileManager.MaryManager.MrTransliterator;
import marintileeditor.main.TileManager.MaryManager.SchematicPaster;
import marintileeditor.main.TileManager.TileManager.City.City;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import marintileeditor.main.TileManager.TileManager.Player.MarinPlayer;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

import net.md_5.bungee.api.chat.TextComponent;

import static marintileeditor.main.TileManager.TileManager.Main.Main.grid;

// Обработан
public class InventoryHelper {

    // Тех. часть
    public static Logger log = Bukkit.getLogger();

    // Базы данных
    public static Database buildingsDB = Database.BUILDINGS_DATABASE;
    public static Database databaseDB = Database.STAT_DATABASE;
    public static Database colorsChangeDB = Database.COLOR_CHANGE_DATABASE;
    public static Database inviteDB = Database.INVITE_DATABASE;
    public static Database ilDB = Database.ID_LINK_DATABASE;

    // Материалы деревьев
    public static Material[] woods = {
            Material.OAK_LOG, Material.ACACIA_LOG,
            Material.BIRCH_LOG, Material.DARK_OAK_LOG,
            Material.JUNGLE_LOG, Material.SPRUCE_LOG
    };

    // Переменные для изымания предметов
    public static String okNOTitem;
    public static String displayName;
    public static ArrayList<String> lore;
    public static ItemStack Item;

    // Предметы для отображения улучшений государства с текстурами
    public static ItemStack ratushaState = getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzA3OGM2OGY2Zjk2NjkzNzBlYTM5YmU3Mjk0NWEzYTg2ODhlMGUwMjRlNWQ2MTU4ZmQ4NTRmYjJiODBmYiJ9fX0=");
    public static ItemStack ratushaCity = getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2FkNTdkMWY2MDQ3OGFmNmFlMWI2YjFjMjhjZWU1MzRkNWRiYjhjOGNhYjg3OTYzZTljMmVmMGM2YWM1OCJ9fX0=");
    public static ItemStack kazarm = getCustomSkull("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc4N2I3YWZiNWE1OTk1Mzk3NWJiYTI0NzM3NDliNjAxZDU0ZDZmOTNjZWFjN2EwMmFjNjlhYWU3ZjliOCJ9fX0=");

    // Лор для всех предметов в меню выбора цветов
    public static ArrayList<String> loreColor = InventoryHelper.getItemLoreForHallTable("color");
    // Все цвета
    public static ItemStack white = InventoryHelper.getMenuItem(Material.WHITE_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "WHITE"), loreColor, 1);
    public static ItemStack orange = InventoryHelper.getMenuItem(Material.ORANGE_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "ORANGE"), loreColor, 1);
    public static ItemStack siren = InventoryHelper.getMenuItem(Material.MAGENTA_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "SIRENEVIY"), loreColor, 1);
    public static ItemStack light_blue = InventoryHelper.getMenuItem(Material.LIGHT_BLUE_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "LIGHT_BLUE"), loreColor, 1);
    public static ItemStack yellow = InventoryHelper.getMenuItem(Material.YELLOW_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "YELLOW"), loreColor, 1);
    public static ItemStack green = InventoryHelper.getMenuItem(Material.LIME_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "GREEN"), loreColor, 1);
    public static ItemStack pink = InventoryHelper.getMenuItem(Material.PINK_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "PINK"), loreColor, 1);
    public static ItemStack gray = InventoryHelper.getMenuItem(Material.GRAY_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "GRAY"), loreColor, 1);
    public static ItemStack light_gray = InventoryHelper.getMenuItem(Material.LIGHT_GRAY_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "LIGHT_GRAY"), loreColor, 1);
    public static ItemStack birusa = InventoryHelper.getMenuItem(Material.CYAN_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "BIRUSA"), loreColor, 1);
    public static ItemStack purple = InventoryHelper.getMenuItem(Material.PURPLE_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "PURPLE"), loreColor, 1);
    public static ItemStack blue = InventoryHelper.getMenuItem(Material.BLUE_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "BLUE"), loreColor, 1);
    public static ItemStack brown = InventoryHelper.getMenuItem(Material.BROWN_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "BROWN"), loreColor, 1);
    public static ItemStack dark_green = InventoryHelper.getMenuItem(Material.GREEN_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "DARK_GREEN"), loreColor, 1);
    public static ItemStack red = InventoryHelper.getMenuItem(Material.RED_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "RED"), loreColor, 1);
    public static ItemStack black = InventoryHelper.getMenuItem(Material.BLACK_CONCRETE, 1,
            InventoryHelper.getItemNameForHallTable(10, "BLACK"), loreColor, 1);

    // Получаем предмет для наших инвентарей
    public static ItemStack getMenuItem(Material mat, int matAmount, String disName, ArrayList<String> lore, int cmDATA) {
        ItemStack item = new ItemStack(mat, matAmount);
        ItemMeta meta = item.getItemMeta();

        // Меняем имя предмету
        meta.setDisplayName(disName);
        // Меняем лор
        meta.setLore(lore);
        meta.setCustomModelData(cmDATA);
        item.setItemMeta(meta);
        return item;
    }

    // По-умному удаляет предметы, которые ты вписал
    public static void smartRemoveItem(Player player, Material material, int howManyRemove) {
        int itemsToRemove = howManyRemove;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                if (item.getType().equals(material)) {
                    // Количество предметов до манипуляций
                    int preAmount = item.getAmount();
                    int newAmount = Math.max(0, preAmount - itemsToRemove);
                    item.setAmount(newAmount);

                    itemsToRemove = Math.max(0, itemsToRemove - preAmount);
                    if (itemsToRemove == 0) {
                        break;
                    }
                }
            }
        }
    }

    // Функция, необходимая, чтобы узнать содержит ли инвентарь какой-то тип дерева
    // Сделано для универсальности древесины, чтобы можно было закинуть в апгрейд и дуб, и берёзу
    public static boolean doesInventoryContainsWood(Player player, Inventory inventory, int amount, boolean forceFalse, String cityName) {
        if (!forceFalse) {
            return false;
        }
        for (int i = 0; i < woods.length; i++) {
            if (inventory.contains(woods[i], amount)) {
                buildingsDB.write("halltables." + cityName + ".upgrade.secondItemWood", woods[i].toString());
                smartRemoveItem(player, woods[i], amount);
                return true;
            }
        }
        return false;
    }
    // Для бараков
    public static boolean doesInventoryContainsWoodB(Player player, Inventory inventory, int amount, boolean forceFalse, String barrackName) {
        if (forceFalse == false) {
            return false;
        }
        for (int i = 0; i < woods.length; i++) {
            if (inventory.contains(woods[i], amount)) {
                buildingsDB.write("barracks." + barrackName + ".upgrade.secondItemWood", woods[i].toString());
                smartRemoveItem(player, woods[i], amount);
                return true;
            }
        }
        return false;
    }

    // Меняет вещи сразу для нескольких слотов, а не только одного
    public static void setItemForManySlotes(int[] slotes, Inventory inventory, ItemStack item) {
        for (int slot : slotes) {
            inventory.setItem(slot, item);
        }
    }

    // Для изменения цвета
    public static void setColors(Inventory inventory) {
        inventory.setItem(0, white);
        inventory.setItem(1, orange);
        inventory.setItem(2, siren);
        inventory.setItem(3, light_blue);
        inventory.setItem(4, yellow);
        inventory.setItem(5, green);
        inventory.setItem(6, pink);
        inventory.setItem(7, gray);
        inventory.setItem(8, light_gray);
        inventory.setItem(9, birusa);
        inventory.setItem(10, purple);
        inventory.setItem(11, blue);
        inventory.setItem(12, brown);
        inventory.setItem(13, dark_green);
        inventory.setItem(14, red);
        inventory.setItem(15, black);
    }

    public static String getItemNameForHallTable(int number, String okNOT) {
        // 4 - меню вопроса создать ли с ратушей
        // 5 - меню государства
        // 9 - меню выбора изменить цвет или ратушу
        // 10 - цвета
        // 35 - меню выбора типа игрока
        // 37 - кнопка далее
        // 40 - кнопка для выхода из государства игроку

        // Для меню улучшений, заготовки предметов:
        // В скобках указан upgrade.score

        // (0)
        // 1 - 32 буханки хлеба
        // 2 - 128 древесины и 192 гладкого камня
        // 3 - 15 алмазов

        // (1)
        // 6 - 64 свиных стейков
        // 7 - 128 гладкого камня и 192 древесины
        // 8 - 20 алмазов

        // (2)
        // 11 - 64 свиных стейков
        // 12 - 192 дерева и 256 гладкого камня
        // 13 - 16 золотых блоков

        // (3)
        // 14 - 16 тыквенных пирогов
        // 15 - 320 дерева и 256 гладкого камня
        // 16 - 24 золотых блока

        // (4)
        // 17 - 24 тыквенных пирога
        // 18 - 320 дерева и 320 гладкого камня
        // 19 - 32 алмаза

        // (5)
        // 20 - 1 торт
        // 21 - 320 дерева и 384 гладкого камня
        // 22 - 32 алмаза

        // (6)
        // 23 - 1 торт
        // 24 - 384 дерева и 384 гладкого камня
        // 25 - 48 алмаза

        // (7)
        // 26 - 1 торт
        // 27 - 384 дерева и 448 гладкого камня
        // 28 - 48 золотых блоков

        // (8)
        // 29 - 1 торт
        // 30 - 384 дерева и 448 серой шерсти
        // 31 - 48 золотых блоков

        // (9)
        // 32 - 1 торт
        // 33 - 512 дерева и 512 серой шерсти
        // 34 - 64 золотых блока
        
        switch(number) {
            case 1:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 32 буханки хлеба";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 2:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 128 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 192 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 3:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 15 алмазов";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 4:
                // Меню для выбора постройки ратуши или нет
                if (okNOT.equals("LEFT")) {
                    return ChatColor.AQUA + "Создать с ратушей";
                } else if (okNOT.equals("RIGHT")){
                    return ChatColor.GOLD + "Создать без ратуши";
                } else if (okNOT.equals("BARRACK_LEFT")){
                    return ChatColor.BLUE + "Создать с казармой";
                } else if (okNOT.equals("BARRACK_RIGHT")){
                    return ChatColor.RED + "Создать без казармы";
                }
            case 5:
                if (okNOT.equals("Button1")) {
                    return ChatColor.RED + "Игроки";
                } else if (okNOT.equals("Button2")){
                    return ChatColor.DARK_PURPLE + "Государство";
                } else if (okNOT.equals("Button3")){
                    return ChatColor.GOLD + "Улучшения";
                } else if (okNOT.equals("Button4")){
                    return ChatColor.DARK_PURPLE + "Город";
                }
            case 11:
            case 6:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 64 жареной свинины";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 7:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.GREEN + "Требуется 128 гладкого камня";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.RED + "Требуется 192 древесины";
                } else {
                    return ChatColor.RED + "Выполнено";
                }
            case 8:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 20 алмазов";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 9:
                // Меню для выбора постройки ратуши или нет
                if (okNOT.equals("LEFT")) {
                    return ChatColor.DARK_PURPLE + "Ратуша";
                } else {
                    return ChatColor.RED + "Цвет государства";
                }
            case 10:
                // Для цветов-бетонов
                if (okNOT.equals("WHITE")) {
                    return ChatColor.WHITE + "Белый";
                } else if (okNOT.equals("ORANGE")) {
                    return ChatColor.GOLD + "Оранжевый";
                } else if (okNOT.equals("SIRENEVIY")) {
                    return ChatColor.DARK_PURPLE + "Сиреневый";
                } else if (okNOT.equals("LIGHT_BLUE")) {
                    return ChatColor.DARK_AQUA + "Светло-синий";
                } else if (okNOT.equals("YELLOW")) {
                    return ChatColor.YELLOW + "Жёлтый";
                } else if (okNOT.equals("GREEN")) {
                    return ChatColor.GREEN + "Зелёный";
                } else if (okNOT.equals("PINK")) {
                    return ChatColor.LIGHT_PURPLE + "Розовый";
                } else if (okNOT.equals("GRAY")) {
                    return ChatColor.DARK_GRAY + "Серый";
                } else if (okNOT.equals("LIGHT_GRAY")) {
                    return ChatColor.GRAY + "Светло-серый";
                } else if (okNOT.equals("BIRUSA")) {
                    return ChatColor.AQUA + "Бирюзовый";
                } else if (okNOT.equals("PURPLE")) {
                    return ChatColor.DARK_PURPLE + "Фиолетовый";
                } else if (okNOT.equals("BLUE")) {
                    return ChatColor.BLUE + "Синий";
                } else if (okNOT.equals("BROWN")) {
                    return ChatColor.DARK_RED + "Коричневый";
                } else if (okNOT.equals("DARK_GREEN")) {
                    return ChatColor.DARK_GREEN + "Тёмно-зелёный";
                } else if (okNOT.equals("RED")) {
                    return ChatColor.RED + "Красный";
                } else if (okNOT.equals("BLACK")) {
                    return ChatColor.DARK_GRAY + "Чёрный";
                }
            case 12:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 192 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 256 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 13:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 16 золотых блоков";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 14:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 16 тыквенных пирогов";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 15:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 320 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 256 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 16:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 24 золотых блока";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 17:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 24 тыквенных пирога";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 18:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 320 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 320 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 19:
            case 22:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 32 алмаза";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 20:
            case 23:
            case 26:
            case 29:
            case 32:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 1 торт";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 21:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 320 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 384 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 24:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 384 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 384 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 25:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 48 алмазов";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 27:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 384 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 448 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 28:
            case 31:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 48 золотых блоков";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 30:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 384 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 448 серой шерсти";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 33:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 512 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 512 серой шерсти";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 34:
                if (okNOT.equals("NOT")) {
                    return ChatColor.GOLD + "Требуется 64 золотых блока";
                } else {
                    return ChatColor.GOLD + "Выполнено";
                }
            case 35:
                if (okNOT.equals("LEFT")) {
                    return ChatColor.AQUA + "Граждане";
                } else {
                    return ChatColor.DARK_PURPLE + "Свободные игроки";
                }
            case 36:
                if (okNOT.equals("LEFT")) {
                    return ChatColor.GOLD + "Повышение";
                } else if (okNOT.equals("MIDDLE")) {
                    return ChatColor.RED + "Информация об игроках";
                } else {
                    return ChatColor.DARK_PURPLE + "Исключить из государства";
                }
            case 37:
                return ChatColor.GRAY + "Далее";
            case 38:
                if (okNOT.equals("LEFT")) {
                    return ChatColor.GOLD + "Пригласить в государство";
                } else {
                    return ChatColor.DARK_PURPLE + "Информация об игроке";
                }
            case 39:
                return ChatColor.GRAY + "Удалить город";
            case 40:
                return ChatColor.RED + "Выйти из государства";
            case 41:
                if (okNOT.equals("LEFT")) {
                    return ChatColor.GOLD + "Повышение";
                } else {
                    return ChatColor.RED + "Понижение";
                }
        }
        return null;
    }

    public static String getItemNameForBarrack(int number, String okNOT) {
        switch (number) {
            case 1:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 64 стрелы";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 2:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 128 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 128 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 3:
                if (okNOT.equals("NOT")) {
                    return ChatColor.YELLOW + "Требуется 32 снопа сена";
                } else {
                    return ChatColor.YELLOW + "Выполнено";
                }
            case 4:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 128 стрел";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 5:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 128 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 192 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 6:
                if (okNOT.equals("NOT")) {
                    return ChatColor.YELLOW + "Требуется 48 снопов сена";
                } else {
                    return ChatColor.YELLOW + "Выполнено";
                }
            case 7:
                if (okNOT.equals("NOT")) {
                    return ChatColor.DARK_PURPLE + "Требуется 192 стрелы";
                } else {
                    return ChatColor.DARK_PURPLE + "Выполнено";
                }
            case 8:
                if (okNOT.equals("NOT0")) {
                    return ChatColor.RED + "Требуется 192 древесины";
                } else if (okNOT.equals("NOT1")){
                    return ChatColor.GREEN + "Требуется 192 гладкого камня";
                } else {
                    return ChatColor.GREEN + "Выполнено";
                }
            case 9:
                if (okNOT.equals("NOT")) {
                    return ChatColor.YELLOW + "Требуется 64 снопа сена";
                } else {
                    return ChatColor.YELLOW + "Выполнено";
                }
        }
        return null;
    }

    // Лоры предметов
    public static ArrayList<String> getItemLoreForHallTable(String okNOT) {
        ArrayList<String> lore = new ArrayList<>();
        if (okNOT.equals("NOT") || okNOT.equals("NOT0") || okNOT.equals("NOT1")) {
            lore.add(0, ChatColor.WHITE + "  Когда добудешь нужные ресурсы,");
            lore.add(1, ChatColor.WHITE + "положи вещи к себе в инвентарь и нажми");
            lore.add(2, ChatColor.WHITE + "сюда, чтобы потратить их на постройку.");
        } else if (okNOT.equals("OK")){
            lore.add(0, ChatColor.WHITE + "Поздравляю! Слот заполнен!");
        } else if (okNOT.equals("ratushaChoiceLeft")) {
            lore.add(0, ChatColor.WHITE + "  Если вы выберете этот вариант, то");
            lore.add(1, ChatColor.WHITE + "вокруг этого стола возведётся постройка");
            lore.add(2, ChatColor.WHITE + "\"Ратуша\", при этом все близлежащие");
            lore.add(3, ChatColor.WHITE + "блоки уберутся.");
        } else if (okNOT.equals("ratushaChoiceRight")) {
            lore.add(0, ChatColor.WHITE + "  Если вы выберете этот вариант,");
            lore.add(1, ChatColor.WHITE + "то никакой постройки не появится,");
            lore.add(2, ChatColor.WHITE + "останется лишь этот стол.");
        } else if (okNOT.equals("menuCityButton1")) {
            lore.add(0, ChatColor.WHITE + "  Здесь можно управлять вашими");
            lore.add(1, ChatColor.WHITE + "гражданами.");
        } else if (okNOT.equals("menuCityButton2")) {
            lore.add(0, ChatColor.WHITE + "  Нажми, чтобы улучшить постройку");
            lore.add(1, ChatColor.WHITE + "или изменить цвет государства.");
        } else if (okNOT.equals("menuCityButton3")) {
            lore.add(0, ChatColor.WHITE + "  Здесь вы можете увидеть статистику");
            lore.add(1, ChatColor.WHITE + "всех улучшений вашего государства.");
        } else if (okNOT.equals("menuCityButton4")) {
            lore.add(0, ChatColor.WHITE + "Нажми, чтобы улучшить постройку");
        } else if (okNOT.equals("ratushaLeft")) {
            lore.add(0, ChatColor.WHITE + "  Улучшайте ратушу, чтобы получать");
            lore.add(1, ChatColor.WHITE + "больше тайлов под ваш контроль.");
        } else if (okNOT.equals("changeColorRight")) {
            lore.add(0, ChatColor.WHITE + "  Измените цвет отображения вашего");
            lore.add(1, ChatColor.WHITE + "государства на карте, чтобы показать");
            lore.add(1, ChatColor.WHITE + "другим свой стиль.");
        } else if (okNOT.equals("color")) {
            lore.add(0, ChatColor.WHITE + "  Щелкните, чтобы поменять цвет");
            lore.add(1, ChatColor.WHITE + "вашего государства.");
        } else if (okNOT.equals("typePlayersLeft")) {
            lore.add(0, ChatColor.WHITE + "  Здесь вы можете управлять вашими");
            lore.add(1, ChatColor.WHITE + "гражданами.");
        } else if (okNOT.equals("typePlayersRight")) {
            lore.add(0, ChatColor.WHITE + "  Нажмите сюда, чтобы пригласить");
            lore.add(1, ChatColor.WHITE + "нового игрока или узнать о нём.");
            lore.add(2, ChatColor.WHITE + "информацию.");
        } else if (okNOT.equals("civilianPlayerLeft")) {
            lore.add(ChatColor.WHITE + "  Здесь вы можете повысить в должности");
            lore.add(ChatColor.WHITE + "ваших граждан.");
        } else if (okNOT.equals("civilianPlayerMiddle")) {
            lore.add(0, ChatColor.WHITE + "  Здесь вы можете увидеть информацию");
            lore.add(1, ChatColor.WHITE + "об игроках вашего государства.");
        } else if (okNOT.equals("civilianPlayerRight")) {
            lore.add(0, ChatColor.WHITE + "  Гражданин нарушает порядок?");
            lore.add(1, ChatColor.WHITE + "Не медлите и исключите его из");
            lore.add(2, ChatColor.WHITE + "государства прямо сейчас!");
        } else if (okNOT.equals("civilianKick")) {
            lore.add(0, ChatColor.WHITE + "  Нажмите, чтобы исключить этого");
            lore.add(1, ChatColor.WHITE + "игрока из государства.");
        } else if (okNOT.equals("civilianKicked")) {
            lore.add(0, ChatColor.WHITE + "  Этот игрок больше не состоит");
            lore.add(1, ChatColor.WHITE + "в вашем государстве.");
        } else if (okNOT.equals("daleeButton")) {
            lore.add(0, ChatColor.WHITE + "  Перелистывает на следующую");
            lore.add(1, ChatColor.WHITE + "страницу.");
        } else if (okNOT.equals("freePlayerLeft")) {
            lore.add(0, ChatColor.WHITE + "  Здесь вы можете пригласить в");
            lore.add(1, ChatColor.WHITE + "государство новых игроков.");
        } else if (okNOT.equals("freePlayerRight")) {
            lore.add(0, ChatColor.WHITE + "  Узнайте информацию о свободных");
            lore.add(1, ChatColor.WHITE + "игроках.");
        } else if (okNOT.equals("invite")) {
            lore.add(0, ChatColor.WHITE + "  Нажмите, чтобы отправить приглашение");
            lore.add(1, ChatColor.WHITE + "в ваше государство этому игроку.");
        } else if (okNOT.equals("invited")) {
            lore.add(0, ChatColor.WHITE + "Приглашение отправлено.");
        } else if (okNOT.equals("deleteCityButton")) {
            lore.add(0, ChatColor.WHITE + "  Нажмите два раза, чтобы удалить");
            lore.add(1, ChatColor.WHITE + "этот город.");
        } else if (okNOT.equals("citizenHallTable")) {
            lore.add(0, ChatColor.WHITE + "Нажмите, чтобы покинуть государство");
        } else if (okNOT.equals("barrackChoiceLeft")) {
            lore.add(0, ChatColor.WHITE + "  Если вы выберете этот вариант, то");
            lore.add(1, ChatColor.WHITE + "вокруг этого стола возведётся постройка");
            lore.add(2, ChatColor.WHITE + "\"Казарма\", при этом все близлежащие");
            lore.add(3, ChatColor.WHITE + "блоки уберутся.");
        } else if (okNOT.equals("barrackChoiceRight")) {
            lore.add(0, ChatColor.WHITE + "  Если вы выберете этот вариант,");
            lore.add(1, ChatColor.WHITE + "то никакой постройки не появится,");
            lore.add(2, ChatColor.WHITE + "останется лишь этот стол.");
        } else if (okNOT.equals("rankLeft")) {
            lore.add(0, ChatColor.WHITE + "  Здесь вы можете повысить должность");
            lore.add(1, ChatColor.WHITE + "игрока, благодаря чему у него появятся");
            lore.add(2, ChatColor.WHITE + "новые возможности.");
        } else if (okNOT.equals("rankRight")) {
            lore.add(0, ChatColor.WHITE + "  Здесь вы можете понизить должность");
            lore.add(1, ChatColor.WHITE + "игрока, из-за чего он лишится некоторых");
            lore.add(2, ChatColor.WHITE + "возможностей");
        }
        return lore;
    }

    // Для кнопки "создать"
    // P.S. подходит и для бараков
    public static ArrayList<String> getButtonLoreForHallTable(String allOKorNOT) {
        ArrayList<String> lore = new ArrayList<>();
        if (allOKorNOT.equals("NOT")) {
            lore.add(0, ChatColor.WHITE + "  Нажми эту кнопку, когда заполнишь");
            lore.add(1, ChatColor.WHITE + "все слоты, чтобы создать постройку.");
        } else if (allOKorNOT.equals("OK")){
            lore.add(0, ChatColor.WHITE + "  Все слоты заполнены,");
            lore.add(1, ChatColor.WHITE + "нажмите, чтобы создать постройку.");
        } else if (allOKorNOT.equals("UPGRADE_NOT")) {
            lore.add(0, ChatColor.WHITE + "  Нажми эту кнопку, когда заполнишь все");
            lore.add(1, ChatColor.WHITE + "слоты, чтобы улучшить постройку.");
        } else if (allOKorNOT.equals("UPGRADE_OK")) {
            lore.add(0, ChatColor.WHITE + "  Все слоты заполнены,");
            lore.add(1, ChatColor.WHITE + "нажмите, чтобы улучшить постройку.");
        }
        return lore;
    }

    // Для информации об игроке
    public static ArrayList<String> getCivilianLoreInfo(String playerName, String typeOfPlayers) {
        ArrayList<String> lore = new ArrayList<>();

        String playerStatus = databaseDB.getString("marinbay.player." + playerName.toLowerCase() + ".status");
        String status = null;
        if (playerStatus != null) {
            if (playerStatus.equals("general")) {
                status = "Глава государства";
            } else if (playerStatus.equals("guber")) {
                status = "Губернатор";
            } else {
                status = "Гражданин";
            }
        } else {
            status = "Не опознан";
        }

        Player p = Bukkit.getPlayer(playerName);
        MarinPlayer marinPlayer = new MarinPlayer(playerName);
        String value = PlaceholderAPI.setPlaceholders(p, "%statistic_hours_played%");
        int hoursInGame;
        if (value != null && !value.equals("")) {
            hoursInGame = Integer.parseInt(value);
        } else {
            hoursInGame = 0;
        }

        // Рассчитываем сколько времени человек провёл в государстве
        String timeInvite = databaseDB.getString("marinbay.player." + playerName.toLowerCase() + ".timeInvite");
        long hoursInState;
        if (timeInvite != null) {
            long timeInviteLong = Long.parseLong(timeInvite);
            hoursInState = (System.currentTimeMillis() - timeInviteLong) / 1000 / 60 / 60;
        } else {
            hoursInState = 0;
        }

        // Получаем в каких государствах состоял
        String whereBe = databaseDB.getString("marinbay.player." + playerName.toLowerCase() + ".wherebe");
        if (whereBe == null || whereBe == "") {
            whereBe = "-";
        }
        String[] whereHeBe = whereBe.split(", ");

        if (typeOfPlayers.equals("free")) {
            lore.add(0, ChatColor.WHITE + "Информация об игроке:");
            lore.add(1, "");
            lore.add(2, ChatColor.AQUA + "Времени в игре: " + ChatColor.WHITE + hoursInGame + " ч.");
            lore.add(3, ChatColor.AQUA + "Состоял в таких государствах: " + ChatColor.WHITE + whereHeBe[0]);
            addStatesToLore(lore, whereHeBe, 4);

            return lore;
        }

        lore.add(ChatColor.WHITE + "Информация об игроке:");
        lore.add("");
        lore.add(ChatColor.AQUA + "Должность: " + ChatColor.WHITE + status);
        lore.add(ChatColor.AQUA + "Времени в игре: " + ChatColor.WHITE + hoursInGame + " ч.");
        lore.add(ChatColor.AQUA + "Времени в государстве: " + ChatColor.WHITE + hoursInState + " ч.");

        // Если игрок основал какой-то город, то добавляем ему это в инфу
        String placeCity = marinPlayer.getPlaceCity();
        if (placeCity != null) {
            if (!placeCity.equals("NO")) {
                String cityName = databaseDB.getString("marinbay.city." + placeCity + ".name");
                lore.add(ChatColor.AQUA + "Основал: " + ChatColor.WHITE + cityName);
            }
        }
        // Если игрок - генерал, то получаем имя государства, его он и основал
        if (marinPlayer.isGeneral()) {
            String stateName = databaseDB.getString("marinbay.state." + marinPlayer.getStateID() + ".name");
            lore.add(ChatColor.AQUA + "Основал: " + ChatColor.WHITE + stateName);
        }

        lore.add("");
        lore.add(ChatColor.AQUA + "Состоял в таких государствах: " + ChatColor.WHITE + whereHeBe[0]);
        addStatesToLore(lore, whereHeBe, 7);

        return lore;
    }
    // Получаем лор для повышения и понижения игрока
    public static ArrayList<String> getLoreForRankRaiseNDowngrade(String playerName, String raiseOrDowngrade) {
        String playerRank = databaseDB.getString("marinbay.player." + playerName.toLowerCase() + ".status");
        lore = getAddLinesToLoreRank(raiseOrDowngrade, playerRank, 1);
        return lore;
    }
    // Вспомогательный метод для метода выше
    public static ArrayList<String> getAddLinesToLoreRank(String downRaise, String rank, int numberCloser) {
        ArrayList<String> lore = new ArrayList<>();

        ChatColor w = ChatColor.WHITE;
        ChatColor r = ChatColor.RED;
        ChatColor a = ChatColor.AQUA;
        ChatColor g = ChatColor.GOLD;
        ChatColor bold = ChatColor.BOLD;
        if (downRaise.equals("raise")) {
            // Повышение
            if (rank.equals("citizen")) {
                lore.add(w + "Текущая должность: " + a + "Гражданин");
                lore.add("");
                lore.add(w + "  Нажмите, чтобы повысить игрока до");
                lore.add(w + "должности" + a + " Губернатор");
                lore.add(w + "");
                lore.add(w + "  Губернаторы могут создавать свои");
                lore.add(w + "постройки и" + a + " 1 город" + w + ", который они");
                lore.add(w + "полностью контролируют.");
            } else if (rank.equals("guber")) {
                lore.add(w + "Текущая должность: " + a + "Губернатор");
                lore.add("");
                lore.add(w + "  Нажмите, чтобы повысить игрока до");
                lore.add(w + "должности" + a + " Приближённый Государя");
                lore.add(w + "");
                lore.add(w + "  Приближённые имеют " + a + "почти полный");
                lore.add(w + "контроль" + w + " над государством. Единственное,");
                lore.add(w + "чего они не могут, - выгнать из государства");
                lore.add(w + "самого" + a + " Государя");
                lore.add(w + "");
                lore.add(bold + "" + r + "P.S. Приближённые считаются за государя и,");
                lore.add(bold + "" + r + "если его долго нет на месте, но приближённый");
                lore.add(bold + "" + r + "всё так же заведует государством, то государство");
                lore.add(bold + "" + r + "не удалится в течение 7-и дней.");
            } else if (rank.equals("closer") && numberCloser == 1) {
                lore.add(w + "Текущая должность: " + g + "Приближённый Государя");
                lore.add("");
                lore.add(w + "  Нажмите, чтобы повысить игрока до");
                lore.add(w + "должности" + g + " Великий Государь");
                lore.add(w + "");
                lore.add(g + "  Великий Государь" + w + ", вы устали от правления?");
                lore.add(w + "Что ж, чтобы ваше государство и наследие");
                lore.add(w + "всё дальше развивали ваши" + g + " бравые потомки" + w + ",");
                lore.add(w + "выберите одного из них прямо сейчас, а сами");
                lore.add(w + "станьте лишь" + g + " Губернатором");
            } else if (rank.equals("closer") && numberCloser == 2) {
                lore.add(w + "Текущая должность: " + g + "Приближённый Государя");
                lore.add("");
                lore.add(w + "  Нажмите, чтобы повысить игрока до");
                lore.add(w + "должности" + g + " Великий Государь");
                lore.add(w + "");
                lore.add(w + "  Вы уверены? Нажмите" + g + " повторно" + w + ", чтобы");
                lore.add(w + "этот игрок стал" + g + " Великим Государем");
            }
        } else {
            // Понижение
            if (rank.equals("closer")) {
                lore.add(w + "Текущая должность: " + a + "Приближённый Государя");
                lore.add("");
                lore.add(w + "  Нажмите, чтобы понизить игрока до");
                lore.add(w + "должности" + a + " Губернатор");
                lore.add(w + "");
                lore.add(w + "  Игрок больше не сможет" + a + " заведовать");
                lore.add(a + "государством" + w + ", а вы лишитесь верного");
                lore.add(w + "слуги.");
            } else if (rank.equals("guber")) {
                lore.add(w + "Текущая должность: " + a + "Губернатор");
                lore.add("");
                lore.add(w + "  Нажмите, чтобы понизить игрока до");
                lore.add(w + "должности " + a + "Гражданин");
                lore.add(w + "");
                lore.add(bold + "" + r + "ВНИМАНИЕ!");
                lore.add(bold + "" + r + "  Игрок больше не сможет заведовать");
                lore.add(bold + "" + r + "своим городом (при его наличии), из-за");
                lore.add(bold + "" + r + "чего тот пропадёт.");
            }
        }
        return lore;
    }

    // Перенос для метода выше
    public static void addStatesToLore(ArrayList<String> lore, String[] whereHeBe, int indexForLore) {
        for (int i = 1; i < whereHeBe.length; i+=3) {
            String forLoreString = null;
            int lengthMassive = whereHeBe.length;
            if (i < lengthMassive) {
                forLoreString = whereHeBe[i];
            }
            if (i+1 < lengthMassive) {
                forLoreString = forLoreString + ", " + whereHeBe[i+1];
            }
            if (i+2 < lengthMassive) {
                forLoreString = forLoreString + ", " + whereHeBe[i+2];
            }

            lore.add(indexForLore, ChatColor.WHITE + forLoreString);
            indexForLore++;
        }
    }

    // Определяет какую кнопку нарисовать - Создать государство или Создать город
    public static String getCreateButton(String status, String okNO) {
        if (okNO.equals("OK")) {
            return status.equals("state") ? ChatColor.AQUA + "Создать государство" : ChatColor.AQUA + "Создать город";
        } else {
            return status.equals("state") ? ChatColor.RED + "Вы пока не можете создать государство" : ChatColor.RED + "Вы пока не можете создать город";
        }
    }

    // Меняет все апргейд айтемы на NOT, чтобы не было проблем
    public static void toDefaultValuesItems(String htName) {
        buildingsDB.write("halltables." + htName + ".upgrade.firstItem", "NOT");
        buildingsDB.write("halltables." + htName + ".upgrade.secondItem", "NOT0");
        buildingsDB.write("halltables." + htName + ".upgrade.thirdItem", "NOT");
    }
    public static void toDefaultValuesItemsB(String barrackName) {
        buildingsDB.write("barracks." + barrackName + ".upgrade.firstItem", "NOT");
        buildingsDB.write("barracks." + barrackName + ".upgrade.secondItem", "NOT0");
        buildingsDB.write("barracks." + barrackName + ".upgrade.thirdItem", "NOT");
    }

    // Отсек облегчения проверок меню
    // Используется при нажатии на кастомный инвентарь
    public static boolean ifContainsGetItems(String numberItem, String htName, Inventory pInv,
                                             Player player,Material toRemove, int amount, String checkWhatInDatabase) {
        // NOTorNOT0 - вбиваем что должна содержать строка предмета, чтобы прошло отнимание
        // Если NOT0 - дерево
        // NOT1 - гладкий камень
        // NOT - обычный одинарный предмет (прим. алмазы)
        okNOTitem = buildingsDB.getString("halltables." + htName + ".upgrade." + numberItem);
        if (pInv.contains(toRemove, amount)  && okNOTitem.equals(checkWhatInDatabase)) {
            smartRemoveItem(player, toRemove, amount);
            return true;
        }
        return false;
    }
    // Специальная функция для проверки на дерево, так как там проверка иная
    public static boolean ifContainsGetWood(String numberItem, String htName, Inventory pInv,
                                             Player player, int amount, String NOTorNOT0) {
        okNOTitem = buildingsDB.getString("halltables." + htName + ".upgrade." + numberItem);
        if (doesInventoryContainsWood(player, pInv, amount, okNOTitem.equals(NOTorNOT0), htName)) {
            return true;
        }
        return false;
    }
    // Для бараков
    public static boolean ifContainsGetItemsB(String numberItem, String barrackName, Inventory pInv,
                                             Player player,Material toRemove, int amount, String checkWhatInDatabase) {
        // NOTorNOT0 - вбиваем что должна содержать строка предмета, чтобы прошло отнимание
        // Если NOT0 - дерево
        // NOT1 - гладкий камень
        // NOT - обычный одинарный предмет (прим. алмазы)
        okNOTitem = buildingsDB.getString("barracks." + barrackName + ".upgrade." + numberItem);
        if (pInv.contains(toRemove, amount)  && okNOTitem.equals(checkWhatInDatabase)) {
            smartRemoveItem(player, toRemove, amount);
            return true;
        }
        return false;
    }
    public static boolean ifContainsGetWoodB(String numberItem, String barrackName, Inventory pInv,
                                            Player player, int amount, String NOTorNOT0) {
        okNOTitem = buildingsDB.getString("barracks." + barrackName + ".upgrade." + numberItem);
        if (doesInventoryContainsWoodB(player, pInv, amount, okNOTitem.equals(NOTorNOT0), barrackName)) {
            return true;
        }
        return false;
    }

    // Получаем рандомный гексагон при улучшении города
    public static void getRandomHexagon(Location htSignLocation, HallTable ht, Player player) {
        // Получаем гексагон, в который входит табличка ратуши
        Hexagon ratushaHex = grid.getHexagon(htSignLocation);
        int upgradeScore = Main.getInstance().getUpgradeScore("halltables." + ht.getName() + ".upgrade.score");
        Random rand = new Random();
        boolean neighbors2DontChecked = true;

        // Получаем статус холтейбла. Если это город, то записываем ему новый гексагон
        String htStatus = ht.getStatus();
        String htID = ht.getID();

        ArrayList<Hexagon> neighborHexagons = new ArrayList<>(Arrays.asList(ratushaHex.getNeighbors(1)));
        Hexagon neighbor = neighborHexagons.get(rand.nextInt(neighborHexagons.size()));

        while (true) {
            // Если соседних гексагонов больше нет и все они заняты, то так и пишем
            if (neighborHexagons.size() == 0) {
                if (neighbors2DontChecked) {
                    // Если ни одного гексагона рядом нет, то смотрим второй слой, если уж и там нет, то
                    // не добавляем новый гексагон
                    neighborHexagons = new ArrayList<>(Arrays.asList(ratushaHex.getNeighbors(2)));
                    neighbor = neighborHexagons.get(rand.nextInt(neighborHexagons.size()));
                    neighbors2DontChecked = false;
                } else {
                    String newUpgradeScore = String.valueOf(upgradeScore + 1);
                    buildingsDB.write("halltables." + ht.getName() + ".upgrade.score", newUpgradeScore);
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Поздравляем с улучшением ратуши!");
                    player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Вокруг этой постройки не было свободных территорий, поэтому новый тайл не был взят");
                    break;
                }
            }

            // Проверяем не занят ли этот гексагон
            if (!databaseDB.isSet("marinbay.hexagons." + neighbor.toString())) {
                // Записываем нового владельца гексагона
                String ownerOfRatHex = databaseDB.getString("marinbay.hexagons." + ratushaHex.toString() + ".owner");
                databaseDB.write("marinbay.hexagons." + neighbor.toString() + ".owner", ownerOfRatHex);
                String newUpgradeScore = String.valueOf(upgradeScore + 1);
                buildingsDB.write("halltables." + ht.getName() + ".upgrade.score", newUpgradeScore);
                databaseDB.writeNewHexagon("marinbay.state." + ownerOfRatHex + ".hexagons", neighbor.toString());

                // Если это город, то мы записываем этот гексагон ему
                // И записываем trueOwner - спец переменная, которая показывает настоящего владельца гексагона
                if (htStatus.equals("city")) {
                    databaseDB.writeNewHexagon("marinbay.city." + htID + ".hexagons", neighbor.toString());
                    databaseDB.write("marinbay.hexagons." + neighbor.toString() + ".trueOwner", htID);
                }

                // Обновляем регион, получая имя государства по городу
                // Причём если это государство изначально, то просто ничего не изменится
                String stateName = databaseDB.getString("marinbay.state." + ht.getStateID() + ".name");
                removeRegion(stateName);

                // Нужно получить имя государства
                // Если указать имя города, то территории не найдёт
                City htCity = new City(ht.getStateID(),"state");
                HexagonSide[] side = grid.getBounds(htCity.getTerritories());
                List<BlockVector2> points = getPointsFromSide(side);

                String name = MrTransliterator.rusToEng(stateName.toLowerCase().replace(" ", ""));
                createRegion(ownerOfRatHex, name, databaseDB.getString("marinbay.player." + ht.getGeneral() + ".name"),points);

                player.sendMessage(ChatColor.AQUA + "[MarinBay]" + ChatColor.WHITE + " Поздравляем с улучшением ратуши!");
                DynmapDrawer.redrawDynmap();
                break;
            } else {
                // Если тот тайл занят, то мы меняем число и берём другого соседа
                neighborHexagons.remove(neighbor);
                if (neighborHexagons.size() == 0) {
                    continue;
                }
                neighbor = neighborHexagons.get(rand.nextInt(neighborHexagons.size()));
            }
        }
    }

    // Проверяет прошло ли 3 дня с изменения цвета государства игроком
    public static boolean checkThreeDays(Player p) {
        if (colorsChangeDB.isSet(p.getName())) {
            long timeOld = Long.parseLong(colorsChangeDB.getString(p.getName()));
            long timeNow = System.currentTimeMillis();
            long difference = timeNow - timeOld;

            // Так выглядит перевод миллисекунд в дни
            if (difference/1000/60/60/24 < 3) {
                p.sendMessage(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE + "Вы можете изменить цвет только раз в 3 дня");
                return false;
            }
        }
        return true;
    }


        // Основная функция для создания региона
    // Именно эта создаёт регион стола, который нельзя ломать вообще никому кроме оперов
    public static void createRegion(String name, BlockVector3 pos1, BlockVector3 pos2) {
        ProtectedRegion region = new ProtectedCuboidRegion("table_" + name, pos1, pos2);

        DefaultDomain owners = new DefaultDomain();
        // Это рандомный UUID рандомного игрока, просто задаём случайного овнера для прикола,
        // я уже точно не помню для чего я это сделал
        owners.addPlayer(UUID.fromString("0e881ec4-be0c-3a2a-857c-ab82631c17bd"));
        region.setOwners(owners);

        // Все флаги ниже нужны для региона стола, они не дают ломать и ставить блоки
        // даже овнерам региона, соотвественно для региона гексагона эти флаги не нужны
        region.setFlag(Flags.BUILD, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.DENY_MESSAGE, "");
        region.setPriority(100);

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        manager.addRegion(region);
    }
    // Создание региона-гексагона, в котором могут строить овнеры государства
    public static void createRegion(String stateID, String name, String general,List<BlockVector2> points) {
        ProtectedRegion regionHex = new ProtectedPolygonalRegion(name, points, -65, 320);

        // Добавляем всех старых игроков, если строчка в базе данных не пуста
        DefaultDomain owners = new DefaultDomain();
        if (databaseDB.isSet("marinbay.state." + stateID + ".citisens")) {
            String[] citisens = databaseDB.getString("marinbay.state." + stateID + ".citisens").split(", ");
            for (String c : citisens) {
                owners.addPlayer(c);
            }
        } else {
            owners.addPlayer(general);
        }
        regionHex.setOwners(owners);

        regionHex.setFlag(Flags.PVP, StateFlag.State.ALLOW);

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        manager.addRegion(regionHex);
    }

    // Создаёт регион стола + регион гексагона
    public static void createRegionH(HallTable ht) {
        // Чисто регион стола, который принадлежит какому-то фантомному игроку (вроде как мне, я уже не помню)
        BlockVector3 pos1 = ht.getCorner(1);
        BlockVector3 pos2 = ht.getCorner(2);
        String name = MrTransliterator.rusToEng(ht.getName().toLowerCase().replace(" ", ""));
        createRegion(name, pos1, pos2);

        // Создаём регион на всю площадь гексагона
        // Для игроков, чтобы они там могли делать что угодно
        City htCity = new City(ht.getStateID(), "state");
        HexagonSide[] side = grid.getBounds(htCity.getTerritories());

        // С помощью массива выше создаю список точек для формирования региона
        // И наконец-то сам регион
        List<BlockVector2> points = getPointsFromSide(side);
        createRegion(ht.getStateID(),name, databaseDB.getString("marinbay.player." + ht.getGeneral() + ".name"), points);
    }
    // Получение точек гексагона в правильном порядке - последовательном
    public static List<BlockVector2> getPointsFromSide(HexagonSide[] side) {
        // Не самая умная функция, но альтернатив не вижу
        // Да и раз работает, то с пивом пойдет
        List<BlockVector2> points = new ArrayList<>();

        List<String> stringSide = new ArrayList<>();
        String lineOfSide;
        int px1,px2,pz1,pz2;

        for (HexagonSide s : side) {
            Point firstP = s.getEnds()[0];
            Point secondP = s.getEnds()[1];

            px1 = (int) Math.round(firstP.getX());
            pz1 = (int) Math.round(firstP.getY());
            px2 = (int) Math.round(secondP.getX());
            pz2 = (int) Math.round(secondP.getY());

            lineOfSide = px1 + ";" + pz1 + "=" + px2 + ";" + pz2;

            if (stringSide.contains(lineOfSide)) {
                continue;
            }

            stringSide.add(lineOfSide);
        }

        // Если stringSide пуст, то возвращаем пустой список
        if (stringSide.isEmpty()) {
            return points;
        }


            // Формируем последовательные точки
        // Добавляем две первые точки из первой стороны гексагона и сразу же её убираем из списка
        List<String> allStrPoints = new ArrayList<>();

        BlockVector2 bv;
        String[] two_points = stringSide.get(0).split("=");
        String lastPoint = " ";
        for (String p : two_points) {
            String[] xNz = p.split(";");
            bv = BlockVector2.at(Integer.parseInt(xNz[0]), Integer.parseInt(xNz[1]));
            points.add(bv);
            allStrPoints.add(p);

            lastPoint = p;
        }
        stringSide.remove(0);

        // Остальные точки добавляем здесь
        String[] side2points;
        String[] needPoint;
        Boolean notempty = true;
        // Если итераций более 1000, то цикл прервется
        int end1000 = 0;
        while (notempty) {
            end1000 += 1;
            if (end1000 >= 1000) {
                log.info("=====");
                log.info(ChatColor.RED + "[MarinTileEditor] Произошла ошибка при создании региона");
                log.info(ChatColor.RED + "[MarinTileEditor] Количество итераций перевалило за 1000");
                log.info("=====");
                break;
            }

            if (stringSide.isEmpty()) {
                // Если в списке уже нет элементов, то обрываем цикл
                notempty = false;
                continue;
            }
            String subForRemove = "";
            // boolean-переменная для того, чтобы добавлять только 1 точку за раз
            // иначе весь остальной алгоритм работать не будет
            Boolean weAddPoint = false;
            for (String sub : stringSide) {
                if (weAddPoint) {
                    break;
                }

                // В этом массиве в строчном виде хранятся 2 точки стороны
                side2points = sub.split("=");

                // Если в списке осталась лишь 1 точка, то мы немного меняем сценарий
                // Если же в списке больше 1 точки, то всё как обычно
                if (stringSide.size() == 1) {
                    for (String p : side2points) {
                        if (!lastPoint.equals(p)) {
                            // Добавляем последнюю точку и заканчиваем цикл
                            needPoint = p.split(";");
                            bv = BlockVector2.at(Integer.parseInt(needPoint[0]), Integer.parseInt(needPoint[1]));

                            points.add(bv);

                            lastPoint = p;
                            subForRemove = sub;
                            weAddPoint = true;
                            continue;
                        }
                    }
                } else if (Arrays.asList(side2points).contains(lastPoint)) {
                    // Так как эта сторона гексагона имеет общую точку, которая
                    // была последней добавлена в наши точки, то это то, что нам нужно и мы добавляем
                    // новую точку, при этом заменяя переменную lastPoint
                    for (String p : side2points) {
                        // Если в points нет такой точки, то добавялем
                        // Позволяет добавить только новую точку
                        if (!allStrPoints.contains(p)) {
                            needPoint = p.split(";");
                            bv = BlockVector2.at(Integer.parseInt(needPoint[0]), Integer.parseInt(needPoint[1]));

                            points.add(bv);
                            allStrPoints.add(p);

                            lastPoint = p;
                            subForRemove = sub;
                            weAddPoint = true;
                        }
                    }
                }
            }

            // Если добавили точку - то удаляем её из списка
            if (stringSide.contains(subForRemove)) {
                stringSide.remove(subForRemove);
            }
        }

        return points;
    }
    public static void createRegionB(Barrack barrack) {
        BlockVector3 pos1 = barrack.getCorner(1);
        BlockVector3 pos2 = barrack.getCorner(2);
        ProtectedRegion region = new ProtectedCuboidRegion(MrTransliterator.rusToEng(barrack.getName().toLowerCase().replace(" ", "")), pos1, pos2);

        DefaultDomain owners = new DefaultDomain();
        owners.addPlayer(UUID.fromString("0e881ec4-be0c-3a2a-857c-ab82631c17bd"));
        region.setOwners(owners);

        region.setFlag(Flags.BUILD, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_BREAK, StateFlag.State.DENY);
        region.setFlag(Flags.BLOCK_PLACE, StateFlag.State.DENY);
        region.setFlag(Flags.DENY_MESSAGE, "");
        region.setPriority(100);

        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        manager.addRegion(region);
    }

    // Удаляет регион ГЕКСАГОНА
    public static void removeRegion(String htName) {
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        manager.removeRegion(MrTransliterator.rusToEng(htName.toLowerCase().replace(" ", "")));
    }
    // Удаляет регион СТОЛА
    public static void removeRegionTable(String htName) {
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        manager.removeRegion("table_" + MrTransliterator.rusToEng(htName.toLowerCase().replace(" ", "")));
    }
    public static void removeRegionB(String barrackName) {
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));
        manager.removeRegion(MrTransliterator.rusToEng(barrackName.toLowerCase().replace(" ", "")));
    }

    // Добавляем игрока в регион
    public static void addToRegion(String regionName, String playerName) {
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));

        ProtectedRegion region = manager.getRegion(regionName);
        DefaultDomain owners = region.getOwners();
        owners.addPlayer(playerName);
        region.setOwners(owners);
    }
    // Удаляем игрока из региона
    public static void removeFromRegion(String regionName, String playerName) {
        World world = Bukkit.getWorld("world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager manager = container.get(BukkitAdapter.adapt(world));

        ProtectedRegion region = manager.getRegion(regionName);
        DefaultDomain owners = region.getOwners();
        owners.removePlayer(playerName);
        region.setOwners(owners);
    }
    // Обновление региона государства
    public static void updateStateRegion(String stateID) {
        String stateName = databaseDB.getString("marinbay.state." + stateID + ".name");
        Block sign = Main.getInstance().getLocation(databaseDB.getString("marinbay.state." + stateID + ".capital")).getBlock();
        HallTable stateHT = new HallTable(sign, stateName, false);

        removeRegion(stateName);
        createRegionH(stateHT);
    }



    // Ставит постройку, при этом убирая предыдущую
    public static void pasteNewBuilding(HallTable ht, String schematic, boolean undoOld) {
        String htName = ht.getName();
        int direction = ht.getSignDirection();
        boolean isState = ht.getStatus().equals("state");
        Sign sign = (Sign) ht.getSign().getState();

        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        BlockVector3 bv = BlockVector3.at(x, y, z);

        // Удаляем старую постройку только если это надо
        if (undoOld) {
            SchematicPaster.placeSchematic("void.schem", bv, direction, false);
        }
        // Ставим новую
        SchematicPaster.placeSchematic(schematic, bv, direction, true);

        sign = (Sign) ht.getSign().getState();
        // Меняем табличку на нормальную
        String line = "Город";
        if (isState) {
            line = "Государство";
        }
        sign.setLine(1, line);
        sign.setLine(2, htName);
        sign.update();
    }
    public static void pasteNewBuildingB(Barrack barrack, String schematic, boolean undoOld) {
        String barrackName = barrack.getName();
        int direction = barrack.getSignDirection();
        Sign sign = (Sign) barrack.getSign().getState();

        int x = sign.getX();
        int y = sign.getY();
        int z = sign.getZ();
        BlockVector3 bv = BlockVector3.at(x, y, z);

        // Удаляем старую постройку только если это надо
        if (undoOld) {
            SchematicPaster.placeSchematic("voidKazarm.schem", bv, direction, false);
        }
        // Ставим новую
        SchematicPaster.placeSchematic(schematic, bv, direction, true);

        // Меняем табличку на нормальную
        sign = (Sign) barrack.getSign().getState();
        sign.setLine(1, "Казарма");
        sign.update();
    }

    // Подсчитывает количество человек в государстве исключая генерала
    public static int howManyCitizensInStateWithoutGeneral(String stateID) {
        String[] citizens = databaseDB.getString("marinbay.state." + stateID + ".citisens").split(", ");
        return citizens.length-1;
    }

    // Получаем все никнеймы игроков в сети без государства
    public static String[] getAllFreePlayerWithoutState() {
        ArrayList<String> players = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            String pName = p.getName();
            if (!databaseDB.isSet("marinbay.player." + pName.toLowerCase() + ".state")) {
                players.add(pName);
            }
        }
        return players.toArray(new String[0]);
    }

    // Метод создания инвентаря с головами игроков
    public static void createPlayersHeadsInventory(Inventory inventory, String stateID, int number, String inv) {
        String[] players;
        if (inv.equals("kick") || inv.equals("info")) {
            players = databaseDB.getString("marinbay.state." + stateID + ".citisens").split(", ");
        } else if (inv.equals("rankRaise") || inv.equals("rankDowngrade")) {
            players = getRankPlayers(stateID, inv);
        } else {
            // Берем всех игроков, которые в онлайне и при этом не состоят в государстве
            players = getAllFreePlayerWithoutState();
        }
        ChatColor playerColor = ChatColor.GRAY;
        String forSetLore = null;

        if (inv.equals("kick")) {
            playerColor = ChatColor.RED;
            forSetLore = "civilianKick";
        } else if (inv.equals("info")) {
            playerColor = ChatColor.AQUA;
        } else if (inv.equals("infoFree")) {
            playerColor = ChatColor.AQUA;
        } else if (inv.equals("invite")) {
            playerColor = ChatColor.GOLD;
            forSetLore = "invite";
        } else if (inv.equals("rankRaise")) {
            playerColor = ChatColor.AQUA;
        } else if (inv.equals("rankDowngrade")) {
            playerColor = ChatColor.RED;
        }

        // Начинаем сразу со второго игрока, так как игрок под
        // индексом 0 - это глава государства и его не надо показывать
        if (number == 1) {
            // если 0, то король тоже будет отображаться
            int iRavno = 0;
            if (inv.equals("kick")) {
                // Если же открываем кик инвентарь, то король не отображается
                iRavno = 1;
            }

            for (int i = iRavno; i < players.length; i++) {
                SkullMeta skull = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
                skull.setOwningPlayer(Bukkit.getPlayer(players[i]));
                skull.setDisplayName(playerColor + players[i]);
                if(inv.equals("info")) {
                    skull.setLore(getCivilianLoreInfo(players[i], "civil"));
                } else if (inv.equals("infoFree")) {
                    skull.setLore(getCivilianLoreInfo(players[i], "free"));
                } else if (inv.equals("rankRaise")) {
                    skull.setLore(getLoreForRankRaiseNDowngrade(players[i], "raise"));
                } else if (inv.equals("rankDowngrade")) {
                    skull.setLore(getLoreForRankRaiseNDowngrade(players[i], "downgrade"));
                } else {
                    skull.setLore(getItemLoreForHallTable(forSetLore));
                }

                ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                item.setItemMeta(skull);
                inventory.setItem(i - iRavno, item);

                // Если i уже 26, то добавляется кнопка далее и заканчивается for
                // P.S. По логике, должно быть 25, но так как мы отнимаем главу государства, получается 26
                // Так как без короля - 26, а с королём - 25, прибавляем эту переменную
                if (i == 24 + iRavno) {
                    String displayName = getItemNameForHallTable(37, "OCHKO");
                    ArrayList<String> lore = getItemLoreForHallTable("daleeButton");

                    // HONEYCOMB - kickDaleeButton
                    // LEATHER - info
                    // SUGAR - infoFree
                    // CHOURS_FRUIT - invite
                    Material matIt = null;
                    if (inv.equals("kick")) {
                        matIt = Material.HONEYCOMB;
                    } else if (inv.equals("info")){
                        matIt = Material.LEATHER;
                    } else if (inv.equals("infoFree")){
                        matIt = Material.SUGAR;
                    } else if (inv.equals("rankRaise")) {
                        matIt = Material.FEATHER;
                    } else if (inv.equals("rankDowngrade")) {
                        matIt = Material.BOOK;
                    } else {
                        matIt = Material.CHORUS_FRUIT;
                    }

                    ItemStack daleeItem = getMenuItem(matIt, 1, displayName, lore, 1);
                    inventory.setItem(25, daleeItem);
                    inventory.setItem(26, daleeItem);
                    break;
                }
            }
        }
        if (number == 2) {
            // если 25, то король тоже будет отображаться
            int iRavno = 25;
            if (inv.equals("kick")) {
                // Если же открываем кик инвентарь, то король не отображается
                iRavno = 26;
            }
            // Открываем кнопку далее
            for (int i = iRavno; i < players.length; i++) {
                SkullMeta skull = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.PLAYER_HEAD);
                skull.setOwningPlayer(Bukkit.getPlayer(players[i]));
                skull.setDisplayName(playerColor + players[i]);
                if(inv.equals("info")) {
                    skull.setLore(getCivilianLoreInfo(players[i], "civil"));
                } else if (inv.equals("infoFree")) {
                    skull.setLore(getCivilianLoreInfo(players[i], "free"));
                } else if (inv.equals("rankRaise")) {
                    skull.setLore(getLoreForRankRaiseNDowngrade(players[i], "raise"));
                } else if (inv.equals("rankDowngrade")) {
                    skull.setLore(getLoreForRankRaiseNDowngrade(players[i], "downgrade"));
                } else {
                    skull.setLore(getItemLoreForHallTable(forSetLore));
                }

                ItemStack item = new ItemStack(Material.PLAYER_HEAD, 1);
                item.setItemMeta(skull);
                inventory.setItem(i - iRavno, item);
            }
        }
    }
    // Функция нужна, чтобы головы были отсортированы
    // Приближенные идут первые, дальше губеры и только потом граждане
    private static String[] getRankPlayers(String stateID, String raiseDowngrade) {
        ArrayList<String> players = new ArrayList<>();

        String closers = databaseDB.getString("marinbay.state." + stateID + ".ranks.closer");
        String gubers = databaseDB.getString("marinbay.state." + stateID + ".ranks.guber");

        if (closers != null && !closers.equals("")) {
            players.addAll(Arrays.asList(closers.split(", ")));
        }
        if (gubers != null && !gubers.equals("")) {
            players.addAll(Arrays.asList(gubers.split(", ")));
        }
        if (raiseDowngrade.equals("rankRaise")) {
            String citizens = databaseDB.getString("marinbay.state." + stateID + ".ranks.citizen");
            if (citizens != null && !citizens.equals("")) {
                players.addAll(Arrays.asList(citizens.split(", ")));
            }
        }

        return players.toArray(new String[0]);
    }
    // Метод создания инвентаря с головами построек
    public static void createBuildingHeadsInventory(Inventory inventory, String stateID, int number) {
        // Создаём предмет-государство
        String stateHTName = databaseDB.getString("marinbay.state." + stateID + ".name");
        ItemStack item = changeMetaRatusha(ratushaState, stateHTName);
        inventory.setItem(0, item);

        // Создаём предметы-города
        int amount_halltables = 0;
        String halltables = databaseDB.getString("marinbay.state." + stateID + ".buildings.halltables");
        String barracks = databaseDB.getString("marinbay.state." + stateID + ".buildings.barracks");

        // Это уже второй инвентарь, открывается при нажатии на кнопку далее
        if (number == 2) {
            // Получаем список всех построек - и halltables и barracks
            List<String> allBuildings = new ArrayList<>();
            if (halltables != null && !halltables.equals("")) {
                String[] namesOfHTS = halltables.split(", ");
                allBuildings.addAll(Arrays.asList(namesOfHTS));
            }
            if (barracks != null && !barracks.equals("")) {
                String[] namesOfBarracks = barracks.split(", ");
                allBuildings.addAll(Arrays.asList(namesOfBarracks));
            }

            ItemStack itemCity;
            ItemStack itemBarrack;
            for (int i = 24; i < allBuildings.size(); i++) {
                // Создаём предмет в зависимости барак ли это
                if (allBuildings.get(i).startsWith("barrack")) {
                    itemBarrack = changeMetaBarrack(kazarm, allBuildings.get(i));
                    inventory.setItem(i-24, itemBarrack);
                } else {
                    itemCity = changeMetaRatusha(ratushaCity, allBuildings.get(i));
                    inventory.setItem(i-24, itemCity);
                }

                // Создаём кнопку далее
                if (i-24 > 23) {
                    String displayName = getItemNameForHallTable(37, "OCHKO");
                    ArrayList<String> lore = getItemLoreForHallTable("daleeButton");

                    ItemStack daleeItem = getMenuItem(Material.APPLE, 1, displayName, lore, 1);
                    inventory.setItem(25, daleeItem);
                    inventory.setItem(26, daleeItem);
                    return;
                }
            }
            return;
        }

        // Ниже первый инвентарь
        if (halltables != null && !halltables.equals("")) {
            // При этом даже если элемент один и ниже регекса не будет, то split возвратит
            // Полную строку, без разделения в количестве одной штуки
            String[] namesOfHTS = halltables.split(", ");
            amount_halltables = namesOfHTS.length;

            ItemStack itemCity;
            for (int i = 0; i < namesOfHTS.length; i++) {
                itemCity = changeMetaRatusha(ratushaCity, namesOfHTS[i]);
                inventory.setItem(i+1, itemCity);

                // Создаём кнопку далее
                if (i > 23) {
                    String displayName = getItemNameForHallTable(37, "OCHKO");
                    ArrayList<String> lore = getItemLoreForHallTable("daleeButton");

                    ItemStack daleeItem = getMenuItem(Material.GUNPOWDER, 1, displayName, lore, 1);
                    inventory.setItem(25, daleeItem);
                    inventory.setItem(26, daleeItem);
                    return;
                }
            }
        }

        // Создаём предметы-казармы
        if (barracks != null && !barracks.equals("")) {
            String[] namesOfBarracks = barracks.split(", ");

            ItemStack itemBarrack;
            for (int i = 0; i < namesOfBarracks.length; i++) {
                itemBarrack = changeMetaBarrack(kazarm, namesOfBarracks[i]);
                inventory.setItem(i + 1 + amount_halltables, itemBarrack);

                // Создаём кнопку далее
                if (i > 23) {
                    String displayName = getItemNameForHallTable(37, "OCHKO");
                    ArrayList<String> lore = getItemLoreForHallTable("daleeButton");

                    ItemStack daleeItem = getMenuItem(Material.HONEYCOMB, 1, displayName, lore, 1);
                    inventory.setItem(25, daleeItem);
                    inventory.setItem(26, daleeItem);
                    return;
                }
            }
        }
    }
    // Возвращает itemStack с уже нужным описанием и названием предмета
    private static ItemStack changeMetaRatusha(ItemStack item, String htName) {
        ItemMeta meta = item.getItemMeta();
        if (htName.equals("sosiska")) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "СОСИСКА БЛЯТЬ!");
            item.setItemMeta(meta);
            return item;
        }

        HallTable ht = HallTable.getHTByName(htName);
        String htStatus = ht.getStatus();

        // Получаем время создания постройки
        long old_time = ht.getTimeOfCreate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(old_time);

        // Score постройки
        int score = ht.getScore();

        ChatColor colorB = ChatColor.AQUA;
        ChatColor colorW = ChatColor.WHITE;

        int notFormativeMinute = calendar.get(Calendar.MINUTE);
        String forrmativeMinute = String.valueOf(notFormativeMinute);
        if (notFormativeMinute < 10) {
            forrmativeMinute = "0" + notFormativeMinute;
        }
        // Создаём лор
        List<String> lore = new ArrayList<>();
        lore.add(0, colorW + "Информация о ратуше");
        lore.add(1, colorB + "Название: " + colorW + htName);
        lore.add(2, colorB + "Уровень постройки: " + colorW + score);
        lore.add(3, colorB + "Время создания: " + colorW + calendar.get(Calendar.DAY_OF_MONTH) + "." +
                "0" + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR) + " | " +
                (calendar.get(Calendar.HOUR_OF_DAY)) + ":" + forrmativeMinute);

        if (htStatus.equals("state")) {
            // Получаем максимум жителей
            lore.add(4, colorB + "Макс. жителей: " + colorW + ht.getMaximumCitiznes());
        } else {
            // Получаем сколько жителей добавляет
            lore.add(4, colorB + "Добавляет: " + colorW +  "1 жителя");
        }

        meta.setLore(lore);
        if (htStatus.equals("state")) {
            meta.setDisplayName(ChatColor.GOLD + htName);
        } else {
            meta.setDisplayName(ChatColor.YELLOW + htName);
        }
        item.setItemMeta(meta);
        return item;
    }
    // Возвращает itemStack с уже нужным описанием и названием предмета
    private static ItemStack changeMetaBarrack(ItemStack item, String barrackName) {
        ItemMeta meta = item.getItemMeta();
        Barrack barrack = Barrack.getBarrackByName(barrackName);

        // Получаем время создания постройки
        long old_time = barrack.getTimeOfCreate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(old_time);

        // Score постройки
        int score = barrack.getScore();

        ChatColor colorB = ChatColor.AQUA;
        ChatColor colorW = ChatColor.WHITE;

        int notFormativeMinute = calendar.get(Calendar.MINUTE);
        String forrmativeMinute = String.valueOf(notFormativeMinute);
        if (notFormativeMinute < 10) {
            forrmativeMinute = "0" + notFormativeMinute;
        }
        // Создаём лор
        List<String> lore = new ArrayList<>();
        lore.add(0, colorW + "Информация о казарме");
        lore.add(1, colorB + "Принадлежит: " + colorW + barrack.getNameOwner());
        lore.add(2, colorB + "Уровень постройки: " + colorW + score);
        lore.add(3, colorB + "Время создания: " + colorW + calendar.get(Calendar.DAY_OF_MONTH) + "." +
                "0" + (calendar.get(Calendar.MONTH) + 1) + "." + calendar.get(Calendar.YEAR) + " | " +
                (calendar.get(Calendar.HOUR) + 14) + ":" + forrmativeMinute);
        lore.add(4, colorB + "Добавляет: " + colorW + barrack.getPlusAmountOfCitizens() + " жителя(ей)");

        meta.setLore(lore);
        meta.setDisplayName(ChatColor.RED + "Казарма");
        item.setItemMeta(meta);
        return item;
    }

    // Отсылает игроку сообщение инвайта, если кликнет, то его добавляет в государство
    public static void sendInviteMessage(String generalName, String stateID, Player playerToInvite) {
        String nameState = databaseDB.getString("marinbay.state." + stateID + ".name");
        TextComponent message = new TextComponent(ChatColor.AQUA + "[MarinBay] " + ChatColor.WHITE +
                "Государь " + ChatColor.AQUA + generalName + ChatColor.WHITE + " приглашает тебя вступить в " +
                "государство " + ChatColor.AQUA + nameState + ChatColor.WHITE + "." +"\nВыбери действие: [");
        TextComponent allow = new TextComponent(ChatColor.AQUA + "" + ChatColor.BOLD + "Принять");
        TextComponent deny = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "Отклонить");

        allow.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.WHITE +
                "Нажмите, чтобы принять приглашение")));
        allow.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/invite 567 "+
                playerToInvite.getName() + " " + stateID));

        deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(ChatColor.WHITE +
                "Нажмите, чтобы отклонить приглашение")));
        deny.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/desinvite 567 "+
                playerToInvite.getName()));

        message.addExtra(allow);
        message.addExtra("/");
        message.addExtra(deny);
        message.addExtra("]");
        playerToInvite.spigot().sendMessage(message);
    }

    // Проверяет прошло ли 3 часа с того момента, как ты пригласил игрока в своё государтсво
    public static boolean checkIf3HoursPassed(String stateID, Player player) {
        if (!inviteDB.isSet(player.getName() + "." + stateID)) {
            return true;
        }

        long oldTime = Long.parseLong(inviteDB.getString(player.getName() + "." + stateID));
        if ((System.currentTimeMillis() - oldTime)/1000/60/60 < 3) {
            return false;
        }
        return true;
    }

    // Удаляет гексагоны города и удаляет их из государства (по сути полностью удаляет город)
    // !! deleteHexagons - задаёт удаление гексагонов. Если ввести 1, то удаление города
    // происходит с удалением всех построек и всех гексагонов, если же 0, то удаление без
    // удаления гексагонов и построек
    public static void deleteCity(String cityID, HallTable ht, int deleteHexagons) {
        String hexagons = databaseDB.getString("marinbay.city." + cityID + ".hexagons");
        String stateID = databaseDB.getString("marinbay.city." + cityID + ".owner");

        // deleteHexagons = 1 - удаляем постройки и тайлы, относящиеся к городу
        if (deleteHexagons == 1) {
            // Удаляем сами гексагоны (если они вообще есть)
            if (hexagons != null) {
                String[] hexagonsToRemove;
                if (hexagons.split(" ") == null) {
                    // Если гексагон только один, то метод сплит вызовет null
                    databaseDB.remove("marinbay.hexagons." + hexagons);
                } else {
                    hexagonsToRemove = hexagons.split(" ");
                    for (String hex : hexagonsToRemove) {
                        databaseDB.remove("marinbay.hexagons." + hex);
                    }
                }

                // Удаляем гексагоны из соотвествующей строки у государства
                String stateHexagons = databaseDB.getString("marinbay.state." + stateID + ".hexagons");
                List<String> allHexagons = new ArrayList<>();
                allHexagons.addAll(Arrays.asList(stateHexagons.split(" ")));

                hexagonsToRemove = hexagons.split(" ");
                for (String hex : hexagonsToRemove) {
                    allHexagons.remove(hex);
                }
                String newHexagons = String.join(" ", allHexagons);
                databaseDB.write("marinbay.state." + stateID + ".hexagons", newHexagons);
            }

            // Если у города были свои постройки, то удаляем их
            if (databaseDB.isSet("marinbay.city." + cityID + ".buildings.barracks")) {
                if (!databaseDB.getString("marinbay.city." + cityID + ".buildings.barracks").equals("")) {
                    String barracks = databaseDB.getString("marinbay.city." + cityID + ".buildings.barracks");
                    if (barracks.split(", ") == null) {
                        // Если барак только один, то метод сплит вызовет null
                        Barrack.removeFromDBs(barracks);
                    } else {
                        String[] barracksToRemove = barracks.split(", ");
                        for (String bar : barracksToRemove) {
                            Barrack.removeFromDBs(bar);
                        }
                    }
                }
            }
        }

        String name = databaseDB.getString("marinbay.city." + cityID + ".name");
        String hexagonOfBuilding = databaseDB.getString("marinbay.city." + cityID + ".hexagonOfBuilding");

        // Удаляем строку building, в котором был город
        databaseDB.remove("marinbay.hexagons." + hexagonOfBuilding + ".building");
        databaseDB.remove("marinbay.hexagons." + hexagonOfBuilding + ".cityID");
        // Удаляем city из il_database
        ilDB.remove("ids.city_" + name);
        // Удаляем этот city из cities
        databaseDB.removeOneElement("marinbay.state." + stateID + ".cities", cityID, " ");
        // Удаляем halltable города из общего списка холтейблов
        databaseDB.removeOneElement("marinbay.state." + stateID + ".buildings.halltables", ht.getName(), ", ");
        // Удаляем из билдинг датабазе
        buildingsDB.remove("halltables." + name);
        // Человеку, который создал город пишем NO, так как город уже удален
        String ownerOfTheCity = databaseDB.getString("marinbay.city." + cityID + ".general");
        databaseDB.write("marinbay.player." + ownerOfTheCity + ".placeCity", "NO");
        // Удаляем сам город
        databaseDB.remove("marinbay.city." + cityID);
        // Удаляем регион стола
        removeRegionTable(ht.getName());
        // Убираем одного гражданина из общего числа государства
        City.minusOneCitizensAmount(stateID);

        // Обновляем регион основного государства
        updateStateRegion(stateID);
    }

    public static ItemStack getCustomSkull(String url) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (url.isEmpty()) return head;

        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);

        profile.getProperties().put("textures", new Property("textures", url));

        try {
            Method mtd = skullMeta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            mtd.setAccessible(true);
            mtd.invoke(skullMeta, profile);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            ex.printStackTrace();
        }

        head.setItemMeta(skullMeta);
        return head;
    }

    public static BlockVector2[] getVectorOfPoints(Point[] points) {
        List<BlockVector2> bv = new ArrayList<>();

        for(Point p : points) {
            BlockVector2 vector = BlockVector2.at(p.getX(),p.getY());
            bv.add(vector);
        }

        return bv.toArray(new BlockVector2[0]);
    }
}
