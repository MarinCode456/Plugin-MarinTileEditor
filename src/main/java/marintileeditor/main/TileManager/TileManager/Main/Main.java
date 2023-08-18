package marintileeditor.main.TileManager.TileManager.Main;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import marintileeditor.main.TileManager.MaryManager.Barrack.Barrack;
import marintileeditor.main.TileManager.MaryManager.BlockListener;
import library.HexagonalGrid;
import library.Morton64;
import library.Orientation;
import library.Point;
import marintileeditor.main.TileManager.MaryManager.BoundingBox;
import marintileeditor.main.TileManager.MaryManager.HallTable.HallTable;
import marintileeditor.main.TileManager.MaryManager.Inventory.InventoryHelper;
import marintileeditor.main.TileManager.MaryManager.Inventory.InventoryListener;
import marintileeditor.main.TileManager.MaryManager.MrTransliterator;
import marintileeditor.main.TileManager.MaryManager.PlayerListener;
import marintileeditor.main.TileManager.TileManager.City.CityMap;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.MarinHexagons.MarinHexagonsGrid;
import marintileeditor.main.TileManager.TileManager.MrPlaceholder.StatePlaceholder;
import marintileeditor.main.TileManager.TileManager.Player.MarinPlayer;
import marintileeditor.main.TileManager.TileManager.commands.DesinviteCommand;
import marintileeditor.main.TileManager.TileManager.commands.InviteCommand;
import marintileeditor.main.TileManager.TileManager.commands.MBCommand;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

// Обработано
public final class Main extends JavaPlugin {

    // Тех. часть и Main singleton
    private static Main INSTANCE;
    private Logger log = Bukkit.getLogger();

    // Базы данных
    private static Database marinDB;
    private static Database idDB;
    private static Database buildingsDB;
    private static Database colorsChangeDB;
    private static Database generalsDB;
    private static Database inviteDB;

    // Часть ответственная за карту
    private static CityMap cityMap;
    private static DynmapAPI dynmap;
    public static final MarinHexagonsGrid grid = new MarinHexagonsGrid(Orientation.FLAT, new Point(0D,0D), new Point(50D, 50D), new Morton64(2L, 32L));

    // Слушатели
    public BlockListener blocklistener;
    public PlayerListener playerlistener;
    public InventoryListener inventoryListener;

    // Синглтон
    public Main() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        // Если нет папки плагина, создаём папку плагина
        if (!this.getDataFolder().exists()) {
            this.getDataFolder().mkdirs();
        }
        // Создаём папку schematics
        File file = new File(Main.getInstance().getDataFolder() + "/schematics", "glent.txt");
        if (!file.exists()) {
            file.mkdirs();
        }

        // Регистрируем команды, базы данных и синглтон
        this.setUpDBs();
        this.setInstance();
        getCommand("mb").setExecutor(new MBCommand());
        getCommand("invite").setExecutor(new InviteCommand());
        getCommand("desinvite").setExecutor(new DesinviteCommand());

        // Регистрируем слушателей
        blocklistener = new BlockListener();
        playerlistener = new PlayerListener();
        inventoryListener = new InventoryListener();


        // Регистрирую плесхолдер
        new StatePlaceholder().register();

        // Регистрируем ивенты
        this.getServer().getPluginManager().registerEvents(blocklistener, this);
        this.getServer().getPluginManager().registerEvents(playerlistener, this);
        this.getServer().getPluginManager().registerEvents(inventoryListener, this);

        // Удаляем государства, если владелец не заходил 7 дней
        this.deleteCities();

