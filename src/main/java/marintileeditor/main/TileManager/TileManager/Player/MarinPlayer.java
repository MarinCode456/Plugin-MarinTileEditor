package marintileeditor.main.TileManager.TileManager.Player;

import marintileeditor.main.TileManager.TileManager.API.SMassiveAPI;
import marintileeditor.main.TileManager.TileManager.City.City;
import marintileeditor.main.TileManager.TileManager.City.CityMap;
import marintileeditor.main.TileManager.TileManager.Database.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

// Обработано
public class MarinPlayer {

    // Основные переменные
    private String id;
    private String name;

    // Тех. часть
    private Database marinDB = Database.STAT_DATABASE;
    public static Logger log = Bukkit.getLogger();

    public MarinPlayer(Player player) {
        this.name = player.getName();
        this.id = name.toLowerCase();
        this.create();
    }
    public MarinPlayer(String name) {
        this.name = name;
        this.id = name.toLowerCase();
        this.create();
    }

    // Создаём MarinPlayer
    public void create() {
        // Проверяем есть ли такая строка в датабазе
        if(!this.created()) {
            marinDB.write("marinbay.player." + this.id + ".name", name);
            marinDB.write("marinbay.player." + this.id + ".status", "nobody");
        }
    }

    // Интерпритация маринплеера в строку
    public String toString() {
        return name;
    }
    // Проверяет создан ли уже такой игрок
    public boolean created() {
        return marinDB.isSet("marinbay.player." + this.id+ ".name");
    }
    // Проверяет генерал ли этот игрок
    public boolean isGeneral() {
        if (!marinDB.isSet("marinbay.player." + id + ".status")) {
            return false;
        }
        return marinDB.getString("marinbay.player." + id + ".status").equals("general");
    }
    // Проверяет Приближённый ли этот игрок
    public boolean isCloser() {
        if (!marinDB.isSet("marinbay.player." + id + ".status")) {
            return false;
        }
        return marinDB.getString("marinbay.player." + id + ".status").equals("closer");
    }
    // Проверяет губернатор ли этот игрок
    public boolean isGubernator() {
        if (!marinDB.isSet("marinbay.player." + id + ".status")) {
            return false;
        }
        return marinDB.getString("marinbay.player." + id + ".status").equals("guber");
    }

    // Гражданин ли этот игрок указанного государства
    public boolean isCitizenThatState(String stateID) {
        if (this.getStateID() == null) return false;

        if (this.getStateID().equals(stateID)) {
            return true;
        } else {
            return false;
        }
    }

    // Геттеры и сеттеры
    public String getId() {
        return this.id;
    }
    public String getName() {
        return marinDB.getString("marinbay.player." + id + ".name");
    }
    public Player getPlayer() {
        return Bukkit.getPlayer(this.id);
    }
    public String getStateID() {
        if (marinDB.isSet("marinbay.player." + id + ".state")) {
            return marinDB.getString("marinbay.player." + id + ".state");
        }
        return null;
    }
    public String getPlaceCity() { return marinDB.getString("marinbay.player." + id + ".placeCity");}
    public String getRank() {
        return marinDB.getString("marinbay.player." + id + ".status");
    }
    public String getMastersNick() {
        // Получаем ник государя страны, в которой состоит этот игрок
        if (getStateID() == null) return null;
        String generalID = marinDB.getString("marinbay.state." + getStateID() + ".general");
        return marinDB.getString("marinbay.player." + generalID + ".name");
    }

    // Отменяем приглашения в другие города
    public void prepareToJoinCity() {
        this.cancelInvitesFromCities();
    }
    public void cancelInvitesFromCities() {
        String[] invToCities = SMassiveAPI.toMassive(marinDB.getString("marinbay.player." + this.getId() + ".invitesFromCities"));
        CityMap cm = new CityMap();
        for (int i = 0; i < invToCities.length; i++) {
            City city = new City(invToCities[i], "state");
            this.cancelInviteFrom(city);
        }
    }

    // Убираем все инвайты и тд
    public void cancelInviteFrom(City city) {
        marinDB.write("marinbay.player." + this.getId() + ".invitesFromCities", SMassiveAPI.remove(marinDB.getString("marinbay.player." + this.getId() + ".invitesFromCities"), city.getId()));
    }
    // Убираем этого игрока из поля ranks его государства
    public void kickFromRanks() {
        if (getRank() == null) return;
        marinDB.removeOneElement("marinbay.state." + getStateID() + ".ranks." + getRank(), name, ", ");
    }
}
