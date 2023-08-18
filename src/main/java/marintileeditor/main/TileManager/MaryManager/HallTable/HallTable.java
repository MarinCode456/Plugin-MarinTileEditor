package marintileeditor.main.TileManager.MaryManager.HallTable;

import com.sk89q.worldedit.math.BlockVector3;
import marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper;
import marintileeditor.main.TileManager.MaryManager.MarinBayUtil;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.ID.ID;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// Обработан
public class HallTable implements InventoryHolder {

    // Основные переменные
    private final Block sign;
    private Inventory inventory;
    private final HallTableBody body;
    private String id;
    private String name;
    private String status;
    public static String htName;
    String stateID;

    // Вещи которые дропаются, если ломаешь стол
    public static ItemStack breadItem = new ItemStack(Material.BREAD, 32);
    public static ItemStack woodItem = new ItemStack(Material.BIRCH_LOG, 64);
    public static ItemStack smoothStoneItem = new ItemStack(Material.SMOOTH_STONE, 64);
    public static ItemStack diamondItem = new ItemStack(Material.DIAMOND, 15);

    // Тех часть
    private static Logger log = Bukkit.getLogger();

    // Датабазы
    private static Database buildingsDB = Database.BUILDINGS_DATABASE;
    private static Database databaseDB = Database.STAT_DATABASE;
    private static Database idDB = Database.ID_LINK_DATABASE;

    // Болванчик-лор для предметов
    ArrayList<String> lore = new ArrayList<>();

    // Слоты по порядку, в которых мы меняем вещи
    int[] slotesFirst = {
            0, 1, 2,
            9, 10, 11,
            18, 19, 20

    };
    int[] slotesSecond = {
            3, 4, 5,
            12, 13, 14,
            21, 22, 23

    };
    int[] slotesThird = {
            6, 7, 8,
            15, 16, 17,
            24, 25

    };
    int[] slotesThirdForCityMenu = {
            6, 7, 8,
            15, 16, 17,
            24, 25, 26

    };
    int [] slotesCreateButton = {
            26
    };
    int[] choiceRatushaSlotesFirst = {
            1, 2, 3,
            10, 11, 12,
            19, 20, 21

    };
    int[] choiceRatushaSlotesSecond = {
            5, 6, 7,
            14, 15, 16,
            23, 24, 25

    };

    // Имена предметов
    String displayName1;
    String displayName2;
    String displayName3;
    String buttonName;

    // Значения из базы данных - положен ли предмет или нет
    String firstItemOKNOT;
    String secondItemOKNOT;
    String thirdItemOKNOT;

    // Сами предметы
    ItemStack firstItem;
    ItemStack secondItem;
    ItemStack thirdItem;
    ItemStack buttonItem;
    ItemStack deleteCityItem;

    // Разные инвентари для обработки меню

    // Инвентари государства
    public Inventory invRatushaChoice;
    public Inventory invMenuOfCity;
    public Inventory invForCitizen;
    public Inventory invUpgradeMenuCity;
    public Inventory invChangeColorRatusha;
    public Inventory invChangeColor;
    public Inventory invRank;
    // Инвентари с головами игроков
    public Inventory invTypeOfPlayers;
    public Inventory invCivilianPlayer;
    public Inventory invKickCivilian;
    public Inventory invInfoCivilian;
    public Inventory invFreePlayer;
    public Inventory invInfoFree;
    public Inventory invInviteFree;
    public Inventory invRankRaise;
    public Inventory invRankDowngrade;
    // Инвентари с головами построек
    public Inventory invUpgradesBuildings;

