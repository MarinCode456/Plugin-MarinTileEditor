package marintileeditor.main.TileManager.MaryManager.Barrack;

import com.sk89q.worldedit.math.BlockVector3;
import library.Point;
import marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper;
import marintileeditor.main.TileManager.MaryManager.MarinBayUtil;
import marintileeditor.main.TileManager.TileManager.City.City;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDescription;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.ID.ID;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Logger;

public class Barrack implements InventoryHolder {

    // Основные переменные
    private String id;
    private String name;
    private final Block sign;
    private Inventory inventory;
    private final BarrackBody body;

    // Тех часть
    private static Logger log = Bukkit.getLogger();

    // Датабазы
    private static Database buildingsDB = Database.BUILDINGS_DATABASE;
    private static Database databaseDB = Database.STAT_DATABASE;
    private static Database idDB = Database.ID_LINK_DATABASE;

    // Вещи которые дропаются, если ломаешь стол
    public static ItemStack arrowItem = new ItemStack(Material.ARROW, 64);
    public static ItemStack woodItem = new ItemStack(Material.BIRCH_LOG, 128);
    public static ItemStack smoothStoneItem = new ItemStack(Material.SMOOTH_STONE, 128);
    public static ItemStack hayItem = new ItemStack(Material.HAY_BLOCK, 32);

    // Болванчик-лор для предметов
    ArrayList<String> lore = new ArrayList<>();

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
    int [] slotesCreateButton = {
            26
    };
    int[] choiceBarrackSlotesFirst = {
            1, 2, 3,
            10, 11, 12,
            19, 20, 21

    };
    int[] choiceBarrackSlotesSecond = {
            5, 6, 7,
            14, 15, 16,
            23, 24, 25

    };

    // Инвентари
    public Inventory invBarrackChoice;

    // Для новых барраков (имя не определенно)
    public Barrack(Block sign) {
        this.sign = sign;
        name = "barrack" + getNumberForBarracks();
        this.id = ID.getBarrackID(name);
        body = new BarrackBody(this);
    }
    // Для существующих
    public Barrack(String name, Block sign) {
        this.sign = sign;
        this.name = name;
        this.id = ID.getBarrackID(name);
        body = new BarrackBody(this);
    }

    // Геттеры и сеттеры
    public String getName() {
        return name;
    }
    public Block getSign() {
        return sign;
    }
    public BarrackBody getBody() { return body; }
    public String getCreator() {
        return buildingsDB.getString("barracks." + name + ".creator");
    }
    public int getScore() { return Integer.parseInt(buildingsDB.getString("barracks." + name + ".upgrade.score"));}
    public Inventory getInventory() {
        return inventory;
    }
    public int getSignDirection() { return Integer.parseInt(buildingsDB.getString("barracks." + name + ".direction")); }
    public int getPlusAmountOfCitizens() { return Integer.parseInt(buildingsDB.getString("barracks." + name + ".citizensAmount"));}
    public int getNumberForBarracks() {
        // Если это первый баррак, то он и должен быть 1
        if (buildingsDB.isSet("numberForBarracks")) {
            return Integer.parseInt(buildingsDB.getString("numberForBarracks"));
        }
        return 0;
    }
    public String getNameOwner() {
        String owner;
        if (buildingsDB.isSet("barracks." + name + ".ownerCity")) {
            owner = buildingsDB.getString("barracks." + name + ".ownerCity");
            return databaseDB.getString("marinbay.city." + owner + ".name");
        }
        owner = buildingsDB.getString("barracks." + name + ".ownerState");
        return databaseDB.getString("marinbay.state." + owner + ".name");
    }
    public Location getCapital() {
        return Main.getInstance().getLocation(buildingsDB.getString("barracks." + name + ".signLocation"));
    }
    public BlockVector3 getCorner(int number) {
        // number - номер угла, первый - 1, второй - 2
        int x, y, z;
        BlockVector3 vector;

        if (number == 1) {
            x = Integer.parseInt(buildingsDB.getString("barracks." + name + ".bounds.x1"));
            y = Integer.parseInt(buildingsDB.getString("barracks." + name + ".bounds.y1"));
            z = Integer.parseInt(buildingsDB.getString("barracks." + name + ".bounds.z1"));

            vector = BlockVector3.at(x, y, z);
        } else {
            x = Integer.parseInt(buildingsDB.getString("barracks." + name + ".bounds.x2"));
            y = Integer.parseInt(buildingsDB.getString("barracks." + name + ".bounds.y2"));
            z = Integer.parseInt(buildingsDB.getString("barracks." + name + ".bounds.z2"));

            vector = BlockVector3.at(x, y, z);
        }
        return vector;
    }
    public long getTimeOfCreate() { return Long.parseLong(buildingsDB.getString("barracks." + name + ".timeOfCreate"));}
    // +1 житель к бараку
    public void plusOneCitizen() {
        // Добавляем одного жителя в саму казарму
        int amount = Integer.parseInt(buildingsDB.getString("barracks." + name + ".citizensAmount"));
        String pAmount = String.valueOf(amount + 1);
        buildingsDB.write("barracks." + name + ".citizensAmount", pAmount);

        // Добавляем одного жителя в государство, которому принадлежит казарма
        String ownerState = buildingsDB.getString("barracks." + name + ".ownerState");
        City.plusOneCitizensAmount(ownerState);
    }
    public static void plusOneInNumberForBarracks() {
        // +1 в номера бараков
        int numBar = Integer.parseInt(buildingsDB.getString("numberForBarracks")) + 1;
        buildingsDB.write("numberForBarracks", "" + numBar);
    }

