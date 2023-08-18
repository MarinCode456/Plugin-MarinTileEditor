package marintileeditor.main.TileManager.TileManager.City;

import library.Hexagon;
import library.Point;
import marintileeditor.main.TileManager.TileManager.API.SMassiveAPI;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDescription;
import marintileeditor.main.TileManager.TileManager.Dynmap.DynmapDrawer;
import marintileeditor.main.TileManager.TileManager.Main.Main;
import marintileeditor.main.TileManager.TileManager.Player.MarinPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

// Обработано
public class City {

    // Основные переменные
    private String id;
    private String status;
    private String color;

    // Тех. часть
    public static Logger log = Bukkit.getLogger();

    // Базы данных
    private static Database databaseDB = Database.STAT_DATABASE;
    private Database idDB = Database.ID_LINK_DATABASE;

    // Геттеры и сеттеры
    public String getId() {
        return this.id;
    }
    public String getName() {
        if (status.equals("state")) {
            return databaseDB.getString("marinbay.state." + this.id + ".name");
        } else {
            return databaseDB.getString("marinbay.city." + this.id + ".name");
        }
    }
    public String getColor() {
        if (status.equals("state")) {
            return databaseDB.getString("marinbay.state." + this.id + ".color");
        } else {
            String stateID = databaseDB.getString("marinbay.city." + this.id + ".owner");
            return databaseDB.getString("marinbay.city." + stateID + ".color");
        }
    }
    public CityInfo getInfo() {
        return new CityInfo(this);
    }
    public Location getCapital() {
        if (status.equals("state")) {
            return Main.getInstance().getLocation(databaseDB.getString("marinbay.state." + this.id + ".capital"));
        } else {
            return Main.getInstance().getLocation(databaseDB.getString("marinbay.city." + this.id + ".capital"));
        }
    }
    public String getResidentsString() {
        if (status.equals("state")) {
            return databaseDB.getString("marinbay.state." + this.id + ".citisens");
        } else {
            String stateID = databaseDB.getString("marinbay.city." + this.id + ".owner");
            return databaseDB.getString("marinbay.state." + stateID + ".citisens");
        }
    }
    public MarinPlayer getGeneral() {
        MarinPlayer general;
        String name = "";
        if (status.equals("state")) {
            name = databaseDB.getString("marinbay.state." + this.id + ".general");
        } else {
            name = databaseDB.getString("marinbay.city." + this.id + ".general");
        }
        general = new MarinPlayer(name);
        return general;
    }
    private DynmapDescription getDescription() {
        return this.getInfo().toDynmapFormat();
    }
    public String getMaxCitizens() {
        return databaseDB.getString("marinbay.state." + this.id + ".citizensAmount");
    }
    public static int getStateCitizensAmountMax(String stateID) {
        String str_amount = databaseDB.getString("marinbay.state." + stateID + ".citizensAmount");
        return Integer.parseInt(str_amount);
    }
    public static int getStateCitizensAmount(String stateID) {
        String citizens = databaseDB.getString("marinbay.state." + stateID + ".citisens");
        String[] all_citizens = citizens.split(", ");
        return all_citizens.length;
    }
    public static boolean canYouInviteNewCitizens(String stateID) {
        if (getStateCitizensAmount(stateID) == getStateCitizensAmountMax(stateID)) {
            return false;
        } else {
            return true;
        }
    }
    public static void plusOneCitizensAmount(String stateID) {
        String str_amount = databaseDB.getString("marinbay.state." + stateID + ".citizensAmount");
        int new_amount = Integer.parseInt(str_amount) + 1;
        databaseDB.write("marinbay.state." + stateID + ".citizensAmount", String.valueOf(new_amount));
    }
    public static void minusOneCitizensAmount(String stateID) {
        String str_amount = databaseDB.getString("marinbay.state." + stateID + ".citizensAmount");
        int new_amount = Integer.parseInt(str_amount) - 1;
        databaseDB.write("marinbay.state." + stateID + ".citizensAmount", String.valueOf(new_amount));
    }
    public static void setMaxCitizens(String stateID, int amount) {
        databaseDB.write("marinbay.state." + stateID + ".citizensAmount", String.valueOf(amount));
    }



    public City(String id, String status) {
        this.id = id;
        this.status = status;
    }

    // Создаём государство
    // capital - location таблички halltable
    public void createState(MarinPlayer creator, String name, String color, String signLocString) {
        creator.prepareToJoinCity();
        if (!(creator.getPlayer() == null)) {
            String generalID = creator.getId();
            String generalName = creator.getName();
            String capital = signLocString;
            String creatorID = creator.getId();
            String hexagonString = Main.grid.getHexagon(creator).toString();

            databaseDB.write("marinbay.player." + creatorID + ".state", this.id);
            databaseDB.write("marinbay.player." + creatorID + ".status", "general");
            databaseDB.write("marinbay.player." + creatorID + ".timeInvite", String.valueOf(System.currentTimeMillis()));
            databaseDB.writeNewStateWhereBe("marinbay.player." + creatorID + ".wherebe", name);
            databaseDB.write("marinbay.state." + this.id + ".name", name);
            databaseDB.write("marinbay.state." + this.id + ".general", generalID);
            databaseDB.write("marinbay.state." + this.id + ".citisens", generalName);
            databaseDB.write("marinbay.state." + this.id + ".capital", capital);
            databaseDB.write("marinbay.state." + this.id + ".color", color);
            databaseDB.write("marinbay.state." + this.id + ".hexagonOfBuilding", hexagonString);
            databaseDB.write("marinbay.state." + this.id + ".hexagons", hexagonString);
            databaseDB.write("marinbay.state." + this.id + ".citizensAmount", String.valueOf(5));
            databaseDB.write("marinbay.hexagons." + hexagonString + ".owner", this.id);
            databaseDB.write("marinbay.hexagons." + hexagonString + ".building", "state");
            idDB.write("ids.state_" + name, id);
            DynmapDrawer.redrawDynmap();
        }
        else {
            log.info("Вы консоль и не имеете права!");
        }
    }