    // name - название города и столика в buildingsDB
    public HallTable(Block sign, String name) {
        this.sign = sign;
        this.name = name;
        this.id = ID.getHallTableID(name);
        body = new HallTableBody(this);
    }
    // В bool я передаю false, когда мы не создаём новый хотейбл,
    // А когда берем старый. Он служит ответом на вопрос IsNew? - false
    // При этом никаких проверок с этой переменной нет, она просто нужна,
    // Чтобы создать новый конструктор
    public HallTable(Block sign, String name, boolean isNew) {
        this.sign = sign;
        this.name = name;
        this.id = ID.getHallTableID(name);
        body = new HallTableBody(this);

        // Получаем статус
        status = buildingsDB.getString("halltables." + name + ".status");

        // Определяем инвентарь для холтейбла
        inventory = setNgetInventory();

        // Определяем stateID
        if (status.equals("state")) {
            stateID = buildingsDB.getString("halltables." + name + ".id");
        } else if (status.equals("city")) {
            String cityID = buildingsDB.getString("halltables." + name + ".id");
            stateID = databaseDB.getString("marinbay.city." + cityID + ".owner");
        }
    }

    // Функция записи всех переменных в halltable
    // Вызывается только при созданиия холтейбл
    private void  writeAllDataHalltable() {
        String signLocation = Main.getInstance().getStringFrom(sign.getLocation());

        // Записываем строчки апгрейдов
        buildingsDB.write("halltables." + name + ".upgrade.score", String.valueOf(0));
        buildingsDB.write("halltables." + name + ".upgrade.firstItem", "NOT");
        // Для второго предмета NOT0 - значит не загружена древесина
        // NOT1 - значит не загружен камень
        // ОК - загружена и древесина, и камень
        buildingsDB.write("halltables." + name + ".upgrade.secondItem", "NOT0");
        buildingsDB.write("halltables." + name + ".upgrade.thirdItem", "NOT");

        // Записываем статус
        buildingsDB.write("halltables." + name + ".status", status);

        // Записываем локацию таблички
        buildingsDB.write("halltables." + name + ".signLocation", signLocation);
    }

    // Геттеры и сеттеры
    @Override
    public Inventory getInventory() {
        return inventory;
    }
    public Block getSign() {
        return sign;
    }
    public int getScore() { return Integer.parseInt(buildingsDB.getString("halltables." + name + ".upgrade.score"));}
    public Location getSignLocation() {
        Location signLocation = Main.getInstance().getLocation(buildingsDB.getString("halltables." + name + ".signLocation"));
        return signLocation;
    }
    public String getName() { return name; }
    public String getCreator () {
        return buildingsDB.getString("halltables." + name + ".creator");
    }
    public HallTableBody getBody() { return body; }
    public int getSignDirection() { return Integer.parseInt(buildingsDB.getString("halltables." + name + ".direction")); }
    public String getStatus() { return buildingsDB.getString("halltables." + name + ".status"); }
    public String getID() {return id;}
    public String getStateID() {return stateID;}
    public String toString() {
        return name;
    }
    public long getTimeOfCreate() { return Long.parseLong(buildingsDB.getString("halltables." + name + ".timeOfCreate"));}
    public int getMaximumCitiznes() {
        if (status.equals("state")) {
            return Integer.parseInt(databaseDB.getString("marinbay.state." + stateID + ".citizensAmount"));
        }
        return 0;
    }
    public int getBuildingsAmount() {
        List<String> buildings = new ArrayList<>();

        String halltables = databaseDB.getString("marinbay.state." + stateID + ".buildings.halltables");
        String barracks = databaseDB.getString("marinbay.state." + stateID + ".buildings.barracks");
        if (halltables != null && !halltables.equals("")) {
            buildings.addAll(Arrays.asList(halltables.split(", ")));
        }
        if (barracks != null && !barracks.equals("")) {
            buildings.addAll(Arrays.asList(barracks.split(", ")));
        }

        return buildings.size();
    }
    public String getGeneral() {
        return databaseDB.getString("marinbay.state." + stateID + ".general");
    }
    public void setCreator(String newCreator) {
        buildingsDB.write("halltables." + name + ".creator", newCreator);
    }

