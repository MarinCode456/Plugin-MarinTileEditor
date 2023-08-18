package marintileeditor.main.TileManager.TileManager.City;

import library.Hexagon;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import marintileeditor.main.TileManager.TileManager.Main.Main;

import java.util.ArrayList;

// Обработано
public class CityMap {
    // Базы данных
    private Database marinDB;

    public CityMap() {
        marinDB = Main.getMainDatabase();
    }

    // Получаем государство по гексагону
    public City getState(Hexagon hexagon) {
        String strID = this.marinDB.getString("marinbay.hexagons." + hexagon + ".owner");
        if (strID == null) {
            return null;
        }
        String strColor = this.marinDB.getString("marinbay.state." + strID + ".color");
        return new City(strID, "state");
    }

    // Получаем и города и государства
    public City[] getCities() {
        // Получаем айди и цвет городов по айди
        String[] idState = marinDB.getStringList("marinbay.state");
        String[] idCity = marinDB.getStringList("marinbay.city");
        ArrayList<String> colors = new ArrayList<>();
        for (String id : idState) {
            colors.add(marinDB.getString("marinbay.state." + id + ".color"));
        }

        // Получаем государства
        ArrayList<City> cities = new ArrayList<>();
        for (int i = 0; i < idState.length; i++) {
            if (!marinDB.isSet("marinbay.state." + idState[i] + ".name")) {
                continue;
            }
            cities.add(new City(idState[i], "state"));
        }
        for (int i = 0; i < idCity.length; i++) {
            if (!marinDB.isSet("marinbay.city." + idCity[i] + ".name")) {
                continue;
            }
            cities.add(new City(idCity[i], "city"));
        }
        return cities.toArray(new City[0]);
    }
}