    // Функция записи всех переменных в barrack
    // Вызывается только при созданиия барака
    private void writeAllDataBarrack() {
        String signLocation = Main.getInstance().getStringFrom(sign.getLocation());

        // Переменная для определения номера барака
        // barrack0, barrack1 и т.д. делает она
        if (!buildingsDB.isSet("numberForBarracks")) {
            buildingsDB.write("numberForBarracks", "" + 0);
        }

        // Записываем строчки апгрейдов
        buildingsDB.write("barracks." + name + ".upgrade.score", String.valueOf(0));
        buildingsDB.write("barracks." + name + ".upgrade.firstItem", "NOT");
        // Для второго предмета NOT0 - значит не загружена древесина
        // NOT1 - значит не загружен камень
        // ОК - загружена и древесина, и камень
        buildingsDB.write("barracks." + name + ".upgrade.secondItem", "NOT0");
        buildingsDB.write("barracks." + name + ".upgrade.thirdItem", "NOT");

        // Записываем локацию таблички + в каком гексагоне находится постройка
        buildingsDB.write("barracks." + name + ".signLocation", signLocation);
        buildingsDB.write("barracks." + name + ".hexagonOfBuilding", Main.grid.getHexagon(sign.getLocation()).toString());

        // Пишем сколько жителей даёт в государство
        buildingsDB.write("barracks." + name + ".citizensAmount", String.valueOf(0));
    }