    // Отсек инвентарей
    public Inventory setNgetInventory() {
        // Если мэрия еще не создана, то в upgrades у неё ноль
        int upgradeScore = Main.getInstance().getUpgradeScore("halltables." + name + ".upgrade.score");
        String inventoryName;

        // Получаем все OK NOT от всех предметов
        firstItemOKNOT = buildingsDB.getString("halltables." + name + ".upgrade.firstItem");
        secondItemOKNOT = buildingsDB.getString("halltables." + name + ".upgrade.secondItem");
        thirdItemOKNOT = buildingsDB.getString("halltables." + name + ".upgrade.thirdItem");

        // (0) Ратуша не создана, в неё только начинают заполнятся ресурсы
        if (upgradeScore == 0) {
            displayName1 = InventoryHelper.getItemNameForHallTable(1, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(2, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(3, thirdItemOKNOT);

            // Определяем имя инвентаря, если второй слот еще на первой стадии, то
            // будет первая версия инвентаря, если же это NOT1 или OK, то вторая
            if (secondItemOKNOT.equals("NOT1") || secondItemOKNOT.equals("OK")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>∐";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⨊";
            }
            inventory = Bukkit.getServer().createInventory(this, 27, inventoryName);

            // Устанавливаем название кнопки создания
            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = InventoryHelper.getCreateButton(status, "OK");
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = InventoryHelper.getCreateButton(status, "NO");
                lore = InventoryHelper.getButtonLoreForHallTable("NOT");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            }

            // Устанавливаем предметы
            lore = InventoryHelper.getItemLoreForHallTable(firstItemOKNOT);
            firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

            lore = InventoryHelper.getItemLoreForHallTable(secondItemOKNOT);
            secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

            lore = InventoryHelper.getItemLoreForHallTable(thirdItemOKNOT);
            thirdItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName3, lore, 1);


            InventoryHelper.setItemForManySlotes(slotesFirst, inventory, firstItem);
            InventoryHelper.setItemForManySlotes(slotesSecond, inventory, secondItem);
            InventoryHelper.setItemForManySlotes(slotesThird, inventory, thirdItem);
            InventoryHelper.setItemForManySlotes(slotesCreateButton, inventory, buttonItem);

            return inventory;
        }


        // Ратуша создана, поэтому открывается это меню
        if (upgradeScore >= 1) {
            if (status.equals("state")) {
                displayName1 = InventoryHelper.getItemNameForHallTable(5, "Button1");
                displayName2 = InventoryHelper.getItemNameForHallTable(5, "Button2");
                displayName3 = InventoryHelper.getItemNameForHallTable(5, "Button3");

                inventoryName = ChatColor.WHITE + "<shift:-8>ฅ";
                invMenuOfCity = Bukkit.getServer().createInventory(this, 27, inventoryName);

                // Устанавливаем предметы
                lore = InventoryHelper.getItemLoreForHallTable("menuCityButton1");
                firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

                lore = InventoryHelper.getItemLoreForHallTable("menuCityButton2");
                secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

                lore = InventoryHelper.getItemLoreForHallTable("menuCityButton3");
                thirdItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName3, lore, 1);

                InventoryHelper.setItemForManySlotes(slotesFirst, invMenuOfCity, firstItem);
                InventoryHelper.setItemForManySlotes(slotesSecond, invMenuOfCity, secondItem);
                InventoryHelper.setItemForManySlotes(slotesThirdForCityMenu, invMenuOfCity, thirdItem);
            } else {
                displayName1 = InventoryHelper.getItemNameForHallTable(5, "Button4");
                displayName2 = InventoryHelper.getItemNameForHallTable(5, "Button3");

                inventoryName = ChatColor.WHITE + "<shift:-8>⊲";
                invMenuOfCity = Bukkit.getServer().createInventory(this, 27, inventoryName);

                // Устанавливаем предметы
                lore = InventoryHelper.getItemLoreForHallTable("menuCityButton4");
                firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

                lore = InventoryHelper.getItemLoreForHallTable("menuCityButton3");
                secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

                lore = InventoryHelper.getItemLoreForHallTable("deleteCityButton");
                displayName3 = InventoryHelper.getItemNameForHallTable(39, "BUTTON");

                // Кнопки Город и Улучшения
                InventoryHelper.setItemForManySlotes(choiceRatushaSlotesFirst, invMenuOfCity, firstItem);
                InventoryHelper.setItemForManySlotes(choiceRatushaSlotesSecond, invMenuOfCity, secondItem);

                // Кнопка для удаления города
                deleteCityItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName3, lore, 1);
                invMenuOfCity.setItem(26, deleteCityItem);
            }