        // Получаем динмап, регистрируем CityMap и вырисовываем карту
        dynmap = (DynmapAPI) Bukkit.getServer().getPluginManager().getPlugin("dynmap");
        this.registerCityMap();
        this.drawDynmap();
        log.info(ChatColor.AQUA + "[MarinTileEditor] Успешно подключён! 4 основателя заслуживают кириешки");
    }

    // Устанавливает базы данных
    private void setUpDBs() {
        Database.setUp();
        marinDB = Database.STAT_DATABASE;
        idDB = Database.ID_LINK_DATABASE;
        buildingsDB = Database.BUILDINGS_DATABASE;
        colorsChangeDB = Database.COLOR_CHANGE_DATABASE;
        generalsDB = Database.GENERALS_DATABASE;
        inviteDB = Database.INVITE_DATABASE;
    }

    // Удаляет государства, если главы долго не онлайнят
    private void deleteCities() {
        String[] generals = generalsDB.getStringList("generals");
        String[] closers = generalsDB.getStringList("closers");
        List<String> closersInOneState = new ArrayList<>();
        for (String general : generals) {
            if (generalsDB.getString("generals." + general + ".time").equals("NOT_GENERAL_MORE")) {
                return;
            }

            long oldTime = Long.parseLong(generalsDB.getString("generals." + general + ".time"));
            long nowTime = System.currentTimeMillis();
            long difference = nowTime - oldTime;
            String generalState = generalsDB.getString("generals." + general + ".state");
            if (difference/1000/60/60/24 > 7) {
                // Итак, генерал не заходил за последние 7 дней, поэтому надо проверить
                // Приближённых, если и они не заходили за последние 7 дней, то удаляем госво
                for (String closer : closers) {
                    String closerState = generalsDB.getString("closers." + closer + ".state");
                    if (closerState.equalsIgnoreCase(generalState)) {
                        closersInOneState.add(closer);
                    }
                }
                // Если приближённые из этого государства есть, то проверяем были ли они за последние
                // 7 дней в государстве
                if (closersInOneState.size() != 0) {
                    for (String cls : closersInOneState) {
                        if (generalsDB.getString("closers." + cls + ".time").equals("NOT_CLOSER_MORE")) {
                            continue;
                        }

                        long oTime = Long.parseLong(generalsDB.getString("closers." + cls + ".time"));
                        long nTime = System.currentTimeMillis();
                        long differ = nTime - oTime;
                        if (differ/1000/60/60/24 < 7) {
                            // Если хоть один приближённый заходил за последние 7 дней
                            // То мы возвращаем функцию и не удаляем это государство
                            return;
                        }
                    }
                }

                // Генерал больше не генерал
                MarinPlayer notGeneral = new MarinPlayer(general);
                String stateID = notGeneral.getStateID();
                String nameOfHT = marinDB.getString("marinbay.state." + stateID + ".name");

                generalsDB.write("generals." + general + ".time", "NOT_GENERAL_MORE");
                for (String c : closers) {
                    generalsDB.write("closers." + c + ".time", "NOT_CLOSER_MORE");
                }

                World world = Bukkit.getWorld("world");
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionManager manager = container.get(BukkitAdapter.adapt(world));

                // Удаляем все buildings
                String barracks = marinDB.getString("marinbay.state." + stateID + ".buildings.barracks");
                if (barracks != null && !barracks.equals("")) {
                    String[] barracksToRemove = barracks.split(", ");
                    for (String bar : barracksToRemove) {
                        Barrack.removeFromDBs(bar);
                    }
                }

                // Удаляем города
                String idsOfHT = marinDB.getString("marinbay.state." + stateID + ".cities");
                if (idsOfHT != null) {
                    // Если города есть, удаляем - нет, то нет
                    String[] idsOfHTmas = idsOfHT.split(" ");
                    for (String id : idsOfHTmas) {
                        // Удаляем все холлтейблы
                        String htName = marinDB.getString("marinbay.city." + id + ".name");
                        String signLocation = buildingsDB.getString("halltables." + htName + ".signLocation");
                        if (signLocation != null) {
                            Location locBlock = Main.getInstance().getLocation(signLocation);
                            HallTable ht = new HallTable(world.getBlockAt(locBlock), htName);
                            InventoryHelper.deleteCity(id, ht, 1);
                        }
                    }
                }

                // Удаляем регионы стола и гексагонов гос-ва
                InventoryHelper.removeRegionTable(nameOfHT);
                InventoryHelper.removeRegion(nameOfHT);

                // Удаляем строки из buildings.yml и id_link_database
                buildingsDB.remove("halltables." + nameOfHT);
                idDB.remove("ids." + "state_" + nameOfHT);

                // Удаляем гексагоны из государства
                String hexagons = marinDB.getString("marinbay.state." + stateID + ".hexagons");
                if (hexagons.split(" ") == null) {
                    // Если гексагон только один, то метод сплит вызовет null
                    marinDB.remove("marinbay.hexagons." + hexagons);
                } else {
                    String[] hexagonsToRemove = hexagons.split(" ");
                    for (String hex : hexagonsToRemove) {
                        marinDB.remove("marinbay.hexagons." + hex);
                    }
                }

                // Удаляем всех игроков
                String[] citizens = marinDB.getString("marinbay.state." + stateID + ".citisens").split(" ");
                for (String player : citizens) {
                    String lower = player.toLowerCase();
                    marinDB.remove("marinbay.player." + lower + ".state");
                    marinDB.remove("marinbay.player." + lower + ".status");
                    marinDB.remove("marinbay.player." + lower + ".timeInvite");
                    if (colorsChangeDB.isSet(lower)) {
                        colorsChangeDB.remove(lower);
                    }
                }
                marinDB.remove("marinbay.state." + stateID);
            }
        }
    }

    // Рисуем динмап и регистрируем CityMap
    private void drawDynmap() {
        DynmapDrawer.redrawDynmap();
    }
    private void registerCityMap() {
        cityMap = new CityMap();
    }

    // Геттеры и сеттеры
    private void setInstance() {
        INSTANCE = this;
    }
    public static Main getInstance() {
        return INSTANCE;
    }
    public static DynmapAPI getDynmap() {
        return dynmap;
    }
    public String getStringFrom(Location loc) {
        return loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ();
    }
    public static CityMap getCityMap() {
        return cityMap;
    }
    public static Database getMainDatabase() {
        return marinDB;
    }
    public HexagonalGrid getGrid() {
        return this.grid;
    }
    public Location getLocation(String path) {
        // Получи координаты по стрингу
        String[] locationStrings = path.split(";");
        World world = Bukkit.getWorld(locationStrings[0]);
        double x = Double.valueOf(locationStrings[1]);
        double y = Double.valueOf(locationStrings[2]);
        double z = Double.valueOf(locationStrings[3]);

        Location loc = new Location(world, x, y, z);
        return loc;
    }
    public BoundingBox getBounds(String path) {
        // В path нужно указать путь до bounds. Пример:
        // halltables.Челябинск.bounds
        int x1 = Integer.parseInt(buildingsDB.getString(path + ".x1"));
        int y1 = Integer.parseInt(buildingsDB.getString(path + ".y1"));
        int z1 = Integer.parseInt(buildingsDB.getString(path + ".z1"));

        int x2 = Integer.parseInt(buildingsDB.getString(path + ".x2"));
        int y2 = Integer.parseInt(buildingsDB.getString(path + ".y2"));
        int z2 = Integer.parseInt(buildingsDB.getString(path + ".z2"));

        BoundingBox bb = new BoundingBox(x1, y1, z1, x2, y2, z2);
        return bb;
    }
    public int getUpgradeScore(String path) {
        if (!buildingsDB.isSet(path)) {
            return 0;
        }
        return Integer.parseInt(buildingsDB.getString(path));
    }

    // Удаляет гексагоны какого-либо города по его айди
    public void removeHexagonsForCity(String cityID) {
        // Получаем все гексагоны
        String[] hexagons = marinDB.getStringList("marinbay.hexagons");

        for (String hexagon : hexagons) {
            String city = marinDB.getString("marinbay.hexagons." + hexagon + ".owner");
            if (city.equals(cityID)) {
                marinDB.remove("marinbay.hexagons." + hexagon);
            }
        }
    }
}