    // Создаём инвентарь
    public Inventory setNgetInventory() {
        int upgradeScore = Main.getInstance().getUpgradeScore("barracks." + name + ".upgrade.score");
        String inventoryName;

        // Получаем все OK NOT от всех предметов
        firstItemOKNOT = buildingsDB.getString("barracks." + name + ".upgrade.firstItem");
        secondItemOKNOT = buildingsDB.getString("barracks." + name + ".upgrade.secondItem");
        thirdItemOKNOT = buildingsDB.getString("barracks." + name + ".upgrade.thirdItem");

        // (0-1) Баррак еще не создан
        if (upgradeScore == 0) {
            displayName1 = InventoryHelper.getItemNameForBarrack(1, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForBarrack(2, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForBarrack(3, thirdItemOKNOT);

            // Определяем имя инвентаря, если второй слот еще на первой стадии, то
            // будет первая версия инвентаря, если же это NOT1 или OK, то вторая
            if (secondItemOKNOT.equals("NOT1") || secondItemOKNOT.equals("OK")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⦄";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⦃";
            }
            inventory = Bukkit.getServer().createInventory(this, 27, inventoryName);

            // Устанавливаем название кнопки создания
            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Создать казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.AQUA + "Вы пока не можете создать казарму";
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
        }

        // (1-2) Баррак
        if (upgradeScore == 1) {
            displayName1 = InventoryHelper.getItemNameForBarrack(4, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForBarrack(5, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForBarrack(6, thirdItemOKNOT);

            // Определяем имя инвентаря, если второй слот еще на первой стадии, то
            // будет первая версия инвентаря, если же это NOT1 или OK, то вторая
            if (secondItemOKNOT.equals("NOT1") || secondItemOKNOT.equals("OK")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⦄";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⦃";
            }
            inventory = Bukkit.getServer().createInventory(this, 27, inventoryName);

            // Устанавливаем название кнопки создания
            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("UPGRADE_OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.AQUA + "Вы пока не можете улучшить казарму";
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

            InventoryHelper.setItemForManySlotes(slotesFirst, inventory, firstItem);
            InventoryHelper.setItemForManySlotes(slotesSecond, inventory, secondItem);
            InventoryHelper.setItemForManySlotes(slotesThird, inventory, thirdItem);
            InventoryHelper.setItemForManySlotes(slotesCreateButton, inventory, buttonItem);
        }

        // (2-3) Баррак
        if (upgradeScore == 2) {
            displayName1 = InventoryHelper.getItemNameForBarrack(7, firstItemOKNOT);
            displayName2 = InventoryHelper.getItemNameForBarrack(8, secondItemOKNOT);
            displayName3 = InventoryHelper.getItemNameForBarrack(9, thirdItemOKNOT);

            // Определяем имя инвентаря, если второй слот еще на первой стадии, то
            // будет первая версия инвентаря, если же это NOT1 или OK, то вторая
            if (secondItemOKNOT.equals("NOT1") || secondItemOKNOT.equals("OK")) {
                inventoryName = ChatColor.WHITE + "<shift:-8>⦄";
            } else {
                inventoryName = ChatColor.WHITE + "<shift:-8>⦃";
            }
            inventory = Bukkit.getServer().createInventory(this, 27, inventoryName);

            // Устанавливаем название кнопки создания
            if (firstItemOKNOT.equals("OK") && secondItemOKNOT.equals("OK") && thirdItemOKNOT.equals("OK")) {
                buttonName = ChatColor.AQUA + "Улучшить казарму";
                lore = InventoryHelper.getButtonLoreForHallTable("UPGRADE_OK");
                buttonItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, buttonName, lore, 1);
            } else {
                buttonName = ChatColor.AQUA + "Вы пока не можете улучшить казарму";
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

            InventoryHelper.setItemForManySlotes(slotesFirst, inventory, firstItem);
            InventoryHelper.setItemForManySlotes(slotesSecond, inventory, secondItem);
            InventoryHelper.setItemForManySlotes(slotesThird, inventory, thirdItem);
            InventoryHelper.setItemForManySlotes(slotesCreateButton, inventory, buttonItem);
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

    public Inventory getBarrackChoiceInventory() {
        int upgradeScore = Main.getInstance().getUpgradeScore("barracks." + name + ".upgrade.score");

        if (upgradeScore == 0) {
            // Такое название, чтобы корректно отображался интерфейс
            invBarrackChoice = Bukkit.getServer().createInventory(this, 27, ChatColor.WHITE + "<shift:-8>⊳");

            displayName1 = InventoryHelper.getItemNameForHallTable(4, "BARRACK_LEFT");
            displayName2 = InventoryHelper.getItemNameForHallTable(4, "BARRACK_RIGHT");

            lore = InventoryHelper.getItemLoreForHallTable("barrackChoiceLeft");
            firstItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName1, lore, 1);

            lore = InventoryHelper.getItemLoreForHallTable("barrackChoiceRight");
            secondItem = InventoryHelper.getMenuItem(Material.HONEYCOMB, 1, displayName2, lore, 1);

            InventoryHelper.setItemForManySlotes(choiceBarrackSlotesFirst, invBarrackChoice, firstItem);
            InventoryHelper.setItemForManySlotes(choiceBarrackSlotesSecond, invBarrackChoice, secondItem);
        }
        return invBarrackChoice;
    }

    public void openInvBarrackChoice(Player player) {
        invBarrackChoice = getBarrackChoiceInventory();
        player.openInventory(invBarrackChoice);
    }





    public static boolean create(Block sign) {
        Barrack barrack = new Barrack(sign);
        String barrackName = barrack.getName();

        if (barrack.body.getBrokenBlock() == null) {
            barrack.writeAllDataBarrack();
            // +1 к номеру бараков, чтобы в будущем создать новое имя для барака
            Barrack.plusOneInNumberForBarracks();
            return true;
        } else {
            // Если не прошло, удаляем баррак из базы данных
            buildingsDB.remove("barracks." + barrackName);
        }

        return false;
    }

    public static void remove(Block destroyBlock, Player player, Barrack barrack) {
        Barrack barrack1 = barrack;
        if (barrack1 == null) return;
        String barrackName = barrack1.getName();

        // Получаем для каждого предмета NOT, OK, NOT0 и т.д.
        String firstItemStance = buildingsDB.getString("barracks." + barrackName + ".upgrade.firstItem");
        String secondItemStance = buildingsDB.getString("barracks." + barrackName + ".upgrade.secondItem");
        String thirdItemStance = buildingsDB.getString("barracks." + barrackName + ".upgrade.thirdItem");

        // Получаем дерево, которое использовалось при возведении HallTable
        Material matWood = getLogUpgrade(barrackName);
        if (!matWood.isAir()) {
            woodItem = new ItemStack(matWood, 64);
        }

        if (firstItemStance.equals("OK")) {
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), arrowItem);
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
            destroyBlock.getWorld().dropItem(destroyBlock.getLocation(), hayItem);
        }

        // Убираю из всех датабаз
        removeFromDBs(barrackName);
    }

    public static void removeFromDBs(String barrackName) {
        // Удаляем из построек государства
        String ownerState = buildingsDB.getString("barracks." + barrackName + ".ownerState");
        String hexStr = buildingsDB.getString("barracks." + barrackName + ".hexagonOfBuilding");
        databaseDB.removeOneElement("marinbay.state." + ownerState + ".buildings.barracks", barrackName, ", ");

        // Если казарма принадлежит городу, удаляем её оттуда
        if (buildingsDB.isSet("barracks." + barrackName + ".ownerCity")) {
            // Проверяем существует ли город вообще
            // Для случая удаления целого государства
            String ownerCity = buildingsDB.getString("barracks." + barrackName + ".ownerCity");
            if (databaseDB.isSet("marinbay.city." + ownerCity + ".buildings.barracks")) {
                databaseDB.removeOneElement("marinbay.city." + ownerCity + ".buildings.barracks", barrackName, ", ");
            }
        }

        // Удаляем из государства, которому принадлежит казарма, столько жителей,
        // Сколько она давала
        int amountBarrack = Integer.parseInt(buildingsDB.getString("barracks." + barrackName + ".citizensAmount"));
        String stateID = buildingsDB.getString("barracks." + barrackName + ".ownerState");
        int amountState = City.getStateCitizensAmountMax(stateID);
        City.setMaxCitizens(stateID, amountState - amountBarrack);

        // Удаляем из базы построек
        buildingsDB.remove("barracks." + barrackName);

        // Удаляем строку building из гексагона, в котором была казарма
        databaseDB.remove("marinbay.hexagons." + hexStr + ".building");
        databaseDB.remove("marinbay.hexagons." + hexStr + ".barrackName");

        // Удаляем регион
        InventoryHelper.removeRegionB(barrackName);
    }

    public void playOpeningSound() {
        float randomPitch = (float) (Math.random() * 0.1);
        Location loc = sign.getLocation();
        if (loc.getWorld() == null) return;
        loc.getWorld().playSound(loc, getRandomSound(), 0.4f, 0.55f + randomPitch);
    }
    // Рандом для получения каждый раз случайного звука
    private String getRandomSound() {
        Random r = new Random();
        int number = r.nextInt(3) + 1;

        return "kazarm" + number;
    }

    public static Barrack getBarrackByName(String name) {
        if (!buildingsDB.isSet("barracks." + name + ".signLocation")) return null;

        Location locOfSign = Main.getInstance().getLocation(buildingsDB.getString("barracks." + name + ".signLocation"));
        Block tablo = Bukkit.getWorld("world").getBlockAt(locOfSign);

        return new Barrack(name, tablo);
    }

    public static Barrack getBarrackByBlock(Block block) {
        ArrayList<Barrack> barracks = MarinBayUtil.getAllBarracks();

        for (Barrack barrack : barracks) {
            if (barrack.body.getBounds().contains(block)) {
                return barrack;
            }
        }

        return null;
    }

    public static Material getLogUpgrade(String nameBarrack) {
        Material material = Material.AIR;
        String materialString = buildingsDB.getString("barracks." + nameBarrack + ".upgrade.secondItemWood");
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

    // Рисуем флажок барака
    public void draw() {
        DynmapDrawer drawer = new DynmapDrawer();
        String icon = "barrack";
        drawer.draw(new Point(this.getCapital().getBlockX(),this.getCapital().getBlockZ()), icon, new DynmapDescription("Казарма", new String[0]));
    }
}