    // Создаём город
    public void create(MarinPlayer creator, String name, String signLocString, String stateID) {
        creator.prepareToJoinCity();
        if (!(creator.getPlayer() == null)) {
            String generalID = creator.getId();
            String capital = signLocString;
            String hexagonString = Main.grid.getHexagon(creator).toString();

            databaseDB.write("marinbay.player." + creator.getName().toLowerCase() + ".placeCity", this.id);
            databaseDB.write("marinbay.city." + this.id + ".name", name);
            databaseDB.write("marinbay.city." + this.id + ".general", generalID);
            databaseDB.write("marinbay.city." + this.id + ".capital", capital);
            databaseDB.write("marinbay.city." + this.id + ".owner", stateID);
            databaseDB.write("marinbay.city." + this.id + ".hexagonOfBuilding", hexagonString);
            databaseDB.write("marinbay.hexagons." + hexagonString + ".owner", stateID);
            databaseDB.write("marinbay.hexagons." + hexagonString + ".building", "city");
            databaseDB.write("marinbay.hexagons." + hexagonString + ".cityID", this.id);
            databaseDB.writeNewCity("marinbay.state." + stateID + ".cities", this.id);
            plusOneCitizensAmount(stateID);
            idDB.write("ids.city_" + name, id);
            DynmapDrawer.redrawDynmap();
        }
        else {
            log.info("Вы консоль и не имеете права!");
        }
    }

    // Меняем цвет государству
    public static void changeColor(Player player, String color) {
        MarinPlayer player1 = new MarinPlayer(player);
        String idOfState = player1.getStateID();

        databaseDB.write("marinbay.state." + idOfState + ".color", color);
        DynmapDrawer.redrawDynmap();
    }

    // Рисуем город или государство
    // При этом выбираем разные флаги для города и государства
    public void draw() {
        String color = this.getColor();
        DynmapDrawer drawer = new DynmapDrawer();
        String icon;
        if (status.equals("state")) {
            icon = "crown";
            drawer.drawFigure(this.getTerritories(), color, this.getDescription());
        } else {
            icon = "cityflag";
        }
        drawer.draw(new Point(this.getCapital().getBlockX(),this.getCapital().getBlockZ()), icon, this.getDescription());
    }

    // Получаем все гексагоны государства или города
    public Hexagon[] getTerritories() {
        String[] hexagonCodes = databaseDB.getStringList("marinbay.hexagons");
        ArrayList<Hexagon> allHexagons = new ArrayList<>();
        ArrayList<Hexagon> hexagons = new ArrayList<>();

        for (int i = 0; i < hexagonCodes.length; i++) {
            allHexagons.add(Main.getInstance().getGrid().getHexagon(hexagonCodes[i]));
        }
        for (int i = 0; i < allHexagons.size(); i++) {
            if (Objects.equals(Main.getCityMap().getState(allHexagons.get(i)).getId(), this.getId())) {
                hexagons.add(allHexagons.get(i));
            }
        }

        return hexagons.toArray(new Hexagon[0]);
    }

    // Проверяет существует ли такой город
    public boolean exists() {
        if (status.equals("city")) {
            return databaseDB.isSet("marinbay.city." + this.id + ".name");
        }
        return databaseDB.isSet("marinbay.state." + this.id + ".name");
    }

    // Удаляет гексагон из города
    public static void removeOneHexagon(String stateID, String removeHex) {
        // Удаляем ненужный нам гексагон и создаём на основе списка новую строчку
        String[] stateHexagons = databaseDB.getString("marinbay.state." + stateID + ".hexagons").split(" ");
        log.info("1 Получаю такой state - " + stateID);
        log.info("1 Получаю такой removeHex - " + removeHex);
        List<String> newHexagons = new ArrayList<>();

        for (String hex : stateHexagons) {
            if (!hex.equals(removeHex) && hex != null) {
                newHexagons.add(hex);
            }
        }

        databaseDB.write("marinbay.state." + stateID + ".hexagons", String.join(" ", newHexagons));
    }

    // Добавляет гексагон в город
    public static void addOneHexagon(String stateID, String addHex) {
        String[] stateHexagons = databaseDB.getString("marinbay.state." + stateID + ".hexagons").split(" ");

        // Можно было бы и проще, но я не знаю точно где находятся пробелы в строке, поэтому легче уж так
        String newHexagonsString = "";
        for (String hex : stateHexagons) {
            newHexagonsString = newHexagonsString + hex + " ";
        }
        newHexagonsString = newHexagonsString + addHex;

        databaseDB.write("marinbay.state." + stateID + ".hexagons", newHexagonsString);
    }
}