            return invMenuOfCity;
        }

        return inventory;
    }

    public void open(Player player) {
        inventory = setNgetInventory();
        // Если ещё никто не зашёл в этот инвентарь, то он открывается
        if (inventory.getViewers().isEmpty()) {
            player.openInventory(inventory);
            playOpeningSound();
        }
    }

    public Inventory getCitizenInventory() {
        invForCitizen = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "⋪");

        displayName1 = InventoryHelper.getItemNameForHallTable(40, "LEFT");
        lore = InventoryHelper.getItemLoreForHallTable("citizenHallTable");
        firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);
        InventoryHelper.setItemForManySlotes(slotesSecond, invForCitizen, firstItem);
        return invForCitizen;
    }

    public void openForCitizen(Player player) {
        invForCitizen = getCitizenInventory();
        player.openInventory(invForCitizen);
    }

    // Инвентарь для улучшения ратуши
    // Работает, если ратуша хотя бы первого уровня
    public Inventory getUpgradeRatushaInventory() {
        int upgradeScore = Main.getInstance().getUpgradeScore("halltables." + name + ".upgrade.score");
        String inventoryName = null;

        // Общие строки для любых условий
        firstItemOKNOT = buildingsDB.getString("halltables." + name + ".upgrade.firstItem");
        secondItemOKNOT = buildingsDB.getString("halltables." + name + ".upgrade.secondItem");
        thirdItemOKNOT = buildingsDB.getString("halltables." + name + ".upgrade.thirdItem");

        if (upgradeScore == 1) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>∔";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>∸";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(6, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(7, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(8, thirdItemOKNOT);
        } else if (upgradeScore == 2) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>≂";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊕";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(11, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(12, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(13, thirdItemOKNOT);
        } else if (upgradeScore == 3) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊖";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊗";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(14, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(15, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(16, thirdItemOKNOT);
        } else if (upgradeScore == 4) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊘";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊙";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(17, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(18, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(19, thirdItemOKNOT);
        } else if (upgradeScore == 5) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊚";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊛";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(20, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(21, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(22, thirdItemOKNOT);
        } else if (upgradeScore == 6) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊚";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⊛";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(23, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(24, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(25, thirdItemOKNOT);
        } else if (upgradeScore == 7) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⋄";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⋇";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(26, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(27, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(28, thirdItemOKNOT);
        } else if (upgradeScore == 8) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⋆";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⋋";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(29, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(30, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(31, thirdItemOKNOT);
        } else if (upgradeScore == 9) {
            if (secondItemOKNOT.equals("NOT0")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⋆";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⋋";
            }

            displayName1 = InventoryHelper.getItemNameForHallTable(32, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForHallTable(33, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForHallTable(34, thirdItemOKNOT);
        }



        invUpgradeMenuCity = Bukkit.getServer().createInventory(this, 27, inventoryName);

        // Устанавливаем название кнопки создания
        if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
            buttonName = ChatColor.AQUA + "Улучшить ратушу";
            lore = InventoryHelper.getButtonLoreForHallTable("UPGRADE_OK");
            buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
        } else {
            buttonName = ChatColor.RED + "Вы пока не можете улучшить ратушу";
            lore = InventoryHelper.getButtonLoreForHallTable("UPGRADE_NOT");
            buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
        }

        // Устанавливаем предметы
        lore = InventoryHelper.getItemLoreForHallTable(firstItemOKNOT);
        firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable(secondItemOKNOT);
        secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable(thirdItemOKNOT);
        thirdItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName3, lore, 1);


        InventoryHelper.setItemForManySlotes(slotesFirst, invUpgradeMenuCity, firstItem);
        InventoryHelper.setItemForManySlotes(slotesSecond, invUpgradeMenuCity, secondItem);
        InventoryHelper.setItemForManySlotes(slotesThird, invUpgradeMenuCity, thirdItem);
        InventoryHelper.setItemForManySlotes(slotesCreateButton, invUpgradeMenuCity, buttonItem);

        return invUpgradeMenuCity;
    }

    public void openUpgradeRatushaInventory(Player player) {
        invUpgradeMenuCity = getUpgradeRatushaInventory();
        if (invUpgradeMenuCity.getViewers().isEmpty()) {
            player.openInventory(invUpgradeMenuCity);
        }
    }

    public Inventory getRatushaChoiceInventory() {
        int upgradeScore = Main.getInstance().getUpgradeScore("halltables." + name + ".upgrade.score");

        if (upgradeScore == 0) {
            // Такое название, чтобы корректно отображался интерфейс
            invRatushaChoice = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ฃ");

            displayName1 = InventoryHelper.getItemNameForHallTable(4, "LEFT");
            displayName2 = InventoryHelper.getItemNameForHallTable(4, "RIGHT");

            lore = InventoryHelper.getItemLoreForHallTable("ratushaChoiceLeft");
            firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

            lore = InventoryHelper.getItemLoreForHallTable("ratushaChoiceRight");
            secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

            InventoryHelper.setItemForManySlotes(choiceRatushaSlotesFirst, invRatushaChoice, firstItem);
            InventoryHelper.setItemForManySlotes(choiceRatushaSlotesSecond, invRatushaChoice, secondItem);
        }
        return invRatushaChoice;
    }

    public void openInvRatushaChoice(Player player) {
        invRatushaChoice = getRatushaChoiceInventory();
        player.openInventory(invRatushaChoice);
    }

    public Inventory getChangeColorNRatushaInventory() {
        invChangeColorRatusha = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>Δ");

        displayName1 = InventoryHelper.getItemNameForHallTable(9, "LEFT");
        displayName2 = InventoryHelper.getItemNameForHallTable(9, "RIGHT");

        lore = InventoryHelper.getItemLoreForHallTable("ratushaLeft");
        firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable("changeColorRight");
        secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesFirst, invChangeColorRatusha, firstItem);
        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesSecond, invChangeColorRatusha, secondItem);

        return invChangeColorRatusha;
    }

    public void openInvChangeColorRatusha(Player player) {
        invChangeColorRatusha = getChangeColorNRatushaInventory();
        player.openInventory(invChangeColorRatusha);
    }

    public Inventory getChangeColorInventory() {
        invChangeColor = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.setColors(invChangeColor);
        return invChangeColor;
    }

    public void openInvChangeColor(Player player) {
        invChangeColor = getChangeColorInventory();
        player.openInventory(invChangeColor);
    }

    public Inventory getPlayerTypeInventory() {
        // Инвентарь выбора типа игроков - граждане или свободные игроки
        invTypeOfPlayers = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ค");

        displayName1 = InventoryHelper.getItemNameForHallTable(35, "LEFT");
        displayName2 = InventoryHelper.getItemNameForHallTable(35, "RIGHT");

        lore = InventoryHelper.getItemLoreForHallTable("typePlayersLeft");
        firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable("typePlayersRight");
        secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesFirst, invTypeOfPlayers, firstItem);
        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesSecond, invTypeOfPlayers, secondItem);

        return invTypeOfPlayers;
    }

    public void openInvPlayersType(Player player) {
        invTypeOfPlayers = getPlayerTypeInventory();
        player.openInventory(invTypeOfPlayers);
    }

    public Inventory getCivilianPlayerInventory() {
        // Инвентарь выбора действия над гражданским
        invCivilianPlayer = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>จ");

        displayName1 = InventoryHelper.getItemNameForHallTable(36, "LEFT");
        displayName2 = InventoryHelper.getItemNameForHallTable(36, "MIDDLE");
        displayName3 = InventoryHelper.getItemNameForHallTable(36, "RIGHT");

        lore = InventoryHelper.getItemLoreForHallTable("civilianPlayerLeft");
        firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable("civilianPlayerMiddle");
        secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable("civilianPlayerRight");
        thirdItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName3, lore, 1);

        InventoryHelper.setItemForManySlotes(slotesFirst, invCivilianPlayer, firstItem);
        InventoryHelper.setItemForManySlotes(slotesSecond, invCivilianPlayer, secondItem);
        InventoryHelper.setItemForManySlotes(slotesThirdForCityMenu, invCivilianPlayer, thirdItem);

        return invCivilianPlayer;
    }

    public void openInvCivillianPlayer(Player player) {
        invCivilianPlayer = getCivilianPlayerInventory();
        player.openInventory(invCivilianPlayer);
    }

    public void openInvKickCivilian(Player player) {
        int citizensAmount = InventoryHelper.howManyCitizensInStateWithoutGeneral(stateID);
        if (citizensAmount <= 25) {
            invKickCivilian = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        } else {
            invKickCivilian = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ᗩ");
        }

        InventoryHelper.createPlayersHeadsInventory(invKickCivilian, stateID, 1, "kick");
        player.openInventory(invKickCivilian);
    }
    // Если для голов не хватило места в первом
    public void open2InvKickCivilian(Player player) {
        invKickCivilian = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.createPlayersHeadsInventory(invKickCivilian, stateID, 2, "kick");
        player.openInventory(invKickCivilian);
    }
    public void openInvInfoCivilian(Player player) {
        int citizensAmount = InventoryHelper.howManyCitizensInStateWithoutGeneral(stateID);
        if (citizensAmount <= 25) {
            invInfoCivilian = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        } else {
            invInfoCivilian = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ᗩ");
        }

        InventoryHelper.createPlayersHeadsInventory(invInfoCivilian, stateID, 1, "info");
        player.openInventory(invInfoCivilian);
    }
    public void open2InvInfoCivilian(Player player) {
        invInfoCivilian = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.createPlayersHeadsInventory(invInfoCivilian, stateID, 2, "info");
        player.openInventory(invInfoCivilian);
    }

    public Inventory getFreePlayerInventory() {
        // Инвентарь выбора действия над свободным игроком
        invFreePlayer = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>⑮");

        displayName1 = InventoryHelper.getItemNameForHallTable(38, "LEFT");
        displayName2 = InventoryHelper.getItemNameForHallTable(38, "RIGHT");

        lore = InventoryHelper.getItemLoreForHallTable("freePlayerLeft");
        firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable("freePlayerRight");
        secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesFirst, invFreePlayer, firstItem);
        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesSecond, invFreePlayer, secondItem);

        return invFreePlayer;
    }
    public void openInvFreePlayer(Player player) {
        invFreePlayer = getFreePlayerInventory();
        player.openInventory(invFreePlayer);
    }

    public void openInvInfoFree(Player player) {
        int citizensAmount = InventoryHelper.getAllFreePlayerWithoutState().length;
        if (citizensAmount <= 25) {
            invInfoFree = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        } else {
            invInfoFree = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ᗩ");
        }

        InventoryHelper.createPlayersHeadsInventory(invInfoFree, stateID, 1, "infoFree");
        player.openInventory(invInfoFree);
    }
    public void open2InvInfoFree(Player player) {
        invInfoFree = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.createPlayersHeadsInventory(invInfoFree, stateID, 2, "infoFree");
        player.openInventory(invInfoFree);
    }

    public void openInvInviteFree(Player player) {
        int citizensAmount = InventoryHelper.getAllFreePlayerWithoutState().length;
        if (citizensAmount <= 25) {
            invInviteFree = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        } else {
            invInviteFree = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ᗩ");
        }

        InventoryHelper.createPlayersHeadsInventory(invInviteFree, stateID, 1, "invite");
        player.openInventory(invInviteFree);
    }
    public void open2InvInviteFree(Player player) {
        invInviteFree = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.createPlayersHeadsInventory(invInviteFree, stateID, 2, "invite");
        player.openInventory(invInviteFree);
    }

    // (1) Инвентарь построек-улучшений
    public void openInvUpgradesBuildings(Player player) {
        int buildingsAmount = this.getBuildingsAmount();
        if (buildingsAmount <= 24) {
            invUpgradesBuildings = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        } else {
            invUpgradesBuildings = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ᗩ");
        }

        InventoryHelper.createBuildingHeadsInventory(invUpgradesBuildings, stateID, 1);
        player.openInventory(invUpgradesBuildings);
    }
    // (2) Инвентарь построек-улучшений
    public void open2InvUpgradesBuildings(Player player) {
        invUpgradesBuildings = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.createBuildingHeadsInventory(invUpgradesBuildings, stateID, 2);
        player.openInventory(invUpgradesBuildings);
    }

    // Меню с кнопками Повысить и Понизить
    public Inventory getRankInventory() {
        invRank = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>⦗");

        displayName1 = InventoryHelper.getItemNameForHallTable(41, "LEFT");
        displayName2 = InventoryHelper.getItemNameForHallTable(41, "RIGHT");

        lore = InventoryHelper.getItemLoreForHallTable("rankLeft");
        firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

        lore = InventoryHelper.getItemLoreForHallTable("rankRight");
        secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesFirst, invRank, firstItem);
        InventoryHelper.setItemForManySlotes(choiceRatushaSlotesSecond, invRank, secondItem);

        return invRank;
    }
    public void openInvRank(Player player) {
        invRank = getRankInventory();
        player.openInventory(invRank);
    }
    // Меню для повышения и понижения игроков (2 экземпляра)
    public void openInvRankRaise(Player player) {
        int citizensAmount = InventoryHelper.howManyCitizensInStateWithoutGeneral(stateID);
        if (citizensAmount <= 25) {
            invRankRaise = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        } else {
            invRankRaise = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ᗩ");
        }

        InventoryHelper.createPlayersHeadsInventory(invRankRaise, stateID, 1, "rankRaise");
        player.openInventory(invRankRaise);
    }
    public void open2InvRankRaise(Player player) {
        invRankRaise = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.createPlayersHeadsInventory(invRankRaise, stateID, 2, "rankRaise");
        player.openInventory(invRankRaise);
    }
    public void openInvRankDowngrade(Player player) {
        int citizensAmount = InventoryHelper.howManyCitizensInStateWithoutGeneral(stateID);
        if (citizensAmount <= 25) {
            invRankDowngrade = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        } else {
            invRankDowngrade = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>ᗩ");
        }

        InventoryHelper.createPlayersHeadsInventory(invRankDowngrade, stateID, 1, "rankDowngrade");
        player.openInventory(invRankDowngrade);
    }
    public void open2InvRankDowngrade(Player player) {
        invRankDowngrade = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>♠");
        InventoryHelper.createPlayersHeadsInventory(invRankDowngrade, stateID, 2, "rankDowngrade");
        player.openInventory(invRankDowngrade);
    }




    public static boolean create(Block sign, String name, String status) {
        HallTable ht = getHTByName(name);

        if (ht == null) {
            ht = new HallTable(sign, name);
            if (ht.body.getBrokenBlock() == null) {
                ht.writeAllDataHalltable();
                buildingsDB.write("halltables." + name + ".status", status);
                return true;
            } else {
                // Если не прошло, удаляем айди из базы данных
                buildingsDB.remove("halltables." + name);
            }
        }

        return false;
    }

    public static void remove(Block destroyBlock, Player player) {
        HallTable ht = HallTable.getHTByBlock(destroyBlock);
        if (ht == null) return;
        htName = ht.getName();

        // Получаем для каждого предмета NOT, OK, NOT0 и т.д.
        String firstItemStance = buildingsDB.getString("halltables." + htName + ".upgrade.firstItem");
        String secondItemStance = buildingsDB.getString("halltables." + htName + ".upgrade.secondItem");
        String thirdItemStance = buildingsDB.getString("halltables." + htName + ".upgrade.thirdItem");
        // Выпадение вещей только если upgrade 0, если halltable уже поставлено, то всё

        // Получаем дерево, которое использовалось при возведении HallTable
        Material matWood = getLogUpgrade(ht.getName());
        if (!matWood.isAir()) {
            woodItem = new ItemStack(matWood, 64);
        }

        if (firstItemStance.equals("OK")) {
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), breadItem);
        }
        // Если NOT1, значит положено лишь дерево
        if (secondItemStance.equals("NOT1")) {
            // 2 стака
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), woodItem);
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), woodItem);
        } else if (secondItemStance.equals("OK")) {
            // И древо
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), woodItem);
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), woodItem);
            // И камень (3 стака)
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), smoothStoneItem);
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), smoothStoneItem);
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), smoothStoneItem);
        }
        if (thirdItemStance.equals("OK")) {
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), diamondItem);
        }

        // Убираю из всех датабаз этот город
        String htName = ht.getName();
        String cityID = ID.getCityID(htName);
        buildingsDB.remove("halltables." + htName);
        databaseDB.remove("marinbay.city." + cityID);
        idDB.remove("ids." + "city_" + htName);
        // Удаляем гексагоны
        Main.getInstance().removeHexagonsForCity(cityID);
        DynmapDrawer.redrawDynmap();
    }

    public void playOpeningSound() {
        float randomPitch = (float) (Math.random() * 0.1);
        Location loc = sign.getLocation();
        if (loc.getWorld() == null) return;
        loc.getWorld().playSound(loc, "ratusha1", 0.4f, 0.55f + randomPitch);
    }

    public static HallTable getHTByName(String name) {
        if (!buildingsDB.isSet("halltables." + name + ".signLocation")) return null;

        Location locOfSign = Main.getInstance().getLocation(buildingsDB.getString("halltables." + name + ".signLocation"));
        Block tablo = Bukkit.getWorld("world").getBlockAt(locOfSign);

        return new HallTable(tablo, name, false);
    }

    public static HallTable getHTByBlock(Block block) {
        ArrayList<HallTable> hallTables = MarinBayUtil.getAllHallTables();

        for (HallTable ht : hallTables) {
            if (ht.body.getBounds().contains(block)) {
                return ht;
            }
         }

        return null;
    }

    public static Material getLogUpgrade(String cityName) {
        Material material = Material.AIR;
        String materialString = buildingsDB.getString("halltables." + cityName + ".upgrade.secondItemWood");
        // Если дерево не было вброшено, то возвращаем воздух
        if (materialString == null) {
            return material;
        }

        if (materialString.equals("OAK_LOG")) {
            material = Material.OAK_LOG;
        } else if (materialString.equals("BIRCH_LOG")) {
            material = Material.BIRCH_LOG;
        } else if (materialString.equals("ACACIA_LOG")) {
            material = Material.ACACIA_LOG;
        } else if (materialString.equals("JUNGLE_LOG")) {
            material = Material.JUNGLE_LOG;
        } else if (materialString.equals("SPRUCE_LOG")) {
            material = Material.SPRUCE_LOG;
        } else if (materialString.equals("DARK_OAK_LOG")) {
            material = Material.DARK_OAK_LOG;
        }

        return material;
    }

    public BlockVector3 getCorner(int number) {
        // number - номер угла, первый - 1, второй - 2
        int x, y, z;
        BlockVector3 vector;

        if (number == 1) {
            x = Integer.parseInt(buildingsDB.getString("halltables." + name + ".bounds.x1"));
            y = Integer.parseInt(buildingsDB.getString("halltables." + name + ".bounds.y1"));
            z = Integer.parseInt(buildingsDB.getString("halltables." + name + ".bounds.z1"));

            vector = BlockVector3.at(x, y, z);
        } else {
            x = Integer.parseInt(buildingsDB.getString("halltables." + name + ".bounds.x2"));
            y = Integer.parseInt(buildingsDB.getString("halltables." + name + ".bounds.y2"));
            z = Integer.parseInt(buildingsDB.getString("halltables." + name + ".bounds.z2"));

            vector = BlockVector3.at(x, y, z);
        }
        return vector;
    }

    // Обновляет stateID
    public void updStateID() {
        if (status.equals("state")) {
            stateID = buildingsDB.getString("halltables." + name + ".id");
        } else if (status.equals("city")) {
            String cityID = buildingsDB.getString("halltables." + name + ".id");
            stateID = databaseDB.getString("marinbay.city." + cityID + ".owner");
        }
    }
}
